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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.en.AvsAnRule;
import de.danielnaber.languagetool.rules.en.CommaWhitespaceRule;
import de.danielnaber.languagetool.rules.patterns.PatternRuleLoader;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

/**
 * The main class used for checking text against different rules:
 * <ul>
 *  <li>the built-in rules (<i>a</i> vs. <i>an</i>, whitespace after commas)
 *  <li>pattern rules loaded from external XML files with {@link #loadPatternRules(String)}
 *  <li>our own implementation of the abstract {@link Rule} classes added with {@link #addRule(Rule)}
 * </ul>
 * @author Daniel Naber
 */
public class JLanguageTool {
  
  private final static String DEFAULT_GRAMMAR_RULES = "rules" +File.separator+ "en" +File.separator+ "grammar.xml";
  
  private Rule[] builtinRules = new Rule[]{};
  private List userRules = new ArrayList();     // rules added via addRule() method
  private Set disabledRules = new HashSet();

  /**
   * Create a JLanguageTool and setup the builtin rules.
   * @throws IOException
   */
  public JLanguageTool() throws IOException {
    builtinRules = new Rule[] {new AvsAnRule(), new CommaWhitespaceRule()};
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

  public List check(String filename) throws IOException {
    String s = readFile(filename);
    SentenceTokenizer sTokenizer = new SentenceTokenizer();
    WordTokenizer wtokenizer = new WordTokenizer();
    List sentences = sTokenizer.tokenize(s);
    List ruleMatches = new ArrayList();
    List allRules = getallRules();
    int tokenCount = 0;
    for (Iterator iter = sentences.iterator(); iter.hasNext();) {
      String sentence = (String) iter.next();
      //System.out.println(">>"+sentence+"<<");
      List tokens = wtokenizer.tokenize(sentence);
      AnalyzedSentence analyzedText = new AnalyzedSentence(tokens);
      for (Iterator iterator = allRules.iterator(); iterator.hasNext();) {
        Rule rule = (Rule) iterator.next();
        if (disabledRules.contains(rule.getId()))
          continue;
        RuleMatch[] thisMatches = rule.match(analyzedText);
        for (int i = 0; i < thisMatches.length; i++) {
          // change psoitions so they are relative to the complete text,
          // not just to the sentence:
          System.err.println("OLDMATCH="+thisMatches[i]);
          RuleMatch thisMatch = new RuleMatch(thisMatches[i].getRule(),
              thisMatches[i].getFromPos() + tokenCount,
              thisMatches[i].getToPos() + tokenCount,
              thisMatches[i].getMessage());
          System.err.println("NEWMATCH="+thisMatch);
          ruleMatches.add(thisMatch);
        }
      }
      tokenCount += sentence.length();
    }
    // FIXME: ...tagger
    return ruleMatches;
  }
  
  private List getallRules() {
    List rules = new ArrayList();
    rules.addAll(Arrays.asList(builtinRules));
    rules.addAll(userRules);
    return rules;
  }

  private String readFile(String filename) throws IOException {
    FileReader fr = null;
    BufferedReader br = null;
    StringBuffer sb = new StringBuffer();
    try {
      fr = new FileReader(filename);
      br = new BufferedReader(fr);
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
    } finally {
      if (br != null) br.close();
      if (fr != null) fr.close();
    }
    return sb.toString();
  }

  private String getContext(int fromPos, int toPos, String fileContents) {
    // calculate context region:
    final int contextSize = 15;
    int startContent = fromPos - contextSize;
    String prefix = "...";
    String postfix = "...";
    String markerPrefix = "   ";
    if (startContent < 0) {
      prefix = "";
      markerPrefix = "";
      startContent = 0;
    }
    int endContent = toPos + contextSize;
    if (endContent > fileContents.length()) {
      postfix = "";
      endContent = fileContents.length();
    }
    // make "^" marker. inefficient but robust implementation:
    StringBuffer marker = new StringBuffer();
    for (int i = 0; i < fileContents.length() + prefix.length(); i++) {
      if (i >= fromPos && i < toPos)
        marker.append("^");
      else
        marker.append(" ");
    }
    // now build context string plus marker:
    StringBuffer sb = new StringBuffer();
    sb.append(prefix);
    sb.append(fileContents.substring(startContent, endContent));
    sb.append(postfix);
    sb.append("\n");
    sb.append(markerPrefix + marker.substring(startContent, endContent));
    return sb.toString();
  }
  
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    if (args.length < 1) {
      System.out.println("Usage: java JLanguageTool <file>");
      System.exit(1);
    }
    JLanguageTool lt = new JLanguageTool();
    List patternRules = lt.loadPatternRules(DEFAULT_GRAMMAR_RULES);
    for (Iterator iter = patternRules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      lt.addRule(rule);
    }
    String filename = args[0];
    List ruleMatches = lt.check(filename);
    String fileContents = lt.readFile(filename);
    for (Iterator iter = ruleMatches.iterator(); iter.hasNext();) {
      RuleMatch match = (RuleMatch) iter.next();
      System.out.println(match);
      System.out.println(lt.getContext(match.getFromPos(), match.getToPos(), fileContents));
      if (iter.hasNext())
        System.out.println();
    }
  }

}
