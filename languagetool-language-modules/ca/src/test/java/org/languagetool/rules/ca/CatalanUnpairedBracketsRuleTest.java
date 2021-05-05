/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
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
import org.languagetool.rules.TextLevelRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CatalanUnpairedBracketsRuleTest {

  private TextLevelRule rule;
  private JLanguageTool lt;
  
  @Before
  public void setUp() throws IOException {
    rule = new CatalanUnpairedBracketsRule(TestTools.getEnglishMessages(), new Catalan());
    lt = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {
    
    // correct sentences:
    assertMatches("L'«home és així»", 0);
    assertMatches("l'«home»", 0);
    assertMatches("«\"És així\" o no»", 0);
    assertMatches("«\"És així\", va dir.»", 0);
    assertMatches("«És \"així\" o no»", 0);
    assertMatches("(l'execució a mans d'\"especialistes\")", 0);
    assertMatches("(L'\"especialista\")", 0);
    assertMatches("\"Vine\", li va dir.", 0);
    assertMatches("(Una frase de prova).", 0);
    assertMatches("Aquesta és la paraula 'prova'.", 0);
    assertMatches("This is a sentence with a smiley :-)", 0);
    assertMatches("This is a sentence with a smiley ;-) and so on...", 0);
    assertMatches("Aquesta és l'hora de les decisions.", 0);
    assertMatches("Aquesta és l’hora de les decisions.", 0);
    assertMatches("(fig. 20)", 0);
    assertMatches("\"Sóc la teva filla. El corcó no et rosegarà més.\"\n\n", 0);
    assertMatches("–\"Club dels llagoters\" –va repetir en Ron.", 0);
    assertMatches("—\"Club dels llagoters\" –va repetir en Ron.", 0);
    assertMatches("»Això em porta a demanar-t'ho.", 0);
    assertMatches("»Això em porta (sí) a demanar-t'ho.", 0);
    assertMatches("al capítol 12 \"Llavors i fruits oleaginosos\"", 0);
    assertMatches("\"Per què serveixen les forquilles?\" i aquest respon \"per menjar\".", 0);
    assertMatches("És a 60º 50' 23\"", 0);
    assertMatches("És a 60º 50' 23'", 0);
    assertMatches("60° 50' 23'", 0);
    assertMatches("60° 50'", 0);
    //assertMatches("el grau en 60 parts iguals, tenim el minut (1'):", 0);
    //assertMatches("el minut en 60 parts iguals, tenim el segon (1\"):", 0);
    assertMatches("El tràiler té una picada d'ullet quan diu que \"no es pot fer una pel·lícula 'slasher' com si fos una sèrie\".", 0);
    assertMatches("El tràiler –que té una picada d'ullet quan diu que \"no es pot fer una pel·lícula 'slasher' com si fos una sèrie\"– ja ", 0);
    
    //assertMatches("The screen is 20\" wide.", 0);
    assertMatches("This is a [test] sentence...", 0);
    assertMatches("The plight of Tamil refugees caused a surge of support from most of the Tamil political parties.[90]", 0);
    assertMatches("This is what he said: \"We believe in freedom. This is what we do.\"", 0);
    assertMatches("(([20] [20] [20]))", 0);
    // test for a case that created a false alarm after disambiguation
    assertMatches("This is a \"special test\", right?", 0);
    // numerical bullets
    assertMatches("We discussed this in Chapter 1).", 0);
    assertMatches("The jury recommended that: (1) Four additional deputies be employed.", 0);
    assertMatches("We discussed this in section 1a).", 0);
    assertMatches("We discussed this in section iv).", 0);
    //inches exception shouldn't match " here:
    assertMatches("In addition, the government would pay a $1,000 \"cost of education\" grant to the schools.", 0);
    //assertMatches("Paradise lost to the alleged water needs of Texas' big cities Thursday.", 0);
    assertMatches ("Porta'l cap ací.", 0);
    assertMatches ("Porta-me'n cinquanta!", 0);
    // Saxon genitive
    assertMatches("Harper's Dictionary of Classical Antiquities", 0);
    assertMatches("Harper’s Dictionary of Classical Antiquities", 0);

    // incorrect sentences:
    assertMatches("(aquesta 'és la solució)", 1);
    assertMatches("(L'\"especialista\"", 0);
    assertMatches("(L'\"especialista\".", 1);
    assertMatches("L'«home és així", 0);
    assertMatches("L'«home és així.", 1);
    assertMatches("S'«esperava 'el' (segon) \"resultat\"", 0);
    assertMatches("S'«esperava 'el' (segon) \"resultat\".", 1);
    assertMatches("l'«home", 0);
    assertMatches("l'«home.", 1);
    assertMatches("Ploraria.\"", 1);
    assertMatches("Aquesta és l555’hora de les decisions.", 1);
    assertMatches("Vine\", li va dir.", 1);
    assertMatches("Aquesta és l‘hora de les decisions.", 1);
    assertMatches("(This is a test sentence.", 1);
    assertMatches("This is a test with an apostrophe &'.", 1);
    assertMatches("&'", 0);
    assertMatches("&'.", 1);
    assertMatches("!'", 0);
    assertMatches("!'.", 1);
    assertMatches("What?'", 0);
    assertMatches("What?'.", 1);

    // this is currently considered incorrect... although people often use smileys this way:
    assertMatches("Some text (and some funny remark :-) with more text to follow", 0);
    assertMatches("Some text (and some funny remark :-) with more text to follow?", 1);

    assertMatches("(This is a test” sentence.", 2);
    assertMatches("This [is (a test} sentence.", 3);
  }
  
  private void assertMatches(String input, int expectedMatches) throws IOException {
    final RuleMatch[] matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence(input)));
    assertEquals(expectedMatches, matches.length);
  }

  @Test
  public void testMultipleSentences() throws IOException {
    final JLanguageTool tool = new JLanguageTool(new Catalan());
    tool.enableRule("CA_UNPAIRED_BRACKETS");

    List<RuleMatch> matches;
    matches = tool
        .check("Aquesta és una sentència múltiple amb claudàtors: "
            + "[Ací hi ha un claudàtor. Amb algun text.] i ací continua.\n");
    assertEquals(0, matches.size());
    matches = tool
        .check("\"Era la teva filla. El corcó no et rosegarà més.\"\n\n");
    assertEquals(0, matches.size());
    matches = tool
        .check("\"Era la teva filla. El corcó no et rosegarà més\".\n\n");
    assertEquals(0, matches.size());
    matches = tool
        .check("Aquesta és una sentència múltiple amb claudàtors: "
            + "[Ací hi ha un claudàtor. Amb algun text. I ací continua.\n\n");
    assertEquals(1, matches.size());
    
    matches = tool
        .check("«Els manaments diuen: \"No desitjaràs la dona del teu veí\"»");
    //assertEquals(0, matches.size());
            
    matches = tool
        .check("Aquesta és una sentència múltiple amb parèntesis "
            + "(Ací hi ha un parèntesi. \n\n Amb algun text.) i ací continua.");
    assertEquals(0, matches.size());
  }

}
