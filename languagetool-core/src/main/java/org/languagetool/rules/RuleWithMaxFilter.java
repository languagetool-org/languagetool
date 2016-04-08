/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Marcin Miłkowski (http://www.languagetool.org)
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

import org.languagetool.rules.patterns.AbstractPatternRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Filter rule matches so that only the longest match is kept from overlapping
 * matches with the same rule that contains tokens with maxOccurrence &gt; 1.
 *
 * @since 2.6
 */
public class RuleWithMaxFilter implements RuleMatchFilter {

  @Override
  public final List<RuleMatch> filter(List<RuleMatch> ruleMatches) {
    Collections.sort(ruleMatches);
    List<RuleMatch> filteredRules = new ArrayList<>();
    for (int i = 0; i < ruleMatches.size(); i++) {
      RuleMatch match = ruleMatches.get(i);
      if (i < ruleMatches.size() - 1) {
        RuleMatch nextMatch = ruleMatches.get(i + 1);
        while (includes(match, nextMatch) && haveSameRule(match, nextMatch)
            && i < ruleMatches.size()) {
          i++;  // skip next match
          if (i < ruleMatches.size() - 1) {
            nextMatch = ruleMatches.get(i + 1);
          }
        }
      }
      filteredRules.add(match);
    }
    return filteredRules;
  }

  final boolean includes(RuleMatch match, RuleMatch nextMatch) {
    if (match.getFromPos() <= nextMatch.getFromPos() && match.getToPos() >= nextMatch.getToPos()) {
      return true;
    }
    return false;
  }

  private boolean haveSameRule(RuleMatch match, RuleMatch nextMatch) {
    if (!(match.getRule() instanceof AbstractPatternRule) || !(nextMatch.getRule() instanceof AbstractPatternRule)) {
      return false;
    }
    String id1 = match.getRule().getId();
    String subId1 = ((AbstractPatternRule) match.getRule()).getSubId();
    String subId2 = ((AbstractPatternRule) nextMatch.getRule()).getSubId();
    if (subId1 == null &&  subId2 != null) {
      return false;
    }
    if (subId1 != null && subId2 == null) {
      return false;
    }
    return id1 != null && id1.equals(nextMatch.getRule().getId()) &&
        (subId1 == null && subId2 == null || subId1 != null && subId1.equals(subId2));
  }

}
