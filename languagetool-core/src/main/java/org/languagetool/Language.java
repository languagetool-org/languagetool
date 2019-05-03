/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.jetbrains.annotations.Nullable;
import org.languagetool.chunking.Chunker;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.language.Contributor;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.patterns.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.SimpleSentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Base class for any supported language (English, German, etc). Language classes
 * are detected at runtime by searching the classpath for files named
 * {@code META-INF/org/languagetool/language-module.properties}. Those file(s)
 * need to contain a key {@code languageClasses} which specifies the fully qualified
 * class name(s), e.g. {@code org.languagetool.language.English}. Use commas to specify 
 * more than one class.
 *
 * <p>Sub classes should typically use lazy init for anything that's costly to set up.
 * This improves start up time for the LanguageTool stand-alone version.
 */
public abstract class Language {

  private static final Disambiguator DEMO_DISAMBIGUATOR = new DemoDisambiguator();
  private static final Tagger DEMO_TAGGER = new DemoTagger();
  private static final SentenceTokenizer SENTENCE_TOKENIZER = new SimpleSentenceTokenizer();
  private static final WordTokenizer WORD_TOKENIZER = new WordTokenizer();

  private final UnifierConfiguration unifierConfig = new UnifierConfiguration();
  private final UnifierConfiguration disambiguationUnifierConfig = new UnifierConfiguration();

  private final Pattern ignoredCharactersRegex = Pattern.compile("[\u00AD]");  // soft hyphen
  
  private List<AbstractPatternRule> patternRules;

  /**
   * Get this language's character code, e.g. <code>en</code> for English.
   * For most languages this is a two-letter code according to ISO 639-1,
   * but for those languages that don't have a two-letter code, a three-letter
   * code according to ISO 639-2 is returned.
   * The country parameter (e.g. "US"), if any, is not returned.
   * @since 3.6
   */
  public abstract String getShortCode();

  /**
   * Get this language's name in English, e.g. <code>English</code> or
   * <code>German (Germany)</code>.
   * @return language name
   */
  public abstract String getName();

  /**
   * Get this language's country options , e.g. <code>US</code> (as in <code>en-US</code>) or
   * <code>PL</code> (as in <code>pl-PL</code>).
   * @return String[] - array of country options for the language.
   */
  public abstract String[] getCountries();

  /**
   * Get the name(s) of the maintainer(s) for this language or <code>null</code>.
   */
  @Nullable
  public abstract Contributor[] getMaintainers();

  /**
   * Get the rules classes that should run for texts in this language.
   * @since 4.3
   */
  public abstract List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException;

  // -------------------------------------------------------------------------

  /**
   * A file with commons words, either in the classpath or as a filename in the file system.
   * @since 4.5
   */
  public String getCommonWordsPath() {
    return getShortCode() + "/common_words.txt";     
  }
  
  /**
   * Get this language's variant, e.g. <code>valencia</code> (as in <code>ca-ES-valencia</code>)
   * or <code>null</code>.
   * Attention: not to be confused with "country" option
   * @return variant for the language or {@code null}
   * @since 2.3
   */
  @Nullable
  public String getVariant() {
    return null;
  }
  
  /**
   * Get enabled rules different from the default ones for this language variant. 
   * 
   * @return enabled rules for the language variant.
   * @since 2.4
   */
  public List<String> getDefaultEnabledRulesForVariant() {
    return Collections.emptyList();
  }

  /**
   * Get disabled rules different from the default ones for this language variant. 
   * 
   * @return disabled rules for the language variant.
   * @since 2.4
   */
  public List<String> getDefaultDisabledRulesForVariant() {
    return Collections.emptyList();
  }

  /**
   * @param indexDir directory with a '3grams' sub directory which contains a Lucene index with 3gram occurrence counts
   * @return a LanguageModel or {@code null} if this language doesn't support one
   * @since 2.7
   */
  @Nullable
  public LanguageModel getLanguageModel(File indexDir) throws IOException {
    return null;
  }

