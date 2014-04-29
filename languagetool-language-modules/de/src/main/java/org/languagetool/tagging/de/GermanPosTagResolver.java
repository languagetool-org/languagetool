/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.tagging.TokenPoS;
import org.languagetool.tagging.TokenPoSBuilder;

import java.util.*;

/**
 * Maps Morphy tags like {@code SUB:GEN:PLU:FEM} to structured {@link TokenPoS}.
 * @since 2.6
 */
class GermanPosTagResolver {
  
  private static final Map<String,String> MAP;
  static {
    Map<String,String> m = new HashMap<>();
    m.put("NOM", "nominativ");
    m.put("AKK", "akkusativ");
    m.put("DAT", "dativ");
    m.put("GEN", "genitiv");

    m.put("SIN", "singular");
    m.put("PLU", "plural");
    
    m.put("MAS", "maskulinum");
    m.put("FEM", "femininum");
    m.put("NEU", "neutrum");
    m.put("NOG", "ohne_genus");

    m.put("ART", "mit");  // mit Artikel
    m.put("NOA", "ohne");  // ohne Artikel

    m.put("COU", "land");
    m.put("MOU", "gebirge");
    m.put("VOR", "vorname");
    m.put("NAC", "nachname");
    m.put("GEO", "geographischer_eigenname");
    m.put("GEB", "gebiet");
    m.put("PER", "personal");
    m.put("STD", "stadt");
    m.put("WAT", "gewässer");

    m.put("1", "1");
    m.put("2", "2");
    m.put("3", "3");

    m.put("SFT", "schwach");
    m.put("NON", "nicht-schwach");

    m.put("AUX", "hilfsverb");
    m.put("MOD", "modalverb");

    m.put("INF", "infinitiv");
    m.put("EIZ", "erw_infinitiv_mit_zu");
    m.put("IMP", "imperativ");
    m.put("PA1", "partizip1");
    m.put("PA2", "partizip2");
    
    m.put("KJ1", "konjunktiv1");
    m.put("KJ2", "konjunktiv2");
    m.put("PRT", "präteritum_imperfekt");
    m.put("PRÄ", "präsens");

    m.put("DEF", "bestimmt");
    m.put("IND", "unbestimmt");
    m.put("SOL", "alleinstehend");

    m.put("ATT", "attributiv");
    m.put("PRD", "prädikativ");

    m.put("GRU", "grundform");
    m.put("KOM", "komparativ");
    m.put("SUP", "superlativ");

    m.put("BEG", "begleitend");
    m.put("STV", "stellvertretend");
    // B/S => BEG|STV   see handlePronoun...()

    m.put("DEM", "demonstrativ");
    m.put("IND", "unbestimmt");
    m.put("REL", "relativ");
    m.put("INR", "interrogativ");
    m.put("PER", "personal");
    m.put("REF", "reflexiv");
    m.put("POS", "possessiv");
    // RIN => INR|REL   see handlePronoun...() 

    m.put("LOK", "lokal");
    m.put("TMP", "temporal");
    m.put("MOD", "modal");
    m.put("CAU", "kausal");
    m.put("INR", "interrogativ");
    m.put("PRO", "pronomen");

    m.put("NEB", "nebenordnend");
    m.put("UNT", "unterordnend");
    m.put("VGL", "vergleichend");
    m.put("INF", "infinitiv");
    m.put("PRI", "proportional");
    
    m.put("A", "höflich");
    m.put("B", "vertraut");
    
    MAP = Collections.unmodifiableMap(m);
  }

  private boolean strictMode;

  List<TokenPoS> resolvePOSTag(String posTag) {
    if (posTag == null) {
      return Collections.emptyList();
    }
    String[] parts = posTag.split(":");
    try {
      switch (parts[0]) {
        case "SUB":
          return handleNoun(parts);
        case "EIG":
          return handleProperNoun(parts);
        case "PA1":
        case "PA2":
          return handleParticiple(posTag, parts);
        case "VER":
          return handleVerb(posTag, parts);
        case "ADJ":
          return handleAdjective(posTag, parts);
        case "ART":
          return handleDeterminer(parts);
        case "PRO":
          return handlePronoun(posTag, parts);
        case "ADV":
          return handleAdverb(parts);
        case "PRP":
          return handlePreposition(parts);
        case "NEG":
          return l(pos("negationspartikel"));
        case "ABK":
          return l(pos("abkürzung"));
        case "ZAL":
          return l(pos("zahlwort"));
        case "INJ":
          return l(pos("interjektion"));
        case "ZUS":
          return l(pos("verbzusatz"));
        case "KON":
          return l(pos("konjunktion").add("konjunktion", get(parts[1])));
      }
    } catch (Exception e) {
      if (strictMode) {
        throw new RuntimeException("Could not map Morphy POS tag '" + posTag + "'", e);
      }
    }
    return assertMapping(posTag);
  }

