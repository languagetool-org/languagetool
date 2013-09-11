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
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.de.GermanToken;
import org.languagetool.tagging.de.GermanToken.POSType;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

/**
 * Check that adjectives and verbs are not written with an uppercase
 * first letter (except at the start of a sentence) and cases
 * like this: <tt>Das laufen f&auml;llt mir leicht.</tt> (<tt>laufen</tt> needs
 * to be uppercased).
 *   
 * @author Daniel Naber
 */
public class CaseRule extends GermanRule {

  private final GermanTagger tagger;

  // wenn hinter diesen Wörtern ein Verb steht, ist es wohl ein substantiviertes Verb,
  // muss also groß geschrieben werden:
  private static final Set<String> nounIndicators = new HashSet<>();
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
  
  private static final Set<String> exceptions = new HashSet<>();
  static {
    /*
     * These are words that Morphy only knows as non-nouns. The proper
     * solution is to add all those to our Morphy data, but as a simple
     * workaround to avoid false alarms, these words can be added here.
     */
    exceptions.add("Vorfahre");
    exceptions.add("Mittler");
    exceptions.add("Hr");   // Hr. = Abkürzung für Herr
    exceptions.add("Schwarz");
    exceptions.add("Genese");
    exceptions.add("Rosa");
    exceptions.add("Auftrieb");
    exceptions.add("Zuschnitt");
    exceptions.add("Geschossen");
    exceptions.add("Vortrieb");
    exceptions.add("Abtrieb");
    exceptions.add("Gesandter");
    exceptions.add("Durchfahrt");
    exceptions.add("Durchgriff");
    exceptions.add("Überfahrt");
    exceptions.add("Zeche");
    exceptions.add("Sparte");
    exceptions.add("Sparten");
    exceptions.add("Heiliger");
    exceptions.add("Reisender");
    exceptions.add("Hochdeutsch");
    exceptions.add("Pest");
    exceptions.add("Schwinge");
    exceptions.add("Verlies");
    exceptions.add("Nachfolge");
    exceptions.add("Stift");
    exceptions.add("Belange");
    exceptions.add("Geistlicher");
    exceptions.add("Jenseits");
    exceptions.add("Abends");
    exceptions.add("Abgeordneter");
    exceptions.add("Angestellter");
    exceptions.add("Abriss");
    exceptions.add("Ahne");
    exceptions.add("Ähnlichem");
    exceptions.add("Ähnliches");   // je nach Kontext groß (TODO), z.B. "Er hat Ähnliches erlebt" 
    exceptions.add("Allerlei");
    exceptions.add("Anklang");
    exceptions.add("Anstrich");
    exceptions.add("Armes");
    exceptions.add("Aus");    // "vor dem Aus stehen"
    exceptions.add("Ausdrücke");
    exceptions.add("Auswüchsen");
    exceptions.add("Bände");
    exceptions.add("Bänden");
    exceptions.add("Beauftragter");
    exceptions.add("Belange");
    exceptions.add("besonderes");   // je nach Kontext groß (TODO): "etwas Besonderes" 
    exceptions.add("Biss");
    exceptions.add("De");    // "De Morgan" etc
    exceptions.add("Dr");
    exceptions.add("Durcheinander");
    exceptions.add("Eindrücke");
    exceptions.add("Erwachsener");
    exceptions.add("Flöße");
    exceptions.add("Folgendes");   // je nach Kontext groß (TODO)...
    exceptions.add("Fort");
    exceptions.add("Fraß");
    exceptions.add("Für");      // "das Für und Wider"
    exceptions.add("Genüge");
    exceptions.add("Gläubiger");
    exceptions.add("Goldener");    // Goldener Schnitt
    exceptions.add("Große");    // Alexander der Große, der Große Bär
    exceptions.add("Großen");
    exceptions.add("Guten");    // das Kap der Guten Hoffnung
    exceptions.add("Hechte");
    exceptions.add("Herzöge");
    exceptions.add("Herzögen");
    exceptions.add("Hinfahrt");
    exceptions.add("Hundert");   // je nach Kontext groß (TODO) 
    exceptions.add("Ihnen");
    exceptions.add("Ihr");
    exceptions.add("Ihre");
    exceptions.add("Ihrem");
    exceptions.add("Ihren");
    exceptions.add("Ihrer");
    exceptions.add("Ihres");
    exceptions.add("Infrarot");
    exceptions.add("Jenseits");
    exceptions.add("Jugendlicher");
    exceptions.add("Jünger");
    exceptions.add("Klaue");
    exceptions.add("Kleine");    // der Kleine Bär
    exceptions.add("Konditional");
    exceptions.add("Krähe");
    exceptions.add("Kurzem");
    exceptions.add("Landwirtschaft");
    exceptions.add("Langem");
    exceptions.add("Längerem");
    exceptions.add("Las");   // Las Vegas, nicht "lesen"
    exceptions.add("Le");    // "Le Monde" etc
    exceptions.add("Letzt");
    exceptions.add("Letzt");      // "zu guter Letzt"
    exceptions.add("Letztere");
    exceptions.add("Letzterer");
    exceptions.add("Letzteres");
    exceptions.add("Link");
    exceptions.add("Links");
    exceptions.add("Löhne");
    exceptions.add("Luden");
    exceptions.add("Mitfahrt");
    exceptions.add("Mr");
    exceptions.add("Mrd");
    exceptions.add("Mrs");
    exceptions.add("Nachfrage");
    exceptions.add("Nachts");   // "des Nachts", "eines Nachts"
    exceptions.add("Nähte");
    exceptions.add("Nähten");
    exceptions.add("Neuem");
    exceptions.add("Neues");   // nichts Neues
    exceptions.add("Nr");
    exceptions.add("Nutze");   // zu Nutze
    exceptions.add("Obdachloser");
    exceptions.add("Oder");   // der Fluss
    exceptions.add("Patsche");
    exceptions.add("Pfiffe");
    exceptions.add("Pfiffen");
    exceptions.add("Prof");
    exceptions.add("Puste");
    exceptions.add("Sachverständiger");
    exceptions.add("Sankt");
    exceptions.add("Scheine");
    exceptions.add("Scheiße");
    exceptions.add("Schuft");
    exceptions.add("Schufte");
    exceptions.add("Schuld");
    exceptions.add("Schwärme");
    exceptions.add("Schwarzes");    // Schwarzes Brett
    exceptions.add("Sie");
    exceptions.add("Spitz");
    exceptions.add("St");   // Paris St. Germain
    exceptions.add("Stereotyp");
    exceptions.add("Störe");
    exceptions.add("Tausend");   // je nach Kontext groß (TODO) 
    exceptions.add("Toter");
    exceptions.add("tun");   // "Sie müssen das tun"
    exceptions.add("Übrigen");   // je nach Kontext groß (TODO), z.B. "im Übrigen" 
    exceptions.add("Unvorhergesehenes");   // je nach Kontext groß (TODO), z.B. "etwas Unvorhergesehenes" 
    exceptions.add("Verantwortlicher");
    exceptions.add("Verwandter");
    exceptions.add("Vielfaches");
    exceptions.add("Vorsitzender");
    exceptions.add("Fraktionsvorsitzender");
    exceptions.add("Weitem");
    exceptions.add("Weiteres");
    exceptions.add("Wicht");
    exceptions.add("Wichtiges");
    exceptions.add("Wider");    // "das Für und Wider"
    exceptions.add("Wild");
    exceptions.add("Zeche");
    exceptions.add("Zusage");
    exceptions.add("Zwinge");

    exceptions.add("Erster");   // "er wurde Erster im Langlauf"
    exceptions.add("Zweiter");
    exceptions.add("Dritter");
    exceptions.add("Vierter");
    exceptions.add("Fünfter");
    exceptions.add("Sechster");
    exceptions.add("Siebter");
    exceptions.add("Achter");
    exceptions.add("Neunter");
    exceptions.add("Erste");   // "sie wurde Erste im Langlauf"
    exceptions.add("Zweite");
    exceptions.add("Dritte");
    exceptions.add("Vierte");
    exceptions.add("Fünfte");
    exceptions.add("Sechste");
    exceptions.add("Siebte");
    exceptions.add("Achte");
    exceptions.add("Neunte");

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
  
  private static final Set<String> myExceptionPhrases = new HashSet<>();
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
    myExceptionPhrases.add("Römische Reich Deutscher Nation");
    myExceptionPhrases.add("ein absolutes Muss");
    myExceptionPhrases.add("ein Muss");
  }

