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

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.ApiCleanupNeeded;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleMatcher;
import org.languagetool.tools.StringTools;

import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Information about an error rule that matches text and the position of the match.
 * See {@link org.languagetool.tools.ContextTools} for displaying errors in their original text context.
 * 
 * @author Daniel Naber
 */
public class RuleMatch implements Comparable<RuleMatch> {
  public static final RuleMatch[] EMPTY_ARRAY = new RuleMatch[0];
  public static final String SUGGESTION_START_TAG = "<suggestion>";
  public static final String SUGGESTION_END_TAG = "</suggestion>";

  //private static final Pattern SUGGESTION_PATTERN = Pattern.compile("<suggestion>(.*?)</suggestion>");
  private final Rule rule;
  private final String message;
  private final String shortMessage;   // used e.g. for OOo/LO context menu
  private final AnalyzedSentence sentence;

  private PatternPosition patternPosition;
  private OffsetPosition offsetPosition;
  private LinePosition linePosition = new LinePosition(-1, -1);
  private ColumnPosition columnPosition = new ColumnPosition(-1, -1);
  private Supplier<List<SuggestedReplacement>> suggestedReplacements;
  // track if more work needs to be done to compute suggestions;
  // allows enforcement of timeouts to return partial results without spending more time
  private boolean suggestionsComputed = true;
  private URL url;
  private Type type = Type.Other;
  private SortedMap<String, Float> features = Collections.emptySortedMap();
  private boolean autoCorrect = false;
  private String errorLimitLang;
  
