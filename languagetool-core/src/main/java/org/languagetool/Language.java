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

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.chunking.Chunker;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.language.Contributor;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.patterns.Unifier;
import org.languagetool.rules.patterns.UnifierConfiguration;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tools.MultiKeyProperties;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Base class for any supported language (English, German, etc). Language classes
 * are detected at runtime by searching the classpath for files named
 * {@code META-INF/org/languagetool/language-module.properties}. Those file(s)
 * need to contain a key {@code languageClasses} which specifies the fully qualified
 * class name(s), e.g. {@code org.languagetool.language.English}. Use commas to specify 
 * more than one class.
 */
public abstract class Language {

  private static final String PROPERTIES_PATH = "META-INF/org/languagetool/language-module.properties";
  private static final String PROPERTIES_KEY = "languageClasses";
  
  private static List<Language> externalLanguages = new ArrayList<>();

  private final List<String> externalRuleFiles = new ArrayList<>();

  private boolean isExternalLanguage = false;
  private Pattern ignoredCharactersRegex = Pattern.compile("[\u00AD]");  // soft hyphen
  private List<PatternRule> patternRules;
  
  /**
   * All languages supported by LanguageTool. This includes at least a "demo" language
   * for testing.
   */
  public static Language[] LANGUAGES = getLanguages();

