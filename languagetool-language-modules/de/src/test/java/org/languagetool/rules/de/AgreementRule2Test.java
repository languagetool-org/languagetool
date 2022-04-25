/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class AgreementRule2Test {

  private final GermanyGerman lang = new GermanyGerman();
  private final JLanguageTool lt = new JLanguageTool(lang);
  private final AgreementRule2 rule = new AgreementRule2(TestTools.getEnglishMessages(), lang);

  @Test
  public void testRule() throws IOException {
    assertBad("Kleiner Haus am Waldesrand");
    assertGood("Kleines Haus am Waldesrand");
    assertBad("\"Kleiner Haus am Waldesrand\"");
    assertGood("\"Kleines Haus am Waldesrand\"");
    assertBad("Wirtschaftlich Wachstum kommt ins Stocken");
    assertGood("Wirtschaftliches Wachstum kommt ins Stocken");
    assertGood("Unter Berücksichtigung des Übergangs");

    assertGood("Wirklich Frieden herrscht aber noch nicht");
    assertBad("Deutscher Taschenbuch");
    assertGood("Deutscher Taschenbuch Verlag expandiert");
    
    assertGood("Wohl Anfang 1725 begegnete Bach dem Dichter.");
    assertGood("Weniger Personal wird im ganzen Land gebraucht.");
    assertGood("National Board of Review"); // eng
    assertGood("International Management"); // eng.
    assertGood("Gemeinsam Sportler anfeuern.");
    assertGood("Viel Spaß beim Arbeiten");
    assertGood("Ganz Europa stand vor einer Neuordnung.");
    assertGood("Gesetzlich Versicherte sind davon ausgenommen.");
    assertGood("Ausreichend Bananen essen.");
    assertGood("Nachhaltig Yoga praktizieren");
    assertGood("Überraschend Besuch bekommt er dann von ihr.");
    assertGood("Ruhig Schlafen & Zentral Wohnen");
    assertGood("Voller Mitleid");
    assertGood("Voll Mitleid");
    assertGood("Einzig Fernschüsse brachten Erfolgsaussichten.");
  }

  private void assertGood(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertThat(matches.length, is(0));
  }

  private void assertBad(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertThat(matches.length, is(1));
  }

}
