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
import java.util.Set;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanToken;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanTokenReadings;
import de.danielnaber.languagetool.tagging.de.GermanTagger;
import de.danielnaber.languagetool.tagging.de.GermanToken;
import de.danielnaber.languagetool.tagging.de.GermanToken.POSType;

/**
 * Check that adjectives and verbs are not written with an uppercase
 * first letter (except at the start of a sentence) and cases
 * like this: <tt>Das laufen f&auml;llt mir leicht.</tt> (<tt>laufen</tt> needs
 * to be uppercased).
 *   
 * @author Daniel Naber
 */
public class CaseRule extends GermanRule {

  private GermanTagger tagger = new GermanTagger();
  
  private final static Set<String> exceptions = new HashSet<String>();
  static {
    exceptions.add("Le");
    exceptions.add("Ihr");
    exceptions.add("Ihre");
    exceptions.add("Ihren");
    exceptions.add("Ihnen");
    exceptions.add("Ihrem");
    exceptions.add("Ihrer");
    exceptions.add("Sie");
    exceptions.add("Aus");
  }
  
  private final static Set<String> substVerbenExceptions = new HashSet<String>();
  static {
    substVerbenExceptions.add("so");
    substVerbenExceptions.add("ist");
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
    return "Großschreibung von Nomen und substantivierten Verben.";
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    
    int pos = 0;
    boolean prevTokenIsDas = false;
    for (int i = 0; i < tokens.length; i++) {
    	//FIXME: defaulting to the first analysis
    	//don't know if it's safe
      String posToken = tokens[i].getAnalyzedToken(0).getPOSTag();
      if (posToken != null && posToken.equals(JLanguageTool.SENTENCE_START_TAGNAME))
        continue;
      if (i == 1) {   // don't care about first word, UppercaseSentenceStartRule does this already
        if (tokens[i].getToken().equalsIgnoreCase("das")) {
          prevTokenIsDas = true;
        }
        continue;
      }
      AnalyzedGermanTokenReadings analyzedToken = (AnalyzedGermanTokenReadings)tokens[i];
      String token = analyzedToken.getToken();  
      List<AnalyzedGermanToken> readings = analyzedToken.getGermanReadings();
      AnalyzedGermanTokenReadings analyzedGermanToken2 = null;
      
      boolean isBaseform = false;
      if (analyzedToken.getReadingsLength() > 1 && token.equals(analyzedToken.getAnalyzedToken(0).getLemma())) {
        isBaseform = true;
      }
      if ((readings == null || analyzedToken.getAnalyzedToken(0).getPOSTag() == null || hasOnlyVerbReadings(analyzedToken))
          && isBaseform) {
        // no match, e.g. for "Groß": try if there's a match for the lowercased word:
        try {
          analyzedGermanToken2 = tagger.lookup(token.toLowerCase());
          if (analyzedGermanToken2 != null) {
            readings = analyzedGermanToken2.getGermanReadings();
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        if (prevTokenIsDas) {
          // e.g. essen -> Essen
          String newToken = token.substring(0, 1).toUpperCase() + token.substring(1);
          try {
            analyzedGermanToken2 = tagger.lookup(newToken);
            //analyzedGermanToken2.hasReadingOfType(GermanToken.POSType.VERB)
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          if (Character.isLowerCase(token.charAt(0)) && !substVerbenExceptions.contains(token)) {
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
      boolean hasNounReading = analyzedToken.hasReadingOfType(GermanToken.POSType.NOMEN);
      if (hasNounReading)  // it's the spell checker's task to check that nouns are uppercase
        continue;
      try {
        // TODO: this lookup should only happen once:
        analyzedGermanToken2 = tagger.lookup(token.toLowerCase());
      } catch (IOException e) {
        throw new RuntimeException(e);      }
      if (analyzedToken.getAnalyzedToken(0).getPOSTag() == null && analyzedGermanToken2 == null) {
        continue;
      }
      if (analyzedToken.getAnalyzedToken(0).getPOSTag() == null && analyzedGermanToken2 != null
          && analyzedGermanToken2.getAnalyzedToken(0).getPOSTag() == null) {
        // unknown word, probably a name etc
        continue;
      }
      
      if (Character.isUpperCase(token.charAt(0)) && ! tokens[i-1].getToken().equals(":") && 
          !exceptions.contains(token) &&
          token.length() > 1 &&     // length limit = ignore abbreviations
          !analyzedToken.hasReadingOfType(POSType.PROPER_NOUN)) {
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

  private boolean hasOnlyVerbReadings(AnalyzedGermanTokenReadings analyzedToken) {
    if (!analyzedToken.hasReadingOfType(GermanToken.POSType.NOMEN) &&
        !analyzedToken.hasReadingOfType(GermanToken.POSType.ADJEKTIV) &&
        !analyzedToken.hasReadingOfType(GermanToken.POSType.DETERMINER) &&
        !analyzedToken.hasReadingOfType(GermanToken.POSType.PRONOMEN) &&
        !analyzedToken.hasReadingOfType(GermanToken.POSType.PARTIZIP) &&
        !analyzedToken.hasReadingOfType(GermanToken.POSType.OTHER) &&
        analyzedToken.hasReadingOfType(GermanToken.POSType.VERB)) {
      return true;
    }
    return false;
  }

  public void reset() {
    // nothing
  }

}
