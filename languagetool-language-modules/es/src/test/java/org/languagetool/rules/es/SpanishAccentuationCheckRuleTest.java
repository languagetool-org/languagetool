/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà
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
public class SpanishAccentuationCheckRuleTest {

  private SpanishAccentuationCheckRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws IOException {
    rule = new SpanishAccentuationCheckRule(TestTools.getEnglishMessages());
    langTool = new JLanguageTool(new Spanish());
  }

  @Test
  public void testRule() throws IOException {

    
    
    // correct sentences:
    assertCorrect("el Parlamento solicita a la Comisión");
    assertCorrect("Juan Pablo II beatifica Paula Montal.");
    assertCorrect("La magnífica conservación del palacio.");
    assertCorrect("Ella maquina alguna idea.");

    // incorrect sentences:
    assertIncorrect("La maquina del tiempo.");
    assertIncorrect("Una maquina del tiempo.");
    assertIncorrect("El arbitro se equivocó pitando el penalti.");
    assertIncorrect("La ultima consideración.");
    assertIncorrect("Fue un filosofo romántico.");
    assertIncorrect("Hace tareas especificas.");
    assertIncorrect("Un hombre adultero.");
    assertIncorrect("Hizo una magnifica interpretación.");
    assertIncorrect("La magnifica conservación del palacio.");
    assertIncorrect("Hace falta una nueva formula que la sustituya.");

    final RuleMatch[] matches = rule
        .match(langTool
            .getAnalyzedSentence("Las cascaras que nos rodean son cascaras vacías."));
    assertEquals(2, matches.length);
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

  @Test
  public void testPositions() throws IOException {
    final SpanishAccentuationCheckRule rule = new SpanishAccentuationCheckRule(TestTools.getEnglishMessages());
    final RuleMatch[] matches;
    final JLanguageTool langTool = new JLanguageTool(new Spanish());

    matches = rule.match(langTool
        .getAnalyzedSentence("Son cascaras vacías."));
    assertEquals(4, matches[0].getFromPos());
    assertEquals(12, matches[0].getToPos());
  }

}
