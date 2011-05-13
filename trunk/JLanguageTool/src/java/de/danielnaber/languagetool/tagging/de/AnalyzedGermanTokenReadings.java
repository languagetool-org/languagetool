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
package de.danielnaber.languagetool.tagging.de;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.de.GermanToken.POSType;
import de.danielnaber.languagetool.tools.StringTools;
import de.danielnaber.languagetool.JLanguageTool;

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
    final List<AnalyzedGermanToken> tokens = new ArrayList<AnalyzedGermanToken>();
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

  /**
   * Return true if the analyzed word is a sentence or paragraph end.
   */
  public boolean isSentenceEnd() {
    if (anTokReadings == null) {
      return false;
    }
    for (AnalyzedToken reading : anTokReadings) {
      if (reading.getPOSTag() != null) {
        if (reading.getPOSTag().equals(JLanguageTool.SENTENCE_END_TAGNAME) || reading.getPOSTag().equals(JLanguageTool.PARAGRAPH_END_TAGNAME)) {      
          return true;
        }
      }
    }
    return false;
  }

  public boolean hasReading(GermanToken.Kasus kasus) {
    if (anTokReadings == null)
      return false;
    for (AnalyzedToken reading : anTokReadings) {
      final AnalyzedGermanToken germanReading = (AnalyzedGermanToken) reading;
      if (germanReading.getCasus() == kasus)
        return true;
    }
    return false;
  }

  public boolean hasReading(GermanToken.Numerus numerus) {
    if (anTokReadings == null)
      return false;
    for (AnalyzedToken reading : anTokReadings) {
      final AnalyzedGermanToken germanReading = (AnalyzedGermanToken) reading;
      if (germanReading.getNumerus() == numerus)
        return true;
    }
    return false;
  }

  public boolean hasReading(GermanToken.Genus genus) {
    if (anTokReadings == null)
      return false;
    for (AnalyzedToken reading : anTokReadings) {
      final AnalyzedGermanToken germanReading = (AnalyzedGermanToken) reading;
      if (germanReading.getGenus() == genus)
        return true;
    }
    return false;
  }

  @Override
  public String toString() {
    if (anTokReadings == null) {
      return super.getAnalyzedToken(0).getToken() + "[?]";
    }
    final StringBuilder sb = new StringBuilder(super.getAnalyzedToken(0).getToken());
    final Set<String> printed = new HashSet<String>();
    sb.append('[');
    for (AnalyzedToken reading : anTokReadings) {
      if (!printed.contains(reading.toString())) {
        if (printed.size() > 0)
          sb.append(", ");
        sb.append(reading.toString());
      }
      printed.add(reading.toString());
    }
    sb.append(']');
    return sb.toString();
  }

  /**
   * Returns a string representation like {@code toString()}, but sorts
   * the elements alphabetically.
   */
  public String toSortedString() {
    if (anTokReadings == null) {
      return super.getAnalyzedToken(0).getToken() + "[?]";
    }
    final StringBuilder sb = new StringBuilder(super.getAnalyzedToken(0).getToken());
    final Set<String> elements = new TreeSet<String>();
    sb.append('[');
    for (AnalyzedToken reading : anTokReadings) {
      if (!elements.contains(reading.toString())) {
        elements.add(reading.toString());
      }
    }
    sb.append(StringTools.listToString(elements, ", "));
    sb.append(']');
    return sb.toString();
  }

}
