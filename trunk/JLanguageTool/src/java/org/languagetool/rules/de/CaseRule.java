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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.de.AnalyzedGermanToken;
import org.languagetool.tagging.de.AnalyzedGermanTokenReadings;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.de.GermanToken;
import org.languagetool.tagging.de.GermanToken.POSType;
import org.languagetool.tools.StringTools;

/**
 * Check that adjectives and verbs are not written with an uppercase
 * first letter (except at the start of a sentence) and cases
 * like this: Das laufen fällt mir leicht. (laufen needs to be uppercased).
 *
 * @author Daniel Naber
 */
public class CaseRule extends GermanRule {

  private final GermanTagger tagger = (GermanTagger) Language.GERMAN.getTagger();

  // Wenn hinter diesen Wörtern ein Verb steht, ist es wohl ein substantiviertes Verb,
  // muss also großgeschrieben werden:
  private static final Set<String> nounIndicators = new HashSet<String>();
  static {
    nounIndicators.add("das");
    nounIndicators.add("sein");
    //indicator.add("seines");    // TODO: ?
    //nounIndicators.add("ihr");    // would cause false alarm e.g. "Auf ihr stehen die Ruinen..."
    nounIndicators.add("mein");
    nounIndicators.add("dein");
    nounIndicators.add("euer");
    //indicator.add("ihres");
    //indicator.add("ihren");
  }

  private static final Set<String> sentenceStartExceptions = new HashSet<String>();
  static {
    sentenceStartExceptions.add("(");
    sentenceStartExceptions.add(":");
    sentenceStartExceptions.add("\"");
    sentenceStartExceptions.add("'");
    sentenceStartExceptions.add("„");
    sentenceStartExceptions.add("“");
    sentenceStartExceptions.add("«");
    sentenceStartExceptions.add("»");
  }

