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
package org.languagetool.rules.patterns;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A part of a pattern, represents the 'token' element of the {@code grammar.xml}.
 */
public class PatternToken implements Cloneable {

  /** Matches only tokens without any POS tag. **/
  public static final String UNKNOWN_TAG = "UNKNOWN";

  private final boolean inflected;

  private List<PatternToken> andGroupList;
  private List<PatternToken> orGroupList;
  private StringMatcher textMatcher;
  private PosToken posToken;
  private ChunkTag chunkTag;
  private boolean negation;
  private boolean testWhitespace;
  private boolean whitespaceBefore;
  private boolean isInsideMarker = true;

  private Exceptions exceptions;

  /** True if scope=="next". */
  private boolean exceptionValidNext;

  private byte skip;
  private boolean mayBeOmitted;
  private byte maxOccurrence = 1;

  /** The reference to another element in the pattern. **/
  private Match tokenReference;

  /** String ID of the phrase the element is in. **/
  private String phraseName;

  /**
   * This var is used to determine if calling {@link #setStringElement} makes sense. This method
   * takes most time so it's best to reduce the number of its calls.
   */
  private boolean testString;

  /** Determines whether the element should be ignored when doing unification **/
  private boolean unificationNeutral;

  private boolean uniNegation;
  private Map<String, List<String>> unificationFeatures;

  /** Set to true on tokens that close the unification block. */
  private boolean isLastUnified;

  /**
   * Creates Element that is used to match tokens in the text.
   * @param token String to be matched
   * @param caseSensitive true if the check is case-sensitive
   * @param regExp true if the check uses regular expressions
   * @param inflected true if the check refers to base forms (lemmas), note that {@code token} must be a base form for this to work
   */
  public PatternToken(String token, boolean caseSensitive, boolean regExp, boolean inflected) {
    this(inflected, StringMatcher.create(normalizeTextPattern(token), regExp, caseSensitive));
  }

