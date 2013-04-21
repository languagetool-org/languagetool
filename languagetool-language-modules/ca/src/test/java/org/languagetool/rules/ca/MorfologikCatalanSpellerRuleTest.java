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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MorfologikCatalanSpellerRuleTest {

    @Test
    public void testMorfologikSpeller() throws IOException {
        MorfologikCatalanSpellerRule rule =
                new MorfologikCatalanSpellerRule (TestTools.getMessages("Catalan"), new Catalan());

        RuleMatch[] matches;
        JLanguageTool langTool = new JLanguageTool(new Catalan());


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
        // checks abbreviations 
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Viu al núm. 23 del carrer Nou.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("N'hi ha de color vermell, blau, verd, etc.")).length);
        
        // Test for Multiwords.
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Era vox populi.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Aquell era l'statu quo.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Va ser la XIV edició.")).length);
        
        //test for "LanguageTool":
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("LanguageTool!")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);
        
        //tests for mixed case words
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("pH")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("McDonald")).length);

        //incorrect sentences:

        matches = rule.match(langTool.getAnalyzedSentence("joan"));
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(4, matches[0].getToPos());
        assertEquals("Joan", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("abatusats"));
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(9, matches[0].getToPos());
        assertEquals("abatussats", matches[0].getSuggestedReplacements().get(0));
        
        // incomplete multiword
        matches = rule.match(langTool.getAnalyzedSentence("L'statu"));
        assertEquals(1, matches.length);
        assertEquals(2, matches[0].getFromPos());
        assertEquals(7, matches[0].getToPos());
        assertEquals("sta tu", matches[0].getSuggestedReplacements().get(0));

        matches = rule.match(langTool.getAnalyzedSentence("Pecra"));
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(5, matches[0].getToPos());
        assertEquals("Pera", matches[0].getSuggestedReplacements().get(2));
        
        matches = rule.match(langTool.getAnalyzedSentence("argüit"));
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(6, matches[0].getToPos());
        assertEquals("argüint", matches[0].getSuggestedReplacements().get(0));
        assertEquals("argüir", matches[0].getSuggestedReplacements().get(1));
        assertEquals("arguït", matches[0].getSuggestedReplacements().get(2));
        
        matches = rule.match(langTool.getAnalyzedSentence("ángel"));
        assertEquals(1, matches.length);
        assertEquals("Àngel", matches[0].getSuggestedReplacements().get(0));
        assertEquals("àngel", matches[0].getSuggestedReplacements().get(1));
        assertEquals("angle", matches[0].getSuggestedReplacements().get(2));
        assertEquals("anhel", matches[0].getSuggestedReplacements().get(3));
        
        matches = rule.match(langTool.getAnalyzedSentence("caçessim"));
        assertEquals(1, matches.length);
        assertEquals("caçàssim", matches[0].getSuggestedReplacements().get(0));
        assertEquals("cacessin", matches[0].getSuggestedReplacements().get(1));
        assertEquals("cacessis", matches[0].getSuggestedReplacements().get(2));
        assertEquals("cacéssim", matches[0].getSuggestedReplacements().get(3));
        
        matches = rule.match(langTool.getAnalyzedSentence("cantaríà"));
        assertEquals(1, matches.length);
        assertEquals("cantaria", matches[0].getSuggestedReplacements().get(0));
        assertEquals("cantarà", matches[0].getSuggestedReplacements().get(1));
        
        //best suggestion first
        matches = rule.match(langTool.getAnalyzedSentence("poguem"));
        assertEquals(1, matches.length);
        assertEquals("puguem", matches[0].getSuggestedReplacements().get(0));
        
        //incorrect mixed case words
        assertEquals(1, rule.match(langTool.getAnalyzedSentence("PH")).length);
        assertEquals(1, rule.match(langTool.getAnalyzedSentence("Ph")).length);
        assertEquals(1, rule.match(langTool.getAnalyzedSentence("MCDonald")).length);
        
        matches = rule.match(langTool.getAnalyzedSentence("tAula"));
        assertEquals(1, matches.length);
        assertEquals("taula", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("TAula"));
        assertEquals(1, matches.length);
        assertEquals("taula", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("col·Labora"));
        assertEquals(1, matches.length);
        assertEquals("col·labora", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("col·laborÀ"));
        assertEquals(1, matches.length);
        assertEquals("col·laborà", matches[0].getSuggestedReplacements().get(0));
        
        //capitalized wrong words
        matches = rule.match(langTool.getAnalyzedSentence("En la Pecra"));
        assertEquals(1, matches.length);
        assertEquals("Pedra", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Peira", matches[0].getSuggestedReplacements().get(1));
        
        assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);        
        
    }
    
}
