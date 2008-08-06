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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.FalseFriendRuleLoader;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tools.ReflectionUtils;

/**
 * The main class used for checking text against different rules:
 * <ul>
 *  <li>the built-in rules (<i>a</i> vs. <i>an</i>, whitespace after commas, ...)
 *  <li>pattern rules loaded from external XML files with {@link #loadPatternRules(String)}
 *  <li>your own implementation of the abstract {@link Rule} classes added with {@link #addRule(Rule)}
 * </ul>
 * 
 * <p>Note that the constructors create a language checker that uses the built-in
 * rules only. Other rules (e.g. from XML) need to be added explicitly.
 * 
 * @author Daniel Naber
 */
public final class JLanguageTool {

  public static final String VERSION = "0.9.4-dev";      // keep in sync with build.xml!

  public static final String RULES_DIR = "/rules";
  public static final String PATTERN_FILE = "grammar.xml";
  public static final String FALSE_FRIEND_FILE = "false-friends.xml";
  
  public static final String SENTENCE_START_TAGNAME = "SENT_START";
  public static final String SENTENCE_END_TAGNAME = "SENT_END";
  public static final String PARAGRAPH_END_TAGNAME = "PARA_END";

  private List<Rule> builtinRules = new ArrayList<Rule>();
  private List<Rule> userRules = new ArrayList<Rule>();     // rules added via addRule() method
  private Set<String> disabledRules = new HashSet<String>();
  private Set<String> enabledRules = new HashSet<String>();
  
  private Set<String> disabledCategories = new HashSet<String>();
  
  private Language language = null;
  private Language motherTongue = null;
  private Disambiguator disambiguator = null;
  private Tagger tagger = null;
  private Tokenizer sentenceTokenizer = null;
  private Tokenizer wordTokenizer = null;

  private PrintStream printStream = null;
  
  private int sentenceCount = 0;
    
  private boolean listUnknownWords = false;
  private Set<String> unknownWords = null;

  // just for testing:
  /*private Rule[] allBuiltinRules = new Rule[] {
      new UppercaseSentenceStartRule()
  };*/

  /**
   * Create a JLanguageTool and setup the built-in rules appropriate for the
   * given language, ignoring false friend hints. 
   * @throws IOException 
   */
  public JLanguageTool(final Language language) throws IOException {
    this(language, null);
  }

  /**
   * Create a JLanguageTool and setup the built-in rules appropriate for the
   * given language.
   * @param language the language to be used.
   * @param motherTongue the user's mother tongue or <code>null</code>
   * @throws IOException 
   */
  public JLanguageTool(final Language language, final Language motherTongue) throws IOException {
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
   * Whether the check() method stores unknown words. If set to <code>true</code>
   * (default: false), you can get the list of unknown words using getUnknownWords(). 
   */
  public void setListUnknownWords(final boolean listUnknownWords) {
    this.listUnknownWords = listUnknownWords;    
  }
  
  /**
   * Gets the ResourceBundle for the default language of the user's system.
   */
  public static ResourceBundle getMessageBundle() {
    try {
      return ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle");
    } catch (final MissingResourceException e) {
      return ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle", Locale.ENGLISH);
    }
  }

  /**
   * Gets the ResourceBundle for the given user interface language.
   */
  private static ResourceBundle getMessageBundle(final Language lang) {
    try {
      return ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle",
          lang.getLocale());
    } catch (final MissingResourceException e) {
      return ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle", Locale.ENGLISH);
    }
  }

