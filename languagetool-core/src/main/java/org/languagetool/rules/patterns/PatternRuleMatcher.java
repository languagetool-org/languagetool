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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

/**
 * Matches a pattern rule against plain text.
 */
class PatternRuleMatcher extends AbstractPatternRulePerformer {

  private static final String SUGGESTION_START_TAG = "<suggestion>";
  private static final String SUGGESTION_END_TAG = "</suggestion>";
  
  private final boolean useList;

  PatternRuleMatcher(PatternRule rule, boolean useList) {
    super(rule, rule.getLanguage().getUnifier());
    this.useList = useList;
  }

  final RuleMatch[] match(final AnalyzedSentence text)
    throws IOException {
      final List<ElementMatcher> elementMatchers = createElementMatchers();
      final List<RuleMatch> ruleMatches = new ArrayList<>();
      final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
      final int[] tokenPositions = new int[tokens.length + 1];
      final int patternSize = elementMatchers.size();
      final int limit = Math.max(0, tokens.length - patternSize + 1);
      ElementMatcher elem = null;
      int i = 0;
      while (i < limit && !(rule.sentStart && i > 0)) {
        boolean allElementsMatch = false;
        int firstMatchToken = -1;
        int lastMatchToken = -1;
        int matchingTokens = 0;
        int prevSkipNext = 0;
        // this variable keeps the total number
        // of tokens skipped
        int skipShiftTotal = 0;
        if (rule.testUnification) {
        	unifier.reset();
        }
        for (int k = 0; k < patternSize; k++) {
            final ElementMatcher prevElement = elem;
            elem = elementMatchers.get(k);
            elem.resolveReference(firstMatchToken, tokens, rule.getLanguage());
          final int nextPos = i + k + skipShiftTotal;
          prevMatched = false;
          if (prevSkipNext + nextPos >= tokens.length || prevSkipNext < 0) { // SENT_END?
            prevSkipNext = tokens.length - (nextPos + 1);
          }
          final int maxTok = Math.min(nextPos + prevSkipNext, tokens.length - (patternSize - k));
          for (int m = nextPos; m <= maxTok; m++) {
            allElementsMatch = !tokens[m].isImmunized() && testAllReadings(tokens, elem, prevElement, m,
                    firstMatchToken, prevSkipNext);
            if (allElementsMatch) {
              lastMatchToken = m;
              final int skipShift = lastMatchToken - nextPos;
              tokenPositions[matchingTokens] = skipShift + 1;
              prevSkipNext = translateElementNo(elem.getElement().getSkipNext());
              matchingTokens++;
              skipShiftTotal += skipShift;
              if (firstMatchToken == -1) {
                firstMatchToken = lastMatchToken;
              }
              break;
            }
          }
          if (!allElementsMatch) {
            break;
          }
        }

        if (allElementsMatch && matchingTokens == patternSize) {
          final RuleMatch ruleMatch = createRuleMatch(tokenPositions, tokens,
              firstMatchToken, lastMatchToken, matchingTokens);
          if (ruleMatch != null) {
            ruleMatches.add(ruleMatch);
          }
        }
        i++;
      }
      return ruleMatches.toArray(new RuleMatch[ruleMatches.size()]);
    }

  private RuleMatch createRuleMatch(final int[] tokenPositions,
        final AnalyzedTokenReadings[] tokens, final int firstMatchToken,
        final int lastMatchToken, final int matchingTokens) throws IOException {
      final PatternRule rule = (PatternRule) this.rule;
      final String errMessage = formatMatches(tokens, tokenPositions,
          firstMatchToken, rule.getMessage(), rule.getSuggestionMatches());
      final String suggestionsOutMsg = formatMatches(tokens, tokenPositions,
          firstMatchToken, rule.getSuggestionsOutMsg(), rule.getSuggestionMatchesOutMsg());
      int correctedStPos = 0;
      if (rule.startPositionCorrection > 0) {
        for (int l = 0; l <= rule.startPositionCorrection; l++) {
          correctedStPos += tokenPositions[l];
        }
        correctedStPos--;
      }
      int correctedEndPos = 0;
      if (rule.endPositionCorrection < 0) {
        int l = 0;
        while (l > rule.endPositionCorrection) {
          correctedEndPos -= tokenPositions[matchingTokens + l - 1];
          l--;
        }
      }
      AnalyzedTokenReadings firstMatchTokenObj = tokens[firstMatchToken + correctedStPos];
      boolean startsWithUppercase = StringTools
        .startsWithUppercase(firstMatchTokenObj.getToken())
        && !matchConvertsCase(rule.getSuggestionMatches())
        && !matchConvertsCase(rule.getSuggestionMatchesOutMsg());

      if (firstMatchTokenObj.isSentStart()
          && tokens.length > firstMatchToken + correctedStPos + 1) {
        // make uppercasing work also at sentence start:
        firstMatchTokenObj = tokens[firstMatchToken + correctedStPos + 1];
        startsWithUppercase = StringTools.startsWithUppercase(firstMatchTokenObj.getToken());
      }
      int fromPos = tokens[firstMatchToken + correctedStPos].getStartPos();
      // FIXME: this is fishy, assumes that comma should always come before whitespace:
      if (errMessage.contains(SUGGESTION_START_TAG + ",")
          && firstMatchToken + correctedStPos >= 1) {
        fromPos = tokens[firstMatchToken + correctedStPos - 1].getStartPos()
            + tokens[firstMatchToken + correctedStPos - 1].getToken().length();
      }

      final int toPos = tokens[lastMatchToken + correctedEndPos].getStartPos()
              + tokens[lastMatchToken + correctedEndPos].getToken().length();
      if (fromPos < toPos) { // this can happen with some skip="-1" when the last token is not matched
        //now do some spell-checking:
        if (!(errMessage.contains("<pleasespellme/>") && errMessage.contains("<mistake/>"))) {
          final String clearMsg = errMessage.replaceAll("<pleasespellme/>", "").replaceAll("<mistake/>", "");
          return new RuleMatch(rule, fromPos, toPos, clearMsg,
              rule.getShortMessage(), startsWithUppercase, suggestionsOutMsg);
        }
      } // failed to create any rule match...
      return null;
    }

