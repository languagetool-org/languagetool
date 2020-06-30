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

package org.languagetool.rules.fr;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MorfologikFrenchSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    MorfologikFrenchSpellerRule rule = new MorfologikFrenchSpellerRule(TestTools.getMessages("fr"), new French(), null,
        Collections.emptyList());

    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(new French());

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Écoute-moi.")).length);

    // TODO: autour de 37°C, Si vous prêtez 20$, est 300 000 yen
     
    
    // Test for Multiwords.
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("vox populi")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("statu quo.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Bugs Bunny")).length);

    // tests for mixed case words
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("pH")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("McDonald's")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("McDonald’s")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("McDonald")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("thisisanerror")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("thisIsAnError")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Thisisanerror")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("ThisIsAnError")).length);

    // incorrect words:
    matches = rule.match(langTool.getAnalyzedSentence("ecoute-moi"));
    assertEquals(1, matches.length);
    assertEquals("écoute", matches[0].getSuggestedReplacements().get(0));
    // TODO: is "écoutè" correct?
    assertEquals("écoutè", matches[0].getSuggestedReplacements().get(1));
    assertEquals("écouté", matches[0].getSuggestedReplacements().get(2));
    assertEquals("coute", matches[0].getSuggestedReplacements().get(3));

    matches = rule.match(langTool.getAnalyzedSentence("ecrit-il"));
    assertEquals(1, matches.length);
    assertEquals("écrit", matches[0].getSuggestedReplacements().get(0));
    assertEquals("décrit", matches[0].getSuggestedReplacements().get(1));

    matches = rule.match(langTool.getAnalyzedSentence("Mcdonald"));
    assertEquals(1, matches.length);
    assertEquals("McDonald", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Macdonald", matches[0].getSuggestedReplacements().get(1));

    matches = rule.match(langTool.getAnalyzedSentence("Lhomme"));
    assertEquals(1, matches.length);
    assertEquals("L'homme", matches[0].getSuggestedReplacements().get(0));
    matches = rule.match(langTool.getAnalyzedSentence("dhommes"));
    assertEquals(1, matches.length);
    assertEquals("d'hommes", matches[0].getSuggestedReplacements().get(0));

    // don't split prefixes 
    matches = rule.match(langTool.getAnalyzedSentence("macrodiscipline"));
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getSuggestedReplacements().size());

  }
}
