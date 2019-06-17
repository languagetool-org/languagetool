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
package org.languagetool.rules.es;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MorfologikSpanishSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    Spanish language = new Spanish();
    MorfologikSpanishSpellerRule rule = new MorfologikSpanishSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());
    JLanguageTool langTool = new JLanguageTool(language);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Escriba un texto aquí. LanguageTool le ayudará a afrontar algunas dificultades propias de la escritura.")).length);
    
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Se a hecho un esfuerzo para detectar errores tipográficos, ortograficos y incluso gramaticales."));
    assertEquals(1, matches.length);
    assertEquals(59, matches[0].getFromPos());
    assertEquals(71, matches[0].getToPos());
    assertEquals("ortográficos", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(langTool.getAnalyzedSentence("Se a 😂 hecho un esfuerzo para detectar errores tipográficos, ortograficos y incluso gramaticales."));
    assertEquals(1, matches.length);
    assertEquals(62, matches[0].getFromPos());
    assertEquals(74, matches[0].getToPos());
    assertEquals("ortográficos", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(langTool.getAnalyzedSentence("Se a 😂😂 hecho un esfuerzo para detectar errores tipográficos, ortograficos y incluso gramaticales."));
    assertEquals(1, matches.length);
    assertEquals(64, matches[0].getFromPos());
    assertEquals(76, matches[0].getToPos());
    assertEquals("ortográficos", matches[0].getSuggestedReplacements().get(0));
  }

}
