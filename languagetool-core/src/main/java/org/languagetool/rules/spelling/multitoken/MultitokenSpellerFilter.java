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
import org.languagetool.tools.StringTools;

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
    if (discardRunOnWords(underlinedError, spellingRule)) {
      return null;
    }
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
    int numSpaces = numberOf(underlinedError, " ");
    int numHyphens = numberOf(underlinedError, "-");
    if (keepSpaces) {
      // Only suggestions that keep the number of white spaces
      replacements = replacements.stream()
        .filter(str -> numberOf(str, " ") == numSpaces).collect(Collectors.toList());
    }
    int decreaseDistance = 0;
    if (requireRegexp != null) {
      replacements = replacements.stream()
        .filter(str -> str.matches(".*\\b(" + requireRegexp + ")\\b.*")).collect(Collectors.toList());
      decreaseDistance = 2;
    }
    int maxLevenshteinDistance = 2 + (numSpaces + numHyphens) * 2 - decreaseDistance;
    replacements = replacements.stream()
      .filter(str -> ponderatedDistance(str, underlinedError) < maxLevenshteinDistance).collect(Collectors.toList());
    if (replacements.isEmpty()) {
      return null;
    }
    match.setSuggestedReplacements(replacements);
    return match;
  }

  private boolean discardRunOnWords(String underlinedError, SpellingCheckRule spellingRule) throws IOException {
    String parts[] = underlinedError.split(" ");
    if (parts.length == 2) {
      String sugg1a = parts[0].substring(0, parts[0].length() - 1);
      String sugg1b = parts[0].substring(parts[0].length() - 1) + parts[1];
      if (!spellingRule.isMisspelled(sugg1a) && !spellingRule.isMisspelled(sugg1b)) {
        return true;
      }
      String sugg2a = parts[0].substring(0, parts[0].length() - 1);
      String sugg2b = parts[0].substring(parts[0].length() - 1) + parts[1];
      if (!spellingRule.isMisspelled(sugg2a) && !spellingRule.isMisspelled(sugg2b)) {
        return true;
      }
    }
    return false;
  }

  private int numberOf(String s, String t) {
    return s.length() - s.replaceAll(t, "").length();
  }

  private int ponderatedDistance (String s1, String s2) {
    int distance = levenshteinDistance(s1, s2);
    String parts1[] = s1.split(" ");
    String parts2[] = s2.split(" ");
    if (parts1.length == parts2.length && parts1.length > 1) {
      for (int i=0; i<parts1.length; i++) {
        if (levenshteinDistance(parts1[i], parts2[i]) == 0) {
          distance++;
        }
      }
    }
    return distance;
  }

  private int levenshteinDistance(String s1, String s2) {
    return LevenshteinDistance.getDefaultInstance().apply(
      StringTools.removeDiacritics(s1.toLowerCase()),
      StringTools.removeDiacritics(s2.toLowerCase()));
  }
}