  private static Language[] getLanguages() {
    final List<Language> languages = new ArrayList<>();
    final Set<String> languageClassNames = new HashSet<>();
    try {
      final Enumeration<URL> propertyFiles = Language.class.getClassLoader().getResources(PROPERTIES_PATH);
      while (propertyFiles.hasMoreElements()) {
        final URL url = propertyFiles.nextElement();
        try (InputStream inputStream = url.openStream()) {
          // We want to be able to read properties file with duplicate key, as produced by
          // Maven when merging files:
          final MultiKeyProperties props = new MultiKeyProperties(inputStream);
          final List<String> classNamesStr = props.getProperty(PROPERTIES_KEY);
          if (classNamesStr == null) {
            throw new RuntimeException("Key '" + PROPERTIES_KEY + "' not found in " + url);
          }
          for (String classNames : classNamesStr) {
            final String[] classNamesSplit = classNames.split("\\s*,\\s*");
            for (String className : classNamesSplit) {
              if (languageClassNames.contains(className)) {
                // avoid duplicates - this way we are robust against problems with the maven assembly
                // plugin which aggregates files more than once (in case the deployment descriptor
                // contains both <format>zip</format> and <format>dir</format>):
                continue;
              }
              languages.add(createLanguageObjects(url, className));
              languageClassNames.add(className);
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return languages.toArray(new Language[languages.size()]);
  }

  private static Language createLanguageObjects(URL url, String className) {
    try {
      final Class<?> aClass = Class.forName(className);
      final Constructor<?> constructor = aClass.getConstructor();
      return (Language) constructor.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Class '" + className + "' specified in " + url + " could not be found in classpath", e);
    } catch (Exception e) {
      throw new RuntimeException("Object for class '" + className + "' specified in " + url + " could not created", e);
    }
  }

  /**
   * All languages supported by LanguageTool, but without the demo language.
   */
  public static final Language[] REAL_LANGUAGES = getRealLanguages();

  /**
   * Returns all languages supported by LanguageTool but without the demo language.
   * In contrast to Language.REAL_LANGUAGES contains external languages as well.
   * @return All supported languages.
   * @since 2.6
   */
  public static Language[] getRealLanguages() {
    List<Language> result = new ArrayList<>();
    for (Language lang : LANGUAGES) {
      if (!"xx".equals(lang.getShortName())) {  // skip demo language
        result.add(lang);
      }
    }
    return result.toArray(new Language[result.size()]);
  }

  private static final Language[] BUILTIN_LANGUAGES = LANGUAGES;

  private static final Disambiguator DEMO_DISAMBIGUATOR = new DemoDisambiguator();
  private static final Tagger DEMO_TAGGER = new DemoTagger();
  private static final SentenceTokenizer SENTENCE_TOKENIZER = new SimpleSentenceTokenizer();
  private static final WordTokenizer WORD_TOKENIZER = new WordTokenizer();
  
  private final UnifierConfiguration unifierConfiguration = new UnifierConfiguration();
  private final UnifierConfiguration disambiguationUnifierConfiguration = new UnifierConfiguration();

  // -------------------------------------------------------------------------

  /**
   * Get this language's two character code, e.g. <code>en</code> for English.
   * The country parameter (e.g. "US"), if any, is not returned.
   * @return language code
   */
  public abstract String getShortName();

  /**
   * Get this language's name in English, e.g. <code>English</code> or
   * <code>German (Germany)</code>.
   * @return language name
   */
  public abstract String getName();

  /**
   * Set this language's name in English.
   * @since 2.6
   */
  public abstract void setName(final String name);
  
  /**
   * Get this language's country options , e.g. <code>US</code> (as in <code>en-US</code>) or
   * <code>PL</code> (as in <code>pl-PL</code>).
   * @return String[] - array of country options for the language.
   */
  public abstract String[] getCountries();

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
    return new ArrayList<>();
  }

  /**
   * Get disabled rules different from the default ones for this language variant. 
   * 
   * @return disabled rules for the language variant.
   * @since 2.4
   */
  public List<String> getDefaultDisabledRulesForVariant() {
    return new ArrayList<>();
  }
  /**
   * Get the name(s) of the maintainer(s) for this language or <code>null</code>.
   */
  @Nullable
  public abstract Contributor[] getMaintainers();

  /**
   * Get the rules classes that should run for texts in this language.
   * @since 1.4 (signature modified in 2.7)
   */
  public abstract List<Rule> getRelevantRules(ResourceBundle messages) throws IOException;

  // -------------------------------------------------------------------------

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
   * Get this language's Java locale, not considering the country code.
   */
  public Locale getLocale() {
    return new Locale(getShortName());
  }

  /**
   * Get this language's Java locale, considering language code and country code (if any).
   * @since 2.1
   */
  public Locale getLocaleWithCountryAndVariant() {
    if (getCountries().length > 0) {
      if (getVariant() != null) {
        return new Locale(getShortName(), getCountries()[0], getVariant());
      }
      else {
        return new Locale(getShortName(), getCountries()[0]);
      }
    } else {
      return getLocale();
    }
  }

  /**
   * Get the location of the rule file(s) in a form like {@code /org/languagetool/rules/de/grammar.xml}.
   */
  public List<String> getRuleFileNames() {
    final List<String> ruleFiles = new ArrayList<>();
    ruleFiles.addAll(getExternalRuleFiles());
    final ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    ruleFiles.add(dataBroker.getRulesDir()
            + "/" + getShortName() + "/" + JLanguageTool.PATTERN_FILE);
    if (getShortNameWithCountryAndVariant().length() > 2) {
      final String fileName = getShortName() + "/"
              + getShortNameWithCountryAndVariant()
              + "/" + JLanguageTool.PATTERN_FILE;
      if (dataBroker.ruleFileExists(fileName)) {
        ruleFiles.add(dataBroker.getRulesDir() + "/" + fileName);
      }
    }
    return ruleFiles;
  }

  /**
   * @since 2.6
   */
  public List<String> getExternalRuleFiles() {
    return externalRuleFiles;
  }

  /**
   * Adds an external rule file to the language. After running this method,
   * one has to run JLanguageTool.activateDefaultPatternRules() to make sure
   * that all external rules are activated.
   *
   * @param externalRuleFile Absolute file path to rules.
   * @since 2.6
   */
  public void addExternalRuleFile(String externalRuleFile) {
    externalRuleFiles.add(externalRuleFile);
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
    return unifierConfiguration.createUnifier();
  }
  
  /**
   * Get this language's feature unifier used for disambiguation.
   * Note: it might be different from the normal rule unifier.
   * @return Feature unifier for analyzed tokens.
   */
  public Unifier getDisambiguationUnifier() {
    return disambiguationUnifierConfiguration.createUnifier();
  }

  /**
   * @since 2.3
   */
  public UnifierConfiguration getUnifierConfiguration() {
    return unifierConfiguration;
  }

  /**
   * @since 2.3
   */
  public UnifierConfiguration getDisambiguationUnifierConfiguration() {
    return disambiguationUnifierConfiguration;
  }
  
  /**
   * Get the name of the language translated to the current locale,
   * if available. Otherwise, get the untranslated name.
   */
  public final String getTranslatedName(final ResourceBundle messages) {
    try {
      return messages.getString(getShortNameWithCountryAndVariant());
    } catch (final MissingResourceException e) {
      try {
        return messages.getString(getShortName());
      } catch (final MissingResourceException e1) {
        return getName();
      }
    }
  }
  
  /**
   * Get the short name of the language with country and variant (if any), if it is
   * a single-country language. For generic language classes, get only a two- or
   * three-character code.
   * @since 1.8
   */
  public final String getShortNameWithCountryAndVariant() {
    String name = getShortName();
    if (getCountries().length == 1 
            && !name.contains("-x-")) {   // e.g. "de-DE-x-simple-language"
      name += "-" + getCountries()[0];
      if (getVariant() != null) {   // e.g. "ca-ES-valencia"
        name += "-" + getVariant();
      }
    }
    return name;
  }
  
  
  /**
   * Start symbols used by {@link org.languagetool.rules.GenericUnpairedBracketsRule}.
   * Note that the array must be of equal length as {@link #getUnpairedRuleEndSymbols()} and the sequence of
   * starting symbols must match exactly the sequence of ending symbols.
   * @deprecated will be moved to GenericUnpairedBracketsRule (deprecated since 2.8)
   */
  @Deprecated
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "\"", "'" };
  }

  /**
   * End symbols used by {@link org.languagetool.rules.GenericUnpairedBracketsRule}.
   * @deprecated will be moved to GenericUnpairedBracketsRule (deprecated since 2.8)
   * @see #getUnpairedRuleStartSymbols()
   */
  @Deprecated
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "\"", "'" };
  }
  
