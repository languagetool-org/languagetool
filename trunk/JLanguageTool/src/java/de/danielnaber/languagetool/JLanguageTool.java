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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.rules.CommaWhitespaceRule;
import de.danielnaber.languagetool.rules.DoublePunctuationRule;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.UppercaseSentenceStartRule;
import de.danielnaber.languagetool.rules.WordRepeatRule;
import de.danielnaber.languagetool.rules.de.AgreementRule;
import de.danielnaber.languagetool.rules.de.CaseRule;
import de.danielnaber.languagetool.rules.de.CompoundRule;
import de.danielnaber.languagetool.rules.de.DashRule;
import de.danielnaber.languagetool.rules.de.WiederVsWiderRule;
import de.danielnaber.languagetool.rules.de.WordCoherencyRule;
import de.danielnaber.languagetool.rules.en.AvsAnRule;
import de.danielnaber.languagetool.rules.patterns.FalseFriendRuleLoader;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;
import de.danielnaber.languagetool.rules.pl.PolishWordRepeatRule;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tokenizers.Tokenizer;

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
public class JLanguageTool {

  public static final String VERSION = "0.8.7-dev";      // keep in sync with build.xml!

  public static final String RULES_DIR = "rules";
  public static final String PATTERN_FILE = "grammar.xml";
  public static final String FALSE_FRIEND_FILE = "false-friends.xml";
  
  public static final String SENTENCE_START_TAGNAME = "SENT_START";

  private List<Rule> builtinRules = new ArrayList<Rule>();
  private List<Rule> userRules = new ArrayList<Rule>();     // rules added via addRule() method
  private Set<String> disabledRules = new HashSet<String>();
  
  private static File basedir = null;

  private Language language = null;
  private Language motherTongue = null;
  private Tagger tagger = null;
  private Tokenizer sentenceTokenizer = null;
  private Tokenizer wordTokenizer = null;

  private PrintStream printStream = null;
  
  private int sentenceCount = 0;
  
