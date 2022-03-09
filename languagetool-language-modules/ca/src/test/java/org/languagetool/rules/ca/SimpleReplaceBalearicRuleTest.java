/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class SimpleReplaceBalearicRuleTest {

  private SimpleReplaceBalearicRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
    rule = new SimpleReplaceBalearicRule(TestTools.getMessages("ca"));
    lt = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Això està força bé.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Joan Navarro no és de Navarra ni de Jerez.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Prosper Mérimée.")).length);

    // incorrect sentences:
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("El calcul del telefon."));
    assertEquals(2, matches.length);
    assertEquals("càlcul", matches[0].getSuggestedReplacements().get(0));
    assertEquals("telèfon", matches[1].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("EL CALCUL DEL TELEFON."));
    assertEquals(2, matches.length);
    assertEquals("CÀLCUL", matches[0].getSuggestedReplacements().get(0));
    assertEquals("TELÈFON", matches[1].getSuggestedReplacements().get(0));
        
  }

}
