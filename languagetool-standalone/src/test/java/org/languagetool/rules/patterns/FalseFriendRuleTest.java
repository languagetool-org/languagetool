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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.*;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;
import org.languagetool.rules.en.MorfologikBritishSpellerRule;

public class FalseFriendRuleTest {

  @Test
  @Ignore("not active for German anymore - repalced by ngram-based false friend rule")
  public void testHintsForGermanSpeakers() throws IOException {
    JLanguageTool lt = new JLanguageTool(new English(), new German());
    List<RuleMatch> matches = assertErrors(1, "We will berate you.", lt);
    assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[provide advice, give advice]");
    assertErrors(0, "We will give you advice.", lt);
    assertErrors(1, "I go to high school in Foocity.", lt);
    List<RuleMatch> matches2 = assertErrors(1, "The chef", lt);
    assertEquals("[boss, chief]", matches2.get(0).getSuggestedReplacements().toString());
  }

  @Test
  @Ignore("not active for German anymore - repalced by ngram-based false friend rule")
  public void testHintsForGermanSpeakersWithVariant() throws IOException {
    JLanguageTool lt = new JLanguageTool(new BritishEnglish(), new SwissGerman());
    List<RuleMatch> matches = assertErrors(1, "We will berate you.", lt);
    assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[provide advice, give advice]");
    assertErrors(0, "We will give you advice.", lt);
    assertErrors(1, "I go to high school in Berlin.", lt);
    List<RuleMatch> matches2 = assertErrors(1, "The chef", lt);
    assertEquals("[boss, chief]", matches2.get(0).getSuggestedReplacements().toString());
  }

  @Test
  public void testHintsForDemoLanguage() throws IOException {
    JLanguageTool lt1 = new JLanguageTool(new BritishEnglish(), new Italian());
    lt1.disableRule(MorfologikBritishSpellerRule.RULE_ID);
    List<RuleMatch> matches1 = assertErrors(1, "And forDemoOnly.", lt1);
    assertEquals("DEMO_ENTRY", matches1.get(0).getRule().getId());

    JLanguageTool lt2 = new JLanguageTool(new English(), new Italian());
    lt2.disableRule(MorfologikBritishSpellerRule.RULE_ID);
    List<RuleMatch> matches2 = assertErrors(1, "And forDemoOnly.", lt2);
    assertEquals("DEMO_ENTRY", matches2.get(0).getRule().getId());

    JLanguageTool lt3 = new JLanguageTool(new AmericanEnglish(), new Italian());
    lt3.disableRule(MorfologikAmericanSpellerRule.RULE_ID);
    assertErrors(0, "And forDemoOnly.", lt3);
  }

  @Test
  public void testHintsForEnglishSpeakers() throws IOException {
    JLanguageTool lt = new JLanguageTool(new German(), new English());
    assertErrors(1, "Man sollte ihn nicht so beraten.", lt);
    assertErrors(0, "Man sollte ihn nicht so beschimpfen.", lt);
    assertErrors(1, "Ich gehe in Blubbstadt zur Hochschule.", lt);
  }

  @Test
  public void testHintsForPolishSpeakers() throws IOException {
    JLanguageTool lt = new JLanguageTool(new English() {
      @Override
      protected synchronized List<AbstractPatternRule> getPatternRules() {
        return Collections.emptyList();
      }
    }, new Polish());
    assertErrors(1, "This is an absurd.", lt);
    assertErrors(0, "This is absurdity.", lt);
    assertSuggestions(0, "This is absurdity.", lt);
    assertErrors(1, "I have to speak to my advocate.", lt);
    assertSuggestions(3, "My brother is politic.", lt);
  }
  
  private List<RuleMatch> assertErrors(int errorCount, String s, JLanguageTool lt) throws IOException {
    List<RuleMatch> matches = lt.check(s);
    //System.err.println(matches);
    assertEquals("Matches found: " + matches, errorCount, matches.size());
    return matches;
  }
  
  private void assertSuggestions(int suggestionCount, String text, JLanguageTool lt) throws IOException {
    List<RuleMatch> matches = lt.check(text);
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