  /** Fail with exception if no mapping is possible. */
  void setStrictResolveMode(boolean strictMode) {
    this.strictMode = strictMode;
  }

  private List<TokenPoS> handleNoun(String[] parts) {
    // e.g. SUB:DAT:SIN:MAS:ADJ
    return l(pos("nomen")
            .add("kasus", get(parts[1]))
            .add("numerus", get(parts[2]))
            .add("genus", get(parts[3])));
  }

  private List<TokenPoS> handleProperNoun(String[] parts) {
    // e.g. EIG:AKK:SIN:MAS:ART:VOR
    return l(pos("eigenname")
            .add("kasus", get(parts[1]))
            .add("numerus", get(parts[2]))
            .add("genus", get(parts[3]))
            .add("artikel", get(parts[4]))
            .add("eigenname", get(parts[5])));
  }

  private List<TokenPoS> handleParticiple(String posTag, String[] parts) {
    String participle = "PA1".equals(parts[0]) ? "1" : "2";
    if (parts.length == 3 || parts.length == 4) {
      // e.g. PA1:PRD:GRU:VER
      return l(pos("verb")
              .add("gebrauch", get(parts[1]))
              .add("komparation", get(parts[2]))
              .add("partizip", get(participle))   // extension to Morphy
      );
    } else if (parts.length == 5) {
      // e.g. PA2:PRD:GRU:VER:MOD
      return l(pos("verb")
              .add("gebrauch", get(parts[1]))
              .add("komparation", get(parts[2]))
              .add("form", get(parts[4]))
              .add("partizip", get(participle))   // extension to Morphy
      );
    } else if (parts.length == 6 || parts.length == 7) {
      // e.g. PA1:AKK:PLU:FEM:GRU:SOL:VER
      return l(pos("verb")
              .add("kasus", get(parts[1]))
              .add("numerus", get(parts[2]))
              .add("genus", get(parts[3]))
              .add("komparation", get(parts[4]))
              .add("art", get(parts[5]))
              .add("partizip", get(participle))   // extension to Morphy
      );
    } else {
      return assertMapping(posTag);
    }
  }

  private List<TokenPoS> handleVerb(String posTag, String[] parts) {
    if (parts.length == 2) {
      // e.g. VER:MOD
      return l(pos("verb")
              .add("form", get(parts[1])));
    } else if (parts.length == 3) {
      // e.g. VER:INF:SFT
      return l(pos("verb")
              .add("form", get(parts[1]))
              .add("konjugation", get(parts[2])));
    } else if (parts.length == 4) {
      // e.g. VER:IMP:SIN:SFT
      return l(pos("verb")
              .add("form", get(parts[1]))
              .add("numerus", get(parts[2]))
              .add("konjugation", get(parts[3])));
    } else if (parts.length >= 5) {
      // e.g. VER:3:PLU:PRT:NON
      TokenPoSBuilder builder = pos("verb")
              .add("person", get(parts[1]))
              .add("numerus", get(parts[2]))
              .add("modus", get(parts[3]))
              .add("konjugation", get(parts[4]));
      if (parts.length == 6) {
        // e.g. VER:1:PLU:KJ1:SFT:NEB
        builder.add("gebrauch", "nebensatz");
      }
      return l(builder);
    } else {
      return assertMapping(posTag);
    }
  }

  private List<TokenPoS> handleAdjective(String posTag, String[] parts) {
    if (parts.length == 3) {
      // e.g. ADJ:PRD:KOM
      return l(pos("adjektiv")
              .add("gebrauch", get(parts[1]))
              .add("komparation", get(parts[2])));
    } else if (parts.length == 6) {
      // e.g. ADJ:DAT:SIN:MAS:SUP:DEF
      return l(pos("adjektiv")
              .add("kasus", get(parts[1]))
              .add("numerus", get(parts[2]))
              .add("genus", get(parts[3]))
              .add("komparation", get(parts[4]))
              .add("art", get(parts[5]))
      );
    } else {
      return assertMapping(posTag);
    }
  }

