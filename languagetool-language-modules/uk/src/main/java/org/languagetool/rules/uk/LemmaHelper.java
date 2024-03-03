package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * @since 3.6
 */
public abstract class LemmaHelper {
  private static final String IGNORE_CHARS = "\u00AD\u0301";
  public static final Set<String> CITY_AVENU = new HashSet<>(Arrays.asList("сіті", "ситі", "стріт", "стрит", "рівер", "ривер", "авеню", "штрасе", "штрассе", "сьоркл", "сквер"));
  public static final List<String> MONTH_LEMMAS = Arrays.asList("січень", "лютий", "березень", "квітень", "травень", "червень", "липень", 
      "серпень", "вересень", "жовтень", "листопад", "грудень");
  public static final List<String> DAYS_OF_WEEK = Arrays.asList("понеділок", "вівторок", "середа", "четвер", "п'ятниця", "субота", "неділя");
  public static final List<String> TIME_LEMMAS = Arrays.asList(
      "секунда", "хвилина", "хвилинка", "хвилина-дві", "хвилинка-друга", 
      "година", "годинка", "півгодини", "година-друга", "година-дві", 
      "час", "день", "день-другий", "півдня", "ніч", "ніченька", "вечір", "ранок", "тиждень", "тиждень-два", "тиждень-другий", 
      "місяць", "місяць-два", "місяць-другий", "місяць-півтора", "доба", "мить", "хвилька",
      "рік", "рік-два", "рік-півтора", "півроку", "півроку-рік", "десятиліття", "десятиріччя", "століття", "півстоліття", "сторіччя", "півсторіччя", "тисячоліття", "півтисячоліття", "квартал", "годочок",
      "літо", "зима", "весна", "осінь",
      "тайм", "мить", "період", "термін", "сезон", "декада", "каденція", "раунд", "сезон");
  public static final List<String> DISTANCE_LEMMAS = Arrays.asList(
      "міліметр", "сантиметр", "метр", "кілометр", "кілограм", "кілограм–півтора", 
      "гектар", "миля", "аршин", "дециметр", "верства", "верста",
      "грам", "літр", "фунт", "тонна", "центнер");
  public static final List<String> PSEUDO_NUM_LEMMAS = Arrays.asList(
      "десяток", "десяток-другий","сотня", "сотка", "тисяча", "п'ятірка", "пара", "третина", "чверть", "половина", 
      "дюжина", "жменя", "жменька", "купа", "купка", "парочка", "оберемок", "безліч");
  //TODO: merge with above?
  static final Pattern ADV_QUANT_PATTERN = Pattern.compile(
      "більше|менше|чимало|багато|мало|забагато|замало|немало|багатенько|чималенько|стільки|обмаль|вдосталь|удосталь|трохи|трошки|досить|достатньо|недостатньо|предостатньо"
      + "|багацько|чимбільше|побільше|порівну|більшість|трішки|предосить|повно|повнісінько"
      + "|мільйон|тисяча|сотня|мільярд|трильйон|десяток|нуль|безліч"
      + "|кілька|декілька|пара|парочка|купа|купка|безліч|мінімум|максимум"
      + ""); // last 2 are numeric so only will play with fully disambiguated tokens
  public static final List<String> MONEY_LEMMAS = Arrays.asList("гривня", "копійка");
  public static final Set<String> TIME_PLUS_LEMMAS = new HashSet<>();
  public static final Pattern TIME_PLUS_LEMMAS_PATTERN;
  public static final List<String> TIME_LEMMAS_SHORT = Arrays.asList("секунда", "хвилина", "година", "рік");

  static final Pattern PART_INSERT_PATTERN = Pattern.compile(
      "бодай|буцім(то)?|геть|дедалі|десь|іще|ледве|мов(би(то)?)?|навіть|наче(б(то)?)?|неначе(бто)?|немов(би(то)?)?|ніби(то)?"
          + "|попросту|просто(-напросто)?|справді|усього-на-всього|хай|хоча?|якраз|ж|би?");
  static final Set<String> PLUS_MINUS = new HashSet<>(Arrays.asList(
      "плюс", "мінус", "максимум", "мінімум"
      ));

