/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Pedro Goulart
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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class BrazilianToponymStateCheckFilter extends RegexRuleFilter {
  private static final BrazilianToponymMap map = new BrazilianToponymMap();
  private static final BrazilianStateInfoMap stateMap = new BrazilianStateInfoMap();

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedSentence sentence, Matcher matcher) {
    // Group #2 isn't used *for now*, but at some point we may want to utilise this rule to also fix the separator
    // in one fell swoop, so let's leave it as a matching group.
    String toponym = matcher.group(1);
    String state = matcher.group(3);

    // If it isn't a city in *any* state, it's prob. not intended as a city, so we don't perform the check.
    if (!map.isValidToponym(toponym)) {
      return null;
    }
    if (map.isToponymInState(toponym, state)) {
      return null;
    }
    BrazilianToponymStateCheckResult checkResult = map.getStatesWithMunicipality(toponym);
    setStateAbbrevSuggestions(match, checkResult);
    setMessage(match, checkResult, stateMap.get(state));
    return match;
  }

  private void setStateAbbrevSuggestions(RuleMatch match, BrazilianToponymStateCheckResult checkResult) {
    match.setSuggestedReplacements(
      checkResult.states.stream()
      .map(stateInfo -> stateInfo.abbreviation)
      .collect(Collectors.toList())
    );
  }

  private void setMessage(RuleMatch match, BrazilianToponymStateCheckResult checkResult, BrazilianStateInfo wrongState) {
    String inTheStateOf = String.format("no estado %s %s",
      new PortuguesePreposition("de").contractWith(wrongState.articles[0]),
      wrongState.name);
    String message = String.format("Se estiver se referindo a um município, não parece haver cidades com o nome de %s %s.",
      checkResult.matchedToponym, inTheStateOf);
    match.setMessage(message);
  }
}
