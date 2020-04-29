/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import org.languagetool.Experimental;
import org.languagetool.rules.RuleMatch;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A potential error as returned by the HTTP API of LanguageTool.
 * @since 4.0
 */
@Experimental
class RemoteRuleMatch {

  private final String ruleId;
  private final String msg;
  private final String context;
  private final int contextOffset;
  private final int offset;
  private final int errorLength;
  private final int estimatedContextForSureMatch;

  private String subId;
  private String shortMsg;
  private List<String> replacements;
  private String url;
  private String category;
  private String categoryId;
  private String locQualityIssueType;

  RemoteRuleMatch(String ruleId, String msg, String context, int contextOffset, int offset, int errorLength) {
    this(ruleId, msg, context, contextOffset, offset, errorLength, 0);
  }
  
  RemoteRuleMatch(String ruleId, String msg, String context, int contextOffset, int offset, int errorLength,
                  int estimatedContextForSureMatch) {
    this.ruleId = Objects.requireNonNull(ruleId);
    this.msg = Objects.requireNonNull(msg);
    this.context = Objects.requireNonNull(context);
    this.contextOffset = contextOffset;
    this.offset = offset;
    this.errorLength = errorLength;
    this.estimatedContextForSureMatch = estimatedContextForSureMatch;
  }

  boolean isTouchedByOneOf(List<RuleMatch> matches) {
    for (RuleMatch match : matches) {
      if (offset <= match.getToPos() && offset + errorLength >= match.getFromPos()) {
        return true;
      }
    }
    return false;
  }

  /** Unique (per language) identifier for the error. */
  public String getRuleId() {
    return ruleId;
  }

  /** Optional sub id (rule groups have a sub id for each rule). */
  public Optional<String> getRuleSubId() {
    return Optional.ofNullable(subId);
  }

  /** A text describing the error to the user. */
  public String getMessage() {
    return msg;
  }

  /** Optional short message describing the error. */
  public Optional<String> getShortMessage() {
    return Optional.ofNullable(shortMsg);
  }

  /**
   * Potential corrections for the error. Note that corrections might be wrong and
   * they are not necessarily ordered by quality.
   */
  public Optional<List<String>> getReplacements() {
    return Optional.ofNullable(replacements);
  }

  /** The error in its context. See {@link #getContextOffset()} and {@link #getErrorLength()} to get the exact position. */
  public String getContext() {
    return context;
  }
  
  /** The character position of the error start inside the result of {@link #getContext()}. */
  public int getContextOffset() {
    return contextOffset;
  }
  
  /** The character position where the error starts. */
  public int getErrorOffset() {
    return offset;
  }
  
  /** The length of the error in characters. */
  public int getErrorLength() {
    return errorLength;
  }

  /** URL with a more detailed explanation of the error. */
  public Optional<String> getUrl() {
    return Optional.ofNullable(url);
  }

  /** The error's category. */
  public Optional<String> getCategory() {
    return Optional.of(category);
  }

  /** The id of the error's category. */
  public Optional<String> getCategoryId() {
    return Optional.of(categoryId);
  }

  public Optional<String> getLocQualityIssueType() {
    return Optional.ofNullable(locQualityIssueType);
  }

  //
  // non-public setters
  //
  
  void setRuleSubId(String subId) {
    this.subId = subId;
  }

  void setShortMsg(String shortMsg) {
    this.shortMsg = shortMsg;
  }

  void setReplacements(List<String> replacements) {
    this.replacements = Collections.unmodifiableList(replacements);
  }

  void setUrl(String url) {
    this.url = url;
  }

  void setCategory(String category) {
    this.category = category;
  }

  void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  void setLocQualityIssueType(String locQualityIssueType) {
    this.locQualityIssueType = locQualityIssueType;
  }

  @Experimental
  int estimatedContextForSureMatch() {
    return estimatedContextForSureMatch;
  }

  @Override
  public String toString() {
    return ruleId + "@" + offset + "-" + (offset + errorLength);
  }

}
