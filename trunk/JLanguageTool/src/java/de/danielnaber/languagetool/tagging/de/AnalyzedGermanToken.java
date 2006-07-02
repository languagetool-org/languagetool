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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.de.GermanToken.POSType;

/**
 * All possible readings of an analyzed German word.
 * 
 * @author Daniel Naber
 */
public class AnalyzedGermanToken extends AnalyzedTokenReadings {

  private List readings = new ArrayList();
  
  public AnalyzedGermanToken(String token, List readings, int startPos) {
	  //super(token, null, startPos);
	  super(new AnalyzedToken(token, null, startPos)); 
    this.readings=readings;
  }
  
  public AnalyzedGermanToken(AnalyzedTokenReadings atr) {
	  super(new AnalyzedToken(atr.getToken(), atr.getAnalyzedToken(0).getPOSTag(), atr.getStartPos()));
	  //super.readings=atr.getReadings();
  }
  
  /**
   * @return a list of {@link GermanTokenReading}s.
   */
  public List getReadings() {
    return readings;
  }

  public boolean hasReadingOfType(POSType type) {
    if (readings == null)
      return false;
    for (Iterator iter = readings.iterator(); iter.hasNext();) {
      GermanTokenReading reading = (GermanTokenReading) iter.next();
      if (reading.getType() == type)
        return true;
    }
    return false;
  }

  public boolean hasReading(GermanToken.Kasus kasus) {
    if (readings == null)
      return false;
    for (Iterator iter = readings.iterator(); iter.hasNext();) {
      GermanTokenReading reading = (GermanTokenReading) iter.next();
      if (reading.getCasus() == kasus)
        return true;
    }
    return false;
  }

  public boolean hasReading(GermanToken.Numerus numerus) {
    if (readings == null)
      return false;
    for (Iterator iter = readings.iterator(); iter.hasNext();) {
      GermanTokenReading reading = (GermanTokenReading) iter.next();
      if (reading.getNumerus() == numerus)
        return true;
    }
    return false;
  }

  public boolean hasReading(GermanToken.Genus genus) {
    if (readings == null)
      return false;
    for (Iterator iter = readings.iterator(); iter.hasNext();) {
      GermanTokenReading reading = (GermanTokenReading) iter.next();
      if (reading.getGenus() == genus)
        return true;
    }
    return false;
  }

  public String toString() {
    if (readings == null)
      return super.getAnalyzedToken(0).getToken() + "[?]";
    else {
      StringBuffer sb = new StringBuffer(super.getAnalyzedToken(0).getToken());
      Set printed = new HashSet();
      sb.append("[");
      for (Iterator iter = readings.iterator(); iter.hasNext();) {
        GermanTokenReading reading = (GermanTokenReading) iter.next();
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
