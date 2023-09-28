/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.Map;

public class MultitokenSpellerFilter extends RuleFilter {

  /* Put a multi-token expression inside a single token to find spelling suggestions */
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens) throws IOException {
    PatternRule pr = (PatternRule) match.getRule();
    SpellingCheckRule spellingRule = pr.getLanguage().getDefaultSpellingRule();
    AnalyzedSentence sentence = new AnalyzedSentence(new AnalyzedTokenReadings[] {
      new AnalyzedTokenReadings(new AnalyzedToken("", "SENT_START", "")),
      new AnalyzedTokenReadings(new AnalyzedToken(match.getOriginalErrorStr(), null, null))
    });
    RuleMatch[] matches = spellingRule.match(sentence);
    if (matches.length < 1 || matches[0].getSuggestedReplacements().isEmpty()) {
      return null;
    }
    match.setSuggestedReplacements(matches[0].getSuggestedReplacements());
    return match;
  }
}
