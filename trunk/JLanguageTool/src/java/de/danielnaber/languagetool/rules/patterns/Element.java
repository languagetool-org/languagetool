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
package de.danielnaber.languagetool.rules.patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * A part of a pattern.
 * 
 * @author Daniel Naber
 */
public class Element {

  private String stringToken;

  private String posToken;

  private String regToken;

  private boolean posRegExp;

  private boolean negation;

  private boolean posNegation;

  private final boolean caseSensitive;

  private final boolean stringRegExp;

  private boolean inflected;

  private boolean testWhitespace;

  private boolean whitespaceBefore;

  /**
   * List of exceptions that are valid for the current token and / or some next tokens.
   */
  private List<Element> exceptionList;

  /**
   * True if scope=="next".
   */
  private boolean exceptionValidNext;

  /**
   * True if any exception with a scope=="current" or scope=="next" is set for the element.
   */
  private boolean exceptionSet;

  /**
   * True if attribute scope=="previous".
   */
  private boolean exceptionValidPrevious;

  /**
   * List of exceptions that are valid for a previous token.
   */
  private List<Element> previousExceptionList;

  private List<Element> andGroupList;

  private boolean andGroupSet;

  private boolean[] andGroupCheck;

  private int skip;

  private Pattern p;

  private Pattern pPos;

  private Matcher m;

  private Matcher mPos;

  /** The reference to another element in the pattern. **/
  private Match tokenReference;

  /**
   * True when the element stores a formatted reference to another element of the pattern.
   */
  private boolean containsMatches;

  /** Matches only tokens without any POS tag. **/
  public static final String UNKNOWN_TAG = "UNKNOWN";

  /**
   * Parameter passed to regular expression matcher to enable case insensitive Unicode matching.
   */
  private static final String CASE_INSENSITIVE = "(?iu)";

  private String referenceString;

  /** String ID of the phrase the element is in. **/
  private String phraseName;

  /**
   * This var is used to determine if calling {@link #setStringElement} makes sense. This method
   * takes most time so it's best to reduce the number of its calls.
   **/
  private boolean testString;

  /**
   * Tells if the element is inside the unification, so that {@link Unifier} tests it.
   */
  private boolean unified;

  private boolean uniNegation;

  private Map<String, List<String>> unificationFeatures;
  
  private boolean posUnknown;

  /**
   * Creates Element that is used to match tokens in the text.
   * 
   * @param token
   *          String to be matched
   * @param caseSensitive
   *          True if the check is case-sensitive.
   * @param regExp
   *          True if the check uses regular expressions.
   * @param inflected
   *          True if the check refers to base forms (lemmas).
   */
  public Element(final String token, final boolean caseSensitive, final boolean regExp,
      final boolean inflected) {
    this.caseSensitive = caseSensitive;
    this.stringRegExp = regExp;
    this.inflected = inflected;
    setStringElement(token);
  }

  /**
   * Checks whether the rule element matches the token given as a parameter.
   * 
   * @param token
   * @AnalyzedToken to check matching against
   * @return True if token matches, false otherwise.
   */
  public final boolean isMatched(final AnalyzedToken token) {
    if (testWhitespace && !isWhitespaceBefore(token)) {
      return false;
    }
    boolean matched = false;
    if (testString) {
      matched = (isStringTokenMatched(token) ^ negation)
          && (isPosTokenMatched(token) ^ posNegation);
    } else {
      matched = (!negation) && (isPosTokenMatched(token) ^ posNegation);
    }

    if (andGroupSet) {
      andGroupCheck[0] |= matched;
    }
    return matched;
  }

  /**
   * Checks whether an exception matches.
   * 
   * @param token
   * @AnalyzedToken to check matching against
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
   * Enables testing multiple conditions specified by different elements. Doesn't test exceptions.
   * 
   * Works as logical AND operator only if preceded with {@link #setupAndGroup()}, and followed by
   * {@link #checkAndGroup(boolean)}.
   * 
   * @param token
   *          AnalyzedToken - the token checked.
   */
  public final void addMemberAndGroup(final AnalyzedToken token) {
    if (andGroupSet) {
      for (int i = 0; i < andGroupList.size(); i++) {
        if (!andGroupCheck[i + 1]) {
          final Element testAndGroup = andGroupList.get(i);
          if (testAndGroup.isMatched(token)) {
            andGroupCheck[i + 1] = true;
          }
        }
      }
    }
  }

  public final void setupAndGroup() {
    if (andGroupSet) {
      andGroupCheck = new boolean[andGroupList.size() + 1];
      Arrays.fill(andGroupCheck, false);
    }
  }

  public final boolean checkAndGroup(final boolean previousValue) {
    if (andGroupSet) {
      boolean allConditionsMatch = true;
      for (final boolean testValue : andGroupCheck) {
        allConditionsMatch &= testValue;
      }
      return allConditionsMatch;
    }
    return previousValue;
  }

