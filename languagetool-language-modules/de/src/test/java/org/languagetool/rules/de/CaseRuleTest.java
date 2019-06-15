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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;

public class CaseRuleTest {

  private CaseRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws IOException {
    rule = new CaseRule(TestTools.getMessages("de"), new GermanyGerman());
    lt = new JLanguageTool(new GermanyGerman());
  }

  @Test
  public void testRuleActivation() throws IOException {
    assertTrue(rule.supportsLanguage(new GermanyGerman()));
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertGood("Das ist eine Abkehr von Gottes Geboten.");
    assertGood("Dem Hund Futter geben");
    assertGood("Heute spricht Frau Stieg.");
    assertGood("Ein einfacher Satz zum Testen.");
    assertGood("Das Laufen fällt mir leicht.");
    assertGood("Das Winseln stört.");
    assertGood("Das schlägt nicht so zu Buche.");
    assertGood("Dirk Hetzel ist ein Name.");
    assertGood("Sein Verhalten war okay.");
    assertGood("Hier ein Satz. \"Ein Zitat.\"");
    assertGood("Hier ein Satz. 'Ein Zitat.'");
    assertGood("Hier ein Satz. «Ein Zitat.»");
    assertGood("Hier ein Satz. »Ein Zitat.«");
    assertGood("Hier ein Satz. (Noch einer.)");
    assertGood("Hier geht es nach Tel Aviv.");
    assertGood("Unser Jüngster ist da.");
    assertGood("Alles Erfundene ist wahr.");
    assertGood("Sie hat immer ihr Bestes getan.");
    assertGood("Er wird etwas Verrücktes träumen.");
    assertGood("Er wird etwas schön Verrücktes träumen.");
    assertGood("Er wird etwas ganz schön Verrücktes träumen.");
    assertGood("Mit aufgewühltem Innerem.");
    assertGood("Mit völlig aufgewühltem Innerem.");
    assertGood("Er wird etwas so Verrücktes träumen.");
    assertGood("Tom ist etwas über dreißig.");
    assertGood("Diese Angriffe bleiben im Verborgenen.");
    assertGood("Ihr sollt mich das wissen lassen.");
    assertGood("Wenn er mich das rechtzeitig wissen lässt, gerne.");
    assertGood("Und sein völlig aufgewühltes Inneres erzählte von den Geschehnissen.");
    assertGood("Aber sein aufgewühltes Inneres erzählte von den Geschehnissen.");
    assertGood("Sein aufgewühltes Inneres erzählte von den Geschehnissen.");
    assertGood("Aber sein Inneres erzählte von den Geschehnissen.");
    assertGood("Ein Kaninchen, das zaubern kann.");
    assertGood("Keine Ahnung, wie ich das prüfen sollte.");
    assertGood("Und dann noch Strafrechtsdogmatikerinnen.");
    assertGood("Er kann ihr das bieten, was sie verdient.");
    assertGood("Das fragen sich mittlerweile viele.");
    assertGood("Ich habe gehofft, dass du das sagen würdest.");
    assertGood("Eigentlich hätte ich das wissen müssen.");
    assertGood("Mir tut es wirklich leid, Ihnen das sagen zu müssen.");
    assertGood("Der Wettkampf endete im Unentschieden.");
    assertGood("Er versuchte, Neues zu tun.");
    assertGood("Du musst das wissen, damit du die Prüfung bestehst");
    assertGood("Er kann ihr das bieten, was sie verdient.");
    assertGood("Er fragte, ob das gelingen wird.");
    assertGood("Er mag Obst, wie zum Beispel Apfelsinen.");
    assertGood("Er will die Ausgaben für Umweltschutz und Soziales kürzen.");
    assertGood("Die Musicalverfilmung „Die Schöne und das Biest“ bricht mehrere Rekorde.");
    assertGood("Joachim Sauer lobte Johannes Rau.");
    assertGood("Im Falle des Menschen ist dessen wirkendes Wollen gegeben.");
    assertGood("Szenario: 1) Zwei Galaxien verschmelzen."); // should be accepted by isNumbering
    assertGood("Existieren Außerirdische im Universum?");
    assertGood("Tom vollbringt Außerordentliches.");
    assertGood("Er führt Böses im Schilde.");
    assertGood("Es gab Überlebende.");
    assertGood("'Wir werden das stoppen.'");
    assertGood("Wahre Liebe muss das aushalten.");
    assertGood("Du kannst das machen.");
    assertGood("Vor dem Aus stehen.");
    assertGood("Ich Armer!");
    assertGood("Parks Vertraute Choi Soon Sil ist zu drei Jahren Haft verurteilt worden.");
    assertGood("Bei einer Veranstaltung Rechtsextremer passierte es.");
    assertGood("Eine Gruppe Betrunkener singt.");
    assertGood("Bei Betreten des Hauses.");
    assertGood("Das Aus für Italien ist bitter.");
    assertGood("Das Aus kam unerwartet.");
    assertGood("Anmeldung bis Fr. 1.12.");
    assertGood("Weil er Unmündige sexuell missbraucht haben soll, wurde ein Lehrer verhaftet.");
    assertGood("Tausende Gläubige kamen.");
    assertGood("Es kamen Tausende Gläubige.");
    assertGood("Das schließen Forscher aus den gefundenen Spuren.");
    assertGood("Wieder Verletzter bei Unfall");
    assertGood("Eine Gruppe Aufständischer verwüstete die Bar.");
    assertGood("‚Dieser Satz.‘ Hier kommt der nächste Satz.");
    assertGood("Dabei werden im Wesentlichen zwei Prinzipien verwendet:");
    assertGood("Er fragte, ob das gelingen oder scheitern wird.");
    assertGood("Einen Tag nach Bekanntwerden des Skandals");
    assertGood("Das machen eher die Erwachsenen.");
    assertGood("Das ist ihr Zuhause.");
    assertGood("Das ist Sandras Zuhause.");
    assertGood("Das machen eher wohlhabende Leute.");

    //assertBad("Sie sind nicht Verständlich");
    assertBad("Das machen der Töne ist schwierig.");
    assertBad("Sie Vertraute niemandem.");
    assertBad("Beten Lernt man in Nöten.");
    assertBad("Ich gehe gerne Joggen.");
    assertBad("Er ist Groß.");
    assertBad("Die Zahl ging auf Über 1.000 zurück.");
    assertBad("Er sammelt Große und kleine Tassen.");
    assertBad("Er sammelt Große, mittlere und kleine Tassen.");
    assertBad("Dann will sie mit London Über das Referendum verhandeln.");
    assertBad("Sie kann sich täglich Über vieles freuen.");
    assertBad("Der Vater (51) Fuhr nach Rom.");
    assertBad("Er müsse Überlegen, wie er das Problem löst.");
    assertBad("Er sagte, dass er Über einen Stein stolperte.");
    assertBad("Tom ist etwas über Dreißig.");
    assertBad("Unser warten wird sich lohnen.");
    assertBad("Tom kann mit fast Allem umgehen.");
    assertBad("Dabei Übersah er sie.");
    assertBad("Der Brief wird am Mittwoch in Brüssel Übergeben.");
    assertBad("Damit sollen sie die Versorgung in der Region Übernehmen.");
    assertBad("Die Unfallursache scheint geklärt, ein Lichtsignal wurde Überfahren.");
    assertBad("Der Lenker hatte die Höchstgeschwindigkeit um 76 km/h Überschritten.");
    //assertBad("Das Extreme Sportfest");
    //assertBad("Das Extreme Sportfest findet morgen statt.");
    assertGood("Stets suchte er das Extreme.");
    assertGood("Ich möchte zwei Kilo Zwiebeln.");
    // "NIL" reading in Morphy that used to confuse CaseRule:
    assertGood("Ein Menschenfreund.");
    // works only thanks to addex.txt:
    assertGood("Der Nachfahre.");
    // both can be correct:
    assertGood("Hier ein Satz, \"Ein Zitat.\"");
    assertGood("Hier ein Satz, \"ein Zitat.\"");
    // Exception 'Le':
    assertGood("Schon Le Monde schrieb das.");
    // unknown word:
    assertGood("In Blubberdorf macht man das so.");
    // Exception defined in case_rule_exceptions.txt:
    assertGood("Der Thriller spielt zur Zeit des Zweiten Weltkriegs");

    assertGood("Anders als physikalische Konstanten werden mathematische Konstanten unabhängig von jedem physikalischen Maß definiert.");
    assertGood("Eine besonders einfache Klasse bilden die polylogarithmischen Konstanten.");
    assertGood("Das südlich von Berlin gelegene Dörfchen.");
    assertGood("Weil er das kommen sah, traf er Vorkehrungen.");
    
    assertGood("Sie werden im Allgemeinen gefasst.");
    assertGood("Sie werden im allgemeinen Fall gefasst.");
    //assertBad("Sie werden im allgemeinen gefasst.");
    assertBad("Sie werden im Allgemeinen Fall gefasst.");

    // sentences that used to trigger an error because of incorrect compound tokenization:
    assertGood("Das sind Euroscheine.");
    assertGood("John Stallman isst.");
    assertGood("Das ist die neue Gesellschafterin hier.");
    assertGood("Das ist die neue Dienerin hier.");
    assertGood("Das ist die neue Geigerin hier.");
    assertGood("Die ersten Gespanne erreichen Köln.");
    assertGood("Er beschrieb den Angeklagten wie einen Schuldigen");
    assertGood("Er beschrieb den Angeklagten wie einen Schuldigen.");
    assertGood("Es dauerte bis ins neunzehnte Jahrhundert");

    assertGood("Das ist das Dümmste, was ich je gesagt habe.");
    assertBad("Das ist das Dümmste Kind.");

    assertGood("Wacht auf, Verdammte dieser Welt!");
    assertGood("Er sagt, dass Geistliche davon betroffen sind.");
    assertBad("Er sagt, dass Geistliche Würdenträger davon betroffen sind.");
    assertBad("Er sagt, dass Geistliche und weltliche Würdenträger davon betroffen sind.");
    assertBad("Er ist begeistert Von der Fülle.");
    assertBad("Er wohnt Über einer Garage.");
    assertBad("„Weißer Rauch“ Über Athen");
    assertBad("Die Anderen 90 Prozent waren krank.");

    assertGood("Man sagt, Liebe mache blind.");
    assertGood("Die Deutschen sind sehr listig.");
    assertGood("Der Lesestoff bestimmt die Leseweise.");
    assertGood("Ich habe nicht viel von einem Reisenden.");
    assertGood("Die Vereinigten Staaten");
    assertGood("Der Satz vom ausgeschlossenen Dritten.");
    //TODO:
    assertGood("Die Ausgewählten werden gut betreut.");
    assertGood("Die ausgewählten Leute werden gut betreut.");
    //assertBad("Die ausgewählten werden gut betreut.");
    assertBad("Die Ausgewählten Leute werden gut betreut.");

    // used to trigger error because of wrong POS tagging:
    assertGood("Die Schlinge zieht sich zu.");
    assertGood("Die Schlingen ziehen sich zu.");
    
    // used to trigger error because of "abbreviation"
    assertGood("Sie fällt auf durch ihre hilfsbereite Art. Zudem zeigt sie soziale Kompetenz.");
    
    assertGood("Das ist es: kein Satz.");
    assertGood("Werner Dahlheim: Die Antike.");
    assertGood("1993: Der talentierte Mr. Ripley");
    assertGood("Ian Kershaw: Der Hitler-Mythos: Führerkult und Volksmeinung.");
    assertBad("Das ist es: Kein Satz.");
    assertBad("Wen magst du lieber: Die Giants oder die Dragons?");

    assertGood("Das wirklich Wichtige ist dies:");
    assertGood("Das wirklich wichtige Verfahren ist dies:");
    //assertBad("Das wirklich wichtige ist dies:");
    assertBad("Das wirklich Wichtige Verfahren ist dies:");

    // incorrect sentences:
    assertBad("Die Schöne Tür");
    assertBad("Das Blaue Auto.");
    //assertBad("Der Grüne Baum.");
    assertBad("Ein Einfacher Satz zum Testen.");
    assertBad("Eine Einfache Frage zum Testen?");
    assertBad("Er kam Früher als sonst.");
    assertBad("Er rennt Schneller als ich.");
    assertBad("Das Winseln Stört.");
    assertBad("Sein verhalten war okay.");
    assertEquals(1, lt.check("Karten werden vom Auswahlstapel gezogen. Auch […] Der Auswahlstapel gehört zum Inhalt.").size());
    //assertEquals(2, lt.check("Karten werden vom Auswahlstapel gezogen. Auch [...] Der Auswahlstapel gehört zum Inhalt.").size());

    assertEquals(0, lt.check("Karten werden vom Auswahlstapel gezogen. […] Der Auswahlstapel gehört zum Inhalt.").size());
    //assertEquals(1, lt.check("Karten werden vom Auswahlstapel gezogen. [...] Der Auswahlstapel gehört zum Inhalt.").size());
    //TODO: error not found:
    //assertBad("So schwer, dass selbst Er ihn nicht hochheben kann.");

    assertGood("Im Norwegischen klingt das schöner.");
    assertGood("Übersetzt aus dem Norwegischen von Ingenieur Frederik Dingsbums.");
    assertGood("Dem norwegischen Ingenieur gelingt das gut.");
    assertBad("Dem Norwegischen Ingenieur gelingt das gut.");
    assertGood("Peter Peterson, dessen Namen auf Griechisch Stein bedeutet.");
    assertGood("Peter Peterson, dessen Namen auf Griechisch gut klingt.");
    assertGood("Das dabei Erlernte und Erlebte ist sehr nützlich.");
    assertBad("Das dabei erlernte und Erlebte Wissen ist sehr nützlich.");
    assertGood("Ein Kapitän verlässt als Letzter das sinkende Schiff.");
    assertBad("Diese Regelung wurde als Überholt bezeichnet.");
    assertBad("Die Dolmetscherin und Der Vorleser gehen spazieren.");
    assertGood("Es hilft, die Harmonie zwischen Führer und Geführten zu stützen.");
    assertGood("Das Gebäude des Auswärtigen Amts.");
    assertGood("Das Gebäude des Auswärtigen Amtes.");
    assertGood("   Im Folgenden beschreibe ich das Haus."); // triggers WHITESPACE_RULE, but should not trigger CASE_RULE (see github #258)
    assertGood("\"Im Folgenden beschreibe ich das Haus.\""); //triggers TYPOGRAFISCHE_ANFUEHRUNGSZEICHEN, but should not trigger CASE_RULE
    assertGood("Gestern habe ich 10 Spieße gegessen.");
    assertGood("Die Verurteilten wurden mit dem Fallbeil enthauptet.");
    assertGood("Den Begnadigten kam ihre Reue zugute.");
    assertGood("Die Zahl Vier ist gerade.");
    assertGood("Ich glaube, dass das geschehen wird.");
    assertGood("Ich glaube, dass das geschehen könnte.");
    assertGood("Ich glaube, dass mir das gefallen wird.");
    assertGood("Ich glaube, dass mir das gefallen könnte.");
    assertGood("Alldem wohnte etwas faszinierend Rätselhaftes inne.");
    assertGood("Schau mich an, Kleine!");
    assertGood("Schau mich an, Süßer!");
    assertGood("Weißt du, in welchem Jahr das geschehen ist?");
    assertGood("Das wissen viele nicht.");
    assertBad("Das sagen haben hier viele.");
    assertGood("Die zum Tode Verurteilten wurden in den Hof geführt.");
    assertGood("Wenn Sie das schaffen, retten Sie mein Leben!");
    assertGood("Etwas Grünes, Schleimiges klebte an dem Stein.");
    assertGood("Er befürchtet Schlimmeres.");
    
    // uppercased adjective compounds
    assertGood("Er isst UV-bestrahltes Obst.");
    assertGood("Er isst Na-haltiges Obst.");
    assertGood("Er vertraut auf CO2-arme Wasserkraft");
    assertGood("Das Entweder-oder ist kein Problem.");
    assertGood("Er liebt ihre Makeup-freie Haut.");
  }

