/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.dev.bigdata;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple hack to filter the result of AutomaticConfusionRuleEvaluator.
 */
final class AutomaticConfusionRuleEvaluatorFilter {
  
  private final static float MIN_PRECISION = 0.99f; 
  private final static int MIN_OCCURRENCES = 25; 

  private AutomaticConfusionRuleEvaluatorFilter() {
  }

  private static String reformat(String s) {
    int spaceStart = s.indexOf("0;");
    if (spaceStart == -1) {
      spaceStart = s.indexOf("1;");
    }
    int spaceEnd = s.indexOf('#');
    if (spaceStart > 0 && spaceEnd > 0) {
      String spaces = StringUtils.repeat(" ", 52-spaceStart);
      return s.substring(0, spaceStart) + spaces + s.substring(spaceEnd);
    }
    return s;
  }
  
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + AutomaticConfusionRuleEvaluatorFilter.class.getSimpleName() + " <file>");
      System.out.println("       <file> is the output of " + AutomaticConfusionRuleEvaluator.class.getName());
      System.exit(0);
    }
    List<String> lines = Files.readAllLines(Paths.get(args[0]), Charset.forName("utf-8"));
    String prevKey = null;
    int skippedCount = 0;
    int lowPrecisionCount = 0;
    int lowOccurrenceCount = 0;
    int usedCount = 0;
    boolean skipping = false;
    for (String line : lines) {
      if (!line.startsWith("=>")) {
        continue;
      }
      String[] parts = line.replaceFirst("=> ", "").replaceFirst("; \\d.*", "").split("; ");
      String key = parts[0] + ";" + parts[1];
      Pattern data = Pattern.compile("^(.+?); (.+?);.*p=(\\d\\.\\d+), r=(\\d\\.\\d+), (\\d+)\\+(\\d+),.*");
      Matcher m = data.matcher(line.replaceFirst("=> ", ""));
      m.find();
      String word1 = m.group(1);
      String word2 = m.group(2);
      String wordGroup = word1 + "; " + word2;
      if (word1.compareTo(word2) > 0) {
        wordGroup = word2 + "; " + word1;
      }
      float precision = Float.parseFloat(m.group(3));
      int occ1 = Integer.parseInt(m.group(5));
      int occ2 = Integer.parseInt(m.group(6));
      if (prevKey != null && key.equals(prevKey)) {
        if (skipping) {
          //System.out.println("SKIP: " + reformat(line));
        }
      } else {
        if (precision < MIN_PRECISION) {
          lowPrecisionCount++;
          skippedCount++;
          skipping = true;
          continue;
        }
        if (occ1 < MIN_OCCURRENCES || occ2 < MIN_OCCURRENCES) {
          lowOccurrenceCount++;
          skippedCount++;
          skipping = true;
          continue;
        }
        System.out.println(reformat(line.replaceFirst("=> .+?; .+?; ", wordGroup + "; ")));
        skipping = false;
        usedCount++;
      }
      prevKey = key;
    }
    System.err.println("Skipped: " + skippedCount);
    System.err.println("lowPrecisionCount: " + lowPrecisionCount);
    System.err.println("lowOccurrences: " + lowOccurrenceCount);
    System.err.println("Used: " + usedCount);
  }
  
}
