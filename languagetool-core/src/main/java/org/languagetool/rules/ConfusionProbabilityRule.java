/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.languagemodel.LanguageModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * LanguageTool's homophone confusion check that uses ngram lookups
 * to decide which word in a confusion set suits best.
 * Inspired by After the Deadline.
 * 
 * @since 2.7
 */
public abstract class ConfusionProbabilityRule extends Rule {

  @Override
  public abstract String getDescription();

  // This might be used to boost the trust in the original text so that alternatives
  // only get selected when they are clearly higher:
  private static final double TEXT_SCORE_ADVANTAGE = 0.0;
  private static final String HOMOPHONES = "homophonedb.txt";
  
  private final Map<String,ConfusionSet> wordToSet;
  private final LanguageModel languageModel;
  
  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    super(messages);
    ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
    InputStream inputStream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(HOMOPHONES);
    this.wordToSet = confusionSetLoader.loadConfusionSet(inputStream);
    this.languageModel = languageModel;
  }

  /** @deprecated used only for tests */
  public void setConfusionSet(ConfusionSet set) {
    wordToSet.clear();
    for (String word : set.set) {
      wordToSet.put(word, set);
    }
  }

  @Override
  public String getId() {
    return "CONFUSION_RULE";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    List<RuleMatch> matches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedTokenReadings token : tokens) {
      ConfusionSet confusionSet = wordToSet.get(token.getToken());
      boolean isEasilyConfused = confusionSet != null;
      if (isEasilyConfused) {
        String betterAlternative = getBetterAlternativeOrNull(tokens, pos, confusionSet);
        if (betterAlternative != null) {
          int endPos = token.getStartPos() + token.getToken().length();
          RuleMatch match = new RuleMatch(this, token.getStartPos(), endPos, "Did you maybe mean '" + betterAlternative + "'?");
          match.setSuggestedReplacement(betterAlternative);
          matches.add(match);
        }
      }
      pos++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  // non-private for tests
  String getBetterAlternativeOrNull(AnalyzedTokenReadings[] tokens, int pos, ConfusionSet confusionSet) {
    AnalyzedTokenReadings token = tokens[pos];
    //
    // TODO: LT's tokenization is different to the Google one. E.g. Google "don't" vs LT "don ' t"
    //
    String next = getStringAtOrNull(tokens, pos + 1);
    String next2 = getStringAtOrNull(tokens, pos + 2);
    String prev = getStringAtOrNull(tokens, pos - 1);
    String prev2 = getStringAtOrNull(tokens, pos - 2);
    @SuppressWarnings("UnnecessaryLocalVariable")
    double textScore = score(token.getToken(), next, next2, prev, prev2) + TEXT_SCORE_ADVANTAGE;
    double bestScore = textScore;
    String betterAlternative = null;
    for (String alternative : confusionSet.set) {
      if (alternative.equalsIgnoreCase(token.getToken())) {
        // this is the text variant, calculated above already...
        continue;
      }
      double alternativeScore = score(alternative, next, next2, prev, prev2);
      if (alternativeScore > bestScore) {
        betterAlternative = alternative;
        bestScore = alternativeScore;
      }
    }
    return betterAlternative;
  }

  private String getStringAtOrNull(AnalyzedTokenReadings[] tokens, int i) {
    if (i == -1) {
      // Note: this is not in the v1 data from Google:
      return LanguageModel.GOOGLE_SENTENCE_START;
    } else if (i >= tokens.length) {
      // Note: this is not in the v1 data from Google:
      return LanguageModel.GOOGLE_SENTENCE_END;
    } else if (i >= 0 && i < tokens.length) {
      return tokens[i].getToken();
    }
    return null;
  }

  /**
   * Using only 3grams is the result of trying different variants with the Pedler corpus. It leads
   * to slightly better results compared to using 2grams and 3grams combined, and it has the advantage
   * of requiring less lookups.
   * 
   * This is the place to add a machine learning algorithm. However, according to 
   * "Web-Scale N-gram Models for Lexical Disambiguation" (Bergsma, Lin, Goebel; 2009) this
   * might improve the results only a little bit.
   * 
   * @param option the word in question
   * @param next1 the next word
   * @param next2 the word after the next word
   */
  private double score(String option, String next1, String next2, String prev1, String prev2) {
    //long ngram2left = languageModel.getCount(prev, option);
    //long ngram2right = languageModel.getCount(option, next);
    // TODO: the v1 of the Google ngram corpus contains no commas, so we should not try to look them up:
    long ngram3 = languageModel.getCount(prev1, option, next1);
    long ngram3left = languageModel.getCount(prev2, prev1, option);
    long ngram3right = languageModel.getCount(option, next1, next2);

    //double val1 = Math.log(Math.max(1, ngram2left));
    //double val2 = Math.log(Math.max(1, ngram2right));
    double val3 = Math.log(Math.max(1, ngram3));
    double val4 = Math.log(Math.max(1, ngram3left));
    double val5 = Math.log(Math.max(1, ngram3right));

    // baseline:
    //double val = 1.0;  // f-measure: 0.3417 (perfect suggestions only)
    
    // 2grams only:
    //double val = val1 * val2;  // f-measure: 0.5115 (perfect suggestions only)
    //double val = val1 + val2;  // f-measure: 0.5168 (perfect suggestions only)

    // 2grams and 3grams:
    //double val = val1 * val2 * val3;  // f-measure: 0.4986 (perfect suggestions only)
    //double val = val1 + val2 + val3;  // f-measure: 0.5203 (perfect suggestions only)
    //double val = val1 + val2 + val3 + val4 + val5;  // f-measure: 0.5212 (perfect suggestions only)

    // 3grams only:
    //double val = Math.max(1, ngram3);  // f-measure: 0.5038 (perfect suggestions only)
    double val = val3 + val4 + val5;  // f-measure: 0.5292 (perfect suggestions only)

    return val;
  }

  @Override
  public void reset() {
  }

  public static class ConfusionSet {
    private final Set<String> set = new HashSet<>();
    ConfusionSet(String... words) {
      Collections.addAll(this.set, words);
    }
    public Set<String> getSet() {
      return set;
    }
  }
}
