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
public class NgramProbabilityRule extends Rule {

  /** @since 3.2 */
  public static final String RULE_ID = "NGRAM_RULE";
  
  private static final float MIN_OKAY_OCCURRENCES = 1;
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

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
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