  PatternToken(boolean inflected, @NotNull StringMatcher textMatcher) {
    this.inflected = inflected;
    setTextMatcher(textMatcher);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  /**
   * Checks whether the rule element matches the token given as a parameter.
   * @param token AnalyzedToken to check matching against
   * @return True if token matches, false otherwise.
   */
  public boolean isMatched(AnalyzedToken token) {
    if (testWhitespace && !isWhitespaceBefore(token)) {
      return false;
    }
    boolean posNegation = posToken != null && posToken.negation;
    if (testString) {
      return textMatcher.matches(getTestToken(token)) ^ negation &&
             isPosTokenMatched(token) ^ posNegation;
    } else {
      return !negation &&
             isPosTokenMatched(token) ^ posNegation;
    }
  }

  /**
   * Checks whether an exception matches.
   * @param token AnalyzedToken to check matching against
   * @return True if any of the exceptions matches (logical disjunction).
   */
  public boolean isExceptionMatched(AnalyzedToken token) {
    if (exceptions != null && exceptions.currentAndNext != null) {
      for (PatternToken testException : exceptions.currentAndNext) {
        if (!testException.exceptionValidNext && testException.isMatched(token)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Enables testing multiple conditions specified by multiple element exceptions.
   * Works as logical AND operator.
   * @param token the token checked for exceptions.
   * @return true if all conditions are met, false otherwise.
   */
  public boolean isAndExceptionGroupMatched(AnalyzedToken token) {
    if (andGroupList == null) return false;
    for (PatternToken testAndGroup : andGroupList) {
      if (testAndGroup.isExceptionMatched(token)) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method checks exceptions both in AND-group and the token. Introduced to for clarity.
   * @param token Token to match
   * @return True if matched.
   */
  public boolean isExceptionMatchedCompletely(AnalyzedToken token) {
    // note: short-circuiting possible
    return isExceptionMatched(token) || isAndExceptionGroupMatched(token);
  }

  public void setAndGroupElement(PatternToken andToken) {
    if (andGroupList == null) {
      andGroupList = new ArrayList<>(0);
    }
    andGroupList.add(Objects.requireNonNull(andToken));
  }

  /**
   * Checks if this element has an AND group associated with it.
   * @return true if the element has a group of elements that all should match.
   */
  public boolean hasAndGroup() {
    return andGroupList != null && andGroupList.size() > 0;
  }

  /**
   * Returns the group of elements linked with AND operator.
   */
  public List<PatternToken> getAndGroup() {
    return andGroupList == null ? Collections.emptyList() : Collections.unmodifiableList(andGroupList);
  }

  /** @since 2.3 */
  public void setOrGroupElement(PatternToken orToken) {
    if (orGroupList == null) {
      orGroupList = new ArrayList<>(0);
    }
    orGroupList.add(Objects.requireNonNull(orToken));
  }

  /**
   * Checks if this element has an OR group associated with it.
   * @return true if the element has a group of elements that all should match.
   * @since 2.3
   */
  public boolean hasOrGroup() {
    return orGroupList != null && orGroupList.size() > 0;
  }

  /**
   * Returns the group of elements linked with OR operator.
   * @since 2.3
   */
  public List<PatternToken> getOrGroup() {
    return orGroupList == null ? Collections.emptyList() : Collections.unmodifiableList(orGroupList);
  }

  /**
   * Checks whether a previously set exception matches (in case the exception had scope == "next").
   * @param token {@link AnalyzedToken} to check matching against.
   * @return True if any of the exceptions matches.
   */
  public boolean isMatchedByScopeNextException(AnalyzedToken token) {
    if (exceptions != null && exceptions.currentAndNext != null) {
      for (PatternToken testException : exceptions.currentAndNext) {
        if (testException.exceptionValidNext && testException.isMatched(token)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks whether an exception for a previous token matches (in case the exception had scope ==
   * "previous").
   * @param token {@link AnalyzedToken} to check matching against.
   * @return True if any of the exceptions matches.
   */
  public boolean isMatchedByPreviousException(AnalyzedToken token) {
    if (hasPreviousException()) {
      for (PatternToken testException : exceptions.previous) {
        if (!testException.exceptionValidNext && testException.isMatched(token)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks whether an exception for a previous token matches all readings of a given token (in case
   * the exception had scope == "previous").
   * @param prevToken {@link AnalyzedTokenReadings} to check matching against.
   * @return true if any of the exceptions matches.
   */
  public boolean isMatchedByPreviousException(AnalyzedTokenReadings prevToken) {
    for (AnalyzedToken analyzedToken : prevToken) {
      if (isMatchedByPreviousException(analyzedToken)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the token is a sentence start.
   * @return True if the element starts the sentence and the element hasn't been set to have negated POS token.
   */
  public boolean isSentenceStart() {
    return posToken != null && JLanguageTool.SENTENCE_START_TAGNAME.equals(posToken.posTag) && !posToken.negation;
  }

  /** @since 2.9 */
  public void setPosToken(PosToken posToken) {
    this.posToken = posToken;
  }

  /** @since 2.9 */
  public void setChunkTag(ChunkTag chunkTag) {
    this.chunkTag = chunkTag;
  }

  public String getString() {
    return textMatcher.pattern;
  }

  public void setStringElement(String token) {
    setTextMatcher(StringMatcher.create(normalizeTextPattern(token), isRegularExpression(), isCaseSensitive()));
  }

  void setTextMatcher(@NotNull StringMatcher matcher) {
    textMatcher = matcher;
    testString = !StringTools.isEmpty(matcher.pattern);
  }

  static String normalizeTextPattern(String token) {
    return token == null ? "" : StringTools.trimWhitespace(token);
  }

  /**
   * Sets a string and/or pos exception for matching tokens.
   * @param token The string in the exception.
   * @param regExp True if the string is specified as a regular expression.
   * @param inflected True if the string is a base form (lemma).
   * @param negation True if the exception is negated.
   * @param scopeNext True if the exception scope is next tokens.
   * @param scopePrevious True if the exception should match only a single previous token.
   * @param posToken The part of the speech tag in the exception.
   * @param posRegExp True if the POS is specified as a regular expression.
   * @param posNegation True if the POS exception is negated.
   * @param caseSensitivity if null, use this element's setting for case sensitivity, otherwise the specified value
   * @since 2.9
   */
  public void setStringPosException(
          String token, boolean regExp, boolean inflected,
          boolean negation, boolean scopeNext, boolean scopePrevious,
          String posToken, boolean posRegExp, boolean posNegation, Boolean caseSensitivity) {
    PatternToken exception = new PatternToken(token, caseSensitivity == null ? isCaseSensitive() : caseSensitivity, regExp, inflected);
    exception.setNegation(negation);
    exception.setPosToken(new PosToken(posToken, posRegExp, posNegation));
    exception.exceptionValidNext = scopeNext;
    if (exceptions == null) {
      exceptions = new Exceptions();
    }
    exceptions.addException(exception, scopePrevious);
  }

  /**
   * Tests if part of speech matches a given string.
   * Special value UNKNOWN_TAG matches null POS tags.
   * @param token Token to test.
   * @return true if matches
   */
  private boolean isPosTokenMatched(AnalyzedToken token) {
    PosToken pos = posToken;
    if (pos == null || pos.posTag == null || pos.posUnknown && token.hasNoTag()) {
      return true;
    }
    String tokenPos = token.getPOSTag();
    if (tokenPos == null) {
      return false;
    }
    return pos.posPattern != null ? pos.posPattern.matcher(tokenPos).matches() : pos.posTag.equals(tokenPos);
  }

  private String getTestToken(AnalyzedToken token) {
    // enables using words with lemmas and without lemmas
    // in the same regexp with inflected="yes"
    if (inflected) {
      if (token.getLemma() != null) {
        return token.getLemma();
      } else {
        return token.getToken();
      }
    }
    return token.getToken();
  }

  /**
   * Gets the exception scope length.
   * @return scope length in tokens
   */
  public int getSkipNext() {
    return skip;
  }

  /**
   * The minimum number of times the element needs to occur.
   */
  public int getMinOccurrence() {
    return mayBeOmitted ? 0 : 1;
  }

  /**
   * The maximum number of times the element may occur.
   */
  public int getMaxOccurrence() {
    return maxOccurrence;
  }

  /**
   * @param i exception scope length.
   */
  public void setSkipNext(int i) {
    if (i < -1 || i > Byte.MAX_VALUE) {
      throw new IllegalArgumentException("'skip' should be between -1 and " + Byte.MAX_VALUE);
    }
    skip = (byte) i;
  }

  /**
   * The minimum number of times this element may occur.
   * @param i currently only {@code 0} and {@code 1} are supported
   */
  public void setMinOccurrence(int i) {
    if (i != 0 && i != 1) {
      throw new IllegalArgumentException("minOccurrences must be 0 or 1: " + i);
    }
    mayBeOmitted = i == 0;
  }

  /**
   * The maximum number of times this element may occur.
   * @param i a number &gt;= 1 or {@code -1} for unlimited occurrences
   */
  public void setMaxOccurrence(int i) {
    if (i == 0) {
      throw new IllegalArgumentException("maxOccurrences may not be 0");
    }
    if (i < -1 || i > Byte.MAX_VALUE) {
      throw new IllegalArgumentException("maxOccurrences should be between -1 and " + Byte.MAX_VALUE);
    }
    maxOccurrence = (byte) i;
  }

  /**
   * Checks if the element has an exception for a previous token.
   * @return True if the element has a previous token matching exception.
   */
  public boolean hasPreviousException() {
    return exceptions != null && exceptions.previous != null;
  }

  /**
   * Checks if the element has an exception for a next scope.
   * (only used for testing)
   * @return True if the element has exception for the next scope.
   */
  public boolean hasNextException() {
    return exceptionValidNext;
  }

  /**
   * Negates the matching so that non-matching elements match and vice-versa.
   */
  public void setNegation(boolean negation) {
    this.negation = negation;
  }

  /**
   * see {@link #setNegation}
   * @since 0.9.3
   */
  public boolean getNegation() {
    return negation;
  }

  /**
   * @return true when this element refers to another token.
   */
  public boolean isReferenceElement() {
    return tokenReference != null;
  }

  /**
   * Sets the reference to another token.
   * @param match Formatting object for the token reference.
   */
  public void setMatch(Match match) {
    tokenReference = Objects.requireNonNull(match);
  }

  public Match getMatch() {
    return tokenReference;
  }

  /**
   * Prepare PatternToken for matching by formatting its string token and POS (if the Element is supposed
   * to refer to some other token).
   * @param token the token specified as {@link AnalyzedTokenReadings}
   * @param synth the language synthesizer ({@link Synthesizer})
   */
  public PatternToken compile(AnalyzedTokenReadings token, Synthesizer synth) throws IOException {
    PatternToken compiledPatternToken;
    try {
      compiledPatternToken = (PatternToken) clone();
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Could not clone element", e);
    }
    compiledPatternToken.doCompile(token, synth);
    return compiledPatternToken;
  }

  private void doCompile(AnalyzedTokenReadings token, Synthesizer synth) throws IOException {
    MatchState matchState = tokenReference.createState(synth, token);
    String reference = "\\" + tokenReference.getTokenRef();
    if (tokenReference.setsPos()) {
      String posReference = matchState.getTargetPosTag();
      if (posReference != null) {
        setPosToken(new PosToken(posReference, tokenReference.posRegExp(), negation));
      }
      setStringElement(getString().replace(reference, ""));
    } else {
      setStringElement(getString().replace(reference, matchState.toTokenString()));
    }
  }

  /**
   * Sets the phrase the element is in.
   * @param id ID of the phrase.
   */
  public void setPhraseName(String id) {
    phraseName = id;
  }

  /**
   * Checks if the Element is in any phrase.
   * @return True if the Element is contained in the phrase.
   */
  public boolean isPartOfPhrase() {
    return phraseName != null;
  }

  /**
   * Whether the element matches case sensitively.
   * @since 2.3
   */
  public boolean isCaseSensitive() {
    return textMatcher.caseSensitive;
  }

  /**
   * Tests whether the element matches a regular expression.
   * @since 0.9.6
   */
  public boolean isRegularExpression() {
    return textMatcher.isRegExp;
  }

  /**
   * Tests whether the POS matches a regular expression.
   * @since 1.3.0
   */
  public boolean isPOStagRegularExpression() {
    return posToken != null && posToken.posPattern != null;
  }

  /**
   * @return the POS of the Element or {@code null}
   * @since 0.9.6
   */
  @Nullable
  public String getPOStag() {
    return posToken != null ? posToken.posTag : null;
  }

  /**
   * @return the chunk tag of the Element or {@code null}
   * @since 2.3
   */
  @Nullable
  public ChunkTag getChunkTag() {
    return chunkTag;
  }

  /**
   * @return true if the POS is negated.
   */
  public boolean getPOSNegation() {
    return posToken != null && posToken.negation;
  }

  /**
   * @return true if the token matches all inflected forms
   */
  public boolean isInflected() {
    return inflected;
  }

  /**
   * Gets the phrase the element is in.
   * @return String The name of the phrase.
   */
  @Nullable
  public String getPhraseName() {
    return phraseName;
  }

  public boolean isUnified() {
    return unificationFeatures != null;
  }

  public void setUnification(Map<String, List<String>> uniFeatures) {
    unificationFeatures = Objects.requireNonNull(uniFeatures);
  }

  /**
   * Get unification features and types.
   * @return A map from features to a list of types or {@code null}
   * @since 1.0.1
   */
  @Nullable
  public Map<String, List<String>> getUniFeatures() {
    return unificationFeatures;
  }

  public void setUniNegation() {
    uniNegation = true;
  }

  public boolean isUniNegated() {
    return uniNegation;
  }

  public boolean isLastInUnification() {
    return isLastUnified;
  }

  public void setLastInUnification() {
    isLastUnified = true;
  }

  /**
   * Determines whether the element should be silently ignored during unification,
   * and simply added.
   * @return True when the element is not included in unifying.
   * @since 2.5
   */
  public boolean isUnificationNeutral() {
    return unificationNeutral;
  }

  /**
   * Sets the element as ignored during unification.
   * @since 2.5
   */
  public void setUnificationNeutral() {
    unificationNeutral = true;
  }


  public void setWhitespaceBefore(boolean isWhite) {
    whitespaceBefore = isWhite;
    testWhitespace = true;
  }

  public boolean isInsideMarker() {
    return isInsideMarker;
  }

  public void setInsideMarker(boolean isInsideMarker) {
    this.isInsideMarker = isInsideMarker;
  }

  /**
   * Sets the attribute on the exception that determines matching of patterns
   * that depends on whether there was a space before the token matching the exception
   * or not.
   * The same procedure is used for tokens that are valid for previous or current tokens.
   * 
   * @param isWhite If true, the space before exception is required.
   */
  public void setExceptionSpaceBefore(boolean isWhite) {
    if (exceptions != null) {
      List<PatternToken> list = hasPreviousException() ? exceptions.previous : exceptions.currentAndNext;
      if (list != null) {
        list.get(list.size() - 1).setWhitespaceBefore(isWhite);
      }
    }
  }

  public boolean isWhitespaceBefore(AnalyzedToken token) {
    return whitespaceBefore == token.isWhitespaceBefore();
  }

  /**
   * @return A List of Exceptions. Used for testing.
   * @since 1.0.0
   */
  @Nullable
  public List<PatternToken> getExceptionList() {
    return exceptions == null ? null : exceptions.currentAndNext;
  }

  public boolean hasExceptionList() {
    return exceptions != null;
  }

  /**
   * @return all possible forms that this token pattern can accept, or {@code null} if such set is unknown/unbounded.
   * This is used internally for performance optimizations.
   */
  @Nullable
  public Set<String> calcFormHints() {
    Set<String> result = inflected ? null : calcOwnPossibleStringValues();
    if (result == null) return null;

    if (andGroupList != null) {
      result = new HashSet<>(result);

      for (PatternToken token : andGroupList) {
        Set<String> hints = token.calcFormHints();
        if (hints != null) {
          result.retainAll(hints);
        }
      }
    } else if (orGroupList != null) {
      result = new HashSet<>(result);

      for (PatternToken token : orGroupList) {
        Set<String> hints = token.calcFormHints();
        if (hints == null) return null;

        result.addAll(hints);
      }
    }

    return result;
  }

  @Nullable
  private Set<String> calcOwnPossibleStringValues() {
    if (negation || !hasStringThatMustMatch()) {
      return null;
    }
    return textMatcher.getPossibleValues();
  }

  boolean hasStringThatMustMatch() {
    return tokenReference == null && !mayBeOmitted && !getString().isEmpty();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (negation) {
      sb.append('!');
    }
    sb.append(getString());
    if (phraseName != null) {
      sb.append(" {");
      sb.append(phraseName);
      sb.append('}');
    }
    if (posToken != null) {
      sb.append('/');
      sb.append(posToken);
    }
    if (chunkTag != null) {
      sb.append('/');
      sb.append(chunkTag);
    }
    List<PatternToken> exceptionList = getExceptionList();
    if (exceptionList != null) {
      sb.append("/exceptions=");
      sb.append(exceptionList);
    }
    return sb.toString();
  }

  public static class PosToken {

    private final String posTag;
    private final boolean negation;
    private final Pattern posPattern;
    private final boolean posUnknown;

    public PosToken(String posTag, boolean regExp, boolean negation) {
      this.posTag = posTag;
      this.negation = negation;
      if (regExp) {
        posPattern = Pattern.compile(posTag);
        posUnknown = posPattern.matcher(UNKNOWN_TAG).matches();
      } else {
        posPattern = null;
        posUnknown = UNKNOWN_TAG.equals(posTag);
      }
    }

    @Override
    public String toString() {
      return posTag;
    }
  }

  private static class Exceptions {
    /**  List of exceptions that are valid for the current token and / or some next tokens. */
    private List<PatternToken> currentAndNext;

    /** List of exceptions that are valid for a previous token. */
    private List<PatternToken> previous;

    private void addException(PatternToken pToken, boolean scopePrevious) {
      if (scopePrevious) {
        if (previous == null) {
          previous = new ArrayList<>();
        }
        previous.add(pToken);
      } else {
        if (currentAndNext == null) {
          currentAndNext = new ArrayList<>();
        }
        currentAndNext.add(pToken);
      }
    }
  }

}
