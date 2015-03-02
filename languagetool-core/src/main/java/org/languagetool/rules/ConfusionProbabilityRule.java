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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.languagemodel.LanguageModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * LanguageTool's homophone confusion check that uses ngram lookups
 * to decide which word in a confusion set suits best.
 * Inspired by After the Deadline.
 * @since 2.7
 */
public abstract class ConfusionProbabilityRule extends Rule {

  /* The minimal amount the alternative's score needs to be better to be used as a replacement.
   * If all alternatives have smaller differences to the text score, no error will be reported: */
  private static final int MIN_SCORE_DIFF = 6;
  /* The minimum score of the alternative (e.g. 'there' if the text is 'their') to be considered at
   * all. Setting this to > 0 avoids very exotic suggestions that are backed only by a small number
   * of occurrences (and thus often wrong): */
  private static final int MIN_ALTERNATIVE_SCORE = 14;
  /* The maximum score of the original text up to which it will be considered a potential error.
   * In other words, if the original text is this or more common, it will not be considered an error,
   * no matter how common the alternatives are. */
  private static final int MAX_TEXT_SCORE = Integer.MAX_VALUE;
  /* The minimum sentences that each homophone must have been tested with to be considered at 
   * all (see homophones-info.txt): */
  private static final int MIN_SENTENCES = 0;
  /* The maximum error rate of each homophone to be considered at all (see homophones-info.txt): */
  private static final float MAX_ERROR_RATE = 10.0f;

  private static final String HOMOPHONES = "homophones.txt";
  private static final String HOMOPHONES_INFO = "homophones-info.txt";
  
  private final Map<String,ConfusionSet> wordToSet;
  private final LanguageModel languageModel;

  @Override
  public abstract String getDescription();

  public abstract String getMessage(String suggestion);

  public ConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) throws IOException {
    super(messages);
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    String prefix = "/" + language.getShortName() + "/";
    InputStream homophonesInfoStream = dataBroker.getFromResourceDirAsStream(prefix + HOMOPHONES_INFO);
    ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader(homophonesInfoStream, MIN_SENTENCES, MAX_ERROR_RATE);
    InputStream homophonesStream = dataBroker.getFromResourceDirAsStream(prefix + HOMOPHONES);
    this.wordToSet = confusionSetLoader.loadConfusionSet(homophonesStream);
    this.languageModel = languageModel;
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
          RuleMatch match = new RuleMatch(this, token.getStartPos(), endPos, getMessage(betterAlternative));
          match.setSuggestedReplacement(betterAlternative);
          matches.add(match);
        }
      }
      pos++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  @Override
  public void reset() {
  }

  /** @deprecated used only for tests */
  public void setConfusionSet(ConfusionSet set) {
    wordToSet.clear();
    for (String word : set.set) {
      wordToSet.put(word, set);
    }
  }

  // non-private for tests
  @Nullable
  String getBetterAlternativeOrNull(AnalyzedTokenReadings[] tokens, int pos, ConfusionSet confusionSet) {
    AnalyzedTokenReadings token = tokens[pos];
    //
    // TODO: LT's tokenization is different to the Google one. E.g. Google "don't" vs LT "don ' t"
    //
    String next = getStringAtOrNull(tokens, pos + 1);
    String next2 = getStringAtOrNull(tokens, pos + 2);
    String prev = getStringAtOrNull(tokens, pos - 1);
    String prev2 = getStringAtOrNull(tokens, pos - 2);
    if ((next + next2 + prev + prev2).contains(",")) {
      // v1 of Google ngram corpus doesn't contain commas, so we better stop instead of getting confused:
      return null;
    }
    @SuppressWarnings("UnnecessaryLocalVariable")
    double textScore = score(token.getToken(), next, next2, prev, prev2);
    if (textScore >= MAX_TEXT_SCORE) {
      // too common, let's assume it is not an error
      return null;
    }
    double bestScore = textScore;
    String betterAlternative = null;
    for (String alternative : confusionSet.set) {
      if (alternative.equalsIgnoreCase(token.getToken())) {
        // this is the text variant, calculated above already...
        continue;
      }
      double alternativeScore = score(alternative, next, next2, prev, prev2);
      if (alternativeScore >= bestScore + MIN_SCORE_DIFF && alternativeScore >= MIN_ALTERNATIVE_SCORE) {
        betterAlternative = alternative;
        bestScore = alternativeScore;
      }
    }
    return betterAlternative;
  }

  @Nullable
  private String getStringAtOrNull(AnalyzedTokenReadings[] tokens, int i) {
    if (i == -1) {
      // Note: we should use LanguageModel.GOOGLE_SENTENCE_START, but this is not in the v1 data from Google:
      return null;
    } else if (i >= tokens.length) {
      // Note: we should use LanguageModel.GOOGLE_SENTENCE_END, but this is not in the v1 data from Google:
      return null;
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
   * This is also the place to add a machine learning algorithm. However, according to 
   * "Web-Scale N-gram Models for Lexical Disambiguation" (Bergsma, Lin, Goebel; 2009) this
   * might improve the results only a little bit.
   * 
   * @param option the word in question
   * @param next1 the next word
   * @param next2 the word after the next word
   * @param prev1 the word before {@code option}
   * @param prev2 the word before {@code prev1}
   */
  private double score(String option, String next1, String next2, String prev1, String prev2) {
    Objects.requireNonNull(option);
    //long ngram2left = languageModel.getCount(prev, option);
    //long ngram2right = languageModel.getCount(option, next);
    // the values may be null, see getStringAtOrNull():
    long ngram3      = (prev1 != null && next1 != null) ? languageModel.getCount(prev1, option, next1) : 0;
    long ngram3left  = (prev2 != null && prev1 != null) ? languageModel.getCount(prev2, prev1, option) : 0;
    long ngram3right = (next1 != null && next2 != null) ? languageModel.getCount(option, next1, next2) : 0;

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
    /*String ngram3String = prev1 + " " + option + " " + next1;
    String ngram3leftString = prev2 + " " + prev1 + " " + option;
    String ngram3rightString = option + " " + next1+ " " + next2;
    System.out.println("=> " + option + ": " + val3 + ", " + val4 + ", " + val5 + " = " + val
            + " (" + ngram3String + "=" + ngram3 + ", " + ngram3leftString + "=" + ngram3left + ", " + ngram3rightString + "=" + ngram3right + ")");
    */

    return val;
  }

  public static class ConfusionSet {
    private final Set<String> set = new HashSet<>();
    ConfusionSet(String... words) {
      Collections.addAll(this.set, words);
    }
    public Set<String> getSet() {
      return set;
    }
    @Override
    public String toString() {
      return set.toString();
    }
  }
}
