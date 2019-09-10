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

import java.io.*;
import java.util.*;

/**
 * Loads a confusion set from a plain text file (UTF-8). See {@code confusion_sets.txt}
 * for a description of the file format.
 * @since 2.7
 */
public class ConfusionSetLoader {

  private static final String CHARSET = "utf-8";

  public ConfusionSetLoader() {
  }

  public Map<String,List<ConfusionPair>> loadConfusionPairs(InputStream stream) throws IOException {
    Map<String,List<ConfusionPair>> map = new HashMap<>();
    try (
      InputStreamReader reader = new InputStreamReader(stream, CHARSET);
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#") || line.trim().isEmpty()) {
          continue;
        }
        String[] parts = line.replaceFirst("\\s*#.*", "").split("\\s*(;|->)\\s*");
        if (parts.length != 3) {
          throw new RuntimeException("Unexpected format: '" + line + "' - expected three semicolon-separated values: word1; word2; factor");
        }
        boolean bidirectional = !line.replaceFirst("#.*", "").contains(" -> ");
        List<ConfusionString> confusionStrings = new ArrayList<>();
        Set<String> loadedForSet = new HashSet<>();
        String prevWord = null;
        for (String part : Arrays.asList(parts).subList(0, parts.length - 1)) {
          String[] subParts = part.split("\\|");
          String word = subParts[0];
          if (bidirectional && prevWord != null && word.compareTo(prevWord) < 0) {
            // Quick hack for reordering lines
            //System.err.println("Delete: " + line);
            //String comment = line.substring(line.indexOf("#"));
            //String newLine = parts[1] + "; " + parts[0] + "; " + parts[2] + "; " + comment;
            //System.err.println("Add: " + newLine);
            throw new RuntimeException("Order words alphabetically per line in the confusion set file: " + line + ": found " + word + " after " + prevWord);
          }
          prevWord = word;
          String description = subParts.length == 2 ? subParts[1] : null;
          if (loadedForSet.contains(word)) {
            throw new RuntimeException("Word appears twice in same confusion set: '" + word + "'");
          }
          confusionStrings.add(new ConfusionString(word, description));
          loadedForSet.add(word);
        }
        ConfusionPair confusionSet = new ConfusionPair(confusionStrings.get(0), confusionStrings.get(1), Long.parseLong(parts[parts.length-1]), bidirectional);
        for (ConfusionString confusionString : confusionStrings) {
          String key = confusionString.getString();
          List<ConfusionPair> existingEntry = map.get(key);
          if (existingEntry != null) {
            existingEntry.add(confusionSet);
          } else {
            List<ConfusionPair> pairs = new ArrayList<>();
            pairs.add(confusionSet);
            map.put(key, pairs);
          }
        }
      }
    }
    return map;
  }

}
