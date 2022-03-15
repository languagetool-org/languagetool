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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

public class MorfologikCatalanSpellerRuleTest {

    @Test
    public void testMorfologikSpeller() throws IOException {
        MorfologikCatalanSpellerRule rule =
                new MorfologikCatalanSpellerRule (TestTools.getMessages("ca"), new Catalan(), null, Collections.emptyList());

        RuleMatch[] matches;
        JLanguageTool lt = new JLanguageTool(new Catalan());

        // prefixes and suffixes.
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("S'autodefineixin com a populars.")).length);
        //assertEquals(0, rule.match(langTool.getAnalyzedSentence("Redibuixen el futur.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("L'exdirigent del partit.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("S'autoprenia.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("S'autocanta.")).length);
        
        // word not well-formed with prefix 
        Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("S'autopren.")).length);

        // correct sentences:
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Abacallanada")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Abatre-les-en")).length);
        
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Allò que més l'interessa.")).length);
        // checks that "WORDCHARS ·-'" is added to Hunspell .aff file
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Porta'n quatre al col·legi.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Has de portar-me'n moltes.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
        // Spellcheck dictionary contains Valencian and general accentuation
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Francès i francés.")).length);
        // checks abbreviations 
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Viu al núm. 23 del carrer Nou.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("N'hi ha de color vermell, blau, verd, etc.")).length);
              
        
        // Test for Multiwords.
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Era vox populi.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Aquell era l'statu quo.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Va ser la XIV edició.")).length);
        
        //test for "LanguageTool":
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("LanguageTool!")).length);
        
        //test for numbers, punctuation
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("1234,54")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("1.234,54")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("1 234,54")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("-1 234,54")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Fa una temperatura de 30°C")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Fa una temperatura de 30 °C")).length);
        Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Any2010")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("−0,4 %, −0,4%.")).length); // minus sign
        
        
        //tests for mixed case words
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("pH")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("McDonald")).length);
        Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("AixòÉsUnError")).length);

        //incorrect words:
        
        matches = rule.match(lt.getAnalyzedSentence("Bordoy"));
        Assertions.assertEquals(1, matches.length);
        
        //Bordó; Bordoi; Bordo; bordon
        Assertions.assertEquals("Bordó", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("Bordoi", matches[0].getSuggestedReplacements().get(1));
        Assertions.assertEquals("Bordo", matches[0].getSuggestedReplacements().get(2));
        Assertions.assertEquals("Bordon", matches[0].getSuggestedReplacements().get(3));
        
        matches = rule.match(lt.getAnalyzedSentence("Mal'aysia"));
        Assertions.assertEquals(1, matches.length);
        
        matches = rule.match(lt.getAnalyzedSentence("Mala’ysia"));
        Assertions.assertEquals(1, matches.length);
        
        matches = rule.match(lt.getAnalyzedSentence("Malaysia"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("Malàisia", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals(1, matches[0].getSuggestedReplacements().size());
        
        matches = rule.match(lt.getAnalyzedSentence("quna"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("que", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("una", matches[0].getSuggestedReplacements().get(1));
        Assertions.assertEquals("quan", matches[0].getSuggestedReplacements().get(2));
        
        //capitalized suggestion
        matches = rule.match(lt.getAnalyzedSentence("Video"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("Vídeo", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("bànner"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("bàner", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("especialisats"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("especialitzats", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("colaborassió"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("col·laboració", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("colaboració"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("col·laboració", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"));
        Assertions.assertEquals(1, matches.length);
        
        matches = rule.match(lt.getAnalyzedSentence("plassa"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("plaça", matches[0].getSuggestedReplacements().get(0));     
        
        matches = rule.match(lt.getAnalyzedSentence("Deú"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("Deu", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("Déu", matches[0].getSuggestedReplacements().get(1));
        Assertions.assertEquals("Dau", matches[0].getSuggestedReplacements().get(2));

        matches = rule.match(lt.getAnalyzedSentence("joan"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals(0, matches[0].getFromPos());
        Assertions.assertEquals(4, matches[0].getToPos());
        Assertions.assertEquals("Joan", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("abatusats"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals(0, matches[0].getFromPos());
        Assertions.assertEquals(9, matches[0].getToPos());
        Assertions.assertEquals("abatussats", matches[0].getSuggestedReplacements().get(0));
        
        // incomplete multiword
        matches = rule.match(lt.getAnalyzedSentence("L'statu"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals(2, matches[0].getFromPos());
        Assertions.assertEquals(7, matches[0].getToPos());
        Assertions.assertEquals("tato", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("argüit"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals(0, matches[0].getFromPos());
        Assertions.assertEquals(6, matches[0].getToPos());
        Assertions.assertEquals("arguït", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("argüir", matches[0].getSuggestedReplacements().get(1));
        Assertions.assertEquals("argüint", matches[0].getSuggestedReplacements().get(2));

        
        matches = rule.match(lt.getAnalyzedSentence("ángel"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("àngel", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("Àngel", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("caçessim"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("cacéssim", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("cassàssim", matches[0].getSuggestedReplacements().get(1));
        Assertions.assertEquals("casséssim", matches[0].getSuggestedReplacements().get(2));
        Assertions.assertEquals("casàssim", matches[0].getSuggestedReplacements().get(3));
        Assertions.assertEquals("caséssim", matches[0].getSuggestedReplacements().get(4));
        
        matches = rule.match(lt.getAnalyzedSentence("coche"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("cotxe", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("cuixa", matches[0].getSuggestedReplacements().get(1));
        Assertions.assertEquals("coixa", matches[0].getSuggestedReplacements().get(2));
        
        
        matches = rule.match(lt.getAnalyzedSentence("cantaríà"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("cantaria", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("cantera", matches[0].getSuggestedReplacements().get(1));
        
        //best suggestion first
        matches = rule.match(lt.getAnalyzedSentence("poguem"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("puguem", matches[0].getSuggestedReplacements().get(0));
        
        //incorrect mixed case words
        Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("PH")).length);
        Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("Ph")).length);
        Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("MCDonald")).length);
        
        matches = rule.match(lt.getAnalyzedSentence("tAula"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("taula", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("TAula"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("Taula", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("col·Labora"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("col·labora", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("col·laborÀ"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("col·labora", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("col·laborà", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("después"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("després", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("dessinstalasio"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("desinstal·làssiu", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("desinstal·lació", matches[0].getSuggestedReplacements().get(1));
       

        matches = rule.match(lt.getAnalyzedSentence("matitzàrem"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("matisarem", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("matisàrem", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("tamitzéssim"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("t'amistéssim", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("tamisàssim", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("adquireixquen"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("adquirisquen", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("adquiresquen", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("calificar"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("qualificar", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("desconte"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("d'escolta", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("descompte", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("transtors"));
        //assertEquals("trastorns", matches[0].getSuggestedReplacements().get(0)); TODO: update info file
        matches = rule.match(lt.getAnalyzedSentence("pissara"));
        //assertEquals("pissarra", matches[0].getSuggestedReplacements().get(0)); TODO: update info file
        
        
        matches = rule.match(lt.getAnalyzedSentence("atentats"));
        Assertions.assertEquals("atemptats", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("síntomes"));
        Assertions.assertEquals("símptomes", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("atentats"));
        Assertions.assertEquals("atemptats", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("contable"));
        Assertions.assertEquals("comptable", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("desició"));
        Assertions.assertEquals("d'edició", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("decisió", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("España"));
        Assertions.assertEquals("Espanya", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("concenciosament"));
        Assertions.assertEquals("conscienciosament", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("excelent"));
        Assertions.assertEquals("excel·lent", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("exceleixquen"));
        Assertions.assertEquals("excel·lisquen", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("caligrafia"));
        Assertions.assertEquals("cal·ligrafia", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("calificaren"));
        Assertions.assertEquals("qualificaren", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Excelentissim"));
        Assertions.assertEquals("Excel·lentíssim", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("IlustRISIM"));
        Assertions.assertEquals("Il·lustríssim", matches[0].getSuggestedReplacements().get(0));
        //matches = rule.match(langTool.getAnalyzedSentence("Xicago"));
        //assertEquals("Chicago", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("Chile"));
        Assertions.assertEquals("Xile", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("transment"));
        Assertions.assertEquals("transmet", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("nordment"));
        Assertions.assertEquals("normant", matches[0].getSuggestedReplacements().get(0));
        
        //matches = rule.match(langTool.getAnalyzedSentence("transtors"));
        //assertEquals("trastorns", matches[0].getSuggestedReplacements().get(0));
        
        // Needs Morfologik Speller 2.1.0
        matches = rule.match(lt.getAnalyzedSentence("milisegons"));
        Assertions.assertEquals("mil·lisegons", matches[0].getSuggestedReplacements().get(0));
        
        /*  change in Speller necessary: words of length = 4*/
        matches = rule.match(lt.getAnalyzedSentence("nula"));
        Assertions.assertEquals("nul·la", matches[0].getSuggestedReplacements().get(0));
        
        //capitalized wrong words
        matches = rule.match(lt.getAnalyzedSentence("En la Pecra"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("Para", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("Pare", matches[0].getSuggestedReplacements().get(1));

        matches = rule.match(lt.getAnalyzedSentence("IVa"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("Iva", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("IVA", matches[0].getSuggestedReplacements().get(1));

        matches = rule.match(lt.getAnalyzedSentence("Dvd"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("DVD", matches[0].getSuggestedReplacements().get(0));
        
        // deprecated characters of "ela geminada"
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("S'hi havien instaŀlat.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("S'HI HAVIEN INSTAĿLAT.")).length);

        Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("a")).length);
        
        matches = rule.match(lt.getAnalyzedSentence("Windows10"));
        Assertions.assertEquals("Windows 10", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("Windows", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("windows10"));
        Assertions.assertEquals("Windows 10", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("Windows", matches[0].getSuggestedReplacements().get(1));
        
        // pronoms febles
        matches = rule.match(lt.getAnalyzedSentence("ferse"));
        Assertions.assertEquals("fer-se", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Magradaria"));
        Assertions.assertEquals("M'agradaria", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("tenvio"));
        Assertions.assertEquals("t'envio", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("consultins"));
        Assertions.assertEquals("consulti'ns", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("portarvos"));
        Assertions.assertEquals("portar-vos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("portemne"));
        Assertions.assertEquals("portem-ne", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("dacontentar"));
        Assertions.assertEquals("d'acontentar", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("acontentar", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("devidents"));
        Assertions.assertEquals("de vidents", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("d'evidents", matches[0].getSuggestedReplacements().get(1));
        Assertions.assertEquals("evidents", matches[0].getSuggestedReplacements().get(2));
        matches = rule.match(lt.getAnalyzedSentence("lacomplexat"));
        Assertions.assertEquals("la complexat", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("l'acomplexat", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("dacomplexats"));
        Assertions.assertEquals("d'acomplexats", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("acomplexats", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("lacomplexats"));
        Assertions.assertEquals("la complexats", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("acomplexats", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("veurehi"));
        Assertions.assertEquals("veure-hi", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("veureles"));
        Assertions.assertEquals("veure-les", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("lilla"));
        Assertions.assertEquals("Lilla", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("l'illa", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("portas"));
        Assertions.assertEquals("portàs", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("portes", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("mantenir'me"));
        Assertions.assertEquals("mantenir-me", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("elcap"));
        Assertions.assertEquals("el cap", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("almeu"));
        Assertions.assertEquals("al meu", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("delteu"));
        Assertions.assertEquals("del teu", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("unshomes"));
        Assertions.assertEquals("uns homes", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("pelsseus"));
        Assertions.assertEquals("pels seus", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("daquesta"));
        Assertions.assertEquals("d'aquesta", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("daquelles"));
        Assertions.assertEquals("d'aquelles", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("lah"));
        Assertions.assertEquals("la", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("dela"));
        Assertions.assertEquals("Dela", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("de la", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("sha"));
        Assertions.assertEquals("s'ha", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("xe", matches[0].getSuggestedReplacements().get(1));
        Assertions.assertEquals("xa", matches[0].getSuggestedReplacements().get(2));
        matches = rule.match(lt.getAnalyzedSentence("Sha"));
        Assertions.assertEquals("S'ha", matches[0].getSuggestedReplacements().get(0));
        //assertEquals("Xe", matches[0].getSuggestedReplacements().get(1));
        //assertEquals("Xa", matches[0].getSuggestedReplacements().get(2));
        matches = rule.match(lt.getAnalyzedSentence("avegades"));
        Assertions.assertEquals("a vegades", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Encanvi"));
        Assertions.assertEquals("En canvi", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Nosé"));
        Assertions.assertEquals("No sé", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("air"));
        Assertions.assertEquals("Aïr", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("aïr", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("Misiones"));
        Assertions.assertEquals("Missiones", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("quedan"));
        Assertions.assertEquals("queden", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("portan"));
        Assertions.assertEquals("porten", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("porta'n", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("portans"));
        Assertions.assertEquals("porta'ns", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("porta'nshi"));
        //assertEquals("porta'ns-hi", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("porto'nz"));
        //assertEquals("porta'ns", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("portalhi"));
        Assertions.assertEquals("portal hi", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("veurels"));
        Assertions.assertEquals("veure'ls", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("veuret"));
        Assertions.assertEquals("veure", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("veure't", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("veures"));
        Assertions.assertEquals("veure's", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("beures", matches[0].getSuggestedReplacements().get(1));
        
        matches = rule.match(lt.getAnalyzedSentence("serlo"));
        Assertions.assertEquals("ser-lo", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("verlo"));
        Assertions.assertEquals("parlo", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("baró", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("relajarme"));
        Assertions.assertEquals("relaxar-me", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("aborrirnos"));
        Assertions.assertEquals("avorrir-nos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("aborirnos"));
        Assertions.assertEquals("avorrir-nos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("sescontaminarla"));
        Assertions.assertEquals("descontaminar-la", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("daconseguirlos"));
        Assertions.assertEquals("aconseguir-los", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("aconseguiràs", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("laconseguirlos"));
        Assertions.assertEquals("aconseguir-los", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("aconseguiràs", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("portarinhi"));
        Assertions.assertEquals("portaran", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("portaria", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("norueg"));
        Assertions.assertEquals("noruega", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("noruec", matches[0].getSuggestedReplacements().get(1));
        //matches = rule.match(langTool.getAnalyzedSentence("prenense"));
        //assertEquals("prenent-se", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("cual"));
        Assertions.assertEquals("qual", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("m0entretinc"));
        Assertions.assertEquals("m'entretinc", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("m9entretinc"));
        Assertions.assertEquals("m'entretinc", matches[0].getSuggestedReplacements().get(0));
        
        
        //Ela geminada 
        matches = rule.match(lt.getAnalyzedSentence("La sol•licitud"));
        Assertions.assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("La sol-licitud"));
        Assertions.assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("La sol⋅licitud"));
        Assertions.assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("La sol∙licitud"));
        Assertions.assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("un estat sindical-laborista"));
        Assertions.assertEquals(0, matches.length);
        matches = rule.match(lt.getAnalyzedSentence("en un estat sindical.La classe obrera"));
        Assertions.assertEquals(0, matches.length);
        matches = rule.match(lt.getAnalyzedSentence("al-Ladjdjun"));
        Assertions.assertEquals(3, matches[0].getFromPos());
        Assertions.assertEquals(11, matches[0].getToPos());
        // "ela geminada" error + another spelling error
        matches = rule.match(lt.getAnalyzedSentence("La sol•licitut")); 
        Assertions.assertEquals("sol·licitud", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("Il•lustran")); 
        Assertions.assertEquals("Il·lustren", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("bél.lica")); 
        Assertions.assertEquals("bèl·lica", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("mercar")); 
        Assertions.assertEquals("marcar", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("Mercer", matches[0].getSuggestedReplacements().get(1));
        
        //majúscules
        matches = rule.match(lt.getAnalyzedSentence("De PH 4")); 
        Assertions.assertEquals("pH", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("De l'any 156 Ac a l'any 2000.")); 
        Assertions.assertEquals("aC", matches[0].getSuggestedReplacements().get(0)); 
        
        //split words
        Assertions.assertEquals(2, rule.match(lt.getAnalyzedSentence("sobre el llit d'en Ron i el va colpir la certesa del que havia passat amb la força d'un troll quan envesteix")).length);
        matches = rule.match(lt.getAnalyzedSentence("unaa juda")); 
        Assertions.assertEquals("una ajuda", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("elsi nteressos")); 
        Assertions.assertEquals("els interessos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("el sinteressos")); 
        //assertEquals("els interessos", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("interessos", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("ell ustre")); 
        Assertions.assertEquals("el lustre", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("unah ora")); 
        Assertions.assertEquals("una hora", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("benv inguts")); 
        Assertions.assertEquals("ben vinguts", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("benvinguts", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("benam at")); 
        Assertions.assertEquals("bena mat", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("benamat", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("estimade s"));
        Assertions.assertEquals("estimada", matches[0].getSuggestedReplacements().get(0));
        //assertEquals("estimades", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("estimad es")); 
        Assertions.assertEquals("estimar", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("estimat", matches[0].getSuggestedReplacements().get(1));
        matches = rule.match(lt.getAnalyzedSentence("co nstel·lació")); 
        Assertions.assertEquals("constel·lació", matches[0].getSuggestedReplacements().get(0));
        
        //diacritics
        matches = rule.match(lt.getAnalyzedSentence("literaria"));
        Assertions.assertEquals("literària", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals("l'iteraria", matches[0].getSuggestedReplacements().get(1));
        
        // different speller dictionaries Cat/Val
        matches = rule.match(lt.getAnalyzedSentence("ingeniaria")); 
        Assertions.assertEquals(1, matches.length);
        
        matches = rule.match(lt.getAnalyzedSentence("l'unic")); 
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("únic", matches[0].getSuggestedReplacements().get(0));
        
        matches = rule.match(lt.getAnalyzedSentence("rà dio")); 
        Assertions.assertEquals("ràdio", matches[0].getSuggestedReplacements().get(0));
        Assertions.assertEquals(0, matches[0].getFromPos());
        Assertions.assertEquals(6, matches[0].getToPos());
        
        //don't split prefixes
        matches = rule.match(lt.getAnalyzedSentence("multiindisciplina")); 
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals(0, matches[0].getSuggestedReplacements().size());
        
        //Casing
        matches = rule.match(lt.getAnalyzedSentence("SOL.LICITUD"));
        Assertions.assertEquals("SOL·LICITUD", matches[0].getSuggestedReplacements().get(0));
        matches = rule.match(lt.getAnalyzedSentence("PROBATURA")); 
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals(1, matches[0].getSuggestedReplacements().size());
        Assertions.assertEquals("PROVATURA", matches[0].getSuggestedReplacements().get(0));
        
        //special chars
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("33° 5′ 40″ N i 32° 59′ 0″ E.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("33°5′40″N i 32°59′0″E.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Fa 5·10-³ metres.")).length);
        
        // mentions, hashtags, domain names
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Parlem del #temagran amb @algugros en algunacosa.cat.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("En el domini .org hi ha fitxers d'extensió .txt.")).length);
        Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("En el domini .live hi ha fitxers d'extensió .7z.")).length);
        
        matches = rule.match(lt.getAnalyzedSentence("En1993"));
        Assertions.assertEquals(1, matches.length);
        Assertions.assertEquals("En 1993", matches[0].getSuggestedReplacements().get(0));
        
    }
}
