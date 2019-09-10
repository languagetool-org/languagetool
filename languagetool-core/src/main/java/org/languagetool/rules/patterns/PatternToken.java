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

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.InterruptibleCharSequence;
import org.languagetool.tools.StringTools;

/**
 * A part of a pattern, represents the 'token' element of the {@code grammar.xml}.
 */
public class PatternToken implements Cloneable {

  /** Matches only tokens without any POS tag. **/
  public static final String UNKNOWN_TAG = "UNKNOWN";

  /** Parameter passed to regular expression matcher to enable case insensitive Unicode matching. */
  private static final String CASE_INSENSITIVE = "(?iu)";

  private final boolean caseSensitive;
  private final boolean stringRegExp;
  private final List<PatternToken> andGroupList = new ArrayList<>();
  private final List<PatternToken> orGroupList = new ArrayList<>();
  private final boolean inflected;

  private String stringToken;
  private PosToken posToken;
  private ChunkTag chunkTag;
  private boolean negation;
  private boolean testWhitespace;
  private boolean whitespaceBefore;
  private boolean isInsideMarker = true;

  /**  List of exceptions that are valid for the current token and / or some next tokens. */
  private List<PatternToken> exceptionList;

  /** True if scope=="next". */
  private boolean exceptionValidNext;

  /** True if any exception with a scope=="current" or scope=="next" is set for the element. */
  private boolean exceptionSet;

  /** True if attribute scope=="previous". */
  private boolean exceptionValidPrevious;

  /** List of exceptions that are valid for a previous token. */
  private List<PatternToken> previousExceptionList;

  private int skip;
  private int minOccurrence = 1;
  private int maxOccurrence = 1;

  private Pattern pattern;

