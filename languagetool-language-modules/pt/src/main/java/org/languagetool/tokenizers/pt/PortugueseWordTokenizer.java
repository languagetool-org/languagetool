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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.Tokenizer;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own token.
 *
 * @author Tiago F. Santos based on Spanish tokenizer
 * @since 3.6
 */
public class PortugueseWordTokenizer implements Tokenizer {
  private static final String SPLIT_CHARS = "\u0020\u002d" 
        + "\u00A0\u115f\u1160\u1680"
        + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
        + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
        + "\u2010\u2011\u2012\u2013\u2014\u2015"
        + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
        + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
        + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
        + ",.;()[]{}<>!?:/\\\"'«»„”“‘`’…¿¡\t\n\r";


  // Section copied from UkranianWordTokenizer.java for handling exceptions
  
  private static final char DECIMAL_COMMA_SUBST = '\uE001'; // some unused character to hide comma in decimal number temporary for tokenizer run
  private static final char NON_BREAKING_SPACE_SUBST = '\uE002';
  private static final char NON_BREAKING_DOT_SUBST = '\uE003'; // some unused character to hide dot in date temporary for tokenizer run
  private static final char NON_BREAKING_COLON_SUBST = '\uE004';
  
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

  // url
  private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", Pattern.CASE_INSENSITIVE);
  private static final int URL_START_REPLACE_CHAR = 0xE300;

  public PortugueseWordTokenizer() {
  }

  @Override
  public List<String> tokenize(String text) {
    HashMap<String, String> urls = new HashMap<>();

    text = cleanup(text);
    
    if( text.contains(",") ) {
    	text = DECIMAL_COMMA_PATTERN.matcher(text).replaceAll(DECIMAL_COMMA_REPL);
    }

    // check for urls
    if( text.contains("tp") ) { // https?|ftp
      Matcher matcher = URL_PATTERN.matcher(text);
      int urlReplaceChar = URL_START_REPLACE_CHAR;
      
      while( matcher.find() ) {
        String urlGroup = matcher.group();
        String replaceChar = String.valueOf((char)urlReplaceChar);
        urls.put(replaceChar, urlGroup);
        text = matcher.replaceAll(replaceChar);
        urlReplaceChar++;
      }
    }

    // if period is not the last character in the sentence
    int dotIndex = text.indexOf(".");
    boolean dotInsideSentence = dotIndex >= 0 && dotIndex < text.length()-1;
    
    if( dotInsideSentence ){
  
      text = DATE_PATTERN.matcher(text).replaceAll(DATE_PATTERN_REPL);
      text = DOTTED_NUMBERS_PATTERN.matcher(text).replaceAll(DOTTED_NUMBERS_REPL);
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

    List<String> tokenList = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, SPLIT_CHARS, true);

    while (st.hasMoreElements()) {
      String token = st.nextToken();
      
      token = token.replace(DECIMAL_COMMA_SUBST, ',');
      
      token = token.replace(NON_BREAKING_COLON_SUBST, ':');
      token = token.replace(NON_BREAKING_SPACE_SUBST, ' ');
      
      // token = token.replace(LEFT_BRACE_SUBST, '(');
      // token = token.replace(RIGHT_BRACE_SUBST, ')');

      // outside of if as we also replace back sentence-ending abbreviations
      token = token.replace(NON_BREAKING_DOT_SUBST, '.');

      if( ! urls.isEmpty() ) {
        for(Entry<String, String> entry : urls.entrySet()) {
          token = token.replace(entry.getKey(), entry.getValue());
        }
      }
      
      tokenList.add( token );
    }

    return tokenList;
  }

  private static String cleanup(String text) {
    text = text.replace('’', '\'').replace('ʼ', '\'').replace('‘', '\'');
    text = text.replace('\u2011', '-'); // we handle \u2013 in tagger so we can base our rule on it

    return text;
  }

}
