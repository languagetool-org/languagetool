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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

/**
 * A part of a pattern, represents the 'token' element of the {@code grammar.xml}.
 * 
 * @author Daniel Naber
 */
public class Element implements Cloneable {

  /** Matches only tokens without any POS tag. **/
  public static final String UNKNOWN_TAG = "UNKNOWN";

  /** Parameter passed to regular expression matcher to enable case insensitive Unicode matching. */
  private static final String CASE_INSENSITIVE = "(?iu)";

  private final boolean caseSensitive;
  private final boolean stringRegExp;

  private String stringToken;
  private String posToken;
  private ChunkTag chunkToken;
  private boolean posRegExp;
  private boolean negation;
  private boolean posNegation;
  private boolean inflected;
  private boolean testWhitespace;
  private boolean whitespaceBefore;
  private boolean isInsideMarker = true;

  /**  List of exceptions that are valid for the current token and / or some next tokens. */
  private List<Element> exceptionList;

  /** True if scope=="next". */
  private boolean exceptionValidNext;

  /** True if any exception with a scope=="current" or scope=="next" is set for the element. */
  private boolean exceptionSet;

  /** True if attribute scope=="previous". */
  private boolean exceptionValidPrevious;

  /** List of exceptions that are valid for a previous token. */
  private List<Element> previousExceptionList;
  private List<Element> andGroupList;
  private boolean andGroupSet;
  private List<Element> orGroupList;
  private boolean orGroupSet;

  private int skip;
  private int minOccurrence = 1;
  private int maxOccurrence = 1;

  private Pattern p;
  private Pattern pPos;

  /** The reference to another element in the pattern. **/
  private Match tokenReference;
  /** True when the element stores a formatted reference to another element of the pattern. */
  private boolean containsMatches;
  private String referenceString;
  /** String ID of the phrase the element is in. **/
  private String phraseName;

  /**
   * This var is used to determine if calling {@link #setStringElement} makes sense. This method
   * takes most time so it's best to reduce the number of its calls.
   */
  private boolean testString;
  /** Tells if the element is inside the unification, so that {@link Unifier} tests it.  */
  private boolean unified;

  /** Determines whether the element should be ignored when doing unification **/
  private boolean unificationNeutral;

  private boolean uniNegation;
  private Map<String, List<String>> unificationFeatures;
  private boolean posUnknown;

  /**
   * Set to true on tokens that close the unification block.
   */
  private boolean isLastUnified;