  private static final Set<String> substVerbenExceptions = new HashSet<>();
  static {
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
  }
  
  public CaseRule(final ResourceBundle messages, final German german) {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_case")));
    
    this.tagger = (GermanTagger) german.getTagger();
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
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    
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
      if (i > 0 && (tokens[i-1].getToken().equals("Herr") || tokens[i-1].getToken().equals("Herrn") || tokens[i-1].getToken().equals("Frau")) ) {   // "Frau Stieg" could be a name, ignore
        continue;
      }
      final AnalyzedTokenReadings analyzedToken = tokens[i];
      final String token = analyzedToken.getToken();
      List<AnalyzedToken> readings = analyzedToken.getReadings();
      AnalyzedTokenReadings analyzedGermanToken2;
      
      boolean isBaseform = false;
      if (analyzedToken.getReadingsLength() >= 1 && analyzedToken.hasLemma(token)) {
        isBaseform = true;
      }
      if ((readings == null || analyzedToken.getAnalyzedToken(0).getPOSTag() == null || GermanHelper.hasReadingOfType(analyzedToken, GermanToken.POSType.VERB))
          && isBaseform) {
        // no match, e.g. for "Groß": try if there's a match for the lowercased word:
        analyzedGermanToken2 = tagger.lookup(token.toLowerCase());
        if (analyzedGermanToken2 != null) {
          readings = analyzedGermanToken2.getReadings();
        }
        boolean nextTokenIsPersonalPronoun = false;
        if (i < tokens.length - 1) {
          // avoid false alarm for "Das haben wir getan." etc:
          nextTokenIsPersonalPronoun = tokens[i + 1].hasPartialPosTag("PRO:PER") || tokens[i + 1].getToken().equals("Sie");
        }
        potentiallyAddLowercaseMatch(ruleMatches, tokens[i], prevTokenIsDas, token, nextTokenIsPersonalPronoun);
      }
      prevTokenIsDas = nounIndicators.contains(tokens[i].getToken().toLowerCase());
      if (readings == null) {
        continue;
      }
      final boolean hasNounReading = GermanHelper.hasReadingOfType(analyzedToken, GermanToken.POSType.NOMEN);
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

  private void potentiallyAddLowercaseMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings tokenReadings, boolean prevTokenIsDas, String token, boolean nextTokenIsPersonalPronoun) {
    if (prevTokenIsDas && !nextTokenIsPersonalPronoun) {
      // e.g. essen -> Essen
      if (Character.isLowerCase(token.charAt(0)) && !substVerbenExceptions.contains(token)) {
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
        !sentenceStartExceptions.contains(tokens[i - 1].getToken()) &&
        !StringTools.isAllUppercase(token) &&
        !exceptions.contains(token) &&
        !GermanHelper.hasReadingOfType(analyzedToken, POSType.PROPER_NOUN) &&
        !analyzedToken.isSentenceEnd() &&
        !( (tokens[i-1].getToken().equals("]") || tokens[i-1].getToken().equals(")")) && // sentence starts with […]
           ( (i == 4 && tokens[i-2].getToken().equals("…")) || (i == 6 && tokens[i-2].getToken().equals(".")) ) ) &&
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

  private boolean compareLists(AnalyzedTokenReadings[] tokens, int startIndex, int endIndex, String[] parts) {
    if (startIndex < 0) {
      return false;
    }
    int i = 0;
    for (int j = startIndex; j <= endIndex; j++) {
      if (i >= parts.length) {
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