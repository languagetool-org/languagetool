/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Jaume Ortola
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
package org.languagetool.rules.ca;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

public class RemoveSuggestionsFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) {
    Pattern regexpPattern = Pattern.compile(getRequired("removeSuggestionsRegexp", arguments), Pattern.UNICODE_CASE);
    List<String> newReplacements = new ArrayList<>();
    for (String replacement : match.getSuggestedReplacements()) {
      if (!regexpPattern.matcher(replacement).matches()) {
        newReplacements.add(replacement);
      }
    }
    if (newReplacements.isEmpty()) {
      return null;
    }
    match.setSuggestedReplacements(newReplacements);
    return match;
  }

}