  private String specificRuleId = "";

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
    this(rule, sentence, fromPos, toPos, fromPos, toPos, message, null, false, null);
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
    this(rule, sentence, fromPos, toPos, fromPos, toPos, message, shortMessage, false, null);
  }

  /**
   * Creates a RuleMatch object, taking the rule that triggered
   * this match, position of the match and an explanation message.
   * This message is scanned for &lt;suggestion&gt;...&lt;/suggestion&gt;
   * to get suggested fixes for the problem detected by this rule.
   *
   * @param shortMessage used for example in OpenOffice/LibreOffice's context menu
   * @since 4.9
   */
  public RuleMatch(Rule rule, AnalyzedSentence sentence, int fromPos, int toPos, int patternStartPos, int patternEndPos, String message, String shortMessage) {
    this(rule, sentence, fromPos, toPos, patternStartPos, patternEndPos, message, shortMessage, false, null);
  }

  /**
   * Create a rule match with any suggestions in the message overridden by the given suggestions
   * @since 4.7
   */
  public RuleMatch(Rule rule, AnalyzedSentence sentence, int fromPos, int toPos, String message, String shortMessage, List<String> suggestions) {
    this(rule, sentence, fromPos, toPos, fromPos, toPos, message, shortMessage, false, null);
    setSuggestedReplacements(suggestions);
  }
  /**
   * @deprecated use a constructor that also takes an {@code AnalyzedSentence} parameter (deprecated since 4.0)
   */
  public RuleMatch(Rule rule, int fromPos, int toPos, String message, String shortMessage,
                   boolean startWithUppercase, String suggestionsOutMsg) {
    this(rule, null, fromPos, toPos, fromPos, toPos, message, shortMessage, startWithUppercase, suggestionsOutMsg);
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
  public RuleMatch(Rule rule, AnalyzedSentence sentence, int fromPos, int toPos, int patternFromPos, int patternToPos,
                   String message, String shortMessage, boolean startWithUppercase, String suggestionsOutMsg) {
    this.rule = Objects.requireNonNull(rule);
    if (toPos <= fromPos) {
      throw new IllegalArgumentException("fromPos (" + fromPos + ") must be less than toPos (" + toPos + ")");
    }
    this.patternPosition = new PatternPosition(patternFromPos, patternToPos);
    this.offsetPosition = new OffsetPosition(fromPos, toPos);
    this.message = Objects.requireNonNull(message);
    this.shortMessage = shortMessage;
    // extract suggestion from <suggestion>...</suggestion> in message:
    LinkedHashSet<SuggestedReplacement> replacements = new LinkedHashSet<>();
    String suggestion = message + (suggestionsOutMsg != null ? suggestionsOutMsg : "");
    int pos = suggestion.indexOf(SUGGESTION_START_TAG);
    while (pos != -1) {
      int end = suggestion.indexOf(SUGGESTION_END_TAG, pos);
      if (end == -1) {
        break;
      }
      String replacement = suggestion.substring(pos + SUGGESTION_START_TAG.length(), end);
      pos = end + SUGGESTION_END_TAG.length();
      if (replacement.contains(PatternRuleMatcher.MISTAKE)) {
        continue;
      }
      if (startWithUppercase) {
        replacement = StringTools.uppercaseFirstChar(replacement);
      }
      replacements.add(new SuggestedReplacement(replacement));
      pos = suggestion.indexOf(SUGGESTION_START_TAG, pos);
    }

    this.sentence = sentence;

    suggestedReplacements = Suppliers.ofInstance(new ArrayList<>(replacements));
  }

  @SuppressWarnings("CopyConstructorMissesField")
  public RuleMatch(RuleMatch clone) {
    this(clone.getRule(), clone.getSentence(), clone.getFromPos(), clone.getToPos(), clone.getMessage(), clone.getShortMessage());
    this.setPatternPosition(clone.getPatternFromPos(), clone.getPatternToPos());
    suggestedReplacements = clone.suggestedReplacements;
    this.setAutoCorrect(clone.isAutoCorrect());
    this.setFeatures(clone.getFeatures());
    this.setUrl(clone.getUrl());
    this.setType(clone.getType());
    this.setLine(clone.getLine());
    this.setEndLine(clone.getEndLine());
    this.setColumn(clone.getColumn());
    this.setEndColumn(clone.getEndColumn());
    this.setSpecificRuleId(clone.getSpecificRuleId());
  }
  
  //clone with new replacements
  public RuleMatch(RuleMatch clone, List<String> replacements) {
    this(clone.getRule(), clone.getSentence(), clone.getFromPos(), clone.getToPos(), clone.getMessage(), clone.getShortMessage());
    this.setPatternPosition(clone.getPatternFromPos(), clone.getPatternToPos());
    this.setSuggestedReplacements(replacements);
    this.setAutoCorrect(clone.isAutoCorrect());
    this.setFeatures(clone.getFeatures());
    this.setUrl(clone.getUrl());
    this.setType(clone.getType());
    this.setLine(clone.getLine());
    this.setEndLine(clone.getEndLine());
    this.setColumn(clone.getColumn());
    this.setEndColumn(clone.getEndColumn());
    this.setSpecificRuleId(clone.getSpecificRuleId());
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
   * Position of the start of the pattern (in characters, zero-based, relative to the original input text).
   */
  public int getPatternFromPos() { return patternPosition.getStart(); }

  /**
   * Position of the end of the mistake pattern (in characters, zero-based, relative to the original input text).
   */
  public int getPatternToPos() { return patternPosition.getEnd(); }

  public void setPatternPosition(int fromPos, int toPos) {
    if (toPos <= fromPos) {
      throw new RuntimeException("fromPos (" + fromPos + ") must be less than toPos (" + toPos + ")");
    }
    patternPosition = new PatternPosition(fromPos, toPos);
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
      throw new RuntimeException("fromPos (" + fromPos + ") must be less than toPos (" + toPos + ") for match: <sentcontent>" + this + "</sentcontent>");
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
  
  public void addSuggestedReplacement(String replacement) {
    Objects.requireNonNull(replacement, "replacement may be empty but not null");
    addSuggestedReplacements(Collections.singletonList(replacement));
  }

  public void addSuggestedReplacements(List<String> replacements) {
    Objects.requireNonNull(replacements, "replacements may be empty but not null");
    Supplier<List<SuggestedReplacement>> prev = suggestedReplacements;
    setLazySuggestedReplacements(() ->
      Lists.newArrayList(Iterables.concat(prev.get(), Iterables.transform(replacements, SuggestedReplacement::new))));
  }
  /**
   * The text fragments which might be an appropriate fix for the problem. One
   * of these fragments can be used to replace the old text between {@link #getFromPos()}
   * to {@link #getToPos()}.
   * @return unmodifiable list of String objects or an empty List
   */
  public List<String> getSuggestedReplacements() {
    return Collections.unmodifiableList(
      suggestedReplacements.get().stream().map(SuggestedReplacement::getReplacement).collect(Collectors.toList())
    );
  }

  /**
   * @see #getSuggestedReplacements()
   */
  public void setSuggestedReplacements(List<String> replacements) {
    Objects.requireNonNull(replacements, "replacements may be empty but not null");
    suggestionsComputed = true;
    suggestedReplacements = Suppliers.ofInstance(
      replacements.stream().map(SuggestedReplacement::new).collect(Collectors.toList())
    );
  }

  public List<SuggestedReplacement> getSuggestedReplacementObjects() {
    return Collections.unmodifiableList(suggestedReplacements.get());
  }

  /**
   * @see #getSuggestedReplacements()
   */
  public void setSuggestedReplacementObjects(List<SuggestedReplacement> replacements) {
    Objects.requireNonNull(replacements, "replacements may be empty but not null");
    suggestedReplacements = Suppliers.ofInstance(replacements);
    suggestionsComputed = true;
  }

  /**
   * Set a lazy supplier that will compute suggested replacements
   * when {@link #getSuggestedReplacements()} or {@link #getSuggestedReplacementObjects()} is called.
   * This can be used to speed up sentence analysis
   * in cases when computationally expensive replacements won't necessarily be needed
   * (e.g. for an IDE in the same process).
   */
  public void setLazySuggestedReplacements(@NotNull Supplier<List<SuggestedReplacement>> replacements) {
    Objects.requireNonNull(replacements, "replacements may not be null");
    suggestedReplacements = Suppliers.memoize(replacements::get);
    suggestionsComputed = false;
  }

  /**
   * Force computing replacements, e.g. for accurate metrics for computation time and to set timeouts for this process
   * Used in server use case (i.e. {@code org.languagetool.server.TextChecker})
   */
  public void computeLazySuggestedReplacements() {
    suggestedReplacements = Suppliers.ofInstance(suggestedReplacements.get());
    suggestionsComputed = true;
  }

  /**
   * Discard lazy suggested replacements, but keep other suggestions
   * Useful to enforce time limits on result computation
   */
  public void discardLazySuggestedReplacements() {
    if (!suggestionsComputed) {
      setSuggestedReplacementObjects(Collections.emptyList());
    }
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
  public Type getType() {
    return this.type;
  }
  
  /**
   * @since 4.3
   */
  public void setType(Type type) {
    this.type = Objects.requireNonNull(type);
  }

  @Override
  public String toString() {
    if (rule instanceof PatternRule) {
      //String covered = getSentence().getText().substring(getFromPos(), getToPos());
      //return ((PatternRule) rule).getFullId() + ":" + offsetPosition + ":" + message + ":" + covered + " -> " + getSuggestedReplacements();
      return rule.getFullId() + ":" + offsetPosition + ":" + message;
    } else {
      //String covered = getSentence().getText().substring(getFromPos(), getToPos());
      //return rule.getId() + ":" + offsetPosition + ":" + message + ":" + covered + " -> " + getSuggestedReplacements();
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
        && Objects.equals(patternPosition, other.patternPosition)
        && Objects.equals(offsetPosition, other.offsetPosition)
        && Objects.equals(message, other.message)
        && Objects.equals(sentence, other.sentence)
        && Objects.equals(type, other.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rule.getId(), offsetPosition, patternPosition, message, sentence, type);
  }

  /**
   * The language that the text might be in if the error limit has been reached.
   * @since 5.3
   */
  @Nullable
  public String getErrorLimitLang() {
    return errorLimitLang;
  }

  /**
   * Call if the error limit is reached for this sentence. The caller will then get text ranges for the
   * sentence and can ignore errors there. Note: will not have an effect for text-level rules.
   * @param langCode the language this could be instead
   * @since 5.3
   */
  public void setErrorLimitLang(String langCode) {
    this.errorLimitLang = langCode;
  }

  /**
   * Unlike {@link Category}, this is specific to a RuleMatch, not to a rule.
   * It is mainly used for selecting the underline color in clients.
   * Note: this is experimental and might change soon (types might be added, deleted or renamed
   * without deprecating them first)
   * @since 4.3
   */
  public enum Type {
    /** Spelling errors, typically red. */
    UnknownWord,
    /** Style errors, typically light blue. */
    Hint,
    /** Other errors (including grammar), typically yellow/orange. */
    Other
  }

  static class PatternPosition extends MatchPosition {
    PatternPosition(int start, int end) {
      super(start, end);
    }
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
  
  /**
   * Set a new specific rule ID in the RuleMatch to replace getRule().getId() in
   * the output. Used for statistical purposes.
   * @since 5.6
   */
  public void setSpecificRuleId(String ruleId) {
    specificRuleId = ruleId;
  }

  /**
   * Get the specific rule ID from the RuleMatch to replace getRule().getId() in
   * the output. Used for statistical purposes.
   * @since 5.6
   */
  public String getSpecificRuleId() {
    if (specificRuleId.isEmpty()) {
      return this.getRule().getId();
    } else {
      return specificRuleId;
    }
  }
  
}
