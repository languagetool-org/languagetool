/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.language.GermanyGerman;
import org.languagetool.languagemodel.BaseLanguageModel;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.languagetool.tools.StringTools.*;

/**
 * Find compounds that might be morphologically correct but are still probably wrong, like 'Lehrzeile'.
 * @since 4.1
 */
public class ProhibitedCompoundRule extends Rule {

  /**
   * @since 4.3
   * @deprecated each pair has its own id since LT 5.1
   */
  public static final String RULE_ID = "DE_PROHIBITED_COMPOUNDS";
  // have objects static for better performance (rule gets initialized for every check)
  private static final List<Pair> lowercasePairs = Arrays.asList(
          // NOTE: words here must be all-lowercase
          // NOTE: no need to add words from confusion_sets.txt, they will be used automatically (if starting with uppercase char)
          new Pair("schenke", "Gastwirtschaft (auch: Schänke)", "schenkel", "Ober- und Unterschenkel"),
          new Pair("rune", "Schriftzeichen der Germanen", "runde", "Rundstrecke"),
          new Pair("mai", "Monat nach April", "mail", "E-Mail"),
          new Pair("pump", "'auf Pump': umgangssprachlich für 'auf Kredit'", "pumpe", "Gerät zur Beförderung von Flüssigkeiten"),
          new Pair("mitte", "zentral", "mittel", "Methode, um etwas zu erreichen"),
          new Pair("fein", "feinkörnig, genau, gut", "feind", "Gegner"),
          new Pair("traum", "Erleben während des Schlafes", "trauma", "Verletzung"),
          new Pair("name", "Bezeichnung (z.B. 'Vorname')", "nahme", "zu 'nehmen' (z.B. 'Teilnahme')"),
          new Pair("bart", "Haarbewuchs im Gesicht", "dart", "Wurfpfeil"),
          new Pair("hart", "fest", "dart", "Wurfpfeil"),
          new Pair("speiche", "Verbindung zwischen Nabe und Felge beim Rad", "speicher", "Lagerraum"),
          new Pair("speichen", "Verbindung zwischen Nabe und Felge beim Rad", "speicher", "Lagerraum"),
          new Pair("kart", "Gokart (Fahrzeug)", "karte", "Fahrkarte, Postkarte, Landkarte, ..."),
          new Pair("karts", "Kart = Gokart (Fahrzeug)", "karte", "Fahrkarte, Postkarte, Landkarte, ..."),
          new Pair("kurz", "Gegenteil von 'lang'", "kur", "medizinische Vorsorge und Rehabilitation"),
          new Pair("kiefer", "knöcherner Teil des Schädels", "kiefern", "Kieferngewächse (Baum)"),
          new Pair("gel", "dickflüssige Masse", "geld", "Zahlungsmittel"),
          new Pair("flucht", "Entkommen, Fliehen", "frucht", "Ummantelung des Samens einer Pflanze"),
          new Pair("kamp", "Flurname für ein Stück Land", "kampf", "Auseinandersetzung"),
          new Pair("obst", "Frucht", "ost", "Himmelsrichtung"),
          new Pair("beeren", "Früchte", "bären", "Raubtiere"),
          new Pair("laus", "Insekt", "lauf", "Bewegungsart"),
          new Pair("läuse", "Insekt", "läufe", "Bewegungsart"),
          new Pair("läusen", "Insekt", "läufen", "Bewegungsart"),
          new Pair("ruck", "plötzliche Bewegung", "druck", "Belastung"),
          new Pair("brüste", "Plural von Brust", "bürste", "Gerät mit Borsten, z.B. zum Reinigen"),
          new Pair("attraktion", "Sehenswürdigkeit", "akttaktion", "vermutlicher Tippfehler"),
          new Pair("nah", "zu 'nah' (wenig entfernt)", "näh", "zu 'nähen' (mit einem Faden verbinden)"),
          new Pair("turn", "zu 'turnen'", "turm", "hohes Bauwerk"),
          new Pair("mit", "Präposition", "miet", "zu 'Miete' (Überlassung gegen Bezahlung)"),
          new Pair("bart", "Behaarung im Gesicht", "brat", "zu 'braten', z.B. 'Bratkartoffel'"),
          new Pair("uhr", "Instrument zur Zeitmessung", "ur", "ursprünglich"),
          new Pair("abschluss", "Ende", "abschuss", "Vorgang des Abschießens, z.B. mit einer Waffe"),
          new Pair("brache", "verlassenes Grundstück", "branche", "Wirtschaftszweig"),
          new Pair("wieder", "erneut, wiederholt, nochmal (Wiederholung, Wiedervorlage, ...)", "wider", "gegen, entgegen (Widerwille, Widerstand, Widerspruch, ...)"),
          new Pair("leer", "ohne Inhalt", "lehr", "bezogen auf Ausbildung und Wissen"),
          new Pair("gewerbe", "wirtschaftliche Tätigkeit", "gewebe", "gewebter Stoff; Verbund ähnlicher Zellen"),
          new Pair("schuh", "Fußbekleidung", "schul", "auf die Schule bezogen"),
          new Pair("klima", "langfristige Wetterzustände", "lima", "Hauptstadt von Peru"),
          new Pair("modell", "vereinfachtes Abbild der Wirklichkeit", "model", "Fotomodell"),
          new Pair("treppen", "Folge von Stufen (Mehrzahl)", "truppen", "Armee oder Teil einer Armee (Mehrzahl)"),
          new Pair("häufigkeit", "Anzahl von Ereignissen", "häutigkeit", "z.B. in Dunkelhäutigkeit"),
          new Pair("hin", "in Richtung", "hirn", "Gehirn, Denkapparat"),
          new Pair("verklärung", "Beschönigung, Darstellung in einem besseren Licht", "erklärung", "Darstellung, Erläuterung"),
          new Pair("spitze", "spitzes Ende eines Gegenstandes", "spritze", "medizinisches Instrument zur Injektion"),
          new Pair("punk", "Jugendkultur", "punkt", "Satzzeichen"),
          new Pair("reis", "Nahrungsmittel", "eis", "gefrorenes Wasser"),
          new Pair("balkan", "Region in Südosteuropa", "balkon", "Plattform, die aus einem Gebäude herausragt"),
          new Pair("haft", "Freiheitsentzug", "schaft", "-schaft (Element zur Wortbildung)"),
          new Pair("stande", "zu 'Stand'", "stange", "länglicher Gegenstand")
  );
  public static final GermanyGerman german = new GermanyGerman();
  private static GermanSpellerRule spellerRule;
  private static LinguServices linguServices;
  private static final List<String> ignoreWords = Arrays.asList("Die", "De");
  private static final List<String> blacklistRegex = Arrays.asList(
    "reisender",  // Ägyptenreisender etc.
    "[a-zöäüß]+sender",  // wg. sende/sender, z.B. bremsender, abzulassender
    "gra(ph|f)ische?",  // kosmografisch etc.
    "gra(ph|f)ische[rsnm]",  // kosmografischen etc.
    "gra(ph|f)s?$",  // Elektrokardiograph
    "gra(ph|f)en",  // Elektrokardiographen
    "gra(ph|f)in",  // Demographin/Demografin
    "gra(ph|f)ik",  // Kunstgrafik
    "gra(ph|f)ie",  // Geographie
    "Gra(ph|f)it"   // Grafit/Graphit
  );
  private static final Set<String> blacklist = new HashSet<>(Arrays.asList(
          "Schwabenweg",
          "Datenspende",
          "Datenspenden",
          "Designermaske",
          "Designermasken",
          "Herdenschutz",
          "Maskendisziplin",
          "Maskenmode",
          "Maskenmoden",
          "Maskenmoral",
          "Impfvorrang",
          "Volksmaske",
          "Volksmasken",
          "Testmeilen",  // vs Testteilen
          "Hauptstrand",  // vs Hauptstand
          "Hauptstrands",
          "Hauptstrandes",
          "Hüttenschuhe",
          "Hüttenschuhen",
          "Hüttenschuhs",
          "Mietbedingung",
          "Mietbedingungen",
          "Modeltyp",
          "Modeltyps",
          "Modeltypen",
          "Musikversand",
          "Musikversands",
          "Musikversandes",
          "Paragrafzeichen",
          "Paragrafzeichens",
          "Pflanzenmarkt",
          "Pflanzenmarkts",
          "Pflanzenmarktes",
          "Privatstrand",
          "Privatstrands",
          "Privatstrände",
          "Privatstränden",
          "Privatstrandes",
          "Reisessig",
          "Reisessigs",
          "Reiswein",
          "Reisweins",
          "Reisweine",
          "Schuhabteilung",
          "Schuhabteilungen",
          "Schulfirma",
          "Schulfirmen",
          "Schulmagazin",
          "Schulmagazins",
          "Schulmagazinen",
          "Schulmagazinen",
          "Segelschuhe",
          "Segelschuhen",
          "Spitzenhaus",
          "Spitzenhauses",
          "Spitzenhäuser",
          "Spitzenhäusern",
          "Standgebläse",
          "Standgebläsen",
          "Standgebläses",
          "Standstreifen",
          "Standstreifens",
          "Strandfigur",
          "Strandfoto",
          "Strandfotos",
          "Strandkonzert",
          "Strandkonzerts",
          "Strandkonzertes",
          "Strandkonzerte",
          "Strandkonzerten",
          "Strandverlust",
          "Strandverluste",
          "Strandverlusten",
          "Tierversand",
          "Tierversands",
          "Treppenart",
          "Treppenarten",
          "Winterflucht",
          "Nachtmitte",  // vs. Nachtmittel
          "Gemeindemitte", // vs. Gemeindemittel
          "Feinbeurteilung",  // vs. Feindbeurteilung
          "Bremssand",
          "Bratform",
          "Devisenspritze",
          "Einkaufszielen",
          "einnähmt",
          "hinübernähmen",
          "maschinennäher",
          "zentrumsnäher",
          "Einzelversandes",
          "Eisbällchen",
          "Eisenbahnrades",
          "Eisläufer",
          "Eisläufern",
          "Eisläufers",
          "Fachversand",
          "Fachversandes",
          "Feinwahrnehmung",
          "Feinwahrnehmungen",
          "Fluchtkapsel",
          "Fluchtkapseln",
          "Fluchtschiffe",
          "Fluchtschiffes",
          "Fluchtschiffs",
          "Fluchtschiffen",
          "Flügeltreppe",
          "Flügeltreppen",
          "Fruchtspiel",
          "Gletschersand",
          "Gletschersands",
          "Gletschersandes",
          "Grafem",
          "Grafems",
          "Grafeme",
          "Grafemen",
          "grafitgrau",
          "grafithaltig",
          "grafithaltige",
          "grafithaltiger",
          "grafithaltigen",
          "grafithaltigem",
          "grafithaltiges",
          "grafithaltigeren",
          "grafithaltigerem",
          "Reitschuhe",
          "Reitschuhen",
          "Nordbalkon",
          "Ostbalkon",
          "Südbalkon",
          "Westbalkon",
          "Zahngel",
          "Reinigungsgel",
          "Schutzname",
          "Schutznamen",
          "Gebrauchsname",
          "Gebrauchsnamen",
          "Erbname",
          "Erbnamen",
          "Datenname",
          "Datennamen",
          "Geldnahme",
          "Geldnahmen",
          "Kreispokal",
          "Gründertag",
          "Korrekturlösung",
          "Regelschreiber",
          "Glasreinigern",
          "Holzstele",
          "Brandschutz",
          "Testbahn",
          "Testbahnen",
          "Startglocke",
          "Startglocken",
          "Ladepunkte",
          "Kinderpreise",
          "Kinderpreisen",
          "Belegungsoptionen",
          "Brandgebiete",
          "Brandgebieten",
          "Innenfell",
          "Innenfelle",
          "Batteriepreis",
          "Alltagsschuhe",
          "Alltagsschuhen",
          "Arbeiterschuhe",
          "Arbeiterschuhen",
          "Bartvogel",
          "Abschiedsmail",
          "Abschiedsmails",
          "Wohnindex",
          "Entwicklungsstudio",
          "Ermittlungsgesetz",
          "Lindeverfahren",
          "Stromspender",
          "Turmverlag",  // eigtl. Turm-Verlag, muss hier als Ausnahme aber so stehen
          "Bäckerlunge",
          "Reisbeutel",
          "Reisbeuteln",
          "Reisbeutels",
          "Fellnase",
          "Fellnasen",
          "Kletterwald",
          "Kletterwalds",
          "Lusthöhle",
          "Lusthöhlen",
          "Abschlagswert",
          "Schuhfach",
          "Schuhfächer",
          "Spülkanüle",
          "Spülkanülen",
          "Tankkosten",
          "Hangout",
          "Hangouts",
          "Kassenloser",
          "kassenloser",
          "Reisnadel",
          "Reisnadeln",
          "stielloses",
          "stielloser",
          "stiellosen",
          "Beiratsregelung",
          "Beiratsregelungen",
          "Kreiskongress",
          "Lagekosten",
          "hineinfeiern",
          "Maskenhersteller", // vs Marken
          "Wabendesign",
          "Maskenherstellers",
          "Maskenherstellern",
          "Firmenvokabular",
          "Maskenproduktion",
          "Maskenpflicht",
          "Nachmiete",
          "Ringseil",
          "Ringseilen",
          "Jagdschule",
          "Tachograf",
          "Tachografs",
          "Tachografen",
          "Grafitpulver",
          "Grafitmine",
          "Grafitminen",
          "Nesselstraße",
          "Reitsachen",
          "Mehrfachabrechnung",
          "Stuhlrolle",
          "Stuhlrollen",
          "neugestartet",
          "Vertragskonto",
          "Männerding",
          "Restwoche",
          "Startpakete", // vs Rakete
          "Startpaketen", // vs Rakete
          "Suchintention", // vs Sach
          "Wettglück", // vs Welt
          "Wettprogramm", // vs Welt
          "Wettprogramme", // vs Welt
          "Zählerwechsel",
          "Zählerwechsels",
          "Nährstoffleitungen",  // vs ...leistungen
          "Verhandlungskreise",
          "Verhandlungskreisen",
          "Mietsuchenden",
          "Mietsuchende",
          "Mietsuchender",
          "Autoboss",
          "Autobossen",
          "Testmonat",
          "Testmonats",
          "Testmonate",
          "Naturseife",
          "Naturseifen",
          "Ankerkraut", // Firmenname (Lebensmittel)
          "Ankerkrauts",
          "Bewerbungstool",
          "Bewerbungstools",
          "Elektromarke",
          "Elektromarken",
          "Ankerkraut",
          "Testuser",
          "Testangeboten",
          "Testangebots",
          "Testangebotes",
          "verkeimt",
          "verkeimte",
          "verkeimter",
          "verkeimtes",
          "verkeimten",
          "verkeimtem",
          "Flugscham", // vs. Flugschau
          "Kurseinführung",
          "Januar-Miete",
          "Februar-Miete",
          "März-Miete",
          "April-Miete",
          "Mai-Miete",
          "Juni-Miete",
          "Juli-Miete",
          "August-Miete",
          "September-Miete",
          "Oktober-Miete",
          "November-Miete",
          "Dezember-Miete",
          "Suchindices",
          "Kirchenfreizeit",
          "Kirchenfreizeiten",
          "Erklärbär",
          "Wettart",
          "Wettarten",
          "Einzelwette",
          "Einzelwetten",
          "Artikelzeile",
          "Artikelzeilen",
          "Echtgeld",
          "Kartenansicht",
          "Kartenansichten",
          "Systemwette",
          "Systemwetten",
          "Dachzelt",
          "Dachzelte",
          "Badeloch",
          "Bonusaktion",
          "Bonusaktionen",
          "Projektannahme",
          "Pollenbelastung",
          "Fastenfenster",
          "Bauchtasche",
          "Bauchtaschen",
          "Zahloption",
          "Zahloptionen",
          "Zeckenschutz",
          "Vertragskonto",
          "Zeichengrenze",
          "Zeichengrenzen",
          "Kartontasche",
          "Kartontaschen",
          "Mietbestätigung",
          "Mietbestätigungen",
          "Tunnelzelt",
          "Tunnelzelts",
          "Tunnelzelte",
          "Dichtleistung",
          "Dichtleistungen",
          "Testkonto",
          "Testkontos",
          "Konzernnummer",
          "Konzernnummern",
          "Vertragsstunde",
          "Vertragsstunden",
          "Zaunträger",
          "Zaunträgern",
          "Zaunträgers",
          "Hallenschuh",
          "Hallenschuhs",
          "Hallenschuhe",
          "Rekrutierungsausgabe",
          "Rekrutierungsausgaben",
          "geruchsfreies",
          "Tafelfolie",
          "Gartenservice",
          "Gartenservices",
          "Rollgerüste",
          "Rollgerüsten",
          "Grasabfall",
          "Grasabfällen",
          "Ketogrippe",
          "Gerätehülle",
          "kaltweiß",
          "Maskenbefreiung",
          "Lusttropfen",
          "Kundenstimme",
          "Deichschafen",
          "Industriehefe",
          "Freizeitschuhe",
          "Freizeitschuhen",
          "Trainingsschuhe",
          "Trainingsschuhen",
          "Schuhblatt",
          "Nachbacken",
          "Wassermelder",
          "Schutzsegen",
          "Fischversteigerung",
          "Fischversteigerungen",
          "Konfigurationsteile",
          "Konfigurationsteilen",
          "Wasserbauch",
          "Wasserbauchs",
          "Stadtrad",
          "Stadtrads",
          "Seniorenrad",
          "Seniorenrads",
          "Bodenplane",
          "Schwimmschuhe",
          "Familienstrand",
          "versiegelbaren",
          "Überraschungsfeier",
          "Überraschungsfeiern",
          "ballseitig",
          "Genussgarten",
          "Genussgartens",
          "Edelsteingarten",
          "Edelsteingartens",
          "Insektengarten",
          "Insektengartens",
          "Klimakurs",
          "Klimakurses",
          "Kursperioden",
          "Musikreise",
          "Musikreisen",
          "Ziegenhof",
          "Ziegenhofs",
          "Außendecke",
          "Außendecken",
          "Bewegungsbarriere",
          "Bewegungsbarrieren",
          "Gerichtsantrag",
          "Gerichtsantrags",
          "Gerichtsanträge",
          "Spezialwette",
          "Spezialwetten",
          "Geldmagnet",
          "Testartikel",
          "Testartikeln",
          "Testartikels",
          "folierte",
          "foliert",
          "folierten",
          "foliertes",
          "foliertem",
          "Implementierungsvorgaben",
          "Bücherzelle",
          "Bücherzellen",
          "Cuttermesser",
          "Cuttermessern",
          "Cuttermessers",
          "Kabelsammlung",
          "Kabelsammlungen",
          "Schleifschwamm",
          "Schleifschwamms",
          "Schleifschwämme",
          "Teemarke",
          "Teemarken",
          "Vorzelt",
          "Vorzelte",
          "Supportleitung", // vs leistung
          "Kursname",
          "Schmucksorte",
          "Schmucksorten",
          "Farbsorte",
          "Farbsorten",
          "Donaublick",
          "Rundhals",
          "Trittschutz",
          "Laufhaus",
          "Wickeltasche",
          "Bayernliga",
          "Badsanierung",
          "Laufbereitschaft",
          "Geschenkkarten",
          "Landesklasse",
          "Firmenlauf",
          "Satzverlust",
          "Satzgewinn",
          "Monatslinsen",
          "Tageslinsen",
          "Sexstellung",
          "Traumbad",
          "Schlafsystem",
          "Startspieler",
          "Tabellenrang",
          "Heimerfolg",
          "Dachboxen",
          "Videotest",
          "Zugbindung",
          "Badplanung",
          "Badschrank",
          "Reiturlaub",
          "Zeittraining",
          "Dichtungssatz",
          "Wettbörsen",
          "Bildungspaket",
          "anklickst",
          "Frauenlauf",
          "Problemhaut",
          "Absperrpfosten",
          "Regenhülle",
          "Satzball",
          "Auswärtsfahrt",
          "Dichtsatz",
          "Nutzungserlebnis",
          "Saumabschluss",
          "Rundengewinn",
          "Haussteuerung",
          "Unterfederung",
          "Sterneküche",
          "Wickeltaschen",
          "Jahreslinsen",
          "Blutmond",
          "Badeplattform",
          "Wettquote",
          "Haarmaske",
          "Schlussgang",
          "Damengrößen",
          "Hautanalyse",
          "Außenkamera",
          "Kuscheldecken",
          "Feinhefe",
          "Radstation",
          "Satzbälle",
          "gelinkter",
          "Nacktbild",
          "Bücherbär",
          "Winzerhof",
          "Laufhäuser",
          "Verbandsklasse",
          "Rennrunde",
          "Luftmasche",
          "Suchfiltern",
          "Wohndecke",
          "bespaßt",
          "Endrohren",
          "ablutschte",
          "Waldkatzen",
          "Sicherheitsweste",
          "Gelbsperre",
          "Kopfdichtung",
          "Wurfzelt",
          "Gemeinschaftskarten",
          "Kornkreis",
          "Bronzerang",
          "Fensterfolien",
          "einköpfen",
          "Einkaufsnacht",
          "Reiserad",
          "Leistungsspange",
          "Ladepunkten",
          "Breitbänder",
          "Wochenplaner",
          "Leserunden",
          "Königsleiten",
          "Hochlader",
          "Lauferlebnis",
          "Radstrecken",
          "Kinderlauf",
          "Bettsystem",
          "Reifentests",
          "Fettleder",
          "Bildungsstreik",
          "Satzrückstand",
          "Bürstenköpfe",
          "Werbegesicht",
          "Bassreflex",
          "Postrock",
          "gelaserten",
          "Ohrbügel",
          "Bestweite",
          "Golfschuhe",
          "Genussreise",
          "Barkultur",
          "Ladepunkt",
          "Sportreifen",
          "Begleitdamen",
          "Tauchgebiet",
          "Stadttouren",
          "vermixen",
          "Suchvorschläge",
          "Damenmodell",
          "Putzkittel",
          "Eistees",
          "Begleitdame",
          "Frontmanns",
          "Katzenbett",
          "Pizzaöfen",
          "Sitzerhöhungen",
          "Golfkurse",
          "Ventilkappen",
          "Kinderuhr",
          "Bachblüte",
          "rockigem",
          "Kinderpreis",
          "Massivhauses",
          "Golfstar",
          "Herstellerwertung",
          "Herznoten",
          "Geldklammer",
          "Einzelkatze",
          "Fellwechsels",
          "Duftreis",
          "Jugendpokal",
          "Werbelüge",
          "Superzoom",
          "Inselpark",
          "Golfschuh",
          "Schuhwahl",
          "Schwingtor",
          "Sexabenteuern",
          "Insektenhaus",
          "Ramschniveau",
          "Verbrenners",
          "Doppelklingen",
          "Clubkonzert",
          "pullert",
          "Meisterchor",
          "Bienenfarm",
          "Feuchtmann" //name
  ));

