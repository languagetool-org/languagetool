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
package de.danielnaber.languagetool.rules.patterns;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Daniel Naber
 */
public class PatternRuleTest extends TestCase {

  private static JLanguageTool langTool = null;
  
  public void setUp() throws IOException {
    if (langTool == null)
      langTool = new JLanguageTool(Language.ENGLISH);
  }

  public void testEnglishGrammarRulesFromXML() throws IOException, ParserConfigurationException, SAXException {
    PatternRuleLoader ruleLoader = new PatternRuleLoader();
    List rules = ruleLoader.getRules("rules/en/grammar.xml");
    for (Iterator iter = rules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      String goodSentence = rule.getCorrectExample();
      String badSentence = rule.getIncorrectExample();
      assertTrue(goodSentence.trim().length() > 0);
      assertTrue(badSentence.trim().length() > 0);
      assertFalse("Did not expect error in: " + goodSentence, match(rule, goodSentence));
      assertTrue("Did  expect error in: " + badSentence, match(rule, badSentence));
    }
  }
  
  private boolean match(Rule rule, String sentence) {
    AnalyzedSentence text = langTool.getAnalyzedText(sentence);
    //System.err.println(text);
    RuleMatch[] matches = rule.match(text);
    /*for (int i = 0; i < matches.length; i++) {
      System.err.println(matches[i]);
    }*/
    return matches.length > 0;
  }

  public void testRule() {
    PatternRule pr;
    RuleMatch[] matches;

    pr = new PatternRule("ID1", Language.ENGLISH, "\"one\"", "test rule");
    matches = pr.match(langTool.getAnalyzedText("A non-matching sentence."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedText("A matching sentence with one match."));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedText("one one and one: three matches"));
    assertEquals(3, matches.length);

    pr = new PatternRule("ID1", Language.ENGLISH, "\"one\" \"two\"", "test rule");
    matches = pr.match(langTool.getAnalyzedText("this is one not two"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedText("this is two one"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedText("this is one two three"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedText("one two"));
    assertEquals(1, matches.length);
    
    pr = new PatternRule("ID1", Language.ENGLISH, "\"one|foo|xxxx\" \"two\"", "test rule");
    matches = pr.match(langTool.getAnalyzedText("one foo three"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedText("one two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedText("foo two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedText("one foo two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedText("y x z one two blah foo"));
    assertEquals(1, matches.length);

    pr = new PatternRule("ID1", Language.ENGLISH, "\"one|foo|xxxx\" \"two|yyy\"", "test rule");
    matches = pr.match(langTool.getAnalyzedText("one, yyy"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedText("one yyy"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedText("xxxx two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedText("xxxx yyy"));
    assertEquals(1, matches.length);
  }
  
}
