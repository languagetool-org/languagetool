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
import java.util.List;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * A Rule that describes a language error as a simple pattern of words or of part-of-speech tags.
 * 
 * @author Daniel Naber
 */
public class PatternRule extends Rule {

  private String id;

  private Language[] language;

  private String description;

  private String message;

  private int startPositionCorrection = 0;

  private int endPositionCorrection = 0;

  private boolean caseSensitive = false;

  private boolean regExp = false;

  private List<Element> patternElements;

  PatternRule(String id, Language language, List<Element> elements, String description,
      String message) {
    if (id == null)
      throw new NullPointerException("id cannot be null");
    if (language == null)
      throw new NullPointerException("language cannot be null");
    if (elements == null)
      throw new NullPointerException("elements cannot be null");
    if (description == null)
      throw new NullPointerException("description cannot be null");
    this.id = id;
    this.language = new Language[] { language };
    // this.pattern = pattern;
    this.description = description;
    this.message = message;
    this.patternElements = new ArrayList<Element>(elements); // copy elements
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public String getMessage() {
    return message;
  }

  public Language[] getLanguages() {
    return language;
  }

  public String toString() {
    return id + ":" + patternElements + ":" + description;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean getCaseSensitive() {
    return caseSensitive;
  }

  public void setCaseSensitive(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  public boolean getregExpSetting() {
    return regExp;
  }

  public void setregExpSetting(boolean regExp) {
    this.regExp = regExp;
  }

  public int getStartPositionCorrection() {
    return startPositionCorrection;
  }

  public void setStartPositionCorrection(int startPositionCorrection) {
    this.startPositionCorrection = startPositionCorrection;
  }

  public int getEndPositionCorrection() {
    return endPositionCorrection;
  }

  public void setEndPositionCorrection(int endPositionCorrection) {
    this.endPositionCorrection = endPositionCorrection;
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    
    int tokenPos = 0;
    int prevSkipNext = 0;
    int skipNext = 0;
    int matchPos = 0;
    int skipShift = 0;
    
    int firstMatchToken = -1;
    int lastMatchToken = -1;
    Element elem = null, prevElement = null;
    
    for (int i = 0; i < tokens.length; i++) {
      boolean allElementsMatch = true;
      
      int matchingTokens = 0;
      for (int k = 0; k < patternElements.size(); k++) {
        if (elem!=null) { 
          prevElement = elem;
        }
        elem = patternElements.get(k);
        skipNext = elem.getSkipNext();
        int nextPos = tokenPos + k + skipShift;
        if (nextPos >= tokens.length) {
          allElementsMatch = false;
          break;
        }
        boolean skipMatch = false, exceptionMatched = false;
        if (prevSkipNext + nextPos >= tokens.length || prevSkipNext < 0) { // SENT_END?
          prevSkipNext = tokens.length - (nextPos + 1);
        }
        for (int m = nextPos; m <= nextPos + prevSkipNext; m++) {
          boolean matched = false;
          for (int l = 0; l < tokens[m].getReadingsLength(); l++) {
            
            AnalyzedToken matchToken = tokens[m].getAnalyzedToken(l);
            
            if (prevSkipNext >0 && prevElement!=null) {
              if (prevElement.exceptionMatch(matchToken)) {
                exceptionMatched = true;
              }               
            }
            
            // Logical OR (cannot be AND):
            if (!elem.match(matchToken)) {             
              matched = matched || false;
            } else {              
            matched = true && !exceptionMatched;
            matchPos = m;
            skipShift = matchPos - nextPos;
          }
          skipMatch = skipMatch || matched;
          
        }
        if (skipMatch) {
          break;
        }
        }
        allElementsMatch = skipMatch;
        if (skipMatch) {
          prevSkipNext = skipNext;        
        } else {
          prevSkipNext = 0;
        }
        if (!allElementsMatch) {
          break;
        } else {
          matchingTokens++;
          lastMatchToken = matchPos; // nextPos;
          if (firstMatchToken == -1)
            firstMatchToken = matchPos; // nextPos;
        }
      }
    
      if (allElementsMatch) {
        String errMessage = message;
        // TODO: implement skipping tokens while marking error tokens
        // replace back references like \1 in message:
        for (int j = 0; j < matchingTokens; j++) {
          errMessage = errMessage.replaceAll("\\\\" + (j + 1), tokens[firstMatchToken + j]
                                                                      .getToken());
        }
        boolean startsWithUppercase = StringTools.startsWithUppercase(tokens[firstMatchToken
                                                                             + startPositionCorrection].toString());
        RuleMatch ruleMatch = new RuleMatch(this, tokens[firstMatchToken + startPositionCorrection]
                                                         .getStartPos(), tokens[lastMatchToken + endPositionCorrection].getStartPos()
                                                         + tokens[lastMatchToken + endPositionCorrection].getToken().length(), errMessage,
                                                         startsWithUppercase);
        ruleMatches.add(ruleMatch);
      } else {
        firstMatchToken = -1;
        lastMatchToken = -1;
      }      
      tokenPos++;
    }
    
    return (RuleMatch[]) ruleMatches.toArray(new RuleMatch[0]);
  }

  public void reset() {
    // nothing
  }

}
