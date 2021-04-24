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
package org.languagetool.rules.es;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class SpanishWrongWordInContextRuleTest {

  @Test
  public void testRule() throws IOException {
    SpanishWrongWordInContextRule rule = new SpanishWrongWordInContextRule(null);
    JLanguageTool lt = new JLanguageTool(new Spanish());
    
    // infligir / infringir
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Le infringió un duro castigo"));
    assertEquals(1, matches.length);
    assertEquals("infligió", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("Infligía todas las normas."));
    assertEquals(1, matches.length);
    assertEquals("Infringía", matches[0].getSuggestedReplacements().get(0));
    
    //baca /vaca
    matches = rule.match(lt.getAnalyzedSentence("La baca da leche."));
    assertEquals(2, matches.length);
    assertEquals("vaca", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("Pon la maleta en la vaca."));
    assertEquals(1, matches.length);
    assertEquals("baca", matches[0].getSuggestedReplacements().get(0));
    
  }
  
}
