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
import org.languagetool.rules.uk.RuleException.Type;
import org.languagetool.rules.uk.SearchHelper.Condition;
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

  private static final Pattern MODALS_ADJ = Pattern.compile("змушений|вимушений|повинний|здатний|готовий|ладний|радий");
  
  private TokenAgreementVerbNounExceptionHelper() {
  }

  public static boolean isException(AnalyzedTokenReadings[] tokens, 
                                    State state, List<VerbInflectionHelper.Inflection> verbInflections, 
                                    List<VerbInflectionHelper.Inflection> nounAdjInflections,
                                    List<AnalyzedToken> verbTokenReadings, 
                                    List<AnalyzedToken> nounTokenReadings) {
    
    int verbPos = state.verbPos;
    int nounAdjPos = state.nounPos;

    String cleanTokenLower = tokens[nounAdjPos].getCleanToken().toLowerCase();

    // боротиметься кілька однопартійців
    // входило двоє студентів
    if( (PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("numr.*v_naz.*"))
            || LemmaHelper.hasLemma(tokens[nounAdjPos], LemmaHelper.ADV_QUANT_PATTERN, Pattern.compile("noun.*v_naz.*|adv.*|part.*"))) ) {
      if( PosTagHelper.hasPosTag(state.verbAnalyzedTokenReadings, Pattern.compile(".*:[sn](:.*|$)")) ) {
        logException();
        return true; 
      }
      // буде лежати двоє хворих
      if( verbPos > 1 
          && PosTagHelper.hasPosTag(state.verbAnalyzedTokenReadings, Pattern.compile("verb.*inf.*"))
          && LemmaHelper.hasLemma(tokens[verbPos-1], Pattern.compile("бути|мусити"), Pattern.compile("verb.*(past:n|:s:3).*")) ) {
        logException();
        return true; 
      }
    }

    if( verbPos > 1
        && LemmaHelper.hasLemma(tokens[verbPos], "бути") ) {
      // здатна була
      if( LemmaHelper.hasLemma(tokens[verbPos-1], TokenAgreementVerbNounExceptionHelper.MODALS_ADJ, Pattern.compile("adj:.:v_naz.*")) ) {
          // повинен був випадок передбачити
//          && agrees(tokens[verbPos], state.nounAdjNazInflections, state.nounAdjIndirTokenReadings) ) {
        logException();
        return true; 
      }
    }

    // TODO: temp: коли зможе силою розуму освоїти
    if( LemmaHelper.hasLemma(tokens[verbPos], Pattern.compile("з?могти"))
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], ".*v_oru.*") ) {
      logException();
      return true; 
    }
    
    // чим могла
    if( verbPos > 1
        && LemmaHelper.hasLemma(tokens[verbPos], Pattern.compile("з?могти"))
        && tokens[verbPos-1].getCleanToken().toLowerCase().equals("чим") ) {
      logException();
      return true; 
    }
    
    // хоче маляром
    if( LemmaHelper.hasLemma(tokens[verbPos], "хотіти") 
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_oru") ) {
      logException();
      return true; 
    }

    // має своїм неодмінним наслідком
    if( LemmaHelper.hasLemma(tokens[verbPos], Pattern.compile("мати|маючи|мавши"))
//        && new SearchHelper.Match()
//          .target(new Condition(Pattern.compile("наслідок|результат|принцип|підґрунтя|виток|причина|коріння|ідеал"), Pattern.compile(".*v_oru.*")))
//          .skip(Condition.postag(Pattern.compile("(.*v_oru|part|adv).*")))
//          .limit(4)
//          .mAfter(tokens, nounAdjPos) >= 0
        && PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_oru")
        ) {
      logException();
      return true; 
    }

    // були б іншої думки/такого змісту
    if( LemmaHelper.hasLemma(tokens[verbPos], Pattern.compile("бути"))
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], "(adj|numr).*v_rod.*") ) {
      logException();
      return true; 
    }
    
    // що є сил
    if( verbPos > 1
        && tokens[verbPos-1].getCleanToken().toLowerCase().equals("що")
        && LemmaHelper.hasLemma(tokens[verbPos], Pattern.compile("бути"), Pattern.compile("verb.*(:s:3|past:n).*"))
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], "(adj|noun).*v_rod.*") ) {
      logException();
      return true; 
    }

    // навіщо було город городити
    if( verbPos > 1
        && tokens[verbPos].getCleanToken().toLowerCase().equals("було")
        && tokens[verbPos-1].getCleanToken().toLowerCase().equals("навіщо") ) {
      logException();
      return true; 
    }

    // чесніше було б державний фонд
    if( verbPos > 1
        && tokens[verbPos].getCleanToken().toLowerCase().equals("було")
        && PosTagHelper.hasPosTag(tokens[verbPos-1], "(adv:comp[cs].*|.*predic.*)") ) {
      logException();
      return true; 
    }

    if( verbPos > 2
        && tokens[verbPos].getCleanToken().toLowerCase().equals("було")
        && tokens[verbPos-1].getCleanToken().toLowerCase().matches("би?")
        && PosTagHelper.hasPosTag(tokens[verbPos-2], "(adv:comp[cs].*|.*predic.*)") ) {
      logException();
      return true; 
    }
    
    // квітне притухлий було пафос
    if( verbPos > 1
        && tokens[verbPos].getCleanToken().toLowerCase().equals("було")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile(".*v_naz.*"))      // may be not just for v_naz
        && PosTagHelper.hasPosTag(tokens[verbPos-1], "adj:.:v_naz:&adjp:.*:perf.*") ) { // may be not just for v_naz
      logException();
      return true; 
    }

    // підстрахуватися не зайве
    if( // tokens[verbPos].getCleanToken().toLowerCase().matches("було|буде")
        cleanTokenLower.matches("зайве|резон") ) {
      logException();
      return true; 
    }

    // було всі 90-ті
    if( tokens[verbPos].getCleanToken().toLowerCase().matches("було|буде")
        && LemmaHelper.hasLemma(tokens[nounAdjPos], Arrays.asList("весь"), Pattern.compile(".*v_zna.*")) ) {
      logException();
      return true; 
    }

    // буде видно тільки супутники
    if( tokens[verbPos].getCleanToken().toLowerCase().matches("було|буде")
      && new SearchHelper.Match()
        .target(Condition.postag(Pattern.compile(".*predic.*")))
        .limit(nounAdjPos-verbPos)
        .mAfter(tokens, verbPos+1) >= 0 ) {
      logException();
      return true; 
    }

    // потрібно буде ше склянку
    if( tokens[verbPos].getCleanToken().toLowerCase().matches("було|буде")
      && new SearchHelper.Match()
        .target(Condition.lemma(Pattern.compile("треба|потрібно")))
        .mNow(tokens, verbPos-1) >= 0 ) {
      logException();
      return true; 
    }
    
    // він був талановита людина
    if( tokens[verbPos].getCleanToken().toLowerCase().equals("був") ) {
      if (tokens[nounAdjPos].getCleanToken().toLowerCase().matches("людина|знаменитість")) {
        logException();
        return true; 
      }
      if ( nounAdjPos < tokens.length -1 
          && tokens[nounAdjPos+1].getCleanToken().toLowerCase().matches("людина")) {
        logException();
        return true; 
      }
    }
    
    // Конкурс був десь шість
    if( verbPos > 1
        && tokens[verbPos-1].getCleanToken().toLowerCase().equals("конкурс")
        && LemmaHelper.hasLemma(tokens[verbPos], Pattern.compile("бути"), Pattern.compile("verb.*(:s:3|past:m).*"))
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("num.*")) ) {
      logException();
      return true; 
    }

    // розподілятиметься пропорційно вкладеній праці
    if( nounAdjPos - verbPos > 1 ) {
      Set<String> advReq = CaseGovernmentHelper.getCaseGovernments(tokens[nounAdjPos-1], Pattern.compile("adv(?!p).*"));
      if( advReq.size() > 0 ) {
        for(int ii=verbPos+1; ii<nounAdjPos; ii++) {
          if (TokenAgreementPrepNounRule.hasVidmPosTag(advReq, tokens[ii])) {
            logException();
            return true;
          }
        }
      }
    }

    if( LemmaHelper.PLUS_MINUS.contains(cleanTokenLower) ) {
      logException();
      return true;
    }
    
    // закінчилося 18-го ввечері
    // наслухатися дорогою
    if( cleanTokenLower.matches("[0-9]+-.+|дорогою|толком|дивом|чверть|третину|половину|святая") ) {
      logException();
      return true;
    }

    // сміялася всю дорогу/цілою дорогою
    if( nounAdjPos < tokens.length-1
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("adj:[fn]:v_(zna|oru).*"))
        && LemmaHelper.hasLemma(tokens[nounAdjPos+1], Arrays.asList("дорога", "життя", "міра"), Pattern.compile("noun:inanim:[fn]:v_(zna|oru).*")) ) {
      logException();
      return true;
    }
    
    // запропоновано відділом
    if( PosTagHelper.hasPosTagPart(tokens[verbPos], "impers")
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile(".*v_oru.*")) ) {
      logException();
      return true;
    }

    // займаючись кожен своїми справами
    if( LemmaHelper.hasLemma(tokens[nounAdjPos], Arrays.asList("кожний"), Pattern.compile(".*v_naz.*")) ) {
      logException();
      return true;
    }
    
    // звалося Подєбради
    // TODO: звався українська медицина
    if( LemmaHelper.hasLemma(tokens[verbPos], Arrays.asList("звати", "називати", "зватися", "називатися"))
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

    // V_DAV
    if( PosTagHelper.hasPosTagPart(tokens[nounAdjPos], "v_dav") ) {

      // INF + V_DAV
      if( PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf") ) {

        // як боротися підприємцям
        if( verbPos > 1 
            && LemmaHelper.hasLemma(tokens[verbPos-1], Arrays.asList("як", "куди", "де", "що", "чого", "чи"))) {
          logException();
          return true;
        }
        // Квапитися їй нікуди
        if( nounAdjPos < tokens.length -1
            && tokens[nounAdjPos+1].getCleanToken().toLowerCase().matches("ніколи|нікуди|нічого|нічим|ніде|немає?|не")) {
          logException();
          return true;
        }

        // тут жити мешканцям було б добре
        if( LemmaHelper.hasLemma(tokens[verbPos], Arrays.asList("жити", "сидіти", "судити"))) {
          logException();
          return true;
        }
        // нічим пишатися селянам
        // TODO: нікуди було діватися поліції
        if( verbPos > 1
            && tokens[verbPos-1].getCleanToken().toLowerCase().matches("ніколи|нікуди|нічого|нічим|ніде|де|немає?|не")) {
          logException();
          return true;
        }

        // не бачити вам цирку
        if( verbPos > 1 && nounAdjPos < tokens.length - 1
            && tokens[verbPos-1].getCleanToken().toLowerCase().matches("не|а?ні")
            && PosTagHelper.hasPosTagPart(tokens[nounAdjPos+1], "v_rod") ) {
          logException();
          return true;
        }

        // слід проходити людям
        if( verbPos > 1
            && tokens[verbPos-1].getCleanToken().toLowerCase().matches("слід|снаги|силу")) {
          logException();
          return true;
        }
      }

      // розсміявся брату в обличчя
      if( nounAdjPos < tokens.length - 2
          && tokens[nounAdjPos+1].getCleanToken().toLowerCase().matches("в|у|на|від|під|по|до|і?з|з[іо]|над|з-під|перед|попід|поза|напереріз")
          && PosTagHelper.hasPosTag(tokens[nounAdjPos+2], Pattern.compile("(noun|adj).*"))) {
        logException();
        return true;
      }
      if( nounAdjPos < tokens.length - 1
          && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile(".*v_dav.*"))
          && tokens[nounAdjPos+1].getCleanToken().toLowerCase().matches("назустріч|навперейми|навздогін|услід")) {
        logException();
        return true;
      }

      // закружляли мені десь у тьмі
      if( nounAdjPos < tokens.length - 2
          && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("noun.*?v_dav:&pron:(pers|refl).*"))) {
        logException();
        return true;
      }
    }

    // сміятися гріх
    if( PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
        && tokens[nounAdjPos].getCleanToken().equalsIgnoreCase("гріх") ) {
      logException();
      return true;
    }

    // тренувалися годину
    // працювала рік-два
