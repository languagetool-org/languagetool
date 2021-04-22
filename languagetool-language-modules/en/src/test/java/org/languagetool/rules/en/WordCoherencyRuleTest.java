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
package org.languagetool.rules.en;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

public class WordCoherencyRuleTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));

  @Before
  public void before() {
    TestTools.disableAllRulesExcept(lt, "EN_WORD_COHERENCY");
  }
  
  @Test
  public void testRule() throws IOException {
    // correct sentences:
    assertGood("He likes archeology. She likes archeology, too.");
    assertGood("He likes archaeology. She likes archaeology, too.");
    // errors:
    assertError("He likes archaeology. She likes archeology, too.");
    
    List<RuleMatch> matches1 = lt.check("He is reelected, or he will be re-elected.");
    assertThat(matches1.size(), is(1));
    assertThat(matches1.get(0).getFromPos(), is(31));
    assertThat(matches1.get(0).getToPos(), is(41));
    assertThat(matches1.get(0).getSuggestedReplacements().toString(), is("[reelected]"));
    
    List<RuleMatch> matches2 = lt.check("He was reelected, and I will re-elect him again in 2002.");
    assertThat(matches2.size(), is(1));
    assertThat(matches2.get(0).getFromPos(), is(29));
    assertThat(matches2.get(0).getToPos(), is(37));
    assertThat(matches2.get(0).getSuggestedReplacements().toString(), is("[reelect]"));

    List<RuleMatch> matches3 = lt.check("He oxidises o, or he oxidizes");
    assertThat(matches3.size(), is(1));
    assertThat(matches3.get(0).getFromPos(), is(21));
    assertThat(matches3.get(0).getToPos(), is(29));
    assertThat(matches3.get(0).getSuggestedReplacements().toString(), is("[oxidises]"));
  }

  @Test
  public void testCallIndependence() throws IOException {
    assertGood("He likes archaeology.");
    assertGood("She likes archeology, too.");  // this won't be noticed, the calls are independent of each other
  }

  @Test
  public void testMatchPosition() throws IOException {
    List<RuleMatch> ruleMatches = lt.check("He likes archaeology. She likes archeology, too.");
    assertThat(ruleMatches.size(), is(1));
    assertThat(ruleMatches.get(0).getFromPos(), is(32));
    assertThat(ruleMatches.get(0).getToPos(), is(42));
  }

  private void assertGood(String s) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(s);
    assertEquals(0, rule.match(analyzedSentences).length);
  }

  @Test
  public void testRuleCompleteTexts() throws IOException {
    assertEquals(0, lt.check("He likes archaeology. Really? She likes archaeology, too.").size());
    assertEquals(1, lt.check("He likes archaeology. Really? She likes archeology, too.").size());
    assertEquals(1, lt.check("He likes archeology. Really? She likes archaeology, too.").size());
    assertEquals(1, lt.check("Mix of upper case and lower case: Westernize and westernise.").size());
  }

  private void assertError(String s) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(s);
    assertEquals(1, rule.match(analyzedSentences).length);
  }

}
