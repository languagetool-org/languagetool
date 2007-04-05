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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

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

  private ArrayList<Element> exceptionList;
  public boolean exceptionValidNext = true;
  private boolean exceptionSet = false;

  private int skip = 0;
  
  private Pattern p = null;
  private Pattern pPos = null;
  
  private Matcher m = null;
  private Matcher mPos = null;

  Element(final String token, final boolean caseSensitive, final boolean regExp,
      final boolean inflected) {
    this.stringToken = token;
    this.caseSensitive = caseSensitive;
    this.stringRegExp = regExp;
    this.inflected = inflected;
    if (!stringToken.equals("") && stringRegExp) {
      regToken = stringToken;
      if (!caseSensitive) {
        regToken = "(?u)".concat(stringToken);
      }
      p = Pattern.compile(regToken);
    }
  }

  final boolean match(final AnalyzedToken token) {
    // this var is used to determine
    // if calling matchStringToken
    // has any sense - this method takes
    // most time so it's best reduce the
    // number it's being called
    boolean testString = true;
    if (stringToken == null) {
      testString = false;
    } else {
    if (stringToken.equals("")) {
      testString = false;
      }
    }
    if (testString) {
      return (matchStringToken(token) != negation) && (matchPosToken(token) != posNegation);
    } else {
      return (!negation) && (matchPosToken(token) != posNegation);
    }
  }

  /** Tests if the exception is valid for next tokens 
   * (used to define exception scope). 
   * 
   * @author Marcin Milkowski
   * 
   * @return boolean 
   * 
   */  
  final boolean exceptionValid() {
    boolean eNext = false;
    if (exceptionSet) {
      for (Element testException : exceptionList) {
        eNext |= testException.exceptionValidNext;
        if (eNext) {
          break;
        }
      }
    }
    return eNext;
  }
  
  final boolean exceptionMatch(final AnalyzedToken token) {
    boolean exceptionMatched = false;
    if (exceptionSet) {
      for (Element testException : exceptionList) {
        exceptionMatched |= testException.match(token);
        if (exceptionMatched) {
          break;
        }
      }
    }
    return exceptionMatched;
  }

  final boolean prevExceptionMatch(final AnalyzedToken token) {
    boolean exceptionMatched = false;
    if (exceptionSet) {      
      for (Element testException : exceptionList) {
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
  
  public final boolean isSentStart() {
    boolean equals = false;
    if (posToken != null) {
      // not sure if this should be logical AND
      equals = posToken.equals(JLanguageTool.SENTENCE_START_TAGNAME) && posNegation;
    }
    return equals;
  }
  
  public final String toString() {
    if (posToken != null) {
      return stringToken.concat("/").concat(posToken);
    } else {
      return stringToken;
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

  public final void setStringElement(final String token) {
    this.stringToken = token;
    if (!stringToken.equals("") && stringRegExp) {
      regToken = stringToken;
      if (!caseSensitive) {
        regToken = "(?u)".concat(stringToken);
      }
      p = Pattern.compile(regToken);
    }
  }
  
  public final void setPosException(final String posToken, final boolean regExp,
      final boolean negation, final boolean scope) {
    Element posException = new Element("", this.caseSensitive, regExp, false);
    posException.setPosElement(posToken, regExp, negation);
    posException.exceptionValidNext = scope;
    if (exceptionList == null) {
      exceptionList = new ArrayList<Element>();
    }
    if (!exceptionSet) {
      exceptionSet = true;
    }
    exceptionList.add(posException);
  }

  public final void setStringException(final String token, final boolean regExp,
      final boolean inflected, final boolean negation, final boolean scope) {
    Element stringException = new Element(token, this.caseSensitive, regExp, inflected);
    stringException.setNegation(negation);
    stringException.exceptionValidNext = scope;
    if (exceptionList == null) {
      exceptionList = new ArrayList<Element>();
    }
    if (!exceptionSet) {
      exceptionSet = true;
    }
    exceptionList.add(stringException);
  }

  final boolean matchPosToken(final AnalyzedToken token) {
    // if no POS set
    // defaulting to true
    if (posToken == null) {
      return true;
    }
    boolean match = false;
    if (!posRegExp) {
      match = posToken.equals(token.getPOSTag());              
    } else
    // changed to match regexps
    if (token.getPOSTag() != null) {
      if (mPos == null) {
        mPos = pPos.matcher(token.getPOSTag());
      } else {
        mPos.reset(token.getPOSTag());
      }
      match = mPos.matches();                     
    }
    return match;
  }

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

  public final int getSkipNext() {
    return skip;
  }

  public final void setSkipNext(final int i) {
    skip = i;
  }

  final String getTokens() {
    return stringToken;
  }

  /**
   * Negates the meaning of match().
   */
  final void setNegation(final boolean negation) {
    this.negation = negation;
  }

}
