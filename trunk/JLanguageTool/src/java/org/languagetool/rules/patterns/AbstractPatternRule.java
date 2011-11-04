/* LanguageTool, a natural language style checker 
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
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
import java.util.List;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * An Abstract Pattern Rule that describes a pattern of words or part-of-speech tags 
 * used for PatternRule and DisambiguationPatternRule.
 * 
 * Introduced to minimize code duplication between those classes.
 * 
 * @author Marcin Miłkowski
 */

public abstract class AbstractPatternRule extends Rule {

  private final String id;

  private final String description;

  protected final List<Element> patternElements;

  protected Unifier unifier;

  protected final Language language;

  protected int startPositionCorrection;

  protected int endPositionCorrection;

  protected boolean prevMatched;

  protected final boolean testUnification;

  private final boolean getUnified;

  private boolean groupsOrUnification;

  protected AnalyzedTokenReadings[] unifiedTokens;  

  protected final boolean sentStart;

  public AbstractPatternRule(final String id, 
      final String description,
      final Language language,
      final List<Element> elements,
      boolean getUnified) {
    this.id = id; 
    this.description = description;
    this.patternElements = new ArrayList<Element>(elements); // copy elements
    this.language = language;
    this.getUnified = getUnified;
    unifier = language.getUnifier();
    testUnification = initUnifier();
    sentStart = patternElements.size() > 0 && patternElements.get(0).isSentStart();    
    if (!testUnification) {
      for (Element elem : patternElements) {
        if (elem.hasAndGroup()) {
          groupsOrUnification = true;
          break;
        }
      }
    } else {
      groupsOrUnification = true;
    }
  }

  private boolean initUnifier() {
    for (final Element elem : patternElements) {
      if (elem.isUnified()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public final String toString() {
    return id + ":" + patternElements + ":" + description;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence text) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub
  }

  public final void setStartPositionCorrection(final int startPositionCorrection) {
    this.startPositionCorrection = startPositionCorrection;
  }

  public final void setEndPositionCorrection(final int endPositionCorrection) {
    this.endPositionCorrection = endPositionCorrection;
  }


  protected void setupAndGroup(final int firstMatchToken,
      final Element elem, final AnalyzedTokenReadings[] tokens)
  throws IOException {    
    if (elem.hasAndGroup()) {
      for (final Element andElement : elem.getAndGroup()) {
        if (andElement.isReferenceElement()) {
          setupRef(firstMatchToken, andElement, tokens);
        }
      }      
      elem.setupAndGroup();
    }    
  }

  //TODO: add .compile for all exceptions of the element?
  protected void setupRef(final int firstMatchToken, final Element elem,
      final AnalyzedTokenReadings[] tokens) throws IOException {
    if (elem.isReferenceElement()) {
      final int refPos = firstMatchToken + elem.getMatch().getTokenRef();
      if (refPos < tokens.length) {
        elem.compile(tokens[refPos], language.getSynthesizer());
      }
    }
  }  

  protected boolean testAllReadings(final AnalyzedTokenReadings[] tokens,
      final Element elem, final Element prevElement, final int tokenNo,
      final int firstMatchToken, final int prevSkipNext) throws IOException {    
    boolean thisMatched = false;
    final int numberOfReadings = tokens[tokenNo].getReadingsLength();
    setupAndGroup(firstMatchToken, elem, tokens);
    for (int l = 0; l < numberOfReadings; l++) {
      final AnalyzedToken matchToken = tokens[tokenNo].getAnalyzedToken(l);
      prevMatched = prevMatched || prevSkipNext > 0 && prevElement != null
      && prevElement.isMatchedByScopeNextException(matchToken);
      if (prevMatched) {
        return false;
      }
      thisMatched = thisMatched || elem.isMatched(matchToken);
      if (!thisMatched && !elem.isInflected() && elem.getPOStag() == null 
          && (prevElement != null && prevElement.getExceptionList() == null)) {
        return false; // the token is the same, we will not get a match
      }
      if (groupsOrUnification) {
        thisMatched &= testUnificationAndGroups(thisMatched,
            l + 1 == numberOfReadings, matchToken, elem);
      }
    }
    if (thisMatched) {
      for (int l = 0; l < numberOfReadings; l++) {
        if (elem.isExceptionMatchedCompletely(tokens[tokenNo].getAnalyzedToken(l)))
          return false;
      }    
      if (tokenNo > 0 && elem.hasPreviousException()) {
        if (elem.isMatchedByPreviousException(tokens[tokenNo - 1]))
          return false;
      }
    }
    return thisMatched;
  }

  protected boolean testUnificationAndGroups(final boolean matched,
      final boolean lastReading, final AnalyzedToken matchToken,
      final Element elem) {
    boolean thisMatched = matched;
    if (testUnification) {
      if (matched && elem.isUnified()) {
        thisMatched = thisMatched && unifier.isUnified(matchToken, elem.getUniFeatures(), 
            elem.isUniNegated(), lastReading);
      }
      if (thisMatched && getUnified) {
        unifiedTokens = unifier.getFinalUnified();
      }
      if (!elem.isUnified()) {
        unifier.reset();
      }
    }    
    elem.addMemberAndGroup(matchToken);
    if (lastReading) {
      thisMatched &= elem.checkAndGroup(thisMatched);
    }        
    return thisMatched;
  }


}
