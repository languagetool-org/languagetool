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
import org.languagetool.tools.StringTools;

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
  private final long totalTokenCount;

  private double minProbability = 0.1f;

  public NgramProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    super(messages);
    setCategory(new Category(messages.getString("category_typo")));
    setLocQualityIssueType(ITSIssueType.NonConformance);
    this.lm = Objects.requireNonNull(languageModel);
    this.language = Objects.requireNonNull(language);
    totalTokenCount = lm.getTotalTokenCount();
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Experimental
  public void setMinProbability(double minProbability) {
    this.minProbability = minProbability;
  }

  /*
    Without bigrams:
      0.00000100000000000000: f=0.390, precision=0.260, recall=0.784
      0.00000010000000000000: f=0.391, precision=0.261, recall=0.784
      0.00000001000000000000: f=0.400, precision=0.267, recall=0.794
      0.00000000100000000000: f=0.422, precision=0.286, recall=0.804
      0.00000000010000000000: f=0.420, precision=0.290, recall=0.765
      0.00000000001000000000: f=0.491, precision=0.350, recall=0.824
      0.00000000000100000000: f=0.505, precision=0.377, recall=0.765
      0.00000000000010000000: f=0.554, precision=0.438, recall=0.755
      0.00000000000001000000: f=0.594, precision=0.503, recall=0.725
      0.00000000000000100000: f=0.645, precision=0.602, recall=0.696 *
      0.00000000000000010000: f=0.589, precision=0.611, recall=0.569
      0.00000000000000001000: f=0.536, precision=0.623, recall=0.471

     With bigram occurrences added: TODO
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
          Probability p = getPseudoProbability(Arrays.asList(prevToken.token, token, next.token));
          //System.out.println("P=" + p + " for " + Arrays.asList(prevToken.token, token, next.token));
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
          //TODO:
          //long biGramLeft = lm.getCount(prevToken.token, token);
          //long biGramRight = lm.getCount(token, next.token);
          //allOccurrences += biGramLeft;
          //allOccurrences += biGramRight;
          if (p.getProb() < minProbability) {
            //debug("biGramLeft : " + biGramLeft + " for '" + prevToken.token + " " + token + "'");
            //debug("biGramRight: " + biGramRight + " for '" + token + " " + next.token + "'");
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

  // This is not always guaranteed to be a real probability (0.0 to 1.0)
  Probability getPseudoProbability(List<String> context) {
    int maxCoverage = 0;
    int coverage = 0;
    long firstWordCount = lm.getCount(context.get(0));
    maxCoverage++;
    if (firstWordCount > 0) {
      coverage++;
    }
    // chain rule of probability (https://www.coursera.org/course/nlp, "Introduction to N-grams" and "Estimating N-gram Probabilities"),
    // https://www.ibm.com/developerworks/community/blogs/nlp/entry/the_chain_rule_of_probability?lang=en
    double p = (double) (firstWordCount + 1) / (totalTokenCount + 1);
    debug("    P for %s: %.20f (%d)\n", context.get(0), p, firstWordCount);
    for (int i = 2; i <= context.size(); i++) {
      List<String> subList = context.subList(0, i);
      long phraseCount = lm.getCount(subList);
      double thisP = (double) (phraseCount + 1) / (firstWordCount + 1);
      // Variant:
      //long prevPhraseCount = lm.getCount(subList.subList(0, subList.size()-1));
      //double thisP = (double) (phraseCount + 1) / (prevPhraseCount + 1);
      maxCoverage++;
      debug("    P for " + subList + ": %.20f (%d)\n", thisP, phraseCount);
      //debug("    (%s)\n", subList.subList(0, subList.size()-1));
      if (phraseCount > 0) {
        coverage++;
      }
      p *= thisP;
    }
    debug("  " + StringTools.listToString(context, " ") + " => %.20f\n", p);
    return new Probability(p, (float)coverage/maxCoverage);
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
