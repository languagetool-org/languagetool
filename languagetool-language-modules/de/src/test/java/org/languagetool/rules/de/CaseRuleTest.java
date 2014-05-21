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

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.German;

/**
 * @author Daniel Naber
 */
public class CaseRuleTest extends TestCase {

  public void testRuleActivation() throws IOException {
    CaseRule rule = new CaseRule(null, new German());
    assertTrue(rule.supportsLanguage(new German()));
  }

  public void testRule() throws IOException {
    CaseRule rule = new CaseRule(null, new German());
    JLanguageTool langTool = new JLanguageTool(new German());

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Heute spricht Frau Stieg.")).length);
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
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Hier geht es nach Tel Aviv.")).length);
    assertEquals(0, langTool.check("Karten werden vom Auswahlstapel gezogen. […] Der Auswahlstapel gehört zum Inhalt.").size());
//     assertEquals(1, langTool.check("Karten werden vom Auswahlstapel gezogen. [...] Der Auswahlstapel gehört zum Inhalt.").size());
    // "NIL" reading in Morphy that used to confuse CaseRule:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ein Menschenfreund.")).length);
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
    
    // used to trigger error because of "abbreviation"
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Sie fällt auf durch ihre hilfsbereite Art. Zudem zeigt sie soziale Kompetenz.")).length);
    
    // TODO: nach dem Doppelpunkt wird derzeit nicht auf groß/klein getestet:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist es: kein Satz.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist es: Kein Satz.")).length);

    // incorrect sentences:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Ein Einfacher Satz zum Testen.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das Winseln Stört.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Sein verhalten war okay.")).length);
    assertEquals(1, langTool.check("Karten werden vom Auswahlstapel gezogen. Auch […] Der Auswahlstapel gehört zum Inhalt.").size());
//     assertEquals(2, langTool.check("Karten werden vom Auswahlstapel gezogen. Auch [...] Der Auswahlstapel gehört zum Inhalt.").size());
  }

  public void testSubstantivierteVerben() throws IOException {
    CaseRule rule = new CaseRule(null, new German());
    JLanguageTool langTool = new JLanguageTool(new German());

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das fahrende Auto.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das können wir so machen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Denn das Fahren ist einfach.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Fahren ist einfach.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Gehen fällt mir leicht.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Ernten der Kartoffeln ist mühsam.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Entschuldige das späte Weiterleiten.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ich liebe das Lesen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Betreten des Rasens ist verboten.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das haben wir aus eigenem Antrieb getan.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das haben wir.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das haben wir schon.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das lesen sie doch sicher in einer Minute durch.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das lesen Sie doch sicher in einer Minute durch!")).length);

    // Source of the following examples: http://www.canoo.net/services/GermanSpelling/Amtlich/GrossKlein/pgf57-58.html
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Lesen fällt mir schwer.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Sie hörten ein starkes Klopfen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Wer erledigt das Fensterputzen?")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Viele waren am Zustandekommen des Vertrages beteiligt.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Sache kam ins Stocken.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das ist zum Lachen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Euer Fernbleiben fiel uns auf.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Uns half nur noch lautes Rufen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Mitbewohner begnügten sich mit Wegsehen und Schweigen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Sie wollte auf Biegen und Brechen gewinnen.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Er klopfte mit Zittern und Zagen an.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ich nehme die Tabletten auf Anraten meiner Ärztin.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Sie hat ihr Soll erfüllt.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Dies ist ein absolutes Muss.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Das Lesen fällt mir schwer.")).length);

    // incorrect sentences:
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Das fahren ist einfach.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Denn das fahren ist einfach.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Denn das laufen ist einfach.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Denn das essen ist einfach.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Denn das gehen ist einfach.")).length);
    // TODO: detect all the cases not preceded with 'das'
  }

  public void testPhraseExceptions() throws IOException {
    CaseRule rule = new CaseRule(null, new German());
    JLanguageTool langTool = new JLanguageTool(new German());

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
