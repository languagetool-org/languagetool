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
public class CaseRuleTest extends TestCase {

  public void testRuleActivation() throws IOException {
    CaseRule rule = new CaseRule(null);
    assertTrue(rule.supportsLanguage(Language.GERMAN));
    assertFalse(rule.supportsLanguage(Language.ENGLISH));
  }

  public void testRule() throws IOException {
    CaseRule rule = new CaseRule(null);
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ein einfacher Satz zum Testen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Laufen fällt mir leicht.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Winseln stört.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das schlägt nicht so zu Buche.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Dirk Hetzel ist ein Name.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Sein Verhalten war okay.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hier ein Satz. \"Ein Zitat.\"")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hier ein Satz. 'Ein Zitat.'")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hier ein Satz. «Ein Zitat.»")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hier ein Satz. »Ein Zitat.«")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hier ein Satz. (Noch einer.)")).length);
    // works only thanks to addex.txt:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Nachfahre.")).length);
    // both can be correct:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hier ein Satz, \"Ein Zitat.\"")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hier ein Satz, \"ein Zitat.\"")).length);
    // Exception 'Le':
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Schon Le Monde schrieb das.")).length);
    // unknown word:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("In Blubberdorf macht man das so.")).length);

    // sentences that used to trigger an error because of incorrect compound tokenization:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das sind Euroscheine.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("John Stallman isst.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist die neue Gesellschafterin hier.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist die neue Dienerin hier.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist die neue Geigerin hier.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die ersten Gespanne erreichen Köln.")).length);
    
    // used to trigger error because of wrong POS tagging:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Schlinge zieht sich zu.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Schlingen ziehen sich zu.")).length);
    
    // TODO: nach dem Doppelpunkt wird derzeit nicht auf groß/klein getestet:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist es: kein Satz.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist es: Kein Satz.")).length);

    // incorrect sentences:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Ein Einfacher Satz zum Testen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Winseln Stört.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Sein verhalten war okay.")).length);
  }

  public void testSubstantivierteVerben() throws IOException {
    CaseRule rule = new CaseRule(null);
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

  public void testPhraseExceptions() throws IOException {
    CaseRule rule = new CaseRule(null);
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das gilt ohne Wenn und Aber.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("ohne Wenn und Aber")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das gilt ohne Wenn und Aber bla blubb.")).length);
    // as long as phrase exception isn't complete, there's no error:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das gilt ohne wenn")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das gilt ohne wenn und")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("wenn und aber")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("und aber")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("aber")).length);
    // incorrect sentences:
    // error not found here as it's in the XML rules:
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das gilt ohne wenn und aber.")).length);
  }
  
}
