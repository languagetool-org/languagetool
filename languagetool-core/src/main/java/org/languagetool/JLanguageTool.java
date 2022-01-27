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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.broker.ClassBroker;
import org.languagetool.broker.DefaultClassBroker;
import org.languagetool.broker.DefaultResourceDataBroker;
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.markup.TextPart;
import org.languagetool.rules.*;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.patterns.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.LoggingTools;
import org.languagetool.tools.LtThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The main class used for checking text against different rules:
 * <ul>
 * <li>built-in Java rules (for English: <i>a</i> vs. <i>an</i>, whitespace after commas, ...)
 * <li>built-in pattern rules loaded from external XML files (usually called {@code grammar.xml})
 * <li>your own implementation of the abstract {@link Rule} classes added with {@link #addRule(Rule)}
 * </ul>
 *
 * <p>You will probably want to use the sub class {@link MultiThreadedJLanguageTool} for best performance.
 *
 * <p><b>Thread-safety:</b> this class is not thread safe. Create one instance per thread,
 * but create the language only once (e.g. {@code new AmericanEnglish()}) and use it for all
 * instances of JLanguageTool.</p>
 *
 * @see MultiThreadedJLanguageTool
 */
public class JLanguageTool {
  private static final Logger logger = LoggerFactory.getLogger(JLanguageTool.class);

  /** LanguageTool version as a string like {@code 2.3} or {@code 2.4-SNAPSHOT}. */
  public static final String VERSION = "5.7-SNAPSHOT";
  /** LanguageTool build date and time like {@code 2013-10-17 16:10} or {@code null} if not run from JAR. */
  @Nullable public static final String BUILD_DATE = getBuildDate();
  /**
   * Abbreviated git id or {@code null} if not available.
   *
   * @since 4.5
   */
  @Nullable
  public static final String GIT_SHORT_ID = getShortGitId();

  /**
   * The name of the file with error patterns.
   */
  public static final String PATTERN_FILE = "grammar.xml";
  /**
   * The name of the file with false friend information.
   */
  public static final String FALSE_FRIEND_FILE = "false-friends.xml";
  /**
   * The internal tag used to mark the beginning of a sentence.
   */
  public static final String SENTENCE_START_TAGNAME = "SENT_START";
  /**
   * The internal tag used to mark the end of a sentence.
   */
  public static final String SENTENCE_END_TAGNAME = "SENT_END";
  /**
   * The internal tag used to mark the end of a paragraph.
   */
  public static final String PARAGRAPH_END_TAGNAME = "PARA_END";
  /**
   * Name of the message bundle for translations.
   */
  public static final String MESSAGE_BUNDLE = "org.languagetool.MessagesBundle";
  /**
   * Extension of dictionary files read by Spellers
   */
  public static final String DICTIONARY_FILENAME_EXTENSION = ".dict";

  private final ResultCache cache;
  private final UserConfig userConfig;
  private final GlobalConfig globalConfig;
  private final ShortDescriptionProvider descProvider;

  private float maxErrorsPerWordRate;

  /**
   * Returns the build date or {@code null} if not run from JAR.
   */
  @Nullable
  private static String getBuildDate() {
    try {
      URL res = getDataBroker().getAsURL("/" + JLanguageTool.class.getName().replace('.', '/') + ".class");
      if (res == null) {
        // this will happen on Android, see http://stackoverflow.com/questions/15371274/
        return null;
      }
      Object connObj = res.openConnection();
      if (connObj instanceof JarURLConnection) {
        Manifest manifest = ((JarURLConnection) connObj).getManifest();
        if (manifest != null) {
          return manifest.getMainAttributes().getValue("Implementation-Date");
        }
      }
      return null;
    } catch (IOException e) {
      throw new RuntimeException("Could not get build date from JAR", e);
    }
  }

  /**
   * Returns the abbreviated git id or {@code null}.
   */
  @Nullable
  private static String getShortGitId() {
    try {
      InputStream in = getDataBroker().getAsStream("/git.properties");
      if (in != null) {
        Properties props = new Properties();
        props.load(in);
        return props.getProperty("git.commit.id.abbrev");
      }
      return null;
    } catch (IOException e) {
      throw new RuntimeException("Could not get git id from 'git.properties'", e);
    }
  }

  private static ResourceDataBroker dataBroker = new DefaultResourceDataBroker();
  private static ClassBroker classBroker = new DefaultClassBroker();

  private static volatile boolean useCustomPasswordAuthenticator = true;

  private final List<Rule> builtinRules;
  private final List<Rule> userRules = new ArrayList<>(); // rules added via addRule() method
  // rules fetched via getRelevantLanguageModelCapableRules()
  private final Set<String> optionalLanguageModelRules = new HashSet<>();
  private final Set<String> disabledRules = new HashSet<>();
  private final Set<CategoryId> disabledRuleCategories = new HashSet<>();
  private final Set<String> enabledRules = new HashSet<>();
  private final Set<CategoryId> enabledRuleCategories = new HashSet<>();
  private final Language language;
  private final List<Language> altLanguages;
  private final Language motherTongue;
  // allow logging of input in stack traces
  private final boolean inputLogging;

  private final List<RuleMatchFilter> matchFilters = new LinkedList<>();

  private CheckCancelledCallback checkCancelledCallback;

  private PrintStream printStream;
  private boolean listUnknownWords;
  private Set<String> unknownWords = new HashSet<>();
  private boolean cleanOverlappingMatches;

  /**
   * Constants for correct paragraph-rule handling.
   */
  public enum ParagraphHandling {
    /**
     * Handle normally - all kinds of rules run.
     */
    NORMAL,
    /**
     * Run only paragraph-level rules.
     */
    ONLYPARA,
    /**
     * Run only sentence-level rules.
     */
    ONLYNONPARA
  }

  public enum Mode {
    // IMPORTANT: directly logged via toString into check_log database table.
    // column is varchar(32), so take care to not exceed this length here
    /**
     * Use all active rules for checking.
     */
    ALL,
    /**
     * Use only text-level rules for checking. This is typically much faster then using all rules or {@code ALL_BUT_TEXTLEVEL_ONLY}.
     */
    TEXTLEVEL_ONLY,
    /**
     * Use all activate rules for checking except the text-level rules.
     */
    ALL_BUT_TEXTLEVEL_ONLY
  }

  public enum Level {
    DEFAULT,
    PICKY
  }

  private static final List<File> temporaryFiles = new ArrayList<>();

  /**
   * Create a JLanguageTool and setup the built-in rules for the
   * given language and false friend rules for the text language / mother tongue pair.
   *
   * @param lang         the language of the text to be checked
   * @param motherTongue the user's mother tongue, used for false friend rules, or <code>null</code>.
   *                     The mother tongue may also be used as a source language for checking bilingual texts.
   */
  public JLanguageTool(Language lang, Language motherTongue) {
    this(lang, motherTongue, null);
  }

  /**
   * Create a JLanguageTool and setup the built-in Java rules for the
   * given language.
   *
   * @param language the language of the text to be checked
   */
  public JLanguageTool(Language language) {
    this(language, null, null, null);
  }

  /**
   * Create a JLanguageTool and setup the built-in rules for the
   * given language and false friend rules for the text language / mother tongue pair.
   *
   * @param language     the language of the text to be checked
   * @param motherTongue the user's mother tongue, used for false friend rules, or <code>null</code>.
   *                     The mother tongue may also be used as a source language for checking bilingual texts.
   * @param cache        a cache to speed up checking if the same sentences get checked more than once,
   *                     e.g. when LT is running as a server and texts are re-checked due to changes
   * @since 3.7
   */
  public JLanguageTool(Language language, Language motherTongue, ResultCache cache) {
    this(language, motherTongue, cache, null);
  }

  /**
   * Create a JLanguageTool and setup the built-in rules for the
   * given language and false friend rules for the text language / mother tongue pair.
   *
   * @param language the language of the text to be checked
   * @param cache    a cache to speed up checking if the same sentences get checked more than once,
   *                 e.g. when LT is running as a server and texts are re-checked due to changes. Use
   *                 {@code null} to deactivate the cache.
   * @since 4.2
   */
  public JLanguageTool(Language language, ResultCache cache, UserConfig userConfig) {
    this(language, null, cache, userConfig);
  }

  public JLanguageTool(Language language, List<Language> altLanguages, Language motherTongue, ResultCache cache,
                       GlobalConfig globalConfig, UserConfig userConfig) {
    this(language, altLanguages, motherTongue, cache, globalConfig, userConfig, true);
  }
  
  /**
   * Create a JLanguageTool and setup the built-in rules for the
   * given language and false friend rules for the text language / mother tongue pair.
   *
   * @param language     the language of the text to be checked
   * @param altLanguages The languages that are accepted as alternative languages - currently this means
   *                     words are accepted if they are in an alternative language and not similar to
   *                     a word from {@code language}. If there's a similar word in {@code language},
   *                     there will be an error of type {@link RuleMatch.Type#Hint} (EXPERIMENTAL)
   * @param motherTongue the user's mother tongue, used for false friend rules, or <code>null</code>.
   *          The mother tongue may also be used as a source language for checking bilingual texts.
   * @param cache a cache to speed up checking if the same sentences get checked more than once,
   *              e.g. when LT is running as a server and texts are re-checked due to changes
   * @param inputLogging allow inclusion of input in logs on exceptions
   * @since 4.3
   */
  public JLanguageTool(Language language, List<Language> altLanguages, Language motherTongue, ResultCache cache,
                       GlobalConfig globalConfig, UserConfig userConfig, boolean inputLogging) {
    this.language = Objects.requireNonNull(language, "language cannot be null");
    this.altLanguages = Objects.requireNonNull(altLanguages, "altLanguages cannot be null (but empty)");
    this.motherTongue = motherTongue;
    if (userConfig == null) {
      this.userConfig = new UserConfig();
    } else {
      this.userConfig = userConfig;
    }
    this.globalConfig = globalConfig;
    ResourceBundle messages = ResourceBundleTools.getMessageBundle(language);
    builtinRules = getAllBuiltinRules(language, messages, userConfig, globalConfig);
    this.cleanOverlappingMatches = true;
    try {
      activateDefaultPatternRules();
      if (!language.hasNGramFalseFriendRule(motherTongue)) {
        // use the old false friends, which always match, not depending on context
        activateDefaultFalseFriendRules();
      }
      updateOptionalLanguageModelRules(null); // start out with rules without language model
    } catch (Exception e) {
      throw new RuntimeException("Could not activate rules", e);
    }
    this.cache = cache;
    descProvider = new ShortDescriptionProvider();
    this.inputLogging = inputLogging;
  }

  /**
   * Create a JLanguageTool and setup the built-in rules for the
   * given language and false friend rules for the text language / mother tongue pair.
   *
   * @param language     the language of the text to be checked
   * @param motherTongue the user's mother tongue, used for false friend rules, or <code>null</code>.
   *                     The mother tongue may also be used as a source language for checking bilingual texts.
   * @param cache        a cache to speed up checking if the same sentences get checked more than once,
   *                     e.g. when LT is running as a server and texts are re-checked due to changes
   * @since 4.2
   */
  public JLanguageTool(Language language, Language motherTongue, ResultCache cache, UserConfig userConfig) {
    this(language, Collections.emptyList(), motherTongue, cache, null, userConfig);
  }

  /**
   * The grammar checker needs resources from following
   * directories:
   * <ul>
   *   <li>{@code /resource}</li>
   *   <li>{@code /rules}</li>
   * </ul>
   *
   * @return The currently set data broker which allows obtaining
   * resources from the mentioned directories above. If no
   * data broker was set, a new {@link DefaultResourceDataBroker} will
   * be instantiated and returned.
   * @since 1.0.1
   */
  public static synchronized ResourceDataBroker getDataBroker() {
    if (JLanguageTool.dataBroker == null) {
      JLanguageTool.dataBroker = new DefaultResourceDataBroker();
    }
    return JLanguageTool.dataBroker;
  }

  /**
   * The grammar checker needs resources from following
   * directories:
   * <ul>
   * <li>{@code /resource}</li>
   * <li>{@code /rules}</li>
   * </ul>
   *
   * @param broker The new resource broker to be used.
   * @since 1.0.1
   */
  public static synchronized void setDataBroker(ResourceDataBroker broker) {
    JLanguageTool.dataBroker = broker;
  }

  /**
   * @return The currently set class broker which allows to load classes.
   * If no class broker was set, a new {@link DefaultClassBroker} will
   * be instantiated and returned.
   * @since 4.9
   */
  public static synchronized ClassBroker getClassBroker() {
    if (JLanguageTool.classBroker == null) {
      JLanguageTool.classBroker = new DefaultClassBroker();
    }
    return JLanguageTool.classBroker;
  }

  /**
   * @param broker The new class broker to be used.
   * @since 4.9
   */
  public static synchronized void setClassBrokerBroker(ClassBroker broker) {
    JLanguageTool.classBroker = broker;
  }

  /**
   * Whether the {@code Tools.setPasswordAuthenticator()} should be called when loading rules
   * in rule loader to use {@code PasswordAuthenticator} as default one.
   *
   * @return true if {@code PasswordAuthenticator} should be used
   * @since 5.6
   */
  public static boolean isCustomPasswordAuthenticatorUsed() {
    return JLanguageTool.useCustomPasswordAuthenticator;
  }

  /**
   * Whether the {@code Tools.setPasswordAuthenticator()} should be called when loading rules
   * in rule loader to use {@code PasswordAuthenticator} as default one.
   *
   * @param use true if {@code PasswordAuthenticator} should be used
   * @since 5.6
   */
  public static void useCustomPasswordAuthenticator(boolean use) {
    JLanguageTool.useCustomPasswordAuthenticator = use;
  }

  /**
   * Whether the {@link #check(String)} methods store unknown words. If set to
   * <code>true</code> (default: false), you can get the list of unknown words
   * using {@link #getUnknownWords()}.
   */
  public void setListUnknownWords(boolean listUnknownWords) {
    this.listUnknownWords = listUnknownWords;
  }

  /**
   * Whether the {@link #check(String)} methods return overlapping errors. If set to
   * <code>true</code> (default: true), it removes overlapping errors according to
   * the priorities established for the language.
   *
   * @since 3.6
   */
  public void setCleanOverlappingMatches(boolean cleanOverlappingMatches) {
    this.cleanOverlappingMatches = cleanOverlappingMatches;
  }

  /**
   * Maximum errors per word rate, checking will stop with an exception if the rate is higher.
   * For example, with a rate of 0.33, the checking would stop if the user's
   * text has so many errors that more than every 3rd word causes a rule match.
   * Note that this may not apply for very short texts.
   *
   * @since 4.0
   */
  public void setMaxErrorsPerWordRate(float maxErrorsPerWordRate) {
    this.maxErrorsPerWordRate = maxErrorsPerWordRate;
  }

  /**
   * Callback to determine if result of executing {@link #check(String)} is still needed.
   */
  public void setCheckCancelledCallback(CheckCancelledCallback callback) {
    this.checkCancelledCallback = callback;
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the default language of the user's system.
   */
  public static ResourceBundle getMessageBundle() {
    return ResourceBundleTools.getMessageBundle();
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the given user interface language.
   *
   * @since 2.4 (public since 2.4)
   */
  public static ResourceBundle getMessageBundle(Language lang) {
    return ResourceBundleTools.getMessageBundle(lang);
  }

  private List<Rule> getAllBuiltinRules(Language language, ResourceBundle messages, UserConfig userConfig, GlobalConfig globalConfig) {
    try {
      List<Rule> rules = new ArrayList<>(language.getRelevantRules(messages, userConfig, motherTongue, altLanguages));
      rules.addAll(language.getRelevantRulesGlobalConfig(messages, globalConfig, userConfig, motherTongue, altLanguages));
      return rules;
    } catch (IOException e) {
      throw new RuntimeException("Could not get rules of language " + language, e);
    }
  }

  /**
   * Set a PrintStream that will receive verbose output. Set to
   * {@code null} (which is the default) to disable verbose output.
   */
  public void setOutput(PrintStream printStream) {
    this.printStream = printStream;
  }

  /**
   * Load pattern rules from an XML file. Use {@link #addRule(Rule)} to add these
   * rules to the checking process.
   *
   * @param filename path to an XML file in the classpath or in the filesystem - the classpath is checked first
   * @return a List of {@link PatternRule} objects
   */
  public List<AbstractPatternRule> loadPatternRules(String filename) throws IOException {
    PatternRuleLoader ruleLoader = new PatternRuleLoader();
    try (InputStream is = getDataBroker().getAsStream(filename)) {
      if (is == null) {
        // happens for external rules plugged in as an XML file or testing files:
        if (filename.contains("-test-")) {
          // ignore, for testing
          return Collections.emptyList();
        } else {
          return ruleLoader.getRules(new File(filename));
        }
      } else {
        return ruleLoader.getRules(is, filename);
      }
    }
  }

  /**
   * Load false friend rules from an XML file. Only those pairs will be loaded
   * that match the current text language and the mother tongue specified in the
   * JLanguageTool constructor. Use {@link #addRule(Rule)} to add these rules to the
   * checking process.
   *
   * @param filename path to an XML file in the classpath or in the filesystem - the classpath is checked first
   * @return a List of {@link PatternRule} objects, or an empty list if mother tongue is not set
   */
  public List<AbstractPatternRule> loadFalseFriendRules(String filename)
    throws ParserConfigurationException, SAXException, IOException {
    if (motherTongue == null) {
      return Collections.emptyList();
    }
    FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader(motherTongue);
    try (InputStream is = getDataBroker().getAsStream(filename)) {
      if (is == null) {
        return ruleLoader.getRules(new File(filename), language, motherTongue);
      } else {
        return ruleLoader.getRules(is, language, motherTongue);
      }
    }
  }

  /**
   * Remove rules that can profit from a language model, recreate them with the given model and add them again
   *
   * @param lm the language model or null if none is available
   */
  private void updateOptionalLanguageModelRules(@Nullable LanguageModel lm) {
    ResourceBundle messages = getMessageBundle(language);
    try {
      List<Rule> rules = language.getRelevantLanguageModelCapableRules(messages, lm, globalConfig, userConfig, motherTongue, altLanguages);
      userRules.removeIf(rule -> optionalLanguageModelRules.contains(rule.getId()));
      optionalLanguageModelRules.clear();
      rules.stream().map(Rule::getId).forEach(optionalLanguageModelRules::add);
      userRules.addAll(rules);
    } catch (Exception e) {
      throw new RuntimeException("Could not load language model capable rules.", e);
    }
    ruleSetCache.clear();
  }

  /**
   * Activate rules that depend on pre-trained neural network models.
   *
   * @param modelDir root dir of exported models
   * @since 4.4
   */
  public void activateNeuralNetworkRules(File modelDir) throws IOException {
    ResourceBundle messages = getMessageBundle(language);
    List<Rule> rules = language.getRelevantNeuralNetworkModels(messages, modelDir);
    userRules.addAll(rules);
    ruleSetCache.clear();
  }

  /**
   * Activate rules that depend on a language model. The language model currently
   * consists of Lucene indexes with ngram occurrence counts.
   *
   * @param indexDir directory with a '3grams' sub directory which contains a Lucene index with 3gram occurrence counts
   * @since 2.7
   */
  public void activateLanguageModelRules(File indexDir) throws IOException {
    LanguageModel languageModel = language.getLanguageModel(indexDir);
    if (languageModel != null) {
      ResourceBundle messages = getMessageBundle(language);
      List<Rule> rules = language.getRelevantLanguageModelRules(messages, languageModel, userConfig);
      userRules.addAll(rules);
      updateOptionalLanguageModelRules(languageModel);
    }
  }

  private void transformRules(Function<Rule, Rule> mapper, List<Rule> rules) {
    // transform this way because variables are final + could log where rule was changed
    for (int i = 0; i < rules.size(); i++) {
      Rule original = rules.get(i);
      Rule transformed = mapper.apply(original);
      if (transformed != original) {
        rules.set(i, transformed);
      }
    }
  }

  public void activateRemoteRules(@Nullable File configFile) throws IOException {
    List<RemoteRuleConfig> configs;
    try {
      if (configFile != null) {
        configs = RemoteRuleConfig.load(configFile);
      } else {
        configs = Collections.emptyList();
      }
      activateRemoteRules(configs);
    } catch (IOException e) {
      throw new IOException("Could not load remote rules.", e);
    } catch (ExecutionException e) {
      throw new IOException("Could not load remote rules configuration at " + configFile.getAbsolutePath(), e);
    }
  }

  public void activateRemoteRules(List<RemoteRuleConfig> configs) throws IOException {
    List<Rule> rules = language.getRelevantRemoteRules(getMessageBundle(language), configs,
      globalConfig, userConfig, motherTongue, altLanguages, inputLogging);
    userRules.addAll(rules);
    Function<Rule, Rule> enhanced = language.getRemoteEnhancedRules(getMessageBundle(language), configs, userConfig, motherTongue, altLanguages, inputLogging);
    transformRules(enhanced, builtinRules);
    transformRules(enhanced, userRules);
    ruleSetCache.clear();
  }

  /**
   * Activate rules that depend on a word2vec language model.
   *
   * @param indexDir directory with a subdirectories like 'en', each containing dictionary.txt and final_embeddings.txt
   * @since 4.0
   */
  public void activateWord2VecModelRules(File indexDir) throws IOException {
    Word2VecModel word2vecModel = language.getWord2VecModel(indexDir);
    if (word2vecModel != null) {
      ResourceBundle messages = getMessageBundle(language);
      List<Rule> rules = language.getRelevantWord2VecModelRules(messages, word2vecModel);
      userRules.addAll(rules);
    }
  }

  /**
   * Loads and activates the pattern rules from
   * {@code org/languagetool/rules/<languageCode>/grammar.xml}.
   */
  private void activateDefaultPatternRules() throws IOException {
    List<AbstractPatternRule> patternRules = language.getPatternRules();
    List<String> enabledRules = language.getDefaultEnabledRulesForVariant();
    List<String> disabledRules = language.getDefaultDisabledRulesForVariant();
    if (!enabledRules.isEmpty() || !disabledRules.isEmpty()) {
      for (AbstractPatternRule patternRule : patternRules) {
        if (enabledRules.contains(patternRule.getId())) {
          patternRule.setDefaultOn();
        }
        if (disabledRules.contains(patternRule.getId())) {
          patternRule.setDefaultOff();
        }
      }
    }
    List<Rule> transformed = transformPatternRules(patternRules, language);
    userRules.addAll(transformed);
  }

  private List<Rule> transformPatternRules(List<AbstractPatternRule> patternRules, Language lang) {
    List<AbstractPatternRule> rules = new ArrayList<>(patternRules);
    List<PatternRuleTransformer> transforms = Arrays.asList(new RepeatedPatternRuleTransformer(lang));

    List<Rule> transformed = new ArrayList<>();
    for (PatternRuleTransformer op : transforms) {
      PatternRuleTransformer.TransformedRules result = op.apply(rules);
      rules = result.getRemainingRules();
      transformed.addAll(result.getTransformedRules());
    }
    transformed.addAll(rules);
    return transformed;
  }

  /**
   * Loads and activates the false friend rules from
   * <code>rules/false-friends.xml</code>.
   */
  private void activateDefaultFalseFriendRules()
    throws ParserConfigurationException, SAXException, IOException {
    String falseFriendRulesFilename = JLanguageTool.getDataBroker().getRulesDir() + "/" + FALSE_FRIEND_FILE;
    userRules.addAll(loadFalseFriendRules(falseFriendRulesFilename));
    ruleSetCache.clear();
  }

  /**
   * Add a {@link RuleMatchFilter} for post-processing of rule matches
   * Filters are called sequentially in the same order as added
   *
   * @param filter filter to add
   * @since 4.7
   */
  public void addMatchFilter(@NotNull RuleMatchFilter filter) {
    matchFilters.add(Objects.requireNonNull(filter));
  }

  /**
   * Add a rule to be used by the next call to the check methods like {@link #check(String)}.
   */
  public void addRule(Rule rule) {
    userRules.add(rule);
    ruleSetCache.clear();
  }

  /**
   * Disable a given rule so the check methods like {@link #check(String)} won't use it.
   *
   * @param ruleId the id of the rule to disable - no error will be thrown if the id does not exist
   * @see #enableRule(String)
   */
  public void disableRule(String ruleId) {
    disabledRules.add(ruleId);
    enabledRules.remove(ruleId);
    ruleSetCache.clear();
  }

  /**
   * Disable the given rules so the check methods like {@link #check(String)} won't use them.
   *
   * @param ruleIds the ids of the rules to disable - no error will be thrown if the id does not exist
   * @since 2.4
   */
  public void disableRules(List<String> ruleIds) {
    disabledRules.addAll(ruleIds);
    enabledRules.removeAll(ruleIds);
    ruleSetCache.clear();
  }

  /**
   * Disable the given rule category so the check methods like {@link #check(String)} won't use it.
   *
   * @param id the id of the category to disable - no error will be thrown if the id does not exist
   * @see #enableRuleCategory(CategoryId)
   * @since 3.3
   */
  public void disableCategory(CategoryId id) {
    disabledRuleCategories.add(id);
    enabledRuleCategories.remove(id);
    ruleSetCache.clear();
  }

  /**
   * Returns true if a category is explicitly disabled.
   *
   * @param id the id of the category to check - no error will be thrown if the id does not exist
   * @return true if this category is explicitly disabled.
   * @see #disableCategory(org.languagetool.rules.CategoryId)
   * @since 3.5
   */
  public boolean isCategoryDisabled(CategoryId id) {
    return disabledRuleCategories.contains(id);
  }

  /**
   * Get the language that was used to configure this instance.
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * Get rule ids of the rules that have been explicitly disabled.
   */
  public Set<String> getDisabledRules() {
    return disabledRules;
  }

  /**
   * Enable a given rule so the check methods like {@link #check(String)} will use it.
   * This will <em>not</em> throw an exception if the given rule id doesn't exist.
   *
   * @param ruleId the id of the rule to enable
   * @see #disableRule(String)
   */
  public void enableRule(String ruleId) {
    disabledRules.remove(ruleId);
    enabledRules.add(ruleId);
    ruleSetCache.clear();
  }

  /**
   * Enable all rules of the given category so the check methods like {@link #check(String)} will use it.
   * This will <em>not</em> throw an exception if the given rule id doesn't exist.
   *
   * @see #disableCategory(org.languagetool.rules.CategoryId)
   * @since 3.3
   */
  public void enableRuleCategory(CategoryId id) {
    disabledRuleCategories.remove(id);
    enabledRuleCategories.add(id);
    ruleSetCache.clear();
  }

  /**
   * Tokenizes the given text into sentences.
   */
  public List<String> sentenceTokenize(String text) {
    return language.getSentenceTokenizer().tokenize(text);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   *
   * @param text the text to be checked
   * @return a List of {@link RuleMatch} objects
   */
  public List<RuleMatch> check(String text) throws IOException {
    return check(text, true, ParagraphHandling.NORMAL);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   *
   * @param text the text to be checked
   * @return a List of {@link RuleMatch} objects
   * @since 3.7
   */
  public List<RuleMatch> check(String text, RuleMatchListener listener) throws IOException {
    return check(text, true, ParagraphHandling.NORMAL, listener);
  }

  public List<RuleMatch> check(String text, boolean tokenizeText, ParagraphHandling paraMode) throws IOException {
    return check(new AnnotatedTextBuilder().addText(text).build(), tokenizeText, paraMode);
  }

  /**
   * @since 3.7
   */
  public List<RuleMatch> check(String text, boolean tokenizeText, ParagraphHandling paraMode, RuleMatchListener listener) throws IOException {
    return check(new AnnotatedTextBuilder().addText(text).build(), tokenizeText, paraMode, listener);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules, adjusting error positions so they refer
   * to the original text <em>including</em> markup.
   *
   * @since 2.3
   */
  public List<RuleMatch> check(AnnotatedText text) throws IOException {
    return check(text, true, ParagraphHandling.NORMAL);
  }

  /**
   * @since 3.9
   */
  public List<RuleMatch> check(AnnotatedText text, RuleMatchListener listener) throws IOException {
    return check(text, true, ParagraphHandling.NORMAL, listener);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   *
   * @param annotatedText The text to be checked, created with {@link AnnotatedTextBuilder}.
   *                      Call this method with the complete text to be checked. If you call it
   *                      repeatedly with smaller chunks like paragraphs or sentence, those rules that work across
   *                      paragraphs/sentences won't work (their status gets reset whenever this method is called).
   * @param tokenizeText  If true, then the text is tokenized into sentences.
   *                      Otherwise, it is assumed it's already tokenized, i.e. it is only one sentence
   * @param paraMode      Uses paragraph-level rules only if true.
   * @return a List of {@link RuleMatch} objects, describing potential errors in the text
   * @since 2.3
   */
  public List<RuleMatch> check(AnnotatedText annotatedText, boolean tokenizeText, ParagraphHandling paraMode) throws IOException {
    return check(annotatedText, tokenizeText, paraMode, null);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   *
   * @since 3.7
   */
  public List<RuleMatch> check(AnnotatedText annotatedText, boolean tokenizeText, ParagraphHandling paraMode, RuleMatchListener listener) throws IOException {
    Mode mode;
    if (paraMode == ParagraphHandling.ONLYNONPARA) {
      mode = Mode.ALL_BUT_TEXTLEVEL_ONLY;
    } else if (paraMode == ParagraphHandling.ONLYPARA) {
      mode = Mode.TEXTLEVEL_ONLY;
    } else {
      mode = Mode.ALL;
    }
    return check(annotatedText, tokenizeText, paraMode, listener, mode, Level.DEFAULT);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules depending on {@code mode}.
   *
   * @since 4.3
   */
  public List<RuleMatch> check(AnnotatedText annotatedText, boolean tokenizeText, ParagraphHandling paraMode, RuleMatchListener listener, Mode mode, Level level) throws IOException {
    return check(annotatedText, tokenizeText, paraMode, listener, mode, level, null);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules depending on {@code mode}.
   *
   * @param textSessionID UserConfig.getTextSessionID can be outdated because of pipeline pool caching, so pass through directly
   * @since 5.2
   */
  public List<RuleMatch> check(AnnotatedText annotatedText, boolean tokenizeText, ParagraphHandling paraMode, RuleMatchListener listener,
                               Mode mode, Level level, @Nullable Long textSessionID) throws IOException {
    annotatedText = cleanText(annotatedText);
    List<String> sentences = getSentences(annotatedText, tokenizeText);
    List<AnalyzedSentence> analyzedSentences = analyzeSentences(sentences);
    return checkInternal(annotatedText, paraMode, listener, mode, level, textSessionID, sentences, analyzedSentences).getRuleMatches();
  }

  public CheckResults check2(AnnotatedText annotatedText, boolean tokenizeText, ParagraphHandling paraMode, RuleMatchListener listener,
                             Mode mode, Level level, @Nullable Long textSessionID) throws IOException {
    annotatedText = cleanText(annotatedText);
    List<String> sentences = getSentences(annotatedText, tokenizeText);
    List<AnalyzedSentence> analyzedSentences = analyzeSentences(sentences);
    return checkInternal(annotatedText, paraMode, listener, mode, level, textSessionID, sentences, analyzedSentences);
  }

  private List<String> getSentences(AnnotatedText annotatedText, boolean tokenizeText) {
    List<String> sentences;
    if (tokenizeText) {
      sentences = sentenceTokenize(annotatedText.getPlainText());
    } else {
      sentences = new ArrayList<>();
      sentences.add(annotatedText.getPlainText());
    }
    return sentences;
  }

  private AnnotatedText cleanText(AnnotatedText annotatedText) {
    AnnotatedTextBuilder atb = new AnnotatedTextBuilder();
    annotatedText.getGlobalMetaData().forEach((key, value) -> atb.addGlobalMetaData(key, value));
    annotatedText.getCustomMetaData().forEach((key, value) -> atb.addGlobalMetaData(key, value));
    List<TextPart> parts = annotatedText.getParts();
    for (TextPart part : parts) {
      if (part.getType() == TextPart.Type.TEXT) {
        String byteOrderMark = "\uFEFF";  // BOM or zero-width non-breaking space
        StringTokenizer st = new StringTokenizer(part.getPart(), byteOrderMark, true);
        while (st.hasMoreElements()) {
          Object next = st.nextElement();
          if (next.equals(byteOrderMark)) {
            atb.addMarkup(byteOrderMark);
          } else {
            atb.addText(next.toString());
          }
        }
      } else {
        atb.add(part);
      }
    }
    return atb.build();
  }
  
  private CheckResults checkInternal(AnnotatedText annotatedText, ParagraphHandling paraMode, RuleMatchListener listener,
                                     Mode mode, Level level,
                                     @Nullable Long textSessionID, List<String> sentences, List<AnalyzedSentence> analyzedSentences) throws IOException {
    RuleSet rules = getActiveRulesForLevel(level);
    if (printStream != null) {
      printIfVerbose(rules.allRules().size() + " rules activated for language " + language);
    }

    List<RuleMatch> remoteMatches = new LinkedList<>();
    List<FutureTask<RemoteRuleResult>> remoteRuleTasks = null;

    List<RemoteRule> remoteRules = rules.allRules().stream()
      .filter(RemoteRule.class::isInstance).map(RemoteRule.class::cast)
      .collect(Collectors.toList());

    long remoteRuleCheckStart = System.nanoTime();
    // map by sentence index, as the same sentence can be repeated multiple times in a text
    // -> need to distinguish offsets / matches
    Map<Integer, List<RuleMatch>> cachedResults = new HashMap<>();
    Map<Integer, Integer> matchOffset = new HashMap<>();
    // store actual request sizes (i.e. without cached sentences), so timeouts and metrics are calculated correctly
    List<Integer> requestSize = new ArrayList<>();
    ExecutorService remoteRulesThreadPool =
      mode == Mode.TEXTLEVEL_ONLY || remoteRules.isEmpty() ? null :
      LtThreadPoolFactory.getFixedThreadPoolExecutor(LtThreadPoolFactory.REMOTE_RULE_EXECUTING_POOL).orElse(null);
    if (remoteRulesThreadPool != null) {
      // trigger remote rules to run on whole text at once, at the start, then we wait for the results
      remoteRuleTasks = new ArrayList<>();
      checkRemoteRules(remoteRules, analyzedSentences, mode, level,
        remoteRuleTasks, requestSize, cachedResults, matchOffset, textSessionID, remoteRulesThreadPool);
    }

    long deadlineStartNanos = System.nanoTime();
    CheckResults res = performCheck(analyzedSentences, sentences, rules,
            paraMode, annotatedText, listener, mode, level, remoteRulesThreadPool == null);
    long textCheckEnd = System.nanoTime();

    fetchRemoteRuleResults(deadlineStartNanos, mode, level, analyzedSentences, remoteMatches, remoteRuleTasks, remoteRules, requestSize,
      cachedResults, matchOffset, annotatedText, textSessionID);
    long remoteRuleCheckEnd = System.nanoTime();
    if (remoteRules.size() > 0) {
      long wait = TimeUnit.NANOSECONDS.toMillis(remoteRuleCheckEnd - textCheckEnd);
      logger.info("Local checks took {}ms, remote checks {}ms; waited {}ms on remote results",
        TimeUnit.NANOSECONDS.toMillis(textCheckEnd - deadlineStartNanos),
        TimeUnit.NANOSECONDS.toMillis(remoteRuleCheckEnd - remoteRuleCheckStart), wait);
      RemoteRuleMetrics.wait(language.getShortCode(), wait);
    }

    List<RuleMatch> ruleMatches = res.getRuleMatches();

    ruleMatches.addAll(remoteMatches);

    return ruleMatches.isEmpty() ? res :
           new CheckResults(filterMatches(annotatedText, rules, ruleMatches), res.getIgnoredRanges());
  }

  private List<RuleMatch> filterMatches(AnnotatedText annotatedText, RuleSet rules, List<RuleMatch> ruleMatches) {
    // rules can create matches with rule IDs different from the original rule (see e.g. RemoteRules)
    // so while we can't avoid execution of these rules, we still want disabling them to work
    // so do another pass with ignoreRule here
    ruleMatches = ruleMatches.stream().filter(match -> !ignoreRule(match.getRule())).collect(Collectors.toList());

    ruleMatches = new SameRuleGroupFilter().filter(ruleMatches);
    // no sorting: SameRuleGroupFilter sorts rule matches already
    if (cleanOverlappingMatches) {
      ruleMatches = new CleanOverlappingFilter(language, userConfig.getHidePremiumMatches()).filter(ruleMatches);
    }
    ruleMatches = new LanguageDependentFilter(language, rules).filter(ruleMatches);

    return applyCustomFilters(ruleMatches, annotatedText);
  }

  private final Map<Level, RuleSet> ruleSetCache = new ConcurrentHashMap<>();

  private RuleSet getActiveRulesForLevel(Level level) {
    return ruleSetCache.computeIfAbsent(level, l -> {
      List<Rule> allRules = getAllActiveRules();
      return RuleSet.textLemmaHinted(l == Level.DEFAULT ? allRules.stream().filter(rule -> !rule.hasTag(Tag.picky)).collect(Collectors.toList()) : allRules);
    });
  }

  protected void fetchRemoteRuleResults(long deadlineStartNanos, Mode mode, Level level, List<AnalyzedSentence> analyzedSentences, List<RuleMatch> remoteMatches,
                                        List<FutureTask<RemoteRuleResult>> remoteRuleTasks, List<RemoteRule> remoteRules,
                                        List<Integer> requestSize,
                                        Map<Integer, List<RuleMatch>> cachedResults,
                                        Map<Integer, Integer> matchOffset,
                                        AnnotatedText annotatedText, Long textSessionID) {
    if (remoteRuleTasks != null && !remoteRuleTasks.isEmpty()) {
      int timeout = IntStream.range(0, requestSize.size()).map(i ->
        (int) remoteRules.get(i).getTimeout(requestSize.get(i))
      ).max().getAsInt();
      long deadlineEndNanos;
      if (timeout <= 0) {
        deadlineEndNanos = 0;
      } else {
        deadlineEndNanos = deadlineStartNanos + TimeUnit.MILLISECONDS.toNanos(timeout);
      }
      // fetch results from remote rules
      for (int taskIndex = 0; taskIndex < remoteRuleTasks.size(); taskIndex++) {
        FutureTask<RemoteRuleResult> task = remoteRuleTasks.get(taskIndex);
        RemoteRule rule = remoteRules.get(taskIndex);
        String ruleKey = rule.getId();
        long chars = requestSize.get(taskIndex);
        if (task == null && chars == 0) { // everything cached
          //logger.info("Results for remote rule already cached");
          continue;
        } else if (task == null) { // circuitbreaker open or task rejected from pool
          // rejected tasks are already logged/tracked in LtThreadPoolFactory
          RemoteRuleMetrics.request(ruleKey, deadlineStartNanos, chars, RemoteRuleMetrics.RequestResult.DOWN);
          continue;
        }
        try {
          //logger.info("Fetching results for remote rule for {} chars", chars);
          RemoteRuleMetrics.inCircuitBreaker(deadlineStartNanos, rule, ruleKey, chars, () ->
            fetchResults(deadlineStartNanos, mode, level, analyzedSentences, remoteMatches, matchOffset, annotatedText, textSessionID, chars, deadlineEndNanos, task, rule, ruleKey));
        } catch (InterruptedException e) {
          break;
        }
      }

      for (Integer cachedSentenceIndex : cachedResults.keySet()) {
        List<RuleMatch> cachedMatches = cachedResults.get(cachedSentenceIndex);
        int sentenceOffset = matchOffset.get(cachedSentenceIndex);
        for (RuleMatch cachedMatch : cachedMatches) {
          // clone so that we don't adjust match position for cache
          RuleMatch match = new RuleMatch(cachedMatch);
          adjustOffset(annotatedText, sentenceOffset, match);
          remoteMatches.add(match);
        }
      }

      for (RuleMatch match : remoteMatches) {
        match.setSuggestedReplacementObjects(extendSuggestions(match.getSuggestedReplacementObjects()));
      }

      // cancel any remaining tasks (e.g. after interrupt because request timed out)
      remoteRuleTasks.stream().filter(Objects::nonNull).forEach(t -> t.cancel(true));
    }
  }

  private RemoteRuleResult fetchResults(long deadlineStartNanos, Mode mode, Level level, List<AnalyzedSentence> analyzedSentences, List<RuleMatch> remoteMatches, Map<Integer, Integer> matchOffset, AnnotatedText annotatedText, Long textSessionID, long chars, long deadlineEndNanos, FutureTask<RemoteRuleResult> task, RemoteRule rule, String ruleKey) throws InterruptedException, ExecutionException, TimeoutException {
    RemoteRuleResult result;
    if (rule.getTimeout(chars) <= 0) {
      result = task.get();
    } else {
      long waitTime = Math.max(0, deadlineEndNanos - System.nanoTime());
      logger.debug("Waiting for {}ms for check of {} ({} chars)",
        TimeUnit.NANOSECONDS.toMillis(waitTime), ruleKey, chars);
      result = task.get(waitTime, TimeUnit.NANOSECONDS);
    }
    RemoteRuleMetrics.RequestResult loggedResult = result.isSuccess() ?
      RemoteRuleMetrics.RequestResult.SUCCESS : RemoteRuleMetrics.RequestResult.ERROR;
    RemoteRuleMetrics.request(ruleKey, deadlineStartNanos, chars, loggedResult);
    for (int sentenceIndex = 0; sentenceIndex < analyzedSentences.size(); sentenceIndex++) {
      AnalyzedSentence sentence = analyzedSentences.get(sentenceIndex);
      List<RuleMatch> matches = result.matchesForSentence(sentence);
      if (matches == null) {
        continue;
      }
      if (cache != null && result.isSuccess()) {
        // store in cache
        InputSentence cacheKey = new InputSentence(
          sentence.getText(), language, motherTongue, disabledRules, disabledRuleCategories,
          enabledRules, enabledRuleCategories, userConfig, altLanguages, mode, level, textSessionID);
        Map<String, List<RuleMatch>> cacheEntry = cache.getRemoteMatchesCache().get(cacheKey, HashMap::new);
        cacheEntry.put(ruleKey, matches);
      }
      // adjust rule match position
      // rules check all sentences batched, but should keep position adjustment logic out of rule
      int offset = matchOffset.get(sentenceIndex);
      // clone matches before adjusting offsets
      // match objects could be relevant to multiple (duplicate) sentences at different offsets
      List<RuleMatch> adjustedMatches = matches.stream().map(RuleMatch::new).collect(Collectors.toList());
      for (RuleMatch match : adjustedMatches) {
        adjustOffset(annotatedText, offset, match);
      }
      remoteMatches.addAll(adjustedMatches);
    }
    return result;
  }

  private void adjustOffset(AnnotatedText annotatedText, int offset, RuleMatch match) {
    int fromPos;
    int toPos;
    if (annotatedText != null) {
      fromPos = annotatedText.getOriginalTextPositionFor(match.getFromPos() + offset, false);
      toPos = annotatedText.getOriginalTextPositionFor(match.getToPos() + offset - 1, true) + 1;
    } else {
      fromPos = match.getFromPos() + offset;
      toPos = match.getToPos() + offset;
    }
    match.setOffsetPosition(fromPos, toPos);
  }

  private void checkRemoteRules(List<RemoteRule> rules, List<AnalyzedSentence> analyzedSentences, Mode mode, Level level,
                                List<FutureTask<RemoteRuleResult>> remoteRuleTasks, List<Integer> requestSize,
                                Map<Integer, List<RuleMatch>> cachedResults, Map<Integer, Integer> matchOffset,
                                Long textSessionID, ExecutorService executor) {
    List<InputSentence> cacheKeys = new LinkedList<>();
    int offset = 0;
    // prepare keys for caching, offsets for adjusting match positions
    for (int i = 0; i < analyzedSentences.size(); i++) {
      AnalyzedSentence s = analyzedSentences.get(i);
      matchOffset.put(i, offset);
      offset += s.getText().length();
      InputSentence cacheKey = new InputSentence(s.getText(), language, motherTongue,
        disabledRules, disabledRuleCategories, enabledRules, enabledRuleCategories,
        userConfig, altLanguages, mode, level, textSessionID);
      cacheKeys.add(cacheKey);
    }
    for (RemoteRule rule : rules) {
      FutureTask<RemoteRuleResult> task;
      List<AnalyzedSentence> input;
      int size;
      if (cache != null) {
        List<AnalyzedSentence> nonCachedSentences = new ArrayList<>();
        for (int sentenceIndex = 0; sentenceIndex < analyzedSentences.size(); sentenceIndex++) {
          // filter out sentences with cached results
          InputSentence cacheKey = cacheKeys.get(sentenceIndex);
          String ruleKey = rule.getId();
          AnalyzedSentence sentence = analyzedSentences.get(sentenceIndex);
          Map<String, List<RuleMatch>> cacheEntry;
          try {
            cacheEntry = cache.getRemoteMatchesCache().get(cacheKey, HashMap::new);
          } catch (ExecutionException e) {
            throw new RuntimeException(e);
          }
          if (cacheEntry == null) {
            throw new RuntimeException("Couldn't access remote matches cache.");
          }
          List<RuleMatch> cachedMatches = cacheEntry.get(ruleKey);
          // mark for check or retrieve from cache
          if (cachedMatches == null) {
            nonCachedSentences.add(sentence);
          } else {
            cachedResults.putIfAbsent(sentenceIndex, new LinkedList<>());
            cachedResults.get(sentenceIndex).addAll(cachedMatches);
          }
        }
        // userConfig is cached by pipeline pool,
        // logger.info("Checking {} not cached sentences out of {}", nonCachedSentences.size(), analyzedSentences.size());
        input = nonCachedSentences;
      } else {
        input = analyzedSentences;
      }
      size = input.stream().map(s -> s.getText().length()).reduce(0, Integer::sum);
      task = rule.run(input, textSessionID);
      requestSize.add(size);

      try {
        // skip calls (which send requests) if open/forced_open
        // try calls if half_open
        // would need manual tracking if we use tryAcquirePermission, this is easier
        // does require automaticTransitionFromOpenToHalfOpenEnabled settting
        if (size == 0 ||
          rule.circuitBreaker().getState() == CircuitBreaker.State.OPEN ||
          rule.circuitBreaker().getState() == CircuitBreaker.State.FORCED_OPEN) {
          task = null;
        } else {
          executor.submit(task);
        }
        remoteRuleTasks.add(task);
      } catch (RejectedExecutionException ignored) {
        // remoteRuleTasks and remoteRules lists are expected to be aligned
        remoteRuleTasks.add(null);
      }
    }
  }

  /**
   * Use this method if you want to access LanguageTool's otherwise
   * internal analysis of the text. For actual text checking, use the {@code check...} methods instead.
   *
   * @param text The text to be analyzed
   * @since 2.5
   */
  public List<AnalyzedSentence> analyzeText(String text) throws IOException {
    List<String> sentences = sentenceTokenize(text);
    return analyzeSentences(sentences);
  }

  protected List<AnalyzedSentence> analyzeSentences(List<String> sentences) throws IOException {
    unknownWords = new HashSet<>();
    List<AnalyzedSentence> analyzedSentences = new ArrayList<>();
    int j = 0;
    for (String sentence : sentences) {
      if (checkCancelledCallback != null && checkCancelledCallback.checkCancelled()) {
        break;
      }
      AnalyzedSentence analyzedSentence = getAnalyzedSentence(sentence);
      rememberUnknownWords(analyzedSentence);
      if (++j == sentences.size()) {
        analyzedSentence = markAsParagraphEnd(analyzedSentence);
      }
      analyzedSentences.add(analyzedSentence);
      printSentenceInfo(analyzedSentence);
    }
    return analyzedSentences;
  }

  @NotNull
  static AnalyzedSentence markAsParagraphEnd(AnalyzedSentence analyzedSentence) {
    AnalyzedTokenReadings[] anTokens = analyzedSentence.getTokens();
    anTokens[anTokens.length - 1].setParagraphEnd();
    AnalyzedTokenReadings[] preDisambigAnTokens = analyzedSentence.getPreDisambigTokens();
    preDisambigAnTokens[anTokens.length - 1].setParagraphEnd();
    return new AnalyzedSentence(anTokens, preDisambigAnTokens);  ///TODO: why???
  }

  protected void printSentenceInfo(AnalyzedSentence analyzedSentence) {
    if (printStream != null) {
      printIfVerbose(analyzedSentence.toString());
      printIfVerbose(analyzedSentence.getAnnotations());
    }
  }

  /**
   * @deprecated use {@link #performCheck(List, List, RuleSet, ParagraphHandling, AnnotatedText, RuleMatchListener, Mode, Level, boolean)}
   */
  @Deprecated
  protected CheckResults performCheck(List<AnalyzedSentence> analyzedSentences, List<String> sentences,
                                         List<Rule> allRules, ParagraphHandling paraMode, AnnotatedText annotatedText, Mode mode, Level level) throws IOException {
    List<Rule> nonIgnored = allRules.stream().filter(r -> !ignoreRule(r)).collect(Collectors.toList());
    return performCheck(analyzedSentences, sentences, nonIgnored, paraMode, annotatedText, null, mode, level, true);
  }

  /**
   * @deprecated use {@link #performCheck(List, List, RuleSet, ParagraphHandling, AnnotatedText, RuleMatchListener, Mode, Level, boolean)}
   * @since 3.7
   */
  protected CheckResults performCheck(List<AnalyzedSentence> analyzedSentences, List<String> sentenceTexts,
                                         List<Rule> allRules, ParagraphHandling paraMode, AnnotatedText annotatedText, RuleMatchListener listener, Mode mode, Level level, boolean checkRemoteRules) throws IOException {
    return performCheck(analyzedSentences, sentenceTexts, RuleSet.plain(allRules), paraMode, annotatedText, listener, mode, level, checkRemoteRules);
  }

  /**
   * @since 5.2
   */
  protected CheckResults performCheck(List<AnalyzedSentence> analyzedSentences, List<String> sentenceTexts,
                                         RuleSet ruleSet, ParagraphHandling paraMode, AnnotatedText annotatedText, RuleMatchListener listener, Mode mode, Level level, boolean checkRemoteRules) throws IOException {
    List<SentenceData> sentences = computeSentenceData(analyzedSentences, sentenceTexts);
    Callable<CheckResults> matcher = new TextCheckCallable(ruleSet, sentences, paraMode, annotatedText, listener, mode, level, checkRemoteRules);
    try {
      return matcher.call();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected final List<SentenceData> computeSentenceData(List<AnalyzedSentence> analyzedSentences, List<String> texts) {
    int charCount = 0;
    int lineCount = 0;
    int columnCount = 1;
    List<SentenceData> result = new ArrayList<>(texts.size());
    for (int i = 0; i < texts.size(); i++) {
      String sentence = texts.get(i);
      result.add(new SentenceData(analyzedSentences.get(i), sentence, charCount, lineCount, columnCount));

      charCount += sentence.length();
      lineCount += countLineBreaks(sentence);
      columnCount = processColumnChange(columnCount, sentence);
    }
    return result;
  }

  private int processColumnChange(int columnCount, String sentence) {
    int lineBreakPos = sentence.lastIndexOf('\n');
    if (lineBreakPos == -1) {
      columnCount += sentence.length();
    } else {
      columnCount = sentence.length() - lineBreakPos;
      if (lineBreakPos == 0 && !language.getSentenceTokenizer().singleLineBreaksMarksPara()) {
        columnCount--;
      }
    }
    return columnCount;
  }

  /**
   * This is an internal method that's public only for technical reasons.
   *
   * @since 2.3
   * @deprecated use one of the {@link #check} methods instead.
   */
  @Deprecated
  public List<RuleMatch> checkAnalyzedSentence(ParagraphHandling paraMode,
                                               List<Rule> rules, AnalyzedSentence analyzedSentence) throws IOException {
    List<Rule> nonIgnored = rules.stream().filter(r -> !ignoreRule(r)).collect(Collectors.toList());
    return checkAnalyzedSentence(paraMode, nonIgnored, analyzedSentence, false);
  }

  /**
   * This is an internal method that's public only for technical reasons, please use one
   * of the {@link #check(String)} methods instead.
   *
   * @since 4.9
   */
  public List<RuleMatch> checkAnalyzedSentence(ParagraphHandling paraMode,
                                               List<Rule> rules, AnalyzedSentence analyzedSentence, boolean checkRemoteRules) throws IOException {
    return checkAnalyzedSentence(paraMode, rules, analyzedSentence, checkRemoteRules, -1);
  }

  private List<RuleMatch> checkAnalyzedSentence(ParagraphHandling paraMode, List<Rule> rules, AnalyzedSentence analyzedSentence, boolean checkRemoteRules, int wordCounter) throws IOException {
    if (paraMode == ParagraphHandling.ONLYPARA) {
      return Collections.emptyList();
    }
    List<RuleMatch> sentenceMatches = new ArrayList<>();
    List<String> errorRateLog = new ArrayList<>();
    float tmpErrorsPerWord = 0.0f;
    for (int i = 0, rulesSize = rules.size(); i < rulesSize; i++) {
      Rule rule = rules.get(i);
      if (rule instanceof TextLevelRule || !checkRemoteRules && rule instanceof RemoteRule) {
        continue;
      }
      if (checkCancelledCallback != null && checkCancelledCallback.checkCancelled()) {
        break;
      }
      RuleMatch[] thisMatches = rule.match(analyzedSentence);
      Collections.addAll(sentenceMatches, thisMatches);
      if (wordCounter > 0) {
        //check if the maxErrorsPerWordRate is already reached for the full text with this sentence and rule  
        float errorsPerWord = sentenceMatches.size() / (float) wordCounter;
        if (tmpErrorsPerWord < errorsPerWord) {
          errorRateLog.add("With rule: " + rule.getFullId() + " " + (i+1) + "/" + rulesSize + " the sentence error rate increased by: " + (errorsPerWord - tmpErrorsPerWord) + " from: " + tmpErrorsPerWord + " to total: " + errorsPerWord);
          tmpErrorsPerWord = errorsPerWord;
        }
        if (maxErrorsPerWordRate > 0 && errorsPerWord > maxErrorsPerWordRate && wordCounter > 25) {
          errorRateLog.forEach(e -> logger.info(LoggingTools.BAD_REQUEST, e));
          logger.info(LoggingTools.BAD_REQUEST, "ErrorRateTooHigh is reached by a single sentence after rule: " + rule.getFullId() +
            " the whole text contains " + wordCounter + " words " +
            " this sentence has " + sentenceMatches.size() + " matches");
          throw new ErrorRateTooHighException("ErrorRateTooHigh is reached by a single sentence after rule: " + rule.getFullId() +
            " the whole text contains " + wordCounter + " words" +
            " this sentence has " + sentenceMatches.size() + " matches");
        }
      }
    }
    if (sentenceMatches.isEmpty()) {
      return sentenceMatches;
    }

    AnnotatedText text = new AnnotatedTextBuilder().addText(analyzedSentence.getText()).build();
    // rules can create matches with rule IDs different from the original rule (see e.g. RemoteRules)
    // so while we can't avoid execution of these rules, we still want disabling them to work
    // so do another pass with ignoreRule here
    sentenceMatches = sentenceMatches.stream()
      .filter(match -> !ignoreRule(match.getRule())).collect(Collectors.toList());
    return applyCustomFilters(new SameRuleGroupFilter().filter(sentenceMatches), text);
  }

  private boolean ignoreRule(Rule rule) {
    Category ruleCategory = rule.getCategory();
    boolean isCategoryDisabled = (disabledRuleCategories.contains(ruleCategory.getId()) || rule.getCategory().isDefaultOff())
      && !enabledRuleCategories.contains(ruleCategory.getId());
    boolean isRuleDisabled = disabledRules.contains(rule.getFullId()) || disabledRules.contains(rule.getId())
      || (rule.isDefaultOff() && !(enabledRules.contains(rule.getFullId()) || enabledRules.contains(rule.getId())));
    boolean isDisabled;
    if (isCategoryDisabled) {
      isDisabled = !(enabledRules.contains(rule.getFullId()) || enabledRules.contains(rule.getId()));
    } else {
      isDisabled = isRuleDisabled;
    }
    return isDisabled;
  }

  /**
   * Change RuleMatch positions so they are relative to the complete text,
   * not just to the sentence.
   *
   * @param charCount   Count of characters in the sentences before
   * @param columnCount Current column number
   * @param lineCount   Current line number
   * @param sentence    The text being checked
   * @return The RuleMatch object with adjustments
   */
  public RuleMatch adjustRuleMatchPos(RuleMatch match, int charCount,
                                      int columnCount, int lineCount, String sentence, AnnotatedText annotatedText) {
    int fromPos = match.getFromPos() + charCount;
    int toPos = match.getToPos() + charCount;
    if (annotatedText != null) {
      fromPos = annotatedText.getOriginalTextPositionFor(fromPos, false);
      toPos = annotatedText.getOriginalTextPositionFor(toPos - 1, true) + 1;
    }
    RuleMatch thisMatch = new RuleMatch(match);
    thisMatch.setOffsetPosition(fromPos, toPos);

    int startPos = match.getPatternFromPos() + charCount;
    int endPos = match.getPatternToPos() + charCount;
    thisMatch.setPatternPosition(startPos, endPos);

    thisMatch.setLazySuggestedReplacements(() -> extendSuggestions(match.getSuggestedReplacementObjects()));

    String sentencePartToError = sentence.substring(0, match.getFromPos());
    String sentencePartToEndOfError = sentence.substring(0, match.getToPos());
    int lastLineBreakPos = sentencePartToError.lastIndexOf('\n');
    int column;
    int endColumn;
    if (lastLineBreakPos == -1) {
      column = sentencePartToError.length() + columnCount;
    } else {
      column = sentencePartToError.length() - lastLineBreakPos;
    }
    int lastLineBreakPosInError = sentencePartToEndOfError.lastIndexOf('\n');
    if (lastLineBreakPosInError == -1) {
      endColumn = sentencePartToEndOfError.length() + columnCount;
    } else {
      endColumn = sentencePartToEndOfError.length() - lastLineBreakPosInError;
    }
    int lineBreaksToError = countLineBreaks(sentencePartToError);
    int lineBreaksToEndOfError = countLineBreaks(sentencePartToEndOfError);
    thisMatch.setLine(lineCount + lineBreaksToError);
    thisMatch.setEndLine(lineCount + lineBreaksToEndOfError);
    thisMatch.setColumn(column);
    thisMatch.setEndColumn(endColumn);
    return thisMatch;
  }

  private List<SuggestedReplacement> extendSuggestions(List<SuggestedReplacement> replacements) {
    List<SuggestedReplacement> extended = new ArrayList<>();
    for (SuggestedReplacement replacement : replacements) {
      SuggestedReplacement newReplacement = new SuggestedReplacement(replacement);
      if (replacement.getShortDescription() == null) {  // don't overwrite more specific suggestions from the rule
        String descOrNull = descProvider.getShortDescription(replacement.getReplacement(), language);
        newReplacement.setShortDescription(descOrNull);
        newReplacement.setSuffix(replacement.getSuffix());
      }
      extended.add(newReplacement);
    }
    return extended;
  }

  protected void rememberUnknownWords(AnalyzedSentence analyzedText) {
    if (listUnknownWords) {
      AnalyzedTokenReadings[] atr = analyzedText.getTokensWithoutWhitespace();
      for (AnalyzedTokenReadings reading : atr) {
        if (!reading.isTagged()) {
          unknownWords.add(reading.getToken());
        }
      }
    }
  }

  /**
   * Get the alphabetically sorted list of unknown words in the latest run of one of the {@link #check(String)} methods.
   *
   * @throws IllegalStateException if {@link #setListUnknownWords(boolean)} has been set to {@code false}
   */
  public List<String> getUnknownWords() {
    if (!listUnknownWords) {
      throw new IllegalStateException("listUnknownWords is set to false, unknown words not stored");
    }
    List<String> words = new ArrayList<>(unknownWords);
    Collections.sort(words);
    return words;
  }

  // non-private only for test case
  static int countLineBreaks(String s) {
    int pos = -1;
    int count = 0;
    while (true) {
      int nextPos = s.indexOf('\n', pos + 1);
      if (nextPos == -1) {
        break;
      }
      pos = nextPos;
      count++;
    }
    return count;
  }

  /**
   * Tokenizes the given {@code sentence} into words and analyzes it,
   * and then disambiguates POS tags.
   *
   * @param sentence sentence to be analyzed
   */
  public AnalyzedSentence getAnalyzedSentence(String sentence) throws IOException {
    SimpleInputSentence cacheKey = new SimpleInputSentence(sentence, language);
    AnalyzedSentence cachedSentence = cache != null ? cache.getIfPresent(cacheKey) : null;
    if (cachedSentence != null) {
      return cachedSentence;
    } else {
      AnalyzedSentence raw = getRawAnalyzedSentence(sentence);
      AnalyzedSentence disambig = language.getDisambiguator().disambiguate(raw, checkCancelledCallback);
      AnalyzedSentence analyzedSentence = new AnalyzedSentence(disambig.getTokens(), raw.getTokens());
      if (language.getPostDisambiguationChunker() != null) {
        language.getPostDisambiguationChunker().addChunkTags(Arrays.asList(analyzedSentence.getTokens()));
      }
      if (cache != null) {
        cache.put(cacheKey, analyzedSentence);
      }
      return analyzedSentence;
    }
  }

  static class CleanToken {
    private final String origToken;
    private final String cleanToken;
    CleanToken(String origToken, String cleanToken) {
      this.origToken = origToken;
      this.cleanToken = cleanToken;
    }
  }

  /**
   * Tokenizes the given {@code sentence} into words and analyzes it.
   * This is the same as {@link #getAnalyzedSentence(String)} but it does not run
   * the disambiguator.
   *
   * @param sentence sentence to be analyzed
   * @since 0.9.8
   */
  public AnalyzedSentence getRawAnalyzedSentence(String sentence) throws IOException {
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    Map<Integer, CleanToken> softHyphenTokens = replaceSoftHyphens(tokens);

    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    if (language.getChunker() != null) {
      language.getChunker().addChunkTags(aTokens);
    }

    AnalyzedTokenReadings[] tokenArray = new AnalyzedTokenReadings[tokens.size() + 1];
    AnalyzedToken[] startTokenArray = new AnalyzedToken[1];
    int toArrayCount = 0;
    AnalyzedToken sentenceStartToken = new AnalyzedToken("", SENTENCE_START_TAGNAME, null);
    startTokenArray[0] = sentenceStartToken;
    tokenArray[toArrayCount++] = new AnalyzedTokenReadings(startTokenArray, 0);
    int startPos = 0;
    for (AnalyzedTokenReadings posTag : aTokens) {
      posTag.setStartPos(startPos);
      tokenArray[toArrayCount++] = posTag;
      startPos += posTag.getToken().length();
    }

    int numTokens = aTokens.size();
    int posFix = 0;
    for (int i = 0; i < numTokens; i++) {
      if (i > 0) {
        aTokens.get(i).setWhitespaceBefore(aTokens.get(i - 1).getToken());
        aTokens.get(i).setStartPos(aTokens.get(i).getStartPos() + posFix);
        aTokens.get(i).setPosFix(posFix);
      }
      if (!softHyphenTokens.isEmpty() && softHyphenTokens.get(i) != null) {
        // addReading() modifies a readings.token if last token is longer - need to use it first
        posFix += softHyphenTokens.get(i).origToken.length() - aTokens.get(i).getToken().length();
        AnalyzedToken newToken = language.getTagger().createToken(softHyphenTokens.get(i).origToken, null);
        aTokens.get(i).addReading(newToken, "softHyphenTokens");
        aTokens.get(i).setCleanToken(softHyphenTokens.get(i).cleanToken);
      }
    }

    // add additional tags
    int lastToken = toArrayCount - 1;
    // make SENT_END appear at last not whitespace token
    for (int i = 0; i < toArrayCount - 1; i++) {
      if (!tokenArray[lastToken - i].isWhitespace()) {
        lastToken -= i;
        break;
      }
    }

    tokenArray[lastToken].setSentEnd();

    if (tokenArray.length == lastToken + 1 && tokenArray[lastToken].isLinebreak()) {
      tokenArray[lastToken].setParagraphEnd();
    }
    return new AnalyzedSentence(tokenArray);
  }

  private Map<Integer, CleanToken> replaceSoftHyphens(List<String> tokens) {
    Pattern ignoredCharacterRegex = language.getIgnoredCharactersRegex();
    Map<Integer, CleanToken> ignoredCharsTokens = new HashMap<>();
    if (ignoredCharacterRegex == null) {
      return ignoredCharsTokens;
    }
    for (int i = 0; i < tokens.size(); i++) {
      Matcher matcher = ignoredCharacterRegex.matcher(tokens.get(i));
      if (matcher.find()) {
        String cleaned = matcher.replaceAll("");
        ignoredCharsTokens.put(i, new CleanToken(tokens.get(i), cleaned));
        tokens.set(i, matcher.replaceAll(""));
      }
    }
    return ignoredCharsTokens;
  }

  /**
   * Get all rule categories for the current language.
   *
   * @return a map of {@link Category Categories}, keyed by their {@link CategoryId id}.
   * @since 3.5
   */
  public Map<CategoryId, Category> getCategories() {
    Map<CategoryId, Category> map = new HashMap<>();
    for (Rule rule : getAllRules()) {
      map.put(rule.getCategory().getId(), rule.getCategory());
    }
    return map;
  }

  /**
   * Get all rules for the current language that are built-in or that have been
   * added using {@link #addRule(Rule)}. Please note that XML rules that are grouped
   * will appear as multiple rules with the same id. To tell them apart, check if
   * they are of type {@code AbstractPatternRule}, cast them to that type and call
   * their {@link AbstractPatternRule#getSubId()} method.
   *
   * @return a List of {@link Rule} objects
   */
  public List<Rule> getAllRules() {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(builtinRules);
    rules.addAll(userRules);
    return rules;
  }

  /**
   * Get all active (not disabled) rules for the current language that are built-in or that
   * have been added using e.g. {@link #addRule(Rule)}. See {@link #getAllRules()} for hints
   * about rule ids.
   *
   * @return a List of {@link Rule} objects
   */
  public List<Rule> getAllActiveRules() {
    List<Rule> rules = new ArrayList<>();
    List<Rule> rulesActive = new ArrayList<>();
    rules.addAll(builtinRules);
    rules.addAll(userRules);
    // Some rules have an internal state so they can do checks over sentence
    // boundaries. These need to be reset so the checks don't suddenly
    // work on different texts with the same data. However, it could be useful
    // to keep the state information if we're checking a continuous text.    
    for (Rule rule : rules) {
      if (!ignoreRule(rule)) {
        rulesActive.add(rule);
      }
    }
    return rulesActive;
  }

  /**
   * Get all spelling check rules for the current language that are built-in or
   * that have been added using {@link #addRule(Rule)}.
   *
   * @return a List of {@link SpellingCheckRule} objects
   * @since 5.0
   */
  public List<SpellingCheckRule> getAllSpellingCheckRules() {
    List<SpellingCheckRule> rules = new ArrayList<>();
    for (Rule rule : builtinRules) {
      if (rule instanceof SpellingCheckRule) {
        rules.add((SpellingCheckRule) rule);
      }
    }
    for (Rule rule : userRules) {
      if (rule instanceof SpellingCheckRule) {
        rules.add((SpellingCheckRule) rule);
      }
    }
    return rules;
  }

  /**
   * Works like getAllActiveRules but overrides defaults by office defaults
   *
   * @return a List of {@link Rule} objects
   * @since 4.0
   */
  public List<Rule> getAllActiveOfficeRules() {
    List<Rule> rules = new ArrayList<>();
    List<Rule> rulesActive = new ArrayList<>();
    rules.addAll(builtinRules);
    rules.addAll(userRules);
    for (Rule rule : rules) {
      if (!ignoreRule(rule) && !rule.isOfficeDefaultOff()) {
        rulesActive.add(rule);
      } else if (rule.isOfficeDefaultOn() && !disabledRules.contains(rule.getId())) {
        rulesActive.add(rule);
        enableRule(rule.getId());
      } else if (!ignoreRule(rule) && rule.isOfficeDefaultOff() && !enabledRules.contains(rule.getId())) {
        disableRule(rule.getId());
      }
    }
    return rulesActive;
  }

  /**
   * Get pattern rules by Id and SubId. This returns a list because rules that use {@code <or>...</or>}
   * are internally expanded into several rules.
   *
   * @return a List of {@link Rule} objects
   * @since 2.3
   */
  public List<AbstractPatternRule> getPatternRulesByIdAndSubId(String id, String subId) {
    List<Rule> rules = getAllRules();
    List<AbstractPatternRule> rulesById = new ArrayList<>();
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        // test wrapped rules as normal in PatternRuleTest
        if (rule instanceof RepeatedPatternRuleTransformer.RepeatedPatternRule) {
          List<AbstractPatternRule> wrappedRules = ((RepeatedPatternRuleTransformer.RepeatedPatternRule) rule).getWrappedRules();
          rulesById.addAll(wrappedRules.stream().filter(r -> r.getSubId().equals(subId)).collect(Collectors.toList()));
        } else if (rule instanceof AbstractPatternRule &&((AbstractPatternRule) rule).getSubId().equals(subId)){
          rulesById.add((AbstractPatternRule) rule);
        }
      }
    }
    return rulesById;
  }

  protected void printIfVerbose(String s) {
    if (printStream != null) {
      printStream.println(s);
    }
  }

  /**
   * Adds a temporary file to the internal list
   * (internal method, you should never need to call this as a user of LanguageTool)
   *
   * @param file the file to be added.
   */
  public static void addTemporaryFile(File file) {
    temporaryFiles.add(file);
  }

  /**
   * Clean up all temporary files, if there are any.
   */
  public static void removeTemporaryFiles() {
    for (File file : temporaryFiles) {
      file.delete();
    }
  }

  /**
   * should be called just once with complete list of matches, before returning them to caller
   *
   * @param matches matches after applying rules and default filters
   * @param text    text that matches refer to
   * @return transformed matches (after applying filters in {@link #matchFilters})
   * @since 4.7
   */
  protected List<RuleMatch> applyCustomFilters(List<RuleMatch> matches, AnnotatedText text) {
    List<RuleMatch> transformed = matches;
    for (RuleMatchFilter filter : matchFilters) {
      transformed = filter.filter(transformed, text);
    }
    return transformed;
  }

  /**
   * Callback for checking if result of {@link #check(String)} is still needed.
   */
  public interface CheckCancelledCallback {
    /**
     * @return true if request was cancelled else false
     */
    boolean checkCancelled();
  }

  static class SentenceData {
    final AnalyzedSentence analyzed;
    private final String text;
    private final int startOffset;
    private final int startLine;
    private final int startColumn;
    private final int wordCount;

    SentenceData(AnalyzedSentence analyzed, String text, int startOffset, int startLine, int startColumn) {
      this.analyzed = analyzed;
      this.text = text;
      this.startOffset = startOffset;
      this.startLine = startLine;
      this.startColumn = startColumn;
      wordCount = analyzed.getTokensWithoutWhitespace().length;
    }
  }

  class TextCheckCallable implements Callable<CheckResults> {
    private final RuleSet rules;
    private final boolean checkRemoteRules;
    private final ParagraphHandling paraMode;
    private final AnnotatedText annotatedText;
    private final List<SentenceData> sentences;
    private final RuleMatchListener listener;
    private final Mode mode;
    private final Level level;

    TextCheckCallable(RuleSet rules, List<SentenceData> sentences,
                      ParagraphHandling paraMode, AnnotatedText annotatedText,
                      RuleMatchListener listener, Mode mode, Level level, boolean checkRemoteRules) {
      this.rules = rules;
      this.checkRemoteRules = checkRemoteRules;
      this.sentences = Objects.requireNonNull(sentences);
      this.paraMode = Objects.requireNonNull(paraMode);
      this.annotatedText = Objects.requireNonNull(annotatedText);
      this.listener = listener;
      this.mode = Objects.requireNonNull(mode);
      this.level = Objects.requireNonNull(level);
    }

    @Override
    public CheckResults call() throws Exception {
      List<RuleMatch> ruleMatches = new ArrayList<>();
      List<Range> ignoreRanges = new ArrayList<>();
      if (mode == Mode.ALL) {
        ruleMatches.addAll(getTextLevelRuleMatches());
        CheckResults otherRuleMatches = getOtherRuleMatches();
        ruleMatches.addAll(otherRuleMatches.getRuleMatches());
        ignoreRanges.addAll(otherRuleMatches.getIgnoredRanges());
      } else if (mode == Mode.ALL_BUT_TEXTLEVEL_ONLY) {
        CheckResults otherRuleMatches = getOtherRuleMatches();
        ruleMatches.addAll(otherRuleMatches.getRuleMatches());
        ignoreRanges.addAll(otherRuleMatches.getIgnoredRanges());
      } else if (mode == Mode.TEXTLEVEL_ONLY) {
        ruleMatches.addAll(getTextLevelRuleMatches());
      } else {
        throw new IllegalArgumentException("Unknown mode: " + mode);
      }
      // can't call applyCustomRuleFilters here, done in performCheck ->
      // should run just once w/ complete list of matches
      return new CheckResults(ruleMatches, ignoreRanges);
    }

    private List<RuleMatch> getTextLevelRuleMatches() throws IOException {
      List<RuleMatch> ruleMatches = new ArrayList<>();
      List<AnalyzedSentence> analyzedSentences = null;
      for (Rule rule : rules.allRules()) {
        if (rule instanceof TextLevelRule && paraMode != ParagraphHandling.ONLYNONPARA) {
          if (checkCancelledCallback != null && checkCancelledCallback.checkCancelled()) {
            break;
          }
          if (analyzedSentences == null) {
            analyzedSentences = sentences.stream().map(s -> s.analyzed).collect(Collectors.toList());
          }
          RuleMatch[] matches = ((TextLevelRule) rule).match(analyzedSentences, annotatedText);
          List<RuleMatch> adaptedMatches = new ArrayList<>();
          for (RuleMatch match : matches) {
            LineColumnPosition from = findLineColumn(match.getFromPos());
            LineColumnPosition to = findLineColumn(match.getToPos());
            int newFromPos;
            int newToPos;
            try {
              newFromPos = annotatedText.getOriginalTextPositionFor(match.getFromPos(), false);
              newToPos = annotatedText.getOriginalTextPositionFor(match.getToPos() - 1, true) + 1;
            } catch (RuntimeException e) {
              throw new RuntimeException("Getting positions failed for match " + match, e);
            }
            RuleMatch newMatch = new RuleMatch(match);
            newMatch.setOffsetPosition(newFromPos, newToPos);
            newMatch.setLine(from.line);
            newMatch.setEndLine(to.line);
            newMatch.setColumn(from.column - (from.line == 0 ? 1 : 0));
            newMatch.setEndColumn(to.column - (to.line == 0 ? 1 : 0));
            newMatch.setSuggestedReplacementObjects(extendSuggestions(match.getSuggestedReplacementObjects()));
            adaptedMatches.add(newMatch);
          }
          ruleMatches.addAll(adaptedMatches);
          if (listener != null) {
            for (RuleMatch adaptedMatch : adaptedMatches) {
              listener.matchFound(adaptedMatch);
            }
          }
        }
      }
      return ruleMatches;
    }

    private CheckResults getOtherRuleMatches() {
      List<RuleMatch> ruleMatches = new ArrayList<>();
      List<Range> ignoreRanges = new ArrayList<>();
      int textWordCounter = sentences.stream().map(sentenceData -> sentenceData.wordCount).reduce(0, Integer::sum);
      int wordCounter = 0;
      float tmpErrorsPerWord = 0.0f;
      List<String> errorRateLog = new ArrayList<>();
      for (int i = 0, sentencesSize = sentences.size(); i < sentencesSize; i++) {
        SentenceData sentence = sentences.get(i);
        wordCounter += sentence.wordCount;
        try {
          //comment in to trigger an exception via input text:
          //if (analyzedSentence.getText().contains("fakecrash")) {
          //  throw new RuntimeException("fake crash");
          //}
          List<RuleMatch> sentenceMatches = null;
          InputSentence cacheKey = null;
          if (cache != null) {
            cacheKey = new InputSentence(sentence.text, language, motherTongue,
                    disabledRules, disabledRuleCategories,
                    enabledRules, enabledRuleCategories, userConfig, altLanguages, mode, level);
            sentenceMatches = cache.getIfPresent(cacheKey);
          }
          if (sentenceMatches == null) {

            List<Rule> rules = new ArrayList<>(this.rules.rulesForSentence(sentence.analyzed));
            rules.addAll(userConfig.getRules());
            sentenceMatches = checkAnalyzedSentence(paraMode, rules, sentence.analyzed, checkRemoteRules, textWordCounter);
          }
          if (cache != null) {
            cache.put(cacheKey, sentenceMatches);
          }
          if (!sentenceMatches.isEmpty()) {
            if (checkCancelledCallback != null && checkCancelledCallback.checkCancelled()) {
              break;
            }
            for (RuleMatch elem : sentenceMatches) {
              RuleMatch thisMatch = adjustRuleMatchPos(elem, sentence.startOffset, sentence.startColumn, sentence.startLine, sentence.text, annotatedText);
              if (elem.getErrorLimitLang() != null) {
                Range ignoreRange = new Range(sentence.startOffset, sentence.startOffset + sentence.text.length(), elem.getErrorLimitLang());
                if (!ignoreRanges.contains(ignoreRange)) {
                  ignoreRanges.add(ignoreRange);
                }
              }
              ruleMatches.add(thisMatch);
              if (listener != null) {
                listener.matchFound(thisMatch);
              }
            }
          }
          float errorsPerWord = ruleMatches.size() / (float) wordCounter;
          if (tmpErrorsPerWord < errorsPerWord) {
            errorRateLog.add("With sentence: " + (i + 1) + " (of " + sentencesSize + ") the text error rate increased by: " + (errorsPerWord - tmpErrorsPerWord) + " from: " + tmpErrorsPerWord  + " to total: " + errorsPerWord);
            tmpErrorsPerWord = errorsPerWord;
          }
          if (maxErrorsPerWordRate > 0 && errorsPerWord > maxErrorsPerWordRate && wordCounter > 25) {
            errorRateLog.forEach(e -> logger.info(LoggingTools.BAD_REQUEST, e));
            throw new ErrorRateTooHighException("Text checking was stopped due to too many errors (more than " + String.format("%.0f", maxErrorsPerWordRate * 100) +
              "% of words seem to have an error). Are you sure you have set the correct text language? Language set: " + JLanguageTool.this.language.getName() +
              ", text length: " + annotatedText.getPlainText().length());
            //        ", text length: " + annotatedText.getPlainText().length() + ", common word count: " + commonWords.getKnownWordsPerLanguage(annotatedText.getPlainText()));
          }
        } catch (ErrorRateTooHighException e) {
          throw e;
        } catch (StackOverflowError e) {
          System.out.println("Could not check sentence due to StackOverflowError (language: " + language + "): <sentcontent>"
                  + StringUtils.abbreviate(sentence.analyzed.toTextString(), 10_000) + "</sentcontent>");
          throw e;
        } catch (Exception e) {
          throw new RuntimeException("Could not check sentence (language: " + language + "): <sentcontent>"
                  + StringUtils.abbreviate(sentence.analyzed.toTextString(), 500) + "</sentcontent>", e);
        }
      }
      return new CheckResults(ruleMatches, ignoreRanges);
    }

    private LineColumnPosition findLineColumn(int offset) {
      if (sentences.isEmpty()) return new LineColumnPosition(0, 0);

      SentenceData sentence = findSentenceContaining(offset);
      String prefix = sentence.text.substring(0, offset - sentence.startOffset);
      return new LineColumnPosition(
        sentence.startLine + countLineBreaks(prefix),
        processColumnChange(sentence.startColumn, prefix));
    }

    private SentenceData findSentenceContaining(int offset) {
      int low = 0;
      int high = sentences.size() - 1;
      while (low <= high) {
        int mid = (low + high) / 2;
        SentenceData sentence = sentences.get(mid);
        if (sentence.startOffset < offset) low = mid + 1;
        else if (sentence.startOffset > offset) high = mid - 1;
        else return sentence;
      }
      return sentences.get(low - 1);
    }

    private class LineColumnPosition {
      int line;
      int column;

      private LineColumnPosition(int line, int column) {
        this.line = line;
        this.column = column;
      }
    }
  }

  public void setConfigValues(Map<String, Integer> v) {
    userConfig.insertConfigValues(v);
  }

}
