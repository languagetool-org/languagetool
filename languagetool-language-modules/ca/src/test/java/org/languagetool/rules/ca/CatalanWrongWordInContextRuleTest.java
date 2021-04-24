/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Markus Brenneis
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
package org.languagetool.rules.ca;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Catalan;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class CatalanWrongWordInContextRuleTest {

  @Test
  public void testRule() throws IOException {
    CatalanWrongWordInContextRule rule = new CatalanWrongWordInContextRule(null);
    JLanguageTool lt = new JLanguageTool(new Catalan());
    
    //assertEquals(1, rule.match(lt.getAnalyzedSentence("La policia feia d'escolta.")).length);
    //assertEquals(0, rule.match(lt.getAnalyzedSentence("La policia feia escoltes telefòniques.")).length);
    //assertEquals(0, rule.match(lt.getAnalyzedSentence("La policia feia escoltes il·legals.")).length);
    //assertEquals(1, rule.match(lt.getAnalyzedSentence("Van escoltar el detingut fins al calabós.")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Li va infringir un mal terrible.")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("És un terreny abonat per als problemes.")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("No li va cosir bé les betes.")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Sempre li seguia la beta.")).length);
    //assertEquals(1, rule.match(lt.getAnalyzedSentence("un any en el qual la reina Victoria encara era al tro britànic")).length);
    
    //pali, pal·li
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Sota els palis.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Els pal·lis.")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("El pal·li i el sànscrit.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("El pali i el sànscrit.")).length);
    
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Vam comprar xocolate de mànec.")).length);
    
    assertEquals(1, rule.match(lt.getAnalyzedSentence("El pic de l'ocell.")).length);
  }
  
}
