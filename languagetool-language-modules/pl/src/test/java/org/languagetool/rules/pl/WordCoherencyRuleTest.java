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
package org.languagetool.rules.pl;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

public class WordCoherencyRuleTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pl-PL"));

  @Before
  public void before() {
    TestTools.disableAllRulesExcept(lt, "PL_WORD_COHERENCY");
  }
  
  @Test
  public void testRule() throws IOException {
    // correct sentences:
    assertGood("To jest grejpfrut. Dobry grejpfrut.");
    assertGood("Lubię Twoje blefy. Blef to jest coś.");
    // errors:
    assertError("To jest grapefruit. Dobry grejpfrut.", "grapefruit");

    List<RuleMatch> matches1 = lt.check("To jest jego bluff. A może blef?");
    assertThat(matches1.size(), is(1));
    assertThat(matches1.get(0).getFromPos(), is(27));
    assertThat(matches1.get(0).getToPos(), is(31));
    assertThat(matches1.get(0).getSuggestedReplacements().toString(), is("[bluff]"));

    List<RuleMatch> matches2 = lt.check("To jest jego bluff. Nie chwalił się tym blefem.");
    assertThat(matches2.size(), is(1));
    assertThat(matches2.get(0).getFromPos(), is(40));
    assertThat(matches2.get(0).getToPos(), is(46));
    assertThat(matches2.get(0).getSuggestedReplacements().toString(), is("[bluffem]"));
  }

  @Test
  public void testCallIndependence() throws IOException {
    assertGood("To jest blef.");
    assertGood("A to nie bluff.");  // this won't be noticed, the calls are independent of each other
  }

  @Test
  public void testMatchPosition() throws IOException {
    List<RuleMatch> ruleMatches = lt.check("To jest blef. Ale nie bluff.");
    assertThat(ruleMatches.size(), is(1));
    assertThat(ruleMatches.get(0).getFromPos(), is(22));
    assertThat(ruleMatches.get(0).getToPos(), is(27));
  }

  private void assertError(String s, String expectedSuggestion) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(s);
    RuleMatch[] matches = rule.match(analyzedSentences);
    assertEquals(1, matches.length);
    assertEquals("[" + expectedSuggestion + "]", matches[0].getSuggestedReplacements().toString());
  }

  private void assertError(String s) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(s);
    assertEquals(1, rule.match(analyzedSentences).length);
  }

  private void assertGood(String s) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(s);
    assertEquals(0, rule.match(analyzedSentences).length);
  }

  @Test
  public void testRuleCompleteTexts() throws IOException {
    assertEquals(0, lt.check("To jest blef. Nie wierzysz? To naprawdę blef!").size());
    assertEquals(1, lt.check("To jest blef. Nie wierzysz? To naprawdę bluff!").size());
    assertEquals(1, lt.check("To jest bluff. Nie wierzysz? To naprawdę blef!").size());
    
    // also find full forms:
    assertEquals(0, lt.check("To jest blef. Nie wierzysz? Nie widzisz blefu!").size());
    assertEquals(1, lt.check("To jest blef. Nie wierzysz? Nie widzisz bluffu!").size());

    // cross-paragraph checks
    assertEquals(1, lt.check("Chwalił się blefem.\n\nTak było nie zmyślam. Ale bluff mu nie wyszedł.").size());
  }

}
