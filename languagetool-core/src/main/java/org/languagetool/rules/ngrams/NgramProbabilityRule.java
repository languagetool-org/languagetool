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
  
  private static final float MIN_OKAY_OCCURRENCES = 1000;
  private static final boolean DEBUG = false;

  private final LanguageModel lm;
  private final Language language;

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

  // MIN_OKAY_OCCURRENCES=1:      17 out of 31 matches are real errors => 0,55 precision, 0,17 recall
  // MIN_OKAY_OCCURRENCES=100:    26 out of 42 matches are real errors => 0,62 precision, 0,25 recall
  // MIN_OKAY_OCCURRENCES=1000:   46 out of 78 matches are real errors => 0,59 precision, 0,45 recall
  // MIN_OKAY_OCCURRENCES=10000:  63 out of 133 matches are real errors => 0,47 precision, 0,62 recall
  // MIN_OKAY_OCCURRENCES=100000: 72 out of 193 matches are real errors => 0,37 precision, 0,71 recall
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
          if (allOccurrences < MIN_OKAY_OCCURRENCES) {
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
