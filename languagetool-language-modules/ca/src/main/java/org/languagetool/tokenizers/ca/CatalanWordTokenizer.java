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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

import static org.languagetool.tools.StringTools.CHARS_NOT_FOR_SPELLING;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own token.
 * Special treatment for hyphens and apostrophes in Catalan.
 *
 * @author Jaume Ortolà 
 */
public class CatalanWordTokenizer extends WordTokenizer {

  private static final String wordCharacters = "§©@€£\\$_\\p{L}\\d·\\-\u0300-\u036F\u00A8\u2070-\u209F°%‰‱&\uFFFD\u00AD\u00AC";
  private static final Pattern tokenizerPattern = Pattern.compile("[" + wordCharacters + "]+|[^" + wordCharacters + "]");
  //all possible forms of "pronoms febles" after a verb.
  private static final String PF = "(['’]en|['’]hi|['’]ho|['’]l|['’]ls|['’]m|['’]n|['’]ns|['’]s|['’]t|-el|-els|-em|-en|-ens|-hi|-ho|-l|-la|-les|-li|-lo|-los|-m|-me|-n|-ne|-nos|-s|-se|-t|-te|-us|-vos)";
  private static final Pattern PATTERN_1 = Pattern.compile("xxCA_APOS_RECTExx", Pattern.LITERAL);
  private static final Pattern PATTERN_2 = Pattern.compile("xxCA_APOS_RODOxx", Pattern.LITERAL);
  private static final Pattern PATTERN_3 = Pattern.compile("xxCA_HYPHENxx", Pattern.LITERAL);
  private static final Pattern PATTERN_4 = Pattern.compile("xxCA_DECIMALPOINTxx", Pattern.LITERAL);
  private static final Pattern PATTERN_5 = Pattern.compile("xxCA_DECIMALCOMMAxx", Pattern.LITERAL);
  private static final Pattern PATTERN_6 = Pattern.compile("xxCA_SPACExx", Pattern.LITERAL);
  private static final Pattern PATTERN_7 = Pattern.compile("xxELA_GEMINADAxx", Pattern.LITERAL);
  private static final Pattern PATTERN_8 = Pattern.compile("xxELA_GEMINADA_UPPERCASExx", Pattern.LITERAL);
  private static final Pattern SOFT_HYPHEN = Pattern.compile("\u00AD");
  private static final Pattern CURLY_SINGLE_QUOTE = Pattern.compile("’", Pattern.LITERAL);
  private static final Pattern LL = Pattern.compile("l-l", Pattern.LITERAL);

  private static final int maxPatterns = 11;
  private final Pattern[] patterns = new Pattern[maxPatterns];

