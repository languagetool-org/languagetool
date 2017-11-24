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

import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.*;

import java.io.IOException;
import java.util.*;

/**
 * Finds spellings that were only correct in the pre-reform orthography.
 * @since 3.8
 */
public class OldSpellingRule extends Rule {

  private static final String DESC = "Findet Schreibweisen, die nur in der alten Rechtschreibung gültig waren";

  private static final OldSpellingData data = new OldSpellingData(DESC);

  public OldSpellingRule(ResourceBundle messages) throws IOException {
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
    return DESC;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    for (OldSpellingRuleWithSuggestion ruleWithSuggestion : data.get()) {
      Rule rule = ruleWithSuggestion.rule;
      RuleMatch[] matches = rule.match(sentence);
      if (matches.length > 0) {
        RuleMatch match = matches[0];
        String matchedText = sentence.getText().substring(match.getFromPos(), match.getToPos());
        String textFromMatch = sentence.getText().substring(match.getFromPos());
        if (textFromMatch.startsWith("Schloß Holte")) {
          continue;
        }
        String suggestion = matchedText.replace(ruleWithSuggestion.oldSpelling, ruleWithSuggestion.newSpelling);
        if (!suggestion.equals(matchedText)) {   // "Schlüsse" etc. is otherwise considered incorrect (inflected form of "Schluß")
          match.setSuggestedReplacement(suggestion);
          ruleMatches.addAll(Arrays.asList(matches));
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }
  
}
