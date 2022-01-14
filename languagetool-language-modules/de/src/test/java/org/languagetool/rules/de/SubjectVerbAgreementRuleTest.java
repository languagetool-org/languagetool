/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.languagetool.*;
import org.languagetool.chunking.GermanChunker;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;

public class SubjectVerbAgreementRuleTest {

  private static SubjectVerbAgreementRule rule;
  private static JLanguageTool lt;

  @BeforeClass
  public static void setUp() {
    Language german = Languages.getLanguageForShortCode("de-DE");
    rule = new SubjectVerbAgreementRule(TestTools.getMessages("de"), (German) german);
    lt = new JLanguageTool(german);
  }

  @Test
  public void testTemp() throws IOException {
    // For debugging, comment in the next three lines:
    //GermanChunker.setDebug(true);
    //assertGood("...");
    //assertBad("...");
    // Hier ist (auch: sind) sowohl Anhalten wie Parken verboten.
    // TODO - false alarms from Tatoeba and Wikipedia:
    // "Die restlichen sechsundachtzig oder siebenundachtzig Prozent sind in der Flasche.",
    // "Die Führung des Wortes in Unternehmensnamen ist nur mit Genehmigung zulässig.",   // OpenNLP doesn't find 'Unternehmensnamen' as a noun
    // "Die ältere der beiden Töchter ist hier.",
    // "Liebe und Hochzeit sind nicht das Gleiche."
    // "Die Typologie oder besser Typographie ist die Klassifikation von Objekten"
    // "Im Falle qualitativer, quantitativer und örtlicher Veränderung ist dies ein konkretes Einzelding,"
    // "...zu finden, in denen die Päpste selbst Partei waren."
    // "Hauptfigur der beiden Bücher ist Golan Trevize."
    // "Das größte und bekannteste Unternehmen dieses Genres ist der Cirque du Soleil."
    // "In Schweden, Finnland, Dänemark und Österreich ist die Haltung von Wildtieren erlaubt."
    // "Die einzige Waffe, die keine Waffe der Gewalt ist: die Wahrheit."
    // "Du weißt ja wie töricht Verliebte sind."
    // "Freies Assoziieren und Phantasieren ist erlaubt."
    // "In den beiden Städten Bremen und Bremerhaven ist jeweils eine Müllverbrennungsanlage in Betrieb."
    // "Hauptstadt und größte Stadt des Landes ist Sarajevo."
    // "Durch Rutschen, Fallrohre oder Schläuche ist der Beton bis in die Schalung zu leiten."
    // "Wegen ihres ganzen Erfolgs war sie unglücklich."
    // "Eines der bedeutendsten Museen ist das Museo Nacional de Bellas Artes."
    // "Die Nominierung der Filme sowie die Auswahl der Jurymitglieder ist Aufgabe der Festivaldirektion."
    // "Ehemalige Fraktionsvorsitzende waren Schmidt, Kohl und Merkel."
    // "Die Hälfte der Äpfel sind verfault."
    // "... in der Geschichte des Museums, die Sammlung ist seit März 2011 dauerhaft der Öffentlichkeit zugänglich."
    // "Ein gutes Aufwärmen und Dehnen ist zwingend notwendig."
    // "Eine Stammfunktion oder ein unbestimmtes Integral ist eine mathematische Funktion ..."
    // "Wenn die Begeisterung für eine Person, Gruppe oder Sache religiöser Art ist ..."
    // "Ein Staat, dessen Oberhaupt nicht ein König oder eine Königin ist."
    // "Des Menschen größter Feind und bester Freund ist ein anderer Mensch."
    // "Die Nauheimer Musiktage, die zu einer Tradition geworden sind und immer wieder ein kultureller Höhepunkt sind."
    // "Ein erheblicher Teil der anderen Transportmaschinen waren schwerbeschädigt."  // ??
    // "Die herrschende Klasse und die Klassengesellschaft war geboren."  // ??
    // "Russland ist der größte Staat der Welt und der Vatikan ist der kleinste Staat der Welt.",
    // "Eine Rose ist eine Blume und eine Taube ist ein Vogel.",
    // "Der beste Beobachter und der tiefste Denker ist immer der mildeste Richter.",
    //assertGood("Dumas ist der Familienname folgender Personen.");  // Dumas wird als Plural von Duma erkannt
    //assertGood("Berlin war Hauptstadt des Vergnügens und der Wintergarten war angesagt.");  // wg. 'und'
    //assertGood("Elemente eines axiomatischen Systems sind:");  // 'Elemente' ist ambig (SIN, PLU)
    //assertGood("Auch wenn Dortmund größte Stadt und ein Zentrum dieses Raums ist.");  // unsere 'und'-Regel darf hier nicht matchen
    //assertGood("Die Zielgruppe waren Glaubensangehörige im Ausland sowie Reisende.");  // Glaubensangehörige hat kein Plural-Reading in Morphy
  }

