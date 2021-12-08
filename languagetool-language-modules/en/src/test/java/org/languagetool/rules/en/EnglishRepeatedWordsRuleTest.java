/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.TestTools;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TextLevelRule;

public class EnglishRepeatedWordsRuleTest {

  private TextLevelRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new EnglishRepeatedWordsRule(TestTools.getEnglishMessages());
    lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
  }

  @Test
  public void testRule() throws IOException {

    // form
    assertCorrectText("They went on to form a new group. The bacteria causes a blood clot to form in the jugular vein.");

    // global
    assertCorrectText("Asia Global Crossing Ltd. Global Crossing and Asia Global Crossing.");
    assertCorrectText("It was a global effort. Announcing the participation of Enron Global Markets.");

    // great
    assertCorrectText("Matthew S. Anderson, Peter the Great. The Tomahawks were shipped from Great Britain.");
    assertCorrectText("It was great. The Tomahawks were shipped from Great Britain.");

    // interesting
    assertCorrectText("I found it very interesting. An interesting fact about me is that I have a twin.");

    // need
    assertCorrectText("It needs to be done. That needs to be done.");
    assertCorrectText("This was needed. There is a need to do it.");
    assertCorrectText("It needs to be done. That also needed to be done.");
    assertCorrectText("I still need to sign in somewhere. You need to sign in too.");

    // new
    assertCorrectText("This is a new experience. Happy New Year!");

    // often
    assertCorrectText("It's often gloomy outside. More often than not, it's raining.");
    assertCorrectText("We have bad weather here often. Often times, it's raining.");

    // problem
    assertCorrectText("The students were given some problems. They needed help to solve the problems.");
    assertCorrectText("Then, there were numerous problems after that. His initial interest lay in an attempt to solve Hilbert's fifth problem.");
    assertCorrectText("There were some problems with the tests. No problem, I'm not in a rush.");
    assertCorrectText("The students were given some problems. They were math problems.");

    // several
    assertCorrectText("We noticed them several times. Several thousand people stormed the gate.");

    // suggest
    assertCorrectText("I suggested that, but he also suggests that.");
    assertCorrectText("He suggested that we review them again. What do these suggest about the transaction history?");
    assertCorrectText("I suggested he look it over again. This strongly suggests that Mr. Batt is guilty.");

    // ignore sentences without period at the end
    assertCorrectText("I suggested this. She suggests that");

    RuleMatch[] matches=getRuleMatches("I suggested this. She suggests that.");
    assertEquals(1, matches.length);
    assertEquals(22, matches[0].getFromPos());
    assertEquals(30, matches[0].getToPos());
    assertEquals("proposes", matches[0].getSuggestedReplacements().get(0));
    assertEquals("recommends", matches[0].getSuggestedReplacements().get(1));
    assertEquals("submits", matches[0].getSuggestedReplacements().get(2));
    
    matches=getRuleMatches("I suggested this. She suggests that. And they suggested that.");
    assertEquals(2, matches.length);
    assertEquals(22, matches[0].getFromPos());
    assertEquals(46, matches[1].getFromPos());
    assertEquals("proposes", matches[0].getSuggestedReplacements().get(0));
    assertEquals("proposed", matches[1].getSuggestedReplacements().get(0));
    
    matches=getRuleMatches ("The problem was weird. And the solutions needed to be weird.");
    assertEquals(1, matches.length);
    assertEquals("odd", matches[0].getSuggestedReplacements().get(0));
    
    //matches=getRuleMatches("It needs to be done. That needs to be done.");
    //assertEquals(1, matches.length);
    //assertEquals("requires", matches[0].getSuggestedReplacements().get(0));
    
  }
  
  private RuleMatch[] getRuleMatches(String sentences) throws IOException {
    AnnotatedText aText = new AnnotatedTextBuilder().addText(sentences).build();
    return rule.match(lt.analyzeText(sentences), aText);
  }

  private void assertCorrectText(String sentences) throws IOException {
    AnnotatedText aText = new AnnotatedTextBuilder().addText(sentences).build();
    RuleMatch[] matches = rule.match(lt.analyzeText(sentences), aText);
    assertEquals(0, matches.length);
  }


}
