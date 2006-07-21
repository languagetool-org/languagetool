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

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.de.GermanToken.POSType;

/**
 * All possible readings of an analyzed German word.
 * 
 * @author Daniel Naber
 */
public class AnalyzedGermanTokenReadings extends AnalyzedTokenReadings {

  public AnalyzedGermanTokenReadings(AnalyzedGermanToken[] aTokens) {
	  super(aTokens);
  }
  
  public AnalyzedGermanTokenReadings(AnalyzedGermanToken aToken) {
    super(aToken);
  }
  
  /**
   * @return a list of {@link AnalyzedGermanToken}s.
   */
  public List<AnalyzedGermanToken> getGermanReadings() {
    List<AnalyzedGermanToken> l = new ArrayList<AnalyzedGermanToken>();
    for (int i = 0; i < ATreadings.length; i++) {
      l.add((AnalyzedGermanToken)ATreadings[i]);
    }
    return l;
  }

  public boolean hasReadingOfType(POSType type) {
    if (ATreadings == null)
      return false;
    for (AnalyzedToken reading : ATreadings) {
      AnalyzedGermanToken germanReading = (AnalyzedGermanToken) reading;
      if (germanReading.getType() == type)
        return true;
    }
    return false;
  }

  public boolean hasReading(GermanToken.Kasus kasus) {
    if (ATreadings == null)
      return false;
    for (AnalyzedToken reading : ATreadings) {
      AnalyzedGermanToken germanReading = (AnalyzedGermanToken) reading;
      if (germanReading.getCasus() == kasus)
        return true;
    }
    return false;
  }

  public boolean hasReading(GermanToken.Numerus numerus) {
    if (ATreadings == null)
      return false;
    for (AnalyzedToken reading : ATreadings) {
      AnalyzedGermanToken germanReading = (AnalyzedGermanToken) reading;
      if (germanReading.getNumerus() == numerus)
        return true;
    }
    return false;
  }

  public boolean hasReading(GermanToken.Genus genus) {
    if (ATreadings == null)
      return false;
    for (AnalyzedToken reading : ATreadings) {
      AnalyzedGermanToken germanReading = (AnalyzedGermanToken) reading;
      if (germanReading.getGenus() == genus)
        return true;
    }
    return false;
  }

  public String toString() {
    if (ATreadings == null)
      return super.getAnalyzedToken(0).getToken() + "[?]";
    else {
      StringBuffer sb = new StringBuffer(super.getAnalyzedToken(0).getToken());
      Set<String> printed = new HashSet<String>();
      sb.append("[");
      for (AnalyzedToken reading : ATreadings) {
        if (!printed.contains(reading.toString())) {
          if (printed.size() > 0)
            sb.append(", ");
          sb.append(reading.toString());
        }
        printed.add(reading.toString());
      }
      sb.append("]");
      return sb.toString();
    }
  }

}
