package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.uk.InflectionHelper.Inflection;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.PosTagHelper;
import org.languagetool.tools.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.6
 */
final class TokenAgreementAdjNounExceptionHelper {
  private static Logger logger = LoggerFactory.getLogger(TokenAgreementAdjNounExceptionHelper.class);

  private static final Pattern NUMBER_V_NAZ = Pattern.compile("number|numr:p:v_naz|noun.*?:p:v_naz:&numr.*");
  // including latin 'a' and 'i' so the rules don't trip on them in Ukrainian sentences
  static final List<String> CONJ_FOR_PLURAL_WITH_COMMA = Arrays.asList("і", "й", "та", "чи", "або", "ані", "також", "то", "a", "i", ",");
  static final List<String> CONJ_FOR_PLURAL = Arrays.asList("і", "й", "та", "чи", "або", "ані", "також", "то", "a", "i");
  static final Pattern CONJ_FOR_PLULAR_PATTERN = Pattern.compile(StringUtils.join(CONJ_FOR_PLURAL, "|"));
  private static final Pattern DOVYE_TROYE = Pattern.compile(".*[2-4]|.*[2-4][\u2013\u2014-].*[2-4]|два|обидва|двоє|три|троє|чотири|один[\u2013\u2014-]два|два[\u2013\u2014-]три|три[\u2013\u2014-]чотири|двоє[\u2013\u2014-]троє|троє[\u2013\u2014-]четверо");
  private static final Pattern VERB_NOT_INSERT_PATTERN = Pattern.compile("verb(?!.*insert)");


  private TokenAgreementAdjNounExceptionHelper() {
  }


  public static boolean isException(AnalyzedTokenReadings[] tokens, int i, 
      List<InflectionHelper.Inflection> masterInflections, List<InflectionHelper.Inflection> slaveInflections, 
      List<AnalyzedToken> adjTokenReadings, List<AnalyzedToken> slaveTokenReadings) {

    AnalyzedTokenReadings adjAnalyzedTokenReadings = tokens[i-1];


    if( i > 1
        && StringTools.isCapitalizedWord(tokens[i-1].getToken())
        && StringTools.isCapitalizedWord(tokens[i-2].getToken())
        && (LemmaHelper.hasLemma(tokens[i-1], "вітчизняний") || LemmaHelper.hasLemma(tokens[i-1], "житомирський"))
        && LemmaHelper.hasLemma(tokens[i-2], "великий")
        && ! LemmaHelper.hasLemma(tokens[i], "війна") ) {
      logException();
      return true;
    }

    if( i > 1
        && LemmaHelper.hasLemma(tokens[i-1], "національний")
        && LemmaHelper.hasLemma(tokens[i-2], "перший")
        && Character.isUpperCase(tokens[i-1].getToken().charAt(0))
        && Character.isUpperCase(tokens[i-2].getToken().charAt(0))) {
      logException();
      return true;
    }

    //  в день Хрещення Господнього священики
    if( LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("божий", "господній", "Христовий"))
        && Character.isUpperCase(tokens[i-1].getToken().charAt(0))) {
      logException();
      return true;
    }

    // князівством Литовським подоляни
    if( i > 1
        && PosTagHelper.hasPosTagPart(tokens[i-2], "noun")
        && Character.isUpperCase(tokens[i-1].getToken().charAt(0))  //TODO: 2nd char is lowercase?
        && ! Collections.disjoint(masterInflections, InflectionHelper.getNounInflections(tokens[i-2].getReadings())) ) {
      logException();
      return true;
    }

    // 5-а клас
    if( Pattern.compile("([1-9]|1[0-2])[\u2018-][а-д]").matcher(adjAnalyzedTokenReadings.getToken()).matches()
        && LemmaHelper.hasLemma(tokens[i], "клас") ) {
      logException();
      return true;
    }

    // we add pos "number" in disambiguation
//    // маршрутка номер 29-а фірми
//    if( i > 2
//        && Pattern.compile("[0-9]+[\u2018-][а-яіїєґ]").matcher(adjAnalyzedTokenReadings.getToken()).matches()
//        && LemmaHelper.hasLemma(tokens[i-2], Arrays.asList("номер", "пункт", "№")) ) {
//      logException();
//      return true;
//    }
//
//    // на вул. Рубчака, 17-а Тарас Стецьків
//    if( i > 2
//        && Pattern.compile("[0-9]+[\u2018-][а-яіїєґ]").matcher(adjAnalyzedTokenReadings.getToken()).matches()
//        && LemmaHelper.reverseSearch(tokens, i-2, 4, Pattern.compile("вул\\.|вулиця"), null) ) {
//      logException();
//      return true;
//    }
    
    // Першими голодування оголосили
    // одним із перших
    if( i > 1
        && LemmaHelper.hasLemma(adjAnalyzedTokenReadings, Arrays.asList("перший")) ) { 
      //                && PosTagHelper.hasPosTag(slaveTokenReadings, ".*v_naz.*")) ) {
      logException();
      return true;
    }

    // абзац другий частини першої
    if( i > 2 && i < tokens.length -1
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*&numr.*") 
        && PosTagHelper.hasPosTag(tokens[i+1], "adj:.:v_rod.*&numr.*")
        && LemmaHelper.hasLemma(tokens[i-2], Arrays.asList("абзац", "розділ", "пункт", "частина"))
        && LemmaHelper.hasLemma(tokens[i], Arrays.asList("абзац", "розділ", "пункт", "частина"))
        ) { 
      logException();
      return true;
    }

