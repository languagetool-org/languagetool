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
 * Loads a confusion set from a plain text file (UTF-8). Expects a file
 * where there is one confusion set per line, words separated by commas.
 * Also optionally loads information about the quality of the confusion 
 * sets from another file so confusion sets that might not produce good
 * results can be ignored.
 * @since 2.7
 */
public class ConfusionSetLoader {

  private static final String CHARSET = "utf-8";

  private final int minSentences;
  private final float maxErrorRate;
  private final Set<String> usefulHomophones;

  public ConfusionSetLoader() {
    this(null, 0, 100);
  }
  
  /**
   * @param minSentences the minimum sentences that each homophone must have been tested with to be considered at all (see homophones-info.txt)
   * @param maxErrorRate the maximum error rate of each homophone to be considered at all (see homophones-info.txt)
   */
  public ConfusionSetLoader(InputStream infoStream, int minSentences, float maxErrorRate) {
    this.minSentences = minSentences;
    this.maxErrorRate = maxErrorRate;
    usefulHomophones = infoStream != null ? getUsefulHomophones(infoStream) : null;
  }

  public Map<String,ConfusionProbabilityRule.ConfusionSet> loadConfusionSet(InputStream stream) throws IOException {
    Map<String,ConfusionProbabilityRule.ConfusionSet> map = new HashMap<>();
    try (
      InputStreamReader reader = new InputStreamReader(stream, CHARSET);
      BufferedReader br = new BufferedReader(reader)
    ) {
      //int homophonesAvailable = 0;
      //int homophonesLoaded = 0;
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }
        String[] words = line.split(",\\s*");
        ConfusionProbabilityRule.ConfusionSet confusionSet = new ConfusionProbabilityRule.ConfusionSet(words);
        for (String word : words) {
          if (usefulHomophones == null || usefulHomophones.contains(word)) {
            map.put(word, confusionSet);
          }
          //homophonesLoaded++;
        }
        //homophonesAvailable += words.length;
      }
      //System.out.println(homophonesLoaded + " of " + homophonesAvailable + " homophones loaded");
    }
    return map;
  }

  private Set<String> getUsefulHomophones(InputStream infoStream) {
    Set<String> result = new HashSet<>();
    try (
      InputStreamReader reader = new InputStreamReader(infoStream, CHARSET);
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#") || line.isEmpty()) {
          continue;
        }
        String[] parts = line.split(";");
        String word = parts[0];
        int sentences = Integer.parseInt(parts[1]);
        float errorRate = Float.parseFloat(parts[3]);
        if (sentences >= minSentences && errorRate <= maxErrorRate) {
          result.add(word);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

}
