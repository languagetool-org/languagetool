/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.RuleMatchFilter;
import org.languagetool.rules.RuleWithMaxFilter;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Matches a pattern rule against text.
 */
final class PatternRuleMatcher extends AbstractPatternRulePerformer implements RuleMatcher {

  private static final String SUGGESTION_START_TAG = "<suggestion>";
  private static final String SUGGESTION_END_TAG = "</suggestion>";
  private static final String MISTAKE = "<mistake/>";

  private final boolean useList;
  private final List<PatternTokenMatcher> patternTokenMatchers;

  PatternRuleMatcher(PatternRule rule, boolean useList) {
    super(rule, rule.getLanguage().getUnifier());
    this.useList = useList;
    this.patternTokenMatchers = createElementMatchers();
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();

    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    List<Integer> tokenPositions = new ArrayList<>(tokens.length + 1);
    int patternSize = patternTokenMatchers.size();
    int limit = Math.max(0, tokens.length - patternSize + 1);
    PatternTokenMatcher pTokenMatcher = null;
    int i = 0;
    int minOccurCorrection = getMinOccurrenceCorrection();
    while (i < limit + minOccurCorrection && !(rule.isSentStart() && i > 0)) {
      int skipShiftTotal = 0;
      boolean allElementsMatch = false;
      int firstMatchToken = -1;
      int lastMatchToken = -1;
      int firstMarkerMatchToken = -1;
      int lastMarkerMatchToken = -1;
      int prevSkipNext = 0;
      if (rule.isTestUnification()) {
        unifier.reset();
      }
      tokenPositions.clear();
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
          allElementsMatch = !tokens[m].isImmunized() && testAllReadings(tokens, pTokenMatcher, prevTokenMatcher, m, firstMatchToken, prevSkipNext);

          if (pTokenMatcher.getPatternToken().getMinOccurrence() == 0) {
            boolean foundNext = false;
            for (int k2 = k + 1; k2 < patternSize; k2++) {
              PatternTokenMatcher nextElement = patternTokenMatchers.get(k2);
              boolean nextElementMatch = !tokens[m].isImmunized() && testAllReadings(tokens, nextElement, pTokenMatcher, m,
                  firstMatchToken, prevSkipNext);
              if (nextElementMatch) {
                // this element doesn't match, but it's optional so accept this and continue
                allElementsMatch = true;
                minOccurSkip++;
                tokenPositions.add(0);
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
                prevTokenMatcher, m, patternSize - k -1);
            lastMatchToken = m + skipForMax;
            int skipShift = lastMatchToken - nextPos;
            tokenPositions.add(skipShift + 1);
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
      if (allElementsMatch && tokenPositions.size() == patternSize) {
        RuleMatch ruleMatch = createRuleMatch(tokenPositions,
            tokens, firstMatchToken, lastMatchToken, firstMarkerMatchToken, lastMarkerMatchToken);
        if (ruleMatch != null) {
          ruleMatches.add(ruleMatch);
        }
      }
      i++;
    }
    RuleMatchFilter maxFilter = new RuleWithMaxFilter();
    List<RuleMatch> filteredMatches = maxFilter.filter(ruleMatches);
    return filteredMatches.toArray(new RuleMatch[filteredMatches.size()]);
  }

  @Nullable
  private RuleMatch createRuleMatch(List<Integer> tokenPositions,
      AnalyzedTokenReadings[] tokens, int firstMatchToken,
      int lastMatchToken, int firstMarkerMatchToken, int lastMarkerMatchToken) throws IOException {
    PatternRule rule = (PatternRule) this.rule;
    String errMessage = formatMatches(tokens, tokenPositions,
            firstMatchToken, rule.getMessage(), rule.getSuggestionMatches());
    String shortErrMessage = formatMatches(tokens, tokenPositions,
        firstMatchToken, rule.getShortMessage(), rule.getSuggestionMatches());
    String suggestionsOutMsg = formatMatches(tokens, tokenPositions,
            firstMatchToken, rule.getSuggestionsOutMsg(), rule.getSuggestionMatchesOutMsg());
    int correctedStPos = 0;
    if (rule.startPositionCorrection > 0) {
      for (int l = 0; l <= Math.min(rule.startPositionCorrection, tokenPositions.size() - 1); l++) {
        correctedStPos += tokenPositions.get(l);
      }
      correctedStPos--;
    }
    int idx = firstMatchToken + correctedStPos;
    if (idx >= tokens.length) {
      // TODO: hacky workaround, find a proper solution. See EnglishPatternRuleTest.testBug()
      // This is important when the reference points to a token with min="0", which has not been
      // matched... the subsequent match elements need to be renumbered, I guess, and that one
      // silently discarded
      idx = tokens.length - 1;
    }
    AnalyzedTokenReadings firstMatchTokenObj = tokens[idx];
    boolean startsWithUppercase = StringTools.startsWithUppercase(firstMatchTokenObj.getToken())
        && matchPreservesCase(rule.getSuggestionMatches(), rule.getMessage())
        && matchPreservesCase(rule.getSuggestionMatchesOutMsg(), rule.getSuggestionsOutMsg());

    if (firstMatchTokenObj.isSentenceStart() && tokens.length > firstMatchToken + correctedStPos + 1) {
      // make uppercasing work also at sentence start:
      firstMatchTokenObj = tokens[firstMatchToken + correctedStPos + 1];
      startsWithUppercase = StringTools.startsWithUppercase(firstMatchTokenObj.getToken());
    }
    if (firstMarkerMatchToken == -1) {
      firstMarkerMatchToken = firstMatchToken;
    }
    int fromPos = tokens[firstMarkerMatchToken].getStartPos();
    // FIXME: this is fishy, assumes that comma should always come before whitespace:
    if (errMessage.contains(SUGGESTION_START_TAG + ",") && firstMarkerMatchToken >= 1) {
      fromPos = tokens[firstMarkerMatchToken - 1].getStartPos()
          + tokens[firstMarkerMatchToken - 1].getToken().length();
    }
    if (lastMarkerMatchToken == -1) {
      lastMarkerMatchToken = lastMatchToken;
    }
    AnalyzedTokenReadings token = tokens[Math.min(lastMarkerMatchToken, tokens.length-1)];
    int toPos = token.getEndPos();
    if (fromPos < toPos) { // this can happen with some skip="-1" when the last token is not matched
      //now do some spell-checking:
      if (!(errMessage.contains(PatternRuleHandler.PLEASE_SPELL_ME) && errMessage.contains(MISTAKE))) {
        String clearMsg = errMessage.replaceAll(PatternRuleHandler.PLEASE_SPELL_ME, "").replaceAll(MISTAKE, "");
        RuleMatch ruleMatch = new RuleMatch(rule, fromPos, toPos, clearMsg,
                shortErrMessage, startsWithUppercase, suggestionsOutMsg);
        if (rule.getFilter() != null) {
          RuleFilterEvaluator evaluator = new RuleFilterEvaluator(rule.getFilter());
          AnalyzedTokenReadings[] patternTokens = Arrays.copyOfRange(tokens, firstMatchToken, lastMatchToken + 1);
          return evaluator.runFilter(rule.getFilterArguments(), ruleMatch, patternTokens, tokenPositions);
        } else {
          return ruleMatch; 
        }
      }
    } // failed to create any rule match...
    return null;
  }

  /**
   * Checks if the suggestion starts with a match that is supposed to preserve
   * case. If it does not, perform the default conversion to uppercase.
   * @return true, if the match preserves the case of the token.
   */
  private boolean matchPreservesCase(List<Match> suggestionMatches, String msg) {
    if (suggestionMatches != null && !suggestionMatches.isEmpty()) {
      //PatternRule rule = (PatternRule) this.rule;
      int sugStart = msg.indexOf(SUGGESTION_START_TAG) + SUGGESTION_START_TAG.length();
      for (Match sMatch : suggestionMatches) {
        if (!sMatch.isInMessageOnly() && sMatch.convertsCase()
            && msg.charAt(sugStart) == '\\') {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Gets the index of the element indexed by i, adding any offsets because of
   * the phrases in the rule.
   * @param i Current element index.
   * @return int Index translated into XML element no.
   */
  private int translateElementNo(int i) {
    if (!useList || i < 0) {
      return i;
    }
    int j = 0;
    PatternRule rule = (PatternRule) this.rule;
    for (int k = 0; k < i; k++) {
      j += rule.getElementNo().get(k);
    }
    return j;
  }

  /**
   * Replace back references generated with &lt;match&gt; and \\1 in message
   * using Match class, and take care of skipping.
   * @param tokenReadings Array of AnalyzedTokenReadings that were matched against the pattern
   * @param positions Array of relative positions of matched tokens
   * @param firstMatchTok Position of the first matched token
   * @param errorMsg String containing suggestion markup
   * @return String Formatted message.
   */
  private String formatMatches(AnalyzedTokenReadings[] tokenReadings,
      List<Integer> positions, int firstMatchTok, String errorMsg,
      List<Match> suggestionMatches) throws IOException {
    String errorMessage = errorMsg;
    int matchCounter = 0;
    int[] numbersToMatches = new int[errorMsg.length()];
    boolean newWay = false;
    int errLen = errorMessage.length();
    int errMarker = errorMessage.indexOf('\\');
    boolean numberFollows = false;
    if (errMarker >= 0 && errMarker < errLen - 1) {
      numberFollows = StringTools.isPositiveNumber(errorMessage.charAt(errMarker + 1));
    }
    while (errMarker >= 0 && numberFollows) {
      int backslashPos = errorMessage.indexOf('\\');
      if (backslashPos >= 0 && StringTools.isPositiveNumber(errorMessage.charAt(backslashPos + 1))) {
        int numLen = 1;
        while (backslashPos + numLen < errorMessage.length()
            && Character.isDigit(errorMessage.charAt(backslashPos + numLen))) {
          numLen++;
        }
        int j = Integer.parseInt(errorMessage.substring(backslashPos + 1, backslashPos
            + numLen)) - 1;
        int repTokenPos = 0;
        int nextTokenPos = 0;
        for (int l = 0; l <= Math.min(j, positions.size() - 1); l++) {
          repTokenPos += positions.get(l);
        }
        if (j + 1 < positions.size()) {
          nextTokenPos = firstMatchTok + repTokenPos + positions.get(j + 1);
        }

        if (suggestionMatches != null && suggestionMatches.size() > 0) {
          if (matchCounter < suggestionMatches.size()) {
            numbersToMatches[j] = matchCounter;
            // if token is optional remove it from suggestions:
            String[] matches;
            if (j >= positions.size()) {
              matches = concatMatches(matchCounter, j, firstMatchTok + repTokenPos, tokenReadings, nextTokenPos, suggestionMatches);
            } else if (positions.get(j) != 0) {
              matches = concatMatches(matchCounter, j, firstMatchTok + repTokenPos, tokenReadings, nextTokenPos, suggestionMatches);
            } else {
              matches = new String[] { "" };
            }
            String leftSide = errorMessage.substring(0, backslashPos);
            String rightSide = errorMessage.substring(backslashPos + numLen);
            if (matches.length == 1) {
              // if we removed optional token from suggestion then remove leading space from the next word
              if (matches[0].isEmpty() && (leftSide.endsWith(" ") || leftSide.endsWith("suggestion>")) && rightSide.startsWith(" ")) {
                errorMessage = leftSide + rightSide.substring(1);
              } else {
                errorMessage = leftSide + matches[0] + rightSide;
              }
            } else {
              errorMessage = formatMultipleSynthesis(matches, leftSide, rightSide);
            }
            matchCounter++;
            newWay = true;
          } else {
            // FIXME: is this correct? this is how we deal with multiple matches
            suggestionMatches.add(suggestionMatches.get(numbersToMatches[j]));
          }
        }
        if (!newWay) {
          // in case <match> elements weren't used (yet)
          errorMessage = errorMessage.replace("\\" + (j + 1),
              tokenReadings[firstMatchTok + repTokenPos - 1].getToken());
        }
      }
      errMarker = errorMessage.indexOf('\\');
      numberFollows = false;
      errLen = errorMessage.length();
      if (errMarker >= 0 && errMarker < errLen - 1) {
        numberFollows = StringTools.isPositiveNumber(errorMessage.charAt(errMarker + 1));
      }
    }
    return errorMessage;
  }

  // non-private for tests
  static String formatMultipleSynthesis(String[] matches,
      String leftSide, String rightSide) {
    String errorMessage;
    String suggestionLeft = "";
    String suggestionRight = "";
    String rightSideNew = rightSide;
    int sPos = leftSide.lastIndexOf(SUGGESTION_START_TAG);
    if (sPos >= 0) {
      suggestionLeft = leftSide.substring(sPos + SUGGESTION_START_TAG.length());
    }
    if (StringTools.isEmpty(suggestionLeft)) {
      errorMessage = leftSide;
    } else {
      errorMessage = leftSide.substring(0, leftSide.lastIndexOf(SUGGESTION_START_TAG)) + SUGGESTION_START_TAG;
    }
    int rPos = rightSide.indexOf(SUGGESTION_END_TAG);
    if (rPos >= 0) {
      suggestionRight = rightSide.substring(0, rPos);
    }
    if (!StringTools.isEmpty(suggestionRight)) {
      rightSideNew = rightSide.substring(rightSide.indexOf(SUGGESTION_END_TAG));
    }
    int lastLeftSugEnd = leftSide.indexOf(SUGGESTION_END_TAG);
    int lastLeftSugStart = leftSide.lastIndexOf(SUGGESTION_START_TAG);
    StringBuilder sb = new StringBuilder();
    sb.append(errorMessage);
    for (int z = 0; z < matches.length; z++) {
      sb.append(suggestionLeft);
      sb.append(matches[z]);
      sb.append(suggestionRight);
      if (z < matches.length - 1 && lastLeftSugEnd < lastLeftSugStart) {
        sb.append(SUGGESTION_END_TAG);
        sb.append(", ");
        sb.append(SUGGESTION_START_TAG);
      }
    }
    sb.append(rightSideNew);
    return sb.toString();
  }

  /**
   * Concatenates the matches, and takes care of phrases (including inflection
   * using synthesis).
   * @param start Position of the element as referenced by match element in the rule.
   * @param index The index of the element found in the matching sentence.
   * @param tokenIndex The position of the token in the AnalyzedTokenReadings array.
   * @param tokens Array of AnalyzedTokenReadings
   * @return @String[] Array of concatenated strings
   */
  private String[] concatMatches(int start, int index,
      int tokenIndex, AnalyzedTokenReadings[] tokens,
      int nextTokenPos, List<Match> suggestionMatches)
          throws IOException {
    String[] finalMatch;
    int len = phraseLen(index);
    Language language = rule.language;
    if (len == 1) {
      int skippedTokens = nextTokenPos - tokenIndex;
      MatchState matchState = suggestionMatches.get(start).createState(language.getSynthesizer(), tokens, tokenIndex - 1, skippedTokens);
      finalMatch = matchState.toFinalString(language);
      if (suggestionMatches.get(start).checksSpelling()
          && finalMatch.length == 1
          && "".equals(finalMatch[0])) {
        finalMatch = new String[1];
        finalMatch[0] = MISTAKE;
      }
    } else {
      List<String[]> matchList = new ArrayList<>();
      for (int i = 0; i < len; i++) {
        int skippedTokens = nextTokenPos - (tokenIndex + i);
        MatchState matchState = suggestionMatches.get(start).createState(language.getSynthesizer(), tokens, tokenIndex - 1 + i, skippedTokens);
        matchList.add(matchState.toFinalString(language));
      }
      return combineLists(matchList.toArray(new String[matchList.size()][]),
          new String[matchList.size()], 0, language);
    }
    return finalMatch;
  }

  private int phraseLen(int i) {
    PatternRule rule = (PatternRule) this.rule;
    List<Integer> elementNo = rule.getElementNo();
    if (!useList || i > elementNo.size() - 1) {
      return 1;
    }
    return elementNo.get(i);
  }

  /**
   * Creates a Cartesian product of the arrays stored in the input array.
   * @param input Array of string arrays to combine.
   * @param output Work array of strings.
   * @param r Starting parameter (use 0 to get all combinations).
   * @param lang Text language for adding spaces in some languages.
   * @return Combined array of String.
   */
  private static String[] combineLists(String[][] input,
      String[] output, int r, Language lang) {
    List<String> outputList = new ArrayList<>();
    if (r == input.length) {
      StringBuilder sb = new StringBuilder();
      for (int k = 0; k < output.length; k++) {
        sb.append(output[k]);
        if (k < output.length - 1) {
          sb.append(StringTools.addSpace(output[k + 1], lang));
        }
      }
      outputList.add(sb.toString());
    } else {
      for (int c = 0; c < input[r].length; c++) {
        output[r] = input[r][c];
        String[] sList = combineLists(input, output, r + 1, lang);
        outputList.addAll(Arrays.asList(sList));
      }
    }
    return outputList.toArray(new String[outputList.size()]);
  }

}
