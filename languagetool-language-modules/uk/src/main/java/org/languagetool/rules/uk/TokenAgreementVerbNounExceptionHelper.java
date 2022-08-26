package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.uk.LemmaHelper.Dir;
import org.languagetool.rules.uk.TokenAgreementVerbNounRule.State;
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.9
 */
public final class TokenAgreementVerbNounExceptionHelper {

  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementVerbNounExceptionHelper.class);

//  private static final Pattern MODALS = Pattern.compile("було|буде|варто|необхідно|треба|потрібно|муси.*|повин(ен|н[оеаі])||готов(ий|[еаі])");
  private static final Pattern MODALS = Pattern.compile("варто|можна|необхідно|треба|потрібно|муси.+|зму[сш].+|(повин|здат)(ен|н[оеаі]|ий)||готов(ий|[еаі])");
  
  private TokenAgreementVerbNounExceptionHelper() {
  }

  public static boolean isException(AnalyzedTokenReadings[] tokens, int verbPos, int nounAdjPos, 
                                    State state, List<VerbInflectionHelper.Inflection> nounInflections, 
                                    List<VerbInflectionHelper.Inflection> verbInflections,
                                    List<AnalyzedToken> nounTokenReadings, 
                                    List<AnalyzedToken> verbTokenReadings) {

    // тренувалися годину
    if( LemmaHelper.hasLemma(tokens[nounAdjPos], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(zna|rod|oru).*"))) {
      logException();
      return true;
    }
    
    // серйозно каже Вадо.
//    if( LemmaHelper.isCapitalized(tokens[nounAdjPos].getCleanToken())
//        && ! PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "prop") ) {
//      logException();
//      return true;
//    }

    // закінчилося 18-го ввечері
    if( tokens[nounAdjPos].getCleanToken().matches("[0-9]+-.+") ) {
      logException();
      return true;
    }

    // запропоновано відділом
    if( PosTagHelper.hasPosTagPart(tokens[verbPos], "impers")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile(".*v_oru.*")) ) {
      logException();
      return true;
    }

    // звалося Подєбради
    if( LemmaHelper.hasLemma(tokens[verbPos], Arrays.asList("зватися", "називатися"))
        && Character.isUpperCase(tokens[nounAdjPos].getCleanToken().charAt(0)) ) {
      logException();
      return true;
    }
    
    // як боротися підприємцям
    if( verbPos > 1 
        && LemmaHelper.hasLemma(tokens[verbPos-1], Arrays.asList("як", "куди", "де", "що", "чого", "чи"))
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|noun).*v_dav.*"))) {
      logException();
      return true;
    }

    // покататися самому
    if( PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && LemmaHelper.hasLemma(tokens[nounAdjPos], Arrays.asList("самий"), Pattern.compile("adj:.:v_dav.*"))) {
      logException();
      return true;
    }

    // сміятися гріх
    if( PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && tokens[nounAdjPos].getCleanToken().equalsIgnoreCase("гріх") ) {
      logException();
      return true;
    }

    // відбудеться наступного дня
    if( nounAdjPos < tokens.length - 1 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|numr).*v_(rod|zna|rod|oru).*"))
        && LemmaHelper.hasLemma(tokens[nounAdjPos+1], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(rod|zna|rod|oru).*"))) {
      // TODO: agrees
      logException();
      return true;
    }

    // відбувається кожні два роки
    // розпочнеться того ж дня
    if( nounAdjPos < tokens.length - 2 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|numr).*v_(rod|zna|rod|oru).*"))
        && (tokens[nounAdjPos+1].getCleanToken().matches("же?")
         || PosTagHelper.hasPosTag(tokens[nounAdjPos+1], Pattern.compile("(adj|numr).*v_(rod|zna|rod|oru).*|number")))
        && LemmaHelper.hasLemma(tokens[nounAdjPos+2], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(rod|zna|rod|oru).*"))) {
      // TODO: agrees
      logException();
      return true;
    }

    // залучити інвестицій на 20—30
    if( nounAdjPos < tokens.length - 2 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("noun.*v_(rod|zna).*"))
        && tokens[nounAdjPos+1].getCleanToken().matches("на|з|із|зо|під")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos+2], Pattern.compile("number|numr.*v_zna.*")) ) {
      logException();
      return true;
    }
    
    // відрізнятись один від одного
    if( nounAdjPos < tokens.length - 2 
        && tokens[nounAdjPos].getCleanToken().matches("один|одна|одне")
        && PosTagHelper.hasPosTagStart(tokens[nounAdjPos+1], "prep")
        && LemmaHelper.hasLemma(tokens[nounAdjPos+2], "один") ) {
      logException();
      return true;
    }

    // вірити один одному
    if( nounAdjPos < tokens.length - 1
        && tokens[nounAdjPos].getCleanToken().matches("один|одна|одне")
        && LemmaHelper.hasLemma(tokens[nounAdjPos+1], "один") ) {
      logException();
      return true;
    }

    // сам на сам
    if( nounAdjPos < tokens.length - 2 
        && tokens[nounAdjPos].getCleanToken().equals("сам")
        && LemmaHelper.hasLemma(tokens[nounAdjPos+1], "на")
        && tokens[nounAdjPos].getCleanToken().equals("сам") ) {
      logException();
      return true;
    }

    // не бачити вам цирку
    if( verbPos > 1 && nounAdjPos < tokens.length - 1
        && tokens[verbPos-1].getCleanToken().toLowerCase().equals("не")
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_dav")
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos+1], "v_rod") ) {
      logException();
      return true;
    }

    // слід проходити людям
    if( verbPos > 1
        && tokens[verbPos-1].getCleanToken().toLowerCase().equals("слід")
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_dav")) {
      logException();
      return true;
    }

    // меншає людей
    if( LemmaHelper.hasLemma(state.verbAnalyzedTokenReadings, Pattern.compile("(по)?меншати|(по)?більшати|стати"), Pattern.compile("verb.*:[sn](:.*|$)"))
        && PosTagHelper.hasPosTag(tokens[state.nounPos], Pattern.compile("(noun|adj).*v_rod.*")) ) {
      logException();
      return true;
    }

    // дай Боже
    if( PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_kly")
        && PosTagHelper.hasPosTagPart(tokens[verbPos], "impr")) {
      logException();
      return true;
    }

    // повинні існувати такі
    if( verbPos > 1
        && MODALS.matcher(tokens[verbPos-1].getCleanToken().toLowerCase()).matches() 
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf") ) { 
//        && ! Collections.disjoint(state.nounAdjInflections, InflectionHelper.getAdjInflections(tokens[verbPos-1].getReadings()))  ) {
//      agrees(verbPos-1, nounAdjPos)
      logException();
      return true;
    }

    // робити здатна
    if( MODALS.matcher(tokens[nounAdjPos].getCleanToken().toLowerCase()).matches() 
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf") ) { 
      logException();
      return true;
    }
    
    // здаватися коаліціанти не збираються
    if( nounAdjPos < tokens.length - 2
        && tokens[nounAdjPos+1].getCleanToken().equalsIgnoreCase("не")
        && PosTagHelper.hasPosTagStart(tokens[nounAdjPos+2], "verb") 
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_naz") 
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf") ) {
      
//      agree(tokens[nounAdjPos+2], tokens[nounAdjPos]);
      
      logException();
      return true;
    }

    // мусить чимось перекрити
    if( nounAdjPos < tokens.length - 1
//        && LemmaHelper.hasLemma(tokens[verbPos], Arrays.asList("мати", "бути", "могти", "мусити"), "verb") 
//        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos+1], Pattern.compile("verb.*?:inf.*"))
        ) {
      
      // agree 
      
      logException();
      return true;
    }

    // не існувало конкуренції
    // не було мізків
    // стане сили
    // TODO: все залежить нас
    if( PosTagHelper.hasPosTagPart(tokens[state.nounPos], "v_rod")
        && PosTagHelper.hasPosTag(state.verbTokenReadings, Pattern.compile("verb.*?(futr|past):(s:3.*|n($|:.+))"))) {
//      Pattern vRodDriverPattern = Pattern.compile("не|скільки|(най)?більше|(най)?менше|(не)?багато|(чи)?мало|трохи", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
//      int xpos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, (String)null, vRodDriverPattern, Pattern.compile("[a-z].*"), Dir.REVERSE);
//      if( xpos >= 0 && xpos >= state.verbPos-3 ) {
        //        if( state.verbPos > 1
        //            && tokens[state.verbPos-1].getCleanToken().toLowerCase().matches("не|скільки|(най)?більше|(най)?менше|багато|мало") ) {
//        cases = new HashSet<>(Arrays.asList("v_rod"));
//      }
      logException();
      return true;
    }

    if( state.verbPos > 1
        && PosTagHelper.hasPosTagPart(tokens[state.nounPos], "v_rod") ) {

      Pattern vRodDriverPattern = Pattern.compile("не|(на)?скільки|(най)?більше|(най)?менше|(не)?багато|(чи|за)?мало|трохи", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
      int xpos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, (String)null, vRodDriverPattern, Pattern.compile("[a-z].*"), Dir.REVERSE);
      if( xpos >= 0 && xpos >= state.verbPos-4 ) {

        // небагато надходить книжок
        // трохи зменшується матерії
//        if ( PosTagHelper.hasPosTag(state.verbTokenReadings, Pattern.compile("verb:.*().*")) ) {
          logException();
          return true;
//        }
      }

//     && state.cases.contains("v_zna") ) {

       // скільки загалом здійснили постановок
//       if( PosTagHelper.hasPosTag(state.verbTokenReadings, Pattern.compile("verb:(im)?perf.*:s:3.*"))) ) {
//      Pattern vRodDriverPattern = Pattern.compile("не|скільки|(най)?більше|(най)?менше|(не)?багато|(чи)?мало|трохи", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
//      int xpos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, (String)null, vRodDriverPattern, Pattern.compile("[a-z].*"), Dir.REVERSE);
//      if( xpos >= 0 && xpos >= state.verbPos-3 ) {
//        //        if( state.verbPos > 1
//        //            && tokens[state.verbPos-1].getCleanToken().toLowerCase().matches("не|скільки|(най)?більше|(най)?менше|багато|мало") ) {
//        logException();
//        return true;
//      }
    }

