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
package de.danielnaber.languagetool.tokenizers.de;

import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

public class GermanSentenceTokenizer extends SentenceTokenizer {

  private static final String[] ABBREV_LIST = {
    "d", "Übers", "usw", "bzw", "Abh", "Abk", "Abt", "ahd", "Akk",
    "allg", "alltagsspr", "altdt", "alttest", "amerikan", "Anh",
    "Ank", "Anm", "Art", "Az", "Bat", "bayr", "Bd", "Bde", "Bed",
    "Bem", "bes", "bez", "Bez", "Bhf", "bspw", "btto", "bw", "bzw",
    "cts", "ct", "ca", "chem", "chin", "Chr", "cresc", "dat", "Dat",
    "desgl", "ders", "dgl", "Di", "Dipl", "Dir", "Do", "Doz", "Dr",
    "dt", "ebd", "Ed", "eigtl", "engl", "Erg", "al", "etc", "etw",
    "ev", "evtl", "exkl", "Expl", "Exz", "ff", "Fa", "fachspr", "fam",
    "fem", "Fem", "Fr", "fr", "franz", "frz", "frdl", "Frl",
    "Fut", "Gd", "geb", "gebr", "Gebr", "geh", "geh", "geleg", "gen",
    "Gen", "germ", "gesch", "ges", "get", "ggf", "Ggs", "ggT",
    "griech", "hebr", "hg", "Hrsg", "Hg", "hist", "hochd", "hochspr",
    "Hptst", "Hr", "Allg", "ill", "inkl", "incl", "Ind", "Inf", "Ing",
    "ital", "Tr", "Jb", "Jg", "Jh", "jmd", "jmdm", "jmdn", "jmds",
    "jur", "Kap", "kart", "kath", "kfm", "kaufm", "Kfm", "kgl",
    "Kl", "Konj", "Krs", "Kr", "Kto", "lat", "lfd", "Lit", "lt",
    "Lz", "Mask", "mask", "Mrd", "mdal", "med", "met", "mhd", "Mi",
    "Mio", "min", "Mo", "mod", "nachm", "nördlBr", "neutr",
    "Nhd", "Nom", "Nr", "Nrn", "Num", "Obj", "od", "dgl", "offz",
    "Part", "Pass", "Perf", "Pers", "Pfd", "Pl", "Plur",
    "pl", "Plusq", "Pos", "pp", "Präp", "Präs", "Prät", "Prov", "Prof",
    "rd", "reg", "resp", "Rhld", "rit", "Sa", "südl", "Br",
    "sel", "sen", "Sept", "Sing", "sign", "So", "sog", "Sp", "St",
    "St", "St", "Std", "stacc", "Str", "stud", "Subst", "sva", "svw",
    "sZ", "Temp", "trans", "Tsd", "übertr", "übl", "ff", "ugs", "univ",
    "urspr", "usw", "vgl", "Vol", "vorm", "vorm", "Vp", "Vs",
    "vs", "wg", "Hd", "Ztr", "zus", "Zus", "zzt", "zz", "Zz", "Zt" };

  // einige deutsche Monate, vor denen eine Zahl erscheinen kann,
  // ohne dass eine Satzgrenze erkannt wird (z.B. "am 13. Dezember" -> keine Satzgrenze)
  private static final String[] MONTH_NAMES = { "Januar", "Februar", "März", "April", "Mai",
      "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" };

  public GermanSentenceTokenizer() {
    super(ABBREV_LIST);
    super.monthNames = MONTH_NAMES;
  }
 
}
