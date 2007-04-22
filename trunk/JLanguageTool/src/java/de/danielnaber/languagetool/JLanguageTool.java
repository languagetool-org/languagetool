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

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

  public static final String VERSION = "0.9-dev";      // keep in sync with build.xml!

  public static final String RULES_DIR = "/rules";
  public static final String PATTERN_FILE = "grammar.xml";
  public static final String FALSE_FRIEND_FILE = "false-friends.xml";
  
  public static final String SENTENCE_START_TAGNAME = "SENT_START";
  public static final String SENTENCE_END_TAGNAME = "SENT_END";
  public static final String PARAGRAPH_END_TAGNAME = "PARA_END";

  private List<Rule> builtinRules = new ArrayList<Rule>();
  private List<Rule> userRules = new ArrayList<Rule>();     // rules added via addRule() method
  private Set<String> disabledRules = new HashSet<String>();
  
  private Language language = null;
  private Language motherTongue = null;
  private Disambiguator disambiguator = null;
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
    messages = getMessageBundle(language);
    Rule[] allBuiltinRules = getAllBuiltinRules(language, messages);
    for (int i = 0; i < allBuiltinRules.length; i++) {
      if (allBuiltinRules[i].supportsLanguage(language))
        builtinRules.add(allBuiltinRules[i]);
    }
    disambiguator = language.getDisambiguator();
    tagger = language.getTagger();
    sentenceTokenizer = language.getSentenceTokenizer();
    wordTokenizer = language.getWordTokenizer();
  }

  /**
   * Gets the ResourceBundle for the default language of the user's system.
   */
  public static ResourceBundle getMessageBundle() {
    try {
      return ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle");
    } catch (MissingResourceException e) {
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
    } catch (MissingResourceException e) {
      return ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle", Locale.ENGLISH);
    }
  }

  private Rule[] getAllBuiltinRules(Language language, ResourceBundle messages) {
    // use reflection to get a list of all non-pattern rules under
    // "de.danielnaber.languagetool.rules"
    // generic rules first, then language-specific ones
    // TODO: the order of loading classes are not guaranteed so we may want to implement rule
    // precedence

    List<Rule> rules = new ArrayList<Rule>();
    try {
      // we pass ".*Rule$" regexp to improve efficiency, see javadoc
      Class[] classes1 = ReflectionUtils.findClasses(Rule.class.getClassLoader(), 
          Rule.class.getPackage().getName(), ".*Rule$", 0, Rule.class, null);
      Class[] classes2 = ReflectionUtils.findClasses(Rule.class.getClassLoader(), 
          Rule.class.getPackage().getName() + "." + language.getShortName(),
          ".*Rule$", 0, Rule.class, null);

      List<Class> classes = new ArrayList<Class>();
      classes.addAll(Arrays.asList(classes1));
      classes.addAll(Arrays.asList(classes2));

      for (Class class1 : classes) {
        Constructor[] constructors = class1.getConstructors();
        for (Constructor constructor : constructors) {
          Class[] paramTypes = constructor.getParameterTypes();
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
    } catch (Exception e) {
      throw new RuntimeException("Failed to load rules: " + e.getMessage(), e);
    }
    //	System.err.println("Loaded " + rules.size() + " rules");
    return rules.toArray(new Rule[0]);
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
    PatternRuleLoader ruleLoader = new PatternRuleLoader();
    return ruleLoader.getRules(this.getClass().getResourceAsStream(filename), filename);
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
    String defaultPatternFilename = 
      RULES_DIR + "/" + language.getShortName() + "/" + PATTERN_FILE;
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
    String falseFriendRulesFilename =  RULES_DIR + "/" + FALSE_FRIEND_FILE;
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
   * Get rule ids of the rules that have been explicitly disabled.
   */
  public Set<String> getDisabledRules() {
    return disabledRules;
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
      
      if (sentenceCount == sentences.size()) {
        AnalyzedTokenReadings[] anTokens = analyzedText.getTokens();
        AnalyzedToken paragraphEnd =
          new AnalyzedToken(anTokens[anTokens.length - 1].getToken(),
              PARAGRAPH_END_TAGNAME,
              anTokens[anTokens.length - 1].getAnalyzedToken(0).getLemma(),
              anTokens[anTokens.length - 1].getAnalyzedToken(0).getStartPos());
        anTokens[anTokens.length - 1].addReading(paragraphEnd);
        analyzedText = new AnalyzedSentence(anTokens); 
      }
      
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
          String sentencePartToEndOfError = sentence.substring(0, thisMatches[i].getToPos());          
          int lastLineBreakPos = sentencePartToError.lastIndexOf('\n');
          int column = -1;
          int endColumn = -1;
          if (lastLineBreakPos == -1) {
            column = sentencePartToError.length() + columnCount;
          } else {
            column = sentencePartToError.length() - lastLineBreakPos - 1;
          }
          int lastLineBreakPosInError = sentencePartToEndOfError.lastIndexOf('\n');
          if (lastLineBreakPosInError == -1) {
            endColumn = sentencePartToEndOfError.length() + columnCount + 1;
          } else {
            endColumn = sentencePartToEndOfError.length() - lastLineBreakPos;
          }
          int lineBreaksToError = countLineBreaks(sentencePartToError);
          int lineBreaksToEndOfError = countLineBreaks(sentencePartToEndOfError);
          thisMatch.setLine(lineCount + lineBreaksToError);
          thisMatch.setEndLine(lineCount + lineBreaksToEndOfError);
          thisMatch.setColumn(column);
          thisMatch.setEndColumn(endColumn);
          thisMatch.setOffset(thisMatches[i].getFromPos() + tokenCount);
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
      int linebreakPos = sentence.indexOf('\n');
      if (linebreakPos == -1) {
        columnCount += sentence.length();
      } else {
        columnCount = sentence.length() - linebreakPos - 1;
      }
    }
    
    //removing false positives in paragraph-level rules
    for (Rule rule : allRules) {
      if (rule.isParagraphBackTrack()) {
        if (rule.getMatches() != null) {
        List <RuleMatch> rm = rule.getMatches();           
          for (RuleMatch r : rm) {
            if (rule.isInRemoved(r)) {
              ruleMatches.remove(r);
            }
          }
       }
     }          
    }
    
    return ruleMatches;
  }
  
  static int countLineBreaks(final String s) {
    int pos = -1;
    int count = 0;
    while (true) {
      int nextPos = s.indexOf('\n', pos + 1);
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
    List<AnalyzedTokenReadings> aTokens = tagger.tag(tokens);
    AnalyzedTokenReadings[] tokenArray = new AnalyzedTokenReadings[tokens.size() + 1];
    AnalyzedToken[] startTokenArray = new AnalyzedToken[1];  
    int toArrayCount = 0;
    AnalyzedToken sentenceStartToken = new AnalyzedToken("", SENTENCE_START_TAGNAME, 0);
    startTokenArray[0] = sentenceStartToken;
    tokenArray[toArrayCount++] = new AnalyzedTokenReadings(startTokenArray);
    int startPos = 0;
    for (AnalyzedTokenReadings posTag : aTokens) {
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
    AnalyzedToken sentenceEnd = 
      new AnalyzedToken(tokenArray[lastToken].getToken(), 
          SENTENCE_END_TAGNAME,
          tokenArray[lastToken].getAnalyzedToken(0).getLemma(),
          tokenArray[lastToken].getAnalyzedToken(0).getStartPos());
        tokenArray[lastToken].addReading(sentenceEnd);
        
    if (tokenArray.length == 2) {
    if (tokenArray[0].isSentStart() 
        && tokenArray[1].getToken().equals("\n")) {
      AnalyzedToken paragraphEnd =
      new AnalyzedToken(tokenArray[lastToken].getToken(),
          PARAGRAPH_END_TAGNAME,
          tokenArray[lastToken].getAnalyzedToken(0).getLemma(),
          tokenArray[lastToken].getAnalyzedToken(0).getStartPos());
      tokenArray[lastToken].addReading(paragraphEnd);        
      }
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
