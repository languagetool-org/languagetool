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

import org.apache.commons.lang3.StringUtils;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

public class SpaceInCompoundRule extends Rule {

  private static final Map<String, String> normalizedCompound2message = new HashMap<>();
  private static final AhoCorasickDoubleArrayTrie<String> trie = getTrie();

  public SpaceInCompoundRule(ResourceBundle messages) {
    //setDefaultTempOff();  // TODO
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
      String[] words = wordParts.split(" ");
      generateVariants("", Arrays.asList(words), result);
      if (normalizedCompound2message.containsKey(glueParts(wordParts))) {
        throw new RuntimeException("Duplicate item '" + wordParts + "' in file " + filename);
      }
      String message = "U bedoelt misschien: "+glueParts(wordParts)+ " ("+lineParts[1]+").";
      normalizedCompound2message.put(glueParts(wordParts), message);
    }
    Map<String, String> map = new HashMap<>();
    for (String variant : result) {
      map.put(variant, variant);
    }
    AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<>();
    trie.build(map);
    return trie;
  }

  private static String glueParts(String s) {
    String spelledWords = "(abc|adv|aed|apk|b2b|bh|bhv|bso|btw|bv|cao|cd|cfk|ckv|cv|dc|dj|dtp|dvd|fte|gft|ggo|ggz|gm|gmo|gps|gsm|hbo|" +
            "hd|hiv|hr|hrm|hst|ic|ivf|kmo|lcd|lp|lpg|lsd|mbo|mdf|mkb|mms|msn|mt|ngo|nv|ob|ov|ozb|p2p|pc|pcb|pdf|pk|pps|" +
            "pr|pvc|roc|rvs|sms|tbc|tbs|tl|tv|uv|vbo|vj|vmbo|vsbo|vwo|wc|wo|xtc|zzp)";
    //System.out.print("******"+s+"****** => ");
    String[] parts = s.split(" ");
    String compound = parts[0];
    for (int i = 1; i < parts.length; i++) {
      String word2 = parts[i];
      char lastChar = compound.charAt(compound.length() - 1);
      char firstChar = word2.charAt(0);
      String connection = lastChar + String.valueOf(firstChar);
      if (StringUtils.containsAny(connection, "aa", "ae", "ai", "ao", "au", "ee", "ei", "eu", "ie", "ii", "oe", "oi", "oo", "ou", "ui", "uu", "ij")) {
        compound = compound + '-' + word2;
      } else if (isUpperCase(firstChar) && isLowerCase(lastChar)) {
        compound = compound + '-' + word2;
      } else if (isUpperCase(lastChar) && isLowerCase(firstChar)) {
        compound = compound + '-' + word2;
      } else if (compound.matches("(^|.+-)?" + spelledWords) || word2.matches(spelledWords + "(-.+|$)?")) {
        compound = compound + '-' + word2;
      } else if (compound.matches(".+-[a-z]$") || word2.matches("^[a-z]-.+")) {
        compound = compound + '-' + word2;
      } else {
        compound = compound + word2;
      }
    }
    //System.out.println("******"+compound+"******");
    return compound;
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
      String coveredNoSpaces = glueParts(covered);
      String message = normalizedCompound2message.get(coveredNoSpaces);
      RuleMatch match = new RuleMatch(this, sentence, hit.begin, hit.end, hit.begin, hit.end, message, null, false, "");
      match.setSuggestedReplacement(coveredNoSpaces);
      matches.add(match);
    }
    return toRuleMatchArray(matches);
  }
}
