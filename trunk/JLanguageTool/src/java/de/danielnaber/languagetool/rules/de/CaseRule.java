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
package de.danielnaber.languagetool.rules.de;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanToken;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanTokenReadings;
import de.danielnaber.languagetool.tagging.de.GermanTagger;
import de.danielnaber.languagetool.tagging.de.GermanToken;
import de.danielnaber.languagetool.tagging.de.GermanToken.POSType;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Check that adjectives and verbs are not written with an uppercase
 * first letter (except at the start of a sentence) and cases
 * like this: <tt>Das laufen f&auml;llt mir leicht.</tt> (<tt>laufen</tt> needs
 * to be uppercased).
 *   
 * @author Daniel Naber
 */
public class CaseRule extends GermanRule {

  private final GermanTagger tagger = (GermanTagger) Language.GERMAN.getTagger();

  // wenn hinter diesen Wörtern ein Verb steht, ist es wohl ein substantiviertes Verb,
  // muss also groß geschrieben werden:
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
    exceptions.add("Letzt");      // "zu guter Letzt"
    exceptions.add("Für");      // "das Für und Wider"
    exceptions.add("Wider");    // "das Für und Wider"
    exceptions.add("Nachts");   // "des Nachts", "eines Nachts"
    exceptions.add("Genüge");
    exceptions.add("Zusage");
    exceptions.add("Nachfrage");
    exceptions.add("Sachverständiger");
    exceptions.add("Nr");
    exceptions.add("Sankt");
    exceptions.add("Toter");
    exceptions.add("Verantwortlicher");
    exceptions.add("Wichtiges");
    exceptions.add("Dr");
    exceptions.add("Prof");
    exceptions.add("Mr");
    exceptions.add("Mrs");
    exceptions.add("De");    // "De Morgan" etc
    exceptions.add("Le");    // "Le Monde" etc
    exceptions.add("Ihr");
    exceptions.add("Ihre");
    exceptions.add("Ihres");
    exceptions.add("Ihren");
    exceptions.add("Ihnen");
    exceptions.add("Ihrem");
    exceptions.add("Ihrer");
    exceptions.add("Sie");
    exceptions.add("Aus");    // "vor dem Aus stehen"
    exceptions.add("Oder");   // der Fluss
    exceptions.add("tun");   // "Sie müssen das tun"
    exceptions.add("St");   // Paris St. Germain
    exceptions.add("Las");   // Las Vegas, nicht "lesen"
    exceptions.add("Folgendes");   // je nach Kontext groß (TODO)...
    exceptions.add("besonderes");   // je nach Kontext groß (TODO): "etwas Besonderes" 
    exceptions.add("Hundert");   // je nach Kontext groß (TODO) 
    exceptions.add("Tausend");   // je nach Kontext groß (TODO) 
    exceptions.add("Übrigen");   // je nach Kontext groß (TODO), z.B. "im Übrigen" 
    exceptions.add("Unvorhergesehenes");   // je nach Kontext groß (TODO), z.B. "etwas Unvorhergesehenes" 

    exceptions.add("Englisch");   // TODO: alle Sprachen 
    exceptions.add("Deutsch"); 
    exceptions.add("Französisch"); 
    exceptions.add("Spanisch");
    exceptions.add("Italienisch");
    exceptions.add("Portugiesisch");
    exceptions.add("Dänisch");
    exceptions.add("Norwegisch");
    exceptions.add("Schwedisch");
    exceptions.add("Finnisch");
    exceptions.add("Holländisch");
    exceptions.add("Niederländisch");
    exceptions.add("Polnisch");
    exceptions.add("Tschechisch");
    exceptions.add("Arabisch");
    exceptions.add("Persisch");

    exceptions.add("Schuld");
    exceptions.add("Erwachsener");
    exceptions.add("Jugendlicher");
    exceptions.add("Link");
    exceptions.add("Ausdrücke");
    exceptions.add("Landwirtschaft");
    exceptions.add("Flöße");
    exceptions.add("Wild");
    exceptions.add("Vorsitzender");
    exceptions.add("Mrd");
    exceptions.add("Links");
    // Änderungen an der Rechtschreibreform 2006 erlauben hier Großschreibung:
    exceptions.add("Du");
    exceptions.add("Dir");
    exceptions.add("Dich");
    exceptions.add("Deine");
    exceptions.add("Deinen");
    exceptions.add("Deinem");
    exceptions.add("Deines");
    exceptions.add("Deiner");
    exceptions.add("Euch");

    exceptions.add("Neuem");
    exceptions.add("Weitem");
    exceptions.add("Weiteres");
    exceptions.add("Langem");
    exceptions.add("Längerem");
    exceptions.add("Kurzem");
    exceptions.add("Schwarzes");    // Schwarzes Brett
    exceptions.add("Goldener");    // Goldener Schnitt
    // TODO: add more exceptions here
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
  }

  private static final Set<String> substVerbenExceptions = new HashSet<String>();
  static {
    substVerbenExceptions.add("gehören");
    substVerbenExceptions.add("bedeutet");    // "und das bedeutet..."
    substVerbenExceptions.add("ermöglicht");    // "und das ermöglicht..."
    substVerbenExceptions.add("sollen");
    substVerbenExceptions.add("werden");
    substVerbenExceptions.add("dürfen");
    substVerbenExceptions.add("müssen");
    substVerbenExceptions.add("so");
    substVerbenExceptions.add("ist");
    substVerbenExceptions.add("können");
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
      final AnalyzedGermanTokenReadings analyzedToken = (AnalyzedGermanTokenReadings)tokens[i];
      final String token = analyzedToken.getToken();
      List<AnalyzedGermanToken> readings = analyzedToken.getGermanReadings();
      AnalyzedGermanTokenReadings analyzedGermanToken2;
      
      boolean isBaseform = false;
      if (analyzedToken.getReadingsLength() > 1 && token.equals(analyzedToken.getAnalyzedToken(0).getLemma())) {
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
        !analyzedToken.isSentenceEnd() &&
        !isExceptionPhrase(i, tokens)) {
      final String msg = "Außer am Satzanfang werden nur Nomen und Eigennamen groß geschrieben";
      final RuleMatch ruleMatch = new RuleMatch(this, tokens[i].getStartPos(),
          tokens[i].getStartPos() + token.length(), msg);
      final String word = tokens[i].getToken();
      final String fixedWord = Character.toLowerCase(word.charAt(0)) + word.substring(1);
      ruleMatch.setSuggestedReplacement(fixedWord);
      ruleMatches.add(ruleMatch);
    }
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