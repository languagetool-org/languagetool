/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.nl;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.util.*;

public class SpaceInCompoundRule extends Rule {

  private static final Map<String, String> normalizedCompound2message = new HashMap<>();
  private static final AhoCorasickDoubleArrayTrie<String> trie = getTrie();

  public SpaceInCompoundRule(ResourceBundle messages) {
    setDefaultTempOff();  // TODO
  }

  private static AhoCorasickDoubleArrayTrie<String> getTrie() {
    Set<String> result = new HashSet<>();
    String filename = "/nl/multipartcompounds.txt";
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(filename);
    for (String line : lines) {
      if (line.startsWith("#")) {
        continue;
      }
      String[] lineParts = line.split("\\|");
      if (lineParts.length != 2) {
        throw new RuntimeException("Unexpected format in " + filename + ", expected 2 columns separated by '|': " + line);
      }
      String wordParts = lineParts[0];
      String message = lineParts[1];
      String[] words = wordParts.split(" ");
      generateVariants("", Arrays.asList(words), result);
      if (normalizedCompound2message.containsKey(removeSpaces(wordParts))) {
        throw new RuntimeException("Duplicate item '" + wordParts + "' in file " + filename);
      }
      normalizedCompound2message.put(removeSpaces(wordParts), message);
    }
    Map<String, String> map = new HashMap<>();
    for (String variant : result) {
      map.put(variant, variant);
    }
    AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<>();
    trie.build(map);
    return trie;
  }

  private static String removeSpaces(String s) {
    return s.replaceAll(" ", "");
  }

  static void generateVariants(String soFar, List<String> l, Set<String> result) {
    if (l.size() == 1) {
      if (soFar.contains(" ")) {
        result.add(soFar + l.get(0));
      }
      result.add(soFar + " " + l.get(0));
    } else {
      List<String> rest = l.subList(1, l.size());
      generateVariants(soFar + l.get(0), rest, result);
      if (!soFar.isEmpty()) {
        generateVariants(soFar + " " + l.get(0), rest, result);
      }
    }
  }

  @Override
  public String getId() {
    return "NL_ADDED_SPACES";
  }

  @Override
  public String getDescription() {
    return "Detecteert spatiefouten";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> matches = new ArrayList<>();
    String text = sentence.getText();
    List<AhoCorasickDoubleArrayTrie.Hit<String>> hits = trie.parseText(text);
    for (AhoCorasickDoubleArrayTrie.Hit<String> hit : hits) {
      String covered = text.substring(hit.begin, hit.end);
      String coveredNoSpaces = removeSpaces(covered);
      String message = normalizedCompound2message.get(coveredNoSpaces);
      RuleMatch match = new RuleMatch(this, sentence, hit.begin, hit.end, hit.begin, hit.end, message, null, false, "");
      match.setSuggestedReplacement(coveredNoSpaces);
      matches.add(match);
    }
    return toRuleMatchArray(matches);
  }
}
