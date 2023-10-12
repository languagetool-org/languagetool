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
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultitokenSpellerFilter extends RuleFilter {

  /* Put a multi-token expression inside a single token to find spelling suggestions */
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens) throws IOException {
    boolean keepSpaces = getOptional("keepSpaces", arguments, "true").equalsIgnoreCase("true")? true: false;
    String requireRegexp = getOptional("requireRegexp", arguments);
    String underlinedError = match.getOriginalErrorStr();
    PatternRule pr = (PatternRule) match.getRule();
    SpellingCheckRule spellingRule = pr.getLanguage().getDefaultSpellingRule();
    AnalyzedSentence sentence = new AnalyzedSentence(new AnalyzedTokenReadings[] {
      new AnalyzedTokenReadings(new AnalyzedToken("", "SENT_START", "")),
      new AnalyzedTokenReadings(new AnalyzedToken(underlinedError, null, null))
    });
    RuleMatch[] matches = spellingRule.match(sentence);
    if (matches.length < 1 || matches[0].getSuggestedReplacements().isEmpty()) {
      return null;
    }
    List<String> replacements = new ArrayList<>();
    replacements.addAll(matches[0].getSuggestedReplacements());
    if (keepSpaces) {
      // Only suggestions that keep the number of white spaces
      int numSpaces = numberOfSpaces(underlinedError);
      replacements = replacements.stream()
        .filter(str -> numberOfSpaces(str) == numSpaces).collect(Collectors.toList());
    }
    if (requireRegexp != null) {
      replacements = replacements.stream()
        .filter(str -> str.matches(".*\\b(" + requireRegexp + ")\\b.*")).collect(Collectors.toList());
    }
    replacements = replacements.stream()
      .filter(str -> LevenshteinDistance.getDefaultInstance().apply(str, underlinedError)<6).collect(Collectors.toList());
    if (replacements.isEmpty()) {
      return null;
    }
    match.setSuggestedReplacements(replacements);
    return match;
  }

  private int numberOfSpaces(String s) {
    return s.length() - s.replaceAll(" ", "").length();
  }
}
