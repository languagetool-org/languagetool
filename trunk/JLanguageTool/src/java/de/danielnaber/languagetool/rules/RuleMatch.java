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
package de.danielnaber.languagetool.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.tools.StringTools;

/**
 * A class that holds information about where a rule matches text.
 * 
 * @author Daniel Naber
 */
public class RuleMatch implements Comparable<RuleMatch> {

  private static final Pattern SUGGESTION_PATTERN = Pattern.compile("<suggestion>(.*?)</suggestion>");

  private int fromLine = -1;
  private int column = -1;
  private int offset = -1;
  private int endLine = -1;
  private int endColumn = -1;

  private Rule rule;
  private int fromPos;
  private int toPos;
  private String message;
  // for OOo context menu
  private String shortMessage;

  private List<String> suggestedReplacements = new ArrayList<String>();

//TODO: remove this one after all rules get their short comments in place 
  public RuleMatch(Rule rule, int fromPos, int toPos, String message) {
    this(rule, fromPos, toPos, message, null, false);
  }
  
  // TODO: remove this constructor?
  public RuleMatch(Rule rule, int fromPos, int toPos, String message, String shortMessage) {
    this(rule, fromPos, toPos, message, shortMessage, false);
  }

  /**
   * Creates a RuleMatch object, taking the rule that triggered
   * this match, position of the match and an explanation message.
   * This message is scanned for &lt;suggestion>...&lt;/suggestion> to get suggested
   * fixes for the problem detected by this rule. 
   * 
   * @param startWithUppercase whether the original text at the position
   *    of the match start with an uppercase character
   */
  public RuleMatch(Rule rule, int fromPos, int toPos, String message, String shortMessage, 
      boolean startWithUppercase) {
    this.rule = rule;
    this.fromPos = fromPos;
    this.toPos = toPos;
    this.message = message;
    this.shortMessage = shortMessage;
    // extract suggestion from <suggestion>...</suggestion> in message:
    final Matcher matcher = SUGGESTION_PATTERN.matcher(message);
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
   * Set the line number in which the match occurs.
   */
  public void setLine(final int fromLine) {
    this.fromLine = fromLine;
  }

  /**
   * Get the line number in which the match occurs.
   */
  public int getLine() {
    return fromLine;
  }

  /**
   * Set the line number in which the match ends.
   */
  public void setEndLine(final int endLine) {
    this.endLine = endLine;
  }

  /**
   * Get the line number in which the match ends.
   */
  public int getEndLine() {
    return endLine;
  }

  /**
   * Set the column number in which the match occurs.
   */
  public void setColumn(final int column) {
    this.column = column;
  }

  /**
   * Get the column number in which the match occurs.
   */
  public int getColumn() {
    return column;
  }

  /**
   * Set the column number in which the match ends.
   */
  public void setEndColumn(final int endColumn) {
    this.endColumn = endColumn;
  }

  /**
   * Get the column number in which the match ends.
   */
  public int getEndColumn() {
    return endColumn;
  }

  /**
   * Set the character offset at which the match occurs.
   */
  public void setOffset(final int offset) {
    this.offset = offset;
  }

  /**
   * Get the character offset at which the match occurs.
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Position of the start of the error (in characters).
   */
  public int getFromPos() {
    return fromPos;
  }

  /**
   * Position of the end of the error (in characters).
   */
  public int getToPos() {
    return toPos;
  }

  /**
   * A human-readable explanation describing the error.
   */
  public String getMessage() {
    return message;
  }  

  /**
   * A shorter human-readable explanation describing the error.
   */
  public String getShortMessage() {
    return shortMessage;
  }


  /**
   * @see #getSuggestedReplacements()
   */
  public void setSuggestedReplacement(final String replacement) {
    if (replacement == null)
      throw new NullPointerException("replacement might be empty but not null");
    final List<String> replacements = new ArrayList<String>();
    replacements.add(replacement);
    setSuggestedReplacements(replacements);
  }

  /**
   * @see #getSuggestedReplacements()
   */
  public void setSuggestedReplacements(final List<String> replacement) {
    if (replacement == null)
      throw new NullPointerException("replacement might be empty but not null");
    this.suggestedReplacements = replacement;
  }

  /**
   * The text fragments which might be an appropriate fix for the problem. One
   * of these fragments can be used to replace the old text between getFromPos()
   * to getToPos(). Text between &lt;suggestion> and &lt;/suggestion> is
   * taken as the suggested replacement. 
   * @return List of String objects or an empty List
   */
  public List<String> getSuggestedReplacements() {
    return suggestedReplacements;
  }

  @Override
  public String toString() {
    return rule.getId() + ":" + fromPos + "-" + toPos + ":" + message;
  }

  @Override
  public int compareTo(final RuleMatch other) {
    if (other == null)
      throw new ClassCastException();
    if (getFromPos() < other.getFromPos())
      return -1;
    if (getFromPos() > other.getFromPos())
      return 1;
    return 0;
  }

}
