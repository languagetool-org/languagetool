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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;

import org.languagetool.tagging.pt.PortugueseTagger;
import org.languagetool.tokenizers.WordTokenizer;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own token.
 *
 * @author Tiago F. Santos
 * @since 3.6
 */
public class PortugueseWordTokenizer extends WordTokenizer {

  private final PortugueseTagger tagger;
  
  private static final String SPLIT_CHARS = "\u0020\u002d" 
        + "\u00A0\u115f\u1160\u1680"
        + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
        + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
        + "\u2010\u2011\u2012\u2013\u2014\u2015"
        + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
        + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
        + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
        + "\u002A\u002B×∗·÷:=≠≂≃≄≅≆≇≈≉≤≥≪≫∧∨∩∪∈∉∊∋∌∍"
        + ",.;()[]{}<>!?:/\\\"'«»„”“‘`’…¿¡\t\n\r";

  // Section copied from UkranianWordTokenizer.java for handling exceptions
  
  private static final char DECIMAL_COMMA_SUBST = '\uE001'; // some unused character to hide comma in decimal number temporary for tokenizer run
  private static final char NON_BREAKING_SPACE_SUBST = '\uE002';
  private static final char NON_BREAKING_DOT_SUBST = '\uE003'; // some unused character to hide dot in date temporary for tokenizer run
  private static final char NON_BREAKING_COLON_SUBST = '\uE004';
  private static final String HYPHEN_SUBST = "\u0001\u0001PT_HYPHEN\u0001\u0001";
  
  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA_PATTERN = Pattern.compile("([\\d]),([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String DECIMAL_COMMA_REPL = "$1" + DECIMAL_COMMA_SUBST + "$2";

  // space between digits
  private static final Pattern DECIMAL_SPACE_PATTERN = Pattern.compile("(?<=^|[\\s(])\\d{1,3}( [\\d]{3})+(?=[\\s(]|$)", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);


  // dots in numbers
  private static final Pattern DOTTED_NUMBERS_PATTERN = Pattern.compile("([\\d])\\.([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String DOTTED_NUMBERS_REPL = "$1" + NON_BREAKING_DOT_SUBST + "$2";
  
  // colon in numbers
  private static final Pattern COLON_NUMBERS_PATTERN = Pattern.compile("([\\d]):([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String COLON_NUMBERS_REPL = "$1" + NON_BREAKING_COLON_SUBST + "$2";

  // dates
  private static final Pattern DATE_PATTERN = Pattern.compile("([\\d]{2})\\.([\\d]{2})\\.([\\d]{4})|([\\d]{4})\\.([\\d]{2})\\.([\\d]{2})|([\\d]{4})-([\\d]{2})-([\\d]{2})", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String DATE_PATTERN_REPL = "$1" + NON_BREAKING_DOT_SUBST + "$2" + NON_BREAKING_DOT_SUBST + "$3";
  // END of Section copied from UkranianWordTokenizer.java for handling exceptions

  // dots in ordinals
  private static final Pattern DOTTED_ORDINALS_PATTERN = Pattern.compile("([\\d])\\.([aoªº][sˢ]?)", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String DOTTED_ORDINALS_REPL = "$1" + NON_BREAKING_DOT_SUBST + "$2";
  
  // hyphens inside words
  private static final Pattern HYPHEN_PATTERN = Pattern.compile("([\\p{L}])-([\\p{L}\\d])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final String HYPHEN_REPL = "$1" + HYPHEN_SUBST + "$2";
  private static final Pattern NEARBY_HYPHENS_PATTERN = Pattern.compile("([\\p{L}])-([\\p{L}])-([\\p{L}])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final String NEARBY_HYPHENS_REPL = "$1" + HYPHEN_SUBST + "$2" + HYPHEN_SUBST + "$3";
  public PortugueseWordTokenizer() { 
    tagger = new PortugueseTagger();
  }
  
  @Override
  public List<String> tokenize(String text) {

    if( text.contains(",") ) {
    	text = DECIMAL_COMMA_PATTERN.matcher(text).replaceAll(DECIMAL_COMMA_REPL);
    }

    // if period is not the last character in the sentence
    int dotIndex = text.indexOf('.');
    boolean dotInsideSentence = dotIndex >= 0 && dotIndex < text.length()-1;
    if( dotInsideSentence ){
      text = DATE_PATTERN.matcher(text).replaceAll(DATE_PATTERN_REPL);
      text = DOTTED_NUMBERS_PATTERN.matcher(text).replaceAll(DOTTED_NUMBERS_REPL);
      text = DOTTED_ORDINALS_PATTERN.matcher(text).replaceAll(DOTTED_ORDINALS_REPL);
    }

    // 2 000 000
    Matcher spacedDecimalMatcher = DECIMAL_SPACE_PATTERN.matcher(text);
    if( spacedDecimalMatcher.find() ) {
    	StringBuffer sb = new StringBuffer();
    	do {
    		String splitNumber = spacedDecimalMatcher.group(0);
    		String splitNumberAdjusted = splitNumber.replace(' ', NON_BREAKING_SPACE_SUBST);
    		splitNumberAdjusted = splitNumberAdjusted.replace('\u00A0', NON_BREAKING_SPACE_SUBST);
    		spacedDecimalMatcher.appendReplacement(sb, splitNumberAdjusted);
    	} while( spacedDecimalMatcher.find() );
    	spacedDecimalMatcher.appendTail(sb);
    	text = sb.toString();
    }

    // 12:25
    if( text.contains(":") ) {
    	text = COLON_NUMBERS_PATTERN.matcher(text).replaceAll(COLON_NUMBERS_REPL);
    }
    if (text.contains("-")) {
      text = NEARBY_HYPHENS_PATTERN.matcher(text).replaceAll(NEARBY_HYPHENS_REPL);
      text = HYPHEN_PATTERN.matcher(text).replaceAll(HYPHEN_REPL);  
    }
    
    List<String> tokenList = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, SPLIT_CHARS, true);
    while (st.hasMoreElements()) {
      String token = st.nextToken(); 
      token = token.replace(DECIMAL_COMMA_SUBST, ',');
      token = token.replace(NON_BREAKING_COLON_SUBST, ':');
      token = token.replace(NON_BREAKING_SPACE_SUBST, ' ');
      // outside of if as we also replace back sentence-ending abbreviations
      token = token.replace(NON_BREAKING_DOT_SUBST, '.');
      token = token.replaceAll(HYPHEN_SUBST, "-");
      tokenList.addAll( wordsToAdd(token));
    }

    return joinEMailsAndUrls(tokenList);
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
          if (tagger.tag(Arrays.asList(s.replace("’", "'"))).get(0).isTagged()) {
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
