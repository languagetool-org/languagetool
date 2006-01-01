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

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

/**
 * @author Daniel Naber
 */
public class AgreementRuleTest extends TestCase {

  public void testDetNounRule() throws IOException {
    AgreementRule rule = new AgreementRule();
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);

    /* debugging:
    RuleMatch[] rm = rule.match(langTool.getAnalyzedSentence("Wer für die Kosten"));
    System.err.println(rm[0]);
    if (true)
      return;
    */

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("So ist es in den USA.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist der Tisch.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist das Haus.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist die Frau.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist das Auto der Frau.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das gehört dem Mann.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Auto des Mannes.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das interessiert den Mann.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das interessiert die Männer.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Auto von einem Mann.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Auto eines Mannes.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Des großen Mannes.")).length);
    
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Dach von meinem Auto.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Dach von meinen Autos.")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Dach meines Autos.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Dach meiner Autos.")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Dach meines großen Autos.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Dach meiner großen Autos.")).length);

    //assertEquals(0, rule.match(langTool.getAnalyzedSentence("... wo Krieg den Unschuldigen Leid und Tod bringt.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Abschuss eines Papageien.")).length);
    // TODO:
    //assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Recht, das Frauen eingeräumt wird.")).length);

    // incorrect sentences:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Es sind die Tisch.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Es sind das Tisch.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Es sind die Haus.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Es sind der Haus.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Es sind das Frau.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Auto des Mann.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das interessiert das Mann.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das interessiert die Mann.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Auto ein Mannes.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Auto einem Mannes.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Auto einer Mannes.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Auto einen Mannes.")).length);
    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Des großer Mannes.")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Dach von meine Auto.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Dach von meinen Auto.")).length);
    
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Dach mein Autos.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Dach meinem Autos.")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Dach meinem großen Autos.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Dach mein großen Autos.")).length);

    // TODO: not yet detected:
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Dach meine großen Autos.")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Dach meinen großen Autos.")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Dach meine Autos.")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Es ist das Haus dem Mann.")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das interessiert der Männer.")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das interessiert der Mann.")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das gehört den Mann.")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Es sind der Frau.")).length);
  }
  
  public void testDetAdjNounRule() throws IOException {
    AgreementRule rule = new AgreementRule();
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist der riesige Tisch.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der riesige Tisch ist groß.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Kanten der der riesigen Tische.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Den riesigen Tisch mag er.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Es mag den riesigen Tisch.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Kante des riesigen Tisches.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Dem riesigen Tisch fehlt was.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die riesigen Tische sind groß.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der riesigen Tische wegen.")).length);
    // TODO: incorrectly detected as incorrect:
    // Dann hat das natürlich Nachteile.
    
    // incorrect sentences:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Es sind die riesigen Tisch.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Dort, die riesigen Tischs!")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Als die riesigen Tischs kamen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Als die riesigen Tisches kamen.")).length);
    // TODO: not yet detected:
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der riesigen Tisch und so.")).length);
  }
  
}
