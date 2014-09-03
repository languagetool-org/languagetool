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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 2.3
 */
class DisambiguationPatternRuleReplacer extends AbstractPatternRulePerformer {

  List<Boolean> elementsMatched;

  public DisambiguationPatternRuleReplacer(DisambiguationPatternRule rule) {
    super(rule, rule.getLanguage().getDisambiguationUnifier());
    elementsMatched = new ArrayList<>(rule.getPatternElements().size());
  }

  public final AnalyzedSentence replace(final AnalyzedSentence sentence)
      throws IOException {
    List<ElementMatcher> elementMatchers = createElementMatchers();

    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    AnalyzedTokenReadings[] whTokens = sentence.getTokens();
    final int[] tokenPositions = new int[tokens.length + 1];
    final int patternSize = elementMatchers.size();
    final int limit = Math.max(0, tokens.length - patternSize + 1);
    ElementMatcher elem = null;
    boolean changed = false;

    elementsMatched.clear();
    for (ElementMatcher elementMatcher : elementMatchers) { //the list has exactly the same number
                                                          // of elements as the list of ElementMatchers
      elementsMatched.add(false);
    }

    int i = 0;
    int minOccurCorrection = getMinOccurrenceCorrection();
    while (i < limit + minOccurCorrection && !(rule.isSentStart() && i > 0)) {
      boolean allElementsMatch = false;
      unifiedTokens = null;
      int matchingTokens = 0;
      int skipShiftTotal = 0;
      int firstMatchToken = -1;
      int lastMatchToken;
      int firstMarkerMatchToken = -1;
      int lastMarkerMatchToken = -1;
      int prevSkipNext = 0;
      if (rule.isTestUnification()) {
        unifier.reset();
      }
      int minOccurSkip = 0;
      for (int k = 0; k < patternSize; k++) {
        final ElementMatcher prevElement = elem;
        elem = elementMatchers.get(k);
        elem.resolveReference(firstMatchToken, tokens, rule.getLanguage());
        final int nextPos = i + k + skipShiftTotal - minOccurSkip;
        prevMatched = false;
        if (prevSkipNext + nextPos >= tokens.length || prevSkipNext < 0) { // SENT_END?
          prevSkipNext = tokens.length - (nextPos + 1);
        }
        final int maxTok = Math.min(nextPos + prevSkipNext, tokens.length - (patternSize - k) + minOccurCorrection);
        for (int m = nextPos; m <= maxTok; m++) {
          allElementsMatch = testAllReadings(tokens, elem, prevElement, m, firstMatchToken, prevSkipNext);

          if (elem.getElement().getMinOccurrence() == 0) {
            final ElementMatcher nextElement = elementMatchers.get(k + 1);
            final boolean nextElementMatch = testAllReadings(tokens, nextElement, elem, m,
                firstMatchToken, prevSkipNext);
            if (nextElementMatch) {
              // this element doesn't match, but it's optional so accept this and continue
              allElementsMatch = true;
              minOccurSkip++;
              elementsMatched.set(k, false);
              break;
            }
          }
          if (allElementsMatch) {
            elementsMatched.set(k, true);
            int skipForMax = skipMaxTokens(tokens, elem, firstMatchToken, prevSkipNext,
                prevElement, m, patternSize - k -1);
            lastMatchToken = m + skipForMax;
            final int skipShift = lastMatchToken - nextPos;
            tokenPositions[matchingTokens] = skipShift + 1;
            prevSkipNext = elem.getElement().getSkipNext();
            matchingTokens++;
            skipShiftTotal += skipShift;
            if (firstMatchToken == -1) {
              firstMatchToken = lastMatchToken - skipForMax;
            }
            if (firstMarkerMatchToken == -1 && elem.getElement().isInsideMarker()) {
              firstMarkerMatchToken = lastMatchToken - skipForMax;
            }
            if (elem.getElement().isInsideMarker()) {
              lastMarkerMatchToken = lastMatchToken;
            }
            break;
          }
        }
        if (!allElementsMatch) {
          break;
        }
      }
      if (allElementsMatch && matchingTokens == patternSize || matchingTokens == patternSize - minOccurSkip && firstMatchToken != -1) {
        whTokens = executeAction(sentence, whTokens, unifiedTokens, firstMatchToken, lastMarkerMatchToken, matchingTokens, tokenPositions);
        changed = true;
      }
      i++;
    }
    if (changed) {
      return new AnalyzedSentence(whTokens, sentence.getWhPositions());
    }
    return sentence;
  }

