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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MorfologikSpellerRule extends SpellingCheckRule {
  protected MorfologikSpeller speller;
  protected Locale conversionLocale;

  private boolean ignoreTaggedWords = false;
  private boolean checkCompound = false;
  private Pattern compoundRegex = Pattern.compile("-");

  /**
   * Get the filename, e.g., <tt>/resource/pl/spelling.dict</tt>.
   */
  public abstract String getFileName();

  public MorfologikSpellerRule(ResourceBundle messages, Language language) throws IOException {
    super(messages, language);
    super.setCategory(new Category(messages.getString("category_typo")));
    this.conversionLocale = conversionLocale != null ? conversionLocale : Locale.getDefault();
    init();
    setLocQualityIssueType(ITSIssueType.Misspelling);
  }

  @Override
  public abstract String getId();

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
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    //lazy init
    if (speller == null) {
      if (JLanguageTool.getDataBroker().resourceExists(getFileName())) {
        speller = new MorfologikSpeller(getFileName(), conversionLocale);
        setConvertsCase(speller.convertsCase());
      } else {
        // should not happen, as we only configure this rule (or rather its subclasses)
        // when we have the resources:
        return toRuleMatchArray(ruleMatches);
      }
    }
    int idx = -1;
    for (AnalyzedTokenReadings token : tokens) {
      idx++;
      if (token.isSentenceStart()) {
        continue;
      }
      if (isUrl(token.getToken())) {
        continue;
      }
      if (ignoreToken(tokens, idx) || token.isImmunized() || token.isIgnoredBySpeller()) {
        continue;
      }
      if (ignoreTaggedWords && token.isTagged()) {
        continue;
      }
      final String word = token.getToken();
      if (tokenizingPattern() == null) {
        ruleMatches.addAll(getRuleMatch(word, token.getStartPos()));
      } else {
        int index = 0;
        final Matcher m = tokenizingPattern().matcher(word);
        while (m.find()) {
          final String match = word.subSequence(index, m.start()).toString();
          ruleMatches.addAll(getRuleMatch(match, token.getStartPos() + index));
          index = m.end();
        }
        if (index == 0) { // tokenizing char not found
          ruleMatches.addAll(getRuleMatch(word, token.getStartPos()));
        } else {
          ruleMatches.addAll(getRuleMatch(word.subSequence(
              index, word.length()).toString(), token.getStartPos() + index));
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }


  /**
   * @return true if the word is misspelled
   * @since 2.4
   */
  protected boolean isMisspelled(MorfologikSpeller speller, String word) {
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

  protected List<RuleMatch> getRuleMatch(final String word, final int startPos) throws IOException {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    if (isMisspelled(speller, word)) {
      final RuleMatch ruleMatch = new RuleMatch(this, startPos, startPos
          + word.length(), messages.getString("spelling"),
          messages.getString("desc_spelling_short"));
      List<String> suggestions = speller.getSuggestions(word);
      suggestions.addAll(getAdditionalSuggestions(suggestions, word));
      if (!suggestions.isEmpty()) {
        ruleMatch.setSuggestedReplacements(orderSuggestions(suggestions,word));
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
   * @return A compiled {@link Pattern} that is used to tokenize words or null.
   */
  public Pattern tokenizingPattern() {
    return null;
  }

  protected List<String> orderSuggestions(List<String> suggestions, String word) {
    return suggestions;
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

}