  private static final Set<String> exceptions = new HashSet<String>();
  static {

    exceptions.add("Abends");
    exceptions.add("Abfahrt");
    exceptions.add("Abfuhr");
    exceptions.add("Abgeordnete");
    exceptions.add("Abgeordneten");
    exceptions.add("Abgeordneter");
    exceptions.add("Abriss");
    exceptions.add("Absage");
    exceptions.add("Absagen");
    exceptions.add("Abschnitt");
    exceptions.add("Abschnitten");
    exceptions.add("Absteige");
    exceptions.add("Abstieg");
    exceptions.add("Abstiegen");
    exceptions.add("Achtern");
    exceptions.add("Ahne");
    exceptions.add("Ahnen");
    exceptions.add("Ähnlichem");
    exceptions.add("Ähnliches");   // je nach Kontext groß (TODO), z.B. "Er hat Ähnliches erlebt"
    exceptions.add("Allerlei");
    exceptions.add("Alter");
    exceptions.add("Alters");
    exceptions.add("Angriff");
    exceptions.add("Angriffe");
    exceptions.add("Angriffen");
    exceptions.add("Angst");
    exceptions.add("Anklang");
    exceptions.add("Anrufe");
    exceptions.add("Anrufen");
    exceptions.add("Ansage");
    exceptions.add("Ansagen");
    exceptions.add("Anschnitt");
    exceptions.add("Anschnitte");
    exceptions.add("Ansinnen");
    exceptions.add("Anstieg");
    exceptions.add("Anstiegen");
    exceptions.add("Anstrich");
    exceptions.add("Antike");
    exceptions.add("Antonym");
    exceptions.add("Antonyme");
    exceptions.add("Antonymen");
    exceptions.add("Antrieb");
    exceptions.add("Antriebe");
    exceptions.add("Antrieben");
    exceptions.add("Appellativ");
    exceptions.add("Appellativen");
    exceptions.add("Arabeske");
    exceptions.add("Arabesken");
    exceptions.add("Ärger");
    exceptions.add("Arm");
    exceptions.add("Arme");
    exceptions.add("Armen");
    exceptions.add("Armes");
    exceptions.add("Äste");
    exceptions.add("Ästen");
    exceptions.add("Aufriss");
    exceptions.add("Aufschnitt");
    exceptions.add("Aufstand");
    exceptions.add("Aufstieg");
    exceptions.add("Auftritt");
    exceptions.add("Aus");    // "vor dem Aus stehen"
    exceptions.add("Ausdrücke");
    exceptions.add("Ausdrücken");
    exceptions.add("Ausfuhr");
    exceptions.add("Ausgaben");
    exceptions.add("Auslagen");
    exceptions.add("Ausmaßen");
    exceptions.add("Ausritt");
    exceptions.add("Ausritte");
    exceptions.add("Ausritten");
    exceptions.add("Aussage");
    exceptions.add("Aussagen");
    exceptions.add("Ausschnitt");
    exceptions.add("Ausschnitte");
    exceptions.add("Ausschnitten");
    exceptions.add("Ausstieg");
    exceptions.add("Austritt");
    exceptions.add("Auswüchsen");
    exceptions.add("Baden");
    exceptions.add("Bahnen");
    exceptions.add("Bände");
    exceptions.add("Bänden");
    exceptions.add("Bau");
    exceptions.add("Bäume");
    exceptions.add("Bäumen");
    exceptions.add("Bauten");
    exceptions.add("Beauftragte");
    exceptions.add("Beauftragten");
    exceptions.add("Beauftragter");
    exceptions.add("Bechern");
    exceptions.add("Bedenken");
    exceptions.add("Befehle");
    exceptions.add("Befehlen");
    exceptions.add("Befunden");
    exceptions.add("Beilagen");
    exceptions.add("Beistand");
    exceptions.add("Beitritt");
    exceptions.add("Belang");
    exceptions.add("Belange");
    exceptions.add("Belangen");
    exceptions.add("Berge");
    exceptions.add("Bergen");
    exceptions.add("Berufe");
    exceptions.add("Berufen");
    exceptions.add("Bescheid");
    exceptions.add("Bescheide");
    exceptions.add("Bescheiden");
    exceptions.add("besonderes");   // je nach Kontext groß (TODO): "etwas Besonderes"
    exceptions.add("Bestand");
    exceptions.add("Betrieb");
    exceptions.add("Betten");
    exceptions.add("Beute");
    exceptions.add("Biss");
    exceptions.add("Bissen");
    exceptions.add("Blase");
    exceptions.add("Blasen");
    exceptions.add("Blick");
    exceptions.add("Blicken");
    exceptions.add("Block");
    exceptions.add("Brauch");
    exceptions.add("Breite");
    exceptions.add("Bremse");
    exceptions.add("Bremsen");
    exceptions.add("Brüste");
    exceptions.add("Brüsten");
    exceptions.add("Buche");
    exceptions.add("Buchen");
    exceptions.add("Bündel");
    exceptions.add("Bündeln");
    exceptions.add("Bürde");
    exceptions.add("Bürden");
    exceptions.add("Bürgern");
    exceptions.add("Burleske");
    exceptions.add("Bürste");
    exceptions.add("Bürsten");
    exceptions.add("Creme");
    exceptions.add("Dank");
    exceptions.add("Dauertest");
    exceptions.add("De");    // "De Morgan" etc.
    exceptions.add("Deckeln");
    exceptions.add("Delinquent");
    exceptions.add("Delinquenten");
    exceptions.add("Delta");
    exceptions.add("Dichte");
    exceptions.add("Dichter");
    exceptions.add("Differential");
    exceptions.add("Diminutiv");
    exceptions.add("Diminutive");
    exceptions.add("Diminutiven");
    exceptions.add("Diminutives");
    exceptions.add("Dingen");
    exceptions.add("Double");
    exceptions.add("Dr");
    exceptions.add("Dreißigern");
    exceptions.add("Drittel");
    exceptions.add("Druck");
    exceptions.add("Durcheinander");
    exceptions.add("Durchschnitt");
    exceptions.add("Durchtritt");
    exceptions.add("Düse");
    exceptions.add("Düsen");
    exceptions.add("Ebbe");
    exceptions.add("Ebben");
    exceptions.add("Eck");
    exceptions.add("Ecke");
    exceptions.add("Ecken");
    exceptions.add("Ehe");
    exceptions.add("Ehre");
    exceptions.add("Ehren");
    exceptions.add("Eichen");
    exceptions.add("Eile");
    exceptions.add("Einband");
    exceptions.add("Eindrücke");
    exceptions.add("Eindrücken");
    exceptions.add("Eingaben");
    exceptions.add("Eingriff");
    exceptions.add("Eingriffen");
    exceptions.add("Einreise");
    exceptions.add("Einreisen");
    exceptions.add("Einschnitt");
    exceptions.add("Einschnitten");
    exceptions.add("Eislaufen");
    exceptions.add("Elend");
    exceptions.add("Ende");
    exceptions.add("Erlöse");
    exceptions.add("Erlösen");
    exceptions.add("Ernst");
    exceptions.add("Ernstes");
    exceptions.add("Ertrag");
    exceptions.add("Erwachsene");
    exceptions.add("Erwachsenen");
    exceptions.add("Erwachsener");
    exceptions.add("Essen");
    exceptions.add("Fabeln");
    exceptions.add("Fach");
    exceptions.add("Fächern");
    exceptions.add("Faches");
    exceptions.add("Faden");
    exceptions.add("Fahrt");
    exceptions.add("Fall");
    exceptions.add("Falle");
    exceptions.add("Fallen");
    exceptions.add("Fälle");
    exceptions.add("Fällen");
    exceptions.add("Falte");
    exceptions.add("Falten");
    exceptions.add("Feile");
    exceptions.add("Feind");
    exceptions.add("Ferne");
    exceptions.add("Fest");
    exceptions.add("Feste");
    exceptions.add("Festen");
    exceptions.add("Fett");
    exceptions.add("Fette");
    exceptions.add("Fetten");
    exceptions.add("Fiedeln");
    exceptions.add("Filme");
    exceptions.add("Filmen");
    exceptions.add("Filz");
    exceptions.add("Filze");
    exceptions.add("Fingern");
    exceptions.add("Fische");
    exceptions.add("Fischen");
    exceptions.add("Flanken");
    exceptions.add("Flaute");    // 'flaute ab'
    exceptions.add("Flauten");
    exceptions.add("Fliege");
    exceptions.add("Fliegen");
    exceptions.add("Fliese");
    exceptions.add("Fliesen");
    exceptions.add("Flöße");
    exceptions.add("Flößen");
    exceptions.add("Flöte");
    exceptions.add("Flöten");
    exceptions.add("Flotte");
    exceptions.add("Flotten");
    exceptions.add("Flucht");    // 'er flucht auf der Flucht'
    exceptions.add("Folge");
    exceptions.add("Folgen");
    exceptions.add("Folgendes");   // je nach Kontext groß (TODO)...
    exceptions.add("Forscher");    // die forschen Forscher forschen forsch
    exceptions.add("Fort");
    exceptions.add("Fortschritt");
    exceptions.add("Fortschritten");
    exceptions.add("Frage");
    exceptions.add("Fragen");
    exceptions.add("Franse");
    exceptions.add("Fransen");
    exceptions.add("Fraß");
    exceptions.add("Fremde");
    exceptions.add("Fresse");
    exceptions.add("Freunde");
    exceptions.add("Freunden");
    exceptions.add("Frühbarock");
    exceptions.add("Frühjahrs");
    exceptions.add("Fungizid");
    exceptions.add("Fungizide");
    exceptions.add("Fungiziden");
    exceptions.add("Furniere");
    exceptions.add("Furnieren");
    exceptions.add("Geboten");
    exceptions.add("Gebrechen");
    exceptions.add("Gefreite");
    exceptions.add("Gefreiten");
    exceptions.add("Gefreiter");
    exceptions.add("Geige");
    exceptions.add("Geigen");
    exceptions.add("Gemach");
    exceptions.add("Genüge");
    exceptions.add("Gerät");
    exceptions.add("Gewissen");
    exceptions.add("Gier");
    exceptions.add("Gläubiger");
    exceptions.add("Gleichstand");
    exceptions.add("Goldene");
    exceptions.add("Goldener");    // Goldener Schnitt
    exceptions.add("Graben");
    exceptions.add("Grade");
    exceptions.add("Gram");
    exceptions.add("Greise");
    exceptions.add("Greisen");
    exceptions.add("Grenze");
    exceptions.add("Grenzen");
    exceptions.add("Große");    // Alexander der Große, der Große Bär
    exceptions.add("Großen");
    exceptions.add("Großtat");
    exceptions.add("Großtaten");
    exceptions.add("Großteils");
    exceptions.add("Gruben");
    exceptions.add("Gründe");
    exceptions.add("Gründen");
    exceptions.add("Grüße");
    exceptions.add("Grüßen");
    exceptions.add("Gutachten");
    exceptions.add("Guten");    // das Kap der Guten Hoffnung
    exceptions.add("Hacke");
    exceptions.add("Hacken");
    exceptions.add("Haken");
    exceptions.add("Halbtotale");
    exceptions.add("Halbtotalen");
    exceptions.add("Halle");
    exceptions.add("Hallen");
    exceptions.add("Hamstern");
    exceptions.add("Hanteln");
    exceptions.add("Härte");
    exceptions.add("Härten");
    exceptions.add("Hechte");
    exceptions.add("Hechten");   // Fische
    exceptions.add("Hecke");
    exceptions.add("Hecken");    // 'was hecken die da aus?'
    exceptions.add("Heimfahrt");
    exceptions.add("Helvetica");
    exceptions.add("Herbst");
    exceptions.add("Herfahrt");
    exceptions.add("Herzen");
    exceptions.add("Herzöge");
    exceptions.add("Herzögen");
    exceptions.add("Hexe");
    exceptions.add("Hexen");
    exceptions.add("Hieb");
    exceptions.add("Hiebe");
    exceptions.add("Hieben");
    exceptions.add("Hinfahrt");
    exceptions.add("Hobel");
    exceptions.add("Hobeln");
    exceptions.add("Höhle");
    exceptions.add("Höhlen");
    exceptions.add("Humanoide");
    exceptions.add("Humanoiden");
    exceptions.add("Hundert");   // je nach Kontext groß (TODO)
    exceptions.add("Hungers");
    exceptions.add("Ihnen");
    exceptions.add("Ihr");
    exceptions.add("Ihre");
    exceptions.add("Ihrem");
    exceptions.add("Ihren");
    exceptions.add("Ihrer");
    exceptions.add("Ihres");
    exceptions.add("Illustrierte");
    exceptions.add("Illustrierten");
    exceptions.add("Infrarot");
    exceptions.add("Initiale");
    exceptions.add("Initialen");
    exceptions.add("Intrigant");
    exceptions.add("Intriganten");
    exceptions.add("Invalide");
    exceptions.add("Invaliden");
    exceptions.add("Invalider");
    exceptions.add("Jenseits");
    exceptions.add("Jugendliche");
    exceptions.add("Jugendlichen");
    exceptions.add("Jugendlicher");
    exceptions.add("Junge");
    exceptions.add("Jungen");
    exceptions.add("Jünger");
    exceptions.add("Kabeln");
    exceptions.add("Kante");
    exceptions.add("Kanten");
    exceptions.add("Kapern");
    exceptions.add("Kapital");
    exceptions.add("Kappe");
    exceptions.add("Kappen");
    exceptions.add("Karre");
    exceptions.add("Karren");
    exceptions.add("Käse");
    exceptions.add("Kästen");
    exceptions.add("Kegel");
    exceptions.add("Kegeln");
    exceptions.add("Kehle");
    exceptions.add("Kehlen");
    exceptions.add("Kellern");
    exceptions.add("Kellnern");
    exceptions.add("Kesseln");
    exceptions.add("Klage");
    exceptions.add("Klagen");
    exceptions.add("Klammer");
    exceptions.add("Klammern");
    exceptions.add("Klang");
    exceptions.add("Klau");
    exceptions.add("Klaue");    // die Klaue
    exceptions.add("Klauen");
    exceptions.add("Kleine");    // der Kleine Bär
    exceptions.add("Klinge");
    exceptions.add("Klingel");
    exceptions.add("Klingen");
    exceptions.add("Knebel");
    exceptions.add("Knebeln");
    exceptions.add("Knick");
    exceptions.add("Knicken");
    exceptions.add("Knöpfe");    // die knöpfe ich mir vor, die Knöpfe
    exceptions.add("Knöpfen");
    exceptions.add("Knospen");
    exceptions.add("Knoten");
    exceptions.add("Koch");
    exceptions.add("Kommode");
    exceptions.add("Komparativ");
    exceptions.add("Komparative");
    exceptions.add("Konditional");
    exceptions.add("Konditionale");
    exceptions.add("Köpfe");
    exceptions.add("Köpfen");
    exceptions.add("Kosten");   // die Kosten sind sehr hoch
    exceptions.add("Kraft");    // kraft meines Amtes
    exceptions.add("Kragen");
    exceptions.add("Krähe");    // "die Krähen krähen nicht"
    exceptions.add("Krähen");
    exceptions.add("Kralle");
    exceptions.add("Krallen");
    exceptions.add("Krebse");
    exceptions.add("Krebsen");
    exceptions.add("Kreide");    // "das mit der Kreide kreide ich dir nicht an"
    exceptions.add("Kreiseln");
    exceptions.add("Kreise");   // "störe meine Kreise nicht"
    exceptions.add("Kreisen");
    exceptions.add("Kreuz");
    exceptions.add("Kreuze");
    exceptions.add("Kreuzen");
    exceptions.add("Kriege");
    exceptions.add("Kriegen");
    exceptions.add("Kugeln");
    exceptions.add("Kuppeln");
    exceptions.add("Kürze");
    exceptions.add("Kurzem");
    exceptions.add("Kutsche");
    exceptions.add("Kutschen");
    exceptions.add("Laden");
    exceptions.add("Langem");
    exceptions.add("Längerem");
    exceptions.add("Lappen");
    exceptions.add("Las");   // Las Vegas, nicht "lesen"
    exceptions.add("Lauf");
    exceptions.add("Laut");
    exceptions.add("Laute");
    exceptions.add("Lauten");
    exceptions.add("Le");    // "Le Monde" etc
    exceptions.add("Leben");
    exceptions.add("Leck");
    exceptions.add("Leere");
    exceptions.add("Legende");
    exceptions.add("Legenden");
    exceptions.add("Lehre");
    exceptions.add("Lehren");
    exceptions.add("Leid");
    exceptions.add("Leiste");
    exceptions.add("Leisten");
    exceptions.add("Letzt");      // "zu guter Letzt"
    exceptions.add("Letztere");
    exceptions.add("Letzterer");
    exceptions.add("Letzteres");
    exceptions.add("Leuchte");
    exceptions.add("Leuchten");
    exceptions.add("Licht");
    exceptions.add("Lichter");
    exceptions.add("Liebe");
    exceptions.add("Liege");
    exceptions.add("Liegen");
    exceptions.add("Link");
    exceptions.add("Links");
    exceptions.add("Liste");
    exceptions.add("Listen");
    exceptions.add("Löcher");
    exceptions.add("Löchern");
    exceptions.add("Löhne");
    exceptions.add("Los");
    exceptions.add("Lose");
    exceptions.add("Losen");
    exceptions.add("Loses");
    exceptions.add("Luden");
    exceptions.add("Lüge");
    exceptions.add("Lügen");
    exceptions.add("Lumpen");
    exceptions.add("Macht");
    exceptions.add("Mal");
    exceptions.add("Manifest");
    exceptions.add("Manifeste");
    exceptions.add("Manifesten");
    exceptions.add("Manifestes");
    exceptions.add("Marine");
    exceptions.add("Marinen");
    exceptions.add("Maß");
    exceptions.add("Maßen");
    exceptions.add("Matte");
    exceptions.add("Matten");
    exceptions.add("Miss");
    exceptions.add("Mitfahrt");
    exceptions.add("Mitschnitt");
    exceptions.add("Mitschnitten");
    exceptions.add("Mittags");
    exceptions.add("Mittelalter");
    exceptions.add("Morde");
    exceptions.add("Morden");
    exceptions.add("Morgen");
    exceptions.add("Morgens");
    exceptions.add("Mr");
    exceptions.add("Mrd");
    exceptions.add("Mrs");
    exceptions.add("Mühe");
    exceptions.add("Mühen");
    exceptions.add("Münze");
    exceptions.add("Münzen");
    exceptions.add("Nachfrage");
    exceptions.add("Nachfragen");
    exceptions.add("Nachkomme");
    exceptions.add("Nachkommen");
    exceptions.add("Nachmittags");
    exceptions.add("Nachts");   // "des Nachts", "eines Nachts"
    exceptions.add("Nachwuchs");
    exceptions.add("Nähe");
    exceptions.add("Naht");
    exceptions.add("Nähte");
    exceptions.add("Nähten");
    exceptions.add("Namens");
    exceptions.add("Neubarock");
    exceptions.add("Neuem");
    exceptions.add("Norden");
    exceptions.add("Notfalls");
    exceptions.add("Nr");
    exceptions.add("Nudeln");
    exceptions.add("Nutzen");
    exceptions.add("Obdachlose");
    exceptions.add("Obdachlosen");
    exceptions.add("Obdachloser");
    exceptions.add("Oder");   // der Fluss
    exceptions.add("Offensive");
    exceptions.add("Orakeln");
    exceptions.add("Paar");
    exceptions.add("Patent");
    exceptions.add("Patsche");
    exceptions.add("Pauke");
    exceptions.add("Pauken");
    exceptions.add("Pauschale");
    exceptions.add("Pauschalen");
    exceptions.add("Pause");
    exceptions.add("Pausen");
    exceptions.add("Perle");
    exceptions.add("Perlen");
    exceptions.add("Pfeife");
    exceptions.add("Pfeifen");
    exceptions.add("Pfiffe");
    exceptions.add("Pfiffen");
    exceptions.add("Pflichten");
    exceptions.add("Planer");
    exceptions.add("Planes");
    exceptions.add("Platte");
    exceptions.add("Platten");
    exceptions.add("Platz");
    exceptions.add("Pleite");
    exceptions.add("Plural");
    exceptions.add("Plurale");
    exceptions.add("Pluralen");
    exceptions.add("Post");
    exceptions.add("Präsent");
    exceptions.add("Predigt");
    exceptions.add("Predigten");
    exceptions.add("Preis");
    exceptions.add("Preise");
    exceptions.add("Preisen");
    exceptions.add("Presse");
    exceptions.add("Probe");
    exceptions.add("Proben");
    exceptions.add("Prof");
    exceptions.add("Puste");    // da geht dir die Puste aus
    exceptions.add("Quelle");
    exceptions.add("Quellen");
    exceptions.add("Rackern");
    exceptions.add("Rahmen");
    exceptions.add("Ränge");
    exceptions.add("Rängen");
    exceptions.add("Rast");
    exceptions.add("Ratsche");
    exceptions.add("Räume");
    exceptions.add("Räumen");
    exceptions.add("Rausschmiss");
    exceptions.add("Rausschmisse");
    exceptions.add("Rausschmissen");
    exceptions.add("Rechen");
    exceptions.add("Recht");
    exceptions.add("Rechte");
    exceptions.add("Rechten");
    exceptions.add("Rede");
    exceptions.add("Reden");
    exceptions.add("Regeln");
    exceptions.add("Regen");
    exceptions.add("Reich");    // das Reich
    exceptions.add("Reiche");
    exceptions.add("Reichen");
    exceptions.add("Reiches");
    exceptions.add("Reif");
    exceptions.add("Reifen");
    exceptions.add("Reihe");
    exceptions.add("Reise");
    exceptions.add("Reisen");
    exceptions.add("Rentiere");
    exceptions.add("Rentieren");
    exceptions.add("Retroflexe");
    exceptions.add("Retroflexen");
    exceptions.add("Retrospektive");
    exceptions.add("Reue");
    exceptions.add("Riegeln");
    exceptions.add("Ring");
    exceptions.add("Ringe");
    exceptions.add("Ringen");
    exceptions.add("Robbe");
    exceptions.add("Robben");
    exceptions.add("Rolle");
    exceptions.add("Rollen");
    exceptions.add("Rückfrage");
    exceptions.add("Rückfragen");
    exceptions.add("Rüde");
    exceptions.add("Rüden");
    exceptions.add("Ruf");
    exceptions.add("Rufe");
    exceptions.add("Rufen");
    exceptions.add("Rüge");
    exceptions.add("Rügen");
    exceptions.add("Ruhe");
    exceptions.add("Rümpfe");
    exceptions.add("Rümpfen");    // über die Rümpfe rümpfe ich die Nase
    exceptions.add("Runde");
    exceptions.add("Runden");
    exceptions.add("Sachverständige");
    exceptions.add("Sachverständigen");
    exceptions.add("Sachverständiger");
    exceptions.add("Sahne");
    exceptions.add("Samt");     // 'in Samt und Seide' vs. 'samt und sonders'
    exceptions.add("Sankt");
    exceptions.add("Säume");
    exceptions.add("Säumen");
    exceptions.add("Säure");
    exceptions.add("Schächte");
    exceptions.add("Schächten");
    exceptions.add("Schaden");
    exceptions.add("Schal");
    exceptions.add("Schale");
    exceptions.add("Schalen");
    exceptions.add("Schau");
    exceptions.add("Schäume");
    exceptions.add("Scheine");
    exceptions.add("Scheinen");
    exceptions.add("Scheiße");
    exceptions.add("Schere");
    exceptions.add("Scheren");
    exceptions.add("Scherze");
    exceptions.add("Scherzen");
    exceptions.add("Schiefer");
    exceptions.add("Schiene");
    exceptions.add("Schienen");
    exceptions.add("Schippe");
    exceptions.add("Schippen");
    exceptions.add("Schlag");
    exceptions.add("Schlampe");
    exceptions.add("Schlampen");
    exceptions.add("Schleuse");
    exceptions.add("Schleusen");
    exceptions.add("Schlinge");
    exceptions.add("Schlingen");
    exceptions.add("Schmelze");
    exceptions.add("Schmerzen");
    exceptions.add("Schmiede");
    exceptions.add("Schnäbeln");
    exceptions.add("Schnalle");
    exceptions.add("Schnallen");
    exceptions.add("Schnitt");
    exceptions.add("Schnitten");
    exceptions.add("Schnorcheln");
    exceptions.add("Schnupfen");
    exceptions.add("Schock");
    exceptions.add("Schotte");
    exceptions.add("Schotten");
    exceptions.add("Schritt");
    exceptions.add("Schritte");
    exceptions.add("Schritten");
    exceptions.add("Schubs");
    exceptions.add("Schuft");
    exceptions.add("Schufte");
    exceptions.add("Schuften");
    exceptions.add("Schuld");
    exceptions.add("Schulden");
    exceptions.add("Schule");
    exceptions.add("Schulen");
    exceptions.add("Schund");
    exceptions.add("Schürze");
    exceptions.add("Schürzen");
    exceptions.add("Schütze");
    exceptions.add("Schützen");
    exceptions.add("Schwamm");
    exceptions.add("Schwänze");
    exceptions.add("Schwänzen");
    exceptions.add("Schwärme");
    exceptions.add("Schwärmen");
    exceptions.add("Schwarzes");    // Schwarzes Brett
    exceptions.add("Schwebe");    // in der Schwebe
    exceptions.add("Sie");
    exceptions.add("Siebe");
    exceptions.add("Sieben");
    exceptions.add("Siege");
    exceptions.add("Siegen");
    exceptions.add("Sitze");
    exceptions.add("Sitzen");
    exceptions.add("Solo");
    exceptions.add("Sommers");
    exceptions.add("Sorge");
    exceptions.add("Sorgen");
    exceptions.add("Spätantike");
    exceptions.add("Speisen");
    exceptions.add("Spitz");    // Hund
    exceptions.add("Spitze");
    exceptions.add("Spitzen");
    exceptions.add("Spleiße");
    exceptions.add("Splittern");
    exceptions.add("Spritze");
    exceptions.add("Spritzen");
    exceptions.add("St");   // Paris St. Germain
    exceptions.add("Stacheln");
    exceptions.add("Stand");
    exceptions.add("Stände");
    exceptions.add("Ständen");
    exceptions.add("Stärke");
    exceptions.add("Stärken");
    exceptions.add("Stecken");
    exceptions.add("Stelle");
    exceptions.add("Stellen");
    exceptions.add("Stereotyp");
    exceptions.add("Stereotypen");
    exceptions.add("Steuern");
    exceptions.add("Stich");    // Imperativ vs. 'der Stich'
    exceptions.add("Stillschweigen");
    exceptions.add("Stillstand");
    exceptions.add("Stimme");
    exceptions.add("Stimmen");
    exceptions.add("Stolz"); // mein ganzer Stolz
    exceptions.add("Stoß");  // "Stoß zu!"
    exceptions.add("Störe"); // Fische
    exceptions.add("Stören");
    exceptions.add("Strafe");
    exceptions.add("Strafen");
    exceptions.add("Strecke");
    exceptions.add("Strecken");
    exceptions.add("Streiche");
    exceptions.add("Streichen");
    exceptions.add("Strich");
    exceptions.add("Strichen");
    exceptions.add("Strippe");
    exceptions.add("Strippen");
    exceptions.add("Strudeln");
    exceptions.add("Stufe");    // 'ich stufe dich ein' vs. 'die Stufe'
    exceptions.add("Stufen");
    exceptions.add("Stunde");
    exceptions.add("Stunden");
    exceptions.add("Stütze");
    exceptions.add("Stützen");
    exceptions.add("Suche");
    exceptions.add("Sucht");
    exceptions.add("Tage");
    exceptions.add("Tagen");    // Sie tagen seit Tagen.
    exceptions.add("Taste");
    exceptions.add("Tasten");
    exceptions.add("Tat");
    exceptions.add("Taten");
    exceptions.add("Tausend");   // je nach Kontext groß (TODO)
    exceptions.add("Texte");
    exceptions.add("Texten");
    exceptions.add("Textil");
    exceptions.add("Throne");
    exceptions.add("Tote");
    exceptions.add("Toten");
    exceptions.add("Toter");
    exceptions.add("Touren");
    exceptions.add("Träger");
    exceptions.add("Träume");
    exceptions.add("Träumen");
    exceptions.add("Treue");
    exceptions.add("Trieb");
    exceptions.add("Trieben");
    exceptions.add("Tritt");
    exceptions.add("Trotz"); // aller Vernunft zum Trotz
    exceptions.add("tun");   // "Sie müssen das tun"
    exceptions.add("Türke");
    exceptions.add("Türken");
    exceptions.add("Übergriff");
    exceptions.add("Übergriffen");
    exceptions.add("Übrigen");   // je nach Kontext groß (TODO), z.B. "im Übrigen"
    exceptions.add("Ufern");
    exceptions.add("Umriss");
    exceptions.add("Umrissen");
    exceptions.add("Unke");
    exceptions.add("Unterlagen");
    exceptions.add("Unterschied");
    exceptions.add("Unterschieden");
    exceptions.add("Untertan");
    exceptions.add("Unvorhergesehene");
    exceptions.add("Unvorhergesehenen");
    exceptions.add("Unvorhergesehenes");   // je nach Kontext groß (TODO), z.B. "etwas Unvorhergesehenes"
    exceptions.add("Variable");
    exceptions.add("Variablen");
    exceptions.add("Verantwortliche");
    exceptions.add("Verantwortlichen");
    exceptions.add("Verantwortlicher");
    exceptions.add("Verbände");
    exceptions.add("Verbänden");
    exceptions.add("Verdienst");
    exceptions.add("Verlass");
    exceptions.add("Verlauf");
    exceptions.add("Vermerk");
    exceptions.add("Vermerke");
    exceptions.add("Vermerken");
    exceptions.add("Verriss");
    exceptions.add("Vertrauen");
    exceptions.add("Vertrieb");
    exceptions.add("Verwandte");
    exceptions.add("Verwandten");
    exceptions.add("Verwandter");
    exceptions.add("Verzehr");
    exceptions.add("Vielfache");
    exceptions.add("Vielfaches");
    exceptions.add("Virtuose");
    exceptions.add("Virtuosen");
    exceptions.add("Vögeln");
    exceptions.add("Vokal");
    exceptions.add("Vokale");
    exceptions.add("Vokalen");
    exceptions.add("Vorbehalte");
    exceptions.add("Vorbehalten");
    exceptions.add("Vordrucke");
    exceptions.add("Vordrucken");
    exceptions.add("Vorformen");
    exceptions.add("Vorgaben");
    exceptions.add("Vorgriff");
    exceptions.add("Vorgriffe");
    exceptions.add("Vorgriffen");
    exceptions.add("Vorlagen");
    exceptions.add("Vorsitzende");
    exceptions.add("Vorsitzenden");
    exceptions.add("Vorsitzender");
    exceptions.add("Vorständen");
    exceptions.add("Vorwärtsschritt");
    exceptions.add("Vorwärtsschritte");
    exceptions.add("Vorwärtsschritten");
    exceptions.add("Vorwürfe");    // "wenn er mir das nicht immer vorwürfe!"
    exceptions.add("Voten");
    exceptions.add("Wache");
    exceptions.add("Wachen");
    exceptions.add("Wagen");
    exceptions.add("Wand");
    exceptions.add("Waren");
    exceptions.add("Warte");
    exceptions.add("Weg");
    exceptions.add("Wegen");
    exceptions.add("Weide");
    exceptions.add("Weiden");
    exceptions.add("Weihe");
    exceptions.add("Weile");
    exceptions.add("Wein");
    exceptions.add("Weinen");
    exceptions.add("Weise");
    exceptions.add("Weisen");
    exceptions.add("Weitem");
    exceptions.add("Weiteres");
    exceptions.add("Werft");
    exceptions.add("Werte");
    exceptions.add("Werten");
    exceptions.add("Wettrennen");
    exceptions.add("Wettstreite");
    exceptions.add("Wettstreiten");
    exceptions.add("Wicht");
    exceptions.add("Wichtiges");
    exceptions.add("Widerstand");
    exceptions.add("Widerstände");
    exceptions.add("Widerständen");
    exceptions.add("Wiege");
    exceptions.add("Wiese");
    exceptions.add("Wiesen");
    exceptions.add("Wild");
    exceptions.add("Wolle");
    exceptions.add("Wunder");
    exceptions.add("Wunders");
    exceptions.add("Wünsche");
    exceptions.add("Wünschen");
    exceptions.add("Würde");
    exceptions.add("Würden");
    exceptions.add("Würze");
    exceptions.add("Wüste");
    exceptions.add("Wüsten");
    exceptions.add("Zank");
    exceptions.add("Zeche");
    exceptions.add("Zelte");
    exceptions.add("Zelten");
    exceptions.add("Zentrale");
    exceptions.add("Zentralen");
    exceptions.add("Zerfall");
    exceptions.add("Zeter");
    exceptions.add("Zettel");
    exceptions.add("Zetteln");
    exceptions.add("Zeug");
    exceptions.add("Zeuge");
    exceptions.add("Zeugen");
    exceptions.add("Ziel");
    exceptions.add("Ziele");
    exceptions.add("Zielen");
    exceptions.add("Zier");
    exceptions.add("Zimmer");
    exceptions.add("Zimmern");
    exceptions.add("Zirkeln");
    exceptions.add("Zufuhr");
    exceptions.add("Zügel");
    exceptions.add("Zügeln");
    exceptions.add("Zugriff");
    exceptions.add("Zugriffe");
    exceptions.add("Zugriffen");
    exceptions.add("Zusage");
    exceptions.add("Zusagen");
    exceptions.add("Zusammenschnitt");
    exceptions.add("Zusammenschnitte");
    exceptions.add("Zusammenschnitten");
    exceptions.add("Zustand");
    exceptions.add("Zutritt");
    exceptions.add("Zwang");
    exceptions.add("Zwänge");
    exceptions.add("Zwängen");
    exceptions.add("Zweifel");
    exceptions.add("Zweifeln");
    exceptions.add("Zwinge");

    // TODO: alle Sprachen + flektierte Formen
    exceptions.add("Afrikanisch");
    exceptions.add("Altarabisch");
    exceptions.add("Altchinesisch");
    exceptions.add("Altgriechisch");
    exceptions.add("Althochdeutsch");
    exceptions.add("Altpersisch");
    exceptions.add("Amerikanisch");
    exceptions.add("Arabisch");
    exceptions.add("Chinesisch");
    exceptions.add("Dänisch");
    exceptions.add("Deutsch");
    exceptions.add("Englisch");
    exceptions.add("Finnisch");
    exceptions.add("Französisch");
    exceptions.add("Frühneuhochdeutsch");
    exceptions.add("Germanisch");
    exceptions.add("Griechisch");
    exceptions.add("Hocharabisch");
    exceptions.add("Hochchinesisch");
    exceptions.add("Hochdeutsch");
    exceptions.add("Holländisch");
    exceptions.add("Italienisch");
    exceptions.add("Japanisch");
    exceptions.add("Jiddisch");
    exceptions.add("Jugoslawisch");
    exceptions.add("Koreanisch");
    exceptions.add("Kroatisch");
    exceptions.add("Lateinisch");
    exceptions.add("Luxemburgisch");
    exceptions.add("Mittelhochdeutsch");
    exceptions.add("Neuhochdeutsch");
    exceptions.add("Niederländisch");
    exceptions.add("Norwegisch");
    exceptions.add("Persisch");
    exceptions.add("Polnisch");
    exceptions.add("Portugiesisch");
    exceptions.add("Russisch");
    exceptions.add("Schwedisch");
    exceptions.add("Schweizerisch");
    exceptions.add("Serbisch");
    exceptions.add("Serbokroatisch");
    exceptions.add("Slawisch");
    exceptions.add("Spanisch");
    exceptions.add("Tschechisch");
    exceptions.add("Türkisch");
    exceptions.add("Ukrainisch");
    exceptions.add("Ungarisch");
    exceptions.add("Weißrussisch");

    // Änderungen an der Rechtschreibreform 2006 erlauben hier Großschreibung:
    exceptions.add("Dein");
    exceptions.add("Deine");
    exceptions.add("Deinem");
    exceptions.add("Deinen");
    exceptions.add("Deiner");
    exceptions.add("Deines");
    exceptions.add("Dich");
    exceptions.add("Dir");
    exceptions.add("Du");
    exceptions.add("Euch");
    exceptions.add("Euer");
    exceptions.add("Eure");
    exceptions.add("Eurem");
    exceptions.add("Euren");
    exceptions.add("Eures");
  }

