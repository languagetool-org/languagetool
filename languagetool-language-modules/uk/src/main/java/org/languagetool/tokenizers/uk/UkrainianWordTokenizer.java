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
package org.languagetool.tokenizers.uk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.Tokenizer;

/**
 * Tokenizes a sentence into words.
 * Punctuation and whitespace gets its own token.
 * Specific to Ukrainian: apostrophes (0x27 and U+2019) not in the list as they are part of the word
 * 
 * @author Andriy Rysin
 */
public class UkrainianWordTokenizer implements Tokenizer {
  private static final String SPLIT_CHARS = "\u0020\u00A0\u115f\u1160\u1680" 
        + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007" 
        + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
        + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
        + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
        + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb" 
        + ",.;()[]{}<>!?:/|\\\"«»„”“`´‘‛′…¿¡\t\n\r\uE100";

  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA_PATTERN = Pattern.compile("([\\d]),([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final char DECIMAL_COMMA_SUBST = '\uE001'; // some unused character to hide comma in decimal number temporary for tokenizer run
  // space between digits
//  private static final Pattern DECIMAL_SPACE_PATTERN = Pattern.compile("([\\d]{1,3})( ([\\d]{3}))+", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
//  private static final char DECIMAL_SPACE_SUBST = '\uE008';
  // dots in numbers
  private static final Pattern DOTTED_NUMBERS_PATTERN = Pattern.compile("([\\d])\\.([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final char NUMBER_DOT_SUBST = '\uE002';
  // colon in numbers
  private static final Pattern COLON_NUMBERS_PATTERN = Pattern.compile("([\\d]):([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final char COLON_DOT_SUBST = '\uE003';
  // dates
  private static final Pattern DATE_PATTERN = Pattern.compile("([\\d]{2})\\.([\\d]{2})\\.([\\d]{4})|([\\d]{4})\\.([\\d]{2})\\.([\\d]{2})|([\\d]{4})-([\\d]{2})-([\\d]{2})", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final char DATE_DOT_SUBST = '\uE004'; // some unused character to hide dot in date temporary for tokenizer run
  // braces in words
  private static final Pattern BRACE_IN_WORD_PATTERN = Pattern.compile("([а-яіїєґ'])\\(([а-яіїєґ']+)\\)", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final char LEFT_BRACE_SUBST = '\uE005';
  private static final char RIGHT_BRACE_SUBST = '\uE006';
  // abbreviation dot
  //TODO: also use abbreviation list to allow next letter to be capital
  private static final Pattern ABBR_DOT_PATTERN = Pattern.compile("([а-яіїєґ])\\. ([а-яіїєґ])");
  private static final Pattern ABBR_DOT_PATTERN2 = Pattern.compile("([Аа]кад|[Пп]роф|[Дд]оц|[Аа]сист|с|м|вул|о|р|ім)\\.\\s([А-ЯІЇЄҐ])");
  private static final char ABBR_DOT_SUBST = '\uE007';
  // ellipsis
  private static final String ELLIPSIS = "...";
  private static final String ELLIPSIS_SUBST = "\uE100";


  public UkrainianWordTokenizer() {
  }

  @Override
  public List<String> tokenize(String text) {
    text = cleanup(text);
    
    if( text.contains(",") ) {
      text = DECIMAL_COMMA_PATTERN.matcher(text).replaceAll("$1" + DECIMAL_COMMA_SUBST + "$2");
    }
    
    if( text.contains(".") ) {
      text = DATE_PATTERN.matcher(text).replaceAll("$1" + DATE_DOT_SUBST + "$2" + DATE_DOT_SUBST + "$3");
      text = DOTTED_NUMBERS_PATTERN.matcher(text).replaceAll("$1" + NUMBER_DOT_SUBST + "$2");
      text = ABBR_DOT_PATTERN.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + " $2");
      text = ABBR_DOT_PATTERN2.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + " $2");
    }

    if( text.contains(":") ) {
      text = COLON_NUMBERS_PATTERN.matcher(text).replaceAll("$1" + COLON_DOT_SUBST + "$2");
    }

    if( text.contains("(") ) {
      text = BRACE_IN_WORD_PATTERN.matcher(text).replaceAll("$1" + LEFT_BRACE_SUBST + "$2" + RIGHT_BRACE_SUBST);
    }

//    if( text.contains(" ") ) {
//      text = DECIMAL_SPACE_PATTERN.matcher(text).replaceAll("$1" + DECIMAL_SPACE_SUBST + "$2");
//    }
    
    if( text.contains(ELLIPSIS) ) {
      text = text.replace(ELLIPSIS, ELLIPSIS_SUBST);
    }
    
    List<String> tokenList = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, SPLIT_CHARS, true);

    while (st.hasMoreElements()) {
      String token = st.nextToken();
      
      token = token.replace(DECIMAL_COMMA_SUBST, ',');
      
      //TODO: merge all dots to speed things up ???
      token = token.replace(DATE_DOT_SUBST, '.');
      token = token.replace(NUMBER_DOT_SUBST, '.');
      token = token.replace(ABBR_DOT_SUBST, '.');
      
      token = token.replace(COLON_DOT_SUBST, ':');
      token = token.replace(LEFT_BRACE_SUBST, '(');
      token = token.replace(RIGHT_BRACE_SUBST, ')');
      token = token.replaceAll(ELLIPSIS_SUBST, ELLIPSIS);

      tokenList.add( token );
    }

    return tokenList;
  }

  private static String cleanup(String text) {
    text = text.replace('’', '\'').replace('ʼ', '\'');

//    if( text.contains("\u0301") || text.contains("\u00AD") ) {
//      text = text.replace("\u0301", "").replace("\u00AD", "");
//    }

//    while( text.contains("\u0301") || text.contains("\u00AD") ) {
//      text = IGNORE_CHARS_PATTERN.matcher(text).replaceAll("$1$3");
//    }

    return text;
  }

}
