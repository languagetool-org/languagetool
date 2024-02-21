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

import org.languagetool.*;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.synthesis.pt.PortugueseSynthesizer;
import org.languagetool.tagging.pt.PortugueseTagger;
import org.languagetool.tools.StringTools;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.languagetool.JLanguageTool.getDataBroker;

public class MorfologikPortugueseSpellerRule extends MorfologikSpellerRule {

  private final Language spellerLanguage;
  private final String dictFilepath;
  // Path, in pt/resources, where the list of words to be removed from the suggestion list is to be found.
  private static final String doNotSuggestWordsFilepath = "/pt/do_not_suggest.txt";
  // Set of words that we do not want to add to the suggestions, despite being correctly spelt. Mostly profanity.
  private static final Set<String> doNotSuggestWords = getDoNotSuggestWords();
  // Path, in pt/resources, where a list of abbreviations is found. These are simple abbreviations of the shape \w+\.
  private static final String abbreviationFilepath = "/pt/abbreviations.txt";
  private static final Set<String> abbreviations = getAbbreviations();
  private static final String dialectAlternationsFilepath = "/pt/dialect_alternations.txt";
  private final Map<String, String> dialectAlternationMapping;
  private static final PortugueseTagger tagger = new PortugueseTagger();
  private static final PortugueseSynthesizer synth = PortugueseSynthesizer.INSTANCE;
  // Moved out of hunspell/spelling.txt for Morfologik.
  private static final String SPELLING_FILE = "pt/spelling.txt";
  private static final String MULTIWORDS_FILE = "pt/multiwords.txt";
  private static final String SPELLING_IGNORE_FILE = "pt/ignore.txt";
  private static final String SPELLING_PROHIBIT_FILE = "pt/prohibit.txt";


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
  public String getIgnoreFileName() {
    return SPELLING_IGNORE_FILE;
  }

  @Override
  public String getProhibitFileName() {
    return SPELLING_PROHIBIT_FILE;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_"
      + language.getShortCodeWithCountryAndVariant().replace("-", "_").toUpperCase();
  }

  protected String getIdForDialectIssue() {
    return getId() + "_DIALECT";
  }

  // TODO: document this, as it's about to get messy
  @Nullable
  protected String dialectAlternative(String word) throws IOException {
    // Naive check: word is identical to lemma present on the TXT list
    String lemmaCheckResult = dialectAlternationMapping.get(word.toLowerCase());
    if (lemmaCheckResult != null) {
      return lemmaCheckResult;
    }
    List<String> wordAsList = Collections.singletonList(word);
    List<AnalyzedTokenReadings> readings = tagger.tag(wordAsList);
    // Annoying check: the word's lemma exists on the TXT list
    for (AnalyzedTokenReadings reading : readings) {
      for (AnalyzedToken token : reading.getReadings()) {
        String lemma = token.getLemma();
        String tag = token.getPOSTag();
        if (tag != null && dialectAlternationMapping.containsKey(lemma)) {
          String candidate = dialectAlternationMapping.get(lemma);
          Predicate<String> tagPredicate = tagStr -> tagStr.contentEquals(tag);
          String[] forms = synth.synthesizeForPosTags(candidate, tagPredicate);
          // the assumption is these words are almost identical, their tagging also ought to be; so even if it returns
          // many POS tags, the synthesiser should always yield the same form for the same set of tags
          if (forms.length > 0) {
            return forms[0];
          }
        }
      }
    }
    return null;
  }

  @Nullable
  private String checkDiaeresis(String word) {
    if (word.indexOf('ü') >= 0) {
      return word.replace('ü', 'u');
    }
    return null;
  }

  @Nullable
  private String checkEuropeanStyle1PLPastTense(String word) {
    if (Objects.equals(spellerLanguage.getShortCodeWithCountryAndVariant(), "pt-BR") && word.endsWith("ámos")) {
      return word.replace('á', 'a');
    }
    return null;
  }

  private boolean isTitlecasedHyphenatedWord(String[] wordParts) {
    for (String part : wordParts) {
      if (StringTools.isMixedCase(part)) {
        return false;
      }
    }
    return true;
  }

  // This is a bit of a hack specifically for the cases when the tagger dictionary has a hyphenated form (which is,
  // therefore, NOT split in tokenisation) and the speller dictionary doesn't have it.
  @Nullable
  private String checkCompoundElements(String[] wordParts) {
    List<String> suggestedParts = new ArrayList<>();
    for (String part : wordParts) {
      if (speller1.isMisspelled(part)) {
        // keep it simple, only first suggestion for now
        suggestedParts.add(speller1.getSuggestions(part).get(0));
      } else {
        suggestedParts.add(part);
      }
    }
    String newSuggestion = String.join("-", suggestedParts);
    if (newSuggestion.equals(String.join("-", wordParts))) {
      return null;
    } else {
      return newSuggestion;
    }
  }

