/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.disambiguation.rules;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.patterns.*;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.List;

/**
 * @since 2.3
 */
class DisambiguationPatternRuleReplacer extends AbstractPatternRulePerformer {

  public DisambiguationPatternRuleReplacer(DisambiguationPatternRule rule) {
    super(rule, rule.getLanguage().getDisambiguationUnifier());
  }

  public final AnalyzedSentence replace(final AnalyzedSentence text)
      throws IOException {
    List<ElementMatcher> elementMatchers = createElementMatchers();

    final AnalyzedTokenReadings[] tokens = text
        .getTokensWithoutWhitespace();
    AnalyzedTokenReadings[] whTokens = text.getTokens();
    final int[] tokenPositions = new int[tokens.length + 1];
    final int patternSize = elementMatchers.size();
    final int limit = Math.max(0, tokens.length - patternSize + 1);
    ElementMatcher elem = null;
    boolean changed = false;
    for (int i = 0; i < limit && !(rule.isSentStart() && i > 0); i++) {
      boolean allElementsMatch = false;
      unifiedTokens = null;
      int matchingTokens = 0;
      int skipShiftTotal = 0;
      int firstMatchToken = -1;
      int prevSkipNext = 0;
      if (rule.isTestUnification()) {
        unifier.reset();
      }
      int minOccurSkip = 0;
      for (int k = 0; k < patternSize; k++) {
        final ElementMatcher prevElement = elem;
        elem = elementMatchers.get(k);
        elem.resolveReference(firstMatchToken, tokens,
            rule.getLanguage());

        final int nextPos = i + k + skipShiftTotal - minOccurSkip;
        prevMatched = false;
        if (prevSkipNext + nextPos >= tokens.length || prevSkipNext < 0) { // SENT_END?
          prevSkipNext = tokens.length - (nextPos + 1);
        }
        final int maxTok = Math.min(nextPos + prevSkipNext,
            tokens.length - (patternSize - k));
        for (int m = nextPos; m <= maxTok; m++) {
          allElementsMatch = testAllReadings(tokens, elem,
              prevElement, m, firstMatchToken, prevSkipNext);
           if (elem.getElement().getMinOccurrence() == 0) {
                final ElementMatcher nextElement = elementMatchers.get(k + 1);
                final boolean nextElementMatch = !tokens[m].isImmunized() && testAllReadings(tokens, nextElement, elem, m,
                        firstMatchToken, prevSkipNext);
                if (nextElementMatch) {
                    // this element doesn't match, but it's optional so accept this and continue
                    allElementsMatch = true;
                    minOccurSkip++;
                    break;
                }
           }
          if (allElementsMatch) {
            final int skipShift = m - nextPos;
            tokenPositions[matchingTokens] = skipShift + 1;
            prevSkipNext = elem.getElement().getSkipNext();
            matchingTokens++;
            skipShiftTotal += skipShift;
            if (firstMatchToken == -1) {
              firstMatchToken = m;
            }
            break;
          }
        }
        if (!allElementsMatch) {
          break;
        }
      }
      if (allElementsMatch && matchingTokens == patternSize || matchingTokens == patternSize - minOccurSkip && firstMatchToken != -1) {
        whTokens = executeAction(text, whTokens, unifiedTokens,
            firstMatchToken, matchingTokens, tokenPositions);
        changed = true;
      }
    }
    if (changed) {
      return new AnalyzedSentence(whTokens, text.getWhPositions());
    }
    return text;
  }

