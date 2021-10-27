package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.uk.LemmaHelper.Dir;
import org.languagetool.rules.uk.SearchHelper.Condition;
import org.languagetool.rules.uk.SearchHelper.Match;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.6
 */
public final class TokenAgreementNounVerbExceptionHelper {

  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementNounVerbExceptionHelper.class);

  private static final Set<String> MASC_FEM_SET = extendSet(ExtraDictionaryLoader.loadSet("/uk/masc_fem.txt"), "екс-");
  private static final Pattern INF_ARGREEMENT_PATTERN = Pattern.compile(
      "(не)?(здатний|змушений|з?г[іо]дний|зобов'язаний|повинний|готовий|достойний|покликаний|спроможний|радий|налаштований|зацікавлений|повинно|змога|стан|можна)");
  private static final Collection<String> GEO_QUALIFIERS = new HashSet<>(Arrays.asList(
      "село", "селище", "місто", "містечко", "хутір", "республіка", "держава", "гора", "планета", "мікрорайон", "райцентр", "заповідник", "мис",
      "м.", "с.", "п.", // (н.п.)
      "штат", "округ", "графство", 
      "вірус", "ураган"));

  
  
  private TokenAgreementNounVerbExceptionHelper() {
  }

  public static boolean isException(AnalyzedTokenReadings[] tokens, int nounPos, int verbPos, 
                                    List<VerbInflectionHelper.Inflection> nounInflections, 
                                    List<VerbInflectionHelper.Inflection> verbInflections,
                                    List<AnalyzedToken> nounTokenReadings, 
                                    List<AnalyzedToken> verbTokenReadings) {

    // Любителі фотографувати їжу
    // навичка збиратися швидко (але не «навички»)
    if( PosTagHelper.hasPosTag(tokens[verbPos], PosTagHelper.VERB_INF_PATTERN) ) {
      if( CaseGovernmentHelper.hasCaseGovernment(tokens[nounPos], "v_inf") 
          && ! PosTagHelper.hasPosTagStart(tokens[nounPos], "noun:inanim:p:v_naz") ) {
        logException();
        return true;
      }
      // handled by xml rule
      if( new Match().tokenLine("не сила").mBefore(tokens, nounPos) > 0) {
        logException();
        return true;
      }
      if( new Match().tokenLine("не проти").mBefore(tokens, nounPos) > 0) {
        logException();
        return true;
      }
      if( Arrays.asList("хтось", "дехто").contains(tokens[nounPos].getCleanToken().toLowerCase()) ) {
        logException();
        return true;
      }
      if( Arrays.asList("намагаючись").contains(tokens[verbPos-1].getCleanToken().toLowerCase()) ) {
        logException();
        return true;
      }
    }

    // шкода було, годі буде
    if( PosTagHelper.hasPosTagPart(tokens[nounPos], "predic") 
        && Arrays.asList("було", "буде").contains(tokens[verbPos].getCleanToken()) ) {
      logException();
      return true;
    }

    if( Arrays.asList("правда").contains(tokens[nounPos].getToken().toLowerCase()) ) {
      logException();
      return true;
    }

    // під три чорти відіслати
    if( new Match().tokenLine("під три чорти").mBefore(tokens, nounPos) > 0) {
      logException();
      return true;
    }

    if( new Match().tokenLine("не штука").mBefore(tokens, nounPos) > 0 ) {
      logException();
      return true;
    }

    if( new Match().tokenLine("бісики").mBefore(tokens, nounPos) > 0 ) {
      logException();
      return true;
    }

    // handled by WORDS_WITH_DASH
    if( new Match().tokenLine("будь якого").mAfter(tokens, verbPos) >= 1 ) {
      logException();
      return true;
    }

    if( new Match().tokenLine("не сказати б").mAfter(tokens, verbPos-1) >= 1 ) {
      logException();
      return true;
    }

    // Не проти бізнесмени користуватися
    if( new Match().tokenLine("не проти").mBefore(tokens, verbPos-1) > 0 ) {
      logException();
      return true;
    }

    // handled by xml rule (VONO_IMPERF)
    if( LemmaHelper.hasLemma(tokens[nounPos], Arrays.asList("воно", "решта")) ) {
      if( PosTagHelper.hasPosTagPart(tokens[verbPos], ":impers") ) {
        logException();
        return true;
      }
    }

    // handled by xml rule
    if( LemmaHelper.hasLemma(tokens[verbPos-1], Arrays.asList("Газа")) ) {
      logException();
      return true;
    }
    
    // кандидат в президенти поїхав
    // вона межі люди пішла
    List<String> V_PREZYDENTY_PREP_LIST = Arrays.asList("в", "у", "між", "межи", "поміж", "на");
    if( nounPos > 1
        && PosTagHelper.hasPosTagStart(tokens[nounPos], "noun:anim:p:v_naz") 
        && LemmaHelper.hasLemma(tokens[nounPos-1], V_PREZYDENTY_PREP_LIST) ) {
      logException();
      return true;
    }
    
    // кандидат в народні депутати поїхав
    if( nounPos > 2
        && PosTagHelper.hasPosTagStart(tokens[nounPos], "noun:anim:p:v_naz") 
        && PosTagHelper.hasPosTagStart(tokens[nounPos-1], "adj:p:v_zna:rinanim") 
        && LemmaHelper.hasLemma(tokens[nounPos-2], V_PREZYDENTY_PREP_LIST) ) {
      logException();
      return true;
    }

    // невідомі прізвища, що виглядають, як дієслово: Андрій Качала
    if( LemmaHelper.isCapitalized(tokens[verbPos].getToken())
        && LemmaHelper.isCapitalized(tokens[nounPos].getToken()) ) {
      logException();
      return true;
    }
    
    // на прізвисько Михайло відбулася
    if( nounPos > 1
        && PosTagHelper.hasPosTag(tokens[nounPos], Pattern.compile("noun:anim:.:v_naz:prop:[fl]name.*"))
        && Arrays.asList("ім'я", "прізвище", "прізвисько").contains(tokens[nounPos-1].getCleanToken().toLowerCase()) ) {
      logException();
      return true;
    }
    

    // невідомі прізвища, як іменник
    // Любов Євтушок зауважила
    if( verbPos > 2
        && LemmaHelper.isCapitalized(tokens[nounPos].getToken())
        && LemmaHelper.isCapitalized(tokens[nounPos-1].getToken())
        && ! Collections.disjoint(
            VerbInflectionHelper.getNounInflections(tokens[nounPos-1].getReadings()), 
            verbInflections) ) {
      logException();
      return true;
    }

    // Тарас ЗАКУСИЛО
    if( StringUtils.isAllUpperCase(tokens[verbPos].getToken()) ) {
      logException();
      return true;
    }

    // Збережені Я позбудуться необхідності
    if( nounPos > 1
        && tokens[nounPos].getToken().equals("Я") ) {
      logException();
      return true;
    }

    // а він давай пити горілку
    // а він давай за своє
    if( verbPos > 2 && verbPos < tokens.length - 1
        && tokens[verbPos].getToken().equals("давай") ) {
      logException();
      return true;
    }

    // Ви може образились
    // but not: Як ви може оцінити
    // and not: що ми не може просто так
    if( verbPos > 1 && verbPos < tokens.length-1
        && tokens[verbPos].getToken().equals("може")
        && ! tokens[verbPos-1].getToken().equals("не")
        && ! PosTagHelper.hasPosTag(tokens[verbPos+1], PosTagHelper.VERB_INF_PATTERN)) {
      logException();
      return true;
    }

    // можуть російськомовні громадяни вважатися
    if( nounPos > 1
        && PosTagHelper.hasPosTag(tokens[verbPos], PosTagHelper.VERB_INF_PATTERN) ) {
      int foundIdx = LemmaHelper.reverseSearchIdx(tokens, nounPos-1, 6, INF_ARGREEMENT_PATTERN, null); 
      if( foundIdx >=0 ) {
        if( ! PosTagHelper.hasPosTagStart(tokens[foundIdx], "adj") 
            || ! Collections.disjoint(
                InflectionHelper.getNounInflections(tokens[nounPos].getReadings()), 
                InflectionHelper.getAdjInflections(tokens[foundIdx].getReadings())) ) {
          logException();
          return true;
        }
      }
    }

    // ці громадяни проголосувати готові лише...
    if( verbPos < tokens.length - 1
        && PosTagHelper.hasPosTag(tokens[verbPos], PosTagHelper.VERB_INF_PATTERN) ) {
        int foundIdx = LemmaHelper.forwardLemmaSearchIdx(tokens, verbPos+1, 7, INF_ARGREEMENT_PATTERN, null);
        if( foundIdx >=0 ) {
          if( ! PosTagHelper.hasPosTagStart(tokens[foundIdx], "adj") 
              || ! Collections.disjoint(
                  InflectionHelper.getNounInflections(nounTokenReadings), 
                  InflectionHelper.getAdjInflections(tokens[foundIdx].getReadings())) ) {
            logException();
            return true;
          }
        }
    }

    // як навчила мене бабуся місити тісто
    if( nounPos > 1
        && PosTagHelper.hasPosTag(tokens[verbPos], PosTagHelper.VERB_INF_PATTERN) ) {
      int prevVerbIdx = LemmaHelper.reverseSearchIdx(tokens, nounPos-1, 7, null, Pattern.compile("verb.*")); 
      if( prevVerbIdx >=0 
          && ! Collections.disjoint(
              VerbInflectionHelper.getVerbInflections(tokens[prevVerbIdx].getReadings()), 
              VerbInflectionHelper.getNounInflections(tokens[nounPos].getReadings())) ) {
        logException();
        return true;
      }
    }

    // ці громадяни проголосувати зможуть лише...
    if( verbPos < tokens.length - 1
        && PosTagHelper.hasPosTag(tokens[verbPos], PosTagHelper.VERB_INF_PATTERN) ) {
//      int nextVerbPos = LemmaHelper.forwardLemmaSearchIdx(tokens, verbPos+1, 7, null, Pattern.compile("verb.*"));
      int nextVerbPos = new Match()
          .ignoreInserts()
          .limit(8)
          .target(Condition.postag(Pattern.compile("verb.*")))
          .mAfter(tokens, verbPos+1);
      if( nextVerbPos >=0 
          && ! Collections.disjoint(
              VerbInflectionHelper.getVerbInflections(tokens[nextVerbPos].getReadings()), 
              VerbInflectionHelper.getNounInflections(tokens[nounPos].getReadings())) ) {
        logException();
        return true;
      }
    }

    // — це були невільники
    // — це передбачено
    if( nounPos > 1 && verbPos < tokens.length - 1
        && tokens[nounPos].getToken().equals("це") 
        && LemmaHelper.DASHES_PATTERN.matcher(tokens[nounPos-1].getToken()).matches() ) {
//        && ! Collections.disjoint(verbInflections, TokenAgreementNounVerbRule.getNounInflections(tokens[i+1].getReadings())) ) {
      logException();
      return true;
    }

    // це не передбачено
    if( tokens[nounPos].getToken().equals("це")
        && PosTagHelper.hasPosTagPart(verbTokenReadings, "impers") ) {
      logException();
      return true;
    }

    // 22 льотчики загинуло миттєво
    // два сини народилося там
    if( nounPos > 1
        && PosTagHelper.hasPosTag(tokens[nounPos], Pattern.compile("noun.*:p:v_naz.*"))
        && PosTagHelper.hasPosTag(verbTokenReadings, Pattern.compile("verb.*?past:n.*"))
        && (Pattern.compile("\\d+[234]").matcher(tokens[nounPos-1].getCleanToken()).matches() 
            || Arrays.asList("два", "три", "чотири").contains(tokens[nounPos-1].getCleanToken()) ) ) {
      logException();
      return true;
    }

    // зіркова пара Леброн Джеймс-Дуейн Вейн вирішили вивести
    if( PosTagHelper.hasPosTag(verbTokenReadings, Pattern.compile("verb.*:[fp](:.*|$)")) ) {
      if( new Match()
          .target(Condition.token("пара"))
          .skip(Condition.token(TokenAgreementAdjNounExceptionHelper.CONJ_FOR_PLURAL_PATTERN).negate())
          .mBefore(tokens, nounPos-1) > 0 ) {
        logException();
        return true;
      }
    }

    if( PosTagHelper.hasPosTag(verbTokenReadings, Pattern.compile("verb.*:p(:.*|$)")) ) {

      // Колесніков/Ахметов посилили
      // Олександр Недовєсов / Сергій Стаховський не змогли
      if( nounPos > 2
          && (tokens[nounPos-1].getToken().equals("/")
              || tokens[nounPos-2].getToken().equals("/")) ) {
        logException();
        return true;
      }

      // кефаль, барабуля, хамса не затримуються
      if( nounPos > 2
          && TokenAgreementAdjNounExceptionHelper.CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[nounPos-1].getToken())
          && PosTagHelper.hasPosTag(tokens[nounPos-2], PosTagHelper.NOUN_V_NAZ_PATTERN) ) {
        logException();
        return true;
      }
      
      // його побут, життєва поведінка не можуть
      if( nounPos > 3
          && TokenAgreementAdjNounExceptionHelper.CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[nounPos-2].getToken())
          && PosTagHelper.hasPosTag(tokens[nounPos-3], PosTagHelper.NOUN_V_NAZ_PATTERN) 
          && ! Collections.disjoint(
              InflectionHelper.getAdjInflections(tokens[nounPos-1].getReadings()),
              InflectionHelper.getNounInflections(tokens[nounPos].getReadings())) ) {
        logException();
        return true;
      }

      // моя мама й сестра мешкали
      // каналізація і навіть охорона пропонувалися
      // Ґорбачов і його дружина виглядали
