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
  private JLanguageTool langTool;

  @Before
  public void setUp() throws Exception {
    rule = new CheckCaseRule(TestTools.getMessages("ca"), new Catalan());
    langTool = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("'Da Vinci'")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("‒ 'Da Vinci'")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("‒ ¡'Da Vinci'!")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("El Prat de Llobregat")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("08820 - El Prat de Llobregat")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("el Prat de Llobregat")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Da Vinci")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Amb Joan Pau i Josep Maria.")).length);
    
    // incorrect sentences:
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Joan pau"));
    assertEquals(1, matches.length);
    assertEquals("Joan Pau", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("Expedient de Regulació Temporal d'Ocupació"));
    assertEquals(1, matches.length);
    assertEquals("Expedient de regulació temporal d'ocupació", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("Em vaig entrevistar amb Joan maria"));
    assertEquals(1, matches.length);
    assertEquals("Joan Maria", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("Em vaig entrevistar amb Leonardo Da Vinci"));
    assertEquals(1, matches.length);
    assertEquals("da Vinci", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("-\"Leonardo Da Vinci\""));
    assertEquals(1, matches.length);
    assertEquals("da Vinci", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("-\"¿Leonardo Da Vinci?\""));
    assertEquals(1, matches.length);
    assertEquals("da Vinci", matches[0].getSuggestedReplacements().get(0));
        
  }
}
