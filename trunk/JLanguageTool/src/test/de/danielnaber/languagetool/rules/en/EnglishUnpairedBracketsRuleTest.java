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

package de.danielnaber.languagetool.rules.en;

import java.io.IOException;
import java.util.List;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.RuleMatch;
import junit.framework.TestCase;

public class EnglishUnpairedBracketsRuleTest extends TestCase {

  public void testRule() throws IOException {
    EnglishUnpairedBracketsRule rule = new EnglishUnpairedBracketsRule(TestTools
        .getEnglishMessages(), Language.ENGLISH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    // correct sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("(This is a test sentence)."));
    assertEquals(0, matches.length);
    matches = rule
        .match(langTool.getAnalyzedSentence("This is a word 'test'."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("This is the joint presidents' declaration."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("The screen is 20\" wide."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("This is a [test] sentence..."));
    assertEquals(0, matches.length);
    matches = rule
        .match(langTool
            .getAnalyzedSentence("The plight of Tamil refugees caused a surge of support from most of the Tamil political parties.[90]"));
    assertEquals(0, matches.length);
    matches = rule
        .match(langTool
            .getAnalyzedSentence("This is what he said: \"We believe in freedom. This is what we do.\""));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("(([20] [20] [20]))"));
    assertEquals(0, matches.length);
    // test for a case that created a false alarm after disambiguation
    matches = rule.match(langTool
        .getAnalyzedSentence("This is a \"special test\", right?"));
    assertEquals(0, matches.length);
    // numerical bullets
    matches = rule.match(langTool
        .getAnalyzedSentence("We discussed this in Chapter 1)."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("The jury recommended that: (1) Four additional deputies be employed."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("We discussed this in section 1a)."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("We discussed this in section iv)."));
    assertEquals(0, matches.length);

    //inches exception shouldn't match " here:
    matches = rule.match(langTool
        .getAnalyzedSentence("In addition, the government would pay a $1,000 \"cost of education\" grant to the schools."));
    assertEquals(0, matches.length);

    matches = rule.match(langTool
        .getAnalyzedSentence("Paradise lost to the alleged water needs of Texas' big cities Thursday."));
    assertEquals(0, matches.length);

    matches = rule.match(langTool
        .getAnalyzedSentence("Kill 'em all!"));
    assertEquals(0, matches.length);

    matches = rule.match(langTool
        .getAnalyzedSentence("Puttin' on the Ritz"));
    assertEquals(0, matches.length);    
    
    // incorrect sentences:
    matches = rule.match(langTool
        .getAnalyzedSentence("(This is a test sentence."));
    assertEquals(1, matches.length);
    
    //tests for Edward's bug
    matches = rule.match(langTool
        .getAnalyzedSentence("This is a test with an apostrophe &'."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("&'"));
    assertEquals(1, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("!'"));
    assertEquals(1, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("What?'"));
    assertEquals(1, matches.length);
    //
    matches = rule.match(langTool
        .getAnalyzedSentence("(This is a test” sentence."));
    assertEquals(2, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("This is a {test sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool
        .getAnalyzedSentence("This [is (a test} sentence."));
    assertEquals(3, matches.length);
  }

  public void testMultipleSentences() throws IOException {
    final JLanguageTool tool = new JLanguageTool(Language.ENGLISH);
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
