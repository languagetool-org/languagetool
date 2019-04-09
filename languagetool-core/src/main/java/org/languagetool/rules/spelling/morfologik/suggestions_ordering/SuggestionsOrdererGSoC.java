/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Oleg Serikov
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
package org.languagetool.rules.spelling.morfologik.suggestions_ordering;

import biz.k11i.xgboost.Predictor;
import biz.k11i.xgboost.util.FVec;
import org.apache.commons.lang3.tuple.Pair;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.MockLanguageModel;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.ngrams.GoogleTokenUtil;
import org.languagetool.rules.spelling.suggestions.SuggestionsOrderer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Deprecated
public class SuggestionsOrdererGSoC implements SuggestionsOrderer {

  // NOTE: deleted data on 2019-04-09 (kept in Git history), needs to be manually included
  private static final String SPC_NGRAM_BASED_MODEL_FILENAME = "spc_ngram.model";
  private static final String NO_NGRAM_BASED_MODEL_FILENAME = "spc_naive.model";
  private static final String XGBOOST_MODEL_BASE_PATH = "org/languagetool/resource/speller_rule/models/";
  private static final String COMMON_DEFAULT_MODEL_PATH = XGBOOST_MODEL_BASE_PATH + NO_NGRAM_BASED_MODEL_FILENAME;
  private static final Integer DEFAULT_CONTEXT_LENGTH = 2;

  private boolean mlAvailable = true;

  private NGramUtil nGramUtil = null;
  private Predictor predictor;

  @Override
  public boolean isMlAvailable() {
    return mlAvailable && SuggestionsOrdererConfig.isMLSuggestionsOrderingEnabled();
  }

