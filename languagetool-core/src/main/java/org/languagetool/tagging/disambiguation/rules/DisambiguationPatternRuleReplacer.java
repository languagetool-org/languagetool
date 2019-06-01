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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.*;
import org.languagetool.tools.StringTools;

/**
 * @since 2.3
 */
class DisambiguationPatternRuleReplacer extends AbstractPatternRulePerformer {

  private final List<Boolean> pTokensMatched;

  DisambiguationPatternRuleReplacer(DisambiguationPatternRule rule) {
    super(rule, rule.getLanguage().getDisambiguationUnifier());
    pTokensMatched = new ArrayList<>(rule.getPatternTokens().size());
  }

  public final AnalyzedSentence replace(AnalyzedSentence sentence)
      throws IOException {
    List<PatternTokenMatcher> patternTokenMatchers = createElementMatchers();

    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    AnalyzedTokenReadings[] preDisambigTokens = sentence.getTokens();
    AnalyzedTokenReadings[] whTokens = sentence.getTokens();
    int[] tokenPositions = new int[tokens.length + 1];
    int patternSize = patternTokenMatchers.size();
    int limit = Math.max(0, tokens.length - patternSize + 1);
    PatternTokenMatcher pTokenMatcher = null;
    boolean changed = false;

    pTokensMatched.clear();
    // the list has exactly the same number of elements as the list of ElementMatchers:
    for (int i = 0; i < patternTokenMatchers.size(); i++) {
      pTokensMatched.add(Boolean.FALSE);
    }

    int i = 0;
    int minOccurCorrection = getMinOccurrenceCorrection();
    while (i < limit + minOccurCorrection && !(rule.isSentStart() && i > 0)) {
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
        int nextPos = i + k + skipShiftTotal - minOccurSkip;
        prevMatched = false;
        if (prevSkipNext + nextPos >= tokens.length || prevSkipNext < 0) { // SENT_END?
          prevSkipNext = tokens.length - (nextPos + 1);
        }
        int maxTok = Math.min(nextPos + prevSkipNext, tokens.length - (patternSize - k) + minOccurCorrection);
        for (int m = nextPos; m <= maxTok; m++) {
          allElementsMatch = testAllReadings(tokens, pTokenMatcher, prevTokenMatcher, m, firstMatchToken, prevSkipNext);

          if (pTokenMatcher.getPatternToken().getMinOccurrence() == 0 && k + 1 < patternTokenMatchers.size()) {
            PatternTokenMatcher nextElement = patternTokenMatchers.get(k + 1);
            boolean nextElementMatch = testAllReadings(tokens, nextElement, pTokenMatcher, m,
                firstMatchToken, prevSkipNext);
            if (nextElementMatch) {
              // this element doesn't match, but it's optional so accept this and continue
              allElementsMatch = true;
              minOccurSkip++;
              pTokensMatched.set(k, false);
              break;
            }
          }
          if (allElementsMatch) {
            pTokensMatched.set(k, true);
            int skipForMax = skipMaxTokens(tokens, pTokenMatcher, firstMatchToken, prevSkipNext,
                prevTokenMatcher, m, patternSize - k -1);
            lastMatchToken = m + skipForMax;
            int skipShift = lastMatchToken - nextPos;
            tokenPositions[matchingTokens] = skipShift + 1;
            prevSkipNext = pTokenMatcher.getPatternToken().getSkipNext();
            matchingTokens++;
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
      if (allElementsMatch && matchingTokens == patternSize || matchingTokens == patternSize - minOccurSkip && firstMatchToken != -1) {
        int ruleMatchFromPos = -1;
        int ruleMatchToPos = -1;
        int tokenCount = 0;
        for (AnalyzedTokenReadings token : tokens) {
          if (ruleMatchFromPos == -1 && tokenCount == firstMatchToken) {
            ruleMatchFromPos = token.getStartPos();
          }
          if (ruleMatchToPos == -1 && tokenCount == lastMatchToken) {
            ruleMatchToPos = token.getEndPos();
          }
          tokenCount++;
        }
        if (keepDespiteFilter(tokens, tokenPositions, firstMatchToken, lastMatchToken) && keepByDisambig(sentence, ruleMatchFromPos, ruleMatchToPos)) {
          whTokens = executeAction(sentence, whTokens, unifiedTokens, firstMatchToken, lastMarkerMatchToken, matchingTokens, tokenPositions);
          changed = true;
        }
      }
      i++;
    }
    if (changed) {
      return new AnalyzedSentence(whTokens, preDisambigTokens);
    }
    return sentence;
  }

  private boolean keepByDisambig(AnalyzedSentence sentence, int ruleMatchFromPos, int ruleMatchToPos) throws IOException {
    List<DisambiguationPatternRule> antiPatterns = rule.getAntiPatterns();
    for (DisambiguationPatternRule antiPattern : antiPatterns) {
      PatternRule disambigRule = new PatternRule("fake-disambig-id", rule.getLanguage(), antiPattern.getPatternTokens(), "desc", "msg", "short");
      RuleMatch[] matches = disambigRule.match(sentence);
      if (matches != null) {
        for (RuleMatch disMatch : matches) {
          if ((disMatch.getFromPos() <= ruleMatchFromPos && disMatch.getToPos() >= ruleMatchFromPos) ||  // left overlap of rule match start
              (disMatch.getFromPos() <= ruleMatchToPos && disMatch.getToPos() >= ruleMatchToPos) ||  // right overlap of rule match end
              (disMatch.getFromPos() >= ruleMatchFromPos && disMatch.getToPos() <= ruleMatchToPos)  // inside longer rule match
          ) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean keepDespiteFilter(AnalyzedTokenReadings[] tokens, int[] tokenPositions, int firstMatchToken, int lastMatchToken) {
    RuleFilter filter = rule.getFilter();
    if (filter != null) {
      RuleFilterEvaluator ruleFilterEval = new RuleFilterEvaluator(filter);
      List<Integer> tokensPos = new ArrayList<>();
      for (int tokenPosition : tokenPositions) {
        tokensPos.add(tokenPosition);
      }
      Map<String, String> resolvedArguments = ruleFilterEval.getResolvedArguments(rule.getFilterArguments(), tokens, tokensPos);
      AnalyzedTokenReadings[] relevantTokens = Arrays.copyOfRange(tokens, firstMatchToken, lastMatchToken + 1);
      return filter.matches(resolvedArguments, relevantTokens);
    }
    return true;
  }

  /* (non-Javadoc)
   * @see org.languagetool.rules.patterns.AbstractPatternRulePerformer#skipMaxTokens(org.languagetool.AnalyzedTokenReadings[], org.languagetool.rules.patterns.PatternTokenMatcher, int, int, org.languagetool.rules.patterns.PatternTokenMatcher, int, int)
   */
  @Override
  protected int skipMaxTokens(AnalyzedTokenReadings[] tokens, PatternTokenMatcher matcher, int firstMatchToken, int prevSkipNext, PatternTokenMatcher prevElement, int m, int remainingElems) throws IOException {
    int maxSkip = 0;
    int maxOccurrences = matcher.getPatternToken().getMaxOccurrence() == -1 ? Integer.MAX_VALUE : matcher.getPatternToken().getMaxOccurrence();
    for (int j = 1; j < maxOccurrences && m+j < tokens.length - remainingElems; j++) {
      boolean nextAllElementsMatch = testAllReadings(tokens, matcher, prevElement, m+j, firstMatchToken, prevSkipNext);
      if (nextAllElementsMatch) {
        maxSkip++;
      } else {
        break;
      }
    }
    return maxSkip;
  }


  private AnalyzedTokenReadings[] executeAction(AnalyzedSentence sentence,
                                                AnalyzedTokenReadings[] whiteTokens,
                                                AnalyzedTokenReadings[] unifiedTokens,
                                                int firstMatchToken, int lastMatchToken,
                                                int matchingTokens, int[] tokenPositions) {
    AnalyzedTokenReadings[] whTokens = whiteTokens.clone();
    DisambiguationPatternRule rule = (DisambiguationPatternRule) this.rule;

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
      for (int j = 0; j < pTokensMatched.size(); j++) {
        if (!pTokensMatched.get(j)) {
          tokenPositionList.add(j, 0);    // add zero-length token corresponding to the non-matching pattern element so that position count is fine
        }
      }

      for (int l = 0; l <= startPositionCorrection && tokenPositionList.size() > l; l++) {
        correctedStPos += tokenPositionList.get(l);
      }

      int w = startPositionCorrection; // adjust to make sure the token count is fine as it's checked later
      for (int j = 0; j <= w; j++) {
        if (j < pTokensMatched.size() && !pTokensMatched.get(j)) {
          startPositionCorrection--;
        }
      }
    }

    if (endPositionCorrection < 0) { // adjust the end position correction if one of the elements has not been matched
      for (int d = startPositionCorrection; d < pTokensMatched.size(); d++) {
        if (!pTokensMatched.get(d)) {
          endPositionCorrection++;
        }
      }
    }

    if (lastMatchToken != -1) {
      int maxPosCorrection = Math.max((lastMatchToken + 1 - (firstMatchToken + correctedStPos)) - matchingTokens, 0);
      matchingTokensWithCorrection += maxPosCorrection;
    }

    int fromPos = sentence.getOriginalPosition(firstMatchToken + correctedStPos);

    boolean spaceBefore = whTokens[fromPos].isWhitespaceBefore();
    DisambiguationPatternRule.DisambiguatorAction disAction = rule.getAction();

    AnalyzedToken[] newTokenReadings = rule.getNewTokenReadings();
    Match matchElement = rule.getMatchElement();
    String disambiguatedPOS = rule.getDisambiguatedPOS();

    switch (disAction) {
    case UNIFY:
      if (unifiedTokens != null &&
          unifiedTokens.length == matchingTokensWithCorrection - startPositionCorrection + endPositionCorrection) {
        //TODO: unifiedTokens.length is larger > matchingTokensWithCorrection in cases where there are no markers...
        if (whTokens[sentence.getOriginalPosition(firstMatchToken
            + correctedStPos + unifiedTokens.length - 1)].isSentenceEnd()) {
          unifiedTokens[unifiedTokens.length - 1].setSentEnd();
        }
        for (int i = 0; i < unifiedTokens.length; i++) {
          int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
          unifiedTokens[i].setStartPos(whTokens[position].getStartPos());
          String prevValue = whTokens[position].toString();
          String prevAnot = whTokens[position].getHistoricalAnnotations();
          List<ChunkTag> chTags = whTokens[position].getChunkTags();
          whTokens[position] = unifiedTokens[i];
          whTokens[position].setChunkTags(chTags);
          annotateChange(whTokens[position], prevValue, prevAnot);
        }
      }
      break;
    case REMOVE:
      if (newTokenReadings != null && newTokenReadings.length > 0) {
        if (newTokenReadings.length == matchingTokensWithCorrection
            - startPositionCorrection + endPositionCorrection) {
          for (int i = 0; i < newTokenReadings.length; i++) {
            int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
            String prevValue = whTokens[position].toString();
            String prevAnot = whTokens[position].getHistoricalAnnotations();
            whTokens[position].removeReading(newTokenReadings[i]);
            annotateChange(whTokens[position], prevValue, prevAnot);
          }
        }
      } else if (!StringTools.isEmpty(disambiguatedPOS)) { // negative filtering
        Pattern p = Pattern.compile(disambiguatedPOS);
        AnalyzedTokenReadings tmp = new AnalyzedTokenReadings(whTokens[fromPos].getReadings(),
            whTokens[fromPos].getStartPos());
        for (AnalyzedToken analyzedToken : tmp) {
          if (analyzedToken.getPOSTag() != null && p.matcher(analyzedToken.getPOSTag()).matches()) {
            int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos);
            String prevValue = whTokens[position].toString();
            String prevAnot = whTokens[position].getHistoricalAnnotations();
            whTokens[position].removeReading(analyzedToken);
            annotateChange(whTokens[position], prevValue, prevAnot);
          }
        }
      }
      break;
    case ADD:
      if (newTokenReadings != null && newTokenReadings.length == matchingTokensWithCorrection
            - startPositionCorrection + endPositionCorrection) {
        for (int i = 0; i < newTokenReadings.length; i++) {
          String token;
          int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
          if (newTokenReadings[i].getToken().isEmpty()) {
            token = whTokens[position].getToken();
          } else {
            token = newTokenReadings[i].getToken();
          }
          String lemma;
          if (newTokenReadings[i].getLemma() == null) {
            lemma = token;
          } else {
            lemma = newTokenReadings[i].getLemma();
          }
          AnalyzedToken newTok = new AnalyzedToken(token,
              newTokenReadings[i].getPOSTag(), lemma);
          String prevValue = whTokens[position].toString();
          String prevAnot = whTokens[position].getHistoricalAnnotations();
          whTokens[position].addReading(newTok);
          annotateChange(whTokens[position], prevValue, prevAnot);
        }
      }
      break;
    case FILTERALL:
      for (int i = 0; i < matchingTokensWithCorrection - startPositionCorrection + endPositionCorrection; i++) {
        int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
        PatternToken pToken;
        if (pTokensMatched.get(i + startPositionCorrection)) {
          pToken = rule.getPatternTokens().get(i + startPositionCorrection);
        } else {
          int k = 1;
          while (i + startPositionCorrection + k < rule.getPatternTokens().size() + endPositionCorrection &&
              !pTokensMatched.get(i + startPositionCorrection + k)) {
            k++;
          }
         pToken = rule.getPatternTokens().get(i + k + startPositionCorrection);
        }
        Match tmpMatchToken = new Match(pToken.getPOStag(), null,
            true,
            pToken.getPOStag(),
            null, Match.CaseConversion.NONE, false, false,
            Match.IncludeRange.NONE);

        MatchState matchState = tmpMatchToken.createState(rule.getLanguage().getSynthesizer(), whTokens[position]);
        String prevValue = whTokens[position].toString();
        String prevAnot = whTokens[position].getHistoricalAnnotations();
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
        Match tmpMatchToken = new Match(disambiguatedPOS, null,
            true, disambiguatedPOS, null,
            Match.CaseConversion.NONE, false, false,
            Match.IncludeRange.NONE);
        boolean newPOSmatches = false;

        // only apply filter rule when it matches previous tags:
        for (int i = 0; i < whTokens[fromPos].getReadingsLength(); i++) {
          if (!whTokens[fromPos].getAnalyzedToken(i).hasNoTag() &&
              whTokens[fromPos].getAnalyzedToken(i).getPOSTag() != null &&
              whTokens[fromPos].getAnalyzedToken(i).getPOSTag().matches(disambiguatedPOS)) {
            newPOSmatches = true;
            break;
          }
        }
        if (newPOSmatches) {
          MatchState matchState = tmpMatchToken.createState(rule.getLanguage().getSynthesizer(), whTokens[fromPos]);
          String prevValue = whTokens[fromPos].toString();
          String prevAnot = whTokens[fromPos].getHistoricalAnnotations();
          whTokens[fromPos] = matchState.filterReadings();
          annotateChange(whTokens[fromPos], prevValue, prevAnot);
        }
        break;
      }
      //fallthrough
    case REPLACE:
    default:
        if (newTokenReadings != null && newTokenReadings.length > 0) {
          if (newTokenReadings.length == matchingTokensWithCorrection - startPositionCorrection + endPositionCorrection) {
            for (int i = 0; i < newTokenReadings.length; i++) {
              String token;
              int position = sentence.getOriginalPosition(firstMatchToken + correctedStPos + i);
              if ("".equals(newTokenReadings[i].getToken())) { // empty token
                token = whTokens[position].getToken();
              } else {
                token = newTokenReadings[i].getToken();
              }
              String lemma;
              if (newTokenReadings[i].getLemma() == null) { // empty lemma
                lemma = token;
              } else {
                lemma = newTokenReadings[i].getLemma();
              }
              AnalyzedToken analyzedToken = new AnalyzedToken(token, newTokenReadings[i].getPOSTag(), lemma);
              AnalyzedTokenReadings toReplace = new AnalyzedTokenReadings(
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

          AnalyzedToken analyzedToken = new AnalyzedToken(whTokens[fromPos].getToken(), disambiguatedPOS, lemma);
          AnalyzedTokenReadings toReplace = new AnalyzedTokenReadings(
              analyzedToken, whTokens[fromPos].getStartPos());
          whTokens[fromPos] = replaceTokens(whTokens[fromPos], toReplace);
        } else {
          // using the match element
          MatchState matchElementState = matchElement.createState(rule.getLanguage().getSynthesizer(), whTokens[fromPos]);
          String prevValue = whTokens[fromPos].toString();
          String prevAnot = whTokens[fromPos].getHistoricalAnnotations();
          whTokens[fromPos] = matchElementState.filterReadings();
          whTokens[fromPos].setWhitespaceBefore(spaceBefore);
          annotateChange(whTokens[fromPos], prevValue, prevAnot);
        }
      }

    return whTokens;
  }

  private void annotateChange(AnalyzedTokenReadings atr,
      String prevValue, String prevAnot) {
    atr.setHistoricalAnnotations(prevAnot + "\n" + rule.getFullId() + ": "
        + prevValue + " -> " + atr);
  }

  private AnalyzedTokenReadings replaceTokens(AnalyzedTokenReadings oldAtr,
      AnalyzedTokenReadings newAtr) {
    String prevValue = oldAtr.toString();
    String prevAnot = oldAtr.getHistoricalAnnotations();
    boolean isSentEnd = oldAtr.isSentenceEnd();
    boolean isParaEnd = oldAtr.isParagraphEnd();
    boolean spaceBefore = oldAtr.isWhitespaceBefore();
    int startPosition = oldAtr.getStartPos();
    List<ChunkTag> chunkTags = oldAtr.getChunkTags();
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
