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

import org.languagetool.tokenizers.Tokenizer;

/**
 * Tokenizes a sentence into words.
 * Punctuation and whitespace gets its own token.
 * Specific to Ukrainian: apostrophes (0x27 and U+2019) not in the list as they are part of the word
 * 
 * @author Andriy Rysin
 */
public class UkrainianWordTokenizer implements Tokenizer {

  private static final String SPLIT_CHARS =
            "!{2,3}|\\?{2,3}|\\.{3}|[!?][!?.]{1,2}|[\u0020\u00A0\\n\\r\\t"
            + ",.;!?:\"()\\[\\]{}<>/|\\\\«»„”“…=¿¡"
            + "\u2000-\u200F"
            + "\u201A\u2020-\u202F\u2030\u2031\u2033-\u206F"
            + "\u2400-\u27FF"                                                       // Control Pictures
            + String.valueOf(Character.toChars(0x1F400)) + "-" + String.valueOf(Character.toChars(0x1F64F))          // Emojis
            + "\ufeff\uffa0\ufff9\ufffa\ufffb"
            + "\ufe00-\uffff"
            + "\uE110]";

  private static final Pattern SPLIT_CHARS_REGEX = Pattern.compile(SPLIT_CHARS);


  // for handling exceptions

  private static final char DECIMAL_COMMA_SUBST = '\uE001'; // some unused character to hide comma in decimal number temporary for tokenizer run
  private static final char NON_BREAKING_SPACE_SUBST = '\uE002';
  private static final char NON_BREAKING_DOT_SUBST = '\uE003'; // some unused character to hide dot in date temporary for tokenizer run
  private static final char NON_BREAKING_COLON_SUBST = '\uE004';
  private static final char NON_BREAKING_SLASH_SUBST = '\uE007';    // hide slash in с/г

