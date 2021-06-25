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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.chunking.ChunkTag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @since 2.3
 */
public abstract class AbstractPatternRulePerformer {

  protected boolean prevMatched;
  protected AbstractTokenBasedRule rule;
  protected Unifier unifier;
  protected AnalyzedTokenReadings[] unifiedTokens;
  private final List<PatternTokenMatcher> patternTokenMatchers;
  private final int patternSize;
  private final int minOccurCorrection;

  protected AbstractPatternRulePerformer(AbstractTokenBasedRule rule, Unifier unifier) {
    this.rule = Objects.requireNonNull(rule);
    this.unifier = Objects.requireNonNull(unifier);
    patternTokenMatchers = createElementMatchers();
    patternSize = patternTokenMatchers.size();
    minOccurCorrection = getMinOccurrenceCorrection();
  }

  private List<PatternTokenMatcher> createElementMatchers() {
    List<PatternTokenMatcher> patternTokenMatchers = new ArrayList<>(rule.patternTokens.size());
    for (PatternToken pToken : rule.patternTokens) {
      PatternTokenMatcher matcher = new PatternTokenMatcher(pToken);
      patternTokenMatchers.add(matcher);
    }
    return patternTokenMatchers;
  }

  protected void doMatch(AnalyzedSentence sentence, AnalyzedTokenReadings[] tokens, MatchConsumer consumer) throws IOException {
    AbstractTokenBasedRule.TokenHint anchor = rule.anchorHint;
    List<Integer> anchorIndices = anchor == null || isInterpretPosTagsPreDisambiguation() ? null : anchor.getPossibleIndices(sentence);

    int[] tokenPositions = new int[patternTokenMatchers.size()];
    int limit = rule.isSentStart() ? 1 : Math.max(0, tokens.length - patternSize + 1) + minOccurCorrection;
    if (anchorIndices != null) {
      for (Integer anchorIndex : anchorIndices) {
        int i = anchorIndex - anchor.tokenIndex;
        if (i >= 0 && i < limit) {
          matchFrom(i, tokens, consumer, tokenPositions);
        }
      }
    } else {
      for (int i = 0; i < limit; i++) {
        matchFrom(i, tokens, consumer, tokenPositions);
      }
    }
  }

  private void matchFrom(int startIndex, AnalyzedTokenReadings[] tokens, MatchConsumer consumer, int[] tokenPositions) throws IOException {
    PatternTokenMatcher pTokenMatcher = null;
    int skipShiftTotal = 0;
    boolean allElementsMatch = false;
    unifiedTokens = null;
    int matchingTokens = 0;
    int firstMatchToken = -1;
    int lastMatchToken = -1;
    int firstMarkerMatchToken = -1;
    int lastMarkerMatchToken = -1;
    int prevSkipNext = 0;
    if (rule.isTestUnification()) {
      unifier.reset();
    }
    int minOccurSkip = 0;
    for (int k = 0; k < patternSize; k++) {
      PatternTokenMatcher prevTokenMatcher = pTokenMatcher;
      pTokenMatcher = patternTokenMatchers.get(k);
      pTokenMatcher.resolveReference(firstMatchToken, tokens, rule.getLanguage());
      int nextPos = startIndex + k + skipShiftTotal - minOccurSkip;
      prevMatched = false;
      if (prevSkipNext + nextPos >= tokens.length || prevSkipNext < 0) { // SENT_END?
        prevSkipNext = tokens.length - (nextPos + 1);
      }
      int maxTok = Math.min(nextPos + prevSkipNext, tokens.length - (patternSize - k) + minOccurCorrection);
      for (int m = nextPos; m <= maxTok; m++) {
        allElementsMatch = testAllReadings(tokens, pTokenMatcher, prevTokenMatcher, m, firstMatchToken, prevSkipNext);

        if (pTokenMatcher.getPatternToken().getMinOccurrence() == 0) {
          boolean foundNext = false;
          for (int k2 = k + 1; k2 < patternSize; k2++) {
            PatternTokenMatcher nextElement = patternTokenMatchers.get(k2);
            boolean nextElementMatch = testAllReadings(tokens, nextElement, pTokenMatcher, m,
              firstMatchToken, prevSkipNext);
            if (nextElementMatch) {
              // this element doesn't match, but it's optional so accept this and continue
              allElementsMatch = true;
              minOccurSkip++;
              tokenPositions[matchingTokens++] = 0;
              foundNext = true;
              break;
            } else if (nextElement.getPatternToken().getMinOccurrence() > 0) {
              break;
            }
          }
          if (foundNext) {
            break;
          }
        }

        if (allElementsMatch) {
          int skipForMax = skipMaxTokens(tokens, pTokenMatcher, firstMatchToken, prevSkipNext,
            prevTokenMatcher, m, patternSize - k - 1);
          lastMatchToken = m + skipForMax;
          int skipShift = lastMatchToken - nextPos;
          tokenPositions[matchingTokens++] = skipShift + 1;
          prevSkipNext = translateElementNo(pTokenMatcher.getPatternToken().getSkipNext());
          skipShiftTotal += skipShift;
          if (firstMatchToken == -1) {
            firstMatchToken = lastMatchToken - skipForMax;
          }
          if (firstMarkerMatchToken == -1 && pTokenMatcher.getPatternToken().isInsideMarker()) {
            firstMarkerMatchToken = lastMatchToken - skipForMax;
          }
          if (pTokenMatcher.getPatternToken().isInsideMarker()) {
            lastMarkerMatchToken = lastMatchToken;
          }
          break;
        }
      }
      if (!allElementsMatch) {
        break;
      }
    }
    if (allElementsMatch && matchingTokens == patternSize) {
      consumer.consume(tokenPositions, firstMatchToken, lastMatchToken, firstMarkerMatchToken, lastMarkerMatchToken);
    }
  }

