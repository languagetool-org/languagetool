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

import com.google.common.base.Suppliers;
import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.*;

import java.util.*;
import java.util.function.Supplier;

/**
 * Finds spellings that were only correct in the pre-reform orthography.
 * @since 3.8
 */
public class OldSpellingRule extends Rule {

  private static final String FILE_PATH = "/de/alt_neu.csv";
  private static final List<String> exceptions = Arrays.asList("Schloß Holte");
  private static final Supplier<SpellingData> DATA = Suppliers.memoize(() -> new SpellingData(FILE_PATH));

  public OldSpellingRule(ResourceBundle messages) {
    super.setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("Der <marker>Abfluß</marker> ist schon wieder verstopft."),
                   Example.fixed("Der <marker>Abfluss</marker> ist schon wieder verstopft."));
  }

  @Override
  public String getId() {
    return "OLD_SPELLING";
  }

  @Override
  public String getDescription() {
    return "Findet Schreibweisen, die nur in der alten Rechtschreibung gültig waren";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> matches = new ArrayList<>();
    String text = sentence.getText();
    List<AhoCorasickDoubleArrayTrie.Hit<String>> hits = DATA.get().getTrie().parseText(text);
    Set<Integer> startPositions = new HashSet<>();
    Collections.reverse(hits);  // work on longest matches first
    for (AhoCorasickDoubleArrayTrie.Hit<String> hit : hits) {
      if (startPositions.contains(hit.begin)) {
        continue;   // avoid overlapping matches
      }
      boolean ignore = false;
      for (String exception : exceptions) {
        if (hit.begin + exception.length() <= text.length()) {
          String excCovered = text.substring(hit.begin, hit.begin + exception.length());
          if (excCovered.equals(exception)) {
            ignore = true;
            break;
          }
        }
      }
      if (hit.begin > 0 && !isBoundary(text.substring(hit.begin-1, hit.begin))) {
        // prevent substring matches
        ignore = true;
      }
      if (hit.end < text.length() && !isBoundary(text.substring(hit.end, hit.end+1))) {
        // prevent substring matches, e.g. "Foto" for "Photons"
        ignore = true;
      }
      if (hit.begin-5 >= 0) {
        String before = text.substring(hit.begin-5, hit.begin-1);
        if (before.equals("Herr") || before.equals("Frau")) {
          ignore = true;
        }
      }
      if (!ignore) {
        RuleMatch match = new RuleMatch(this, sentence, hit.begin, hit.end,
          "Diese Schreibweise war nur in der alten Rechtschreibung korrekt.", "Alte Rechtschreibung");
        String[] suggestions = hit.value.split("\\|");
        match.setSuggestedReplacements(Arrays.asList(suggestions));
        matches.add(match);
        startPositions.add(hit.begin);
      }
    }
    return toRuleMatchArray(matches);
  }

  private boolean isBoundary(String s) {
    return !s.matches("[a-zA-Zöäüß]");
  }
  
}