  @Test
  public void testPrevChunkIsNominative() throws IOException {
    assertTrue(rule.prevChunkIsNominative(getTokens("Die Katze ist süß"), 2));
    assertTrue(rule.prevChunkIsNominative(getTokens("Das Fell der Katzen ist süß"), 4));

    assertFalse(rule.prevChunkIsNominative(getTokens("Dem Mann geht es gut."), 2));
    assertFalse(rule.prevChunkIsNominative(getTokens("Dem alten Mann geht es gut."), 2));
    assertFalse(rule.prevChunkIsNominative(getTokens("Beiden Filmen war kein Erfolg beschieden."), 2));
    assertFalse(rule.prevChunkIsNominative(getTokens("Aber beiden Filmen war kein Erfolg beschieden."), 3));
    //assertFalse(rule.prevChunkIsNominative(getTokens("Der Katzen Fell ist süß"), 3));
  }

  @Test
  public void testArrayOutOfBoundsBug() throws IOException {
    rule.match(lt.getAnalyzedSentence("Die nicht Teil des Näherungsmodells sind"));
  }

  private AnalyzedTokenReadings[] getTokens(String s) throws IOException {
    return lt.getAnalyzedSentence(s).getTokensWithoutWhitespace();
  }

  @Test
  public void testRuleWithIncorrectSingularVerb() throws IOException {
    List<String> sentences = Arrays.asList(
        "Die Autos ist schnell.",
        "Der Hund und die Katze ist draußen.",
        "Ein Hund und eine Katze ist schön.",
        "Der Hund und die Katze ist schön.",
        "Der große Hund und die Katze ist schön.",
        "Der Hund und die graue Katze ist schön.",
        "Der große Hund und die graue Katze ist schön.",
        "Die Kenntnisse ist je nach Bildungsgrad verschieden.",
        "Die Kenntnisse der Sprachen ist je nach Bildungsgrad verschieden.",
        "Die Kenntnisse der Sprache ist je nach Bildungsgrad verschieden.",
        "Die Kenntnisse der europäischen Sprachen ist je nach Bildungsgrad verschieden.",
        "Die Kenntnisse der neuen europäischen Sprachen ist je nach Bildungsgrad verschieden.",
        "Die Kenntnisse der deutschen Sprache ist je nach Bildungsgrad verschieden.",
        "Die Kenntnisse der aktuellen deutschen Sprache ist je nach Bildungsgrad verschieden.",
        "Drei Katzen ist im Haus.",
        "Drei kleine Katzen ist im Haus.",
        "Viele Katzen ist schön.",
        "Drei Viertel der Erdoberfläche ist Wasser.",  // http://canoonet.eu/blog/2012/04/02/ein-drittel-der-schueler-istsind/
        "Die ältesten und bekanntesten Maßnahmen ist die Einrichtung von Schutzgebieten.",
        "Ein Gramm Pfeffer waren früher wertvoll.",
        "Isolation und ihre Überwindung ist ein häufiges Thema in der Literatur."
        //"Katzen ist schön."
    );
    for (String sentence : sentences) {
      assertBad(sentence);
    }
  }