  private AnalyzedTokenReadings[] executeAction(final AnalyzedSentence text,
      final AnalyzedTokenReadings[] whiteTokens,
      final AnalyzedTokenReadings[] unifiedTokens,
      final int firstMatchToken, final int matchingTokens,
      final int[] tokenPositions) {
    final AnalyzedTokenReadings[] whTokens = whiteTokens.clone();
    final DisambiguationPatternRule rule = (DisambiguationPatternRule) this.rule;

    int correctedStPos = 0;
    int startPositionCorrection = rule.getStartPositionCorrection();
    int endPositionCorrection = rule.getEndPositionCorrection();
    if (startPositionCorrection > 0) {
      for (int l = 0; l <= startPositionCorrection; l++) {
        correctedStPos += tokenPositions[l];
      }
      correctedStPos--;
    }
    final int fromPos = text.getOriginalPosition(firstMatchToken
        + correctedStPos);
    final boolean spaceBefore = whTokens[fromPos].isWhitespaceBefore();
    boolean filtered = false;
    final DisambiguationPatternRule.DisambiguatorAction disAction = rule.getAction();

    final AnalyzedToken[] newTokenReadings = rule.getNewTokenReadings();
    final Match matchElement = rule.getMatchElement();
    final String disambiguatedPOS = rule.getDisambiguatedPOS();

    switch (disAction) {
    case UNIFY:
      if (unifiedTokens != null) {
        if (unifiedTokens.length == matchingTokens - startPositionCorrection + endPositionCorrection) {
          if (whTokens[text.getOriginalPosition(firstMatchToken
              + correctedStPos + unifiedTokens.length - 1)].isSentenceEnd()) {
            unifiedTokens[unifiedTokens.length - 1].setSentEnd();
          }
          for (int i = 0; i < unifiedTokens.length; i++) {
            final int position = text.getOriginalPosition(firstMatchToken+ correctedStPos + i);
            unifiedTokens[i].setStartPos(whTokens[position].getStartPos());
            final String prevValue = whTokens[position].toString();
            final String prevAnot = whTokens[position].getHistoricalAnnotations();
            whTokens[position] = unifiedTokens[i];
            annotateChange(whTokens[position], prevValue, prevAnot);
          }
        }
      }
      break;
    case REMOVE:
      if (newTokenReadings != null) {
        if (newTokenReadings.length == matchingTokens
            - startPositionCorrection + endPositionCorrection) {
          for (int i = 0; i < newTokenReadings.length; i++) {
            final int position = text.getOriginalPosition(firstMatchToken + correctedStPos + i);
            final String prevValue = whTokens[position].toString();
            final String prevAnot = whTokens[position].getHistoricalAnnotations();
            whTokens[position].removeReading(newTokenReadings[i]);
            annotateChange(whTokens[position], prevValue, prevAnot);
          }
        }
      }
      break;
    case ADD:
      if (newTokenReadings != null) {
        if (newTokenReadings.length == matchingTokens
            - startPositionCorrection + endPositionCorrection) {
          for (int i = 0; i < newTokenReadings.length; i++) {
            final String token;
            final int position = text.getOriginalPosition(firstMatchToken+ correctedStPos + i);
            if ("".equals(newTokenReadings[i].getToken())) { // empty token
              token = whTokens[position].getToken();
            } else {
              token = newTokenReadings[i].getToken();
            }
            final String lemma;
            if (newTokenReadings[i].getLemma() == null) { // empty lemma
              lemma = token;
            } else {
              lemma = newTokenReadings[i].getLemma();
            }
            final AnalyzedToken newTok = new AnalyzedToken(token,
                newTokenReadings[i].getPOSTag(), lemma);
            final String prevValue = whTokens[position].toString();
            final String prevAnot = whTokens[position].getHistoricalAnnotations();
            whTokens[position].addReading(newTok);
            annotateChange(whTokens[position], prevValue, prevAnot);
          }
        }
      }
      break;
    case FILTERALL:
      for (int i = 0; i < matchingTokens - startPositionCorrection + endPositionCorrection; i++) {
        final int position = text.getOriginalPosition(firstMatchToken + correctedStPos + i);
        final Element myEl = rule.getPatternElements().get(i + startPositionCorrection);
        final Match tmpMatchToken = new Match(myEl.getPOStag(), null,
            true,
            myEl.getPOStag(), // myEl.isPOStagRegularExpression()
            null, Match.CaseConversion.NONE, false, false,
            Match.IncludeRange.NONE);

        MatchState matchState = tmpMatchToken.createState(rule.getLanguage().getSynthesizer(), whTokens[position]);
        final String prevValue = whTokens[position].toString();
        final String prevAnot = whTokens[position].getHistoricalAnnotations();
        whTokens[position] = matchState.filterReadings();
        annotateChange(whTokens[position], prevValue, prevAnot);
      }
      break;
    case IMMUNIZE:
      for (int i = 0; i < matchingTokens - startPositionCorrection + endPositionCorrection; i++) {
        whTokens[text.getOriginalPosition(firstMatchToken + correctedStPos + i)].immunize();
      }
      break;
    case IGNORE_SPELLING:
      for (int i = 0; i < matchingTokens - startPositionCorrection + endPositionCorrection; i++) {
        whTokens[text.getOriginalPosition(firstMatchToken + correctedStPos + i)].ignoreSpelling();
      }
      break;
    case FILTER:
      if (matchElement == null) { // same as REPLACE if using <match>
        final Match tmpMatchToken = new Match(disambiguatedPOS, null,
            true, disambiguatedPOS, null,
            Match.CaseConversion.NONE, false, false,
            Match.IncludeRange.NONE);

        final MatchState matchState = tmpMatchToken.createState(rule.getLanguage().getSynthesizer(), whTokens[fromPos]);
        final String prevValue = whTokens[fromPos].toString();
        final String prevAnot = whTokens[fromPos].getHistoricalAnnotations();
        whTokens[fromPos] = matchState.filterReadings();
        annotateChange(whTokens[fromPos], prevValue, prevAnot);
        filtered = true;
      }
      //fallthrough
    case REPLACE:
    default:
      if (!filtered) {
        if (newTokenReadings != null && newTokenReadings.length > 0) {
          if (newTokenReadings.length == matchingTokens - startPositionCorrection + endPositionCorrection) {
            for (int i = 0; i < newTokenReadings.length; i++) {
              final String token;
              final int position = text.getOriginalPosition(firstMatchToken + correctedStPos + i);
              if ("".equals(newTokenReadings[i].getToken())) { // empty token
                token = whTokens[position].getToken();
              } else {
                token = newTokenReadings[i].getToken();
              }
              final String lemma;
              if (newTokenReadings[i].getLemma() == null) { // empty lemma
                lemma = token;
              } else {
                lemma = newTokenReadings[i].getLemma();
              }
              final AnalyzedToken analyzedToken = new AnalyzedToken(token, newTokenReadings[i].getPOSTag(), lemma);
              final AnalyzedTokenReadings toReplace = new AnalyzedTokenReadings(
                  analyzedToken,
                  whTokens[fromPos].getStartPos());
              whTokens[position] = replaceTokens(
                  whTokens[position], toReplace);
            }
          }
        } else if (matchElement == null) {
          String lemma = "";
          for (AnalyzedToken analyzedToken : whTokens[fromPos]) {
            if (analyzedToken.getPOSTag() != null
                && analyzedToken.getPOSTag().equals(disambiguatedPOS) && analyzedToken.getLemma() != null) {
              lemma = analyzedToken.getLemma();
            }
          }
          if (StringTools.isEmpty(lemma)) {
            lemma = whTokens[fromPos].getAnalyzedToken(0).getLemma();
          }

          final AnalyzedToken analyzedToken = new AnalyzedToken(whTokens[fromPos].getToken(), disambiguatedPOS, lemma);
          final AnalyzedTokenReadings toReplace = new AnalyzedTokenReadings(
              analyzedToken, whTokens[fromPos].getStartPos());
          whTokens[fromPos] = replaceTokens(whTokens[fromPos], toReplace);
        } else {
          // using the match element
          final MatchState matchElementState = matchElement.createState(rule.getLanguage().getSynthesizer(), whTokens[fromPos]);
          final String prevValue = whTokens[fromPos].toString();
          final String prevAnot = whTokens[fromPos].getHistoricalAnnotations();
          whTokens[fromPos] = matchElementState.filterReadings();
          whTokens[fromPos].setWhitespaceBefore(spaceBefore);
          annotateChange(whTokens[fromPos], prevValue, prevAnot);
        }
      }

    }
    return whTokens;
  }

