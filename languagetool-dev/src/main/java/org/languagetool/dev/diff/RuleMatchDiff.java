/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.diff;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

class RuleMatchDiff {

  private final Status status;
  private final LightRuleMatch oldMatch;
  private final LightRuleMatch newMatch;
  private final LightRuleMatch replacedBy;  // the added match that a removed match (maybe) was replaced by
  private LightRuleMatch replaces;    // the removed match that this match (maybe) replaces

  enum Status {
    ADDED, REMOVED, MODIFIED
  }

  static RuleMatchDiff added(LightRuleMatch newMatch) {
    return new RuleMatchDiff(Status.ADDED, null, newMatch, null);
  }
  
  static RuleMatchDiff removed(LightRuleMatch oldMatch) {
    return new RuleMatchDiff(Status.REMOVED, oldMatch, null, null);
  }
  
  static RuleMatchDiff removed(LightRuleMatch oldMatch, LightRuleMatch replacedBy) {
    return new RuleMatchDiff(Status.REMOVED, oldMatch, null, replacedBy);
  }

  static RuleMatchDiff modified(LightRuleMatch oldMatch, LightRuleMatch newMatch) {
    return new RuleMatchDiff(Status.MODIFIED, oldMatch, newMatch, null);
  }
  
  private RuleMatchDiff(Status status, LightRuleMatch oldMatch, LightRuleMatch newMatch, LightRuleMatch replacedBy) {
    this.status = Objects.requireNonNull(status);
    this.oldMatch = oldMatch;
    this.newMatch = newMatch;
    this.replacedBy = replacedBy;
  }

  Status getStatus() {
    return status;
  }

  String getMarkedText() {
    return newMatch == null ? oldMatch.getCoveredText() : newMatch.getCoveredText();
  }

  @Nullable
  LightRuleMatch getOldMatch() {
    return oldMatch;
  }

  @Nullable
  LightRuleMatch getNewMatch() {
    return newMatch;
  }

  LightRuleMatch getReplacedBy() {
    return replacedBy;
  }

  void setReplaces(LightRuleMatch oldMatch) {
    replaces = oldMatch;
  }

  LightRuleMatch getReplaces() {
    return replaces;
  }

  @Override
  public String toString() {
    return status +
      ": oldMatch=" + oldMatch +
      ", newMatch=" + newMatch;
  }
}