  private Rule[] getAllBuiltinRules(final Language language, final ResourceBundle messages) {
    // use reflection to get a list of all non-pattern rules under
    // "de.danielnaber.languagetool.rules"
    // generic rules first, then language-specific ones
    // TODO: the order of loading classes is not guaranteed so we may want to implement rule
    // precedence

    final List<Rule> rules = new ArrayList<Rule>();
    try {
      // we pass ".*Rule$" regexp to improve efficiency, see javadoc
      final Class[] classes1 = ReflectionUtils.findClasses(Rule.class.getClassLoader(), 
          Rule.class.getPackage().getName(), ".*Rule$", 0, Rule.class, null);
      final Class[] classes2 = ReflectionUtils.findClasses(Rule.class.getClassLoader(), 
          Rule.class.getPackage().getName() + "." + language.getShortName(),
          ".*Rule$", 0, Rule.class, null);

      final List<Class> classes = new ArrayList<Class>();
      classes.addAll(Arrays.asList(classes1));
      classes.addAll(Arrays.asList(classes2));

      for (final Class class1 : classes) {
        final Constructor[] constructors = class1.getConstructors();
        for (final Constructor constructor : constructors) {
          final Class[] paramTypes = constructor.getParameterTypes();
          if (paramTypes.length == 1 && paramTypes[0].equals(ResourceBundle.class)) {
            rules.add((Rule) constructor.newInstance(messages));
            break;
          }
          if (paramTypes.length == 2 && paramTypes[0].equals(ResourceBundle.class)
              && paramTypes[1].equals(Language.class)) {
            rules.add((Rule) constructor.newInstance(messages, language));
            break;
          }
          throw new RuntimeException("Unknown constructor for rule class: " + class1.getName());
        }
      }
    } catch (final Exception e) {
      throw new RuntimeException("Failed to load rules: " + e.getMessage(), e);
    }
    //	System.err.println("Loaded " + rules.size() + " rules");
    return rules.toArray(new Rule[rules.size()]);
  }
  
  /**
   * Set a PrintStream that will receive verbose output. Set
   * to <code>null</code> to disable verbose output.
   */
  public void setOutput(final PrintStream printStream) {
    this.printStream = printStream;
  }

  /**
   * Get the File, assuming it's under the base directory.
   * 
   * @param relFilename a non-absolute file name 
   
  public File getAbsoluteFile(final String relFilename) {    
    if (basedir == null)
      return new File(relFilename);
    return new File(basedir, relFilename);
  }
  */

