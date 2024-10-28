package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.uk.RuleException.Type;
import org.languagetool.rules.uk.SearchHelper.Condition;
import org.languagetool.rules.uk.SearchHelper.Match;
import org.languagetool.tagging.uk.PosTagHelper;
import org.languagetool.rules.uk.TokenAgreementPrepNounRule.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenAgreementPrepNounExceptionHelper {
  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementPrepNounExceptionHelper.class);

  private static final Set<String> NAMES = new HashSet<>(Arrays.asList(
      "ім'я", "прізвище"
      ));
  
  public static RuleException getExceptionInfl(AnalyzedTokenReadings[] tokens, int i, State state) {
    AnalyzedTokenReadings tokenReadings = tokens[i];
    String token = tokenReadings.getCleanToken();
    String tokenLower = token.toLowerCase();
    String prep = state.prepTokenReadings.getCleanToken().toLowerCase();

    
    // на дивом уцілілій техніці
    if( tokenReadings.getToken().equals("дивом") )
      return new RuleException(0);

    // в тисяча шістсот якомусь році
    if( i < tokens.length - 1 
        && tokenReadings.getToken().equals("тисяча")
        && PosTagHelper.hasPosTagPart(tokens[i+1], "numr")) {
      return new RuleException(0);
    }
    // в дев'яносто восьмому
    if( i < tokens.length - 1 
        //tokenReadings.getToken().equals("тисяча")
        && PosTagHelper.hasPosTagPart(tokenReadings, "numr") && PosTagHelper.hasPosTagPart(tokenReadings, "v_naz")
        && PosTagHelper.hasPosTagPart(tokens[i+1], "numr") && PosTagHelper.hasPosTag(tokenReadings, Pattern.compile(".*v_(rod|dav|zna|oru|mis).*")) ) {
      return new RuleException(1);
    }

    if (prep.equals("на")) {
      // 1) на (свято) Купала, на (вулиці) Мазепи, на (вулиці) Тюльпанів
      if (LemmaHelper.isCapitalized(token) && PosTagHelper.hasPosTag(tokenReadings, Pattern.compile("noun.*?:.:v_rod.*"))) {
        return new RuleException(Type.exception);
      } 
      if (PosTagHelper.hasPosTag(tokenReadings, Pattern.compile("noun:anim:.:v_naz:prop:[fl]name.*"))
          && ((i > 1 && NAMES.contains(tokens[i-2].getAnalyzedToken(0).getToken()))
              || (i > 2 && NAMES.contains(tokens[i-3].getAnalyzedToken(0).getLemma())))) {
        return new RuleException(Type.exception);
      }

      // handled by xml rule
      if( token.equals("манер") ) {
        return new RuleException(Type.exception);
      }
      // на біс (TODO: можливо краще tag=intj?)
      if( tokenLower.equals("біс") ) {
        return new RuleException(Type.exception);
      }
    }

    // TODO: temporary until we have better logic - skip
    // при їх виборі
    if( prep.equals("при") ) {
      if( token.equals("їх") ) {
        return new RuleException(Type.skip);
      }
    }
    else
      if( prep.equals("з") ) {
        if( token.equals("рана") ) {
          return new RuleException(Type.exception);
        }
      }
      else
        if( prep.equals("від") ) {
          if( token.equalsIgnoreCase("а") || token.equals("рана") || token.equals("корки") || token.equals("мала") ) {  // корки/мала ловиться іншим правилом
            return new RuleException(Type.exception);
          }
        }
        else if( prep.equals("до") ) {
          if( token.equalsIgnoreCase("я") || token.equals("корки") || token.equals("велика") ) {  // корки/велика ловиться іншим правилом
            return new RuleException(Type.exception);
          }
        }

    
    // exceptions
    if( tokens.length > i+1 ) {
      //      if( tokens.length > i+1 && Character.isUpperCase(tokenReadings.getAnalyzedToken(0).getToken().charAt(0))
      //        && hasRequiredPosTag(Arrays.asList("v_naz"), tokenReadings)
      //        && Character.isUpperCase(tokens[i+1].getAnalyzedToken(0).getToken().charAt(0)) )
      //          continue; // "у Конан Дойла", "у Робін Гуда"

      // по Пенсильванія авеню
//      if( LemmaHelper.isCapitalized( token ) 
//          && LemmaHelper.CITY_AVENU.contains( tokens[i+1].getAnalyzedToken(0).getToken().toLowerCase() ) ) {
//        return new RuleException(Type.exception);
//      }

      // від мінус 1 до плюс 1
      if( (PosTagHelper.hasPosTagStart(tokens[i+1], "num")
            || tokens[i+1].getToken().equals("$"))
          && LemmaHelper.PLUS_MINUS.contains(tokenLower) ) {
        return new RuleException(Type.exception);
      }

      // на мохом стеленому дні - пропускаємо «мохом»
      if( PosTagHelper.hasPosTag(tokenReadings, "noun.*?:v_oru.*")
          && tokens[i+1].hasPartialPosTag("adjp:pasv") ) {
        return new RuleException(1);
      }

      if( token.equals("святая")
          && tokens[i+1].getToken().equals("святих") ) {
        return new RuleException(Type.exception);
      }

      if( (prep.equals("через") || prep.equals("на"))  // років 10, відсотки 3-4
          && (//PosTagHelper.hasPosTagStart(tokenReadings, "noun:inanim:p:v_naz") 
//              || PosTagHelper.hasPosTagStart(tokenReadings, "noun:inanim:p:v_rod")) // token.equals("років")
              LemmaHelper.hasLemma(tokenReadings, LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:p:v_(rod|zna).*")))
          && (tokens[i+1].hasPartialPosTag("num") // IPOSTag.isNum(tokens[i+1].getAnalyzedToken(0).getPOSTag())
              // відсотки зо 3
              || (i<tokens.length-2
                  && LemmaHelper.hasLemma(tokens[i+1], Arrays.asList("зо", "з", "із"))
                  && tokens[i+2].hasPartialPosTag("num")) ) ) {
        return new RuleException(Type.exception);
      }

      // на вами ж отриману 
//      if( (token.matches("вами|тобою|їми|мною|ним"))
//          && tokens[i+1].getCleanToken().matches("же?") ) {
//        return new RuleException(0);
//      }
      if( //(token.equals("собі") || token.equals("йому") || token.equals("їм"))
          PosTagHelper.hasPosTag(tokenReadings, Pattern.compile("noun.*v_dav:&pron:(refl|pers).*"))
          && tokens[i+1].getCleanToken().startsWith("подібн") ) {
        return new RuleException(0);
      }
      if( (token.equals("усім") || token.equals("всім"))
          && tokens[i+1].getCleanToken().startsWith("відом") ) {
        return new RuleException(0);
      }

      if( prep.equalsIgnoreCase("до") && token.equals("схід") 
          && tokens[i+1].getCleanToken().equals("сонця") ) {
        return new RuleException(Type.exception);
      }

//      if( tokens[i+1].getToken().matches("[«„“\"]") 
//          && PosTagHelper.hasPosTagPart(tokens[i], ":abbr") ) {
//        return new RuleException(Type.exception);
//      }

      if( tokens.length > i+2 ) {
        // спиралося на місячної давнини рішення
        if (/*prep.equals("на") &&*/ PosTagHelper.hasPosTag(tokenReadings, "adj:[mfn]:v_rod.*")) {
          String genders = PosTagHelper.getGenders(tokenReadings, "adj:[mfn]:v_rod.*");

          if ( PosTagHelper.hasPosTag(tokens[i+1], "noun.*?:["+genders+"]:v_rod.*")) {
            i += 1;
            return new RuleException(1);
          }
        }

        if( // (token.equals("нікому") || token.equals("ніким") || token.equals("нічим") || token.equals("нічому"))
          PosTagHelper.hasPosTag(tokenReadings, Pattern.compile("noun.*v_(dav|oru):&pron:neg.*"))
            && tokens[i+1].getCleanToken().equals("не")) {
          //          reqTokenReadings = null;
          return new RuleException(Type.skip);
        }
      }
    }

    return new RuleException(Type.none);
  }

  public static RuleException getExceptionStrong(AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings prepTokenReadings) {
    AnalyzedTokenReadings tokenReadings = tokens[i];
    String token = tokenReadings.getCleanToken();
    String tokenLower = token.toLowerCase();
    String prep = prepTokenReadings.getCleanToken().toLowerCase();

    

    // TODO: make it more generic
//    if( tokenLower.equals("брати") ) {
//      prepTokenReadings = null;
//      continue;
//    }

    // TODO: про вчора, щодо завтра

    if( prep.equals("до") || prep.equals("по") ) {
      if( tokenLower.matches("сьогодні|[ву]чора|позавчора|(після)?завтра|тепер|зараз|нині|опівдня|опівночі|досі|навпаки") ) {
        return new RuleException(Type.exception);
      }
    }

    if( prep.equals("на") || prep.equals("від") || prep.equals("про") ) {
      if( tokenLower.matches("сьогодні|[ву]чора|позавчора|(після)?завтра|тепер|зараз|нині|тоді|потім|щодень|повсякдень") ) {
        return new RuleException(Type.exception);
      }
    }

    if( prep.matches("за|зі?|із") ) {
      if( tokenLower.matches("сьогодні|[ву]чора|позавчора|(після)?завтра") ) {
        return new RuleException(Type.exception);
      }
    }

    if( prep.equals("в") || prep.equals("у") ) {
      if( Arrays.asList("нікуди").contains(tokenLower) ) {
        return new RuleException(Type.exception);
      }
    }

    // помилка: до не властиву йому функцію
    if( i < tokens.length - 1
        && token.equals("не")
        && PosTagHelper.hasPosTagStart(tokens[i+1], "ad") )
      return new RuleException(0);

//    "чимало|стільки|обмаль"
    
    // про чимало обмежень
    if( i < tokens.length - 1 
        && LemmaHelper.ADV_QUANT_PATTERN.matcher(tokenLower).matches() ) {
      return new RuleException(Type.exception);
    }

    // за цілком собі реалістичною соціальною
    if( PosTagHelper.hasPosTagAll(tokenReadings.getReadings(), Pattern.compile("adv(?!p).*"))) {
      if( i < tokens.length - 1 
          && tokens[i+1].getCleanToken().equals("собі") ) { 
        return new RuleException(1);
      }
      return new RuleException(0);
    }

    // замість вже самому засвоїти
    if( prep.equals("замість") ) {
      if( new Match()
          .target(Condition.postag(Pattern.compile("verb.*:inf.*")))
          .limit(4)
          .skip(Condition.token("можна").negate())
          .mAfter(tokens, i+1) > 0 ) {
        return new RuleException(Type.exception);
      }
    }

    // Усупереч не те що лихим
    if( new Match().tokenLine("не те").mBefore(tokens, i) > 0 ) {
      return new RuleException(Type.exception);
    }

    return new RuleException(Type.none);
  }

  public static RuleException getExceptionNonInfl(AnalyzedTokenReadings[] tokens, int i, State state) {
    AnalyzedTokenReadings tokenReadings = tokens[i];
    String token = tokenReadings.getCleanToken();
//    String prep = prepTokenReadings.getCleanToken().toLowerCase();

//    if( PosTagHelper.hasPosTagPart(tokenReadings, "insert") )
//      return new RuleException(0);

    if( PosTagHelper.hasPosTagStart(tokenReadings, "part") ) {
      if( LemmaHelper.PART_INSERT_PATTERN.matcher(token.toLowerCase()).matches() ) {
        return new RuleException(0);
      }
    }

   // if( i < tokens.length - 1 && token.equals("їх") && PosTagHelper.hasPosTag(tokens[i+1], Pattern.compile("(adj|noun).*")) ) {
     // return new RuleException(Type.skip);
   // }

    if( token.matches("лиш(е(нь)?)?") ) {
      return new RuleException(0);
    }

    if( tokenReadings.getToken().equals("наприклад") )
      return new RuleException(0);

    if( PosTagHelper.hasPosTag(tokenReadings, Pattern.compile("adv(?!p).*")) ) {
      // по швидко напруженим рукам
      if( i < tokens.length -1 
          && PosTagHelper.hasPosTagStart(tokens[i+1], "adj")
          && PosTagHelper.hasPosTagPartAll(tokenReadings, "adv") )
        return new RuleException(0);
     
      return new RuleException(Type.exception);
    }

    if( tokens.length > i+1 ) {
      // на лише їм відомому ...
      // на вже всім відомому ...
      if ( PosTagHelper.hasPosTag(tokens[i], Pattern.compile("noun:(un)?anim:.:v_dav:&pron.*")) ) {
          if( PosTagHelper.hasPosTagStart(tokens[i+1], "adj")
              && CaseGovernmentHelper.hasCaseGovernment(tokens[i+1], "v_dav") )
          return new RuleException(1);

          if( tokens.length > i+2
              && PosTagHelper.hasPosTagStart(tokens[i+1], "adv")
              && PosTagHelper.hasPosTagStart(tokens[i+2], "adj")
              && CaseGovernmentHelper.hasCaseGovernment(tokens[i+2], "v_dav") )
          return new RuleException(2);
        }
    }    
    if( tokens.length > i+2 ) {
      // на нічого не вартий папірець
      if ( token.equals("нічого")
          && tokens[i+1].getToken().equals("не")
          && PosTagHelper.hasPosTagStart(tokens[i+2], "adj")
          ) {
        return new RuleException(1);
      }
    }
    return new RuleException(Type.none);
  }


  static void logException() {
    if( logger.isDebugEnabled() ) {
      StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
      logger.debug("exception: " /*+ stackTraceElement.getFileName()*/ + stackTraceElement.getLineNumber());
    }
  }

}
