package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.uk.LemmaHelper.Dir;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.6
 */
public final class TokenAgreementNounVerbExceptionHelper {
  private static Logger logger = LoggerFactory.getLogger(TokenAgreementNounVerbExceptionHelper.class);

  private static final Set<String> MASC_FEM_SET = extendSet(ExtraDictionaryLoader.loadSet("/uk/masc_fem.txt"), "екс-");

  private TokenAgreementNounVerbExceptionHelper() {
  }

  private static boolean isCapitalized(String token) {
    return token != null
        && token.length() > 1
        && Character.isUpperCase(token.charAt(0))
        && Character.isLowerCase(token.charAt(1));
  }

  public static boolean isException(AnalyzedTokenReadings[] tokens, int i,
                                    List<TokenAgreementNounVerbRule.Inflection> nounInflections, List<TokenAgreementNounVerbRule.Inflection> verbInflections,
                                    List<AnalyzedToken> nounTokenReadings, List<AnalyzedToken> verbTokenReadings) {

    // ми ігноруємо inf в Inflection.equals()
//    if( PosTagHelper.hasPosTag(tokens[i], "verb:.*:inf.*") ) {
//      if( CaseGovernmentHelper.hasCaseGovernment(tokens[i-1], "v_inf") ) {
//        logException();
//        return true;
//      }
    //    }

    // невідомі прізвища, що виглядають, як дієслово: Андрій Качала
    if( isCapitalized(tokens[i].getToken())
        && isCapitalized(tokens[i-1].getToken()) ) {
      logException();
      return true;
    }

    // невідомі прізвища, як іменник
    // Любов Євтушок зауважила
    if( i > 2
        && isCapitalized(tokens[i-1].getToken())
        && isCapitalized(tokens[i-2].getToken())
        && ! Collections.disjoint(TokenAgreementNounVerbRule.getNounInflections(tokens[i-2].getReadings()), verbInflections) ) {
      logException();
      return true;
    }

    // Тарас ЗАКУСИЛО
    if( StringUtils.isAllUpperCase(tokens[i].getToken()) ) {
      logException();
      return true;
    }

    // Колесніков/Ахметов посилили
    if( i > 2
        && tokens[i-2].getToken().equals("/") ) {
      logException();
      return true;
    }

    // Збережені Я позбудуться необхідності
    if( i > 2
        && tokens[i-1].getToken().equals("Я") ) {
      logException();
      return true;
    }

    // а він давай пити горілку
    // а він давай за своє
    if( i > 2
        && tokens[i].getToken().equals("давай") ) {
      logException();
      return true;
    }

    // — це були невільники
    if( i > 2
        && tokens[i-1].getToken().equals("це") 
        && tokens[i-2].getToken().matches("[—–-]") ) {
      logException();
      return true;
    }


    // handled by xml rule
    if( LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("воно", "решта")) ) {
      if( PosTagHelper.hasPosTagPart(tokens[i], ":impers") ) {
        logException();
        return true;
      }
    }

