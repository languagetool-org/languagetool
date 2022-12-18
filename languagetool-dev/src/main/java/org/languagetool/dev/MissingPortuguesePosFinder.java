/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.pt.PortugueseTagger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Portuguese: Fast hack to find words which have no POS tagging (note: does not consider the disambiguator).
 */
public class MissingPortuguesePosFinder {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + MissingPortuguesePosFinder.class.getSimpleName() + " <file> <gaia_file>");
      System.out.println("   <gaia_file> is e.g. pt_br_wordlist.xml from https://github.com/mozilla-b2g/gaia/tree/master/apps/keyboard/js/imes/latin/dictionaries");
      System.exit(1);
    }
    Map<String, Integer> occ = getOccurrences(new File(args[1]));
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    //List<String> lines = Arrays.asList("DC");
    PortugueseTagger tagger = new PortugueseTagger();
    for (String word : lines) {
      int origCount = -1;
      if (word.matches("\\d+ .*")) {
        String[] parts = word.split(" ");
        origCount = Integer.parseInt(parts[0]);
        word = parts[1];
      }
      word = word.trim();
      if (word.endsWith(".")) {
        word = word.substring(0, word.length()-1);
      }
      List<AnalyzedTokenReadings> matches = tagger.tag(Collections.singletonList(word));
      List<AnalyzedTokenReadings> lcMatches = tagger.tag(Collections.singletonList(word.toLowerCase()));
      if (matches.size() == 1 && noTag(matches.get(0)) && lcMatches.size() == 1 && noTag(lcMatches.get(0))) {
        if (occ.containsKey(word)) {
          long count = origCount == -1 ? occ.get(word) : origCount;
          System.out.println(count + "\t" + word);
        }
      }
    }
  }

  private static Map<String,Integer> getOccurrences(File gaiaXmlFile) throws IOException {
    List<String> lines = Files.readAllLines(gaiaXmlFile.toPath());
    Map<String,Integer> map = new HashMap<>();
    Pattern p = Pattern.compile("<w f=\"(\\d+)\" flags=\".*?\">(.*?)</w>");
    for (String line : lines) {
      line = line.trim();
      if (line.startsWith("<w ")) {
        Matcher matcher = p.matcher(line);
        if (matcher.matches()) {
          int occ = Integer.parseInt(matcher.group(1));
          String word = matcher.group(2);
          //System.out.println(occ + " " + word);
          map.put(word, occ);
        } else {
          System.out.println("Skipping line, doesn't match regex: " + line);
        }
      } else {
        System.out.println("Skipping line: " + line);
      }
    }
    return map;
  }

  private static boolean noTag(AnalyzedTokenReadings atr) {
    return !atr.isTagged();
  }
}