  // have per-class static list of these and reference that in instance
  // -> avoid loading word list for every instance, but allow variations in subclasses
  protected AhoCorasickDoubleArrayTrie<String> ahoCorasickDoubleArrayTrie;
  protected Map<String, List<Pair>> pairMap;

  private static final AhoCorasickDoubleArrayTrie<String> prohibitedCompoundRuleSearcher;
  private static final Map<String, List<Pair>> prohibitedCompoundRulePairMap;

  static {
    List<Pair> pairs = new ArrayList<>();
    Map<String, List<Pair>> pairMap = new HashMap<>();
    addUpperCaseVariants(pairs);
    addItemsFromConfusionSets(pairs, "/de/confusion_sets.txt", true);
    prohibitedCompoundRuleSearcher = setupAhoCorasickSearch(pairs, pairMap);
    prohibitedCompoundRulePairMap = pairMap;
  }

  private static void addAllCaseVariants(List<Pair> candidatePairs, Pair lcPair) {
    candidatePairs.add(new Pair(lcPair.part1, lcPair.part1Desc, lcPair.part2, lcPair.part2Desc));
    String ucPart1 = uppercaseFirstChar(lcPair.part1);
    String ucPart2 = uppercaseFirstChar(lcPair.part2);
    if (!lcPair.part1.equals(ucPart1) || !lcPair.part2.equals(ucPart2)) {
      candidatePairs.add(new Pair(ucPart1, lcPair.part1Desc, ucPart2, lcPair.part2Desc));
    }
  }

