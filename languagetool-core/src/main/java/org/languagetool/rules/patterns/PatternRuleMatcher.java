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

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.rules.*;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Matches a pattern rule against text.
 */
final public class PatternRuleMatcher extends AbstractPatternRulePerformer implements RuleMatcher {

  public static final String MISTAKE = "<mistake/>";

  private static final Map<String,Integer> currentlyActiveRules = new ConcurrentHashMap<>();

  private static final String allowedChars = "[^<>()]*?";
  private static final Pattern SUGGESTION_PATTERN_SUPPRESS = Pattern
      .compile(RuleMatch.SUGGESTION_START_TAG + PatternRuleHandler.PLEASE_SPELL_ME
          + allowedChars + "(\\(" + allowedChars + "\\)|" + MISTAKE + ")" + allowedChars  
          + RuleMatch.SUGGESTION_END_TAG);

  private final boolean useList;
  //private final Integer slowMatchThreshold;
  private static final boolean monitorRules = System.getProperty("monitorActiveRules") != null;

  @ApiStatus.Internal
  public PatternRuleMatcher(AbstractTokenBasedRule rule, boolean useList) {
    super(rule, rule.getLanguage().getUnifier());
    this.useList = useList;
    //String slowMatchThresholdStr = System.getProperty("slowMatchThreshold");
    //slowMatchThreshold = slowMatchThresholdStr != null ? Integer.parseInt(slowMatchThresholdStr) : null;
  }

  public static Map<String, Integer> getCurrentRules() {
    return currentlyActiveRules;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
//    long startTime = System.currentTimeMillis();
    List<RuleMatch> ruleMatches = new ArrayList<>();
    String key = monitorRules ? rule.getFullId() + ": " + sentence.getText() : null;
    if (key != null) {
      currentlyActiveRules.compute(key, (k, v) -> v == null ? 1 : v + 1);
    }
    try {
      AnalyzedTokenReadings[] tokens = isInterpretPosTagsPreDisambiguation()
                                       ? sentence.getPreDisambigTokensWithoutWhitespace()
                                       : sentence.getTokensWithoutWhitespace();
      doMatch(sentence, tokens, (tokenPositions, firstMatchToken, lastMatchToken, firstMarkerMatchToken, lastMarkerMatchToken) -> {
        RuleMatch ruleMatch = createRuleMatch(tokenPositions, tokens, firstMatchToken, lastMatchToken, firstMarkerMatchToken, lastMarkerMatchToken, sentence);
        if (ruleMatch != null) {
          ruleMatches.add(ruleMatch);
        }
      });
      RuleMatchFilter maxFilter = new RuleWithMaxFilter();
      List<RuleMatch> filteredMatches = maxFilter.filter(ruleMatches);
      /*if (slowMatchThreshold != null) {
        long runTime = System.currentTimeMillis() - startTime;
        if (runTime > slowMatchThreshold) {
          logger.warn("Slow match for rule " + rule.getFullId() + ": " + runTime + "ms, sentence len: " + sentence.getText().length() + " (threshold: " + slowMatchThreshold + "ms)");
        }
      }*/
      return filteredMatches.toArray(RuleMatch.EMPTY_ARRAY);
    } catch (IOException e) {
      throw new IOException("Error analyzing sentence: '" + sentence + "'", e);
    } catch (Exception e) {
      throw new RuntimeException("Error analyzing sentence: '" + sentence + "' with rule " + rule.getFullId(), e);
    } finally {
      if (key != null) {
        currentlyActiveRules.computeIfPresent(key, (k, v) -> v - 1 > 0 ? v - 1 : null);
      }
    }
  }

  @Override
  protected boolean testAllReadings(AnalyzedTokenReadings[] tokens, PatternTokenMatcher matcher, PatternTokenMatcher prevElement, int tokenNo, int firstMatchToken, int prevSkipNext) throws IOException {
    if (tokens[tokenNo].isImmunized()) return false;

    return super.testAllReadings(tokens, matcher, prevElement, tokenNo, firstMatchToken, prevSkipNext);
  }

