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
package org.languagetool.rules.en;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Fixes the suggestion for ordinal suffixes, like "1nd" -&gt; "1st".
 * @since 5.3
 */
public class OrdinalSuffixFilter extends RuleFilter {

  private static final Pattern PATTERN = Pattern.compile(".*(11|12|13)");
  private static final Pattern NON_DIGITS = Pattern.compile("[^0-9]");

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    String ordinal = NON_DIGITS.matcher(match.getSuggestedReplacements().get(0)).replaceAll("");
    if (PATTERN.matcher(ordinal).matches()) {
      match.setSuggestedReplacement(ordinal + "th");
    } else if (ordinal.endsWith("1")) {
      match.setSuggestedReplacement(ordinal + "st");
    } else if (ordinal.endsWith("2")) {
      match.setSuggestedReplacement(ordinal + "nd");
    } else if (ordinal.endsWith("3")) {
      match.setSuggestedReplacement(ordinal + "rd");
    } else {
      match.setSuggestedReplacement(ordinal + "th");
    }
    return match;
  }
}
