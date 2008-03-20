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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.JLanguageTool;

/**
 * A part of a pattern.
 * 
 * @author Daniel Naber
 */
public class Element {

  private String stringToken;
  private String posToken;
  private String regToken;
  private boolean posRegExp = false;  

  private boolean negation = false;
  private boolean posNegation = false;
  
  private boolean caseSensitive = false;
  private boolean stringRegExp = false;
  private boolean inflected = false;

  /**
   * List of exceptions that are valid for 
   * the current token and / or some next
   * tokens.
   */
  private ArrayList<Element> exceptionList;
  
  /**
   * True if scope=="next".
   */
  private boolean exceptionValidNext = false;
  
  /** 
   * True if any exception with a scope=="current"
   * or scope=="next" is set for the element.
   */
  private boolean exceptionSet = false;
  
  /**
   * True if attribute scope=="previous".
   */
  private boolean exceptionValidPrevious = false;
  
  /**
   * List of exceptions that are valid for 
   * a previous token.
   */
  private ArrayList<Element> previousExceptionList;
  
  private ArrayList<Element> andGroupList;  
  private boolean andGroupSet = false;  
  private boolean[] andGroupCheck;

  private int skip = 0;
  
  private Pattern p = null;
  private Pattern pPos = null;
  
  private Matcher m = null;
  private Matcher mPos = null;
    
  /** The reference to another element in the pattern. **/
  private Match tokenReference = null;
  
  /** True when the element stores a formatted reference
   * to another element of the pattern.
   */
  private boolean containsMatches = false;
  
  /** Matches only tokens without any POS tag. **/
  private static final String UNKNOWN_TAG = "UNKNOWN";
  
  /** Parameter passed to regular expression matcher
   * to enable case insensitive Unicode matching.
   */
  private static final String CASE_INSENSITIVE = "(?iu)";
  
  private String referenceString = "";

  /** String ID of the phrase the element is in. **/
  private String phraseName = null; 

  
  public Element(final String token, final boolean caseSensitive, final boolean regExp,
      final boolean inflected) {
    this.stringToken = token;
    this.caseSensitive = caseSensitive;
    this.stringRegExp = regExp;
    this.inflected = inflected;
    if (!"".equals(stringToken) && stringRegExp) {
      regToken = stringToken;
      if (!caseSensitive) {
        regToken = CASE_INSENSITIVE + stringToken;
      }
      p = Pattern.compile(regToken);
    }
  }

  /**
   * Checks whether the rule element matches the token
   * given as a parameter.
   * @param token @AnalyzedToken to check matching against
   * @return True if token matches, false otherwise.
   */
  public final boolean match(final AnalyzedToken token) {
    // this var is used to determine
    // if calling matchStringToken
    // has any sense - this method takes
    // most time so it's best reduce the
    // number it's being called
    boolean testString = true;
    if (stringToken == null) {
      testString = false;
    } else {
    if (("").equals(stringToken)) {
      testString = false;
      }
    }
    boolean matched;
    if (testString) {
      matched = (matchStringToken(token) != negation) 
          && (matchPosToken(token) != posNegation);
    } else {
      matched = (!negation) && (matchPosToken(token) != posNegation);
    }
    
    if (andGroupSet) {
      andGroupCheck[0] |= matched;
    }
    
    return matched;
  }
  
  /**
   * Checks whether an exception matches.
   * @param token @AnalyzedToken to check matching against
   * @return True if any of the exceptions matches (logical disjunction).
   */
  public final boolean exceptionMatch(final AnalyzedToken token) {
    boolean exceptionMatched = false;
    if (exceptionSet) {
      for (final Element testException : exceptionList) {
        if (!testException.exceptionValidNext) {
          exceptionMatched |= testException.match(token);
        }
        if (exceptionMatched) {
          break;
        }
      }
    }
    return exceptionMatched;
  }
  