    // статтю 6-ту закону
    if( i > 1
        && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "num")
        && LemmaHelper.hasLemma(tokens[i-2], "стаття")
        && ! Collections.disjoint(masterInflections, InflectionHelper.getNounInflections(tokens[i-2].getReadings())) ) {
      logException();
      return true;
    }

    // лава запасних партії
    if( i > 1
        && tokens[i-1].getToken().equals("запасних")
        && LemmaHelper.hasLemma(tokens[i-2], "лава")) {
      logException();
      return true;
    }

    // старший зміни
    if( i > 1
        && tokens[i].getToken().equals("зміни")
        && LemmaHelper.hasLemma(tokens[i-1], "старший")) {
      logException();
      return true;
    }

    // на повну людей розкрутили
    if( i > 1
        && tokens[i-1].getToken().equals("повну")
        && tokens[i-2].getToken().equalsIgnoreCase("на")) {
      logException();
      return true;
    }

    // у Другій світовій участь
    if( i > 1
        && LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("світовий"), ":f:")
        && LemmaHelper.hasLemma(tokens[i-2], Arrays.asList("другий", "перший"), ":f:") ) {
      logException();
      return true;
    }
    
    // площею 100 кв. м
    // довжиною до 500
    if( i < tokens.length -1
        && Arrays.asList("площею", "об'ємом", "довжиною", "висотою", "зростом").contains(tokens[i].getToken())
        && PosTagHelper.hasPosTag(tokens[i+1], "prep.*|.*num.*") ) {
      logException();
      return true;
    }

    // 10 метрів квадратних води
    if( i > 3
        && LemmaHelper.hasLemma(tokens[i-2], Pattern.compile(".*метр.*"))
        && LemmaHelper.hasLemma(tokens[i-1], Pattern.compile("квадратний|кубічний"))
        && PosTagHelper.hasPosTagPart(tokens[i], "v_rod") ) {
      logException();
      return true;
    }

    // молодшого гвардії сержанта
    if( i > 1 && i < tokens.length - 1
        && tokens[i].getToken().equals("гвардії")
        && PosTagHelper.hasPosTag(tokens[i+1], "noun.*")
        && ! Collections.disjoint(masterInflections, InflectionHelper.getNounInflections(tokens[i+1].getReadings())) ) {
      logException();
      return true;
    }

    // 200% річних прибутку
    if( i > 1
        && tokens[i-2].getToken().endsWith("%") 
        && tokens[i-1].getToken().equals("річних") ) {
      logException();
      return true;
    }

    // пасли задніх
    if( i > 2
        && LemmaHelper.hasLemma(tokens[i-2], "пасти")
        && tokens[i-1].getToken().equals("задніх") ) {
      logException();
      return true;
    }

    // старший групи
    if( i > 1
        && LemmaHelper.hasLemma(tokens[i-1], "старший")
        && tokens[i].getToken().equals("групи") ) {
      logException();
      return true;
    }

    // не мати рівних
    if( i > 2
        && LemmaHelper.hasLemma(tokens[i-2], "мати")
        && tokens[i-1].getToken().equals("рівних") ) {
      logException();
      return true;
    }

    // taken care by barbarism rule
    // на манер
    if( i > 1 
        && tokens[i].getToken().equals("манер")
        && tokens[i-2].getToken().equalsIgnoreCase("на") ) {
      logException();
      return true;
    }

    // taken care by barbarism rule
    // усі до єдиного
    if( i > 2 
        && tokens[i-1].getToken().equals("єдиного")
        && tokens[i-2].getToken().equals("до")
        && LemmaHelper.hasLemma(tokens[i-3], Arrays.asList("весь", "увесь"), ":p:") ) {
      logException();
      return true;
    }

    // сильні світу цього
    if( i < tokens.length -1
        && (tokens[i].getToken().equals("світу") || tokens[i].getToken().equals("миру"))
        && ( LemmaHelper.hasLemma(adjAnalyzedTokenReadings, Arrays.asList("сильний", "могутній", "великий"))
          || LemmaHelper.hasLemma(tokens[i+1], Arrays.asList("цей", "сей"), ":m:v_rod") ) ) {
      logException();
      return true;
    }

    // колишня Маяковського
    if( LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("колишній", "тодішній", "теперішній", "нинішній"), Pattern.compile("adj.*:f:.*")) 
        && Character.isUpperCase(tokens[i].getToken().charAt(0)) ) {
      logException();
      return true;
    }

    // імені Шевченка
    // 4-й Запорізький ім. гетьмана Б. Хмельницького
    if( i < tokens.length -1
        && Arrays.asList("ім.", "імені", "ордена").contains(tokens[i].getToken()) ) { 
//        && Character.isUpperCase(tokens[i+1].getToken().charAt(0)) ) {
      logException();
      return true;
    }

    // на дівоче Анна
    if( i > 1
        && Arrays.asList("дівоче").contains(tokens[i-1].getToken()) 
        && PosTagHelper.hasPosTagPart(tokens[i], "name") ) {
      logException();
      return true;
    }

    // вольному воля
    if( Arrays.asList("вольному", "вільному").contains(adjAnalyzedTokenReadings.getToken().toLowerCase())
        && tokens[i].getToken().equals("воля") ) {
      logException();
      return true;
    }

    // порядок денний
    if( i > 1
        && LemmaHelper.hasLemma(adjAnalyzedTokenReadings, "денний")
        && LemmaHelper.hasLemma(tokens[i-2], "порядок")
        && ! Collections.disjoint(masterInflections, InflectionHelper.getNounInflections(tokens[i-2].getReadings())) ) {
      logException();
      return true;
    }

    // Вони здатні екскаватором переорювати
    if( LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("здатний", "змушений", "винний", "повинний", "готовий", "спроможний")) ) {
      logException();
      return true;
    }

    // протягом минулих травня – липня
    if( i < tokens.length - 2
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:.*")
        && tokens[i+1].getToken().matches("[\u2014\u2013-]")
        && PosTagHelper.hasPosTag(tokens[i+2], "(adj|noun).*")
        //TODO: hasOverlapIgnoreGender(masterInflections, tokens[i+2])
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // моїх маму й сестер
    if( i < tokens.length - 2
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:.*")
        && forwardConjFind(tokens, i+1, 2)
        && hasOverlapIgnoreGender(masterInflections, slaveInflections, "p", null) ) {
      logException();
      return true;
    }

    // зв'язаних ченця з черницею
    // на зарубаних матір з двома синами
    if( i < tokens.length - 2
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:.*")
        && Arrays.asList("з", "із", "зі").contains(tokens[i+1].getToken())
        && PosTagHelper.hasPosTag(tokens[i+2], "(noun|numr).*:v_oru.*")
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // навчальної та середньої шкіл
    if( i > 2 
        && PosTagHelper.hasPosTag(tokens[i], "noun:.*:p:.*")
        && (reverseConjFind(tokens, i-2, 3) || reverseConjAdvFind(tokens, i-2, 3))
        && hasOverlapIgnoreGender(masterInflections, slaveInflections, null, "p") 
        && LemmaHelper.reverseSearch(tokens, i-3, 100, null, Pattern.compile("adj.*")) ) {
      logException();
      return true;
    }

    // Большого та Маріїнського театрів
    // Пляжі 3, 4 і 5-ї категорій
    if( i > 2 
        && PosTagHelper.hasPosTag(tokens[i], "noun:.*:p:.*")
        && (reverseConjFind2(tokens, i-2, 3) )
        && hasOverlapIgnoreGender(masterInflections, slaveInflections, null, "p") ) {
      logException();
      return true;
    }

    // ні у методологічному, ні у практичному аспектах
    if( i > 7
        && PosTagHelper.hasPosTag(tokens[i], "noun:.*:p:.*")
        && PosTagHelper.hasPosTag(tokens[i-1], "adj:.*")
        && PosTagHelper.hasPosTag(tokens[i-2], "prep.*")
        && LemmaHelper.hasLemma(tokens[i-3], Arrays.asList("ні", "ані", "хоч", "що", "як"))
        && tokens[i-4].getToken().equals(",")
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // на проурядову і, здається, пропрезидентську частини
    if( i > 6
        && PosTagHelper.hasPosTag(tokens[i], "noun:.*:p:.*")
        && PosTagHelper.hasPosTag(tokens[i-1], "adj:.*")
        && tokens[i-2].getToken().equals(",")
        && ( (tokens[i-4].getToken().equals(",") && CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[i-5].getToken().toLowerCase())
                && ! PosTagHelper.hasPosTag(tokens[i-3], VERB_NOT_INSERT_PATTERN))
            || (tokens[i-5].getToken().equals(",") && CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[i-6].getToken().toLowerCase())
                            && ! PosTagHelper.hasPosTag(tokens[i-3], VERB_NOT_INSERT_PATTERN)
                            && ! PosTagHelper.hasPosTag(tokens[i-4], VERB_NOT_INSERT_PATTERN)) )
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // коринфський з іонійським ордери
    if( i > 2 
        && PosTagHelper.hasPosTag(tokens[i], "noun:.*:p:.*")
        && tokens[i-2].getToken().matches("з|із|зі")
        && PosTagHelper.hasPosTag(tokens[i-1], "adj.*v_oru.*")
        && hasOverlapIgnoreGender(InflectionHelper.getAdjInflections(tokens[i-3].getReadings()), slaveInflections) ) {
      logException();
      return true;
    }

    // на довгих півстоліття
    if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:v_rod.*")
        && tokens[i].getToken().startsWith("пів")
        && PosTagHelper.hasPosTag(tokens[i], "noun.*v_rod.*") ) {
      logException();
      return true;
    }

    // на довгих чверть століття
    if( i < tokens.length-1
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:v_rod.*")
        && tokens[i].getToken().equals("чверть")
        && PosTagHelper.hasPosTag(tokens[i+1], "noun.*v_rod.*") ) {
      logException();
      return true;
    }

    // присудок ж.р. + професія ч.р.
    if( Arrays.asList("переконана", "впевнена", "упевнена", "годна", "ладна", "певна", "причетна", "обрана", "призначена").contains(adjAnalyzedTokenReadings.getToken())
        && PosTagHelper.hasPosTag(tokens[i], "noun:anim:m:v_naz.*") ) {
      logException();
      return true;
    }

    // чинних станом на
    if( i < tokens.length-1
        && tokens[i].getToken().equals("станом")
        && tokens[i+1].getToken().equals("на") ) {
      logException();
      return true;
    }
    
    // постійно на рівних міністри, президенти
    if( i > 1
        && tokens[i-1].getToken().equals("рівних")
        && tokens[i-2].getToken().equalsIgnoreCase("на") ) {
      logException();
      return true;
    }

    // польські зразка 1620—1650 років
    if( i < tokens.length-1
        && tokens[i].getToken().equals("зразка") ) {
      logException();
      return true;
    }

    // три зелених плюс два червоних
    if( Arrays.asList("мінус", "плюс").contains(tokens[i].getToken()) ) {
      logException();
      return true;
    }

    // важкими пару років
    // неконституційними низку законів
    // природний тисячею років підтверджений
    if( i < tokens.length-1 
        && LemmaHelper.hasLemma(tokens[i], Arrays.asList("пара", "низка", "ряд", "купа", "більшість", "десятка", "сотня", "тисяча", "мільйон"))
        && (PosTagHelper.hasPosTag(tokens[i+1], "noun.*?:p:v_rod.*")
          || (i < tokens.length-1
            && PosTagHelper.hasPosTag(tokens[i+1], "adj:p:v_rod.*")
            && PosTagHelper.hasPosTag(tokens[i+2], "noun.*?:p:v_rod.*")) ) ) {
      logException();
      return true;
    }

    // разів (у) десять
    if( i < tokens.length-1
        && LemmaHelper.hasLemma(tokens[i], Arrays.asList("раз"), Pattern.compile(".*p:v_(naz|rod).*"))
        && (PosTagHelper.hasPosTag(tokens[i+1], "number|numr:p:v_naz|noun.*?:p:v_naz:&numr.*")
            || PosTagHelper.hasPosTagPart(tokens[i+1], "prep")) ) {
      logException();
      return true;
    }

    // років 6, відсотків зо два
    if( i < tokens.length-1
        && LemmaHelper.hasLemma(tokens[i], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun.*?p:v_(naz|rod).*"))
        && (PosTagHelper.hasPosTag(tokens[i+1], NUMBER_V_NAZ)
            || (i < tokens.length-2
              && LemmaHelper.hasLemma(tokens[i+1], Arrays.asList("на", "за", "з", "із", "зо", "через", "під"), "prep")
                && PosTagHelper.hasPosTag(tokens[i+2], NUMBER_V_NAZ))) ) {
      logException();
      return true;
    }

    // осіб на 30
    if( i < tokens.length-2
        && LemmaHelper.hasLemma(tokens[i], Arrays.asList("особа"), Pattern.compile("noun.*?p:v_(naz|rod).*"))
        && LemmaHelper.hasLemma(tokens[i+1], Arrays.asList("на", "з", "із", "зо", "під"), "prep")
        && PosTagHelper.hasPosTag(tokens[i+2], NUMBER_V_NAZ) ) {
      logException();
      return true;
    }

    // хвилини з 55-ї вірмени почали
    if( i > 3
        && LemmaHelper.hasLemma(tokens[i-3], LemmaHelper.TIME_LEMMAS_SHORT)
        && PosTagHelper.hasPosTagPart(tokens[i-2], "prep")
        && PosTagHelper.hasPosTagPart(tokens[i-1], "num")) {

      Collection<String> prepGovernedCases = CaseGovernmentHelper.getCaseGovernments(tokens[i-2], IPOSTag.prep.name());
      if( TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[i-3])
          && TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[i-1]) ) {
        logException();
        return true;
      }
    }

    // пофарбований рік тому
    if( i < tokens.length-1
        && LemmaHelper.hasLemma(tokens[i], LemmaHelper.TIME_LEMMAS) 
        && LemmaHelper.hasLemma(tokens[i+1], "тому") ) {
      logException();
      return true;
    }

    // замість звичного десятиліттями
    if( i < tokens.length-1
        && LemmaHelper.hasLemma(tokens[i], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:p:v_oru.*")) ) {
      logException();
      return true;
    }

    
    // кількох десятих відсотка
    if( LemmaHelper.hasLemma(tokens[i-1], Arrays.asList("десятий", "сотий", "тисячний", "десятитисячний", "стотитисячний", "мільйонний", "мільярдний"))
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, ".*:[fp]:.*")
        && PosTagHelper.hasPosTag(tokens[i], "noun.*v_rod.*") ) {
      logException();
      return true;
    }


    // два нових горнятка (див. #1 нижче)
    // два відомих імені
    // 33 народних обранці
    // TODO: два великих комфортних піщаних пляжі
    // TODO: два закинутих, похмурих палаци
    if( i>1 && i<tokens.length
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, ".*:p:v_(rod|naz).*")
        && LemmaHelper.reverseSearch(tokens, i-2, 5, DOVYE_TROYE, null)