  protected boolean isInterpretPosTagsPreDisambiguation() {
    return rule instanceof PatternRule && ((PatternRule) rule).isInterpretPosTagsPreDisambiguation();
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
      if (!thisMatched && (prevElement == null || !prevElement.getPatternToken().hasCurrentOrNextExceptions())) {
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
    ChunkTag chunkTag = matcher.getPatternToken().getChunkTag();
    if (chunkTag != null) {
      if (chunkTag.isRegexp()) {
        thisMatched &= tokens[tokenNo].getChunkTags().stream().anyMatch(k -> k.getChunkTag().matches(chunkTag.getChunkTag()))
                        ^ matcher.getPatternToken().getNegation();
      } else {
        thisMatched &= tokens[tokenNo].getChunkTags().contains(chunkTag)
                        ^ matcher.getPatternToken().getNegation();
      }
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

  private int getMinOccurrenceCorrection() {
    int minOccurCorrection = 0;
    for (PatternToken patternToken : rule.getPatternTokens()) {
      if (patternToken.getMinOccurrence() == 0) {
        minOccurCorrection++;
      }
    }
    return minOccurCorrection;
  }

  private int skipMaxTokens(AnalyzedTokenReadings[] tokens, PatternTokenMatcher elem, int firstMatchToken, int prevSkipNext, PatternTokenMatcher prevElement, int m, int remainingElems) throws IOException {
    int maxSkip = 0;
    int maxOccurrences = elem.getPatternToken().getMaxOccurrence() == -1 ? Integer.MAX_VALUE : elem.getPatternToken().getMaxOccurrence();
    for (int j = 1; j < maxOccurrences && m+j < tokens.length - remainingElems; j++) {
      boolean nextAllElementsMatch = testAllReadings(tokens, elem, prevElement, m+j, firstMatchToken, prevSkipNext);
      if (nextAllElementsMatch) {
        maxSkip++;
      } else {
        break;
      }
    }
    return maxSkip;
  }

  int translateElementNo(int i) {
    return i;
  }

  protected interface MatchConsumer {
    void consume(int[] tokenPositions,
                 int firstMatchToken,
                 int lastMatchToken, int firstMarkerMatchToken, int lastMarkerMatchToken) throws IOException;
  }
}