  private void assertGood(String input) throws IOException {
    assertEquals("Did not expect error in: '" + input + "'", 0, rule.match(lt.getAnalyzedSentence(input)).length);
  }

  private void assertBad(String input) throws IOException {
    assertEquals("Did not find expected error in: '" + input + "'", 1, rule.match(lt.getAnalyzedSentence(input)).length);
  }

  @Test
  public void testSubstantivierteVerben() throws IOException {
    // correct sentences:
    assertGood("Das fahrende Auto.");
    assertGood("Das können wir so machen.");
    assertGood("Denn das Fahren ist einfach.");
    assertGood("Das Fahren ist einfach.");
    assertGood("Das Gehen fällt mir leicht.");
    assertGood("Das Ernten der Kartoffeln ist mühsam.");
    assertGood("Entschuldige das späte Weiterleiten.");
    assertGood("Ich liebe das Lesen.");
    assertGood("Das Betreten des Rasens ist verboten.");
    assertGood("Das haben wir aus eigenem Antrieb getan.");
    assertGood("Das haben wir.");
    assertGood("Das haben wir schon.");
    assertGood("Das lesen sie doch sicher in einer Minute durch.");
    assertGood("Das lesen Sie doch sicher in einer Minute durch!");
    assertGood("Formationswasser, das oxidiert war.");

    // Source of the following examples: http://www.canoonet.eu/services/GermanSpelling/Amtlich/GrossKlein/pgf57-58.html
    assertGood("Das Lesen fällt mir schwer.");
    assertGood("Sie hörten ein starkes Klopfen.");
    assertGood("Wer erledigt das Fensterputzen?");
    assertGood("Viele waren am Zustandekommen des Vertrages beteiligt.");
    assertGood("Die Sache kam ins Stocken.");
    assertGood("Das ist zum Lachen.");
    assertGood("Euer Fernbleiben fiel uns auf.");
    assertGood("Uns half nur noch lautes Rufen.");
    assertGood("Die Mitbewohner begnügten sich mit Wegsehen und Schweigen.");
    assertGood("Sie wollte auf Biegen und Brechen gewinnen.");
    assertGood("Er klopfte mit Zittern und Zagen an.");
    assertGood("Ich nehme die Tabletten auf Anraten meiner Ärztin.");
    assertGood("Sie hat ihr Soll erfüllt.");
    assertGood("Dies ist ein absolutes Muss.");
    assertGood("Das Lesen fällt mir schwer.");

    // incorrect sentences:
    assertBad("Das fahren ist einfach.");
    assertBad("Denn das fahren ist einfach.");
    assertBad("Denn das laufen ist einfach.");
    assertBad("Denn das essen ist einfach.");
    assertBad("Denn das gehen ist einfach.");
    assertBad("Das Große Auto wurde gewaschen.");
    assertBad("Ich habe ein Neues Fahrrad.");
    // TODO: detect all the cases not preceded with 'das'
  }

