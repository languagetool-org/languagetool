/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import org.languagetool.rules.RuleMatch;

import java.util.List;

/**
 * A rule match with the application of its corrections.
 */
public class AppliedRuleMatch {

  private final RuleMatch ruleMatch;
  private final List<RuleMatchApplication> ruleMatchApplications;

  public AppliedRuleMatch(RuleMatch ruleMatch, List<RuleMatchApplication> ruleMatchApplications) {
    this.ruleMatch = ruleMatch;
    this.ruleMatchApplications = ruleMatchApplications;
  }

  public RuleMatch getRuleMatch() {
    return ruleMatch;
  }

  public List<RuleMatchApplication> getRuleMatchApplications() {
    return ruleMatchApplications;
  }

  @Override
  public String toString() {
    return ruleMatch + ":" + ruleMatchApplications;
  }
}
