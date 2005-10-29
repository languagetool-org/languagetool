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

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.de.GermanToken.Kasus;
import de.danielnaber.languagetool.tagging.de.GermanToken.Genus;
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
   * @param morphyString For example: <code>ADJ NOM SIN MAS</code>
   */
  public static GermanTokenReading createTokenReadingFromMorphyString(String morphyString, String token) {
    String[] parts = morphyString.split(" ");
    POSType type = null;
    Kasus casus = null;
    Numerus numerus = null;
    Genus genus = null;
    if (parts.length == 1 && parts[0].equals(JLanguageTool.SENTENCE_START_TAGNAME)) {
      return new GermanTokenReading(POSType.OTHER, null, null, null);
    }
    if (parts.length != 4) {
      // FIXME: throw exception?!
      System.err.println("WARNING: unknown format: " + morphyString + " for " + token);
      return new GermanTokenReading(POSType.OTHER, null, null, null);
    }
    String thisType = parts[0];
    // FIXME:can this really be ignored?!?!
    if (thisType.equals("PRO")) {
      return new GermanTokenReading(POSType.OTHER, null, null, null);
    }
    // Type:
    if (thisType.equals("VER"))
      type = POSType.VERB;
    else if (thisType.equals("SUB") || thisType.equals("EIG"))
      type = POSType.NOMEN;
    else if (thisType.equals("ADJ"))
      type = POSType.ADJEKTIV;
    else if (thisType.equals("ART") || thisType.equals("PRO"))
      type = POSType.DETERMINER;
    else
      type = POSType.OTHER;
    // Kasus:
    thisType = parts[1];
    if (thisType.equals("NOM"))
      casus = Kasus.NOMINATIV;
    else if (thisType.equals("AKK"))
      casus = Kasus.AKKUSATIV;
    else if (thisType.equals("DAT"))
      casus = Kasus.DATIV;
    else if (thisType.equals("GEN"))
      casus = Kasus.GENITIV;
    else
      casus = Kasus.OTHER;
    // Numerus:
    thisType = parts[2];
    if (thisType.equals("SIN"))
      numerus = Numerus.SINGULAR;
    else if (thisType.equals("PLU"))
      numerus = Numerus.PLURAL;
    else
      numerus = Numerus.OTHER;
    // Genus:
    thisType = parts[3];
    if (thisType.equals("MAS"))
      genus = Genus.MASKULINUM;
    else if (thisType.equals("FEM"))
      genus = Genus.FEMININUM;
    else if (thisType.equals("NEU"))
      genus = Genus.NEUTRUM;
    else if (thisType.equals("NOG"))  //FIXME?
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
