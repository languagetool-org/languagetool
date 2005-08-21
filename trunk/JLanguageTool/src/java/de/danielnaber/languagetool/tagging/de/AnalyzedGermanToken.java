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

/**
 * Experimental word information class for the German language.
 * 
 * @author Daniel Naber
 */
public class AnalyzedGermanToken extends AnalyzedToken {

  private List readings = new ArrayList();
  
  public AnalyzedGermanToken(String token, String tagInfoAsString, int startPos) {
    super(token, null, startPos);
    if (tagInfoAsString == null)
      return;
    if (tagInfoAsString.startsWith("[") && tagInfoAsString.endsWith("]"))
      tagInfoAsString = tagInfoAsString.substring(1, tagInfoAsString.length()-1);
    String[] parts = tagInfoAsString.split(",");
    for (int i = 0; i < parts.length; i++) {
      //System.err.println("##"+parts[i]);
      AnalyzedGermanTokenReading reading = new AnalyzedGermanTokenReading(parts[i].trim());
      readings.add(reading);
    }
  }

  public List getReadings() {
    return readings;
  }

  public boolean hasReadingOfType(Type type) {
    for (Iterator iter = readings.iterator(); iter.hasNext();) {
      AnalyzedGermanTokenReading reading = (AnalyzedGermanTokenReading) iter.next();
      if (reading.getType() == type)
        return true;
    }
    return false;
  }

  public static class Type {
    public static final Type NOMEN = new Type("Nomen");
    public static final Type VERB = new Type("Verb");
    public static final Type ADJEKTIV = new Type("Adjektiv");
    public static final Type DETERMINER = new Type("Determiner");
    public static final Type OTHER = new Type("Other");      // e.g. sentence start

    private String name;
    
    private Type(String name) {
      this.name = name;
    }
    
    public String toString() {
      return name;
    }
  }
  
  public static class Casus {
    public static final Casus NOMINATIV = new Casus("Nominativ");
    public static final Casus AKKUSATIV = new Casus("Akkusativ");
    public static final Casus DATIV = new Casus("Dativ");
    public static final Casus GENITIV = new Casus("Genitiv");

    private String name;
    
    private Casus(String name) {
      this.name = name;
    }
    
    public String toString() {
      return name;
    }
  }

  public static class Numerus {
    public static final Numerus SINGULAR = new Numerus("Singular");
    public static final Numerus PLURAL = new Numerus("Plural");

    private String name;
    
    private Numerus(String name) {
      this.name = name;
    }
    
    public String toString() {
      return name;
    }
  }

  public static class Genus {
    public static final Genus NEUTRUM = new Genus("Neutrum");
    public static final Genus MASKULINUM = new Genus("Maskulinum");
    public static final Genus FEMININUM = new Genus("Femininum");

    private String name;
    
    private Genus(String name) {
      this.name = name;
    }
    
    public String toString() {
      return name;
    }
  }

}
