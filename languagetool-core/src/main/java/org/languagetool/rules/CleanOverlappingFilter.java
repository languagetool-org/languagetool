/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Jaume Ortolà (http://www.languagetool.org)
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

import org.languagetool.Language;

import java.util.ArrayList;
import java.util.List;

/* 
 * Remove overlapping errors according to the priorities established for the language.  
 * (It assumes the input list is ordered by start position)
 * 
 * @since 3.6
 */
public class CleanOverlappingFilter implements RuleMatchFilter {

  private Language language;
  
  public CleanOverlappingFilter(Language lang) {
    this.language = lang;
  }
  
  @Override
  public final List<RuleMatch> filter(List<RuleMatch> ruleMatches) {
    List<RuleMatch> cleanList = new ArrayList<>();
    RuleMatch prevRuleMatch = null;
    for(RuleMatch ruleMatch: ruleMatches) {
      // first item
      if (prevRuleMatch == null) {
        prevRuleMatch = ruleMatch;
        continue;
      }
      // check sorting
      if (ruleMatch.getFromPos() < prevRuleMatch.getFromPos()) {
        throw new IllegalArgumentException(
            "The list of rule matches is not ordered. Make sure it is sorted by start position.");
      }
      // no overlapping (juxtaposed errors are not removed)
      if (ruleMatch.getFromPos() >= prevRuleMatch.getToPos()) {
        cleanList.add(prevRuleMatch);
        prevRuleMatch = ruleMatch;
        continue;
      }
      //overlapping
      // get priorities
      int currentPriority = getMatchPriority(ruleMatch);
      int prevPriority = getMatchPriority(prevRuleMatch);
      // break the tie
      if (currentPriority == prevPriority) {
        // take the longest error
        currentPriority = ruleMatch.getToPos() - ruleMatch.getFromPos();
        prevPriority = prevRuleMatch.getToPos() - prevRuleMatch.getFromPos();
      }
      if (currentPriority == prevPriority) {
        currentPriority++; // take the last one (to keep the current results in the web UI) 
      }
      // compare
      if (currentPriority > prevPriority ) {
        //skip prevRuleMatch
        prevRuleMatch = ruleMatch;
      } //else skip current RuleMatch;
    }
    //last match
    if (prevRuleMatch != null) {
      cleanList.add(prevRuleMatch);
    }
    return cleanList;
  }
  
  private int getMatchPriority(RuleMatch r) {
    if (r.getRule().getCategory().getId() == null) {
      return 0;
    }
    int categoryPriority = language.getPriorityForId(r.getRule().getCategory().getId().toString());
    int rulePriority = language.getPriorityForId(r.getRule().getId());
    // if there is a priority defined for rule it takes precedence over category priority
    if (rulePriority != 0) {
      return rulePriority;
    } else {
      return categoryPriority;
    }
  }
  
}