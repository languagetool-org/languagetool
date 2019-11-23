package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.PosTagHelper;

public class TokenAgreementPrepNounExceptionHelper {
  private static final Set<String> NAMES = new HashSet<>(Arrays.asList(
      "ім'я", "прізвище"
      ));

  //|лиш(е(нь)?)?
  private static final Pattern PART_INSERT_PATTERN = Pattern.compile("бодай|буцім(то)?|геть|дедалі|десь|іще|ледве|мов(би(то)?)?|навіть|наче(б(то)?)?|неначе(бто)?|немов(би(то)?)?|ніби(то)?"
      + "|попросту|просто(-напросто)?|справді|усього-на-всього|хай|хоча?|якраз");

  public enum Type { none, exception, skip }
  
  public static class RuleException {
    public final Type type;
    public final int skip;

    public RuleException(Type type) {
      this.type = type;
      this.skip = 0;
    }
    public RuleException(int skip) {
      this.type = Type.skip;
      this.skip = skip;
    }

  }

  
  public static RuleException getExceptionInfl(AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings prepTokenReadings, Set<String> posTagsToFind) {
    AnalyzedTokenReadings tokenReadings = tokens[i];
    String token = tokenReadings.getToken();
    String prep = prepTokenReadings.getToken().toLowerCase();

    
    // на дивом уцілілій техніці
    if( tokenReadings.getToken().equals("дивом") )
      return new RuleException(0);

    // за двісті метрів
    if( PosTagHelper.hasPosTag(tokenReadings, Pattern.compile("numr:.:v_naz.*")) ) {
      return new RuleException(Type.exception);
    }

    //TODO: only for subset: президенти/депутати/мери/гості... or by verb піти/йти/балотуватися/записатися...
    if( prep.matches("в|у|межи|між|на") ) {
      if( PosTagHelper.hasPosTag(tokenReadings, "noun:anim:p:v_naz[^&]*") ) { // but not &pron:
        return new RuleException(Type.exception);
      }
    }

    if (prep.equals("на")) {
      // 1) на (свято) Купала, на (вулиці) Мазепи, на (вулиці) Тюльпанів
      if ((Character.isUpperCase(token.charAt(0)) && PosTagHelper.hasPosTag(tokenReadings, Pattern.compile("noun.*?:.:v_rod.*")))
          // 2) поміняти ім'я на Захар; поміняв Іван на Петро
          || (PosTagHelper.hasPosTag(tokenReadings, Pattern.compile(".*[fl]name.*"))
              && ((i > 1 && NAMES.contains(tokens[i-2].getAnalyzedToken(0).getToken()))
                  || (i > 2 && NAMES.contains(tokens[i-3].getAnalyzedToken(0).getLemma()))))) {
        return new RuleException(Type.exception);
      }

      // handled by xml rule
      if( token.equals("манер") ) {
        return new RuleException(Type.exception);
      }
      // на біс (можливо краще tag=intj?)
      if( token.equalsIgnoreCase("біс") ) {
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

      if( isCapitalized( token ) 
          && LemmaHelper.CITY_AVENU.contains( tokens[i+1].getAnalyzedToken(0).getToken().toLowerCase() ) ) {
        return new RuleException(Type.exception);
      }

      if( PosTagHelper.hasPosTag(tokens[i+1], "num.*")
          && (token.equals("мінус") || token.equals("плюс")
              || token.equals("мінімум") || token.equals("максимум") ) ) {
        return new RuleException(Type.exception);
      }

      // на мохом стеленому дні - пропускаємо «мохом»
      if( PosTagHelper.hasPosTag(tokenReadings, "noun.*?:.:v_oru.*")
          && tokens[i+1].hasPartialPosTag("adjp:pasv") ) {
        return new RuleException(1);
      }

      if( (prep.equalsIgnoreCase("через") || prep.equalsIgnoreCase("на"))  // років 10, відсотки 3-4
          && (PosTagHelper.startsWithPosTag2(tokenReadings.getReadings(), "noun:inanim:p:v_naz") 
              || PosTagHelper.startsWithPosTag2(tokenReadings.getReadings(), "noun:inanim:p:v_rod")) // token.equals("років") 
          && (IPOSTag.isNum(tokens[i+1].getAnalyzedToken(0).getPOSTag())
              || (i<tokens.length-2
                  && LemmaHelper.hasLemma(tokens[i+1], Arrays.asList("зо", "з", "із"))
                  && tokens[i+2].hasPartialPosTag("num")) ) ) {
        return new RuleException(Type.exception);
      }

      if( (token.equals("вами") || token.equals("тобою") || token.equals("їми"))
          && tokens[i+1].getAnalyzedToken(0).getToken().startsWith("ж") ) {
        return new RuleException(0);
      }
      if( (token.equals("собі") || token.equals("йому") || token.equals("їм"))
          && tokens[i+1].getAnalyzedToken(0).getToken().startsWith("подібн") ) {
        return new RuleException(0);
      }
      if( (token.equals("усім") || token.equals("всім"))
          && tokens[i+1].getAnalyzedToken(0).getToken().startsWith("відом") ) {
        return new RuleException(0);
      }

      if( prep.equalsIgnoreCase("до") && token.equals("схід") 
          && tokens[i+1].getAnalyzedToken(0).getToken().equals("сонця") ) {
        return new RuleException(Type.exception);
      }

      if( tokens[i+1].getAnalyzedToken(0).getToken().equals("«") 
          && tokens[i].getAnalyzedToken(0).getPOSTag().contains(":abbr") ) {
        return new RuleException(Type.exception);
      }

      if( tokens.length > i+2 ) {
        // на лише їм відомому ...
//        if ( token.matches("лиш(е(нь))?")
//            && PosTagHelper.hasPosTag(tokens[i+1].getReadings(), Pattern.compile("noun:.:v_dav:&pron:pers:.*"))
//            && PosTagHelper.hasPosTag(tokens[i+1].getReadings(), Pattern.compile("adj.*"))
//            ) {
//          return new RuleException(Type.skip2;
//        }

        // спиралося на місячної давнини рішення
        if (/*prep.equals("на") &&*/ PosTagHelper.hasPosTag(tokenReadings, "adj:[mfn]:v_rod.*")) {
          String genders = PosTagHelper.getGenders(tokenReadings, "adj:[mfn]:v_rod.*");

          if ( PosTagHelper.hasPosTag(tokens[i+1], "noun.*?:["+genders+"]:v_rod.*")) {
            i += 1;
            return new RuleException(1);
          }
        }

        if ((token.equals("нікому") || token.equals("ніким") || token.equals("нічим") || token.equals("нічому")) 
            && tokens[i+1].getAnalyzedToken(0).getToken().equals("не")) {
          //          reqTokenReadings = null;
          return new RuleException(Type.skip);
        }
      }
    }

    return new RuleException(Type.none);
  }

  public static RuleException getExceptionStrong(AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings prepTokenReadings, Set<String> posTagsToFind) {
    AnalyzedTokenReadings tokenReadings = tokens[i];
    String token = tokenReadings.getToken();
    String prep = prepTokenReadings.getToken().toLowerCase();

    if( tokenReadings.getToken().equals("дуже") )
      return new RuleException(0);

    if( prep.equals("до") ) {
      if( token.equalsIgnoreCase("навпаки") ) {
        return new RuleException(Type.exception);
      }
    }

    return new RuleException(Type.none);
  }

  public static RuleException getExceptionNonInfl(AnalyzedTokenReadings[] tokens, int i, AnalyzedTokenReadings prepTokenReadings, Set<String> posTagsToFind) {
    AnalyzedTokenReadings tokenReadings = tokens[i];
    String token = tokenReadings.getToken();
    String prep = prepTokenReadings.getToken().toLowerCase();

    if( PosTagHelper.hasPosTagPart(tokenReadings, "insert") )
      return new RuleException(0);

    if( PosTagHelper.startsWithPosTag2(tokenReadings.getReadings(), "part") ) {
      if( PART_INSERT_PATTERN.matcher(token.toLowerCase()).matches() ) {
        return new RuleException(0);
      }
    }


    if( prep.equals("до") || prep.equals("від") ) {
      if( token.equalsIgnoreCase("сьогодні") || token.equals("завтра") || token.equals("вчора") ) {
        return new RuleException(Type.exception);
      }
    }

    if( PosTagHelper.hasPosTag(tokenReadings, Pattern.compile("adv.*")) )
      return new RuleException(Type.exception);

    if( tokens.length > i+2 ) {
      // на лише їм відомому ...
      if ( token.matches("лиш(е(нь)?)?")
          && PosTagHelper.hasPosTag(tokens[i+1].getReadings(), Pattern.compile("noun:(un)?anim:.:v_dav:&pron:pers:.*"))
          && PosTagHelper.hasPosTag(tokens[i+2].getReadings(), Pattern.compile("adj.*"))
          ) {
        return new RuleException(1);
      }
    }
    return new RuleException(Type.none);
  }

  private static boolean isCapitalized(String token) {
    return token.length() > 1 && Character.isUpperCase(token.charAt(0)) && Character.isLowerCase(token.charAt(1));
  }

}
