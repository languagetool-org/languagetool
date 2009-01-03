/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.tokenizers.nl;

import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

public class DutchSentenceTokenizer extends SentenceTokenizer {

  private static final String[] ABBREV_LIST = { "W", "bijv", "nl", "prof",
      "ca", "mr", "blz", "vnl", "voorz", "drs", "ing", "ds", "dr", "Mgr",
      "r.-k", "red", "jl", "zgn", "enz", "evt", "jr", "zg", "nr", "mw", "ll",
      "ir", "fr", "mln", "afd", "dhr", "mevr", "excl", "cum", "dd", "Br",
      "gem", "cie", "arr", "verg", "vlg", "bep", "onbep", "Ph", "nov", "Ned",
      "div", "pag", "dir", "geh", "jhr", "Stct", "afz", "incl", "vgl", "vs",
      "etc", "j", "resp", "St", "Z" };

  // De maanden van het jaar
  private static final String[] MONTH_NAMES = { "Januari", "Februari", "Maart",
      "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober",
      "November", "December" };

  public DutchSentenceTokenizer() {
    super(ABBREV_LIST);
    super.monthNames = MONTH_NAMES;
  }

}
