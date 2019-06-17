/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling.hunspell;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.tokenizers.CompoundWordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

/**
 * A spell checker that combines Hunspell und Morfologik spell checking
 * to support compound words and offer fast suggestions for some misspelled
 * compound words.
 */
public abstract class CompoundAwareHunspellRule extends HunspellRule {

  private static final int MAX_SUGGESTIONS = 20;
  
  private final CompoundWordTokenizer compoundSplitter;
  private final MorfologikMultiSpeller morfoSpeller;

  protected abstract void filterForLanguage(List<String> suggestions);

  public CompoundAwareHunspellRule(ResourceBundle messages, Language language, CompoundWordTokenizer compoundSplitter, MorfologikMultiSpeller morfoSpeller, UserConfig userConfig) {
    this(messages, language, compoundSplitter, morfoSpeller, userConfig, Collections.emptyList());
  }

  /**
   * @since 4.3
   */
  public CompoundAwareHunspellRule(ResourceBundle messages, Language language, CompoundWordTokenizer compoundSplitter, 
                                   MorfologikMultiSpeller morfoSpeller, UserConfig userConfig, List<Language> altLanguages) {
    this(messages, language, compoundSplitter, morfoSpeller, userConfig, altLanguages, null);
  }

  public CompoundAwareHunspellRule(ResourceBundle messages, Language language, CompoundWordTokenizer compoundSplitter,
                                   MorfologikMultiSpeller morfoSpeller, UserConfig userConfig, List<Language> altLanguages,
                                   LanguageModel languageModel) {
    super(messages, language, userConfig, altLanguages, languageModel);
    this.compoundSplitter = compoundSplitter;
    this.morfoSpeller = morfoSpeller;
  }

  /**
   * As a hunspell-based approach is too slow, we use Morfologik to create suggestions. As this
   * won't work for compounds not in the dictionary, we split the word and also get suggestions
   * on the compound parts. In the end, all candidates are filtered against Hunspell again (which
   * supports compounds).
   */
  @Override
  public List<String> getSuggestions(String word) throws IOException {
    if (needsInit) {
      init();
    }
    //System.out.println("Computing suggestions for " + word);
    List<String> candidates = getCandidates(word);
    List<String> simpleSuggestions = getCorrectWords(candidates);
    //System.out.println("simpleSuggestions: " + simpleSuggestions);

    List<String> noSplitSuggestions = morfoSpeller.getSuggestions(word);  // after getCorrectWords() so spelling.txt is considered
    handleWordEndPunctuation(".", word, noSplitSuggestions);
    handleWordEndPunctuation("...", word, noSplitSuggestions);
    List<String> noSplitLowercaseSuggestions = new ArrayList<>();
    if (StringTools.startsWithUppercase(word) && !StringTools.isAllUppercase(word)) {
      // almost all words can be uppercase because they can appear at the start of a sentence:
      noSplitLowercaseSuggestions = morfoSpeller.getSuggestions(word.toLowerCase());
    }
    //System.out.println("noSplitSuggestions: " + noSplitSuggestions);
    //System.out.println("noSplitLcSuggestions: " + noSplitLowercaseSuggestions);
    // We don't know about the quality of the results here, so mix both lists together,
    // taking elements from both lists on a rotating basis:
    List<String> suggestions = new ArrayList<>();
    int max = IntStream.of(simpleSuggestions.size(), noSplitSuggestions.size(), noSplitLowercaseSuggestions.size()).max().orElse(0);
    for (int i = 0; i < max; i++) {
      if (i < noSplitSuggestions.size()) {
        suggestions.add(noSplitSuggestions.get(i));
      }
      if (i < noSplitLowercaseSuggestions.size()) {
        suggestions.add(StringTools.uppercaseFirstChar(noSplitLowercaseSuggestions.get(i)));
      }
      // put these behind suggestions by Morfologik, often low-quality / made-up words
      if (i < simpleSuggestions.size()) {
        suggestions.add(simpleSuggestions.get(i));
      }
    }
    //System.out.println("suggestions (mixed from simpleSuggestions, noSplitSuggestions, noSplitLowerCaseSuggestions): " + suggestions);

    filterDupes(suggestions);
    filterForLanguage(suggestions);

    List<String> sortedSuggestions = sortSuggestionByQuality(word, suggestions);
    //System.out.println("sortSuggestionByQuality(): " + sortedSuggestions);
    // This is probably be the right place to sort suggestions by probability:
    //SuggestionSorter sorter = new SuggestionSorter(new LuceneLanguageModel(new File("/home/dnaber/data/google-ngram-index/de")));
    //sortedSuggestions = sorter.sortSuggestions(sortedSuggestions);
    //System.out.println();
    return sortedSuggestions.subList(0, Math.min(MAX_SUGGESTIONS, sortedSuggestions.size()));
  }

