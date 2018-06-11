/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.spelling.morfologik;

import ml.dmlc.xgboost4j.java.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.ngrams.GoogleTokenUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MorfologikSpellerRule extends SpellingCheckRule {
  private static final Integer DEFAULT_CONTEXT_LENGTH = 2;
  protected MorfologikMultiSpeller speller1;
  protected MorfologikMultiSpeller speller2;
  protected MorfologikMultiSpeller speller3;
  protected Locale conversionLocale;

  private boolean ignoreTaggedWords = false;
  private boolean checkCompound = false;
  private Pattern compoundRegex = Pattern.compile("-");
  private final UserConfig userConfig;

  private static final String XGBOOST_MODEL_BASE_PATH = "org/languagetool/resource/speller_rule/models/";
  private static final String DEFAULT_PATH_TO_NGRAMS = "/home/ec2-user/ngram"; //TODO
  private static NGramUtil nGramUtil;
  private static Booster booster;
  /**
   * Get the filename, e.g., <tt>/resource/pl/spelling.dict</tt>.
   */
  public abstract String getFileName();

  @Override
  public abstract String getId();

  public MorfologikSpellerRule(ResourceBundle messages, Language language) throws IOException {
    this(messages, language, null);
  }

  public MorfologikSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig) throws IOException {
    super(messages, language, userConfig);
    this.userConfig = userConfig;
    super.setCategory(Categories.TYPOS.getCategory(messages));
    this.conversionLocale = conversionLocale != null ? conversionLocale : Locale.getDefault();
    init();
    setLocQualityIssueType(ITSIssueType.Misspelling);
    nGramUtil = new NGramUtil(language);
    try (InputStream models_path = this.getClass().getClassLoader().getResourceAsStream(XGBOOST_MODEL_BASE_PATH + this.getId() + "/spc.model")) {
      booster = XGBoost.loadModel(models_path);
    } catch (XGBoostError xgBoostError) {
      throw new RuntimeException("error when loading xgboost model for " + this.getId());
    }
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_spelling");
  }

  public void setLocale(Locale locale) {
    conversionLocale = locale;
  }

  /**
   * Skip words that are known in the POS tagging dictionary, assuming they
   * cannot be incorrect.
   */
  public void setIgnoreTaggedWords() {
    ignoreTaggedWords = true;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    //lazy init
    if (speller1 == null) {
      String binaryDict = null;
      if (JLanguageTool.getDataBroker().resourceExists(getFileName())) {
        binaryDict = getFileName();
      }
      if (binaryDict != null) {
        initSpeller(binaryDict);
      } else {
        // should not happen, as we only configure this rule (or rather its subclasses)
        // when we have the resources:
        return toRuleMatchArray(ruleMatches);
      }
    }
    int idx = -1;
    for (AnalyzedTokenReadings token : tokens) {
      idx++;
      if (canBeIgnored(tokens, idx, token)) {
        continue;
      }
      // if we use token.getToken() we'll get ignored characters inside and speller will choke
      String word = token.getAnalyzedToken(0).getToken();
      if (tokenizingPattern() == null) {
        ruleMatches.addAll(getRuleMatches(word, token.getStartPos(), sentence));
      } else {
        int index = 0;
        Matcher m = tokenizingPattern().matcher(word);
        while (m.find()) {
          String match = word.subSequence(index, m.start()).toString();
          ruleMatches.addAll(getRuleMatches(match, token.getStartPos() + index, sentence));
          index = m.end();
        }
        if (index == 0) { // tokenizing char not found
          ruleMatches.addAll(getRuleMatches(word, token.getStartPos(), sentence));
        } else {
          ruleMatches.addAll(getRuleMatches(word.subSequence(
              index, word.length()).toString(), token.getStartPos() + index, sentence));
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private void initSpeller(String binaryDict) throws IOException {
    String plainTextDict = null;
    if (JLanguageTool.getDataBroker().resourceExists(getSpellingFileName())) {
      plainTextDict = getSpellingFileName();
    }
    if (plainTextDict != null) {
      speller1 = new MorfologikMultiSpeller(binaryDict, plainTextDict, userConfig, 1);
      speller2 = new MorfologikMultiSpeller(binaryDict, plainTextDict, userConfig, 2);
      speller3 = new MorfologikMultiSpeller(binaryDict, plainTextDict, userConfig, 3);
      setConvertsCase(speller1.convertsCase());
    } else {
      throw new RuntimeException("Could not find ignore spell file in path: " + getSpellingFileName());
    }
  }

  private boolean canBeIgnored(AnalyzedTokenReadings[] tokens, int idx, AnalyzedTokenReadings token) throws IOException {
    return token.isSentenceStart() ||
           token.isImmunized() ||
           token.isIgnoredBySpeller() ||
           isUrl(token.getToken()) ||
           isEMail(token.getToken()) ||
           (ignoreTaggedWords && token.isTagged()) ||
           ignoreToken(tokens, idx);
  }


  /**
   * @return true if the word is misspelled
   * @since 2.4
   */
  protected boolean isMisspelled(MorfologikMultiSpeller speller, String word) {
    if (!speller.isMisspelled(word)) {
      return false;
    }

    if (checkCompound) {
      if (compoundRegex.matcher(word).find()) {
        String[] words = compoundRegex.split(word);
        for (String singleWord: words) {
          if (speller.isMisspelled(singleWord)) {
            return true;
          }
        }
        return false;
      }
    }

    return true;
  }

  protected List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (isMisspelled(speller1, word) || isProhibited(word)) {
      RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, startPos
          + word.length(), messages.getString("spelling"),
          messages.getString("desc_spelling_short"));
      List<String> suggestions = speller1.getSuggestions(word);
      if (suggestions.isEmpty() && word.length() >= 5) {
        // speller1 uses a maximum edit distance of 1, it won't find suggestion for "garentee", "greatful" etc.
        suggestions.addAll(speller2.getSuggestions(word));
        if (suggestions.isEmpty()) {
          suggestions.addAll(speller3.getSuggestions(word));
        }
      }
      suggestions.addAll(0, getAdditionalTopSuggestions(suggestions, word));
      suggestions.addAll(getAdditionalSuggestions(suggestions, word));
      if (!suggestions.isEmpty()) {
        filterSuggestions(suggestions);
        ruleMatch.setSuggestedReplacements(orderSuggestions(suggestions, word, sentence, startPos, word.length()));
      }
      ruleMatches.add(ruleMatch);
    }
    return ruleMatches;
  }

  /**
   * Get the regular expression pattern used to tokenize
   * the words as in the source dictionary. For example,
   * it may contain a hyphen, if the words with hyphens are
   * not included in the dictionary
   * @return A compiled {@link Pattern} that is used to tokenize words or {@code null}.
   */
  @Nullable
  public Pattern tokenizingPattern() {
    return null;
  }

  protected List<String> orderSuggestions(List<String> suggestions, String word) {
    return suggestions;
  }

  protected List<String> orderSuggestions(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos, int wordLength) {


    List<Pair<String, Float>> suggestionsProbs = new LinkedList<>();
    for (int i = 0; i < suggestions.size(); i++) {
      String suggestion = suggestions.get(i);
      String text = sentence.getText();
      String correctedSentence = text.substring(0, startPos) + suggestion + sentence.getText().substring(startPos + wordLength);
      float score = 0;
      try {
        score = processRow(text, correctedSentence, word, suggestion, startPos, DEFAULT_CONTEXT_LENGTH);
      } catch (IOException e) {
        e.printStackTrace();
      }
      suggestionsProbs.add(Pair.of(suggestion, score));

    }
    Comparator<Pair<String, Float>> comparing = Comparator.comparing(Pair::getValue);
    suggestionsProbs.sort(comparing.reversed());
    List<String> result = new LinkedList<>();

    suggestionsProbs.iterator().forEachRemaining((Pair<String, Float> p) -> result.add(p.getKey()));
    return result;
  }


  private static float processRow(String sentence, String correctedSentence, String covered, String replacement,
                                  Integer suggestionPos, Integer contextLength) throws IOException {


    Pair<String, String> context = Pair.of("", "");
    int errorStartIdx;

    int sentencesDifferenceCharIdx = Utils.firstDifferencePosition(sentence, correctedSentence);
    if (sentencesDifferenceCharIdx != -1) {
      errorStartIdx = Utils.startOfErrorString(sentence, covered, sentencesDifferenceCharIdx);
      if (errorStartIdx != -1) {
        context = Utils.extractContext(sentence, covered, errorStartIdx, contextLength);
      }
    }

    String leftContextCovered = context.getKey();
    String rightContextCovered = context.getValue();
//        String covered = covered;
    String correction = replacement;

    String leftContextCorrection = leftContextCovered.isEmpty() ? "" : leftContextCovered.substring(0, leftContextCovered.length() - covered.length()) + correction;
    String rightContextCorrection = rightContextCovered.isEmpty() ? "" : correction + rightContextCovered.substring(covered.length());

    boolean firstLetterMatches = Utils.longestCommonPrefix(new String[]{correction, covered}).length() != 0;

    Integer editDistance = Utils.editDisctance(covered, correction);

    List<String> leftContextCoveredTokenized = nGramUtil.tokenizeString(leftContextCovered.isEmpty() ? covered : leftContextCovered);
    double leftContextCoveredProba = nGramUtil.stringProbability(leftContextCoveredTokenized, 3);
    List<String> rightContextCoveredTokenized = nGramUtil.tokenizeString(rightContextCovered.isEmpty() ? covered : rightContextCovered);
    double rightContextCoveredProba = nGramUtil.stringProbability(rightContextCoveredTokenized, 3);

    List<String> leftContextCorrectionTokenized = nGramUtil.tokenizeString(leftContextCorrection.isEmpty() ? correction : leftContextCorrection);
    double leftContextCorrectionProba = nGramUtil.stringProbability(leftContextCorrectionTokenized, 3);
    List<String> rightContextCorrectionTokenized = nGramUtil.tokenizeString(rightContextCorrection.isEmpty() ? correction : rightContextCorrection);
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
            right_context_correction_length, right_context_correction_proba, first_letter_matches, edit_distance};
    float res = -1;
    try {
      res = booster.predict(new DMatrix(data, 1, data.length))[0][0];
    } catch (XGBoostError xgBoostError) {
      xgBoostError.printStackTrace();
    }

    return res;
  }


  /**
   * @param checkCompound If true and the word is not in the dictionary
   * it will be split (see {@link #setCompoundRegex(String)})
   * and each component will be checked separately
   * @since 2.4
   */
  protected void setCheckCompound(boolean checkCompound) {
    this.checkCompound = checkCompound;
  }

  /**
   * @param compoundRegex see {@link #setCheckCompound(boolean)}
   * @since 2.4
   */
  protected void setCompoundRegex(String compoundRegex) {
    this.compoundRegex = Pattern.compile(compoundRegex);
  }

  /**
   * Checks whether a given String consists only of surrogate pairs.
   * @param word to be checked
   * @since 4.2
   */
  protected boolean isSurrogatePairCombination (String word) {
    if (word.length() > 1 && word.length() % 2 == 0 && word.codePointCount(0, word.length()) != word.length()) {
      // some symbols such as emojis (😂) have a string length that equals 2
      boolean isSurrogatePairCombination = true;
      for (int i = 0; i < word.length() && isSurrogatePairCombination; i += 2) {
        isSurrogatePairCombination &= Character.isSurrogatePair(word.charAt(i), word.charAt(i + 1));
      }
      if (isSurrogatePairCombination) {
        return isSurrogatePairCombination;
      }
    }
    return false;
  }
}