  /**
   * Enables testing multiple conditions specified by multiple element exceptions.
   * 
   * Works as logical AND operator.
   * 
   * @param token
   *          AnalyzedToken - the token checked for exceptions.
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
   * 
   * @param token
   *          Token to match
   * @return True if matched.
   */
  public final boolean isExceptionMatchedCompletely(final AnalyzedToken token) {
    // note: short-circuiting possible
    return isExceptionMatched(token) || isAndExceptionGroupMatched(token);
  }

  public final void setAndGroupElement(final Element andToken) {
    if (andToken != null) {
      if (andGroupList == null) {
        andGroupList = new ArrayList<Element>();
      }
      if (!andGroupSet) {
        andGroupSet = true;
      }
      andGroupList.add(andToken);
    }
  }

  /**
   * Checks if this element has an AND group associated with it.
   * 
   * @return true if the element has a group of elements that all should match.
   */
  public final boolean hasAndGroup() {
    return andGroupSet;
  }

  /**
   * Returns the group of elements linked with AND operator.
   * 
   * @return List of Elements.
   */
  public final List<Element> getAndGroup() {
    return andGroupList;
  }

  /**
   * Checks whether a previously set exception matches (in case the exception had scope == "next").
   * 
   * @param token
   * @AnalyzedToken to check matching against.
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
   * @param token
   *          {@link AnalyzedToken} to check matching against.
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
   * @param prevToken
   *          {@link AnalyzedTokenReadings} to check matching against.
   * @return true if any of the exceptions matches.
   */
  public final boolean isMatchedByPreviousException(final AnalyzedTokenReadings prevToken) {
    final int numReadings = prevToken.getReadingsLength();
    for (int i = 0; i < numReadings; i++) {
      if (isMatchedByPreviousException(prevToken.getAnalyzedToken(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the token is a SENT_START.
   * 
   * @return True if the element starts the sentence and the element hasn't been set to have negated
   *         POS token.
   * 
   */
  public final boolean isSentStart() {
    return JLanguageTool.SENTENCE_START_TAGNAME.equals(posToken) && !posNegation;
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
    return sb.toString();
  }

  public final void setPosElement(final String posToken, final boolean regExp,
      final boolean negation) {
    this.posToken = posToken;
    this.posNegation = negation;
    posRegExp = regExp;
    if (posRegExp) {
      pPos = Pattern.compile(posToken);
      if (mPos == null) {
        mPos = pPos.matcher(UNKNOWN_TAG);
      } else {
        mPos.reset(UNKNOWN_TAG);
      }
      posUnknown = mPos.matches();        
    } else {
      posUnknown = UNKNOWN_TAG.equals(posToken); 
    }
  }

  public final String getString() {
    return stringToken;
  }

  public final void setStringElement(final String token) {
    this.stringToken = token;
    testString = !StringTools.isEmpty(stringToken);
    if (testString && stringRegExp) {
      regToken = stringToken;
      if (!caseSensitive) {
        regToken = CASE_INSENSITIVE + stringToken;
      }
      if (!"\\0".equals(token)) {
        p = Pattern.compile(regToken);
      }
    }
  }

  /**
   * Sets a POS-type exception for matching string tokens.
   * 
   * @param posToken
   *          The part of the speech tag in the exception.
   * @param regExp
   *          True if the POS is specified as a regular expression.
   * @param negation
   *          True if the exception is negated.
   * @param scopeNext
   *          True if the exception scope is next tokens.
   * @param scopePrevious
   *          True if the exception should match only a single previous token.
   */
  public final void setPosException(final String posToken, final boolean regExp,
      final boolean negation, final boolean scopeNext, final boolean scopePrevious) {
    final Element posException = new Element("", this.caseSensitive, false, false);
    posException.setPosElement(posToken, regExp, negation);
    posException.exceptionValidNext = scopeNext;
    setException(posException, scopePrevious);
  }

  /**
   * Sets a string-type exception for matching string tokens.
   * 
   * @param token
   *          The string in the exception.
   * @param regExp
   *          True if the string is specified as a regular expression.
   * @param inflected
   *          True if the string is a base form (lemma).
   * @param negation
   *          True if the exception is negated.
   * @param scopeNext
   *          True if the exception scope is next tokens.
   * @param scopePrevious
   *          True if the exception should match only a single previous token.
   */
  public final void setStringException(final String token, final boolean regExp,
      final boolean inflected, final boolean negation, final boolean scopeNext,
      final boolean scopePrevious) {
    final Element stringException = new Element(token, this.caseSensitive, regExp, inflected);
    stringException.setNegation(negation);
    stringException.exceptionValidNext = scopeNext;
    setException(stringException, scopePrevious);
  }

  private void setException(final Element elem, final boolean scopePrevious) {
    exceptionValidPrevious |= scopePrevious;
    if (exceptionList == null && !scopePrevious) {
      exceptionList = new ArrayList<Element>();
    }
    if (previousExceptionList == null && scopePrevious) {
      previousExceptionList = new ArrayList<Element>();
    }
    if (scopePrevious) {
      previousExceptionList.add(elem);
    } else {
      if (!exceptionSet) {
        exceptionSet = true;
      }
      if (exceptionSet) {
        exceptionList.add(elem);
      }
    }
  }

  /**
   * Tests if part of speech matches a given string.
   * 
   * @param token
   *          Token to test.
   * @return true if matches
   * 
   *         Special value UNKNOWN_TAG matches null POS tags.
   * 
   */
  private boolean isPosTokenMatched(final AnalyzedToken token) {
    // if no POS set
    // defaulting to true
    if (posToken == null) {
      return true;
    }
    if (token.getPOSTag() == null) {      
      if (posUnknown) {
        return token.hasNoTag();
      }
      return false;
    }
    boolean match;
    if (posRegExp) {
      if (mPos == null) {
        mPos = pPos.matcher(token.getPOSTag());
      } else {
        mPos.reset(token.getPOSTag());
      }
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
   * 
   * @param token
   *          {@link AnalyzedToken} to match against.
   * @return True if matches.
   */
  private boolean isStringTokenMatched(final AnalyzedToken token) {
    final String testToken = getTestToken(token);
    if (stringRegExp) {
      if (m == null) {
        m = p.matcher(testToken);
      } else {
        m.reset(testToken);
      }
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
   * 
   * @return Scope length.
   */
  public final int getSkipNext() {
    return skip;
  }

  /**
   * Sets the exception scope length.
   * 
   * @param i
   *          Exception scope length.
   */
  public final void setSkipNext(final int i) {
    skip = i;
  }

  /**
   * Checks if the element has an exception for a previous token.
   * 
   * @return True if the element has a previous token matching exception.
   */
  public final boolean hasPreviousException() {
    return exceptionValidPrevious;
  }

  /**
   * Negates the meaning of match().
   * 
   * @param negation
   *          - true if the meaning of match() is to be negated.
   */
  public final void setNegation(final boolean negation) {
    this.negation = negation;
  }

  /**
   * see {@link #setNegation}
   * 
   * @since 0.9.3
   */
  public final boolean getNegation() {
    return this.negation;
  }

  /**
   * 
   * @return true when this element refers to another token.
   */
  public final boolean isReferenceElement() {
    return containsMatches;
  }

  /**
   * Sets the reference to another token.
   * 
   * @param match
   *          Formatting object for the token reference.
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
   * @param token
   *          the token specified as {@link AnalyzedTokenReadings}
   * @param synth
   *          the language synthesizer ({@link Synthesizer})
   * 
   */
  public final void compile(final AnalyzedTokenReadings token, final Synthesizer synth)
      throws IOException {

    m = null;
    p = null;
    tokenReference.setToken(token);
    tokenReference.setSynthesizer(synth);

    if (StringTools.isEmpty(referenceString)) {
      referenceString = stringToken;
    }
    if (tokenReference.setsPos()) {
      final String posReference = tokenReference.getTargetPosTag();
      if (posReference != null) {
        if (mPos != null) {
          mPos = null;
        }
        setPosElement(posReference, tokenReference.posRegExp(), negation);
      }
      setStringElement(referenceString.replace("\\" + tokenReference.getTokenRef(), ""));
      inflected = true;
    } else {
      setStringElement(referenceString.replace("\\" + tokenReference.getTokenRef(),
          tokenReference.toTokenString()));
    }
  }

  /**
   * Sets the phrase the element is in.
   * 
   * @param s
   *          ID of the phrase.
   */
  public final void setPhraseName(final String s) {
    phraseName = s;
  }

  /**
   * Checks if the Element is in any phrase.
   * 
   * @return True if the Element is contained in the phrase.
   */
  public final boolean isPartOfPhrase() {
    return phraseName != null;
  }

  /**
   * Whether the element matches case sensitively.
   * 
   * @since 0.9.3
   */
  public final boolean getCaseSensitive() {
    return caseSensitive;
  }

  /**
   * Tests whether the element matches a regular expression.
   * 
   * @since 0.9.6
   */
  public final boolean isRegularExpression() {
    return stringRegExp;
  }

  /**
   * Tests whether the POS matches a regular expression.
   * 
   * @since 1.3.0
   */
  public final boolean isPOStagRegularExpression() {
    return posRegExp;
  }

  /**
   * @return the POS of the Element
   * @since 0.9.6
   */
  public final String getPOStag() {
    return posToken;
  }

  /**
   * Tests whether the POS is negated.
   * 
   * @return true if so.
   */
  public final boolean getPOSNegation() {
    return posNegation;
  }

  /**
   * Whether the token is inflected.
   * 
   * @return True if so.
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
   * 
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

  public final void setWhitespaceBefore(final boolean isWhite) {
    whitespaceBefore = isWhite;
    testWhitespace = true;
  }

  public final void setExceptionSpaceBefore(final boolean isWhite) {
    if (exceptionList != null) {
      exceptionList.get(exceptionList.size()).setWhitespaceBefore(isWhite);
    }
  }

  public final boolean isWhitespaceBefore(final AnalyzedToken token) {
    return whitespaceBefore == token.isWhitespaceBefore();
  }

  /**
   * Since 1.0.0
   * 
   * @return A List of Exceptions. Used for testing.
   */
  public final List<Element> getExceptionList() {
    return exceptionList;
  }
  
  /**
   * 
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
}
