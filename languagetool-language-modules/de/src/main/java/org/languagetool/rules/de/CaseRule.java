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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.StringMatcher;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.de.GermanToken;
import org.languagetool.tagging.de.GermanToken.POSType;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.languagetool.rules.de.CaseRuleAntiPatterns.ANTI_PATTERNS;

/**
 * Check that adjectives and verbs are not written with an uppercase
 * first letter (except at the start of a sentence) and cases
 * like this: <tt>Das laufen f&auml;llt mir leicht.</tt> (<tt>laufen</tt> needs
 * to be uppercased).
 *   
 * @author Daniel Naber
 */
public class CaseRule extends Rule {

  private static final Pattern NUMERALS_EN =
          Pattern.compile("[a-z]|[0-9]+|(m{0,4}(c[md]|d?c{0,3})(x[cl]|l?x{0,3})(i[xv]|v?i{0,3}))$");

  // wenn hinter diesen Wörtern ein Verb steht, ist es wohl ein substantiviertes Verb,
  // muss also groß geschrieben werden:
  private static final Set<String> nounIndicators = new HashSet<>();

  private static final String UPPERCASE_MESSAGE = "Außer am Satzanfang werden nur Nomen und Eigennamen großgeschrieben.";
  private static final String LOWERCASE_MESSAGE = "Falls es sich um ein substantiviertes Verb handelt, wird es großgeschrieben.";
  private static final String COLON_MESSAGE = "Folgt dem Doppelpunkt weder ein Substantiv noch eine wörtliche Rede oder ein vollständiger Hauptsatz, schreibt man klein weiter.";

  static {
    nounIndicators.add("das");
    nounIndicators.add("sein");
    //nounIndicators.add("ihr");    // would cause false alarm e.g. "Auf ihr stehen die Ruinen...", "Ich dachte, dass ihr kommen würdet.", "Ich verdanke ihr meinen Erfolg."
    nounIndicators.add("mein");
    nounIndicators.add("dein");
    nounIndicators.add("euer");
    nounIndicators.add("unser");
  }

  private static final String[] SENTENCE_START_EXCEPTIONS = {"(", "\"", "'", "‘", "„", "«", "»", ".", "!", "?"};

  private static final String[] UNDEFINED_QUANTIFIERS = {"viel", "nichts", "nix", "wenig", "allerlei"};

  private static final String[] INTERROGATIVE_PARTICLES = {"was", "wodurch", "wofür", "womit", "woran", "worauf", "woraus", "wovon", "wie"};

  private static final String[] POSSESSIVE_INDICATORS = {"einer", "eines", "der", "des", "dieser", "dieses"};

  private static final String[] DAS_VERB_EXCEPTIONS = {"nur", "sogar", "auch", "die", "alle", "viele", "zu"};

