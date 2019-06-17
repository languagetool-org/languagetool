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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.ApiCleanupNeeded;
import org.languagetool.Experimental;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tools.StringTools;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Information about an error rule that matches text and the position of the match.
 * See {@link org.languagetool.tools.ContextTools} for displaying errors in their original text context.
 * 
 * @author Daniel Naber
 */
public class RuleMatch implements Comparable<RuleMatch> {

  private static final Pattern SUGGESTION_PATTERN = Pattern.compile("<suggestion>(.*?)</suggestion>");
  private final Rule rule;
  private final String message;
  private final String shortMessage;   // used e.g. for OOo/LO context menu
  private final AnalyzedSentence sentence;
  private OffsetPosition offsetPosition;
  private LinePosition linePosition = new LinePosition(-1, -1);
  private ColumnPosition columnPosition = new ColumnPosition(-1, -1);
  private List<SuggestedReplacement> suggestedReplacements = new ArrayList<>();
  private URL url;
  private Type type = Type.Other;
  private SortedMap<String, Float> features = Collections.emptySortedMap();
  private boolean autoCorrect = false;
  /**
   * Creates a RuleMatch object, taking the rule that triggered
   * this match, position of the match and an explanation message.
   * This message is scanned for &lt;suggestion&gt;...&lt;/suggestion&gt;
   * to get suggested fixes for the problem detected by this rule.
   * @deprecated use a constructor that also takes an {@code AnalyzedSentence} parameter (deprecated since 4.0)
   */
  public RuleMatch(Rule rule, int fromPos, int toPos, String message) {
    this(rule, fromPos, toPos, message, null, false, null);
  }
  /**
   * Creates a RuleMatch object, taking the rule that triggered
   * this match, position of the match and an explanation message.
   * This message is scanned for &lt;suggestion&gt;...&lt;/suggestion&gt;
   * to get suggested fixes for the problem detected by this rule.
   * @since 4.0
   */
  public RuleMatch(Rule rule, AnalyzedSentence sentence, int fromPos, int toPos, String message) {
    this(rule, sentence, fromPos, toPos, message, null, false, null);
  }
  /**
   * Creates a RuleMatch object, taking the rule that triggered
   * this match, position of the match and an explanation message.
   * This message is scanned for &lt;suggestion&gt;...&lt;/suggestion&gt;
   * to get suggested fixes for the problem detected by this rule.
   *
   * @param shortMessage used for example in OpenOffice/LibreOffice's context menu
   * @since 4.0
   */
  public RuleMatch(Rule rule, AnalyzedSentence sentence, int fromPos, int toPos, String message, String shortMessage) {
    this(rule, sentence, fromPos, toPos, message, shortMessage, false, null);
  }


  /**
   * @deprecated use a constructor that also takes an {@code AnalyzedSentence} parameter (deprecated since 4.0)
   */
  public RuleMatch(Rule rule, int fromPos, int toPos, String message, String shortMessage,
                   boolean startWithUppercase, String suggestionsOutMsg) {
    this(rule, null, fromPos, toPos, message, shortMessage, startWithUppercase, suggestionsOutMsg);
  }
  /**
   * Creates a RuleMatch object, taking the rule that triggered
   * this match, position of the match and an explanation message.
   * This message is scanned for &lt;suggestion&gt;...&lt;/suggestion&gt;
   * to get suggested fixes for the problem detected by this rule.
   *
   * @param fromPos error start position in original text
   * @param toPos error end position in original text
   * @param shortMessage used for example in OpenOffice/LibreOffice's context menu (may be null)
   * @param startWithUppercase whether the original text at the position
   *    of the match starts with an uppercase character
   * @since 4.0
   */
  public RuleMatch(Rule rule, AnalyzedSentence sentence, int fromPos, int toPos, String message, String shortMessage,
      boolean startWithUppercase, String suggestionsOutMsg) {
    this.rule = Objects.requireNonNull(rule);
    if (toPos <= fromPos) {
      throw new IllegalArgumentException("fromPos (" + fromPos + ") must be less than toPos (" + toPos + ")");
    }
    this.offsetPosition = new OffsetPosition(fromPos, toPos);
    this.message = Objects.requireNonNull(message);
    this.shortMessage = shortMessage;
    // extract suggestion from <suggestion>...</suggestion> in message:
    Matcher matcher = SUGGESTION_PATTERN.matcher(message + suggestionsOutMsg);
    int pos = 0;
    while (matcher.find(pos)) {
      pos = matcher.end();
      String replacement = matcher.group(1);
      if (startWithUppercase) {
        replacement = StringTools.uppercaseFirstChar(replacement);
      }
      SuggestedReplacement repl = new SuggestedReplacement(replacement);
      if (!suggestedReplacements.contains(repl)) {
        suggestedReplacements.add(repl);
      }
    }
    this.sentence = sentence;
  }

