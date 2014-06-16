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
package org.languagetool.rules.de;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Daniel Naber
 */
public class AgreementRuleTest extends TestCase {

  private AgreementRule rule;
  private JLanguageTool langTool;
  
  @Override
  public void setUp() throws IOException {
    rule = new AgreementRule(null);
    langTool = new JLanguageTool(new German());
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
    assertGood("Und nach der Nummerierung kommt die Überschrift.");
    assertGood("Sie wiesen dieselben Verzierungen auf.");
    assertGood("Die erwähnte Konferenz ist am Samstag.");
    assertGood("Sie erreichten 5 Prozent.");
    assertGood("Sie erreichten mehrere Prozent Zustimmung.");

    assertGood("Das Dach von meinem Auto.");
    assertGood("Das Dach von meinen Autos.");

    assertGood("Das Dach meines Autos.");
    assertGood("Das Dach meiner Autos.");

    assertGood("Das Dach meines großen Autos.");
    assertGood("Das Dach meiner großen Autos.");

    assertGood("Dann schlug er so kräftig wie er konnte mit den Schwingen.");
    assertGood("Also wenn wir Glück haben, ...");
    assertGood("Wenn wir Pech haben, ...");
    assertGood("Ledorn öffnete eines der an ihr vorhandenen Fächer.");
    assertGood("Auf der einen Seite endlose Dünen");
    assertGood("In seinem Maul hielt er einen blutigen Fleischklumpen.");
    assertGood("Gleichzeitig dachte er intensiv an Nebelschwaden, aus denen Wolken ja bestanden.");
    assertGood("Warum stellte der bloß immer wieder dieselben Fragen?");
    assertGood("Bei der Hinreise.");
    assertGood("Schließlich tauchten in einem Waldstück unter ihnen Schienen auf.");

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
    
    assertGood("Die Beibehaltung des Art. 1 ist geplant.");
    assertGood("Die Verschiebung des bisherigen Art. 1 ist geplant.");

    // relative clauses:
    assertGood("Das Recht, das Frauen eingeräumt wird.");
    assertGood("Der Mann, in dem quadratische Fische schwammen.");
    assertGood("Der Mann, durch den quadratische Fische schwammen.");
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
    assertGood("Das wirklich Wichtige kommt jetzt erst.");
    assertGood("Besonders wenn wir Wermut oder Absinth trinken.");
    assertGood("Ich wünsche dir alles Gute.");
    assertGood("Es ist nicht bekannt, mit welchem Alter Kinder diese Fähigkeit erlernen.");
    assertGood("Dieser ist nun in den Ortungsbereich des einen Roboters gefahren.");
    assertGood("Wenn dies großen Erfolg hat, werden wir es weiter fördern.");
    assertGood("Die Ereignisse dieses einen Jahres waren sehr schlimm.");
    assertGood("Er musste einen Hochwasser führenden Fluss nach dem anderen überqueren.");

    // incorrect sentences:
    assertBad("Es sind die Tisch.", "dem Tisch", "den Tisch", "der Tisch", "die Tische");
    assertBad("Es sind das Tisch.", "dem Tisch", "den Tisch", "der Tisch");
    assertBad("Es sind die Haus.", "das Haus", "dem Haus", "die Häuser");
    assertBad("Es sind der Haus.", "das Haus", "dem Haus", "der Häuser");
    assertBad("Es sind das Frau.", "der Frau", "die Frau");
    assertBad("Das Auto des Mann.", "dem Mann", "den Mann", "der Mann", "des Mannes", "des Manns");
    assertBad("Das interessiert das Mann.", "dem Mann", "den Mann", "der Mann");
    assertBad("Das interessiert die Mann.", "dem Mann", "den Mann", "der Mann", "die Männer");
    assertBad("Das Auto ein Mannes.", "ein Mann", "eines Mannes");
    assertBad("Das Auto einem Mannes.", "einem Mann", "einem Manne", "eines Mannes");
    assertBad("Das Auto einer Mannes.", "eines Mannes");
    assertBad("Das Auto einen Mannes.", "einen Mann", "eines Mannes");
    
    assertBad("Die erwähnt Konferenz ist am Samstag.");
    assertBad("Die erwähntes Konferenz ist am Samstag.");
    assertBad("Die erwähnten Konferenz ist am Samstag.");
    assertBad("Die erwähnter Konferenz ist am Samstag.");
    
    assertBad("Des großer Mannes.");

    assertBad("Das Dach von meine Auto.", "mein Auto", "meine Autos", "meinem Auto");
    assertBad("Das Dach von meinen Auto.", "mein Auto", "meinem Auto", "meinen Autos");
    
    assertBad("Das Dach mein Autos.", "mein Auto", "meine Autos", "meinen Autos", "meiner Autos", "meines Autos");
    assertBad("Das Dach meinem Autos.", "meine Autos", "meinem Auto", "meinen Autos", "meiner Autos", "meines Autos");

    assertBad("Das Dach meinem großen Autos.");
    assertBad("Das Dach mein großen Autos.");

    assertBad("Das Klientel der Partei.", "Der Klientel", "Die Klientel");  // gender used to be wrong in Morphy data
    assertGood("Die Klientel der Partei.");

    assertBad("Der Haus ist groß", "Das Haus", "Dem Haus", "Der Häuser");
    assertBad("Aber der Haus ist groß", "das Haus", "dem Haus", "der Häuser");
    
    assertBad("Ich habe einen Feder gefunden.", "eine Feder", "einer Feder");

    // TODO: not yet detected:
    //assertBad("Erst recht wir fleißiges Arbeiter.");
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

  public void testVieleWenige() throws IOException {
    assertGood("Zusammenschluss mehrerer dörflicher Siedlungen an einer Furt");
    assertGood("Für einige markante Szenen");
    assertGood("Für einige markante Szenen baute Hitchcock ein Schloss.");
    assertGood("Haben Sie viele glückliche Erfahrungen in Ihrer Kindheit gemacht?");
    assertGood("Es gibt viele gute Sachen auf der Welt.");
    assertGood("Viele englische Wörter haben lateinischen Ursprung");
    assertGood("Ein Bericht über Fruchtsaft, einige ähnliche Erzeugnisse und Fruchtnektar");
    assertGood("Der Typ, der seit einiger Zeit immer wieder hierher kommt.");
    assertGood("Jede Schnittmenge abzählbar vieler offener Mengen");
    assertGood("Es kam zur Fusion der genannten und noch einiger weiterer Unternehmen.");
    assertGood("Zu dieser Fragestellung gibt es viele unterschiedliche Meinungen.");
  }
  
  public void testDetNounRuleErrorMessages() throws IOException {
    // check detailed error messages:
    assertBadWithMessage("Das Fahrrads.", "bezüglich Kasus");
    assertBadWithMessage("Der Fahrrad.", "bezüglich Genus");
    assertBadWithMessage("Das Fahrräder.", "bezüglich Numerus");
    assertBadWithMessage("Die Tischen sind ecking.", "bezüglich Kasus");
    assertBadWithMessage("Die Tischen sind ecking.", "und Genus");
    //TODO: input is actually correct
    assertBadWithMessage("Bei dem Papierabzüge von Digitalbildern bestellte werden.", "bezüglich Kasus, Genus oder Numerus.");
  }
  
  public void testRegression() throws IOException {
      JLanguageTool gramCheckerEngine = new JLanguageTool(new German());
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
    assertGood("An der roten Ampel.");
    assertGood("Dann hat das natürlich Nachteile.");
    
    // incorrect sentences:
    assertBad("Es sind die riesigen Tisch.");
    //assertBad("Dort, die riesigen Tischs!");    // TODO: error not detected because of comma
    assertBad("Als die riesigen Tischs kamen.");
    assertBad("Als die riesigen Tisches kamen.");
    assertBad("Der riesigen Tisch und so.");
    assertBad("An der roter Ampel.");
    assertBad("An der rote Ampel.");
    assertBad("An der rotes Ampel.");
    assertBad("An der rotem Ampel.");
    // TODO: not yet detected:
    //assertBad("An der rot Ampel.");
  }

  private void assertGood(String s) throws IOException {
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(s));
    assertEquals("Found unexpected match in sentence '" + s + "': " + Arrays.toString(matches), 0, matches.length);
  }

  private void assertBad(String s, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(s));
    assertEquals("Did not find one match in sentence '" + s + "'", 1, matches.length);
    if (expectedSuggestions.length > 0) {
      RuleMatch match = matches[0];
      List<String> suggestions = match.getSuggestedReplacements();
      assertThat(suggestions, is(Arrays.asList(expectedSuggestions)));
    }
  }

  private void assertBadWithMessage(String s, String expectedErrorSubstring) throws IOException {
    assertEquals(1, rule.match(langTool.getAnalyzedSentence(s)).length);
    final String errorMessage = rule.match(langTool.getAnalyzedSentence(s))[0].getMessage();
    assertTrue("Got error '" + errorMessage + "', expected substring '" + expectedErrorSubstring + "'",
            errorMessage.contains(expectedErrorSubstring));
  }
  
}