  // -------------------------------------------------------------------------

  /**
   * Get the pattern rules as defined in the files returned by {@link #getRuleFileNames()}.
   * @since 2.7
   */
  @Experimental
  synchronized List<PatternRule> getPatternRules() throws IOException {
    if (patternRules == null) {
      patternRules = new ArrayList<>();
      PatternRuleLoader ruleLoader = new PatternRuleLoader();
      for (String fileName : getRuleFileNames()) {
        InputStream is = this.getClass().getResourceAsStream(fileName);
        if (is == null) {                     // files loaded via the dialog
          is = new FileInputStream(fileName);
        }
        patternRules.addAll(ruleLoader.getRules(is, fileName));
      }
    }
    return patternRules;
  }
  
  // -------------------------------------------------------------------------
  
  /**
   * Re-inits the built-in languages and adds the specified ones.
   */
  public static void reInit(final List<Language> languages) {
    LANGUAGES = new Language[BUILTIN_LANGUAGES.length + languages.size()];
    int i = BUILTIN_LANGUAGES.length;
    System.arraycopy(BUILTIN_LANGUAGES, 0,
        LANGUAGES, 0, BUILTIN_LANGUAGES.length);
    for (final Language lang : languages) {
      LANGUAGES[i++] = lang;
    }
    externalLanguages = languages;
  }

  /**
   * Return languages that are not built-in but have been added manually.
   */
  public static List<Language> getExternalLanguages() {
    return externalLanguages;
  }
  
  /**
   * Return all languages supported by LanguageTool.
   * @return A list of all languages, including external ones and country variants (e.g. {@code en-US})
   */
  public static List<Language> getAllLanguages() {
    final List<Language> langList = new ArrayList<>();
    Collections.addAll(langList, LANGUAGES);
    langList.addAll(externalLanguages);
    return langList;
  }

  /**
   * Get the Language object for the given language name.
   *
   * @param languageName e.g. <code>English</code> or <code>German</code> (case is significant)
   * @return a Language object or {@code null} if there is no such language
   */
  @Nullable
  public static Language getLanguageForName(final String languageName) {
    for (Language element : Language.LANGUAGES) {
      if (languageName.equals(element.getName())) {
        return element;
      }
    }
    return null;
  }

  /**
   * Get the Language object for the given short language name.
   *
   * @param langCode e.g. <code>en</code> or <code>es-US</code>
   * @return a Language object
   * @throws IllegalArgumentException if the language is not supported or if the language code is invalid
   */
  public static Language getLanguageForShortName(final String langCode) {
    final Language language = getLanguageForShortNameOrNull(langCode);
    if (language == null) {
      final List<String> codes = new ArrayList<>();
      for (Language realLanguage : LANGUAGES) {
        codes.add(realLanguage.getShortNameWithCountryAndVariant());
      }
      Collections.sort(codes);
      throw new IllegalArgumentException("'" + langCode + "' is not a language code known to LanguageTool." +
              " Supported language codes are: " + StringUtils.join(codes, ", ") + ". The list of languages is read from " + PROPERTIES_PATH +
              " in the Java classpath. See http://wiki.languagetool.org/java-api for details.");
    }
    return language;
  }

  /**
   * Return whether a language with the given language code is supported. Which languages
   * are supported depends on the classpath when the {@code Language} object is initialized.
   *
   * @param langCode e.g. {@code en} or {@code en-US}
   * @return true if the language is supported
   * @throws IllegalArgumentException in some cases of an invalid language code format
   * @since 2.1
   */
  public static boolean isLanguageSupported(final String langCode) {
    return getLanguageForShortNameOrNull(langCode) != null;
  }