//        && ( LemmaHelper.hasLemma(tokens[i-2], DOVYE_TROYE)
//            // три жовтих обірваних чоловіки
//            // три предкові слов’янські племені
//            || (i>2 && LemmaHelper.hasLemma(tokens[i-3], DOVYE_TROYE) 
//                && (PosTagHelper.hasPosTag(tokens[i-2], "adv.*|adj.*:p:v_(rod|naz).*")
//                    // два «круглих столи»
//                    || tokens[i-2].getToken().matches("[«„\"]"))) ) 
        && (PosTagHelper.hasPosTag(tokens[i], ".*(:p:v_naz|:n:v_rod).*") 
            || Arrays.asList("імені", "ока").contains(tokens[i].getToken())) ) {
      logException();
      return true;
    }


    // 1-3-й класи
    // на сьомому–восьмому поверхах
    if( (adjAnalyzedTokenReadings.getToken().matches("[0-9]+[\u2014\u2013-][0-9]+[\u2013-][а-яіїєґ]{1,3}")
        || (adjAnalyzedTokenReadings.getToken().matches(".*[а-яїієґ][\u2014\u2013-].*")
            && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "&numr"))) 
        && PosTagHelper.hasPosTag(slaveTokenReadings, ".*:p:.*")
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }
    // восьмого – дев’ятого класів
    if( i > 2 
        && Arrays.asList("\u2013", "\u2014").contains(tokens[i-2].getToken())
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, ".*num.*")
        && PosTagHelper.hasPosTag(tokens[i-3], ".*num.*")
        && PosTagHelper.hasPosTag(slaveTokenReadings, ".*:p:.*")
        && hasOverlapIgnoreGender(InflectionHelper.getAdjInflections(tokens[i-3].getReadings()), slaveInflections)
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // найближчі рік-два
    // понаднормові годину-півтори
    // суперкризовими січнем–лютим
