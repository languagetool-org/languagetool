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
package de.danielnaber.languagetool;

import de.danielnaber.languagetool.databroker.DefaultResourceDataBroker;
import de.danielnaber.languagetool.databroker.ResourceDataBroker;
import de.danielnaber.languagetool.gui.ResourceBundleWithFallback;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.FalseFriendRuleLoader;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * The main class used for checking text against different rules:
 * <ul>
 * <li>the built-in rules (<i>a</i> vs. <i>an</i>, whitespace after commas, ...)
 * <li>pattern rules loaded from external XML files with
 * {@link #loadPatternRules(String)}
 * <li>your own implementation of the abstract {@link Rule} classes added with
 * {@link #addRule(Rule)}
 * </ul>
 * 
 * <p>
 * Note that the constructors create a language checker that uses the built-in
 * rules only. Other rules (e.g. from XML) need to be added explicitly.
 * 
 * @author Daniel Naber
 */
@SuppressWarnings({"UnusedDeclaration"})
public final class JLanguageTool {

  public static final String VERSION = "1.5-dev"; // keep in sync with build.properties!

  public static final String PATTERN_FILE = "grammar.xml";
  public static final String FALSE_FRIEND_FILE = "false-friends.xml";
  public static final String SENTENCE_START_TAGNAME = "SENT_START";

  public static final String SENTENCE_END_TAGNAME = "SENT_END";
  public static final String PARAGRAPH_END_TAGNAME = "PARA_END";

  private static ResourceDataBroker dataBroker = new DefaultResourceDataBroker();

  private final List<Rule> builtinRules = new ArrayList<Rule>();
  private final List<Rule> userRules = new ArrayList<Rule>(); // rules added via addRule() method
  private final Set<String> disabledRules = new HashSet<String>();
  private final Set<String> enabledRules = new HashSet<String>();

  private final Set<String> disabledCategories = new HashSet<String>();

  private Language language;
  private Language motherTongue;
  private Disambiguator disambiguator;
  private Tagger tagger;
  private Tokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;

  private PrintStream printStream;

  private int sentenceCount;

  private boolean listUnknownWords;
  private Set<String> unknownWords;

  /**
   * Constants for correct paragraph-rule handling. 
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
  
  // just for testing:
  /*
   * private Rule[] allBuiltinRules = new Rule[] { new
   * UppercaseSentenceStartRule() };
   */

  /**
   * Create a JLanguageTool and setup the built-in rules appropriate for the
   * given language, ignoring false friend hints.
   * 
   * @throws IOException
   */
  public JLanguageTool(final Language language) throws IOException {
    this(language, null);
  }

  /**
   * Create a JLanguageTool and setup the built-in rules appropriate for the
   * given language.
   * 
   * @param language
   *          the language to be used.
   * @param motherTongue
   *          the user's mother tongue or <code>null</code>. The mother tongue
   *          may also be used as a source language for checking bilingual texts.
   *          
   * @throws IOException
   */
  public JLanguageTool(final Language language, final Language motherTongue)
      throws IOException {
    if (language == null) {
      throw new NullPointerException("language cannot be null");
    }
    this.language = language;
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
  }
  
  /**
   * The grammar checker needs resources from following
   * directories:
   * 
   * <ul style="list-type: circle">
   * <li>{@code /resource}</li>
   * <li>{@code /rules}</li>
   * </ul>
   * 
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
   * 
   * <ul style="list-type: circle">
   * <li>{@code /resource}</li>
   * <li>{@code /rules}</li>
   * </ul>
   * 
   * This method is thread-safe.
   * 
   * @param broker The new resource broker to be used.
   * @since 1.0.1
   */
  public static synchronized void setDataBroker(ResourceDataBroker broker) {
	  JLanguageTool.dataBroker = broker;
  }

  /**
   * Whether the check() method stores unknown words. If set to
   * <code>true</code> (default: false), you can get the list of unknown words
   * using getUnknownWords().
   */
  public void setListUnknownWords(final boolean listUnknownWords) {
    this.listUnknownWords = listUnknownWords;
  }

  /**
   * Gets the ResourceBundle for the default language of the user's system.
   */
  public static ResourceBundle getMessageBundle() {
    try {
      final ResourceBundle bundle = ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle");
      final ResourceBundle fallbackBundle = ResourceBundle.getBundle(
          "de.danielnaber.languagetool.MessagesBundle", Locale.ENGLISH);
      return new ResourceBundleWithFallback(bundle, fallbackBundle);
    } catch (final MissingResourceException e) {
      return ResourceBundle.getBundle(
          "de.danielnaber.languagetool.MessagesBundle", Locale.ENGLISH);
    }
  }

  /**
   * Gets the ResourceBundle for the given user interface language.
   */
  private static ResourceBundle getMessageBundle(final Language lang) {
    try {
      final ResourceBundle bundle = ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle", 
              lang.getLocale());
      final ResourceBundle fallbackBundle = ResourceBundle.getBundle(
          "de.danielnaber.languagetool.MessagesBundle", Locale.ENGLISH);
      return new ResourceBundleWithFallback(bundle, fallbackBundle);
    } catch (final MissingResourceException e) {
      return ResourceBundle.getBundle(
          "de.danielnaber.languagetool.MessagesBundle", Locale.ENGLISH);
    }
  }

  private Rule[] getAllBuiltinRules(final Language language, final ResourceBundle messages) {
    final List<Rule> rules = new ArrayList<Rule>();
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
   * <code>null</code> to disable verbose output.
   */
  public void setOutput(final PrintStream printStream) {
    this.printStream = printStream;
  }

  /**
   * Load pattern rules from an XML file. Use {@link #addRule(Rule)} to add these
   * rules to the checking process.
   * 
   * @throws IOException
   * @return a List of {@link PatternRule} objects
   */
  public List<PatternRule> loadPatternRules(final String filename)
      throws IOException {
    final PatternRuleLoader ruleLoader = new PatternRuleLoader();
    InputStream is = this.getClass().getResourceAsStream(filename);
    if (is == null) {
      // happens for external rules plugged in as an XML file:
      is = new FileInputStream(filename);
    }
    return ruleLoader.getRules(is, filename);
  }

  /**
   * Load false friend rules from an XML file. Only those pairs will be loaded
   * that match the current text language and the mother tongue specified in the
   * JLanguageTool constructor. Use {@link #addRule(Rule)} to add these rules to the
   * checking process.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @return a List of {@link PatternRule} objects
   */
  public List<PatternRule> loadFalseFriendRules(final String filename)
      throws ParserConfigurationException, SAXException, IOException {
    if (motherTongue == null) {
      return new ArrayList<PatternRule>();
    }
    final FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader();
    return ruleLoader.getRules(this.getClass().getResourceAsStream(filename),
        language, motherTongue);
  }

  /**
   * Loads and activates the pattern rules from
   * <code>rules/&lt;language&gt;/grammar.xml</code>.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public void activateDefaultPatternRules() throws IOException {
    final String defaultPatternFilename = language.getRuleFileName();
    final List<PatternRule> patternRules = loadPatternRules(defaultPatternFilename);
    userRules.addAll(patternRules);
  }

  /**
   * Loads and activates the false friend rules from
   * <code>rules/false-friends.xml</code>.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public void activateDefaultFalseFriendRules()
      throws ParserConfigurationException, SAXException, IOException {
    final String falseFriendRulesFilename = JLanguageTool.getDataBroker().getRulesDir() + "/" + FALSE_FRIEND_FILE;
    final List<PatternRule> patternRules = loadFalseFriendRules(falseFriendRulesFilename);
    userRules.addAll(patternRules);
  }

  /**
   * Add a rule to be used by the next call to {@link #check(String)}.
   */
  public void addRule(final Rule rule) {
    userRules.add(rule);
  }

  /**
   * Disable a given rule so {@link #check(String)} won't use it.
   * 
   * @param ruleId
   *          the id of the rule to disable
   */
  public void disableRule(final String ruleId) {
    // TODO: check if such a rule exists
    disabledRules.add(ruleId);
  }

  /**
   * Disable a given category so {@link #check(String)} won't use it.
   * 
   * @param categoryName
   *          the id of the category to disable
   */
  public void disableCategory(final String categoryName) {
    // TODO: check if such a rule exists
    disabledCategories.add(categoryName);
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
   * Enable a rule that was switched off by default.
   * 
   * @param ruleId
   *          the id of the turned off rule to enable.
   * 
   */
  public void enableDefaultOffRule(final String ruleId) {
    enabledRules.add(ruleId);
  }

  /**
   * Get category ids of the rules that have been explicitly disabled.
   */
  public Set<String> getDisabledCategories() {
    return disabledCategories;
  }

  /**
   * Re-enable a given rule so {@link #check(String)} will use it.
   * 
   * @param ruleId
   *          the id of the rule to enable
   */
  public void enableRule(final String ruleId) {
    if (disabledRules.contains(ruleId)) {
      disabledRules.remove(ruleId);
    }
  }

  /**
   * Returns tokenized sentences.
   */
  public List<String> sentenceTokenize(final String text) {
    return sentenceTokenizer.tokenize(text);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   * 
   * @param text
   *          the text to check  
   * @return a List of {@link RuleMatch} objects
   * @throws IOException
   */
  public List<RuleMatch> check(final String text) throws IOException {
    return check(text, true, ParagraphHandling.NORMAL);
  }
  
  
  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   * 
   * @param text
   *          the text to check  
   * @param tokenizeText
   *          If true, then the text is tokenized into sentences. 
   *          Otherwise, it is assumed it's already tokenized.
   * @param paraMode
   *          Uses paragraph-level rules only if true.
 
   * @return a List of {@link RuleMatch} objects
   * @throws IOException
   */
  public List<RuleMatch> check(final String text, boolean tokenizeText, final ParagraphHandling paraMode) throws IOException {
    sentenceCount = 0;
    final List<String> sentences;
    if (tokenizeText) { 
      sentences = sentenceTokenize(text);
    } else {
      sentences = new ArrayList<String>();
      sentences.add(text);
    }
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final List<Rule> allRules = getAllRules();
    printIfVerbose(allRules.size() + " rules activated for language "
        + language);
    int charCount = 0;
    int lineCount = 0;
    int columnCount = 1;
    unknownWords = new HashSet<String>();
    for (final String sentence : sentences) {
      sentenceCount++;      
      AnalyzedSentence analyzedText = getAnalyzedSentence(sentence);
      rememberUnknownWords(analyzedText);

      if (sentenceCount == sentences.size()) {
        final AnalyzedTokenReadings[] anTokens = analyzedText.getTokens();        
        anTokens[anTokens.length - 1].setParaEnd();
        analyzedText = new AnalyzedSentence(anTokens);
      }
      
      printIfVerbose(analyzedText.toString());
      final List<RuleMatch> sentenceMatches = 
      checkAnalyzedSentence(paraMode, allRules, charCount, lineCount,
          columnCount, sentence, analyzedText);

      Collections.sort(sentenceMatches);
      ruleMatches.addAll(sentenceMatches);
      charCount += sentence.length();
      lineCount += countLineBreaks(sentence);
      
      // calculate matching column:      
      final int lineBreakPos = sentence.indexOf('\n');
      if (lineBreakPos == -1) {
        columnCount += sentence.length() -1;        
      } else {
        if (lineBreakPos == 0) {
          columnCount = sentence.length();
          if (!language.getSentenceTokenizer().
              singleLineBreaksMarksPara()) {
            columnCount--;
          }
        } else {
          columnCount = 1;
        }
      }      
    }

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

    return ruleMatches;
  }

  public List<RuleMatch> checkAnalyzedSentence(final ParagraphHandling paraMode,
      final List<Rule> allRules, int tokenCount, int lineCount,
      int columnCount, final String sentence, AnalyzedSentence analyzedText) 
        throws IOException {
    final List<RuleMatch> sentenceMatches = new ArrayList<RuleMatch>();
    for (final Rule rule : allRules) {
      if (disabledRules.contains(rule.getId())
          || (rule.isDefaultOff() && !enabledRules.contains(rule.getId()))) {
        continue;
      }

      if (disabledCategories.contains(rule.getCategory().getName())) {
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

      final RuleMatch[] thisMatches = rule.match(analyzedText);
      for (final RuleMatch element1 : thisMatches) {
        RuleMatch thisMatch = adjustRuleMatchPos(element1,
            tokenCount, columnCount, lineCount, sentence);    
        sentenceMatches.add(thisMatch);
        if (rule.isParagraphBackTrack()) {
          rule.addRuleMatch(thisMatch);
        }
      }
    }
    return sentenceMatches;
  }

  /**
   * Change RuleMatch positions so they are relative to the complete text,
   * not just to the sentence: 
   * @param rm  RuleMatch
   * @param sentLen  Count of characters
   * @param columnCount Current column number
   * @param lineCount Current line number
   * @param sentence  The text being checked
   * @return
   * The RuleMatch object with adjustments.
   */
  public RuleMatch adjustRuleMatchPos(final RuleMatch rm, int sentLen,
      int columnCount, int lineCount, final String sentence) {    
    final RuleMatch thisMatch = new RuleMatch(rm.getRule(),
        rm.getFromPos() + sentLen, rm.getToPos()
            + sentLen, rm.getMessage(), rm
            .getShortMessage());
    thisMatch.setSuggestedReplacements(rm
        .getSuggestedReplacements());
    final String sentencePartToError = sentence.substring(0, rm
        .getFromPos());
    final String sentencePartToEndOfError = sentence.substring(0,
        rm.getToPos());
    final int lastLineBreakPos = sentencePartToError.lastIndexOf('\n');
    final int column;
    final int endColumn;
    if (lastLineBreakPos == -1) {
      column = sentencePartToError.length() + columnCount;
    } else {
      column = sentencePartToError.length() - lastLineBreakPos;
    }
    final int lastLineBreakPosInError = sentencePartToEndOfError
        .lastIndexOf('\n');
    if (lastLineBreakPosInError == -1) {
      endColumn = sentencePartToEndOfError.length() + columnCount + 1;
    } else {
      endColumn = sentencePartToEndOfError.length() - lastLineBreakPos;
    }
    final int lineBreaksToError = countLineBreaks(sentencePartToError);
    final int lineBreaksToEndOfError = countLineBreaks(sentencePartToEndOfError);
    thisMatch.setLine(lineCount + lineBreaksToError);
    thisMatch.setEndLine(lineCount + lineBreaksToEndOfError);
    thisMatch.setColumn(column);
    thisMatch.setEndColumn(endColumn);
    thisMatch.setOffset(rm.getFromPos() + sentLen);
    return thisMatch;
  }

  private void rememberUnknownWords(final AnalyzedSentence analyzedText) {
    if (listUnknownWords) {
      final AnalyzedTokenReadings[] atr = analyzedText
          .getTokensWithoutWhitespace();
      for (final AnalyzedTokenReadings t : atr) {
        if (t.getReadings().toString().contains("null]")) {
          unknownWords.add(t.getToken());
        }
      }
    }
  }

  /**
   * Get the list of unknown words in the last run of the check() method.
   * 
   * @throws IllegalStateException
   *           if listUnknownWords is set to <code>false</code>
   */
  public List<String> getUnknownWords() {
    if (!listUnknownWords) {
      throw new IllegalStateException(
          "listUnknownWords is set to false, unknown words not stored");
    }
    final List<String> words = new ArrayList<String>(unknownWords);
    Collections.sort(words);
    return words;
  }

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
   * Tokenizes the given <code>sentence</code> into words and analyzes it,
   * and then disambiguates POS tags.
   * 
   * @throws IOException
   */
  public AnalyzedSentence getAnalyzedSentence(final String sentence)
      throws IOException {
    // disambiguate assigned tags & return
    return disambiguator.disambiguate(getRawAnalyzedSentence(sentence));
  }

  /**
   * Tokenizes the given <code>sentence</code> into words and analyzes it.
   * 
   * @since 0.9.8
   * @param sentence
   *        Sentence to be analyzed 
   * @return
   *        AnalyzedSentence
   * @throws IOException
   */
  public AnalyzedSentence getRawAnalyzedSentence(final String sentence) throws IOException {
    final List<String> tokens = wordTokenizer.tokenize(sentence);
    final Map<Integer, String> softHyphenTokens = new HashMap<Integer, String>();

    //for soft hyphens inside words, happens especially in OOo:
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i).indexOf('\u00ad') != -1) {
        softHyphenTokens.put(i, tokens.get(i));
        tokens.set(i, tokens.get(i).replaceAll("\u00ad", ""));
      }
    }
    
    final List<AnalyzedTokenReadings> aTokens = tagger.tag(tokens);
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
        
    final AnalyzedTokenReadings[] tokenArray = new AnalyzedTokenReadings[tokens
        .size() + 1];
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
    final List<Rule> rules = new ArrayList<Rule>();
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
   * Number of sentences the latest call to check() has checked.
   */
  public int getSentenceCount() {
    return sentenceCount;

  }

  private void printIfVerbose(final String s) {
    if (printStream != null) {
      printStream.println(s);
    }
  }

}