//      int pos0 = LemmaHelper.tokenSearch(tokens, nounPos-1, (String)null, 
//          TokenAgreementAdjNounExceptionHelper.CONJ_FOR_PLURAL_PATTERN, 
//          Pattern.compile("(noun.*?v_naz|adj:.:v_naz|adv|part).*"), Dir.REVERSE);

      int pos0left = new Match()
          .ignoreInserts()
          .limit(7)
          .target(Condition.token(TokenAgreementAdjNounExceptionHelper.CONJ_FOR_PLURAL_PATTERN))
//          .skip(Condition.postag(Pattern.compile("conj.*|.*pron.*")).negate())
          .skip(Condition.postag(Pattern.compile("(noun.*?v_naz|adj:.:v_naz|adv|part).*"))) //,
//              Condition.token(LemmaHelper.QUOTES_AND_PARENTH_PATTERN))
//              Condition.postag(Pattern.compile("conj.*")).negate())
          .mBefore(tokens, nounPos-1);

      int pos0right = pos0left;
      if( pos0left > 0 ) {
        if( isNonPluralA(tokens, pos0left) ) {
          pos0left = -1;
        }
      }

      // і мама, і тато
      if( pos0left > 1 && tokens[pos0left-1].getToken().equals(",") ) {
        pos0left -= 1;
      }
      
      if( pos0left > 1 ) {
        if( pos0right > 2 ) {
          // і та й інша
          if( pos0left < tokens.length - 1
              && LemmaHelper.hasLemma(tokens[pos0right+1], "інший")
              && LemmaHelper.hasLemma(tokens[pos0left-1], "той") ) {
            logException();
            return true;
          }

          // як Німеччина, так і Україна
          if( PosTagHelper.hasPosTagPart(tokens[pos0left-1], "conj") ) {
            pos0left -= 1;
          }

          List<String> osobysto = Arrays.asList("особисто", "зокрема", "загалом");
          
          // він особисто й облдержадміністрація винесли
          if( osobysto.contains(tokens[pos0left-1].getCleanToken()) ) {
            pos0left -= 1;
          }

          // громада, або ти особисто закликаєте
          if( osobysto.contains(tokens[verbPos-1].getCleanToken()) ) {
            logException();
            return true;
          }
          
          // Німеччина (ще демократична) та Росія почали
          if( tokens[pos0left-1].getToken().equals(")") ) {
            logException();
            return true;
          }

          // і уряд (noun+adv), і президент
          if( PosTagHelper.hasPosTag(tokens[pos0left-1], PosTagHelper.NOUN_V_NAZ_PATTERN) ) {
            logException();
            return true;
          }

          // І спочатку Білорусь, а тепер і Україна пішли
          if( verbPos > 6 ) {
            if( PosTagHelper.hasPosTagPart(tokens[pos0left-1], "adv")
                && PosTagHelper.hasPosTagPart(tokens[pos0left-2], "conj") ) {
              pos0left -= 2;
            }
          }

          while( pos0left > 2 && tokens[pos0left-1].getToken().matches("[,»“”\"]") ) {
            pos0left -= 1;
          }
        }


        // моя мама й сестра мешкали
        // noun.*?v_naz is too strict: "єднання з Римом та королівська адміністрація закручували гайки"
        if( PosTagHelper.hasPosTagStart(tokens[pos0left-1], "noun")
            || PosTagHelper.hasPosTagStart(tokens[pos0left-1], "number:latin")  // Микола ІІ
            || (LemmaHelper.isPossiblyProperNoun(tokens[pos0left-1])) ) {
          logException();
          return true;
        }
        // біологічна і ядерна зброя стають товаром
        if( PosTagHelper.hasPosTag(tokens[pos0left-1], PosTagHelper.ADJ_V_NAZ_PATTERN) ) {
          logException();
          return true;
        }
      }

      // Усі розписи, а також архітектура відрізняються
      int pos3 = LemmaHelper.tokenSearch(tokens, verbPos-2, (String)null, Pattern.compile("також"), 
          Pattern.compile("(noun|adj:.:v_naz|adv|part).*"), Dir.REVERSE);
      if( pos3 > 1 ) {
        logException();
        return true;
      }


      // що пачка цигарок, що ковбаса коштують
      // TODO: що Петро Порошенко, що Володимир Зеленський мають
      if( nounPos > 5 ) {
        String lowerCasePrevToken = tokens[nounPos-1].getToken().toLowerCase();
        if( Arrays.asList("що", "не").contains(lowerCasePrevToken)
            && LemmaHelper.tokenSearch(tokens, nounPos-3, (String)null, Pattern.compile("(?iu)"+lowerCasePrevToken), Pattern.compile("(noun|adj).*"), Dir.REVERSE) > nounPos-7 ) {
          logException();
          return true;
        }
      }


      // Бразилія, Мексика, Індія збувають
      int pos1 = LemmaHelper.tokenSearch(tokens, nounPos-1, (String)null, Pattern.compile(","), Pattern.compile("adj.*"), Dir.REVERSE);
      if( pos1 > 1
          && PosTagHelper.hasPosTag(tokens[pos1-1], PosTagHelper.NOUN_V_NAZ_PATTERN)
          // почуття гумору, іронія були притаманні
          || (pos1 > 2
              && PosTagHelper.hasPosTag(tokens[pos1-1], "noun.*:v_rod.*")
              && PosTagHelper.hasPosTag(tokens[pos1-2], PosTagHelper.NOUN_V_NAZ_PATTERN))) {
        logException();
        return true;
      }

      // Мустафа Джемілєв, Рефат Чубаров зможуть
      // А. Кидисюк, В. Загорський відповідають
      if( nounPos > 4
          && LemmaHelper.isCapitalized(tokens[nounPos].getToken())
          //            && (isCapitalized(tokens[nounPos-1].getToken()) || isInitial(tokens[nounPos-1].getToken()))
          && (PosTagHelper.hasPosTagStart(tokens[nounPos-1], "noun:anim") || LemmaHelper.isInitial(tokens[nounPos-1])) 
          && TokenAgreementAdjNounExceptionHelper.CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[nounPos-2].getToken())
          && LemmaHelper.isCapitalized(tokens[nounPos-3].getToken())
          //            && (isCapitalized(tokens[nounPos-4].getToken()) || isInitial(tokens[nounPos-4].getToken())) ) {
          && (PosTagHelper.hasPosTagStart(tokens[nounPos-4], "noun:anim") || LemmaHelper.isInitial(tokens[nounPos-1])) ) {
        logException();
        return true;
      }

      // закордонний депутат і прем'єр Великої Британії Черчиль
      // а також/потім/навіть голова Європейської ради Дональд Туск провели
      // а потім і голова Європейської ради Дональд Туск провели