  /** The reference to another element in the pattern. **/
  private Match tokenReference;
  /** True when the element stores a formatted reference to another element of the pattern. */
  private String referenceString;
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
    this.caseSensitive = caseSensitive;
    this.stringRegExp = regExp;
    this.inflected = inflected;
    setStringElement(token);
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
      return isStringTokenMatched(token) ^ negation &&
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
    if (exceptionSet) {
      for (PatternToken testException : exceptionList) {
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
    andGroupList.add(Objects.requireNonNull(andToken));
  }

  /**
   * Checks if this element has an AND group associated with it.
   * @return true if the element has a group of elements that all should match.
   */
  public boolean hasAndGroup() {
    return andGroupList.size() > 0;
  }

  /**
   * Returns the group of elements linked with AND operator.
   */
  public List<PatternToken> getAndGroup() {
    return Collections.unmodifiableList(andGroupList);
  }

  /** @since 2.3 */
  public void setOrGroupElement(PatternToken orToken) {
    orGroupList.add(Objects.requireNonNull(orToken));
  }

  /**
   * Checks if this element has an OR group associated with it.
   * @return true if the element has a group of elements that all should match.
   * @since 2.3
   */
  public boolean hasOrGroup() {
    return orGroupList.size() > 0;
  }

  /**
   * Returns the group of elements linked with OR operator.
   * @since 2.3
   */
  public List<PatternToken> getOrGroup() {
    return Collections.unmodifiableList(orGroupList);
  }

  /**
   * Checks whether a previously set exception matches (in case the exception had scope == "next").
   * @param token {@link AnalyzedToken} to check matching against.
   * @return True if any of the exceptions matches.
   */
  public boolean isMatchedByScopeNextException(AnalyzedToken token) {
    if (exceptionSet) {
      for (PatternToken testException : exceptionList) {
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
    if (exceptionValidPrevious) {
      for (PatternToken testException : previousExceptionList) {
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

  @Nullable
  public String getString() {
    return stringToken;
  }

  public void setStringElement(String token) {
    if (token != null) {
      stringToken = StringTools.trimWhitespace(token);
    } else {
      stringToken = null;
    }
    testString = !StringTools.isEmpty(stringToken);
    if (testString && stringRegExp) {
      String regToken = stringToken;
      if (!caseSensitive) {
        regToken = CASE_INSENSITIVE + stringToken;
      }
      if (!"\\0".equals(token)) {
        pattern = Pattern.compile(regToken);
      }
    }
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
    PatternToken exception = new PatternToken(token, caseSensitivity == null ? caseSensitive : caseSensitivity, regExp, inflected);
    exception.setNegation(negation);
    exception.setPosToken(new PosToken(posToken, posRegExp, posNegation));
    exception.exceptionValidNext = scopeNext;
    setException(exception, scopePrevious);
  }

  private void setException(PatternToken pToken, boolean scopePrevious) {
    exceptionValidPrevious |= scopePrevious;
    if (exceptionList == null && !scopePrevious) {
      exceptionList = new ArrayList<>();
    }
    if (previousExceptionList == null && scopePrevious) {
      previousExceptionList = new ArrayList<>();
    }
    if (scopePrevious) {
      previousExceptionList.add(pToken);
    } else {
      if (!exceptionSet) {
        exceptionSet = true;
      }
      exceptionList.add(pToken);
    }
  }

  /**
   * Tests if part of speech matches a given string.
   * Special value UNKNOWN_TAG matches null POS tags.
   * @param token Token to test.
   * @return true if matches
   */
  private boolean isPosTokenMatched(AnalyzedToken token) {
    if (posToken == null || posToken.posTag == null) {
      // if no POS set defaulting to true
      return true;
    }
    if (token.getPOSTag() == null) {
      return posToken.posUnknown && token.hasNoTag();
    }
    boolean match;
    if (posToken.regExp) {
      Matcher mPos = posToken.posPattern.matcher(token.getPOSTag());
      match = mPos.matches();
    } else {
      match = posToken.posTag.equals(token.getPOSTag());
    }
    if (!match && posToken.posUnknown) { // ignore helper tags
      match = token.hasNoTag();
    }
    return match;
  }

  /**
   * Tests whether the string token element matches a given token.
   * @param token {@link AnalyzedToken} to match against.
   * @return True if matches.
   */
  private boolean isStringTokenMatched(AnalyzedToken token) {
    String testToken = getTestToken(token);
    if (stringRegExp) {
      Matcher m = pattern.matcher(new InterruptibleCharSequence(testToken));
      return m.matches();
    }
    if (caseSensitive) {
      return stringToken.equals(testToken);
    }
    return stringToken.equalsIgnoreCase(testToken);
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
    return minOccurrence;
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
    skip = i;
  }

  /**
   * The minimum number of times this element may occur.
   * @param i currently only {@code 0} and {@code 1} are supported
   */
  public void setMinOccurrence(int i) {
    if (i != 0 && i != 1) {
      throw new IllegalArgumentException("minOccurrences must be 0 or 1: " + i);
    }
    minOccurrence = i;
  }

  /**
   * The maximum number of times this element may occur.
   * @param i a number &gt;= 1 or {@code -1} for unlimited occurrences
   */
  public void setMaxOccurrence(int i) {
    if (i == 0) {
      throw new IllegalArgumentException("maxOccurrences may not be 0");
    }
    maxOccurrence = i;
  }

  /**
   * Checks if the element has an exception for a previous token.
   * @return True if the element has a previous token matching exception.
   */
  public boolean hasPreviousException() {
    return exceptionValidPrevious;
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
    if (StringTools.isEmpty(referenceString)) {
      referenceString = stringToken;
    }
    String reference = "\\" + tokenReference.getTokenRef();
    if (tokenReference.setsPos()) {
      String posReference = matchState.getTargetPosTag();
      if (posReference != null) {
        setPosToken(new PosToken(posReference, tokenReference.posRegExp(), negation));
      }
      setStringElement(referenceString.replace(reference, ""));
    } else {
      setStringElement(referenceString.replace(reference, matchState.toTokenString()));
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
    return caseSensitive;
  }

  /**
   * Tests whether the element matches a regular expression.
   * @since 0.9.6
   */
  public boolean isRegularExpression() {
    return stringRegExp;
  }

  /**
   * Tests whether the POS matches a regular expression.
   * @since 1.3.0
   */
  public boolean isPOStagRegularExpression() {
    return posToken != null && posToken.regExp;
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
    this.unificationNeutral = true;
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
    if (previousExceptionList != null && exceptionValidPrevious) {
      previousExceptionList.get(previousExceptionList.size() - 1).setWhitespaceBefore(isWhite);
    } else {
      if (exceptionList != null) {
        exceptionList.get(exceptionList.size() - 1).setWhitespaceBefore(isWhite);
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
  public List<PatternToken> getExceptionList() {
    return exceptionList;
  }

  /**
   * @return List of previous exceptions. Used for testing.
   */
  public List<PatternToken> getPreviousExceptionList() {
    return previousExceptionList;
  }

  public boolean hasExceptionList() {
    return exceptionList != null || previousExceptionList != null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (negation) {
      sb.append('!');
    }
    sb.append(stringToken);
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
    if (exceptionList != null) {
      sb.append("/exceptions=");
      sb.append(exceptionList);
    }
    return sb.toString();
  }

  public static class PosToken {

    private final String posTag;
    private final boolean regExp;
    private final boolean negation;
    private final Pattern posPattern;
    private final boolean posUnknown;

    public PosToken(String posTag, boolean regExp, boolean negation) {
      this.posTag = posTag;
      this.regExp = regExp;
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
}
