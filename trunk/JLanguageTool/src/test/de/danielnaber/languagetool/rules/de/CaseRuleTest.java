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

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

/**
 * @author Daniel Naber
 */
public class CaseRuleTest extends TestCase {

  public void testRule() throws IOException {
    CaseRule rule = new CaseRule();
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ein einfacher Satz zum Testen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Laufen fällt mir leicht.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Winseln stört.")).length);
    // TODO: "Le" is no error:
    //assertEquals(0, rule.match(langTool.getAnalyzedSentence("Schon Le Monde schrieb das.")).length);
    
    // TODO: nach dem Doppelpunkt wird derzeit nicht auf groß/klein getestet:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist es: kein Satz.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist es: Kein Satz.")).length);

    // incorrect sentences:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Ein Einfacher Satz zum Testen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Winseln Stört.")).length);
  }

  public void testSubstantivierteVerben() throws IOException {
    CaseRule rule = new CaseRule();
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das fahrende Auto.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Fahren ist einfach.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Denn das Fahren ist einfach.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das können wir so machen.")).length);
    // incorrect sentences:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das fahren ist einfach.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Denn das fahren ist einfach.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Denn das laufen ist einfach.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Denn das essen ist einfach.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Denn das gehen ist einfach.")).length);
  }
  
}