//      int idx = Match.findBefore(tokens, nounPos-1, 
//          Condition.token(TokenAgreementAdjNounExceptionHelper.CONJ_FOR_PLURAL_PATTERN), 
//          Condition.postag(Pattern.compile("(noun|adj).*?v_(naz|rod).*")),
//          Condition.token(Pattern.compile("і?з|зі|від|на|навіть|також|потім|згодом")),
//          Condition.token(LemmaHelper.QUOTES_AND_PARENT_PATTERN));
      
      int idx = new Match()
        .target(Condition.token(TokenAgreementAdjNounExceptionHelper.CONJ_FOR_PLURAL_PATTERN))
        .ignoreInserts()
        .skip(Condition.postag(Pattern.compile("(noun|adj).*?v_(naz|rod).*")),
            Condition.token(Pattern.compile("і?з|зі|від|на|навіть|також|потім|згодом")) )//,
//            Condition.token(LemmaHelper.QUOTES_AND_PARENTH_PATTERN))
        .mBefore(tokens, nounPos-1);
      
      if( idx > 0 ) {
        if( isNonPluralA(tokens, idx) )
          idx = -1;
      }

      if( idx > 1 
          && (PosTagHelper.hasPosTag(tokens[idx-1], PosTagHelper.NOUN_V_NAZ_PATTERN) 
              || LemmaHelper.isCapitalized(tokens[idx-1].getCleanToken())
              || LemmaHelper.hasLemma(tokens[idx+1], Arrays.asList("навіть", "також", "потім", "згодом"))
              || LemmaHelper.hasLemma(tokens[idx-1], Arrays.asList("потім", "згодом"))) ) {
        logException();
        return true;
      }


      // Швидке заселення земель, вирубування лісів, меліорація призвели
      //        if( pos1 > 2
      //            && PosTagHelper.hasPosTag(tokens[pos1-1], "noun.*:v_rod.*")
      //            && PosTagHelper.hasPosTag(tokens[pos1-2], "noun.*:v_naz.*") ) {
      //          logException();
      //          return true;
      //        }
      
      //        int pos2 = LemmaHelper.tokenSearch(tokens, i-2, (String)null, Pattern.compile("пара"), Pattern.compile("noun.*"), Dir.REVERSE);
      //        if( pos2 > 0
      //            && tokens[pos2].getToken().equalsIgnoreCase("пара") ) {
      //          logException();
      //          return true;
      //        }

      // понад сотня отримали поранення
      if( (PosTagHelper.hasPosTagPart(tokens[nounPos], "numr")
          && ! LemmaHelper.hasLemma(tokens[nounPos], "один"))
          || LemmaHelper.hasLemma(tokens[nounPos], Arrays.asList("сотня", "тисяча", "десяток")) ) {
        logException();
        return true;
      }

      if( nounPos > 1
          && PosTagHelper.hasPosTagPart(tokens[nounPos-1], "number")
          && (! tokens[nounPos-1].getToken().endsWith("1") || tokens[nounPos-1].getToken().endsWith("11")) ) {
        logException();
        return true;
      }

      // 100 чоловік - handled by styling rule
      if( PosTagHelper.hasPosTagPart(tokens[nounPos-1], "num")
          && tokens[nounPos].getToken().equals("чоловік") 
          && LemmaHelper.tokenSearch(tokens, 1, "noun:anim:f:.*", Pattern.compile("жінк[аи]"), Pattern.compile(".*"), Dir.FORWARD) == -1 ) {
        logException();
        return true;
      }


      // 50%+1 акція закріплюються
      // заінтересованість плюс гарна вивіска зіграли злий жарт
      if( nounPos > 1
          && (tokens[nounPos-1].getToken().endsWith("+1") 
              || LemmaHelper.tokenSearch(tokens, verbPos-2, (String)null, Pattern.compile("плюс"), Pattern.compile("(numr|adj).*.:v_naz.*"), Dir.REVERSE) > 0) ) {
        logException();
        return true;
      }

      // Решта 121 депутат висловилися проти
      if( nounPos > 2
          && LemmaHelper.hasLemma(tokens[nounPos-2], "решта") 
          && tokens[nounPos-1].getToken() != null && tokens[nounPos-1].getToken().matches(".+1") ) {
        logException();
        return true;
      }

      // дві групи, кожна виконували просте завдання
      if( nounPos > 2
          && LemmaHelper.hasLemma(tokens[nounPos], "кожний") 
          && PosTagHelper.hasPosTag(verbTokenReadings, Pattern.compile("verb.*(past:p|:p:3).*")) ) {
        logException();
        return true;
      }

      // душ, одеколони, навіть хлорка не допомогли
      if( nounPos > 2
          //            && tokens[verbPos-1].getToken().equals("не")
          && tokens[nounPos-1].getToken().matches("а?ні|жодн.*|навіть") ) {
        logException();
        return true;
      }

      if( nounPos > 2
          && tokens[verbPos-1].getToken().equals("не")
          && LemmaHelper.reverseSearch(tokens, nounPos-1, 5, Pattern.compile("а?ні"), null) ) {
        logException();
        return true;
      }
      
      
      // TODO: Ні світ, ані навіть Європа чекати не будуть