  /*
   * These are words that Morphy only knows as non-nouns (or not at all).
   * The proper solution is to add all those to our Morphy data, but as a simple
   * workaround to avoid false alarms, these words can be added here.
   */
  private static final String[] exceptions = {
    "Vertrauter",
    "Out", // eng
    "Packet", // misspelling of "Paket" (caught by spell checker)
    "Adult", // eng
    "Apart", // eng
    "Different", // eng
    "Fair", // eng
    "Viral", // eng
    "Tough", // eng
    "Superb", // eng und Automodell
    "Resilient", // eng
    "Hexagonal", // eng
    "Responsive", // eng
    "Anno", // Name
    "Mo",
    "Di",
    "Mi",
    "Do",   // "Di. und Do. um 18 Uhr"
    "Fr",   // "Fr. Dr. Müller"
    "Sa",   // Sa. 12 - 16 Uhr
    "Gr",   // "Gr. 12" (Größe)
    "Mag",   // "Mag. Helke Müller"
    "Nov",
    "Diss",
    "Invalide",
    "Invalider",
    "Invaliden",
    "Schutzheilige",
    "Schutzheiliger",
    "Schutzheiligen",
    "Lila",
    "Langzeitarbeitslose",
    "Langzeitarbeitslosen",
    "Langzeitarbeitsloser",
    "Linksintellektuelle",
    "Linksintellektueller",
    "Linksintellektuellen",
    "Beschuldigte",
    "Beschuldigten",
    "Drogenabhängige",
    "Drogenabhängiger",
    "Drogenabhängiger",
    "Drogenabhängigen",
    "Asylsuchender",
    "Asylsuchende",
    "Asylsuchenden",
    "Landtagsabgeordnete",
    "Landtagsabgeordneter",
    "Landtagsabgeordneten",
    "Stadtverordnete",
    "Stadtverordneter",
    "Stadtverordneten",
    "Veränderliche",
    "Veränderlicher",
    "Veränderlichen",
    "Werbetreibende",
    "Werbetreibender",
    "Werbetreibenden",
    "Verletzter",
    "Verletzten",
    "Werktätige",
    "Werktätiger",
    "Werktätigen",
    "Getestete", // temporary fix
    "Getesteten", // temporary fix
    "Genesene", // temporary fix
    "Genesenen", // temporary fix
    "Geimpfte", // temporary fix
    "Geboosterte", // temporary fix
    "Ungeimpfte", // temporary fix
    "Geimpften", // temporary fix
    "Geboosterten", // temporary fix
    "Ungeimpften", // temporary fix
    "Geflüchtete", // temporary fix
    "Geflüchteten", // temporary fix
    "Projektbeteiligte", // temporary fix
    "Projektbeteiligten", // temporary fix
    "Heranwachsende", // temporary fix
    "Heranwachsenden", // temporary fix
    "Interessierte", // temporary fix
    "Interessierten", // temporary fix
    "Infizierte", // temporary fix
    "Infizierten", // temporary fix
    "Gehörlose", // temporary fix
    "Gehörlosen", // temporary fix
    "Drücke",
    "Klecks",
    "Quatsch",
    "Speis",
    "Flash",
    "Suhl",
    "Müh",
    "Bims",
    "Wisch",
    "Außenputz",
    "Rinderhack",
    "Hack",
    "Schlitz",
    "Frevler",
    "Zementputz",
    "Hurst",  // Name
    "Bombardier",  // Name
    "Kraus",  // Nachname
    "Strunz",  // Nachname
    "Bell",  // Nachname
    "Melk",  // Nachname
    "Klopp",  // Nachname
    "Walz",  // Nachname
    "Schiel",  // Nachname
    "Dusch",  // Nachname
    "Penn",  // Nachname
    "Dörr",  // Nachname
    "Kies",
    "Koks",
    "Dell",  // Name
    "Wall",
    "Beige",
    "Zoom",
    "Perl",
    "Parallele",
    "Parallelen",
    "Rutsch",
    "Spar",
    "Merz",
    "Gefahren",
    "Minderjährige",
    "Minderjähriger",
    "Minderjährigen",
    "Scheinselbstständige",
    "Bundestagsabgeordneter",
    "Bundestagsabgeordneten",
    "Bundestagsabgeordnete",
    "Reichstagsabgeordneter",
    "Reichstagsabgeordneten",
    "Reichstagsabgeordnete",
    "Medienschaffende",
    "Medienschaffenden",
    "Medienschaffender",
    "Lehrende",
    "Lehrenden",
    "Vertretene",
    "Vertretenen",
    "Vorstandsvorsitzender",
    "Vorstandsvorsitzenden",
    "Vorstandsvorsitzende",
    "Demonstrierende",
    "Demonstrierenden",
    "Marketingtreibende",
    "Marketingtreibender",
    "Marketingtreibenden",
    "Strafgefangenen",
    "Strafgefangener",
    "Strafgefangene",
    "Pädophile",
    "Pädophiler",
    "Pädophilen",
    "Lehrbeauftragte",
    "Lehrbeauftragter",
    "Lehrbeauftragten",
    "Erkrankte",
    "Erkrankter",
    "Erkrankten",
    "Eigner",
    "Polizeibeamten",
    "Polizeibeamter",
    "Polizeibeamte",
    "Kriegsversehrte",
    "Kriegsversehrter",
    "Kriegsversehrten",
    "Demenzkranke",
    "Demenzkranker",
    "Demenzkranken",
    "Parteivorsitzende",
    "Parteivorsitzender",
    "Parteivorsitzenden",
    "Kriegsgefangene",
    "Kriegsgefangener",
    "Kriegsgefangenen",
    "Ehrenvorsitzende",
    "Ehrenvorsitzender",
    "Ehrenvorsitzenden",
    "Oberkommandierende",
    "Oberkommandierender",
    "Oberkommandierenden",
    "Mitangeklagte",
    "Schuhfilz",
    "Mix",
    "Rahm",
    "Flansch",
    "WhatsApp",
    "Verschleiß",
    "Schutzsuchende", // gendered form
    "Schutzsuchenden", // gendered form
    "Versicherte",
    "Versicherten", // gendered form
    "Cyberkriminelle",
    "Cyberkriminellen",
    "Kriminelle",
    "Kriminellen",
    "Auszubildenden",
    "Auszubildende",
    "Auszubildender",
    "Lernende", // gendered form
    "Lernender", // gendered form
    "Lernenden", // gendered form
    "Teilnehmende", // gendered form
    "Teilnehmenden", // gendered form
    "Radfahrende", // gendered form
    "Radfahrenden", // gendered form
    "Autofahrende", // gendered form
    "Autofahrenden", // gendered form
    "Auszubildene", // gendered form
    "Auszubildenen", // gendered form
    "Absolvierende", // gendered form
    "Absolvierenden", // gendered form
    "Einheimische",
    "Einheimischen",
    "Einheimischer",
    "Wehrbeauftragter",
    "Wehrbeauftragte",
    "Wehrbeauftragten",
    "Wehrbeauftragtem",
    "Prozessbevollmächtigter",
    "Prozessbevollmächtigte",
    "Prozessbevollmächtigten",
    "Prozessbevollmächtigtem",
    "Bundesbeamte",
    "Bundesbeamter",
    "Bundesbeamten",
    "Bundesbeamtem",
    "Datenschutzbeauftragter",
    "Datenschutzbeauftragte",
    "Datenschutzbeauftragten",
    "Datenschutzbeauftragtem",
    "Steuerbevollmächtigte",
    "Steuerbevollmächtigter",
    "Steuerbevollmächtigten",
    "Steuerbevollmächtigtem",
    "Suchtkranken",
    "Suchtkranke",
    "Suchtkranker",
    "Filmschaffende",
    "Filmschaffender",
    "Filmschaffenden",
    "Filmschaffendem",
    "Arbeitssuchende",
    "Arbeitssuchender",
    "Arbeitssuchenden",
    "Arbeitssuchendem",
    "Bausachverständige",
    "Bausachverständiger",
    "Bausachverständigen",
    "Bausachverständigem",
    "Heurige",
    "Ratsuchende",
    "Ratsuchender",
    "Ratsuchenden",
    "Verwundete",
    "Verwundeter",
    "Verwundeten",
    "Vollzugsbeamte",
    "Vollzugsbeamter",
    "Vollzugsbeamten",
    "Schutzbefohlene",
    "Schutzbefohlener",
    "Schutzbefohlenen",
    "Verfahrensbeteiligte",
    "Verfahrensbeteiligter",
    "Verfahrensbeteiligten",
    "Kolonialbeamte",
    "Kolonialbeamter",
    "Kolonialbeamten",
    "Verwaltungsbeamte",
    "Verwaltungsbeamter",
    "Verwaltungsbeamten",
    "Verdächtige",
    "Verdächtiger",
    "Verdächtigen",
    "Leichtverletzte",
    "Leichtverletzten",
    "Leichtverletzte",
    "Dozierende",
    "Dozierenden",
    "Studierende",
    "Studierender",
    "Studierenden",
    "Suchbegriffen",
    "Plattdeutsch",
    "Wallet",
    "Str",
    "Auszubildende",
    "Auszubildender",
    "Gelehrte",
    "Gelehrter",
    "Gelehrten",
    "Vorstehende",
    "Vorstehender",
    "Mitwirkende",
    "Mitwirkender",
    "Mitwirkenden",
    "Tabellenletzte",
    "Tabellenletzter",
    "Familienangehörige",
    "Familienangehöriger",
    "Zeitreisende",
    "Zeitreisender",
    "Zeitreisenden",
    "Erwerbstätige",
    "Erwerbstätigen",
    "Erwerbstätiger",
    "Selbstständige",
    "Selbstständigen",
    "Selbstständiger",
    "Selbständige",
    "Selbständigen",
    "Selbständiger",
    "Genaueres",
    "Äußersten",
    "Dienstreisender",
    "Verletzte",
    "Vermisste",
    "Äußeres",
    "Abseits",
    "Unschuldige",
    "Unschuldiger",
    "Unschuldigen",
    "Mitarbeitende",
    "Mitarbeitender",
    "Mitarbeitenden",
    "Beschäftigter",
    "Beschäftigte",
    "Beschäftigten",
    "Bekannter",
    "Bekannte",
    "Bevollmächtigte",
    "Bevollmächtigter",
    "Bevollmächtigten",
    "Brecht",
    "Tel",  // Tel. = Telefon
    "Unschuldiger",
    "Vorgesetzter",
    "Abs",   // Abs. = Abkürzung für Absatz, Absender, ...
    "Klappe",
    "Vorfahre",
    "Mittler",
    "Hr",   // Hr. = Abkürzung für Herr
    "Schwarz",
    "Genese",
    "Rosa",
    "Auftrieb",
    "Zuschnitt",
    "Geschossen",
    "Vortrieb",
    "Abtrieb",
    "Gesandter",
    "Durchfahrt",
    "Durchgriff",
    "Überfahrt",
    "Zeche",
    "Sparte",
    "Sparten",
    "Heiliger",
    "Reisender",
    "Pest",
    "Schwinge",
    "Verlies",
    "Nachfolge",
    "Stift",
    "Belange",
    "Geistlicher",
    "Google",
    "Hu", // name
    "Jenseits",
    "Abends",
    "Stimmberechtigte",
    "Stimmberechtigten",
    "Stimmberechtigter",
    "Alleinerziehende",
    "Alleinerziehenden",
    "Alleinerziehender",
    "Abgeordneter",
    "Abgeordnete",
    "Abgeordneten",
    "Angestellter",
    "Angestellte",
    "Angestellten",
    "Armeeangehörige",
    "Armeeangehörigen",
    "Armeeangehöriger",
    "Liberaler",
    "Abriss",
    "Ahne",
    "Ähnlichem",
    "Ähnliches",   // je nach Kontext groß (TODO), z.B. "Er hat Ähnliches erlebt" 
    "Allerlei",
    "Anklang",
    "Verlobter",
    "Anstrich",
    "Armes",
    "Ausdrücke",
    "Auswüchsen",
    "Bände",
    "Bänden",
    "Beauftragter",
    "Belange",
    "Biss",
    "De",    // "De Morgan" etc
    "Diesseits", // "im Diesseits"
    "Dr",
    "Durcheinander",
    "Eindrücke",
    "Erwachsener",
    "Familienangehörige", // "Brüder und solche Familienangehörige, die..."
    "Flöße",
    "Folgendes",   // je nach Kontext groß (TODO)...
    "Fort",
    "Fraß",
    "Frevel",
    "Genüge",
    "Gefallen", // Gefallen finden
    "Gläubige",
    "Gläubiger",
    "Gläubigen",
    "Hechte",
    "Herzöge",
    "Herzögen",
    "Hinfahrt",
    "Hilfsstoff",
    "Hilfsstoffe",
    "Hundert",   // groß und klein möglich 
    "Zehntausend",   // groß und klein möglich 
    "Hunderttausend",   // groß und klein möglich 
    "Hyperwallet", // Anglizismus
    "Ihnen",
    "Ihr",
    "Ihre",
    "Ihrem",
    "Ihren",
    "Ihrer",
    "Ihres",
    "Infrarot",
    "Jenseits",
    "Jugendlicher",
    "Jünger",
    "Kant", //Immanuel
    "Klaue",
    "Konditional",
    "Krähe",
    "Kurzem",
    "Landwirtschaft",
    "Langem",
    "Längerem",
    "Lausitz",
    "Le",    // "Le Monde" etc
    "Lehrlingsunterweisung",
    // "Leichter", // Leichter = ein Schiff in oben offener Bauweise ohne Eigenantrieb
    "Letzt",
    "Letzt",      // "zu guter Letzt"
    "Letztere",
    "Letzterer",
    "Letzteres",
    "Link",
    "Links",
    "Löhne",
    "Luden",
    "Milk", // Englisches Wort und eine Form von "melken"
    "Mitfahrt",
    "Mr",
    "Mrd",
    "Mrs",
    "Nachfrage",
    "Nachts",   // "des Nachts", "eines Nachts"
    "Nachspann",
    "Nähte",
    "Nähten",
    "Narkoseverfahren",
    "Neuem",
    "Nr",
    "Nutze",   // zu Nutze
    "Obdachloser",
    "Oder",   // der Fluss
    "Ohrfeige",
    "Patsche",
    "Pfiffe",
    "Pfiffen",
    "Press", // University Press
    "Prof",
    "Puste",
    "Sachverständiger",
    "Sankt",
    "Schaulustige",
    "Scheine",
    "Scheiße",
    "Schuft",
    "Schufte",
    "Schuld",
    "Schwangere",
    "Schwangeren",
    "Schwärme",
    "Schwarzes",    // Schwarzes Brett
    "Sie",
    "Skype",
    "Spitz",
    "Spott",
    "St",   // Paris St. Germain
    "Stereotyp",
    "Störe",
    "Tausend",   // je nach Kontext groß (TODO)
    "Tischende",
    "Toter",
    "Übrigen",   // je nach Kontext groß (TODO), z.B. "im Übrigen"
    "Unentschieden",
    "Unvorhergesehenes",   // je nach Kontext groß (TODO), z.B. "etwas Unvorhergesehenes"
    "Verantwortlicher",
    "Verlass",
    "Verwandter",
    "Verstorbenen",
    "Verstorbene",
    "Vielfache",
    "Vielfaches",
    "Vorsitzender",
    "Fraktionsvorsitzender",
    "Verletzte",
    "Verletzten",
    "Walt",
    "Weitem",
    "Weiteres",
    "Wicht",
    "Wichtiges",
    "Wider",    // "das Für und Wider"
    "Wild",
    "Zeche",
    "Zusage",
    "Zwinge",
    "Zirkusrund",
    "Tertiär",  // geologischer Zeitabschnitt

    "Erster",   // "er wurde Erster im Langlauf"
    "Zweiter",
    "Dritter",
    "Vierter",
    "Fünfter",
    "Sechster",
    "Siebter",
    "Achter",
    "Neunter",
    "Erste",   // "sie wurde Erste im Langlauf"
    "Zweite",
    "Dritte",
    "Vierte",
    "Fünfte",
    "Sechste",
    "Siebte",
    "Achte",
    "Neunte",

    // Änderungen an der Rechtschreibreform 2006 erlauben hier Großschreibung:
    "Dein",
    "Deine",
    "Deinem",
    "Deinen",
    "Deiner",
    "Deines",
    "Dich",
    "Dir",
    "Du",
    "Euch",
    "Euer",
    "Eure",
    "Eurem",
    "Euren",
    "Eures"
  };
  