  private static void addUpperCaseVariants(List<Pair> pairs) {
    for (Pair lcPair : lowercasePairs) {
      if (startsWithUppercase(lcPair.part1)) {
        throw new IllegalArgumentException("Use all-lowercase word in " + ProhibitedCompoundRule.class + ": " + lcPair.part1);
      }
      if (startsWithUppercase(lcPair.part2)) {
        throw new IllegalArgumentException("Use all-lowercase word in " + ProhibitedCompoundRule.class + ": " + lcPair.part2);
      }
      addAllCaseVariants(pairs, lcPair);
    }
  }

  protected static void addItemsFromConfusionSets(List<Pair> pairs, String confusionSetsFile, boolean isUpperCase) {
    try {
      ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
      try (InputStream confusionSetStream = dataBroker.getFromResourceDirAsStream(confusionSetsFile)) {
        ConfusionSetLoader loader = new ConfusionSetLoader(german);
        Map<String, List<ConfusionPair>> confusionPairs = loader.loadConfusionPairs(confusionSetStream);
        for (Map.Entry<String, List<ConfusionPair>> entry : confusionPairs.entrySet()) {
          for (ConfusionPair pair : entry.getValue()) {
            boolean allUpper = pair.getTerms().stream().allMatch(k -> startsWithUppercase(k.getString()) && !ignoreWords.contains(k.getString()));
            if (allUpper || !isUpperCase) {
              List<ConfusionString> cSet = pair.getTerms();
              if (cSet.size() != 2) {
                throw new RuntimeException("Got confusion set with != 2 items: " + cSet);
              }
              Iterator<ConfusionString> it = cSet.iterator();
              ConfusionString part1 = it.next();
              ConfusionString part2 = it.next();
              pairs.add(new Pair(part1.getString(), part1.getDescription(), part2.getString(), part2.getDescription()));
              if (isUpperCase) {
                pairs.add(new Pair(lowercaseFirstChar(part1.getString()), part1.getDescription(), lowercaseFirstChar(part2.getString()), part2.getDescription()));
              } else {
                pairs.add(new Pair(uppercaseFirstChar(part1.getString()), part1.getDescription(), uppercaseFirstChar(part2.getString()), part2.getDescription()));
              }
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static AhoCorasickDoubleArrayTrie<String> setupAhoCorasickSearch(List<Pair> pairs, Map<String, List<Pair>> pairMap) {
    TreeMap<String, String> map = new TreeMap<>();
    for (Pair pair : pairs) {
      map.put(pair.part1, pair.part1);
      map.put(pair.part2, pair.part2);

      pairMap.putIfAbsent(pair.part1, new LinkedList<>());
      pairMap.putIfAbsent(pair.part2, new LinkedList<>());
      pairMap.get(pair.part1).add(pair);
      pairMap.get(pair.part2).add(pair);
    }
    // Build an AhoCorasickDoubleArrayTrie
    AhoCorasickDoubleArrayTrie<String> ahoCorasickDoubleArrayTrie = new AhoCorasickDoubleArrayTrie<>();
    ahoCorasickDoubleArrayTrie.build(map);
    return ahoCorasickDoubleArrayTrie;
  }

  private final BaseLanguageModel lm;
  private Pair confusionPair = null; // specify single pair for evaluation

  public ProhibitedCompoundRule(ResourceBundle messages, LanguageModel lm, UserConfig userConfig) {
    super(messages);
    this.lm = (BaseLanguageModel) Objects.requireNonNull(lm);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    this.ahoCorasickDoubleArrayTrie = prohibitedCompoundRuleSearcher;
    this.pairMap = prohibitedCompoundRulePairMap;
    linguServices = userConfig != null ? userConfig.getLinguServices() : null;
    spellerRule = linguServices == null ? new GermanSpellerRule(JLanguageTool.getMessageBundle(), german, null, null) : null;
    addExamplePair(Example.wrong("Da steht eine <marker>Lehrzeile</marker> zu viel."),
                   Example.fixed("Da steht eine <marker>Leerzeile</marker> zu viel."));
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return "Markiert wahrscheinlich falsche Komposita wie 'Lehrzeile', wenn 'Leerzeile' häufiger vorkommt.";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    for (AnalyzedTokenReadings readings : sentence.getTokensWithoutWhitespace()) {
      String tmpWord = readings.getToken();
      List<String> wordsParts = new ArrayList<>(Arrays.asList(tmpWord.split("-")));
      int partsStartPos = 0;
      for (String wordPart : wordsParts) {
        partsStartPos = getMatches(sentence, ruleMatches, readings, partsStartPos, wordPart, 0);
      }
      String noHyphens = removeHyphensAndAdaptCase(tmpWord);
      if (noHyphens != null) {
        getMatches(sentence, ruleMatches, readings, 0, noHyphens, tmpWord.length()-noHyphens.length());
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean isMisspelled (String word) {
    return (linguServices == null ? spellerRule.isMisspelled(word) : !linguServices.isCorrectSpell(word, german));
  }
  
  private int getMatches(AnalyzedSentence sentence, List<RuleMatch> ruleMatches, AnalyzedTokenReadings readings, int partsStartPos, String wordPart, int toPosCorrection) {
    /* optimizations:
     only nouns can be compounds
     all parts are at least 3 characters long -> words must have at least 6 characters
    */
    if ((readings.isTagged() && !readings.hasPartialPosTag("SUB")) && !readings.hasPosTagStartingWith("EIG:") || wordPart.length() <= 6) {  // EIG: e.g. "Obstdeutschland" -> "Ostdeutschland"
      partsStartPos += wordPart.length() + 1;
      return partsStartPos;
    }
    List<Pair> candidatePairs = new ArrayList<>();
    // ignore other pair when confusionPair is set (-> running for evaluation)

    if (confusionPair == null) {
      List<AhoCorasickDoubleArrayTrie.Hit<String>> wordList = ahoCorasickDoubleArrayTrie.parseText(wordPart);
      // might get duplicates, but since we only ever allow one match per word it doesn't matter
      for (AhoCorasickDoubleArrayTrie.Hit<String> hit : wordList) {
        List<Pair> pair = pairMap.get(hit.value);
        if (pair != null) {
          candidatePairs.addAll(pair);
        }
      }
    } else {
      addAllCaseVariants(candidatePairs, confusionPair);
    }

    List<WeightedRuleMatch> weightedMatches = new ArrayList<>();
    for (Pair pair : candidatePairs) {
      String variant = null;
      if (wordPart.contains(pair.part1)) {
        variant = wordPart.replaceFirst(pair.part1, pair.part2);
      } else if (wordPart.contains(pair.part2)) {
        variant = wordPart.replaceFirst(pair.part2, pair.part1);
      }
      //System.out.println(word + " <> " + variant);
      if (variant == null) {
        partsStartPos += wordPart.length() + 1;
        continue;
      }
      long wordCount = lm.getCount(wordPart);
      long variantCount = lm.getCount(variant);
      //float factor = variantCount / (float)Math.max(wordCount, 1);
      //System.out.println("word: " + wordPart + " (" + wordCount + "), variant: " + variant + " (" + variantCount + "), factor: " + factor + ", pair: " + pair);
      if (variantCount > 0 && wordCount == 0 && !blacklist.contains(wordPart) && !isMisspelled(variant) && blacklistRegex.stream().noneMatch(k -> wordPart.matches(".*" + k + ".*"))) {
        String msg;
        if (pair.part1Desc != null && pair.part2Desc != null) {
          msg = "Möglicher Tippfehler. " + uppercaseFirstChar(pair.part1) + ": " + pair.part1Desc + ", " + uppercaseFirstChar(pair.part2) + ": " + pair.part2Desc;
        } else {
          msg = "Möglicher Tippfehler: " + pair.part1 + "/" + pair.part2;
        }
        int fromPos = readings.getStartPos() + partsStartPos;
        int toPos = fromPos + wordPart.length() + toPosCorrection;
        String id = getId() + "_" + cleanId(pair.part1) + "_" + cleanId(pair.part2);
        RuleMatch match = new RuleMatch(new SpecificIdRule(id, pair.part1, pair.part2, messages), sentence, fromPos, toPos, msg);
        match.setSuggestedReplacement(variant);
        weightedMatches.add(new WeightedRuleMatch(variantCount, match));
      }
    }
    if (weightedMatches.size() > 0) {
      Collections.sort(weightedMatches);  // sort by most popular alternative
      ruleMatches.add(weightedMatches.get(0).match);
    }
    partsStartPos += wordPart.length() + 1;
    return partsStartPos;
  }

  private String cleanId(String id) {
    return id.toUpperCase().replace("Ä", "AE").replace("Ü", "UE").replace("Ö", "OE");
  }

  /**
   * ignore automatically loaded pairs and only match using given confusionPair
   * used for evaluation by ProhibitedCompoundRuleEvaluator
   * @param confusionPair pair to evaluate, parts are assumed to be lowercase / null to reset
   */
  public void setConfusionPair(Pair confusionPair) {
    this.confusionPair = confusionPair;
  }

  @Nullable
  String removeHyphensAndAdaptCase(String word) {
    String[] parts = word.split("-");
    if (parts.length > 1) {
      StringBuilder sb = new StringBuilder();
      int i = 0;
      for (String part : parts) {
        if (part.length() <= 1) {
          // don't: S-Bahn -> Sbahn
          return null;
        }
        sb.append(i == 0 ? part : lowercaseFirstChar(part));
        i++;
      }
      return sb.toString();
    }
    return null;
  }

  static class WeightedRuleMatch implements Comparable<WeightedRuleMatch> {
    long weight;
    RuleMatch match;
    WeightedRuleMatch(long weight, RuleMatch match) {
      this.weight = weight;
      this.match = match;
    }
    @Override
    public int compareTo(@NotNull WeightedRuleMatch other) {
      return Long.compare(other.weight, weight);
    }
  }

  public static class Pair {
    private final String part1;
    private final String part1Desc;
    private final String part2;
    private final String part2Desc;
    public Pair(String part1, String part1Desc, String part2, String part2Desc) {
      this.part1 = part1;
      this.part1Desc = part1Desc;
      this.part2 = part2;
      this.part2Desc = part2Desc;
    }
    @Override
    public String toString() {
      return part1 + "/" + part2;
    }
  }

  static private class SpecificIdRule extends Rule {  // don't extend ProhibitedCompoundRule for performance reasons (speller would get re-initialized a lot)
    private final String id;
    private final String desc;
    SpecificIdRule(String id, String part1, String part2, ResourceBundle messages) {
      this.id = Objects.requireNonNull(id);
      this.desc = "Markiert wahrscheinlich falsche Komposita mit Teilwort '" + uppercaseFirstChar(part1) + "' statt '" + uppercaseFirstChar(part2) + "' und umgekehrt";
      setCategory(Categories.TYPOS.getCategory(messages));
    }
    @Override
    public String getId() {
      return id;
    }
    @Override
    public String getDescription() {
      return desc;
    }
    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      return RuleMatch.EMPTY_ARRAY;
    }
  }
}
