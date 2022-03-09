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

package org.languagetool.rules.fr;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class AnglicismReplaceRuleTest {

  private AnglicismReplaceRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
    rule = new AnglicismReplaceRule(TestTools.getMessages("fr"));
    lt = new JLanguageTool(new French());
  }

  @Test
  public void testRule() throws IOException {

    // incorrect sentences:
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Le group"));
    assertEquals(1, matches.length);
    assertEquals("groupe", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("community manager"));
    assertEquals(1, matches.length);
    assertEquals("animateur de communauté", matches[0].getSuggestedReplacements().get(0));
    
    // correct sentences:
    matches = rule.match(lt.getAnalyzedSentence("Blue Man Group. The Gale Group. Google Maps"));
    assertEquals(0, matches.length);
    
 

  }

}
