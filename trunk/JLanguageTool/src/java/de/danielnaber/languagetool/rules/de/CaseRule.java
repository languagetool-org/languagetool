/* JLanguageTool, a natural language style checker 
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanToken;
import de.danielnaber.languagetool.tagging.de.GermanTagger;
import de.danielnaber.languagetool.tagging.de.GermanToken;
import de.danielnaber.languagetool.tagging.de.GermanTokenReading;

/**
 * Check that adjectives and verbs are not written with an uppercase
 * first letter (except at the start of a sentence) and cases
 * like this: <tt>Das laufen fällt mir leicht.</tt> (<tt>laufen</tt> needs
 * to be uppercased).
 *   
 * @author Daniel Naber
 */
public class CaseRule extends GermanRule {

  private GermanTagger tagger = new GermanTagger();
  
  private final static Set exceptions = new HashSet();
  static {
    exceptions.add("Le");
    exceptions.add("Ihr");
    exceptions.add("Ihre");
    exceptions.add("Ihren");
    exceptions.add("Ihrem");
    exceptions.add("Ihrer");
    exceptions.add("Sie");
  }
  
  private final static Set substVerbenExceptions = new HashSet();
  static {
    substVerbenExceptions.add("können");
    substVerbenExceptions.add("muss");
    substVerbenExceptions.add("muß");
    substVerbenExceptions.add("wollen");
    substVerbenExceptions.add("habe");
  }

  public CaseRule() {
  }
  
  public String getId() {
    return "DE_CASE";
  }

  public String getDescription() {
    return "Prüft die Großschreibung von Nomen und substantivierten Verben.";
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList();
    AnalyzedToken[] tokens = text.getTokensWithoutWhitespace();
    int pos = 0;
    boolean prevTokenIsDas = false;
    for (int i = 0; i < tokens.length; i++) {
      String posToken = tokens[i].getPOSTag();
      if (posToken != null && posToken.equals(JLanguageTool.SENTENCE_START_TAGNAME))
        continue;
      if (i == 1) {   // don't care about first word, UppercaseSentenceStartRule does this already
        if (tokens[i].getToken().equalsIgnoreCase("das")) {
          prevTokenIsDas = true;
        }
        continue;
      }
      AnalyzedGermanToken analyzedToken = (AnalyzedGermanToken)tokens[i];
      String token = analyzedToken.getToken();
      List readings = analyzedToken.getReadings();
      if (readings == null) {
        // no match, e.g. for "Groß": try if there's a match for the lowercased word:
        AnalyzedGermanToken analyzedGermanToken2;
        try {
          analyzedGermanToken2 = tagger.lookup(token.toLowerCase(), -1);
          if (analyzedGermanToken2 != null)
            readings = analyzedGermanToken2.getReadings();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        if (prevTokenIsDas) {
          // e.g. essen -> Essen
          // TODO: this only makes sense as long as there are no verbs in the morph index!
          String newToken = token.substring(0, 1).toUpperCase() + token.substring(1);
          try {
            analyzedGermanToken2 = tagger.lookup(newToken, -1);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          if (analyzedGermanToken2 != null && Character.isLowerCase(token.charAt(0)) && 
              !substVerbenExceptions.contains(token)) {
            String msg = "Substantivierte Verben werden groß geschrieben.";
            RuleMatch ruleMatch = new RuleMatch(this, tokens[i].getStartPos(),
                tokens[i].getStartPos()+token.length(), msg);
            String word = tokens[i].getToken();
            String fixedWord = Character.toUpperCase(word.charAt(0)) + word.substring(1);
            ruleMatch.setSuggestedReplacement(fixedWord);
            ruleMatches.add(ruleMatch);
          }
        }
      }
      if (tokens[i].getToken().equalsIgnoreCase("das")) {
        prevTokenIsDas = true;
      } else {
        prevTokenIsDas = false;
      }
      if (readings == null)
        continue;
      boolean hasNounReading = false;
      for (Iterator iter = readings.iterator(); iter.hasNext();) {
        GermanTokenReading reading = (GermanTokenReading) iter.next();
        if (reading.getType() == GermanToken.POSType.NOMEN)
          hasNounReading = true;
      }
      if (hasNounReading)  // it's the spell checker's task to check that nouns are uppercase
        continue;
      if (Character.isUpperCase(token.charAt(0)) && ! tokens[i-1].getToken().equals(":") && 
          ! exceptions.contains(token) && token.length() > 1) {     // length limit = ignore abbreviations
        String msg = "Außer am Satzanfang werden nur Nomen und Eigennamen groß geschrieben";
        RuleMatch ruleMatch = new RuleMatch(this, tokens[i].getStartPos(),
            tokens[i].getStartPos()+token.length(), msg);
        String word = tokens[i].getToken();
        String fixedWord = Character.toLowerCase(word.charAt(0)) + word.substring(1);
        ruleMatch.setSuggestedReplacement(fixedWord);
        ruleMatches.add(ruleMatch);
      }
      pos += token.length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
    // nothing
  }

}
