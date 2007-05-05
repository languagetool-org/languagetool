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
package de.danielnaber.languagetool.tagging.disambiguation.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.patterns.Element;
import de.danielnaber.languagetool.rules.patterns.Match;

/**
 * A Rule that describes a pattern of words or part-of-speech tags used
 * for disambiguation.
 * 
 * @author Marcin Miłkowski
 */
public class DisambiguationPatternRule {

  private String id;

  private Language language;
  private String description;

  private int startPositionCorrection = 0;
  private int endPositionCorrection = 0;
 
  private List<Element> patternElements;
  
  private String disambiguatedPOS;
  
  private Match matchToken;
    
  /**
   * @param id Id of the Rule
   * @param language Language of the Rule
   * @param elements Element (token) list
   * @param description Description to be shown (name)
   * 
   */
  
  DisambiguationPatternRule(final String id, final String description,
      final Language language, final List<Element> elements, final String disamb,
      final Match posSelect) {
    if (id == null)
      throw new NullPointerException("id cannot be null");
    if (language == null)
      throw new NullPointerException("language cannot be null");
    if (elements == null)
      throw new NullPointerException("elements cannot be null");
    if (description == null)
      throw new NullPointerException("description cannot be null");
    if (disamb == null && posSelect == null)
      throw new NullPointerException("disambiguated POS cannot be null");
    this.id = id;
    this.language = language;
    this.description = description;    
    this.patternElements = new ArrayList<Element>(elements); // copy elements
    this.disambiguatedPOS = disamb;
    this.matchToken = posSelect;
  }
     
  public final String getId() {
    return id;
  }

  public final String getDescription() {
    return description;
  }

  public final String toString() {
    return id + ":" + patternElements + ":" + description;
  }

  public final void setStartPositionCorrection(final int startPositionCorrection) {
    this.startPositionCorrection = startPositionCorrection;
  }  

