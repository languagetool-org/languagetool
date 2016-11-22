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

/**
 * Simple hack to filter the result of AutomaticConfusionRuleEvaluator,
 * in case most results have p=1.000.
 */
final class AutomaticConfusionRuleEvaluatorFilter {

  private AutomaticConfusionRuleEvaluatorFilter() {
  }

  private static String reformat(String s) throws IOException {
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
    List<String> lines = Files.readAllLines(Paths.get(args[0]), Charset.forName("utf-8"));
    String prevKey = null;
    int skip = 0;
    boolean skipping = false;
    for (String line : lines) {
      String[] parts = line.split("; ");
      String key = parts[0] + ";" + parts[1];
      if (prevKey != null && key.equals(prevKey)) {
        if (skipping) {
          System.out.println("SKIP: " + reformat(line));
        }
      } else {
        if (line.contains("p=1.000")) {
          System.out.println(reformat(line));
          skipping = false;
        } else {
          System.out.println("SKIP: " + reformat(line));
          skip++;
          skipping = true;
        }
      }
      prevKey = key;
    }
    System.err.println("Skipped: " + skip);
  }
  
}