  /**
   * Checks if the suggestion starts with a match that is supposed to convert
   * case. If it does, stop the default conversion to uppercase.
   *
   * @return true, if the match converts the case of the token.
   */
  private boolean matchConvertsCase(List<Match> suggestionMatches) {
    //final List<Match> suggestionMatches = rule.getSuggestionMatches();
    if (suggestionMatches != null && !suggestionMatches.isEmpty()) {
      final PatternRule rule = (PatternRule) this.rule;
      final int sugStart = rule.getMessage().indexOf(SUGGESTION_START_TAG) + SUGGESTION_START_TAG.length();
      for (Match sMatch : suggestionMatches) {
        if (!sMatch.isInMessageOnly() && sMatch.convertsCase()
                && rule.getMessage().charAt(sugStart) == '\\') {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets the index of the element indexed by i, adding any offsets because of
   * the phrases in the rule.
   *
   * @param i
   *          Current element index.
   * @return int Index translated into XML element no.
   */
  private int translateElementNo(final int i) {
    if (!useList || i < 0) {
      return i;
    }
    int j = 0;
    final PatternRule rule = (PatternRule) this.rule;
    for (int k = 0; k < i; k++) {
      j += rule.getElementNo().get(k);
    }
    return j;
  }

  /**
   * Replace back references generated with &lt;match&gt; and \\1 in message
   * using Match class, and take care of skipping.
   *
   * @param tokenReadings
   *          Array of AnalyzedTokenReadings that were matched against the
   *          pattern
   * @param positions
   *          Array of relative positions of matched tokens
   * @param firstMatchTok
   *          Position of the first matched token
   * @param errorMsg
   *          String containing suggestion markup
   * @return String Formatted message.
   * @throws IOException
   */
  private String formatMatches(final AnalyzedTokenReadings[] tokenReadings,
      final int[] positions, final int firstMatchTok, final String errorMsg,
      final List<Match> suggestionMatches)
  throws IOException {
    String errorMessage = errorMsg;
    int matchCounter = 0;
    final int[] numbersToMatches = new int[errorMsg.length()];
    boolean newWay = false;
    int errLen = errorMessage.length();
    int errMarker = errorMessage.indexOf('\\');
    boolean numberFollows = false;
    if (errMarker >= 0 && errMarker < errLen - 1) {
      numberFollows = StringTools.isPositiveNumber(errorMessage
          .charAt(errMarker + 1));
    }
    while (errMarker >= 0 && numberFollows) {
      final int backslashPos = errorMessage.indexOf('\\');
      if (backslashPos >= 0 && StringTools.isPositiveNumber(errorMessage.charAt(backslashPos + 1))) {
        int numLen = 1;
        while (backslashPos + numLen < errorMessage.length()
            && StringTools.isPositiveNumber(errorMessage.charAt(backslashPos + numLen))) {
          numLen++;
        }
        final int j = Integer.parseInt(errorMessage.substring(backslashPos + 1, backslashPos
            + numLen)) - 1;
        int repTokenPos = 0;
        int nextTokenPos = 0;
        for (int l = 0; l <= j; l++) {
          repTokenPos += positions[l];
        }
        if (j <= positions.length) {
          nextTokenPos = firstMatchTok + repTokenPos + positions[j + 1];
        }
        //final List<Match> suggestionMatches = rule.getSuggestionMatches();
        if (suggestionMatches != null) {
          if (matchCounter < suggestionMatches.size()) {
            numbersToMatches[j] = matchCounter;
            if (suggestionMatches.get(matchCounter) != null) {
              final String[] matches = concatMatches(matchCounter, j,
                  firstMatchTok + repTokenPos, tokenReadings, nextTokenPos, suggestionMatches);
              final String leftSide = errorMessage.substring(0, backslashPos);
              final String rightSide = errorMessage.substring(backslashPos + numLen);
              if (matches.length == 1) {
                errorMessage = leftSide + matches[0] + rightSide;
              } else {
                errorMessage = formatMultipleSynthesis(matches, leftSide,
                    rightSide);
              }
              matchCounter++;
              newWay = true;
            }
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
        numberFollows = StringTools.isPositiveNumber(errorMessage
            .charAt(errMarker + 1));
      }
    }
    return errorMessage;
  }

  private static String formatMultipleSynthesis(final String[] matches,
      final String leftSide, final String rightSide) {
    final String errorMessage;
    String suggestionLeft = "";
    String suggestionRight = "";
    String rightSideNew = rightSide;
    final int sPos = leftSide.lastIndexOf(SUGGESTION_START_TAG);
    if (sPos > 0) {
      suggestionLeft = leftSide.substring(sPos + SUGGESTION_START_TAG.length());
    }
    if (StringTools.isEmpty(suggestionLeft)) {
      errorMessage = leftSide;
    } else {
      errorMessage = leftSide.substring(0, leftSide.lastIndexOf(SUGGESTION_START_TAG))
      + SUGGESTION_START_TAG;
    }
    final int rPos = rightSide.indexOf(SUGGESTION_END_TAG);
    if (rPos > 0) {
      suggestionRight = rightSide.substring(0, rPos);
    }
    if (!StringTools.isEmpty(suggestionRight)) {
      rightSideNew = rightSide.substring(rightSide.indexOf(SUGGESTION_END_TAG));
    }
    final int lastLeftSugEnd = leftSide.indexOf(SUGGESTION_END_TAG);
    final int lastLeftSugStart = leftSide.lastIndexOf(SUGGESTION_START_TAG);
    final StringBuilder sb = new StringBuilder();
    sb.append(errorMessage);
    for (int z = 0; z < matches.length; z++) {
      sb.append(suggestionLeft);
      sb.append(matches[z]);
      sb.append(suggestionRight);
      if ((z < matches.length - 1) && lastLeftSugEnd < lastLeftSugStart) {
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
   *
   * @param start
   *          Position of the element as referenced by match element in the
   *          rule.
   * @param index
   *          The index of the element found in the matching sentence.
   * @param tokenIndex
   *          The position of the token in the AnalyzedTokenReadings array.
   * @param tokens
   *          Array of @AnalyzedTokenReadings
   * @return @String[] Array of concatenated strings
   * @throws IOException
   *           in case disk operations (used in synthesizer) go wrong.
   */
  private String[] concatMatches(final int start, final int index,
      final int tokenIndex, final AnalyzedTokenReadings[] tokens,
      final int nextTokenPos, final List<Match> suggestionMatches)
  throws IOException {
    String[] finalMatch = null;
    //final List<Match> suggestionMatches = rule.getSuggestionMatches();
    if (suggestionMatches.get(start) != null) {
      final int len = phraseLen(index);
      final Language language = rule.language;
      if (len == 1) {
        final int skippedTokens = nextTokenPos - tokenIndex;
        final MatchState matchState = suggestionMatches.get(start).createState(language.getSynthesizer(), tokens, tokenIndex - 1, skippedTokens);
        finalMatch = matchState.toFinalString(language);
        if (suggestionMatches.get(start).checksSpelling()
                && finalMatch.length == 1
                && "".equals(finalMatch[0])) {
            finalMatch = new String[1];
            finalMatch[0] = "<mistake/>";
        }
                
      } else {
        final List<String[]> matchList = new ArrayList<>();
        for (int i = 0; i < len; i++) {
          final int skippedTokens = nextTokenPos - (tokenIndex + i);
          final MatchState matchState = suggestionMatches.get(start).createState(language.getSynthesizer(), tokens, tokenIndex - 1 + i, skippedTokens);
          matchList.add(matchState.toFinalString(language));
        }
        return combineLists(matchList.toArray(new String[matchList.size()][]),
            new String[matchList.size()], 0, language);
      }
    }
    return finalMatch;
  }

  /**
   * Returns true when the token in the rule references a phrase composed of
   * many tokens.
   *
   * @param i
   *          The index of the token.
   * @return true if the phrase is under the index, false otherwise.
   **/
  private int phraseLen(final int i) {
    final PatternRule rule = (PatternRule) this.rule;
    final List<Integer> elementNo = rule.getElementNo();
    if (!useList || i > (elementNo.size() - 1)) {
      return 1;
    }
    return elementNo.get(i);
  }

  /**
   * Creates a Cartesian product of the arrays stored in the input array.
   *
   * @param input
   *          Array of string arrays to combine.
   * @param output
   *          Work array of strings.
   * @param r
   *          Starting parameter (use 0 to get all combinations).
   * @param lang
   *          Text language for adding spaces in some languages.
   * @return Combined array of @String.
   */
  private static String[] combineLists(final String[][] input,
          final String[] output, final int r, final Language lang) {
      final List<String> outputList = new ArrayList<>();
      if (r == input.length) {
          final StringBuilder sb = new StringBuilder();
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
              final String[] sList = combineLists(input, output, r + 1, lang);
              outputList.addAll(Arrays.asList(sList));              
          }
      }
      return outputList.toArray(new String[outputList.size()]);
  }

}