  private void handleWordEndPunctuation(String punct, String word, List<String> noSplitSuggestions) {
    if (word.endsWith(punct)) {
      // e.g. "informationnen." - the dot is a word char in hunspell, so it needs special treatment here
      List<String> tmp = morfoSpeller.getSuggestions(word.substring(0, word.length()-punct.length()));
      for (String s : tmp) {
        noSplitSuggestions.add(s + punct);
      }
    }
  }

  /**
   * Find potential corrections - it's okay if some of these are not valid words,
   * this list will be filtered against the spellchecker before being returned to the user.
   */
  protected List<String> getCandidates(String word) {
    return compoundSplitter.tokenize(word);
  }

  protected List<String> getCandidates(List<String> parts) {
    int partCount = 0;
    List<String> candidates = new ArrayList<>();
    for (String part : parts) {
      if (hunspellDict.misspelled(part)) {
        // assume noun, so use uppercase:
        boolean doUpperCase = partCount > 0 && !StringTools.startsWithUppercase(part);
        List<String> suggestions = morfoSpeller.getSuggestions(doUpperCase ? StringTools.uppercaseFirstChar(part) : part);
        if (suggestions.isEmpty()) {
          suggestions = morfoSpeller.getSuggestions(doUpperCase ? StringTools.lowercaseFirstChar(part) : part);
        }
        boolean appendS = false;
        if (doUpperCase && part.endsWith("s")) {  // maybe infix-s as in "Dampfschiffahrtskapitän" -> "Dampfschifffahrtskapitän"
          suggestions.addAll(morfoSpeller.getSuggestions(StringUtils.removeEnd(part, "s")));
          appendS = true;
        }
        for (String suggestion : suggestions) {
          List<String> partsCopy = new ArrayList<>(parts);
          if (appendS) {
            suggestion += "s";
          }
          if (partCount > 0 && parts.get(partCount).startsWith("-") && parts.get(partCount).length() > 1) {
            partsCopy.set(partCount, "-" + StringTools.uppercaseFirstChar(suggestion.substring(1)));
          } else if (partCount > 0 && !parts.get(partCount-1).endsWith("-")) {
            partsCopy.set(partCount, suggestion.toLowerCase());
          } else {
            partsCopy.set(partCount, suggestion);
          }
          String candidate = String.join("", partsCopy);
          if (!isMisspelled(candidate)) {
            candidates.add(candidate);
          }
          // Arbeidszimmer -> Arbeitszimmer:
          if (partCount < parts.size()-1 && part.endsWith("s") && suggestion.endsWith("-")) {
            partsCopy.set(partCount, suggestion.substring(0, suggestion.length()-1));
            String infixCandidate = String.join("", partsCopy);
            if (!isMisspelled(infixCandidate)) {
              candidates.add(infixCandidate);
            }
          }
        }
      }
      // What if there's no misspelled parts like for Arbeitamt = Arbeit+Amt ??
      // -> morfologik must be extended to return similar words even for known words
      // But GermanSpellerRule.getCandidates() has a solution for the cases with infix "s".
      partCount++;
    }
    return candidates;
  }

  // avoid over-accepting words, as the Morfologik approach above might construct
  // compound words with parts that are correct but the compound is not correct (e.g. "Arbeit + Amt = Arbeitamt"):
  private List<String> getCorrectWords(List<String> wordsOrPhrases) {
    List<String> result = new ArrayList<>();
    for (String wordOrPhrase : wordsOrPhrases) {
      // this might be a phrase like "aufgrund dessen", so it needs to be split: 
      String[] words = tokenizeText(wordOrPhrase);
      boolean wordIsOkay = true;
      for (String word : words) {
        if (hunspellDict.misspelled(word)) {
          wordIsOkay = false;
          break;
        }
      }
      if (wordIsOkay) {
        result.add(wordOrPhrase);
      }
    }
    return result;
  }

}
