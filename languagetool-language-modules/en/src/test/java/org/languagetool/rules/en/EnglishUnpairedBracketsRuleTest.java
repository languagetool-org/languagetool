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

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.English;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

public class EnglishUnpairedBracketsRuleTest extends TestCase {

  private Rule rule;
  private JLanguageTool langTool;

  @Override
  public void setUp() throws IOException {
    rule = new EnglishUnpairedBracketsRule(TestTools.getEnglishMessages(), new English());
    langTool = new JLanguageTool(new English());
  }

  public void testRule() throws IOException {

    // correct sentences:
    assertCorrect("(This is a test sentence).");
    assertCorrect("This is a word 'test'.");
    assertCorrect("This is a sentence with a smiley :-)");
    assertCorrect("This is a sentence with a smiley ;-) and so on...");
    assertCorrect("I don't know.");
    assertCorrect("This is the joint presidents' declaration.");
    assertCorrect("The screen is 20\" wide.");
    assertCorrect("This is a [test] sentence...");
    assertCorrect("The plight of Tamil refugees caused a surge of support from most of the Tamil political parties.[90]");
    assertCorrect("This is what he said: \"We believe in freedom. This is what we do.\"");
    assertCorrect("(([20] [20] [20]))");
    // test for a case that created a false alarm after disambiguation
    assertCorrect("This is a \"special test\", right?");
    // numerical bullets
    assertCorrect("We discussed this in Chapter 1).");
    assertCorrect("The jury recommended that: (1) Four additional deputies be employed.");
    assertCorrect("We discussed this in section 1a).");
    assertCorrect("We discussed this in section iv).");
    //inches exception shouldn't match " here:
    assertCorrect("In addition, the government would pay a $1,000 \"cost of education\" grant to the schools.");
    assertCorrect("Paradise lost to the alleged water needs of Texas' big cities Thursday.");
    assertCorrect("Kill 'em all!");
    assertCorrect("Puttin' on the Ritz");
    //some more cases
    assertCorrect("(Ketab fi Isti'mal al-'Adad al-Hindi)");
    assertCorrect("On their 'host' societies.");
    assertCorrect("On their 'host society'.");
    assertCorrect("Burke-rostagno the Richard S. Burkes' home in Wayne may be the setting for the wedding reception for their daughter.");
    assertCorrect("The '49 team was off to a so-so 5-5 beginning");
    assertCorrect("A Republican survey says Kennedy won the '60 election on the religious issue.");


    //Should be correct!
    //assertCorrect("On their 'host societies'.");

    // incorrect sentences:
    assertIncorrect("(This is a test sentence.");
    assertIncorrect("This is a test with an apostrophe &'.");
    assertIncorrect("&'");
    assertIncorrect("!'");
    assertIncorrect("What?'");

    // this is currently considered incorrect... although people often use smileys this way:
    assertIncorrect("Some text (and some funny remark :-) with more text to follow");

    RuleMatch[] matches;
    matches = rule.match(langTool.getAnalyzedSentence("(This is a test” sentence."));
    assertEquals(2, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This [is (a test} sentence."));
    assertEquals(3, matches.length);
  }

  private void assertCorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
  }

  public void testMultipleSentences() throws IOException {
    final JLanguageTool tool = new JLanguageTool(new English());
    tool.enableRule("EN_UNPAIRED_BRACKETS");

    List<RuleMatch> matches;
    matches = tool
        .check("This is multiple sentence text that contains a bracket: "
            + "[This is bracket. With some text.] and this continues.\n");
    assertEquals(0, matches.size());
    matches = tool
        .check("This is multiple sentence text that contains a bracket: "
            + "[This is bracket. With some text. And this continues.\n\n");
    assertEquals(1, matches.size());
    // now with a paragraph end inside - we get two alarms because of paragraph
    // resetting
    matches = tool
        .check("This is multiple sentence text that contains a bracket. "
            + "(This is bracket. \n\n With some text.) and this continues.");
    assertEquals(2, matches.size());
  }

}
