/* JLanguageTool, a natural language style checker 
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

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import junit.framework.TestCase;

/**
 * @author Daniel Naber
 */
public class AgreementRuleTest extends TestCase {

  public void testRule() throws IOException {
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

    // incorrect sentences:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Es sind die riesigen Tisch.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Dort, die riesigen Tischs!")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Als die riesigen Tischs kamen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Als die riesigen Tisches kamen.")).length);
//FIXME?:
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der riesigen Tisch und so.")).length);
  }
  
}
