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
import org.languagetool.Languages;

public class EnglishWrongWordInContextRuleTest {

  private JLanguageTool lt;
  private EnglishWrongWordInContextRule rule;
  
  @Before
  public void setUp() throws IOException {
    lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));
    rule = new EnglishWrongWordInContextRule(null);
  }

  @Test
  public void testRule() throws IOException {
    // prescribe/proscribe
    assertBad("I have proscribed you a course of antibiotics.");
    assertGood("I have prescribed you a course of antibiotics.");
    assertGood("Name one country that does not proscribe theft.");
    assertBad("Name one country that does not prescribe theft.");
    assertEquals("prescribed", rule.match(lt.getAnalyzedSentence("I have proscribed you a course of antibiotics."))[0].getSuggestedReplacements().get(0));
    // herion/heroine
    assertBad("We know that heroine is highly addictive.");
    assertGood("He wrote about his addiction to heroin.");
    assertGood("A heroine is the principal female character in a novel.");
    assertBad("A heroin is the principal female character in a novel.");
    // bizarre/bazaar
    assertBad("What a bazaar behavior!");
    assertGood("I bought these books at the church bazaar.");
    assertGood("She has a bizarre haircut.");
    assertBad("The Saturday morning bizarre is worth seeing even if you buy nothing.");
    // bridal/bridle
    assertBad("The bridle party waited on the lawn.");
    assertGood("Forgo the champagne treatment a bridal boutique often provides.");
    assertGood("He sat there holding his horse by the bridle.");
    assertBad("Each rider used his own bridal.");
    // desert/dessert
    assertBad("They have some great deserts on this menu.");
    assertGood("They have some great desserts on this menu.");
    // statute/statue
    assertBad("They have some great marble statutes.");
    assertGood("They have a great marble statue.");
    // neutron/neuron
    assertGood("Protons and neutrons");
    assertBad("Protons and neurons");
    // hangar / hanger
    // neutron/neuron
    assertBad("The plane taxied to the hanger.");
    assertGood("The plane taxied to the hangar.");
  }

  private void assertGood(String sentence) throws IOException {
    assertEquals(0, rule.match(lt.getAnalyzedSentence(sentence)).length);
  }

  private void assertBad(String sentence) throws IOException {
    assertEquals(1, rule.match(lt.getAnalyzedSentence(sentence)).length);
  }
}
