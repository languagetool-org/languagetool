/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Jaume Ortolà i Font
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
package org.languagetool.rules.pt;

import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RegexRuleFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class BrazilianToponymFilter extends RegexRuleFilter {
  private static final BrazilianToponymMap map = new BrazilianToponymMap();

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedSentence sentence, Matcher matcher) {
    String toponym = matcher.group(1);
    String underlined = matcher.group(2);
    String state = matcher.group(3);

    // TODO: read this from user options or something...
    String suggestion = "–" + state;
    if (suggestion.equals(underlined)) {
      return null;
    }
    // If it isn't a city in *any* state, it's prob. not intended as a city, so we don't perform the check.
    if (!map.isValidToponym(toponym)) {
      return null;
    }
    match.setSuggestedReplacement(suggestion);
    return match;
  }
}
