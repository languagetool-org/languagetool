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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.*;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.languagetool.rules.en.AvsAnRule.Determiner;

public class AvsAnRuleTest {

  private AvsAnRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws IOException {
    rule = new AvsAnRule(TestTools.getEnglishMessages());
    lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertCorrect("This is a test sentence.");
    assertCorrect("It was an hour ago.");
    assertCorrect("A university is ...");
    assertCorrect("A one-way street ...");
    assertCorrect("An hour's work ...");
    assertCorrect("Going to an \"industry party\".");
    assertCorrect("An 8-year old boy ...");
    assertCorrect("An 18-year old boy ...");
    assertCorrect("The A-levels are ...");
    assertCorrect("An NOP check ...");
    assertCorrect("A USA-wide license ...");
    assertCorrect("...asked a UN member.");
    assertCorrect("In an un-united Germany...");
    //fixed false alarms:
    assertCorrect("Here, a and b are supplementary angles.");
    assertCorrect("The Qur'an was translated into Polish.");
    assertCorrect("See an:Grammatica");
    assertCorrect("See http://www.an.com");
    assertCorrect("Station A equals station B.");
    assertCorrect("e.g., the case endings -a -i -u and mood endings -u -a");

    // errors:
    assertIncorrect("It was a hour ago.");
    assertIncorrect("It was an sentence that's long.");
    assertIncorrect("It was a uninteresting talk.");
    assertIncorrect("An university");
    assertIncorrect("A unintersting ...");
    assertIncorrect("A hour's work ...");
    assertIncorrect("Going to a \"industry party\".");
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("It was a uninteresting talk with an long sentence."));
    assertEquals(2, matches.length);

    // With uppercase letters:
    assertCorrect("A University");
    assertCorrect("A Europe wide something");

    assertIncorrect("then an University sdoj fixme sdoopsd");
    assertIncorrect("A 8-year old boy ...");
    assertIncorrect("A 18-year old boy ...");
    assertIncorrect("...asked an UN member.");
    assertIncorrect("In a un-united Germany...");

    //Test on acronyms/initials:
    assertCorrect("A. R.J. Turgot");

    // list items
    assertCorrect("Make sure that 3.a as well as 3.b are correct.");

    //mixed case as dictionary-based exception
    assertCorrect("Anyone for an MSc?");
    assertIncorrect("Anyone for a MSc?");
    //mixed case from general case
    assertCorrect("Anyone for an XMR-based writer?");

    //Test on apostrophes
    assertCorrect("Its name in English is a[1] (), plural A's, As, as, or a's.");

    // Both are correct according to Merriam Webster (http://www.merriam-webster.com/dictionary/a%5B2%5D),
    // although some people disagree (http://www.theslot.com/a-an.html):
    assertCorrect("An historic event");
    assertCorrect("A historic event");
  }

  private void assertCorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
  }

  @Test
  public void testSuggestions() throws IOException {
    assertEquals("a string", rule.suggestAorAn("string"));
    assertEquals("a university", rule.suggestAorAn("university"));
    assertEquals("an hour", rule.suggestAorAn("hour"));
    assertEquals("an all-terrain", rule.suggestAorAn("all-terrain"));
    assertEquals("a UNESCO", rule.suggestAorAn("UNESCO"));
    assertEquals("a historical", rule.suggestAorAn("historical"));
  }

  @Test
  public void testGetCorrectDeterminerFor() throws IOException {
    assertEquals(Determiner.A, getDeterminerFor("string"));
    assertEquals(Determiner.A, getDeterminerFor("university"));
    assertEquals(Determiner.A, getDeterminerFor("UNESCO"));
    assertEquals(Determiner.A, getDeterminerFor("one-way"));
    assertEquals(Determiner.AN, getDeterminerFor("interesting"));
    assertEquals(Determiner.AN, getDeterminerFor("hour"));
    assertEquals(Determiner.AN, getDeterminerFor("all-terrain"));
    assertEquals(Determiner.A_OR_AN, getDeterminerFor("historical"));
    assertEquals(Determiner.UNKNOWN, getDeterminerFor(""));
    assertEquals(Determiner.UNKNOWN, getDeterminerFor("-way"));
    assertEquals(Determiner.UNKNOWN, getDeterminerFor("camelCase"));
  }

  private Determiner getDeterminerFor(String word) {
    AnalyzedTokenReadings token = new AnalyzedTokenReadings(new AnalyzedToken(word, "fake-postag", "fake-lemma"), 0);
    return rule.getCorrectDeterminerFor(token);
  }

  @Test
  public void testGetCorrectDeterminerForException() throws IOException {
    try {
      rule.getCorrectDeterminerFor(null);
      fail();
    } catch (NullPointerException ignored) {}
  }

  @Test
  public void testPositions() throws IOException {
    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
    // no quotes etc.:
    matches = rule.match(lt.getAnalyzedSentence("a industry standard."));
    assertEquals(0, matches[0].getFromPos());
    assertEquals(1, matches[0].getToPos());

    // quotes..
    matches = rule.match(lt.getAnalyzedSentence("a \"industry standard\"."));
    assertEquals(0, matches[0].getFromPos());
    assertEquals(1, matches[0].getToPos());

    matches = rule.match(lt.getAnalyzedSentence("a - industry standard\"."));
    assertEquals(0, matches[0].getFromPos());
    assertEquals(1, matches[0].getToPos());

    matches = rule.match(lt.getAnalyzedSentence("This is a \"industry standard\"."));
    assertEquals(8, matches[0].getFromPos());
    assertEquals(9, matches[0].getToPos());

    matches = rule.match(lt.getAnalyzedSentence("\"a industry standard\"."));
    assertEquals(1, matches[0].getFromPos());
    assertEquals(2, matches[0].getToPos());

    matches = rule.match(lt.getAnalyzedSentence("\"Many say this is a industry standard\"."));
    assertEquals(18, matches[0].getFromPos());
    assertEquals(19, matches[0].getToPos());

    matches = rule.match(lt.getAnalyzedSentence("Like many \"an desperado\" before him, Bart headed south into Mexico."));
    assertEquals(11, matches[0].getFromPos());
    assertEquals(13, matches[0].getToPos());
  }
}