  /**
   * Enables testing multiple conditions specified by
   * different elements. Doesn't test exceptions.
   * 
   * Works as logical AND operator only if preceded
   * with setupAndGroup(), and followed by checkAndGroup().
   * 
   * @param token AnalyzedToken - the token checked. 
   * @return true if any condition is met, false otherwise.
   */
  public final boolean andGroupMatch(final AnalyzedToken token) {
    boolean andGroupMatched = false;
    if (andGroupSet) {
      for (int i = 0; i < andGroupList.size(); i++) {
        if (!andGroupCheck[i + 1]) {
        final Element testAndGroup = andGroupList.get(i);
        if (testAndGroup.match(token)) {
          andGroupMatched = true;
          andGroupCheck[i + 1] = true;
        }
        }
      }
    }
    return andGroupMatched;
  }
  
  public final void setupAndGroup() {
    if (andGroupSet) {
    andGroupCheck = new boolean[andGroupList.size() + 1];
    for (int i = 0; i < andGroupList.size(); i++) {
      andGroupCheck[i] = false;
    }
    }
  }
  
  public final boolean checkAndGroup(final boolean previousValue) {
    if (andGroupSet) {
      boolean allConditionsMatch = true;
      for (final boolean testValue : andGroupCheck) {
        allConditionsMatch &= testValue;
      }
      return allConditionsMatch;
    } else {
      return previousValue;
    }
  }
  
