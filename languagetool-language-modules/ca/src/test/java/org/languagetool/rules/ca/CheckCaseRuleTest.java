/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

public class CheckCaseRuleTest {
  private CheckCaseRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
    rule = new CheckCaseRule(TestTools.getMessages("ca"), new Catalan());
    lt = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("'Da Vinci'")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("‒ 'Da Vinci'")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("‒ ¡'Da Vinci'!")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("El Prat de Llobregat")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("08820 - El Prat de Llobregat")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("el Prat de Llobregat")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Da Vinci")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Amb Joan Pau i Josep Maria.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("ESTAT D'ALARMA")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("educació secundària")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Educació Secundària Obligatòria")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Educació Secundària obligatòria")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("d'educació secundària obligatòria")).length);
    
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Els drets humans")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Declaració Universal dels Drets Humans")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("El codi Da Vinci")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Declaració Universal dels drets humans")).length);
    
    // incorrect sentences:
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Joan pau"));
    assertEquals(1, matches.length);
    assertEquals("Joan Pau", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("Expedient de Regulació Temporal d'Ocupació"));
    assertEquals(1, matches.length);
    assertEquals("Expedient de regulació temporal d'ocupació", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("Em vaig entrevistar amb Joan maria"));
    assertEquals(1, matches.length);
    assertEquals("Joan Maria", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("Em vaig entrevistar amb Leonardo Da Vinci"));
    assertEquals(1, matches.length);
    assertEquals("da Vinci", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("-\"Leonardo Da Vinci\""));
    assertEquals(1, matches.length);
    assertEquals("da Vinci", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(lt.getAnalyzedSentence("-\"¿Leonardo Da Vinci?\""));
    assertEquals(1, matches.length);
    assertEquals("da Vinci", matches[0].getSuggestedReplacements().get(0));
        
  }
}
