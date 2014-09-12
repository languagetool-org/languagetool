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
                new MorfologikCatalanSpellerRule (TestTools.getMessages("ca"), new Catalan());

        RuleMatch[] matches;
        JLanguageTool langTool = new JLanguageTool(new Catalan());

        // prefixes and suffixes.
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("S'autodefineixin com a populars.")).length);
        //assertEquals(0, rule.match(langTool.getAnalyzedSentence("Redibuixen el futur.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("L'exdirigent del partit.")).length);

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
        
        //test for numbers, punctuation
        assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("1234,54")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("1.234,54")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("1 234,54")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("-1 234,54")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Fa una temperatura de 30°C")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Fa una temperatura de 30 °C")).length);
        assertEquals(1, rule.match(langTool.getAnalyzedSentence("Any2010")).length);
        
        //tests for mixed case words
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("pH")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("McDonald")).length);
        assertEquals(1, rule.match(langTool.getAnalyzedSentence("AixòÉsUnError")).length);

        //incorrect words:
        
        matches = rule.match(langTool.getAnalyzedSentence("Bordoy"));
        assertEquals(1, matches.length);
        
        //Bordó; Bordoi; Bordo; bordon
        assertEquals("Bordó", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Bordoi", matches[0].getSuggestedReplacements().get(1));
        assertEquals("Bordo", matches[0].getSuggestedReplacements().get(2));
        assertEquals("Bordon", matches[0].getSuggestedReplacements().get(3));
        
        matches = rule.match(langTool.getAnalyzedSentence("Malaysia"));
        assertEquals(1, matches.length);
        assertEquals("Malàisia", matches[0].getSuggestedReplacements().get(0));
        assertEquals(1 , matches[0].getSuggestedReplacements().size());
        
        matches = rule.match(langTool.getAnalyzedSentence("quna"));
        assertEquals(1, matches.length);
        assertEquals("que", matches[0].getSuggestedReplacements().get(0));
        assertEquals("una", matches[0].getSuggestedReplacements().get(1));
        assertEquals("quan", matches[0].getSuggestedReplacements().get(2));
        
        //capitalized suggestion
        matches = rule.match(langTool.getAnalyzedSentence("Video"));
        assertEquals(1, matches.length);
        assertEquals("Vídeo", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("bànner"));
        assertEquals(1, matches.length);
        assertEquals("bàner", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("especialisats"));
        assertEquals(1, matches.length);
        assertEquals("especialitzats", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("colaborassió"));
        assertEquals(1, matches.length);
        assertEquals("col·laboració", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("colaboració"));
        assertEquals(1, matches.length);
        assertEquals("col·laboració", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"));
        assertEquals(1, matches.length);
        
        matches = rule.match(langTool.getAnalyzedSentence("plassa"));
        assertEquals(1, matches.length);
        assertEquals("plaça", matches[0].getSuggestedReplacements().get(0));     
        
        matches = rule.match(langTool.getAnalyzedSentence("Deú"));
        assertEquals(1, matches.length);
        assertEquals("Deu", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Déu", matches[0].getSuggestedReplacements().get(1));

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
        assertEquals("tato", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("argüit"));
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(6, matches[0].getToPos());
        assertEquals("arguït", matches[0].getSuggestedReplacements().get(0));
        assertEquals("argüir", matches[0].getSuggestedReplacements().get(1));
        assertEquals("argüint", matches[0].getSuggestedReplacements().get(2));

        
        matches = rule.match(langTool.getAnalyzedSentence("ángel"));
        assertEquals(1, matches.length);
        assertEquals("àngel", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Àngel", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(langTool.getAnalyzedSentence("caçessim"));
        assertEquals(1, matches.length);
        assertEquals("cacéssim", matches[0].getSuggestedReplacements().get(0));
        assertEquals("cassàssim", matches[0].getSuggestedReplacements().get(1));
        assertEquals("casséssim", matches[0].getSuggestedReplacements().get(2));
        assertEquals("casàssim", matches[0].getSuggestedReplacements().get(3));
        assertEquals("caséssim", matches[0].getSuggestedReplacements().get(4));
        
        matches = rule.match(langTool.getAnalyzedSentence("coche"));
        assertEquals(1, matches.length);
        assertEquals("cotxe", matches[0].getSuggestedReplacements().get(0));
        assertEquals("cuixa", matches[0].getSuggestedReplacements().get(1));
        assertEquals("coixa", matches[0].getSuggestedReplacements().get(2));
        
        
        matches = rule.match(langTool.getAnalyzedSentence("cantaríà"));
        assertEquals(1, matches.length);
        assertEquals("cantaria", matches[0].getSuggestedReplacements().get(0));
        assertEquals("cantera", matches[0].getSuggestedReplacements().get(1));
        
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
        assertEquals("Taula", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("col·Labora"));
        assertEquals(1, matches.length);
        assertEquals("col·labora", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("col·laborÀ"));
        assertEquals(1, matches.length);
        assertEquals("col·laborà", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("después"));
        assertEquals(1, matches.length);
        assertEquals("després", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("dessinstalasio"));
        assertEquals(1, matches.length);
        assertEquals("desinstal·làssiu", matches[0].getSuggestedReplacements().get(0));
        assertEquals("desinstal·lació", matches[0].getSuggestedReplacements().get(1));
       

        matches = rule.match(langTool.getAnalyzedSentence("matitzàrem"));
        assertEquals(1, matches.length);
        assertEquals("matisarem", matches[0].getSuggestedReplacements().get(0));
        assertEquals("matisàrem", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(langTool.getAnalyzedSentence("tamitzéssim"));
        assertEquals(1, matches.length);
        assertEquals("tamisàssim", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(langTool.getAnalyzedSentence("adquireixquen"));
        assertEquals(1, matches.length);
        assertEquals("adquirisquen", matches[0].getSuggestedReplacements().get(0));
        assertEquals("adquiresquen", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(langTool.getAnalyzedSentence("calificar"));
        assertEquals(1, matches.length);
        assertEquals("qualificar", matches[0].getSuggestedReplacements().get(0));
        
        //capitalized wrong words
        matches = rule.match(langTool.getAnalyzedSentence("En la Pecra"));
        assertEquals(1, matches.length);
        assertEquals("Para", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Pare", matches[0].getSuggestedReplacements().get(1));

        matches = rule.match(langTool.getAnalyzedSentence("IVa"));
        assertEquals(1, matches.length);
        assertEquals("Iva", matches[0].getSuggestedReplacements().get(0));
        assertEquals("IVA", matches[0].getSuggestedReplacements().get(1));

        matches = rule.match(langTool.getAnalyzedSentence("Dvd"));
        assertEquals(1, matches.length);
        assertEquals("DVD", matches[0].getSuggestedReplacements().get(0));
        
        // deprecated characters of "ela geminada"
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("S'hi havien instaŀlat.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("S'HI HAVIEN INSTAĿLAT.")).length);

        assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);

    }
    
}