class Utils {

  public static String leftContext(String originalSentence, int errorStartIdx, String errorString, int contextLength) {
    String regex = repeat(contextLength, "\\w+\\W+") + errorString + "$";
    String stringToSearch = originalSentence.substring(0, errorStartIdx + errorString.length());

    return findFirstRegexMatch(regex, stringToSearch);
  }

  public static String rightContext(String originalSentence, int errorStartIdx, String errorString, int contextLength) {
    String regex = "^" + errorString + repeat(contextLength, "\\W+\\w+");
    String stringToSearch = originalSentence.substring(errorStartIdx);

    return findFirstRegexMatch(regex, stringToSearch);
  }

  public static int firstDifferencePosition(String sentence1, String sentence2) {
    int result = -1;

    for (int i = 0; i < sentence1.length(); i++) {
      if (i >= sentence2.length() || sentence1.charAt(i) != sentence2.charAt(i)) {
        result = i;
        break;
      }
    }

    return result;
  }

  public static int startOfErrorString(String sentence, String errorString, int sentencesDifferenceCharIdx) {
    int result = -1;

    List<Integer> possibleIntersections = allIndexesOf(sentence.charAt(sentencesDifferenceCharIdx), errorString);
    for (int i : possibleIntersections) {
      if (sentencesDifferenceCharIdx - i < 0 || sentencesDifferenceCharIdx - i + errorString.length() > sentence.length())
        continue;
      String possibleErrorString = sentence.substring(sentencesDifferenceCharIdx - i,
              sentencesDifferenceCharIdx - i + errorString.length());

      if (possibleErrorString.equals(errorString)) {
        result = sentencesDifferenceCharIdx - i;
        break;
      }
    }

    return result;
  }

