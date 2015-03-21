/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tagging.disambiguation.uk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Ukrainian;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.MultiWordChunker;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;

/**
 * Hybrid chunker-disambiguator for Ukrainian.
 */

public class UkrainianHybridDisambiguator implements Disambiguator {
  private static final String LAST_NAME_TAG = ":lname";
  private static final Pattern INITIAL_REGEX = Pattern.compile("[А-ЯІЇЄҐ]");
  private final Disambiguator chunker = new MultiWordChunker("/uk/multiwords.txt", true);
  private final Disambiguator disambiguator = new XmlRuleDisambiguator(new Ukrainian());

  /**
   * Calls two disambiguator classes: (1) a chunker; (2) a rule-based disambiguator.
   */
  @Override
  public final AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    retagInitials(input);
    
    return disambiguator.disambiguate(chunker.disambiguate(input));
  }

  private void retagInitials(AnalyzedSentence input) {
    AnalyzedTokenReadings[] tokens = input.getTokens();
    for (int i = 0; i < tokens.length - 2; i++) {
      if( isInitial(tokens, i) ) {
        boolean spaced = isSpace(tokens[i+2].getToken());
        int spacedOffset = spaced ? 1 : 0;

        int nextPos = i + 2 + spacedOffset;
        
        if( nextPos + 2 + spacedOffset < tokens.length
            && isInitial(tokens, nextPos)
            && (! spaced || isSpace(tokens[nextPos+2].getToken()) )
            && tokens[nextPos + 2 + spacedOffset].hasPartialPosTag(LAST_NAME_TAG) ) {
          
          int currPos = nextPos;
          nextPos += 2 + spacedOffset;
          
          AnalyzedTokenReadings newReadings = getInitialReadings(tokens[currPos], tokens[nextPos], "patr");
          tokens[currPos] = newReadings;
        }
        
        if( nextPos < tokens.length && tokens[nextPos].hasPartialPosTag(LAST_NAME_TAG) ) {
          AnalyzedTokenReadings newReadings = getInitialReadings(tokens[i], tokens[nextPos], "fname");
          tokens[i] = newReadings;
          i = nextPos;
        }
      }
    }
  }

  private static AnalyzedTokenReadings getInitialReadings(AnalyzedTokenReadings initialsReadings, AnalyzedTokenReadings lnameTokens, String initialType) {
    List<AnalyzedToken> newTokens = new ArrayList<>();
    for(AnalyzedToken lnameToken: lnameTokens.getReadings()) {
      String lnamePosTag = lnameToken.getPOSTag();
      if( lnamePosTag == null || ! lnamePosTag.contains(LAST_NAME_TAG) )
        continue;
      
      String initialsToken = initialsReadings.getAnalyzedToken(0).getToken();
      AnalyzedToken newToken = new AnalyzedToken(initialsToken, lnamePosTag.replace(LAST_NAME_TAG, ":"+initialType+":abbr"), initialsToken);
      newTokens.add(newToken);
    }
    return new AnalyzedTokenReadings(newTokens, initialsReadings.getStartPos());
  }

  private static boolean isInitial(AnalyzedTokenReadings[] tokens, int pos) {
    return pos < tokens.length - 2
        && tokens[pos+1].getToken().equals(".")
        && INITIAL_REGEX.matcher(tokens[pos].getToken()).matches();
  }
  
  private static boolean isSpace(String str) {
    return str != null && (str.equals(" ") || str.equals("\u00A0"));
  }
}