  private static final Set<String> myExceptionPhrases = new HashSet<String>();
  static {
    // use proper upper/lowercase spelling here:
    myExceptionPhrases.add("ohne Wenn und Aber");
    myExceptionPhrases.add("Große Koalition");
    myExceptionPhrases.add("Großen Koalition");
    myExceptionPhrases.add("im Großen und Ganzen");
    myExceptionPhrases.add("Im Großen und Ganzen");
    myExceptionPhrases.add("im Guten wie im Schlechten");
    myExceptionPhrases.add("Im Guten wie im Schlechten");
    myExceptionPhrases.add("Russisches Reich");
    myExceptionPhrases.add("Tel Aviv");
    myExceptionPhrases.add("Erster Weltkrieg");
    myExceptionPhrases.add("Ersten Weltkriegs");
    myExceptionPhrases.add("Ersten Weltkrieges");
    myExceptionPhrases.add("Erstem Weltkrieg");
    myExceptionPhrases.add("Zweiter Weltkrieg");
    myExceptionPhrases.add("Zweiten Weltkriegs");
    myExceptionPhrases.add("Zweiten Weltkrieges");
    myExceptionPhrases.add("Zweitem Weltkrieg");
    myExceptionPhrases.add("Auswärtiges Amt");
    myExceptionPhrases.add("Auswärtigen Amt");
    myExceptionPhrases.add("Auswärtigen Amts");
    myExceptionPhrases.add("Auswärtigen Amtes");
    myExceptionPhrases.add("Bürgerliches Gesetzbuch");
    myExceptionPhrases.add("Bürgerlichen Gesetzbuch");
    myExceptionPhrases.add("Bürgerlichen Gesetzbuchs");
    myExceptionPhrases.add("Bürgerlichen Gesetzbuches");
    myExceptionPhrases.add("Haute Couture");
    myExceptionPhrases.add("aus dem Nichts");
    myExceptionPhrases.add("Kleiner Bär");   // das Sternbild
    myExceptionPhrases.add("Zehn Gebote");
    myExceptionPhrases.add("Zehn Geboten");
    myExceptionPhrases.add("Römische Reich Deutscher Nation");
    myExceptionPhrases.add("Römischen Reich Deutscher Nation");
    myExceptionPhrases.add("Römischen Reiches Deutscher Nation");
    myExceptionPhrases.add("Für und Wider");
  }

