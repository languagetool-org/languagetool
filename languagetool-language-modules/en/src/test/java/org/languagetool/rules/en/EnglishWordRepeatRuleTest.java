/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.*;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnglishWordRepeatRuleTest {

  private final Language english = Languages.getLanguageForShortCode("en");
  private final EnglishWordRepeatRule rule = new EnglishWordRepeatRule(TestTools.getEnglishMessages(), english);
  
  private JLanguageTool lt;

  @Test
  public void testRepeatRule() throws IOException {
    lt = new JLanguageTool(english);
    assertGood("This is a test.");
    assertGood("If I had had time, I would have gone to see him.");
    assertGood("I don't think that that is a problem.");
    assertGood("He also said that Azerbaijan had fulfilled a task he set, which was that that their defense budget should exceed the entire state budget of Armenia.");
    assertGood("Just as if that was proof that that English was correct.");
    assertGood("It was noticed after more than a month that that promise had not been carried out.");
    assertGood("It was said that that lady was an actress.");
    assertGood("Kurosawa's three consecutive movies after Seven Samurai had not managed to capture Japanese audiences in the way that that film had.");
    assertGood("The can can hold the water.");
    assertGood("May May awake up?");
    assertGood("May may awake up.");
    assertGood("The cat does meow meow");
    assertGood("Hah Hah");
    assertGood("Hip Hip Hooray");
    assertBad("Hip Hip");
    assertGood("It's S.T.E.A.M.");
    assertGood("Ok ok ok!");
    assertGood("O O O");
    assertGood("Alice and Bob had had a long-standing relationship.");
    assertBad("I may may awake up.");
    assertBad("That is May May.");
    assertGood("Will Will awake up?");
    assertGood("Will will awake up.");
    assertBad("I will will awake up.");
    assertBad("Please wait wait for me.");
    assertGood("Wait wait!");
    assertBad("That is Will Will.");
    assertBad("I will will hold the ladder.");
    assertBad("You can feel confident that that this administration will continue to support a free and open Internet.");
    assertBad("This is is a test.");
    assertGood("b a s i c a l l y");
    assertGood("You can contact E.ON on Instagram.");
    assertBad("But I i was not sure.");
    assertBad("I I am the best.");
    assertGood("In a land far far away.");
    assertGood("I love you so so much.");
    assertGood("What Tom did didn't seem to bother Mary at all.");
    assertGood("Whatever you do don't leave the lid up on the toilet!");
    assertGood("Keep your chin up and whatever you do don't doubt yourself or your actions.");
    assertGood("I know that that can't really happen.");
  }

  private void assertGood(String sentence) throws IOException {
    assertMatches(sentence, 0);
  }

  private void assertBad(String sentence) throws IOException {
    assertMatches(sentence, 1);
  }

  private void assertMatches(String sentence, int expectedMatches) throws IOException {
    AnalyzedSentence aSentence = lt.getAnalyzedSentence(sentence);
    assertThat(rule.match(aSentence).length, is(expectedMatches));
  }

}
