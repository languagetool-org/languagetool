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
public class CaseRule extends GermanRule {

  private static final Pattern NUMERALS_EN =
          Pattern.compile("[a-z]|[0-9]+|(m{0,4}(cm|cd|d?c{0,3})(xc|xl|l?x{0,3})(ix|iv|v?i{0,3}))$");

  // wenn hinter diesen Wörtern ein Verb steht, ist es wohl ein substantiviertes Verb,
  // muss also groß geschrieben werden:
  private static final Set<String> nounIndicators = new HashSet<>();

  private static final String UPPERCASE_MESSAGE = "Außer am Satzanfang werden nur Nomen und Eigennamen großgeschrieben";
  private static final String LOWERCASE_MESSAGE = "Falls es sich um ein substantiviertes Verb handelt, wird es großgeschrieben.";

  // also see case_rule_exception.txt:
  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
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
    sentenceStartExceptions.add(":");
    sentenceStartExceptions.add("\"");
    sentenceStartExceptions.add("'");
    sentenceStartExceptions.add("„");
    sentenceStartExceptions.add("“");
    sentenceStartExceptions.add("«");
    sentenceStartExceptions.add("»");
    sentenceStartExceptions.add(".");
  }

  private static final Set<String> UNDEFINED_QUANTIFIERS = new HashSet<>(Arrays.asList(
      "viel", "nichts", "wenig", "zuviel" ));

  private static final Set<String> INTERROGATIVE_PARTICLES = new HashSet<>(Arrays.asList(
      "was", "wodurch", "wofür", "womit", "woran", "worauf", "woraus", "wovon" ));

  private static final Set<String> POSSESSIVE_INDICATORS = new HashSet<>(Arrays.asList(
      "einer", "eines", "der", "des", "dieser", "dieses" ));

  /*
   * These are words that Morphy only knows as non-nouns (or not at all).
   * The proper solution is to add all those to our Morphy data, but as a simple
   * workaround to avoid false alarms, these words can be added here.
   */
  private static final Set<String> exceptions = new HashSet<>(Arrays.asList(
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
    "Üblichen",
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
    "Aus",    // "vor dem Aus stehen"
    "Ausdrücke",
    "Auswüchsen",
    "Bände",
    "Bänden",
    "Beauftragter",
    "Belange",
    "besonderes",   // je nach Kontext groß (TODO): "etwas Besonderes" 
    "Biss",
    "De",    // "De Morgan" etc
    "Dr",
    "Durcheinander",
    "Eindrücke",
    "Erwachsener",
    "Familienangehörige", // "Brüder und solche Familienangehörige, die..."
    "Flöße",
    "Folgendes",   // je nach Kontext groß (TODO)...
    "Fort",
    "Fraß",
    "Für",      // "das Für und Wider"
    "Genüge",
    "Gläubiger",
    "Goldener",    // Goldener Schnitt
    "Guten",    // das Kap der Guten Hoffnung
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
    "Nähte",
    "Nähten",
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
    "Spitz",
    "St",   // Paris St. Germain
    "Stereotyp",
    "Störe",
    "Tausend",   // je nach Kontext groß (TODO) 
    "Toter",
    "tun",   // "Sie müssen das tun"
    "Übrigen",   // je nach Kontext groß (TODO), z.B. "im Übrigen" 
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
    languages.add("Isländisch");
    languages.add("Italienisch");
    languages.add("Japanisch");
    languages.add("Jiddisch");
    languages.add("Jugoslawisch");
    languages.add("Kantonesisch");
    languages.add("Katalanisch");
    languages.add("Koreanisch");
    languages.add("Kroatisch");
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
    substVerbenExceptions.add("machen");  // "Du kannst das machen."
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
    for (int i = 0; i < tokens.length; i++) {
      //Note: defaulting to the first analysis is only save if we only query for sentence start
      String posToken = tokens[i].getAnalyzedToken(0).getPOSTag();
      if (posToken != null && posToken.equals(JLanguageTool.SENTENCE_START_TAGNAME)) {
        continue;
      }
      if (i == 1) {   // don't care about first word, UppercaseSentenceStartRule does this already
        if (nounIndicators.contains(tokens[1].getToken().toLowerCase())) {
          prevTokenIsDas = true;
        }
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
          if (nextToken.isSentenceEnd() || nextToken.getToken().equals(",")) {
            // avoid false alarm for "So sollte das funktionieren." (might also remove true alarms...)
            continue;
          }
          if (prevTokenIsDas && (nextToken.getToken().equals("nur") || nextToken.getToken().equals("sogar") ||
                nextToken.getToken().equals("auch") || nextToken.getToken().equals("die") || nextToken.getToken().equals("alle") ||
                nextToken.getToken().equals("viele") ||nextToken.getToken().equals("zu"))) {
            // avoid false alarm for "Das wissen die meisten." / "Um das sagen zu können, ..."
            continue;
          }
          if (prevTokenIsDas && isFollowedByRelativeOrSubordinateClause(i, tokens)) {
            // avoid false alarm for "Er kann ihr das bieten, was sie verdient."
            // avoid false alarm for "Du musst/solltest/könntest das wissen, damit du die Prüfung bestehst / weil wir das gestern besprochen haben."
            continue;
          }
        }
        if (isPrevProbablyRelativePronoun(tokens, i)) {
          continue;
        }
        if (prevTokenIsDas && getTokensWithPartialPosTag(tokens, "VER").length == 1) {
          // ignore sentences containing a single verb, e.g., "Das wissen viele nicht."
          continue;
        }
        potentiallyAddLowercaseMatch(ruleMatches, tokens[i], prevTokenIsDas, token, nextTokenIsPersonalOrReflexivePronoun);
      }
      prevTokenIsDas = nounIndicators.contains(tokens[i].getToken().toLowerCase());
      if (hasNounReading(analyzedToken)) {  // it's the spell checker's task to check that nouns are uppercase
        continue;
      }
      AnalyzedTokenReadings lowercaseReadings = tagger.lookup(token.toLowerCase());
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

  private AnalyzedTokenReadings[] getTokensWithPartialPosTag(AnalyzedTokenReadings[] tokens, String partialPosTag) {
    return Arrays.stream(tokens).filter(token -> token.hasPartialPosTag(partialPosTag)).toArray(size -> new AnalyzedTokenReadings[size]);
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
    return token.equals("Herr") || token.equals("Herrn") || token.equals("Frau");
  }

  private boolean hasNounReading(AnalyzedTokenReadings readings) {
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
    return false;
  }

  private void potentiallyAddLowercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings tokenReadings, boolean prevTokenIsDas, String token, boolean nextTokenIsPersonalOrReflexivePronoun) {
    if (prevTokenIsDas && !nextTokenIsPersonalOrReflexivePronoun) {
      // e.g. essen -> Essen
      if (Character.isLowerCase(token.charAt(0)) && !substVerbenExceptions.contains(token) && tokenReadings.hasPartialPosTag("VER:INF")
              && !tokenReadings.isIgnoredBySpeller() && !tokenReadings.isImmunized()) {
        String fixedWord = StringTools.uppercaseFirstChar(tokenReadings.getToken());
        addRuleMatch(ruleMatches, LOWERCASE_MESSAGE, tokenReadings, fixedWord);
      }
    }
  }

  private void potentiallyAddUppercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings analyzedToken, String token, AnalyzedTokenReadings lowercaseReadings) {
    if (Character.isUpperCase(token.charAt(0)) &&
        token.length() > 1 &&     // length limit = ignore abbreviations
        !tokens[i].isIgnoredBySpeller() &&
        !tokens[i].isImmunized() &&
        !sentenceStartExceptions.contains(tokens[i - 1].getToken()) &&
        !exceptions.contains(token) &&
        !StringTools.isAllUppercase(token) &&
        !isLanguage(i, tokens) &&
        !isProbablyCity(i, tokens) &&
        !GermanHelper.hasReadingOfType(analyzedToken, POSType.PROPER_NOUN) &&
        !analyzedToken.isSentenceEnd() &&
        !isEllipsis(i, tokens) &&
        !isNumbering(i, tokens) &&
        !isNominalization(i, tokens) &&
        !isAdverbAndNominalization(i, tokens) &&
        !isSpecialCase(i, tokens) &&
        !isAdjectiveAsNoun(i, tokens, lowercaseReadings) &&
        !isExceptionPhrase(i, tokens)) {
      String fixedWord = StringTools.lowercaseFirstChar(tokens[i].getToken());
      addRuleMatch(ruleMatches, UPPERCASE_MESSAGE, tokens[i], fixedWord);
    }
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
            && NUMERALS_EN.matcher(tokens[i-2].getToken()).matches();
  }

  private boolean isEllipsis(int i, AnalyzedTokenReadings[] tokens) {
    return (tokens[i-1].getToken().equals("]") || tokens[i-1].getToken().equals(")")) && // sentence starts with […]
           ((i == 4 && tokens[i-2].getToken().equals("…")) || (i == 6 && tokens[i-2].getToken().equals(".")));
  }

  private boolean isNominalization(int i, AnalyzedTokenReadings[] tokens) {
    String token = tokens[i].getToken();
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    // TODO: "vor Schlimmerem", "Er hatte Schlimmes zu befürchten"
    // TODO: wir finden den Fehler in "Die moderne Wissenschaftlich" nicht, weil nicht alle
    // Substantivierungen in den Morphy-Daten stehen (z.B. "Größte" fehlt) und wir deshalb nur
    // eine Abfrage machen, ob der erste Buchstabe groß ist.
    if (StringTools.startsWithUppercase(token) && !isNumber(token) && !hasNounReading(nextReadings) && !token.matches("Alle[nm]")) {
      // Ignore "das Dümmste, was je..." but not "das Dümmste Kind"
      AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
      AnalyzedTokenReadings prevPrevToken = i >= 2 ? tokens[i-2] : null;
      AnalyzedTokenReadings prevPrevPrevToken = i >= 3 ? tokens[i-3] : null;
      String prevTokenStr = prevToken != null ? prevToken.getToken() : "";
      if (prevToken != null && ("und".equals(prevTokenStr) || "oder".equals(prevTokenStr) || "beziehungsweise".equals(prevTokenStr))) {
        if (prevPrevToken != null && tokens[i].hasPartialPosTag("SUB") && tokens[i].hasPartialPosTag(":ADJ")) {
          // "das dabei Erlernte und Erlebte ist ..." -> 'Erlebte' is correct here
          return true;
        }
      }
      return (prevToken != null && ("irgendwas".equals(prevTokenStr) || "aufs".equals(prevTokenStr) || "als".equals(prevTokenStr) || isNumber(prevTokenStr))) ||
         hasPartialTag(prevToken, "ART", "PRO") ||  // "die Verurteilten wurden", "etwas Verrücktes"
         (hasPartialTag(prevPrevPrevToken, "ART") && hasPartialTag(prevPrevToken, "PRP") && hasPartialTag(prevToken, "SUB")) || // "die zum Tode Verurteilten"
         (hasPartialTag(prevPrevToken, "PRO", "PRP") && hasPartialTag(prevToken, "ADJ", "ADV", "PA2", "PA1")) ||  // "etwas schön Verrücktes", "mit aufgewühltem Innerem"
         (hasPartialTag(prevPrevPrevToken, "PRO", "PRP") && hasPartialTag(prevPrevToken, "ADJ", "ADV") && hasPartialTag(prevToken, "ADJ", "ADV", "PA2"));  // "etwas ganz schön Verrücktes"
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

    // ignore "Der Versuch, Neues zu lernen / Gutes zu tun / Spannendes auszuprobieren"
    boolean isPossiblyFollowedByInfinitive = nextReadings != null && nextReadings.getToken().equals("zu");
    boolean isFollowedByInfinitive = nextReadings != null && !isPossiblyFollowedByInfinitive && nextReadings.hasPartialPosTag("EIZ");
    boolean isFollowedByPossessiveIndicator = nextReadings != null && POSSESSIVE_INDICATORS.contains(nextReadings.getToken());

    boolean isUndefQuantifier = prevToken != null && UNDEFINED_QUANTIFIERS.contains(prevToken.getToken().toLowerCase());
    boolean isPrevDeterminer = prevToken != null && (prevToken.hasPartialPosTag("ART") || prevToken.hasPartialPosTag("PRP") || prevToken.hasPartialPosTag("ZAL"));
    if (!isPrevDeterminer && !isUndefQuantifier && !(isPossiblyFollowedByInfinitive || isFollowedByInfinitive)
        && !(isFollowedByPossessiveIndicator && hasPartialTag(lowercaseReadings, "ADJ", "VER")) // "Wacht auf, Verdammte dieser Welt!"
        && !(prevToken != null && prevToken.hasPosTag("KON:UNT") && nextReadings != null && !hasNounReading(nextReadings) && !nextReadings.hasPosTag("KON:NEB"))) {
      AnalyzedTokenReadings prevPrevToken = i > 1 && prevToken.hasPartialPosTag("ADJ") ? tokens[i-2] : null;
      // Another check to avoid false alarms for "ein politischer Revolutionär"
      if (prevPrevToken == null || !(prevPrevToken.hasPartialPosTag("ART") || prevPrevToken.hasPartialPosTag("PRP") || prevToken.hasPartialPosTag("ZAL"))) {
        return false;
      }
    }

    // ignore "die Ausgewählten" but not "die Ausgewählten Leute":
    for (AnalyzedToken reading : tokens[i].getReadings()) {
      String posTag = reading.getPOSTag();
      // ignore "die Ausgewählten" but not "die Ausgewählten Leute":
      if ((posTag == null || posTag.contains("ADJ")) && !hasNounReading(nextReadings)) {
        return true;
      }
    }

    return false;
  }

  private boolean isLanguage(int i, AnalyzedTokenReadings[] tokens) {
    String token = tokens[i].getToken();
    boolean maybeLanguage = languages.contains(token) ||
                            languages.contains(StringUtils.removeEnd(token, "e")) ||  // z.B. "ins Japanische übersetzt"
                            languages.contains(StringUtils.removeEnd(token, "en"));   // z.B. "im Japanischen"
    AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    return maybeLanguage && ((nextReadings != null && !hasNounReading(nextReadings)) ||
                             (prevToken != null && prevToken.getToken().equals("auf")));
  }

  private boolean isProbablyCity(int i, AnalyzedTokenReadings[] tokens) {
    String token = tokens[i].getToken();
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

  @Override
  public void reset() {
    // nothing
  }

}