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
import java.util.Collections;
import java.util.List;

import java.util.Objects;

/**
 * @since 4.3
 */
class SpellingRuleWithSuggestions {

  private final Rule rule;
  private final String alternative;
  private final List<String> suggestions;
  private final boolean skipIfAfterQuote;

  SpellingRuleWithSuggestions(Rule rule, String alternative, String suggestion) {
    this(rule, alternative, Collections.singletonList(suggestion));
  }

  /**
   * @since 4.6
   */
  SpellingRuleWithSuggestions(Rule rule, String alternative, List<String> suggestions) {
    this(rule, alternative, suggestions, false);
  }

  /**
   * @since 4.6
   */
  SpellingRuleWithSuggestions(Rule rule, String alternative, List<String> suggestions, boolean skipIfAfterQuote) {
    this.rule = Objects.requireNonNull(rule);
    this.alternative = Objects.requireNonNull(alternative);
    this.suggestions = Objects.requireNonNull(suggestions);
    this.skipIfAfterQuote = skipIfAfterQuote;
  }

  static List<RuleMatch> computeMatches(AnalyzedSentence sentence, SpellingData data, String[] exceptions) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    for (SpellingRuleWithSuggestions ruleWithSuggestion : data.get()) {
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
        List<String> suggestions = new ArrayList<>();
        for (String s : ruleWithSuggestion.suggestions) {
          String suggestion = matchedText.replace(ruleWithSuggestion.alternative, s);
          if (!suggestion.equals(matchedText)) {   // "Schlüsse" etc. is otherwise considered incorrect (inflected form of "Schluß")
            suggestions.add(suggestion);
          }
        }
        if (suggestions.size() > 0) {
          match.setSuggestedReplacements(suggestions);
          if (ruleWithSuggestion.skipIfAfterQuote && match.getFromPos() > 0 && 
              sentence.getText().substring(match.getFromPos()-1, match.getFromPos()).matches("['\"„«»]")) {
            continue;
          }
          ruleMatches.add(match);
        }
      }
    }
    return ruleMatches;
  }

}
