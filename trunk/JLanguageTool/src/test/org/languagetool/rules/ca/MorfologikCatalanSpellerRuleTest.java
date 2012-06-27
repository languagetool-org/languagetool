/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

public class MorfologikCatalanSpellerRuleTest {

    @Test
    public void testMorfologikSpeller() throws IOException {
        MorfologikCatalanSpellerRule rule =
                new MorfologikCatalanSpellerRule (TestTools.getMessages("Catalan"), Language.CATALAN);

        RuleMatch[] matches;
        JLanguageTool langTool = new JLanguageTool(Language.CATALAN);


        // correct sentences:
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Abacallanada")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Abatre-les-en")).length);
        
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Allò que més l'interessa.")).length);
        // checks that "WORDCHARS ·-'" is added to Hunspell .aff file
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Porta'n quatre al col·legi.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Has de portar-me'n moltes.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
        // Spellcheck dictionary contains Valencian and general accentuation
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Francès i francés.")).length);
        
        
        //test for "LanguageTool":
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("LanguageTool!")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);

        //incorrect sentences:

        matches = rule.match(langTool.getAnalyzedSentence("Abatusats"));
        // check match positions:
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(9, matches[0].getToPos());
        assertEquals("Abatussats", matches[0].getSuggestedReplacements().get(0));

      //incorrect sentences:
        matches = rule.match(langTool.getAnalyzedSentence("Pecra"));
        // check match positions:
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(5, matches[0].getToPos());
        assertEquals("Pera", matches[0].getSuggestedReplacements().get(2));
        
        assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);

        
        
    }
    
}