  public final void setEndPositionCorrection(final int endPositionCorrection) {
    this.endPositionCorrection = endPositionCorrection;
  }
  
  
  public final AnalyzedSentence replace(final AnalyzedSentence text) throws IOException {
                    
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    AnalyzedTokenReadings[] whTokens = text.getTokens();
    int[] tokenPositions = new int[tokens.length + 1 ];
    
    int tokenPos = 0;
    int prevSkipNext = 0;
    int skipNext = 0;
    int matchPos = 0;
    int skipShift = 0;
    // this variable keeps the total number
    // of tokens skipped - used to avoid
    // that nextPos gets back to unmatched tokens...
    int skipShiftTotal = 0;

    int firstMatchToken = -1;
    //int lastMatchToken = -1;
    final int patternSize = patternElements.size();
    Element elem = null, prevElement = null;
    final boolean startWithSentStart = patternElements.get(0).isSentStart();

    for (int i = 0; i < tokens.length; i++) {
      boolean allElementsMatch = true;

      //stop processing if rule is longer than the sentence
      if (patternSize + i > tokens.length) {
        allElementsMatch = false;
        break;
      }            
      
      //stop looking for sent_start - it will never match any
      //token except the first
      if (startWithSentStart && i > 0) {
        allElementsMatch = false;
        break;
      }

      int matchingTokens = 0;
      for (int k = 0; (k < patternSize); k++) {
        if (elem != null) {
          prevElement = elem;
        }
        elem = patternElements.get(k);
        skipNext = elem.getSkipNext();
        final int nextPos = tokenPos + k + skipShiftTotal;
               
        if (nextPos >= tokens.length) {
          allElementsMatch = false;
          break;
        }

        boolean skipMatch = false, thisMatched = false, prevMatched = false;
        boolean exceptionMatched = false;
        if (prevSkipNext + nextPos >= tokens.length || prevSkipNext < 0) { // SENT_END?
          prevSkipNext = tokens.length - (nextPos + 1);
        }
        for (int m = nextPos; m <= nextPos + prevSkipNext; m++) {          
          boolean matched = false;
          final int numberOfReadings = tokens[m].getReadingsLength();

          for (int l = 0; l < numberOfReadings; l++) {
            final AnalyzedToken matchToken = tokens[m].getAnalyzedToken(l);
            if (prevSkipNext > 0 && prevElement != null) {
              if (prevElement.prevExceptionMatch(matchToken)) {
                exceptionMatched = true;
                prevMatched = true;
              }
            }
            if (elem.referenceElement()) {
              if (firstMatchToken + elem.getMatch().getTokenRef() 
                  < tokens.length) {
                elem.getMatch().setToken(tokens[firstMatchToken 
                                       + elem.getMatch().getTokenRef()]);
                elem.getMatch().setSynthesizer(language.getSynthesizer());
                elem.compile();
              }
            }
            thisMatched |= elem.match(matchToken);
            exceptionMatched |= (elem.exceptionMatch(matchToken)
                || elem.andGroupExceptionMatch(matchToken));
            // Logical OR (cannot be AND):
            if (!(thisMatched || exceptionMatched)) {
              matched |= false;
            } else {
              matched = true;
              matchPos = m;
              skipShift = matchPos - nextPos;              
              tokenPositions[matchingTokens] = skipShift + 1;              
            }
            skipMatch = (skipMatch || matched) && !exceptionMatched;
          }
          
          //disallow exceptions that should match only current tokens          
          if (!(thisMatched || prevMatched)) {
            exceptionMatched = false;
          }
                    
          if (skipMatch) {
            break;
          }
          
        }
        //disallow exceptions that should match only current tokens        
        if (!(thisMatched || prevMatched)) {
          skipMatch = false;
        }
        allElementsMatch = skipMatch;
        if (skipMatch) {
          prevSkipNext = skipNext;
        } else {
          prevSkipNext = 0;
        }
        if (allElementsMatch) {                              
          matchingTokens++;
        //  lastMatchToken = matchPos;           
          if (firstMatchToken == -1) {
            firstMatchToken = matchPos; 
          }
          skipShiftTotal += skipShift;
        } else {
          skipShiftTotal = 0;
          break;
        }
      }
      
      tokenPos++;
      
      if (allElementsMatch) {
        int correctedStPos = 0;
        if (startPositionCorrection > 0) {        
        for (int l = 0; l <= startPositionCorrection; l++) {
          correctedStPos +=  tokenPositions[l];
        }
        correctedStPos--;
        }        
        
        int correctedEndPos = 0;
        if (endPositionCorrection < 0) {
          int l = 0;
          while (l > endPositionCorrection) {
            int test = matchingTokens + l - 1;
            test = tokenPositions[test];
            correctedEndPos -= tokenPositions[matchingTokens + l - 1];
            l--;
          }
          }         
        
        final int fromPos = text.getOriginalPosition(firstMatchToken + correctedStPos);
        //int toPos = lastMatchToken + correctedEndPos;
          final int numRead = whTokens[fromPos].getReadingsLength();
          if (matchToken == null) {
          String lemma = "";
          for (int l = 0; l < numRead; l++) {
            if (whTokens[fromPos].getAnalyzedToken(l).getPOSTag() != null) {
            if (whTokens[fromPos].getAnalyzedToken(l).getPOSTag().equals(disambiguatedPOS)) {
              if (whTokens[fromPos].getAnalyzedToken(l).getLemma() != null) {
                lemma = whTokens[fromPos].getAnalyzedToken(l).getLemma();
              }
            }
            } 
          }
          if (lemma.equals("")) {
            lemma = whTokens[fromPos].getAnalyzedToken(0).getLemma();
          }
          
          final AnalyzedTokenReadings toReplace = new AnalyzedTokenReadings(
                new AnalyzedToken(whTokens[fromPos].getToken(), disambiguatedPOS, lemma,
                    whTokens[fromPos].getStartPos()));
          whTokens[fromPos] = toReplace;
          } else {
            // using the match element
            matchToken.setToken(whTokens[fromPos]);
            whTokens[fromPos] = matchToken.filterReadings(whTokens[fromPos]);
          }
      } else {
        firstMatchToken = -1;
        //lastMatchToken = -1;
        skipShiftTotal = 0;
      }
    }

    return new AnalyzedSentence(whTokens);
  }

}


