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
package org.languagetool.rules.patterns;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link PatternRule}'s id with an optional sub-id.
 */
public final class PatternRuleId {

  private final String id;
  private final String subId;

  /**
   * @param id the rule id
   */
  public PatternRuleId(String id) {
    Validate.notEmpty(id, "id must be set");
    this.id = id;
    this.subId = null;
  }

  /**
   * @param id the rule id
   * @param subId the sub id of a rulegroup, starting at {@code 1}
   */
  public PatternRuleId(String id, String subId) {
    Validate.notEmpty(id, "id must be set");
    Validate.notEmpty(subId, "subId must be set, if specified");
    this.id = id;
    this.subId = subId;
  }

  public String getId() {
    return id;
  }

  /**
   * @return a sub id or {@code null} if no sub id has been set
   */
  @Nullable
  public String getSubId() {
    return subId;
  }

  @Override
  public String toString() {
    if (subId != null) {
      return id + "[" + subId + "]";
    } else {
      return id;
    }
  }

}
