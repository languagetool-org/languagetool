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

import de.danielnaber.languagetool.tagging.de.GermanToken.Genus;
import de.danielnaber.languagetool.tagging.de.GermanToken.Kasus;
import de.danielnaber.languagetool.tagging.de.GermanToken.Numerus;
import de.danielnaber.languagetool.tagging.de.GermanToken.POSType;

/**
 * One reading of a German word. Many words can have more
 * than one reading, e.g. "Tische" can be both Nominativ Plural
 * and Genitiv Plural (among other readings).
 * 
 * @author Daniel Naber
 */
public class GermanTokenReading {

  private POSType type;
  private Kasus casus;
  private Numerus numerus;
  private Genus genus;

  public GermanTokenReading(POSType type, Kasus casus, Numerus numerus, Genus genus) {
    this.type = type;
    this.casus = casus;
    this.numerus = numerus;
    this.genus = genus;
  }
  
  /**
   * @param morphyString For example: <code>JNSM</code> (short for "ADJ NOM SIN MAS")
   */
  public static GermanTokenReading createTokenReadingFromMorphyString(String morphyString, String token) {
    if (morphyString.length() != 4) {
      throw new RuntimeException("Unknown format: " + morphyString + " for " + token);
    }
    char[] parts = new char[4];
    parts[0] = morphyString.charAt(0);
    parts[1] = morphyString.charAt(1);
    parts[2] = morphyString.charAt(2);
    parts[3] = morphyString.charAt(3);
    POSType type = null;
    Kasus casus = null;
    Numerus numerus = null;
    Genus genus = null;
    //FIXME?!
    /*if (parts.length == 1 && parts[0].equals(JLanguageTool.SENTENCE_START_TAGNAME)) {
      return new GermanTokenReading(POSType.OTHER, null, null, null);
    }*/
    // FIXME: can this really be ignored?!?!
    if (parts[0] == 'O') {     // PRO
      return new GermanTokenReading(POSType.OTHER, null, null, null);
    }
    // Type:
    if (parts[0] == 'V')     // VER
      type = POSType.VERB;
    else if (parts[0] == 'S' || parts[0] == 'E')      // SUB,  EIG
      type = POSType.NOMEN;
    else if (parts[0] == 'J')          // ADJ
      type = POSType.ADJEKTIV;
    else if (parts[0] == 'T')        // ART
      type = POSType.DETERMINER;
    else
      type = POSType.OTHER;
    // Kasus:
    if (parts[1] == 'N')         // NOM
      casus = Kasus.NOMINATIV;
    else if (parts[1] == 'A')    // AKK
      casus = Kasus.AKKUSATIV;
    else if (parts[1] == 'D')    // DAT
      casus = Kasus.DATIV;
    else if (parts[1] == 'G')    // GEN
      casus = Kasus.GENITIV;
    else
      casus = Kasus.OTHER;
    // Numerus:
    if (parts[2] == 'S')     // SIN
      numerus = Numerus.SINGULAR;
    else if (parts[2] == 'P')    // PLU
      numerus = Numerus.PLURAL;
    else
      numerus = Numerus.OTHER;
    // Genus:
    if (parts[3] == 'M')     // MAS
      genus = Genus.MASKULINUM;
    else if (parts[3] == 'F')    // FEM
      genus = Genus.FEMININUM;
    else if (parts[3] == 'N')    // NEU
      genus = Genus.NEUTRUM;
    else if (parts[3] == 'O')  // NOG, FIXME?
      genus = Genus.OTHER;
    return new GermanTokenReading(type, casus, numerus, genus);
  }

  public POSType getType() {
    return type; 
  }

  public Kasus getCasus() {
    return casus; 
  }

  public Numerus getNumerus() {
    return numerus; 
  }

  public Genus getGenus() {
    return genus; 
  }

  public String toString() {
    String casusStr = makeReadableString(casus);
    String numerusStr = makeReadableString(numerus);
    String genusStr = makeReadableString(genus);
    return type + "/" + casusStr +"/"+ numerusStr +"/"+ genusStr;
  }
  
  private String makeReadableString(Object obj) {
    final int length = 3;
    String str = null;
    if (obj == null)
      str = "-";
    else
      str = obj.toString().substring(0, length);
    return str;
  }
  
}