  //Patterns to avoid splitting words in certain special cases
  // allows correcting typographical errors in "ela geminada"
  private static final Pattern ELA_GEMINADA = Pattern.compile("([aeiouàéèíóòúïüAEIOUÀÈÉÍÒÓÚÏÜ])l[.\u2022\u22C5\u2219\uF0D7]l([aeiouàéèíóòúïü])",Pattern.UNICODE_CASE);
  private static final Pattern ELA_GEMINADA_UPPERCASE = Pattern.compile("([AEIOUÀÈÉÍÒÓÚÏÜ])L[.\u2022\u22C5\u2219\uF0D7]L([AEIOUÀÈÉÍÒÓÚÏÜ])",Pattern.UNICODE_CASE);
  // apostrophe 
  private static final Pattern APOSTROF_RECTE = Pattern.compile("([\\p{L}])'([\\p{L}\"‘“«])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_RODO = Pattern.compile("([\\p{L}])’([\\p{L}\"‘“«])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // apostrophe before number 1. Ex.: d'1 km, és l'1 de gener, és d'1.4 kg
  private static final Pattern APOSTROF_RECTE_1 = Pattern.compile("([dlDL])'(\\d[\\d\\s\\.,]?)",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern APOSTROF_RODO_1 = Pattern.compile("([dlDL])’(\\d[\\d\\s\\.,]?)",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // decimal point between digits
  private static final Pattern DECIMAL_POINT= Pattern.compile("([\\d])\\.([\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA= Pattern.compile("([\\d]),([\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // space between digits
  // the first is an exception to the two next patterns
  private static final Pattern SPACE_DIGITS0= Pattern.compile("([\\d]{4}) ",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern SPACE_DIGITS= Pattern.compile("([\\d]) ([\\d][\\d][\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern SPACE_DIGITS2= Pattern.compile("([\\d]) ([\\d][\\d][\\d]) ([\\d][\\d][\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern SPACE0 = Pattern.compile("xxCA_SPACE0xx");
  // Sàsser-l'Alguer
  private static final Pattern HYPHEN_L= Pattern.compile("([\\p{L}]+)(-)([Ll]['’])([\\p{L}]+)",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  public CatalanWordTokenizer() {

    // Apostrophe at the beginning of a word. Ex.: l'home, s'estima, n'omple, hivern, etc.
    // It creates 2 tokens: <token>l'</token><token>home</token>
    patterns[0] = Pattern.compile("^([lnmtsd]['’])([^'’\\-]*)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

    // Exceptions to (Match verb+1 pronom feble)
    // It creates 1 token: <token>qui-sap-lo</token>
    patterns[1] = Pattern.compile("^(qui-sap-lo|qui-sap-la|qui-sap-los|qui-sap-les)|(Castella)(-)(la)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

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
    patterns[7] = Pattern.compile("^(.+[^wo])"+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

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
   *         Note: a special string xxCA_APOSxx is used to replace apostrophes,
   *         and xxCA_HYPHENxx to replace hyphens.
   */
  @Override
  public List<String> tokenize(final String text) {
    final List<String> l = new ArrayList<>();
    // replace hyphen, non-break hyphen -> hyphen-minus
    String auxText = text.replace('\u2010', '\u002d');
    auxText = auxText.replace('\u2011', '\u002d');
    Matcher matcher=ELA_GEMINADA.matcher(auxText);
    auxText = matcher.replaceAll("$1xxELA_GEMINADAxx$2");
    matcher=ELA_GEMINADA_UPPERCASE.matcher(auxText);
    auxText = matcher.replaceAll("$1xxELA_GEMINADA_UPPERCASExx$2");
    matcher=APOSTROF_RECTE.matcher(auxText);
    auxText = matcher.replaceAll("$1xxCA_APOS_RECTExx$2");
    matcher=APOSTROF_RECTE_1.matcher(auxText);
    auxText = matcher.replaceAll("$1xxCA_APOS_RECTExx$2");
    matcher=APOSTROF_RODO.matcher(auxText);
    auxText = matcher.replaceAll("$1xxCA_APOS_RODOxx$2");
    matcher=APOSTROF_RODO_1.matcher(auxText);
    auxText = matcher.replaceAll("$1xxCA_APOS_RODOxx$2");
    matcher=DECIMAL_POINT.matcher(auxText);
    auxText = matcher.replaceAll("$1xxCA_DECIMALPOINTxx$2");
    matcher=DECIMAL_COMMA.matcher(auxText);
    auxText = matcher.replaceAll("$1xxCA_DECIMALCOMMAxx$2");
    matcher=SPACE_DIGITS0.matcher(auxText);
    auxText = matcher.replaceAll("$1xxCA_SPACE0xx");
    matcher=SPACE_DIGITS2.matcher(auxText);
    auxText = matcher.replaceAll("$1xxCA_SPACExx$2xxCA_SPACExx$3");
    matcher=SPACE_DIGITS.matcher(auxText);
    auxText = matcher.replaceAll("$1xxCA_SPACExx$2");
    matcher=SPACE0.matcher(auxText);
    auxText = SPACE0.matcher(auxText).replaceAll(" ");

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
      s = PATTERN_7.matcher(s).replaceAll("l.l");
      s = PATTERN_8.matcher(s).replaceAll("L.L");
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
          if (groupStr!=null) {
            l.addAll(wordsToAdd(groupStr));  
          }
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

  /* Splits a word containing hyphen(-) if it doesn't exist in the dictionary. 
   * Split apostrophe in the last char */
  private List<String> wordsToAdd(String s) {
    final List<String> l = new ArrayList<>();
    synchronized (this) { //speller is not thread-safe
      if (!s.isEmpty()) {
        if (!s.contains("-") && !s.endsWith("'") && !s.endsWith("’")) {
          l.add(s);
        } else {
          // words containing hyphen (-) are looked up in the dictionary
          if (CatalanTagger.INSTANCE_CAT.tag(Arrays.asList(CURLY_SINGLE_QUOTE.matcher(SOFT_HYPHEN.matcher(s).replaceAll("")).replaceAll("'"))).get(0).isTagged()) {
            l.add(s);
          }
          // some camel-case words containing hyphen (is there any better fix?)
          else if (s.equalsIgnoreCase("mers-cov") || s.equalsIgnoreCase("mcgraw-hill") 
              || s.equalsIgnoreCase("sars-cov-2") || s.equalsIgnoreCase("sars-cov") 
              || s.equalsIgnoreCase("ph-metre") || s.equalsIgnoreCase("ph-metres")) {
            l.add(s);
          }
          // words with "ela geminada" with typo: col-legi (col·legi)
          else if (CatalanTagger.INSTANCE_CAT.tag(Arrays.asList(LL.matcher(SOFT_HYPHEN.matcher(s).replaceAll("")).replaceAll("l·l"))).get(0).isTagged()) {
            l.add(s);
          // apostrophe in the last char
          } else if ((s.endsWith("'") || s.endsWith("’")) && s.length() > 1) {
            l.addAll(wordsToAdd(s.substring(0, s.length() - 1)));
            l.add(s.substring(s.length() - 1));
          } else {
            Matcher matcher = HYPHEN_L.matcher(s);
            if (matcher.matches()) {
              for (int i = 1; i <= matcher.groupCount(); i++) {
                String groupStr = matcher.group(i);
                l.addAll(wordsToAdd(groupStr));
              }
            } else {
              // if not found, the word is split
              final StringTokenizer st2 = new StringTokenizer(s, "-", true);
              while (st2.hasMoreElements()) {
                l.add(st2.nextToken());
              }
            }
          }
        }
      }
      return l;
    }
  }

}