  @Nullable
  private static Language getLanguageForShortNameOrNull(final String langCode) {
    StringTools.assureSet(langCode, "langCode");
    Language result = null;
    if (langCode.contains("-x-")) {
      // e.g. "de-DE-x-simple-language"
      for (Language element : Language.LANGUAGES) {
        if (element.getShortName().equalsIgnoreCase(langCode)) {
          return element;
        }
      }
    } else if (langCode.contains("-")) {
      final String[] parts = langCode.split("-"); 
      if (parts.length == 2) { // e.g. en-US
        for (Language element : Language.LANGUAGES) {
          if (parts[0].equalsIgnoreCase(element.getShortName())
              && element.getCountries().length == 1
              && parts[1].equalsIgnoreCase(element.getCountries()[0])) {
            result = element;
            break;
          }
        }
      } else if (parts.length == 3) { // e.g. ca-ES-valencia
        for (Language element : Language.LANGUAGES) {
          if (parts[0].equalsIgnoreCase(element.getShortName())
              && element.getCountries().length == 1
              && parts[1].equalsIgnoreCase(element.getCountries()[0])
              && parts[2].equalsIgnoreCase(element.getVariant())) {
            result = element;
            break;
          }
        }
      }
      else { 
        throw new IllegalArgumentException("'" + langCode + "' isn't a valid language code");
      }
    } else {
      for (Language element : Language.LANGUAGES) {
        if (langCode.equalsIgnoreCase(element.getShortName())) {
          result = element;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * Get the best match for a locale, using American English as the final fallback if nothing
   * else fits. The returned language will be a country variant language (e.g. British English, not just English)
   * if available.
   * @since 1.8
   * @throws RuntimeException if no language was found and American English as a fallback is not available
   */
  public static Language getLanguageForLocale(final Locale locale) {
    final Language language = getLanguageForLanguageNameAndCountry(locale);
    if (language != null) {
      return language;
    } else {
      final Language firstFallbackLanguage = getLanguageForLanguageNameOnly(locale);
      if (firstFallbackLanguage != null) {
        return firstFallbackLanguage;
      }
    }
    for (Language aLanguage : REAL_LANGUAGES) {
      if (aLanguage.getShortNameWithCountryAndVariant().equals("en-US")) {
        return aLanguage;
      }
    }
    throw new RuntimeException("No appropriate language found, not even en-US. Supported languages: " + Arrays.toString(REAL_LANGUAGES));
  }

  @Nullable
  private static Language getLanguageForLanguageNameAndCountry(Locale locale) {
    for (Language language : Language.REAL_LANGUAGES) {
      if (language.getShortName().equals(locale.getLanguage())) {
        final List<String> countryVariants = Arrays.asList(language.getCountries());
        if (countryVariants.contains(locale.getCountry())) {
          return language;
        }
      }
    }
    return null;
  }

  @Nullable
  private static Language getLanguageForLanguageNameOnly(Locale locale) {
    // use default variant if available:
    for (Language language : Language.REAL_LANGUAGES) {
      if (language.getShortName().equals(locale.getLanguage()) && language.hasVariant()) {
        final Language defaultVariant = language.getDefaultLanguageVariant();
        if (defaultVariant != null) {
          return defaultVariant;
        }
      }
    }
    // use the first match otherwise (which should be the only match):
    for (Language language : Language.REAL_LANGUAGES) {
      if (language.getShortName().equals(locale.getLanguage()) && !language.hasVariant()) {
        return language;
      }
    }
    return null;
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
    for (Language language : LANGUAGES) {
      final boolean skip = language.getShortNameWithCountryAndVariant().equals(getShortNameWithCountryAndVariant());
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
    for (Language language : LANGUAGES) {
      final boolean skip = language.getShortNameWithCountryAndVariant().equals(getShortNameWithCountryAndVariant());
      if (!skip && getClass().isAssignableFrom(language.getClass())) {
        return true;
      }
    }
    return false;
  }

  public boolean isExternal() {
    return isExternalLanguage;
  }

  /**
   * Sets the language as external. Useful for
   * making a copy of an existing language.
   * @since 2.6
   */
  public void makeExternal() {
    isExternalLanguage = true;
  }

  /**
   * Return true if this is the same language as the given one, considering country
   * variants only if set for both languages. For example: en = en, en = en-GB, en-GB = en-GB,
   * but en-US != en-GB
   * @since 1.8
   */
  public boolean equalsConsiderVariantsIfSpecified(Language otherLanguage) {
    if (getShortName().equals(otherLanguage.getShortName())) {
      final boolean thisHasCountry = hasCountry();
      final boolean otherHasCountry = otherLanguage.hasCountry();
      return !(thisHasCountry && otherHasCountry) ||
              getShortNameWithCountryAndVariant().equals(otherLanguage.getShortNameWithCountryAndVariant());
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
   * Sets the regular expression (usually set of chars) to ignore inside tokens.
   * By default only soft hyphen ({@code \u00AD}) is ignored.
   * @since 2.9
   */
  public void setIgnoredCharactersRegex(String ignoredCharactersRegex) {
    this.ignoredCharactersRegex = Pattern.compile(ignoredCharactersRegex);
  }

}