  private Map<String,String> getDialectAlternationMapping() {
    Map<String,String> wordMap = new HashMap<>();
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(dialectAlternationsFilepath);
    String fullLanguageCode = spellerLanguage.getShortCodeWithCountryAndVariant();
    int column = -1;
    switch(fullLanguageCode) {
      case "pt-BR": column = 1; break;  // hash from pt-PT to pt-BR
      case "pt-PT": column = 0; break;  // hash from pt-BR to pt-PT
    }
    if (column == -1) { // not supported for pre-45 dictionaries
      return Collections.emptyMap();
    }
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      String[] parsedLine = line.split("=");
      if (parsedLine.length != 2) {
        throw new RuntimeException("Unexpected format in " + dialectAlternationsFilepath + ": " + line +
          " - expected two parts delimited by '='");
      }
      wordMap.put(parsedLine[column].toLowerCase(), parsedLine[column == 1 ? 0 : 1]);
    }
    return wordMap;
  }

  public MorfologikPortugueseSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
                                      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    // the tagger tags pt-PT and pt-BR words all the same, as it should, but they're still incorrect if they belong
    // to the wrong dialect, commenting this out
    // this.setIgnoreTaggedWords();
    if (language.getShortCodeWithCountryAndVariant().equals("pt")) {
      spellerLanguage = language.getDefaultLanguageVariant();
    } else {
      spellerLanguage = language;
    }
    dictFilepath = "/pt/spelling/" + getDictFilename() + JLanguageTool.DICTIONARY_FILENAME_EXTENSION;
    dialectAlternationMapping = getDialectAlternationMapping();
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

  private String getDictFilename() {
    String dictFilename = "pt-BR";  // default dict is pt-BR with 1990 spelling
    String fullLanguageCode = this.spellerLanguage.getShortCodeWithCountryAndVariant();
    switch (fullLanguageCode) {
      case "pt-BR":               dictFilename = "pt-BR";    break;
      case "pt-PT":               dictFilename = "pt-PT-90"; break;
      case "pt-AO": case "pt-MZ": dictFilename = "pt-PT-45"; break;
    }
    return dictFilename;
  }

  @Override
  public List<String> getAdditionalSpellingFileNames() {
    return Arrays.asList(SpellingCheckRule.GLOBAL_SPELLING_FILE, SPELLING_FILE, MULTIWORDS_FILE);
  }

  private void replaceFormsOfFirstMatch(String message, AnalyzedSentence sentence, List<RuleMatch> ruleMatches,
                                        String suggestion, boolean dialectIssue) {
    // recreating match, might overwrite information by SuggestionsRanker;
    // this has precedence
    RuleMatch oldMatch = ruleMatches.get(0);
    RuleMatch newMatch = new RuleMatch(this, sentence, oldMatch.getFromPos(), oldMatch.getToPos(), message);
    newMatch.setType(oldMatch.getType());
    if (dialectIssue) {
      newMatch.setSpecificRuleId(getIdForDialectIssue());
    }
    SuggestedReplacement sugg = new SuggestedReplacement(suggestion);
    sugg.setShortDescription(language.getName());
    newMatch.setSuggestedReplacementObjects(Collections.singletonList(sugg));
    ruleMatches.set(0, newMatch);
  }

  @Override
  public List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence,
                                        List<RuleMatch> ruleMatchesSoFar, int idx,
                                        AnalyzedTokenReadings[] tokens) throws IOException {
    List<RuleMatch> ruleMatches = super.getRuleMatches(word, startPos, sentence, ruleMatchesSoFar, idx, tokens);
    if (!ruleMatches.isEmpty()) {
      if (word.indexOf('-') > 0) {
        String[] wordParts = word.split("-");
        if (isTitlecasedHyphenatedWord(wordParts)) {
          // if the word is hyphenated and each element is titlecased, downcase it and see if it's still misspelt
          if (!speller1.isMisspelled(word.toLowerCase())) {  // if not misspelt, empty out the rule matches
            ruleMatches = Collections.emptyList();
          }
        }
        // When the word is incorrect but we don't have a suggestion, put one together based on each of its parts
        if (!ruleMatches.isEmpty() && ruleMatches.get(0).getSuggestedReplacements().isEmpty()) {
          @Nullable String newSuggestion = checkCompoundElements(wordParts);
          if (newSuggestion == null) {
            ruleMatches = Collections.emptyList();
          } else {
            replaceFormsOfFirstMatch(ruleMatches.get(0).getMessage(), sentence, ruleMatches, newSuggestion, false);
          }
        }
      }
      String wordWithBrazilianStylePastTense = checkEuropeanStyle1PLPastTense(word);
      if (wordWithBrazilianStylePastTense != null) {
        String message = "No Brasil, o pretérito perfeito da primeira pessoa do plural escreve-se sem acento.";
        replaceFormsOfFirstMatch(message, sentence, ruleMatches, wordWithBrazilianStylePastTense, true);
      }
      String wordWithoutDiaeresis = checkDiaeresis(word);
      if (wordWithoutDiaeresis != null) {
        String message = "No mais recente acordo ortográfico, não se usa mais o trema no português.";
        replaceFormsOfFirstMatch(message, sentence, ruleMatches, wordWithoutDiaeresis, false);
      }
      String dialectAlternative = this.dialectAlternative(word);
      if (dialectAlternative != null) {
        String otherVariant = "europeu";
        if (Objects.equals(spellerLanguage.getShortCodeWithCountryAndVariant(), "pt-PT")) {
          otherVariant = "brasileiro";
        }
        String message = "Possível erro de ortografia: esta é a grafia utilizada no português " + otherVariant + ".";
        String suggestion = StringTools.startsWithUppercase(word) ? StringTools.uppercaseFirstChar(dialectAlternative) : dialectAlternative;
        replaceFormsOfFirstMatch(message, sentence, ruleMatches, suggestion, true);
      }
    }
    return ruleMatches;
  }
}
