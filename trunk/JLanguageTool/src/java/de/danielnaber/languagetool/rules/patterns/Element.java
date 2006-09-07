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
import java.util.ArrayList;
import java.util.Iterator;

import de.danielnaber.languagetool.AnalyzedToken;

/**
 * A part of a pattern.
 * 
 * @author Daniel Naber
 */
public class Element {

  private String stringToken;
  private String posToken;
  private boolean posRegExp = false;

  boolean negation = false;
  boolean posNegation = false;
  
  private boolean caseSensitive = false;
  private boolean stringRegExp = false;
  private boolean inflected = false;

  private ArrayList<Element> exceptionList;
  public boolean exceptionValidNext = true;
  private boolean exceptionSet = false;

  int skip = 0;

  boolean match(AnalyzedToken token) {
    return (matchStringToken(token) != negation) && (matchPosToken(token) != posNegation);
  }

  boolean exceptionValid() {
    boolean eNext = false;
    if (exceptionSet) {
      Iterator<Element> it = exceptionList.iterator();    
      while (it.hasNext()) {
        eNext = eNext || it.next().exceptionValidNext;
        if (eNext) {
          break;
        }
      }
    }
      return eNext;
  }
  boolean exceptionMatch(AnalyzedToken token) {
    boolean exceptionMatched = false;
    if (exceptionSet) {
    Iterator<Element> it = exceptionList.iterator();    
    while (it.hasNext()) {
      Element testException =it.next();
      //if (testException.exceptionValidNext) {
      exceptionMatched = exceptionMatched || testException.match(token);
      //}      
      if (exceptionMatched) {
        break;
      }
    }
    }
    return exceptionMatched;    
  }

  boolean prevExceptionMatch(AnalyzedToken token) {
    boolean exceptionMatched = false;
    if (exceptionSet) {
    Iterator<Element> it = exceptionList.iterator();    
    while (it.hasNext()) {
      Element testException =it.next();
      if (testException.exceptionValidNext) {
      exceptionMatched = exceptionMatched || testException.match(token);
      }      
      if (exceptionMatched) {
        break;
      }
    }
    }
    return exceptionMatched;    
  }
  
  
  Element(String token, boolean caseSensitive, boolean regExp, boolean inflected) {
    this.stringToken = token;
    this.caseSensitive = caseSensitive;
    this.stringRegExp = regExp;
    this.inflected = inflected;
  }

  public String toString() {
    if (posToken != null) {
      return stringToken.concat("/").concat(posToken);
    } else {
      return stringToken;
    }
  }

  public void setPosElement(String posToken, boolean regExp, boolean negation) {
    this.posToken = posToken;
    this.posNegation = negation;
    posRegExp = regExp;
  }

  public void setStringElement(String token)  {
    this.stringToken = token;
  }
  
  public void setPosException(String posToken, boolean regExp, boolean negation, boolean scope) {
      Element posException = new Element("", this.caseSensitive, regExp, false);   
      posException.setPosElement(posToken, regExp, negation);
      posException.exceptionValidNext=scope;
      if (exceptionList == null) {
        exceptionList = new ArrayList<Element>();
      }
      if (!exceptionSet) {
        exceptionSet = true;  
      }
      exceptionList.add(posException);          
  }

  public void setStringException(String token, boolean regExp, boolean inflected, boolean negation, boolean scope) {
    Element stringException = new Element(token, this.caseSensitive, regExp, inflected);
    stringException.setNegation(negation);
    stringException.exceptionValidNext=scope;
    if (exceptionList == null) {
      exceptionList = new ArrayList<Element>();
    }
    if (!exceptionSet) {
      exceptionSet = true;          
    }
      exceptionList.add(stringException);    
  }

  boolean matchPosToken(AnalyzedToken token) {
    // if no POS set
    // defaulting to true
    if (posToken == null) {
      return true;
    }
    boolean match = false;
    if (!posRegExp) {
      if (posToken.equals(token.getPOSTag())) {
        match = true;
      }
    } else
    // changed to match regexps
    if (token.getPOSTag() != null) {
      if (Pattern.matches(posToken, token.getPOSTag())) {
        match = true;
      }
    }
    return match;
  }

  boolean matchStringToken(AnalyzedToken token) {

    // if no string set
    // defaulting to true
    if (stringToken == null) {
      return true;
    }
    if (stringToken.equals("")) {
      return true;
    }

    String testToken = null;
    //enables using words with lemmas and without lemmas
    //in the same regexp with inflected="yes"
    if (inflected) {
      testToken = token.getLemma();
      if (testToken==null) 
        testToken=token.getToken();
    }
    else
      testToken = token.getToken();

    if (caseSensitive) {
      if (stringRegExp) {
        if (token.getToken() != null)
          if (Pattern.matches(stringToken, testToken))
            return true;
      } else {
        if (stringToken.equals(testToken))
          return true;
      }
    } else {
      if (stringRegExp) {
        if (testToken != null)
          // (?u) - regex matching
          // case insensitive in Unicode
          if (Pattern.matches("(?u)".concat(stringToken), testToken))
            return true;
      } else {
        if (stringToken.equalsIgnoreCase(testToken))
          return true;
      }
    }
    return false;
  }

  public int getSkipNext() {
    return skip;
  }

  public void setSkipNext(int i) {
    skip = i;
  }

  String getTokens() {
    return stringToken;
  }

  /**
   * Negates the meaning of match().
   */
  void setNegation(boolean negation) {
    this.negation = negation;
  }

}
