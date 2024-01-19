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
package org.languagetool.tokenizers.pt;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import org.languagetool.tagging.pt.PortugueseTagger;
import org.languagetool.tokenizers.WordTokenizer;

import static java.util.regex.Pattern.*;
import static java.util.regex.Pattern.compile;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own token.
 *
 * @author Tiago F. Santos
 * @since 3.6
 */
public class PortugueseWordTokenizer extends WordTokenizer {

  private final PortugueseTagger tagger;

  // Section copied from UkranianWordTokenizer.java for handling exceptions
  private static final char DECIMAL_COMMA_SUBST = '\uE001'; // some unused character to hide comma in decimal number temporary for tokenizer run
  private static final char NON_BREAKING_SPACE_SUBST = '\uE002';
  private static final char NON_BREAKING_DOT_SUBST = '\uE003'; // some unused character to hide dot in date temporary for tokenizer run
  private static final char NON_BREAKING_COLON_SUBST = '\uE004';
  private static final Pattern CURLY_QUOTE = compile("’");
  private static final Pattern HYPHEN_SUBST = compile("\u0001\u0001PT_HYPHEN\u0001\u0001");

  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA_PATTERN = compile("([\\d]),([\\d])", CASE_INSENSITIVE| UNICODE_CASE);
  private static final String DECIMAL_COMMA_REPL = "$1" + DECIMAL_COMMA_SUBST + "$2";

  // space between digits
  private static final Pattern DECIMAL_SPACE_PATTERN = compile("(?<=^|[\\s(])\\d{1,3}( \\d{3})+(?:[" + DECIMAL_COMMA_SUBST + NON_BREAKING_DOT_SUBST + "]\\d+)?(?=\\D|$)", CASE_INSENSITIVE|UNICODE_CASE);

  // dots in numbers
  private static final Pattern DOTTED_NUMBERS_PATTERN = compile("([\\d])\\.([\\d])", CASE_INSENSITIVE| UNICODE_CASE);
  private static final String DOTTED_NUMBERS_REPL = "$1" + NON_BREAKING_DOT_SUBST + "$2";

  // colon in numbers
  private static final Pattern COLON_NUMBERS_PATTERN = compile("([\\d]):([\\d])", CASE_INSENSITIVE| UNICODE_CASE);
  private static final String COLON_NUMBERS_REPL = "$1" + NON_BREAKING_COLON_SUBST + "$2";

  // dates
  private static final Pattern DATE_PATTERN = compile("([\\d]{2})\\.([\\d]{2})\\.([\\d]{4})|([\\d]{4})\\.([\\d]{2})\\.([\\d]{2})|([\\d]{4})-([\\d]{2})-([\\d]{2})", CASE_INSENSITIVE| UNICODE_CASE);
  private static final String DATE_PATTERN_REPL = "$1" + NON_BREAKING_DOT_SUBST + "$2" + NON_BREAKING_DOT_SUBST + "$3";
  // END of Section copied from UkranianWordTokenizer.java for handling exceptions

  // dots in ordinals
  private static final Pattern DOTTED_ORDINALS_PATTERN = compile("([\\d])\\.([aoªºᵃᵒ][sˢ]?)", CASE_INSENSITIVE| UNICODE_CASE);
  private static final String DOTTED_ORDINALS_REPL = "$1" + NON_BREAKING_DOT_SUBST + "$2";

  // hyphens inside words
  private static final Pattern HYPHEN_PATTERN = compile("([\\p{L}])-([\\p{L}\\d])", CASE_INSENSITIVE | UNICODE_CASE);
  private static final String HYPHEN_REPL = "$1" + HYPHEN_SUBST + "$2";
  private static final Pattern NEARBY_HYPHENS_PATTERN = compile("([\\p{L}])-([\\p{L}])-([\\p{L}])", CASE_INSENSITIVE | UNICODE_CASE);
  private static final String NEARBY_HYPHENS_REPL = "$1" + HYPHEN_SUBST + "$2" + HYPHEN_SUBST + "$3";
  private final String PT_TOKENISING_CHARS = getTokenizingCharacters() + "⌈⌋″©%";

  public PortugueseWordTokenizer() {
    tagger = new PortugueseTagger();
  }

