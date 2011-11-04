/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules.de;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Daniel Naber
 */
public class AgreementRuleTest extends TestCase {

  private AgreementRule rule;
  private JLanguageTool langTool;
  
  @Override
  public void setUp() throws IOException {
    rule = new AgreementRule(null);
    langTool = new JLanguageTool(Language.GERMAN);
  }
  
  public void testDetNounRule() throws IOException {

    /* debugging:
    RuleMatch[] rm = rule.match(langTool.getAnalyzedSentence("Wer für die Kosten"));
    System.err.println(rm[0]);
    if (true)
      return;
    */

    // correct sentences:
    assertGood("So ist es in den USA.");
    assertGood("Das ist der Tisch.");
    assertGood("Das ist das Haus.");
    assertGood("Das ist die Frau.");
    assertGood("Das ist das Auto der Frau.");
    assertGood("Das gehört dem Mann.");
    assertGood("Das Auto des Mannes.");
    assertGood("Das interessiert den Mann.");
    assertGood("Das interessiert die Männer.");
    assertGood("Das Auto von einem Mann.");
    assertGood("Das Auto eines Mannes.");
    assertGood("Des großen Mannes.");
    
    assertGood("Das Dach von meinem Auto.");
    assertGood("Das Dach von meinen Autos.");

    assertGood("Das Dach meines Autos.");
    assertGood("Das Dach meiner Autos.");

    assertGood("Das Dach meines großen Autos.");
    assertGood("Das Dach meiner großen Autos.");

    assertGood("Das Wahlrecht, das Frauen damals zugesprochen bekamen.");
    assertGood("Es war Karl, dessen Leiche Donnerstag gefunden wurde.");

    assertGood("Erst recht ich Arbeiter.");
    assertGood("Erst recht wir Arbeiter.");
    assertGood("Erst recht wir fleißigen Arbeiter.");

    assertGood("Dann lud er Freunde ein.");
    assertGood("Dann lud sie Freunde ein.");
    assertGood("Aller Kommunikation liegt dies zugrunde.");
    assertGood("Pragmatisch wählt man solche Formeln als Axiome.");
    assertGood("Der eine Polizist rief dem anderen zu...");
    assertGood("Das eine Kind rief dem anderen zu...");
    assertGood("Er wollte seine Interessen wahrnehmen.");

    assertGood("... wo Krieg den Unschuldigen Leid und Tod bringt.");
    assertGood("Der Abschuss eines Papageien.");

    // relative clauses:
    assertGood("Das Recht, das Frauen eingeräumt wird.");
    assertGood("Der Mann, in dem quadratische Fische schwammen.");
    assertGood("Gutenberg, der quadratische Mann.");
    // TODO: not detected, because "die" is considered a relative pronoun:
    //assertBad("Gutenberg, die Genie.");
    
    // some of these used to cause false alarms:
    assertGood("Das Münchener Fest.");
    assertGood("Das Münchner Fest.");
    assertGood("Die Planung des Münchener Festes.");
    assertGood("Das Berliner Wetter.");
    assertGood("Den Berliner Arbeitern ist das egal.");
    assertGood("Das Haus des Berliner Arbeiters.");
    assertGood("Es gehört dem Berliner Arbeiter.");
    assertGood("Das Stuttgarter Auto.");
    assertGood("Das Bielefelder Radio.");
    assertGood("Das Gütersloher Radio.");
    
    // incorrect sentences:
    assertBad("Es sind die Tisch.");
    assertBad("Es sind das Tisch.");
    assertBad("Es sind die Haus.");
    assertBad("Es sind der Haus.");
    assertBad("Es sind das Frau.");
    assertBad("Das Auto des Mann.");
    assertBad("Das interessiert das Mann.");
    assertBad("Das interessiert die Mann.");
    assertBad("Das Auto ein Mannes.");
    assertBad("Das Auto einem Mannes.");
    assertBad("Das Auto einer Mannes.");
    assertBad("Das Auto einen Mannes.");
    
    assertBad("Des großer Mannes.");

    assertBad("Das Dach von meine Auto.");
    assertBad("Das Dach von meinen Auto.");
    
    assertBad("Das Dach mein Autos.");
    assertBad("Das Dach meinem Autos.");

    assertBad("Das Dach meinem großen Autos.");
    assertBad("Das Dach mein großen Autos.");

    assertBad("Erst recht wir fleißiges Arbeiter.");

    // TODO: not yet detected:
    //assertBad("Erst recht ich fleißiges Arbeiter.");
    //assertBad("Das Dach meine großen Autos.");
    //assertBad("Das Dach meinen großen Autos.");
    //assertBad("Das Dach meine Autos.");
    //assertBad("Es ist das Haus dem Mann.");
    //assertBad("Das interessiert der Männer.");
    //assertBad("Das interessiert der Mann.");
    //assertBad("Das gehört den Mann.");
    //assertBad("Es sind der Frau.");
  }

  public void testDetNounRuleErrorMessages() throws IOException {
    // check detailed error messages:
    assertBad("Das Fahrrads.", "bezüglich Kasus");
    assertBad("Der Fahrrad.", "bezüglich Genus");
    assertBad("Das Fahrräder.", "bezüglich Numerus");
    assertBad("Die Tischen sind ecking.", "bezüglich Kasus");
    assertBad("Die Tischen sind ecking.", "und Genus");
    //TODO: input is actually correct
    assertBad("Bei dem Papierabzüge von Digitalbildern bestellte werden.", "bezüglich Kasus, Genus oder Numerus.");
  }
  
  public void testRegression() throws IOException {
      JLanguageTool gramCheckerEngine = new JLanguageTool(Language.GERMAN);
      gramCheckerEngine.activateDefaultPatternRules();
      // used to be not detected > 1.0.1:
      String str = "Und so.\r\nDie Bier.";
      List<RuleMatch> matches = gramCheckerEngine.check(str);
      assertEquals(1, matches.size());
  }
  
  public void testDetAdjNounRule() throws IOException {
    // correct sentences:
    assertGood("Das ist der riesige Tisch.");
    assertGood("Der riesige Tisch ist groß.");
    assertGood("Die Kanten der der riesigen Tische.");
    assertGood("Den riesigen Tisch mag er.");
    assertGood("Es mag den riesigen Tisch.");
    assertGood("Die Kante des riesigen Tisches.");
    assertGood("Dem riesigen Tisch fehlt was.");
    assertGood("Die riesigen Tische sind groß.");
    assertGood("Der riesigen Tische wegen.");
    // TODO: incorrectly detected as incorrect:
    // Dann hat das natürlich Nachteile.
    
    // incorrect sentences:
    assertBad("Es sind die riesigen Tisch.");
    //assertBad("Dort, die riesigen Tischs!");    // TODO: error not detected because of comma
    assertBad("Als die riesigen Tischs kamen.");
    assertBad("Als die riesigen Tisches kamen.");
    // TODO: not yet detected:
    //assertBad("Der riesigen Tisch und so.");
  }

  private void assertGood(String s) throws IOException {
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(s)).length);
  }

  private void assertBad(String s) throws IOException {
    assertEquals(1, rule.match(langTool.getAnalyzedSentence(s)).length);
  }

  private void assertBad(String s, String expectedErrorSubstring) throws IOException {
    assertEquals(1, rule.match(langTool.getAnalyzedSentence(s)).length);
    final String errorMessage = rule.match(langTool.getAnalyzedSentence(s))[0].getMessage();
    assertTrue("Got error '" + errorMessage + "', expected substring '" + expectedErrorSubstring + "'",
            errorMessage.contains(expectedErrorSubstring));
  }
  
}
