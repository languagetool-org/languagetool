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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

public class MorfologikSpanishSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    Spanish language = new Spanish();
    MorfologikSpanishSpellerRule rule = new MorfologikSpanishSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());
    JLanguageTool lt = new JLanguageTool(language);

    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Escriba un texto aquí. LanguageTool le ayudará a afrontar algunas dificultades propias de la escritura.")).length);
    
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Hagámosle, deme, démelo, europeízate, homogenéizalo. Anúnciate. Desáhucialos.")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Veíanse")).length); //This is archaic

    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("En la p. 25, pp. 33-45. Ctrl+A")).length);
    
    // ignore tagged words not in the speller dictionary ("anillos")
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Del libro de los cinco anillos")).length);

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Se a hecho un esfuerzo para detectar errores tipográficos, ortograficos y incluso gramaticales."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(59, matches[0].getFromPos());
    Assertions.assertEquals(71, matches[0].getToPos());
    Assertions.assertEquals("ortográficos", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("Se a 😂 hecho un esfuerzo para detectar errores tipográficos, ortograficos y incluso gramaticales."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(62, matches[0].getFromPos());
    Assertions.assertEquals(74, matches[0].getToPos());
    Assertions.assertEquals("ortográficos", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("Se a 😂😂 hecho un esfuerzo para detectar errores tipográficos, ortograficos y incluso gramaticales."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(64, matches[0].getFromPos());
    Assertions.assertEquals(76, matches[0].getToPos());
    Assertions.assertEquals("ortográficos", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("Juan -el menor- jugó a la pelota."));
    Assertions.assertEquals(0, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("vilbaino."));
    Assertions.assertEquals("bilbaíno", matches[0].getSuggestedReplacements().get(0));
    
    //This needs to be handled with rules for different variants.
    //In Spain is a spelling error, but not in other countries. 
    //matches = rule.match(langTool.getAnalyzedSentence("confirmame."));
    //assertEquals("confírmame", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("confirmame."));
    Assertions.assertEquals(0, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("confírmame."));
    Assertions.assertEquals(0, matches.length);
    
    matches = rule.match(lt.getAnalyzedSentence("DECANTACION."));
    Assertions.assertEquals("DECANTACIÓN", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("distopia"));
    Assertions.assertEquals("distopía", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("Aministraciones"));
    Assertions.assertEquals("Administraciones", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("respostas"));
    Assertions.assertEquals("respuestas", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("mui"));
    Assertions.assertEquals("muy", matches[0].getSuggestedReplacements().get(0)); 
    
    matches = rule.match(lt.getAnalyzedSentence("finga"));
    Assertions.assertEquals("finja", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("esque"));
    Assertions.assertEquals("es que", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("hicistes"));
    Assertions.assertEquals("[hiciste, hicisteis]", matches[0].getSuggestedReplacements().toString());
    
    matches = rule.match(lt.getAnalyzedSentence("Windows10"));
    Assertions.assertEquals("Windows 10", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("Windows", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("windows10"));
    Assertions.assertEquals("Windows 10", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("Windows", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("windows1995"));
    Assertions.assertEquals("Windows 1995", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("en1995"));
    Assertions.assertEquals("en 1995", matches[0].getSuggestedReplacements().get(0));
    
    //currencies
    matches = rule.match(lt.getAnalyzedSentence("$100"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("$10,000"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("10,000 USD"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("10,000 EUR"));
    Assertions.assertEquals(0, matches.length);
    
    // emojis
    matches = rule.match(lt.getAnalyzedSentence("🧡"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("🚴"));
    Assertions.assertEquals(0, matches.length);
    //matches = rule.match(langTool.getAnalyzedSentence("🏽"));
    //assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("♂️"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("🎉"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("💛"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("✈️"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("🧡🚴"));
    Assertions.assertEquals(0, matches.length);
    matches = rule.match(lt.getAnalyzedSentence("🧡🚴🏽♂️ , 🎉💛✈️"));
    Assertions.assertEquals(0, matches.length);
    
    // Combining diacritics
    matches = rule.match(lt.getAnalyzedSentence("publicacio\u0301n"));
    Assertions.assertEquals("publicación", matches[0].getSuggestedReplacements().get(0));
    // Other rare characters
    /*matches = rule.match(lt.getAnalyzedSentence("𝐩𝐮𝐛𝐥𝐢𝐜𝐚𝐜𝐢𝐨́𝐧"));
    assertEquals("publicación", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("se daba cuenta c´"));
    assertEquals("cm", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("𝐩𝐮𝐛𝐥𝐢𝐜𝐚𝐜𝐢𝐨𝐧"));
    assertEquals("publicación", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("𝐩𝐮𝐛𝐥𝐢𝐜𝐛𝐥𝐢𝐜𝐚𝐛𝐥𝐢𝐜𝐜𝐢𝐨𝐧𝐛𝐥𝐢𝐜"));
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getSuggestedReplacements().size());*/
    
    //special chars
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("33° 5′ 40″ N; 32° 59′ 0″ E.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("33°5′40″N i 32°59′0″E.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("5·10-³ metros.")).length);
    
    // hashtags, domain names, mentions
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Una #mecion de @algunamigo en es.wikipedia.org")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("documentos publicados en ADSLZone.net")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Europa-Agricola.es es una página web dedicada a los anuncios")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("LANGUAGETOOL.ORG")).length);
    
    
  }

}