    // handled by xml rule
    if( LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("Газа")) ) {
      logException();
      return true;
    }

    
    if( PosTagHelper.hasPosTag(verbTokenReadings, ".*:p(:.*|$)") ) {

        // моя мама й сестра мешкали
        // каналізація і навіть охорона пропонувалися
        // Ґорбачов і його дружина виглядали
        int pos0 = LemmaHelper.tokenSearch(tokens, i-2, (String)null, 
            TokenAgreementAdjNounExceptionHelper.CONJ_FOR_PLULAR_PATTERN, 
            Pattern.compile("(noun.*?v_naz|adj:.:v_naz|adv|part).*"), Dir.REVERSE);
        
        if( pos0 > 1 ) {
          if( pos0 > 2 ) {
            // і та й інша
            if( pos0 < tokens.length - 1
                && LemmaHelper.hasLemma(tokens[pos0+1], "інший")
                && LemmaHelper.hasLemma(tokens[pos0-1], "той") ) {
              logException();
              return true;
            }
            
            // як Німеччина, так і Україна
            if( PosTagHelper.hasPosTagPart(tokens[pos0-1], "conj") ) {
              pos0 -= 1;
            }
            
            // він особисто й облдержадміністрація винесли
            if( LemmaHelper.hasLemma(tokens[pos0-1], "особисто") ) {
              pos0 -= 1;
            }
            
            // Німеччина (ще демократична) та Росія почали
            if( tokens[pos0-1].getToken().equals(")") ) {
              logException();
              return true;
            }

            // І спочатку Білорусь, а тепер і Україна пішли
            if( i > 6 ) {
              if( PosTagHelper.hasPosTagPart(tokens[pos0-1], "adv")
                  && PosTagHelper.hasPosTagPart(tokens[pos0-2], "conj") ) {
                pos0 -= 2;
              }
            }
            
            while( pos0 > 2 && tokens[pos0-1].getToken().matches("[,»“”\"]") ) {
              pos0 -= 1;
            }
          }


          
        // моя мама й сестра мешкали
        // noun.*?v_naz is too strict: "єднання з Римом та королівська адміністрація закручували гайки"
          if( PosTagHelper.hasPosTag(tokens[pos0-1], "noun.*")
              || (tokens[pos0-1].getAnalyzedToken(0).hasNoTag() && isCapitalized(tokens[pos0-1].getToken())) ) {
            logException();
            return true;
          }
          // біологічна і ядерна зброя стають товаром
          if( PosTagHelper.hasPosTag(tokens[pos0-1], "adj:.:v_naz.*") ) {
            logException();
            return true;
          }
        }
        
        // Усі розписи, а також архітектура відрізняються
        int pos3 = LemmaHelper.tokenSearch(tokens, i-2, (String)null, Pattern.compile("також"), 
            Pattern.compile("(noun|adj:.:v_naz|adv|part).*"), Dir.REVERSE);
        if( pos3 > 1 ) {
          logException();
          return true;
        }


        // що пачка цигарок, що ковбаса коштують
        if( i > 6 ) {
          if( LemmaHelper.hasLemma(tokens[i-2], "що")
              && LemmaHelper.tokenSearch(tokens, i-4, (String)null, Pattern.compile("(?iu)що"), Pattern.compile("(noun|adj).*"), Dir.REVERSE) > i-8 ) {
            logException();
            return true;
          }
          if( LemmaHelper.hasLemma(tokens[i-2], "не")
              && LemmaHelper.tokenSearch(tokens, i-4, (String)null, Pattern.compile("(?iu)не"), Pattern.compile("(noun|adj).*"), Dir.REVERSE) > i-8 ) {
            logException();
            return true;
          }
        }


        // Бразилія, Мексика, Індія збувають
        int pos1 = LemmaHelper.tokenSearch(tokens, i-2, (String)null, Pattern.compile(","), Pattern.compile("adj.*"), Dir.REVERSE);
        if( pos1 > 1
            && PosTagHelper.hasPosTag(tokens[pos1-1], "noun.*:v_naz.*")
            // почуття гумору, іронія були притаманні
            || (pos1 > 2
                && PosTagHelper.hasPosTag(tokens[pos1-1], "noun.*:v_rod.*")
                && PosTagHelper.hasPosTag(tokens[pos1-2], "noun.*:v_naz.*"))) {
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
        
        // зіркова пара Леброн Джеймс-Дуейн Вейн вирішили вивести
//        int pos2 = LemmaHelper.tokenSearch(tokens, i-2, (String)null, Pattern.compile("пара"), Pattern.compile("noun.*"), Dir.REVERSE);
//        if( pos2 > 0
//            && tokens[pos2].getToken().equalsIgnoreCase("пара") ) {
//          logException();
//          return true;
//        }
        
        if( (PosTagHelper.hasPosTagPart(tokens[i-1], "numr")
            && ! LemmaHelper.hasLemma(tokens[i-1], "один"))
            || LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("сотня", "тисяча", "десяток")) ) {
          logException();
          return true;
        }
        
        if( i > 2
            && PosTagHelper.hasPosTagPart(tokens[i-2], "number")
            && (! tokens[i-2].getToken().endsWith("1") || tokens[i-2].getToken().endsWith("11")) ) {
          logException();
          return true;
        }

        // 100 чоловік - handled by styling rule
        if( PosTagHelper.hasPosTagPart(tokens[i-2], "num")
            && tokens[i-1].getToken().equals("чоловік") 
            && LemmaHelper.tokenSearch(tokens, 1, "noun:anim:f:.*", Pattern.compile("жінк[аи]"), Pattern.compile(".*"), Dir.FORWARD) == -1 ) {
          logException();
          return true;
        }


        // 50%+1 акція закріплюються
        // заінтересованість плюс гарна вивіска зіграли злий жарт
        if( i > 2
            && (tokens[i-2].getToken().endsWith("+1") 
                || LemmaHelper.tokenSearch(tokens, i-2, (String)null, Pattern.compile("плюс"), Pattern.compile("(numr|adj).*.:v_naz.*"), Dir.REVERSE) > 0) ) {
          logException();
          return true;
        }
    }


    // Сейм Республіки Польща проігнорував
    // творіння братів Люм'єр
    if( i > 3
        && PosTagHelper.hasPosTagPart(tokens[i-1], ":prop")
        && PosTagHelper.hasPosTag(tokens[i-2], Pattern.compile("noun.*:v_rod.*"))
        && (! Collections.disjoint(TokenAgreementNounVerbRule.getNounInflections(tokens[i-3].getReadings()), verbInflections)) ) {
//            || PosTagHelper.hasPosTag(tokens[i], "verb.*:impers.*")) ) {
      logException();
      return true;
    }
    
    // комітет порятунку села Оляниця вирішив
    if( i > 3
        && PosTagHelper.hasPosTagPart(tokens[i-1], "v_naz:prop")
        && PosTagHelper.hasPosTag(tokens[i-2], Pattern.compile("noun.*:v_(rod|zna|mis).*")) ) {
//        && ! PosTagHelper.hasPosTag(tokens[i-2], "noun.*:v_naz.*") ) {
      logException();
      return true;
    }
    
    // У невизнаній республіці Південна Осетія відбулися вибори
    if( i > 4
        && PosTagHelper.hasPosTagPart(tokens[i-1], "v_naz:prop")
        && PosTagHelper.hasPosTag(tokens[i-2], "adj:.:v_naz.*")
        && PosTagHelper.hasPosTag(tokens[i-3], Pattern.compile("noun.*:v_(rod|zna|mis).*")) ) {
//        && ! PosTagHelper.hasPosTag(tokens[i-2], "noun.*:v_naz.*") ) {
      logException();
      return true;
    }
    
    // У штатах Техас і Луїзіана запроваджено надзвичайний стан
    // Хоча б межі курорту Східниця визначено?
    if( i > 2
        && PosTagHelper.hasPosTagPart(tokens[i-1], ":prop")
        && PosTagHelper.hasPosTag(tokens[i], "verb.*:impers.*") ) {
      logException();
      return true;
    }

    // на австралійський штат Вікторія налетів сильний шторм
    if( i > 4
        && PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("noun:inanim:.:v_naz:prop.*"))
        && PosTagHelper.hasPosTag(tokens[i-2], Pattern.compile("noun:inanim:.*"))
        && PosTagHelper.hasPosTag(tokens[i-3], Pattern.compile("adj:.*"))
        && PosTagHelper.hasPosTagPart(tokens[i-4], "prep") ) {

      Collection<String> prepGovernedCases = CaseGovernmentHelper.getCaseGovernments(tokens[i-4], IPOSTag.prep.name());
      if( TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[i-2])
            && TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[i-3]) ) {
        logException();
        return true;
      }
    }

    // Угорщина було пішла шляхом
    if( i < tokens.length - 1
        && tokens[i].getToken().equals("було") ) {
        int pos = LemmaHelper.tokenSearch(tokens, i+1, ":past", null, Pattern.compile("adv.*"), Dir.FORWARD); // PosTagHelper.hasPosTag(tokens[i+1], "verb.*:past.*")
        if( pos >= 0 
            && ! Collections.disjoint(TokenAgreementNounVerbRule.getNounInflections(tokens[i-1].getReadings()), 
                TokenAgreementNounVerbRule.getVerbInflections(tokens[pos].getReadings())) ) {
          logException();
          return true;
        }
    }

    // клан Рана було знищено
    if( i < tokens.length - 1
        && PosTagHelper.hasPosTagPart(tokens[i-1], ":prop")
        && tokens[i].getToken().equals("було")
        && PosTagHelper.hasPosTag(tokens[i+1], "verb.*:impers.*") ) {
      logException();
      return true;
    }

    // діагноз дизентерія підтвердився
    // селище Криниця розташувалося
    if( i > 2
        && PosTagHelper.hasPosTag(tokens[i-2], Pattern.compile("noun:inanim:.:v_naz.*"))
        && ! PosTagHelper.hasPosTagPart(tokens[i-2], ":&pron")
        && ! PosTagHelper.hasPosTag(tokens[i-1], "noun.*pron.*")
        && (! Collections.disjoint(TokenAgreementNounVerbRule.getNounInflections(tokens[i-2].getReadings()), verbInflections)) ) {
      logException();
      return true;
    }

    // Прем’єр-міністр повторила у телезверненні
    if( PosTagHelper.hasPosTagPart(nounTokenReadings, "noun:anim:m:v_naz")
        && PosTagHelper.hasPosTag(verbTokenReadings, "verb.*:f(:.*|$)")
        && hasMascFemLemma(nounTokenReadings) ) {
      logException();
      return true;
    }
    
    // пора було
    if( Arrays.asList("пора").contains(tokens[i-1].getToken().toLowerCase()) 
        && Arrays.asList("було").contains(tokens[i].getToken()) ) {
      logException();
      return true;
    }

    // решта забороняються
    if( Arrays.asList("решта", "частина", "частка", "половина", "третина", "чверть").contains(tokens[i-1].getToken().toLowerCase()) 
        && PosTagHelper.hasPosTag(verbTokenReadings, ".*:[pn](:.*|$)") ) {
      logException();
      return true;
    }
    
    // Решта 121 депутат висловилися проти
    if( i > 3
        && LemmaHelper.hasLemma(tokens[i-3], "решта") 
        && tokens[i-2].getToken() != null && tokens[i-2].getToken().matches(".+1") 
        && PosTagHelper.hasPosTag(verbTokenReadings, ".*:p(:.*|$)") ) {
      logException();
      return true;
    }
    
    // більше ніж будь-хто маємо повне право
    if( i > 3
        && LemmaHelper.hasLemma(tokens[i-2], "ніж")) {
      logException();
      return true;
    }

    // вона візьми та й скажи
    if( i < tokens.length-2
        && tokens[i].getToken().equals("візьми")
        && LemmaHelper.hasLemma(tokens[i+1], Arrays.asList("і", "й", "та"))) {
      logException();
      return true;
    }


    // ми в державі Україна маємо права
    int vPos = i;
    if( i > 3
        && PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("noun:inanim:.:v_naz:prop.*")) ) {

      // в селі Червона Слобода було вирішено
      String token = tokens[i-2].getToken();
      if( token.matches("[А-ЯІЇҐ][а-яіїєґ'].*") && PosTagHelper.hasPosTagPart(tokens[i-2], "adj:") ) {
        vPos -= 1;
      }

      if( PosTagHelper.hasPosTag(tokens[vPos-2], Pattern.compile("noun:inanim:.*"))
          && PosTagHelper.hasPosTagPart(tokens[vPos-3], "prep") ) {

        Collection<String> prepGovernedCases = CaseGovernmentHelper.getCaseGovernments(tokens[vPos-3], IPOSTag.prep.name());
        if( TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[vPos-2]) ) {
          logException();
          return true;
        }
      }
    }

    // не встиг я отямитися
    // Хотів би я подивитися
    int verbPos = LemmaHelper.tokenSearch(tokens, i-2, "verb", null, Pattern.compile("(adv|part).*"), Dir.REVERSE);
    if( verbPos != -1
        && PosTagHelper.hasPosTag(verbTokenReadings, "verb.*:inf.*") 
        && ! Collections.disjoint(TokenAgreementNounVerbRule.getVerbInflections(tokens[verbPos].getReadings()), nounInflections) ) {
      logException();
      return true;
    }

    // чи готові ми сидіти
    if( i > 1
        && PosTagHelper.hasPosTagPart(tokens[i-2], "adj") 
        && PosTagHelper.hasPosTag(verbTokenReadings, "verb.*:inf.*")
        && CaseGovernmentHelper.hasCaseGovernment(tokens[i-2], "v_inf")
        && ! Collections.disjoint(InflectionHelper.getAdjInflections(tokens[i-2].getReadings()), InflectionHelper.getNounInflections(nounTokenReadings))) {
      logException();
      return true;
    }


    // що ми зробити не зможемо
    // це я робити вмію
    // це я робити швидко вмію
    if( i < tokens.length - 1
        && PosTagHelper.hasPosTag(verbTokenReadings, "verb.*:inf.*") ) {
      
      int verbPos2 = LemmaHelper.tokenSearch(tokens, i+1, Pattern.compile("verb.*"), null, Pattern.compile("(adv|part|adj|noun|conj|part|prep).*"), Dir.FORWARD);
      if( verbPos2 != -1
          && ! Collections.disjoint(TokenAgreementNounVerbRule.getVerbInflections(tokens[verbPos2].getReadings()), nounInflections) ) {
        logException();
        return true;
      }
      
      // це ми рахувати не повинні
      // ми проходити зобов'язані
      int adjPos2 = LemmaHelper.tokenSearch(tokens, i+1, "adj", null, Pattern.compile("(adv|part).*"), Dir.FORWARD);
      if( adjPos2 != -1
          && CaseGovernmentHelper.hasCaseGovernment(tokens[adjPos2], "v_inf")
          && ! Collections.disjoint(InflectionHelper.getAdjInflections(tokens[adjPos2].getReadings()), InflectionHelper.getNounInflections(nounTokenReadings)) ) {
        logException();
        return true;
      }
    }

    // тому що, як австрієць маєте
    if( PosTagHelper.hasPosTag(tokens[i-1], "noun.*:v_naz.*")
        && LemmaHelper.tokenSearch(tokens, i-2, (String)null, Pattern.compile("[Яя]к"), Pattern.compile("adj.*"), Dir.REVERSE) != -1 ) {
      logException();
      return true;
    } 
    
    return false;
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
