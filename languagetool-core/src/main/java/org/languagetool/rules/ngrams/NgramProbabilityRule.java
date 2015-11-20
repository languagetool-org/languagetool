/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ngrams;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Experimental;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Category;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tokenizers.Tokenizer;

import java.util.*;

/**
 * LanguageTool's probability check that uses ngram lookups
 * to decide if an ngram of the input text is so rare in our
 * ngram index that it should be considered an error.
 * Also see <a href="http://wiki.languagetool.org/finding-errors-using-n-gram-data">http://wiki.languagetool.org/finding-errors-using-n-gram-data</a>.
 * @since 3.2
 */
@Experimental
public class NgramProbabilityRule extends Rule {

  /** @since 3.2 */
  public static final String RULE_ID = "NGRAM_RULE";
  
  private static final int MIN_OKAY_OCCURRENCES = 1000;
  private static final boolean DEBUG = false;

  private final LanguageModel lm;
  private final Language language;
  
  private long minOkayOccurrences = MIN_OKAY_OCCURRENCES;

  public NgramProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    this(messages, languageModel, language, 3);
  }
  
  public NgramProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams) {
    super(messages);
    setCategory(new Category(messages.getString("category_typo")));
    setLocQualityIssueType(ITSIssueType.NonConformance);
    this.lm = Objects.requireNonNull(languageModel);
    this.language = Objects.requireNonNull(language);
    if (grams < 1 || grams > 5) {
      throw new IllegalArgumentException("grams must be between 1 and 5: " + grams);
    }
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Experimental
  public void setMinOkayOccurrences(long minOkayOccurrences) {
    this.minOkayOccurrences = minOkayOccurrences;
  }

  /*
    Without bigrams:
                  10: f=0.256, precision=0.548, recall=0.167
                 100: f=0.361, precision=0.619, recall=0.255
                1000: f=0.511, precision=0.590, recall=0.451
               10000: f=0.536, precision=0.474, recall=0.618 *
              100000: f=0.488, precision=0.373, recall=0.706
             1000000: f=0.449, precision=0.313, recall=0.794
            10000000: f=0.441, precision=0.297, recall=0.853
           100000000: f=0.383, precision=0.256, recall=0.765
          1000000000: f=0.390, precision=0.260, recall=0.784
         10000000000: f=0.390, precision=0.260, recall=0.784
        100000000000: f=0.390, precision=0.260, recall=0.784

     With bigram occurrences added:
                  10: f=0.038, precision=1.000, recall=0.020
                 100: f=0.075, precision=1.000, recall=0.039
                1000: f=0.140, precision=0.667, recall=0.078
               10000: f=0.229, precision=0.517, recall=0.147
              100000: f=0.417, precision=0.530, recall=0.343
             1000000: f=0.446, precision=0.376, recall=0.549 *
            10000000: f=0.443, precision=0.313, recall=0.755
           100000000: f=0.404, precision=0.272, recall=0.784
          1000000000: f=0.398, precision=0.266, recall=0.794
         10000000000: f=0.390, precision=0.260, recall=0.784
        100000000000: f=0.390, precision=0.260, recall=0.784

   */
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    String text = sentence.getText();
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens(text, true, getGoogleStyleWordTokenizer());
    List<RuleMatch> matches = new ArrayList<>();
    GoogleToken prevPrevToken = null;
    GoogleToken prevToken = null;
    int i = 0;
    for (GoogleToken googleToken : tokens) {
      String token = googleToken.token;
      if (prevPrevToken != null && prevToken != null) {
        if (i < tokens.size()-1) {
          GoogleToken next = tokens.get(i+1);
          long occurrences = lm.getCount(prevToken.token, token, next.token);
          long leftOccurrences = lm.getCount(prevPrevToken.token, prevToken.token, token);
          long rightOccurrences = 0;
          if (i < tokens.size()-2) {
            GoogleToken nextNext = tokens.get(i+2);
            rightOccurrences = lm.getCount(token, next.token, nextNext.token);
          }
          String ngram = prevToken + " " + token + " " + next.token;
          long allOccurrences = leftOccurrences + occurrences + rightOccurrences;
          if (i < tokens.size()-2) {
            GoogleToken nextNext = tokens.get(i+2);
            debug("lookup: " + ngram + " => " + occurrences + ", " +
                    prevPrevToken.token + " " + prevToken.token + " " + token + " => " + leftOccurrences + ", " +
                    token + " " + next.token + " " + nextNext.token + " => " + rightOccurrences +
                    "\n");
          } else {
            debug("lookup: " + ngram + " => " + occurrences + ", " +
                    prevPrevToken.token + " " + prevToken.token + " " + token + " => " + leftOccurrences +
                    " + 0\n");
          }
          long biGramLeft = lm.getCount(prevToken.token, token);
          long biGramRight = lm.getCount(token, next.token);
          allOccurrences += biGramLeft;
          allOccurrences += biGramRight;
          if (allOccurrences < minOkayOccurrences) {
            debug("biGramLeft : " + biGramLeft + " for '" + prevToken.token + " " + token + "'");
            debug("biGramRight: " + biGramRight + " for '" + token + " " + next.token + "'");
            String message = "ngram '" + ngram + "' rarely occurs in ngram reference corpus (occurrences: " + occurrences + ")";
            RuleMatch match = new RuleMatch(this, prevToken.startPos, next.endPos, message);
            matches.add(match);
          }
        }
      }
      prevPrevToken = prevToken;
      prevToken = googleToken;
      i++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  // 63 out of 120 matches are real errors => 0,52 precision, 0,62 recall
  public RuleMatch[] matchOld2(AnalyzedSentence sentence) {
    String text = sentence.getText();
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens(text, true, getGoogleStyleWordTokenizer());
    List<RuleMatch> matches = new ArrayList<>();
    GoogleToken prevPrevToken = null;
    GoogleToken prevToken = null;
    int i = 0;
    for (GoogleToken googleToken : tokens) {
      String token = googleToken.token;
      if (prevPrevToken != null && prevToken != null) {
        if (i < tokens.size()-1) {
          GoogleToken next = tokens.get(i+1);
          long occurrences = lm.getCount(prevToken.token, token, next.token);
          String ngram = prevToken + " " + token + " " + next.token;
          debug("lookup: " + ngram + " => " + occurrences + "\n");
          if (occurrences < MIN_OKAY_OCCURRENCES) {
            String message = "ngram '" + ngram + "' rarely occurs in ngram reference corpus (occurrences: " + occurrences + ")";
            RuleMatch match = new RuleMatch(this, prevToken.startPos, next.endPos, message);
            matches.add(match);
          }
        }
      }
      prevPrevToken = prevToken;
      prevToken = googleToken;
      i++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  // 59 out of 121 matches are real errors => 0,49 precision, 0,58 recall
  public RuleMatch[] matchOld1(AnalyzedSentence sentence) {
    String text = sentence.getText();
    List<GoogleToken> tokens = GoogleToken.getGoogleTokens(text, true, getGoogleStyleWordTokenizer());
    List<RuleMatch> matches = new ArrayList<>();
    GoogleToken prevPrevToken = null;
    GoogleToken prevToken = null;
    for (GoogleToken googleToken : tokens) {
      String token = googleToken.token;
      if (prevPrevToken != null && prevToken != null) {
        long occurrences = lm.getCount(prevPrevToken.token, prevToken.token, token);
        String ngram = prevPrevToken + " " + prevToken + " " + token;
        debug("lookup: " + ngram + " => " + occurrences + "\n");
        if (occurrences < MIN_OKAY_OCCURRENCES) {
          String message = "ngram '" + ngram + "' rarely occurs in ngram reference corpus (occurrences: " + occurrences + ")";
          RuleMatch match = new RuleMatch(this, prevPrevToken.startPos, googleToken.endPos, message);
          matches.add(match);
        }
      }
      prevPrevToken = prevToken;
      prevToken = googleToken;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  @Override
  public String getDescription() {
    //return Tools.i18n(messages, "statistics_rule_description");
    return "Assume errors for ngrams that occur rarely in the reference index";
  }

  @Override
  public void reset() {
  }

  protected Tokenizer getGoogleStyleWordTokenizer() {
    return language.getWordTokenizer();
  }
  
  private void debug(String message, Object... vars) {
    if (DEBUG) {
      System.out.printf(Locale.ENGLISH, message, vars);
    }
  }
  
}