  /**
   * Get a list of rules that require a {@link LanguageModel}. Returns an empty list for
   * languages that don't have such rules.
   * @since 2.7
   */
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    return Collections.emptyList();
  }


  /**
   * Get a list of rules that can optionally use a {@link LanguageModel}. Returns an empty list for
   * languages that don't have such rules.
   * @since 4.5
   * @param languageModel null if no language model is available
   */
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel languageModel,
                                                         UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Collections.emptyList();
  }

  /**
   * @param indexDir directory with a subdirectories like 'en', each containing dictionary.txt and final_embeddings.txt
   * @return a {@link Word2VecModel} or {@code null} if this language doesn't support one
   * @since 4.0
   */
  @Nullable
  public Word2VecModel getWord2VecModel(File indexDir) throws IOException {
    return null;
  }

  /**
   * Get a list of rules that require a {@link Word2VecModel}. Returns an empty list for
   * languages that don't have such rules.
   * @since 4.0
   */
  public List<Rule> getRelevantWord2VecModelRules(ResourceBundle messages, Word2VecModel word2vecModel) throws IOException {
    return Collections.emptyList();
  }

  /**
   * Get a list of rules that load trained neural networks. Returns an empty list for
   * languages that don't have such rules.
   * @since 4.4
   */
  public List<Rule> getRelevantNeuralNetworkModels(ResourceBundle messages, File modelDir) {
    return Collections.emptyList();
  }

  /**
   * Get the rules classes that should run for texts in this language.
   * @since 4.6
   */
  public List<Rule> getRelevantRulesGlobalConfig(ResourceBundle messages, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Collections.emptyList();
  }

  /**
   * Get this language's Java locale, not considering the country code.
   */
  public Locale getLocale() {
    return new Locale(getShortCode());
  }

  /**
   * Get this language's Java locale, considering language code and country code (if any).
   * @since 2.1
   */
  public Locale getLocaleWithCountryAndVariant() {
    if (getCountries().length > 0) {
      if (getVariant() != null) {
        return new Locale(getShortCode(), getCountries()[0], getVariant());
      } else {
        return new Locale(getShortCode(), getCountries()[0]);
      }
    } else {
      return getLocale();
    }
  }

  /**
   * Get the location of the rule file(s) in a form like {@code /org/languagetool/rules/de/grammar.xml},
   * i.e. a path in the classpath. The files must exist or an exception will be thrown, unless the filename
   * contains the string {@code -test-}.
   */
  public List<String> getRuleFileNames() {
    List<String> ruleFiles = new ArrayList<>();
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    ruleFiles.add(dataBroker.getRulesDir()
            + "/" + getShortCode() + "/" + JLanguageTool.PATTERN_FILE);
    if (getShortCodeWithCountryAndVariant().length() > 2) {
      String fileName = getShortCode() + "/"
              + getShortCodeWithCountryAndVariant()
              + "/" + JLanguageTool.PATTERN_FILE;
      if (dataBroker.ruleFileExists(fileName)) {
        ruleFiles.add(dataBroker.getRulesDir() + "/" + fileName);
      }
    }
    return ruleFiles;
  }

  /**
   * Languages that have country variants need to overwrite this to select their most common variant.
   * @return default country variant or {@code null}
   * @since 1.8
   */
  @Nullable
  public Language getDefaultLanguageVariant() {
    return null;
  }

  /**
   * Get this language's part-of-speech disambiguator implementation.
   */
  public Disambiguator getDisambiguator() {
    return DEMO_DISAMBIGUATOR;
  }

  /**
   * Get this language's part-of-speech tagger implementation. The tagger must not 
   * be {@code null}, but it can be a trivial pseudo-tagger that only assigns {@code null} tags.
   */
  public Tagger getTagger() {
    return DEMO_TAGGER;
  }

  /**
   * Get this language's sentence tokenizer implementation.
   */
  public SentenceTokenizer getSentenceTokenizer() {
    return SENTENCE_TOKENIZER;
  }

  /**
   * Get this language's word tokenizer implementation.
   */
  public Tokenizer getWordTokenizer() {
    return WORD_TOKENIZER;
  }

  /**
   * Get this language's chunker implementation or {@code null}.
   * @since 2.3
   */
  @Nullable
  public Chunker getChunker() {
    return null;
  }

  /**
   * Get this language's chunker implementation or {@code null}.
   * @since 2.9
   */
  @Nullable
  public Chunker getPostDisambiguationChunker() {
    return null;
  }

  /**
   * Get this language's part-of-speech synthesizer implementation or {@code null}.
   */
  @Nullable
  public Synthesizer getSynthesizer() {
    return null;
  }

  /**
   * Get this language's feature unifier.
   * @return Feature unifier for analyzed tokens.
   */
  public Unifier getUnifier() {
    return unifierConfig.createUnifier();
  }
  
  /**
   * Get this language's feature unifier used for disambiguation.
   * Note: it might be different from the normal rule unifier.
   * @return Feature unifier for analyzed tokens.
   */
  public Unifier getDisambiguationUnifier() {
    return disambiguationUnifierConfig.createUnifier();
  }

  /**
   * @since 2.3
   */
  public UnifierConfiguration getUnifierConfiguration() {
    return unifierConfig;
  }

  /**
   * @since 2.3
   */
  public UnifierConfiguration getDisambiguationUnifierConfiguration() {
    return disambiguationUnifierConfig;
  }
  
  /**
   * Get the name of the language translated to the current locale,
   * if available. Otherwise, get the untranslated name.
   */
  public final String getTranslatedName(ResourceBundle messages) {
    try {
      return messages.getString(getShortCodeWithCountryAndVariant());
    } catch (MissingResourceException e) {
      try {
        return messages.getString(getShortCode());
      } catch (MissingResourceException e1) {
        return getName();
      }
    }
  }
  
  /**
   * Get the short name of the language with country and variant (if any), if it is
   * a single-country language. For generic language classes, get only a two- or
   * three-character code.
   * @since 3.6
   */
  public final String getShortCodeWithCountryAndVariant() {
    String name = getShortCode();
    if (getCountries().length == 1 && !name.contains("-x-")) {   // e.g. "de-DE-x-simple-language"
      name += "-" + getCountries()[0];
      if (getVariant() != null) {   // e.g. "ca-ES-valencia"
        name += "-" + getVariant();
      }
    }
    return name;
  }
  
  /**
   * Get the pattern rules as defined in the files returned by {@link #getRuleFileNames()}.
   * @since 2.7
   */
  @SuppressWarnings("resource")
  protected synchronized List<AbstractPatternRule> getPatternRules() throws IOException {
    // use lazy loading to speed up server use case and start of stand-alone LT, where all the languages get initialized:
    if (patternRules == null) {
      List<AbstractPatternRule> rules = new ArrayList<>();
      PatternRuleLoader ruleLoader = new PatternRuleLoader();
      for (String fileName : getRuleFileNames()) {
        InputStream is = null;
        try {
          is = this.getClass().getResourceAsStream(fileName);
          boolean ignore = false;
          if (is == null) {                     // files loaded via the dialog
            try {
              is = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
              if (fileName.contains("-test-")) {
                // ignore, used for testing
                ignore = true;
              } else {
                throw e;
              }
            }
          }
          if (!ignore) {
            rules.addAll(ruleLoader.getRules(is, fileName));
            patternRules = Collections.unmodifiableList(rules);
          }
        } finally {
          if (is != null) {
            is.close();
          }
        }
      }
    }
    return patternRules;
  }
  
  @Override
  public final String toString() {
    return getName();
  }

  /**
   * Whether this is a country variant of another language, i.e. whether it doesn't
   * directly extend {@link Language}, but a subclass of {@link Language}.
   * @since 1.8
   */
  public final boolean isVariant() {
    for (Language language : Languages.get()) {
      boolean skip = language.getShortCodeWithCountryAndVariant().equals(getShortCodeWithCountryAndVariant());
      if (!skip && language.getClass().isAssignableFrom(getClass())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether this class has at least one subclass that implements variants of this language.
   * @since 1.8
   */
  public final boolean hasVariant() {
    for (Language language : Languages.get()) {
      boolean skip = language.getShortCodeWithCountryAndVariant().equals(getShortCodeWithCountryAndVariant());
      if (!skip && getClass().isAssignableFrom(language.getClass())) {
        return true;
      }
    }
    return false;
  }

  /**
   * For internal use only. Overwritten to return {@code true} for languages that
   * have been loaded from an external file after start up.
   */
  public boolean isExternal() {
    return false;
  }

  /**
   * Return true if this is the same language as the given one, considering country
   * variants only if set for both languages. For example: en = en, en = en-GB, en-GB = en-GB,
   * but en-US != en-GB
   * @since 1.8
   */
  public boolean equalsConsiderVariantsIfSpecified(Language otherLanguage) {
    if (getShortCode().equals(otherLanguage.getShortCode())) {
      boolean thisHasCountry = hasCountry();
      boolean otherHasCountry = otherLanguage.hasCountry();
      return !(thisHasCountry && otherHasCountry) ||
              getShortCodeWithCountryAndVariant().equals(otherLanguage.getShortCodeWithCountryAndVariant());
    } else {
      return false;
    }
  }

  private boolean hasCountry() {
    return getCountries().length == 1;
  }

  /**
   * @return Return compiled regular expression to ignore inside tokens
   * @since 2.9
   */
  public Pattern getIgnoredCharactersRegex() {
    return ignoredCharactersRegex;
  }

  /**
   * Information about whether the support for this language in LanguageTool is actively maintained.
   * If not, the user interface might show a warning.
   * @since 3.3
   */
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.LookingForNewMaintainer;
  }
  
  /*
   * True if language should be hidden on GUI (i.e. en, de, pt, 
   * instead of en-US, de-DE, pt-PT)
   * @since 3.3
   */
  public boolean isHiddenFromGui() {
    return hasVariant() && !isVariant() && !isTheDefaultVariant();
  }

  private boolean isTheDefaultVariant() {
    if (getDefaultLanguageVariant() != null) {
      return getClass().equals(getDefaultLanguageVariant().getClass());
    }
    return false;
  }
  
  /**
   * Returns a priority for Rule or Category Id (default: 0).
   * Positive integers have higher priority.
   * Negative integers have lower priority.
   * @since 3.6
   */
  public int getPriorityForId(String id) {
    return 0;
  }

  /**
   * Whether this language supports spell checking only and
   * no advanced grammar and style checking.
   * @since 4.5
   */
  public boolean isSpellcheckOnlyLanguage() {
    return false;
  }

  /**
   * Return true if language has ngram-based false friend rule returned by {@link #getRelevantLanguageModelCapableRules}.
   * @since 4.6
   */
  public boolean hasNGramFalseFriendRule(Language motherTongue) {
    return false;
  }

  /**
   * Considers languages as equal if their language code, including the country and variant codes are equal.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Language other = (Language) o;
    return Objects.equals(getShortCodeWithCountryAndVariant(), other.getShortCodeWithCountryAndVariant());
  }

  @Override
  public int hashCode() {
    return getShortCodeWithCountryAndVariant().hashCode();
  }
}
