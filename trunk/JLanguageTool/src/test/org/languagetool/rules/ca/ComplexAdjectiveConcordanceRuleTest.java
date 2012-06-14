/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà
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

import java.io.IOException;

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;

/**
 * @author Jaume Ortolà
 */
public class ComplexAdjectiveConcordanceRuleTest extends TestCase {

  private ComplexAdjectiveConcordanceRule rule;
  private JLanguageTool langTool;

  @Override
  public void setUp() throws IOException {
    rule = new ComplexAdjectiveConcordanceRule(null);
    langTool = new JLanguageTool(Language.CATALAN);
  }

  public void testRule() throws IOException {
	  
    // correct sentences:
    assertCorrect("Les perspectives de futur immediat.");
    assertCorrect("Les perspectives de futur immediates.");
    assertCorrect("la tècnica i l'art cinematogràfiques.");
    assertCorrect("la tècnica i l'art cinematogràfic.");
    assertCorrect("la tècnica i l'art cinematogràfics.");
    assertCorrect("la tècnica i l'art cinematogràfica.");
    assertCorrect("Les perspectives i el futur immediats.");
    assertCorrect("Un punt de densitat i gravetat infinites.");
    assertCorrect("De la literatura i la cultura catalanes.");
    assertCorrect("Es fa segons regles de lectura constants i regulars.");
    assertCorrect("Les meitats dreta i esquerra de la mandíbula.");
    assertCorrect("Els períodes clàssic i medieval.");
    assertCorrect("Els costats superior i laterals.");
    assertCorrect("En una molècula de glucosa i una de fructosa unides.");
    // Sholud be Incorrect, but it is impossible to detect
    assertCorrect("Índex de desenvolupament humà i qualitat de vida elevat"); 
    assertCorrect("Índex de desenvolupament humà i qualitat de vida elevats");
    assertCorrect("Índex de desenvolupament humà i qualitat de vida elevada");
    assertCorrect("La massa, el radi i la lluminositat llistats per ell.");
    assertCorrect("La massa, el radi i la lluminositat llistada per ell.");

    // errors:
    assertIncorrect("Les perspectives de futur immediata.");
    assertIncorrect("Les perspectives de futur immediats.");
    assertIncorrect("De la llengua i de la cultura catalans.");
    assertIncorrect("En una molècula de glucosa i una de fructosa units.");
    assertIncorrect("Un punt de densitat i gravetat infinits.");
    assertIncorrect("Índex de desenvolupament humà i qualitat de vida elevades.");
    // Sholud be Incorrect, but it is impossible to detect
    // assertIncorrect("Índex de desenvolupament humà i qualitat de vida elevat");
    assertIncorrect("La massa, el radi i la lluminositat llistat per ell.");
    assertIncorrect("La massa, el radi i la lluminositat llistades per ell.");

 /*   final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Les circumstancies que ens envolten són circumstancies extraordinàries."));
    assertEquals(2, matches.length);*/
  }
  
  private void assertCorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
  }

}
