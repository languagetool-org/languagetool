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
    
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hagámosle, deme, démelo, europeízate, homogenéizalo.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Veíanse")).length); //This is archaic
    
    // ignore tagged words not in the speller dictionary ("anillos")
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Del libro de los cinco anillos")).length);

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
    
    matches = rule.match(langTool.getAnalyzedSentence("Juan -el menor- jugó a la pelota."));
    assertEquals(0, matches.length);
    
    matches = rule.match(langTool.getAnalyzedSentence("vilbaino."));
    assertEquals("bilbaíno", matches[0].getSuggestedReplacements().get(0));
    
    //This needs to be handled with rules for different variants.
    //In Spain is a spelling error, but not in other countries. 
    //matches = rule.match(langTool.getAnalyzedSentence("confirmame."));
    //assertEquals("confírmame", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("confirmame."));
    assertEquals(0, matches.length);
    
    matches = rule.match(langTool.getAnalyzedSentence("confírmame."));
    assertEquals(0, matches.length);
    
    matches = rule.match(langTool.getAnalyzedSentence("DECANTACION."));
    assertEquals("Decantación", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("distopia"));
    assertEquals("distopía", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("Aministraciones"));
    assertEquals("Administraciones", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("respostas"));
    assertEquals("respuestas", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("mui"));
    assertEquals("muy", matches[0].getSuggestedReplacements().get(0)); 
    
    matches = rule.match(langTool.getAnalyzedSentence("finga"));
    assertEquals("finja", matches[0].getSuggestedReplacements().get(0));
    
    //currencies
    matches = rule.match(langTool.getAnalyzedSentence("$100"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("$10,000"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("10,000 USD"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("10,000 EUR"));
    assertEquals(0, matches.length);
    
  }

}
