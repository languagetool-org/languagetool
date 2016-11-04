/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
import java.util.Collections;
import java.util.List;

/**
 * Filter rule matches so that only the first match is kept from overlapping
 * matches with the same rule group (actually: the same id).
 * @since 1.8
 */
public class SameRuleGroupFilter implements RuleMatchFilter {

  /**
   * @param ruleMatches list of matches
   */
  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches) {
    Collections.sort(ruleMatches);
    List<RuleMatch> filteredRules = new ArrayList<>();
    for (int i = 0; i < ruleMatches.size(); i++) {
      RuleMatch match = ruleMatches.get(i);
      while (i < ruleMatches.size() - 1 && overlapAndMatch(match, ruleMatches.get(i + 1))) {
        i++;  // skip next match
      }
      filteredRules.add(match);
    }
    return filteredRules;
  }

  private boolean overlapAndMatch(RuleMatch match, RuleMatch nextMatch) {
    return overlaps(match, nextMatch) && haveSameRuleGroup(match, nextMatch);
  }

  boolean overlaps(RuleMatch match, RuleMatch nextMatch) {
    if (match.getFromPos() <= nextMatch.getToPos() && match.getToPos() >= nextMatch.getFromPos()) {
      return true;
    }
    return false;
  }

  private boolean haveSameRuleGroup(RuleMatch match, RuleMatch nextMatch) {
    String id1 = match.getRule().getId();
    return id1 != null && id1.equals(nextMatch.getRule().getId());
  }

}
