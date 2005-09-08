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
package de.danielnaber.languagetool.tagging.de;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.tagging.de.GermanToken.POSType;

/**
 * All possible readings of an analyzed German word.
 * 
 * @author Daniel Naber
 */
public class AnalyzedGermanToken extends AnalyzedToken {

  private List readings = new ArrayList();
  
  /**
   * @param token the analyzed word
   * @param tagInfoAsString comma-separated list of readings, e.g. <code>ADJ NOM SIN MAS, ADJ NOM SIN FEM</code>
   * @param startPos
   * @deprecated
   */
  // FIXME: remove?
  public AnalyzedGermanToken(String token, String tagInfoAsString, int startPos) {
    super(token, null, startPos);
    if (tagInfoAsString == null)
      return;
    if (tagInfoAsString.startsWith("[") && tagInfoAsString.endsWith("]"))
      tagInfoAsString = tagInfoAsString.substring(1, tagInfoAsString.length()-1);
    String[] parts = tagInfoAsString.split(",");
    for (int i = 0; i < parts.length; i++) {
      GermanTokenReading reading = GermanTokenReading.createTokenReadingFromMorphyString(parts[i].trim(), token);
      readings.add(reading);
    }
  }

  public AnalyzedGermanToken(String token, List readings, int startPos) {
    super(token, null, startPos);
    this.readings = readings;
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

  public String toString() {
    if (readings == null)
      return token + ":<unknown>";
    else
      return token + ":" + readings.toString();
  }

}
