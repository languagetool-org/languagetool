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
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

/**
 * @since 4.3
 */
class SpellingRuleWithSuggestion {

  private final Rule rule;
  private final String alternative;
  private final String suggestion;

  SpellingRuleWithSuggestion(Rule rule, String alternative, String suggestion) {
    this.rule = Objects.requireNonNull(rule);
    this.alternative = Objects.requireNonNull(alternative);
    this.suggestion = Objects.requireNonNull(suggestion);
  }

  static List<RuleMatch> computeMatches(AnalyzedSentence sentence, SpellingData data, String[] exceptions) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    for (SpellingRuleWithSuggestion ruleWithSuggestion : data.get()) {
      Rule rule = ruleWithSuggestion.rule;
      RuleMatch[] matches = rule.match(sentence);
      for (RuleMatch match : matches) {
        String matchedText = sentence.getText().substring(match.getFromPos(), match.getToPos());
        String textFromMatch = sentence.getText().substring(match.getFromPos());
        boolean isException = false;
        for (String exception: exceptions) {
          if (textFromMatch.startsWith(exception)) {
            isException = true;
            break;
          }
        }
        if (isException) {
          continue;
        }
        String suggestion = matchedText.replace(ruleWithSuggestion.alternative, ruleWithSuggestion.suggestion);
        if (!suggestion.equals(matchedText)) {   // "Schlüsse" etc. is otherwise considered incorrect (inflected form of "Schluß")
          match.setSuggestedReplacement(suggestion);
          ruleMatches.add(match);
        }
      }
    }
    return ruleMatches;
  }

}
