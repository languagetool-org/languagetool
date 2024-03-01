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

  private static final String wordCharacters = "§©@€£\\$_\\p{L}\\d\\-\u0300-\u036F\u00A8\u2070-\u209F°%‰‱&\uFFFD\u00AD\u00AC";
  private static final Pattern tokenizerPattern = Pattern.compile("[" + wordCharacters + "]+|[^" + wordCharacters + "]");
  private static final Pattern SOFT_HYPHEN = Pattern.compile("\u00AD");
  private static final Pattern CURLY_QUOTE = Pattern.compile("’");
  private static final Pattern PATTERN_1 = Pattern.compile("xxFR_APOS_TYPEWxx");
  private static final Pattern PATTERN_2 = Pattern.compile("xxFR_APOS_TYPOGxx");
  private static final Pattern PATTERN_3 = Pattern.compile("xxFR_HYPHENxx");
  private static final Pattern PATTERN_4 = Pattern.compile("xxFR_DECIMALPOINTxx");
  private static final Pattern PATTERN_5 = Pattern.compile("xxFR_DECIMALCOMMAxx");
  private static final Pattern PATTERN_6 = Pattern.compile("xxFR_SPACExx");

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
  private static final Pattern SPACE0 = Pattern.compile("xxFR_SPACE0xx");

  private static final List<String> doNotSplit = Arrays.asList("mers-cov", "mcgraw-hill", "sars-cov-2", "sars-cov",
      "ph-metre", "ph-metres", "anti-ivg", "anti-uv", "anti-vih", "al-qaïda", "c'est-à-dire", "add-on", "add-ons",
      "rendez-vous", "garde-à-vous", "chez-eux", "chez-moi", "chez-nous", "chez-soi", "chez-toi", "chez-vous", "m'as-tu-vu");
  
  //the string used to tokenize characters
  private final String frTokenizingChars = super.getTokenizingCharacters() + "-"; // hyphen

  final static int maxPatterns = 7;
  final static Pattern[] patterns = new Pattern[maxPatterns];
  static {
    // words not to be split
    patterns[0] = Pattern.compile("^(c['’]te?|m['’]as-tu-vu|c['’]est-à-dire|add-on|add-ons|rendez-vous|garde-à-vous|chez-eux|chez-moi|chez-nous|chez-soi|chez-toi|chez-vous)$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    patterns[1] = Pattern.compile(
      "^([cç]['’]|j['’]|n['’]|m['’]|t['’]|s['’]|l['’]|d['’]|qu['’]|jusqu['’]|lorsqu['’]|puisqu['’]|quoiqu['’])([^\\-]*)(-ce|-elle|-t-elle|-elles|-t-elles|-en|-il|-t-il|-ils|-t-ils|-je|-la|-le|-les|-leur|-lui|-moi|-nous|-on|-t-on|-toi|-tu|-vous|-vs|-y)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    // Apostrophe at the beginning of a word. ce, je, ne, me, te, se, le, la, de, que, si // NO: presqu['’] |quelqu['’]
    // It creates 2 tokens: <token>l'</token><token>homme</token>
    patterns[2] = Pattern.compile(
      "^([cç]['’]|j['’]|n['’]|m['’]|t['’]|s['’]|l['’]|d['’]|qu['’]|jusqu['’]|lorsqu['’]|puisqu['’]|quoiqu['’])([^'’\\-].*)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    patterns[3] = Pattern.compile(
      "^([^\\-\\d]+)(-ce|-t-elle|-t-elles|-elle|-elles|-en|-il|-t-il|-ils|-t-ils|-je|-la|-le|-les|-leur|-lui|-moi|-nous|-on|-t-on|-toi|-tu|-vous|-vs|-y)(-ce|-elle|-t-elle|-elles|-t-elles|-en|-il|-t-il|-ils|-t-ils|-je|-la|-le|-les|-leur|-lui|-moi|-nous|-on|-t-on|-toi|-tu|-vous|-vs|-y)$",
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

  public FrenchWordTokenizer() {
  }

  /**
   * @param text Text to tokenize
   * @return List of tokens. Note: a special string
   *         xxFR_APOSxx is used to replace apostrophes, and
   *         xxFR_HYPHENxx to replace hyphens.
   */
  @Override
  public List<String> tokenize(final String text) {
    final List<String> l = new ArrayList<>();
    // replace hyphen, non-break hyphen -> hyphen-minus
    String auxText = text.replace('\u2010', '\u002d');
    auxText = auxText.replace('\u2011', '\u002d');
    Matcher matcher = TYPEWRITER_APOSTROPHE.matcher(auxText);
    auxText = matcher.replaceAll("$1xxFR_APOS_TYPEWxx$2");
    matcher = TYPOGRAPHIC_APOSTROPHE.matcher(auxText);
    auxText = matcher.replaceAll("$1xxFR_APOS_TYPOGxx$2");
    matcher = NEARBY_HYPHENS.matcher(auxText);
    auxText = matcher.replaceAll("$1xxFR_HYPHENxx$2xxFR_HYPHENxx$3");
    matcher = HYPHENS.matcher(auxText);
    auxText = matcher.replaceAll("$1xxFR_HYPHENxx$2");
    matcher = DECIMAL_POINT.matcher(auxText);
    auxText = matcher.replaceAll("$1xxFR_DECIMALPOINTxx$2");
    matcher = DECIMAL_COMMA.matcher(auxText);
    auxText = matcher.replaceAll("$1xxFR_DECIMALCOMMAxx$2");
    matcher = SPACE_DIGITS2.matcher(auxText);
    auxText = matcher.replaceAll("$1xxFR_SPACExx$2xxFR_SPACExx$3");
    matcher = SPACE_DIGITS0.matcher(auxText);
    auxText = matcher.replaceAll("$1xxFR_SPACE0xx");
    matcher = SPACE_DIGITS.matcher(auxText);
    auxText = matcher.replaceAll("$1xxFR_SPACExx$2");
    matcher = SPACE0.matcher(auxText);
    auxText = matcher.replaceAll(" ");

    Matcher tokenizerMatcher = tokenizerPattern.matcher(auxText);
    while (tokenizerMatcher.find()) {
      String s = tokenizerMatcher.group();
      if (l.size() > 0 && s.length() == 1 && s.codePointAt(0)>=0xFE00 && s.codePointAt(0)<=0xFE0F) {
        l.set(l.size() - 1, l.get(l.size() - 1) + s);
        continue;
      }
      s = PATTERN_1.matcher(s).replaceAll("'");
      s = PATTERN_2.matcher(s).replaceAll("’");
      s = PATTERN_3.matcher(s).replaceAll("-");
      s = PATTERN_4.matcher(s).replaceAll(".");
      s = PATTERN_5.matcher(s).replaceAll(",");
      s = PATTERN_6.matcher(s).replaceAll(" ");
      boolean matchFound = false;
      while (s.length() > 1 && s.startsWith("-")) {
        l.add("-");
        s = s.substring(1);
      }
      int hyphensAtEnd = 0;
      while (s.length() > 1 && s.endsWith("-")) {
        s = s.substring(0, s.length() - 1);
        hyphensAtEnd++;
      }
      int j = 0;
      while (j < maxPatterns && !matchFound) {
        matcher = patterns[j].matcher(s);
        matchFound = matcher.find();
        j++;
      }
      if (matchFound) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
          String groupStr = matcher.group(i);
          l.addAll(wordsToAdd(groupStr));
        }
      } else {
        l.addAll(wordsToAdd(s));
      }
      while (hyphensAtEnd > 0) {
        l.add("-");
        hyphensAtEnd--;
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
          String normalized = SOFT_HYPHEN.matcher(s).replaceAll("");
          normalized = CURLY_QUOTE.matcher(normalized).replaceAll("'");
          if (FrenchTagger.INSTANCE.tag(Arrays.asList(normalized)).get(0).isTagged()) {
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
