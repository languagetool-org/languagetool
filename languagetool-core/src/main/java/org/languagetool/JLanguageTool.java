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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.databroker.DefaultResourceDataBroker;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.FalseFriendRuleLoader;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

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

  /** LanguageTool version as a string like {@code 2.3} or {@code 2.4-SNAPSHOT}. */
  public static final String VERSION = "3.9-SNAPSHOT";
  /** LanguageTool build date and time like {@code 2013-10-17 16:10} or {@code null} if not run from JAR. */
  @Nullable public static final String BUILD_DATE = getBuildDate();

  /** The name of the file with error patterns. */
  public static final String PATTERN_FILE = "grammar.xml";
  /** The name of the file with false friend information. */
  public static final String FALSE_FRIEND_FILE = "false-friends.xml";
  /** The internal tag used to mark the beginning of a sentence. */
  public static final String SENTENCE_START_TAGNAME = "SENT_START";
  /** The internal tag used to mark the end of a sentence. */
  public static final String SENTENCE_END_TAGNAME = "SENT_END";
  /** The internal tag used to mark the end of a paragraph. */
  public static final String PARAGRAPH_END_TAGNAME = "PARA_END";
  /** Name of the message bundle for translations. */
  public static final String MESSAGE_BUNDLE = "org.languagetool.MessagesBundle";

  private final ResultCache cache;

  /**
   * Returns the build date or {@code null} if not run from JAR.
   */
  @Nullable
  private static String getBuildDate() {
    try {
      URL res = JLanguageTool.class.getResource(JLanguageTool.class.getSimpleName() + ".class");
      if (res == null) {
        // this will happen on Android, see http://stackoverflow.com/questions/15371274/
        return null;
      }
      Object connObj = res.openConnection();
      if (connObj instanceof JarURLConnection) {
        JarURLConnection conn = (JarURLConnection) connObj;
        Manifest manifest = conn.getManifest();
        return manifest.getMainAttributes().getValue("Implementation-Date");
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not get build date from JAR", e);
    }
  }
  
  private static ResourceDataBroker dataBroker = new DefaultResourceDataBroker();

  private final List<Rule> builtinRules;
  private final List<Rule> userRules = new ArrayList<>(); // rules added via addRule() method
  private final Set<String> disabledRules = new HashSet<>();
  private final Set<CategoryId> disabledRuleCategories = new HashSet<>();
  private final Set<String> enabledRules = new HashSet<>();
  private final Set<CategoryId> enabledRuleCategories = new HashSet<>();
  private final Language language;
  private final Language motherTongue;

  private PrintStream printStream;
  private boolean listUnknownWords;
  private Set<String> unknownWords;
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
  
  private static final List<File> temporaryFiles = new ArrayList<>();

  /**
   * Create a JLanguageTool and setup the built-in rules for the
   * given language and false friend rules for the text language / mother tongue pair.
   *
   * @param lang the language of the text to be checked
   * @param motherTongue the user's mother tongue, used for false friend rules, or <code>null</code>.
   *          The mother tongue may also be used as a source language for checking bilingual texts.
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
    this(language, null, null);
  }

  /**
   * Create a JLanguageTool and setup the built-in rules for the
   * given language and false friend rules for the text language / mother tongue pair.
   * 
   * @param language the language of the text to be checked
   * @param motherTongue the user's mother tongue, used for false friend rules, or <code>null</code>.
   *          The mother tongue may also be used as a source language for checking bilingual texts.
   * @param cache a cache to speed up checking if the same sentences get checked more than once,
   *              e.g. when LT is running as a server and texts are re-checked due to changes
   * @since 3.7
   */
  @Experimental
  public JLanguageTool(Language language, Language motherTongue, ResultCache cache) {
    this.language = Objects.requireNonNull(language, "language cannot be null");
    this.motherTongue = motherTongue;
    ResourceBundle messages = ResourceBundleTools.getMessageBundle(language);
    builtinRules = getAllBuiltinRules(language, messages);
    this.cleanOverlappingMatches = true;
    try {
      activateDefaultPatternRules();
      activateDefaultFalseFriendRules();
    } catch (Exception e) {
      throw new RuntimeException("Could not activate rules", e);
    }
    this.cache = cache;
  }
  
  /**
   * The grammar checker needs resources from following
   * directories:
   * <ul>
   *   <li>{@code /resource}</li>
   *   <li>{@code /rules}</li>
   * </ul>
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
  public void setListUnknownWords(boolean listUnknownWords) {
    this.listUnknownWords = listUnknownWords;
  }
  
  /**
   * Whether the {@link #check(String)} methods return overlapping errors. If set to
   * <code>true</code> (default: true), it removes overlapping errors according to 
   * the priorities established for the language. 
   * @since 3.6
   */
  public void setCleanOverlappingMatches(boolean cleanOverlappingMatches) {
    this.cleanOverlappingMatches = cleanOverlappingMatches;
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the default language of the user's system.
   */
  public static ResourceBundle getMessageBundle() {
    return ResourceBundleTools.getMessageBundle();
  }

  /**
   * Gets the ResourceBundle (i18n strings) for the given user interface language.
   * @since 2.4 (public since 2.4)
   */
  public static ResourceBundle getMessageBundle(Language lang) {
    return ResourceBundleTools.getMessageBundle(lang);
  }
  
  private List<Rule> getAllBuiltinRules(Language language, ResourceBundle messages) {
    try {
      return language.getRelevantRules(messages);
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
   * @param filename path to an XML file in the classpath or in the filesystem - the classpath is checked first
   * @return a List of {@link PatternRule} objects
   */
  public List<AbstractPatternRule> loadPatternRules(String filename) throws IOException {
    PatternRuleLoader ruleLoader = new PatternRuleLoader();
    try (InputStream is = this.getClass().getResourceAsStream(filename)) {
      if (is == null) {
        // happens for external rules plugged in as an XML file:
        return ruleLoader.getRules(new File(filename));
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
   * @param filename path to an XML file in the classpath or in the filesystem - the classpath is checked first
   * @return a List of {@link PatternRule} objects, or an empty list if mother tongue is not set
   */
  public List<AbstractPatternRule> loadFalseFriendRules(String filename)
      throws ParserConfigurationException, SAXException, IOException {
    if (motherTongue == null) {
      return Collections.emptyList();
    }
    FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader();
    try (InputStream is = this.getClass().getResourceAsStream(filename)) {
      if (is == null) {
        return ruleLoader.getRules(new File(filename), language, motherTongue);
      } else {
        return ruleLoader.getRules(is, language, motherTongue);
      }
    }
  }

  /**
   * Activate rules that depend on a language model. The language model currently
   * consists of Lucene indexes with ngram occurrence counts.
   * @param indexDir directory with a '3grams' sub directory which contains a Lucene index with 3gram occurrence counts
   * @since 2.7
   */
  public void activateLanguageModelRules(File indexDir) throws IOException {
    LanguageModel languageModel = language.getLanguageModel(indexDir);
    if (languageModel != null) {
      ResourceBundle messages = getMessageBundle(language);
      List<Rule> rules = language.getRelevantLanguageModelRules(messages, languageModel);
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
    userRules.addAll(patternRules);
  }

  /**
   * Loads and activates the false friend rules from
   * <code>rules/false-friends.xml</code>.
   */
  private void activateDefaultFalseFriendRules()
      throws ParserConfigurationException, SAXException, IOException {
    String falseFriendRulesFilename = JLanguageTool.getDataBroker().getRulesDir() + "/" + FALSE_FRIEND_FILE;
    userRules.addAll(loadFalseFriendRules(falseFriendRulesFilename));
  }

  /**
   * Add a rule to be used by the next call to the check methods like {@link #check(String)}.
   */
  public void addRule(Rule rule) {
    userRules.add(rule);
  }

  /**
   * Disable a given rule so the check methods like {@link #check(String)} won't use it.
   * @param ruleId the id of the rule to disable - no error will be thrown if the id does not exist
   * @see #enableRule(String) 
   */
  public void disableRule(String ruleId) {
    disabledRules.add(ruleId);
    enabledRules.remove(ruleId);
  }

  /**
   * Disable the given rules so the check methods like {@link #check(String)} won't use them.
   * @param ruleIds the ids of the rules to disable - no error will be thrown if the id does not exist
   * @since 2.4
   */
  public void disableRules(List<String> ruleIds) {
    disabledRules.addAll(ruleIds);
    enabledRules.removeAll(ruleIds);
  }

  /**
   * Disable the given rule category so the check methods like {@link #check(String)} won't use it.
   * @param id the id of the category to disable - no error will be thrown if the id does not exist
   * @since 3.3
   * @see #enableRuleCategory(CategoryId) 
   */
  public void disableCategory(CategoryId id) {
    disabledRuleCategories.add(id);
    enabledRuleCategories.remove(id);
  }

  /**
   * Returns true if a category is explicitly disabled.
   * 
   * @param id the id of the category to check - no error will be thrown if the id does not exist
   * @return true if this category is explicitly disabled.
   * @since 3.5
   * @see #disableCategory(org.languagetool.rules.CategoryId) 
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
   * @param ruleId the id of the rule to enable
   * @see #disableRule(String)
   */
  public void enableRule(String ruleId) {
    disabledRules.remove(ruleId);
    enabledRules.add(ruleId);
  }

  /**
   * Enable all rules of the given category so the check methods like {@link #check(String)} will use it.
   * This will <em>not</em> throw an exception if the given rule id doesn't exist.
   * @since 3.3
   * @see #disableCategory(org.languagetool.rules.CategoryId) 
   */
  public void enableRuleCategory(CategoryId id) {
    disabledRuleCategories.remove(id);
    enabledRuleCategories.add(id);
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
  @Experimental
  public List<RuleMatch> check(String text, RuleMatchListener listener) throws IOException {
    return check(text, true, ParagraphHandling.NORMAL, listener);
  }

  public List<RuleMatch> check(String text, boolean tokenizeText, ParagraphHandling paraMode) throws IOException {
    return check(new AnnotatedTextBuilder().addText(text).build(), tokenizeText, paraMode);
  }

  /**
   * @since 3.7
   */
  @Experimental
  public List<RuleMatch> check(String text, boolean tokenizeText, ParagraphHandling paraMode, RuleMatchListener listener) throws IOException {
    return check(new AnnotatedTextBuilder().addText(text).build(), tokenizeText, paraMode, listener);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules, adjusting error positions so they refer 
   * to the original text <em>including</em> markup.
   * @since 2.3
   */
  public List<RuleMatch> check(AnnotatedText text) throws IOException {
    return check(text, true, ParagraphHandling.NORMAL);
  }
  
  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   * @param annotatedText The text to be checked, created with {@link AnnotatedTextBuilder}. 
   *          Call this method with the complete text to be checked. If you call it
   *          repeatedly with smaller chunks like paragraphs or sentence, those rules that work across
   *          paragraphs/sentences won't work (their status gets reset whenever this method is called).
   * @param tokenizeText If true, then the text is tokenized into sentences.
   *          Otherwise, it is assumed it's already tokenized, i.e. it is only one sentence
   * @param paraMode Uses paragraph-level rules only if true.
   * @return a List of {@link RuleMatch} objects, describing potential errors in the text
   * @since 2.3
   */
  public List<RuleMatch> check(AnnotatedText annotatedText, boolean tokenizeText, ParagraphHandling paraMode) throws IOException {
    return check(annotatedText, tokenizeText, paraMode, null);
  }
  
  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   * @since 3.7
   */
  @Experimental
  public List<RuleMatch> check(AnnotatedText annotatedText, boolean tokenizeText, ParagraphHandling paraMode, RuleMatchListener listener) throws IOException {
    List<String> sentences;
    if (tokenizeText) { 
      sentences = sentenceTokenize(annotatedText.getPlainText());
    } else {
      sentences = new ArrayList<>();
      sentences.add(annotatedText.getPlainText());
    }
    List<Rule> allRules = getAllRules();
    if (printStream != null) {
      printIfVerbose(allRules.size() + " rules activated for language " + language);
    }

    unknownWords = new HashSet<>();
    List<AnalyzedSentence> analyzedSentences = analyzeSentences(sentences);
    
    List<RuleMatch> ruleMatches = performCheck(analyzedSentences, sentences, allRules, paraMode, annotatedText, listener);
    ruleMatches = new SameRuleGroupFilter().filter(ruleMatches);
    // no sorting: SameRuleGroupFilter sorts rule matches already
    if (cleanOverlappingMatches) {
      ruleMatches = new CleanOverlappingFilter(language).filter(ruleMatches);
    }
    return ruleMatches;
  }
  
  /**
   * Use this method if you want to access LanguageTool's otherwise
   * internal analysis of the text. For actual text checking, use the {@code check...} methods instead.
   * @param text The text to be analyzed 
   * @since 2.5
   */
  public List<AnalyzedSentence> analyzeText(String text) throws IOException {
    List<String> sentences = sentenceTokenize(text);
    return analyzeSentences(sentences);
  }
  
  protected List<AnalyzedSentence> analyzeSentences(List<String> sentences) throws IOException {
    List<AnalyzedSentence> analyzedSentences = new ArrayList<>();
    int j = 0;
    for (String sentence : sentences) {
      AnalyzedSentence analyzedSentence = getAnalyzedSentence(sentence);
      rememberUnknownWords(analyzedSentence);
      if (++j == sentences.size()) {
        AnalyzedTokenReadings[] anTokens = analyzedSentence.getTokens();
        anTokens[anTokens.length - 1].setParagraphEnd();
        analyzedSentence = new AnalyzedSentence(anTokens);
      }
      analyzedSentences.add(analyzedSentence);
      printSentenceInfo(analyzedSentence);
    }
    return analyzedSentences;
  }

  protected void printSentenceInfo(AnalyzedSentence analyzedSentence) {
    if (printStream != null) {
      printIfVerbose(analyzedSentence.toString());
      printIfVerbose(analyzedSentence.getAnnotations());
    }
  }
  
  protected List<RuleMatch> performCheck(List<AnalyzedSentence> analyzedSentences, List<String> sentences,
                                         List<Rule> allRules, ParagraphHandling paraMode, AnnotatedText annotatedText) throws IOException {
    return performCheck(analyzedSentences, sentences, allRules, paraMode, annotatedText, null);
  }

  /**
   * @since 3.7
   */
  @Experimental
  protected List<RuleMatch> performCheck(List<AnalyzedSentence> analyzedSentences, List<String> sentences,
                                         List<Rule> allRules, ParagraphHandling paraMode, AnnotatedText annotatedText, RuleMatchListener listener) throws IOException {
    Callable<List<RuleMatch>> matcher = new TextCheckCallable(allRules, sentences, analyzedSentences, paraMode, annotatedText, 0, 0, 1, listener);
    try {
      return matcher.call();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This is an internal method that's public only for technical reasons, please use one
   * of the {@link #check(String)} methods instead. 
   * @since 2.3
   */
  public List<RuleMatch> checkAnalyzedSentence(ParagraphHandling paraMode,
        List<Rule> rules, AnalyzedSentence analyzedSentence) throws IOException {
    List<RuleMatch> sentenceMatches = new ArrayList<>();
    for (Rule rule : rules) {
      if (rule instanceof TextLevelRule) {
        continue;
      }
      if (ignoreRule(rule)) {
        continue;
      }
      if (rule instanceof PatternRule && ((PatternRule)rule).canBeIgnoredFor(analyzedSentence)) {
        // this is a performance optimization, it should have no effect on matching logic
        continue;
      }
      if (paraMode == ParagraphHandling.ONLYPARA) {
        continue;
      }
      RuleMatch[] thisMatches = rule.match(analyzedSentence);
      for (RuleMatch elem : thisMatches) {
        sentenceMatches.add(elem);
      }
    }
    return new SameRuleGroupFilter().filter(sentenceMatches);
  }

  private boolean ignoreRule(Rule rule) {
    Category ruleCategory = rule.getCategory();
    boolean isCategoryDisabled = (disabledRuleCategories.contains(ruleCategory.getId()) || rule.getCategory().isDefaultOff()) 
            && !enabledRuleCategories.contains(ruleCategory.getId());
    boolean isRuleDisabled = disabledRules.contains(rule.getId()) 
            || (rule.isDefaultOff() && !enabledRules.contains(rule.getId()));
    boolean isDisabled;
    if (isCategoryDisabled) {
      isDisabled = !enabledRules.contains(rule.getId());
    } else {
      isDisabled = isRuleDisabled;
    }
    return isDisabled;
  }

  /**
   * Change RuleMatch positions so they are relative to the complete text,
   * not just to the sentence. 
   * @param charCount Count of characters in the sentences before
   * @param columnCount Current column number
   * @param lineCount Current line number
   * @param sentence The text being checked
   * @return The RuleMatch object with adjustments
   */
  public RuleMatch adjustRuleMatchPos(RuleMatch match, int charCount,
      int columnCount, int lineCount, String sentence, AnnotatedText annotatedText) {
    int fromPos = match.getFromPos() + charCount;
    int toPos = match.getToPos() + charCount;
    if (annotatedText != null) {
      fromPos = annotatedText.getOriginalTextPositionFor(fromPos);
      toPos = annotatedText.getOriginalTextPositionFor(toPos - 1) + 1;
    }
    RuleMatch thisMatch = new RuleMatch(match.getRule(),
        fromPos, toPos, match.getMessage(), match.getShortMessage());
    thisMatch.setSuggestedReplacements(match.getSuggestedReplacements());
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
   * @param sentence sentence to be analyzed
   */
  public AnalyzedSentence getAnalyzedSentence(String sentence) throws IOException {
    SimpleInputSentence cacheKey = new SimpleInputSentence(sentence, language);
    AnalyzedSentence cachedSentence = cache != null ? cache.getIfPresent(cacheKey) : null;
    if (cachedSentence != null) {
      return cachedSentence;
    } else {
      AnalyzedSentence analyzedSentence = language.getDisambiguator().disambiguate(getRawAnalyzedSentence(sentence));
      if (language.getPostDisambiguationChunker() != null) {
        language.getPostDisambiguationChunker().addChunkTags(Arrays.asList(analyzedSentence.getTokens()));
      }
      if (cache != null) {
        cache.put(cacheKey, analyzedSentence);
      }
      return analyzedSentence;
    }
  }

  /**
   * Tokenizes the given {@code sentence} into words and analyzes it.
   * This is the same as {@link #getAnalyzedSentence(String)} but it does not run
   * the disambiguator.
   * @param sentence sentence to be analyzed
   * @since 0.9.8
   */
  public AnalyzedSentence getRawAnalyzedSentence(String sentence) throws IOException {
    List<String> tokens = language.getWordTokenizer().tokenize(sentence);
    Map<Integer, String> softHyphenTokens = replaceSoftHyphens(tokens);

    List<AnalyzedTokenReadings> aTokens = language.getTagger().tag(tokens);
    if (language.getChunker() != null) {
      language.getChunker().addChunkTags(aTokens);
    }
    int numTokens = aTokens.size();
    int posFix = 0; 
    for (int i = 1; i < numTokens; i++) {
      aTokens.get(i).setWhitespaceBefore(aTokens.get(i - 1).isWhitespace());
      aTokens.get(i).setStartPos(aTokens.get(i).getStartPos() + posFix);
      if (!softHyphenTokens.isEmpty()) {
        if (softHyphenTokens.get(i) != null) {
          aTokens.get(i).addReading(language.getTagger().createToken(softHyphenTokens.get(i), null));
          posFix += softHyphenTokens.get(i).length() - aTokens.get(i).getToken().length();
        }
      }
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

  private Map<Integer, String> replaceSoftHyphens(List<String> tokens) {
    Pattern ignoredCharacterRegex = language.getIgnoredCharactersRegex();
    Map<Integer, String> ignoredCharsTokens = new HashMap<>();
    if (ignoredCharacterRegex == null) {
      return ignoredCharsTokens;
    }
    for (int i = 0; i < tokens.size(); i++) {
      if (ignoredCharacterRegex.matcher(tokens.get(i)).find()) {
        ignoredCharsTokens.put(i, tokens.get(i));
        tokens.set(i, ignoredCharacterRegex.matcher(tokens.get(i)).replaceAll(""));
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
   * Get pattern rules by Id and SubId. This returns a list because rules that use {@code <or>...</or>}
   * are internally expanded into several rules.
   * @return a List of {@link Rule} objects
   * @since 2.3
   */
  public List<AbstractPatternRule> getPatternRulesByIdAndSubId(String Id, String subId) {
    List<Rule> rules = getAllRules();
    List<AbstractPatternRule> rulesById = new ArrayList<>();   
    for (Rule rule : rules) {
      if (rule instanceof AbstractPatternRule) {
        if (rule.getId().equals(Id) && ((AbstractPatternRule) rule).getSubId().equals(subId)) {
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

  class TextCheckCallable implements Callable<List<RuleMatch>> {

    private final List<Rule> rules;
    private final ParagraphHandling paraMode;
    private final AnnotatedText annotatedText;
    private final List<String> sentences;
    private final List<AnalyzedSentence> analyzedSentences;
    private final RuleMatchListener listener;
    
    private int charCount;
    private int lineCount;
    private int columnCount;

    TextCheckCallable(List<Rule> rules, List<String> sentences, List<AnalyzedSentence> analyzedSentences,
                      ParagraphHandling paraMode, AnnotatedText annotatedText, int charCount, int lineCount, int columnCount, RuleMatchListener listener) {
      this.rules = rules;
      if (sentences.size() != analyzedSentences.size()) {
        throw new IllegalArgumentException("sentences and analyzedSentences do not have the same length : " + sentences.size() + " != " + analyzedSentences.size());
      }
      this.sentences = Objects.requireNonNull(sentences);
      this.analyzedSentences = Objects.requireNonNull(analyzedSentences);
      this.paraMode = Objects.requireNonNull(paraMode);
      this.annotatedText = Objects.requireNonNull(annotatedText);
      this.charCount = charCount;
      this.lineCount = lineCount;
      this.columnCount = columnCount;
      this.listener = listener;
    }

    @Override
    public List<RuleMatch> call() throws Exception {
      List<RuleMatch> ruleMatches = new ArrayList<>();
      ruleMatches.addAll(getTextLevelRuleMatches());
      ruleMatches.addAll(getOtherRuleMatches());
      return ruleMatches;
    }

    private List<RuleMatch> getTextLevelRuleMatches() throws IOException {
      List<RuleMatch> ruleMatches = new ArrayList<>();
      for (Rule rule : rules) {
        if (rule instanceof TextLevelRule && !ignoreRule(rule) && paraMode != ParagraphHandling.ONLYNONPARA) {
          RuleMatch[] matches = ((TextLevelRule) rule).match(analyzedSentences);
          List<RuleMatch> adaptedMatches = new ArrayList<>();
          for (RuleMatch match : matches) {
            LineColumnRange range = getLineColumnRange(match);
            int newFromPos = annotatedText.getOriginalTextPositionFor(match.getFromPos());
            int newToPos = annotatedText.getOriginalTextPositionFor(match.getToPos() - 1) + 1;
            RuleMatch newMatch = new RuleMatch(match.getRule(), newFromPos, newToPos, match.getMessage(), match.getShortMessage());
            newMatch.setLine(range.from.line);
            newMatch.setEndLine(range.to.line);
            if (match.getLine() == 0) {
              newMatch.setColumn(range.from.column + 1);
            } else {
              newMatch.setColumn(range.from.column);
            }
            newMatch.setEndColumn(range.to.column);
            newMatch.setSuggestedReplacements(match.getSuggestedReplacements());
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

    private List<RuleMatch> getOtherRuleMatches() {
      List<RuleMatch> ruleMatches = new ArrayList<>();
      int i = 0;
      for (AnalyzedSentence analyzedSentence : analyzedSentences) {
        String sentence = sentences.get(i++);
        try {
          List<RuleMatch> sentenceMatches = null;
          InputSentence cacheKey = null;
          if (cache != null) {
            cacheKey = new InputSentence(analyzedSentence.getText(), language, motherTongue,
                    disabledRules, disabledRuleCategories,
                    enabledRules, enabledRuleCategories);
            sentenceMatches = cache.getIfPresent(cacheKey);
          }
          if (sentenceMatches == null) {
            sentenceMatches = checkAnalyzedSentence(paraMode, rules, analyzedSentence);
          }
          if (cache != null) {
            cache.put(cacheKey, sentenceMatches);
          }
          List<RuleMatch> adaptedMatches = new ArrayList<>();
          for (RuleMatch elem : sentenceMatches) {
            RuleMatch thisMatch = adjustRuleMatchPos(elem,
                    charCount, columnCount, lineCount, sentence, annotatedText);
            adaptedMatches.add(thisMatch);
            if (listener != null) {
              listener.matchFound(thisMatch);
            }
          }
          ruleMatches.addAll(adaptedMatches);
          charCount += sentence.length();
          lineCount += countLineBreaks(sentence);

          // calculate matching column:
          int lineBreakPos = sentence.lastIndexOf('\n');
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
        } catch (Exception e) {
          throw new RuntimeException("Could not check sentence (language: " + language + "): '"
                  + StringUtils.abbreviate(analyzedSentence.toTextString(), 200) + "'", e);
        }
      }
      return ruleMatches;
    }

    private LineColumnRange getLineColumnRange(RuleMatch match) {
      LineColumnPosition fromPos = new LineColumnPosition(-1, -1);
      LineColumnPosition toPos = new LineColumnPosition(-1, -1);
      LineColumnPosition pos = new LineColumnPosition(0, 0);
      int charCount = 0;
      for (AnalyzedSentence analyzedSentence : analyzedSentences) {
        for (AnalyzedTokenReadings readings : analyzedSentence.getTokens()) {
          String token = readings.getToken();
          if ("\n".equals(token)) {
            pos.line++;
            pos.column = 0;
          }
          pos.column += token.length();
          charCount += token.length();
          if (charCount == match.getFromPos()) {
            fromPos = new LineColumnPosition(pos.line, pos.column);
          } 
          if (charCount == match.getToPos()) {
            toPos = new LineColumnPosition(pos.line, pos.column);
          }
        }
      }
      return new LineColumnRange(fromPos, toPos);
    }
    
    private class LineColumnPosition {
      int line;
      int column;
      private LineColumnPosition(int line, int column) {
        this.line = line;
        this.column = column;
      }
    }
  
    private class LineColumnRange {
      LineColumnPosition from;
      LineColumnPosition to;
      private LineColumnRange(LineColumnPosition from, LineColumnPosition to) {
        this.from = from;
        this.to = to;
      }
    }
  
  }

}
