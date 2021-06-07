/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Marcin Miłkowski (http://www.languagetool.org)
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

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Tag;

import java.io.IOException;
import java.util.*;

/**
 * Another use of the compounds file -- check for compounds written with
 * dashes instead of hyphens (for example, Rabka — Zdrój).
 * @since 3.8
 */
public abstract class AbstractDashRule extends Rule {

  public AbstractDashRule(ResourceBundle messages) {
    super(messages);
    setCategory(Categories.TYPOGRAPHY.getCategory(messages));
    setTags(Collections.singletonList(Tag.picky));
  }

  @Override
  public String getId() {
    return "DASH_RULE";
  }

  @Override
  public abstract String getDescription();

  public abstract String getMessage();

  protected abstract AhoCorasickDoubleArrayTrie<String> getCompoundsData();

  @Override
  public int estimateContextForSureMatch() {
    return 2;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    String text = sentence.getText();
    List<AhoCorasickDoubleArrayTrie.Hit<String>> hits = getCompoundsData().parseText(text);
    Set<Integer> startPositions = new HashSet<>();
    Collections.reverse(hits);  // work on longest matches first
    for (AhoCorasickDoubleArrayTrie.Hit<String> hit : hits) {
      if (startPositions.contains(hit.begin)) {
        continue;   // avoid overlapping matches
      }
      if (hit.begin > 0 && !isBoundary(text.substring(hit.begin-1, hit.begin))) {
        // prevent substring matches
        continue;
      }
      if (hit.end < text.length() && !isBoundary(text.substring(hit.end, hit.end+1))) {
        // prevent substring matches, e.g. "Foto" for "Photons"
        continue;
      }
      RuleMatch match = new RuleMatch(this, sentence, hit.begin, hit.end, hit.begin, hit.end,
              getMessage(), null, false, "");
      String covered = text.substring(hit.begin, hit.end);
      match.setSuggestedReplacement(covered.replaceAll(" ?[–—] ?", "-"));
      matches.add(match);
      startPositions.add(hit.begin);
    }
    return matches.toArray(new RuleMatch[0]);
  }

  protected boolean isBoundary(String s) {
    return !s.matches("[a-zA-Z]");
  }

  protected static AhoCorasickDoubleArrayTrie<String> loadCompoundFile(String path) {
    List<String> words = new ArrayList<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      if (line.isEmpty() || line.charAt(0) == '#') {
        continue;     // ignore comments
      }
      if (line.endsWith("+") || line.endsWith("?")) {
        continue; // skip non-hyphenated suggestions
      } else if (line.endsWith("*") || line.endsWith("$")) {
        line = removeLastCharacter(line);
      }
      words.add(line);
    }
    Map<String,String> map = new HashMap<>();
    for (String word : words) {
      String variant1 = word.replace("-", "–");
      map.put(variant1, variant1);
      String variant2 = word.replace("-", "—");
      map.put(variant2, variant2);
      String variant3 = word.replace("-", " – ");
      map.put(variant3, variant3);
      String variant4 = word.replace("-", " — ");
      map.put(variant4, variant4);
    }
    AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<>();
    trie.build(map);
    return trie;
  }

  private static String removeLastCharacter(String str) {
    return str.substring(0, str.length() - 1);
  }

}