  private List<TokenPoS> handleDeterminer(String[] parts) {
    // z.B. ART:DEF:NOM:PLU:MAS
    return l(pos("artikel")
            .add("artikel", get(parts[1]))
            .add("kasus", get(parts[2]))
            .add("numerus", get(parts[3]))
            .add("genus", get(parts[4]))
    );
  }

  private List<TokenPoS> handlePronoun(String posTag, String[] parts) {
    if (parts.length == 2) {
      // only PRO:DEM (selber, selbst)
      return l(pos("artikel")
              .add("pronomen", get(parts[1])));
    } else if (parts.length == 4) {
      // e.g. PRO:RIN:DAT:MAS
      TokenPoSBuilder builder = pos("pronomen")
              .add("kasus", get(parts[2]))
              .add("genus", get(parts[3]));
      handlePronounProperty(parts[1], builder);
      return l(builder);
    } else if (parts.length >= 5) {
      TokenPoSBuilder builder = pos("pronomen");
      if ("ALG".equals(parts[3])) {
        // e.g. PRO:IND:SIN:ALG:2:B
        handlePronounProperty(parts[1], builder);
        builder.add("numerus", get(parts[2]));
        handlePronounGender(parts[3], builder);
      } else {
        // e.g. PRO:RIN:NOM:SIN:NEU:B/S
        handlePronounProperty(parts[1], builder);
        builder.add("kasus", get(parts[2]))
               .add("numerus", get(parts[3]));
        handlePronounGender(parts[4], builder);
      }
      if (parts.length == 6) {
        handlePronounPosition(parts[5], builder);
      }
      return l(builder);
    } else {
      return assertMapping(posTag);
    }
  }

  private List<TokenPoS> handleAdverb(String[] parts) {
    // e.g. ADV:MOD+TMP+LOK
    TokenPoSBuilder builder = pos("adverb");
    if (parts.length > 1) {
      addParts(parts[1], "adverb", builder);
    }
    return l(builder);
  }

  private List<TokenPoS> handlePreposition(String[] parts) {
    // e.g. PRP:LOK+TMP+MOD+CAU:DAT
    TokenPoSBuilder propBuilder = pos("präposition");
    addParts(parts[1], "präposition", propBuilder);
    if (parts.length > 2) {
      addParts(parts[2], "kasus", propBuilder);
    }
    return l(propBuilder);
  }

  // =====================================================================

  private List<TokenPoS> assertMapping(String posTag) {
    if (strictMode) {
      throw new RuntimeException("posTag '" + posTag + "' not yet handled");
    }
    return l();
  }

  private void addParts(String part, String name, TokenPoSBuilder propBuilder) {
    String[] prpParts = part.split("\\+");
    for (String propPart : prpParts) {
      propBuilder.add(name, get(propPart));
    }
  }

  private void handlePronounProperty(String part, TokenPoSBuilder builder) {
    if ("RIN".equals(part)) {
      builder.add("pronomen", get("REL"));
      builder.add("pronomen", get("INR"));
    } else {
      builder.add("pronomen", get(part));
    }
  }

  private void handlePronounPosition(String part, TokenPoSBuilder builder) {
    if ("B/S".equals(part)) {
      builder.add("stellung", get("BEG"));
      builder.add("stellung", get("STV"));
    } else {
      builder.add("stellung", get(part));
    }
  }

  private void handlePronounGender(String part, TokenPoSBuilder builder) {
    if ("ALG".equals(part)) {
      builder.add("genus", get("MAS"));
      builder.add("genus", get("FEM"));
      builder.add("genus", get("NEU"));
    } else {
      builder.add("genus", get(part));
    }
  }

  private String get(String key) {
    String val = MAP.get(key);
    if (val == null) {
      throw new RuntimeException("No value found for key '" + key + "'");
    }
    return val;
  }

  private TokenPoSBuilder pos(String posType) {
    return new TokenPoSBuilder().add("pos", posType);
  }

  private List<TokenPoS> l(TokenPoSBuilder... builders) {
    List<TokenPoS> result = new ArrayList<>();
    for (TokenPoSBuilder builder : builders) {
      result.add(builder.create());
    }
    return result;
  }
}
