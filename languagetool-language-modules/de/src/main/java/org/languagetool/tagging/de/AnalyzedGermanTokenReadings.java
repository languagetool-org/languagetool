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
package org.languagetool.tagging.de;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.de.GermanToken.POSType;

import java.util.ArrayList;
import java.util.List;

/**
 * All possible readings of an analyzed German word.
 * 
 * @author Daniel Naber
 */
public class AnalyzedGermanTokenReadings extends AnalyzedTokenReadings {

  public AnalyzedGermanTokenReadings(AnalyzedGermanToken[] aTokens, final int startPos) {
    super(aTokens, startPos);
  }    
   
  public AnalyzedGermanTokenReadings(AnalyzedGermanToken aToken, final int startPos) {
    super(aToken, startPos);
  }
  
  /**
   * @return a list of {@link AnalyzedGermanToken}s.
   */
  public List<AnalyzedGermanToken> getGermanReadings() {
    final List<AnalyzedGermanToken> tokens = new ArrayList<>();
    for (AnalyzedToken reading : anTokReadings) {
      if (reading.getPOSTag() != null) {
        if (!reading.getPOSTag().equals(JLanguageTool.SENTENCE_END_TAGNAME) && !reading.getPOSTag().equals(JLanguageTool.PARAGRAPH_END_TAGNAME)) {
          tokens.add((AnalyzedGermanToken)reading);
        }
      } else {
        tokens.add((AnalyzedGermanToken)reading);
      }
    }
    return tokens;
  }

  public boolean hasReadingOfType(POSType type) {
    if (anTokReadings == null)
      return false;
    for (AnalyzedToken reading : anTokReadings) {
      if (reading.getPOSTag() != null) {
        if (reading.getPOSTag().equals(JLanguageTool.SENTENCE_END_TAGNAME) || reading.getPOSTag().equals(JLanguageTool.PARAGRAPH_END_TAGNAME)) {
          return false;
        }
      }
      final AnalyzedGermanToken germanReading = (AnalyzedGermanToken) reading;
      if (germanReading.getType() == type)
        return true;
    }
    return false;
  }

}