  public RuleMatch(RuleMatch clone) {
    this(clone.getRule(), clone.getSentence(), clone.getFromPos(), clone.getToPos(), clone.getMessage(), clone.getShortMessage());
    this.setSuggestedReplacementObjects(clone.getSuggestedReplacementObjects());
    this.setAutoCorrect(clone.isAutoCorrect());
    this.setFeatures(clone.getFeatures());
    this.setUrl(clone.getUrl());
    this.setType(clone.getType());
    this.setLine(clone.getLine());
    this.setEndLine(clone.getEndLine());
    this.setColumn(clone.getColumn());
    this.setEndColumn(clone.getEndColumn());
  }
  
  //clone with new replacements
  public RuleMatch(RuleMatch clone, List<String> replacements) {
    this(clone.getRule(), clone.getSentence(), clone.getFromPos(), clone.getToPos(), clone.getMessage(), clone.getShortMessage());
    this.setSuggestedReplacements(replacements);
    this.setAutoCorrect(clone.isAutoCorrect());
    this.setFeatures(clone.getFeatures());
    this.setUrl(clone.getUrl());
    this.setType(clone.getType());
    this.setLine(clone.getLine());
    this.setEndLine(clone.getEndLine());
    this.setColumn(clone.getColumn());
    this.setEndColumn(clone.getEndColumn());
  }

  @NotNull
  public SortedMap<String, Float> getFeatures() {
    return features;
  }

  public void setFeatures(@NotNull SortedMap<String, Float> features) {
    this.features = features;
  }

  public boolean isAutoCorrect() {
    return autoCorrect;
  }

  public void setAutoCorrect(boolean autoCorrect) {
    this.autoCorrect = autoCorrect;
  }
  
  public Rule getRule() {
    return rule;
  }

  /**
   * Get the line number in which the match occurs (zero-based).
   * @deprecated rely on the character-based {@link #getFromPos()} instead (deprecated since 3.4)
   */
  public int getLine() {
    return linePosition.getStart();
  }

  /**
   * Set the line number in which the match occurs (zero-based).
   */
  public void setLine(int fromLine) {
    linePosition = new LinePosition(fromLine, linePosition.getEnd());
  }

  /**
   * Get the line number in which the match ends (zero-based).
   * @deprecated rely on {@link #getToPos()} instead (deprecated since 3.4)
   */
  public int getEndLine() {
    return linePosition.getEnd();
  }

  /**
   * Set the line number in which the match ends (zero-based).
   */
  public void setEndLine(int endLine) {
    linePosition = new LinePosition(linePosition.getStart(), endLine);
  }

  /**
   * Get the column number in which the match occurs (zero-based).
   * @deprecated rely on the character-based {@link #getFromPos()} instead (deprecated since 3.4)
   */
  public int getColumn() {
    return columnPosition.getStart();
  }

  /**
   * Set the column number in which the match occurs (zero-based).
   * @deprecated (deprecated since 3.5)
   */
  public void setColumn(int column) {
    this.columnPosition = new ColumnPosition(column, columnPosition.getEnd());
  }

  /**
   * Get the column number in which the match ends (zero-based).
   * @deprecated rely on {@link #getToPos()} instead (deprecated since 3.4)
   */
  public int getEndColumn() {
    return columnPosition.getEnd();
  }

  /**
   * Set the column number in which the match ends (zero-based).
   * @deprecated (deprecated since 3.5)
   */
  public void setEndColumn(int endColumn) {
    this.columnPosition = new ColumnPosition(columnPosition.getStart(), endColumn);
  }

  /**
   * Position of the start of the error (in characters, zero-based, relative to the original input text).
   */
  public int getFromPos() {
    return offsetPosition.getStart();
  }

  /**
   * Position of the end of the error (in characters, zero-based, relative to the original input text).
   */
  public int getToPos() {
    return offsetPosition.getEnd();
  }

