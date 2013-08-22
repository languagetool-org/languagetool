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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;

import javax.xml.parsers.ParserConfigurationException;

import org.languagetool.chunking.Chunker;
import org.languagetool.databroker.DefaultResourceDataBroker;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Category;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.RuleMatchFilter;
import org.languagetool.rules.SameRuleGroupFilter;
import org.languagetool.rules.patterns.FalseFriendRuleLoader;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.SuggestionExtractor;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.Tokenizer;
import org.xml.sax.SAXException;

/**
 * The main class used for checking text against different rules:
 * <ul>
 * <li>the built-in Java rules (for English: <i>a</i> vs. <i>an</i>, whitespace after commas, ...)
 * <li>pattern rules loaded from external XML files with
 * {@link #loadPatternRules(String)}
 * <li>your own implementation of the abstract {@link Rule} classes added with
 * {@link #addRule(Rule)}
 * </ul>
 * 
 * <p>Note that the constructors create a language checker that uses the built-in
 * Java rules only. Other rules (e.g. from XML) need to be added explicitly or
 * activated using {@link #activateDefaultPatternRules()}.
 * 
 * @see MultiThreadedJLanguageTool
 */
@SuppressWarnings({"UnusedDeclaration"})
public class JLanguageTool {

  public static final String VERSION = "2.3-SNAPSHOT";
  public static final String BUILD_DATE = getBuildDate();

  public static final String PATTERN_FILE = "grammar.xml";
  public static final String FALSE_FRIEND_FILE = "false-friends.xml";
  public static final String SENTENCE_START_TAGNAME = "SENT_START";

  public static final String SENTENCE_END_TAGNAME = "SENT_END";
  public static final String PARAGRAPH_END_TAGNAME = "PARA_END";
  public static final String MESSAGE_BUNDLE = "org.languagetool.MessagesBundle";