  @Test
  public void testRuleWithCorrectSingularVerb() throws IOException {
    List<String> sentences = Arrays.asList(
        "All diesen Stadtteilen ist die Nähe zum Hamburger Hafen und zu den Industrie- und Gewerbegebieten gemein.",
        "All diesen Bereichen ist gemeinsam, dass sie unterfinanziert sind.",
        "Nicht entmutigen lassen, nur weil Sie kein Genie sind.",
        "Denken Sie daran, dass Sie hier zu Gast sind und sich entsprechend verhalten sollten.",
        "Ist es wahr, dass Sie ein guter Mensch sind?",
        "Die Katze ist schön.",
        "Die eine Katze ist schön.",
        "Eine Katze ist schön.",
        "Beiden Filmen war kein Erfolg beschieden.",
        "In einigen Fällen ist der vermeintliche Beschützer schwach.",
        "Was Wasser für die Fische ist.",
        "In den letzten Jahrzehnten ist die Zusammenarbeit der Astronomie verbessert worden.",
        "Für Oberleitungen bei elektrischen Bahnen ist es dagegen anders.",
        "... deren Thema die Liebe zwischen männlichen Charakteren ist.",
        "Mehr als das in westlichen Produktionen der Fall ist.",
        "Da das ein fast aussichtsloses Unterfangen ist.",
        "Was sehr verbreitet bei der Synthese organischer Verbindungen ist.",
        "In chemischen Komplexverbindungen ist das Kation wichtig.",
        "In chemischen Komplexverbindungen ist das As5+-Kation wichtig.",
        "Die selbstständige Behandlung psychischer Störungen ist jedoch ineffektiv.",
        "Die selbstständige Behandlung eigener psychischer Störungen ist jedoch ineffektiv.",
        "Im Gegensatz zu anderen akademischen Berufen ist es in der Medizin durchaus üblich ...",
        "Im Unterschied zu anderen Branchen ist Ärzten anpreisende Werbung verboten.",
        "Aus den verfügbaren Quellen ist es ersichtlich.",
        "Das Mädchen mit den langen Haaren ist Judy.",
        "Der Durchschnitt offener Mengen ist nicht notwendig offen.",
        "Der Durchschnitt vieler offener Mengen ist nicht notwendig offen.",
        "Der Durchschnitt unendlich vieler offener Mengen ist nicht notwendig offen.",
        "Der Ausgangspunkt für die heute gebräuchlichen Alphabete ist ...",
        "Nach sieben männlichen Amtsvorgängern ist Merkel ...",
        "Für einen japanischen Hamburger ist er günstig.",
        "Derzeitiger Bürgermeister ist seit 2008 der ehemalige Minister Müller.",
        "Derzeitiger Bürgermeister der Stadt ist seit 2008 der ehemalige Minister Müller.",
        "Die Eingabe mehrerer assoziativer Verknüpfungen ist beliebig.",
        "Die inhalative Anwendung anderer Adrenalinpräparate zur Akutbehandlung asthmatischer Beschwerden ist somit außerhalb der arzneimittelrechtlichen Zulassung.",
        "Die Kategorisierung anhand morphologischer Merkmale ist nicht objektivierbar.",
        "Die Kategorisierung mit morphologischen Merkmalen ist nicht objektivierbar.",
        "Ute, deren Hauptproblem ihr Mangel an Problemen ist, geht baden.",
        "Ute, deren Hauptproblem ihr Mangel an realen Problemen ist, geht baden.",
        "In zwei Wochen ist Weihnachten.",
        "In nur zwei Wochen ist Weihnachten.",
        "Mit chemischen Methoden ist es möglich, das zu erreichen.",
        "Für die Stadtteile ist auf kommunalpolitischer Ebene jeweils ein Beirat zuständig.",
        "Für die Stadtteile und selbständigen Ortsteile ist auf kommunalpolitischer Ebene jeweils ein Beirat zuständig.",
        "Die Qualität der Straßen ist unterschiedlich.",
        "In deutschen Installationen ist seit Version 3.3 ein neues Feature vorhanden.",
        "In deren Installationen ist seit Version 3.3 ein neues Feature vorhanden.",
        "In deren deutschen Installationen ist seit Version 3.3 ein neues Feature vorhanden.",
        "Die Führung des Wortes in Unternehmensnamen ist nur mit Genehmigung zulässig.",
        "Die Führung des Wortes in Unternehmensnamen und Institutionen ist nur mit Genehmigung zulässig.",
        "Die Hintereinanderreihung mehrerer Einheitenvorsatznamen oder Einheitenvorsatzzeichen ist nicht zulässig.",
        "Eines ihrer drei Autos ist blau und die anderen sind weiß.",
        "Eines von ihren drei Autos ist blau und die anderen sind weiß.",
        "Bei fünf Filmen war Robert F. Boyle für das Production Design verantwortlich.",
        "Insbesondere das Wasserstoffatom als das einfachste aller Atome war dabei wichtig.",
        "In den darauf folgenden Wochen war die Partei führungslos",
        "Gegen die wegen ihrer Schönheit bewunderte Phryne ist ein Asebie-Prozess überliefert.",
        "Dieses für Ärzte und Ärztinnen festgestellte Risikoprofil ist berufsunabhängig.",
        "Das ist problematisch, da kDa eine Masseeinheit und keine Gewichtseinheit ist.",
        "Nach sachlichen oder militärischen Kriterien war das nicht nötig.",
        "Die Pyramide des Friedens und der Eintracht ist ein Bauwerk.",
        "Ohne Architektur der Griechen ist die westliche Kultur der Neuzeit nicht denkbar.",
        "Ohne Architektur der Griechen und Römer ist die westliche Kultur der Neuzeit nicht denkbar.",
        "Ohne Architektur und Kunst der Griechen und Römer ist die westliche Kultur der Neuzeit nicht denkbar.",
        "In denen jeweils für eine bestimmte Anzahl Elektronen Platz ist.",
        "Mit über 1000 Handschriften ist Aristoteles ein Vielschreiber.",
        "Mit über neun Handschriften ist Aristoteles ein Vielschreiber.",
        "Die Klammerung assoziativer Verknüpfungen ist beliebig.",
        "Die Klammerung mehrerer assoziativer Verknüpfungen ist beliebig.",
        "Einen Sonderfall bildete jedoch Ägypten, dessen neue Hauptstadt Alexandria eine Gründung Alexanders und der Ort seines Grabes war.",
        "Jeder Junge und jedes Mädchen war erfreut.",
        "Jedes Mädchen und jeder Junge war erfreut.",
        "Jede Frau und jeder Junge war erfreut.",
        "Als Wissenschaft vom Erleben des Menschen einschließlich der biologischen Grundlagen ist die Psychologie interdisziplinär.",
        "Als Wissenschaft vom Erleben des Menschen einschließlich der biologischen und sozialen Grundlagen ist die Psychologie interdisziplinär.",
        "Als Wissenschaft vom Erleben des Menschen einschließlich der biologischen und neurowissenschaftlichen Grundlagen ist die Psychologie interdisziplinär.",  // 'neurowissenschaftlichen' not known
        "Als Wissenschaft vom Erleben und Verhalten des Menschen einschließlich der biologischen bzw. sozialen Grundlagen ist die Psychologie interdisziplinär.",
        "Alle vier Jahre ist dem Volksfest das Landwirtschaftliche Hauptfest angeschlossen.",
        "Aller Anfang ist schwer.",
        "Alle Dichtung ist zudem Darstellung von Handlungen.",
        "Allen drei Varianten ist gemeinsam, dass meistens nicht unter bürgerlichem...",
        "Er sagte, dass es neun Uhr war.",
        "Auch den Mädchen war es untersagt, eine Schule zu besuchen.",
        "Das dazugehörende Modell der Zeichen-Wahrscheinlichkeiten ist unter Entropiekodierung beschrieben.",
        "Ein über längere Zeit entladener Akku ist zerstört.",
        "Der Fluss mit seinen Oberläufen Río Paraná und Río Uruguay ist der wichtigste Wasserweg.",
        "In den alten Mythen und Sagen war die Eiche ein heiliger Baum.",
        "In den alten Religionen, Mythen und Sagen war die Eiche ein heiliger Baum.",
        "Zehn Jahre ist es her, seit ich mit achtzehn nach Tokio kam.",
        "Bei den niedrigen Oberflächentemperaturen ist Wassereis hart wie Gestein.",
        "Bei den sehr niedrigen Oberflächentemperaturen ist Wassereis hart wie Gestein.",
        "Die älteste und bekannteste Maßnahme ist die Einrichtung von Schutzgebieten.",
        "Die größte Dortmunder Grünanlage ist der Friedhof.",
        "Die größte Berliner Grünanlage ist der Friedhof.",
        "Die größte Bielefelder Grünanlage ist der Friedhof.",
        "Die Pariser Linie ist hier mit 2,2558 mm gerechnet.",
        "Die Frankfurter Innenstadt ist 7 km entfernt.",
        "Die Dortmunder Konzernzentrale ist ein markantes Gebäude an der Bundesstraße 1.",
        "Die Düsseldorfer Brückenfamilie war ursprünglich ein Sammelbegriff.",
        "Die Düssel ist ein rund 40 Kilometer langer Fluss.",
        "Die Berliner Mauer war während der Teilung Deutschlands die Grenze.",
        "Für amtliche Dokumente und Formulare ist das anders.",
        "Wie viele Kilometer ist ihre Stadt von unserer entfernt?",
        "Über laufende Sanierungsmaßnahmen ist bislang nichts bekannt.",
        "In den letzten zwei Monate war ich fleißig wie eine Biene.",
        "Durch Einsatz größerer Maschinen und bessere Kapazitätsplanung ist die Zahl der Flüge gestiegen.",
        "Die hohe Zahl dieser relativ kleinen Verwaltungseinheiten ist immer wieder Gegenstand von Diskussionen.",
        "Teil der ausgestellten Bestände ist auch die Bierdeckel-Sammlung.",
        "Teil der umfangreichen dort ausgestellten Bestände ist auch die Bierdeckel-Sammlung.",
        "Teil der dort ausgestellten Bestände ist auch die Bierdeckel-Sammlung.",
        "Der zweite Teil dieses Buches ist in England angesiedelt.",
        "Eine der am meisten verbreiteten Krankheiten ist die Diagnose",
        "Eine der verbreitetsten Krankheiten ist hier.",
        "Die Krankheit unserer heutigen Städte und Siedlungen ist folgendes.",
        "Die darauffolgenden Jahre war er ...",
        "Die letzten zwei Monate war ich fleißig wie eine Biene.",
        "Bei sehr guten Beobachtungsbedingungen ist zu erkennen, dass ...",
        "Die beste Rache für Undank und schlechte Manieren ist Höflichkeit.",
        "Ein Gramm Pfeffer war früher wertvoll.",
        "Die größte Stuttgarter Grünanlage ist der Friedhof.",
        "Mancher will Meister sein und ist kein Lehrjunge gewesen.",
        "Ellen war vom Schock ganz bleich.",  // Ellen: auch Plural von Elle
        "Nun gut, die Nacht ist sehr lang, oder?",
        "Der Morgen ist angebrochen, die lange Nacht ist vorüber.",
        "Die stabilste und häufigste Oxidationsstufe ist dabei −1.",
        "Man kann nicht eindeutig zuordnen, wer Täter und wer Opfer war.",
        "Ich schätze, die Batterie ist leer.",
        "Der größte und schönste Tempel eines Menschen ist in ihm selbst.",
        "Begehe keine Dummheit zweimal, die Auswahl ist doch groß genug!",
        "Seine größte und erfolgreichste Erfindung war die Säule.",
        "Egal was du sagst, die Antwort ist Nein.",
        "... in der Geschichte des Museums, die Sammlung ist seit 2011 zugänglich.",
        "Deren Bestimmung und Funktion ist allerdings nicht so klar.",
        "Sie hat eine Tochter, die Pianistin ist.",
        "Ja, die Milch ist sehr gut.",
        "Der als Befestigung gedachte östliche Teil der Burg ist weitgehend verfallen.",
        "Das Kopieren und Einfügen ist sehr nützlich.",
        "Der letzte der vier großen Flüsse ist die Kolyma.",
        "In christlichen, islamischen und jüdischen Traditionen ist das höchste Ziel der meditativen Praxis.",
        "Der Autor der beiden Spielbücher war Markus Heitz selbst.",
        "Der Autor der ersten beiden Spielbücher war Markus Heitz selbst.",
        "Das Ziel der elf neuen Vorstandmitglieder ist klar definiert.",
        "Laut den meisten Quellen ist das Seitenverhältnis der Nationalflagge...",
        "Seine Novelle, die eigentlich eine Glosse ist, war toll.",
        "Für in Österreich lebende Afrikaner und Afrikanerinnen ist dies nicht üblich.",
        "Von ursprünglich drei Almhütten ist noch eine erhalten.",
        "Einer seiner bedeutendsten Kämpfe war gegen den späteren Weltmeister.",
        "Aufgrund stark schwankender Absatzmärkte war die GEFA-Flug Mitte der 90er Jahre gezwungen, ...",
        "Der Abzug der Besatzungssoldaten und deren mittlerweile ansässigen Angehörigen der Besatzungsmächte war vereinbart.",
        "Das Bündnis zwischen der Sowjetunion und Kuba war für beide vorteilhaft.",
        "Knapp acht Monate ist die Niederlage nun her.",
        "Vier Monate ist die Niederlage nun her.",
        "Sie liebt Kunst und Kunst war auch kein Problem, denn er würde das Geld zurückkriegen.",
        "Bei komplexen und andauernden Störungen ist der Stress-Stoffwechsel des Hundes entgleist.",
        "Eltern ist der bisherige Kita-Öffnungsplan zu unkonkret",
        "Einer der bedeutendsten Māori-Autoren der Gegenwart ist Witi Ihimaera.",
        "Start und Ziel ist Innsbruck",
        "Anfänger wie auch Fortgeschrittene sind herzlich willkommen!",
        "Die Aussichten für Japans Zukunft sind düster.",
        "Das Angeln an Mallorcas Felsküsten ist überaus Erfolg versprechend.",
        "Das bedeutendste Bauwerk und Wahrzeichen der Stadt ist die ehemalige Klosterkirche des Klosters Hofen.",
        "Das saisonale Obst und Gemüse ist köstlich und oft deutlich günstiger als in der Stadt.",
        "Gründer und Leiter des Zentrums ist der Rabbiner Marvin Hier, sein Stellvertreter ist Rabbi Abraham Cooper."
    );
    for (String sentence : sentences) {
      assertGood(sentence);
    }
  }