  private static final Set<StringMatcher[]> exceptionPatterns = CaseRuleExceptions.getExceptionPatterns();

  private static final Set<String> substVerbenExceptions = new HashSet<>();
  static {
    substVerbenExceptions.add("hinziehen");
    substVerbenExceptions.add("helfen");
    substVerbenExceptions.add("lassen");
    substVerbenExceptions.add("passieren");  // "das Schlimmste, das passieren könnte"
    substVerbenExceptions.add("haben");  // "Das haben schon viele versucht."
    substVerbenExceptions.add("passiert");  // "Das passiert..."
    substVerbenExceptions.add("beschränkt");  // "Das beschränkt sich..."
    substVerbenExceptions.add("wiederholt");
    substVerbenExceptions.add("scheinen");
    substVerbenExceptions.add("klar");
    substVerbenExceptions.add("heißen");
    substVerbenExceptions.add("einen");
    substVerbenExceptions.add("gehören");
    substVerbenExceptions.add("bedeutet");    // "und das bedeutet..."
    substVerbenExceptions.add("ermöglicht");    // "und das ermöglicht..."
    substVerbenExceptions.add("funktioniert");    // "Das funktioniert..."
    substVerbenExceptions.add("sollen");
    substVerbenExceptions.add("werden");
    substVerbenExceptions.add("dürfen");
    substVerbenExceptions.add("müssen");
    substVerbenExceptions.add("so");
    substVerbenExceptions.add("ist");
    substVerbenExceptions.add("können");
    substVerbenExceptions.add("mein"); // "etwas, das mein Interesse geweckt hat"
    substVerbenExceptions.add("sein");
    substVerbenExceptions.add("muss");
    substVerbenExceptions.add("muß");
    substVerbenExceptions.add("wollen");
    substVerbenExceptions.add("habe");
    substVerbenExceptions.add("ein");   // nicht "einen" (Verb)
    substVerbenExceptions.add("tun");   // "...dann wird er das tun."
    substVerbenExceptions.add("bestätigt");
    substVerbenExceptions.add("bestätigte");
    substVerbenExceptions.add("bestätigten");
    substVerbenExceptions.add("bekommen");
    substVerbenExceptions.add("sauer");
    substVerbenExceptions.add("bedeuten");
  }