  private ResourceBundle messages = null;

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
    this(language, null, null);
  }

  /**
   * Create a JLanguageTool and setup the built-in rules appropriate for the
   * given language, ignoring false friend hints.
   * @param basedirArg the installation directory of LanguageTool
   * @throws IOException 
   */
  public JLanguageTool(final Language language, final File basedirArg) throws IOException {
    this(language, null, basedirArg);
  }

  /**
   * Create a JLanguageTool and setup the built-in rules appropriate for the
   * given language.
   * @param language the text language
   * @param motherTongue the user's mother tongue or <code>null</code>
   * @throws IOException 
   */
  public JLanguageTool(final Language language, final Language motherTongue) throws IOException {
    this(language, motherTongue, null);
  }

  /**
   * Create a JLanguageTool and setup the built-in rules appropriate for the
   * given language.
   * @param motherTongue the user's mother tongue or <code>null</code>
   * @param basedirArg the installation directory of LanguageTool
   * @throws IOException 
   */
  public JLanguageTool(final Language language, final Language motherTongue, final File basedirArg) throws IOException {
    if (language == null) {
      throw new NullPointerException("language cannot be null");
    }
    basedir = basedirArg;
    this.language = language;
    this.motherTongue = motherTongue;
    messages = getMessageBundle(language);
    Rule[] allBuiltinRules = getAllBuiltinRules(messages);
    for (int i = 0; i < allBuiltinRules.length; i++) {
      if (allBuiltinRules[i].supportsLanguage(language))
        builtinRules.add(allBuiltinRules[i]);
    }
    tagger = language.getTagger();
    sentenceTokenizer = language.getSentenceTokenizer();
    wordTokenizer = language.getWordTokenizer();
  }

  /**
   * Gets the ResourceBundle for the default language of the user's system.
   */
  public static ResourceBundle getMessageBundle() {
    return ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle");
  }

  /**
   * Gets the ResourceBundle for the given user interface language.
   */
  private static ResourceBundle getMessageBundle(final Language lang) {
    return ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle",
        lang.getLocale());
  }

  private Rule[] getAllBuiltinRules(ResourceBundle messages) throws IOException {
    // TODO: use reflection to get a list of all non-pattern rules?:
    Rule[] allBuiltinRules = new Rule[] { 
        // Several languages:
        new CommaWhitespaceRule(messages), 
        new WordRepeatRule(messages, language),
        new DoublePunctuationRule(messages),
        new UppercaseSentenceStartRule(messages, language),
        // English:
        new AvsAnRule(messages),        
        // German:
        new WordCoherencyRule(messages),
        new CaseRule(messages),
        new WiederVsWiderRule(messages),
        new AgreementRule(messages),
        new DashRule(messages),
        new CompoundRule(messages),
        // Polish:
        new PolishWordRepeatRule(messages)
      };
    return allBuiltinRules;
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
   */
  public static File getAbsoluteFile(final String relFilename) {
    if (basedir == null)
      return new File(relFilename);
    return new File(basedir, relFilename);
  }

  /**
   * Load pattern rules from an XML file. Use {@link #addRule} to add
   * these rules to the checking process.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @return a List of {@link PatternRule} objects
   */
  public List<PatternRule> loadPatternRules(final String filename) throws ParserConfigurationException, SAXException, IOException {
    PatternRuleLoader ruleLoader = new PatternRuleLoader();
    return ruleLoader.getRules(filename);
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
    if (motherTongue == null)
      return new ArrayList<PatternRule>();
    FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader();
    return ruleLoader.getRules(filename, language, motherTongue);
  }

  /**
   * Loads and activates the pattern rules from <code>rules/&lt;language&gt;/grammar.xml</code>.
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public void activateDefaultPatternRules() throws ParserConfigurationException, SAXException, IOException {
    String defaultPatternFilename = 
      RULES_DIR +File.separator+ language.getShortName() +File.separator+ PATTERN_FILE;
    List<PatternRule> patternRules = loadPatternRules(defaultPatternFilename);
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
    String falseFriendRulesFilename =  RULES_DIR +File.separator+ FALSE_FRIEND_FILE;
    List<PatternRule> patternRules = loadFalseFriendRules(falseFriendRulesFilename);
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
    List<String> sentences = sentenceTokenizer.tokenize(text);
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    List<Rule> allRules = getAllRules();
    printIfVerbose(allRules.size() + " rules activated for language " + language);
    int tokenCount = 0;
    int lineCount = 0;
    int columnCount = 0;
    for (Iterator<String> iter = sentences.iterator(); iter.hasNext();) {
      String sentence = iter.next();
      sentenceCount++;
      AnalyzedSentence analyzedText = getAnalyzedSentence(sentence);
      List<RuleMatch> sentenceMatches = new ArrayList<RuleMatch>();
      printIfVerbose(analyzedText.toString());
      for (Iterator<Rule> iterator = allRules.iterator(); iterator.hasNext();) {
        Rule rule = iterator.next();
        if (disabledRules.contains(rule.getId()))
          continue;
        RuleMatch[] thisMatches = rule.match(analyzedText);
        for (int i = 0; i < thisMatches.length; i++) {
          // change positions so they are relative to the complete text,
          // not just to the sentence:
          RuleMatch thisMatch = new RuleMatch(thisMatches[i].getRule(),
              thisMatches[i].getFromPos() + tokenCount,
              thisMatches[i].getToPos() + tokenCount,
              thisMatches[i].getMessage());
          thisMatch.setSuggestedReplacements(thisMatches[i].getSuggestedReplacements());
          String sentencePartToError = sentence.substring(0, thisMatches[i].getFromPos());
          int lastLineBreakPos = sentencePartToError.lastIndexOf("\n");
          int column = -1;
          if (lastLineBreakPos == -1) {
            column = sentencePartToError.length() + columnCount;
          } else {
            column = sentencePartToError.length() - lastLineBreakPos - 1;
          }
          thisMatch.setLine(lineCount + countLineBreaks(sentencePartToError));
          thisMatch.setColumn(column);
          sentenceMatches.add(thisMatch);
        }
      }
      Collections.sort(sentenceMatches);
      ruleMatches.addAll(sentenceMatches);
      tokenCount += sentence.length();
      lineCount += countLineBreaks(sentence);
      // calculate matching column:
      int linebreakPos = sentence.indexOf("\n");
      if (linebreakPos == -1) {
        columnCount += sentence.length();
      } else {
        columnCount = sentence.length() - linebreakPos - 1;
      }
    }
    return ruleMatches;
  }
  
  static int countLineBreaks(final String s) {
    int pos = -1;
    int count = 0;
    while (true) {
      int nextPos = s.indexOf("\n", pos+1);
      if (nextPos == -1)
        break;
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
    List<String> tokens = wordTokenizer.tokenize(sentence);
    List<String> noWhitespaceTokens = new ArrayList<String>();
    // whitespace confuses tagger, so give it the tokens but no whitespace tokens:
    for (String token : tokens) {
      if (isWord(token)) {
        noWhitespaceTokens.add(token);
      }
    }
    List<AnalyzedTokenReadings> aTokens = tagger.tag(noWhitespaceTokens);
    AnalyzedTokenReadings[] tokenArray = new AnalyzedTokenReadings[tokens.size()+1];
    AnalyzedToken[] startTokenArray = new AnalyzedToken[1];  
    int toArrayCount = 0;
    AnalyzedToken sentenceStartToken = new AnalyzedToken("", SENTENCE_START_TAGNAME, 0);
    startTokenArray[0]=sentenceStartToken;
    tokenArray[toArrayCount++]=new AnalyzedTokenReadings(startTokenArray);
    int startPos = 0;
    int noWhitespaceCount = 0;
    for (String tokenStr : tokens) {
      AnalyzedTokenReadings posTag = null;
      if (isWord(tokenStr)) {      
        posTag = (AnalyzedTokenReadings)aTokens.get(noWhitespaceCount);
        posTag.startPos = startPos;
        noWhitespaceCount++;
      } else {
        posTag = (AnalyzedTokenReadings)tagger.createNullToken(tokenStr, startPos); 
      }
      tokenArray[toArrayCount++] = posTag;
      startPos += tokenStr.length();
    }
    return new AnalyzedSentence(tokenArray);
  }

  private boolean isWord(final String token) {
    for (int i = 0; i < token.length(); i++) {
      char c = token.charAt(i);
      if (Character.isLetter(c) || Character.isDigit(c))
        return true;
    }
    return false;
  }

  /**
   * Get all rules for the current language that are built-in or that have been
   * added using {@link #addRule}.
   *  
   * @return a List of {@link Rule} objects
   */
  public List<Rule> getAllRules() {
    List<Rule> rules = new ArrayList<Rule>();
    rules.addAll(builtinRules);
    rules.addAll(userRules);
    // Some rules have an internal state so they can do checks over sentence
    // boundaries. These need to be reset so the checks don't suddendly
    // work on different texts with the same data:
    for (Rule rule : rules) {
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
    if (printStream != null)
      printStream.println(s);
  }
  
}
