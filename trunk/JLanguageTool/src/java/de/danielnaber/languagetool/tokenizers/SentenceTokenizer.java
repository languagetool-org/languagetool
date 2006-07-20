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
package de.danielnaber.languagetool.tokenizers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Tokenizes text into sentences by looking for typical end-of-sentence markers,
 * but considering exceptions (e.g. abbreviations).
 * 
 * @author Daniel Naber
 */
public class SentenceTokenizer implements Tokenizer {

  // end of sentence marker:
  private final static String EOS = "\0";
  // private final static String EOS = "#"; // for testing only
  private final static String P = "[\\.!?]"; // PUNCTUATION
  private final static String AP = "(?:'|«|\"||\\)|\\]|\\})?"; // AFTER PUNCTUATION
  private final static String PAP = P + AP;

  // Check out the private methods for comments and examples about these
  // regular expressions:

  private static final Pattern paragraph = Pattern.compile("(\\n\\s*\\n)");
  private static final Pattern punctWhitespace = Pattern.compile("(" + PAP + "\\s)");
  // \p{Lu} = uppercase, mit Beachtung von Unicode (\p{Upper} ist nur US-ASCII!):
  private static final Pattern punctUpperLower = Pattern.compile("(" + PAP
      + ")([\\p{Lu}][^\\p{Lu}.])");
  private static final Pattern letterPunct = Pattern.compile("(\\s[\\wüöäÜÖÄß]" + P + ")");
  private static final Pattern abbrev1 = Pattern.compile("([^-\\wüöäÜÖÄß][\\wüöäÜÖÄß]" + PAP + "\\s)" + EOS);
  private static final Pattern abbrev2 = Pattern.compile("([^-\\wüöäÜÖÄß][\\wüöäÜÖÄß]" + P + ")" + EOS);
  private static final Pattern abbrev3 = Pattern.compile("(\\s[\\wüöäÜÖÄß]\\.\\s+)" + EOS);
  private static final Pattern abbrev4 = Pattern.compile("(\\.\\.\\. )" + EOS + "([\\p{Ll}])");
  private static final Pattern abbrev5 = Pattern.compile("(['\"]" + P + "['\"]\\s+)" + EOS);
  private static final Pattern abbrev6 = Pattern.compile("([\"']\\s*)" + EOS + "(\\s*[\\p{Ll}])");
  private static final Pattern abbrev7 = Pattern.compile("(\\s" + PAP + "\\s)" + EOS);
  // z.b. 3.10. (im Datum):
  private static final Pattern abbrev8 = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\s+)" + EOS);
  private static final Pattern repair1 = Pattern.compile("('[\\wüöäÜÖÄß]" + P + ")(\\s)");
  private static final Pattern repair2 = Pattern.compile("(\\sno\\.)(\\s+)(?!\\d)");
  private static final Pattern repair3 = Pattern.compile("([ap]\\.m\\.\\s+)([\\p{Lu}])");

  // some German and English abbreviations:
  private static final String[] ABBREV_LIST = {
      // English:
      "Mr", "Mrs", "No", "pp", "St", "no", 
      "Sr", "Bros", "etc", "vs", "esp", "Fig", "fig", "Jan", "Feb", "Mar", "Apr", "Jun", "Jul",
      "Aug", "Sep", "Sept", "Oct", "Okt", "Nov", "Dec", "Ph.D", "PhD",
      "al",  // in "et al."
      "cf",
      // German:
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
      "vs", "wg", "Hd", "Ztr", "zus", "Zus", "zzt", "zz", "Zz", "Zt",
      //Polish:
      "np", "p.n.e", "m.in", "itd", "itp", "pt","cdn", "czyt", "dyr","hab",
      "inż","jw", "lek","n.e","nb","rys", "tj", "tzw", "zob" 
  };

  // einige deutsche Monate, vor denen eine Zahl erscheinen kann,
  // ohne dass eine Satzgrenze erkannt wird (z.B. "am 13. Dezember" -> keine Satzgrenze)
  private static final String[] germanMonthList = { "Januar", "Februar", "März", "April", "Mai",
      "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" };

  private static Set<String> abbreviations = new HashSet<String>();
  private StringTokenizer stringTokenizer = null;

  /**
   * Create a sentence tokenizer.
   */
  public SentenceTokenizer() {
    for (int i = 0; i < ABBREV_LIST.length; i++) {
      abbreviations.add(ABBREV_LIST[i]);
    }
  }

  public List<String> tokenize(String s) {
    s = firstSentenceSplitting(s);
    s = removeFalseEndOfSentence(s);
    s = splitUnsplitStuff(s);
    stringTokenizer = new StringTokenizer(s, EOS);
    List<String> l = new ArrayList<String>();
    while (stringTokenizer.hasMoreTokens()) {
      String sentence = stringTokenizer.nextToken();
      l.add(sentence);
    }
    return l;
  }

  /**
   * Add a special break character at all places with typical sentence delimiters.
   */
  private String firstSentenceSplitting(String s) {
    // Double new-line means a new sentence:
    s = paragraph.matcher(s).replaceAll("$1" + EOS);
    // Punctuation followed by whitespace means a new sentence:
    s = punctWhitespace.matcher(s).replaceAll("$1" + EOS);
    // New (compared to the perl module): Punctuation followed by uppercase followed
    // by non-uppercase character (except dot) means a new sentence:
    s = punctUpperLower.matcher(s).replaceAll("$1" + EOS + "$2");
    // Break also when single letter comes before punctuation:
    s = letterPunct.matcher(s).replaceAll("$1" + EOS);
    return s;
  }

  /**
   * Repair some positions that don't require a split, i.e. remove the special break character at
   * those positions.
   */
  private String removeFalseEndOfSentence(String s) {
    // Don't split at e.g. "U. S. A.":
    s = abbrev1.matcher(s).replaceAll("$1");
    // Don't split at e.g. "U.S.A.":
    s = abbrev2.matcher(s).replaceAll("$1");
    // Don't split after a white-space followed by a single letter followed
    // by a dot followed by another whitespace.
    // e.g. " p. "
    s = abbrev3.matcher(s).replaceAll("$1");
    // Don't split at "bla bla... yada yada" (TODO: use \.\.\.\s+ instead?)
    s = abbrev4.matcher(s).replaceAll("$1$2");
    // Don't split [.?!] when the're quoted:
    s = abbrev5.matcher(s).replaceAll("$1");

    // Don't split at abbreviations:
    for (String abbrev : abbreviations) {
      Pattern pattern = Pattern.compile("(\\b" + abbrev + PAP + "\\s)" + EOS);
      s = pattern.matcher(s).replaceAll("$1");
    }
    // Don't break after quote unless there's a capital letter:
    // e.g.: "That's right!" he said.
    s = abbrev6.matcher(s).replaceAll("$1$2");

    // fixme? not sure where this should occur, leaving it commented out:
    // don't break: text . . some more text.
    // text=~s/(\s\.\s)$EOS(\s*)/$1$2/sg;

    // e.g. "Das ist . so." -> assume one sentence
    s = abbrev7.matcher(s).replaceAll("$1");

    // e.g. "Das ist . so." -> assume one sentence
    s = abbrev8.matcher(s).replaceAll("$1");

    // extension by dnaber --commented out, doesn't help:
    // text = re.compile("(:\s+)%s(\s*[%s])" % (self.EOS, string.lowercase),
    // re.DOTALL).sub("\\1\\2", text)

    // "13. Dezember" etc. -> keine Satzgrenze:
    for (int i = 0; i < germanMonthList.length; i++) {
      s = s.replaceAll("(\\d+\\.) " + EOS + "(" + germanMonthList[i] + ")", "$1 $2");
    }

    // z.B. "Das hier ist ein(!) Satz."
    s = s.replaceAll("\\(([!?]+)\\) " + EOS, "($1) ");
    return s;
  }

  /**
   * Treat some more special cases that make up a sentence boundary. Insert the special break
   * character at these positions.
   */
  private String splitUnsplitStuff(String s) {
    // e.g. "x5. bla..." -- not sure, leaving commented out:
    // text = re.compile("(\D\d+)(%s)(\s+)" % self.P, re.DOTALL).sub("\\1\\2%s\\3" % self.EOS, text)
    // Not sure about this one, leaving out four now:
    // text = re.compile("(%s\s)(\s*\()" % self.PAP, re.DOTALL).sub("\\1%s\\2" % self.EOS, text)
    // Split e.g.: He won't. #Really.
    s = repair1.matcher(s).replaceAll("$1" + EOS + "$2");
    // Split e.g.: He won't say no. Not really.
    s = repair2.matcher(s).replaceAll("$1" + EOS + "$2");
    // Split at "a.m." or "p.m." followed by a capital letter.
    s = repair3.matcher(s).replaceAll("$1" + EOS + "$2");
    return s;
  }

}
