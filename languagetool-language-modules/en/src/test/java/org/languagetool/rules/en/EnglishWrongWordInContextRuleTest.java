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
import org.languagetool.Language;
import org.languagetool.Languages;

public class EnglishWrongWordInContextRuleTest {

  private JLanguageTool lt;
  private EnglishWrongWordInContextRule rule;
  
  @Before
  public void setUp() throws IOException {
    Language english = Languages.getLanguageForShortCode("en-US");
    lt = new JLanguageTool(english);
    rule = new EnglishWrongWordInContextRule(null, english);
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
    assertGood("When sugar began to be manufactured in the Middle Ages more sweet desserts became available.");
    // statute/statue
    assertBad("They have some great marble statutes.");
    assertGood("They have a great marble statue.");
    assertGood("Then they gathered some of the flowers of the season, crowned the statues with garlands and hung up Dorcon's pipe as a votive offering to the Nymphs.");
    // neutron/neuron
    assertGood("Protons and neutrons");
    assertBad("Protons and neurons");
    assertGood("Unlike CBs, gems do not contain small nuclear ribonucleoproteins (snRNPs), but do contain a protein called survivor of motor neurons (SMN) whose function relates to snRNP biogenesis.");
    // hangar / hanger
    assertBad("The plane taxied to the hanger.");
    assertGood("The plane taxied to the hangar.");
    // massage / message
    assertGood("Finally, administrative professionals will receive a certificate for $5 off the cost of a massage at the Body Shop.");
    assertGood("You will receive lunch, manicure, pedicure, full body massage, and facial.");
    assertGood("Then, when they present their certificate at the time of their appointment, they will receive an additional $5 off the cost of a 30, 45, or 60 minute massage for a total discount of $10.");
    // sign / sing
    assertGood("The song \"Whiskey Bottle,\" by Uncle Tupelo, is rumored to be about the city of Columbia as it makes specific reference to a sign which used be displayed on a Columbia tackle shop sign which read, \"Liquor, Guns, and Ammo.\"");
    assertGood("You can sign up for hundreds of different movie and sports packages, digital music channels and receive all your local channels in this special offer.");
    assertGood("Begbick and her cohorts take it as a sign that Jimmy is right; they join him, Jenny, and his three friends in singing a new, defiant song: If someone walks on, then it's me, and if someone gets walked on, then it's you.");
  }

  private void assertGood(String sentence) throws IOException {
    assertEquals(0, rule.match(lt.getAnalyzedSentence(sentence)).length);
  }

  private void assertBad(String sentence) throws IOException {
    assertEquals(1, rule.match(lt.getAnalyzedSentence(sentence)).length);
  }
}
