/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Loads a confusion set from a plain text file (UTF-8). See {@code neuralnetwork_confusion_sets.txt}
 * for a description of the file format.
 */
public class ScoredConfusionSetLoader {

  private static final String CHARSET = "utf-8";

  public ScoredConfusionSetLoader() {
  }

  public Map<String,List<ScoredConfusionSet>> loadConfusionSet(InputStream stream) throws IOException {
    Map<String,List<ScoredConfusionSet>> map = new HashMap<>();
    try (
      InputStreamReader reader = new InputStreamReader(stream, CHARSET);
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#") || line.trim().isEmpty()) {
          continue;
        }
        String[] parts = line.replaceFirst("\\s*#.*", "").split(";\\s*");
        if (parts.length != 3) {
          throw new RuntimeException("Unexpected format: '" + line + "' - expected three semicolon-separated values: word1; word2; factor");
        }
        List<ConfusionString> confusionStrings = new ArrayList<>();
        Set<String> loadedForSet = new HashSet<>();
        String prevWord = null;
        for (String part : Arrays.asList(parts).subList(0, parts.length-1)) {
          String[] subParts = part.split("\\|");
          String word = subParts[0];
          if (prevWord != null && word.compareTo(prevWord) < 0) {
            throw new RuntimeException("Order words alphabetically per line in the confusion set file: " + line);
          }
          prevWord = word;
          String description = subParts.length == 2 ? subParts[1] : null;
          if (loadedForSet.contains(word)) {
            throw new RuntimeException("Word appears twice in same confusion set: '" + word + "'");
          }
          confusionStrings.add(new ConfusionString(word, description));
          loadedForSet.add(word);
        }
        ScoredConfusionSet confusionSet = new ScoredConfusionSet(Float.parseFloat(parts[parts.length-1]), confusionStrings);
        for (ConfusionString confusionString : confusionStrings) {
          String key = confusionString.getString();
          List<ScoredConfusionSet> existingEntry = map.get(key);
          if (existingEntry != null) {
            existingEntry.add(confusionSet);
          } else {
            List<ScoredConfusionSet> sets = new ArrayList<>();
            sets.add(confusionSet);
            map.put(key, sets);
          }
        }
      }
    }
    return map;
  }

}