  public static String getMaximalPossibleRightContext(String sentence, int errorStartIdx, String errorString,
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

  public static String getMaximalPossibleLeftContext(String sentence, int errorStartIdx, String errorString,
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

  public static Pair<String, String> extractContext(String sentence, String covered, int errorStartIdx, int contextLength) {
    int errorEndIdx = errorStartIdx + covered.length();
    String errorString = sentence.substring(errorStartIdx, errorEndIdx);

    String leftContext = getMaximalPossibleLeftContext(sentence, errorStartIdx, errorString, contextLength);
    String rightContext = getMaximalPossibleRightContext(sentence, errorStartIdx, errorString, contextLength);

    return Pair.of(leftContext, rightContext);
  }


  public static String longestCommonPrefix(String[] strs) {
    if (strs == null || strs.length == 0) {
      return "";
    }

    if (strs.length == 1)
      return strs[0];

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

  public static int editDisctance(String x, String y) {
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

class NGramUtil {

  //    private static final JLanguageTool lt = new JLanguageTool(new AmericanEnglish());
  private static Language language;
  private static LanguageModel languageModel;

  public NGramUtil(Language language) {
    try {
      NGramUtil.language = language;
      System.out.println("in ngram utils: " + System.getProperty("ngram.path"));
      languageModel = language.getLanguageModel(Paths.get(System.getProperty("ngram.path")).toFile());
    } catch (IOException e) {
      throw new RuntimeException("NGram file not found");
    }
  }

  public List<String> tokenizeString(String s) {
    return GoogleTokenUtil.getGoogleTokensForString(s, false, language);
  }

  public Double stringProbability(List<String> sTokenized, int length) {
    if (sTokenized.size() > length) {
      sTokenized = sTokenized.subList(sTokenized.size() - length, sTokenized.size());
    }
    return sTokenized.isEmpty() ? null : languageModel.getPseudoProbability(sTokenized).getProb();
  }
}
