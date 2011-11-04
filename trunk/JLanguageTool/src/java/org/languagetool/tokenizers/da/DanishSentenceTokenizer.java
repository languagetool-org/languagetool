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
package de.danielnaber.languagetool.tokenizers.da;

import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

/**
 * @deprecated use {@code new SRXSentenceTokenizer("da")} instead
 * @author Daniel Naber
 */
public class DanishSentenceTokenizer extends SentenceTokenizer {

  private static final String[] ABBREV_LIST = {
"abs", "abstr", "adj", "adm", "adr", "adv", "afd", "afg", "afl", "afs", "afvig", "agro", "akad", "akk", "allr", "alm", "amer", "anat", "ang", "anm", "anv", "apot", "appos", "apr", "arab", "arkais", "arkæol", "arp", "arr", "art", "ass", "astr", "att", "attrib", "aud", "aug", "aut", "bag", "barb", "barnespr", "bd", "bdt", "beg", "besl", "best", "bet", "bhk", "biavl", "bibet", "bibl", "bibliot", "billard", "billedl", "biol", "bjergv", "bk", "bl", "bogb", "bogh", "bogtr", "bornh", "bot", "br", "bryg", "bto", "bygn", "bødk", "ca", "cand", "Chr", "cirk", "cit", "co", "d", "da", "dagl", "dans", "dat", "dec", "def", "demonstr", "dep", "dial", "diam", "dim", "disp", "distr", "distrib", "dobb", "dr", "dvs", "e", "egl", "eks", "eksam", "ekskl", "eksp", "ekspl", "el", "ell", "ellipt", "emb", "endv", "eng", "enk", "ent", "etc", "etnogr", "eufem", "eur", "event", "evt", "f", "fagl", "fakt", "farv", "feb", "ff", "fhv", "fig", "filos", "fisk", "fk", "fl", "flg", "flt", "flyv", "fmd", "fon", "foragt", "forb", "foreg", "forf", "forsikr", "forsk", "forst", "foræld", "fot", "fr", "fre", "fris", "frk", "fsv", "fuldm", "fx", "fys", "fysiol", "fægt", "gart", "gartn", "garv", "gdr", "gen", "genopt", "geogr", "geol", "geom", "germ", "gl", "glarm", "glda", "gldgs", "glholl", "glno", "gns", "got", "gr", "gradbøjn", "gram", "gross", "grundbet", "græc", "guldsm", "gym", "h", "hat", "hd", "hebr", "henh", "hensobj", "herald", "hhv", "hist", "hj", "holl", "hovedbet", "hr", "hty", "højtid", "haandarb", "haandv", "i", "if", "ifm", "ift", "iht", "imp", "indb", "indik", "inf", "ing", "Inkl", "inkl", "insp", "instr", "interj", "intk", "intr", "iron", "isl", "ital", "jan", "jarg", "jf", "jnr", "jr", "jul", "jun", "jur", "jy", "jæg", "jærnb", "jød", "Kbh", "kbh", "kem", "kgl", "kirk", "kl", "kld", "knsp", "kog", "koll", "komm", "komp", "konj", "konkr", "kons", "Kr", "kr", "kurv", "kvt", "køkkenspr", "l", "landbr", "landmaaling", "lat", "lb", "lic", "lign", "litt", "Ll", "log", "Loll", "loll", "lrs", "lør", "m", "maj", "maks", "mal", "man", "mar", "mat", "mdl", "mdr", "med", "medl", "meng", "merc", "meteorol", "meton", "metr", "mf", "mfl", "mht", "mia", "min", "mineral", "mio", "ml", "mlat", "mm", "mnt", "mods", "modsætn", "modt", "mr", "mrk", "mur", "mv", "mvh", "mytol", "møl", "mønt", "n", "naturv", "ndf", "Ndr", "nedsæt", "nht", "no", "nom", "nov", "nr", "nt", "num", "nyda", "nydann", "nylat", "naal", "obj", "obl", "oblik", "obs", "odont", "oecon", "oeng", "ofl", "ogs", "oht", "okt", "oldfr", "oldfris", "oldn", "olgn", "omg", "omkr", "omtr", "ons", "opr", "ordspr", "org", "osax", "osv", "ovenst", "overf", "overs", "ovf", "p", "pag", "part", "pass", "pct", "perf", "pga", "ph", "pharm", "phil", "pk", "pkt", "pl", "plur", "poet", "polit", "port", "poss", "post", "pott", "pr", "pron", "propr", "prov", "præd", "præp", "præs", "præt", "psych", "pt", "pæd", "paavirkn", "reb", "ref", "refl", "regn", "relat", "relig", "resp", "retor", "rid", "rigsspr", "run", "russ", "s", "sa", "sanskr", "scient", "sdjy", "sdr", "sek", "sen", "sep", "sept", "shetl", "sj", "sjæll", "skibsbygn", "sko", "skol", "skr", "skriftspr", "skræd", "Skt", "slagt", "slutn", "smed", "sml", "smsat", "smst", "snedk", "soldat", "sp", "spec", "sport", "spot", "spr", "sprogv", "spøg", "ssg", "ssgr", "st", "stk", "str", "stud", "subj", "subst", "superl", "sv", "sætn", "søn", "talem", "talespr", "tandl", "td", "tdl", "teat", "techn", "telef", "telegr", "teol", "th", "theol", "tir", "tirs", "tlf", "told", "tor", "tors", "trans", "tsk", "ty", "tyrk", "tøm", "u", "ubesl", "ubest", "udd", "udenl", "udg", "udtr", "uegl", "ugtl", "ult", "underbet", "undt", "univ", "upers", "ur", "urnord", "v", "var", "vbs", "vedk", "vedl", "vedr", "vejl", "verb", "vet", "vha", "vol", "vs", "vsa", "vulg", "væv", "zool", "æ", "æda", "ænht", "ænyd", "æstet", "ø", "å", "årg", "årh"
  };

  // Month names like "januar" that should not be considered a sentence
  // boundary in string like "13. januar".
  private static final String[] MONTH_NAMES = { "januar", "februar", "marts", "april", "maj",
      "juni", "juli", "august", "september", "oktober", "november", "december" };

  public DanishSentenceTokenizer() {
    super(ABBREV_LIST);
    super.monthNames = MONTH_NAMES;
  }
 
}
