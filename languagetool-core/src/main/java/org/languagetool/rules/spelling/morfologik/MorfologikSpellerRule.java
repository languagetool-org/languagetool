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

import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.morfologik.suggestions_ordering.SuggestionsOrderer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MorfologikSpellerRule extends SpellingCheckRule {

  protected MorfologikMultiSpeller speller1;
  protected MorfologikMultiSpeller speller2;
  protected MorfologikMultiSpeller speller3;
  protected Locale conversionLocale;

  private boolean ignoreTaggedWords = false;
  private boolean checkCompound = false;
  private Pattern compoundRegex = Pattern.compile("-");
  private final UserConfig userConfig;
  private static SuggestionsOrderer suggestionsOrderer = null;

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
    this.suggestionsOrderer = new SuggestionsOrderer(language, this.getId());
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
        ruleMatches.addAll(getRuleMatches(word, token.getStartPos(), sentence, ruleMatches));
      } else {
        int index = 0;
        Matcher m = tokenizingPattern().matcher(word);
        while (m.find()) {
          String match = word.subSequence(index, m.start()).toString();
          ruleMatches.addAll(getRuleMatches(match, token.getStartPos() + index, sentence, ruleMatches));
          index = m.end();
        }
        if (index == 0) { // tokenizing char not found
          ruleMatches.addAll(getRuleMatches(word, token.getStartPos(), sentence, ruleMatches));
        } else {
          ruleMatches.addAll(getRuleMatches(word.subSequence(
              index, word.length()).toString(), token.getStartPos() + index, sentence, ruleMatches));
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private void initSpeller(String binaryDict) throws IOException {
    String plainTextDict = null;
    String languageVariantPlainTextDict = null;
    if (JLanguageTool.getDataBroker().resourceExists(getSpellingFileName())) {
      plainTextDict = getSpellingFileName();
    }
    if (getLanguageVariantSpellingFileName() != null && JLanguageTool.getDataBroker().resourceExists(getLanguageVariantSpellingFileName())) {
      languageVariantPlainTextDict = getLanguageVariantSpellingFileName();
    }
    if (plainTextDict != null) {
      speller1 = new MorfologikMultiSpeller(binaryDict, plainTextDict, languageVariantPlainTextDict, userConfig, 1);
      speller2 = new MorfologikMultiSpeller(binaryDict, plainTextDict, languageVariantPlainTextDict, userConfig, 2);
      speller3 = new MorfologikMultiSpeller(binaryDict, plainTextDict, languageVariantPlainTextDict, userConfig, 3);
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

    if (checkCompound && compoundRegex.matcher(word).find()) {
      String[] words = compoundRegex.split(word);
      for (String singleWord: words) {
        if (speller.isMisspelled(singleWord)) {
          return true;
        }
      }
      return false;
    }

    return true;
  }

  protected List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence, List<RuleMatch> ruleMatchesSoFar) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (isMisspelled(speller1, word) || isProhibited(word)) {
      RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, startPos
          + word.length(), messages.getString("spelling"),
          messages.getString("desc_spelling_short"));
      if (userConfig == null || userConfig.getMaxSpellingSuggestions() == 0 || ruleMatchesSoFar.size() <= userConfig.getMaxSpellingSuggestions()) {
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
      } else {
        // limited to save CPU
        ruleMatch.setSuggestedReplacement(messages.getString("too_many_errors"));
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

  private List<String> orderSuggestions(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos, int wordLength) {
    List<String> orderedSuggestions;
    if (suggestionsOrderer.isMlAvailable()) {
      orderedSuggestions = suggestionsOrderer.orderSuggestionsUsingModel(suggestions, word, sentence, startPos, word.length());
    } else {
      orderedSuggestions = orderSuggestions(suggestions, word);
    }
    return orderedSuggestions;
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
      return isSurrogatePairCombination;
    }
    return false;
  }

  /**
   * Ignore surrogate pairs (emojis) 
   * @since 4.3 
   * @see org.languagetool.rules.spelling.SpellingCheckRule#ignoreWord(java.lang.String)
   */
  protected boolean ignoreWord(String word) throws IOException {
    return super.ignoreWord(word) || isSurrogatePairCombination(word);
  }
}
