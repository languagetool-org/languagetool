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
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.de.GermanToken;
import org.languagetool.tagging.de.GermanToken.POSType;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

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

  private static final String UPPERCASE_MESSAGE = "Außer am Satzanfang werden nur Nomen und Eigennamen großgeschrieben";
  private static final String LOWERCASE_MESSAGE = "Falls es sich um ein substantiviertes Verb handelt, wird es großgeschrieben.";
  private static final String COLON_MESSAGE = "Folgt dem Doppelpunkt weder ein Substantiv noch eine wörtliche Rede oder ein vollständiger Hauptsatz, schreibt man klein weiter.";

  // also see case_rule_exceptions.txt:
  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      // see https://www.duden.de/suchen/dudenonline/u-f%C3%B6rmig
      regex("[A-Z]-förmig(e[mnrs]?)?")
    ),
    Arrays.asList(
      token("Geboten")
    ),
    Arrays.asList(
      // see http://www.lektorenverband.de/die-deutsche-rechtschreibung-was-ist-neu/
      // and http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Goldenen?"),
      regex("Hochzeit(en)?")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Graue[nr]?"),
      regex("Stars?|Eminenz")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Große[nr]?"),
      regex("Strafkammer|Latinums?|Rats?")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      csToken("Guten"),
      csToken("Tag")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Höheren?"),
      regex("Schule|Mathematik")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Künstliche[nr]?"),
      token("Intelligenz")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Neue[ns]?"),
      token("Jahr(s|es)?|Linken?")
    ),
    Arrays.asList(
      token("Neues"),
      token("\\?")
    ),
    Arrays.asList(
        token("Zahl"),
        pos("UNKNOWN")
    ),
    Arrays.asList(
        token(","),
        posRegex(".*ADJ.*|UNKNOWN"),
        regex("[\\.?!]")
    ),
    Arrays.asList(
        csToken(","),
        regex("[md]eine?|du"),
        posRegex(".*ADJ.*|UNKNOWN"),
        regex("[\\.?!]")
    ),
    Arrays.asList(
       posRegex(".*ADJ.*|UNKNOWN"),
       regex("Konstanten?")
    ),
    Arrays.asList(
        token("das"),
        posRegex("PA2:.*"),
        posRegex("VER:AUX:.*")
    ),
    Arrays.asList(
        // Er fragte,ob das gelingen wird.
        csToken("das"),
        posRegex("VER:.*"),
        posRegex("VER:AUX:.*"),
        posRegex("PKT|KON:NEB")
    ),
    Arrays.asList(
        // Er fragte, ob das gelingen oder scheitern wird.
        csToken("das"),
        posRegex("VER:.+"),
        new PatternTokenBuilder().pos("KON:NEB").setSkip(5).build(),
        posRegex("VER:AUX:.*"),
        posRegex("PKT|KON:NEB")
    ),
    Arrays.asList(
        // um ihren eigenen Glauben an das Gute, Wahre und Schöne zu stärken.
        token("das"),
        posRegex("SUB:.+"),
        token(","),
        regex("[A-ZÄÖÜ][a-zäöü]+"),
        regex("und|oder")
    ),
    Arrays.asList(
      // "... weshalb ihr das wissen wollt."
      pos("VER:INF:NON"),
      pos("VER:MOD:2:PLU:PRÄ")
    ),
    Arrays.asList(
      pos("UNKNOWN"),
      token("und"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      // "... wie ich das prüfen sollte."
      posRegex("VER:INF.+"),
      posRegex("VER:MOD:.+")
    ),
    Arrays.asList(
        // "... wie ich das prüfen würde."
        posRegex("VER:INF.+"),
        posRegex("VER:AUX:.:(SIN|PLU)(:KJ2)?")
    ),
    Arrays.asList(
       // "... etwas Interessantes und Spannendes suchte"
       regex("etwas|nichts|viel|wenig|allerlei|was"),
       regex("[A-ZÄÖÜ].*es"),
       regex("und|oder|,"),
       regex("[A-ZÄÖÜ].*es")
    ),
    Arrays.asList(
       // "... bringt Interessierte und Experten zusammen"
       posRegex("VER:.*[1-3]:.*"),
       posRegex("SUB:AKK:.+:ADJ"),
       regex("und|oder|,"),
       posRegex("SUB:AKK:.+:(NEU|FEM|MAS)|ART:.*")
    ),
    Arrays.asList(
        // "Das südöstlich von Berlin gelegene"
        regex("(süd|nord|ost|west).*lich"),
        token("von")
     ),
     Arrays.asList(
        // "Entscheiden 42,5 Millionen Stimmberechtigte über..."
        regex("Million(en)?"),
        posRegex("SUB:.*:ADJ")
     ),
     Arrays.asList(
        // "Vor Betreten des" / "Trotz Verboten seiner Eltern"
        posRegex("PRP:.+|ADV:MOD"),
        pos("VER:PA2:NON"),
        posRegex("(ART|PRO):(IND|DE[FM]|POS):GEN:.*")
     ),
     Arrays.asList(
        // "Er liebt UV-bestrahltes, Na-haltiges und Makeup-freies Obst."
        // "Er vertraut auf CO2-arme Wasserkraft"
        regex("[A-ZÄÖÜ0-9]+[a-zäöüß0-9]-[a-zäöüß]+")
     ),
     Arrays.asList(
       // "Das Aus für Italien kam unerwartet." / "Müller drängt auf Aus bei Pflichtmitgliedschaft"
       regex("auf|das|vor|a[mn]"),
       csToken("Aus"),
       posRegex("^PRP:.+|VER:[1-3]:.+")
     ),
     Arrays.asList(
       // "Bündnis 90/Die Grünen"
       csToken("90"),
       csToken("/"),
       csToken("Die")
     ),
     Arrays.asList(
       // https://de.wikipedia.org/wiki/Neue_Mittelschule
       regex("Neue[nrs]?"),
       new PatternTokenBuilder().tokenRegex("Mitte(lschule)?|Rathaus|Testament|Welt|Markt|Rundschau").matchInflectedForms().build()
     ),
     Arrays.asList( // "Das schließen Forscher aus ..."
       new PatternTokenBuilder().token("das").build(),
       new PatternTokenBuilder().posRegex("VER:INF:(SFT|NON)").build(), 
       new PatternTokenBuilder().posRegex("SUB:NOM:PLU:.+|ADV:MOD").build()
    ),
    Arrays.asList( // "Tausende Gläubige kamen, um ihn zu sehen."
      new PatternTokenBuilder().tokenRegex("[tT]ausende?").build(),
      new PatternTokenBuilder().posRegex("SUB:NOM:.+").build(), 
      new PatternTokenBuilder().posRegex(JLanguageTool.SENTENCE_END_TAGNAME+"|VER:[1-3]:.+").build()
   ),
    Arrays.asList( // "Er befürchtete Schlimmeres."
      regex("Schlimm(er)?es"), 
      pos(JLanguageTool.SENTENCE_END_TAGNAME)
    ),
    Arrays.asList(
      regex("Angehörige[nr]?")
    ),
    Arrays.asList( // aus Alt wird neu
      csToken("Alt"),
      regex("mach|w[iu]rde?"),
      csToken("Neu")
    ),
    Arrays.asList( // see GermanTagger.getSubstantivatedForms
      pos("SUB:NOM:SIN:MAS:ADJ"),
      posRegex("PRP:.+")
    ),
    Arrays.asList( // Einen Tag nach Bekanntwerden des Skandals
      pos("ZUS"),
      csToken("Bekanntwerden")
    ),
    Arrays.asList( // Das ist also ihr Zuhause.
      posRegex(".+:(POS|GEN):.+"),
      csToken("Zuhause")
    ),
    Arrays.asList( // Ein anderes Zuhause habe ich nicht.
      regex("altes|anderes|k?ein|neues"),
      csToken("Zuhause")
    ),
    Arrays.asList( // Weil er das kommen sah, traf er Vorkehrungen.
      csToken("das"),
      csToken("kommen"),
      new PatternTokenBuilder().csToken("sehen").matchInflectedForms().build()
    ),
    Arrays.asList(
      token("auf"),
      csToken("die"),
      csToken("Schnelle")
    ),
    Arrays.asList( // denn es fehlt bis heute am Nötigsten
    	new PatternTokenBuilder().csToken("fehlen").matchInflectedForms().setSkip(3).build(),
      csToken("am"),
      csToken("Nötigsten")
    ),
    Arrays.asList(
      csToken("am"),
      csToken("Nötigsten"),
      new PatternTokenBuilder().csToken("fehlen").matchInflectedForms().build()
    )
  );

  private static PatternToken token(String token) {
    return new PatternTokenBuilder().tokenRegex(token).build();
  }

  private static PatternToken csToken(String token) {
    return new PatternTokenBuilder().csToken(token).build();
  }

  private static PatternToken regex(String regex) {
    return new PatternTokenBuilder().tokenRegex(regex).build();
  }

  private static PatternToken pos(String posTag) {
    return new PatternTokenBuilder().pos(posTag).build();
  }

  private static PatternToken posRegex(String posTag) {
    return new PatternTokenBuilder().posRegex(posTag).build();
  }

  static {
    nounIndicators.add("das");
    nounIndicators.add("sein");
    //nounIndicators.add("ihr");    // would cause false alarm e.g. "Auf ihr stehen die Ruinen...", "Ich dachte, dass ihr kommen würdet.", "Ich verdanke ihr meinen Erfolg."
    nounIndicators.add("mein");
    nounIndicators.add("dein");
    nounIndicators.add("euer");
    nounIndicators.add("unser");
  }
  
  private static final Set<String> sentenceStartExceptions = new HashSet<>(Arrays.asList(
      "(", "\"", "'", "‘", "„", "«", "»", "."));

  private static final Set<String> UNDEFINED_QUANTIFIERS = new HashSet<>(Arrays.asList(
      "viel", "nichts", "wenig", "allerlei"));

  private static final Set<String> INTERROGATIVE_PARTICLES = new HashSet<>(Arrays.asList(
      "was", "wodurch", "wofür", "womit", "woran", "worauf", "woraus", "wovon", "wie"));

  private static final Set<String> POSSESSIVE_INDICATORS = new HashSet<>(Arrays.asList(
      "einer", "eines", "der", "des", "dieser", "dieses"));

  private static final Set<String> DAS_VERB_EXCEPTIONS = new HashSet<>(Arrays.asList(
      "nur", "sogar", "auch", "die", "alle", "viele", "zu"));

  /*
   * These are words that Morphy only knows as non-nouns (or not at all).
   * The proper solution is to add all those to our Morphy data, but as a simple
   * workaround to avoid false alarms, these words can be added here.
   */
  private static final Set<String> exceptions = new HashSet<>(Arrays.asList(
    "Str",
    "Auszubildende",
    "Auszubildender",
    "Gelehrte",
    "Gelehrter",
    "Vorstehende",
    "Vorstehender",
    "Mitwirkende",
    "Mitwirkender",
    "Mitwirkenden",
    "Selbstständige",
    "Selbstständiger",
    "Genaueres",
    "Äußersten",
    "Dienstreisender",
    "Verletzte",
    "Vermisste",
    "Äußeres",
    "Abseits",
    "Beschäftigter",
    "Beschäftigte",
    "Beschäftigten",
    "Bekannter",
    "Bekannte",
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
    "Jenseits",
    "Abends",
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
    "Gläubiger",
    "Hechte",
    "Herzöge",
    "Herzögen",
    "Hinfahrt",
    "Hundert",   // je nach Kontext groß (TODO) 
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
    "Le",    // "Le Monde" etc
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
    "Vielfache",
    "Vielfaches",
    "Vorsitzender",
    "Fraktionsvorsitzender",
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
  ));
  
  private static final Set<String> languages = new HashSet<>();
    static {
    // TODO: alle Sprachen
    languages.add("Angelsächsisch");
    languages.add("Afrikanisch");
    languages.add("Albanisch");
    languages.add("Altarabisch");
    languages.add("Altchinesisch");
    languages.add("Altgriechisch");
    languages.add("Althochdeutsch");
    languages.add("Altpersisch");
    languages.add("Amerikanisch");
    languages.add("Arabisch");
    languages.add("Armenisch");
    languages.add("Bairisch");
    languages.add("Baskisch");
    languages.add("Bengalisch");
    languages.add("Bulgarisch");
    languages.add("Chinesisch");
    languages.add("Dänisch");
    languages.add("Deutsch");
    languages.add("Englisch");
    languages.add("Estnisch");
    languages.add("Finnisch");
    languages.add("Französisch");
    languages.add("Frühneuhochdeutsch");
    languages.add("Germanisch");
    languages.add("Georgisch");
    languages.add("Griechisch");
    languages.add("Hebräisch");
    languages.add("Hocharabisch");
    languages.add("Hochchinesisch");
    languages.add("Hochdeutsch");
    languages.add("Holländisch");
    languages.add("Indonesisch");
    languages.add("Irisch");
    languages.add("Isländisch");
    languages.add("Italienisch");
    languages.add("Japanisch");
    languages.add("Jiddisch");
    languages.add("Jugoslawisch");
    languages.add("Kantonesisch");
    languages.add("Katalanisch");
    languages.add("Klingonisch");
    languages.add("Koreanisch");
    languages.add("Kroatisch");
    languages.add("Kurdisch");
    languages.add("Lateinisch");
    languages.add("Lettisch");
    languages.add("Litauisch");
    languages.add("Luxemburgisch");
    languages.add("Mittelhochdeutsch");
    languages.add("Mongolisch");
    languages.add("Neuhochdeutsch");
    languages.add("Niederländisch");
    languages.add("Norwegisch");
    languages.add("Persisch");
    languages.add("Plattdeutsch");
    languages.add("Polnisch");
    languages.add("Portugiesisch");
    languages.add("Rätoromanisch");
    languages.add("Rumänisch");
    languages.add("Russisch");
    languages.add("Sächsisch");
    languages.add("Schwäbisch");
    languages.add("Schwedisch");
    languages.add("Schweizerisch");
    languages.add("Serbisch");
    languages.add("Serbokroatisch");
    languages.add("Slawisch");
    languages.add("Slowakisch");
    languages.add("Slowenisch");
    languages.add("Spanisch");
    languages.add("Tamilisch");
    languages.add("Tibetisch");
    languages.add("Tschechisch");
    languages.add("Tschetschenisch");
    languages.add("Türkisch");
    languages.add("Turkmenisch");
    languages.add("Uigurisch");
    languages.add("Ukrainisch");
    languages.add("Ungarisch");
    languages.add("Usbekisch");
    languages.add("Vietnamesisch");
    languages.add("Walisisch");
    languages.add("Weißrussisch");
  }

  private static final Set<Pattern[]> exceptionPatterns = CaseRuleExceptions.getExceptionPatterns();

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
  private final German german;

  public CaseRule(ResourceBundle messages, German german) {
    this.german = german;
    super.setCategory(Categories.CASING.getCategory(messages));
    this.tagger = (GermanTagger) german.getTagger();
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
    return Tools.getUrl("http://www.canoonet.eu/services/GermanSpelling/Regeln/Gross-klein/index.html");
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
      if (i > 0 && isSalutation(tokens[i-1].getToken())) {   // e.g. "Frau Stieg" could be a name, ignore
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
              && (DAS_VERB_EXCEPTIONS.contains(nextToken.getToken()) ||
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
            (prevTokenIsDas && getTokensWithPartialPosTagCount(tokens, "VER") == 1)) {// ignore sentences containing a single verb, e.g., "Das wissen viele nicht."
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
                 tokens[i+1].matchesPosTagRegex("VER:[123]:.+")) {
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

  private int getTokensWithPartialPosTagCount(AnalyzedTokenReadings[] tokens, String partialPosTag) {
    return Arrays.stream(tokens).filter(token -> token.hasPartialPosTag(partialPosTag)).mapToInt(e -> 1).sum();
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
    // find error in: "Man müsse Überlegen, wie man das Problem löst."
    boolean isPotentialError = pos < tokens.length - 3
        && tokens[pos+1].getToken().equals(",")
        && INTERROGATIVE_PARTICLES.contains(tokens[pos+2].getToken())
        && tokens[pos-1].hasPosTagStartingWith("VER:MOD")
        && !tokens[pos-1].hasLemma("mögen")
        && !tokens[pos+3].getToken().equals("zum");
    if (!isPotentialError &&
        lowercaseReadings != null
        && tokens[pos].hasAnyPartialPosTag("SUB:NOM:SIN:NEU:INF", "SUB:DAT:PLU:")
        && ("zu".equals(tokens[pos-1].getToken()) || hasPartialTag(tokens[pos-1], "SUB", "EIG", "VER:AUX:3:", "ADV:TMP", "ABK"))) {
      // find error in: "Der Brief wird morgen Übergeben." / "Die Ausgaben haben eine Mrd. Euro Überschritten."
      isPotentialError |= lowercaseReadings.hasPosTag("PA2:PRD:GRU:VER") && !tokens[pos-1].hasPosTagStartingWith("VER:AUX:3");
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
    return makeAntiPatterns(ANTI_PATTERNS, german);
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
    return StringUtils.equalsAny(token, "Herr", "Herrn", "Frau");
  }

  private boolean hasNounReading(AnalyzedTokenReadings readings) {
    if (readings != null) {
      // Anmeldung bis Fr. 1.12. (Fr. as abbreviation of Freitag is has a noun reading!)
      if (readings.hasPosTagStartingWith("ABK") && readings.hasPartialPosTag("SUB")) {
        return true;
      }
      // "Die Schöne Tür": "Schöne" also has a noun reading but like "SUB:AKK:SIN:FEM:ADJ", ignore that:
      AnalyzedTokenReadings allReadings = lookup(readings.getToken());  // unification in disambiguation.xml removes reading, so look up again
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
    if (isUpperFirst &&
        token.length() > 1 &&     // length limit = ignore abbreviations
        !tokens[i].isIgnoredBySpeller() &&
        !tokens[i].isImmunized() &&
        !sentenceStartExceptions.contains(tokens[i - 1].getToken()) &&
        !exceptions.contains(token) &&
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
        !isExceptionPhrase(i, tokens)) {
      String fixedWord = StringTools.lowercaseFirstChar(tokens[i].getToken());
      if (":".equals(tokens[i - 1].getToken())) {
        AnalyzedTokenReadings[] subarray = new AnalyzedTokenReadings[i];
        System.arraycopy(tokens, 0, subarray, 0, i);
        if (isVerbFollowing(i, tokens, lowercaseReadings) || getTokensWithPartialPosTagCount(subarray, "VER") == 0) {
          // no error
        } else {
          addRuleMatch(ruleMatches, sentence, COLON_MESSAGE, tokens[i], fixedWord);
        }
        return;
      }
      addRuleMatch(ruleMatches, sentence, UPPERCASE_MESSAGE, tokens[i], fixedWord);
    }
  }

  private boolean isVerbFollowing(int i, AnalyzedTokenReadings[] tokens, AnalyzedTokenReadings lowercaseReadings) {
    AnalyzedTokenReadings[] subarray = new AnalyzedTokenReadings[ tokens.length - i ];
    System.arraycopy(tokens, i, subarray, 0, subarray.length);
    if (lowercaseReadings != null) {
      subarray[0] = lowercaseReadings;
    }
    // capitalization after ":" requires an independent clause to follow
    // if there is not a single verb, the tokens cannot be part of an independent clause
    return getTokensWithPartialPosTagCount(subarray, "VER:") != 0;
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
              lowercaseReadings != null && lowercaseReadings.hasPartialPosTag("ADJ"))) {
       return true;
     }
      if (lowercaseReadings != null && lowercaseReadings.hasPosTag("PA1:PRD:GRU:VER")) {
        // "aus sechs Überwiegend muslimischen Ländern"
        return false;
      }
      return (prevToken != null && ("irgendwas".equals(prevTokenStr) || "aufs".equals(prevTokenStr) || isNumber(prevTokenStr))) ||
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

    if (i > 1 && sentenceStartExceptions.contains(tokens[i-2].getToken())) {
      prevLowercaseReadings = lookup(prevToken.getToken().toLowerCase());
    }

    // ignore "Der Versuch, Neues zu lernen / Gutes zu tun / Spannendes auszuprobieren"
    boolean isPossiblyFollowedByInfinitive = nextReadings != null && nextReadings.getToken().equals("zu");
    boolean isFollowedByInfinitive = nextReadings != null && !isPossiblyFollowedByInfinitive && nextReadings.hasPartialPosTag("EIZ");
    boolean isFollowedByPossessiveIndicator = nextReadings != null && POSSESSIVE_INDICATORS.contains(nextReadings.getToken());

    boolean isUndefQuantifier = prevToken != null && UNDEFINED_QUANTIFIERS.contains(prevToken.getToken().toLowerCase());
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
        if(posTag == null && hasPartialTag(lowercaseReadings, "PRP:LOK", "PA2:PRD:GRU:VER", "PA1:PRD:GRU:VER", "ADJ:PRD:KOM")) {
          // skip to avoid a false true for, e.g. "Die Zahl ging auf Über 1.000 zurück."/ "Dies gilt schon lange als Überholt."
          // but not for "Er versuchte, Neues zu wagen."
        } else {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isLanguage(int i, AnalyzedTokenReadings[] tokens, String token) {
    boolean maybeLanguage = (token.endsWith("sch") && languages.contains(token)) ||
                            languages.contains(StringUtils.removeEnd(StringUtils.removeEnd(token, "n"), "e"));   // z.B. "im Japanischen" / z.B. "ins Japanische übersetzt"
    AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    return maybeLanguage && (!hasNounReading(nextReadings) || (prevToken != null && prevToken.getToken().equals("auf")));
  }

  private boolean isProbablyCity(int i, AnalyzedTokenReadings[] tokens, String token) {
    boolean hasCityPrefix = StringUtils.equalsAny(token, "Klein", "Groß", "Neu");
    if (hasCityPrefix) {
      AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
      return nextReadings != null && (!nextReadings.isTagged() || nextReadings.hasPartialPosTag("EIG"));
    }
    return false;
  }

  private boolean isFollowedByRelativeOrSubordinateClause(int i, AnalyzedTokenReadings[] tokens) {
    if (i < tokens.length - 4) {
      return ",".equals(tokens[i+1].getToken()) && (INTERROGATIVE_PARTICLES.contains(tokens[i+2].getToken()) || tokens[i+2].hasPosTag("KON:UNT"));
    }
    return false;
  }

  private boolean isExceptionPhrase(int i, AnalyzedTokenReadings[] tokens) {
    for (Pattern[] patterns : exceptionPatterns) {
      for (int j = 0; j < patterns.length; j++) {
        if (patterns[j].matcher(tokens[i].getToken()).matches()) {
          int startIndex = i-j;
          if (compareLists(tokens, startIndex, startIndex+patterns.length-1, patterns)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // non-private for tests
  boolean compareLists(AnalyzedTokenReadings[] tokens, int startIndex, int endIndex, Pattern[] patterns) {
    if (startIndex < 0) {
      return false;
    }
    int i = 0;
    for (int j = startIndex; j <= endIndex; j++) {
      if (i >= patterns.length || j >= tokens.length || !patterns[i].matcher(tokens[j].getToken()).matches()) {
        return false;
      }
      i++;
    }
    return true;
  }

  private AnalyzedTokenReadings lookup(String word) {
    AnalyzedTokenReadings lookupResult = null;
    try {
      lookupResult = tagger.lookup(word);
    } catch (IOException e) {
      throw new RuntimeException("Could not lookup '"+word+"'.", e);
    }
    return lookupResult;
  }
}
