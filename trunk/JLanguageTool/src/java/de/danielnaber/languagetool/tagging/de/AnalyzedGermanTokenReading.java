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

import de.danielnaber.languagetool.tagging.de.AnalyzedGermanToken.Casus;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanToken.Genus;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanToken.Numerus;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanToken.Type;

/**
 * @author Daniel Naber
 */
public class AnalyzedGermanTokenReading {

  private Type type;
  private Casus casus;
  private Numerus numerus;
  private Genus genus;

  public AnalyzedGermanTokenReading(String taggerResultAsString) {
    String[] parts = taggerResultAsString.split(" ");
    String thisType = parts[0];
    if (thisType.equals("SENT_START")) {
      type = Type.OTHER;
      return;
    }
    // FIXME:can this really be ignored?!?!
    if (thisType.equals("PRO")) {
      type = Type.OTHER;
      return;
    }
    // Type:
    if (thisType.equals("VER"))
      type = Type.VERB;
    else if (thisType.equals("SUB"))
      type = Type.NOMEN;
    else if (thisType.equals("ADJ"))
      type = Type.ADJEKTIV;
    else if (thisType.equals("ART") || thisType.equals("PRO"))
      type = Type.DETERMINER;
    else
      throw new IllegalArgumentException("Unknown category: " + thisType + ", input: " + taggerResultAsString);
    // Kasus:
    thisType = parts[1];
    if (thisType.equals("NOM"))
      casus = Casus.NOMINATIV;
    else if (thisType.equals("AKK"))
      casus = Casus.AKKUSATIV;
    else if (thisType.equals("DAT"))
      casus = Casus.DATIV;
    else if (thisType.equals("GEN"))
      casus = Casus.GENITIV;
    else
      throw new IllegalArgumentException("Unknown category: " + thisType + ", input: " + taggerResultAsString);
    // Numerus:
    thisType = parts[2];
    if (thisType.equals("SIN"))
      numerus = Numerus.SINGULAR;
    else if (thisType.equals("PLU"))
      numerus = Numerus.PLURAL;
    else
      throw new IllegalArgumentException("Unknown category: " + thisType + ", input: " + taggerResultAsString);
    // Genus:
    thisType = parts[3];
    if (thisType.equals("MAS"))
      genus = Genus.MASKULINUM;
    else if (thisType.equals("FEM"))
      genus = Genus.FEMININUM;
    else if (thisType.equals("NEU"))
      genus = Genus.NEUTRUM;
    else
      throw new IllegalArgumentException("Unknown category: " + thisType + ", input: " + taggerResultAsString);
  }
  
  public Type getType() {
    return type; 
  }

  public Casus getCasus() {
    return casus; 
  }

  public Numerus getNumerus() {
    return numerus; 
  }

  public Genus getGenus() {
    return genus; 
  }

  public String toString() {
    return type + "/" + casus +"/"+ numerus +"/"+ genus;
  }
  
}