  private final GermanTagger tagger;
  private final GermanSpellerRule speller;
  private final Supplier<List<DisambiguationPatternRule>> antiPatterns;

  public CaseRule(ResourceBundle messages, German german) {
    super.setCategory(Categories.CASING.getCategory(messages));
    tagger = (GermanTagger) german.getTagger();
    speller = new GermanSpellerRule(JLanguageTool.getMessageBundle(), german);
    antiPatterns = cacheAntiPatterns(german, ANTI_PATTERNS);
    addExamplePair(Example.wrong("<marker>Das laufen</marker> fällt mir schwer."),
                   Example.fixed("<marker>Das Laufen</marker> fällt mir schwer."));
  }
  
  @Override
  public String getId() {
    return "DE_CASE";
  }

  @Override
  public int estimateContextForSureMatch() {
    return ANTI_PATTERNS.stream().mapToInt(List::size).max().orElse(0);
  }
  
  @Override
  public URL getUrl() {
    return Tools.getUrl("https://dict.leo.org/grammatik/deutsch/Rechtschreibung/Regeln/Gross-klein/index.html");
  }

  @Override
  public String getDescription() {
    return "Großschreibung von Nomen und substantivierten Verben";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    
    boolean prevTokenIsDas = false;
    boolean isPrecededByModalOrAuxiliary = false;
    for (int i = 0; i < tokens.length; i++) {
      //Note: defaulting to the first analysis is only save if we only query for sentence start
      String posToken = tokens[i].getAnalyzedToken(0).getPOSTag();
      if (JLanguageTool.SENTENCE_START_TAGNAME.equals(posToken)) {
        continue;
      }
      if (i == 1) {   // don't care about first word, UppercaseSentenceStartRule does this already
        prevTokenIsDas = nounIndicators.contains(tokens[1].getToken().toLowerCase());
        continue;
      }
      if (i > 0 && (isSalutation(tokens[i-1].getToken()) || isCompany(tokens[i-1].getToken()))) {   // e.g. "Frau Stieg" could be a name, ignore
        continue;
      }

      // 1.1 Technische Dokumentation
      if (i > 2 && NUMERALS_EN.matcher(tokens[i-1].getToken()).matches() && isDot(tokens[i-2].getToken()) && NUMERALS_EN.matcher(tokens[i-3].getToken()).matches()) {
        continue;
      }

      AnalyzedTokenReadings analyzedToken = tokens[i];
      String token = analyzedToken.getToken();

      boolean isBaseform = analyzedToken.getReadingsLength() >= 1 && analyzedToken.hasLemma(token);
      if ((analyzedToken.getAnalyzedToken(0).getPOSTag() == null || GermanHelper.hasReadingOfType(analyzedToken, GermanToken.POSType.VERB))
          && isBaseform) {
        boolean nextTokenIsPersonalOrReflexivePronoun = false;
        if (i < tokens.length - 1) {
          AnalyzedTokenReadings nextToken = tokens[i + 1];
          // avoid false alarm for "Das haben wir getan." etc:
          nextTokenIsPersonalOrReflexivePronoun = nextToken.hasPartialPosTag("PRO:PER") || StringUtils.equalsAny(nextToken.getToken(), "sich", "Sie");
          if (nextToken.hasPosTag("PKT")) {
            // avoid false alarm for "So sollte das funktionieren." (might also remove true alarms...)
            continue;
          }
          if (prevTokenIsDas
              && (StringUtils.equalsAny(nextToken.getToken(), DAS_VERB_EXCEPTIONS) ||
                  isFollowedByRelativeOrSubordinateClause(i, tokens)) ||
                  (i > 1 && hasPartialTag(tokens[i-2], "VER:AUX", "VER:MOD"))) {
            // avoid false alarm for "Er kann ihr das bieten, was sie verdient."
            // avoid false alarm for "Das wissen die meisten." / "Um das sagen zu können, ..."
            // avoid false alarm for "Du musst/solltest/könntest das wissen, damit du die Prüfung bestehst / weil wir das gestern besprochen haben."
            // avoid false alarm for "Wir werden das stoppen."
            // avoid false alarm for "Wahre Liebe muss das aushalten."
            continue;
          }
        }
        if (isPrevProbablyRelativePronoun(tokens, i) ||
            (prevTokenIsDas && getTokensWithPosTagStartingWithCount(tokens, "VER") == 1)) {// ignore sentences containing a single verb, e.g., "Das wissen viele nicht."
          continue;
        }
        potentiallyAddLowercaseMatch(ruleMatches, tokens[i], prevTokenIsDas, token, nextTokenIsPersonalOrReflexivePronoun, sentence);
      }
      prevTokenIsDas = nounIndicators.contains(tokens[i].getToken().toLowerCase());
      if (analyzedToken.matchesPosTagRegex("VER:(MOD|AUX):[1-3]:.*")) {
        isPrecededByModalOrAuxiliary = true;
      }
      AnalyzedTokenReadings lowercaseReadings = tagger.lookup(token.toLowerCase());
      if (hasNounReading(analyzedToken)) { // it's the spell checker's task to check that nouns are uppercase
        if (!isPotentialUpperCaseError(i, tokens, lowercaseReadings, isPrecededByModalOrAuxiliary)) {
          continue;
        }
      } else if (analyzedToken.hasPosTagStartingWith("SUB:") &&
                 i < tokens.length-1 &&
                 Character.isLowerCase(tokens[i+1].getToken().charAt(0)) &&
                 tokens[i+1].matchesPosTagRegex("(VER:[123]:|PA2).+")) {
        // "Viele Minderjährige sind" but not "Das wirklich Wichtige Verfahren ist"
        continue;  
      }
      if (analyzedToken.getAnalyzedToken(0).getPOSTag() == null && lowercaseReadings == null) {
        continue;
      }
      if (analyzedToken.getAnalyzedToken(0).getPOSTag() == null && lowercaseReadings != null
          && (lowercaseReadings.getAnalyzedToken(0).getPOSTag() == null || analyzedToken.getToken().endsWith("innen"))) {
        continue;  // unknown word, probably a name etc.
      }
      potentiallyAddUppercaseMatch(ruleMatches, tokens, i, analyzedToken, token, lowercaseReadings, sentence);
    }
    return toRuleMatchArray(ruleMatches);
  }

