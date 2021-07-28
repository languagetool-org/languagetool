/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (www.danielnaber.de)
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
package org.languagetool;

import org.languagetool.rules.RuleMatch;

import java.util.List;
import java.util.Objects;

/**
 * @since 5.3
 */
public class CheckResults {

  private List<RuleMatch> ruleMatches;
  private List<Range> ignoredRanges;

  public CheckResults(List<RuleMatch> ruleMatches, List<Range> ignoredRanges) {
    this.ruleMatches = Objects.requireNonNull(ruleMatches);
    this.ignoredRanges = Objects.requireNonNull(ignoredRanges);
  }

  public List<Range> getIgnoredRanges() {
    return ignoredRanges;
  }

  public List<RuleMatch> getRuleMatches() {
    return ruleMatches;
  }

  public void setIgnoredRanges(List<Range> ignoredRanges) {
    this.ignoredRanges = Objects.requireNonNull(ignoredRanges);
  }

  public void setRuleMatches(List<RuleMatch> ruleMatches) {
    this.ruleMatches = Objects.requireNonNull(ruleMatches);
  }

}