//    if( LemmaHelper.hasLemmaBase(tokens[nounAdjPos], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(zna|rod|oru).*"))) {
//      logException();
//      return true;
//    }
//
    // відбудеться наступного дня
//    if( nounAdjPos < tokens.length - 1 
//        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|numr).*v_(rod|zna|oru).*|noun.*v_(rod|zna|oru).*numr.*"))
//        && LemmaHelper.hasLemma(tokens[nounAdjPos+1], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(rod|zna|oru).*"))) {
//      logException();
//      return true;
//    }

    // відбувається кожні два роки
    // розпочнеться того ж дня
    if( 
//        nounAdjPos < tokens.length - 2 
//        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|numr).*v_(rod|zna|oru).*"))
//        && (tokens[nounAdjPos+1].getCleanToken().matches("же?")
//         || PosTagHelper.hasPosTag(tokens[nounAdjPos+1], Pattern.compile("(adj|numr).*v_(rod|zna|oru).*|number|noun.*v_(rod|zna|oru).*numr.*")))
//        && LemmaHelper.hasLemma(tokens[nounAdjPos+2], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(rod|zna|oru).*"))) {
      
      new SearchHelper.Match()
          .skip(Condition.postag(Pattern.compile(".*v_(rod|zna|oru).*|part.*|number")))
         .target(Condition.lemma(LemmaHelper.TIME_PLUS_LEMMAS_PATTERN))
         .limit(4)
         .mAfter(tokens, nounAdjPos) > 0 ) {
      
      logException();
      return true;
    }

    // йде три з половиною години
    if( nounAdjPos < tokens.length - 3 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("numr.*v_zna.*"))