  /**
   * Enables testing multiple conditions specified by
   * multiple element exceptions.
   * 
   * Works as logical AND operator.
   * 
   * @param token AnalyzedToken - the token checked for exceptions. 
   * @return true if all conditions are met, false otherwise.
   */
  public final boolean andGroupExceptionMatch(final AnalyzedToken token) {
    boolean andGroupExceptionMatched = false;
    if (andGroupSet) {
      for (final Element testAndGroup : andGroupList) {
        andGroupExceptionMatched |= testAndGroup.exceptionMatch(token);
        if (andGroupExceptionMatched) {
          return andGroupExceptionMatched;
        }
      }
    }
    return andGroupExceptionMatched;
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
   * @return true if the element has a group of elements that
   * all should match.
   */
  public final boolean hasAndGroup() {
    return andGroupSet;
  }
  
  /**
   * Returns the group of elements linked with AND
   * operator.
   * @return List of Elements.
   */
  public final ArrayList<Element> getAndGroup() {
    return andGroupList;
  }
  
  /**
   * Checks whether a previously set exception matches
   * (in case the exception had scope == "next").
   * @param token @AnalyzedToken to check matching against.
   * @return True if any of the exceptions matches.
   */
  public final boolean scopeNextExceptionMatch(final AnalyzedToken token) {
    boolean exceptionMatched = false;
    if (exceptionSet) {      
      for (final Element testException : exceptionList) {
        if (testException.exceptionValidNext) {
          exceptionMatched |= testException.match(token);
        }
        if (exceptionMatched) {
          break;
        }
      }
    }
    return exceptionMatched;
  }
  
  /**
   * Checks whether an exception for a previous token matches
   * (in case the exception had scope == "previous").
   * @param token @AnalyzedToken to check matching against.
   * @return True if any of the exceptions matches.
   */
  public final boolean scopePreviousExceptionMatch(final AnalyzedToken token) {
    boolean exceptionMatched = false;
    if (exceptionValidPrevious) {      
      for (final Element testException : previousExceptionList) {
        if (!testException.exceptionValidNext) {
          exceptionMatched |= testException.match(token);
        }
        if (exceptionMatched) {
          break;
        }
      }
    }
    return exceptionMatched;
  }
  
  
  /**
   * Checks if the token is a SENT_START.
   * @return True if the token starts the sentence.
   */
  public final boolean isSentStart() {
    boolean equals = false;
    if (posToken != null) {
      // not sure if this should be logical AND
      equals = JLanguageTool.SENTENCE_START_TAGNAME.equals(posToken) && posNegation;
    }
    return equals;
  }
  
  @Override
  public final String toString() {
    String negate = "";
    String tokString = stringToken;
    if (phraseName != null) {
    tokString += " {" + phraseName + "}";
    }
    
    if (negation) {
      negate = "!"; 
    }
    if (posToken != null) {
      return negate + tokString + "/" + posToken;
    } else {
      return negate + tokString;
    }
  }

  public final void setPosElement(final String posToken, final boolean regExp, final boolean negation) {
    this.posToken = posToken;
    this.posNegation = negation;
    posRegExp = regExp;
    if (posRegExp) {
      pPos = Pattern.compile(posToken);
    }
  }

  public final String getString() {
    return stringToken;
  }
  
  public final void setStringElement(final String token) {
    this.stringToken = token;
    if (!"".equals(stringToken) && stringRegExp) {
      regToken = stringToken;
      if (!caseSensitive) {
        regToken = CASE_INSENSITIVE + stringToken;
      }
      p = Pattern.compile(regToken);
    }
  }
  /**
   * Sets a POS-type exception for matching string tokens.
   * @param posToken The part of the speech tag in the exception.
   * @param regExp True if the POS is specified as a regular
   * expression.
   * @param negation True if the exception is negated.
   * @param scopeNext True if the exception scope is next tokens.
   * @param scopePrevious True if the exception should match only a single
   * previous token.
   */
  public final void setPosException(final String posToken, final boolean regExp,
      final boolean negation, final boolean scopeNext, final boolean scopePrevious) {
    final Element posException = 
        new Element("", this.caseSensitive, regExp, false);
    posException.setPosElement(posToken, regExp, negation);
    posException.exceptionValidNext = scopeNext;
    exceptionValidPrevious = scopePrevious;
    if (exceptionList == null && !scopePrevious) {
      exceptionList = new ArrayList<Element>();
    }
    if (previousExceptionList == null && scopePrevious) {
      previousExceptionList = new ArrayList <Element>();
    }
    if (!exceptionSet && !scopePrevious) {
      exceptionSet = true;
    }
    if (exceptionSet && !scopePrevious) {
    exceptionList.add(posException);
    } 
    if (scopePrevious) {
      previousExceptionList.add(posException);
    }
  }

  /**
   * Sets a string-type exception for matching string tokens.
   * @param token The string in the exception.
   * @param regExp True if the string is specified as a regular
   * expression.
   * @param inflected True if the string is a base form (lemma).
   * @param negation True if the exception is negated.
   * @param scopeNext True if the exception scope is next tokens.
   * @param scopePrevious True if the exception should match only a single
   * previous token.
   */
  public final void setStringException(final String token, final boolean regExp,
      final boolean inflected, final boolean negation, final boolean scopeNext,
      final boolean scopePrevious) {
    final Element stringException = new Element(
          token, this.caseSensitive, regExp, inflected);
    stringException.setNegation(negation);
    stringException.exceptionValidNext = scopeNext;
    exceptionValidPrevious = scopePrevious;
    if (exceptionList == null && !scopePrevious) {
      exceptionList = new ArrayList<Element>();
    }
    if (previousExceptionList == null && scopePrevious) {
      previousExceptionList = new ArrayList <Element>();
    }
    if (!exceptionSet && !scopePrevious) {
      exceptionSet = true;
    }
    if (exceptionSet && !scopePrevious) {
    exceptionList.add(stringException);
    } 
    if (scopePrevious) {
      previousExceptionList.add(stringException);
    }
  }

  /**
   * Tests if part of speech matches a given string.
   * @param token Token to test.
   * @return true if matches
   * 
   * Special value UNKNOWN_TAG matches null POS tags.
   * 
   */
  final boolean matchPosToken(final AnalyzedToken token) {
    
    if (token.getPOSTag() == null) {
      if (!posRegExp) {
        if (UNKNOWN_TAG.equals(posToken)) {
              return true;
        }
      } else {
        if (mPos == null) {
          mPos = pPos.matcher(UNKNOWN_TAG);
        } else {
          mPos.reset(UNKNOWN_TAG);
        }
        return mPos.matches();
      }        
    }
    // if no POS set
    // defaulting to true
    if (posToken == null) {
      return true;
    }
    boolean match = false;
    if (!posRegExp) {
      match = posToken.equals(token.getPOSTag());              
    } else if (token.getPOSTag() != null) {
      if (mPos == null) {
        mPos = pPos.matcher(token.getPOSTag());
      } else {
        mPos.reset(token.getPOSTag());
      }
      match = mPos.matches();                     
    }
    return match;
  }

  /**
   * Tests whether the string token element matches a given token.
   * @param token @AnalyzedToken to match against.
   * @return True if matches.
   */
  final boolean matchStringToken(final AnalyzedToken token) {
    String testToken = null;
    // enables using words with lemmas and without lemmas
    // in the same regexp with inflected="yes"
    if (inflected) {
      testToken = token.getLemma();
      if (testToken == null) {
        testToken = token.getToken();
      }
    } else {
      testToken = token.getToken();
    }

    if (!stringRegExp) {
      if (caseSensitive) {
        return stringToken.equals(testToken);
      } else {
        return stringToken.equalsIgnoreCase(testToken);
      }
    } else {
      if (token.getToken() != null) {
        if (m == null) {
          m = p.matcher(testToken);
        } else {
          m.reset(testToken);
        }
        return m.matches();
      }
    }

    return false;
  }

  /**
   * Gets the exception scope length.
   * @return @int Scope length.
   */
  public final int getSkipNext() {
    return skip;
  }

  /**
   * Sets the exception scope length.
   * @param i @int Exception scope length.
   */
  public final void setSkipNext(final int i) {
    skip = i;
  }

  /**
   * Checks if the element has an exception for a
   * previous token.
   * @return True if the element has a previous token 
   * matching exception.
   */
  public final boolean hasPreviousException() {
    return exceptionValidPrevious;
  }
  
  /**
   * Negates the meaning of match().
   * @param negation - true if the meaning of match()
   * is to be negated.
   */
  public final void setNegation(final boolean negation) {
    this.negation = negation;
  }

  /**
   * 
   * @return true when this element refers to another token.
   */
  public final boolean referenceElement() {
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
   * Prepare Element for matching by formatting its string token
   * and POS (if the Element is supposed to refer to some other
   * token).
   *
   */
  public final void compile() {
    if ("".equals(referenceString)) {
      referenceString = stringToken;
    }
    final boolean setsPos = tokenReference.setsPos(); 
    if (!setsPos) {
    stringToken = referenceString.replaceAll("\\\\"  
        + tokenReference.getTokenRef(), 
        tokenReference.toTokenString());
    }
    if (!"".equals(stringToken) && stringRegExp) {
      regToken = stringToken;
      if (!caseSensitive) {
        regToken = CASE_INSENSITIVE + stringToken;
      }
      p = Pattern.compile(regToken);
    }
    if (setsPos) {
      final String posReference = tokenReference.getTargetPosTag();
      if (posReference != null) {
        if (mPos != null) {
          mPos = null;
        }
      setPosElement(posReference, tokenReference.posRegExp(), negation);
      }
      stringToken = referenceString.replaceAll("\\\\"  
          + tokenReference.getTokenRef(), "");
      inflected = true;      
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
   * @return True if the Element is contained
   * in the phrase.
   */
  public final boolean isPartOfPhrase() {
    return phraseName != null;
  }
  
  /** 
   * Gets the phrase the element is in.
   * @return String The name of the phrase.
   */
  public final String getPhraseName() {
    return phraseName;
  }
  
}
