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
  private JLanguageTool langTool;
  
  @Before
  public void setUp() throws IOException {
    rule = new CatalanUnpairedBracketsRule(TestTools.getEnglishMessages(), new Catalan());
    langTool = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException {
    // correct sentences:
    assertCorrect("L'«home és així»");
    assertCorrect("l'«home»");
    assertCorrect("«\"És així\" o no»");
    assertCorrect("«\"És així\", va dir.»");
    assertCorrect("«És \"així\" o no»");
    assertCorrect("(l'execució a mans d'\"especialistes\")");
    assertCorrect("(L'\"especialista\")");
    assertCorrect("\"Vine\", li va dir.");
    assertCorrect("(Una frase de prova).");
    assertCorrect("Aquesta és la paraula 'prova'.");
    assertCorrect("This is a sentence with a smiley :-)");
    assertCorrect("This is a sentence with a smiley ;-) and so on...");
    assertCorrect("Aquesta és l'hora de les decisions.");
    assertCorrect("Aquesta és l’hora de les decisions.");
    assertCorrect("(fig. 20)");
    assertCorrect("\"Sóc la teva filla. El corcó no et rosegarà més.\"\n\n");
    assertCorrect("–\"Club dels llagoters\" –va repetir en Ron.");
    assertCorrect("—\"Club dels llagoters\" –va repetir en Ron.");
    assertCorrect("»Això em porta a demanar-t'ho.");
    assertCorrect("»Això em porta (sí) a demanar-t'ho.");
    assertCorrect("al capítol 12 \"Llavors i fruits oleaginosos\"");
    assertCorrect("\"Per què serveixen les forquilles?\" i aquest respon \"per menjar\".");
    assertCorrect("És a 60º 50' 23\"");
    assertCorrect("És a 60º 50' 23'");
    assertCorrect("60° 50' 23'");
    assertCorrect("60° 50'");
    //assertCorrect("el grau en 60 parts iguals, tenim el minut (1'):");
    //assertCorrect("el minut en 60 parts iguals, tenim el segon (1\"):");
    assertCorrect("El tràiler té una picada d'ullet quan diu que \"no es pot fer una pel·lícula 'slasher' com si fos una sèrie\".");
    assertCorrect("El tràiler –que té una picada d'ullet quan diu que \"no es pot fer una pel·lícula 'slasher' com si fos una sèrie\"– ja ");
    
    //assertCorrect("The screen is 20\" wide.");
    assertCorrect("This is a [test] sentence...");
    assertCorrect("The plight of Tamil refugees caused a surge of support from most of the Tamil political parties.[90]");
    assertCorrect("This is what he said: \"We believe in freedom. This is what we do.\"");
    assertCorrect("(([20] [20] [20]))");
    // test for a case that created a false alarm after disambiguation
    assertCorrect("This is a \"special test\", right?");
    // numerical bullets
    assertCorrect("We discussed this in Chapter 1).");
    assertCorrect("The jury recommended that: (1) Four additional deputies be employed.");
    assertCorrect("We discussed this in section 1a).");
    assertCorrect("We discussed this in section iv).");
    //inches exception shouldn't match " here:
    assertCorrect("In addition, the government would pay a $1,000 \"cost of education\" grant to the schools.");
    //assertCorrect("Paradise lost to the alleged water needs of Texas' big cities Thursday.");
    assertCorrect ("Porta'l cap ací.");
    assertCorrect ("Porta-me'n cinquanta!");

    // incorrect sentences:
    assertIncorrect("(L'\"especialista\"");
    assertIncorrect("L'«home és així");
    assertIncorrect("S'«esperava 'el' (segon) \"resultat\"");
    assertIncorrect("l'«home");
    assertIncorrect("Ploraria.\"");
    assertIncorrect("Aquesta és l555’hora de les decisions.");
    assertIncorrect("Vine\", li va dir.");
    assertIncorrect("Aquesta és l‘hora de les decisions.");
    assertIncorrect("(This is a test sentence.");
    assertIncorrect("This is a test with an apostrophe &'.");
    assertIncorrect("&'");
    assertIncorrect("!'");
    assertIncorrect("What?'");

    // this is currently considered incorrect... although people often use smileys this way:
    assertIncorrect("Some text (and some funny remark :-) with more text to follow");

    RuleMatch[] matches;
    matches = rule.match(Collections.singletonList(langTool.getAnalyzedSentence("(This is a test” sentence.")));
    assertEquals(2, matches.length);
    matches = rule.match(Collections.singletonList(langTool.getAnalyzedSentence("This [is (a test} sentence.")));
    assertEquals(3, matches.length);
  }

  private void assertCorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(Collections.singletonList(langTool.getAnalyzedSentence(sentence)));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(Collections.singletonList(langTool.getAnalyzedSentence(sentence)));
    assertEquals(1, matches.length);
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
        .check("\"Sóc la teva filla. El corcó no et rosegarà més.\"\n\n");
    assertEquals(0, matches.size());
    matches = tool
        .check("\"Sóc la teva filla. El corcó no et rosegarà més\".\n\n");
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
