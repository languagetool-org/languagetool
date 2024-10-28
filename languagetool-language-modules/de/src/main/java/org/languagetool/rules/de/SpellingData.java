/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.synthesis.GermanSynthesizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.languagetool.tools.StringTools.*;

/**
 * Old to new spelling data and similar formats loaded from CSV.
 * @since 4.3
 */
class SpellingData {

  private final AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<>();
  private final AhoCorasickDoubleArrayTrie<String> sentenceStartTrie = new AhoCorasickDoubleArrayTrie<>();

  SpellingData(String filePath) {
    trie.build(getCoherencyMap(filePath, false));
    sentenceStartTrie.build(getCoherencyMap(filePath, true));
  }

  @NotNull
  private static Map<String, String> getCoherencyMap(String filePath, boolean sentStartMode) {
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(filePath);
    Map<String,String> coherencyMap = new HashMap<>();
    for (String line : lines) {
      if (line.startsWith("#")) {
        continue;
      }
      String[] parts = line.split(";");
      if (parts.length < 2) {
        throw new RuntimeException("Unexpected format in file " + filePath + ": " + line);
      }
      String oldSpelling = parts[0];
      String newSpelling = parts[1];
      sanityChecks(filePath, line, oldSpelling, newSpelling, coherencyMap);
      if (sentStartMode && startsWithLowercase(oldSpelling) && startsWithLowercase(newSpelling)) {
        // lowercase words can be uppercase at sentence start:
        coherencyMap.put(uppercaseFirstChar(oldSpelling), uppercaseFirstChar(newSpelling));
      } else {
        coherencyMap.put(oldSpelling, newSpelling);
      }
      if (oldSpelling.contains("ß") && oldSpelling.replaceAll("ß", "ss").equals(newSpelling)) {
        try {
          String[] forms = GermanSynthesizer.INSTANCE.synthesizeForPosTags(oldSpelling, s -> true);
          for (String form : forms) {
            if (!form.contains("ss")) {  // avoid e.g. "Schlüsse" as form of "Schluß", as that's the new spelling
              coherencyMap.put(form, form.replaceAll("ß", "ss"));
            }
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return coherencyMap;
  }

  private static void sanityChecks(String filePath, String line, String oldSpelling, String newSpelling, Map<String, String> coherencyMap) {
    if (oldSpelling.equals(newSpelling)) {
      throw new RuntimeException("Old and new spelling are the same in " + filePath + ": " + line);
    }
    String lookup = coherencyMap.get(newSpelling);
    if (lookup != null && lookup.equals(oldSpelling)) {
      throw new RuntimeException("Contradictory entry in " + filePath + ": '" + oldSpelling + "' suggests '" + lookup + "' and vice versa");
    }
    if (coherencyMap.containsKey(oldSpelling) && !coherencyMap.get(oldSpelling).equals(newSpelling)) {
      throw new RuntimeException("Duplicate key in " + filePath + ": " + oldSpelling + ", val: " + coherencyMap.get(oldSpelling) + " vs. " + newSpelling);
    }
  }

  public AhoCorasickDoubleArrayTrie<String> getTrie() {
    return trie;
  }

  public AhoCorasickDoubleArrayTrie<String> getSentenceStartTrie() {
    return sentenceStartTrie;
  }
}