  public void setOffsetPosition(int fromPos, int toPos) {
    if (toPos <= fromPos) {
      throw new RuntimeException("fromPos (" + fromPos + ") must be less than toPos (" + toPos + ")");
    }
    offsetPosition = new OffsetPosition(fromPos, toPos);
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
  @ApiCleanupNeeded("Should return an Optional")
  public String getShortMessage() {
    if (shortMessage == null) {
      return "";  // just because this is what we have documented
    }
    return shortMessage;
  }

  /**
   * @see #getSuggestedReplacements()
   */
  public void setSuggestedReplacement(String replacement) {
    Objects.requireNonNull(replacement, "replacement may be empty but not null");
    List<String> replacements = new ArrayList<>();
    replacements.add(replacement);
    setSuggestedReplacements(replacements);
  }

  /**
   * The text fragments which might be an appropriate fix for the problem. One
   * of these fragments can be used to replace the old text between {@link #getFromPos()}
   * to {@link #getToPos()}.
   * @return unmodifiable list of String objects or an empty List
   */
  public List<String> getSuggestedReplacements() {
    List<String> l = new ArrayList<>();
    for (SuggestedReplacement repl : suggestedReplacements) {
      l.add(repl.getReplacement());
    }
    return Collections.unmodifiableList(l);
  }

  /**
   * @see #getSuggestedReplacements()
   */
  public void setSuggestedReplacements(List<String> replacements) {
    Objects.requireNonNull(replacements, "replacements may be empty but not null");
    this.suggestedReplacements.clear();
    for (String replacement : replacements) {
      this.suggestedReplacements.add(new SuggestedReplacement(replacement));
    }
  }

  public List<SuggestedReplacement> getSuggestedReplacementObjects() {
    return Collections.unmodifiableList(suggestedReplacements);
  }

  /**
   * @see #getSuggestedReplacements()
   */
  public void setSuggestedReplacementObjects(List<SuggestedReplacement> replacements) {
    this.suggestedReplacements = Objects.requireNonNull(replacements, "replacements may be empty but not null");
  }

  /**
   * A URL that points to a more detailed error description or {@code null}.
   * Note that the {@link Rule} itself might also have an URL, which is usually
   * a less specific one than this. This one will overwrite the rule's URL in
   * the JSON output.
   * @since 4.0
   */
  @Nullable
  public URL getUrl() {
    return url;
  }

  /** @since 4.0 */
  public void setUrl(URL url) {
    this.url = url;
  }

  /** @since 4.0 */
  public AnalyzedSentence getSentence() {
    return sentence;
  }

  /**
   * @since 4.3
   */
  @Experimental
  public Type getType() {
    return this.type;
  }
  
  /**
   * @since 4.3
   */
  @Experimental
  public void setType(Type type) {
    this.type = Objects.requireNonNull(type);
  }

  @Override
  public String toString() {
    if (rule instanceof PatternRule) {
      return ((PatternRule) rule).getFullId() + ":" + offsetPosition + ":" + message;
    } else {
      return rule.getId() + ":" + offsetPosition + ":" + message;
    }
  }

  /** Compare by start position. */
  @Override
  public int compareTo(RuleMatch other) {
    Objects.requireNonNull(other);
    return Integer.compare(getFromPos(), other.getFromPos());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RuleMatch other = (RuleMatch) o;
    return Objects.equals(rule.getId(), other.rule.getId())
        && Objects.equals(offsetPosition, other.offsetPosition)
        && Objects.equals(message, other.message)
        && Objects.equals(suggestedReplacements, other.suggestedReplacements)
        && Objects.equals(sentence, other.sentence)
        && Objects.equals(type, other.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rule.getId(), offsetPosition, message, suggestedReplacements, sentence, type);
  }

  /**
   * Unlike {@link Category}, this is specific to a RuleMatch, not to a rule.
   * It is mainly used for selecting the underline color in clients.
   * Note: this is experimental and might change soon (types might be added, deleted or renamed
   * without deprecating them first)
   * @since 4.3
   */
  @Experimental
  public enum Type {
    /** Spelling errors, typically red. */
    UnknownWord,
    /** Style errors, typically light blue. */
    Hint,
    /** Other errors (including grammar), typically yellow/orange. */
    Other
  }

  static class OffsetPosition extends MatchPosition {
    OffsetPosition(int start, int end) {
      super(start, end);
    }
  }

  static class LinePosition extends MatchPosition {
    LinePosition(int start, int end) {
      super(start, end);
    }
  }

  static class ColumnPosition extends MatchPosition {
    ColumnPosition(int start, int end) {
      super(start, end);
    }
  }
}
