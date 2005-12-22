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
import de.danielnaber.languagetool.rules.de.DashRule;
import de.danielnaber.languagetool.rules.de.WiederVsWiderRule;
import de.danielnaber.languagetool.rules.de.WordCoherencyRule;
import de.danielnaber.languagetool.rules.en.AvsAnRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

/**
 * The main class used for checking text against different rules:
 * <ul>
 *  <li>the built-in rules (<i>a</i> vs. <i>an</i>, whitespace after commas, ...)
 *  <li>pattern rules loaded from external XML files with {@link #loadPatternRules(String)}
 *  <li>our own implementation of the abstract {@link Rule} classes added with {@link #addRule(Rule)}
 * </ul>
 * @author Daniel Naber
 */
public class JLanguageTool {

  public final static String VERSION = "0.8.2dev";      // keep in sync with build.xml!

  public final static String RULES_DIR = "rules";
  public final static String PATTERN_FILE = "grammar.xml";
  
  public final static String SENTENCE_START_TAGNAME = "SENT_START";

  private List builtinRules = new ArrayList();
  private List userRules = new ArrayList();     // rules added via addRule() method
  private Set disabledRules = new HashSet();
  
  private static File basedir = null;
  private Language language = null;
  private Tagger tagger = null;
  private PrintStream printStream = null;
  
  private int sentenceCount = 0;

  // just for testing:
  /*private Rule[] allBuiltinRules = new Rule[] {
      new UppercaseSentenceStartRule()
  };*/

  /**
   * Create a JLanguageTool and setup the builtin rules appropriate for the
   * given language.
   * @throws IOException 
   */
  public JLanguageTool(Language language) throws IOException {
    this(language, null);
  }
  
  /**
   * Create a JLanguageTool and setup the builtin rules appropriate for the
   * given language.
   * @throws IOException 
   */
  public JLanguageTool(Language language, File basedirArg) throws IOException {
    if (language == null) {
      throw new NullPointerException("language cannot be null");
    }
    basedir = basedirArg;
    this.language = language;
    // TODO: use reflection to get a list of all non-pattern rules:
    Rule[] allBuiltinRules = new Rule[] { 
        // Several languages:
        new CommaWhitespaceRule(), 
        new WordRepeatRule(language),
        new WordCoherencyRule(),
        new DoublePunctuationRule(),
        // English:
        new AvsAnRule(),
        // German:
        new CaseRule(),
        new WiederVsWiderRule(),
        new AgreementRule(),
        new UppercaseSentenceStartRule(),
        new DashRule()
      };
    for (int i = 0; i < allBuiltinRules.length; i++) {
      if (allBuiltinRules[i].supportsLanguage(language))
        builtinRules.add(allBuiltinRules[i]);
    }
    tagger = language.getTagger();
  }
  
  /**
   * Set a PrintStream that will receive verbose output. Set
   * to <code>null</code> to disable verbose output.
   */
  public void setOutput(PrintStream printStream) {
    this.printStream = printStream;
  }

  public static File getAbsoluteFile(String relFilename) {
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
   * @return a List of {@link Rule} objects
   */
  public List loadPatternRules(String filename) throws ParserConfigurationException, SAXException, IOException {
    PatternRuleLoader ruleLoader = new PatternRuleLoader();
    return ruleLoader.getRules(filename);
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
    List patternRules = loadPatternRules(defaultPatternFilename);
    for (Iterator iter = patternRules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      addRule(rule);
    }
  }

  /**
   * Add a rule to be used by the next call to {@link #check}.
   */
  public void addRule(Rule rule) {
    userRules.add(rule);
  }

  /**
   * Disable a given rule so {@link #check} won't use it.
   * @param ruleId the id of the rule to disable
   */
  public void disableRule(String ruleId) {
    // TODO: check if such a rule exists
    disabledRules.add(ruleId);
  }

  /**
   * Re-enable a given rule so {@link #check} will use it.
   * @param ruleId the id of the rule to enable
   */
  public void enableRule(String ruleId) {
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
  public List check(String text) throws IOException {
    sentenceCount = 0;
    SentenceTokenizer sTokenizer = new SentenceTokenizer();
    List sentences = sTokenizer.tokenize(text);
    List ruleMatches = new ArrayList();
    List allRules = getAllRules();
    printIfVerbose(allRules.size() + " rules activated for language " + language);
    int tokenCount = 0;
    int lineCount = 0;
    int columnCount = 0;
    for (Iterator iter = sentences.iterator(); iter.hasNext();) {
      String sentence = (String) iter.next();
      sentenceCount++;
      AnalyzedSentence analyzedText = getAnalyzedSentence(sentence);
      List sentenceMatches = new ArrayList();
      printIfVerbose(analyzedText.toString());
      for (Iterator iterator = allRules.iterator(); iterator.hasNext();) {
        Rule rule = (Rule) iterator.next();
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
  
  static int countLineBreaks(String s) {
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
  public AnalyzedSentence getAnalyzedSentence(String sentence) throws IOException {
    WordTokenizer wtokenizer = new WordTokenizer();
    List tokens = wtokenizer.tokenize(sentence);
    List noWhitespaceTokens = new ArrayList();
    // whitespace confuses tagger, so give it the tokens but no whitespace tokens:
    for (Iterator iterator = tokens.iterator(); iterator.hasNext();) {
      String token = (String) iterator.next();
      if (!token.trim().equals("")) {
        noWhitespaceTokens.add(token);
      }
    }
    List aTokens = tagger.tag(noWhitespaceTokens);
    AnalyzedToken[] tokenArray = new AnalyzedToken[tokens.size()+1];
    int toArrayCount = 0;
    AnalyzedToken sentenceStartToken = new AnalyzedToken("", SENTENCE_START_TAGNAME, 0);
    tokenArray[toArrayCount++] = sentenceStartToken;
    int startPos = 0;
    int noWhitespaceCount = 0;
    for (Iterator iterator = tokens.iterator(); iterator.hasNext();) {
      String tokenStr = (String) iterator.next();
      AnalyzedToken posTag = null;
      if (!tokenStr.trim().equals("")) {
        posTag = (AnalyzedToken)aTokens.get(noWhitespaceCount);
        posTag.startPos = startPos;
        noWhitespaceCount++;
      } else {
        posTag = new AnalyzedToken(tokenStr, null, startPos);
      }
      tokenArray[toArrayCount++] = posTag;
      startPos += tokenStr.length();
    }
    return new AnalyzedSentence(tokenArray);
  }

  /**
   * Get all rules for the current language that are built-in or that have been
   * added using {@link #addRule}.
   *  
   * @return a List of {@link Rule} objects
   */
  public List getAllRules() {
    List rules = new ArrayList();
    rules.addAll(builtinRules);
    rules.addAll(userRules);
    // Some rules have an internal state so they can do checks over sentence
    // boundaries. These need to be reset so the checks don't suddendly
    // work on different texts with the same data:
    for (Iterator iter = rules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      rule.reset();
    }
    return rules;
  }
  
  /**
   * Number of sentences the latest call to check() has checked.
   */
  int getSentenceCount() {
    return sentenceCount;
    
  }

  private void printIfVerbose(String s) {
    if (printStream != null)
      printStream.println(s);
  }
  
}