  @Override
  protected int skipMaxTokens(AnalyzedTokenReadings[] tokens, ElementMatcher elem, int firstMatchToken, int prevSkipNext, ElementMatcher prevElement, int m, int remainingElems) throws IOException {
    int maxSkip = 0;
    int maxOccurrences = elem.getElement().getMaxOccurrence() == -1 ? Integer.MAX_VALUE : elem.getElement().getMaxOccurrence();
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


  private AnalyzedTokenReadings[] executeAction(final AnalyzedSentence sentence,
                                                final AnalyzedTokenReadings[] whiteTokens,
                                                final AnalyzedTokenReadings[] unifiedTokens,
                                                final int firstMatchToken, int lastMatchToken,
                                                final int matchingTokens, final int[] tokenPositions) {
    final AnalyzedTokenReadings[] whTokens = whiteTokens.clone();
    final DisambiguationPatternRule rule = (DisambiguationPatternRule) this.rule;

    int correctedStPos = 0;
    int startPositionCorrection = rule.getStartPositionCorrection();
    int endPositionCorrection = rule.getEndPositionCorrection();

    int matchingTokensWithCorrection = matchingTokens;

    List<Integer> tokenPositionList = new ArrayList<>();
    for (int i : tokenPositions) {
      tokenPositionList.add(i);
    }

    if (startPositionCorrection > 0) {
      correctedStPos--; //token positions are shifted by 1
      for (int j = 0; j < elementsMatched.size(); j++) {
        if (!elementsMatched.get(j)) {
          tokenPositionList.add(j, 0);    // add zero-length token corresponding to the non-matching pattern element so that position count is fine
        }
      }

      for (int l = 0; l <= startPositionCorrection; l++) {
        correctedStPos += tokenPositionList.get(l);
      }

      int w = startPositionCorrection; // adjust to make sure the token count is fine as it's checked later
      for (int j = 0; j <= w; j++) {
        if (j < elementsMatched.size() && !elementsMatched.get(j)) {
          startPositionCorrection--;
        }
      }
    }

    if (endPositionCorrection < 0) { // adjust the end position correction if one of the elements has not been matched
      for (int d = startPositionCorrection; d < elementsMatched.size(); d++) {
        if (!elementsMatched.get(d)) {
          endPositionCorrection++;
        }
      }
    }

    if (lastMatchToken != -1) {
      int maxPosCorrection = Math.max((lastMatchToken + 1 - (firstMatchToken + correctedStPos)) - matchingTokens, 0);
      matchingTokensWithCorrection += maxPosCorrection;
    }

    final int fromPos = sentence.getOriginalPosition(firstMatchToken + correctedStPos);

    final boolean spaceBefore = whTokens[fromPos].isWhitespaceBefore();
    final DisambiguationPatternRule.DisambiguatorAction disAction = rule.getAction();

    final AnalyzedToken[] newTokenReadings = rule.getNewTokenReadings();
    final Match matchElement = rule.getMatchElement();
    final String disambiguatedPOS = rule.getDisambiguatedPOS();

    switch (disAction) {
    case UNIFY:
      if (unifiedTokens != null) {
        //TODO: unifiedTokens.length is larger > matchingTokensWithCorrection in cases where there are no markers...
        if (unifiedTokens.length == matchingTokensWithCorrection - startPositionCorrection + endPositionCorrection) {
          if (whTokens[sentence.getOriginalPosition(firstMatchToken
              + correctedStPos + unifiedTokens.length - 1)].isSentenceEnd()) {
            unifiedTokens[unifiedTokens.length - 1].setSentEnd();
          }
          for (int i = 0; i < unifiedTokens.length; i++) {
            final int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
            unifiedTokens[i].setStartPos(whTokens[position].getStartPos());
            final String prevValue = whTokens[position].toString();
            final String prevAnot = whTokens[position].getHistoricalAnnotations();
            List<ChunkTag> chTags = whTokens[position].getChunkTags();
            whTokens[position] = unifiedTokens[i];
            whTokens[position].setChunkTags(chTags);
            annotateChange(whTokens[position], prevValue, prevAnot);
          }
        }
      }
      break;
    case REMOVE:
      if (newTokenReadings != null && newTokenReadings.length > 0) {
        if (newTokenReadings.length == matchingTokensWithCorrection
            - startPositionCorrection + endPositionCorrection) {
          for (int i = 0; i < newTokenReadings.length; i++) {
            final int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
            final String prevValue = whTokens[position].toString();
            final String prevAnot = whTokens[position].getHistoricalAnnotations();
            whTokens[position].removeReading(newTokenReadings[i]);
            annotateChange(whTokens[position], prevValue, prevAnot);
          }
        }
      } else if (!StringTools.isEmpty(disambiguatedPOS)) { // negative filtering
        Pattern p = Pattern.compile(disambiguatedPOS);
        AnalyzedTokenReadings tmp = new AnalyzedTokenReadings(whTokens[fromPos].getReadings(),
            whTokens[fromPos].getStartPos());
        for (AnalyzedToken analyzedToken : tmp) {
          if (analyzedToken.getPOSTag() != null) {
            final Matcher mPos = p.matcher(analyzedToken.getPOSTag());
            if (mPos.matches()) {
              final int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos);
              final String prevValue = whTokens[position].toString();
              final String prevAnot = whTokens[position].getHistoricalAnnotations();
              whTokens[position].removeReading(analyzedToken);
              annotateChange(whTokens[position], prevValue, prevAnot);
            }
          }
        }
      }
      break;
    case ADD:
      if (newTokenReadings != null) {
        if (newTokenReadings.length == matchingTokensWithCorrection
            - startPositionCorrection + endPositionCorrection) {
          for (int i = 0; i < newTokenReadings.length; i++) {
            final String token;
            final int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
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
      for (int i = 0; i < matchingTokensWithCorrection - startPositionCorrection + endPositionCorrection; i++) {
        final int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
        Element myEl;
      if (elementsMatched.get(i + startPositionCorrection)) {
        myEl = rule.getPatternElements().get(i + startPositionCorrection);
        } else {
          int k = 1;
          while (i + startPositionCorrection + k < rule.getPatternElements().size() + endPositionCorrection &&
              !elementsMatched.get(i + startPositionCorrection + k)) {
            k++;
          }
        //FIXME: this is left to see whether this fails anywhere
         assert(i + k + startPositionCorrection < rule.getPatternElements().size());
         myEl = rule.getPatternElements().get(i + k + startPositionCorrection);
        }
        final Match tmpMatchToken = new Match(myEl.getPOStag(), null,
            true,
            myEl.getPOStag(),
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
      for (int i = 0; i < matchingTokensWithCorrection - startPositionCorrection + endPositionCorrection; i++) {
        whTokens[sentence.getOriginalPosition(firstMatchToken + correctedStPos + i)].immunize();
      }
      break;
    case IGNORE_SPELLING:
      for (int i = 0; i < matchingTokensWithCorrection - startPositionCorrection + endPositionCorrection; i++) {
        whTokens[sentence.getOriginalPosition(firstMatchToken + correctedStPos + i)].ignoreSpelling();
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
        break;
      }
      //fallthrough
    case REPLACE:
    default:
        if (newTokenReadings != null && newTokenReadings.length > 0) {
          if (newTokenReadings.length == matchingTokensWithCorrection - startPositionCorrection + endPositionCorrection) {
            for (int i = 0; i < newTokenReadings.length; i++) {
              final String token;
              final int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
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
    if (oldAtr.isImmunized()) {
      newAtr.immunize();
    }
    annotateChange(newAtr, prevValue, prevAnot);
    return newAtr;
  }
}
