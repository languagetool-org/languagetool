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

import de.danielnaber.languagetool.AnalyzedToken;
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
public class AnalyzedGermanToken extends AnalyzedToken {

  private POSType type;
  private Kasus casus;
  private Numerus numerus;
  private Genus genus;

  public AnalyzedGermanToken(String token, String posTag) {
    super(token, posTag, null);
    initFromPOSTagString(posTag);
  }

  public AnalyzedGermanToken(String token, String posTag, String lemma) {
    super(token, posTag, lemma);
    initFromPOSTagString(posTag);
  }
  
  private void initFromPOSTagString(String posTagString) {
    if (posTagString == null) {
      return;
    }
    String[] parts = posTagString.split(":");
    if (parts.length < 3) {
      //FIXME ??
      //System.err.println(posTagString);
      return;
    }
    
    //System.err.println(fullform + " " + posTagString);
    for (int i = 0; i < parts.length; i++) {
      if (parts[i].equals("EIG"))
        type = POSType.PROPER_NOUN;
      else if (parts[i].equals("SUB") && type == null)
        type = POSType.NOMEN;
      else if (parts[i].equals("PA1") || parts[i].equals("PA2"))
        type = POSType.PARTIZIP;
      else if (parts[i].equals("VER") && type == null)
        type = POSType.VERB;
      else if (parts[i].equals("ADJ") && type == null)
        type = POSType.ADJEKTIV;
      else if (parts[i].equals("PRO") && type == null)
        type = POSType.PRONOMEN;
      else if (parts[i].equals("ART") && type == null)
        type = POSType.DETERMINER;
      
      else if (parts[i].equals("AKK"))
        casus = Kasus.AKKUSATIV;
      else if (parts[i].equals("GEN"))
        casus = Kasus.GENITIV;
      else if (parts[i].equals("NOM"))
        casus = Kasus.NOMINATIV;
      else if (parts[i].equals("DAT"))
        casus = Kasus.DATIV;
      
      else if (parts[i].equals("PLU"))
        numerus = Numerus.PLURAL;
      else if (parts[i].equals("SIN"))
        numerus = Numerus.SINGULAR;
      
      else if (parts[i].equals("MAS"))
        genus = Genus.MASKULINUM;
      else if (parts[i].equals("FEM"))
        genus = Genus.FEMININUM;
      else if (parts[i].equals("NEU"))
        genus = Genus.NEUTRUM;
      else if (parts[i].equals("NOG"))
        genus = Genus.FEMININUM;    // NOG = no genus because only used as plural
      
      else if (parts[i].equals("DEF"))
        ; // not yet used
      else if (parts[i].equals("DEM"))    //???
        ; // not yet used
      else if (parts[i].equals("PER"))
        ; // not yet used
      
      //else
        //System.err.println("unknown: " + posTagString + " for fullform " + fullform);
      // TODO: add else here that throws execption?!
    }
    
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
    return getPOSTag();
  }
  
}
