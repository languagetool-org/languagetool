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

import java.util.Objects;

class RuleMatchDiff {

  private final Status status;
  private final LightRuleMatch oldMatch;
  private final LightRuleMatch newMatch;

  enum Status {
    ADDED, REMOVED, MODIFIED
  }

  static RuleMatchDiff added(LightRuleMatch newMatch) {
    return new RuleMatchDiff(Status.ADDED, null, newMatch);
  }
  
  static RuleMatchDiff removed(LightRuleMatch oldMatch) {
    return new RuleMatchDiff(Status.REMOVED, oldMatch, null);
  }
  
  static RuleMatchDiff modified(LightRuleMatch oldMatch, LightRuleMatch newMatch) {
    return new RuleMatchDiff(Status.MODIFIED, oldMatch, newMatch);
  }
  
  private RuleMatchDiff(Status status, LightRuleMatch oldMatch, LightRuleMatch newMatch) {
    this.status = Objects.requireNonNull(status);
    this.oldMatch = oldMatch;
    this.newMatch = newMatch;
  }

  Status getStatus() {
    return status;
  }

  LightRuleMatch getOldMatch() {
    return oldMatch;
  }

  LightRuleMatch getNewMatch() {
    return newMatch;
  }
  
  @Override
  public String toString() {
    return status +
      ": oldMatch=" + oldMatch +
      ", newMatch=" + newMatch;
  }
}
