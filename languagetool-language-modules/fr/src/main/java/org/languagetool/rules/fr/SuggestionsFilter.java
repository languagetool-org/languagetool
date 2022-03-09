/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà
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
package org.languagetool.rules.fr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

/*
 * Remove suggestions that match a regular expression
 */

public class SuggestionsFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    String removeSuggestionsRegexp = getRequired("RemoveSuggestionsRegexp", arguments);
    RuleMatch ruleMatch = match;
    List<String> replacements = match.getSuggestedReplacements();
    List<String> newReplacements = new ArrayList<>();
    Pattern p = Pattern.compile(removeSuggestionsRegexp, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    for (String r : replacements) {
      if (!p.matcher(r).matches()) {
        newReplacements.add(r);
      }
    }
    ruleMatch.setSuggestedReplacements(newReplacements);
    return ruleMatch;
  }

}