  private static final Set<String> substVerbenExceptions = new HashSet<String>();
  static {
    substVerbenExceptions.add("bedeutet");    // "und das bedeutet..."
    substVerbenExceptions.add("bekommen");
    substVerbenExceptions.add("bestätigt");
    substVerbenExceptions.add("bestätigte");
    substVerbenExceptions.add("bestätigten");
    substVerbenExceptions.add("dürfen");
    substVerbenExceptions.add("ein");   // nicht "einen" (Verb)
    substVerbenExceptions.add("ermöglicht");    // "und das ermöglicht..."
    substVerbenExceptions.add("gehören");
    substVerbenExceptions.add("habe");
    substVerbenExceptions.add("ist");
    substVerbenExceptions.add("können");
    substVerbenExceptions.add("muss");
    substVerbenExceptions.add("müssen");
    substVerbenExceptions.add("so");
    substVerbenExceptions.add("sollen");
    substVerbenExceptions.add("tun");   // "...dann wird er das tun."
    substVerbenExceptions.add("werden");
    substVerbenExceptions.add("wollen");
  }

  public CaseRule(final ResourceBundle messages) {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_case")));
  }

  @Override
  public String getId() {
    return "DE_CASE";
  }

  @Override
  public String getDescription() {
    return "Großschreibung von Nomen und substantivierten Verben";
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence text) throws IOException {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();

    boolean prevTokenIsDas = false;
    for (int i = 0; i < tokens.length; i++) {
      //FIXME: defaulting to the first analysis don't know if it's safe
      final String posToken = tokens[i].getAnalyzedToken(0).getPOSTag();
      if (posToken != null && posToken.equals(JLanguageTool.SENTENCE_START_TAGNAME)) {
        continue;
      }
      if (i == 1) {   // don't care about first word, UppercaseSentenceStartRule does this already
        if (nounIndicators.contains(tokens[i].getToken().toLowerCase())) {
          prevTokenIsDas = true;
        }
        continue;
      }
      if (i > 0 && (tokens[i-1].getToken().equals("Herr") || tokens[i-1].getToken().equals("Herrn") || tokens[i-1].getToken().equals("Frau")) ) {   // "Frau Stieg" could be a name, ignore
        continue;
      }
      final AnalyzedGermanTokenReadings analyzedToken = (AnalyzedGermanTokenReadings)tokens[i];
      final String token = analyzedToken.getToken();
      List<AnalyzedGermanToken> readings = analyzedToken.getGermanReadings();
      AnalyzedGermanTokenReadings analyzedGermanToken2;

      boolean isBaseform = false;
      if (analyzedToken.getReadingsLength() >= 1 && token.equals(analyzedToken.getAnalyzedToken(0).getLemma())) {
        isBaseform = true;
      }
      if ((readings == null || analyzedToken.getAnalyzedToken(0).getPOSTag() == null || analyzedToken.hasReadingOfType(GermanToken.POSType.VERB))
          && isBaseform) {
        // no match, e.g. for "Groß": try if there's a match for the lowercased word:
        analyzedGermanToken2 = tagger.lookup(token.toLowerCase());
        if (analyzedGermanToken2 != null) {
          readings = analyzedGermanToken2.getGermanReadings();
        }
        potentiallyAddLowercaseMatch(ruleMatches, tokens[i], prevTokenIsDas, token);
      }
      prevTokenIsDas = nounIndicators.contains(tokens[i].getToken().toLowerCase());
      if (readings == null) {
        continue;
      }
      final boolean hasNounReading = analyzedToken.hasReadingOfType(GermanToken.POSType.NOMEN);
      if (hasNounReading) {  // it's the spell checker's task to check that nouns are uppercase
        continue;
      }
      // TODO: this lookup should only happen once:
      analyzedGermanToken2 = tagger.lookup(token.toLowerCase());
      if (analyzedToken.getAnalyzedToken(0).getPOSTag() == null && analyzedGermanToken2 == null) {
        continue;
      }
      if (analyzedToken.getAnalyzedToken(0).getPOSTag() == null && analyzedGermanToken2 != null
          && analyzedGermanToken2.getAnalyzedToken(0).getPOSTag() == null) {
        // unknown word, probably a name etc
        continue;
      }
      potentiallyAddUppercaseMatch(ruleMatches, tokens, i, analyzedToken, token);
    }
    return toRuleMatchArray(ruleMatches);
  }

