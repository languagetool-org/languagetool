/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Jaume Ortolà (http://www.languagetool.org)
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.languagetool.Language;
import org.languagetool.rules.patterns.RuleSet;

public class RepetitionMatchFilter implements RuleMatchFilter {

  protected boolean toBeChecked;
  protected int distance; // numer of tokens

  public RepetitionMatchFilter(Language lang, RuleSet rules) {
    toBeChecked = lang.hasMinMatchesRules();
    distance = 350; // characters
  }

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches) {
    if (toBeChecked) {
      List<RuleMatch> newRuleMatches = new ArrayList<>();
      Map<String, Integer> mapRulesPositions = new HashMap<>();
      Map<String, Integer> mapRulesPrevMatches = new HashMap<>();
      for (RuleMatch rm : ruleMatches) {
        int minPrevMatches = rm.getRule().getMinPrevMatches();
        boolean ignoreRule = false;
        if (minPrevMatches > 0) {
          String ruleId = rm.getRule().getId();
          int pos = rm.getFromPos();
          if (mapRulesPositions.containsKey(ruleId)) {
            int lastSeenPos = mapRulesPositions.get(ruleId);
            int prevMatches = mapRulesPrevMatches.get(ruleId);
            ignoreRule = pos - lastSeenPos > distance || prevMatches < minPrevMatches;
            mapRulesPositions.put(ruleId, pos);
            mapRulesPrevMatches.put(ruleId, prevMatches + 1);
          } else {
            mapRulesPositions.put(ruleId, pos);
            mapRulesPrevMatches.put(ruleId, 1);
            ignoreRule = true;
          }
        }
        if (!ignoreRule) {
          newRuleMatches.add(rm);
        }
      }
      return newRuleMatches;
    }
    return ruleMatches;
  }
}