/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Markus Brenneis
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.en.EnglishWrongWordInContextRule;

public class EnglishWrongWordInContextRuleTest {

  private JLanguageTool langTool;
  private EnglishWrongWordInContextRule rule;
  
  @Before
  public void setUp() throws IOException {
    langTool = new JLanguageTool(new AmericanEnglish());
    rule = new EnglishWrongWordInContextRule(null);
  }

  @Test
  public void testRule() throws IOException {
    // prescribe/proscribe
    assertBad("I have proscribed you a course of antibiotics.");
    assertGood("I have prescribed you a course of antibiotics.");
    assertGood("Name one country that does not proscribe theft.");
    assertBad("Name one country that does not prescribe theft.");
    assertEquals("prescribed", rule.match(langTool.getAnalyzedSentence("I have proscribed you a course antibiotics."))[0].getSuggestedReplacements().get(0));
    // herion/heroine
    assertBad("We know that heroine is highly addictive.");
    assertGood("He wrote about his addiction to heroin.");
    assertGood("A heroine is the principal female character in a novel.");
    assertBad("A heroin is the principal female character in a novel.");
  }

  private void assertGood(String sentence) throws IOException {
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(sentence)).length);
  }

  private void assertBad(String sentence) throws IOException {
    assertEquals(1, rule.match(langTool.getAnalyzedSentence(sentence)).length);
  }
}
