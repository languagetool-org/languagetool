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
public class SimpleReplaceDNVRuleTest {

  private SimpleReplaceDNVRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws Exception {
    rule = new SimpleReplaceDNVRule(TestTools.getMessages("ca"));
    langTool = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ella és molt incauta.")).length);

    // incorrect sentences:
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("L'arxipèleg."));
    assertEquals(1, matches.length);
    assertEquals("arxipèlag", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("Els arxipèlegs"));
    assertEquals(1, matches.length);
    assertEquals("arxipèlags", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("Llavors no ensajaven"));
    assertEquals(1, matches.length);
    assertEquals("assajaven", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("acevéssiu"));
    assertEquals(1, matches.length);
    assertEquals("encebéssiu", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(langTool.getAnalyzedSentence("S'arropeixen"));
    assertEquals(1, matches.length);
    assertEquals("arrupeixen", matches[0].getSuggestedReplacements().get(0));
    assertEquals("arrupen", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(langTool.getAnalyzedSentence("incautaren"));
    assertEquals(1, matches.length);
    assertEquals("confiscaren", matches[0].getSuggestedReplacements().get(0));
    assertEquals("requisaren", matches[0].getSuggestedReplacements().get(1));
    assertEquals("comissaren", matches[0].getSuggestedReplacements().get(2));
    assertEquals("decomissaren", matches[0].getSuggestedReplacements().get(3));
  }

}