  @Test
  public void testPhraseExceptions() throws IOException {
    // correct sentences:
    assertGood("Das gilt ohne Wenn und Aber.");
    assertGood("Ohne Wenn und Aber");
    assertGood("Das gilt ohne Wenn und Aber bla blubb.");
    // as long as phrase exception isn't complete, there's no error:
    assertGood("Das gilt ohne wenn");
    assertGood("Das gilt ohne wenn und");
    assertGood("wenn und aber");
    assertGood("und aber");
    assertGood("aber");
    // incorrect sentences:
    // error not found here as it's in the XML rules:
    //assertBad("Das gilt ohne wenn und aber.");
  }

  @Test
  public void testCompareLists() throws IOException {
    AnalyzedSentence sentence1 = lt.getAnalyzedSentence("Hier ein Test");
    assertTrue(rule.compareLists(sentence1.getTokensWithoutWhitespace(), 0, 2, new Pattern[]{Pattern.compile(""), Pattern.compile("Hier"), Pattern.compile("ein")}));
    assertTrue(rule.compareLists(sentence1.getTokensWithoutWhitespace(), 1, 2, new Pattern[]{Pattern.compile("Hier"), Pattern.compile("ein")}));
    assertTrue(rule.compareLists(sentence1.getTokensWithoutWhitespace(), 0, 3, new Pattern[]{Pattern.compile(""), Pattern.compile("Hier"), Pattern.compile("ein"), Pattern.compile("Test")}));
    assertFalse(rule.compareLists(sentence1.getTokensWithoutWhitespace(), 0, 4, new Pattern[]{Pattern.compile(""), Pattern.compile("Hier"), Pattern.compile("ein"), Pattern.compile("Test")}));

    AnalyzedSentence sentence2 = lt.getAnalyzedSentence("das Heilige Römische Reich");
    assertTrue(rule.compareLists(sentence2.getTokensWithoutWhitespace(), 0, 4, new Pattern[]{Pattern.compile(""), Pattern.compile("das"), Pattern.compile("Heilige"), Pattern.compile("Römische"), Pattern.compile("Reich")}));
    assertFalse(rule.compareLists(sentence2.getTokensWithoutWhitespace(), 8, 11, new Pattern[]{Pattern.compile(""), Pattern.compile("das"), Pattern.compile("Heilige"), Pattern.compile("Römische"), Pattern.compile("Reich")}));
  }
}