//        && tokens[nounAdjPos+1].getCleanToken().matches("з")
//        && tokens[nounAdjPos+2].getCleanToken().matches("половиною")
//        && LemmaHelper.hasLemma(tokens[nounAdjPos+3], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:.:v_(rod|zna).*"))) {
        && new SearchHelper.Match().target(Condition.lemma(LemmaHelper.TIME_PLUS_LEMMAS_PATTERN))
            .limit(4)
            .mAfter(tokens, nounAdjPos+1) > 0 ) {
      logException();
      return true;
    }

    if( new SearchHelper.Match()
        .skip(Condition.postag(Pattern.compile(".*v_oru.*|part.*|adv.*")))
        .target(new Condition(Pattern.compile("мова"), Pattern.compile("noun:inanim:.:v_oru.*")))
        .limit(4)
        .mAfter(tokens, nounAdjPos) > 0 ) {

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

    // меншає людей
    if( LemmaHelper.hasLemma(state.verbAnalyzedTokenReadings, Pattern.compile("(по)?меншати|(по)?більшати|стати"), Pattern.compile("verb.*:[sn](:.*|$)"))
        && PosTagHelper.hasPosTag(tokens[state.nounPos], Pattern.compile("(noun|adj).*v_rod.*")) ) {
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

    // небагато надходить книжок
    // трохи зменшується матерії
    if( state.verbPos > 1
        && PosTagHelper.hasPosTagPart(tokens[state.nounPos], "v_rod") ) {

      Pattern vRodDriverPattern = Pattern.compile("не|(на)?с[кт]ільки|(най)?більше|(най)?менше|(не|за)?багато|(не|чи|за)?мало|трохи|годі|неможливо|а?ніж|вдосталь|купу", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
      int xpos = LemmaHelper.tokenSearch(tokens, state.verbPos-1, (String)null, vRodDriverPattern, Pattern.compile("[a-z].*"), Dir.REVERSE);
      if( xpos >= 0 && xpos >= state.verbPos-4 ) {
          logException();
          return true;
      }
    }
    
    // V + N + V:INF
    // вміємо цим зазвичай користуватися
    if( nounAdjPos < tokens.length - 1
        && CaseGovernmentHelper.hasCaseGovernment(state.verbAnalyzedTokenReadings, PosTagHelper.VERB_ADVP_PATTERN, "v_inf") ) {
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
    
    // ADVP + N + V
    // резюмуючи політик наголосив
    if( nounAdjPos < tokens.length - 1
        && PosTagHelper.hasPosTagStart(tokens[verbPos], "advp") ) {
      int v2pos = LemmaHelper.tokenSearch(tokens, state.nounPos+1, PosTagHelper.VERB_PATTERN, null, Pattern.compile("[a-z].*"), Dir.FORWARD);
      if( v2pos >= 0 && v2pos <= state.nounPos+3 ) { 
        if( agrees(tokens[v2pos], state.nounAdjNazInflections, state.nounAdjIndirTokenReadings) ) { 
          logException();
          return true;
        }
      }
    }
    
    // V + ADVP + N
    // пригадує посміхаючись Аскольд - only: сміючись, посміхаючись; if more generic hides TP
    // TP: знищила існуючи бази даних
    if( verbPos > 1
        && PosTagHelper.hasPosTagStart(tokens[verbPos], "advp")
        && Arrays.asList("посміхаючись", "сміючись").contains(tokens[verbPos].getCleanToken())
        && PosTagHelper.hasPosTagStart(tokens[verbPos-1], "verb")) {
      if( agrees(tokens[verbPos-1], state.nounAdjNazInflections, state.nounAdjIndirTokenReadings) ) { 
        logException();
        return true;
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

      int lookupPos = state.verbPos-1;

      // перестають діяти й розвиватися демократичні
      if( verbPos > 3 ) {
        if( LemmaHelper.hasLemma(tokens[verbPos-1], Arrays.asList("і", "й", "та"))
            && PosTagHelper.hasPosTagPart(tokens[verbPos-2], ":inf") ) {
          lookupPos = verbPos - 3;
        }
      }
      
      int v2pos = LemmaHelper.tokenSearch(tokens, lookupPos, PosTagHelper.VERB_ADVP_PATTERN, null, Pattern.compile("[a-z].*"), Dir.REVERSE);
      if( v2pos >= 0 && v2pos >= state.verbPos-5
          && (CaseGovernmentHelper.hasCaseGovernment(tokens[v2pos], PosTagHelper.VERB_ADVP_PATTERN, "v_inf") 
            || tokens[verbPos].getCleanToken().matches("(по)?їсти")) 
          ) {
        if( agrees(tokens[v2pos], state.nounAdjNazInflections, state.nounAdjIndirTokenReadings) ) {
          logException();
          return true;
        }
        // заважають розвиватися погане управління, війна
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
      // не в змозі приховати офіційна статистика
      if( tokens[verbPos-1].getCleanToken().toLowerCase().matches("змозі|змогу|силі|силах") ) { // але "під силу" + v_dav
        logException();
        return true;
      }

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
    
    // ADJ + бути + N
    if( verbPos > 1
        && LemmaHelper.hasLemma(tokens[verbPos], "бути") 
        && PosTagHelper.hasPosTag(tokens[verbPos-1], "adj:.:v_naz.*")
//        && agrees(tokens[verbPos], tokens[verbPos-1])
        && CaseGovernmentHelper.hasCaseGovernment(tokens[verbPos-1], "v_rod") 
        && PosTagHelper.hasPosTag(tokens[nounAdjPos], Pattern.compile("(adj|noun).*v_rod.*"))
        ) {
      logException();
      return true;
    }

    // V:IMPERS + бути + N
    if( verbPos > 1
        && LemmaHelper.hasLemma(tokens[verbPos], "бути") 
        && PosTagHelper.hasPosTag(tokens[verbPos-1], "verb.*impers.*")
        && agrees(tokens[verbPos-1], state.nounAdjNazInflections, state.nounAdjIndirTokenReadings)
        ) {
      logException();
      return true;
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

  static int isExceptionHardAdjNoun(AnalyzedTokenReadings[] tokens, int i, State state) {

    // понад - very complicated
    // зайнявся понад тисячу справ
//    if( tokens[i].getCleanToken().equals("понад") )
//      continue;

    String cleanTokenLower = tokens[i].getCleanToken().toLowerCase();
    if( cleanTokenLower.matches("[0-9]{4}-.+|нікому|нічому|нічого|нікого|нічим|решту|ніщо") ) {
      return 1;
    }

    if( LemmaHelper.hasLemma(tokens[i], Arrays.asList("сам", "самий", "себе", "один")) ) {
      return 1;
    }

    // висміювати такого роду забобони
    if( i < tokens.length - 1
        && PosTagHelper.hasPosTag(tokens[i], Pattern.compile("adj:m:v_rod.*"))
        && tokens[i+1].getCleanToken().matches("роду|разу|типу|штибу|розміру")
        ) {
      return 1;
    }
    if( i < tokens.length - 1
        && PosTagHelper.hasPosTag(tokens[i], Pattern.compile("(adj|numr):[mp]:v_oru.*"))
        && tokens[i+1].getCleanToken().matches("чином|способом|робом|ходом|шляхом|коштом") ) {
          return 1;
    }
    if( i < tokens.length - 1
        && PosTagHelper.hasPosTag(tokens[i], Pattern.compile("adj:f:v_oru.*"))
        && tokens[i+1].getCleanToken().matches("мірою")
        ) {
      return 1;
    }
    if( i < tokens.length - 1
        && PosTagHelper.hasPosTag(tokens[i], Pattern.compile("adj:f:v_rod.*"))
        && tokens[i+1].getCleanToken().matches("якості|свіжості")
        ) {
      return 1;
    }
    if( i < tokens.length - 1
        && tokens[i+1].getCleanToken().toLowerCase().matches("темпами")
        ) {
      return 1;
    }

    if (new SearchHelper.Match().tokenLine("не те щоб").mNow(tokens, i) == i + 2
        || new SearchHelper.Match().tokenLine("не те що").mNow(tokens, i) == i + 2
        || new SearchHelper.Match().tokenLine("не останньою чергою").mNow(tokens, i) == i + 2)
      return 3;

    if( new SearchHelper.Match().tokenLine("не те, що").mNow(tokens, i) == i + 3 )
      return 4;

    if( new SearchHelper.Match().tokenLine("світ за очі").mNow(tokens, i) == i + 2 )
      return 3;

    if( new SearchHelper.Match().tokenLine("ні світ ні").mNow(tokens, i) == i + 2 )
      return 3;

    if( new SearchHelper.Match().tokenLine("куди очі").mNow(tokens, i) == i + 1 )
      return 3;

    if( new SearchHelper.Match().tokenLine("куди очі").mNow(tokens, i) == i + 1 )
      return 3;

    if( new SearchHelper.Match().tokenLine("станом на").mNow(tokens, i) == i + 1 )
      return 3;

    if( new SearchHelper.Match().tokenLine("страх як").mNow(tokens, i) == i + 1 )
      return 3;

    if( new SearchHelper.Match().tokenLine("жах як").mNow(tokens, i) == i + 1 )
      return 3;

    if( tokens[i-1].getCleanToken().equals("не")
        && tokens[i].getCleanToken().matches("указ|варіант|рідкість")
        ) {
      return 0;
    }

    return -1;
  }

  private static final Pattern PARTS_CANT_SKIP = Pattern.compile("і|й|та|чи|або|але|як|де|куди|наче|ніби|хоч|навіщо|немов|вдвічі|дедалі|щойно|наскільки");
  static int isExceptionSkip(AnalyzedTokenReadings[] tokens, int i) {

    String cleanTokenLower = tokens[i].getCleanToken().toLowerCase();
    
    if( PosTagHelper.hasPosTagAll(tokens[i].getReadings(), Pattern.compile("(part|adv).*"))
       && ! LemmaHelper.ADV_QUANT_PATTERN.matcher(cleanTokenLower).matches()
      && ! PARTS_CANT_SKIP.matcher(cleanTokenLower).matches() ) {
      return 0;
    }
    if( PosTagHelper.hasPosTag(tokens[i].getReadings(), Pattern.compile("part.*"))
       && PosTagHelper.hasPosTagAll(tokens[i].getReadings(), Pattern.compile("(part|conj|adv).*")) 
       && ! PARTS_CANT_SKIP.matcher(cleanTokenLower).matches() ) {
      return 0;
    }

    return -1;
  }

  static RuleException isExceptionVerb(AnalyzedTokenReadings[] tokens, int i, State state) {
    
    if( LemmaHelper.hasLemma(tokens[i], Arrays.asList("мусити")) )
      return new RuleException(Type.exception);
    
    String cleanTokenLower = tokens[i].getCleanToken().toLowerCase();

    if( cleanTokenLower.equals("може") )
      return new RuleException(Type.exception);

    // як є
    if( i > 1 
        && (cleanTokenLower.matches("є") || LemmaHelper.hasLemma(tokens[i], "могти"))
        && tokens[i-1].getCleanToken().equalsIgnoreCase("як")
        ) {
      return new RuleException(Type.exception);
    }
    
    // будь то
    if( i < tokens.length - 2
        && cleanTokenLower.equals("будь")
        && tokens[i+1].getCleanToken().equalsIgnoreCase("то")
        ) {
      return new RuleException(Type.exception);
    }
    
    // вкласти спати Маринку
    if( i > 1 && i < tokens.length - 1
        && tokens[i].getCleanToken().toLowerCase().equals("спати")
        && LemmaHelper.hasLemma(tokens[i-1], Pattern.compile("(по|в)?кла(сти|вши)"))
        ) {
      return new RuleException(Type.skip);
    }
    
    // розпочав був
    if( i > 1 && state != null ) {
      // TODO: merge with unify
      if( cleanTokenLower.matches("був|було")
          && PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("verb.*:past:m.*")) ) {
        return new RuleException(0);
      }
      if( cleanTokenLower.matches("були|було")
          && PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("verb.*:past:p.*")) ) { 
        return new RuleException(0); 
      }
      if( cleanTokenLower.equals("було")
          && PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("verb.*:past:n.*")) ) { 
        return new RuleException(0); 
      }
      if( cleanTokenLower.matches("була|було")
          && PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("verb.*:past:f.*")) ) { 
        return new RuleException(0); 
      }
    }

    if( i > 1
        && cleanTokenLower.matches("було|буде") ) {
      // чути/проголошено було
      if( state != null 
          && PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("verb.*(impers|predic).*")) ) {
        return new RuleException(0); 
      }
      // видно/варто було
//      if( LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("видно", "помітно")) // temporary until new dict
//          || PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile(".*predic.*")) ) {
//        return new RuleException(Type.exception); 
//      }
    }

    return new RuleException(Type.none); 
  }
}