  private static final Pattern WEIRD_APOSTROPH_PATTERN = Pattern.compile("([бвджзклмнпрстфхш])[\"\u201D\u201F]([єїюя])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA_PATTERN = Pattern.compile("([\\d]),([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String DECIMAL_COMMA_REPL = "$1" + DECIMAL_COMMA_SUBST + "$2";

  // space between digits
  private static final Pattern DECIMAL_SPACE_PATTERN = Pattern.compile("(?<=^|[\\s(])\\d{1,3}([ \u00A0\u202F][\\d]{3})+(?=[\\s(]|$)", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);


  // dots in numbers
  private static final Pattern DOTTED_NUMBERS_PATTERN = Pattern.compile("([\\d])\\.([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String DOTTED_NUMBERS_REPL = "$1" + NON_BREAKING_DOT_SUBST + "$2";

  // colon in numbers
  private static final Pattern COLON_NUMBERS_PATTERN = Pattern.compile("([\\d]):([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String COLON_NUMBERS_REPL = "$1" + NON_BREAKING_COLON_SUBST + "$2";

  // dates
  private static final Pattern DATE_PATTERN = Pattern.compile("([\\d]{2})\\.([\\d]{2})\\.([\\d]{4})|([\\d]{4})\\.([\\d]{2})\\.([\\d]{2})|([\\d]{4})-([\\d]{2})-([\\d]{2})", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String DATE_PATTERN_REPL = "$1" + NON_BREAKING_DOT_SUBST + "$2" + NON_BREAKING_DOT_SUBST + "$3";

  // braces in words
  private static final Pattern BRACE_IN_WORD_PATTERN = Pattern.compile("([а-яіїєґ'])\\(([а-яіїєґ']+)\\)", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final char LEFT_BRACE_SUBST = '\uE005';
  private static final char RIGHT_BRACE_SUBST = '\uE006';
  private static final String BREAKING_PLACEHOLDER = "\uE110";

  // abbreviation dot
  private static final Pattern ABBR_DOT_VO_PATTERN1 = Pattern.compile("(в)\\.([\\s\u00A0\u202F]*о)\\.");
  private static final Pattern ABBR_DOT_VO_PATTERN2 = Pattern.compile("(к)\\.([\\s\u00A0\u202F]*с)\\.");
  private static final Pattern ABBR_DOT_VO_PATTERN3 = Pattern.compile("(ч|ст)\\.([\\s\u00A0\u202F]*л)\\.");
//  private static final Pattern ABBR_DOT_VO_PATTERN4 = Pattern.compile("(р)\\.([\\s\u00A0\u202F]*х)\\.");
  private static final Pattern ABBR_DOT_TYS_PATTERN1 = Pattern.compile("([0-9IІ][\\s\u00A0\u202F]+)(тис|арт)\\.");
  private static final Pattern ABBR_DOT_TYS_PATTERN2 = Pattern.compile("(тис|арт)\\.([\\s\u00A0\u202F]+[а-яіїєґ0-9])");
  private static final Pattern ABBR_DOT_LAT_PATTERN = Pattern.compile("([^а-яіїєґА-ЯІЇЄҐ'\u0301-]лат)\\.([\\s\u00A0\u202F]+[a-zA-Z])");
  private static final Pattern ABBR_DOT_PROF_PATTERN = Pattern.compile("([Аа]кад|[Пп]роф|[Дд]оц|[Аа]сист|[Аа]рх|вул|о|р|ім|упоряд)\\.([\\s\u00A0\u202F]+[А-ЯІЇЄҐ])");

  // tokenize initials with dot before last name, e.g. "А.", "Ковальчук"
  private static final Pattern INITIALS_DOT_PATTERN_SP_2 = Pattern.compile("([А-ЯІЇЄҐ])\\.([\\s\u00A0\u202F]?[А-ЯІЇЄҐ])\\.([\\s\u00A0\u202F]?[А-ЯІЇЄҐ][а-яіїєґ']+)");
  private static final Pattern INITIALS_DOT_PATTERN_SP_1 = Pattern.compile("([А-ЯІЇЄҐ])\\.([\\s\u00A0\u202F]?[А-ЯІЇЄҐ][а-яіїєґ']+)");

  // tokenize initials with dot after last name, e.g.  "Ковальчук", "А."
  private static final Pattern INITIALS_DOT_PATTERN_RSP_2 = Pattern.compile("([А-ЯІЇЄҐ][а-яіїєґ']+)([\\s\u00A0\u202F]?[А-ЯІЇЄҐ])\\.([\\s\u00A0\u202F]?[А-ЯІЇЄҐ])\\.");
  private static final Pattern INITIALS_DOT_PATTERN_RSP_1 = Pattern.compile("([А-ЯІЇЄҐ][а-яіїєґ']+)([\\s\u00A0\u202F]?[А-ЯІЇЄҐ])\\.");

  private static final String INITIALS_DOT_REPL_SP_2 = "$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$3";
  private static final String INITIALS_DOT_REPL_SP_1 = "$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2";
  private static final String INITIALS_DOT_REPL_RSP_2 = "$1" + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$3" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER;
  private static final String INITIALS_DOT_REPL_RSP_1 = "$1" + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER;

  // село, місто, річка (якщо з цифрою: секунди, метри, роки) - з роками складно
//  private static final Pattern ABBR_DOT_INVALID_DOT_PATTERN = Pattern.compile("((?:[0-9]|кв\\.|куб\\.)[\\s\u00A0\u202F]+(?:[смкд]|мк)?м)\\.(.)");
  private static final Pattern ABBR_DOT_KUB_SM_PATTERN = Pattern.compile("(кв|куб)\\.([\\s\u00A0\u202F]*(?:[смкд]|мк)?м)");
  private static final Pattern ABBR_DOT_S_G_PATTERN = Pattern.compile("(с)\\.(-г)\\.");
  private static final Pattern ABBR_DOT_PN_ZAH_PATTERN = Pattern.compile("(пн|пд)\\.(-(зах|сх))\\.");
  private static final Pattern ABBR_DOT_2_SMALL_LETTERS_PATTERN = Pattern.compile("([^а-яіїєґ'-][векнпрстцч]{1,2})\\.([екмнпрстч]{1,2})\\.");
  private static final String ABBR_DOT_2_SMALL_LETTERS_REPL = "$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER;
  
  private static final String ONE_DOT_TWO_REPL = "$1" + NON_BREAKING_DOT_SUBST + "$2";

  // скорочення що не можуть бути в кінці речення
  private static final Pattern ABBR_DOT_NON_ENDING_PATTERN = Pattern.compile("(?<![а-яіїєґА-ЯІЇЄҐ'\u0301-])(абз|амер|англ|акад(ем)?|арк|ауд|бл(?:изьк)?|буд|в|вип|вірм|грец(?:ьк)"
      + "|держ|див|дод|дол|досл|доц|доп|екон|ел|жін|зав|заст|зах|зб|зв|зовн|ім|івр|ісп|іст|італ"
      + "|к|каб|каф|канд|кв|[1-9]-кімн|кімн|кл|кн|коеф|мал|моб|н|напр|нац|оп|оф|п|пен|перекл|пл|пол|пов|пор|поч|пп|прибл|пров|пром|просп"
      + "|[Рр]ед|[Рр]еж|розд|рт|с|[Сс]вв?|скор|соц|співавт|ст|стор|сх|табл|[тТ]ел|укр|філол|фр|франц|ч|чайн|част|ц|яп)\\.(?!\\.* *$)");
  private static final Pattern ABBR_DOT_NON_ENDING_PATTERN_2 = Pattern.compile("([^а-яіїєґА-ЯІЇЄҐ'-]м)\\.([\\s\u00A0\u202F]*[А-ЯІЇЄҐ])");
  // скорочення що можуть бути в кінці речення
  private static final Pattern ABBR_DOT_ENDING_PATTERN = Pattern.compile("([^а-яіїєґА-ЯІЇЄҐ'\u0301-]((та|й|і) ін|(та|й|і) под|інш|атм|відс|гр|е|коп|обл|р|рр|руб|ст|стол|стор|чол|шт))\\.");
  private static final Pattern ABBR_DOT_I_T_P_PATTERN = Pattern.compile("([ій][\\s\u00A0\u202F]+т)\\.([\\s\u00A0\u202F]*(д|п|ін))\\.");
  private static final Pattern ABBR_DOT_T_ZV_PATTERN = Pattern.compile("([\\s\u00A0\u202F]+т)\\.([\\s\u00A0\u202F]*(зв))\\.");

  private static final Pattern ABBR_AT_THE_END = Pattern.compile("(?<![а-яіїєґА-ЯІЇЄҐ\u0301])(тис|[А-ЯІЇЄҐ])\\.$");

  private static final Pattern YEAR_WITH_R = Pattern.compile("((?:[12][0-9]{3}[—–-])?[12][0-9]{3})(рр?\\.)");

  // Сьогодні (у четвер. - Ред.), вранці.
//  private static final Pattern ABBR_DOT_PATTERN8 = Pattern.compile("([\\s\u00A0\u202F]+[–—-][\\s\u00A0\u202F]+(?:[Рр]ед|[Аа]вт))\\.([\\)\\]])");
  private static final Pattern ABBR_DOT_RED_AVT_PATTERN = Pattern.compile("([\\s\u00A0\u202F]+(?:[Рр]ед|[Аа]вт))\\.([\\)\\]])");
  
  private static final String SOFT_HYPHEN_WRAP = "\u00AD\n";
  private static final String SOFT_HYPHEN_WRAP_SUBST = "\uE103";
  // url
  private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", Pattern.CASE_INSENSITIVE);
  private static final int URL_START_REPLACE_CHAR = 0xE300;


  public UkrainianWordTokenizer() {
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

    if( text.indexOf('\u2014') != -1 ) {
      text = text.replaceAll("\u2014(\\s)", BREAKING_PLACEHOLDER + "\u2014$1");
    }

    if( text.indexOf("с/г") != -1 ) {
      text = text.replaceAll("с/г", "с" +NON_BREAKING_SLASH_SUBST + "г");
    }

    if( text.indexOf("р.") != -1 ) {
      Matcher matcher = YEAR_WITH_R.matcher(text);
      if( matcher.find() ) {
        text = matcher.replaceAll("$1" + BREAKING_PLACEHOLDER + "$2");
      }
    }

    // if period is not the last character in the sentence
    int dotIndex = text.indexOf('.');
    boolean dotInsideSentence = dotIndex >= 0 && dotIndex < text.length()-1;

    if( dotInsideSentence 
        || (dotIndex == text.length()-1
            && ABBR_AT_THE_END.matcher(text).find()) ) {  // ugly - special case for тис.

      text = DATE_PATTERN.matcher(text).replaceAll(DATE_PATTERN_REPL);
      text = DOTTED_NUMBERS_PATTERN.matcher(text).replaceAll(DOTTED_NUMBERS_REPL);

      text = ABBR_DOT_2_SMALL_LETTERS_PATTERN.matcher(text).replaceAll(ABBR_DOT_2_SMALL_LETTERS_REPL);
      text = ABBR_DOT_VO_PATTERN1.matcher(text).replaceAll(ABBR_DOT_2_SMALL_LETTERS_REPL);
      text = ABBR_DOT_VO_PATTERN2.matcher(text).replaceAll(ABBR_DOT_2_SMALL_LETTERS_REPL);
      text = ABBR_DOT_VO_PATTERN3.matcher(text).replaceAll(ABBR_DOT_2_SMALL_LETTERS_REPL);
      text = ABBR_DOT_TYS_PATTERN1.matcher(text).replaceAll("$1$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER);
      text = ABBR_DOT_TYS_PATTERN2.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_LAT_PATTERN.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_PROF_PATTERN.matcher(text).replaceAll(ONE_DOT_TWO_REPL);

      text = INITIALS_DOT_PATTERN_SP_2.matcher(text).replaceAll(INITIALS_DOT_REPL_SP_2);
      text = INITIALS_DOT_PATTERN_SP_1.matcher(text).replaceAll(INITIALS_DOT_REPL_SP_1);
      text = INITIALS_DOT_PATTERN_RSP_2.matcher(text).replaceAll(INITIALS_DOT_REPL_RSP_2);
      text = INITIALS_DOT_PATTERN_RSP_1.matcher(text).replaceAll(INITIALS_DOT_REPL_RSP_1);

//      text = ABBR_DOT_INVALID_DOT_PATTERN.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_KUB_SM_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2");
      text = ABBR_DOT_S_G_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER);
      text = ABBR_DOT_PN_ZAH_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER);
      text = ABBR_DOT_I_T_P_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER);
      text = ABBR_DOT_T_ZV_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER);
      text = ABBR_DOT_RED_AVT_PATTERN.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_NON_ENDING_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER);
      text = ABBR_DOT_NON_ENDING_PATTERN_2.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
    }

    // preserve * inside words (sometimes used instead of apostrophe or to mask profane words)
    // but split if it's the beginning or end of the word (often used for mark-up and footnotes)
    if( text.contains("*") ) {
      text = text.replaceAll("((?:^|[^а-яіїєґА-ЯІЇЄҐ])\\*+)([а-яіїєґА-ЯІЇЄҐ])", "$1" + BREAKING_PLACEHOLDER + "$2");
      text = text.replaceAll("([а-яіїєґА-ЯІЇЄҐ])(\\*+(?:[^а-яіїєґА-ЯІЇЄҐ]|$))", "$1" + BREAKING_PLACEHOLDER + "$2");
    }

    text = ABBR_DOT_ENDING_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST);

    // 2 000 000
    Matcher spacedDecimalMatcher = DECIMAL_SPACE_PATTERN.matcher(text);
    if( spacedDecimalMatcher.find() ) {
    	StringBuffer sb = new StringBuffer();
    	do {
    		String splitNumber = spacedDecimalMatcher.group(0);
    		String splitNumberAdjusted = splitNumber.replace(' ', NON_BREAKING_SPACE_SUBST);
    		splitNumberAdjusted = splitNumberAdjusted.replace('\u00A0', NON_BREAKING_SPACE_SUBST);
    		splitNumberAdjusted = splitNumberAdjusted.replace('\u202F', NON_BREAKING_SPACE_SUBST);
    		spacedDecimalMatcher.appendReplacement(sb, splitNumberAdjusted);
    	} while( spacedDecimalMatcher.find() );

    	spacedDecimalMatcher.appendTail(sb);
    	text = sb.toString();
    }

    // 12:25
    if( text.contains(":") ) {
    	text = COLON_NUMBERS_PATTERN.matcher(text).replaceAll(COLON_NUMBERS_REPL);
    }

    // ВКПБ(о)
    if( text.contains("(") ) {
      text = BRACE_IN_WORD_PATTERN.matcher(text).replaceAll("$1" + LEFT_BRACE_SUBST + "$2" + RIGHT_BRACE_SUBST);
    }

    if( text.contains(SOFT_HYPHEN_WRAP) ) {
      text = text.replace(SOFT_HYPHEN_WRAP, SOFT_HYPHEN_WRAP_SUBST);
    }


    List<String> tokenList = new ArrayList<>();

    List<String> tokens = splitWithDelimiters(text, SPLIT_CHARS_REGEX);

    for(String token: tokens) {

      if( token.equals(BREAKING_PLACEHOLDER) )
        continue;

      token = token.replace(DECIMAL_COMMA_SUBST, ',');

      token = token.replace(NON_BREAKING_SLASH_SUBST, '/');
      token = token.replace(NON_BREAKING_COLON_SUBST, ':');
      token = token.replace(NON_BREAKING_SPACE_SUBST, ' ');

      token = token.replace(LEFT_BRACE_SUBST, '(');
      token = token.replace(RIGHT_BRACE_SUBST, ')');

      // outside of if as we also replace back sentence-ending abbreviations
      token = token.replace(NON_BREAKING_DOT_SUBST, '.');

      token = token.replace(SOFT_HYPHEN_WRAP_SUBST, SOFT_HYPHEN_WRAP);

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
    text = text
        .replace('\u2019', '\'')
        .replace('\u02BC', '\'')
        .replace('\u2018', '\'')
        .replace('`', '\'')
        .replace('´',  '\'')
        .replace('\u201A', ',')  // SINGLE LOW-9 QUOTATION MARK sometimes used as a comma
        .replace('\u2011', '-'); // we handle \u2013 in tagger so we can base our rule on it

    text = WEIRD_APOSTROPH_PATTERN.matcher(text).replaceAll("$1'$2");

    return text;
  }

  private static List<String> splitWithDelimiters(String str, Pattern delimPattern) {
    List<String> parts = new ArrayList<String>();

    Matcher matcher = delimPattern.matcher(str);

    int lastEnd = 0;
    while (matcher.find()) {
      int start = matcher.start();

      if (lastEnd != start) {
        String nonDelim = str.substring(lastEnd, start);
        parts.add(nonDelim);
      }

      String delim = matcher.group();
      parts.add(delim);

      lastEnd = matcher.end();
    }

    if (lastEnd != str.length()) {
      String nonDelim = str.substring(lastEnd);
      parts.add(nonDelim);
    }

    return parts;
  }

}