  private int getTokensWithPosTagStartingWithCount(AnalyzedTokenReadings[] tokens, String partialPosTag) {
    return Arrays.stream(tokens).filter(token -> token.hasPosTagStartingWith(partialPosTag)).mapToInt(e -> 1).sum();
  }

  private boolean isPotentialUpperCaseError (int pos, AnalyzedTokenReadings[] tokens,
      AnalyzedTokenReadings lowercaseReadings, boolean isPrecededByModalOrAuxiliary) {
    if (pos <= 1) {
      return false;
    }

    // "Das ist zu Prüfen." but not "Das geht zu Herzen."
    if ("zu".equals(tokens[pos-1].getToken()) &&
      !tokens[pos].matchesPosTagRegex(".*(NEU|MAS|FEM)$") &&
      lowercaseReadings != null &&
      lowercaseReadings.hasPosTagStartingWith("VER:INF")) {
      return true;
    }
    if (tokens[pos].getToken().matches(".+verhalten")) {
      return false;
    }
    // find error in: "Man müsse Überlegen, wie man das Problem löst."
    boolean isPotentialError = pos < tokens.length - 3
        && tokens[pos+1].getToken().equals(",")
        && StringUtils.equalsAny(tokens[pos+2].getToken(), INTERROGATIVE_PARTICLES)
        && tokens[pos-1].hasPosTagStartingWith("VER:MOD")
        && !tokens[pos-1].hasLemma("mögen")
        && !tokens[pos+3].getToken().equals("zum");
    if (!isPotentialError &&
        lowercaseReadings != null
        && tokens[pos].hasAnyPartialPosTag("SUB:NOM:SIN:NEU:INF", "SUB:DAT:PLU:")
        && ("zu".equals(tokens[pos-1].getToken()) || hasPartialTag(tokens[pos-1], "SUB", "EIG", "VER:AUX:3:", "ADV:TMP", "ABK"))) {
      // find error in: "Der Brief wird morgen Übergeben." / "Die Ausgaben haben eine Mrd. Euro Überschritten."
      isPotentialError |= lowercaseReadings.hasPosTag("PA2:PRD:GRU:VER") && !tokens[pos-1].hasPosTagStartingWith("VER:AUX:3") && !lowercaseReadings.hasPosTag("VER:3:PLU:PRT:NON");
      // find error in: "Er lässt das Arktisbohrverbot Überprüfen."
      // find error in: "Sie bat ihn, es zu Überprüfen."
      // find error in: "Das Geld wird Überwiesen."
      isPotentialError |= (pos >= tokens.length - 2 || ",".equals(tokens[pos+1].getToken()))
        && ("zu".equals(tokens[pos-1].getToken()) || isPrecededByModalOrAuxiliary)
        && tokens[pos].getToken().startsWith("Über")
        && lowercaseReadings.hasAnyPartialPosTag("VER:INF:", "PA2:PRD:GRU:VER");
      }
    return isPotentialError;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns.get();
  }

  // e.g. "Ein Kaninchen, das zaubern kann" - avoid false alarm here
  //                          ^^^^^^^
  private boolean isPrevProbablyRelativePronoun(AnalyzedTokenReadings[] tokens, int i) {
    return i >= 3 &&
      tokens[i-1].getToken().equals("das") &&
      tokens[i-2].getToken().equals(",") &&
      tokens[i-3].matchesPosTagRegex("SUB:...:SIN:NEU");
  }

  private boolean isSalutation(String token) {
    return StringUtils.equalsAny(token, "Herr", "Hr", "Herrn", "Frau", "Fr", "Fräulein");
  }

  private boolean isCompany(String token) {
    return StringUtils.equalsAny(token, "Firma", "Familie", "Unternehmen", "Firmen", "Bäckerei", "Metzgerei", "Fa");
  }

  private boolean isDot(String token) {
    return token.equals(".");
  }