//    if( LemmaHelper.hasLemma(adjAnalyzedTokenReadins, Arrays.asList("найближчий", "минулий"), ":p:") 
    if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*:p:.*") 
        && tokens[i].getToken().matches(".*[\u2014\u2013-].*")
        && (LemmaHelper.TIME_PLUS_LEMMAS.contains(tokens[i].getAnalyzedToken(0).getLemma().split("[\u2014\u2013-]")[0])
        // does not work for тиждень-два due to dynamic tagging returning singular
          || hasOverlapIgnoreGender(masterInflections, slaveInflections)) ) {
      logException();
      return true;
    }

    // Від наступних пари десятків
    if( i < tokens.length - 1
        && LemmaHelper.hasLemma(tokens[i], "пара")
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*:p:.*")
        && PosTagHelper.hasPosTag(tokens[i+1], ".*:p:v_rod.*") ) {      // adding "num" fails "десятків" тощо 
      logException();
      return true;
    }

    // п'ять шостих світу
    if( i > 1
        && PosTagHelper.hasPosTag(tokens[i-1], ".*:p:v_rod.*num.*")
        && PosTagHelper.hasPosTagPart(tokens[i-2], "num")
        && PosTagHelper.hasPosTag(tokens[i], "noun.*v_rod.*") ) {
      logException();
      return true;
    }

    // 1/8-ї фіналу
    if( i > 3
        && "/".equals(tokens[i-2].getToken())
        && PosTagHelper.hasPosTagPart(tokens[i-3], "numb")
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // з 3-ма вікнами
    // TODO: temporary: зачасто вживають зайвий наросток для кількісного числівника
//    if( Pattern.compile(".*[0-9]-ма").matcher(adjAnalyzedTokenReadings.getToken()).matches() ) {
//      logException();
//      return true;
//    }
    
    // dates
    if( PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, ":&numr") ) {
      String adjToken = adjAnalyzedTokenReadings.getToken();

      // Ставши 2003-го прем’єром
      if( adjToken.matches("([12][0-9])?[0-9][0-9][\u2014\u2013-](й|го|м|му)")
          || adjToken.matches("([12][0-9])?[0-9]0[\u2014\u2013-](ті|тих|их|х)")
          || adjToken.matches("([12][0-9])?[0-9][0-9][\u2014\u2013-]([12][0-9])?[0-9][0-9][\u2014\u2013-](й|го|м|му|ті|тих|их|х)") ) {
        logException();
        return true;
      }
      // Призначений на 11-ту похід
      if( i > 1 
          && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, ":f:")
          && LemmaHelper.hasLemma(tokens[i-2], Arrays.asList("на", "в", "у", "за", "о", "до", "після", "близько", "раніше"))
          && ! LemmaHelper.hasLemma(tokens[i], Arrays.asList("хвилина", "година")) ) {
        logException();
        return true;
      }
      // 11-й ранку
      // Arrays.asList("ранок", "день", "вечір", "ніч", "пополудень") + "v_rod"
      if( PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, ":f:") 
          && tokens[i].getToken().matches("ранку|дня|вечора|ночі|пополудня") ) {
        logException();
        return true;
      }
      // дев'яте травня
      if( PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, ":n:")
          && LemmaHelper.hasLemma(tokens[i], LemmaHelper.MONTH_LEMMAS, "v_rod") ) {
        logException();
        return true;
      }
    }


    // обмежуючий власність, створивший історію
    // let simple replace rule take care of this
    if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, ".*?adjp:actv.*:bad.*") ) {
//        && PosTagHelper.hasPosTag(slaveTokenReadings, "noun.*v_zna")) {
      logException();
      return true;
    }

    // нічого протизаконного жінка не зробила
    // нічого поганого людям не зробили
    // що нічим дієвим ініціативи не завершаться
    // писав про щось подібне Юрій
    if( i > 2 && i <= tokens.length - 1
        && LemmaHelper.hasLemma(tokens[i-2], Arrays.asList("ніщо", "щось", "ніхто", "хтось"))
//        && PosTagHelper.hasPosTag(tokens[i-1], "adj:.*v_rod.*")
        // we now have gender for pron
        && ! Collections.disjoint(InflectionHelper.getNounInflections(tokens[i-2].getReadings()), masterInflections)
        //&& tokens[i+1].getToken().equals("не")
        ) {
      logException();
      return true;
    }

    // визнання неконституційним закону
    // визнання тут шкідливою орієнтацію
    if( i > 1
        && LemmaHelper.revSearch(tokens, i-2, Pattern.compile(".*(ння|ття)"), null)
        && PosTagHelper.hasPosTag(tokens[i-1], "adj.*:v_oru.*")
        && PosTagHelper.hasPosTag(tokens[i], "noun:.*:v_rod.*") 
        && genderMatches(masterInflections, slaveInflections, "v_oru", "v_rod") ) {
      logException();
      return true;
    }
   
    
    int verbPos = LemmaHelper.revSearchIdx(tokens, i-2, Pattern.compile("бути|ставати|стати|залишатися|залишитися"), null);
    if( verbPos != -1 ) {
      if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*v_naz.*adjp:pasv.*") ) {
        // був змушений
        if( genderMatches(masterInflections, slaveInflections, "v_naz", "v_naz") ) {
          logException();
          return true;
        }
        // був заповнений відвідувачами
        else if( genderMatches(masterInflections, slaveInflections, "v_naz", "v_naz") ) {
          logException();
          return true;
        }
      }
      else if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*v_oru.*") ) {
        // була чинною заборона
        if( PosTagHelper.hasPosTag(slaveTokenReadings, "noun.*v_naz.*")) {
          if( genderMatches(masterInflections, slaveInflections, "v_oru", "v_naz") ) {
            // не можуть бути толерантними ізраїльтяни
            if( PosTagHelper.hasPosTagPart(tokens[verbPos], ":inf")
                || TokenAgreementNounVerbRule.inflectionsOverlap(tokens[verbPos].getReadings(), tokens[i].getReadings()) ) {
                  logException();
                  return true;
            }
          }
          // Стали дорожчими хліб чи бензин
          else if( i < tokens.length -1 
              && PosTagHelper.hasPosTagPart(tokens[i-1], "adj:p:")
              && CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[i+1].getToken().toLowerCase()) ) {
            logException();
            return true;
          }
        }
        // слід бути обережними туристам у горах
        else if( PosTagHelper.hasPosTag(slaveTokenReadings, "noun.*v_dav.*")) {
          if( genderMatches(masterInflections, slaveInflections, "v_oru", "v_dav") ) {
            logException();
            return true;
          }
        }
      }
    }

    verbPos = LemmaHelper.revSearchIdx(tokens, i-2, null, "verb.*");
    if( verbPos != -1 ) {
      if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*v_oru.*") ) {
        // визнали справедливою наставники обох команд
        if( PosTagHelper.hasPosTag(slaveTokenReadings, "noun.*v_naz.*")
              && TokenAgreementNounVerbRule.inflectionsOverlap(tokens[verbPos].getReadings(), tokens[i].getReadings()) ) {
          logException();
          return true;
        }
      }
    }

    
    // помальована в (усе) біле кімната
    if( i > 2
        && Arrays.asList("біле", "чорне", "оранжеве", "червоне", "жовте", "синє", "зелене", "фіолетове").contains(tokens[i-1].getToken())
        && Arrays.asList("в", "у").contains(tokens[i-2].getToken())
        && PosTagHelper.hasPosTagPart(tokens[i-3], "adjp:pasv") ) {

      List<InflectionHelper.Inflection> masterInflections_ = InflectionHelper.getAdjInflections(tokens[i-3].getReadings());
      if( ! Collections.disjoint(masterInflections_, slaveInflections) ) {
        logException();
        return true;
      }
    }
    if( i > 3
        && Arrays.asList("біле", "чорне").contains(tokens[i-1].getToken())
        && Arrays.asList("усе", "все").contains(tokens[i-2].getToken())
        && Arrays.asList("в", "у").contains(tokens[i-3].getToken())
        && PosTagHelper.hasPosTagPart(tokens[i-4], "adjp:pasv") ) {

      List<InflectionHelper.Inflection> masterInflections_ = InflectionHelper.getAdjInflections(tokens[i-4].getReadings());
      if( ! Collections.disjoint(masterInflections_, slaveInflections) ) {
        logException();
        return true;
      }
    }

    // повторена тисячу разів
    if( i < tokens.length - 1
        && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "adjp:pasv")
        && Arrays.asList("тисячу", "сотню", "десятки").contains(tokens[i].getToken())
        && Arrays.asList("разів", "раз", "років").contains(tokens[i+1].getToken()) ) {
      logException();
      return true;
    }


    if( i > 2 ) {
//      // порівняно з попереднім
//      if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadins, "adj.*v_oru")
//          && LemmaHelper.hasLemma(tokens[i-3], Arrays.asList("порівняно", "аналогічно")) 
//          && LemmaHelper.hasLemma(tokens[i-2], Pattern.compile("з|із|зі")) ) {
//        logException();
//        return true;
//      }
      
      // наближена до сімейної форма
      if( PosTagHelper.hasPosTagPart(tokens[i-2], "prep") ) {
        if (PosTagHelper.hasPosTag(tokens[i-3], "(adj|verb|part|noun|adv).*")) {

          Collection<String> prepGovernedCases = CaseGovernmentHelper.getCaseGovernments(tokens[i-2], IPOSTag.prep.name());
          if( TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[i-1]) ) {

            // відрізнялася (б) від нинішньої ситуація
            // відрізнялося від російського способом
            // поряд з енергетичними Москва висувала
            // а відміну від європейських санкції США
            // can't just ignore noun: ігнорує "асоціюється в нас із сучасною цивілізацію"
            if( (PosTagHelper.hasPosTag(tokens[i-3], "(verb|part).*")
                  || Arrays.asList("поряд", "відміну", "порівнянні").contains(tokens[i-3].getToken().toLowerCase()) )
                && PosTagHelper.hasPosTag(tokens[i], "noun.*v_(naz|zna|oru).*") ) {
              //TODO: check noun case agreement with verb
              logException();
              return true;
            }
            
            List<InflectionHelper.Inflection> masterInflections_ = InflectionHelper.getAdjInflections(tokens[i-3].getReadings());

            if( ! Collections.disjoint(masterInflections_, slaveInflections) ) {
              logException();
              return true;
            }
            
            // тотожні із загальносоюзними герб і прапор
            if( i < tokens.length - 1
                && PosTagHelper.hasPosTagPart(tokens[i-1], "adj:p:")
                && CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[i+1].getToken().toLowerCase()) 
                && PosTagHelper.hasPosTagPart(tokens[i-3], "adj:p:")
                && hasOverlapIgnoreGender(InflectionHelper.getAdjInflections(tokens[i-3].getReadings()), slaveInflections, "p", null)) {
              logException();
              return true;
            }
          }
        }
      }
    }
    
    
    // adjp:pasv + adj:v_oru + noun (case governed by adjp)
    // підсвічений синім діамант
    if( i > 1
        && PosTagHelper.hasPosTagPart(tokens[i-2], "adjp:pasv") // could be :&adjp or :&&adjp
        && PosTagHelper.hasPosTag(tokens[i-1], "adj.*v_oru.*")
        && ! Collections.disjoint(InflectionHelper.getAdjInflections(tokens[i-2].getReadings()), slaveInflections) ) {
      logException();
      return true;
    }

    // adjp:pasv + noun:v_oru
    // захищені законом (від образ)
    // Змучений тягарем життя
    // оприлюднений депутатом Юрієм
    // вкриті плющем будинки
    // всі вкриті плющем
    if( PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "adjp:pasv") // could be :&adjp or :&&adjp
        && PosTagHelper.hasPosTagPart(tokens[i], "v_oru") ) {
      logException();
      return true;
    }

    // adj:v_oru + noun:v_naz + (verb)
    // Найнижчою частка таких є на Півдні
    // Слабшою критики вважають
    if( i > 1
        && ! PosTagHelper.hasPosTag(tokens[i-2], ".*adjp:pasv.*|prep.*") 
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*v_oru.*") 
        && PosTagHelper.hasPosTag(slaveTokenReadings, "noun.*v_naz.*") 
        && LemmaHelper.forwardPosTagSearch(tokens, i+1, "verb", 3)) {
      logException();
      return true;
    }

    // verb + adj:v_oru + noun:v_zna
    // зроблять неможливою ротацію влади
    // we still want to trigger on: за наявною інформацію
    if( (i < 3 || ! CaseGovernmentHelper.hasCaseGovernment(tokens[i-2], "v_oru"))
        && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "v_oru") 
        && PosTagHelper.hasPosTag(slaveTokenReadings, ".*v_zna.*") 
        && genderMatches(masterInflections, slaveInflections, "v_oru", "v_zna") ) {
      logException();
      return true;
    }


    if( caseGovernmentMatches(adjTokenReadings, slaveInflections) ) {

      if( i < tokens.length - 1 
          && PosTagHelper.hasPosTagPart(tokens[i+1], "noun:") ) {

        // вдячний редакторові Вільяму
//        if( PosTagHelper.hasPosTag(tokens[i+1], "noun:anim.*?[flp]name.*")
//            && caseGovernmentMatches(adjTokenReadings, InflectionHelper.getNounInflections(tokens[i+1].getReadings())) ) {
//          logException();
//          return true;
//        }

        // Нав’язаний Австрії коаліцією
        // будуть вдячні державі Україна
        // мають бути підпорядковані служінню
        // радий присутності генерала
        if( PosTagHelper.hasPosTag(tokens[i+1], "noun.*v_(rod|oru|naz|dav).*") ) {
//            && ! PosTagHelper.hasPosTag(adjAnalyzedTokenReadins, "adj.*v_oru.*") ) {
          logException();
          return true;
        }

        // Нав’язаний Австрії нейтралитет
        List<InflectionHelper.Inflection> slave2Inflections = InflectionHelper.getNounInflections(tokens[i+1].getReadings());

        if( ! Collections.disjoint(masterInflections, slave2Inflections) ) {
          logException();
          return true;
        }
      }
      else {
        // Нав’язаний Австрії,
        logException();
        return true;
      }
    }

    // альтернативну олігархічній модель
    // альтернативні газовому варіанти
    if( i > 1
        && PosTagHelper.hasPosTagPart(tokens[i-2], "adj")
        && caseGovernmentMatches(tokens[i-2].getReadings(), masterInflections) ) {

      List<Inflection> preAdjInflections = InflectionHelper.getAdjInflections(tokens[i-2].getReadings());

      if( //genderMatches(masterInflections, slaveInflections, null, null)
          ! Collections.disjoint(preAdjInflections, slaveInflections) ) {
        logException();
        return true;
      } 
    }

    // not an exception
    return false;
  }


  private static boolean genderMatches(List<InflectionHelper.Inflection> masterInflections, List<InflectionHelper.Inflection> slaveInflections, String masterCaseFilter, String slaveCaseFilter) {
    for (InflectionHelper.Inflection masterInflection : masterInflections) {
      for (InflectionHelper.Inflection slaveInflection : slaveInflections) {
        if( (masterCaseFilter == null || masterInflection._case.equals(masterCaseFilter))
            && (slaveCaseFilter == null || slaveInflection._case.equals(slaveCaseFilter))
            && slaveInflection.gender.equals(masterInflection.gender) ) 
          return true;
      }
    }
    return false;
  }

  private static boolean reverseConjAdvFind(AnalyzedTokenReadings[] tokens, int pos, int depth) {
    for(int i=pos; i>pos-depth && i>=2; i--) {

      if( CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[i].getToken().toLowerCase())
          && (PosTagHelper.hasPosTag(tokens[i-1], "adv(?!p).*")
              || PosTagHelper.hasPosTag(tokens[i+1], "(adv(?!p)|part).*")) ) {
        return true;
      }

      if( PosTagHelper.hasPosTagPart(tokens[i], "verb") )
        return false;
    }

    return false;
  }

  private static boolean reverseConjFind(AnalyzedTokenReadings[] tokens, int pos, int depth) {
    for(int i=pos; i>pos-depth && i>=1; i--) {

      if( CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[i].getToken().toLowerCase()) ) {

        if( i < 2
            || (! PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("(adj|conj:coord).*")) ) )
                
          return false;

        return true;
      }

      if( i >= 1
          && ! PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("(adj|conj:coord|num|prep|adv(?!p)).*"))
          && ! tokens[i-1].getToken().equals(",")
           )
        return false;
    }

    return false;
  }

  private static boolean reverseConjFind2(AnalyzedTokenReadings[] tokens, int pos, int depth) {
    for(int i=pos; i>pos-depth && i>=1; i--) {

      if( CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[i].getToken().toLowerCase()) ) {

        if( i < 2
            || ( (! tokens[i-1].hasPosTag("number") || ! PosTagHelper.hasPosTag(tokens[i+1], Pattern.compile("adj.*?&numr.*"))) // 1, 2 та 3-й 
                && ! tokens[i-1].getToken().equals(",") )
                && ! tokens[i-1].getToken().matches(".*[–-]")   // дво- і тривимірний формати
                && ! tokens[i-1].getToken().matches("[)»”]")   // 1-й (...) та 2-й ряди
                && (! tokens[i-1].getToken().equals("/") || ! tokens[i].getToken().equals("або"))
                && ! PosTagHelper.isUnknownWord(tokens[i-1])
                )
          return false;

        return true;
      }

      if( i >= 1
          && ! PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("(adj|conj:coord|num|prep|adv(?!p)).*"))
          && ! tokens[i-1].getToken().equals(",")
           )
        return false;
    }

    return false;
  }


  private static boolean checkTextInSent(AnalyzedTokenReadings[] tokens, int pos, String text) {
    String[] words = text.split(" ");
    for(int i=0; i<words.length && i+pos < tokens.length; i++) {
      if( ! tokens[i+pos].getToken().equalsIgnoreCase(words[i]) )
        return false;
    }
    return true;
  }


  private static boolean forwardConjFind(AnalyzedTokenReadings[] tokens, int pos, int depth) {
    for(int i=pos; i<tokens.length && i<= pos+depth; i++) {
      if( CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[i].getToken().toLowerCase()) ) {

        // check 2nd part of plural
        if( i < tokens.length-3
            && checkTextInSent(tokens, i+1, "а також")
            && PosTagHelper.hasPosTag(tokens[i+3], Pattern.compile("(noun|adj|num|adv(?!p)).*") ) )
          return true;

        if( i==tokens.length-1
            || (! PosTagHelper.hasPosTag(tokens[i+1], Pattern.compile("(noun|adj|num|adv(?!p)).*"))
                && ! StringTools.isCapitalizedWord(tokens[i+1].getToken())
                && ! tokens[i+1].getToken().matches("[\"«“„]") ) )
          return false;

        return true;
      }

      if( ! PosTagHelper.hasPosTag(tokens[i], Pattern.compile("(noun|adj|prep|adv(?!p)|number:latin).*"))
            && ! StringTools.isCapitalizedWord(tokens[i].getToken()) )  // for unknown last names: згадувані Костянтин Скоркін та Олена Заславська
        return false;
    }

    return false;
  }

  private static boolean caseGovernmentMatches(List<AnalyzedToken> adjTokenReadings, List<InflectionHelper.Inflection> slaveInflections) {
    // TODO: key tags (e.g. pos) should be part of the map key
    // but now we pass only adj token readings so it's ok
    return adjTokenReadings.stream().map(p -> p.getLemma()).distinct().anyMatch( item -> {
      Set<String> inflections = CaseGovernmentHelper.CASE_GOVERNMENT_MAP.get( item );
      //        System.err.println("Found inflections " + item + ": " + inflections);
      if( inflections != null ) {
        // TODO: shall we check for ranim/rinanim or is it overkill?
        for (InflectionHelper.Inflection inflection : slaveInflections) {
          if( inflections.contains(inflection._case) )
            return true;
        }
      }
      return false;
    }
        );
  }

  
  private static boolean hasOverlapIgnoreGender(List<InflectionHelper.Inflection> masterInflections, List<InflectionHelper.Inflection> slaveInflections) {
    return hasOverlapIgnoreGender(masterInflections, slaveInflections, null, null);
  }
  
  private static boolean hasOverlapIgnoreGender(List<InflectionHelper.Inflection> masterInflections, List<InflectionHelper.Inflection> slaveInflections,
      String masterGenderFilter, String slaveGenderFilter) {
  
    for (InflectionHelper.Inflection mInflection : masterInflections) {
      if( masterGenderFilter != null && ! mInflection.gender.equalsIgnoreCase(masterGenderFilter) )
        continue;

      for(InflectionHelper.Inflection sInflection : slaveInflections) {
        if( slaveGenderFilter != null && ! sInflection.gender.equalsIgnoreCase(slaveGenderFilter) )
          continue;

        if( mInflection.equalsIgnoreGender(sInflection) )
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


  // #1 Із «Теоретичної граматики» (с.173):
  //    
  //    "Якщо в числівниково-іменникових конструкціях із числівниками два, три,  
  //    чотири (а також зі складеними числівниками, де кінцевими компонентами  
  //    виступають два, три, чотири) у формах називного — знахідного відмінка множини
  //    вживаються прикметники, дієприкметники або займенникові прикметники, то
  //    ці означальні компоненти або узгоджуються з іменником, набуваючи форм  
  //    відповідно називного чи знахідного відмінка множини, або функціонують у  
  //    формі родового відмінка множини, напр.: Тенор переплітається з сопраном,  
  //    неначе дві срібні нитки (І. Нечуй-Левицький,); Дві людських руки вкупі— се кільце,
  //    за яке, ухопившися, можна зрушити землю (Ю. Яновський)."


}
