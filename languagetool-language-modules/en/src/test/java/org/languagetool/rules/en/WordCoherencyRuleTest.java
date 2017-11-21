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
import org.languagetool.TestTools;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

public class WordCoherencyRuleTest {

  private final JLanguageTool lt = new JLanguageTool(new AmericanEnglish());

  @Before
  public void before() throws IOException {
    TestTools.disableAllRulesExcept(lt, "EN_WORD_COHERENCY");
  }
  
  @Test
  public void testRule() throws IOException {
    // correct sentences:
    assertGood("He owns a disk. She owns a disk, too.");
    assertGood("He owns a disc. She owns a disc, too.");
    // errors:
    assertError("He owns a disc. She owns a disk, too.");
  }

  @Test
  public void testCallIndependence() throws IOException {
    assertGood("He owns a disc");
    assertGood("She owns a disk, too.");  // this won't be noticed, the calls are independent of each other
  }

  @Test
  public void testMatchPosition() throws IOException {
    List<RuleMatch> ruleMatches = lt.check("He owns a disk. She owns a disc, too.");
    assertThat(ruleMatches.size(), is(1));
    assertThat(ruleMatches.get(0).getFromPos(), is(27));
    assertThat(ruleMatches.get(0).getToPos(), is(31));
  }

  private void assertGood(String s) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(s);
    assertEquals(0, rule.match(analyzedSentences).length);
  }

  @Test
  public void testRuleCompleteTexts() throws IOException {
    assertEquals(0, lt.check("He owns a disk. Really? Yes, he owns a disk.").size());
    assertEquals(1, lt.check("He owns a disk. Really? Yes, he owns a disc.").size());
    assertEquals(1, lt.check("He owns a disc. Really? Yes, he owns a disk.").size());
  }

  private void assertError(String s) throws IOException {
    WordCoherencyRule rule = new WordCoherencyRule(TestTools.getEnglishMessages());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(s);
    assertEquals(1, rule.match(analyzedSentences).length);
  }

}