  private boolean hasNounReading(AnalyzedTokenReadings readings) {
    if (readings != null) {
      // Anmeldung bis Fr. 1.12. (Fr. as abbreviation of Freitag is has a noun reading!)
      if (readings.hasPosTagStartingWith("ABK") && readings.hasPartialPosTag("SUB")) {
        return true;
      }
      // "Die Schöne Tür": "Schöne" also has a noun reading but like "SUB:AKK:SIN:FEM:ADJ", ignore that:
      AnalyzedTokenReadings allReadings = lookup(readings.getToken().replaceAll("\\u00AD", ""));  // unification in disambiguation.xml removes reading, so look up again, removing soft hyphens
      if (allReadings != null) {
        for (AnalyzedToken reading : allReadings) {
          String posTag = reading.getPOSTag();
          if (posTag != null && posTag.contains("SUB:") && !posTag.contains(":ADJ")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void potentiallyAddLowercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings tokenReadings, boolean prevTokenIsDas, String token, boolean nextTokenIsPersonalOrReflexivePronoun, AnalyzedSentence sentence) {
    // e.g. essen -> Essen
    if (prevTokenIsDas &&
        !nextTokenIsPersonalOrReflexivePronoun &&
        Character.isLowerCase(token.charAt(0)) &&
        !substVerbenExceptions.contains(token) &&
        tokenReadings.hasPosTagStartingWith("VER:INF") &&
        !tokenReadings.isIgnoredBySpeller() &&
        !tokenReadings.isImmunized()) {
      addRuleMatch(ruleMatches, sentence, LOWERCASE_MESSAGE, tokenReadings, StringTools.uppercaseFirstChar(tokenReadings.getToken()));
    }
  }

  private void potentiallyAddUppercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings analyzedToken, String token, AnalyzedTokenReadings lowercaseReadings, AnalyzedSentence sentence) {
    boolean isUpperFirst = Character.isUpperCase(token.charAt(0));
    String lcWord = StringTools.lowercaseFirstChar(tokens[i].getToken());
    if (isUpperFirst &&
        token.length() > 1 &&     // length limit = ignore abbreviations
        !tokens[i].isIgnoredBySpeller() &&
        !tokens[i].isImmunized() &&
        !StringUtils.equalsAny(tokens[i - 1].getToken(), SENTENCE_START_EXCEPTIONS) &&
        !StringUtils.equalsAny(token, exceptions) &&
        !StringTools.isAllUppercase(token) &&
        !isLanguage(i, tokens, token) &&
        !isProbablyCity(i, tokens, token) &&
        !GermanHelper.hasReadingOfType(analyzedToken, POSType.PROPER_NOUN) &&
        !analyzedToken.isSentenceEnd() &&
        !isEllipsis(i, tokens) &&
        !isNumbering(i, tokens) &&
        !isNominalization(i, tokens, token, lowercaseReadings) &&
        !isAdverbAndNominalization(i, tokens) &&
        !isSpecialCase(i, tokens) &&
        !isAdjectiveAsNoun(i, tokens, lowercaseReadings) &&
        !isSingularImperative(lowercaseReadings, tokens[i]) &&  // too many names like "Kusch", "Klemm" etc.
        !isExceptionPhrase(i, tokens) &&
        !(i == 2 && "“".equals(tokens[i-1].getToken())) &&   // closing quote at sentence start (https://github.com/languagetool-org/languagetool/issues/2558)
        !isCaseTypo(tokens[i].getToken()) &&
        !followedByGenderGap(tokens, i) &&
        !isNounWithVerbReading(i, tokens) &&
        !speller.isMisspelled(lcWord)) {
      if (":".equals(tokens[i - 1].getToken())) {
        AnalyzedTokenReadings[] subarray = new AnalyzedTokenReadings[i];
        System.arraycopy(tokens, 0, subarray, 0, i);
        if (isVerbFollowing(i, tokens, lowercaseReadings) || getTokensWithPosTagStartingWithCount(subarray, "VER") == 0) {
          // no error
        } else {
          addRuleMatch(ruleMatches, sentence, COLON_MESSAGE, tokens[i], lcWord);
        }
        return;
      }
      addRuleMatch(ruleMatches, sentence, UPPERCASE_MESSAGE, tokens[i], lcWord);
    }
  }

  private boolean followedByGenderGap(AnalyzedTokenReadings[] tokens, int i) {
    if (i + 2 < tokens.length && tokens[i+1].getToken().equals(":") && tokens[i+2].getToken().matches("in|innen")) {
      return true;
    }
    return false;
  }

  private boolean isCaseTypo(String token) {
    return token.matches("[A-ZÖÄÜ][A-ZÖÄÜ][a-zöäüß-]+");   // e.g. "WUrzeln"
  }

  private boolean isSingularImperative(AnalyzedTokenReadings lowercaseReadings, AnalyzedTokenReadings token) {
    return lowercaseReadings != null && lowercaseReadings.hasPosTagStartingWith("VER:IMP:SIN") &&
              !"Ein".equals(token.getToken()) && !"Eine".equals(token.getToken());
  }

  private boolean isNounWithVerbReading(int i, AnalyzedTokenReadings[] tokens) {
    return tokens[i].hasPosTagStartingWith("SUB") &&
    		tokens[i].hasPosTagStartingWith("VER:INF");
	}

	private boolean isVerbFollowing(int i, AnalyzedTokenReadings[] tokens, AnalyzedTokenReadings lowercaseReadings) {
    AnalyzedTokenReadings[] subarray = new AnalyzedTokenReadings[ tokens.length - i ];
    System.arraycopy(tokens, i, subarray, 0, subarray.length);
    if (lowercaseReadings != null) {
      subarray[0] = lowercaseReadings;
    }
    // capitalization after ":" requires an independent clause to follow
    // if there is not a single verb, the tokens cannot be part of an independent clause
    return getTokensWithPosTagStartingWithCount(subarray, "VER:") != 0;
}

  private void addRuleMatch(List<RuleMatch> ruleMatches, AnalyzedSentence sentence, String msg, AnalyzedTokenReadings tokenReadings, String fixedWord) {
    RuleMatch ruleMatch = new RuleMatch(this, sentence, tokenReadings.getStartPos(), tokenReadings.getEndPos(), msg);
    ruleMatch.setSuggestedReplacement(fixedWord);
    ruleMatches.add(ruleMatch);
  }

  // e.g. "a) bla bla"
  private boolean isNumbering(int i, AnalyzedTokenReadings[] tokens) {
    return i >= 2
            && StringUtils.equalsAny(tokens[i-1].getToken(), ")", "]")
            && NUMERALS_EN.matcher(tokens[i-2].getToken()).matches()
            && !(i > 3 && tokens[i-3].getToken().equals("(")
              && tokens[i-4].hasPosTagStartingWith("SUB:")); // no numbering "Der Vater (51) fuhr nach Rom."
  }

  private boolean isEllipsis(int i, AnalyzedTokenReadings[] tokens) {
    return StringUtils.equalsAny(tokens[i-1].getToken(), "]", ")") && // sentence starts with […]
           ((i == 4 && tokens[i-2].getToken().equals("…")) || (i == 6 && tokens[i-2].getToken().equals(".")));
  }

  private boolean isNominalization(int i, AnalyzedTokenReadings[] tokens, String token, AnalyzedTokenReadings lowercaseReadings) {
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    // TODO: "vor Schlimmerem", "Er hatte Schlimmes zu befürchten"
    // TODO: wir finden den Fehler in "Die moderne Wissenschaftlich" nicht, weil nicht alle
    // Substantivierungen in den Morphy-Daten stehen (z.B. "Größte" fehlt) und wir deshalb nur
    // eine Abfrage machen, ob der erste Buchstabe groß ist.
    if (StringTools.startsWithUppercase(token) && !isNumber(token) && !(hasNounReading(nextReadings) ||
        (nextReadings != null && StringUtils.isNumeric(nextReadings.getToken()))) && !token.matches("Alle[nm]")) {
      if (lowercaseReadings != null && lowercaseReadings.hasPosTag("PRP:LOK+TMP+CAU:DAT+AKK")) {
        return false;
      }
      // Ignore "das Dümmste, was je..." but not "das Dümmste Kind"
      AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
      AnalyzedTokenReadings prevPrevToken = i >= 2 ? tokens[i-2] : null;
      AnalyzedTokenReadings prevPrevPrevToken = i >= 3 ? tokens[i-3] : null;
      String prevTokenStr = prevToken != null ? prevToken.getToken() : "";
      if (StringUtils.equalsAny(prevTokenStr, "und", "oder", "beziehungsweise") && prevPrevToken != null &&
          (tokens[i].hasPartialPosTag("SUB") && tokens[i].hasPartialPosTag(":ADJ")) || //"das dabei Erlernte und Erlebte ist ..." -> 'Erlebte' is correct here
          (prevPrevToken.hasPartialPosTag("SUB") && !hasNounReading(nextReadings) && // "die Ausgaben für Umweltschutz und Soziales"
              lowercaseReadings != null && lowercaseReadings.hasPartialPosTag("ADJ") && !prevTokenStr.equals(","))) {
       return true;
     }
      if (lowercaseReadings != null && lowercaseReadings.hasPosTag("PA1:PRD:GRU:VER")) {
        // "aus sechs Überwiegend muslimischen Ländern"
        return false;
      }
      return ((prevToken != null && prevTokenStr.matches("irgendwelche|irgendwas|irgendein|weniger?|einiger?|mehr|aufs") && tokens[i].hasPartialPosTag("SUB"))
              || isNumber(prevTokenStr)) ||
         (hasPartialTag(prevToken, "ART", "PRO:") && !(((i < 4 && tokens.length > 4) || prevToken.getReadings().size() == 1 || prevPrevToken.hasLemma("sein")) && prevToken.hasPosTagStartingWith("PRO:PER:NOM:"))  && !prevToken.hasPartialPosTag(":STD")) ||  // "die Verurteilten", "etwas Verrücktes", "ihr Bestes"
         (hasPartialTag(prevPrevPrevToken, "ART") && hasPartialTag(prevPrevToken, "PRP") && hasPartialTag(prevToken, "SUB")) || // "die zum Tode Verurteilten"
         (hasPartialTag(prevPrevToken, "PRO:", "PRP") && hasPartialTag(prevToken, "ADJ", "ADV", "PA2", "PA1")) ||  // "etwas schön Verrücktes", "mit aufgewühltem Innerem"
         (hasPartialTag(prevPrevPrevToken, "PRO:", "PRP") && hasPartialTag(prevPrevToken, "ADJ", "ADV") && hasPartialTag(prevToken, "ADJ", "ADV", "PA2")) || // "etwas ganz schön Verrücktes"
         (tokens[i].hasPosTagStartingWith("SUB:") && hasPartialTag(prevToken, "GEN") && !hasPartialTag(nextReadings, "PKT")); // "Parks Vertraute Choi Soon Sil ist zu drei Jahren Haft verurteilt worden."
    }
    return false;
  }

  private boolean isNumber(String token) {
    if (StringUtils.isNumeric(token)) {
      return true;
    }
    AnalyzedTokenReadings lookup = lookup(StringTools.lowercaseFirstChar(token));
    return lookup != null && lookup.hasPosTag("ZAL");
  }

  private boolean isAdverbAndNominalization(int i, AnalyzedTokenReadings[] tokens) {
    String prevPrevToken = i > 1 ? tokens[i-2].getToken() : "";
    AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
    String token = tokens[i].getToken();
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    // ignore "das wirklich Wichtige":
    return "das".equalsIgnoreCase(prevPrevToken) && hasPartialTag(prevToken, "ADV")
            && StringTools.startsWithUppercase(token) && !hasNounReading(nextReadings);
  }

  private boolean hasPartialTag(AnalyzedTokenReadings token, String... posTags) {
    if (token != null) {
      for (String posTag : posTags) {
        if (token.hasPartialPosTag(posTag)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isSpecialCase(int i, AnalyzedTokenReadings[] tokens) {
    String prevToken = i > 1 ? tokens[i-1].getToken() : "";
    String token = tokens[i].getToken();
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    // ignore "im Allgemeinen gilt" but not "im Allgemeinen Fall":
    return "im".equalsIgnoreCase(prevToken) && "Allgemeinen".equals(token) && !hasNounReading(nextReadings);
  }

  private boolean isAdjectiveAsNoun(int i, AnalyzedTokenReadings[] tokens, AnalyzedTokenReadings lowercaseReadings) {
    AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    AnalyzedTokenReadings prevLowercaseReadings = null;

    if (i > 1 && StringUtils.equalsAny(tokens[i-2].getToken(), SENTENCE_START_EXCEPTIONS)) {
      prevLowercaseReadings = lookup(prevToken.getToken().toLowerCase());
    }

    // ignore "Der Versuch, Neues zu lernen / Gutes zu tun / Spannendes auszuprobieren"
    boolean isPossiblyFollowedByInfinitive = nextReadings != null && nextReadings.getToken().equals("zu");
    boolean isFollowedByInfinitive = nextReadings != null && !isPossiblyFollowedByInfinitive && nextReadings.hasPartialPosTag("EIZ");
    boolean isFollowedByPossessiveIndicator = nextReadings != null && StringUtils.equalsAny(nextReadings.getToken(),POSSESSIVE_INDICATORS);

    boolean isUndefQuantifier = prevToken != null && StringUtils.equalsAny(prevToken.getToken().toLowerCase(), UNDEFINED_QUANTIFIERS);
    boolean isPrevDeterminer = prevToken != null
                               && (hasPartialTag(prevToken, "ART", "PRP", "ZAL") || hasPartialTag(prevLowercaseReadings, "ART", "PRP", "ZAL"))
                               && !prevToken.hasPartialPosTag(":STD");
    boolean isPrecededByVerb = prevToken != null && prevToken.matchesPosTagRegex("VER:(MOD:|AUX:)?[1-3]:.*") && !prevToken.hasLemma("sein");
    if (!isPrevDeterminer && !isUndefQuantifier && !(isPossiblyFollowedByInfinitive || isFollowedByInfinitive)
        && !(isPrecededByVerb && lowercaseReadings != null && hasPartialTag(lowercaseReadings, "ADJ:", "PA") && nextReadings != null &&
             !StringUtils.equalsAny(nextReadings.getToken(), "und", "oder", ","))
        && !(isFollowedByPossessiveIndicator && hasPartialTag(lowercaseReadings, "ADJ", "VER")) // "Wacht auf, Verdammte dieser Welt!"
        && !(prevToken != null && prevToken.hasPosTag("KON:UNT") && !hasNounReading(nextReadings) && nextReadings != null && !nextReadings.hasPosTag("KON:NEB"))) {
      AnalyzedTokenReadings prevPrevToken = i > 1 && prevToken != null && prevToken.hasPartialPosTag("ADJ") ? tokens[i-2] : null;
      // Another check to avoid false alarms for "eine Gruppe Aufständischer starb"
      if (!isPrecededByVerb && lowercaseReadings != null && prevToken != null) {
        if (prevToken.hasPartialPosTag("SUB:") && lowercaseReadings.matchesPosTagRegex("(ADJ|PA2):GEN:PLU:MAS:GRU:SOL.*")) {
          return nextReadings != null && !nextReadings.hasPartialPosTag("SUB:");
        } else if (nextReadings != null && nextReadings.getReadingsLength() == 1 && prevToken.hasPosTagStartingWith("PRO:PER:NOM:") && nextReadings.hasPosTag("ADJ:PRD:GRU")) {
          // avoid false alarm "Weil er Unmündige sexuell missbraucht haben soll,..."
          return true;
        }
      }
      // Another check to avoid false alarms for "ein politischer Revolutionär"
      if (!hasPartialTag(prevPrevToken, "ART", "PRP", "ZAL")) {
        return false;
      }
    }

    // ignore "die Ausgewählten" but not "die Ausgewählten Leute":
    for (AnalyzedToken reading : tokens[i].getReadings()) {
      String posTag = reading.getPOSTag();
      if ((posTag == null || posTag.contains("ADJ")) && !hasNounReading(nextReadings) && !StringUtils.isNumeric(nextReadings != null ? nextReadings.getToken() : "")) {
        if(posTag == null && hasPartialTag(lowercaseReadings, "PRP:LOK", "PA2:PRD:GRU:VER", "PA1:PRD:GRU:VER", "ADJ:PRD:KOM", "ADV:TMP")) {
          // skip to avoid a false true for, e.g. "Die Zahl ging auf Über 1.000 zurück."/ "Dies gilt schon lange als Überholt." / "Bis Bald!"
          // but not for "Er versuchte, Neues zu wagen."
        } else {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isLanguage(int i, AnalyzedTokenReadings[] tokens, String token) {
    boolean maybeLanguage = (token.endsWith("sch") && LanguageNames.get().contains(token)) ||
                            LanguageNames.get().contains(StringUtils.removeEnd(StringUtils.removeEnd(token, "n"), "e"));   // z.B. "im Japanischen" / z.B. "ins Japanische übersetzt"
    AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    return maybeLanguage && (!hasNounReading(nextReadings) || (prevToken != null && prevToken.getToken().equals("auf")));
  }

  private boolean isProbablyCity(int i, AnalyzedTokenReadings[] tokens, String token) {
    boolean hasCityPrefix = StringUtils.equalsAny(token, "Klein", "Groß", "Neu");
    if (hasCityPrefix) {
      AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
      return nextReadings != null && (!nextReadings.isTagged() || nextReadings.hasPosTagStartingWith("EIG"));
    }
    return false;
  }

  private boolean isFollowedByRelativeOrSubordinateClause(int i, AnalyzedTokenReadings[] tokens) {
    if (i < tokens.length - 4) {
      return ",".equals(tokens[i+1].getToken())
             && (StringUtils.equalsAny(tokens[i+2].getToken(),INTERROGATIVE_PARTICLES) || tokens[i+2].hasPosTag("KON:UNT"));
    }
    return false;
  }

  private boolean isExceptionPhrase(int i, AnalyzedTokenReadings[] tokens) {
    for (StringMatcher[] patterns : exceptionPatterns) {
      for (int j = 0; j < patterns.length; j++) {
        if (patterns[j].matches(tokens[i].getToken())) {
          int startIndex = i-j;
          if (compareLists(tokens, startIndex, startIndex+patterns.length-1, patterns)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @VisibleForTesting
  static boolean compareLists(AnalyzedTokenReadings[] tokens, int startIndex, int endIndex, StringMatcher... patterns) {
    if (startIndex < 0) {
      return false;
    }
    int i = 0;
    for (int j = startIndex; j <= endIndex; j++) {
      if (i >= patterns.length || j >= tokens.length || !patterns[i].matches(tokens[j].getToken())) {
        return false;
      }
      i++;
    }
    return true;
  }

  private AnalyzedTokenReadings lookup(String word) {
    try {
      return tagger.lookup(word);
    } catch (IOException e) {
      throw new RuntimeException("Could not lookup '" + word + "'.", e);
    }
  }
}
