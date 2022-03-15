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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.ValencianCatalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

/**
 * @author Jaume Ortolà
 */
public class SimpleReplaceDNVRuleTest {

  private SimpleReplaceDNVRule rule;
  private JLanguageTool lt;

  @BeforeEach
  public void setUp() throws Exception {
    rule = new SimpleReplaceDNVRule(TestTools.getMessages("ca"), new ValencianCatalan());
    lt = new JLanguageTool(new ValencianCatalan());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Ella és molt incauta.")).length);

    // incorrect sentences:
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("L'arxipèleg."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("arxipèlag", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("colmena"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("buc", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("rusc", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("colmenes"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("bucs", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("ruscos", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("ruscs", matches[0].getSuggestedReplacements().get(2));
    
    matches = rule.match(lt.getAnalyzedSentence("afincaments"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("establiments", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("instal·lacions", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("Els arxipèlegs"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("arxipèlags", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("acevéssiu"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("encebéssiu", matches[0].getSuggestedReplacements().get(0));
        
    matches = rule.match(lt.getAnalyzedSentence("S'arropeixen"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("arrupeixen", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("arrupen", matches[0].getSuggestedReplacements().get(1));
    
    matches = rule.match(lt.getAnalyzedSentence("incautaren"));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("confiscaren", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("requisaren", matches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("comissaren", matches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("decomissaren", matches[0].getSuggestedReplacements().get(3));
  }

}