  static {
    TIME_PLUS_LEMMAS.addAll(TIME_LEMMAS);
    TIME_PLUS_LEMMAS.addAll(DISTANCE_LEMMAS);
    TIME_PLUS_LEMMAS.addAll(DAYS_OF_WEEK);
    TIME_PLUS_LEMMAS.addAll(MONTH_LEMMAS);
    TIME_PLUS_LEMMAS.addAll(PSEUDO_NUM_LEMMAS);
    TIME_PLUS_LEMMAS.addAll(MONEY_LEMMAS);
    TIME_PLUS_LEMMAS.addAll(Arrays.asList("вихідний", "уїк-енд", "уїкенд", "вікенд",
        "відсоток", "раз", "крок"));
    TIME_PLUS_LEMMAS_PATTERN = Pattern.compile(StringUtils.join(TIME_PLUS_LEMMAS, "|"));
  }
  

  public static boolean hasLemma(AnalyzedTokenReadings analyzedTokenReadings, Collection<String> lemmas) {
    List<AnalyzedToken> readings = analyzedTokenReadings.getReadings();
    return hasLemma(readings, lemmas);
  }

  public static boolean hasLemma(List<AnalyzedToken> readings, Collection<String> lemmas) {
    for(AnalyzedToken analyzedToken: readings) {
      if( lemmas.contains(analyzedToken.getLemma()) ) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasLemma(List<AnalyzedToken> readings, Pattern lemmaRegex) {
    for(AnalyzedToken analyzedToken: readings) {
      if( lemmaRegex.matcher(analyzedToken.getLemma()).matches() ) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasLemma(AnalyzedTokenReadings analyzedTokenReadings, List<String> lemmas, String partPos) {
    for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
      for(String lemma: lemmas) {
        if( lemma.equals(analyzedToken.getLemma()) 
            && analyzedToken.getPOSTag() != null && analyzedToken.getPOSTag().contains(partPos) ) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasLemma(AnalyzedTokenReadings analyzedTokenReadings, Collection<String> lemmas, Pattern posRegex) {
    if( ! analyzedTokenReadings.hasReading() )
      return false;

    for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
      if( analyzedToken.getPOSTag() != null 
          && posRegex.matcher(analyzedToken.getPOSTag()).matches()) {
        
        if( lemmas.contains(analyzedToken.getLemma()) )
          return true;
      }
    }
    return false;
  }

  public static boolean hasLemmaBase(AnalyzedTokenReadings analyzedTokenReadings, Collection<String> lemmas, Pattern posRegex) {
    if( ! analyzedTokenReadings.hasReading() )
      return false;

    for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
      if( analyzedToken.getPOSTag() != null 
          && posRegex.matcher(analyzedToken.getPOSTag()).matches()) {

        String lemma = analyzedToken.getLemma();
        if( lemmas.contains(lemma) )
          return true;

        int idx = lemma.indexOf('-');
        if( idx > 2 && idx < lemma.length()-1 
            && lemmas.contains(lemma.substring(0, idx)) )
          return true;
      }
    }
    return false;
  }

  public static boolean hasLemma(AnalyzedTokenReadings analyzedTokenReadings, String lemma) {
    for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
      if( lemma.equals(analyzedToken.getLemma()) ) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasLemma(AnalyzedTokenReadings analyzedTokenReadings, Pattern pattern) {
    for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
      String lemma = analyzedToken.getLemma();
      if( lemma != null && pattern.matcher(lemma).matches() ) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasLemma(AnalyzedTokenReadings analyzedTokenReadings, Pattern pattern, Pattern posTagRegex) {
    for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
      String lemma = analyzedToken.getLemma();
      if( lemma != null && pattern.matcher(lemma).matches()
          && posTagRegex != null && analyzedToken.getPOSTag() != null && posTagRegex.matcher(analyzedToken.getPOSTag()).matches() ) {
        return true;
      }
    }
    return false;
  }

  static boolean reverseSearch(AnalyzedTokenReadings[] tokens, int pos, int depth, Pattern lemma, Pattern postag) {
    return reverseSearchIdx(tokens, pos, depth, lemma, postag) >= 0;
  }
  
  static int reverseSearchIdx(AnalyzedTokenReadings[] tokens, int pos, int depth, Pattern lemma, Pattern postag) {
    for(int i=pos; i>pos-depth && i>=0; i--) {
      if( (lemma == null || hasLemma(tokens[i], lemma))
          && (postag == null || PosTagHelper.hasPosTag(tokens[i], postag)) )
        return i;
    }
    return -1;
  }

  static int forwardLemmaSearchIdx(AnalyzedTokenReadings[] tokens, int pos, int depth, Pattern lemma, Pattern postag) {
    for(int i=pos; i<pos+depth && i<tokens.length; i++) {
      if( (lemma == null || hasLemma(tokens[i], lemma))
          && (postag == null || PosTagHelper.hasPosTag(tokens[i], postag)) )
        return i;
    }
    return -1;
  }

  static boolean forwardPosTagSearch(AnalyzedTokenReadings[] tokens, int pos, String posTag, int maxSkip) {
    for(int i=pos; i < tokens.length && i <= pos + maxSkip; i++) {
      if( PosTagHelper.hasPosTagPart(tokens[i], posTag) )
        return true;
    }
    return false;
  }

  enum Dir {FORWARD, REVERSE}

  private static final Pattern QUOTES = Pattern.compile("[«»„“\u201C]");

  static int tokenSearch(AnalyzedTokenReadings[] tokens, int pos, String posTag, Pattern token, Pattern posTagsToIgnore, Dir dir) {
    int step = dir == Dir.FORWARD ? 1 : -1;

    for(int i = pos; i < tokens.length && i > 0; i += step) {
      AnalyzedTokenReadings currToken = tokens[i];
      if( (posTag == null || PosTagHelper.hasPosTagPart(currToken, posTag)) 
          && (token == null || token.matcher(currToken.getCleanToken()).matches()) )
        return i;

      if( posTagsToIgnore != null ) {
      if( ! PosTagHelper.hasPosTag(currToken, posTagsToIgnore)
          && ! QUOTES.matcher(currToken.getCleanToken()).matches() )
        break;
      }
    }

    return -1;
  }

  static int tokenSearch(AnalyzedTokenReadings[] tokens, int pos, Pattern posTag, Pattern token, Pattern posTagsToIgnore, Dir dir) {
    int step = dir == Dir.FORWARD ? 1 : -1;

    for(int i = pos; i < tokens.length && i > 0; i += step) {
      if( (posTag == null || PosTagHelper.hasPosTag(tokens[i], posTag)) 
          && (token == null || token.matcher(tokens[i].getCleanToken()).matches()) )
        return i;

      if( posTagsToIgnore != null ) {
        if( ! PosTagHelper.hasPosTag(tokens[i], posTagsToIgnore)
            && ! QUOTES.matcher(tokens[i].getCleanToken()).matches() )
          break;
      }
    }

    return -1;
  }

  static boolean revSearch(AnalyzedTokenReadings[] tokens, int startPos, Pattern lemma, String postagRegex) {
    return LemmaHelper.revSearchIdx(tokens, startPos, lemma, postagRegex) != -1;
  }

  static int revSearchIdx(AnalyzedTokenReadings[] tokens, int startPos, Pattern lemma, String postagRegex) {
    if( startPos > 0 && PosTagHelper.hasPosTag(tokens[startPos], "part.*") ) {
      //    if( startPos > 0 && LemmaHelper.hasLemma(tokens[startPos], Arrays.asList("б", "би")) ) {
      startPos -= 1;
    }

    if( startPos > 0 && PosTagHelper.hasPosTag(tokens[startPos], "adv(:.*)?|.*pron.*") ) {
      startPos -= 1;
    }

    if( startPos > 0 && PosTagHelper.hasPosTag(tokens[startPos], "part.*") ) {
      startPos -= 1;
    }

    if( startPos > 0 ) {
      if( lemma != null && ! hasLemma(tokens[startPos], lemma) )
        return -1;
      if( postagRegex != null && ! PosTagHelper.hasPosTag(tokens[startPos], postagRegex) )
        return -1;

      return startPos;
    }

    return -1;
  }

  public static boolean isAllUppercaseUk(String word) {
    int sz = word.length();
    for (int i = 0; i < sz; i++) {
        char ch = word.charAt(i);
        if (ch != '-' && ch != '\u2013' && ch != '\'' && ch != '\u0301' && ch != '\u00AD' 
            && !Character.isUpperCase(ch)) {
          return false;
        }
    }
    return true;
  }

  public static String capitalizeProperName(String word) {
    char[] chars = new char[word.length()];
    char prevChar = '-';
    for(int i=0; i<chars.length; i++) {
      char ch = word.charAt(i);
      chars[i] = prevChar == '-' ? ch : Character.toLowerCase(ch);
      prevChar = ch == '\u2013' ? '-' : ch;
    }
    return new String(chars);
  }

  public static boolean isCapitalized(String word) {
    if( word == null || word.length() < 2 )
      return false;

    char char0 = word.charAt(0);
    if( ! Character.isUpperCase(char0) )
      return false;
    
    // lax on Latin: EuroGas
    if( char0 >= 'A' && char0 <= 'Z' && Character.isLowerCase(word.charAt(1)) )
      return true;

    boolean prevDash = false;
    int sz = word.length();
    for (int i = 1; i < sz; i++) {
        char ch = word.charAt(i);
        
        if( IGNORE_CHARS.indexOf(ch) >= 0 )
          continue;

        boolean dash = ch == '-' || ch == '\u2013';
        if( dash ) {
          if( i == sz-2 && Character.isDigit(word.charAt(i+1)) )
            return true;

          prevDash = dash;
          continue;
        }

        if( ch != '\'' && ch != '\u0301' && ch != '\u00AD'
            && (prevDash != Character.isUpperCase(ch)) ) {
          return false;
        }
        
        prevDash = false;
    }
    return true;
  }

//public static boolean isInitial(String token) {
//  return token.matches("[А-ЯІЇЄҐA-Z]\\.");
//}

  static final Pattern DASHES_PATTERN = Pattern.compile("[\u2010-\u2015-]");
  static final Pattern QUOTES_PATTERN = Pattern.compile("[\\p{Pi}\\p{Pf}]");
//  static final Pattern QUOTES_AND_PARENTH_PATTERN = Pattern.compile("[\\p{Pi}\\p{Pf}()]");


  static boolean isPossiblyProperNoun(AnalyzedTokenReadings analyzedTokenReadings) {
    return // analyzedTokenReadings.getAnalyzedToken(0).hasNoTag() && 
        isCapitalized(analyzedTokenReadings.getCleanToken());
  }

  public static boolean isInitial(AnalyzedTokenReadings analyzedTokenReadings) {
    return analyzedTokenReadings.getCleanToken().endsWith(".")
        && analyzedTokenReadings.getCleanToken().matches("[А-ЯІЇЄҐA-Z]\\.");
  }

  public static boolean isDash(AnalyzedTokenReadings analyzedTokenReadings) {
    return analyzedTokenReadings.getCleanToken() != null
        && DASHES_PATTERN.matcher(analyzedTokenReadings.getCleanToken()).matches();
  }

}
