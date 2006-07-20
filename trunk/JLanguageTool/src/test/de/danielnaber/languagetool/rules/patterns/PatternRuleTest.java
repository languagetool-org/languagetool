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
package de.danielnaber.languagetool.rules.patterns;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

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

  public void testGrammarRulesFromXML() throws IOException, ParserConfigurationException, SAXException {
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      Language lang = Language.LANGUAGES[i];
      PatternRuleLoader ruleLoader = new PatternRuleLoader();
      JLanguageTool languageTool = new JLanguageTool(lang);
      List rules = ruleLoader.getRules("rules" + File.separator
          + lang.getShortName() + File.separator + "grammar.xml");
      testGrammarRulesFromXML(rules, languageTool);
    }
  }
  
  private void testGrammarRulesFromXML(List rules, JLanguageTool languageTool) throws IOException {
    for (Iterator iter = rules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      List goodSentences = rule.getCorrectExamples();
      for (Iterator iterator = goodSentences.iterator(); iterator.hasNext();) {
        String goodSentence = (String) iterator.next();
        assertTrue(goodSentence.trim().length() > 0);
        assertFalse("Did not expect error in: " + goodSentence + " (ID="+rule.getId()+")",
            match(rule, goodSentence, languageTool));
      }
      List badSentences = rule.getIncorrectExamples();
      for (Iterator iterator = badSentences.iterator(); iterator.hasNext();) {
        String badSentence = (String) iterator.next();
        assertTrue(badSentence.trim().length() > 0);
        assertTrue("Did expect error in: " + badSentence + " (ID="+rule.getId()+")",
            match(rule, badSentence, languageTool));
      }
    }
  }
  
  private boolean match(Rule rule, String sentence, JLanguageTool languageTool) throws IOException {
    AnalyzedSentence text = languageTool.getAnalyzedSentence(sentence);
    //System.err.println(text);
    RuleMatch[] matches = rule.match(text);
    /*for (int i = 0; i < matches.length; i++) {
      System.err.println(matches[i]);
    }*/
    return matches.length > 0;
  }

/*  public void testRule() throws IOException {
    PatternRule pr;
    RuleMatch[] matches;

    pr = makePatternRule("\"one\"");
    matches = pr.match(langTool.getAnalyzedSentence("A non-matching sentence."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("A matching sentence with one match."));
    assertEquals(1, matches.length);
    assertEquals(25, matches[0].getFromPos());
    assertEquals(28, matches[0].getToPos());
    // these two are not set if the rule is called standalone (not via JLanguageTool):
    assertEquals(-1, matches[0].getColumn());
    assertEquals(-1, matches[0].getLine());
    assertEquals("ID1", matches[0].getRule().getId());
    assertTrue(matches[0].getMessage().equals("user visible message"));
    matches = pr.match(langTool.getAnalyzedSentence("one one and one: three matches"));
    assertEquals(3, matches.length);

    pr = makePatternRule("\"one\" \"two\"");
    matches = pr.match(langTool.getAnalyzedSentence("this is one not two"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("this is two one"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("this is one two three"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("one two"));
    assertEquals(1, matches.length);
    
    pr = makePatternRule("\"one|foo|xxxx\" \"two\"");
    matches = pr.match(langTool.getAnalyzedSentence("one foo three"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("one two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("foo two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("one foo two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("y x z one two blah foo"));
    assertEquals(1, matches.length);

    pr = makePatternRule("\"one|foo|xxxx\" \"two|yyy\"");
    matches = pr.match(langTool.getAnalyzedSentence("one, yyy"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("one yyy"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("xxxx two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("xxxx yyy"));
    assertEquals(1, matches.length);
  }*/

/*  private PatternRule makePatternRule(String s) {
    return makePatternRule(s, false);
  }*/

/*  private PatternRule makePatternRule(String s, boolean caseSensitive) {
    PatternRule rule = new PatternRule("ID1", Language.ENGLISH, s, "test rule", "user visible message");
    rule.setCaseSensitive(caseSensitive);
    return rule;
  }*/

/*  public void testSentenceStart() throws IOException {
    PatternRule pr;
    RuleMatch[] matches;

    pr = makePatternRule("SENT_START \"One\"");
    matches = pr.match(langTool.getAnalyzedSentence("Not One word."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("One word."));
    assertEquals(1, matches.length);
  }*/

  
/*public void testNegation() throws IOException {
    PatternRule pr;
    RuleMatch[] matches;

    pr = makePatternRule("\"one\" ^\"two\"");
    matches = pr.match(langTool.getAnalyzedSentence("Here's one two."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("Here's one four."));
    assertEquals(1, matches.length);

    pr = makePatternRule("\"one\" ^\"two|three\"");
    matches = pr.match(langTool.getAnalyzedSentence("Here's one two."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("Here's one three."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("Here's one four."));
    assertEquals(1, matches.length);

    pr = makePatternRule("SENT_START ^\"One\"");
    matches = pr.match(langTool.getAnalyzedSentence("One two."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("Two three."));
    assertEquals(1, matches.length);

    pr = makePatternRule("\"One\" ^CD");
    matches = pr.match(langTool.getAnalyzedSentence("One two."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("One walk."));
    assertEquals(1, matches.length);

    pr = makePatternRule("CD^one \"foo\"");
    matches = pr.match(langTool.getAnalyzedSentence("One foo."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("Two foo."));
    assertEquals(1, matches.length);

    pr = makePatternRule("CD^one|three|five \"foo\"");
    matches = pr.match(langTool.getAnalyzedSentence("One foo."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("Three foo."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("Five foo."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("Eight foo."));
    assertEquals(1, matches.length);

    pr = makePatternRule("CD^one \"foo\"", true);
    matches = pr.match(langTool.getAnalyzedSentence("One foo."));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("the one foo."));
    assertEquals(0, matches.length);
  }
*/
}
