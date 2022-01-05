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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.chunking.Chunker;
import org.languagetool.language.Contributor;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.RemoteRuleConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TestRemoteRule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.patterns.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tokenizers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
  private static final Logger logger = LoggerFactory.getLogger(Language.class);

  private static final Disambiguator DEMO_DISAMBIGUATOR = new DemoDisambiguator();
  private static final Tagger DEMO_TAGGER = new DemoTagger();
  private static final SentenceTokenizer SENTENCE_TOKENIZER = new SimpleSentenceTokenizer();
  private static final WordTokenizer WORD_TOKENIZER = new WordTokenizer();
  private static final Pattern INSIDE_SUGGESTION = Pattern.compile("<suggestion>(.+?)</suggestion>");
  private static final Pattern APOSTROPHE = Pattern.compile("([\\p{L}\\d-])'([\\p{L}«])",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private final UnifierConfiguration unifierConfig = new UnifierConfiguration();
  private final UnifierConfiguration disambiguationUnifierConfig = new UnifierConfiguration();

  private final Pattern ignoredCharactersRegex = Pattern.compile("[\u00AD]");  // soft hyphen
  
  private List<AbstractPatternRule> patternRules;
  private final AtomicBoolean noLmWarningPrinted = new AtomicBoolean();

  private Disambiguator disambiguator;
  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Chunker chunker;
  private Chunker postDisambiguationChunker;
  private Synthesizer synthesizer;

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

  protected LanguageModel initLanguageModel(File indexDir, LanguageModel languageModel) {
    if (languageModel == null) {
      File topIndexDir = new File(indexDir, getShortCode());
      if (topIndexDir.exists()) {
        languageModel = new LuceneLanguageModel(topIndexDir);
      } else if (noLmWarningPrinted.compareAndSet(false, true)) {
        System.err.println("WARN: ngram index dir " + topIndexDir + " not found for " + getName());
      }
    }
    return languageModel;
  }

  /**
   * Get a list of rules that require a {@link LanguageModel}. Returns an empty list for
   * languages that don't have such rules.
   * @since 2.7
   */
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Collections.emptyList();
  }


  /**
   * Get a list of rules that can optionally use a {@link LanguageModel}. Returns an empty list for
   * languages that don't have such rules.
   * @since 4.5
   * @param languageModel null if no language model is available
   */
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel languageModel,
                                                         GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Collections.emptyList();
  }


  /**
   * For rules that depend on a remote server; based on {@link org.languagetool.rules.RemoteRule}
   * will be executed asynchronously, with timeout, retries, etc.  as configured
   * Can return non-remote rules (e.g. if configuration missing, or for A/B tests), will be executed normally
   */
  public List<Rule> getRelevantRemoteRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs,
                                           GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages, boolean inputLogging)
    throws IOException {
    return configs.stream()
      .filter(config -> config.getRuleId().startsWith("TEST"))
      .map(c -> new TestRemoteRule(this, c))
      .collect(Collectors.toList());
  }

  /**
   * For rules whose results are extended using some remote service, e.g. {@link org.languagetool.rules.BERTSuggestionRanking}
   * @return function that transforms old rule into remote-enhanced rule
   * @since 4.8
   */
  @Experimental
  public Function<Rule, Rule> getRemoteEnhancedRules(
    ResourceBundle messageBundle, List<RemoteRuleConfig> configs, UserConfig userConfig,
    Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
    return Function.identity();
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
   * Create an instance of the default spelling rule of this language
   * Accessed (with caching) via getDefaultSpellingRule
   * @since 5.5
   */
  private static final Map<Class<? extends Language>, SpellingCheckRule> spellingRules = new ConcurrentHashMap<>();
  @Nullable
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return null;
  }

  /**
   * Retrieve default spelling rule for this language
   * Useful for rules to implement suppression of misspelled suggestions
   * @since 5.5
   */
  @Nullable
  public SpellingCheckRule getDefaultSpellingRule(ResourceBundle messages) {
    return spellingRules.computeIfAbsent(this.getClass(), c -> {
      try {
        return createDefaultSpellingRule(messages);
      } catch (IOException e) {
        logger.warn("Failed to create default spelling rule", e);
        return null;
      }
    }) ;
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
      String premiumFileName = getShortCode() + "/" + getShortCodeWithCountryAndVariant() + "/grammar-premium.xml";
      if (dataBroker.ruleFileExists(premiumFileName)) {
        ruleFiles.add(dataBroker.getRulesDir() + "/" + premiumFileName);
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
   * Creates language specific disambiguator. This function will be called each time in
   * {@link #getDisambiguator()} if disambiguator is not set.
   */
  public Disambiguator createDefaultDisambiguator() {
    return DEMO_DISAMBIGUATOR;
  }

  /**
   * Get this language's part-of-speech disambiguator implementation.
   */
  public synchronized Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = createDefaultDisambiguator();
    }

    return disambiguator;
  }

  /**
   * Set this language's part-of-speech disambiguator implementation.
   */
  public void setDisambiguator(Disambiguator disambiguator) {
    this.disambiguator = disambiguator;
  }

  /**
   * Creates language specific part-of-speech tagger. The tagger must not be {@code null},
   * but it can be a trivial pseudo-tagger that only assigns {@code null} tags.
   * This function will be called each time in {@link #getTagger()} ()} if tagger is not set.
   */
  @NotNull
  public Tagger createDefaultTagger() {
    return DEMO_TAGGER;
  }

  /**
   * Get this language's part-of-speech tagger implementation.
   */
  @NotNull
  public synchronized Tagger getTagger() {
    if (tagger == null) {
      tagger = createDefaultTagger();
    }
    return tagger;
  }

  /**
   * Set this language's part-of-speech tagger implementation.
   */
  public void setTagger(Tagger tagger) {
    this.tagger = tagger;
  }

  /**
   * Creates language specific sentence tokenizer. This function will be called each time in
   * {@link #getSentenceTokenizer()} if sentence tokenizer is not set.
   */
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return SENTENCE_TOKENIZER;
  }

  /**
   * Get this language's sentence tokenizer implementation.
   */
  public synchronized SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = createDefaultSentenceTokenizer();
    }
    return sentenceTokenizer;
  }

  /**
   * Set this language's sentence tokenizer implementation.
   */
  public void setSentenceTokenizer(SentenceTokenizer tokenizer) {
    sentenceTokenizer = tokenizer;
  }

  /**
   * Creates language specific word tokenizer. This function will be called each time in
   * {@link #getWordTokenizer()} if word tokenizer is not set.
   */
  public Tokenizer createDefaultWordTokenizer() {
    return WORD_TOKENIZER;
  }

  /**
   * Get this language's word tokenizer implementation.
   */
  public synchronized Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = createDefaultWordTokenizer();
    }

    return wordTokenizer;
  }

  /**
   * Set this language's word tokenizer implementation.
   */
  public void setWordTokenizer(Tokenizer tokenizer) {
    wordTokenizer = tokenizer;
  }

  /**
   * Creates language specific chunker. This function will be called each time in
   * {@link #getChunker()} if chunker is not set.
   */
  @Nullable
  public Chunker createDefaultChunker() {
    return null;
  }

  /**
   * Get this language's chunker implementation or {@code null}.
   * @since 2.3
   */
  @Nullable
  public synchronized Chunker getChunker() {
    if (chunker == null) {
      chunker = createDefaultChunker();
    }
    return chunker;
  }

  /**
   * Set this language's chunker implementation or {@code null}.
   */
  public void setChunker(Chunker chunker) {
    this.chunker = chunker;
  }

  /**
   * Creates language specific post disambiguation chunker. This function will be called
   * each time in {@link #getPostDisambiguationChunker()} if chunker is not set.
   */
  @Nullable
  public Chunker createDefaultPostDisambiguationChunker() {
    return null;
  }

  /**
   * Get this language's post disambiguation chunker implementation or {@code null}.
   * @since 2.9
   */
  @Nullable
  public synchronized Chunker getPostDisambiguationChunker() {
    if (postDisambiguationChunker == null) {
      postDisambiguationChunker = createDefaultPostDisambiguationChunker();
    }
    return postDisambiguationChunker;
  }

  /**
   * Set this language's post disambiguation chunker implementation or {@code null}.
   */
  public void setPostDisambiguationChunker(Chunker chunker) {
    postDisambiguationChunker = chunker;
  }

  /**
   * Creates language specific part-of-speech synthesizer. This function will be called
   * each time in {@link #getSynthesizer()} if synthesizer is not set.
   */
  @Nullable
  public Synthesizer createDefaultSynthesizer() {
    return null;
  }

  /**
   * Get this language's part-of-speech synthesizer implementation or {@code null}.
   */
  @Nullable
  public synchronized Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = createDefaultSynthesizer();
    }
    return synthesizer;
  }

  /**
   * Set this language's part-of-speech synthesizer implementation or {@code null}.
   */
  public void setSynthesizer(Synthesizer synthesizer) {
    this.synthesizer = synthesizer;
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
          is = JLanguageTool.getDataBroker().getAsStream(fileName);
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
  public boolean isVariant() {
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
  protected int getPriorityForId(String id) {
    if (id.equalsIgnoreCase("TOO_LONG_SENTENCE")) {
      return -101;  // don't hide spelling errors
    }
    if (id.equalsIgnoreCase("STYLE")) {  // category
      return -50;  // don't let style issues hide more important errors
    }
    return 0;
  }
  
  /**
   * Returns a priority for Rule (default: 0).
   * Positive integers have higher priority.
   * Negative integers have lower priority.
   * @since 5.0
   */
  public int getRulePriority(Rule rule) {
    int categoryPriority = this.getPriorityForId(rule.getCategory().getId().toString());
    int rulePriority = this.getPriorityForId(rule.getId());
    // if there is a priority defined for rule it takes precedence over category priority
    if (rulePriority != 0) {
      return rulePriority;
    } else {
      return categoryPriority;
    }
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

  /** @since 5.1 */
  public String getOpeningDoubleQuote() {
    return "\"";
  }

  /** @since 5.1 */
  public String getClosingDoubleQuote() {
    return "\"";
  }
  
  /** @since 5.1 */
  public String getOpeningSingleQuote() {
    return "'";
  }

  /** @since 5.1 */
  public String getClosingSingleQuote() {
    return "'";
  }
  
  /** @since 5.1 */
  public boolean isAdvancedTypographyEnabled() {
    return false;
  }
  
  /** @since 5.1 */
  public String toAdvancedTypography(String input) {
    if (!isAdvancedTypographyEnabled()) {
      return input.replaceAll("<suggestion>", getOpeningDoubleQuote()).replaceAll("</suggestion>", getClosingDoubleQuote());
    }
    String output = input;
   
    //Preserve content inside <suggestion></suggestion>
    List<String> preservedStrings = new ArrayList<>();
    int countPreserved = 0; 
    Matcher m = INSIDE_SUGGESTION.matcher(output);
    int offset = 0;
    while (m.find(offset)) {
      String group = m.group(1);
      preservedStrings.add(group);
      output = output.replaceFirst("<suggestion>" + Pattern.quote(group) + "</suggestion>", "\\\\" + String.valueOf(countPreserved));
      countPreserved++;
      offset = m.end();
    }
    
    // Ellipsis (for all languages?)
    output = output.replaceAll("\\.\\.\\.", "…");
    
    // non-breaking space
    output = output.replaceAll("\\b([a-zA-Z]\\.) ([a-zA-Z]\\.)", "$1\u00a0$2");
    output = output.replaceAll("\\b([a-zA-Z]\\.) ", "$1\u00a0");
    
    Matcher matcher = APOSTROPHE.matcher(output);
    output = matcher.replaceAll("$1’$2");
    
    // single quotes
    if (output.startsWith("'")) { 
      output = output.replaceFirst("'", getOpeningSingleQuote());
    }
    if (output.endsWith("'")) { 
      output = output.substring(0, output.length() - 1 ) + getClosingSingleQuote();
    }
    output = output.replaceAll(" '(.)'", " " + getOpeningSingleQuote()+"$1"+getClosingSingleQuote()); //exception single character
    output = output.replaceAll("([\\u202f\\u00a0 «\"\\(])'", "$1" + getOpeningSingleQuote());
    output = output.replaceAll("'([\u202f\u00a0 !\\?,\\.;:\"\\)])", getClosingSingleQuote() + "$1");
    output = output.replaceAll("‘s\\b([^’])", "’s$1"); // exception genitive
    
    // double quotes
    if (output.startsWith("\"")) { 
      output = output.replaceFirst("\"", getOpeningDoubleQuote());
    }
    if (output.endsWith("\"")) { 
      output = output.substring(0, output.length() - 1 ) + getClosingDoubleQuote();
    }
    output = output.replaceAll("([ \\(])\"", "$1" + getOpeningDoubleQuote());
    output = output.replaceAll("\"([\\u202f\\u00a0 !\\?,\\.;:\\)])", getClosingDoubleQuote() + "$1");   
    
    //restore suggestions
    for (int i = 0; i < preservedStrings.size(); i++) {
      output = output.replaceFirst("\\\\" + i, getOpeningDoubleQuote() + Matcher.quoteReplacement(preservedStrings.get(i)) + getClosingDoubleQuote() );
    }
    
    return output.replaceAll("<suggestion>", getOpeningDoubleQuote()).replaceAll("</suggestion>", getClosingDoubleQuote());
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
  
  /*
   * @since 5.1 
   * Some rules contain the field min_matches to check repeated patterns 
   */
  public boolean hasMinMatchesRules() {
    return false;
  }
  
  /** @since 5.6 */
  public List<RuleMatch> adaptSuggestions(List<RuleMatch> ruleMatches, Set<String> enabledRules) {
	  return ruleMatches;
  }
  
}