  public SuggestionsOrdererGSoC(Language language, LanguageModel languageModel, String ruleId) {
    try {
      //nGramUtil = new NGramUtil(language);
      nGramUtil = new NGramUtil(language, languageModel);
      String ngramBasedModelFilename = XGBOOST_MODEL_BASE_PATH + ruleId + "/" + SPC_NGRAM_BASED_MODEL_FILENAME;
      String nonNgramModelFilename = XGBOOST_MODEL_BASE_PATH + ruleId + "/" + NO_NGRAM_BASED_MODEL_FILENAME;

      String languageModelFileName;
      if (nGramUtil.isMockLanguageModel()) {
        languageModelFileName = nonNgramModelFilename;
      } else {
        languageModelFileName = ngramBasedModelFilename;
      }
      try (InputStream modelsPath = this.getClass().getClassLoader().getResourceAsStream(languageModelFileName)) {
        predictor = new Predictor(modelsPath);
      } catch (IOException | NullPointerException e) {
        try (InputStream modelsPath = this.getClass().getClassLoader().getResourceAsStream(COMMON_DEFAULT_MODEL_PATH)) {
          predictor = new Predictor(modelsPath);
        } catch (IOException | NullPointerException e1) {
          mlAvailable = false;
        }
      }
    } catch (RuntimeException e) {
      if (e.getMessage().equalsIgnoreCase("NGram file not found")) {
        mlAvailable = false;
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  private float processRow(String sentence, String correctedSentence, String covered, String replacement,
                                  Integer contextLength) {

    Pair<String, String> context = Pair.of("", "");
    int errorStartIdx;

    int sentencesDifferenceCharIdx = ContextUtils.firstDifferencePosition(sentence, correctedSentence);
    if (sentencesDifferenceCharIdx != -1) {
      errorStartIdx = ContextUtils.startOfErrorString(sentence, covered, sentencesDifferenceCharIdx);
      if (errorStartIdx != -1) {
        context = ContextUtils.extractContext(sentence, covered, errorStartIdx, contextLength);
      }
    }

    String leftContextCovered = context.getKey();
    String rightContextCovered = context.getValue();

    String leftContextCorrection = leftContextCovered.isEmpty() ? "" : leftContextCovered.substring(0, leftContextCovered.length() - covered.length()) + replacement;
    String rightContextCorrection = rightContextCovered.isEmpty() ? "" : replacement + rightContextCovered.substring(covered.length());

    boolean firstLetterMatches = ContextUtils.longestCommonPrefix(new String[]{replacement, covered}).length() != 0;

    Integer editDistance = ContextUtils.editDistance(covered, replacement);

    List<String> leftContextCoveredTokenized = nGramUtil.tokenizeString(leftContextCovered.isEmpty() ? covered : leftContextCovered);
    double leftContextCoveredProba = nGramUtil.stringProbability(leftContextCoveredTokenized, 3);
    List<String> rightContextCoveredTokenized = nGramUtil.tokenizeString(rightContextCovered.isEmpty() ? covered : rightContextCovered);
    double rightContextCoveredProba = nGramUtil.stringProbability(rightContextCoveredTokenized, 3);

    List<String> leftContextCorrectionTokenized = nGramUtil.tokenizeString(leftContextCorrection.isEmpty() ? replacement : leftContextCorrection);
    double leftContextCorrectionProba = nGramUtil.stringProbability(leftContextCorrectionTokenized, 3);
    List<String> rightContextCorrectionTokenized = nGramUtil.tokenizeString(rightContextCorrection.isEmpty() ? replacement : rightContextCorrection);
    double rightContextCorrectionProba = nGramUtil.stringProbability(rightContextCorrectionTokenized, 3);

    float left_context_covered_length = leftContextCoveredTokenized.size();
    float left_context_covered_proba = (float) leftContextCoveredProba;
    float right_context_covered_length = rightContextCoveredTokenized.size();
    float right_context_covered_proba = (float) rightContextCoveredProba;
    float left_context_correction_length = leftContextCorrectionTokenized.size();
    float left_context_correction_proba = (float) leftContextCorrectionProba;
    float right_context_correction_length = rightContextCorrectionTokenized.size();
    float right_context_correction_proba = (float) rightContextCorrectionProba;
    float first_letter_matches = firstLetterMatches ? 1f : 0f;
    float edit_distance = editDistance;
    
    float[] data = {left_context_covered_length, left_context_covered_proba,
            right_context_covered_length, right_context_covered_proba,
            left_context_correction_length, left_context_correction_proba,
            right_context_correction_length, right_context_correction_proba,
            first_letter_matches, edit_distance};

    FVec featuresVector = FVec.Transformer.fromArray(data,false);

    double[] predictions = predictor.predict(featuresVector);
    double predictedScore = predictions.length == 0 ? 0 : predictions[0];

    return (float) predictedScore;
  }

  @Override
  public List<SuggestedReplacement> orderSuggestions(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos) {
    if (!isMlAvailable()) {
      return suggestions.stream().map(SuggestedReplacement::new).collect(Collectors.toList());
    }
    List<Pair<String, Float>> suggestionsScores = new LinkedList<>();
    for (String suggestion : suggestions) {
      String text = sentence.getText();
      String correctedSentence = text.substring(0, startPos) + suggestion + sentence.getText().substring(startPos + word.length());

      float score = processRow(text, correctedSentence, word, suggestion, DEFAULT_CONTEXT_LENGTH);
      suggestionsScores.add(Pair.of(suggestion, score));
    }
    Comparator<Pair<String, Float>> comparing = Comparator.comparing(Pair::getValue);
    suggestionsScores.sort(comparing.reversed());

    return suggestionsScores.stream().map(p -> {
      SuggestedReplacement s = new SuggestedReplacement(p.getKey());
      s.setConfidence(p.getRight());
      return s;
    }).collect(Collectors.toList());
  }

  private static class NGramUtil {

    private final Language language;
    private LanguageModel languageModel;
    private boolean mockLanguageModel = false;

    private NGramUtil(Language language, LanguageModel languageModel) {
      this.language = language;
      if (languageModel != null) {
        this.languageModel = languageModel;
      } else {
        this.languageModel = new MockLanguageModel();
        this.mockLanguageModel = true;
      }
    }

    private NGramUtil(Language language) {
      this.language = language;
      try {
        String ngramPath = SuggestionsOrdererConfig.getNgramsPath();
        if (ngramPath != null) {
          this.languageModel = language.getLanguageModel(Paths.get(ngramPath).toFile());
        } else {
          this.languageModel = null; // no ngrams path specified
        }
        if (this.languageModel == null) {
          this.mockLanguageModel = true;
          this.languageModel = new MockLanguageModel(); // mock ngrams for language
        }
      } catch (IOException | RuntimeException e) {
        this.languageModel = new MockLanguageModel(); // mock ngrams for language
      }
    }

    private List<String> tokenizeString(String s) {
      return GoogleTokenUtil.getGoogleTokensForString(s, false, language);
    }

    private Double stringProbability(List<String> sTokenized, int length) {
      if (sTokenized.size() > length) {
        sTokenized = sTokenized.subList(sTokenized.size() - length, sTokenized.size());
      }
      return sTokenized.isEmpty() ? null : languageModel.getPseudoProbability(sTokenized).getProb();
    }

    private boolean isMockLanguageModel() {
      return mockLanguageModel;
    }
  }

  private static class ContextUtils {

    private static String leftContext(String originalSentence, int errorStartIdx, String errorString, int contextLength) {
      String regex = repeat(contextLength, "\\w+\\W+") + Pattern.quote(errorString) + "$";
      String stringToSearch = originalSentence.substring(0, errorStartIdx + errorString.length());
      return findFirstRegexMatch(regex, stringToSearch);
    }

    private static String rightContext(String originalSentence, int errorStartIdx, String errorString, int contextLength) {
      String regex = "^" + Pattern.quote(errorString) + repeat(contextLength, "\\W+\\w+");
      String stringToSearch = originalSentence.substring(errorStartIdx);
      return findFirstRegexMatch(regex, stringToSearch);
    }

    private static int firstDifferencePosition(String sentence1, String sentence2) {
      int result = -1;
      for (int i = 0; i < sentence1.length(); i++) {
        if (i >= sentence2.length() || sentence1.charAt(i) != sentence2.charAt(i)) {
          result = i;
          break;
        }
      }
      return result;
    }

    private static int startOfErrorString(String sentence, String errorString, int sentencesDifferenceCharIdx) {
      int result = -1;
      List<Integer> possibleIntersections = allIndexesOf(sentence.charAt(sentencesDifferenceCharIdx), errorString);
      for (int i : possibleIntersections) {
        if (sentencesDifferenceCharIdx - i < 0 || sentencesDifferenceCharIdx - i + errorString.length() > sentence.length()) {
          continue;
        }
        String possibleErrorString = sentence.substring(sentencesDifferenceCharIdx - i,
                sentencesDifferenceCharIdx - i + errorString.length());
        if (possibleErrorString.equals(errorString)) {
          result = sentencesDifferenceCharIdx - i;
          break;
        }
      }
      return result;
    }

    private static String getMaximalPossibleRightContext(String sentence, int errorStartIdx, String errorString,
                                                        int startingContextLength) {
      String rightContext = "";
      for (int contextLength = startingContextLength; contextLength > 0; contextLength--) {
        rightContext = rightContext(sentence, errorStartIdx, errorString, contextLength);
        if (!rightContext.isEmpty()) {
          break;
        }
      }
      return rightContext;
    }

    private static String getMaximalPossibleLeftContext(String sentence, int errorStartIdx, String errorString,
                                                       int startingContextLength) {
      String leftContext = "";
      for (int contextLength = startingContextLength; contextLength > 0; contextLength--) {
        leftContext = leftContext(sentence, errorStartIdx, errorString, contextLength);
        if (!leftContext.isEmpty()) {
          break;
        }
      }
      return leftContext;
    }

    private static Pair<String, String> extractContext(String sentence, String covered, int errorStartIdx, int contextLength) {
      int errorEndIdx = errorStartIdx + covered.length();
      String errorString = sentence.substring(errorStartIdx, errorEndIdx);

      String leftContext = getMaximalPossibleLeftContext(sentence, errorStartIdx, errorString, contextLength);
      String rightContext = getMaximalPossibleRightContext(sentence, errorStartIdx, errorString, contextLength);

      return Pair.of(leftContext, rightContext);
    }

    private static String longestCommonPrefix(String[] strs) {
      if (strs == null || strs.length == 0) {
        return "";
      }

      if (strs.length == 1) {
        return strs[0];
      }

      int minLen = strs.length + 1;

      for (String str : strs) {
        if (minLen > str.length()) {
          minLen = str.length();
        }
      }

      for (int i = 0; i < minLen; i++) {
        for (int j = 0; j < strs.length - 1; j++) {
          String s1 = strs[j];
          String s2 = strs[j + 1];
          if (s1.charAt(i) != s2.charAt(i)) {
            return s1.substring(0, i);
          }
        }
      }

      return strs[0].substring(0, minLen);
    }

    private static int editDistance(String x, String y) {
      int[][] dp = new int[x.length() + 1][y.length() + 1];

      for (int i = 0; i <= x.length(); i++) {
        for (int j = 0; j <= y.length(); j++) {
          if (i == 0) {
            dp[i][j] = j;
          } else if (j == 0) {
            dp[i][j] = i;
          } else {
            dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1);
          }
        }
      }

      return dp[x.length()][y.length()];
    }


    private static int costOfSubstitution(char a, char b) {
      return a == b ? 0 : 1;
    }

    private static int min(int... numbers) {
      return Arrays.stream(numbers)
              .min().orElse(Integer.MAX_VALUE);
    }

    private static String findFirstRegexMatch(String regex, String stringToSearch) {
      String result = "";
      Pattern pattern = Pattern.compile(regex);
      Matcher stringToSearchMatcher = pattern.matcher(stringToSearch);
      if (stringToSearchMatcher.find()) {
        result = stringToSearch.substring(stringToSearchMatcher.start(), stringToSearchMatcher.end());
      }
      return result;
    }

    private static String repeat(int count, String with) {
      return new String(new char[count]).replace("\0", with);
    }

    private static List<Integer> allIndexesOf(char character, String string) {
      List<Integer> indexes = new ArrayList<>();
      for (int index = string.indexOf(character); index >= 0; index = string.indexOf(character, index + 1)) {
        indexes.add(index);
      }
      return indexes;
    }
  }
}
