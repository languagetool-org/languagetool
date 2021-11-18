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
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MorfologikCatalanSpellerRuleTest {

    @Test
    public void testMorfologikSpeller() throws IOException {
        MorfologikCatalanSpellerRule rule =
                new MorfologikCatalanSpellerRule (TestTools.getMessages("ca"), new Catalan(), null, Collections.emptyList());

        RuleMatch[] matches;
        JLanguageTool lt = new JLanguageTool(new Catalan());

        // prefixes and suffixes.
        assertEquals(0, rule.match(lt.getAnalyzedSentence("S'autodefineixin com a populars.")).length);
        //assertEquals(0, rule.match(langTool.getAnalyzedSentence("Redibuixen el futur.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("L'exdirigent del partit.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("S'autoprenia.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("S'autocanta.")).length);
        
        // word not well-formed with prefix 
        assertEquals(1, rule.match(lt.getAnalyzedSentence("S'autopren.")).length);

        // correct sentences:
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Abacallanada")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Abatre-les-en")).length);
        
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Allò que més l'interessa.")).length);
        // checks that "WORDCHARS ·-'" is added to Hunspell .aff file
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Porta'n quatre al col·legi.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Has de portar-me'n moltes.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
        // Spellcheck dictionary contains Valencian and general accentuation
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Francès i francés.")).length);
        // checks abbreviations 
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Viu al núm. 23 del carrer Nou.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("N'hi ha de color vermell, blau, verd, etc.")).length);
              
        
        // Test for Multiwords.
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Era vox populi.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Aquell era l'statu quo.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Va ser la XIV edició.")).length);
        
        //test for "LanguageTool":
        assertEquals(0, rule.match(lt.getAnalyzedSentence("LanguageTool!")).length);
        
        //test for numbers, punctuation
        assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("1234,54")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("1.234,54")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("1 234,54")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("-1 234,54")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Fa una temperatura de 30°C")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Fa una temperatura de 30 °C")).length);
        assertEquals(1, rule.match(lt.getAnalyzedSentence("Any2010")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("−0,4 %, −0,4%.")).length); // minus sign
        
        
        //tests for mixed case words
        assertEquals(0, rule.match(lt.getAnalyzedSentence("pH")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("McDonald")).length);
        assertEquals(1, rule.match(lt.getAnalyzedSentence("AixòÉsUnError")).length);

        //incorrect words:
        
        matches = rule.match(lt.getAnalyzedSentence("Bordoy"));
        assertEquals(1, matches.length);
        
        //Bordó; Bordoi; Bordo; bordon
        assertEquals("Bordó", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Bordoi", matches[0].getSuggestedReplacements().get(1));
        assertEquals("Bordo", matches[0].getSuggestedReplacements().get(2));
        assertEquals("Bordon", matches[0].getSuggestedReplacements().get(3));
        
        matches = rule.match(lt.getAnalyzedSentence("Mal'aysia"));
        assertEquals(1, matches.length);
        
        matches = rule.match(lt.getAnalyzedSentence("Mala’ysia"));
        assertEquals(1, matches.length);
        
        matches = rule.match(lt.getAnalyzedSentence("Malaysia"));
        assertEquals(1, matches.length);
        assertEquals("Malàisia", matches[0].getSuggestedReplacements().get(0));
        assertEquals(1 , matches[0].getSuggestedReplacements().size());
        
        matches = rule.match(lt.getAnalyzedSentence("quna"));
        assertEquals(1, matches.length);
        assertEquals("que", matches[0].getSuggestedReplacements().get(0));
        assertEquals("una", matches[0].getSuggestedReplacements().get(1));
        assertEquals("quan", matches[0].getSuggestedReplacements().get(2));
        
        //capitalized suggestion
        matches = rule.match(lt.getAnalyzedSentence("Video"));
        assertEquals(1, matches.length);
        assertEquals("Vídeo", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("bànner"));
        assertEquals(1, matches.length);
        assertEquals("bàner", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("especialisats"));
        assertEquals(1, matches.length);
        assertEquals("especialitzats", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("colaborassió"));
        assertEquals(1, matches.length);
        assertEquals("col·laboració", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("colaboració"));
        assertEquals(1, matches.length);
        assertEquals("col·laboració", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"));
        assertEquals(1, matches.length);
        
        matches = rule.match(lt.getAnalyzedSentence("plassa"));
        assertEquals(1, matches.length);
        assertEquals("plaça", matches[0].getSuggestedReplacements().get(0));     
        
        matches = rule.match(lt.getAnalyzedSentence("Deú"));
        assertEquals(1, matches.length);
        assertEquals("Deu", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Déu", matches[0].getSuggestedReplacements().get(1));
        assertEquals("Dau", matches[0].getSuggestedReplacements().get(2));

        matches = rule.match(lt.getAnalyzedSentence("joan"));
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(4, matches[0].getToPos());
        assertEquals("Joan", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("abatusats"));
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(9, matches[0].getToPos());
        assertEquals("abatussats", matches[0].getSuggestedReplacements().get(0));
        
        // incomplete multiword
        matches = rule.match(lt.getAnalyzedSentence("L'statu"));
        assertEquals(1, matches.length);
        assertEquals(2, matches[0].getFromPos());
        assertEquals(7, matches[0].getToPos());
        assertEquals("tato", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("argüit"));
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(6, matches[0].getToPos());
        assertEquals("arguït", matches[0].getSuggestedReplacements().get(0));
        assertEquals("argüir", matches[0].getSuggestedReplacements().get(1));
        assertEquals("argüint", matches[0].getSuggestedReplacements().get(2));

        
        matches = rule.match(lt.getAnalyzedSentence("ángel"));
        assertEquals(1, matches.length);
        assertEquals("àngel", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Àngel", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("caçessim"));
        assertEquals(1, matches.length);
        assertEquals("cacéssim", matches[0].getSuggestedReplacements().get(0));
        assertEquals("cassàssim", matches[0].getSuggestedReplacements().get(1));
        assertEquals("casséssim", matches[0].getSuggestedReplacements().get(2));
        assertEquals("casàssim", matches[0].getSuggestedReplacements().get(3));
        assertEquals("caséssim", matches[0].getSuggestedReplacements().get(4));
        
        matches = rule.match(lt.getAnalyzedSentence("coche"));
        assertEquals(1, matches.length);
        assertEquals("cotxe", matches[0].getSuggestedReplacements().get(0));
        assertEquals("cuixa", matches[0].getSuggestedReplacements().get(1));
        assertEquals("coixa", matches[0].getSuggestedReplacements().get(2));
        
        
        matches = rule.match(lt.getAnalyzedSentence("cantaríà"));
        assertEquals(1, matches.length);
        assertEquals("cantaria", matches[0].getSuggestedReplacements().get(0));
        assertEquals("cantera", matches[0].getSuggestedReplacements().get(1));
        
        //best suggestion first
        matches = rule.match(lt.getAnalyzedSentence("poguem"));
        assertEquals(1, matches.length);
        assertEquals("puguem", matches[0].getSuggestedReplacements().get(0));
        
        //incorrect mixed case words
        assertEquals(1, rule.match(lt.getAnalyzedSentence("PH")).length);
        assertEquals(1, rule.match(lt.getAnalyzedSentence("Ph")).length);
        assertEquals(1, rule.match(lt.getAnalyzedSentence("MCDonald")).length);
        
        matches = rule.match(lt.getAnalyzedSentence("tAula"));
        assertEquals(1, matches.length);
        assertEquals("taula", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("TAula"));
        assertEquals(1, matches.length);
        assertEquals("Taula", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("col·Labora"));
        assertEquals(1, matches.length);
        assertEquals("col·labora", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("col·laborÀ"));
        assertEquals(1, matches.length);
        assertEquals("col·labora", matches[0].getSuggestedReplacements().get(0));
        assertEquals("col·laborà", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("después"));
        assertEquals(1, matches.length);
        assertEquals("després", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("dessinstalasio"));
        assertEquals(1, matches.length);
        assertEquals("desinstal·làssiu", matches[0].getSuggestedReplacements().get(0));
        assertEquals("desinstal·lació", matches[0].getSuggestedReplacements().get(1));
       

        matches = rule.match(lt.getAnalyzedSentence("matitzàrem"));
        assertEquals(1, matches.length);
        assertEquals("matisarem", matches[0].getSuggestedReplacements().get(0));
        assertEquals("matisàrem", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("tamitzéssim"));
        assertEquals(1, matches.length);
        assertEquals("t'amistéssim", matches[0].getSuggestedReplacements().get(0));
        assertEquals("tamisàssim", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("adquireixquen"));
        assertEquals(1, matches.length);
        assertEquals("adquirisquen", matches[0].getSuggestedReplacements().get(0));
        assertEquals("adquiresquen", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("calificar"));
        assertEquals(1, matches.length);
        assertEquals("qualificar", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("desconte"));
        assertEquals(1, matches.length);
        assertEquals("d'escolta", matches[0].getSuggestedReplacements().get(0));
        assertEquals("descompte", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("transtors"));
        //assertEquals("trastorns", matches[0].getSuggestedReplacements().get(0)); TODO: update info file
        matches = rule.match(lt.getAnalyzedSentence("pissara"));
        //assertEquals("pissarra", matches[0].getSuggestedReplacements().get(0)); TODO: update info file
        
        
        matches = rule.match(lt.getAnalyzedSentence("atentats"));
        assertEquals("atemptats", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("síntomes"));
        assertEquals("símptomes", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("atentats"));
        assertEquals("atemptats", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("contable"));
        assertEquals("comptable", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("desició"));
        assertEquals("d'edició", matches[0].getSuggestedReplacements().get(0));
        assertEquals("decisió", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("España"));
        assertEquals("Espanya", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("concenciosament"));
        assertEquals("conscienciosament", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("excelent"));
        assertEquals("excel·lent", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("exceleixquen"));
        assertEquals("excel·lisquen", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("caligrafia"));
        assertEquals("cal·ligrafia", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("calificaren"));
        assertEquals("qualificaren", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Excelentissim"));
        assertEquals("Excel·lentíssim", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("IlustRISIM"));
        assertEquals("Il·lustríssim", matches[0].getSuggestedReplacements().get(0));
        //matches = rule.match(langTool.getAnalyzedSentence("Xicago"));
        //assertEquals("Chicago", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("Chile"));
        assertEquals("Xile", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("transment"));
        assertEquals("transmet", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("nordment"));
        assertEquals("normant", matches[0].getSuggestedReplacements().get(0));
        
        //matches = rule.match(langTool.getAnalyzedSentence("transtors"));
        //assertEquals("trastorns", matches[0].getSuggestedReplacements().get(0));
        
        // Needs Morfologik Speller 2.1.0
        matches = rule.match(lt.getAnalyzedSentence("milisegons"));
        assertEquals("mil·lisegons", matches[0].getSuggestedReplacements().get(0));
        
        /*  change in Speller necessary: words of length = 4*/
        matches = rule.match(lt.getAnalyzedSentence("nula"));
        assertEquals("nul·la", matches[0].getSuggestedReplacements().get(0));
        
        //capitalized wrong words
        matches = rule.match(lt.getAnalyzedSentence("En la Pecra"));
        assertEquals(1, matches.length);
        assertEquals("Para", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Pare", matches[0].getSuggestedReplacements().get(1));

        matches = rule.match(lt.getAnalyzedSentence("IVa"));
        assertEquals(1, matches.length);
        assertEquals("Iva", matches[0].getSuggestedReplacements().get(0));
        assertEquals("IVA", matches[0].getSuggestedReplacements().get(1));

        matches = rule.match(lt.getAnalyzedSentence("Dvd"));
        assertEquals(1, matches.length);
        assertEquals("DVD", matches[0].getSuggestedReplacements().get(0));
        
        // deprecated characters of "ela geminada"
        assertEquals(0, rule.match(lt.getAnalyzedSentence("S'hi havien instaŀlat.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("S'HI HAVIEN INSTAĿLAT.")).length);

        assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("a")).length);
        
        matches = rule.match(lt.getAnalyzedSentence("Windows10"));
        assertEquals("Windows 10", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Windows", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("windows10"));
        assertEquals("Windows 10", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Windows", matches[0].getSuggestedReplacements().get(1));
        
        // pronoms febles
        matches = rule.match(lt.getAnalyzedSentence("ferse"));
        assertEquals("fer-se", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Magradaria"));
        assertEquals("M'agradaria", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("tenvio"));
        assertEquals("t'envio", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("consultins"));
        assertEquals("consulti'ns", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("portarvos"));
        assertEquals("portar-vos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("portemne"));
        assertEquals("portem-ne", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("dacontentar"));
        assertEquals("d'acontentar", matches[0].getSuggestedReplacements().get(0));
        assertEquals("acontentar", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("devidents"));
        assertEquals("de vidents", matches[0].getSuggestedReplacements().get(0));
        assertEquals("d'evidents", matches[0].getSuggestedReplacements().get(1));
        assertEquals("evidents", matches[0].getSuggestedReplacements().get(2));
        matches = rule.match(lt.getAnalyzedSentence("lacomplexat"));
        assertEquals("la complexat", matches[0].getSuggestedReplacements().get(0));
        assertEquals("l'acomplexat", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("dacomplexats"));
        assertEquals("d'acomplexats", matches[0].getSuggestedReplacements().get(0));
        assertEquals("acomplexats", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("lacomplexats"));
        assertEquals("la complexats", matches[0].getSuggestedReplacements().get(0));
        assertEquals("acomplexats", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("veurehi"));
        assertEquals("veure-hi", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("veureles"));
        assertEquals("veure-les", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("lilla"));
        assertEquals("Lilla", matches[0].getSuggestedReplacements().get(0));
        assertEquals("l'illa", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("portas"));
        assertEquals("portàs", matches[0].getSuggestedReplacements().get(0));
        assertEquals("portes", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("mantenir'me"));
        assertEquals("mantenir-me", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("elcap"));
        assertEquals("el cap", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("almeu"));
        assertEquals("al meu", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("delteu"));
        assertEquals("del teu", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("unshomes"));
        assertEquals("uns homes", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("pelsseus"));
        assertEquals("pels seus", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("daquesta"));
        assertEquals("d'aquesta", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("daquelles"));
        assertEquals("d'aquelles", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("lah"));
        assertEquals("la", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("dela"));
        assertEquals("Dela", matches[0].getSuggestedReplacements().get(0));
        assertEquals("de la", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("sha"));
        assertEquals("s'ha", matches[0].getSuggestedReplacements().get(0));
        assertEquals("xe", matches[0].getSuggestedReplacements().get(1));
        assertEquals("xa", matches[0].getSuggestedReplacements().get(2));
        matches = rule.match(lt.getAnalyzedSentence("Sha"));
        assertEquals("S'ha", matches[0].getSuggestedReplacements().get(0));
        //assertEquals("Xe", matches[0].getSuggestedReplacements().get(1));
        //assertEquals("Xa", matches[0].getSuggestedReplacements().get(2));
        matches = rule.match(lt.getAnalyzedSentence("avegades"));
        assertEquals("a vegades", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Encanvi"));
        assertEquals("En canvi", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Nosé"));
        assertEquals("No sé", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("air"));
        assertEquals("Aïr", matches[0].getSuggestedReplacements().get(0));
        assertEquals("aïr", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("Misiones"));
        assertEquals("Missiones", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("quedan"));
        assertEquals("queden", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("portan"));
        assertEquals("porten", matches[0].getSuggestedReplacements().get(0));
        assertEquals("porta'n", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("portans"));
        assertEquals("porta'ns", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("porta'nshi"));
        //assertEquals("porta'ns-hi", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("porto'nz"));
        //assertEquals("porta'ns", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("portalhi"));
        assertEquals("portal hi", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("veurels"));
        assertEquals("veure'ls", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("veuret"));
        assertEquals("veure", matches[0].getSuggestedReplacements().get(0));
        assertEquals("veure't", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("veures"));
        assertEquals("veure's", matches[0].getSuggestedReplacements().get(0));
        assertEquals("beures", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("serlo"));
        assertEquals("ser-lo", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("verlo"));
        assertEquals("parlo", matches[0].getSuggestedReplacements().get(0));
        assertEquals("baró", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("relajarme"));
        assertEquals("relaxar-me", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("aborrirnos"));
        assertEquals("avorrir-nos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("aborirnos"));
        assertEquals("avorrir-nos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("sescontaminarla"));
        assertEquals("descontaminar-la", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("daconseguirlos"));
        assertEquals("aconseguir-los", matches[0].getSuggestedReplacements().get(0));
        assertEquals("aconseguiràs", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("laconseguirlos"));
        assertEquals("aconseguir-los", matches[0].getSuggestedReplacements().get(0));
        assertEquals("aconseguiràs", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("portarinhi"));
        assertEquals("portaran", matches[0].getSuggestedReplacements().get(0));
        assertEquals("portaria", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("norueg"));
        assertEquals("noruega", matches[0].getSuggestedReplacements().get(0));
        assertEquals("noruec", matches[0].getSuggestedReplacements().get(1));
        //matches = rule.match(langTool.getAnalyzedSentence("prenense"));
        //assertEquals("prenent-se", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("cual"));
        assertEquals("qual", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("m0entretinc"));
        assertEquals("m'entretinc", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("m9entretinc"));
        assertEquals("m'entretinc", matches[0].getSuggestedReplacements().get(0));
        
        
        //Ela geminada 
        matches = rule.match(lt.getAnalyzedSentence("La sol•licitud"));
        assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("La sol-licitud"));
        assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("La sol⋅licitud"));
        assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("La sol∙licitud"));
        assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("un estat sindical-laborista"));
        assertEquals(0, matches.length);
        matches = rule.match(lt.getAnalyzedSentence("en un estat sindical.La classe obrera"));
        assertEquals(0, matches.length);
        matches = rule.match(lt.getAnalyzedSentence("al-Ladjdjun"));
        assertEquals(3,matches[0].getFromPos());
        assertEquals(11,matches[0].getToPos());
        // "ela geminada" error + another spelling error
        matches = rule.match(lt.getAnalyzedSentence("La sol•licitut")); 
        assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Il•lustran")); 
        assertEquals("Il·lustren", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("bél.lica")); 
        assertEquals("bèl·lica", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("mercar")); 
        assertEquals("marcar", matches[0].getSuggestedReplacements().get(0));
        assertEquals("Mercer", matches[0].getSuggestedReplacements().get(1));
        
        //majúscules
        matches = rule.match(lt.getAnalyzedSentence("De PH 4")); 
        assertEquals("pH", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("De l'any 156 Ac a l'any 2000.")); 
        assertEquals("aC", matches[0].getSuggestedReplacements().get(0)); 
        
        //split words
        assertEquals(2, rule.match(lt.getAnalyzedSentence("sobre el llit d'en Ron i el va colpir la certesa del que havia passat amb la força d'un troll quan envesteix")).length);
        matches = rule.match(lt.getAnalyzedSentence("unaa juda")); 
        assertEquals("una ajuda", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("elsi nteressos")); 
        assertEquals("els interessos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("el sinteressos")); 
        //assertEquals("els interessos", matches[0].getSuggestedReplacements().get(0));
        assertEquals("interessos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("ell ustre")); 
        assertEquals("el lustre", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("unah ora")); 
        assertEquals("una hora", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("benv inguts")); 
        assertEquals("ben vinguts", matches[0].getSuggestedReplacements().get(0));
        assertEquals("benvinguts", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("benam at")); 
        assertEquals("bena mat", matches[0].getSuggestedReplacements().get(0));
        assertEquals("benamat", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("estimade s"));
        assertEquals("estimada", matches[0].getSuggestedReplacements().get(0));
        //assertEquals("estimades", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("estimad es")); 
        assertEquals("estimar", matches[0].getSuggestedReplacements().get(0));
        assertEquals("estimat", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("co nstel·lació")); 
        assertEquals("constel·lació", matches[0].getSuggestedReplacements().get(0));
        
        //diacritics
        matches = rule.match(lt.getAnalyzedSentence("literaria"));
        assertEquals("literària", matches[0].getSuggestedReplacements().get(0));
        assertEquals("l'iteraria", matches[0].getSuggestedReplacements().get(1));
        
        // different speller dictionaries Cat/Val
        matches = rule.match(lt.getAnalyzedSentence("ingeniaria")); 
        assertEquals(1, matches.length);
        
        matches = rule.match(lt.getAnalyzedSentence("l'unic")); 
        assertEquals(1, matches.length);
        assertEquals("únic", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("rà dio")); 
        assertEquals("ràdio", matches[0].getSuggestedReplacements().get(0));
        assertEquals(0, matches[0].getFromPos());
        assertEquals(6, matches[0].getToPos());
        
        //don't split prefixes
        matches = rule.match(lt.getAnalyzedSentence("multiindisciplina")); 
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getSuggestedReplacements().size());
        
        //Casing
        matches = rule.match(lt.getAnalyzedSentence("SOL.LICITUD"));
        assertEquals("SOL·LICITUD", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("PROBATURA")); 
        assertEquals(1, matches.length);
        assertEquals(1, matches[0].getSuggestedReplacements().size());
        assertEquals("PROVATURA", matches[0].getSuggestedReplacements().get(0));
        
        //special chars
        assertEquals(0, rule.match(lt.getAnalyzedSentence("33° 5′ 40″ N i 32° 59′ 0″ E.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("33°5′40″N i 32°59′0″E.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Fa 5·10-³ metres.")).length);
        
        // mentions, hashtags, domain names
        assertEquals(0, rule.match(lt.getAnalyzedSentence("Parlem del #temagran amb @algugros en algunacosa.cat.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("En el domini .org hi ha fitxers d'extensió .txt.")).length);
        assertEquals(0, rule.match(lt.getAnalyzedSentence("En el domini .live hi ha fitxers d'extensió .7z.")).length);
        
        matches = rule.match(lt.getAnalyzedSentence("En1993"));
        assertEquals(1, matches.length);
        assertEquals("En 1993", matches[0].getSuggestedReplacements().get(0));
        
    }
}