  @Override
  public List<String> tokenize(final String text) {
    String tokenisedText = text;  // it's really bad practice to reassign method params imo...

    if (tokenisedText.contains(",")) {
      tokenisedText = DECIMAL_COMMA_PATTERN.matcher(tokenisedText).replaceAll(DECIMAL_COMMA_REPL);
    }

    // if period is not the last character in the sentence
    int dotIndex = tokenisedText.indexOf('.');
    boolean dotInsideSentence = dotIndex >= 0 && dotIndex < tokenisedText.length() - 1;
    if (dotInsideSentence) {
      tokenisedText = DATE_PATTERN.matcher(tokenisedText).replaceAll(DATE_PATTERN_REPL);
      tokenisedText = DOTTED_NUMBERS_PATTERN.matcher(tokenisedText).replaceAll(DOTTED_NUMBERS_REPL);
      tokenisedText = DOTTED_ORDINALS_PATTERN.matcher(tokenisedText).replaceAll(DOTTED_ORDINALS_REPL);
    }

    // 2 000 000
    Matcher spacedDecimalMatcher = DECIMAL_SPACE_PATTERN.matcher(tokenisedText);
    if (spacedDecimalMatcher.find()) {
      StringBuffer sb = new StringBuffer();
      do {
        String splitNumber = spacedDecimalMatcher.group(0);
        String splitNumberAdjusted = splitNumber.replace(' ', NON_BREAKING_SPACE_SUBST);
        splitNumberAdjusted = splitNumberAdjusted.replace('\u00A0', NON_BREAKING_SPACE_SUBST);
        spacedDecimalMatcher.appendReplacement(sb, splitNumberAdjusted);
      } while (spacedDecimalMatcher.find());
      spacedDecimalMatcher.appendTail(sb);
      tokenisedText = sb.toString();
    }

    // 12:25
    if (tokenisedText.contains(":")) {
      tokenisedText = COLON_NUMBERS_PATTERN.matcher(tokenisedText).replaceAll(COLON_NUMBERS_REPL);
    }
    if (tokenisedText.contains("-")) {
      tokenisedText = NEARBY_HYPHENS_PATTERN.matcher(tokenisedText).replaceAll(NEARBY_HYPHENS_REPL);
      tokenisedText = HYPHEN_PATTERN.matcher(tokenisedText).replaceAll(HYPHEN_REPL);
    }

    List<String> tokenList = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(tokenisedText, PT_TOKENISING_CHARS, true);
    while (st.hasMoreElements()) {
      String token = st.nextToken();
      // make sure we join the % sign with the previous token, if it ends in a digit
      if (Objects.equals(token, "%") && !tokenList.isEmpty()) {
        int lastIndex = tokenList.size() - 1;
        String lastToken = tokenList.get(lastIndex);
        if (lastToken.matches(".*\\d$")) {
          tokenList.set(lastIndex, lastToken + "%");
          continue;
        }
      }
      token = token.replace(DECIMAL_COMMA_SUBST, ',');
      token = token.replace(NON_BREAKING_COLON_SUBST, ':');
      token = token.replace(NON_BREAKING_SPACE_SUBST, ' ');
      // outside of if as we also replace back sentence-ending abbreviations
      token = token.replace(NON_BREAKING_DOT_SUBST, '.');
      token = HYPHEN_SUBST.matcher(token).replaceAll("-");
      tokenList.addAll(wordsToAdd(token));
    }

    return joinEMailsAndUrls(tokenList);
  }

  /* Splits a word containing hyphen(-) if it doesn't exist in the dictionary. */
  private List<String> wordsToAdd(String s) {
    final List<String> l = new ArrayList<>();
    synchronized (this) { // speller is not thread-safe
      if (!s.isEmpty()) {
        if (isCurrencyExpression(s)) {
          l.addAll(splitCurrencyExpression(s));
        } else if (!s.contains("-")) {
          l.add(s);
        } else {
          // words containing hyphen (-) are looked up in the dictionary
          if (tagger.tag(Arrays.asList(CURLY_QUOTE.matcher(s).replaceAll("'"))).get(0).isTagged()) {
            // In the current POS tag, most apostrophes are curly: to be fixed
            l.add(s);
          }
          // some camel-case words containing hyphen (is there any better fix?)
          else if (s.equalsIgnoreCase("mers-cov") || s.equalsIgnoreCase("mcgraw-hill")
            || s.equalsIgnoreCase("sars-cov-2") || s.equalsIgnoreCase("sars-cov") || s.equalsIgnoreCase("ph-metre")
            || s.equalsIgnoreCase("ph-metres") || s.equalsIgnoreCase("anti-ivg") || s.equalsIgnoreCase("anti-uv")
            || s.equalsIgnoreCase("anti-vih") || s.equalsIgnoreCase("al-qaïda")) {
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
