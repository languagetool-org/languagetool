/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.fr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.tagging.fr.FrenchTagger;
import org.languagetool.tokenizers.WordTokenizer;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace get its own
 * token. Special treatment for hyphens and apostrophes in French.
 *
 * @author Jaume Ortolà
 */
public class FrenchWordTokenizer extends WordTokenizer {

  private static final int maxPatterns = 7;
  private final Pattern[] patterns = new Pattern[maxPatterns];

  // Patterns to avoid splitting words in certain special cases

  // apostrophe
  private static final Pattern TYPEWRITER_APOSTROPHE = Pattern.compile("([\\p{L}])'([\\p{L}1\"‘“«])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern TYPOGRAPHIC_APOSTROPHE = Pattern.compile("([\\p{L}])’([\\p{L}1\"‘“«])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  // nearby hyphens. 
  private static final Pattern NEARBY_HYPHENS = Pattern.compile("([\\p{L}])-([\\p{L}])-([\\p{L}])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  // hyphens.
  private static final Pattern HYPHENS = Pattern.compile("([\\p{L}])-([\\p{L}\\d])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  // decimal point between digits
  private static final Pattern DECIMAL_POINT = Pattern.compile("([\\d])\\.([\\d])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA = Pattern.compile("([\\d]),([\\d])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  // space between digits
  // the first is an exception to the two next patterns
  private static final Pattern SPACE_DIGITS0 = Pattern.compile("([\\d]{4}) ",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern SPACE_DIGITS = Pattern.compile("([\\d]) ([\\d][\\d][\\d])\\b",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern SPACE_DIGITS2 = Pattern.compile("([\\d]) ([\\d][\\d][\\d]) ([\\d][\\d][\\d])\\b",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  
  private static final List<String> doNotSplit = Arrays.asList("mers-cov", "mcgraw-hill", "sars-cov-2", "sars-cov",
      "ph-metre", "ph-metres", "anti-ivg", "anti-uv", "anti-vih", "al-qaïda", "c'est-à-dire", "add-on", "add-ons",
      "rendez-vous", "garde-à-vous", "chez-eux", "chez-moi", "chez-nous", "chez-soi", "chez-toi", "chez-vous", "m'as-tu-vu");

  public FrenchWordTokenizer() {

    // words not to be split
    patterns[0] = Pattern.compile("^(m'as-tu-vu|c'est-à-dire|add-on|add-ons|rendez-vous|garde-à-vous|chez-eux|chez-moi|chez-nous|chez-soi|chez-toi|chez-vous)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    patterns[1] = Pattern.compile(
        "^(c['’]|j['’]|n['’]|m['’]|t['’]|s['’]|l['’]|d['’]|qu['’]|jusqu['’]|lorsqu['’]|puisqu['’]|quoiqu['’])([^\\-]*)(-ce|-elle|-t-elle|-elles|-t-elles|-en|-il|-t-il|-ils|-t-ils|-je|-la|-le|-les|-leur|-lui|-moi|-nous|-on|-t-on|-toi|-tu|-vous|-vs|-y)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    // Apostrophe at the beginning of a word. ce, je, ne, me, te, se, le, la, de, que, si // NO: presqu['’] |quelqu['’]
    // It creates 2 tokens: <token>l'</token><token>homme</token>
    patterns[2] = Pattern.compile( 
        "^(c['’]|j['’]|n['’]|m['’]|t['’]|s['’]|l['’]|d['’]|qu['’]|jusqu['’]|lorsqu['’]|puisqu['’]|quoiqu['’])([^'’\\-].*)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    patterns[3] = Pattern.compile(
        "^([^\\-]*)(-ce|-t-elle|-t-elles|-elle|-elles|-en|-il|-t-il|-ils|-t-ils|-je|-la|-le|-les|-leur|-lui|-moi|-nous|-on|-t-on|-toi|-tu|-vous|-vs|-y)(-ce|-elle|-t-elle|-elles|-t-elles|-en|-il|-t-il|-ils|-t-ils|-je|-la|-le|-les|-leur|-lui|-moi|-nous|-on|-t-on|-toi|-tu|-vous|-vs|-y)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    patterns[4] = Pattern.compile(
        "^([^\\-]*)(-t|-m)(['’]en|['’]y)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    patterns[5] = Pattern.compile(
        "^(.*)(-t-elle|-t-elles|-t-il|-t-ils|-t-on)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    patterns[6] = Pattern.compile(
        "^(.*)(-ce|-elle|-t-elle|-elles|-t-elles|-en|-il|-t-il|-ils|-t-ils|-je|-la|-le|-les|-leur|-lui|-moi|-nous|-on|-t-on|-toi|-tu|-vous|-vs|-y)$",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // contractions: au, du ??
    /*patterns[1] = Pattern.compile("^(a|d)(u)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    patterns[2] = Pattern.compile("^(d)(es)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    patterns[3] = Pattern.compile("^(a)(ux)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);*/

  }

  /**
   * @param text Text to tokenize
   * @return List of tokens. Note: a special string
   *         \u0001\u0001FR_APOS\u0001\u0001 is used to replace apostrophes, and
   *         \u0001\u0001FR_HYPHEN\u0001\u0001 to replace hyphens.
   */
  @Override
  public List<String> tokenize(final String text) {
    final List<String> l = new ArrayList<>();
    String auxText = text;

    Matcher matcher = TYPEWRITER_APOSTROPHE.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001FR_APOS_TYPEW\u0001\u0001$2");
    matcher = TYPOGRAPHIC_APOSTROPHE.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001FR_APOS_TYPOG\u0001\u0001$2");
    matcher = NEARBY_HYPHENS.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001FR_HYPHEN\u0001\u0001$2\u0001\u0001FR_HYPHEN\u0001\u0001$3");
    matcher = HYPHENS.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001FR_HYPHEN\u0001\u0001$2");
    matcher = DECIMAL_POINT.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001FR_DECIMALPOINT\u0001\u0001$2");
    matcher = DECIMAL_COMMA.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001FR_DECIMALCOMMA\u0001\u0001$2");
    matcher = SPACE_DIGITS2.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001FR_SPACE\u0001\u0001$2\u0001\u0001FR_SPACE\u0001\u0001$3");
    matcher = SPACE_DIGITS0.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001FR_SPACE0\u0001\u0001");
    matcher = SPACE_DIGITS.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001FR_SPACE\u0001\u0001$2");
    auxText = auxText.replaceAll("\\u0001\\u0001FR_SPACE0\\u0001\\u0001", " ");

    final StringTokenizer st = new StringTokenizer(auxText,
        "\u0020\u00A0\u115f\u1160\u1680" + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
            + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f" + "\u2012\u2013\u2014\u2015\u2022"
            + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
            + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
            + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
            + "|,.;()[]{}=*#∗+×÷<>!?:~/\\\"'«»„”“‘’`´…¿¡\t\n\r-·™®\u203d"
            + "\u2265\u2192\u21FE\u21C9\u21D2\u21E8\u21DB\u2713" // arrows
            + "\u00b9\u00b2\u00b3\u2070\u2071\u2074\u2075\u2076\u2077\u2078\u2079" // superscripts
            + "\u2032", // prime...
        true);
    String s;
    String groupStr;

    while (st.hasMoreElements()) {
      s = st.nextToken().replace("\u0001\u0001FR_APOS_TYPEW\u0001\u0001", "'")
          .replace("\u0001\u0001FR_APOS_TYPOG\u0001\u0001", "’").replace("\u0001\u0001FR_HYPHEN\u0001\u0001", "-")
          .replace("\u0001\u0001FR_DECIMALPOINT\u0001\u0001", ".")
          .replace("\u0001\u0001FR_DECIMALCOMMA\u0001\u0001", ",").replace("\u0001\u0001FR_SPACE\u0001\u0001", " ");
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
    synchronized (this) { // speller is not thread-safe
      if (!s.isEmpty()) {
        if (!s.contains("-")) {
          l.add(s);
        } else {
          // words containing hyphen (-) are looked up in the dictionary
          if (FrenchTagger.INSTANCE.tag(Arrays.asList(s.replaceAll("\u00AD","").replace("’", "'"))).get(0).isTagged()) {
            // In the current POS tag, most apostrophes are curly: to be fixed
            l.add(s);
          }
          // some camel-case words containing hyphen (is there any better fix?)
          else if (doNotSplit.contains(s.toLowerCase())) {
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
