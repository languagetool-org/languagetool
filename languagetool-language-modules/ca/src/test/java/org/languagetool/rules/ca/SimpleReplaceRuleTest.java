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
public class SimpleReplaceRuleTest {

  private SimpleReplaceRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
    rule = new SimpleReplaceRule(TestTools.getMessages("ca"));
    lt = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Això està força bé.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Joan Navarro no és de Navarra ni de Jerez.")).length);

    // incorrect sentences:
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("El recader fa huelga."));
    assertEquals(2, matches.length);
    assertEquals("ordinari", matches[0].getSuggestedReplacements().get(0));
    assertEquals("transportista", matches[0].getSuggestedReplacements().get(1));
    assertEquals("vaga", matches[1].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("EEUU"));
    assertEquals(1, matches.length);
    assertEquals("EUA", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("Aconteixements"));
    assertEquals(1, matches.length);
    assertEquals("Esdeveniments", matches[0].getSuggestedReplacements().get(0));
    
  }

}
