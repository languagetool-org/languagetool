/* LanguageTool, a natural language style checker
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
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
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class EnglishUnpairedQuotesRuleTest {

  private TextLevelRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new EnglishUnpairedQuotesRule(TestTools.getEnglishMessages(), Languages.getLanguageForShortCode("en"));
    lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertCorrect("This is a word 'test'.");
    assertCorrect("I don't know.");
    assertCorrect("This is the joint presidents' declaration.");
    assertCorrect("The screen is 20\"");
    assertCorrect("The screen is 20\" wide.");
    assertIncorrect("The screen is very\" wide.");
    assertCorrect("This is what he said: \"We believe in freedom. This is what we do.\"");
    
    assertCorrect("He was an ol' man.");
    assertCorrect("'till the end.");
    assertCorrect("jack-o'-lantern");
    assertCorrect("jack o'lantern");
    assertCorrect("sittin' there");
    assertCorrect("Nothin'");
    assertCorrect("ya'");
    assertCorrect("I'm not goin'");
    assertCorrect("y'know");
    assertCorrect("Please find attached Fritz' revisions");
    assertCorrect("You're only foolin' round.");
    assertCorrect("I stayed awake 'till the morning.");
    assertCorrect("under the 'Global Markets' heading");
    assertCorrect("He's an 'admin'.");
    assertCorrect("However, he's still expected to start in the 49ers' next game on Oct.");
    assertCorrect("all of his great-grandfathers' names");
    assertCorrect("Though EES' past profits now are in question");
    assertCorrect("Networks' Communicator and FocusFocus' Conference.");
    assertCorrect("Additional funding came from MegaMags' founders and existing individual investors.");
    assertCorrect("al-Jazā’er");
    assertCorrect("second Mu’taq and third");
    assertCorrect("second Mu'taq and third");
    assertCorrect("The phrase ‘\\1 \\2’ is British English.");
    assertCorrect("The phrase ‘1 2’ is British English.");
    
    assertCorrect("22' N., long. ");
    assertCorrect("11º 22'");
    assertCorrect("11° 22'");
    assertCorrect("11° 22.5'");
    assertCorrect("In case I garbled mine, here 'tis.");
    assertCorrect("It's about three o’clock.");
    assertCorrect("It's about three o'clock.");
    assertCorrect("Rory O’More");
    assertCorrect("Rory O'More");
    assertCorrect("Côte d’Ivoire");
    assertCorrect("Côte d'Ivoire");
    assertCorrect("Colonel d’Aubigni");
    
    
    // test for a case that created a false alarm after disambiguation
    assertCorrect("This is a \"special test\", right?");
    //inches exception shouldn't match " here:
    assertCorrect("In addition, the government would pay a $1,000 \"cost of education\" grant to the schools.");
    assertCorrect("Paradise lost to the alleged water needs of Texas' big cities Thursday.");
    assertCorrect("Kill 'em all!");
    assertCorrect("Puttin' on the Ritz");
    assertCorrect("Dunkin' Donuts");
    assertCorrect("Hold 'em!");
    //some more cases
    assertCorrect("(Ketab fi Isti'mal al-'Adad al-Hindi)");
    assertCorrect("(al-'Adad al-Hindi)");
    assertCorrect("On their 'host' societies.");
    assertCorrect("On their 'host society'.");
    assertCorrect("Burke-rostagno the Richard S. Burkes' home in Wayne may be the setting for the wedding reception for their daughter.");
    assertCorrect("The '49 team was off to a so-so 5-5 beginning");
    assertCorrect("The best reason that can be advanced for the state adopting the practice was the advent of expanded highway construction during the 1920s and '30s.");
    assertCorrect("A Republican survey says Kennedy won the '60 election on the religious issue.");
    assertCorrect("Economy class seats have a seat pitch of 31-33\", with newer aircraft having thinner seats that have a 31\" pitch.");
    assertCorrect("\"02\" will sort before \"10\" as expected so it will have size of 10\".");
    assertCorrect("\"02\" will sort before \"10\" as expected so it will have size of 10\""); // inch symbol is at the sentence end
    assertCorrect("\"02\" will sort before \"10\""); // quotation mark is at the sentence end
    assertCorrect("On their 'host societies'.");
    assertCorrect("On their host 'societies'.");
    assertIncorrect("On their 'host societies.");
    //TODO: ambiguous
    assertCorrect("On their host societies'.");
    assertCorrect("I think that Liszt's \"Forgotten Waltz No.3\" is a hidden masterpiece.");
    assertCorrect("I think that Liszt's \"Forgotten Waltz No. 3\" is a hidden masterpiece.");
    assertCorrect("Turkish distinguishes between dotted and dotless \"I\"s.");
    assertCorrect("It has recognized no \"bora\"-like pattern in his behaviour."); //It's fixed with the new tokenizer

    // incorrect sentences:
    assertIncorrect("This is a test with an apostrophe &'.");  
    //FIXME? assertIncorrect("&'");
    //FIXME? assertIncorrect("!'");
    //FIXME: assertIncorrect("What?'");
    assertIncorrect("He is making them feel comfortable all along.\"");
    assertIncorrect("\"He is making them feel comfortable all along.");

    // this is currently considered incorrect... although people often use smileys this way:
    assertCorrect("Some text. This is \"12345\", a number.");
    assertCorrect("Some text.\n\nThis is \"12345\", a number.");
    assertCorrect("Some text. This is 12345\", a number.");  // could be "inch", so no error
    assertCorrect("Some text. This is 12345\", a number.");  // could be "inch", so no error
    assertCorrect("\"When you bring someone,\" he said.\n" +
      "Gibson introduced the short-scale (30.5\") bass in 1961.");  // could be "inch", so no error

    RuleMatch[] matches;
    matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence("\"This is a test” sentence.")));
    assertEquals(2, matches.length);
    matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence("This \"is 'a test” sentence.")));
    assertEquals(3, matches.length);
  }

  private void assertCorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence(sentence)));
    assertEquals(0, matches.length);
  }

  private void assertCorrectText(String sentences) throws IOException {
    AnnotatedText aText = new AnnotatedTextBuilder().addText(sentences).build();
    RuleMatch[] matches = rule.match(lt.analyzeText(sentences), aText);
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence(sentence)));
    assertEquals(1, matches.length);
  }

  @Test
  public void testMultipleSentences() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));

    assertEquals(0, getMatches("This is multiple sentence text that contains Quotes: "
                             + "\"This is a bracket. With some text.\" and this continues.\n", lt));

    assertEquals(0, getMatches("This is multiple sentence text that contains Quotes. "
                             + "“This is a bracket. \n\n With some text.” and this continues.", lt));

    assertEquals(1, getMatches("This is multiple sentence text that contains a Quote: "
                             + "“This is a bracket. With some text. And this continues.\n\n", lt));
  }

  private int getMatches(String input, JLanguageTool lt) throws IOException {
    return lt.check(input).size();
  }

}