  /**
   * Returns the build date or <code>null</code> if not run from JAR.
   */
  private static String getBuildDate() {
    try {
      final URL res = JLanguageTool.class.getResource(JLanguageTool.class.getSimpleName() + ".class");
      final Object connObj = res.openConnection();
      if (connObj instanceof JarURLConnection) {
        final JarURLConnection conn = (JarURLConnection) connObj;
        final Manifest manifest = conn.getManifest();
        return manifest.getMainAttributes().getValue("Implementation-Date");
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not get build date from JAR", e);
    }
  }
  
  private static ResourceDataBroker dataBroker = new DefaultResourceDataBroker();

  private final List<Rule> builtinRules = new ArrayList<>();
  private final List<Rule> userRules = new ArrayList<>(); // rules added via addRule() method
  private final Set<String> disabledRules = new HashSet<>();
  private final Set<String> enabledRules = new HashSet<>();
  private final Set<String> disabledCategories = new HashSet<>();

  private Language language;
  private Language motherTongue;
  private Disambiguator disambiguator;
  private Tagger tagger;
  private Tokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Chunker chunker;

  private PrintStream printStream;

  private int sentenceCount;

  private boolean listUnknownWords;
  private Set<String> unknownWords;  

  /**
   * Constants for correct paragraph-rule handling:
   * <ul>
   * <li>NORMAL -  all kinds of rules run</li>
   * <li>ONLYPARA - only paragraph-level rules</li>
   * <li>ONLYNONPARA - only sentence-level rules</li></ul>
   */
  public static enum ParagraphHandling {
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
  
  private static List<File> temporaryFiles = new ArrayList<>();
  
  /**
   * Create a JLanguageTool and setup the built-in Java rules for the
   * given language, ignoring XML-based rules and false friend rules.
   *
   * @param language the language of the text to be checked
   */
  public JLanguageTool(final Language language) throws IOException {
    this(language, null);
  }

  /**
   * Create a JLanguageTool and setup the built-in Java rules for the
   * given language, ignoring XML-based rules except false friend rules.
   * 
   * @param language the language of the text to be checked
   * @param motherTongue the user's mother tongue, used for false friend rules, or <code>null</code>.
   *          The mother tongue may also be used as a source language for checking bilingual texts.
   */
  public JLanguageTool(final Language language, final Language motherTongue)
      throws IOException {
    this.language = Objects.requireNonNull(language, "language cannot be null");
    this.motherTongue = motherTongue;
    final ResourceBundle messages = getMessageBundle(language);
    final Rule[] allBuiltinRules = getAllBuiltinRules(language, messages);
    for (final Rule element : allBuiltinRules) {
      if (element.supportsLanguage(language)) {
        builtinRules.add(element);
      }
    }
    disambiguator = language.getDisambiguator();
    tagger = language.getTagger();
    sentenceTokenizer = language.getSentenceTokenizer();
    wordTokenizer = language.getWordTokenizer();
    chunker = language.getChunker();
  }
  
  /**
   * The grammar checker needs resources from following
   * directories:
   * <ul>
   * <li>{@code /resource}</li>
   * <li>{@code /rules}</li>
   * </ul>
   * This method is thread-safe.
   * 
   * @return The currently set data broker which allows to obtain
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
   * This method is thread-safe.
   * 
   * @param broker The new resource broker to be used.
   * @since 1.0.1
   */
  public static synchronized void setDataBroker(ResourceDataBroker broker) {
    JLanguageTool.dataBroker = broker;
  }

  /**
   * Whether the {@link #check(String)} methods store unknown words. If set to
   * <code>true</code> (default: false), you can get the list of unknown words
   * using {@link #getUnknownWords()}.
   */
  public void setListUnknownWords(final boolean listUnknownWords) {
    this.listUnknownWords = listUnknownWords;
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the default language of the user's system.
   */
  public static ResourceBundle getMessageBundle() {
    try {
      final ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE);
      final ResourceBundle fallbackBundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
      return new ResourceBundleWithFallback(bundle, fallbackBundle);
    } catch (final MissingResourceException e) {
      return ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
    }
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the given user interface language.
   */
  static ResourceBundle getMessageBundle(final Language lang) {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, lang.getLocaleWithCountry());
      if (!isValidBundleFor(lang, bundle)) {
        bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, lang.getLocale());
        if (!isValidBundleFor(lang, bundle)) {
          // happens if 'xx' is requested but only a MessagesBundle_xx_YY.properties exists:
          final Language defaultVariant = lang.getDefaultVariant();
          if (defaultVariant != null && defaultVariant.getCountryVariants().length > 0) {
            final Locale locale = new Locale(defaultVariant.getShortName(), defaultVariant.getCountryVariants()[0]);
            bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, locale);
          }
        }
      }
      final ResourceBundle fallbackBundle = ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
      return new ResourceBundleWithFallback(bundle, fallbackBundle);
    } catch (final MissingResourceException e) {
      return ResourceBundle.getBundle(MESSAGE_BUNDLE, Locale.ENGLISH);
    }
  }

  private static boolean isValidBundleFor(final Language lang, final ResourceBundle bundle) {
    return lang.getLocale().getLanguage().equals(bundle.getLocale().getLanguage());
  }

  private Rule[] getAllBuiltinRules(final Language language, final ResourceBundle messages) {
    final List<Rule> rules = new ArrayList<>();
    final List<Class<? extends Rule>> languageRules = language.getRelevantRules();
    for (Class<? extends Rule> ruleClass : languageRules) {
      final Constructor[] constructors = ruleClass.getConstructors();
      try {
        if (constructors.length > 0) {
          final Constructor constructor = constructors[0];
          final Class[] paramTypes = constructor.getParameterTypes();
          if (paramTypes.length == 1
              && paramTypes[0].equals(ResourceBundle.class)) {
            rules.add((Rule) constructor.newInstance(messages));
          } else if (paramTypes.length == 2
              && paramTypes[0].equals(ResourceBundle.class)
              && paramTypes[1].equals(Language.class)) {
            rules.add((Rule) constructor.newInstance(messages, language));
          } else {
            throw new RuntimeException("No matching constructor found for rule class: " + ruleClass.getName());            
          }
        } else {
          throw new RuntimeException("No public constructor for rule class: " + ruleClass.getName());
        }
      } catch (Exception e) {
        throw new RuntimeException("Failed to load built-in Java rules for language " + language, e);
      }
    }
    return rules.toArray(new Rule[rules.size()]);
  }

  /**
   * Set a PrintStream that will receive verbose output. Set to
   * <code>null</code> (which is the default) to disable verbose output.
   */
  public void setOutput(final PrintStream printStream) {
    this.printStream = printStream;
  }

  /**
   * Load pattern rules from an XML file. Use {@link #addRule(Rule)} to add these
   * rules to the checking process.
   *
   * @param filename path to an XML file in the classpath or in the filesystem - the classpath is checked first
   * @return a List of {@link PatternRule} objects
   */
  public List<PatternRule> loadPatternRules(final String filename) throws IOException {
    final PatternRuleLoader ruleLoader = new PatternRuleLoader();
    final InputStream is = this.getClass().getResourceAsStream(filename);
    if (is == null) {
      // happens for external rules plugged in as an XML file:
      return ruleLoader.getRules(new File(filename));
    } else {
      return ruleLoader.getRules(is, filename);
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
  public List<PatternRule> loadFalseFriendRules(final String filename)
      throws ParserConfigurationException, SAXException, IOException {
    if (motherTongue == null) {
      return new ArrayList<>();
    }
    final FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader();
    final InputStream is = this.getClass().getResourceAsStream(filename);
    if (is == null) {
      return ruleLoader.getRules(new File(filename), language, motherTongue);
    } else {
      return ruleLoader.getRules(is, language, motherTongue);
    }
  }

  /**
   * Loads and activates the pattern rules from
   * <code>org/languagetool/rules/&lt;languageCode&gt;/grammar.xml</code>.
   */
  public void activateDefaultPatternRules() throws IOException {
    final List<PatternRule> patternRules = new ArrayList<>();
    for (String patternRuleFileName : language.getRuleFileNames()) {
      patternRules.addAll(loadPatternRules(patternRuleFileName));
    }    
    userRules.addAll(patternRules);
  }

  /**
   * Loads and activates the false friend rules from
   * <code>rules/false-friends.xml</code>.
   */
  public void activateDefaultFalseFriendRules()
      throws ParserConfigurationException, SAXException, IOException {
    final String falseFriendRulesFilename = JLanguageTool.getDataBroker().getRulesDir() + "/" + FALSE_FRIEND_FILE;
    final List<PatternRule> patternRules = loadFalseFriendRules(falseFriendRulesFilename);
    userRules.addAll(patternRules);
  }

  /**
   * Add a rule to be used by the next call to the check methods like {@link #check(String)}.
   */
  public void addRule(final Rule rule) {
    userRules.add(rule);
    final SuggestionExtractor extractor = new SuggestionExtractor();
    final List<String> suggestionTokens = extractor.getSuggestionTokens(rule, language);
    final List<Rule> allActiveRules = getAllActiveRules();
    addIgnoreWords(suggestionTokens, allActiveRules);
  }

  private void addIgnoreWords(List<String> ignoreWords, List<Rule> allActiveRules) {
    for (Rule activeRule : allActiveRules) {
      if (activeRule instanceof SpellingCheckRule) {
        ((SpellingCheckRule)activeRule).addIgnoreTokens(ignoreWords);
      }
    }
  }

  private void setIgnoreWords(List<String> ignoreWords, List<Rule> allActiveRules) {
    for (Rule activeRule : allActiveRules) {
      if (activeRule instanceof SpellingCheckRule) {
        ((SpellingCheckRule)activeRule).resetIgnoreTokens();
        ((SpellingCheckRule)activeRule).addIgnoreTokens(ignoreWords);
      }
    }
  }

  /**
   * Disable a given rule so the check methods like {@link #check(String)} won't use it.
   * 
   * @param ruleId the id of the rule to disable - no error will be thrown if the id does not exist
   */
  public void disableRule(final String ruleId) {
    disabledRules.add(ruleId);
    reInitSpellCheckIgnoreWords();
  }

  private void reInitSpellCheckIgnoreWords() {
    final List<Rule> allActiveRules = getAllActiveRules();
    final List<String> ignoreTokens = getAllIgnoreWords(allActiveRules);
    setIgnoreWords(ignoreTokens, allActiveRules);
  }

  private List<String> getAllIgnoreWords(List<Rule> allActiveRules) {
    final List<String> suggestionTokens = new ArrayList<>();
    for (Rule activeRule : allActiveRules) {
      if (activeRule instanceof PatternRule) {
        final SuggestionExtractor extractor = new SuggestionExtractor();
        suggestionTokens.addAll(extractor.getSuggestionTokens(activeRule, language));
      }
    }
    return suggestionTokens;
  }

  /**
   * Disable the given rule category so the check methods like {@link #check(String)} won't use it.
   * 
   * @param categoryName the id of the category to disable - no error will be thrown if the id does not exist
   */
  public void disableCategory(final String categoryName) {
    disabledCategories.add(categoryName);
    reInitSpellCheckIgnoreWords();
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
   * Enable a rule that is switched off by default ({@code default="off"} in the XML).
   * 
   * @param ruleId the id of the turned off rule to enable.
   */
  public void enableDefaultOffRule(final String ruleId) {
    enabledRules.add(ruleId);
  }

  /**
   * Get category ids of the rule categories that have been explicitly disabled.
   */
  public Set<String> getDisabledCategories() {
    return disabledCategories;
  }

  /**
   * Re-enable a given rule so the check methods like {@link #check(String)} will use it.
   * Note that you need to use {@link #enableDefaultOffRule(String)} for rules that
   * are off by default.
   * 
   * @param ruleId the id of the rule to enable
   */
  public void enableRule(final String ruleId) {
    if (disabledRules.contains(ruleId)) {
      disabledRules.remove(ruleId);
    }
  }

  /**
   * Tokenizes the given text into sentences.
   */
  public List<String> sentenceTokenize(final String text) {
    return sentenceTokenizer.tokenize(text);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   * 
   * @param text the text to be checked
   * @return a List of {@link RuleMatch} objects
   */
  public List<RuleMatch> check(final String text) throws IOException {
    return check(text, true, ParagraphHandling.NORMAL);
  }
  
  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   * 
   * @param text The text to be checked. Call this method with the complete text to be checked. If you call it
   *          repeatedly with smaller chunks like paragraphs or sentence, those rules that work across
   *          paragraphs/sentences won't work (their status gets reset whenever this method is called).
   * @param tokenizeText If true, then the text is tokenized into sentences.
   *          Otherwise, it is assumed it's already tokenized.
   * @param paraMode Uses paragraph-level rules only if true.
   * @return a List of {@link RuleMatch} objects, describing potential errors in the text
   */
  public List<RuleMatch> check(final String text, boolean tokenizeText, final ParagraphHandling paraMode) throws IOException {
    final List<String> sentences;
    if (tokenizeText) { 
      sentences = sentenceTokenize(text);
    } else {
      sentences = new ArrayList<>();
      sentences.add(text);
    }
    final List<Rule> allRules = getAllRules();
    printIfVerbose(allRules.size() + " rules activated for language " + language);

    sentenceCount = sentences.size();
    unknownWords = new HashSet<>();
    final List<AnalyzedSentence> analyzedSentences = this.analyzeSentences(sentences);    
    
    List<RuleMatch> ruleMatches = performCheck(analyzedSentences, sentences, allRules, paraMode);
    
    if (!ruleMatches.isEmpty() && !paraMode.equals(ParagraphHandling.ONLYNONPARA)) {
      // removing false positives in paragraph-level rules
      for (final Rule rule : allRules) {
        if (rule.isParagraphBackTrack() && (rule.getMatches() != null)) {
          final List<RuleMatch> rm = rule.getMatches();
          for (final RuleMatch r : rm) {
            if (rule.isInRemoved(r)) {
              ruleMatches.remove(r);
            }
          }
        }
      }
    }

    Collections.sort(ruleMatches);
    return ruleMatches;
  }
  
  private List<AnalyzedSentence> analyzeSentences(List<String> sentences) throws IOException {
    final List<AnalyzedSentence> analyzedSentences = new ArrayList<>();
    
    int j = 0;
    for (final String sentence : sentences) {
      AnalyzedSentence analyzedSentence = getAnalyzedSentence(sentence);
      rememberUnknownWords(analyzedSentence);
      if (++j == sentences.size()) {
        final AnalyzedTokenReadings[] anTokens = analyzedSentence.getTokens();
        anTokens[anTokens.length - 1].setParaEnd();
        analyzedSentence = new AnalyzedSentence(anTokens);
      }
      analyzedSentences.add(analyzedSentence);
      printIfVerbose(analyzedSentence.toString());
      printIfVerbose(analyzedSentence.getAnnotations());
    }
    
    return analyzedSentences;
  }
  
  protected List<RuleMatch> performCheck(List<AnalyzedSentence> analyzedSentences, List<String> sentences, final List<Rule> allRules, ParagraphHandling paraMode) throws IOException {
    Callable<List<RuleMatch>> matcher = new TextCheckCallable(allRules, sentences, analyzedSentences, paraMode, 0, 0, 1);
    try {
      return matcher.call();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<RuleMatch> checkAnalyzedSentence(final ParagraphHandling paraMode,
      final List<Rule> allRules, int tokenCount, int lineCount,
      int columnCount, final String sentence, AnalyzedSentence analyzedSentence)
        throws IOException {
    final List<RuleMatch> sentenceMatches = new ArrayList<>();
    for (final Rule rule : allRules) {
      if (disabledRules.contains(rule.getId())
          || (rule.isDefaultOff() && !enabledRules.contains(rule.getId()))) {
        continue;
      }

      final Category category = rule.getCategory();
      if (category != null && disabledCategories.contains(category.getName())) {
        continue;
      }
      
      switch (paraMode) {
        case ONLYNONPARA: {
          if (rule.isParagraphBackTrack()) {
            continue;
          }
          break;
        }
        case ONLYPARA: {
          if (!rule.isParagraphBackTrack()) {
            continue;
          }
         break;
        }
        case NORMAL:
        default:
      }

      final RuleMatch[] thisMatches = rule.match(analyzedSentence);
      for (final RuleMatch element1 : thisMatches) {
        final RuleMatch thisMatch = adjustRuleMatchPos(element1,
            tokenCount, columnCount, lineCount, sentence);    
        sentenceMatches.add(thisMatch);
        if (rule.isParagraphBackTrack()) {
          rule.addRuleMatch(thisMatch);
        }
      }
    }
    final RuleMatchFilter filter = new SameRuleGroupFilter();
    return filter.filter(sentenceMatches);
  }

  /**
   * Change RuleMatch positions so they are relative to the complete text,
   * not just to the sentence: 
   * @param match RuleMatch
   * @param sentLen Count of characters
   * @param columnCount Current column number
   * @param lineCount Current line number
   * @param sentence The text being checked
   * @return The RuleMatch object with adjustments.
   */
  public RuleMatch adjustRuleMatchPos(final RuleMatch match, int sentLen,
      int columnCount, int lineCount, final String sentence) {
    final RuleMatch thisMatch = new RuleMatch(match.getRule(),
        match.getFromPos() + sentLen, match.getToPos() + sentLen, match.getMessage(), match.getShortMessage());
    thisMatch.setSuggestedReplacements(match.getSuggestedReplacements());
    final String sentencePartToError = sentence.substring(0, match.getFromPos());
    final String sentencePartToEndOfError = sentence.substring(0,match.getToPos());
    final int lastLineBreakPos = sentencePartToError.lastIndexOf('\n');
    final int column;
    final int endColumn;
    if (lastLineBreakPos == -1) {
      column = sentencePartToError.length() + columnCount;
    } else {
      column = sentencePartToError.length() - lastLineBreakPos;
    }
    final int lastLineBreakPosInError = sentencePartToEndOfError.lastIndexOf('\n');
    if (lastLineBreakPosInError == -1) {
      endColumn = sentencePartToEndOfError.length() + columnCount;
    } else {
      endColumn = sentencePartToEndOfError.length() - lastLineBreakPosInError;
    }
    final int lineBreaksToError = countLineBreaks(sentencePartToError);
    final int lineBreaksToEndOfError = countLineBreaks(sentencePartToEndOfError);
    thisMatch.setLine(lineCount + lineBreaksToError);
    thisMatch.setEndLine(lineCount + lineBreaksToEndOfError);
    thisMatch.setColumn(column);
    thisMatch.setEndColumn(endColumn);
    thisMatch.setOffset(match.getFromPos() + sentLen);
    return thisMatch;
  }

  protected void rememberUnknownWords(final AnalyzedSentence analyzedText) {
    if (listUnknownWords) {
      final AnalyzedTokenReadings[] atr = analyzedText
          .getTokensWithoutWhitespace();
      for (final AnalyzedTokenReadings reading : atr) {
        if (reading.getReadings().toString().contains("null]")) {
          unknownWords.add(reading.getToken());
        }
      }
    }
  }

  /**
   * Get the alphabetically sorted list of unknown words in the latest run of one of the {@link #check(String)} methods.
   * 
   * @throws IllegalStateException if {@link #setListUnknownWords(boolean)} has been set to <code>false</code>
   */
  public List<String> getUnknownWords() {
    if (!listUnknownWords) {
      throw new IllegalStateException("listUnknownWords is set to false, unknown words not stored");
    }
    final List<String> words = new ArrayList<>(unknownWords);
    Collections.sort(words);
    return words;
  }

  // non-private only for test case
  static int countLineBreaks(final String s) {
    int pos = -1;
    int count = 0;
    while (true) {
      final int nextPos = s.indexOf('\n', pos + 1);
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
  public AnalyzedSentence getAnalyzedSentence(final String sentence) throws IOException {
    return disambiguator.disambiguate(getRawAnalyzedSentence(sentence));
  }

  /**
   * Tokenizes the given {@code sentence} into words and analyzes it.
   * This is the same as {@link #getAnalyzedSentence(String)} but it does not run
   * the disambiguator.
   * 
   * @param sentence sentence to be analyzed
   * @since 0.9.8
   */
  public AnalyzedSentence getRawAnalyzedSentence(final String sentence) throws IOException {
    final List<String> tokens = wordTokenizer.tokenize(sentence);
    final Map<Integer, String> softHyphenTokens = new HashMap<>();

    //for soft hyphens inside words, happens especially in OOo:
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i).indexOf('\u00ad') != -1) {
        softHyphenTokens.put(i, tokens.get(i));
        tokens.set(i, tokens.get(i).replaceAll("\u00ad", ""));
      }
    }
    
    final List<AnalyzedTokenReadings> aTokens = tagger.tag(tokens);
    if (chunker != null) {
      chunker.addChunkTags(aTokens);
    }
    final int numTokens = aTokens.size();
    int posFix = 0; 
    for (int i = 1; i < numTokens; i++) {
      aTokens.get(i).setWhitespaceBefore(aTokens.get(i - 1).isWhitespace());
      aTokens.get(i).setStartPos(aTokens.get(i).getStartPos() + posFix);
      if (!softHyphenTokens.isEmpty()) {
        if (softHyphenTokens.get(i) != null) {
          aTokens.get(i).addReading(tagger.createToken(softHyphenTokens.get(i), null));
          posFix += softHyphenTokens.get(i).length() - aTokens.get(i).getToken().length();
        }
      }
    }
        
    final AnalyzedTokenReadings[] tokenArray = new AnalyzedTokenReadings[tokens.size() + 1];
    final AnalyzedToken[] startTokenArray = new AnalyzedToken[1];
    int toArrayCount = 0;
    final AnalyzedToken sentenceStartToken = new AnalyzedToken("", SENTENCE_START_TAGNAME, null);
    startTokenArray[0] = sentenceStartToken;
    tokenArray[toArrayCount++] = new AnalyzedTokenReadings(startTokenArray, 0);
    int startPos = 0;
    for (final AnalyzedTokenReadings posTag : aTokens) {
      posTag.setStartPos(startPos);
      tokenArray[toArrayCount++] = posTag;
      startPos += posTag.getToken().length();
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
      tokenArray[lastToken].setParaEnd();
    }
    return new AnalyzedSentence(tokenArray);
  }
  
  /**
   * Get all rules for the current language that are built-in or that have been
   * added using {@link #addRule(Rule)}.
   * @return a List of {@link Rule} objects
   */
  public List<Rule> getAllRules() {
    final List<Rule> rules = new ArrayList<>();
    rules.addAll(builtinRules);
    rules.addAll(userRules);
    // Some rules have an internal state so they can do checks over sentence
    // boundaries. These need to be reset so the checks don't suddenly
    // work on different texts with the same data. However, it could be useful
    // to keep the state information if we're checking a continuous text.    
    for (final Rule rule : rules) {
      rule.reset();
    }
    return rules;
  }
  
  /**
   * Get all active (not disabled) rules for the current language that are built-in or that 
   * have been added using e.g. {@link #addRule(Rule)}.
   * @return a List of {@link Rule} objects
   */
  public List<Rule> getAllActiveRules() {
    final List<Rule> rules = new ArrayList<>();
    final List<Rule> rulesActive = new ArrayList<>();
    rules.addAll(builtinRules);
    rules.addAll(userRules);
    // Some rules have an internal state so they can do checks over sentence
    // boundaries. These need to be reset so the checks don't suddenly
    // work on different texts with the same data. However, it could be useful
    // to keep the state information if we're checking a continuous text.    
    for (final Rule rule : rules) {
      rule.reset();
      if (!disabledRules.contains(rule.getId())) {
        rulesActive.add(rule);
      }
    }    
    return rulesActive;
  }

  /**
   * Number of sentences the latest call to a check method like {@link #check(String)} has checked.
   */
  public int getSentenceCount() {
    return sentenceCount;
  }

  protected void printIfVerbose(final String s) {
    if (printStream != null) {
      printStream.println(s);
    }
  }
  
  /**
   * Adds a temporary file to the internal list
   * @param file the file to be added.
   */
  public static void addTemporaryFile(final File file) {
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

  class TextCheckCallable implements Callable<List<RuleMatch>> {

    private final List<Rule> rules;
    private final ParagraphHandling paraMode;
    
    private List<String> sentences;
    private List<AnalyzedSentence> analyzedSentences;
    private int charCount;
    private int lineCount;
    private int columnCount;

    TextCheckCallable(List<Rule> rules, List<String> sentences, List<AnalyzedSentence> analyzedSentences,
                      ParagraphHandling paraMode, int charCount, int lineCount, int columnCount) {
      this.rules = rules;
      if (sentences.size() != analyzedSentences.size()) {
        throw new IllegalArgumentException("sentences and analyzedSentences do not have the same length : " + sentences.size() + " != " + analyzedSentences.size());
      }
      this.sentences = sentences;
      this.analyzedSentences = analyzedSentences;
      this.paraMode = paraMode;
      this.charCount = charCount;
      this.lineCount = lineCount;
      this.columnCount = columnCount;
    }

    @Override
    public List<RuleMatch> call() throws Exception {
      final List<RuleMatch> ruleMatches = new ArrayList<>();
      int i = 0;
      for (final AnalyzedSentence analyzedSentence : analyzedSentences) {
        final String sentence = sentences.get(i++);
        final List<RuleMatch> sentenceMatches =
                checkAnalyzedSentence(paraMode, rules, charCount, lineCount,
                        columnCount, sentence, analyzedSentence);

        ruleMatches.addAll(sentenceMatches);
        charCount += sentence.length();
        lineCount += countLineBreaks(sentence);

        // calculate matching column:
        final int lineBreakPos = sentence.lastIndexOf('\n');
        if (lineBreakPos == -1) {
          columnCount += sentence.length();
        } else {
          if (lineBreakPos == 0) {
            columnCount = sentence.length();
            if (!language.getSentenceTokenizer().singleLineBreaksMarksPara()) {
              columnCount--;
            }
          } else {
            columnCount = sentence.length() - lineBreakPos;
          }
        }
      }
      return ruleMatches;
    }
  }

}
