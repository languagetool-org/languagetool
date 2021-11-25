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
package org.languagetool.rules.fr;

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

public class FrenchRepeatedWordsRuleTest {

  private TextLevelRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new FrenchRepeatedWordsRule(TestTools.getMessages("fr"));
    lt = new JLanguageTool(Languages.getLanguageForShortCode("fr"));
  }

  @Test
  public void testRule() throws IOException {

    assertCorrectText("Elle est notamment phénoménale. Les choses sont notamment compliquées");

    RuleMatch[] matches = getRuleMatches("Elle est notamment phénoménale. Les choses sont notamment compliquées.");
    assertEquals(1, matches.length);
    assertEquals("[particulièrement, spécialement, singulièrement, surtout, spécifiquement]", matches[0].getSuggestedReplacements().toString());
    
    matches = getRuleMatches("Elle est maintenant phénoménale. Les choses sont maintenant compliquées.");
    assertEquals(1, matches.length);
    assertEquals("[présentement, ce jour-ci, désormais, à présent]", matches[0].getSuggestedReplacements().toString());
    
    
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
