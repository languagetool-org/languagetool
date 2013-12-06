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
package org.languagetool.dev.wikipedia.atom;

import java.util.Date;
import java.util.Objects;

/**
 * A rule match retrieved from the database.
 * @since 2.4
 */
class StoredWikipediaRuleMatch {

  private final String ruleId;
  private final String ruleSubId;
  private final String ruleDescription;
  private final String ruleMessage;
  private final String errorContext;
  private final String title;
  private final Date editDate;
  private final Date fixDate;
  private final long diffId;
  private final long fixDiffId;
  
  StoredWikipediaRuleMatch(String ruleId, String ruleSubId, String ruleDescription, String ruleMessage, String errorContext, String title, Date editDate, Date fixDate, long diffId, long fixDiffId) {
    this.ruleId = Objects.requireNonNull(ruleId);
    this.ruleSubId = ruleSubId;
    this.ruleDescription = Objects.requireNonNull(ruleDescription);
    this.ruleMessage = Objects.requireNonNull(ruleMessage);
    this.errorContext = Objects.requireNonNull(errorContext);
    this.title = Objects.requireNonNull(title);
    this.editDate = Objects.requireNonNull(editDate);
    this.fixDate = fixDate;
    this.diffId = diffId;
    this.fixDiffId = fixDiffId;
  }

  String getRuleId() {
    return ruleId;
  }

  /** May be null. */
  String getRuleSubId() {
    return ruleSubId;
  }

  String getRuleDescription() {
    return ruleDescription;
  }

  String getRuleMessage() {
    return ruleMessage;
  }

  String getErrorContext() {
    return errorContext;
  }

  String getTitle() {
    return title;
  }

  Date getEditDate() {
    return editDate;
  }

  /** May be null (if error is not fixed yet). */
  Date getFixDate() {
    return fixDate;
  }

  /** Wikipedia ID of the change. */
  long getDiffId() {
    return diffId;
  }

  /** Wikipedia ID of the change if this fixes an error. Will be {@code 0} if not set. */
  long getFixDiffId() {
    return fixDiffId;
  }

  @Override
  public String toString() {
    return ruleId;
  }
}
