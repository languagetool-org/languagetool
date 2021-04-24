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

/**
 * Constants used to describe the properties of German tokens.
 * 
 * @author Daniel Naber
 */
public final class GermanToken {

  private GermanToken() {
    // only static stuff
  }

  public static final class POSType {
    public static final POSType NOMEN = new POSType("Nomen");
    public static final POSType VERB = new POSType("Verb");
    public static final POSType ADJEKTIV = new POSType("Adjektiv");
    public static final POSType DETERMINER = new POSType("Determiner");
    public static final POSType PRONOMEN = new POSType("Pronomen");
    public static final POSType PARTIZIP = new POSType("Partizip");
    public static final POSType PROPER_NOUN = new POSType("Eigenname");
    public static final POSType OTHER = new POSType("Other");      // e.g. sentence start

    private final String name;
    
    private POSType(String name) {
      this.name = name;
    }
    
    @Override
    public String toString() {
      return name;
    }
  }
  
  public static final class Kasus {
    public static final Kasus NOMINATIV = new Kasus("Nominativ");
    public static final Kasus AKKUSATIV = new Kasus("Akkusativ");
    public static final Kasus DATIV = new Kasus("Dativ");
    public static final Kasus GENITIV = new Kasus("Genitiv");
    public static final Kasus OTHER = new Kasus("Other");

    private final String name;
    
    private Kasus(String name) {
      this.name = name;
    }
    
    @Override
    public String toString() {
      return name;
    }
  }

  public static final class Numerus {
    public static final Numerus SINGULAR = new Numerus("Singular");
    public static final Numerus PLURAL = new Numerus("Plural");
    public static final Numerus OTHER = new Numerus("Other");

    private final String name;
    
    private Numerus(String name) {
      this.name = name;
    }
    
    @Override
    public String toString() {
      return name;
    }
  }

  public static final class Genus {
    public static final Genus NEUTRUM = new Genus("Neutrum");
    public static final Genus MASKULINUM = new Genus("Maskulinum");
    public static final Genus FEMININUM = new Genus("Femininum");
    public static final Genus OTHER = new Genus("Other");
    public static final Genus ALLGEMEIN = new Genus("Allgemein");   // Morphy ALG = Mas, Fem, or Neu

    private final String name;
    
    private Genus(String name) {
      this.name = name;
    }
    
    @Override
    public String toString() {
      return name;
    }
  }

  /** @since 3.2 */
  public static final class Determination {
    public static final Determination DEFINITE = new Determination("definit");
    public static final Determination INDEFINITE = new Determination("indefinit");

    private final String name;

    private Determination(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
  
}
