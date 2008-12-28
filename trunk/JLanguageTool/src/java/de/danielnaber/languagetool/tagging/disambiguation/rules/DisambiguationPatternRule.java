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

  /** Possible disambiguator actions. **/
  public enum DisambiguatorAction { 
    ADD, FILTER, REMOVE, REPLACE, UNIFY; 

    /** Converts string to the constant enum.
     * @param str String value to be converted.
     * @return DisambiguatorAction enum.
     */
    public static DisambiguatorAction toAction(final String str) {    
      try {
        return valueOf(str);
      } catch (final Exception ex) {
        return REPLACE;
      }  
    }
  };


  private final String id;

  private final Language language;
  private final String description;

  private int startPositionCorrection = 0;
  private int endPositionCorrection = 0;

  private final List<Element> patternElements;

  private final String disambiguatedPOS;

  private final Match matchToken;  

  private DisambiguatorAction disAction;

  private AnalyzedToken[] newTokenReadings;

  /**
   * @param id Id of the Rule
   * @param language Language of the Rule
   * @param elements Element (token) list
   * @param description Description to be shown (name)
   * @param disambiguatorAction - the action to be executed on found token(s),
   * one of the following: add, filter, remove, replace, unify.
   * 
   */

  DisambiguationPatternRule(final String id, final String description,
      final Language language, final List<Element> elements, final String disamb,
      final Match posSelect, final DisambiguatorAction disambAction) {
    if (id == null) {
      throw new NullPointerException("id cannot be null");
    }
    if (language == null) {
      throw new NullPointerException("language cannot be null");
    }
    if (elements == null) {
      throw new NullPointerException("elements cannot be null");
    }
    if (description == null) {
      throw new NullPointerException("description cannot be null");
    }
    if (disamb == null && posSelect == null 
        && disambAction != DisambiguatorAction.UNIFY 
        && disambAction != DisambiguatorAction.ADD
        && disambAction != DisambiguatorAction.REMOVE) {
      throw new NullPointerException("disambiguated POS cannot be null");
    }
    this.id = id;
    this.language = language;
    this.description = description;    
    this.patternElements = new ArrayList<Element>(elements); // copy elements
    this.disambiguatedPOS = disamb;
    this.matchToken = posSelect;
    this.disAction = disambAction;
  }

  public final String getId() {
    return id;
  }

  public final String getDescription() {
    return description;
  }

  @Override
  public final String toString() {
    return id + ":" + patternElements + ":" + description;
  }

  public final void setStartPositionCorrection(final int startPositionCorrection) {
    this.startPositionCorrection = startPositionCorrection;
  }  

  public final void setEndPositionCorrection(final int endPositionCorrection) {
    this.endPositionCorrection = endPositionCorrection;
  }

  /**
   * Used to add new interpretations
   * @param newReadings An array of AnalyzedTokens.
   * The length of the array should be the same as the number
   * of the tokens matched and selected by mark/mark_from & mark_to
   * attributes (>1). 
   */
  public final void setNewInterpretations(final AnalyzedToken[] newReadings) {
    newTokenReadings = newReadings;
  }

  public final AnalyzedSentence replace(final AnalyzedSentence text) throws IOException {

    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    final AnalyzedTokenReadings[] whTokens = text.getTokens();
    final int[] tokenPositions = new int[tokens.length + 1 ];

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
    final int patternSize = patternElements.size();
    Element elem = null, prevElement = null;
    final boolean startWithSentStart = patternElements.get(0).isSentStart();

    boolean inUnification = false;
    boolean uniMatched = false;
    AnalyzedTokenReadings[] unifiedTokens = null;

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
            if (prevSkipNext > 0 && prevElement != null
                && prevElement.isMatchedByScopeNextException(matchToken)) {
              exceptionMatched = true;
              prevMatched = true;              
            }
            if (elem.isReferenceElement()
                && (firstMatchToken + elem.getMatch().getTokenRef() 
                    < tokens.length)) {
              elem.compile(tokens[firstMatchToken + 
                                  elem.getMatch().getTokenRef()],
                                  language.getSynthesizer());

            }
            if (elem.hasAndGroup()) {
              for (final Element andElement : elem.getAndGroup()) {
                if (andElement.isReferenceElement()
                    && (firstMatchToken + andElement.getMatch().getTokenRef() 
                        < tokens.length)) {
                  andElement.compile(tokens[firstMatchToken 
                                            + andElement.getMatch().getTokenRef()],
                                            language.getSynthesizer());                  
                }                               
              }
              if (l == 0) { 
                elem.setupAndGroup();
              }
            }
            thisMatched |= elem.isMatchedCompletely(matchToken);
            if (thisMatched && elem.isUnified()) {
              if (inUnification) {                
                uniMatched = uniMatched || language.getUnifier().
                isSatisfied(matchToken, elem.getUniFeature(), elem.getUniType());
                if (l + 1 == numberOfReadings) {
                  thisMatched &= uniMatched;
                  language.getUnifier().startNextToken();
                  if (uniMatched) {
                    unifiedTokens = 
                      language.getUnifier().getUnifiedTokens();
                  }
                }                
              } else {
                if (elem.getUniNegation()) {
                  language.getUnifier().setNegation(true);
                } 
                thisMatched |= language.getUnifier().
                isSatisfied(matchToken, elem.getUniFeature(), elem.getUniType());                 
                if (l + 1 == numberOfReadings) {
                  inUnification = true;
                  language.getUnifier().startUnify();
                  uniMatched = false;
                }
              }
            } 

            if (!elem.isUnified()) {              
              inUnification = false;
              uniMatched = false;
              language.getUnifier().reset();
            }
            if (l + 1 == numberOfReadings && elem.hasAndGroup()) {
              thisMatched &= elem.checkAndGroup(thisMatched);
            }

            exceptionMatched |= elem.isExceptionMatchedCompletely(matchToken);                
            if (elem.hasPreviousException() && m > 0) {
              final int numReadings = tokens[m - 1].getReadingsLength();
              for (int p = 0; p < numReadings; p++) {
                final AnalyzedToken matchExceptionToken = tokens[m - 1].getAnalyzedToken(p);
                exceptionMatched |= elem.isMatchedByScopePreviousException(matchExceptionToken);
              }
            }            
            // Logical OR (cannot be AND):
            if (thisMatched || exceptionMatched) {
              matched = true;
              matchPos = m;
              skipShift = matchPos - nextPos;              
              tokenPositions[matchingTokens] = skipShift + 1;
            } else {
              matched |= false;                            
            }
            skipMatch = (skipMatch || matched) && !exceptionMatched;
          }

          //disallow exceptions that should match only current tokens          
          if (!(thisMatched || prevMatched)) {
            exceptionMatched = false;
            skipMatch = false;
          }

          if (skipMatch) {
            break;
          }

        }
        allElementsMatch = skipMatch;
        if (skipMatch) {
          prevSkipNext = skipNext;
          matchingTokens++;                    
          if (firstMatchToken == -1) {
            firstMatchToken = matchPos; 
          }
          skipShiftTotal += skipShift;
        } else {
          prevSkipNext = 0;
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
            correctedEndPos -= tokenPositions[matchingTokens + l - 1];
            l--;
          }
        }         

        final int fromPos = text.getOriginalPosition(firstMatchToken + correctedStPos);
        final int numRead = whTokens[fromPos].getReadingsLength();
        boolean filtered = false;
        switch (disAction) {
          case UNIFY : {
            if (unifiedTokens != null) {
              if (unifiedTokens.length == matchingTokens - startPositionCorrection + endPositionCorrection) {
                for (i = 0; i < unifiedTokens.length; i++) {
                  whTokens[text.getOriginalPosition(firstMatchToken + correctedStPos + i)] = unifiedTokens[i]; 
                }
                unifiedTokens = null;
              }
            }
            break;
          }
          case REMOVE : {
            if (newTokenReadings != null) {
              if (newTokenReadings.length == 
                matchingTokens - startPositionCorrection + endPositionCorrection) {
                for (i = 0; i < newTokenReadings.length; i++) {
                  whTokens[text.getOriginalPosition(firstMatchToken + correctedStPos + i)]
                           .removeReading(newTokenReadings[i]); 
                }                
              }
            }
            break;
          }          
          case ADD : {
            if (newTokenReadings != null) {
              if (newTokenReadings.length 
                  == matchingTokens - startPositionCorrection + endPositionCorrection) {
                for (i = 0; i < newTokenReadings.length; i++) {
                  whTokens[text.getOriginalPosition(firstMatchToken + correctedStPos + i)]
                           .addReading(newTokenReadings[i]); 
                }                
              }
            }
            break;
          }
          case FILTER : {
            if (matchToken == null) { // same as REPLACE if using <match>
              Match tmpMatchToken = new Match(disambiguatedPOS, null, true, disambiguatedPOS, 
                  null, Match.CaseConversion.NONE, false);
              tmpMatchToken.setToken(whTokens[fromPos]);
              whTokens[fromPos] = tmpMatchToken.filterReadings(whTokens[fromPos]);
              filtered = true;
            }
          }
          case REPLACE:
          default: {
            if (!filtered) {
              if (matchToken == null) {
                String lemma = "";
                for (int l = 0; l < numRead; l++) {
                  if (whTokens[fromPos].getAnalyzedToken(l).getPOSTag() != null 
                      && (whTokens[fromPos].getAnalyzedToken(l).getPOSTag().
                          equals(disambiguatedPOS)
                          && (whTokens[fromPos].getAnalyzedToken(l).getLemma() != null))) {
                    lemma = whTokens[fromPos].getAnalyzedToken(l).getLemma();                          
                  } 
                }
                if (("").equals(lemma)) {
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
            }
          }

        }
      }
      firstMatchToken = -1;      
      skipShiftTotal = 0;
      language.getUnifier().reset();
      inUnification = false;
      uniMatched = false;
    }

    return new AnalyzedSentence(whTokens);
  }

}


