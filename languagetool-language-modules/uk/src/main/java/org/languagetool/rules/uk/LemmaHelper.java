package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
  public static final List<String> TIME_LEMMAS = Arrays.asList("секунда", "хвилина", "година", "день", "тиждень", "місяць",
      "рік", "півроку", "десятиліття", "десятиріччя", "століття", "півстоліття", "сторіччя", "півсторіччя", "тисячоліття", "півтисячоліття");
  public static final List<String> TIME_PLUS_LEMMAS = Arrays.asList("секунда", "хвилина", "година", "день", "тиждень", "місяць", 
      "рік", "півроку", "десятиліття", "десятиріччя", "століття", "півстоліття", "сторіччя", "тисячеліття",
      "міліметр", "сантиметр", "метр", "кілометр",
      "відсоток");
      //, "раз", - опрацьовуємо окремо);
  public static final List<String> TIME_LEMMAS_SHORT = Arrays.asList("секунда", "хвилина", "година", "рік");


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

  public static boolean hasLemma(AnalyzedTokenReadings analyzedTokenReadings, List<String> lemmas, Pattern posRegex) {
    if( ! analyzedTokenReadings.hasReading() )
      return false;

    for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
      for(String lemma: lemmas) {
        if( lemma.equals(analyzedToken.getLemma()) 
            && analyzedToken.getPOSTag() != null 
            && posRegex.matcher(analyzedToken.getPOSTag()).matches()) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasLemma(AnalyzedTokenReadings analyzedTokenReadings, String lemmas) {
    for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
      if( lemmas.equals(analyzedToken.getLemma()) ) {
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
      if( (posTag == null || PosTagHelper.hasPosTagPart(tokens[i], posTag)) 
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

  static int tokenSearch(AnalyzedTokenReadings[] tokens, int pos, Pattern posTag, Pattern token, Pattern posTagsToIgnore, Dir dir) {
    int step = dir == Dir.FORWARD ? 1 : -1;

    for(int i = pos; i < tokens.length && i > 0; i += step) {
      if( (posTag == null || PosTagHelper.hasPosTag(tokens[i], posTag)) 
          && (token == null || token.matcher(tokens[i].getCleanToken()).matches()) )
        return i;

      if( ! PosTagHelper.hasPosTag(tokens[i], posTagsToIgnore)
          && ! QUOTES.matcher(tokens[i].getCleanToken()).matches() )
        break;
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

}
