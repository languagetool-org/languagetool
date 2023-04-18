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
      if (!wordParts.contains(" ")) {
        throw new RuntimeException("Unexpected format in " + filename + ", expected multi-word (i.e. spaces) left of the '|': " + line);
      }
      String[] words = wordParts.split(" ");
      generateVariants("", Arrays.asList(words), result);
      if (normalizedCompound2message.containsKey(Tools.glueParts(words))) {
        throw new RuntimeException("Duplicate item '" + wordParts + "' in file " + filename);
      }
      String message = "U bedoelt misschien: " + Tools.glueParts(words) + " (" + lineParts[1] + ").";
      normalizedCompound2message.put(Tools.glueParts(words), message);
    }
    Map<String, String> map = new HashMap<>();
    for (String variant : result) {
      map.put(variant, variant);
    }
    AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<>();
    trie.build(map);
    return trie;
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
    return "NL_SPACE_IN_COMPOUND";
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
      if (hit.begin > 0 && !isBoundary(text.substring(hit.begin-1, hit.begin))) {
        // prevent substring matches
        continue;
      }
      if (hit.end < text.length() && !isBoundary(text.substring(hit.end, hit.end+1))) {
        // prevent substring matches
        continue;
      }
      String coveredNoSpaces = Tools.glueParts(covered.split(" "));
      String message = normalizedCompound2message.get(coveredNoSpaces);
      if (message != null) {
        RuleMatch match = new RuleMatch(this, sentence, hit.begin, hit.end, hit.begin, hit.end, message, null, false, "");
        match.setSuggestedReplacement(coveredNoSpaces);
        matches.add(match);
      }
    }
    return toRuleMatchArray(matches);
  }

  private boolean isBoundary(String s) {
    return !s.matches("[a-zA-Z]");
  }
}
