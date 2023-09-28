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

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens) throws IOException {
    PatternRule pr = (PatternRule) match.getRule();
    SpellingCheckRule spellingRule = pr.getLanguage().getDefaultSpellingRule();
    String originalStr = match.getOriginalErrorStr();
    AnalyzedTokenReadings[] atrsArray = new AnalyzedTokenReadings[2];
    AnalyzedTokenReadings atrs0 = new AnalyzedTokenReadings(new AnalyzedToken("", "SENT_START", ""));
    AnalyzedTokenReadings atrs1 = new AnalyzedTokenReadings(new AnalyzedToken(originalStr, null, null));
    atrsArray[0] = atrs0;
    atrsArray[1] = atrs1;
    AnalyzedSentence sentence = new AnalyzedSentence(atrsArray);
    RuleMatch[] matches = spellingRule.match(sentence);
    if (matches.length < 1 || matches[0].getSuggestedReplacements().isEmpty()) {
      return null;
    }
    match.setSuggestedReplacements(matches[0].getSuggestedReplacements());
    return match;
  }
}