  private void annotateChange(AnalyzedTokenReadings atr,
      final String prevValue, String prevAnot) {
    atr.setHistoricalAnnotations(prevAnot + "\n" + rule.getId() + ":"
        + rule.getSubId() + " " + prevValue + " -> " + atr.toString());
  }

  private AnalyzedTokenReadings replaceTokens(AnalyzedTokenReadings oldAtr,
      final AnalyzedTokenReadings newAtr) {
    final String prevValue = oldAtr.toString();
    final String prevAnot = oldAtr.getHistoricalAnnotations();
    final boolean isSentEnd = oldAtr.isSentenceEnd();
    final boolean isParaEnd = oldAtr.isParagraphEnd();
    final boolean spaceBefore = oldAtr.isWhitespaceBefore();
    final int startPosition = oldAtr.getStartPos();
    final List<ChunkTag> chunkTags = oldAtr.getChunkTags();
    if (isSentEnd) {
      newAtr.setSentEnd();
    }
    if (isParaEnd) {
      newAtr.setParagraphEnd();
    }
    newAtr.setWhitespaceBefore(spaceBefore);
    newAtr.setStartPos(startPosition);
    newAtr.setChunkTags(chunkTags);
    annotateChange(newAtr, prevValue, prevAnot);
    return newAtr;
  }
}