//    if( PosTagHelper.hasPosTagStart(tokens[nounAdjPos+2], "verb") 
//        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_naz") 
//        && PosTagHelper.hasPosTag(tokens[verbPos], Pattern.compile(":inf")) ) {
//      
////      agree(tokens[nounAdjPos+2], tokens[nounAdjPos]);
//      
//      logException();
//      return true;
//    }
    
    
    
    // в мені наростали впевеність і ...
    if( nounAdjPos < tokens.length - 2
        && PosTagHelper.hasPosTag(tokens[verbPos], Pattern.compile("verb.*:p(:.*)?"))
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], ":v_naz")
        ) {
      
      logException();
      return true;
    }

    // могли займатися структури
    if( verbPos > 1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTagStart(tokens[verbPos-1], "verb")
        // && agree 
        ) {
      
      logException();
      return true;
    }

    // могли б займатися структури
    // має також народитися власна ідея
    // мали змогу оцінити відвідувачі
    if( verbPos > 2
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTagStart(tokens[verbPos-2], "verb")
        && (LemmaHelper.hasLemma(tokens[verbPos-1], Arrays.asList("б","би"))
            || PosTagHelper.hasPosTag(tokens[verbPos-1], Pattern.compile("adv(?!p).*"))
                || LemmaHelper.hasLemma(tokens[verbPos-2], Arrays.asList("мати"), "verb"))
        // && agree 
        ) {
      
      logException();
      return true;
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
