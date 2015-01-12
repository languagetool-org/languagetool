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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.Category;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.de.GermanToken;
import org.languagetool.tagging.de.GermanToken.POSType;
import org.languagetool.tools.StringTools;

import java.io.IOException;
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

  /*
   * These are words that Morphy only knows as non-nouns. The proper
   * solution is to add all those to our Morphy data, but as a simple
   * workaround to avoid false alarms, these words can be added here.
   */
  private static final Set<String> exceptions = new HashSet<>(Arrays.asList(
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
    "Hochdeutsch",
    "Pest",
    "Schwinge",
    "Verlies",
    "Nachfolge",
    "Stift",
    "Belange",
    "Geistlicher",
    "Jenseits",
    "Abends",
    "Abgeordneter",
    "Angestellter",
    "Liberaler",
    "Abriss",
    "Ahne",
    "Ähnlichem",
    "Ähnliches",   // je nach Kontext groß (TODO), z.B. "Er hat Ähnliches erlebt" 
    "Allerlei",
    "Anklang",
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
    "Kleine",    // der Kleine Bär
    "Konditional",
    "Krähe",
    "Kurzem",
    "Landwirtschaft",
    "Langem",
    "Längerem",
    "Las",   // Las Vegas, nicht "lesen"
    "Le",    // "Le Monde" etc
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
    "Patsche",
    "Pfiffe",
    "Pfiffen",
    "Prof",
    "Puste",
    "Sachverständiger",
    "Sankt",
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
    languages.add("Afrikanisch");
    languages.add("Altarabisch");
    languages.add("Altchinesisch");
    languages.add("Altgriechisch");
    languages.add("Althochdeutsch");
    languages.add("Altpersisch");
    languages.add("Amerikanisch");
    languages.add("Arabisch");
    languages.add("Chinesisch");
    languages.add("Dänisch");
    languages.add("Deutsch");
    languages.add("Englisch");
    languages.add("Finnisch");
    languages.add("Französisch");
    languages.add("Frühneuhochdeutsch");
    languages.add("Germanisch");
    languages.add("Griechisch");
    languages.add("Hocharabisch");
    languages.add("Hochchinesisch");
    languages.add("Hochdeutsch");
    languages.add("Holländisch");
    languages.add("Italienisch");
    languages.add("Japanisch");
    languages.add("Jiddisch");
    languages.add("Jugoslawisch");
    languages.add("Koreanisch");
    languages.add("Kroatisch");
    languages.add("Lateinisch");
    languages.add("Luxemburgisch");
    languages.add("Mittelhochdeutsch");
    languages.add("Neuhochdeutsch");
    languages.add("Niederländisch");
    languages.add("Norwegisch");
    languages.add("Persisch");
    languages.add("Polnisch");
    languages.add("Portugiesisch");
    languages.add("Russisch");
    languages.add("Schwedisch");
    languages.add("Schweizerisch");
    languages.add("Serbisch");
    languages.add("Serbokroatisch");
    languages.add("Slawisch");
    languages.add("Spanisch");
    languages.add("Tschechisch");
    languages.add("Türkisch");
    languages.add("Ukrainisch");
    languages.add("Ungarisch");
    languages.add("Weißrussisch");
  }
  
  private static final Set<String> myExceptionPhrases = new HashSet<>();
  static {
    // use proper upper/lowercase spelling here:
    myExceptionPhrases.add("Gleicher unter Gleichen");
    myExceptionPhrases.add("Jung und Alt");
    myExceptionPhrases.add("Arabische Halbinsel");
    myExceptionPhrases.add("Arabischen Halbinsel");
    myExceptionPhrases.add("Naher Osten");
    myExceptionPhrases.add("Nahen Osten");
    myExceptionPhrases.add("Mittlerer Osten");
    myExceptionPhrases.add("Mittleren Osten");
    myExceptionPhrases.add("Ferne Osten");
    myExceptionPhrases.add("Fernen Osten");
    myExceptionPhrases.add("Ferner Osten");
    myExceptionPhrases.add("Europäische Union");
    myExceptionPhrases.add("Europäischen Union");
    myExceptionPhrases.add("Russische Föderation");
    myExceptionPhrases.add("Russischen Föderation");
    myExceptionPhrases.add("Internationaler Währungsfonds");
    myExceptionPhrases.add("Internationale Währungsfonds");
    myExceptionPhrases.add("Internationalen Währungsfonds");
    myExceptionPhrases.add("Japanische Meer");
    myExceptionPhrases.add("Japanisches Meer");
    myExceptionPhrases.add("Japanischen Meer");
    myExceptionPhrases.add("Japanische Alpen");  // http://www.duden.de/sprachwissen/rechtschreibregeln/namen#K140
    myExceptionPhrases.add("Japanischen Alpen");
    myExceptionPhrases.add("Chinesische Mauer");
    myExceptionPhrases.add("Chinesischen Mauer");
    myExceptionPhrases.add("Französische Revolution");
    myExceptionPhrases.add("Französischen Revolution");
    myExceptionPhrases.add("Süddeutsche Zeitung");
    myExceptionPhrases.add("Süddeutschen Zeitung");
    myExceptionPhrases.add("nichts Wichtigeres");
    myExceptionPhrases.add("nichts Schöneres");
    myExceptionPhrases.add("ohne Wenn und Aber");
    myExceptionPhrases.add("Große Koalition");
    myExceptionPhrases.add("Großen Koalition");
    myExceptionPhrases.add("Alexander der Große");
    myExceptionPhrases.add("Großer Bär");  // Sternbild
    myExceptionPhrases.add("Große Bär");  // Sternbild
    myExceptionPhrases.add("im Großen und Ganzen");
    myExceptionPhrases.add("Im Großen und Ganzen");
    myExceptionPhrases.add("im Guten wie im Schlechten");
    myExceptionPhrases.add("Im Guten wie im Schlechten");
    myExceptionPhrases.add("Russisches Reich");
    myExceptionPhrases.add("Russischen Reich");
    myExceptionPhrases.add("Russischen Reichs");
    myExceptionPhrases.add("Russischen Reiches");
    myExceptionPhrases.add("Tel Aviv");
    myExceptionPhrases.add("Dreißigjährige Krieg");
    myExceptionPhrases.add("Dreißigjährigen Kriegs");
    myExceptionPhrases.add("Dreißigjährigen Krieges");
    myExceptionPhrases.add("Erster Weltkrieg");
    myExceptionPhrases.add("Ersten Weltkriegs");
    myExceptionPhrases.add("Ersten Weltkrieg");
    myExceptionPhrases.add("Ersten Weltkrieges");
    myExceptionPhrases.add("Erstem Weltkrieg");
    myExceptionPhrases.add("Zweiter Weltkrieg");
    myExceptionPhrases.add("Zweiten Weltkrieg");
    myExceptionPhrases.add("Zweiten Weltkriegs");
    myExceptionPhrases.add("Zweiten Weltkrieges");
    myExceptionPhrases.add("Zweitem Weltkrieg");
    myExceptionPhrases.add("Vielfaches");
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
    myExceptionPhrases.add("Heiliges Römisches Reich");
    myExceptionPhrases.add("Heilige Römische Reich");
    myExceptionPhrases.add("Römische Reich Deutscher Nation");
    myExceptionPhrases.add("ein absolutes Muss");
    myExceptionPhrases.add("ein Muss");
    myExceptionPhrases.add("nichts Neues");
    myExceptionPhrases.add("etwas Neues");
    myExceptionPhrases.add("kaum Neues");
    myExceptionPhrases.add("wenig Neues");
    myExceptionPhrases.add("viel Neues");
    myExceptionPhrases.add("Vereinigte Staaten");
    myExceptionPhrases.add("Vereinigten Staaten");
    myExceptionPhrases.add("Vereinten Nationen");
    myExceptionPhrases.add("im Weiteren");
    myExceptionPhrases.add("Im Weiteren");
    myExceptionPhrases.add("Roter Riese");
    myExceptionPhrases.add("Roten Riesen");
    myExceptionPhrases.add("als Erstes");  // aber: als erstes Kind...
    myExceptionPhrases.add("Als Erstes");
  }

  private static final Set<String> substVerbenExceptions = new HashSet<>();
  static {
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
  }

  private final GermanTagger tagger;
  
  public CaseRule(final ResourceBundle messages, final German german) {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_case")));
    }
    this.tagger = (GermanTagger) german.getTagger();
    addExamplePair(Example.wrong("<marker>Das laufen</marker> fällt mir schwer."),
                   Example.fixed("<marker>Das Laufen</marker> fällt mir schwer."));
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
  public RuleMatch[] match(final AnalyzedSentence sentence) throws IOException {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    
    boolean prevTokenIsDas = false;
    for (int i = 0; i < tokens.length; i++) {
      //Note: defaulting to the first analysis is only save if we only query for sentence start
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
      if (i > 0 && isSalutation(tokens[i-1].getToken())) {   // e.g. "Frau Stieg" could be a name, ignore
        continue;
      }
      final AnalyzedTokenReadings analyzedToken = tokens[i];
      final String token = analyzedToken.getToken();

      boolean isBaseform = analyzedToken.getReadingsLength() >= 1 && analyzedToken.hasLemma(token);
      if ((analyzedToken.getAnalyzedToken(0).getPOSTag() == null || GermanHelper.hasReadingOfType(analyzedToken, GermanToken.POSType.VERB))
          && isBaseform) {
        boolean nextTokenIsPersonalPronoun = false;
        if (i < tokens.length - 1) {
          // avoid false alarm for "Das haben wir getan." etc:
          nextTokenIsPersonalPronoun = tokens[i + 1].hasPartialPosTag("PRO:PER") || tokens[i + 1].getToken().equals("Sie");
          if (tokens[i + 1].hasLemma("lassen")) {
            // avoid false alarm for "Ihr sollt mich das wissen lassen."
            continue;
          }
        }
        potentiallyAddLowercaseMatch(ruleMatches, tokens[i], prevTokenIsDas, token, nextTokenIsPersonalPronoun);
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
          && lowercaseReadings.getAnalyzedToken(0).getPOSTag() == null) {
        continue;  // unknown word, probably a name etc
      }
      potentiallyAddUppercaseMatch(ruleMatches, tokens, i, analyzedToken, token);
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean isSalutation(String token) {
    return token.equals("Herr") || token.equals("Herrn") || token.equals("Frau");
  }

  private boolean hasNounReading(AnalyzedTokenReadings readings) {
    // "Die Schöne Tür": "Schöne" also has a noun reading but like "SUB:AKK:SIN:FEM:ADJ", ignore that:
    for (AnalyzedToken reading : readings) {
      String posTag = reading.getPOSTag();
      if (posTag != null && posTag.contains("SUB:") && !posTag.contains(":ADJ")) {
        return true;
      }
    }
    return false;
  }

  private void potentiallyAddLowercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings tokenReadings, boolean prevTokenIsDas, String token, boolean nextTokenIsPersonalPronoun) {
    if (prevTokenIsDas && !nextTokenIsPersonalPronoun) {
      // e.g. essen -> Essen
      if (Character.isLowerCase(token.charAt(0)) && !substVerbenExceptions.contains(token) && tokenReadings.hasPartialPosTag("VER:INF")
              && !tokenReadings.isIgnoredBySpeller()) {
        final String msg = "Substantivierte Verben werden großgeschrieben.";
        final RuleMatch ruleMatch = new RuleMatch(this, tokenReadings.getStartPos(),
            tokenReadings.getStartPos() + token.length(), msg);
        final String word = tokenReadings.getToken();
        final String fixedWord = StringTools.uppercaseFirstChar(word);
        ruleMatch.setSuggestedReplacement(fixedWord);
        ruleMatches.add(ruleMatch);
      }
    }
  }

  private void potentiallyAddUppercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings analyzedToken, String token) {
    if (Character.isUpperCase(token.charAt(0)) &&
        token.length() > 1 &&     // length limit = ignore abbreviations
        !tokens[i].isIgnoredBySpeller() &&
        !sentenceStartExceptions.contains(tokens[i - 1].getToken()) &&
        !StringTools.isAllUppercase(token) &&
        !exceptions.contains(token) &&
        !isLanguage(i, tokens) &&
        !isProbablyCity(i, tokens) &&
        !GermanHelper.hasReadingOfType(analyzedToken, POSType.PROPER_NOUN) &&
        !analyzedToken.isSentenceEnd() &&
        !isEllipsis(i, tokens) &&
        !isNumbering(i, tokens) &&
        !isNominalization(i, tokens) &&
        !isAdverbAndNominalization(i, tokens) &&
        !isSpecialCase(i, tokens) &&
        !isAdjectiveAsNoun(i, tokens) &&
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

  // e.g. "a) bla bla"
  private boolean isNumbering(int i, AnalyzedTokenReadings[] tokens) {
    return i >= 2
            && (tokens[i-1].getToken().equals(")") || (tokens[i-1].getToken().equals("]")))
            && NUMERALS_EN.matcher(tokens[i-2].getToken()).matches();
  }

  private boolean isEllipsis(int i, AnalyzedTokenReadings[] tokens) {
    return ( (tokens[i-1].getToken().equals("]") || tokens[i-1].getToken().equals(")")) && // sentence starts with […]
       ( (i == 4 && tokens[i-2].getToken().equals("…")) || (i == 6 && tokens[i-2].getToken().equals(".")) ) );
  }

  private boolean isNominalization(int i, AnalyzedTokenReadings[] tokens) {
    String token = tokens[i].getToken();
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    // TODO: "vor Schlimmerem", "Er hatte Schlimmes zu befürchten"
    // TODO: wir finden den Fehler in "Die moderne Wissenschaftlich" nicht, weil nicht alle
    // Substantivierungen in den Morphy-Daten stehen (z.B. "Größte" fehlt) und wir deshalb nur
    // eine Abfrage machen, ob der erste Buchstabe groß ist.
    if (StringTools.startsWithUppercase(token) && !isNumber(token) && !hasNounReading(nextReadings)) {
      // Ignore "das Dümmste, was je..." but not "das Dümmste Kind"
      AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
      AnalyzedTokenReadings prevPrevToken = i > 1 ? tokens[i-2] : null;
      return (prevToken != null && ("irgendwas".equals(prevToken.getToken()) || "aufs".equals(prevToken.getToken()))) ||
             hasPartialTag(prevToken, "PRO") ||  // z.B. "etwas Verrücktes"
             (hasPartialTag(prevPrevToken, "PRO") && hasPartialTag(prevToken, "ADJ", "ADV")); // z.B. "etwas schön Verrücktes"
    }
    return false;
  }

  private boolean isNumber(String token) {
    try {
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

  private boolean isAdjectiveAsNoun(int i, AnalyzedTokenReadings[] tokens) {
    AnalyzedTokenReadings prevToken = i > 0 ? tokens[i-1] : null;
    boolean isPrevDeterminer = prevToken != null && (prevToken.hasPartialPosTag("ART") || prevToken.hasPartialPosTag("PRP"));
    if (!isPrevDeterminer) {
      return false;
    }
    AnalyzedTokenReadings nextReadings = i < tokens.length-1 ? tokens[i+1] : null;
    for (AnalyzedToken reading : tokens[i].getReadings()) {
      String posTag = reading.getPOSTag();
      // ignore "die Ausgewählten" but not "die Ausgewählten Leute":
      if (posTag != null && posTag.contains(":ADJ") && !hasNounReading(nextReadings)) {
        return true;
      }
    }
    return false;
  }

  private boolean isLanguage(int i, AnalyzedTokenReadings[] tokens) {
    String token = tokens[i].getToken();
    boolean maybeLanguage = languages.contains(token) ||
                            languages.contains(token.replaceFirst("e$", "")) ||  // z.B. "ins Japanische übersetzt"
                            languages.contains(token.replaceFirst("en$", ""));   // z.B. "im Japanischen"
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

  private boolean isExceptionPhrase(int i, AnalyzedTokenReadings[] tokens) {
    for (String exc : myExceptionPhrases) {
      final String[] parts = exc.split(" ");
      for (int j = 0; j < parts.length; j++) {
        if (parts[j].equals(tokens[i].getToken())) {
          final int startIndex = i-j;
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