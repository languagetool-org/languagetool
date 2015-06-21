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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
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
        + ",.;()[]{}<>!?:/|\\\"«»„”“`´‘‛′…¿¡\t\n\r\uE100\uE101\uE102\uE110";

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
  //TODO: л.с., ч.л./ч. л., ст. л., р. х.
  private static final Pattern ABBR_DOT_PATTERN = Pattern.compile("(тис)\\.([ \u00A0]+[а-яіїєґ])");
  private static final Pattern ABBR_DOT_PATTERN1 = Pattern.compile("([^а-яіїєґА-ЯІЇЄҐ'-]лат)\\.([ \u00A0]+[a-zA-Z])");
  private static final Pattern ABBR_DOT_PATTERN2 = Pattern.compile("([Аа]кад|[Пп]роф|[Дд]оц|[Аа]сист|вул|о|р|ім)\\.([\\s\u00A0]+[А-ЯІЇЄҐ])");
  // село, місто, річка (якщо з цифрою: секунди, метри, роки) - з роками складно
  private static final Pattern ABBR_DOT_PATTERN5 = Pattern.compile("((?:[0-9]|кв\\.?|куб\\.?)[\\s\u00A0]+[см])\\.");
  private static final Pattern ABBR_DOT_PATTERN3 = Pattern.compile("(с)\\.(-г)\\.");
  private static final Pattern ABBR_DOT_PATTERN4 = Pattern.compile("([^а-яіїєґ'-][векнпрстцч]{1,2})\\.([екмнпрстч]{1,2})\\.");
//  private static final Pattern ABBR_DOT_PATTERN5 = Pattern.compile("(ст)\\.([ \u00A0]*[0-9]+)");
  private static final Pattern ABBR_DOT_PATTERN6 = Pattern.compile("([^а-яіїєґА-ЯІЇЄҐ'-]((та|й) ін|амер|англ|бл(изьк)?|вірм|грец(ьк)|див|дол|досл|доц|е|ел|жін|заст|зв|ім|івр|ісп|італ|к|кв|[1-9]-кімн|кімн|кл|коп|м|н|напр|обл|п|пен|перекл|пл|пор|поч|прибл|пров|просп|р|[Рр]ед|[Рр]еж|рр|рт|руб|с|[Сс]в|соц|співавт|ст|стол|стор|табл|тел|укр|філол|фр|франц|ч|чайн|чол|ц|шт))\\.");
  private static final Pattern ABBR_DOT_PATTERN7 = Pattern.compile("([ій][ \u00A0]+т)\\.([ \u00A0]*(д|п|ін))\\.");
  private static final char ABBR_DOT_SUBST = '\uE007';
  private static final String BREAKING_PLACEHOLDER = "\uE110";
  // ellipsis
  private static final String ELLIPSIS = "...";
  private static final String ELLIPSIS_SUBST = "\uE100";
  private static final String ELLIPSIS2 = "!..";
  private static final String ELLIPSIS2_SUBST = "\uE101";
  private static final String ELLIPSIS3 = "?..";
  private static final String ELLIPSIS3_SUBST = "\uE102";
  // url
  private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", Pattern.CASE_INSENSITIVE);
  private static final int URL_START_REPLACE_CHAR = 0xE300;


  public UkrainianWordTokenizer() {
  }

  @Override
  public List<String> tokenize(String text) {
    HashMap<String, String> urls = new HashMap<String, String>();

    text = cleanup(text);
    
    if( text.contains(",") ) {
      text = DECIMAL_COMMA_PATTERN.matcher(text).replaceAll("$1" + DECIMAL_COMMA_SUBST + "$2");
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
    
//    int dotIndex = text.indexOf(".");
//    if( dotIndex >= 0 && dotIndex < text.length()-1 ) {

    if( text.contains(ELLIPSIS) ) {
      text = text.replace(ELLIPSIS, ELLIPSIS_SUBST);
    }
    if( text.contains(ELLIPSIS2) ) {
      text = text.replace(ELLIPSIS2, ELLIPSIS2_SUBST);
    }
    if( text.contains(ELLIPSIS3) ) {
      text = text.replace(ELLIPSIS3, ELLIPSIS3_SUBST);
    }

    if( text.contains(".") ) {
    

      text = DATE_PATTERN.matcher(text).replaceAll("$1" + DATE_DOT_SUBST + "$2" + DATE_DOT_SUBST + "$3");
      text = DOTTED_NUMBERS_PATTERN.matcher(text).replaceAll("$1" + NUMBER_DOT_SUBST + "$2");

//      text = ABBR_DOT_PATTERN3.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + "$2" + ABBR_DOT_SUBST);
      text = ABBR_DOT_PATTERN4.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + BREAKING_PLACEHOLDER + "$2" + ABBR_DOT_SUBST);
      text = ABBR_DOT_PATTERN.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + "$2");
      text = ABBR_DOT_PATTERN1.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + "$2");
      text = ABBR_DOT_PATTERN2.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + "$2");
      text = ABBR_DOT_PATTERN5.matcher(text).replaceAll("$1" + BREAKING_PLACEHOLDER + ABBR_DOT_SUBST);
      text = ABBR_DOT_PATTERN3.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + "$2" + ABBR_DOT_SUBST);
//      text = ABBR_DOT_PATTERN5.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + "$2");
      text = ABBR_DOT_PATTERN6.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST);
      text = ABBR_DOT_PATTERN7.matcher(text).replaceAll("$1" + ABBR_DOT_SUBST + "$2" + ABBR_DOT_SUBST);
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
    
    List<String> tokenList = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, SPLIT_CHARS, true);

    while (st.hasMoreElements()) {
      String token = st.nextToken();
      
      if( token.equals(BREAKING_PLACEHOLDER) )
        continue;
      
      token = token.replace(DECIMAL_COMMA_SUBST, ',');
      
      //TODO: merge all dots to speed things up ???
      token = token.replace(DATE_DOT_SUBST, '.');
      token = token.replace(NUMBER_DOT_SUBST, '.');
      token = token.replace(ABBR_DOT_SUBST, '.');
      
      token = token.replace(COLON_DOT_SUBST, ':');
      token = token.replace(LEFT_BRACE_SUBST, '(');
      token = token.replace(RIGHT_BRACE_SUBST, ')');
      token = token.replaceAll(ELLIPSIS_SUBST, ELLIPSIS);
      token = token.replaceAll(ELLIPSIS2_SUBST, ELLIPSIS2);
      token = token.replaceAll(ELLIPSIS3_SUBST, ELLIPSIS3);
//      token = token.replaceAll(""+BREAKING_DOT_SUBST, "");

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
