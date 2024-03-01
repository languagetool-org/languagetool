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

public class EnglishUnpairedBracketsRuleTest {

  private TextLevelRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new EnglishUnpairedBracketsRule(TestTools.getEnglishMessages(), Languages.getLanguageForShortCode("en"));
    lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertCorrect("(This is a test sentence).");
    assertCorrect("This is a word 'test'.");
    assertCorrect("This is no smiley: (some more text)");
    assertCorrect("This is a sentence with a smiley :)");
    assertCorrect("This is a sentence with a smiley :(");
    assertCorrect("This is a sentence with a smiley :-)");
    assertCorrect("This is a sentence with a smiley ;-) and so on...");
    assertCorrect("This is a [test] sentence...");
    assertCorrect("The plight of Tamil refugees caused a surge of support from most of the Tamil political parties.[90]");
    assertCorrect("(([20] [20] [20]))");
    
    // test for a case that created a false alarm after disambiguation
    assertCorrect("This is a \"special test\", right?");
    // numerical bullets
    assertCorrect("We discussed this in Chapter 1).");
    assertCorrect("The jury recommended that: (1) Four additional deputies be employed.");
    assertCorrect("We discussed this in section 1a).");
    assertCorrect("We discussed this in section iv).");
    //some more cases
    assertCorrect("(Ketab fi Isti'mal al-'Adad al-Hindi)");
    assertCorrect("(al-'Adad al-Hindi)");
    //TODO: ambiguous
    assertCorrect("a) item one\nb) item two\nc) item three");
    assertCorrectText("\n\n" +
                      "a) New York\n" +
                      "b) Boston\n");
    assertCorrectText("\n\n" +
        "1.) New York\n" +
        "2.) Boston\n");
    assertCorrectText("\n\n" +
        "XII.) New York\n" +
        "XIII.) Boston\n");
    assertCorrectText("\n\n" +
                      "A) New York\n" +
                      "B) Boston\n" +
                      "C) Foo\n");

    // incorrect sentences:
    assertIncorrect("(This is a test sentence.");
    assertCorrect("This is not so (neither a nor b");
    assertIncorrect("This is not so (neither a nor b.");
    assertIncorrect("This is not so neither a nor b)");
    assertIncorrect("This is not so neither foo nor bar)");

    // this is currently considered incorrect... although people often use smileys this way:
    assertCorrect("Some text (and some funny remark :-) with more text to follow");  //TODO: Why is this correct and the next one incorrect?
    assertIncorrect("Some text (and some funny remark :-) with more text to follow!");

    RuleMatch[] matches;
    matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence("(This is a test] sentence.")));
    assertEquals(2, matches.length);
    matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence("This [is (a test} sentence.")));
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

    assertEquals(0, getMatches("This is multiple sentence text that contains a bracket: "
                             + "[This is a bracket. With some text.] and this continues.\n", lt));

    assertEquals(0, getMatches("This is multiple sentence text that contains a bracket. "
                             + "(This is a bracket. \n\n With some text.) and this continues.", lt));

    assertEquals(1, getMatches("This is multiple sentence text that contains a bracket: "
                             + "[This is a bracket. With some text. And this continues.\n\n", lt));
  }

  private int getMatches(String input, JLanguageTool lt) throws IOException {
    return lt.check(input).size();
  }

}