  @Test
  public void testRuleWithIncorrectPluralVerb() throws IOException {
    List<String> sentences = Arrays.asList(
        "Die Katze sind schön.",
        "Die Katze waren schön.",
        "Der Text sind gut.",
        "Das Auto sind schnell.",
        //"Herr Müller sind alt." -- Müller has a plural reading
        "Herr Schröder sind alt.",
        "Julia und Karsten ist alt.",
        "Julia, Heike und Karsten ist alt.",
        "Herr Karsten Schröder sind alt."
        //"Die heute bekannten Bonsai sind häufig im japanischen Stil gestaltet."  // plural: Bonsais (laut Duden) - sollte von AgreementRule gefunden werden
   );
    for (String sentence : sentences) {
      assertBad(sentence);
    }
  }

  @Test
  public void testRuleWithCorrectPluralVerb() throws IOException {
    List<String> sentences = Arrays.asList(
        "Glaubt wirklich jemand, dass gute Fotos keine Arbeit sind?",
        "Zwei Schülern war aufgefallen, dass man im Fernsehen dazu nichts mehr sieht.",
        "Auch die Reste eines sehr großen Insektenfressers sind unter den Fossilien.",
        "Eine Persönlichkeit sind Sie selbst.",
        "Die Katzen sind schön.",
        "Frau Meier und Herr Müller sind alt.",
        "Frau Julia Meier und Herr Karsten Müller sind alt.",
        "Julia und Karsten sind alt.",
        "Julia, Heike und Karsten sind alt.",
        "Frau und Herr Müller sind alt.",
        "Herr und Frau Schröder sind alt.",
        "Herr Meier und Frau Schröder sind alt.",
        "Die restlichen 86 Prozent sind in der Flasche.",
        "Die restlichen sechsundachtzig Prozent sind in der Flasche.",
        "Die restlichen 86 oder 87 Prozent sind in der Flasche.",
        "Die restlichen 86 % sind in der Flasche.",
        "Durch den schnellen Zerfall des Actiniums waren stets nur geringe Mengen verfügbar.",
        "Soda und Anilin waren die ersten Produkte des Unternehmens.",
        "Bob und Tom sind Brüder.",
        "Letztes Jahr sind wir nach London gegangen.",
        "Trotz des Regens sind die Kinder in die Schule gegangen.",
        "Die Zielgruppe sind Männer.",
        "Männer sind die Zielgruppe.",
        "Die Zielgruppe sind meist junge Erwachsene.",
        "Die USA sind ein repräsentativer demokratischer Staat.",
        "Wesentliche Eigenschaften der Hülle sind oben beschrieben.",
        "Wesentliche Eigenschaften der Hülle sind oben unter Quantenmechanische Atommodelle und Erklärung grundlegender Atomeigenschaften dargestellt.",
        "Er und seine Schwester sind eingeladen.",
        "Er und seine Schwester sind zur Party eingeladen.",
        "Sowohl er als auch seine Schwester sind zur Party eingeladen.",
        "Rekonstruktionen oder der Wiederaufbau sind wissenschaftlich sehr umstritten.",
        "Form und Materie eines Einzeldings sind aber nicht zwei verschiedene Objekte.",
        "Dieses Jahr sind die Birnen groß.",
        "Es so umzugestalten, dass sie wie ein Spiel sind.",
        "Die Zielgruppe sind meist junge Erwachsene.",
        "Die Ursache eines Hauses sind so Ziegel und Holz.",
        "Vertreter dieses Ansatzes sind unter anderem Roth und Meyer.",
        "Sowohl sein Vater als auch seine Mutter sind tot.",
        "Einige der Inhaltsstoffe sind schädlich.",
        "Diese Woche sind wir schon einen großen Schritt weiter.",
        "Diese Woche sind sie hier.",
        "Vorsitzende des Vereins waren:",
        "Weder Gerechtigkeit noch Freiheit sind möglich, wenn nur das Geld regiert.",
        "Ein typisches Beispiel sind Birkenpollenallergene.",
        "Eine weitere Variante sind die Miniatur-Wohnlandschaften.",
        "Eine Menge englischer Wörter sind aus dem Lateinischen abgeleitet.",
        "Völkerrechtlich umstrittenes Territorium sind die Falklandinseln.",
        "Einige dieser älteren Synthesen sind wegen geringer Ausbeuten ...",
        "Einzelne Atome sind klein.",
        "Die Haare dieses Jungens sind schwarz.",
        "Die wichtigsten Mechanismen des Aminosäurenabbaus sind:",
        "Wasserlösliche Bariumverbindungen sind giftig.",
        "Die Schweizer Trinkweise ist dabei die am wenigsten etablierte.",
        "Die Anordnung der vier Achsen ist damit identisch.",
        "Die Nauheimer Musiktage, die immer wieder ein kultureller Höhepunkt sind.",
        "Räumliche und zeitliche Abstände sowie die Trägheit sind vom Bewegungszustand abhängig.",
        "Solche Gewerbe sowie der Karosseriebau sind traditionell stark vertreten.",
        "Hundert Dollar sind doch gar nichts!",
        "Sowohl Tom als auch Maria waren überrascht.",
        "Robben, die die hauptsächliche Beute der Eisbären sind.",
        "Die Albatrosse sind eine Gruppe von Seevögeln",
        "Die Albatrosse sind eine Gruppe von großen Seevögeln",
        "Die Albatrosse sind eine Gruppe von großen bis sehr großen Seevögeln",
        "Vier Elemente, welche der Urstoff aller Körper sind.",
        "Die Beziehungen zwischen Kanada und dem Iran sind seitdem abgebrochen.",
        "Die diplomatischen Beziehungen zwischen Kanada und dem Iran sind seitdem abgebrochen.",
        "Die letzten zehn Jahre seines Lebens war er erblindet.",
        "Die letzten zehn Jahre war er erblindet.",
        "... so dass Knochenbrüche und Platzwunden die Regel sind.",
        "Die Eigentumsverhältnisse an der Gesellschaft sind unverändert geblieben.",
        "Gegenstand der Definition sind für ihn die Urbilder.",
        "Mindestens zwanzig Häuser sind abgebrannt.",
        "Sie hielten geheim, dass sie Geliebte waren.",
        "Einige waren verspätet.",
        "Kommentare, Korrekturen und Kritik sind verboten.",
        "Kommentare, Korrekturen, Kritik sind verboten.",
        "Letztere sind wichtig, um die Datensicherheit zu garantieren.",
        "Jüngere sind oft davon überzeugt, im Recht zu sein.",
        "Verwandte sind selten mehr als Bekannte.",
        "Ursache waren die hohe Arbeitslosigkeit und die Wohnungsnot.",
        "Ursache waren unter anderem die hohe Arbeitslosigkeit und die Wohnungsnot.", 
        "Er ahnt nicht, dass sie und sein Sohn ein Paar sind.",
        "Die Ursachen der vorliegenden Durchblutungsstörung sind noch unbekannt.",
        "Der See und das Marschland sind ein Naturschutzgebiet",
        "Details, Dialoge, wie auch die Typologie der Charaktere sind frei erfunden.",
        "Die internen Ermittler und auch die Staatsanwaltschaft sind nun am Zug.",
        "Sie sind so erfolgreich, weil sie eine Einheit sind.",
        "Auch Polizisten zu Fuß sind unterwegs.",
        "Julia sagte, dass Vater und Mutter zu Hause sind.",
        "Damit müssen sie zurechtkommen, wenn Kinder zu Hause sind.",
        "Die Züge vor Ort sind nicht klimatisiert."
    );
    for (String sentence : sentences) {
      assertGood(sentence);
    }
  }

