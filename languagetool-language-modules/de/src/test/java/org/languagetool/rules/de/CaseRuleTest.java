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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.German;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.languagetool.rules.patterns.StringMatcher.regexp;

public class CaseRuleTest {

  private CaseRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new CaseRule(TestTools.getMessages("de"), (German) Languages.getLanguageForShortCode("de-DE"));
    lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
  }

  @Test
  public void testRuleActivation() {
    assertTrue(rule.supportsLanguage(Languages.getLanguageForShortCode("de-DE")));
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertGood("(Dauer, Raum, Anwesende)");
    assertGood("Es gibt wenige Befragte.");
    assertGood("Es gibt weniger Befragte, die das machen würden.");
    assertGood("Es gibt mehr Befragte, die das machen würden.");
    assertGood("Das ist eine Abkehr von Gottes Geboten.");
    assertGood("Dem Hund Futter geben");
    assertGood("Heute spricht Frau Stieg.");
    assertGood("So könnte es auch den Handwerksbetrieben gehen, die ausbilden und deren Ausbildung dann Industriebetrieben zugutekäme.");
    assertGood("Die Firma Drosch hat nicht pünktlich geliefert.");
    assertGood("3.1 Technische Dokumentation");
    assertGood("Ein einfacher Satz zum Testen.");
    assertGood("Das Laufen fällt mir leicht.");
    assertGood("Das Winseln stört.");
    assertGood("Das schlägt nicht so zu Buche.");
    assertGood("Dirk Hetzel ist ein Name.");
    assertGood("Aber sie tat es, sodass unsere Klasse das sehen und fotografieren konnte.");
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
    assertGood("Hallo Malte,");
    assertGood("Parks Vertraute Choi Soon Sil ist zu drei Jahren Haft verurteilt worden.");
    assertGood("Bei einer Veranstaltung Rechtsextremer passierte es.");
    assertGood("Eine Gruppe Betrunkener singt.");
    assertGood("Bei Betreten des Hauses.");
    assertGood("Das Aus für Italien ist bitter.");
    assertGood("Das Aus kam unerwartet.");
    assertGood("Anmeldung bis Fr. 1.12.");
    assertGood("Gibt es die Schuhe auch in Gr. 43?");
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
    assertGood("Als Erstes würde ich sofort die Struktur ändern.");
    assertGood("Er sagte: Als Erstes würde ich sofort die Struktur ändern.");
    assertGood("Das schaffen moderne E-Autos locker.");
    assertGood("Das schaffen moderne E-Autos schneller");
    assertGood("Das schaffen moderne und effizientere E-Autos schneller.");
    assertGood("Das verwalten User.");
    assertGood("Man kann das generalisieren");
    assertGood("Aber wie wir das machen und sicher gestalten, darauf konzentriert sich unsere Arbeit.");
    assertGood("Vielleicht kann man das erweitern");
    assertGood("Vielleicht soll er das generalisieren");
    assertGood("Wahrscheinlich müssten sie das überarbeiten");
    assertGood("Assistenzsysteme warnen rechtzeitig vor Gefahren.");
    assertGood("Jeremy Schulte rannte um sein Leben.");
    assertGood("Er arbeitet im Bereich Präsidiales.");
    assertGood("Er spricht Sunnitisch & Schiitisch.");
    assertGood("Er sagte, Geradliniges und Krummliniges sei unvergleichbar.");
    assertGood("Dort erfahren sie Kurioses und Erstaunliches zum Zusammenspiel von Mensch und Natur.");
    assertGood("Dabei unterscheidet die Shareware zwischen Privatem und Dienstlichem bei Fahrten ebenso wie bei Autos.");
    assertGood("Besucher erwartet Handegefertigtes, Leckeres und Informatives rund um den Hund.");
    assertGood("Der Unterschied zwischen Vorstellbarem und Machbarem war niemals geringer.");
    assertGood("Das war Fiete Lang.");
    assertGood("Wenn du an das glaubst, was du tust, kannst du Großes erreichen.");
    assertGood("Dann hat er Großes erreicht.");
    assertGood("Dann hat er Großes geleistet.");
    assertGood("Das Thema Datenaustauschverfahren ist mir wichtig.");
    assertGood("Ist das eine Frage ? Müsste das nicht anders sein?");
    assertGood("Das ist ein Satz !!! Das auch.");
    assertGood("Der russische Erdölmagnat Emanuel Nobel, der Erbauer des ersten Dieselmotorschiffes.");
    assertGood("Zur Versöhnung: Jüdische Gläubige sollen beten.");
    assertGood("Fast im Stundentakt wurden neue Infizierte gemeldet.");
    assertGood("Bert Van Den Brink");
    assertGood("“In den meisten Bundesländern werden solche Studien per se nicht durchgeführt.”");
    assertGood("Aber “in den meisten Bundesländern werden solche Studien per se nicht durchgeführt.”");
    assertGood("A) Das Haus");
    assertGood("Rabi und Polykarp Kusch an der Columbia-Universität");
    assertGood("Man geht davon aus, dass es sich dabei nicht um Reinigungsverhalten handelt.");
    assertGood("Wenn dort oft Gefahren lauern.");
    assertGood("3b) Den Bereich absichern");
    assertGood("@booba Da der Holger keine Zeit hat ...");
    assertGood("Es gibt infizierte Ärzt*innen.");
    assertGood("WUrzeln");  // to be found by spell checker
    assertGood("🙂 Übrigens finde ich dein neues Ordnungssystem richtig genial!");
    assertGood("Ein 10,4 Ah Lithium-Akku");
    assertGood("14:15 Uhr SpVgg Westheim");
    assertGood("Unser Wärmestrom-Tarif WärmeKompakt im Detail");  // ignore so we don't suggest "wärmeKompakt" (#3779)
    assertGood("Autohaus Dornig GmbH");
    assertGood("Hans Pries GmbH");
    assertGood(":D Auf dieses Frl.");
    assertGood("Das Gedicht “Der Panther”.");  // quotes are not correct, but leave that to the quotes rule
    assertGood("Klar, dass wir das brauchen.");
    assertGood("Das wird Scholz' engster Vertrauter Wolfgang Schmidt übernehmen.");

    assertGood("Ist das eine Frage ? Müsste das nicht anders sein?");
    assertGood("Das ist ein Satz !!! Das auch.");
    assertGood("Liebe Kund:in");
    assertGood("Wir sollten das mal labeln.");

    // https://github.com/languagetool-org/languagetool/issues/1515:
    assertGood("▶︎ Dies ist ein Test");
    assertGood("▶ Dies ist ein Test");
    assertGood("* Dies ist ein Test");
    assertGood("- Dies ist ein Test");
    assertGood("• Dies ist ein Test");
    assertGood(":-) Dies ist ein Test");
    assertGood(";-) Dies ist ein Test");
    assertGood(":) Dies ist ein Test");
    assertGood(";) Dies ist ein Test");
    assertGood("..., die ins Nichts griff.");
    assertGood("Er fragte, was sie über das denken und zwinkerte ihnen zu.");
    assertGood("dem Ägyptischen, Berberischen, Semitischen, Kuschitischen, Omotischen und dem Tschadischen");
    assertGood("mit S-Bahn-ähnlichen Verkehrsmitteln");
    assertGood("mit U-Bahn-ähnlichen und günstigen Verkehrsmitteln");
    assertGood("mit Ü-Ei-großen, schweren Hagelkörnern");
    assertGood("mit E-Musik-artigen, komplizierten Harmonien");

    //assertBad("Sie sind nicht Verständlich");
    assertBad("Das machen der Töne ist schwierig.");
    assertBad("Sie Vertraute niemandem.");
    assertBad("Beten Lernt man in Nöten.");
    assertBad("Ich habe Heute keine Zeit.");
    assertBad("Er sagte, Geradliniges und krummliniges sei unvergleichbar.");
    assertBad("Er sagte, ein Geradliniges und Krummliniges Konzept ist nicht tragbar.");
    assertBad("Ä Was?");
    assertBad("… die preiswerte Variante unserer Topseller im Bereich Alternativ Mehle.");
    assertBad("…  jahrzehntelangen Mitstreitern und vielen Freunden aus Nah und Fern.");
    assertBad("Hi und Herzlich willkommen auf meiner Seite.");
    //assertBad("Ich gehe gerne Joggen.");
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
    assertBad("Das sind 10 Millionen Euro, Gleichzeitig und zusätzlich.");
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
    //assertBad("„Weißer Rauch“ Über Athen");   // could be title/quote, so not detected
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
    assertBad("Bis Bald!");
    assertGood("#4 Aktuelle Situation");
    assertGood("Er trinkt ein kühles Blondes.");
    assertGood("* [ ] Ein GitHub Markdown Listenpunkt");
    assertGood("Tom ist ein engagierter, gutaussehender Vierzigjähriger, der...");
    assertGood("a.) Im Zusammenhang mit ...");
    assertGood("✔︎ Weckt Aufmerksamkeit.");
    assertGood("Hallo Eckhart,");
    assertGood("Er kann Polnisch und Urdu.");
    assertGood("---> Der USB 3.0 Stecker");
    assertGood("Black Lives Matter");
    assertGood("== Schrittweise Erklärung");
    assertGood("Audi A5 Sportback 2.0 TDI");
    assertGood("§ 1 Allgemeine Bedingungen");
    assertGood("§1 Allgemeine Bedingungen");
    assertGood("[H3] Was ist Daytrading?");
    assertGood(" Das ist das Aus des Airbus A380.");
    assertGood("Wir sollten ihr irgendwas Erotisches schenken.");
    assertGood("Er trank ein paar Halbe.");
    assertGood("Sie/Er hat Schuld.");
    assertGood("Das war irgendein Irrer.");
    assertGood("Wir wagen Neues.");
    assertGood("Grundsätzlich gilt aber: Essen Sie, was die Einheimischen Essen.");
    assertGood("Vielleicht reden wir später mit ein paar Einheimischen.");
    assertBad("Das existiert im Jazz zunehmend nicht mehr Bei der weiteren Entwicklung des Jazz zeigt sich das.");
    assertGood("Das denken zwar viele, ist aber total falsch.");
    assertGood("Ich habe nix Besseres gefunden.");
    assertGood("Ich habe nichts Besseres gefunden.");
    assertGood("Ich habe noch Dringendes mitzuteilen.");

    // uppercased adjective compounds
    assertGood("Er isst UV-bestrahltes Obst.");
    assertGood("Er isst Na-haltiges Obst.");
    assertGood("Er vertraut auf CO2-arme Wasserkraft");
    assertGood("Das Entweder-oder ist kein Problem.");
    assertGood("Er liebt ihre Makeup-freie Haut.");
    assertGood("Das ist eine Schreibweise.");
    assertBad("Das ist Eine Schreibweise.");
    assertGood("Das ist ein Mann.");
    assertBad("Das ist Ein Mann.");

    assertBad("Sie erhalten bald unsere Neuesten Insights.");
    assertBad("Auf eine Carvingschiene sollte die Kette schon im Kalten Zustand weit durchhängen.");

    assertGood("Du Ärmste!");
    assertGood("Ich habe nur Schlechtes über den Laden gehört.");
    assertGood("Du Ärmster, leg dich besser ins Bett.");
    assertGood("Er wohnt Am Hohen Hain 6a");
    assertGood("Das Bauvorhaben Am Wiesenhang 9");
    assertGood("... und das Zwischenmenschliche Hand in Hand.");
    assertGood("Der Platz auf dem die Ahnungslosen Kopf an Kopf stehen.");
    assertGood("4.)   Bei Beschäftigung von Hilfskräften: Schadenfälle durch Hilfskräfte");
    assertGood("Es besteht aus Schülern, Arbeitstätigen und Studenten.");
    assertGood("Sie starrt ständig ins Nichts.");
    assertGood("\\u2063Das Haus ist schlön.");
    assertGood("\\u2063\\u2063Das Haus ist schlön.");
    assertGood("Die Mannschaft ist eine gelungene Mischung aus alten Haudegen und jungen Wilden.");
    assertGood("Alleine durch die bloße Einwohnerzahl des Landes leben im Land zahlreiche Kulturschaffende, nach einer Schätzung etwa 30.000 Künstler.");
    assertGood("Ich hatte das offenbar vergessen oder nicht ganz verstanden.");
    assertGood("Ich hatte das vergessen oder nicht ganz verstanden.");
    assertGood("Das ist ein zwingendes Muss.");
    assertGood("Er hält eine Handbreit Abstand.");
    assertGood("Das ist das Debakel und Aus für Podolski.");
    assertGood("Der schwedische Psychologe Dan Katz, Autor von 'Angst kocht auch nur mit Wasser', sieht in der Corona-Krise dennoch nicht nur Negatives.");
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
    assertGood("Um das herauszubekommen diskutieren zwei Experten.");

    // Source of the following examples: https://dict.leo.org/grammatik/deutsch/Rechtschreibung/Amtlich/GrossKlein/pgf57-58.html
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
    assertTrue(CaseRule.compareLists(sentence1.getTokensWithoutWhitespace(), 0, 2, regexp(""), regexp("Hier"), regexp("ein")));
    assertTrue(CaseRule.compareLists(sentence1.getTokensWithoutWhitespace(), 1, 2, regexp("Hier"), regexp("ein")));
    assertTrue(CaseRule.compareLists(sentence1.getTokensWithoutWhitespace(), 0, 3, regexp(""), regexp("Hier"), regexp("ein"), regexp("Test")));
    assertFalse(CaseRule.compareLists(sentence1.getTokensWithoutWhitespace(), 0, 4, regexp(""), regexp("Hier"), regexp("ein"), regexp("Test")));

    AnalyzedSentence sentence2 = lt.getAnalyzedSentence("das Heilige Römische Reich");
    assertTrue(CaseRule.compareLists(sentence2.getTokensWithoutWhitespace(), 0, 4, regexp(""), regexp("das"), regexp("Heilige"), regexp("Römische"), regexp("Reich")));
    assertFalse(CaseRule.compareLists(sentence2.getTokensWithoutWhitespace(), 8, 11, regexp(""), regexp("das"), regexp("Heilige"), regexp("Römische"), regexp("Reich")));
  }
}
