package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.uk.LemmaHelper.Dir;
import org.languagetool.rules.uk.TokenAgreementVerbNounRule.State;
import org.languagetool.rules.uk.VerbInflectionHelper.Inflection;
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.9
 */
public final class TokenAgreementVerbNounExceptionHelper {

  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementVerbNounExceptionHelper.class);

  private static final Pattern VCHYTY_PATTERN = Pattern.compile(".*вч[аи]ти(ся)?");
  private static final Pattern ADV_PREDICT_PATTERN = Pattern.compile("(adv|noninfl:&predic).*");

//  private static final Pattern MODALS = Pattern.compile("було|буде|варто|необхідно|треба|потрібно|муси.*|повин(ен|н[оеаі])||готов(ий|[еаі])");
//  private static final Pattern MODALS = Pattern.compile("варто|можна|необхідно|треба|потрібно|муси.+|зму[сш].+|(повин|здат)(ен|н[оеаі]|ий)||готов(ий|[еаі])");
  
  private TokenAgreementVerbNounExceptionHelper() {
  }

  public static boolean isException(AnalyzedTokenReadings[] tokens, int verbPos, int nounAdjPos, 
                                    State state, List<VerbInflectionHelper.Inflection> verbInflections, 
                                    List<VerbInflectionHelper.Inflection> nounAdjInflections,
                                    List<AnalyzedToken> verbTokenReadings, 
                                    List<AnalyzedToken> nounTokenReadings) {
    
    // серйозно каже Вадо.
//    if( LemmaHelper.isCapitalized(tokens[nounAdjPos].getCleanToken())
//        && ! PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "prop") ) {
//      logException();
//      return true;
//    }

    // закінчилося 18-го ввечері
    // наслухатися дорогою
    if( tokens[nounAdjPos].getCleanToken().matches("[0-9]+-.+|дорогою|толком|дивом|чверть|третину|половину|святая") ) {
      logException();
      return true;
    }

    // сміялася всю дорого
    if( nounAdjPos < tokens.length-1
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("adj:f:v_zna.*"))
        && tokens[nounAdjPos+1].getCleanToken().equals("дорогу") ) {
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

    // тривав довгих десять раундів.
    if( LemmaHelper.hasLemma(tokens[verbPos], Arrays.asList("тривати", "протривати", "йти", "іти", "ходити", "їхати"))
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|numr|noun:inanim).*v_zna.*")) ) {
      logException();
      return true;
    }
    
    // світ за очі
    if( nounAdjPos < tokens.length - 2 
        && tokens[nounAdjPos].getCleanToken().equals("світ")
        && LemmaHelper.hasLemma(tokens[nounAdjPos+1], "за")
        && tokens[nounAdjPos+2].getCleanToken().equals("очі") ) {
      logException();
      return true;
    }

    // ні сіло ні впало
    if( verbPos > 3 
        && tokens[verbPos].getCleanToken().equalsIgnoreCase("впало")
        && "ні".equals(tokens[verbPos-1].getCleanToken()) ) {
      logException();
      return true;
    }
    // звичайна, якщо не сказати слабка, людина
    if( verbPos > 2
        && tokens[verbPos].getCleanToken().equalsIgnoreCase("сказати")
        && "не".equals(tokens[verbPos-1].getCleanToken())
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_naz") ) {
      logException();
      return true;
    }
    
    
    // потребувала мільйон
    if( state.cases.contains("v_rod")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("numr.*?v_zna.*|noun.*v_zna.*numr.*")) ) {
      logException();
      return true;
    }
    
    // виростили сортів 10
    if( nounAdjPos < tokens.length - 1
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(noun|adj):.*:v_rod.*")) 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos+1], Pattern.compile("num.*"))
        ) {
      logException();
      return true;
    }
    
    // виростили сортів — 10
    if( nounAdjPos < tokens.length - 2
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(noun|adj):.*:v_rod.*")) 
        && LemmaHelper.isDash(tokens[nounAdjPos+1])
        && PosTagHelper.hasPosTag(tokens[nounAdjPos+2], Pattern.compile("num.*"))
        ) {
      logException();
      return true;
    }
    
    // одержав хабарів на суму 10
    if( nounAdjPos < tokens.length - 2
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(noun:inanim|adj):.:v_rod.*")) ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.nounPos+1, (Pattern)null, Pattern.compile("на"), Pattern.compile("[a-z].*"), Dir.FORWARD);
      if( v2pos >= 0 && v2pos <= state.nounPos+5 && v2pos < tokens.length - 1 ) {
        logException();
        return true;
      }
    }

    // залучити інвестицій на 20—30
    if( nounAdjPos < tokens.length - 2 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("noun.*v_(rod|zna).*"))
        && tokens[nounAdjPos+1].getCleanToken().matches("на|з|із|зо|під")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos+2], Pattern.compile("number|numr.*v_zna.*")) ) {
      logException();
      return true;
    }

    // споживає газу менше
    if( nounAdjPos < tokens.length - 1
        &&  PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("noun:.*v_rod.*")) 
        && tokens[nounAdjPos+1].getCleanToken().matches("менше|більше")
        ) {
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
    // Квапитися їй нікуди
    if( nounAdjPos < tokens.length -1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|noun).*v_dav.*"))
        && tokens[nounAdjPos+1].getCleanToken().toLowerCase().matches("ніколи|нікуди|нічого|нічим|ніде|немає?|не")
            ) {
      logException();
      return true;
    }

    // нічим пишатися селянам
    if( verbPos > 1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|noun).*v_dav.*"))
        && tokens[verbPos-1].getCleanToken().toLowerCase().matches("ніколи|нікуди|нічого|нічим|немає?|не")
            ) {
      logException();
      return true;
    }

    // розсміявся брату в обличчя
    if( nounAdjPos < tokens.length - 2
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile(".*v_dav.*"))
        && tokens[nounAdjPos+1].getCleanToken().toLowerCase().matches("в|у|на|від|під|по|до|і?з|з[іо]|над|з-під|перед|попід|поза|напереріз")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos+2], Pattern.compile("(noun|adj).*"))) {
      logException();
      return true;
    }

    // закружляли мені десь у тьмі
    if( nounAdjPos < tokens.length - 2
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("noun.*?v_dav:&pron:(pers|refl).*"))) {
      logException();
      return true;
    }

    if( nounAdjPos < tokens.length - 1
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile(".*v_dav.*"))
        && tokens[nounAdjPos+1].getCleanToken().toLowerCase().matches("назустріч|навперейми|навздогін|услід")) {
      logException();
      return true;
    }

    // сміятися гріх
    if( PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && tokens[nounAdjPos].getCleanToken().equalsIgnoreCase("гріх") ) {
      logException();
      return true;
    }

    // тренувалися годину
    // працювала рік-два
    if( LemmaHelper.hasLemmaBase(tokens[nounAdjPos], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(zna|rod|oru).*"))) {
      logException();
      return true;
    }

    // відбудеться наступного дня
    if( nounAdjPos < tokens.length - 1 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|numr).*v_(rod|zna|oru).*|noun.*v_(rod|zna|oru).*numr.*"))
        && LemmaHelper.hasLemma(tokens[nounAdjPos+1], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(rod|zna|oru).*"))) {
      logException();
      return true;
    }

    // відбувається кожні два роки
    // розпочнеться того ж дня
    if( nounAdjPos < tokens.length - 2 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|numr).*v_(rod|zna|oru).*"))
        && (tokens[nounAdjPos+1].getCleanToken().matches("же?")
         || PosTagHelper.hasPosTag(tokens[nounAdjPos+1], Pattern.compile("(adj|numr).*v_(rod|zna|oru).*|number|noun.*v_(rod|zna|oru).*numr.*")))
        && LemmaHelper.hasLemma(tokens[nounAdjPos+2], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(rod|zna|oru).*"))) {
      logException();
      return true;
    }

    // йде три з половиною години
    if( nounAdjPos < tokens.length - 3 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("numr.*v_zna.*"))
        && tokens[nounAdjPos+1].getCleanToken().matches("з")
        && tokens[nounAdjPos+2].getCleanToken().matches("половиною")
        && LemmaHelper.hasLemma(tokens[nounAdjPos+3], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(rod|zna).*"))) {
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

    // став жовтого кольору
    if( nounAdjPos < tokens.length - 1
        && tokens[nounAdjPos + 1].getCleanToken().toLowerCase().equals("кольору")
        && PosTagHelper.hasPosTagStart(tokens[nounAdjPos], "adj:m:v_rod")) {
      logException();
      return true;
    }

    // дай Боже
    if( PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_kly")
        && PosTagHelper.hasPosTagPart(tokens[verbPos], "impr")) {
      logException();
      return true;
    }

    // повторила прем’єр-міністр у телезверненні
    if( PosTagHelper.hasPosTagPart(nounTokenReadings, "noun:anim:m:v_naz")
        && PosTagHelper.hasPosTag(verbTokenReadings, Pattern.compile("verb.*:f(:.*|$)"))
        && TokenAgreementNounVerbExceptionHelper.hasMascFemLemma(nounTokenReadings) ) {
      logException();
      return true;
    }

    // не існувало конкуренції
    // не було мізків
    // стане сили
    // TODO: все залежить нас
    if( PosTagHelper.hasPosTagPart(tokens[state.nounPos], "v_rod")
        && PosTagHelper.hasPosTag(state.verbTokenReadings, Pattern.compile("verb.*?(futr|past):(s:3.*|n($|:.+))"))) {
      logException();
      return true;
    }

    // небагато надходить книжок
    // трохи зменшується матерії
    if( state.verbPos > 1
        && PosTagHelper.hasPosTagPart(tokens[state.nounPos], "v_rod") ) {

      Pattern vRodDriverPattern = Pattern.compile("не|(на)?с[кт]ільки|(най)?більше|(най)?менше|(не|за)?багато|(не|чи|за)?мало|трохи|годі|неможливо|а?ніж|вдосталь", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
      int xpos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, (String)null, vRodDriverPattern, Pattern.compile("[a-z].*"), Dir.REVERSE);
      if( xpos >= 0 && xpos >= state.verbPos-4 ) {
          logException();
          return true;
      }
    }

    
    // V + N + V:INF
    // вміємо цим зазвичай користуватися
    if( nounAdjPos < tokens.length - 1
        && CaseGovernmentHelper.hasCaseGovernment(state.verbAnalyzedTokenReadings, PosTagHelper.VERB_PATTERN, "v_inf") ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.nounPos+1, PosTagHelper.VERB_PATTERN, null, Pattern.compile("[a-z].*"), Dir.FORWARD);
      if( v2pos >= 0 && v2pos <= state.nounPos+5
          && agrees(tokens[v2pos], state.nounAdjNazInflections, state.nounAdjIndirTokenReadings) 
          ) {
        logException();
        return true;
      }
    }
    
    // V:INF + N + V
    // працювати ці люди не вміють
    // робити прогнозів не буду
    if( nounAdjPos < tokens.length - 1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf") ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.nounPos+1, PosTagHelper.VERB_PATTERN, null, Pattern.compile("[a-z].*"), Dir.FORWARD);
      if( v2pos >= 0 && v2pos <= state.nounPos+4
          && CaseGovernmentHelper.hasCaseGovernment(tokens[v2pos], PosTagHelper.VERB_PATTERN, "v_inf") ) { 
        if( agrees(tokens[v2pos], state.nounAdjNazInflections, state.nounAdjIndirTokenReadings) ) { 
          logException();
          return true;
        }
        if( tokens[v2pos-1].getCleanToken().equals("не") ) { 
          logException();
          return true;
        }
      }
    }

    // V:INF + N + ADV
    // розібратися людям важко
    if( nounAdjPos < tokens.length - 1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf") ) {
      
      if( ! LemmaHelper.hasLemma(tokens[verbPos], VCHYTY_PATTERN) ) {
        int v2pos = LemmaHelper.tokenSearch(tokens, state.nounPos+1, ADV_PREDICT_PATTERN, null, Pattern.compile("[a-z].*"), Dir.FORWARD);
        while( v2pos >= 0 && v2pos <= state.nounPos+4 ) {
          Set<String> cases = CaseGovernmentHelper.getCaseGovernments(tokens[v2pos], ADV_PREDICT_PATTERN);
          if( TokenAgreementPrepNounRule.hasVidmPosTag(cases, state.nounAdjIndirTokenReadings) ) {
            logException();
            return true;
          }
          v2pos = LemmaHelper.tokenSearch(tokens, v2pos+1, ADV_PREDICT_PATTERN, null, Pattern.compile("[a-z].*"), Dir.FORWARD);
        }
      }
    }

    // V:INF + N + ADJ
    // працювати студенти готові
    if( nounAdjPos < tokens.length - 1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf") ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.nounPos+1, PosTagHelper.ADJ_V_NAZ_PATTERN, null, Pattern.compile("[a-z].*"), Dir.FORWARD);
      if( v2pos >= 0 && v2pos <= state.nounPos+3 ) {
        if( CaseGovernmentHelper.hasCaseGovernment(tokens[v2pos], PosTagHelper.ADJ_V_NAZ_PATTERN, "v_inf") ) {
          String genders1 = PosTagHelper.getGenders(tokens[nounAdjPos], "(noun|adj).*v_naz.*");
          String genders2 = PosTagHelper.getGenders(tokens[v2pos], PosTagHelper.ADJ_V_NAZ_PATTERN);
          if( genders1.matches(".*["+genders2+"].*") ) {
            logException();
            return true;
          }
        }
      }
    }

    // V:INF + ADJ
    // працювати неспроможні
    if( PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], PosTagHelper.ADJ_V_NAZ_PATTERN) ) {
      if( CaseGovernmentHelper.hasCaseGovernment(tokens[nounAdjPos], PosTagHelper.ADJ_V_NAZ_PATTERN, "v_inf") ) {
        logException();
        return true;
      }
    }

    // V + V:INF + N
    // дають (змогу з комфортом) мандрувати чотирьом пасажирам
    // заважають розвиватися погане управління, війна
    if( verbPos > 1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, PosTagHelper.VERB_ADVP_PATTERN, null, Pattern.compile("[a-z].*"), Dir.REVERSE);
      if( v2pos >= 0 && v2pos >= state.verbPos-5
          && (CaseGovernmentHelper.hasCaseGovernment(tokens[v2pos], PosTagHelper.VERB_ADVP_PATTERN, "v_inf") 
            || tokens[verbPos].getCleanToken().matches("(по)?їсти")) 
          ) {
        if( agrees(tokens[v2pos], state.nounAdjNazInflections, state.nounAdjIndirTokenReadings) ) {
          logException();
          return true;
        }
        if( PosTagHelper.hasPosTag(tokens[v2pos], Pattern.compile("verb.*:p($|:.*)")) 
            && PosTagHelper.hasPosTag(tokens[state.nounPos], Pattern.compile(".*v_naz.*")) ) {
          logException();
          return true;
        }
      }
    }
    
    // ADV + V:INF + N
    // важко розібратися багатьом людям
    if( verbPos > 1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, ADV_PREDICT_PATTERN, null, Pattern.compile("[a-z].*"), Dir.REVERSE);
      while( v2pos >= 0 && v2pos >= state.verbPos-3 ) {
        if( PosTagHelper.hasPosTag(tokens[v2pos], Pattern.compile("noninfl.&predic.*")) 
            && PosTagHelper.hasPosTagPart(tokens[state.nounPos], "v_naz") 
           ){
          logException();
          return true;
        }
        Set<String> cases = CaseGovernmentHelper.getCaseGovernments(tokens[v2pos], ADV_PREDICT_PATTERN);
        if( TokenAgreementPrepNounRule.hasVidmPosTag(cases, state.nounAdjIndirTokenReadings) ) {
          logException();
          return true;
        }
        v2pos = LemmaHelper.tokenSearch(tokens, v2pos-1, ADV_PREDICT_PATTERN, null, Pattern.compile("[a-z].*"), Dir.REVERSE);
      }
    }
    
    // ADJ + V:INF + N
    // зацікавлена перейняти угорська сторона
    if( verbPos > 1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_naz")
        ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, PosTagHelper.ADJ_V_NAZ_PATTERN, null, Pattern.compile("[a-z].*"), Dir.REVERSE);
      if( v2pos >= 0 && v2pos >= state.verbPos-3 ) {
        if( CaseGovernmentHelper.hasCaseGovernment(tokens[v2pos], PosTagHelper.ADJ_V_NAZ_PATTERN, "v_inf") ) {
          String genders1 = PosTagHelper.getGenders(tokens[nounAdjPos], "(noun|adj|numr).*v_naz.*");
          String genders2 = PosTagHelper.getGenders(tokens[v2pos], PosTagHelper.ADJ_V_NAZ_PATTERN);
          if( genders1.matches(".*["+genders2+"].*") ) {
            logException();
            return true;
          }
        }
      }
    }
    
    // NOUN + V:INF + N
    // гріх зайнятися Генеральній прокуратурі
    // готовність спілкуватися людини
    // небажання вибачатися пов’язане з
    if( verbPos > 1
        && PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && (PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_dav") || PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_rod")
            || PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("adj:.:v_naz.*")))
        ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, PosTagHelper.NOUN_V_NAZ_PATTERN, null, Pattern.compile("[a-z].*"), Dir.REVERSE);
      if( v2pos >= 0 && v2pos >= state.verbPos-3 ) {
        if( CaseGovernmentHelper.hasCaseGovernment(tokens[v2pos], PosTagHelper.NOUN_V_NAZ_PATTERN, "v_inf") ) {
//            && CaseGovernmentHelper.hasCaseGovernment(tokens[v2pos], PosTagHelper.NOUN_V_NAZ_PATTERN, "v_dav") ) {

          // exc: бажання вчитися новому
          if( PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_dav")
              && LemmaHelper.hasLemma(tokens[verbPos], Pattern.compile(".*вчити(ся)?")) ) {
            return false;
          }
          
          logException();
          return true;

//          if( PosTagHelper.hasPosTagStart(tokens[nounAdjPos], "noun") ) {
//            logException();
//            return true;
//          }
//          if( PosTagHelper.hasPosTagStart(tokens[nounAdjPos], "adj") ) {
//            List<InflectionHelper.Inflection> adjInflections = InflectionHelper.getAdjInflections(tokens[nounAdjPos].getReadings());
//            List<InflectionHelper.Inflection> nounInflections = InflectionHelper.getNounInflections(tokens[nounAdjPos].getReadings());
//            if( ! Collections.disjoint(adjInflections, nounInflections) ) {
//              logException();
//              return true;
//            }
//          }
        }
      }
    }
    
    // V:INF + V + N
    // платити доведеться повну вартість
    if( verbPos > 1
        && CaseGovernmentHelper.hasCaseGovernment(state.verbAnalyzedTokenReadings, PosTagHelper.VERB_PATTERN, "v_inf") ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, PosTagHelper.VERB_PATTERN, null, Pattern.compile("[a-z].*"), Dir.REVERSE);
      if( v2pos >= 0 && v2pos >= state.verbPos-3
          && PosTagHelper.hasPosTagPart(tokens[v2pos], ":inf")
          && agrees(tokens[v2pos], state.nounAdjNazInflections, state.nounAdjIndirTokenReadings) 
          ) {
        logException();
        return true;
      }
    }
    
    
    // в мені наростали впевеність і ...
    if( nounAdjPos < tokens.length - 2
        && PosTagHelper.hasPosTag(tokens[verbPos], Pattern.compile("verb.*:p(:.*)?"))
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], ":v_naz")
        ) {
      logException();
      return true;
    }

    // змалював дивовижної краси церкву
    if( nounAdjPos < tokens.length - 2
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("adj:.:v_rod(?!.*pron).*"))
        && PosTagHelper.hasPosTag(tokens[nounAdjPos+1], Pattern.compile("noun:.*v_rod(?!.*pron).*"))
        && PosTagHelper.hasPosTag(tokens[nounAdjPos+2], Pattern.compile("(noun|adj)(?!.*pron).*")) ) {
      
      List<AnalyzedToken> readings = tokens[nounAdjPos+2].getReadings();
      List<AnalyzedToken> readingsVnaz = readings.stream().filter(r -> PosTagHelper.hasPosTagPart(r, "v_naz")).collect(Collectors.toList());
      List<Inflection> nounAdjNazInflectionsVnaz = VerbInflectionHelper.getNounInflections(readingsVnaz);
      nounAdjNazInflectionsVnaz.addAll(VerbInflectionHelper.getAdjInflections(readingsVnaz));

      List<AnalyzedToken> readingsIndir = readings.stream().filter(r -> ! PosTagHelper.hasPosTagPart(r, "v_naz")).collect(Collectors.toList());
//      List<org.languagetool.rules.uk.InflectionHelper.Inflection> nounAdjInflectionsIndir = InflectionHelper.getNounInflections(readingsIndir);
//      nounAdjInflectionsIndir.addAll(InflectionHelper.getAdjInflections(state.nounAdjIndirTokenReadings));
//      nounAdjInflectionsIndir.addAll(InflectionHelper.getNumrInflections(state.nounAdjIndirTokenReadings));
      
      if( agrees(tokens[verbPos], nounAdjNazInflectionsVnaz, readingsIndir) ) {
        logException();
        return true;
      }
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


  private static boolean agrees(AnalyzedTokenReadings verbTokenReadings, List<Inflection> nounAdjNazInflections, List<AnalyzedToken> nounAdjIndirTokenReadings) {

    if( nounAdjNazInflections != null && nounAdjNazInflections.size() > 0 ) {
      List<VerbInflectionHelper.Inflection> verbInflections = VerbInflectionHelper.getVerbInflections(verbTokenReadings.getReadings());
      if (! Collections.disjoint(verbInflections, nounAdjNazInflections))
        return true;
    }

    if( nounAdjIndirTokenReadings.size() > 0 ) {
      Set<String> cases = CaseGovernmentHelper.getCaseGovernments(verbTokenReadings, PosTagHelper.VERB_ADVP_PATTERN);
      if( cases.size() > 0 && TokenAgreementPrepNounRule.hasVidmPosTag(cases, nounAdjIndirTokenReadings) )
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
