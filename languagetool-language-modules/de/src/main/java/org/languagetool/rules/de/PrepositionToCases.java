/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import java.util.*;

import static org.languagetool.rules.de.PrepositionToCases.Case.*;

/**
 * Note: this is tuned for AgreementSuggestor, be careful when using it for other use cases.
 * @since 5.4
 */
class PrepositionToCases {
  
  private final static Map<String, List<Case>> prep2Cases = new HashMap<>();
  
  enum Case {
    Nom, Akk, Gen, Dat
  }
  
  static {
    // sources: http://deutsche-rechtschreibung.org/woerterbuecher/rektion-praepositionen.php,
    //  https://deutschegrammatik20.de/spezielle-verben/verben-mit-praeposition/verb-mit-praeposition-kasus/
    add("ab", Dat);
    add("abseits", Gen);
    add("abzüglich", Gen);
    add("an", Dat, Akk);
    add("angesichts", Gen);
    add("anhand", Gen);
    add("anlässlich", Gen);
    add("anstatt", Gen);
    add("anstelle", Gen);
    add("auf", Dat, Akk);
    add("aufgrund", Gen);
    add("aus", Dat);
    add("ausgangs", Gen);
    add("ausgenommen", Akk);
    add("ausschließlich", Gen);
    add("ausweislich", Gen);
    add("außer", Dat);
    add("außerhalb", Gen);
    add("bar", Gen);
    add("behufs", Gen);
    add("bei", Dat);
    add("beidseits", Gen);
    add("betreffend", Akk);
    add("betreffs", Gen);
    add("bezüglich", Gen);
    add("binnen", Dat);
    add("bis", Akk);
    add("bis auf", Akk);
    add("dank", Dat, Gen);
    add("diesseits", Gen);
    add("durch", Akk);
    add("eingangs", Gen);
    add("eingedenk", Gen);
    add("einschließlich", Gen);
    add("entgegen", Dat);
    add("entlang", Dat, Akk, Gen);
    add("entsprechend", Dat);
    add("exklusive", Gen);
    add("fern", Dat);
    add("fernab", Gen);
    add("für", Akk);
    add("gegen", /*Dat,*/ Akk);
    add("gegenüber", Dat);
    add("gemäß", Dat);
    add("getreu", Dat);
    add("halber", Gen);
    add("hinsichtlich", Gen);
    add("hinter", Dat, Akk);
    //add("in", Dat, Akk);  // "und in das Sauerstoff nur ..."
    add("in puncto", Gen);
    add("infolge", Gen);
    add("inklusive", Gen);
    add("inmitten", Gen);
    add("innerhalb", Gen);
    add("innert", Gen);
    add("je", Akk);
    add("jenseits", Gen);
    add("kontra", Akk);
    add("kraft", Gen);
    add("laut", Gen, Dat);
    add("längs", Gen);
    add("längsseits", Gen);
    add("mangels", Gen);
    add("mit", Dat);
    add("mitsamt", Dat);
    add("mittels", Gen);
    add("nach", Dat);
    add("namens", Gen);
    add("neben", Dat, Akk);
    add("nebst", Dat);
    add("nächst", Dat);
    //add("ob", Gen);   // "ob das Touristen überhaupt betrifft"
    add("oberhalb", Gen);
    add("ohne", Akk);
    add("per", Akk);
    add("pro", Akk);
    add("rücksichtlich", Gen);
    add("samt", Dat);
    add("seit", Dat);
    add("seitens", Gen);
    add("seitlich", Gen);
    add("statt", Gen);
    add("trotz", Gen);
    add("um", Akk);
    add("unbeschadet", Gen);
    add("uneingedenk", Gen);
    add("ungeachtet", Gen);
    add("unter", Dat, Akk);
    add("unterhalb", Gen);
    add("unweit", Gen);
    add("vermittels", Gen);
    add("vermittelst", Gen);
    add("vermöge", Gen);
    add("versus", Akk);
    add("via", Akk);
    add("vis-à-vis", Dat);
    add("von", Dat);
    add("vor", Dat, Akk);
    add("vorbehaltlich", Gen);
    add("wegen", Gen);
    add("weitab", Gen);
    add("wider", Akk);
    add("während", Gen);
    add("zeit", Gen);
    add("zu", Dat);
    add("zufolge", Dat);
    add("zugunsten", Gen);
    add("zuliebe", Dat);
    add("zuungunsten", Gen);
    add("zuwider", Dat);
    add("zuzüglich", Gen);
    add("zwecks", Gen);
    add("zwischen", Dat, Akk);
    add("à", Akk);
    add("über", Dat, Akk);
  }

  private PrepositionToCases() {
  }

  private static void add(String prep, Case... cases) {
    prep2Cases.put(prep, Arrays.asList(cases));
  }
  
  public static List<Case> getCasesFor(String prep) {
    return prep2Cases.getOrDefault(prep.toLowerCase(), new ArrayList<>());
  }
}