  /**
   * Load pattern rules from an XML file. Use {@link #addRule} to add
   * these rules to the checking process.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @return a List of {@link PatternRule} objects
   */
  public List<PatternRule> loadPatternRules(final String filename) throws IOException {
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
   * that match the current text language and the mother tongue specified
   * in the JLanguageTool constructor. Use {@link #addRule} to add
   * these rules to the checking process.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @return a List of {@link PatternRule} objects
   */
  public List<PatternRule> loadFalseFriendRules(final String filename) throws ParserConfigurationException, SAXException, IOException {
    if (motherTongue == null) {
      return new ArrayList<PatternRule>();
    }
    final FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader();
    return ruleLoader.getRules(this.getClass().getResourceAsStream(filename), language, motherTongue);
  }

  /**
   * Loads and activates the pattern rules from <code>rules/&lt;language&gt;/grammar.xml</code>.
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
   * Loads and activates the false friend rules from <code>rules/false-friends.xml</code>.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public void activateDefaultFalseFriendRules() throws ParserConfigurationException, SAXException, IOException {
    final String falseFriendRulesFilename =  RULES_DIR + "/" + FALSE_FRIEND_FILE;
    final List<PatternRule> patternRules = loadFalseFriendRules(falseFriendRulesFilename);
    userRules.addAll(patternRules);
  }

  /**
   * Add a rule to be used by the next call to {@link #check}.
   */
  public void addRule(final Rule rule) {
    userRules.add(rule);
  }

  /**
   * Disable a given rule so {@link #check} won't use it.
   * @param ruleId the id of the rule to disable
   */
  public void disableRule(final String ruleId) {
    // TODO: check if such a rule exists
    disabledRules.add(ruleId);
  }
  
  /**
   * Disable a given category so {@link #check} won't use it.
   * @param categoryName the id of the category to disable
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
   * @param ruleId the id of the turned off rule to enable. 
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
   * Re-enable a given rule so {@link #check} will use it.
   * @param ruleId the id of the rule to enable
   */
  public void enableRule(final String ruleId) {
    // TODO: check if such a rule exists
    disabledRules.remove(ruleId);
  }

  /**
   * The main check method. Tokenizes the text into sentences and matches these
   * sentences against all currently active rules.
   * 
   * @param text the text to check
   * @return a List of {@link RuleMatch} objects
   * @throws IOException 
   */
  public List<RuleMatch> check(final String text) throws IOException {
    sentenceCount = 0;
    final List<String> sentences = sentenceTokenizer.tokenize(text);
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final List<Rule> allRules = getAllRules();           
    printIfVerbose(allRules.size() + " rules activated for language " + language);
    int tokenCount = 0;
    int lineCount = 0;
    int columnCount = 0;
    unknownWords = new HashSet<String>();
    for (final String sentence : sentences) {
      sentenceCount++;
      AnalyzedSentence analyzedText = getAnalyzedSentence(sentence);
      rememberUnknownWords(analyzedText);
      
      if (sentenceCount == sentences.size()) {
        final AnalyzedTokenReadings[] anTokens = analyzedText.getTokens();
        final AnalyzedToken paragraphEnd =
          new AnalyzedToken(anTokens[anTokens.length - 1].getToken(),
              PARAGRAPH_END_TAGNAME,
              anTokens[anTokens.length - 1].getAnalyzedToken(0).getLemma(),
              anTokens[anTokens.length - 1].getAnalyzedToken(0).getStartPos());
        anTokens[anTokens.length - 1].addReading(paragraphEnd);
        analyzedText = new AnalyzedSentence(anTokens); 
      }
      
      final List<RuleMatch> sentenceMatches = new ArrayList<RuleMatch>();
      printIfVerbose(analyzedText.toString());
      for (final Rule rule : allRules) {
        if (disabledRules.contains(rule.getId())
            || (rule.isDefaultOff() && !enabledRules.contains(rule.getId()))) {
          continue;
        }                
        
        if (disabledCategories.contains(rule.getCategory().getName())) {
          continue;
        }
        
        final RuleMatch[] thisMatches = rule.match(analyzedText);
        for (final RuleMatch element1 : thisMatches) {
          // change positions so they are relative to the complete text,
          // not just to the sentence:
          final RuleMatch thisMatch = new RuleMatch(element1.getRule(),
              element1.getFromPos() + tokenCount,
              element1.getToPos() + tokenCount,
              element1.getMessage());
          thisMatch.setSuggestedReplacements(element1.getSuggestedReplacements());
          final String sentencePartToError = sentence.substring(0, element1.getFromPos());
          final String sentencePartToEndOfError = sentence.substring(0, element1.getToPos());          
          final int lastLineBreakPos = sentencePartToError.lastIndexOf('\n');
          int column = -1;
          int endColumn = -1;
          if (lastLineBreakPos == -1) {
            column = sentencePartToError.length() + columnCount;
          } else {
            column = sentencePartToError.length() - lastLineBreakPos - 1;
          }
          final int lastLineBreakPosInError = sentencePartToEndOfError.lastIndexOf('\n');
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
          thisMatch.setOffset(element1.getFromPos() + tokenCount);
          sentenceMatches.add(thisMatch);
          if (rule.isParagraphBackTrack()) {
            rule.addRuleMatch(thisMatch);
          }
        }        
      }
                  
      Collections.sort(sentenceMatches);
      ruleMatches.addAll(sentenceMatches);
      tokenCount += sentence.length();
      lineCount += countLineBreaks(sentence);
      // calculate matching column:
      final int linebreakPos = sentence.indexOf('\n');
      if (linebreakPos == -1) {
        columnCount += sentence.length();
      } else {
        columnCount = sentence.length() - linebreakPos - 1;
      }
    }
    
    //removing false positives in paragraph-level rules
    for (final Rule rule : allRules) {
      if (rule.isParagraphBackTrack()
          && (rule.getMatches() != null)) {
        final List <RuleMatch> rm = rule.getMatches();           
        for (final RuleMatch r : rm) {
          if (rule.isInRemoved(r)) {
            ruleMatches.remove(r);
          }
        }       
      }          
    }

    return ruleMatches;
  }
  
  private void rememberUnknownWords(final AnalyzedSentence analyzedText) {
    if (listUnknownWords) {
      final AnalyzedTokenReadings[] atr = analyzedText.getTokensWithoutWhitespace();
      for (final AnalyzedTokenReadings t : atr) {
        if (t.getReadings().toString().equals("[null]")) {
          unknownWords.add(t.getToken());
        }
      }
    }
  }

  /**
   * Get the list of unknown words in the last run of the check() method.
   * @throws IllegalStateException listUnknownWords is set to <code>false</code>
   */
  public List<String> getUnknownWords() {
    if (!listUnknownWords) {
      throw new IllegalStateException("listUnknownWords is set to false, unknown words not stored");
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
   * Tokenizes the given <code>sentence</code> into words and analyzes it.
   * @throws IOException 
   */
  public AnalyzedSentence getAnalyzedSentence(final String sentence) throws IOException {
    final List<String> tokens = wordTokenizer.tokenize(sentence);    
    final List<AnalyzedTokenReadings> aTokens = tagger.tag(tokens);
    final AnalyzedTokenReadings[] tokenArray = new AnalyzedTokenReadings[tokens.size() + 1];
    final AnalyzedToken[] startTokenArray = new AnalyzedToken[1];  
    int toArrayCount = 0;
    final AnalyzedToken sentenceStartToken = new AnalyzedToken("", SENTENCE_START_TAGNAME, 0);
    startTokenArray[0] = sentenceStartToken;
    tokenArray[toArrayCount++] = new AnalyzedTokenReadings(startTokenArray);
    int startPos = 0;
    for (final AnalyzedTokenReadings posTag : aTokens) {
      posTag.startPos = startPos;
      tokenArray[toArrayCount++] = posTag;
      startPos += posTag.getToken().length();
    }

    //add additional tags
    int lastToken = toArrayCount - 1;
    //make SENT_END appear at last not whitespace token
    for (int i = 0; i < toArrayCount - 1; i++) {
      if (!tokenArray[lastToken - i].isWhitespace()) {
        lastToken -= i;
        break;
      }
    }
    final AnalyzedToken sentenceEnd = 
      new AnalyzedToken(tokenArray[lastToken].getToken(), 
          SENTENCE_END_TAGNAME,
          tokenArray[lastToken].getAnalyzedToken(0).getLemma(),
          tokenArray[lastToken].getAnalyzedToken(0).getStartPos());
    tokenArray[lastToken].addReading(sentenceEnd);

    if (tokenArray.length == 2 
        && (tokenArray[0].isSentStart()) 
        && tokenArray[1].getToken().equals("\n")) {
      final AnalyzedToken paragraphEnd =
        new AnalyzedToken(tokenArray[lastToken].getToken(),
            PARAGRAPH_END_TAGNAME,
            tokenArray[lastToken].getAnalyzedToken(0).getLemma(),
            tokenArray[lastToken].getAnalyzedToken(0).getStartPos());
      tokenArray[lastToken].addReading(paragraphEnd);        
    }


    AnalyzedSentence finalSentence = new AnalyzedSentence(tokenArray);
    // disambiguate assigned tags            
    finalSentence = disambiguator.disambiguate(finalSentence);

    return finalSentence;
  }
  
  /**
   * Get all rules for the current language that are built-in or that have been
   * added using {@link #addRule}.
   *  
   * @return a List of {@link Rule} objects
   */
  public List<Rule> getAllRules() {
    final List<Rule> rules = new ArrayList<Rule>();
    rules.addAll(builtinRules);
    rules.addAll(userRules);
    // Some rules have an internal state so they can do checks over sentence
    // boundaries. These need to be reset so the checks don't suddenly
    // work on different texts with the same data:
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