//      if( new Match().tokenLine("ані навіть").mBefore(tokens, nounPos-1) ) {
//        logException();
//        return true;
//      }
      

      if( nounPos > 3
          && tokens[verbPos-1].getToken().equals("не")
          && tokens[nounPos-2].getToken().matches("а?ні")
          && ! Collections.disjoint(
              InflectionHelper.getAdjInflections(tokens[nounPos-1].getReadings()),
              InflectionHelper.getNounInflections(tokens[nounPos].getReadings())) ) {
        logException();
        return true;
      }
    } // verb.*:p


    // Сейм Республіки Польща проігнорував
    if( nounPos > 3
        && PosTagHelper.hasPosTagPart(tokens[nounPos], ":prop")
        && PosTagHelper.hasPosTag(tokens[nounPos-1], Pattern.compile("noun.*:v_rod.*"))
        && (! Collections.disjoint(VerbInflectionHelper.getNounInflections(tokens[nounPos-2].getReadings()), verbInflections)) ) {
//            || PosTagHelper.hasPosTag(tokens[i], "verb.*:impers.*")) ) {
      logException();
      return true;
    }
    
    // комітет порятунку села Оляниця вирішив
    // ?? Творіння братів Люм’єр знало.
    // кандидат у губернатори штату Аризона їхав
    if( nounPos > 1 ) {
      if( PosTagHelper.hasPosTag(tokens[nounPos], Pattern.compile("noun:inanim:[mnf]:v_naz:prop:geo.*"))
          && PosTagHelper.hasPosTag(tokens[nounPos-1], Pattern.compile("noun:inanim:[mnf]:v_(?!naz)(?!.*&pron).*")) ) {
      
//      Condition condition = new Condition() {
//        @Override
//        public boolean matches(AnalyzedTokenReadings analyzedTokenReadings, Context context) {
//          if( context.pos >= 2 && LemmaHelper.hasLemma(context.tokens[context.pos-1], Arrays.asList("в", "у")) )
//            return false;
//          return super.matches(analyzedTokenReadings, context);
//        }
//      };
//      condition.postag = NOUN_V_NAZ_PATTERN;
//      int prevNounPos = new Match()
//          .ignoreInserts()
//          .limit(4)
//          .target(condition)
//          .mBefore(tokens, nounPos-2);
//      
//      if( prevNounPos >= 0 
//          && ! Collections.disjoint(
//              verbInflections, 
//              VerbInflectionHelper.getNounInflections(tokens[prevNounPos].getReadings())) ) {
        logException();
        return true;
      }

      if( LemmaHelper.isPossiblyProperNoun(tokens[nounPos])
          && LemmaHelper.hasLemma(tokens[nounPos-1], GEO_QUALIFIERS) ) {
        logException();
        return true;
      }
    }
    
    // У невизнаній республіці Південна Осетія відбулися вибори
    if( nounPos > 3
        && PosTagHelper.hasPosTagPart(tokens[nounPos], "v_naz:prop")
        && PosTagHelper.hasPosTag(tokens[nounPos-1], "adj:.:v_naz.*")
        && PosTagHelper.hasPosTag(tokens[nounPos-2], Pattern.compile("noun.*:v_(rod|zna|mis).*")) ) {
//        && ! PosTagHelper.hasPosTag(tokens[i-2], "noun.*:v_naz.*") ) {
      logException();
      return true;
    }
    
    // У штатах Техас і Луїзіана запроваджено надзвичайний стан
    // Хоча б межі курорту Східниця визначено?
    if( nounPos > 1
        && PosTagHelper.hasPosTagPart(tokens[nounPos], ":prop")
        && PosTagHelper.hasPosTag(tokens[verbPos], "verb.*:impers.*") ) {
      logException();
      return true;
    }

    // на австралійський штат Вікторія налетів сильний шторм
    if( nounPos > 3
        && PosTagHelper.hasPosTag(tokens[nounPos], Pattern.compile("noun:inanim:.:v_naz:prop.*"))
        && PosTagHelper.hasPosTag(tokens[nounPos-1], Pattern.compile("noun:inanim:.*"))
        && PosTagHelper.hasPosTag(tokens[nounPos-2], Pattern.compile("adj:.*"))
        && PosTagHelper.hasPosTagPart(tokens[nounPos-3], "prep") ) {

      Collection<String> prepGovernedCases = CaseGovernmentHelper.getCaseGovernments(tokens[nounPos-3], IPOSTag.prep.name());
      if( TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[nounPos-1])
            && TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[nounPos-2]) ) {
        logException();
        return true;
      }
    }

    // Угорщина було пішла шляхом
    if( verbPos < tokens.length - 1
        && tokens[verbPos].getCleanToken().equals("було") ) {
        int pos = LemmaHelper.tokenSearch(tokens, verbPos+1, "verb:", null, Pattern.compile("adv.*"), Dir.FORWARD); // PosTagHelper.hasPosTag(tokens[i+1], "verb.*:past.*")
        if( pos >= 0 
            && ! Collections.disjoint(
                VerbInflectionHelper.getNounInflections(tokens[nounPos].getReadings()), 
                VerbInflectionHelper.getVerbInflections(tokens[pos].getReadings())) ) {
          logException();
          return true;
        }
    }

    // клан Рана було знищено
    if( verbPos < tokens.length - 1
        && PosTagHelper.hasPosTagPart(tokens[nounPos], ":prop")
        && tokens[verbPos].getCleanToken().equals("було")
        && PosTagHelper.hasPosTag(tokens[verbPos+1], "verb.*:impers.*") ) {
      logException();
      return true;
    }

    // діагноз дизентерія підтвердився
    // селище Криниця розташувалося
    // TODO: do not ignore: вибори Київрада не завадили
    if( nounPos > 1
        && PosTagHelper.hasPosTag(tokens[nounPos-1], Pattern.compile("noun:inanim:.:v_naz.*"))
        && ! PosTagHelper.hasPosTagPart(tokens[nounPos-1], ":&pron")
        && ! PosTagHelper.hasPosTag(tokens[nounPos], "noun.*pron.*")
        && (! Collections.disjoint(VerbInflectionHelper.getNounInflections(tokens[nounPos-1].getReadings()), verbInflections)) ) {
      logException();
      return true;
    }

    // Прем’єр-міністр повторила у телезверненні
    if( PosTagHelper.hasPosTagPart(nounTokenReadings, "noun:anim:m:v_naz")
        && PosTagHelper.hasPosTag(verbTokenReadings, Pattern.compile("verb.*:f(:.*|$)"))
        && hasMascFemLemma(nounTokenReadings) ) {
      logException();
      return true;
    }
    
    // пора було
    if( Arrays.asList("пора").contains(tokens[nounPos].getCleanToken().toLowerCase()) 
        && Arrays.asList("було").contains(tokens[verbPos].getCleanToken()) ) {
      logException();
      return true;
    }

    // решта забороняються
    List<String> pseudoPluralNouns = Arrays.asList("решта", "частина", "частка", "половина", "третина", "чверть");
    if( pseudoPluralNouns.contains(tokens[nounPos].getCleanToken().toLowerCase())
        && PosTagHelper.hasPosTag(verbTokenReadings, Pattern.compile(".*:[pn](:.*|$)")) ) {
      logException();
      return true;
    }
    
    // більше ніж будь-хто маємо повне право
    if( nounPos > 2
        && LemmaHelper.hasLemma(tokens[nounPos-1], "ніж")) {
      logException();
      return true;
    }

    // моя ти зоре
    if( nounPos > 1
        && tokens[nounPos].getToken().equalsIgnoreCase("ти")
        && PosTagHelper.hasPosTag(tokens[verbPos], Pattern.compile("noun.*?v_kly.*"))) {
      logException();
      return true;
    }

    // вона візьми та й скажи
    if( verbPos < tokens.length-2
        && tokens[verbPos].getToken().equals("візьми")
        && LemmaHelper.hasLemma(tokens[verbPos+1], Arrays.asList("і", "й", "та"))) {
      logException();
      return true;
    }


    // ми в державі Україна маємо права
    int vPos = verbPos;
    if( verbPos > 3
        && PosTagHelper.hasPosTag(tokens[verbPos-1], Pattern.compile("noun:inanim:.:v_naz:prop.*")) ) {

      // в селі Червона Слобода було вирішено
      String token = tokens[nounPos-1].getToken();
      if( LemmaHelper.isCapitalized(token) 
          && PosTagHelper.hasPosTagStart(tokens[nounPos-1], "adj") ) {
        vPos -= 1;
      }

      if( PosTagHelper.hasPosTagStart(tokens[vPos-2], "noun:inanim")
          && PosTagHelper.hasPosTagPart(tokens[vPos-3], "prep") ) {

        Collection<String> prepGovernedCases = CaseGovernmentHelper.getCaseGovernments(tokens[vPos-3], IPOSTag.prep.name());
        if( TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[vPos-2]) ) {
          logException();
          return true;
        }
      }
    }

    // чи готові ми сидіти
    //TODO: ніхто знижувати тарифи на газ і комунальні послуги, зрозуміло, не збирається
    if( nounPos > 1
        && PosTagHelper.hasPosTagPart(tokens[nounPos-1], "adj") 
        && PosTagHelper.hasPosTag(verbTokenReadings, PosTagHelper.VERB_INF_PATTERN)
        && CaseGovernmentHelper.hasCaseGovernment(tokens[nounPos-1], "v_inf")
        && ! Collections.disjoint(InflectionHelper.getAdjInflections(tokens[nounPos-1].getReadings()), 
            InflectionHelper.getNounInflections(nounTokenReadings))) {
      logException();
      return true;
    }

    // тому що, як австрієць маєте
    if( PosTagHelper.hasPosTag(tokens[nounPos], PosTagHelper.NOUN_V_NAZ_PATTERN)
        && LemmaHelper.tokenSearch(tokens, nounPos-1, (String)null, Pattern.compile("[Яя]к"), PosTagHelper.ADJ_V_NAZ_PATTERN, Dir.REVERSE) != -1 ) {
      logException();
      return true;
    } 
    
    return false;
  }

  static boolean isNonPluralA(AnalyzedTokenReadings[] tokens, int pos) {
    // both Cyrillic and Latin :(
    return (tokens[pos].getToken().equals("а") || tokens[pos].getToken().equals("a"))
        && ! LemmaHelper.hasLemma(tokens[pos+1], Arrays.asList("також", "потім", "пізніше"));
  }
  

  private static boolean hasMascFemLemma(List<AnalyzedToken> nounTokenReadings) {
    String token = nounTokenReadings.get(0).getToken();
    if( token.endsWith("олог") || token.endsWith("знавець") )
      return true;
    
    for (AnalyzedToken analyzedToken : nounTokenReadings) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag != null && posTag.contains("noun:anim:m:v_naz") ) {
        String lemma = analyzedToken.getLemma();
        if( isInMascFemSet(lemma)
            || ( lemma.contains("-") && isInMascFemSet(lemma.replaceFirst("-.*", "")) ) )
          return true;
      }
    }
    
    return false;
  }

  private static boolean isInMascFemSet(String lemma) {
    return MASC_FEM_SET.contains(lemma.replace('\u2018', '-'));
  }


  private static Set<String> extendSet(Set<String> loadSet, String string) {
    Set<String> extraSet = loadSet.stream().map(line -> "екс-" + line).collect(Collectors.toSet());
    loadSet.addAll(extraSet);
    return loadSet;
  }


  private static void logException() {
    if( logger.isDebugEnabled() ) {
      StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
      logger.debug("exception: " /*+ stackTraceElement.getFileName()*/ + stackTraceElement.getLineNumber());
    }
  }

}
