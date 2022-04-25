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
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.RuleMatch;

public class GenericUnpairedBracketsRuleTest {

  private GenericUnpairedBracketsRule rule;
  private JLanguageTool lt;

  @Test
  public void testGermanRule() throws IOException {
    lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    rule = org.languagetool.rules.GenericUnpairedBracketsRuleTest.getBracketsRule(lt);
    // correct sentences:
    assertMatches("(Das sind die Sätze, die sie testen sollen).", 0);
    assertMatches("(Das sind die «Sätze», die sie testen sollen).", 0);
    assertMatches("(Das sind die »Sätze«, die sie testen sollen).", 0);
    assertMatches("(Das sind die Sätze (noch mehr Klammern [schon wieder!]), die sie testen sollen).", 0);
    assertMatches("Das ist ein Satz mit Smiley :-)", 0);
    assertMatches("Das ist auch ein Satz mit Smiley ;-)", 0);
    assertMatches("Das ist ein Satz mit Smiley :)", 0);
    assertMatches("Das ist ein Satz mit Smiley :(", 0);
    assertMatches("Die URL lautet https://de.wikipedia.org/wiki/Schlammersdorf_(Adelsgeschlecht)", 0);
    assertMatches("Die URL lautet https://de.wikipedia.org/wiki/Schlammersdorf_(Adelsgeschlecht).", 0);
    assertMatches("(Die URL lautet https://de.wikipedia.org/wiki/Schlammersdorf_(Adelsgeschlecht))", 0);
    assertMatches("(Die URL lautet https://de.wikipedia.org/wiki/Schlammersdorf)", 0);
    assertMatches("(Die URL lautet https://de.wikipedia.org/wiki/Schlammersdorf oder so)", 0);
    assertMatches("(Die URL lautet: http://www.pariscinema.org/).", 0);
    assertMatches("Drücken Sie auf den \"Jetzt Starten\"-Knopf.", 0);
    assertMatches("Welches ist dein Lieblings-\"Star Wars\"-Charakter?", 0);
    // incorrect sentences:
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
