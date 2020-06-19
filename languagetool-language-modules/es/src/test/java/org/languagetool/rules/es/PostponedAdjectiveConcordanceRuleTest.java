/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà
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
package org.languagetool.rules.es;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class PostponedAdjectiveConcordanceRuleTest {

  private PostponedAdjectiveConcordanceRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws IOException {
    rule = new PostponedAdjectiveConcordanceRule(TestTools.getEnglishMessages());
    langTool = new JLanguageTool(new Spanish());
  }

  @Test
  public void testRule() throws IOException {

    assertCorrect("Estas son las sillas blancas de las que te hablé.");
    assertCorrect("La casa del pueblo blanca.");
    assertCorrect("La casa del pueblo blanco.");
    assertCorrect("La casa de pueblo blanca.");
    assertCorrect("La casa de pueblo blanco.");
    
    assertIncorrect("Análisis clínica.");
    assertIncorrect("Estas son las sillas blancos de las que te hablé.");
    assertIncorrect("Son casas rojos.");
    assertIncorrect("La casa del pueblo blancas");
    assertIncorrect("La casa del pueblo blancos");
    assertIncorrect("La casa de pueblo blancas");
    assertIncorrect("La casa de pueblo blancos");
    
  }

  private void assertCorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool
        .getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool
        .getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
  }

}
