package org.languagetool.rules.ca;

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
public class CatalanNumberSpellRuleTest {

  private CatalanNumberSpellRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws Exception {
    langTool = new JLanguageTool(new Catalan());
    rule = new CatalanNumberSpellRule(TestTools.getMessages("ca"), langTool.getLanguage());
    
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("34523 dies.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("3124 anys.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("3124 anys")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("3124 mil·lenis")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("3124 mil·lenis de tempts.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("533 persones")).length);

    // incorrect sentences:
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("3100 anys."));
    assertEquals(1, matches.length);
    assertEquals("tres mil cent", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("220 setmanes."));
    assertEquals(1, matches.length);
    assertEquals("dues-centes vint", matches[0].getSuggestedReplacements().get(0));
        
    matches = rule.match(langTool.getAnalyzedSentence("1 mil·lenni de."));
    assertEquals(1, matches.length);
    assertEquals("un", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("1 setmana."));
    assertEquals(1, matches.length);
    assertEquals("una", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("2000000 anys."));
    assertEquals(1, matches.length);
    assertEquals("dos milions", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(langTool.getAnalyzedSentence("2000000 dècades"));
    assertEquals(1, matches.length);
    assertEquals("dos milions", matches[0].getSuggestedReplacements().get(0));
  }

}
