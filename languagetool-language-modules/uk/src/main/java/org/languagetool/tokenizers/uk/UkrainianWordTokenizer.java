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
//      "(?<!\uE120)(!{2,3}|\\?{2,3}|\\.{3}|[!?][!?.]{1,2}"
            "(!{2,3}|\\?{2,3}|\\.{3}|[!?][!?.]{1,2}"
            + "|[\u0020\u00A0\\n\\r\\t"
            + ",.;!?\u2014\u2015:()\\[\\]{}<>/|\\\\…°$€₴=№§¿¡~×]" 
            + "|%(?![-\u2013][а-яіїєґ])" // allow 5%-й
            + "|(?<!\uE109)[\"«»„”“]"                       // quotes have special cases
            + "|(?<=[а-яіїєґА-ЯІЇЄҐ])[\u00B9\u00B2\u2070-\u2079]"  // superscript for regular words only //
          // preserve * inside words (sometimes used instead of apostrophe or to mask profane words)
          // but split if it's the beginning or end of the word (often used for mark-up and footnotes)
            + "|(?<![а-яіїєґА-ЯІЇЄҐa-zA-Z])[_*]+"
            + "|[_*]+(?![а-яіїєґА-ЯІЇЄҐa-zA-Z0-9])"
            + "|[\u2000-\u200F"
            + "\u201A\u2020-\u202F\u2030-\u206F"
            + "\u2400-\u27FF"    // Control Pictures
            + String.valueOf(Character.toChars(0x1F000)) + "-" + String.valueOf(Character.toChars(0x1FFFF))          // Emojis
            + "\uf000-\uffff" // private unicode area: U+E000..U+F8FF
            + "\uE110])(?!\uE120)";

  private static final Pattern SPLIT_CHARS_REGEX = Pattern.compile(SPLIT_CHARS);


  // for handling exceptions

  private static final char DECIMAL_COMMA_SUBST = '\uE001'; // some unused character to hide comma in decimal number temporary for tokenizer run
  private static final char NON_BREAKING_SPACE_SUBST = '\uE002';
  private static final char NON_BREAKING_DOT_SUBST = '\uE003'; // some unused character to hide dot in date temporary for tokenizer run
  private static final char NON_BREAKING_COLON_SUBST = '\uE004';
  private static final char LEFT_BRACE_SUBST = '\uE005';
  private static final char RIGHT_BRACE_SUBST = '\uE006';
  private static final char NON_BREAKING_SLASH_SUBST = '\uE007';    // hide slash in с/г
  private static final char LEFT_ANGLE_SUBST = '\uE008';
  private static final char RIGHT_ANGLE_SUBST = '\uE009';
  private static final char SLASH_SUBST = '\uE010';
  private static final String NON_BREAKING_PLACEHOLDER = "\uE109";
  private static final String BREAKING_PLACEHOLDER = "\uE110";
  private static final String NON_BREAKING_PLACEHOLDER2 = "\uE120";
  // TODO: use \uE120 for most of non-breaking cases

  private static final Pattern WEIRD_APOSTROPH_PATTERN = Pattern.compile("([бвджзклмнпрстфхш])([\"\u201D\u201F`´])([єїюя])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  public static final Pattern WORDS_WITH_BRACKETS_PATTERN = Pattern.compile("([а-яіїєґ])\\[([а-яіїєґ]+)\\]", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA_PATTERN = Pattern.compile("([\\d]),([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String DECIMAL_COMMA_REPL = "$1" + DECIMAL_COMMA_SUBST + "$2";

  // space between digits
  private static final Pattern DECIMAL_SPACE_PATTERN = Pattern.compile("(?<=^|[\\h\\v(])\\d{1,3}([\\h][\\d]{3})+(?=[\\h\\v(]|$)", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  // numbers with n-dash
  private static final Pattern DASH_NUMBERS_PATTERN = Pattern.compile("([IVXІХ]+)([\u2013-])([IVXІХ]+)");
  private static final String DASH_NUMBERS_REPL = "$1" + BREAKING_PLACEHOLDER + "$2" + BREAKING_PLACEHOLDER + "$3";
  private static final Pattern N_DASH_SPACE_PATTERN = Pattern.compile("([а-яіїєґa-z0-9])(\u2013\\h)(?!(та|чи|і|й)[\\h\\v])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern N_DASH_SPACE_PATTERN2 = Pattern.compile("([\\h.,;!?]\u2013)([а-яіїєґa-z])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final String N_DASH_SPACE_REPL = "$1" + BREAKING_PLACEHOLDER + "$2";

  // dots in numbers
  private static final Pattern DOTTED_NUMBERS_PATTERN = Pattern.compile("([\\d])\\.([\\d])");
  private static final Pattern DOTTED_NUMBERS_PATTERN3 = Pattern.compile("([\\d])\\.([\\d]+)\\.([\\d])");

  // colon in numbers
  private static final Pattern COLON_NUMBERS_PATTERN = Pattern.compile("([\\d]):([\\d])");
  private static final String COLON_NUMBERS_REPL = "$1" + NON_BREAKING_COLON_SUBST + "$2";

  // dates
  // braces in words
  private static final Pattern BRACE_IN_WORD_PATTERN = Pattern.compile("([а-яіїєґ])\\(([а-яіїєґ']+)\\)", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  private static final Pattern XML_TAG_PATTERN = Pattern.compile("<(/?[a-z_]+/?)>", Pattern.CASE_INSENSITIVE);

  // tokenize initials with dot before last name, e.g. "А.", "Ковальчук"
  private static final Pattern INITIALS_DOT_PATTERN_SP_2 = Pattern.compile("([А-ЯІЇЄҐ])\\.([\\h\\v]{0,5}[А-ЯІЇЄҐ])\\.([\\h\\v]{0,5}[А-ЯІЇЄҐ][а-яіїєґ']+)");
  private static final Pattern INITIALS_DOT_PATTERN_SP_1 = Pattern.compile("([А-ЯІЇЄҐ])\\.([\\h\\v]{0,5}[А-ЯІЇЄҐ][а-яіїєґ']+)");

  // tokenize initials with dot after last name, e.g.  "Ковальчук", "А."
  private static final Pattern INITIALS_DOT_PATTERN_RSP_2 = Pattern.compile("([А-ЯІЇЄҐ][а-яіїєґ']+)([\\h\\v]?[А-ЯІЇЄҐ])\\.([\\h\\v]?[А-ЯІЇЄҐ])\\.");
  private static final Pattern INITIALS_DOT_PATTERN_RSP_1 = Pattern.compile("([А-ЯІЇЄҐ][а-яіїєґ']+)([\\h\\v]?[А-ЯІЇЄҐ])\\.");

  private static final String INITIALS_DOT_REPL_SP_2 = "$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$3";
  private static final String INITIALS_DOT_REPL_SP_1 = "$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2";
  private static final String INITIALS_DOT_REPL_RSP_2 = "$1" + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$3" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER;
  private static final String INITIALS_DOT_REPL_RSP_1 = "$1" + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER;

  // abbreviation dot
  private static final Pattern ABBR_DOT_VO_PATTERN1 = Pattern.compile("([вВу])\\.([\\h\\v]*о)\\.");
  private static final Pattern ABBR_DOT_VO_PATTERN2 = Pattern.compile("(к)\\.([\\h\\v]*с)\\.");
  private static final Pattern ABBR_DOT_VO_PATTERN3 = Pattern.compile("(ч|ст)\\.([\\h\\v]*л)\\.");
//  private static final Pattern ABBR_DOT_VO_PATTERN4 = Pattern.compile("(р)\\.([\\s\u00A0\u202F]*х)\\.");
  private static final Pattern ABBR_DOT_TYS_PATTERN1 = Pattern.compile("([0-9IІ][\\h\\v]+)(тис|арт)\\.");
  private static final Pattern ABBR_DOT_TYS_PATTERN2 = Pattern.compile("(тис|арт)\\.([\\h\\v]+[а-яіїєґ0-9])");
  private static final Pattern ABBR_DOT_ART_PATTERN = Pattern.compile("([Аа]рт|[Мм]ал|[Рр]ис|[Сс]пр)\\.([\\h]*(№[\\h]*)?[0-9])");
  private static final Pattern ABBR_DOT_MAN_PATTERN = Pattern.compile("(Ман)\\.([\\h]*(Сіті|[Юю]н))");
  private static final Pattern ABBR_DOT_LAT_PATTERN = Pattern.compile("([^а-яіїєґА-ЯІЇЄҐ'\u0301-]лат)\\.([\\h\\v]+[a-zA-Z])");
  private static final Pattern ABBR_DOT_PROF_PATTERN = Pattern.compile("(?<![а-яіїєґА-ЯІЇЄҐ'\u0301-])([Аа]кад|[Пп]роф|[Дд]оц|[Аа]сист|[Аа]рх|ап|тов|вул|бул|бульв|о|р|ім|упорядн?|др|[Пп]реп|Ів|Дж|Ол|[сС]вт|Авг)\\.([\\h\\v]+[А-ЯІЇЄҐа-яіїєґ])");
  private static final Pattern ABBR_DOT_GUB_PATTERN = Pattern.compile("(.[А-ЯІЇЄҐ][а-яіїєґ'-]+[\\h\\v]+губ)\\.");
  private static final Pattern ABBR_DOT_DASH_PATTERN = Pattern.compile("\\b([А-ЯІЇЄҐ]ж?)\\.([-\u2013]([А-ЯІЇЄҐ][а-яіїєґ']{2}|[А-ЯІЇЄҐ]\\.))", Pattern.UNICODE_CHARACTER_CLASS);
  // село, місто, річка (якщо з цифрою: секунди, метри, роки) - з роками складно
  //private static final Pattern ABBR_DOT_INVALID_DOT_PATTERN = Pattern.compile("((?:[0-9]|кв\\.|куб\\.)[\\s\u00A0\u202F]+(?:[смкд]|мк)?м)\\.(.)");
  private static final Pattern ABBR_DOT_KUB_SM_PATTERN = Pattern.compile("(кв|куб)\\.([\\h\\v]*(?:[смкд]|мк)?м)");
  private static final Pattern ABBR_DOT_S_G_PATTERN = Pattern.compile("(с)\\.(-г)\\.");
  private static final Pattern ABBR_DOT_CHL_KOR_PATTERN = Pattern.compile("(чл)\\.(-кор)\\.");
  private static final Pattern ABBR_DOT_PN_ZAH_PATTERN = Pattern.compile("(пн|пд)\\.(-(зах|сх))\\.");
  private static final Pattern INVALID_MLN_DOT_PATTERN = Pattern.compile("(млн|млрд)\\.( [а-яіїєґ])");
  private static final Pattern ABBR_DOT_2_SMALL_LETTERS_PATTERN = Pattern.compile("([^а-яіїєґА-ЯІЇЄҐ'\u0301-][векнпрстцч]{1,2})\\.(\\h*(?![смкд]?м\\.)[екмнпрстч]{1,2})\\.");
  private static final String ABBR_DOT_2_SMALL_LETTERS_REPL = "$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER;
  
  private static final String ONE_DOT_TWO_REPL = "$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER + "$2";

  // скорочення що не можуть бути в кінці речення
  private static final Pattern ABBR_DOT_NON_ENDING_PATTERN = Pattern.compile("(?<![а-яіїєґА-ЯІЇЄҐ'\u0301-])(абз|австрал|ам|амер|англ|акад(ем)?|арк|ауд|біол|бл(?:изьк)?|болг|буд|в(?!\\.+)|вип|вірм|грец(?:ьк)?"
      + "|держ|див|дир|діал|дод|дол|досл|доц|доп|екон|ел|жін|зав|заст|зах|зб|зв|зневажл?|зовн|іл|ім|івр|інж|ісп|іст|італ"
      + "|к|каб|каф|канд|кв|[1-9]-кімн|кімн|кін|кл|кн|коеф|крим|латин|мал|моб|н|[Нн]апр|нач|нім|нац|нпр|образн|оз|оп|оф|п|пен|перекл|перен|пл|пол|пом|пор|порівн|[Пп]оч|пп|прибл|прикм|прим|присл|пров|пром|просп"
      + "|[Рр]ед|[Рр]еж|розд|розм|рос|рт|рум|с|санскр|[Сс]вв?|скор|соц|співавт|[сС]т|стор|суч|сх|табл|тт|[тТ]ел|техн|укр|філол|фр|франц|худ|[цЦ]ит|ч|чайн|част|ц|яп|япон)\\.(?!\uE120|\\.+[\\h\\v]*$)");
  private static final Pattern ABBR_DOT_NON_ENDING_PATTERN_2 = Pattern.compile("([^а-яіїєґА-ЯІЇЄҐ'-]м\\.)([\\h\\v]*[А-ЯІЇЄҐ])");
  
  private static final Pattern ABBR_DOT_NAR_PATTERN_1 = Pattern.compile("(([0-9]|рік|[рp]\\.|[-–—])[\\h\\v]+нар)\\.");
  private static final Pattern ABBR_DOT_NAR_PATTERN_2 = Pattern.compile("\\b(нар)\\.([\\h\\v]+[0-9а-яіїєґ])", Pattern.UNICODE_CHARACTER_CLASS);

  // скорочення що можуть бути в кінці речення
  private static final Pattern ABBR_DOT_ENDING_PATTERN = Pattern.compile("([^а-яіїєґА-ЯІЇЄҐ'\u0301-]((та|й|і) (інш?|под)|атм|відс|гр|коп|дес|дол|обл|пов|р|рр|РР|руб|ст|стст|стол|стор|чол|шт))\\.(?!\uE120)");
  private static final Pattern ABBR_DOT_I_T_P_PATTERN = Pattern.compile("([ій][\\h\\v]+т\\.)([\\h\\v]*(д|п|ін)\\.)");
  private static final Pattern ABBR_DOT_I_T_CH_PATTERN = Pattern.compile("([ву][\\h\\v]+т\\.)([\\h\\v]*ч\\.)");
  private static final Pattern ABBR_DOT_T_ZV_PATTERN = Pattern.compile("([\\h\\v\\(]+т\\.)([\\h\\v]*зв\\.)");

  private static final Pattern ABBR_AT_THE_END = Pattern.compile("(?<![а-яіїєґА-ЯІЇЄҐ'\u0301])(тис|губ|[А-ЯІЇЄҐ])\\.[\\h\\v]*$");

  private static final Pattern APOSTROPHE_BEGIN_PATTERN = Pattern.compile("(^|[\\h\\v(„«\"'])'(?!дно)(\\p{L})");
  private static final Pattern APOSTROPHE_END_PATTER = Pattern.compile("(\\p{L})(?<!\\b(?:мо|тре|тра|чо|нічо|бо|зара|пра))'([^\\p{L}-]|$)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);

  private static final Pattern YEAR_WITH_R = Pattern.compile("((?:[12][0-9]{3}[—–-])?[12][0-9]{3})(рр?\\.)");

  private static final Pattern COMPOUND_WITH_QUOTES1 = Pattern.compile("([а-яіїє]-)([«\"„])([а-яіїєґ'-]+)([»\"“])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern COMPOUND_WITH_QUOTES2 = Pattern.compile("([«\"„])([а-яіїєґ0-9'-]+)([»\\\"“])(-[а-яіїє])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  // Сьогодні (у четвер. - Ред.), вранці.
//  private static final Pattern ABBR_DOT_PATTERN8 = Pattern.compile("([\\s\u00A0\u202F]+[–—-][\\s\u00A0\u202F]+(?:[Рр]ед|[Аа]вт))\\.([\\)\\]])");
  private static final Pattern ABBR_DOT_RED_AVT_PATTERN = Pattern.compile("([\\h\\v]+(?:[Рр]ед|[Аа]вт))\\.(\\h*[)\\]а-яіїєґ])");
  
  private static final String SOFT_HYPHEN_WRAP = "\u00AD\n";
  private static final String SOFT_HYPHEN_WRAP_SUBST = "\uE103";
  // url
  private static final Pattern URL_PATTERN = Pattern.compile("((https?|ftp)://|www\\.)[^\\h\\v/$.?#),]+\\.[^\\h\\v),\">]*|(mailto:)?[\\p{L}\\d._-]+@[\\p{L}\\d_-]+(\\.[\\p{L}\\d_-]+)+", Pattern.CASE_INSENSITIVE);
  private static final int URL_START_REPLACE_CHAR = 0xE300;

  private static final Pattern LEADING_DASH_PATTERN = Pattern.compile("^([\u2014\u2013])([а-яіїєґА-ЯІЇЄҐA-Z])");
  private static final Pattern LEADING_DASH_PATTERN_2 = Pattern.compile("^(-)([А-ЯІЇЄҐA-Z])");

  private static final Pattern NUMBER_MISSING_SPACE = Pattern.compile("((?:[\\h\\v\uE110]|^)[а-яїієґА-ЯІЇЄҐ'-]*[а-яїієґ']?[а-яїієґ])([0-9]+(?![а-яіїєґА-ЯІЇЄҐa-zA-Z»\"“]))");

  private static final Pattern WEB_ENTITIES = Pattern.compile("([а-яіїєґ])\\.(НЕТ|net|Інфо|Info|City|Life|UA|юа|лі|media|com|фм|ru|ру|орг)\\b", Pattern.CASE_INSENSITIVE| Pattern.UNICODE_CHARACTER_CLASS);
  private static final Pattern WEB_ENTITIES2 = Pattern.compile("\\.([a-z_-]+)\\.(ua)", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  
  public UkrainianWordTokenizer() {
  }

  @Override
  public List<String> tokenize(String text) {
    HashMap<String, String> urls = new HashMap<>();

    if( ! text.trim().isEmpty() ) {
      text = adjustTextForTokenizing(text, urls);
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

      token = token.replace(LEFT_ANGLE_SUBST, '<');
      token = token.replace(RIGHT_ANGLE_SUBST, '>');
      token = token.replace(SLASH_SUBST, '/');

      // outside of if as we also replace back sentence-ending abbreviations
      token = token.replace(NON_BREAKING_DOT_SUBST, '.');

      token = token.replace(SOFT_HYPHEN_WRAP_SUBST, SOFT_HYPHEN_WRAP);

      token = token.replace(NON_BREAKING_PLACEHOLDER, "");
      token = token.replace(NON_BREAKING_PLACEHOLDER2, "");

      if( ! urls.isEmpty() ) {
        for(Entry<String, String> entry : urls.entrySet()) {
          token = token.replace(entry.getKey(), entry.getValue());
        }
      }

      tokenList.add( token );
    }

    return tokenList;
  }

  private String adjustTextForTokenizing(String text, HashMap<String, String> urls) {
    text = cleanup(text);

    if( "\u2014\u2013-".indexOf(text.charAt(0)) >=0 ) {
      Matcher matcher = LEADING_DASH_PATTERN.matcher(text);
      if( matcher.find() ) {
        text = matcher.replaceFirst("$1"+BREAKING_PLACEHOLDER+"$2");
      }
      else {
        matcher = LEADING_DASH_PATTERN_2.matcher(text);
        if( matcher.find() ) {
          text = matcher.replaceFirst("$1"+BREAKING_PLACEHOLDER+"$2");
        }
      }
    }
    
    if( text.contains(",") ) {
      text = DECIMAL_COMMA_PATTERN.matcher(text).replaceAll(DECIMAL_COMMA_REPL);
    }

    // check for urls
    if( text.contains("http") || text.contains("www") || text.contains("@") || text.contains("ftp") ) { // https?|ftp
      Matcher matcher = URL_PATTERN.matcher(text);
      int urlReplaceChar = URL_START_REPLACE_CHAR;
      
      while( matcher.find() ) {
        String urlGroup = matcher.group();
        String replaceChar = String.valueOf((char)urlReplaceChar);
        urls.put(replaceChar, urlGroup);
        text = matcher.replaceFirst(replaceChar);
        urlReplaceChar++;
        matcher = URL_PATTERN.matcher(text);
      }
    }

    if( text.indexOf('\u2014') != -1 ) {
      text = text.replaceAll("\u2014([\\h\\v])", BREAKING_PLACEHOLDER + "\u2014$1");
    }

    boolean nDashPresent = text.indexOf('\u2013') != -1;
    if( text.indexOf('-') != -1 || nDashPresent ) {
      text = DASH_NUMBERS_PATTERN.matcher(text).replaceAll(DASH_NUMBERS_REPL);
      if( nDashPresent ) {
        text = N_DASH_SPACE_PATTERN.matcher(text).replaceAll(N_DASH_SPACE_REPL);
        text = N_DASH_SPACE_PATTERN2.matcher(text).replaceAll(N_DASH_SPACE_REPL);
      }
    }

    if( text.indexOf("с/г") != -1 ) {
      text = text.replace("с/г", "с" +NON_BREAKING_SLASH_SUBST + "г");
    }

    if( text.indexOf("Л/ДНР") != -1 ) {
      text = text.replace("Л/ДНР", "Л" +NON_BREAKING_SLASH_SUBST + "ДНР");
    }

    if( text.indexOf("р.") != -1 ) {
      Matcher matcher = YEAR_WITH_R.matcher(text);
      if( matcher.find() ) {
        text = matcher.replaceAll("$1" + BREAKING_PLACEHOLDER + "$2");
      }
    }

    // leave only potential hashtags together
    text = text.replace("#", BREAKING_PLACEHOLDER + "#");
    // leave numbers with following % together
    if( text.indexOf('%') >= 0 ) {
      text = text.replaceAll("%([^-])", "%" + BREAKING_PLACEHOLDER + "$1");
    }

    
    text = COMPOUND_WITH_QUOTES1.matcher(text).replaceAll("$1$2\uE120$3\uE120$4\uE120");
    text = COMPOUND_WITH_QUOTES2.matcher(text).replaceAll("$1\uE120$2\uE120$3\uE120$4");
    if( text.indexOf('[') != -1 ) {
      text = WORDS_WITH_BRACKETS_PATTERN.matcher(text).replaceAll("$1\\[\uE120$2\\]\uE120");
    }
    
    // if period is not the last character in the sentence
    int dotIndex = text.indexOf('.');
    String textRtrimmed = text.replaceFirst("[\\h\\v]*$", "");
    boolean dotInsideSentence = dotIndex >= 0 && dotIndex < textRtrimmed.length()-1;

    if( dotInsideSentence 
        || (dotIndex == textRtrimmed.length()-1
            && ABBR_AT_THE_END.matcher(text).find()) ) {  // ugly - special case for тис. та ініціалів

      text = DOTTED_NUMBERS_PATTERN3.matcher(text).replaceAll("$1.\uE120$2.\uE120$3");
      text = DOTTED_NUMBERS_PATTERN.matcher(text).replaceAll("$1.\uE120$2");

      text = ABBR_DOT_NAR_PATTERN_1.matcher(text).replaceAll("$1.\uE120\uE110");
      text = ABBR_DOT_NAR_PATTERN_2.matcher(text).replaceAll("$1.\uE120\uE110$2");

      text = ABBR_DOT_2_SMALL_LETTERS_PATTERN.matcher(text).replaceAll("$1.\uE120\uE110$2.\uE120\uE110"); //.replaceFirst("(([смкд]|мк)?м\\.[\\h\\v]*)\uE120\uE110$", "$1");
      text = ABBR_DOT_VO_PATTERN1.matcher(text).replaceAll(ABBR_DOT_2_SMALL_LETTERS_REPL);
      text = ABBR_DOT_VO_PATTERN2.matcher(text).replaceAll(ABBR_DOT_2_SMALL_LETTERS_REPL);
      text = ABBR_DOT_VO_PATTERN3.matcher(text).replaceAll(ABBR_DOT_2_SMALL_LETTERS_REPL);
      text = ABBR_DOT_ART_PATTERN.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_MAN_PATTERN.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_TYS_PATTERN1.matcher(text).replaceAll("$1$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER);
      text = ABBR_DOT_TYS_PATTERN2.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_LAT_PATTERN.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_PROF_PATTERN.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_GUB_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER);
      text = ABBR_DOT_DASH_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST + "$2");

      text = INITIALS_DOT_PATTERN_SP_2.matcher(text).replaceAll(INITIALS_DOT_REPL_SP_2);
      text = INITIALS_DOT_PATTERN_SP_1.matcher(text).replaceAll(INITIALS_DOT_REPL_SP_1);
      text = INITIALS_DOT_PATTERN_RSP_2.matcher(text).replaceAll(INITIALS_DOT_REPL_RSP_2);
      text = INITIALS_DOT_PATTERN_RSP_1.matcher(text).replaceAll(INITIALS_DOT_REPL_RSP_1);

//      text = ABBR_DOT_INVALID_DOT_PATTERN.matcher(text).replaceAll(ONE_DOT_TWO_REPL);
      text = ABBR_DOT_KUB_SM_PATTERN.matcher(text).replaceAll("$1.\uE120\uE110$2");
      text = ABBR_DOT_S_G_PATTERN.matcher(text).replaceAll("$1" + NON_BREAKING_DOT_SUBST + "$2" + NON_BREAKING_DOT_SUBST + BREAKING_PLACEHOLDER);
      text = ABBR_DOT_CHL_KOR_PATTERN.matcher(text).replaceAll("$1.\uE120$2.\uE120\uE110");
      text = ABBR_DOT_PN_ZAH_PATTERN.matcher(text).replaceAll("$1.\uE120\uE110$2.\uE120\uE110");
      text = ABBR_DOT_I_T_P_PATTERN.matcher(text).replaceAll("$1\uE120\uE110$2\uE120\uE110");
      text = ABBR_DOT_I_T_CH_PATTERN.matcher(text).replaceAll("$1\uE120\uE110$2\uE120\uE110");
      text = ABBR_DOT_T_ZV_PATTERN.matcher(text).replaceAll("$1\uE120\uE110$2\uE120\uE110");
      text = ABBR_DOT_RED_AVT_PATTERN.matcher(text).replaceAll("$1.\uE120\uE110$2");
      text = ABBR_DOT_NON_ENDING_PATTERN.matcher(text).replaceAll("$1.\uE120\uE110");
      text = ABBR_DOT_NON_ENDING_PATTERN_2.matcher(text).replaceAll("$1\uE120\uE110$2");
      text = INVALID_MLN_DOT_PATTERN.matcher(text).replaceAll("$1.\uE120\uE110$2");
    }

    if( dotInsideSentence ) {
      text = WEB_ENTITIES.matcher(text).replaceAll("$1.\uE120$2");
      text = WEB_ENTITIES2.matcher(text).replaceAll(".\uE120$1.\uE120$2");
    }

    text = ABBR_DOT_ENDING_PATTERN.matcher(text).replaceAll("$1.\uE120\uE110");

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

    if( text.contains("<") ) {
      text = XML_TAG_PATTERN.matcher(text).replaceAll(BREAKING_PLACEHOLDER + LEFT_ANGLE_SUBST + "$1" + RIGHT_ANGLE_SUBST + BREAKING_PLACEHOLDER);
      text = text.replace(LEFT_ANGLE_SUBST+"/", "" + LEFT_ANGLE_SUBST + SLASH_SUBST);
      text = text.replace("/" + RIGHT_ANGLE_SUBST, "" + SLASH_SUBST + RIGHT_ANGLE_SUBST);
    }

    if( text.contains("-") ) {
      text = text.replaceAll("([а-яіїєґА-ЯІЇЄҐ])([»\"-]+-)", "$1" + BREAKING_PLACEHOLDER + "$2");
      text = text.replaceAll("([»\"-]+-)([а-яіїєґА-ЯІЇЄҐ])", "$1" + BREAKING_PLACEHOLDER + "$2");
    }

    if( text.contains(SOFT_HYPHEN_WRAP) ) {
      text = text.replaceAll("(?<!\\s)"+SOFT_HYPHEN_WRAP, SOFT_HYPHEN_WRAP_SUBST);
    }

    if( text.indexOf('\'') >= 0 ) {
      text = APOSTROPHE_BEGIN_PATTERN.matcher(text).replaceAll("$1'" + BREAKING_PLACEHOLDER + "$2");
      text = APOSTROPHE_END_PATTER.matcher(text).replaceAll("$1" + BREAKING_PLACEHOLDER + "'$2");
    }

    if( text.contains("+") ) {
      text = text.replaceAll("\\+(?=[а-яіїєґА-ЯІЇЄҐ0-9])", BREAKING_PLACEHOLDER + "+" + BREAKING_PLACEHOLDER);
    }
    
    // -20C
    if( text.length() > 1 && (text.contains("-") || text.contains("\u2013")) ) {
      text = text.replaceAll("(?<=(^|[\\h\\v]))([-\u2013])(?=[0-9])", "$2" + BREAKING_PLACEHOLDER);
    }
    
    text = NUMBER_MISSING_SPACE.matcher(text).replaceAll("$1" + BREAKING_PLACEHOLDER + "$2");
    return text;
  }

  private static String cleanup(String text) {
    text = text
        .replace('\u2019', '\'')
        .replace('\u02BC', '\'')
        .replace('\u2018', '\'')
//        .replace('`', '\'')
//        .replace('´',  '\'')
        .replace('\u201A', ',')  // SINGLE LOW-9 QUOTATION MARK sometimes used as a comma
        .replace('\u2011', '-'); // we handle \u2013 in tagger so we can base our rule on it

    text = WEIRD_APOSTROPH_PATTERN.matcher(text).replaceAll("$1\uE120$2\uE120$3");

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
