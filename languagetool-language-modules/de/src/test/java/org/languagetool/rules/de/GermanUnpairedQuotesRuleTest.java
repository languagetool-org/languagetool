/* LanguageTool, a natural language style checker 
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.GenericUnpairedQuotesRule;
import org.languagetool.rules.GenericUnpairedQuotesRuleTest;
import org.languagetool.rules.RuleMatch;

public class GermanUnpairedQuotesRuleTest {

  private GenericUnpairedQuotesRule rule;
  private JLanguageTool lt;

  @Test
  public void testGermanRule() throws IOException {
    lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    rule = GenericUnpairedQuotesRuleTest.getQuotesRule(lt);
    // correct sentences:
    assertMatches("»Das sind die Sätze, die sie testen sollen«.", 0);
    assertMatches("«Das sind die ‹Sätze›, die sie testen sollen».", 0);
    assertMatches("»Das sind die ›Sätze‹, die sie testen sollen«.", 0);
    assertMatches("»Das sind die Sätze ›noch mehr Anführungszeichen‹ ›schon wieder!‹, die sie testen sollen«.", 0);
    assertMatches("»Das sind die Sätze ›noch mehr Anführungszeichen ›hier ein Fehler!‹‹, die sie testen sollen«.", 2);
    assertMatches("„Das sind die Sätze ‚noch mehr Anführungszeichen‘ ‚schon wieder!‘, die sie testen sollen“.", 0);
    assertMatches("„Das sind die Sätze ‚noch mehr Anführungszeichen ‚hier ein Fehler!‘‘, die sie testen sollen“.", 2);
    assertMatches("„Das sind die Sätze, die sie testen sollen.“ „Hier steht ein zweiter Satz.“", 0);
    assertMatches("Drücken Sie auf den \"Jetzt Starten\"-Knopf.", 0);
    assertMatches("Welches ist dein Lieblings-\"Star Wars\"-Charakter?", 0);
    assertMatches("‚So 'n Blödsinn!‘", 0);
    assertMatches("‚’n Blödsinn!‘", 0);
    assertMatches("'So 'n Blödsinn!'", 0);
    assertMatches("''n Blödsinn!'", 0);
    assertMatches("‚Das ist Hans’.‘", 0);
    assertMatches("'Das ist Hans'.'", 0);
    assertMatches("Das Fahrrad hat 26\" Räder.", 0);
    assertMatches("\"Das Fahrrad hat 26\" Räder.\"", 0);
    assertMatches("und steigern » Datenbankperformance steigern » Tipps zur Performance-Verbesserung", 0);
    // incorrect sentences:
    assertMatches("\"Das Fahrrad hat 26\" Räder.\" \"Und hier fehlt das abschließende doppelte Anführungszeichen.", 1);
    assertMatches("Die „Sätze zum Testen.", 1);
    assertMatches("Die «Sätze zum Testen.", 1);
    assertMatches("Die »Sätze zum Testen.", 1);
    // these used to have wrong positions, causing "Could not map ... to original position":
    lt.check("Im Kran\u00ADken\u00ADhaus. Auch)");
    lt.check("Ein Kran\u00ADken\u00ADhaus. Auch)");
    lt.check("Das Kran\u00ADken\u00ADhaus. Auch)");
    lt.check("Kran\u00ADken\u00ADhaus. Auch)");
    lt.check("Kran\u00ADken\u00ADhaus. (Auch");
  }

  private void assertMatches(String input, int expectedMatches) throws IOException {
    RuleMatch[] matches = rule.match(Collections.singletonList(lt.getAnalyzedSentence(input)));
    assertEquals(expectedMatches, matches.length);
  }
  
}