  @Nullable
  private RuleMatch createRuleMatch(int[] tokenPositions,
                                    AnalyzedTokenReadings[] tokens, int firstMatchToken,
                                    int lastMatchToken, int firstMarkerMatchToken, int lastMarkerMatchToken,
                                    AnalyzedSentence sentence) throws IOException {
    String errMessage = formatMatches(tokens, tokenPositions,
            firstMatchToken, rule.getMessage(), rule.getSuggestionMatches());
    String shortErrMessage = formatMatches(tokens, tokenPositions,
        firstMatchToken, rule.getShortMessage(), rule.getSuggestionMatches());
    String suggestionsOutMsg = formatMatches(tokens, tokenPositions,
            firstMatchToken, rule.getSuggestionsOutMsg(), rule.getSuggestionMatchesOutMsg());
    int correctedStPos = 0;
    if (rule.startPositionCorrection > 0) {
      for (int l = 0; l <= Math.min(rule.startPositionCorrection, tokenPositions.length - 1); l++) {
        correctedStPos += tokenPositions[l];
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
    if (firstMarkerMatchToken >= 1 && (errMessage.contains(RuleMatch.SUGGESTION_START_TAG + ",")
        || suggestionsOutMsg.contains(RuleMatch.SUGGESTION_START_TAG + ","))) {
      fromPos = tokens[firstMarkerMatchToken - 1].getStartPos()
          + tokens[firstMarkerMatchToken - 1].getToken().length();
    }
    if (lastMarkerMatchToken == -1) {
      lastMarkerMatchToken = lastMatchToken;
    }
    AnalyzedTokenReadings token = tokens[Math.min(lastMarkerMatchToken, tokens.length-1)];
    int toPos = token.getEndPos();
    if (fromPos < toPos) { // this can happen with some skip="-1" when the last token is not matched
      // if the message is "suppress_misspelled" and there are no suggestions,
      // then do not create the rule match
      if (!(errMessage.contains(PatternRuleHandler.PLEASE_SPELL_ME) && !errMessage.contains(RuleMatch.SUGGESTION_START_TAG)
          && !suggestionsOutMsg.contains(RuleMatch.SUGGESTION_START_TAG))) {
        String clearMsg = errMessage.replaceAll(PatternRuleHandler.PLEASE_SPELL_ME, "").replaceAll(MISTAKE, "");
        RuleMatch ruleMatch = new RuleMatch(rule, sentence, fromPos, toPos, tokens[firstMatchToken].getStartPos(), tokens[lastMatchToken].getEndPos(),
                clearMsg, shortErrMessage, startsWithUppercase, suggestionsOutMsg);
        ruleMatch.setType(rule.getType());
        if (rule.getFilter() != null) {
          RuleFilterEvaluator evaluator = new RuleFilterEvaluator(rule.getFilter());
          AnalyzedTokenReadings[] patternTokens = Arrays.copyOfRange(tokens, firstMatchToken, lastMatchToken + 1);
          return evaluator.runFilter(rule.getFilterArguments(), ruleMatch, patternTokens, firstMatchToken, Ints.asList(tokenPositions));
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
      int sugStart = msg.indexOf(RuleMatch.SUGGESTION_START_TAG) + RuleMatch.SUGGESTION_START_TAG.length();
      if (msg.contains(PatternRuleHandler.PLEASE_SPELL_ME)) {
        sugStart += PatternRuleHandler.PLEASE_SPELL_ME.length();
      }
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
  @Override
  int translateElementNo(int i) {
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
      int[] positions, int firstMatchTok, String errorMsg,
      List<Match> suggestionMatches) throws IOException {
    String errorMessage = errorMsg;
    int matchCounter = 0;
    //int prevMatchesLength = 0;
    int[] numbersToMatches = new int[errorMsg.length()];
    boolean newWay = false;
    int errLen = errorMessage.length();
    /*
    track position of already replaced text to avoid recursion
    otherwise, if we insert user matches into text and these contain backslashes
    we will try to interpret these as well -> this can lead to array index violations, etc.
     */
    int errorMessageProcessed = 0;
    int errMarker = errorMessage.indexOf('\\', errorMessageProcessed);
    boolean numberFollows = false;
    if (errMarker >= 0 && errMarker < errLen - 1) {
      numberFollows = StringTools.isPositiveNumber(errorMessage.charAt(errMarker + 1));
    }
    while (errMarker >= 0 && numberFollows) {
      int backslashPos = errorMessage.indexOf('\\', errorMessageProcessed);
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
        for (int l = 0; l <= Math.min(j, positions.length - 1); l++) {
          repTokenPos += positions[l];
        }
        if (j + 1 < positions.length) {
          nextTokenPos = firstMatchTok + repTokenPos + positions[j + 1];
        }

        if (suggestionMatches != null && suggestionMatches.size() > 0) {
          if (matchCounter < suggestionMatches.size()) {
            numbersToMatches[j] = matchCounter;
            // if token is optional remove it from suggestions:
            String[] matches;
            if (j >= positions.length) {
              matches = concatMatches(matchCounter, j, firstMatchTok + repTokenPos, tokenReadings, nextTokenPos, suggestionMatches);
            } else if (positions[j] != 0) {
              matches = concatMatches(matchCounter, j, firstMatchTok + repTokenPos, tokenReadings, nextTokenPos, suggestionMatches);
            } else {
              matches = new String[] { "" };
            }
            String leftSide = errorMessage.substring(0, backslashPos);
            String rightSide = errorMessage.substring(backslashPos + numLen);
            if (matches.length == 1) {
              // if we removed optional token from suggestion then remove leading space from the next word
              if (matches[0].isEmpty()) {
                errorMessage = concatWithoutExtraSpace(leftSide, rightSide);
                errorMessageProcessed = leftSide.length();
              } else {
                errorMessage = leftSide + matches[0] + rightSide;
                errorMessageProcessed = leftSide.length() + matches[0].length();
              }
            } else {
              // TODO compute/return errorMessageProcessed here as well
              errorMessage = formatMultipleSynthesis(matches, leftSide, rightSide);
            }
            //TODO keep the previous matches length and handle it appropriately
            //prevMatchesLength =  matches.length;
            matchCounter++;
            newWay = true;
          } else {
            // FIXME: is this correct? this is how we deal with multiple matches
            suggestionMatches.add(suggestionMatches.get(numbersToMatches[j]));
          }
        }
        if (!newWay) {
          // in case <match> elements weren't used (yet)
          int newErrorMessageProcessed = errorMessage.lastIndexOf("\\" + (j + 1)) +
            tokenReadings[firstMatchTok + repTokenPos - 1].getToken().length();
          errorMessage = errorMessage.substring(0, errorMessageProcessed) +
            errorMessage.substring(errorMessageProcessed).replace("\\" + (j + 1),
              tokenReadings[firstMatchTok + repTokenPos - 1].getToken());
          errorMessageProcessed = newErrorMessageProcessed;
        }
      }
      errMarker = errorMessage.indexOf('\\', errorMessageProcessed);
      numberFollows = false;
      errLen = errorMessage.length();
      if (errMarker >= 0 && errMarker < errLen - 1) {
        numberFollows = StringTools.isPositiveNumber(errorMessage.charAt(errMarker + 1));
      }
    }
    return removeSuppressMisspelled(errorMessage);
  }

  private static String concatWithoutExtraSpace(String leftSide, String rightSide) {
    // can't do \\p{Punct} as it catches \2 placeholder
    if (leftSide.endsWith(" ") && rightSide.matches("[\\s,:;.!?].*")) {
      return leftSide.substring(0, leftSide.length()-1) + rightSide;
    }
    if (leftSide.endsWith("suggestion>") && rightSide.startsWith(" ")) {
      return leftSide + rightSide.substring(1);
    }
    return leftSide + rightSide;
  }

  private static String removeSuppressMisspelled(String s) {
    String result = s;
    // remove suggestions not synthesized: <suggestion><pleasespellme/>...(...)...</suggestion>
    // remove misspelled words: <suggestion><pleasespellme/>...<mistake/>...</suggestion>
    Matcher matcher = SUGGESTION_PATTERN_SUPPRESS.matcher(result);
    result = matcher.replaceAll("");
    // remove the remaining tags <pleasespellme/> in suggestions but not in the message
    result = result.replaceAll(RuleMatch.SUGGESTION_START_TAG + PatternRuleHandler.PLEASE_SPELL_ME, RuleMatch.SUGGESTION_START_TAG);
    return result;
  }

  // non-private for tests
  static String formatMultipleSynthesis(String[] matches,
      String leftSide, String rightSide) {
    String errorMessage;
    String suggestionLeft = "";
    String suggestionRight = "";
    String rightSideNew = rightSide;
    int sPos = leftSide.lastIndexOf(RuleMatch.SUGGESTION_START_TAG);
    if (sPos >= 0) {
      suggestionLeft = leftSide.substring(sPos + RuleMatch.SUGGESTION_START_TAG.length());
    }
    if (StringTools.isEmpty(suggestionLeft)) {
      errorMessage = leftSide;
    } else {
      errorMessage = leftSide.substring(0, leftSide.lastIndexOf(RuleMatch.SUGGESTION_START_TAG)) + RuleMatch.SUGGESTION_START_TAG;
    }
    int rPos = rightSide.indexOf(RuleMatch.SUGGESTION_END_TAG);
    if (rPos >= 0) {
      suggestionRight = rightSide.substring(0, rPos);
    }
    if (!StringTools.isEmpty(suggestionRight)) {
      rightSideNew = rightSide.substring(rightSide.indexOf(RuleMatch.SUGGESTION_END_TAG));
    }
    int lastLeftSugEnd = leftSide.indexOf(RuleMatch.SUGGESTION_END_TAG);
    int lastLeftSugStart = leftSide.lastIndexOf(RuleMatch.SUGGESTION_START_TAG);
    StringBuilder sb = new StringBuilder();
    sb.append(errorMessage);
    for (int z = 0; z < matches.length; z++) {
      sb.append(suggestionLeft);
      sb.append(matches[z]);
      sb.append(suggestionRight);
      if (z < matches.length - 1 && lastLeftSugEnd < lastLeftSugStart) {
        sb.append(RuleMatch.SUGGESTION_END_TAG);
        sb.append(", ");
        sb.append(RuleMatch.SUGGESTION_START_TAG);
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
    return outputList.toArray(new String[0]);
  }

}
