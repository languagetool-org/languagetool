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
  
  private static final boolean DEBUG = false;

  private final LanguageModel lm;
  private final Language language;

  private double minProbability = 0.000000000000001;

  public NgramProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    super(messages);
    setCategory(new Category(messages.getString("category_typo")));
    setLocQualityIssueType(ITSIssueType.NonConformance);
    this.lm = Objects.requireNonNull(languageModel);
    this.language = Objects.requireNonNull(language);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Experimental
  public void setMinProbability(double minProbability) {
    this.minProbability = minProbability;
  }

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
          Probability p = lm.getPseudoProbability(Arrays.asList(prevToken.token, token, next.token));
          //System.out.println("P=" + p + " for " + Arrays.asList(prevToken.token, token, next.token));
          String ngram = prevToken + " " + token + " " + next.token;
          // without bigrams:
          double prob = p.getProb();
          // with bigrams:
          //Probability bigramLeftP = getPseudoProbability(Arrays.asList(prevToken.token, token));
          //Probability bigramRightP = getPseudoProbability(Arrays.asList(token, next.token));
          //double prob = p.getProb() + bigramLeftP.getProb() + bigramRightP.getProb();
          //System.out.printf("%.20f for " + prevToken.token + " " + token + " " + next.token + "\n", prob);
          //System.out.printf("%.20f is minProbability\n", minProbability);
          if (prob < minProbability) {
            String message = "The phrase '" + ngram + "' rarely occurs in the reference corpus (" + p.getOccurrences() + " times)";
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
  
  @Override
  public String getDescription() {
    return "Assume errors for phrases (ngrams) that occur rarely in a reference index";
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
