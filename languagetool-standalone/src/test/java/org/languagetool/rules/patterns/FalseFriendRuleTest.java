/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.*;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;
import org.languagetool.rules.en.MorfologikBritishSpellerRule;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FalseFriendRuleTest {

  @Test
  public void testHintsForGermanSpeakers() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool = new JLanguageTool(new English(), new German());
    List<RuleMatch> matches = assertErrors(1, "We will berate you.", langTool);
    assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[provide advice, give advice]");
    assertErrors(0, "We will give you advice.", langTool);
    assertErrors(1, "I go to high school in Foocity.", langTool);
    List<RuleMatch> matches2 = assertErrors(1, "The chef", langTool);
    assertEquals("[boss, chief]", matches2.get(0).getSuggestedReplacements().toString());
  }

  @Test
  public void testHintsForGermanSpeakersWithVariant() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool = new JLanguageTool(new BritishEnglish(), new SwissGerman());
    List<RuleMatch> matches = assertErrors(1, "We will berate you.", langTool);
    assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[provide advice, give advice]");
    assertErrors(0, "We will give you advice.", langTool);
    assertErrors(1, "I go to high school in Berlin.", langTool);
    List<RuleMatch> matches2 = assertErrors(1, "The chef", langTool);
    assertEquals("[boss, chief]", matches2.get(0).getSuggestedReplacements().toString());
  }

  @Test
  public void testHintsForDemoLanguage() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool1 = new JLanguageTool(new BritishEnglish(), new German());
    langTool1.disableRule(MorfologikBritishSpellerRule.RULE_ID);
    List<RuleMatch> matches1 = assertErrors(1, "And forDemoOnly.", langTool1);
    assertEquals("DEMO_ENTRY", matches1.get(0).getRule().getId());

    JLanguageTool langTool2 = new JLanguageTool(new English(), new German());
    langTool2.disableRule(MorfologikBritishSpellerRule.RULE_ID);
    List<RuleMatch> matches2 = assertErrors(1, "And forDemoOnly.", langTool2);
    assertEquals("DEMO_ENTRY", matches2.get(0).getRule().getId());

    JLanguageTool langTool3 = new JLanguageTool(new AmericanEnglish(), new German());
    langTool3.disableRule(MorfologikAmericanSpellerRule.RULE_ID);
    assertErrors(0, "And forDemoOnly.", langTool3);
  }

  @Test
  public void testHintsForEnglishSpeakers() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool = new JLanguageTool(new German(), new English());
    assertErrors(1, "Man sollte ihn nicht so beraten.", langTool);
    assertErrors(0, "Man sollte ihn nicht so beschimpfen.", langTool);
    assertErrors(1, "Ich gehe in Blubbstadt zur Hochschule.", langTool);
  }

  @Test
  public void testHintsForPolishSpeakers() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool = new JLanguageTool(new English() {
      @Override
      protected synchronized List<AbstractPatternRule> getPatternRules() {
        return Collections.emptyList();
      }
    }, new Polish());
    assertErrors(1, "This is an absurd.", langTool);
    assertErrors(0, "This is absurdity.", langTool);
    assertSuggestions(0, "This is absurdity.", langTool);
    assertErrors(1, "I have to speak to my advocate.", langTool);
    assertSuggestions(3, "My brother is politic.", langTool);
  }
  
  private List<RuleMatch> assertErrors(int errorCount, String s, JLanguageTool langTool) throws IOException {
    List<RuleMatch> matches = langTool.check(s);
    //System.err.println(matches);
    assertEquals("Matches found: " + matches, errorCount, matches.size());
    return matches;
  }
  
  private void assertSuggestions(int suggestionCount, String text, JLanguageTool langTool) throws IOException {
    List<RuleMatch> matches = langTool.check(text);
    int suggestionsFound = 0;
    for (RuleMatch match : matches) {
      int pos = 0;
      while (pos != -1) {
        pos = match.getMessage().indexOf("<suggestion>", pos + 1);
        suggestionsFound ++;
      }       
    }
    if (suggestionsFound > 0) {
      suggestionsFound--;
    }
    assertEquals(suggestionCount, suggestionsFound);
  }
  
}
