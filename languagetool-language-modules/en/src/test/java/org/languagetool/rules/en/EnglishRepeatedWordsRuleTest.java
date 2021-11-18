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
    
    assertCorrectText("This is a new experience. Happy New Year!");
    assertCorrectText("This was needed. There is a need to do it.");
    assertCorrectText("It needs to be done. That also needed to be done.");
    assertCorrectText("I still need to sign-in somewhere. You need to sign-in too.");
    
    assertCorrectText("Asia Global Crossing Ltd. Global Crossing and Asia Global Crossing.");
    assertCorrectText("I suggested that, but he also suggests that.");
    assertCorrectText("Matthew S. Anderson, Peter the Great. The Tomahawks were shipped from Great Britain.");
    assertCorrectText("It was great. The Tomahawks were shipped from Great Britain.");
    assertCorrectText("It was a global effort. Announcing the participation of Enron Global Markets.");
    
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
    
    matches=getRuleMatches("It needs to be done. That needs to be done.");
    assertEquals(1, matches.length);
    assertEquals("requires", matches[0].getSuggestedReplacements().get(0));
    
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
