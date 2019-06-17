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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;

import java.util.Date;
import java.util.Objects;

/**
 * A wrapper around a {@link RuleMatch} with an {@link Object#equals(Object)} implementation
 * that only considers the rule id. This is done so the diff algorithm can compare before/after
 * list of errors without getting confused by changes in character position.
 * @since 2.4
 */
final class WikipediaRuleMatch extends RuleMatch {

  private final Language language;
  private final String errorContext;
  private final String title;
  private final Date editDate;
  private final long diffId;
  
  WikipediaRuleMatch(Language language, RuleMatch match, String errorContext, AtomFeedItem feedItem) {
    super(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage());
    this.language = Objects.requireNonNull(language);
    this.errorContext = Objects.requireNonNull(errorContext);
    this.title = Objects.requireNonNull(feedItem.getTitle());
    this.editDate = Objects.requireNonNull(feedItem.getDate());
    this.diffId = feedItem.getDiffId();
  }

  Language getLanguage() {
    return language;
  }

  String getErrorContext() {
    return errorContext;
  }

  String getTitle() {
    return title;
  }

  Date getEditDate() {
    return new Date(editDate.getTime());
  }

  long getDiffId() {
    return diffId;
  }

  @Override
  public String toString() {
    return getRule().getId() + ":" + getFromPos() + "-" + getToPos();
  }

  @Override
  public int hashCode() {
    return getRule().getId().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof WikipediaRuleMatch) {
      return new EqualsBuilder().append(getRule().getId(), ((RuleMatch) other).getRule().getId()).isEquals();
    }
    return false;
  }
}
