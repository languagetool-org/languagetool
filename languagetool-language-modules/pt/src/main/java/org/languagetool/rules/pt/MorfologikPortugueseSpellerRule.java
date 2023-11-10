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
package org.languagetool.rules.pt;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.languagetool.JLanguageTool.getDataBroker;

public class MorfologikPortugueseSpellerRule extends MorfologikSpellerRule {

  private final String dictFilepath;
  // Path, in pt/resources, where the list of words to be removed from the suggestion list is to be found.
  private static final String doNotSuggestWordsFilepath = "/pt/do_not_suggest.txt";
  // Set of words that we do not want to add to the suggestions, despite being correctly spelt. Mostly profanity.
  private static final Set<String> doNotSuggestWords = getDoNotSuggestWords();
  // Path, in pt/resources, where a list of abbreviations is found. These are simple abbreviations of the shape \w+\.
  private static final String abbreviationFilepath = "/pt/abbreviations.txt";
  private static final Set<String> abbreviations = getAbbreviations();

  @Override
  public String getFileName() {
    return dictFilepath;
  }

  public static Set<String> getDoNotSuggestWords() {
    return getWordSetFromResources(doNotSuggestWordsFilepath);
  }

  public static Set<String> getAbbreviations() {
    return getWordSetFromResources(abbreviationFilepath);
  }

  public static Set<String> getWordSetFromResources(String filepath) {
    return new HashSet<String>(getDataBroker().getFromResourceDirAsLines(filepath));
  }

  @Override
  public String getId() {
    return "HUNSPELL_RULE";
    /*return "MORFOLOGIK_RULE_"
      + language.getShortCodeWithCountryAndVariant().replace("-", "_").toUpperCase();*/
  }

  public MorfologikPortugueseSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
                                      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    // the tagger tags pt-PT and pt-BR words all the same, as it should, but they're still incorrect if they belong
    // to the wrong dialect, commenting this out
    // this.setIgnoreTaggedWords();
    Language spellerLanguage = language;
    if (spellerLanguage.getShortCodeWithCountryAndVariant().equals("pt")) {
      spellerLanguage = spellerLanguage.getDefaultLanguageVariant();
    }
    this.dictFilepath = "/pt/spelling/" + getDictFilename(spellerLanguage) + JLanguageTool.DICTIONARY_FILENAME_EXTENSION;
  }

  @Override
  protected List<SuggestedReplacement> filterNoSuggestWords(List<SuggestedReplacement> suggestedReplacements) {
    return suggestedReplacements.stream().filter(
      suggestedReplacement -> !doNotSuggestWords.contains(
        suggestedReplacement.getReplacement().toLowerCase()
      )).collect(Collectors.toList());
  }

  @Override
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word)
    throws IOException {
    List<String> suggestionsList = suggestions.stream().map(SuggestedReplacement::getReplacement)
      .collect(Collectors.toList());
    return SuggestedReplacement.convert(getAdditionalTopSuggestionsString(suggestionsList, word));
  }

  private List<String> getAdditionalTopSuggestionsString(List<String> suggestions, String word) throws IOException {
    if (isAbbreviation(word)) {
      return Collections.singletonList(word + ".");
    }
    return Collections.emptyList();
  }

  // Check if the word we're checking is in our list of abbreviations.
  protected boolean isAbbreviation(String word) {
    // regular case (since we do have some abbreviations with weird casing) as well as downcased
    return abbreviations.contains(word + ".") || abbreviations.contains(word.toLowerCase() + ".");
  }

  private static String getDictFilename(Language spellerLanguage) {
    String dictFilename = "pt-BR";  // default dict is pt-BR with 1990 spelling
    String fullLanguageCode = spellerLanguage.getShortCodeWithCountryAndVariant();
    switch (fullLanguageCode) {
      case "pt-BR":               dictFilename = "pt-BR";    break;
      case "pt-PT":               dictFilename = "pt-PT-90"; break;
      case "pt-AO": case "pt-MZ": dictFilename = "pt-PT-45"; break;
    }
    return dictFilename;
  }

  @Override
  public List<String> getAdditionalSpellingFileNames() {
    return Arrays.asList(SpellingCheckRule.GLOBAL_SPELLING_FILE, "/pt/multiwords.txt");
  }
}