  @Test
  public void testRuleWithCorrectSingularAndPluralVerb() throws IOException {
    // Manchmal sind beide Varianten korrekt:
    // siehe https://dict.leo.org/grammatik/deutsch/Wort/Verb/Kategorien/Numerus-Person/ProblemNum.html
    List<String> sentences = Arrays.asList(
        "Solchen Personen ist der Zugriff auf diese Daten verboten.",
        "Personen ist der Zugriff auf diese Daten verboten.",
        "So mancher Mitarbeiter und manche Führungskraft ist im Urlaub.",
        "So mancher Mitarbeiter und manche Führungskraft sind im Urlaub.",
        "Jeder Schüler und jede Schülerin ist mal schlecht gelaunt.",
        "Jeder Schüler und jede Schülerin sind mal schlecht gelaunt.",
        "Kaum mehr als vier Prozent der Fläche ist für landwirtschaftliche Nutzung geeignet.",
        "Kaum mehr als vier Prozent der Fläche sind für landwirtschaftliche Nutzung geeignet.",
        "Kaum mehr als vier Millionen Euro des Haushalts ist verplant.",
        "Kaum mehr als vier Millionen Euro des Haushalts sind verplant.",
        "80 Cent ist nicht genug.",   // ugs.
        "80 Cent sind nicht genug.",
        "1,5 Pfund ist nicht genug.",  // ugs.
        "1,5 Pfund sind nicht genug.",
        "Hier ist sowohl Anhalten wie Parken verboten.",
        "Hier sind sowohl Anhalten wie Parken verboten."
    );
    for (String sentence : sentences) {
      assertGood(sentence);
    }
  }

  private void assertGood(String input) throws IOException {
    RuleMatch[] matches = getMatches(input);
    if (matches.length != 0) {
      fail("Got unexpected match(es) for '" + input + "': " + Arrays.toString(matches), input);
    }
  }

  private void assertBad(String input) throws IOException {
    int matchCount = getMatches(input).length;
    if (matchCount == 0) {
      fail("Did not get the expected match for '" + input + "'", input);
    }
  }

  private void fail(String message, String input) throws IOException {
    if (!GermanChunker.isDebug()) {
      GermanChunker.setDebug(true);
      getMatches(input);  // run again with debug mode
    }
    Assert.fail(message);
  }

  private RuleMatch[] getMatches(String input) throws IOException {
    return rule.match(lt.getAnalyzedSentence(input));
  }

}
