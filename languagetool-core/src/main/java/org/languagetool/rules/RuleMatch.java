/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.tools.StringTools;

/**
 * Information about an error rule that matches text and the position of the match.
 * See {@link org.languagetool.tools.ContextTools} for displaying errors in their original text context.
 * 
 * @author Daniel Naber
 */
public class RuleMatch implements Comparable<RuleMatch> {

  private static final Pattern SUGGESTION_PATTERN = Pattern.compile("<suggestion>(.*?)</suggestion>");

  private final Rule rule;
  private final int fromPos;
  private final int toPos;
  private final String message;
  private final String shortMessage;   // used e.g. for OOo/LO context menu

  private int fromLine = -1;
  private int column = -1;
  private int offset = -1;
  private int endLine = -1;
  private int endColumn = -1;

  private List<String> suggestedReplacements = new ArrayList<>();

  /**
   * Creates a RuleMatch object, taking the rule that triggered
   * this match, position of the match and an explanation message.
   * This message is scanned for &lt;suggestion&gt;...&lt;/suggestion&gt;
   * to get suggested fixes for the problem detected by this rule.
   */
  public RuleMatch(Rule rule, int fromPos, int toPos, String message) {
    this(rule, fromPos, toPos, message, null, false, null);
  }

  /**
   * Creates a RuleMatch object, taking the rule that triggered
   * this match, position of the match and an explanation message.
   * This message is scanned for &lt;suggestion&gt;...&lt;/suggestion&gt;
   * to get suggested fixes for the problem detected by this rule.
   *
   * @param shortMessage used for example in OpenOffice/LibreOffice's context menu
   */
  public RuleMatch(Rule rule, int fromPos, int toPos, String message, String shortMessage) {
    this(rule, fromPos, toPos, message, shortMessage, false, null);
  }

  /**
   * Creates a RuleMatch object, taking the rule that triggered
   * this match, position of the match and an explanation message.
   * This message is scanned for &lt;suggestion&gt;...&lt;/suggestion&gt;
   * to get suggested fixes for the problem detected by this rule. 
   * 
   * @param shortMessage used for example in OpenOffice/LibreOffice's context menu
   * @param startWithUppercase whether the original text at the position
   *    of the match starts with an uppercase character
   */
  public RuleMatch(Rule rule, int fromPos, int toPos, String message, String shortMessage, 
      boolean startWithUppercase, String suggestionsOutMsg) {
    this.rule = rule;
    if (toPos <= fromPos) {
      throw new RuntimeException("fromPos (" + fromPos + ") must be less than toPos (" + toPos + ")");
    }
    this.fromPos = fromPos;
    this.toPos = toPos;
    this.message = message;
    this.shortMessage = shortMessage;
    // extract suggestion from <suggestion>...</suggestion> in message:
    final Matcher matcher = SUGGESTION_PATTERN.matcher(message + suggestionsOutMsg);
    int pos = 0;
    while (matcher.find(pos)) {
      pos = matcher.end();
      String replacement = matcher.group(1);
      if (startWithUppercase) {
        replacement = StringTools.uppercaseFirstChar(replacement);
      }
      suggestedReplacements.add(replacement);
    }
  }

  public Rule getRule() {
    return rule;
  }

  /**
   * Set the line number in which the match occurs (zero-based).
   */
  public void setLine(final int fromLine) {
    this.fromLine = fromLine;
  }

  /**
   * Get the line number in which the match occurs (zero-based).
   */
  public int getLine() {
    return fromLine;
  }

  /**
   * Set the line number in which the match ends (zero-based).
   */
  public void setEndLine(final int endLine) {
    this.endLine = endLine;
  }

  /**
   * Get the line number in which the match ends (zero-based).
   */
  public int getEndLine() {
    return endLine;
  }

  /**
   * Set the column number in which the match occurs (zero-based).
   */
  public void setColumn(final int column) {
    this.column = column;
  }

  /**
   * Get the column number in which the match occurs (zero-based).
   */
  public int getColumn() {
    return column;
  }

  /**
   * Set the column number in which the match ends (zero-based).
   */
  public void setEndColumn(final int endColumn) {
    this.endColumn = endColumn;
  }

  /**
   * Get the column number in which the match ends (zero-based).
   */
  public int getEndColumn() {
    return endColumn;
  }

  /**
   * Set the character offset at which the match occurs (zero-based).
   */
  public void setOffset(final int offset) {
    this.offset = offset;
  }

  /**
   * Get the character offset at which the match occurs (zero-based).
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Position of the start of the error (in characters, zero-based).
   */
  public int getFromPos() {
    return fromPos;
  }

  /**
   * Position of the end of the error (in characters, zero-based).
   */
  public int getToPos() {
    return toPos;
  }

  /**
   * A human-readable explanation describing the error. This may contain
   * one or more corrections marked up with &lt;suggestion&gt;...&lt;/suggestion&gt;.
   * @see #getSuggestedReplacements()
   * @see #getShortMessage()
   */
  public String getMessage() {
    return message;
  }  

  /**
   * A shorter human-readable explanation describing the error or an empty string
   * if no such explanation is available.
   * @see #getMessage()
   */
  public String getShortMessage() {
    return shortMessage;
  }

  /**
   * @see #getSuggestedReplacements()
   */
  public void setSuggestedReplacement(final String replacement) {
    Objects.requireNonNull(replacement, "replacement may be empty but not null");
    final List<String> replacements = new ArrayList<>();
    replacements.add(replacement);
    setSuggestedReplacements(replacements);
  }

  /**
   * @see #getSuggestedReplacements()
   */
  public void setSuggestedReplacements(final List<String> replacements) {
    this.suggestedReplacements = Objects.requireNonNull(replacements, "replacements may be empty but not null");
  }

  /**
   * The text fragments which might be an appropriate fix for the problem. One
   * of these fragments can be used to replace the old text between {@link #getFromPos()}
   * to {@link #getToPos()}.
   * @return List of String objects or an empty List
   */
  public List<String> getSuggestedReplacements() {
    return suggestedReplacements;
  }

  @Override
  public String toString() {
    return rule.getId() + ":" + fromPos + "-" + toPos + ":" + message;
  }

  /** Compare by start position. */
  @Override
  public int compareTo(final RuleMatch other) {
    Objects.requireNonNull(other);
    return Integer.compare(getFromPos(), other.getFromPos());
  }

}
