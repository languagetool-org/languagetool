/* JLanguageTool, a natural language style checker 
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import opennlp.grok.preprocess.postag.EnglishPOSTaggerME;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.rules.CommaWhitespaceRule;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.WordRepeatRule;
import de.danielnaber.languagetool.rules.en.AvsAnRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;
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
  
  private List builtinRules = new ArrayList();
  private List userRules = new ArrayList();     // rules added via addRule() method
  private Set disabledRules = new HashSet();
  
  private Language language;
  private PrintStream printStream = null;

  EnglishPOSTaggerME tagger = null;
  
  /**
   * Create a JLanguageTool and setup the builtin rules.
   * @throws IOException
   */
  public JLanguageTool(Language language) throws IOException {
    if (language == null) {
      throw new NullPointerException("language cannot be null");
    }
    this.language = language;
    // TODO: use reflection to get a list of all non-pattern rules:
    Rule[] allBuiltinRules = new Rule[] {new AvsAnRule(), new CommaWhitespaceRule(), new WordRepeatRule()};
    for (int i = 0; i < allBuiltinRules.length; i++) {
      if (allBuiltinRules[i].supportsLanguage(language))
      builtinRules.add(allBuiltinRules[i]); 
    }
    tagger = new EnglishPOSTaggerME();
  }
  
  /**
   * Set a PrintStream that will receive verbose output. Set
   * to <code>null</code> to disable verbose output.
   */
  public void setOutput(PrintStream printStream) {
    this.printStream = printStream;
  }

  /**
   * Load pattern rules from an XML file.
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

  public List check(String s) {
    SentenceTokenizer sTokenizer = new SentenceTokenizer();
    List sentences = sTokenizer.tokenize(s);
    List ruleMatches = new ArrayList();
    List allRules = getAllRules();
    print(allRules.size() + " rules activated for language " + language);
    int tokenCount = 0;
    for (Iterator iter = sentences.iterator(); iter.hasNext();) {
      String sentence = (String) iter.next();
      AnalyzedSentence analyzedText = getAnalyzedText(sentence);
      print(analyzedText.toString());
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
          ruleMatches.add(thisMatch);
        }
      }
      tokenCount += sentence.length();
    }
    return ruleMatches;
  }
  
  public AnalyzedSentence getAnalyzedText(String sentence) {
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
    List posTags = tagger.tag(noWhitespaceTokens);
    AnalyzedToken[] tokenArray = new AnalyzedToken[tokens.size()];
    int toArrayCount = 0;
    int startPos = 0;
    int noWhitespaceCount = 0;
    for (Iterator iterator = tokens.iterator(); iterator.hasNext();) {
      String tokenStr = (String) iterator.next();
      String posTag = null;
      if (!tokenStr.trim().equals("")) {
        posTag = (String)posTags.get(noWhitespaceCount);
        noWhitespaceCount++;
      }
      tokenArray[toArrayCount] = new AnalyzedToken(tokenStr, posTag, startPos);
      toArrayCount++;
      startPos += tokenStr.length();
    }
    return new AnalyzedSentence(tokenArray);
  }

  private List getAllRules() {
    List rules = new ArrayList();
    rules.addAll(builtinRules);
    rules.addAll(userRules);
    return rules;
  }

  private void print(String s) {
    if (printStream != null)
      printStream.println(s);
  }
  
}
