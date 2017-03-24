package org.languagetool.rules.uk;

import java.util.List;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * @since 3.6
 */
public abstract class LemmaHelper {

  public static boolean hasLemma(AnalyzedTokenReadings analyzedTokenReadings, List<String> lemmas) {
    List<AnalyzedToken> readings = analyzedTokenReadings.getReadings();
    return hasLemma(readings, lemmas);
  }

  public static boolean hasLemma(List<AnalyzedToken> readings, List<String> lemmas) {
    for(AnalyzedToken analyzedToken: readings) {
      if( lemmas.contains(analyzedToken.getLemma()) ) {
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

  public static boolean hasLemma(AnalyzedToken token, List<String> asList, String partPos) {
    return asList.contains(token.getLemma())
        && token.getPOSTag() != null && token.getPOSTag().contains(partPos);
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
    for(int i=pos; i>pos-depth && i>=0; i--) {
      if( (lemma == null || hasLemma(tokens[i], lemma))
           && (postag == null || PosTagHelper.hasPosTag(tokens[i], postag)) )
        return true;
    }
    return false;
  }

  static boolean forwardPosTagSearch(AnalyzedTokenReadings[] tokens, int pos, String posTag, int maxSkip) {
    for(int i=pos; i < tokens.length && i <= pos + maxSkip; i++) {
      if( PosTagHelper.hasPosTagPart(tokens[i], posTag) )
        return true;
    }
    return false;
  }

  enum Dir {FORWARD, REVERSE}
  
  private static final Pattern QUOTES = Pattern.compile("[«»„“]");
  
  static int tokenSearch(AnalyzedTokenReadings[] tokens, int pos, String posTag, Pattern token, Pattern posTagsToIgnore, Dir dir) {
    int step = dir == Dir.FORWARD ? 1 : -1;

    for(int i = pos; i < tokens.length && i > 0; i += step) {
      if( (posTag == null || PosTagHelper.hasPosTagPart(tokens[i], posTag)) 
          && (token == null || token.matcher(tokens[i].getToken()).matches()) )
        return i;
      
      if( ! PosTagHelper.hasPosTag(tokens[i], posTagsToIgnore)
          && ! QUOTES.matcher(tokens[i].getToken()).matches() )
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

}
