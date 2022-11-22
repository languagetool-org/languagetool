package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.uk.TokenAgreementNumrNounRule.State;
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.8
 */
final class TokenAgreementNumrNounExceptionHelper {
  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementNumrNounExceptionHelper.class);


  public static boolean isException(AnalyzedTokenReadings[] tokens, State state, 
      List<InflectionHelper.Inflection> numrInflections, List<InflectionHelper.Inflection> slaveInflections, 
      List<AnalyzedToken> slaveTokenReadings) {

    String numrTokenLower = state.numrAnalyzedTokenReadings.getCleanToken().toLowerCase();
    AnalyzedTokenReadings nounAnalyzedTokenReadings = tokens[state.nounPos];

    // для багатьох несподіванкою стало
    if( numrTokenLower.matches("багать(ох|ом|ма)|обо(х|м|ма)|(дв|трь|чотирь)о[хм]|скільки(сь)?(-небудь)?|стільки") ) {
      logException();
      return true;
    }

    String nounLowerToken = nounAnalyzedTokenReadings.getCleanToken().toLowerCase();
    // |десятих|сотих|тисячних - done in main rule
    if( nounLowerToken.matches("плюс|мінус|ранку|вечора|ночі|тепла|морозу|родом|зростом|дивом|станом|вагою|слід|типу|формату|вартістю|році|населення") ) {
      logException();
      return true;
    }

    // рази може у два більший
    if( LemmaHelper.hasLemma(nounAnalyzedTokenReadings, Pattern.compile("у?весь|який(сь)?|свій|сам|цей|решта|кількість|вартий|кожний|жодний|менший|більший|вищий|нижчий")) ) {
      logException();
      return true;
    }

    // done in the rule itself
    // дві англійською; сім загальною вартістю
//    if( PosTagHelper.hasPosTag(nounAnalyzedTokenReadings, Pattern.compile("adj:f:v_oru.*")) ) {
//        logException();
//        return true;
//    }

    // хвилин п'ять люди
    // сотні дві персон
    if( state.numrPos > 1 
        && LemmaHelper.hasLemma(tokens[state.numrPos-1], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun.*?.:v_(naz|rod).*"))
//        && PosTagHelper.hasPosTag(tokens[state.numrPos-1], Pattern.compile("noun(?!.*pron).*"))
//        && ! PosTagHelper.hasPosTag(tokens[state.numrPos-1], Pattern.compile("(?!noun).*"))
        ) {
      List<InflectionHelper.Inflection> nounInflections = InflectionHelper.getNounInflections(tokens[state.numrPos-1].getReadings());
      if( ! Collections.disjoint(numrInflections, nounInflections) ) {
        logException();
        return true;
      }
    }
    
    // півтора довгих роки
    if( state.nounPos < tokens.length -1 ) {
      if( state.numrAnalyzedTokenReadings.getCleanToken().matches("(один-|одне-)?півтора|(одна-)?півтори")
          && PosTagHelper.hasPosTag(tokens[state.nounPos], Pattern.compile("adj:p:v_(naz|rod).*"))
          && PosTagHelper.hasPosTag(tokens[state.nounPos+1], Pattern.compile("noun.*?:p:v_naz.*")) 
          ) {
        logException();
        return true;
      }
    }
    
    // хвилин зо п'ять люди
    if( state.numrPos > 2
        && PosTagHelper.hasPosTagStart(tokens[state.numrPos-1], "prep")
        && LemmaHelper.hasLemma(tokens[state.numrPos-2], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun.*?p:v_(naz|rod).*"))
//        && PosTagHelper.hasPosTag(tokens[state.numrPos-2], Pattern.compile("noun(?!.*pron).*"))
//        && ! PosTagHelper.hasPosTag(tokens[state.numrPos-2], Pattern.compile("(?!noun).*"))
        ) {
      List<InflectionHelper.Inflection> nounInflections = InflectionHelper.getNounInflections(tokens[state.numrPos-2].getReadings());
      if( ! Collections.disjoint(numrInflections, nounInflections) ) {
        logException();
        return true;
      }
    }

    // У свої вісімдесят пан Василь
    if( state.numrPos > 2
        && PosTagHelper.hasPosTagStart(tokens[state.numrPos-2], "prep")
        && tokens[state.numrPos-1].getCleanToken().toLowerCase().equals("свої")
        && PosTagHelper.hasPosTag(tokens[state.numrPos], Pattern.compile("numr:p:v_zna.*"))
        && PosTagHelper.hasPosTag(tokens[state.nounPos], Pattern.compile("noun:anim:.:v_naz.*"))
        ) {
      logException();
      return true;
    }
    
    // два провінційного вигляду персонажі
    if( state.nounPos <= tokens.length - 3  
        && PosTagHelper.hasPosTag(tokens[state.nounPos], Pattern.compile("adj:.:v_rod.*"))
        && PosTagHelper.hasPosTag(tokens[state.nounPos+1], Pattern.compile("noun:inanim:.:v_rod(?!.*pron).*"))
        && PosTagHelper.hasPosTag(tokens[state.nounPos+2], Pattern.compile("noun(?!.*pron).*"))
        ) {
      
      String adj1Genders = PosTagHelper.getGenders(tokens[state.nounPos], "adj:.:v_rod.*");
      String noun1Genders = PosTagHelper.getGenders(tokens[state.nounPos+1], "noun:inanim:.:v_rod(?!.*pron).*");
      
      if( adj1Genders.matches(".*["+noun1Genders+"].*") ) {

        List<InflectionHelper.Inflection> nounInflections = InflectionHelper.getNounInflections(tokens[state.nounPos+2].getReadings());
        if( ! Collections.disjoint(numrInflections, nounInflections) ) {
          logException();
          return true;
        }
      }
    }
    
    // handled by another rule
    if( numrTokenLower.endsWith(",5") ) {
      if( Pattern.compile("тон|тис|коп").matcher(nounLowerToken).matches() 
          || ( state.numrPos > 1
              && Pattern.compile("від|до|протягом|[ув]продовж|близько|після|для|більше|менше").matcher(tokens[state.numrPos-1].getCleanToken().toLowerCase()).matches()) ) {
        logException();
        return true;
      }
    }

    // обоє горбаті
    if( numrTokenLower.matches("обоє|двоє|троє|.+еро") ) {
      if( PosTagHelper.hasPosTagAndToken(nounAnalyzedTokenReadings, Pattern.compile("adj:p:v_naz.*"), Pattern.compile(".+і")) ) {
        logException();
        return true;
      }
    }

    // обоє режисери
    if( numrTokenLower.matches("обоє|обидвоє|троє") ) {
      if( PosTagHelper.hasPosTag(nounAnalyzedTokenReadings, Pattern.compile("noun:anim:p:v_naz.*")) ) {
        logException();
        return true;
      }
    }

    // handled in the rule
    // 22 червня
    if( state.number
         && LemmaHelper.hasLemma(tokens[state.nounPos], LemmaHelper.MONTH_LEMMAS, ":m:v_rod")) {
      logException();
      return true;
    }

    // 3 / 4 понеділка
    if( state.numrPos > 2 ) {
      if( tokens[state.numrPos-1].getCleanToken().equals("/") ) {
        logException();
        return true;
      }
    }

    if( state.numrPos > 1 
        && ( LemmaHelper.hasLemma(tokens[state.numrPos-1], Arrays.asList("ч.", "ст.", "п.", "частина", "стаття", "пункт", "підпункт", "абзац", "№", "номер")) 
            || tokens[state.numrPos-1].getCleanToken().equals("№"))
            ) {
        logException();
        return true;
    }

    // двадцять перший; дві соті
    if( PosTagHelper.hasPosTag(nounAnalyzedTokenReadings, Pattern.compile("adj.*&numr.*"))  ) {
      logException();
      return true;
    }

    if( TokenAgreementNumrNounRule.DVA_3_4_PATTERN.matcher(numrTokenLower).matches() || state.number ) {
      // два нових горнятка
      // три вихідних
      if ( PosTagHelper.hasPosTag(tokens[state.nounPos], "adj(?!.*numr).*:p:v_rod.*")
              && (state.nounPos == tokens.length - 1 
                || PosTagHelper.hasPosTag(tokens[state.nounPos+1], "adj(?!.*numr).*:p:v_rod.*|noun.*:p:v_naz.*|prep")
                || ! PosTagHelper.hasPosTag(tokens[state.nounPos+1], "(adj|noun).*")
                || tokens[state.nounPos+1].getCleanToken().matches("[.,:;()«»—–-]|і|й|та"))
              ) {
        logException();
        return true;
      }
          // 2 хворих/злотих...
      if ( tokens[state.nounPos].getCleanToken().toLowerCase().endsWith("их") 
              && PosTagHelper.hasPosTag(tokens[state.nounPos], "noun.*:p:v_rod.*") ) {
        logException();
        return true;
      }
    }

    // сьома вода
    if( numrTokenLower.matches("сьома|дев.яноста") ) {
      if( PosTagHelper.hasPosTag(nounAnalyzedTokenReadings, Pattern.compile("(noun:.*?|adj):[fp]:v_naz.*")) ) {
        logException();
        return true;
      }
    }

    return false;
  }

  private static void logException() {
    if( logger.isDebugEnabled() ) {
      StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
      logger.debug("exception: " /*+ stackTraceElement.getFileName()*/ + stackTraceElement.getLineNumber());
    }
  }

}
