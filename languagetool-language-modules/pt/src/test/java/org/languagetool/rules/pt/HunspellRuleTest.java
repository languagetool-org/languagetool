/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà (http://www.languagetool.org)
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
package org.languagetool.rules.pt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

public class HunspellRuleTest {

  @Test
  public void testRule() throws Exception {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("pt"), Languages.getLanguageForShortCode("pt-PT"), null);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("pt-PT"));
    
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("A família.")).length);
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("A familia.")); 
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("família", matches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("familiar", matches[0].getSuggestedReplacements().get(1));
    
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Covid-19, COVID-19, covid-19.")).length);
    
    matches = rule.match(lt.getAnalyzedSentence("eu ja fiz isso.")); 
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("já", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("eu so")); 
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("só", matches[0].getSuggestedReplacements().get(0));
    
    matches = rule.match(lt.getAnalyzedSentence("é so")); 
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals("só", matches[0].getSuggestedReplacements().get(0));

    lt.check("- Encontre no autoconheciemen");  // No "Could not map 29 to original position." issue
  }
}