  private void potentiallyAddLowercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings tokenReadings, boolean prevTokenIsDas, String token) {
    if (prevTokenIsDas) {
      // e.g. essen -> Essen
      if (Character.isLowerCase(token.charAt(0)) && !substVerbenExceptions.contains(token)) {
        final String msg = "Substantivierte Verben werden groß geschrieben.";
        final RuleMatch ruleMatch = new RuleMatch(this, tokenReadings.getStartPos(),
            tokenReadings.getStartPos() + token.length(), msg);
        final String word = tokenReadings.getToken();
        final String fixedWord = StringTools.uppercaseFirstChar(word);
        ruleMatch.setSuggestedReplacement(fixedWord);
        ruleMatches.add(ruleMatch);
      }
    }
  }

  private void potentiallyAddUppercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings[] tokens, int i, AnalyzedGermanTokenReadings analyzedToken, String token) {
    if (Character.isUpperCase(token.charAt(0)) &&
        token.length() > 1 &&     // length limit = ignore abbreviations
        !sentenceStartExceptions.contains(tokens[i-1].getToken()) &&
        !StringTools.isAllUppercase(token) &&
        !exceptions.contains(token) &&
        !analyzedToken.hasReadingOfType(POSType.PROPER_NOUN) &&
        !isNilReading(analyzedToken) &&
        !analyzedToken.isSentenceEnd() &&
        !isExceptionPhrase(i, tokens)) {
      final String msg = "Außer am Satzanfang werden nur Nomen und Eigennamen großgeschrieben";
      final RuleMatch ruleMatch = new RuleMatch(this, tokens[i].getStartPos(),
          tokens[i].getStartPos() + token.length(), msg);
      final String word = tokens[i].getToken();
      final String fixedWord = Character.toLowerCase(word.charAt(0)) + word.substring(1);
      ruleMatch.setSuggestedReplacement(fixedWord);
      ruleMatches.add(ruleMatch);
    }
  }

  /** Morphy has about 750 words tagged: wkl="NIL" tip="SUB" - ignore these. */
  private boolean isNilReading(AnalyzedGermanTokenReadings analyzedToken) {
    final List<AnalyzedGermanToken> germanReadings = analyzedToken.getGermanReadings();
    if (germanReadings.size() > 0) {
      if ("NIL:SUB".equals(germanReadings.get(0).getPOSTag())) {
        return true;
      }
    }
    return false;
  }

  private boolean isExceptionPhrase(int i, AnalyzedTokenReadings[] tokens) {
    // TODO: speed up?
    for (String exc : myExceptionPhrases) {
      final String[] parts = exc.split(" ");
      for (int j = 0; j < parts.length; j++) {
        if (parts[j].equals(tokens[i].getToken())) {
          /*System.err.println("*******"+j + " of " + parts.length + ": " + parts[j]);
          System.err.println("start:" + tokens[i-j].getToken());
          System.err.println("end:" + tokens[i-j+parts.length-1].getToken());*/
          final int startIndex = i-j;
          if (compareLists(tokens, startIndex, startIndex+parts.length-1, parts)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean compareLists(AnalyzedTokenReadings[] tokens, int startIndex, int endIndex, String[] parts) {
    if (startIndex < 0) {
      return false;
    }
    int i = 0;
    for (int j = startIndex; j <= endIndex; j++) {
      //System.err.println("**" +tokens[j].getToken() + " <-> "+ parts[i]);
      if (i >= parts.length)
        return false;
      if (!tokens[j].getToken().equals(parts[i])) {
        return false;
      }
      i++;
    }
    return true;
  }

  @Override
  public void reset() {
    // nothing
  }

}