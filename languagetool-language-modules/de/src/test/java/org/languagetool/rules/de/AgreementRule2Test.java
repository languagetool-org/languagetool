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
    assertGood("Gelangweilt Dinge sortieren hilft als Ablenkung.");
    assertGood("Ganzjährig Garten pflegen");
    assertGood("Herzlich Willkommen bei unseren günstigen Rezepten!");
    assertGood("10-tägiges Rückgaberecht");
    assertGood("Angeblich Schüsse vor Explosionen gefallen");
    assertGood("Dickes Danke auch an Elena");
    assertGood("Dickes Dankeschön auch an Elena");
    assertGood("Echt Scheiße");
    assertGood("Entsprechende Automaten werden heute nicht mehr gebaut");
    assertGood("Existenziell Bedrohte kriegen einen Taschenrechner");
    assertGood("Flächendeckend Tempo 30");
    assertGood("Frei Klavier spielen lernen");
    assertGood("Ganz Eilige können es schaffen");
    assertGood("Gering Gebildete laufen Gefahr ...");
    assertGood("Ganz Ohr ist man hier");
    assertGood("Gleichzeitig Muskeln aufbauen und Fett verlieren");
    assertGood("Klar Schiff, Erster Offizier!");
    assertGood("Kostenlos Bewegung schnuppern");
    assertGood("Prinzipiell Anrecht auf eine Vertretung");
    assertGood("Regelrecht Modell gestanden haben Michel");
    assertGood("Weitgehend Konsens, auch über ...");
    assertGood("Alarmierte Polizeibeamte nahmen den Mann fest.");
    assertGood("Anderen Brot und Arbeit ermöglichen - das ist ihr Ziel");
    assertGood("Diverse Unwesen, mit denen sich Hellboy beschäftigen muss, ...");
    assertGood("Gut Qualifizierte bekommen Angebote");
    assertGood("Liebe Mai, wie geht es dir?");
    assertGood("Willkommen Simpsons-Fan!");
  }

  @Test
  public void testSuggestion() throws IOException {
    //assertBad("Kleine Haus am Waldesrand", "Kleines Haus");
    //assertBad("Kleinen Haus am Waldesrand", "Kleines Haus");
    assertGood("Kleinem Haus am Waldesrand ...");
    assertBad("Kleiner Haus am Waldesrand", "Kleines Haus");
    assertBad("Kleines Häuser am Waldesrand", "Kleine Häuser");
    assertBad("Kleinem Häuser am Waldesrand", "Kleine Häuser");
    //assertBad("Kleinen Häuser am Waldesrand", "Kleine Häuser");
    assertBad("Kleines Tisch reicht auch", "Kleiner Tisch");
    assertGood("Junger Frau geht das Geld aus");
    assertGood("Junge Frau gewinnt im Lotto");
    assertBad("Junges Frau gewinnt im Lotto", "Junge Frau");
    //assertBad("Jungen Frau gewinnt im Lotto", "Junge Frau");
    assertBad("Jungem Frau gewinnt im Lotto", "Junge Frau");
    assertBad("Jung Frau gewinnt im Lotto", "Junge Frau");
    assertBad("Wirtschaftlich Wachstum kommt ins Stocken", "Wirtschaftliches Wachstum");
    assertBad("Wirtschaftlicher Wachstum kommt ins Stocken", "Wirtschaftliches Wachstum");
    //assertBad("Wirtschaftliche Wachstum kommt ins Stocken", "Wirtschaftliches Wachstum");
  }

  private void assertGood(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertThat(matches.length, is(0));
  }

  private void assertBad(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertThat(matches.length, is(1));
  }

  private void assertBad(String s, String suggestion) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertThat(matches.length, is(1));
    assertTrue("Got suggestions: " + matches[0].getSuggestedReplacements() + ", expected: " + suggestion,
      matches[0].getSuggestedReplacements().contains(suggestion));
  }

}