  /**
   * Creates Element that is used to match tokens in the text.
   * @param token String to be matched
   * @param caseSensitive True if the check is case-sensitive.
   * @param regExp True if the check uses regular expressions.
   * @param inflected True if the check refers to base forms (lemmas).
   */
  public Element(final String token, final boolean caseSensitive, final boolean regExp,
      final boolean inflected) {
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
  final boolean isMatched(final AnalyzedToken token) {
    if (testWhitespace && !isWhitespaceBefore(token)) {
      return false;
    }
    final boolean matched;
    if (testString) {
      matched = isStringTokenMatched(token) ^ negation
          && isPosTokenMatched(token) ^ posNegation;
    } else {
      matched = !negation
          && isPosTokenMatched(token) ^ posNegation;
    }
    return matched;
  }

  /**
   * Checks whether an exception matches.
   * @param token AnalyzedToken to check matching against
   * @return True if any of the exceptions matches (logical disjunction).
   */
  public final boolean isExceptionMatched(final AnalyzedToken token) {
    if (exceptionSet) {
      for (final Element testException : exceptionList) {
        if (!testException.exceptionValidNext) {
          if (testException.isMatched(token)) {
            return true;
          }
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
  public final boolean isAndExceptionGroupMatched(final AnalyzedToken token) {
    if (andGroupSet) {
      for (final Element testAndGroup : andGroupList) {
        if (testAndGroup.isExceptionMatched(token)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * This method checks exceptions both in AND-group and the token. Introduced to for clarity.
   * @param token Token to match
   * @return True if matched.
   */
  public final boolean isExceptionMatchedCompletely(final AnalyzedToken token) {
    // note: short-circuiting possible
    return isExceptionMatched(token) || isAndExceptionGroupMatched(token);
  }

  public final void setAndGroupElement(final Element andToken) {
    if (andToken != null) {
      if (andGroupList == null) {
        andGroupList = new ArrayList<>();
      }
      if (!andGroupSet) {
        andGroupSet = true;
      }
      andGroupList.add(andToken);
    }
  }

  /**
   * Checks if this element has an AND group associated with it.
   * @return true if the element has a group of elements that all should match.
   */
  public final boolean hasAndGroup() {
    return andGroupSet;
  }

  /**
   * Returns the group of elements linked with AND operator.
   * @return List of Elements.
   */
  public final List<Element> getAndGroup() {
    return andGroupList;
  }

  /**
   * @since 2.3
   */
  public final void setOrGroupElement(final Element orToken) {
    if (orToken != null) {
      if (orGroupList == null) {
        orGroupList = new ArrayList<>();
      }
      if (!orGroupSet) {
        orGroupSet = true;
      }
      orGroupList.add(orToken);
    }
  }

  /**
   * Checks if this element has an OR group associated with it.
   * @return true if the element has a group of elements that all should match.
   * @since 2.3
   */
  public final boolean hasOrGroup() {
    return orGroupSet;
  }

  /**
   * Returns the group of elements linked with OR operator.
   * @return List of Elements.
   * @since 2.3
   */
  public final List<Element> getOrGroup() {
    return orGroupList;
  }

  /**
   * Checks whether a previously set exception matches (in case the exception had scope == "next").
   * 
   * @param token {@link AnalyzedToken} to check matching against.
   * @return True if any of the exceptions matches.
   */
  public final boolean isMatchedByScopeNextException(final AnalyzedToken token) {
    if (exceptionSet) {
      for (final Element testException : exceptionList) {
        if (testException.exceptionValidNext) {
          if (testException.isMatched(token)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Checks whether an exception for a previous token matches (in case the exception had scope ==
   * "previous").
   * 
   * @param token {@link AnalyzedToken} to check matching against.
   * @return True if any of the exceptions matches.
   */
  public final boolean isMatchedByPreviousException(final AnalyzedToken token) {
    if (exceptionValidPrevious) {
      for (final Element testException : previousExceptionList) {
        if (!testException.exceptionValidNext) {
          if (testException.isMatched(token)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Checks whether an exception for a previous token matches all readings of a given token (in case
   * the exception had scope == "previous").
   * 
   * @param prevToken {@link AnalyzedTokenReadings} to check matching against.
   * @return true if any of the exceptions matches.
   */
  public final boolean isMatchedByPreviousException(final AnalyzedTokenReadings prevToken) {
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
  public final boolean isSentenceStart() {
    return JLanguageTool.SENTENCE_START_TAGNAME.equals(posToken) && !posNegation;
  }

  public final void setPosElement(final String posToken, final boolean regExp,
      final boolean negation) {
    this.posToken = posToken;
    this.posNegation = negation;
    posRegExp = regExp;
    if (posRegExp) {
      pPos = Pattern.compile(posToken);
      final Matcher mPos = pPos.matcher(UNKNOWN_TAG);
      posUnknown = mPos.matches();
    } else {
      posUnknown = UNKNOWN_TAG.equals(posToken);
    }
  }

  /**
   * @since 2.3
   */
  public final void setChunkElement(final ChunkTag chunkTag) {
    this.chunkToken = chunkTag;
  }

  public final String getString() {
    return stringToken;
  }

  public final void setStringElement(final String token) {
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
        p = Pattern.compile(regToken);
      }
    }
  }

  /**
   * Sets a string and/or pos exception for matching tokens.
   * 
   * @param token The string in the exception.
   * @param regExp True if the string is specified as a regular expression.
   * @param inflected True if the string is a base form (lemma).
   * @param negation True if the exception is negated.
   * @param scopeNext True if the exception scope is next tokens.
   * @param scopePrevious True if the exception should match only a single previous token.
   * @param posToken The part of the speech tag in the exception.
   * @param posRegExp True if the POS is specified as a regular expression.
   * @param posNegation True if the POS exception is negated.
   */
  public final void setStringPosException(
      final String token, final boolean regExp, final boolean inflected,
      final boolean negation, final boolean scopeNext, final boolean scopePrevious,
      final String posToken, final boolean posRegExp, final boolean posNegation) {

    final Element exception = new Element(token, caseSensitive, regExp, inflected);
    exception.setNegation(negation);
    exception.setPosElement(posToken, posRegExp, posNegation);
    exception.exceptionValidNext = scopeNext;
    setException(exception, scopePrevious);
  }

  /**
   * Sets a string and/or pos exception for matching tokens.
   *
   * @param token The string in the exception.
   * @param regExp True if the string is specified as a regular expression.
   * @param inflected True if the string is a base form (lemma).
   * @param negation True if the exception is negated.
   * @param scopeNext True if the exception scope is next tokens.
   * @param scopePrevious True if the exception should match only a single previous token.
   * @param posToken The part of the speech tag in the exception.
   * @param posRegExp True if the POS is specified as a regular expression.
   * @param posNegation True if the POS exception is negated.
   * @param caseSensitivity True if exception is case sensitive (in a different way than the whole token).
   * @since 2.6
   */
  public final void setStringPosException(
      final String token, final boolean regExp, final boolean inflected,
      final boolean negation, final boolean scopeNext, final boolean scopePrevious,
      final String posToken, final boolean posRegExp, final boolean posNegation, final boolean caseSensitivity) {

    final Element exception = new Element(token, caseSensitivity, regExp, inflected);
    exception.setNegation(negation);
    exception.setPosElement(posToken, posRegExp, posNegation);
    exception.exceptionValidNext = scopeNext;
    setException(exception, scopePrevious);
  }


  private void setException(final Element elem, final boolean scopePrevious) {
    exceptionValidPrevious |= scopePrevious;
    if (exceptionList == null && !scopePrevious) {
      exceptionList = new ArrayList<>();
    }
    if (previousExceptionList == null && scopePrevious) {
      previousExceptionList = new ArrayList<>();
    }
    if (scopePrevious) {
      previousExceptionList.add(elem);
    } else {
      if (!exceptionSet) {
        exceptionSet = true;
      }
      exceptionList.add(elem);
    }
  }

  /**
   * Sets an exception for matching Optional tokens.
   * 
   * @param token The string in the exception.
   * @param regExp True if the string is specified as a regular expression.
   * @param inflected True if the string is a base form (lemma).
   * @param negation True if the exception is negated.
   * @param posToken The part of the speech tag in the exception.
   * @param posRegExp True if the POS is specified as a regular expression.
   * @param posNegation True if the POS exception is negated.
   * @since 2.5
   */
  public final void setOptionalException(
      final String token, final boolean regExp, final boolean inflected,
      final boolean negation, final String posToken,
      final boolean posRegExp, final boolean posNegation) {

    final Element exception = new Element(token, caseSensitive, regExp, inflected);
    exception.setNegation(negation);
    exception.setPosElement(posToken, posRegExp, posNegation);
    setException(exception, false);
  }

  /**
   * Tests if part of speech matches a given string.
   * Special value UNKNOWN_TAG matches null POS tags.
   * @param token Token to test.
   * @return true if matches
   */
  private boolean isPosTokenMatched(final AnalyzedToken token) {
    if (posToken == null) {
      // if no POS set defaulting to true
      return true;
    }
    if (token.getPOSTag() == null) {
      return posUnknown && token.hasNoTag();
    }
    boolean match;
    if (posRegExp) {
      final Matcher mPos = pPos.matcher(token.getPOSTag());
      match = mPos.matches();
    } else {
      match = posToken.equals(token.getPOSTag());
    }
    if (!match && posUnknown) { // ignore helper tags
      match = token.hasNoTag();
    }
    return match;
  }

  /**
   * Tests whether the string token element matches a given token.
   * @param token {@link AnalyzedToken} to match against.
   * @return True if matches.
   */
  boolean isStringTokenMatched(final AnalyzedToken token) {
    final String testToken = getTestToken(token);
    if (stringRegExp) {
      final Matcher m = p.matcher(testToken);
      return m.matches();
    }
    if (caseSensitive) {
      return stringToken.equals(testToken);
    }
    return stringToken.equalsIgnoreCase(testToken);
  }

  private String getTestToken(final AnalyzedToken token) {
    // enables using words with lemmas and without lemmas
    // in the same regexp with inflected="yes"
    if (inflected) {
      return token.getTokenInflected();
    }
    return token.getToken();
  }

  /**
   * Gets the exception scope length.
   * @return scope length in tokens
   */
  public final int getSkipNext() {
    return skip;
  }

  /**
   * The minimum number of times the element needs to occur.
   */
  public final int getMinOccurrence() {
    return minOccurrence;
  }

  /**
   * The maximum number of times the element may occur.
   */
  public final int getMaxOccurrence() {
    return maxOccurrence;
  }

  /**
   * @param i exception scope length.
   */
  public final void setSkipNext(final int i) {
    skip = i;
  }

  /**
   * The minimum number of times this element may occur.
   * @param i currently only {@code 0} and {@code 1} are supported
   */
  public final void setMinOccurrence(final int i) {
    if (i != 0 && i != 1) {
      throw new IllegalArgumentException("minOccurrences must be 0 or 1: " + i);
    }
    minOccurrence = i;
  }

  /**
   * The maximum number of times this element may occur.
   * @param i a number >= 1 or {@code -1} for unlimited occurrences
   */
  public final void setMaxOccurrence(final int i) {
    if (i == 0) {
      throw new IllegalArgumentException("maxOccurrences may not be 0: " + i);
    }
    maxOccurrence = i;
  }

  /**
   * Checks if the element has an exception for a previous token.
   * @return True if the element has a previous token matching exception.
   */
  public final boolean hasPreviousException() {
    return exceptionValidPrevious;
  }

  /**
   * Checks if the element has an exception for a next scope.
   * (only used for testing)
   * @return True if the element has exception for the next scope.
   */
  public final boolean hasNextException() {
    return exceptionValidNext;
  }

  /**
   * Negates the matching so that non-matching elements match and vice-versa.
   */
  public final void setNegation(final boolean negation) {
    this.negation = negation;
  }

  /**
   * see {@link #setNegation}
   * @since 0.9.3
   */
  public final boolean getNegation() {
    return negation;
  }

  /**
   * @return true when this element refers to another token.
   */
  public final boolean isReferenceElement() {
    return containsMatches;
  }

  /**
   * Sets the reference to another token.
   * @param match Formatting object for the token reference.
   */
  public final void setMatch(final Match match) {
    tokenReference = match;
    containsMatches = true;
  }

  public final Match getMatch() {
    return tokenReference;
  }

  /**
   * Prepare Element for matching by formatting its string token and POS (if the Element is supposed
   * to refer to some other token).
   * 
   * @param token the token specified as {@link AnalyzedTokenReadings}
   * @param synth the language synthesizer ({@link Synthesizer})
   */
  public final Element compile(final AnalyzedTokenReadings token, final Synthesizer synth)
      throws IOException {
    final Element compiledElement;
    try {
      compiledElement = (Element) clone();
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Could not clone element", e);
    }
    compiledElement.doCompile(token, synth);
    return compiledElement;
  }

  void doCompile(final AnalyzedTokenReadings token, final Synthesizer synth) throws IOException {
    p = null;
    final MatchState matchState = tokenReference.createState(synth, token);

    if (StringTools.isEmpty(referenceString)) {
      referenceString = stringToken;
    }
    if (tokenReference.setsPos()) {
      final String posReference = matchState.getTargetPosTag();
      if (posReference != null) {
        setPosElement(posReference, tokenReference.posRegExp(), negation);
      }
      setStringElement(referenceString.replace("\\" + tokenReference.getTokenRef(), ""));
      inflected = true;
    } else {
      setStringElement(referenceString.replace("\\" + tokenReference.getTokenRef(),
          matchState.toTokenString()));
    }
  }

  /**
   * Sets the phrase the element is in.
   * @param s ID of the phrase.
   */
  public final void setPhraseName(final String s) {
    phraseName = s;
  }

  /**
   * Checks if the Element is in any phrase.
   * @return True if the Element is contained in the phrase.
   */
  public final boolean isPartOfPhrase() {
    return phraseName != null;
  }

  /**
   * Whether the element matches case sensitively.
   * @since 2.3
   */
  public final boolean isCaseSensitive() {
    return caseSensitive;
  }

  /**
   * Tests whether the element matches a regular expression.
   * @since 0.9.6
   */
  public final boolean isRegularExpression() {
    return stringRegExp;
  }

  /**
   * Tests whether the POS matches a regular expression.
   * @since 1.3.0
   */
  public final boolean isPOStagRegularExpression() {
    return posRegExp;
  }

  /**
   * @return the POS of the Element or {@code null}
   * @since 0.9.6
   */
  public final String getPOStag() {
    return posToken;
  }

  /**
   * @return the chunk tag of the Element or {@code null}
   * @since 2.3
   */
  public final ChunkTag getChunkTag() {
    return chunkToken;
  }

  /**
   * @return true if the POS is negated.
   */
  public final boolean getPOSNegation() {
    return posNegation;
  }

  /**
   * @return true if the token matches all inflected forms
   */
  public final boolean isInflected() {
    return inflected;
  }

  /**
   * Gets the phrase the element is in.
   * 
   * @return String The name of the phrase.
   */
  public final String getPhraseName() {
    return phraseName;
  }

  public final boolean isUnified() {
    return unified;
  }



  public final void setUnification(final Map<String, List<String>> uniFeatures) {
    unificationFeatures = uniFeatures;
    unified = true;
  }

  /**
   * Get unification features and types.
   * @return A map from features to a list of types.
   * @since 1.0.1
   */
  public final Map<String, List<String>> getUniFeatures() {
    return unificationFeatures;
  }

  public final void setUniNegation() {
    uniNegation = true;
  }

  public final boolean isUniNegated() {
    return uniNegation;
  }

  public final boolean isLastInUnification() {
    return isLastUnified;
  }

  public final void setLastInUnification() {
    isLastUnified = true;
  }

  /**
   * Determines whether the element should be silently ignored during unification,
   * and simply added.
   * @return True when the element is not included in unifying.
   * @since 2.5
   **/
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


  public final void setWhitespaceBefore(final boolean isWhite) {
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
  public final void setExceptionSpaceBefore(final boolean isWhite) {
    if (previousExceptionList != null && exceptionValidPrevious) {
      previousExceptionList.get(previousExceptionList.size() - 1).setWhitespaceBefore(isWhite);
    } else {
      if (exceptionList != null) {
        exceptionList.get(exceptionList.size() - 1).setWhitespaceBefore(isWhite);
      }
    }
  }

  public final boolean isWhitespaceBefore(final AnalyzedToken token) {
    return whitespaceBefore == token.isWhitespaceBefore();
  }

  /**
   * @return A List of Exceptions. Used for testing.
   * @since 1.0.0
   */
  public final List<Element> getExceptionList() {
    return exceptionList;
  }

  /**
   * @return List of previous exceptions. Used for testing.
   */
  public final List<Element> getPreviousExceptionList() {
    return previousExceptionList;
  }

  public final boolean hasExceptionList() {
    return exceptionList != null || previousExceptionList != null;
  }

  public final boolean testWhitespace() {
    return testWhitespace;
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
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
    if (chunkToken != null) {
      sb.append('/');
      sb.append(chunkToken);
    }
    if (exceptionList != null) {
      sb.append("/exceptions=");
      sb.append(exceptionList);
    }
    return sb.toString();
  }

}
