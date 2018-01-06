/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
 * Copyright (C) 2013 Stefan Lotties
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
import java.util.Objects;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

/**
 * @since 2.3
 */
public abstract class AbstractPatternRulePerformer {

  protected boolean prevMatched;
  protected AbstractPatternRule rule;
  protected Unifier unifier;
  protected AnalyzedTokenReadings[] unifiedTokens;

  protected AbstractPatternRulePerformer(AbstractPatternRule rule, Unifier unifier) {
    this.rule = Objects.requireNonNull(rule);
    this.unifier = Objects.requireNonNull(unifier);
  }

  protected List<PatternTokenMatcher> createElementMatchers() {
    List<PatternTokenMatcher> patternTokenMatchers = new ArrayList<>(rule.patternTokens.size());
    for (PatternToken pToken : rule.patternTokens) {
      PatternTokenMatcher matcher = new PatternTokenMatcher(pToken);
      patternTokenMatchers.add(matcher);
    }
    return patternTokenMatchers;
  }

  protected boolean testAllReadings(AnalyzedTokenReadings[] tokens,
      PatternTokenMatcher matcher, PatternTokenMatcher prevElement,
      int tokenNo, int firstMatchToken, int prevSkipNext)
          throws IOException {
    boolean thisMatched = false;
    int numberOfReadings = tokens[tokenNo].getReadingsLength();
    matcher.prepareAndGroup(firstMatchToken, tokens, rule.getLanguage());

    for (int i = 0; i < numberOfReadings; i++) {
      AnalyzedToken matchToken = tokens[tokenNo].getAnalyzedToken(i);
      boolean tested = false;
      prevMatched = prevMatched || prevSkipNext > 0
          && prevElement != null
          && prevElement.isMatchedByScopeNextException(matchToken);
      
      // a workaround to allow exception with scope="next" without "skip" in previous token
      // this allows to check for exception in the next token even if the current one is the last one in the sentence
      prevMatched = prevMatched || prevSkipNext == 0
          && tokenNo <= tokens.length-2
          && matcher.isMatchedByScopeNextException(tokens[tokenNo+1].getAnalyzedToken(0));
      
      if (prevMatched) {
        return false;
      }

      if (!thisMatched) {
        thisMatched = matcher.isMatched(matchToken);
        tested = true;
      }

      //short-circuit when the search cannot possibly match
      if (!thisMatched && (prevElement == null ||
          prevElement.getPatternToken().getExceptionList() == null)) {
        if (matcher.getPatternToken().getPOStag() == null) {
          if (matcher.getPatternToken().isInflected()) {
            if (tokens[tokenNo].hasSameLemmas()) {
              return false; // same lemmas everywhere
            }
          } else {
            return false; // the token is the same, we will not get a match
          }
        } else if (!matcher.getPatternToken().getPOSNegation() // postag =! null
            && !tokens[tokenNo].isTagged()) {
          return false; // we won't find any postag here anyway
        }
      }
      if (rule.isGroupsOrUnification()) {
        if (!matcher.getPatternToken().isUnificationNeutral()) {
        thisMatched &= testUnificationAndGroups(thisMatched,
            i + 1 == numberOfReadings, matchToken, matcher, tested);
        }
      }
    }
    if (thisMatched) {
      for (int i = 0; i < numberOfReadings; i++) {
        if (matcher.isExceptionMatchedCompletely(tokens[tokenNo].getAnalyzedToken(i))) {
          return false;
        }
      }
      if (tokenNo > 0 && matcher.hasPreviousException()) {
        if (matcher.isMatchedByPreviousException(tokens[tokenNo - 1])) {
          return false;
        }
      }
      if (matcher.getPatternToken().isUnificationNeutral()) {
        unifier.addNeutralElement(tokens[tokenNo]);
      }
    }
    if (matcher.getPatternToken().getChunkTag() != null) {
      thisMatched &=
          tokens[tokenNo].getChunkTags().contains(matcher.getPatternToken().getChunkTag())
          ^ matcher.getPatternToken().getNegation();
    }
    if (matcher.getPatternToken().hasAndGroup()) {
      for (PatternToken e : matcher.getPatternToken().getAndGroup()) {
        if (e.getChunkTag() != null) {
          thisMatched &= tokens[tokenNo].getChunkTags().contains(e.getChunkTag())
              ^ e.getNegation();
        }
      }
    }

    return thisMatched;
  }

  protected boolean testUnificationAndGroups(boolean matched, boolean lastReading,
                                             AnalyzedToken matchToken,
                                             PatternTokenMatcher elemMatcher, boolean alreadyTested) {
    boolean thisMatched = matched;
    boolean elemIsMatched = alreadyTested || elemMatcher.isMatched(matchToken);
    PatternToken elem = elemMatcher.getPatternToken();

    if (rule.testUnification) {
      if (matched && elem.isUnified()) {
        if (elem.isUniNegated()) {
          thisMatched = !unifier.isUnified(matchToken,
              elem.getUniFeatures(), lastReading,
              elemIsMatched);
        } else {
          if (elem.isLastInUnification()) {
            thisMatched = unifier.isUnified(matchToken,
                elem.getUniFeatures(), lastReading,
                elemIsMatched);
            if (thisMatched && rule.isGetUnified()) {
              unifiedTokens = unifier.getFinalUnified();
            }
          } else { // we don't care about the truth value, let it run
            unifier.isUnified(matchToken,
                elem.getUniFeatures(), lastReading,
                elemIsMatched);
          }
        }
      }
      if (!elem.isUnified()) {
        unifier.reset();
      }
    }
    elemMatcher.addMemberAndGroup(matchToken);
    if (lastReading) {
      thisMatched &= elemMatcher.checkAndGroup(thisMatched);
    }
    return thisMatched;
  }

  /**
   * @since 2.5
   */
  protected int getMinOccurrenceCorrection() {
    int minOccurCorrection = 0;
    for (PatternToken patternToken : rule.getPatternTokens()) {
      if (patternToken.getMinOccurrence() == 0) {
        minOccurCorrection++;
      }
    }
    return minOccurCorrection;
  }

  /**
   * @since 2.5
   */
  protected int skipMaxTokens(AnalyzedTokenReadings[] tokens, PatternTokenMatcher elem, int firstMatchToken, int prevSkipNext, PatternTokenMatcher prevElement, int m, int remainingElems) throws IOException {
    int maxSkip = 0;
    int maxOccurrences = elem.getPatternToken().getMaxOccurrence() == -1 ? Integer.MAX_VALUE : elem.getPatternToken().getMaxOccurrence();
    for (int j = 1; j < maxOccurrences && m+j < tokens.length - remainingElems; j++) {
      boolean nextAllElementsMatch = !tokens[m+j].isImmunized() &&
          testAllReadings(tokens, elem, prevElement, m+j, firstMatchToken, prevSkipNext);
      if (nextAllElementsMatch) {
        maxSkip++;
      } else {
        break;
      }
    }
    return maxSkip;
  }

}
