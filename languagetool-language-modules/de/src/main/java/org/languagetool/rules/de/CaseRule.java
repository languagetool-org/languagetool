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

import java.io.IOException;
import java.net.MalformedURLException;
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
          Pattern.compile("[a-z]|[0-9]+|(m{0,4}(cm|cd|d?c{0,3})(xc|xl|l?x{0,3})(ix|iv|v?i{0,3}))$");

  // wenn hinter diesen Wörtern ein Verb steht, ist es wohl ein substantiviertes Verb,
  // muss also groß geschrieben werden:
  private static final Set<String> nounIndicators = new HashSet<>();

  private static final String UPPERCASE_MESSAGE = "Außer am Satzanfang werden nur Nomen und Eigennamen großgeschrieben";
  private static final String LOWERCASE_MESSAGE = "Falls es sich um ein substantiviertes Verb handelt, wird es großgeschrieben.";
  private static final String COLON_MESSAGE = "Folgt dem Doppelpunkt weder ein Substantiv noch eine wörtliche Rede oder ein vollständiger Hauptsatz, schreibt man klein weiter.";

  // also see case_rule_exception.txt:
  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      // see http://www.lektorenverband.de/die-deutsche-rechtschreibung-was-ist-neu/
      // and http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Goldenen?"),
      regex("Hochzeit|Hochzeiten")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Graue[nr]?"),
      regex("Star|Eminenz")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Große[nr]?"),
      regex("Strafkammer|Latinums?")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      token("Guten"),
      token("Tag")
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
      regex("Neuen?"),
      token("Jahr(s|es)?|Linken?")
    ),
    Arrays.asList(
      // see http://www.rechtschreibrat.com/DOX/rfdr_Woerterverzeichnis_2017.pdf
      regex("Roten?"),
      token("Grütze")
    ),
    Arrays.asList(
      regex("Vereinigte[ns]?"),
      regex("Staaten|Königreiche?s?")
    ),
    Arrays.asList(
      token("Den"),
      token("Haag")
    ),
    Arrays.asList(
      token("Neues"),
      token("\\?")
    ),
    Arrays.asList(
      token("Hin"),
      token("und"),
      token("Her")
    ),
    Arrays.asList(
      token("Bares"),
      token("ist"),
      token("Wahres")
    ),
    Arrays.asList(
      token("Auf"),
      token("und"),
      token("Ab")
    ),
    Arrays.asList(
      token("Lug"),
      token("und"),
      token("Trug")
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
        token(","),
        regex("[md]eine?|du"),
        posRegex(".*ADJ.*|UNKNOWN"),
        regex("[\\.?!]")
    ),
    Arrays.asList(
       posRegex(".*ADJ.*|UNKNOWN"),
       regex("Konstanten?")
    ),
    Arrays.asList(
        //token("dass"),
        token("das"),
        posRegex("PA2:.*"),
        posRegex("VER:AUX:.*")
    ),
    Arrays.asList(
        //token("dass"),
        posRegex("PRO:PER:.*|EIG:.*"),
        token("das"),
        posRegex("PA2:.*"),
        posRegex("VER:AUX:.*")
    ),
    Arrays.asList(
        // Er fragte,ob das gelingen wird.
        token("das"),
        posRegex("VER:.*"),
        posRegex("VER:AUX:.*"),
        pos("PKT")
    ),
    Arrays.asList(
        // um ihren eigenen Glauben an das Gute, Wahre und Schöne zu stärken.
        token("das"),
        posRegex("SUB:.*"),
        token(","),
        regex("[A-ZÄÖÜ][a-zäöü]+"),
        regex("und|oder")
    ),
    Arrays.asList(
      token("Treu"),
      token("und"),
      token("Glauben")
    ),
    Arrays.asList(
      token("Speis"),
      token("und"),
      token("Trank")
    ),
    Arrays.asList(
        token("Sang"),
        token("und"),
        token("Klang")
    ),
    Arrays.asList(
        regex("US-amerikanisch(e|er|es|en|em)?")
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
      posRegex("VER:INF.*"),
      posRegex("VER:MOD:.*")
    ),
    Arrays.asList(
        // "... wie ich das prüfen würde."
        posRegex("VER:INF.*"),
        posRegex("VER:AUX:.:(SIN|PLU)(:KJ2)?")
    ),
    Arrays.asList(
       // "... etwas Interessantes und Spannendes suchte"
       regex("etwas|nichts|viel|wenig"),
       regex("[A-ZÄÖÜ].*es"),
       regex("und|oder|,"),
       regex("[A-ZÄÖÜ].*es")
    ),
    Arrays.asList(
       // "... bringt Interessierte und Experten zusammen"
       posRegex("VER:.*[1-3]:.*"),
       posRegex("SUB:AKK:.*:ADJ"),
       regex("und|oder|,"),
       posRegex("SUB:AKK:.*:(NEU|FEM|MAS)|ART:.*")
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
        posRegex("PRP:.*|ADV:MOD"),
        pos("VER:PA2:NON"),
        posRegex("(ART|PRO):(IND|DE[FM]|POS):GEN:.*")
     )
  );

  private static PatternToken token(String token) {
    return new PatternTokenBuilder().tokenRegex(token).build();
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
  
  private static final Set<String> sentenceStartExceptions = new HashSet<>();
  static {
    sentenceStartExceptions.add("(");
    sentenceStartExceptions.add("\"");
    sentenceStartExceptions.add("'");
    sentenceStartExceptions.add("„");
    sentenceStartExceptions.add("«");
    sentenceStartExceptions.add("»");
    sentenceStartExceptions.add(".");
  }

  private static final Set<String> UNDEFINED_QUANTIFIERS = new HashSet<>(Arrays.asList(
      "viel", "nichts", "wenig"));

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
    "Bankangestellter",
    "Bankangestellte",
    "Bankangestellten",
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
    "besonderes",   // je nach Kontext groß (TODO): "etwas Besonderes" 
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
    "Vielfaches",
    "Vorsitzender",
    "Fraktionsvorsitzender",
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
  
  private static final Set<String> myExceptionPhrases = CaseRuleExceptions.getExceptions();

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
  public URL getUrl() {
    try {
      return new URL("http://www.canoo.net/services/GermanSpelling/Regeln/Gross-klein/index.html");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
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
      if (posToken != null && posToken.equals(JLanguageTool.SENTENCE_START_TAGNAME)) {
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

      markLowerCaseNounErrors(ruleMatches, tokens, i, analyzedToken);
      boolean isBaseform = analyzedToken.getReadingsLength() >= 1 && analyzedToken.hasLemma(token);
      if ((analyzedToken.getAnalyzedToken(0).getPOSTag() == null || GermanHelper.hasReadingOfType(analyzedToken, GermanToken.POSType.VERB))
          && isBaseform) {
        boolean nextTokenIsPersonalOrReflexivePronoun = false;
        if (i < tokens.length - 1) {
          AnalyzedTokenReadings nextToken = tokens[i + 1];
          // avoid false alarm for "Das haben wir getan." etc:
          nextTokenIsPersonalOrReflexivePronoun = nextToken.hasPartialPosTag("PRO:PER") || nextToken.getToken().equals("sich") || nextToken.getToken().equals("Sie");
          if ("!?.\",".contains(nextToken.getToken())) {
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
        if (isPrevProbablyRelativePronoun(tokens, i)) {
          continue;
        }
        if (prevTokenIsDas && getTokensWithPartialPosTagCount(tokens, "VER") == 1) {
          // ignore sentences containing a single verb, e.g., "Das wissen viele nicht."
          continue;
        }
        potentiallyAddLowercaseMatch(ruleMatches, tokens[i], prevTokenIsDas, token, nextTokenIsPersonalOrReflexivePronoun);
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
      } else if (analyzedToken.hasPartialPosTag("SUB:") &&
                 i < tokens.length-1 &&
                 Character.isLowerCase(tokens[i+1].getToken().charAt(0)) &&
                 tokens[i+1].matchesPosTagRegex("VER:[123]:.*")) {
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
      potentiallyAddUppercaseMatch(ruleMatches, tokens, i, analyzedToken, token, lowercaseReadings);
    }
    return toRuleMatchArray(ruleMatches);
  }

  private int getTokensWithPartialPosTagCount(AnalyzedTokenReadings[] tokens, String partialPosTag) {
    return Arrays.stream(tokens).filter(token -> token.hasPartialPosTag(partialPosTag)).mapToInt(e -> 1).sum();
  }

  private int getTokensWithMatchingPosTagRegexpCount(AnalyzedTokenReadings[] tokens, String regexp) {
    return Arrays.stream(tokens).filter(token -> token.matchesPosTagRegex(regexp)).mapToInt(e -> 1).sum();
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
      lowercaseReadings.hasPartialPosTag("VER:INF:")) {
      return true;
    }
    // find error in: "Man müsse Überlegen, wie man das Problem löst."
    boolean isPotentialError = pos < tokens.length - 3
        && tokens[pos+1].getToken().equals(",")
        && INTERROGATIVE_PARTICLES.contains(tokens[pos+2].getToken())
        && tokens[pos-1].hasPartialPosTag("VER:MOD:")
        && !tokens[pos-1].hasLemma("mögen")
        && !tokens[pos+3].getToken().equals("zum");
    if (!isPotentialError &&
        lowercaseReadings != null
        && (tokens[pos].hasPosTag("SUB:NOM:SIN:NEU:INF") || tokens[pos].hasPartialPosTag("SUB:DAT:PLU:"))
        && ("zu".equals(tokens[pos-1].getToken()) || hasPartialTag(tokens[pos-1], "SUB", "EIG", "VER:AUX:3:", "ADV:TMP", "ABK"))) {
      // find error in: "Der Brief wird morgen Übergeben." / "Die Ausgaben haben eine Mrd. Euro Überschritten."
      isPotentialError |= lowercaseReadings.hasPosTag("PA2:PRD:GRU:VER") && !hasPartialTag(tokens[pos-1], "VER:AUX:3:");
      // find error in: "Er lässt das Arktisbohrverbot Überprüfen."
      // find error in: "Sie bat ihn, es zu Überprüfen."
      // find error in: "Das Geld wird Überwiesen."
      isPotentialError |= (pos >= tokens.length - 2 || ",".equals(tokens[pos+1].getToken()))
        && ("zu".equals(tokens[pos-1].getToken()) || isPrecededByModalOrAuxiliary)
        && tokens[pos].getToken().startsWith("Über")
        && (lowercaseReadings.hasPartialPosTag("VER:INF:") || lowercaseReadings.hasPosTag("PA2:PRD:GRU:VER"));
      }
    return isPotentialError;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return makeAntiPatterns(ANTI_PATTERNS, german);
  }

  private void markLowerCaseNounErrors(List<RuleMatch> ruleMatches, AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings analyzedToken) throws IOException {
    // commented out, too many false alarms...
    /*if (i > 0 && i + 1 < tokens.length) {
      AnalyzedTokenReadings prevToken = tokens[i - 1];
      AnalyzedTokenReadings nextToken = tokens[i + 1];
      String prevTokenStr = prevToken.getToken();
      if (!nextToken.hasPartialPosTag("SUB:") &&
          !analyzedToken.getToken().matches("bitte|einen|sein|habe") &&
          !analyzedToken.hasPartialPosTag("ADJ:") &&
          !prevTokenStr.matches("einen|zu") && 
          (prevToken.hasPartialPosTag("PRP:") ||
           prevToken.matchesPosTagRegex("^ART:.*") ||
           prevToken.getToken().matches("seiner|seine|seinen|seinem|seines"))) {
        if (!StringTools.startsWithUppercase(analyzedToken.getToken())
                && analyzedToken.matchesPosTagRegex("^VER:.*") 
                && !analyzedToken.matchesPosTagRegex("^PA2:.*")) {
          String ucToken = StringTools.uppercaseFirstChar(analyzedToken.getToken());
          AnalyzedTokenReadings ucLookup = tagger.lookup(ucToken);
          if (ucLookup != null && ucLookup.hasPartialPosTag("SUB:")) {
            String msg = "Wenn '" + analyzedToken.getToken() + "' hier als Nomen benutzt wird, muss es großgeschrieben werden.";
            RuleMatch ucMatch = new RuleMatch(this, analyzedToken.getStartPos(), analyzedToken.getEndPos(), msg);
            ucMatch.setSuggestedReplacement(ucToken);
            ruleMatches.add(ucMatch);
          }
        }
      }
    }*/
  }

  // e.g. "Ein Kaninchen, das zaubern kann" - avoid false alarm here
  //                          ^^^^^^^
  private boolean isPrevProbablyRelativePronoun(AnalyzedTokenReadings[] tokens, int i) {
    if (i >= 3) {
      AnalyzedTokenReadings prev1 = tokens[i-1];
      AnalyzedTokenReadings prev2 = tokens[i-2];
      AnalyzedTokenReadings prev3 = tokens[i-3];
      if (prev1.getToken().equals("das") &&
          prev2.getToken().equals(",") &&
          prev3.matchesPosTagRegex("SUB:...:SIN:NEU")) {
        return true;
      }
    }
    return false;
  }

  private boolean isSalutation(String token) {
    if (token.length() == 4) {
      return "Herr".equals(token) || "Frau".equals(token);
    } else if (token.length() == 5) {
      return "Herrn".equals(token);
    }
    return false;
  }

  private boolean hasNounReading(AnalyzedTokenReadings readings) {
    if (readings !=  null) {
      // "Die Schöne Tür": "Schöne" also has a noun reading but like "SUB:AKK:SIN:FEM:ADJ", ignore that:
      try {
        AnalyzedTokenReadings allReadings = tagger.lookup(readings.getToken());  // unification in disambiguation.xml removes reading, so look up again
        if (allReadings != null) {
          for (AnalyzedToken reading : allReadings) {
            String posTag = reading.getPOSTag();
            if (posTag != null && posTag.contains("SUB:") && !posTag.contains(":ADJ")) {
              return true;
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException("Could not lookup " + readings.getToken(), e);
      }
    }
    return false;
  }

  private void potentiallyAddLowercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings tokenReadings, boolean prevTokenIsDas, String token, boolean nextTokenIsPersonalOrReflexivePronoun) {
    // e.g. essen -> Essen
    if (prevTokenIsDas &&
        !nextTokenIsPersonalOrReflexivePronoun &&
        Character.isLowerCase(token.charAt(0)) &&
        !substVerbenExceptions.contains(token) &&
        tokenReadings.hasPartialPosTag("VER:INF") &&
        !tokenReadings.isIgnoredBySpeller() &&
        !tokenReadings.isImmunized()) {
      addRuleMatch(ruleMatches, LOWERCASE_MESSAGE, tokenReadings, StringTools.uppercaseFirstChar(tokenReadings.getToken()));
    }
  }

  private void potentiallyAddUppercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings analyzedToken, String token, AnalyzedTokenReadings lowercaseReadings) {
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
          addRuleMatch(ruleMatches, COLON_MESSAGE, tokens[i], fixedWord);
        }
        return;
      }
      addRuleMatch(ruleMatches, UPPERCASE_MESSAGE, tokens[i], fixedWord);
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
	return getTokensWithPartialPosTagCount(subarray, "VER") != 0;
}

private void addRuleMatch(List<RuleMatch> ruleMatches, String msg, AnalyzedTokenReadings tokenReadings, String fixedWord) {
    RuleMatch ruleMatch = new RuleMatch(this, tokenReadings.getStartPos(), tokenReadings.getEndPos(), msg);
    ruleMatch.setSuggestedReplacement(fixedWord);
    ruleMatches.add(ruleMatch);
  }

  // e.g. "a) bla bla"
  private boolean isNumbering(int i, AnalyzedTokenReadings[] tokens) {
    return i >= 2
            && (tokens[i-1].getToken().equals(")") || tokens[i-1].getToken().equals("]"))
            && NUMERALS_EN.matcher(tokens[i-2].getToken()).matches()
            && !(i > 3 && tokens[i-3].getToken().equals("(") && tokens[i-4].hasPartialPosTag("SUB:")); // no numbering "Der Vater (51) fuhr nach Rom."
  }

  private boolean isEllipsis(int i, AnalyzedTokenReadings[] tokens) {
    return (tokens[i-1].getToken().equals("]") || tokens[i-1].getToken().equals(")")) && // sentence starts with […]
           ((i == 4 && tokens[i-2].getToken().equals("…")) || (i == 6 && tokens[i-2].getToken().equals(".")));
  }

  private boolean isNominalization(int i, AnalyzedTokenReadings[] tokens, String token, AnalyzedTokenReadings lowercaseReadings) {
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    // TODO: "vor Schlimmerem", "Er hatte Schlimmes zu befürchten"
    // TODO: wir finden den Fehler in "Die moderne Wissenschaftlich" nicht, weil nicht alle
    // Substantivierungen in den Morphy-Daten stehen (z.B. "Größte" fehlt) und wir deshalb nur
    // eine Abfrage machen, ob der erste Buchstabe groß ist.
    if (StringTools.startsWithUppercase(token) && !isNumber(token) && !hasNounReading(nextReadings) && !token.matches("Alle[nm]")) {
      if (lowercaseReadings != null && lowercaseReadings.hasPosTag("PRP:LOK+TMP+CAU:DAT+AKK")) {
        return false;
      }
      // Ignore "das Dümmste, was je..." but not "das Dümmste Kind"
      AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
      AnalyzedTokenReadings prevPrevToken = i >= 2 ? tokens[i-2] : null;
      AnalyzedTokenReadings prevPrevPrevToken = i >= 3 ? tokens[i-3] : null;
      String prevTokenStr = prevToken != null ? prevToken.getToken() : "";
      if (prevToken != null && ("und".equals(prevTokenStr) || "oder".equals(prevTokenStr) || "beziehungsweise".equals(prevTokenStr))) {
        if (prevPrevToken != null) {
          if (tokens[i].hasPartialPosTag("SUB") && tokens[i].hasPartialPosTag(":ADJ")) {
            // "das dabei Erlernte und Erlebte ist ..." -> 'Erlebte' is correct here
            return true;
          } else if (prevPrevToken.hasPartialPosTag("SUB") && !hasNounReading(nextReadings)) {
            if (lowercaseReadings != null && lowercaseReadings.hasPartialPosTag("ADJ")) {
              // "die Ausgaben für Umweltschutz und Soziales"
              return true;
            }
          }
        }
      }
      if (lowercaseReadings != null && lowercaseReadings.hasPosTag("PA1:PRD:GRU:VER")) {
        // "aus sechs Überwiegend muslimischen Ländern"
        return false;
      }
      return (prevToken != null && ("irgendwas".equals(prevTokenStr) || "aufs".equals(prevTokenStr) || isNumber(prevTokenStr))) ||
         (hasPartialTag(prevToken, "ART", "PRO:") && !(((i < 4 && tokens.length > 4) || prevToken.getReadings().size() == 1 || prevPrevToken.hasLemma("sein")) && prevToken.hasPartialPosTag("PRO:PER:NOM:"))  && !prevToken.hasPartialPosTag(":STD")) ||  // "die Verurteilten", "etwas Verrücktes", "ihr Bestes"
         (hasPartialTag(prevPrevPrevToken, "ART") && hasPartialTag(prevPrevToken, "PRP") && hasPartialTag(prevToken, "SUB")) || // "die zum Tode Verurteilten"
         (hasPartialTag(prevPrevToken, "PRO:", "PRP") && hasPartialTag(prevToken, "ADJ", "ADV", "PA2", "PA1")) ||  // "etwas schön Verrücktes", "mit aufgewühltem Innerem"
         (hasPartialTag(prevPrevPrevToken, "PRO:", "PRP") && hasPartialTag(prevPrevToken, "ADJ", "ADV") && hasPartialTag(prevToken, "ADJ", "ADV", "PA2")) || // "etwas ganz schön Verrücktes"
         (tokens[i].hasPartialPosTag("VER:") && getTokensWithMatchingPosTagRegexpCount(tokens, "VER:[123]:SIN:.*") > 1 && getTokensWithPartialPosTagCount(tokens, "PKT") < 2); // "Parks Vertraute Choi Soon Sil ist zu drei Jahren Haft verurteilt worden."
    }
    return false;
  }

  private boolean isNumber(String token) {
    try {
      if(token.matches("\\d+")) {
        return true;
      }
      AnalyzedTokenReadings lookup = tagger.lookup(StringTools.lowercaseFirstChar(token));
      return lookup != null && lookup.hasPosTag("ZAL");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
      try {
        prevLowercaseReadings = tagger.lookup(prevToken.getToken().toLowerCase());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
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
             !nextReadings.getToken().equals("und") && !nextReadings.getToken().equals("oder") && !nextReadings.getToken().equals(","))
        && !(isFollowedByPossessiveIndicator && hasPartialTag(lowercaseReadings, "ADJ", "VER")) // "Wacht auf, Verdammte dieser Welt!"
        && !(prevToken != null && prevToken.hasPosTag("KON:UNT") && !hasNounReading(nextReadings) && !nextReadings.hasPosTag("KON:NEB"))) {
      AnalyzedTokenReadings prevPrevToken = i > 1 && prevToken.hasPartialPosTag("ADJ") ? tokens[i-2] : null;
      // Another check to avoid false alarms for "eine Gruppe Aufständischer starb"
      if (!isPrecededByVerb && lowercaseReadings != null && prevToken != null) {
        if (prevToken.hasPartialPosTag("SUB:") && lowercaseReadings.matchesPosTagRegex("(ADJ|PA2):GEN:PLU:MAS:GRU:SOL.*")) {
          return nextReadings != null && !nextReadings.hasPartialPosTag("SUB:");
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
      if ((posTag == null || posTag.contains("ADJ")) && !hasNounReading(nextReadings)) {
        if(posTag == null && hasPartialTag(lowercaseReadings, "PRP:LOK", "PA2:PRD:GRU:VER", "PA1:PRD:GRU:VER")) {
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
    boolean maybeLanguage = languages.contains(token) ||
                            languages.contains(StringUtils.removeEnd(token, "e")) ||  // z.B. "ins Japanische übersetzt"
                            languages.contains(StringUtils.removeEnd(token, "en"));   // z.B. "im Japanischen"
    AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    return maybeLanguage && (!hasNounReading(nextReadings) || (prevToken != null && prevToken.getToken().equals("auf")));
  }

  private boolean isProbablyCity(int i, AnalyzedTokenReadings[] tokens, String token) {
    boolean hasCityPrefix = "Klein".equals(token) || "Groß".equals(token) || "Neu".equals(token);
    if (hasCityPrefix) {
      AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
      return nextReadings != null && (!nextReadings.isTagged() || nextReadings.hasPartialPosTag("EIG"));
    }
    return false;
  }

  private boolean isFollowedByRelativeOrSubordinateClause(int i, AnalyzedTokenReadings[] tokens) {
    if (i < tokens.length - 2) {
      return ",".equals(tokens[i+1].getToken()) && (INTERROGATIVE_PARTICLES.contains(tokens[i+2].getToken()) || tokens[i+2].hasPartialPosTag("KON:UNT"));
    }
    return false;
  }

  private boolean isExceptionPhrase(int i, AnalyzedTokenReadings[] tokens) {
    for (String phrase : myExceptionPhrases) {
      String[] parts = phrase.split(" ");
      for (int j = 0; j < parts.length; j++) {
        if (tokens[i].getToken().matches(parts[j])) {
          int startIndex = i-j;
          if (compareLists(tokens, startIndex, startIndex+parts.length-1, parts)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // non-private for tests
  boolean compareLists(AnalyzedTokenReadings[] tokens, int startIndex, int endIndex, String[] parts) {
    if (startIndex < 0) {
      return false;
    }
    int i = 0;
    for (int j = startIndex; j <= endIndex; j++) {
      if (i >= parts.length || j >= tokens.length) {
        return false;
      }
      if (!tokens[j].getToken().matches(parts[i])) {
        return false;
      }
      i++;
    }
    return true;
  }

}