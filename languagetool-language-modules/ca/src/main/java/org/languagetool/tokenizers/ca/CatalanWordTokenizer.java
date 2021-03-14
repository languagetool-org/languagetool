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
package org.languagetool.tokenizers.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.JLanguageTool;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tokenizers.WordTokenizer;


/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own token.
 * Special treatment for hyphens and apostrophes in Catalan.
 *
 * @author Jaume Ortolà 
 */
public class CatalanWordTokenizer extends WordTokenizer {

  //all possible forms of "pronoms febles" after a verb.
  private static final String PF = "(['’]en|['’]hi|['’]ho|['’]l|['’]ls|['’]m|['’]n|['’]ns|['’]s|['’]t|-el|-els|-em|-en|-ens|-hi|-ho|-l|-la|-les|-li|-lo|-los|-m|-me|-n|-ne|-nos|-s|-se|-t|-te|-us|-vos)";

  private static final int maxPatterns = 11;
  private final Pattern[] patterns = new Pattern[maxPatterns];
  
  private static final String DICT_FILENAME = "/ca/ca-ES-valencia.dict";
  protected MorfologikSpeller speller;

  //Patterns to avoid splitting words in certain special cases
  // allows correcting typographical errors in "ela geminada"
  private static final Pattern ELA_GEMINADA = Pattern.compile("([aeiouàéèíóòúïüAEIOUÀÈÉÍÒÓÚÏÜ])l[.\u2022]l([aeiouàéèíóòúïü])",Pattern.UNICODE_CASE);
  private static final Pattern ELA_GEMINADA_UPPERCASE = Pattern.compile("([AEIOUÀÈÉÍÒÓÚÏÜ])L[.\u2022]L([AEIOUÀÈÉÍÒÓÚÏÜ])",Pattern.UNICODE_CASE);
  // apostrophe 
  private static final Pattern APOSTROF_RECTE = Pattern.compile("([\\p{L}])'([\\p{L}\"‘“«])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_RODO = Pattern.compile("([\\p{L}])’([\\p{L}\"‘“«])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // apostrophe before number 1. Ex.: d'1 km, és l'1 de gener, és d'1.4 kg
  private static final Pattern APOSTROF_RECTE_1 = Pattern.compile("([dlDL])'(\\d[\\d\\s\\.,]?)",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_RODO_1 = Pattern.compile("([dlDL])’(\\d[\\d\\s\\.,]?)",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // nearby hyphens. Ex.: vint-i-quatre 
  private static final Pattern NEARBY_HYPHENS= Pattern.compile("([\\p{L}])-([\\p{L}])-([\\p{L}])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // hyphens. Ex.: vint-i-quatre 
  private static final Pattern HYPHENS= Pattern.compile("([\\p{L}])-([\\p{L}\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // decimal point between digits
  private static final Pattern DECIMAL_POINT= Pattern.compile("([\\d])\\.([\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA= Pattern.compile("([\\d]),([\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // space between digits
  // the first is an exception to the two next patterns
  private static final Pattern SPACE_DIGITS0= Pattern.compile("([\\d]{4}) ",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern SPACE_DIGITS= Pattern.compile("([\\d]) ([\\d][\\d][\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern SPACE_DIGITS2= Pattern.compile("([\\d]) ([\\d][\\d][\\d]) ([\\d][\\d][\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  public CatalanWordTokenizer() {

    // lazy init
    if (speller == null) {
      if (JLanguageTool.getDataBroker().resourceExists(DICT_FILENAME)) {
        try {
          speller = new MorfologikSpeller(DICT_FILENAME);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    // Apostrophe at the beginning of a word. Ex.: l'home, s'estima, n'omple, hivern, etc.
    // It creates 2 tokens: <token>l'</token><token>home</token>
    patterns[0] = Pattern.compile("^([lnmtsd]['’])([^'’\\-]*)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

    // Exceptions to (Match verb+1 pronom feble)
    // It creates 1 token: <token>qui-sap-lo</token>
    patterns[1] = Pattern.compile("^(qui-sap-lo|qui-sap-la|qui-sap-los|qui-sap-les)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

    // Match verb+3 pronoms febles (rare but possible!). Ex: Emporta-te'ls-hi.
    // It creates 4 tokens: <token>Emporta</token><token>-te</token><token>'ls</token><token>-hi</token>
    patterns[2] = Pattern.compile("^([lnmtsd]['’])(.{2,})"+PF+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    patterns[3] = Pattern.compile("^(.{2,})"+PF+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

    // Match verb+2 pronoms febles. Ex: Emporta-te'ls. 
    // It creates 3 tokens: <token>Emporta</token><token>-te</token><token>'ls</token>
    patterns[4] = Pattern.compile("^([lnmtsd]['’])(.{2,})"+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    patterns[5] = Pattern.compile("^(.{2,})"+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

    // match verb+1 pronom feble. Ex: Emporta't, vés-hi, porta'm.
    // It creates 2 tokens: <token>Emporta</token><token>'t</token>
    // ^(.+[^cbfhjkovwyzCBFHJKOVWYZ])
    patterns[6] = Pattern.compile("^([lnmtsd]['’])(.{2,})"+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    patterns[7] = Pattern.compile("^(.{2,})"+PF+"$",Pattern.UNICODE_CASE);

    // d'emportar
    patterns[8] = Pattern.compile("^([lnmtsd]['’])(.*)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

    //contractions: al, als, pel, pels, del, dels, cal (!), cals (!) 
    patterns[9] = Pattern.compile("^(a|de|pe)(ls?)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

    //contraction: can
    patterns[10] = Pattern.compile("^(ca)(n)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);


  }

  /**
   * @param text Text to tokenize
   * @return List of tokens.
   *         Note: a special string \u0001\u0001CA_APOS\u0001\u0001 is used to replace apostrophes,
   *         and \u0001\u0001CA_HYPHEN\u0001\u0001 to replace hyphens.
   */
  @Override
  public List<String> tokenize(final String text) {
    final List<String> l = new ArrayList<>();
    String auxText=text;

    Matcher matcher=ELA_GEMINADA.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001ELA_GEMINADA\u0001\u0001$2");
    matcher=ELA_GEMINADA_UPPERCASE.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001ELA_GEMINADA_UPPERCASE\u0001\u0001$2");
    matcher=APOSTROF_RECTE.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_APOS_RECTE\u0001\u0001$2");
    matcher=APOSTROF_RECTE_1.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_APOS_RECTE\u0001\u0001$2");
    matcher=APOSTROF_RODO.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_APOS_RODO\u0001\u0001$2");
    matcher=APOSTROF_RODO_1.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_APOS_RODO\u0001\u0001$2");
    matcher=NEARBY_HYPHENS.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_HYPHEN\u0001\u0001$2\u0001\u0001CA_HYPHEN\u0001\u0001$3");
    matcher=HYPHENS.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_HYPHEN\u0001\u0001$2");
    matcher=DECIMAL_POINT.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_DECIMALPOINT\u0001\u0001$2");
    matcher=DECIMAL_COMMA.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_DECIMALCOMMA\u0001\u0001$2");
    matcher=SPACE_DIGITS0.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_SPACE0\u0001\u0001");
    matcher=SPACE_DIGITS2.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_SPACE\u0001\u0001$2\u0001\u0001CA_SPACE\u0001\u0001$3");
    matcher=SPACE_DIGITS.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001CA_SPACE\u0001\u0001$2");
    auxText = auxText.replaceAll("\\u0001\\u0001CA_SPACE0\\u0001\\u0001", " ");
    
    final StringTokenizer st = new StringTokenizer(auxText,
            "\u0020\u00A0\u115f\u1160\u1680"
                    + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
                    + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
                    + "\u2012\u2013\u2014\u2015\u2022"
                    + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
                    + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
                    + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
                    + "|,.;()[]{}=*#∗+×÷<>!?:~/\\\"'«»„”“‘’`´…¿¡\t\n\r-", true); // Not included: middle dot (·)
    String s;
    String groupStr;

    while (st.hasMoreElements()) {
      s = st.nextToken()
              .replace("\u0001\u0001CA_APOS_RECTE\u0001\u0001", "'")
              .replace("\u0001\u0001CA_APOS_RODO\u0001\u0001", "’")
              .replace("\u0001\u0001CA_HYPHEN\u0001\u0001", "-")
              .replace("\u0001\u0001CA_DECIMALPOINT\u0001\u0001", ".")
              .replace("\u0001\u0001CA_DECIMALCOMMA\u0001\u0001", ",")
              .replace("\u0001\u0001CA_SPACE\u0001\u0001", " ")
              .replace("\u0001\u0001ELA_GEMINADA\u0001\u0001", "l.l")
              .replace("\u0001\u0001ELA_GEMINADA_UPPERCASE\u0001\u0001", "L.L");
      boolean matchFound = false;
      int j = 0;
      while (j < maxPatterns && !matchFound) {
        matcher = patterns[j].matcher(s);
        matchFound = matcher.find();
        j++;
      }
      if (matchFound) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
          groupStr = matcher.group(i);
          l.addAll(wordsToAdd(groupStr));
        }
      } else {
        l.addAll(wordsToAdd(s));
      }
    }
    return joinEMailsAndUrls(l);
  }

  /* Splits a word containing hyphen(-) if it doesn't exist in the dictionary. */
  private List<String> wordsToAdd(String s) {
    final List<String> l = new ArrayList<>();
    synchronized (this) { //speller is not thread-safe
      if (!s.isEmpty()) {
        if (!s.contains("-")) {
          l.add(s);
        } else {
          // words containing hyphen (-) are looked up in the dictionary
          if (!speller.isMisspelled(s.replace("’", "'"))) {
            l.add(s);
          }
          // some camel-case words containing hyphen (is there any better fix?)
          else if (s.equalsIgnoreCase("mers-cov") || s.equalsIgnoreCase("mcgraw-hill") 
              || s.equalsIgnoreCase("sars-cov-2") || s.equalsIgnoreCase("sars-cov") 
              || s.equalsIgnoreCase("ph-metre") || s.equalsIgnoreCase("ph-metres")) {
            l.add(s);
          }
          // words with "ela geminada" with typo: col-legi (col·legi)
          else if (!speller.isMisspelled(s.replace("l-l", "l·l"))) {
            l.add(s);
          } else {
            // if not found, the word is split
            final StringTokenizer st2 = new StringTokenizer(s, "-", true);
            while (st2.hasMoreElements()) {
              l.add(st2.nextToken());
            }
          }
        }
      }
      return l;
    }
  }
}
