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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.6
 */
final class TokenAgreementAdjNounExceptionHelper {
  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementAdjNounExceptionHelper.class);

  private static final Pattern NUMBER_V_NAZ = Pattern.compile("number|numr:p:v_naz|noun.*?:p:v_naz:&numr.*");
  // including latin 'a' and 'i' so the rules don't trip on them in Ukrainian sentences
  static final List<String> CONJ_FOR_PLURAL_WITH_COMMA = Arrays.asList("і", "а", "й", "та", "чи", "або", "ані", "також", "плюс", "то", "a", "i", ",");
  static final List<String> CONJ_FOR_PLURAL = Arrays.asList("і", "а", "й", "та", "чи", "або", "ані", "також", "то", "a", "i");
  static final Pattern CONJ_FOR_PLURAL_PATTERN = Pattern.compile(StringUtils.join(CONJ_FOR_PLURAL, "|"));
  static final Pattern CONJ_FOR_PLURAL_WITH_COMMA_PATTERN = Pattern.compile(StringUtils.join(CONJ_FOR_PLURAL_WITH_COMMA, "|"));
  private static final Pattern DOVYE_TROYE = Pattern.compile(".*[2-4]|.*[2-4][\u2013\u2014-].*[2-4]|два|обидва|двоє|три|троє|чотири|один[\u2013\u2014-]два|два[\u2013\u2014-]три|три[\u2013\u2014-]чотири|двоє[\u2013\u2014-]троє|троє[\u2013\u2014-]четверо");
  private static final Pattern VERB_NOT_INSERT_PATTERN = Pattern.compile("verb(?!.*insert)");


  private TokenAgreementAdjNounExceptionHelper() {
  }


  public static boolean isException(AnalyzedTokenReadings[] tokens, int adjPos, int nounPos, 
      List<InflectionHelper.Inflection> masterInflections, List<InflectionHelper.Inflection> slaveInflections, 
      List<AnalyzedToken> adjTokenReadings, List<AnalyzedToken> slaveTokenReadings) {

    AnalyzedTokenReadings adjAnalyzedTokenReadings = tokens[adjPos];


    if( adjPos > 1
        && LemmaHelper.isCapitalized(tokens[adjPos].getCleanToken())
        && LemmaHelper.isCapitalized(tokens[adjPos-1].getCleanToken())
        && (LemmaHelper.hasLemma(tokens[adjPos], "вітчизняний") || LemmaHelper.hasLemma(tokens[adjPos], "житомирський"))
        && LemmaHelper.hasLemma(tokens[adjPos-1], "великий")
        && ! LemmaHelper.hasLemma(tokens[nounPos], "війна") ) {
      logException();
      return true;
    }

    if( adjPos > 1
        && LemmaHelper.hasLemma(tokens[adjPos], "національний")
        && LemmaHelper.hasLemma(tokens[adjPos-1], "перший")
        && Character.isUpperCase(tokens[adjPos].getToken().charAt(0))
        && Character.isUpperCase(tokens[adjPos-1].getToken().charAt(0))) {
      logException();
      return true;
    }

    // по Підвальній трамваї можуть
    // TODO: забагато FN
//    if( adjPos > 1
//        && PosTagHelper.hasPosTagStart(tokens[adjPos-1], "prep")
//        && LemmaHelper.isCapitalized(tokens[adjPos].getCleanToken())
//        && ! LemmaHelper.isCapitalized(tokens[nounPos].getCleanToken())
//        && PosTagHelper.hasPosTagPart(tokens[adjPos], "v_mis") ) {
//      logException();
//      return true;
//    }

    if( LemmaHelper.hasLemma(tokens[adjPos], "північний")
        && LemmaHelper.hasLemma(tokens[nounPos], "Рейн-Вестфалія") ) {
      logException();
      return true;
    }

    //  в день Хрещення Господнього священики
    if( LemmaHelper.hasLemma(tokens[adjPos], Arrays.asList("божий", "господній", "Христовий"))
        && Character.isUpperCase(tokens[adjPos].getToken().charAt(0))) {
      logException();
      return true;
    }

    // князівством Литовським подоляни
    if( adjPos > 1
        && PosTagHelper.hasPosTagPart(tokens[adjPos-1], "noun")
        && Character.isUpperCase(tokens[adjPos].getToken().charAt(0))  //TODO: 2nd char is lowercase?
        && ! Collections.disjoint(masterInflections, InflectionHelper.getNounInflections(tokens[adjPos-1].getReadings())) ) {
      logException();
      return true;
    }

    // 5-а клас
    if( Pattern.compile("([1-9]|1[0-2])[\u2018-][а-д]").matcher(adjAnalyzedTokenReadings.getToken()).matches()
        && LemmaHelper.hasLemma(tokens[nounPos], "клас") ) {
      logException();
      return true;
    }

    // we add pos "number" in disambiguation
//    // маршрутка номер 29-а фірми
//    if( i > 2
//        && Pattern.compile("[0-9]+[\u2018-][а-яіїєґ]").matcher(adjAnalyzedTokenReadings.getToken()).matches()
//        && LemmaHelper.hasLemma(tokens[adjPos-1], Arrays.asList("номер", "пункт", "№")) ) {
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
    if( nounPos > 1
        && LemmaHelper.hasLemma(adjAnalyzedTokenReadings, Arrays.asList("перший")) 
        && ! LemmaHelper.hasLemma(tokens[nounPos], TokenAgreementAdjNounRule.FAKE_FEM_LIST, "noun:inanim:m:") ) {
      //                && PosTagHelper.hasPosTag(slaveTokenReadings, ".*v_naz.*")) ) {
      logException();
      return true;
    }

    // абзац другий частини першої
    // пункт третій рішення міськради
    if( adjPos > 1 && nounPos < tokens.length -1
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, Pattern.compile("adj:[mf]:.*&numr.*|number.*")) 
        && PosTagHelper.hasPosTag(slaveTokenReadings, Pattern.compile("noun:inanim:.:v_rod.*"))
//        && PosTagHelper.hasPosTag(tokens[i+1], Pattern.compile("adj:.:v_rod.*&numr.*|number.*"))
        && LemmaHelper.hasLemma(tokens[adjPos-1], Arrays.asList("абзац", "розділ", "пункт", "підпункт", "частина", "стаття"))
//        && LemmaHelper.hasLemma(tokens[i], Arrays.asList("абзац", "розділ", "пункт", "підпункт", "частина", "стаття"))
        ) { 
      logException();
      return true;
    }

    // статтю 6-ту закону
    if( adjPos > 1
        && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "num")
        && LemmaHelper.hasLemma(tokens[adjPos-1], "стаття")
        && ! Collections.disjoint(masterInflections, InflectionHelper.getNounInflections(tokens[adjPos-1].getReadings())) ) {
      logException();
      return true;
    }

    // лава запасних партії
    if( adjPos > 1
        && tokens[adjPos].getToken().equals("запасних")
        && LemmaHelper.hasLemma(tokens[adjPos-1], "лава")) {
      logException();
      return true;
    }

    // старший зміни
    if( Arrays.asList("зміни", "групи").contains(tokens[nounPos].getCleanToken())
        && LemmaHelper.hasLemma(tokens[adjPos], "старший")) {
      logException();
      return true;
    }

    // на повну людей розкрутили
    if( adjPos > 1
        && tokens[adjPos].getToken().equals("повну")
        && tokens[adjPos-1].getToken().equalsIgnoreCase("на")) {
      logException();
      return true;
    }

    // у Другій світовій участь
    if( adjPos > 1
        && LemmaHelper.hasLemma(tokens[adjPos], Arrays.asList("світовий"), ":f:")
        && LemmaHelper.hasLemma(tokens[adjPos-1], Arrays.asList("другий", "перший"), ":f:") ) {
      logException();
      return true;
    }

    // знайдений увечері понеділка
    if( nounPos > 1
        && LemmaHelper.hasLemma(tokens[nounPos-1], Arrays.asList("увечері", "уранці", "ввечері", "вранці"))
        && PosTagHelper.hasPosTag(tokens[nounPos], Pattern.compile("noun.*v_rod.*")) ) {
      logException();
      return true;
    }

    // площею 100 кв. м
    // довжиною до 500
    if( nounPos < tokens.length -1
        && Arrays.asList("площею", "об'ємом", "довжиною", "висотою", "зростом").contains(tokens[nounPos].getToken())
        && PosTagHelper.hasPosTag(tokens[nounPos+1], "prep.*|.*num.*") ) {
      logException();
      return true;
    }

    // 10 метрів квадратних води
    if( adjPos > 1
        && LemmaHelper.hasLemma(tokens[adjPos-1], Pattern.compile(".*метр.*"))
        && LemmaHelper.hasLemma(tokens[adjPos], Pattern.compile("квадратний|кубічний"))
        && PosTagHelper.hasPosTagPart(tokens[nounPos], "v_rod") ) {
      logException();
      return true;
    }

    // молодшого гвардії сержанта
    if( nounPos < tokens.length - 1
        && tokens[nounPos].getToken().equals("гвардії")
        && PosTagHelper.hasPosTag(tokens[nounPos+1], "noun.*")
        && ! Collections.disjoint(masterInflections, InflectionHelper.getNounInflections(tokens[nounPos+1].getReadings())) ) {
      logException();
      return true;
    }

    // 200% річних прибутку
    if( adjPos > 1
        && tokens[adjPos-1].getToken().endsWith("%") 
        && tokens[adjPos].getToken().equals("річних") ) {
      logException();
      return true;
    }

    // пасли задніх
    if( adjPos > 1
        && LemmaHelper.hasLemma(tokens[adjPos-1], "пасти")
        && tokens[adjPos].getToken().equals("задніх") ) {
      logException();
      return true;
    }

    // не мати рівних
    if( adjPos > 1
        && LemmaHelper.hasLemma(tokens[adjPos-1], "мати")
        && tokens[adjPos].getToken().equals("рівних") ) {
      logException();
      return true;
    }

    // taken care by barbarism rule
    // на манер
    if( adjPos > 1 
        && tokens[nounPos].getToken().equals("манер")
        && tokens[adjPos-1].getToken().equalsIgnoreCase("на") ) {
      logException();
      return true;
    }

    // taken care by barbarism rule
    // усі до єдиного
    if( adjPos > 2
        && tokens[adjPos].getToken().equals("єдиного")
        && tokens[adjPos-1].getToken().equals("до")
        && LemmaHelper.hasLemma(tokens[adjPos-2], Arrays.asList("весь", "увесь"), ":p:") ) {
      logException();
      return true;
    }

    // сильні світу цього
    if( nounPos < tokens.length -1
        && Arrays.asList("миру", "світу").contains(tokens[nounPos].getCleanToken())
        && ( LemmaHelper.hasLemma(adjAnalyzedTokenReadings, Arrays.asList("сильний", "могутній", "великий"))
          || LemmaHelper.hasLemma(tokens[nounPos+1], Arrays.asList("цей", "сей"), ":m:v_rod") ) ) {
      logException();
      return true;
    }

    // колишня Маяковського
    if( LemmaHelper.hasLemma(tokens[adjPos], Arrays.asList("колишній", "тодішній", "теперішній", "нинішній"), Pattern.compile("adj.*:f:.*")) 
        && Character.isUpperCase(tokens[nounPos].getToken().charAt(0)) ) {
      logException();
      return true;
    }

    // імені Шевченка
    // 4-й Запорізький ім. гетьмана Б. Хмельницького
    if( nounPos < tokens.length -1
        && Arrays.asList("ім.", "імені", "ордена").contains(tokens[nounPos].getToken()) ) { 
//        && Character.isUpperCase(tokens[i+1].getToken().charAt(0)) ) {
      logException();
      return true;
    }

    // на дівоче Анна
    if( Arrays.asList("дівоче").contains(tokens[adjPos].getToken()) 
        && PosTagHelper.hasPosTagPart(tokens[nounPos], "name") ) {
      logException();
      return true;
    }

    // вольному воля
    if( Arrays.asList("вольному", "вільному").contains(adjAnalyzedTokenReadings.getToken().toLowerCase())
        && tokens[nounPos].getToken().equals("воля") ) {
      logException();
      return true;
    }

    // порядок денний
    if( adjPos > 1
        && LemmaHelper.hasLemma(adjAnalyzedTokenReadings, "денний")
        && LemmaHelper.hasLemma(tokens[adjPos-1], "порядок")
        && ! Collections.disjoint(masterInflections, InflectionHelper.getNounInflections(tokens[adjPos-1].getReadings())) ) {
      logException();
      return true;
    }

    // Вони здатні екскаватором переорювати
    if( LemmaHelper.hasLemma(tokens[adjPos], Arrays.asList("здатний", "змушений", "винний", "повинний", "готовий", "спроможний")) ) {
      logException();
      return true;
    }

    // протягом минулих травня – липня
    if( nounPos < tokens.length - 2
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:.*")
        && tokens[nounPos+1].getToken().matches("[\u2014\u2013-]")
        && PosTagHelper.hasPosTag(tokens[nounPos+2], "(adj|noun).*")
        //TODO: hasOverlapIgnoreGender(masterInflections, tokens[i+2])
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // моїх маму й сестер
    if( nounPos < tokens.length - 2
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:.*")
        && forwardConjFind(tokens, nounPos+1, 2)
        && hasOverlapIgnoreGender(masterInflections, slaveInflections, "p", null) ) {
      logException();
      return true;
    }

    // зв'язаних ченця з черницею
    // на зарубаних матір з двома синами
    if( nounPos < tokens.length - 2
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:.*")
        && Arrays.asList("з", "із", "зі").contains(tokens[nounPos+1].getToken())
        && PosTagHelper.hasPosTag(tokens[nounPos+2], "(noun|numr).*:v_oru.*")
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // навчальної та середньої шкіл
    if( adjPos > 2 
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun:.*:p:.*")
        && (reverseConjFind(tokens, adjPos-1, 3) || reverseConjAdvFind(tokens, adjPos-1, 3))
        && hasOverlapIgnoreGender(masterInflections, slaveInflections, null, "p") 
        && LemmaHelper.reverseSearch(tokens, adjPos-2, 100, null, Pattern.compile("adj.*")) ) {
      logException();
      return true;
    }

    // Большого та Маріїнського театрів
    // Пляжі 3, 4 і 5-ї категорій
    if( adjPos > 2 
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun:.*:p:.*")
        && (reverseConjFind2(tokens, adjPos-1, 3) )
        && hasOverlapIgnoreGender(masterInflections, slaveInflections, null, "p") ) {
      logException();
      return true;
    }

    // ні у методологічному, ні у практичному аспектах
    if( adjPos > 6
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun:.*:p:.*")
        && PosTagHelper.hasPosTag(tokens[adjPos], "adj:.*")
        && PosTagHelper.hasPosTag(tokens[adjPos-1], "prep.*")
        && LemmaHelper.hasLemma(tokens[adjPos-2], Arrays.asList("ні", "ані", "хоч", "що", "як"))
        && tokens[adjPos-3].getToken().equals(",")
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    Pattern afterPredicVerbTags = Pattern.compile(".*(inf|past:n|futr:s:3).*");
    if( nounPos < tokens.length - 1 
        && PosTagHelper.hasPosTagPart(tokens[nounPos], "predic")
        && (PosTagHelper.hasPosTag(tokens[nounPos+1], afterPredicVerbTags)
           || nounPos < tokens.length - 2
                && PosTagHelper.hasPosTagStart(tokens[nounPos+1], "adv")
                && PosTagHelper.hasPosTag(tokens[nounPos+2], afterPredicVerbTags)) ) {
      logException();
      return true;
    }
    
    // на проурядову і, здається, пропрезидентську частини
    if( adjPos > 5
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun:.*:p:.*")
        && PosTagHelper.hasPosTag(tokens[adjPos], "adj:.*")
        && tokens[adjPos-1].getToken().equals(",")
        && ( (tokens[adjPos-3].getToken().equals(",") && CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[adjPos-4].getToken().toLowerCase())
                && ! PosTagHelper.hasPosTag(tokens[adjPos-2], VERB_NOT_INSERT_PATTERN))
            || (tokens[adjPos-4].getToken().equals(",") && CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[adjPos-5].getToken().toLowerCase())
                            && ! PosTagHelper.hasPosTag(tokens[adjPos-2], VERB_NOT_INSERT_PATTERN)
                            && ! PosTagHelper.hasPosTag(tokens[adjPos-3], VERB_NOT_INSERT_PATTERN)) )
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // коринфський з іонійським ордери
    if( adjPos > 2 
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun:.*:p:.*")
        && tokens[adjPos-1].getToken().matches("з|із|зі")
        && PosTagHelper.hasPosTag(tokens[adjPos], "adj.*v_oru.*")
        && hasOverlapIgnoreGender(InflectionHelper.getAdjInflections(tokens[adjPos-2].getReadings()), slaveInflections) ) {
      logException();
      return true;
    }

    // на довгих півстоліття
    if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:v_rod.*")
        && tokens[nounPos].getToken().startsWith("пів")
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun.*v_rod.*") ) {
      logException();
      return true;
    }

    // на довгих чверть століття
    if( nounPos < tokens.length-1
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj:p:v_rod.*")
        && tokens[nounPos].getToken().equals("чверть")
        && PosTagHelper.hasPosTag(tokens[nounPos+1], "noun.*v_rod.*") ) {
      logException();
      return true;
    }

    // розділеного вже чверть століття
    // створених близько чверті століття
    if( nounPos < tokens.length-1
        && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "adjp")
        && LemmaHelper.hasLemma(tokens[nounPos], Arrays.asList("чверть", "третина"))
        && PosTagHelper.hasPosTag(tokens[nounPos+1], "noun.*v_rod.*") ) {
      logException();
      return true;
    }

    // заклопотані чимало людей
    // NOTE: мало abmigs with verb
    if( PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "adjp")
        && LemmaHelper.hasLemma(tokens[nounPos-1], Arrays.asList("чимало", "багато", "небагато", "немало", /*"мало",*/ "обмаль"))
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun.*:p:v_rod.*") ) {
      logException();
      return true;
    }

    // присудок ж.р. + професія ч.р.
    if( Arrays.asList("переконана", "впевнена", "упевнена", "годна", "ладна", "певна", "причетна", "обрана", "призначена").contains(adjAnalyzedTokenReadings.getToken())
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun:anim:m:v_naz.*") ) {
      logException();
      return true;
    }

    // чинних станом на
    if( nounPos < tokens.length-1
        && tokens[nounPos].getToken().equals("станом")
        && tokens[nounPos+1].getToken().equals("на") ) {
      logException();
      return true;
    }
    
    // постійно на рівних міністри, президенти
    if( adjPos > 1
        && tokens[adjPos].getToken().equals("рівних")
        && tokens[adjPos-1].getToken().equalsIgnoreCase("на") ) {
      logException();
      return true;
    }

    // польські зразка 1620—1650 років
    if( nounPos < tokens.length-1
        && tokens[nounPos].getToken().equals("зразка") ) {
      logException();
      return true;
    }

    // три зелених плюс два червоних
    if( Arrays.asList("мінус", "плюс").contains(tokens[nounPos].getToken()) ) {
      logException();
      return true;
    }

    // важкими пару років
    // неконституційними низку законів
    // природний тисячею років підтверджений
    if( nounPos < tokens.length-1 
        && LemmaHelper.hasLemma(tokens[nounPos], Arrays.asList("пара", "низка", "ряд", "купа", "більшість", "десятка", "сотня", "тисяча", "мільйон"))
        && (PosTagHelper.hasPosTag(tokens[nounPos+1], "noun.*?:p:v_rod.*")
          || (nounPos < tokens.length-2
            && PosTagHelper.hasPosTag(tokens[nounPos+1], "adj:p:v_rod.*")
            && PosTagHelper.hasPosTag(tokens[nounPos+2], "noun.*?:p:v_rod.*")) ) ) {
      logException();
      return true;
    }

    // разів (у) десять
    if( nounPos < tokens.length-1
        && LemmaHelper.hasLemma(tokens[nounPos], Arrays.asList("раз"), Pattern.compile(".*p:v_(naz|rod).*"))
        && (PosTagHelper.hasPosTag(tokens[nounPos+1], "number|numr:p:v_naz|noun.*?:p:v_naz:&numr.*")
            || PosTagHelper.hasPosTagPart(tokens[nounPos+1], "prep")) ) {
      logException();
      return true;
    }

    // років 6, відсотків зо два
    if( nounPos < tokens.length-1
        && LemmaHelper.hasLemma(tokens[nounPos], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun.*?p:v_(naz|rod).*"))
        && (PosTagHelper.hasPosTag(tokens[nounPos+1], NUMBER_V_NAZ)
            || (nounPos < tokens.length-2
              && LemmaHelper.hasLemma(tokens[nounPos+1], Arrays.asList("на", "за", "з", "із", "зо", "через", "під"), "prep")
                && PosTagHelper.hasPosTag(tokens[nounPos+2], NUMBER_V_NAZ))) ) {
      logException();
      return true;
    }

    // осіб на 30
    if( nounPos < tokens.length-2
        && LemmaHelper.hasLemma(tokens[nounPos], Arrays.asList("особа"), Pattern.compile("noun.*?p:v_(naz|rod).*"))
        && LemmaHelper.hasLemma(tokens[nounPos+1], Arrays.asList("на", "з", "із", "зо", "під"), "prep")
        && PosTagHelper.hasPosTag(tokens[nounPos+2], NUMBER_V_NAZ) ) {
      logException();
      return true;
    }

    // хвилини з 55-ї вірмени почали
    if( adjPos > 2
        && LemmaHelper.hasLemma(tokens[adjPos-2], LemmaHelper.TIME_LEMMAS_SHORT)
        && PosTagHelper.hasPosTagStart(tokens[adjPos-1], "prep")
        && PosTagHelper.hasPosTagPart(tokens[adjPos], "num")) {

      Collection<String> prepGovernedCases = CaseGovernmentHelper.getCaseGovernments(tokens[adjPos-1], IPOSTag.prep.name());
      if( TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[adjPos-2])
          && TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[adjPos]) ) {
        logException();
        return true;
      }
    }

    // пофарбований рік тому
    // TODO: переміщені вже місяць
    if( nounPos < tokens.length-1
        && LemmaHelper.hasLemma(tokens[nounPos], LemmaHelper.TIME_LEMMAS) 
        && LemmaHelper.hasLemma(tokens[nounPos+1], "тому") ) {
      logException();
      return true;
    }

    // замість звичного десятиліттями
    if( nounPos < tokens.length-1
        && LemmaHelper.hasLemma(tokens[nounPos], LemmaHelper.TIME_PLUS_LEMMAS, Pattern.compile("noun:inanim:p:v_oru.*")) ) {
      logException();
      return true;
    }

    
    // кількох десятих відсотка
    if( LemmaHelper.hasLemma(tokens[adjPos], Arrays.asList("десятий", "сотий", "тисячний", "десятитисячний", "стотитисячний", "мільйонний", "мільярдний"))
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, ".*:[fp]:.*")
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun.*v_rod.*") ) {
      logException();
      return true;
    }


    // два нових горнятка (див. #1 нижче)
    // два відомих імені
    // 33 народних обранці
    if( adjPos > 1 && nounPos < tokens.length
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, ".*:p:v_(rod|naz).*")
        && LemmaHelper.reverseSearch(tokens, adjPos-1, 5, DOVYE_TROYE, null)
//        && ( LemmaHelper.hasLemma(tokens[adjPos-1], DOVYE_TROYE)
//            // три жовтих обірваних чоловіки
//            // три предкові слов’янські племені
//            || (i>2 && LemmaHelper.hasLemma(tokens[adjPos-2], DOVYE_TROYE) 
//                && (PosTagHelper.hasPosTag(tokens[adjPos-1], "adv.*|adj.*:p:v_(rod|naz).*")
//                    // два «круглих столи»
//                    || tokens[adjPos-1].getToken().matches("[«„\"]"))) ) 
        && (PosTagHelper.hasPosTag(tokens[nounPos], ".*(:p:v_naz|:n:v_rod).*") 
            || Arrays.asList("імені", "ока").contains(tokens[nounPos].getToken())) ) {
      logException();
      return true;
    }


    // 1-3-й класи
    // на сьомому–восьмому поверхах
    if( (adjAnalyzedTokenReadings.getCleanToken().matches("[0-9]+[\u2014\u2013-][0-9]+[\u2013-][а-яіїєґ]{1,3}")
        || (adjAnalyzedTokenReadings.getCleanToken().matches(".*[а-яїієґ][\u2014\u2013-].*")
            && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "&numr"))) 
        && PosTagHelper.hasPosTagPart(slaveTokenReadings, ":p:")
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }
    // восьмого – дев’ятого класів
    if( nounPos > 2 
        && Arrays.asList("\u2013", "\u2014").contains(tokens[adjPos-1].getToken())
        && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "num")
        && PosTagHelper.hasPosTagPart(tokens[adjPos-2], "num")
        && PosTagHelper.hasPosTagPart(slaveTokenReadings, ":p:")
        && (PosTagHelper.hasPosTagStart(tokens[adjPos-2], "number")
              || hasOverlapIgnoreGender(InflectionHelper.getAdjInflections(tokens[adjPos-2].getReadings()), slaveInflections))
        && hasOverlapIgnoreGender(masterInflections, slaveInflections) ) {
      logException();
      return true;
    }

    // найближчі рік-два
    // понаднормові годину-півтори
    // суперкризовими січнем–лютим
//    if( LemmaHelper.hasLemma(adjAnalyzedTokenReadins, Arrays.asList("найближчий", "минулий"), ":p:") 
    if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*:p:.*") 
        && tokens[nounPos].getToken().matches(".*[\u2014\u2013-].*")
        && (LemmaHelper.TIME_PLUS_LEMMAS.contains(tokens[nounPos].getAnalyzedToken(0).getLemma().split("[\u2014\u2013-]")[0])
        // does not work for тиждень-два due to dynamic tagging returning singular
          || hasOverlapIgnoreGender(masterInflections, slaveInflections)) ) {
      logException();
      return true;
    }

    // Від наступних пари десятків
    if( nounPos < tokens.length - 1
        && LemmaHelper.hasLemma(tokens[nounPos], "пара")
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*:p:.*")
        && PosTagHelper.hasPosTag(tokens[nounPos+1], ".*:p:v_rod.*") ) {      // adding "num" fails "десятків" тощо 
      logException();
      return true;
    }

    if( nounPos > 1
        && PosTagHelper.hasPosTagPart(tokens[adjPos-1], "num")
        && PosTagHelper.hasPosTag(tokens[adjPos], "adj.*num.*")
        ) {
      // п'ять шостих світу
      if( PosTagHelper.hasPosTag(tokens[adjPos-1], "(noun|numr).*") 
          && PosTagHelper.hasPosTag(tokens[adjPos], "adj:p:v_rod.*") ) {
        
        // (вона й) дві других дівчини
        if( LemmaHelper.hasLemma(tokens[adjPos], "другий") 
            && ! LemmaHelper.hasLemma(tokens[adjPos-1], "один") )
          return false;
        
        logException();
        return true;
      }
      // одній восьмій
      if( PosTagHelper.hasPosTag(tokens[adjPos-1], "adj:f:.*pron.*")
          && LemmaHelper.hasLemma(tokens[adjPos-1], "один")
          && ! Collections.disjoint(
              InflectionHelper.getAdjInflections(tokens[adjPos-1].getReadings()),
              InflectionHelper.getAdjInflections(tokens[adjPos].getReadings())) ) {
        logException();
        return true;
      }
    }
    
    // 1/8-ї фіналу
    if( nounPos > 3
        && "/".equals(tokens[adjPos-1].getToken())
        && PosTagHelper.hasPosTagPart(tokens[adjPos-2], "numb")
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
      if( nounPos > 1 
          && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, ":f:")
          && LemmaHelper.hasLemma(tokens[adjPos-1], Arrays.asList("на", "в", "у", "за", "о", "до", "після", "близько", "раніше"))
          && ! LemmaHelper.hasLemma(tokens[nounPos], Arrays.asList("хвилина", "година")) ) {
        logException();
        return true;
      }
      // 11-й ранку
      // Arrays.asList("ранок", "день", "вечір", "ніч", "пополудень") + "v_rod"
      if( PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, ":f:") 
          && tokens[nounPos].getToken().matches("ранку|дня|вечора|ночі|пополудня") ) {
        logException();
        return true;
      }
      // дев'яте травня
      if( PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, ":n:")
          && LemmaHelper.hasLemma(tokens[nounPos], LemmaHelper.MONTH_LEMMAS, "v_rod") ) {
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
    if( nounPos > 2 && nounPos <= tokens.length - 1
        && LemmaHelper.hasLemma(tokens[adjPos-1], Arrays.asList("ніщо", "щось", "ніхто", "хтось"))
//        && PosTagHelper.hasPosTag(tokens[adjPos], "adj:.*v_rod.*")
        // we now have gender for pron
        && ! Collections.disjoint(InflectionHelper.getNounInflections(tokens[adjPos-1].getReadings()), masterInflections)
        //&& tokens[i+1].getToken().equals("не")
        ) {
      logException();
      return true;
    }

    // визнання неконституційним закону
    // визнання тут шкідливою орієнтацію
    if( adjPos > 1
        && LemmaHelper.revSearch(tokens, adjPos-1, Pattern.compile(".*(ння|ття)"), null)
        && PosTagHelper.hasPosTag(tokens[adjPos], "adj.*:v_oru.*")
        && PosTagHelper.hasPosTag(tokens[nounPos], "noun:.*:v_rod.*") 
        && genderMatches(masterInflections, slaveInflections, "v_oru", "v_rod") ) {
      logException();
      return true;
    }
   
    
    int verbPos = LemmaHelper.revSearchIdx(tokens, adjPos-1, Pattern.compile("бути|ставати|стати|залишатися|залишитися"), null);
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
                || VerbInflectionHelper.inflectionsOverlap(tokens[verbPos].getReadings(), tokens[nounPos].getReadings()) ) {
                  logException();
                  return true;
            }
          }
          // Стали дорожчими хліб чи бензин
          else if( nounPos < tokens.length -1 
              && PosTagHelper.hasPosTagPart(tokens[adjPos], "adj:p:")
              && CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[nounPos+1].getToken().toLowerCase()) ) {
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

    verbPos = LemmaHelper.revSearchIdx(tokens, adjPos-1, null, "verb.*");
    if( verbPos != -1 ) {
      if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*v_oru.*") ) {
        // визнали справедливою наставники обох команд
        if( PosTagHelper.hasPosTag(slaveTokenReadings, "noun.*v_naz.*")
              && VerbInflectionHelper.inflectionsOverlap(tokens[verbPos].getReadings(), tokens[nounPos].getReadings()) ) {
          logException();
          return true;
        }
      }
    }

    
    // помальована в (усе) біле кімната
    if( adjPos > 2
        && Arrays.asList("біле", "чорне", "оранжеве", "червоне", "жовте", "синє", "зелене", "фіолетове").contains(tokens[adjPos].getToken())
        && Arrays.asList("в", "у").contains(tokens[adjPos-1].getToken())
        && PosTagHelper.hasPosTagPart(tokens[adjPos-2], "adjp:pasv") ) {

      List<InflectionHelper.Inflection> masterInflections_ = InflectionHelper.getAdjInflections(tokens[adjPos-2].getReadings());
      if( ! Collections.disjoint(masterInflections_, slaveInflections) ) {
        logException();
        return true;
      }
    }
    if( adjPos > 3
        && Arrays.asList("біле", "чорне").contains(tokens[adjPos].getToken())
        && Arrays.asList("усе", "все").contains(tokens[adjPos-1].getToken())
        && Arrays.asList("в", "у").contains(tokens[adjPos-2].getToken())
        && PosTagHelper.hasPosTagPart(tokens[adjPos-3], "adjp:pasv") ) {

      List<InflectionHelper.Inflection> masterInflections_ = InflectionHelper.getAdjInflections(tokens[adjPos-3].getReadings());
      if( ! Collections.disjoint(masterInflections_, slaveInflections) ) {
        logException();
        return true;
      }
    }

    // повторена тисячу разів
    if( nounPos < tokens.length - 1
        && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "adjp:pasv")
        && Arrays.asList("тисячу", "сотню", "десятки").contains(tokens[nounPos].getToken())
        && Arrays.asList("разів", "раз", "років").contains(tokens[nounPos+1].getToken()) ) {
      logException();
      return true;
    }


    if( adjPos > 2 ) {
//      // порівняно з попереднім
//      if( PosTagHelper.hasPosTag(adjAnalyzedTokenReadins, "adj.*v_oru")
//          && LemmaHelper.hasLemma(tokens[adjPos-2], Arrays.asList("порівняно", "аналогічно")) 
//          && LemmaHelper.hasLemma(tokens[adjPos-1], Pattern.compile("з|із|зі")) ) {
//        logException();
//        return true;
//      }
      
      // наближена до сімейної форма
      if( PosTagHelper.hasPosTagPart(tokens[adjPos-1], "prep") ) {
        if (PosTagHelper.hasPosTag(tokens[adjPos-2], "(adj|verb|part|noun|adv).*")) {

          Collection<String> prepGovernedCases = CaseGovernmentHelper.getCaseGovernments(tokens[adjPos-1], IPOSTag.prep.name());
          if( TokenAgreementPrepNounRule.hasVidmPosTag(prepGovernedCases, tokens[adjPos]) ) {

            // відрізнялася (б) від нинішньої ситуація
            // відрізнялося від російського способом
            // поряд з енергетичними Москва висувала
            // на відміну від європейських санкції США
            // can't just ignore noun: ігнорує "асоціюється в нас із сучасною цивілізацію"
            // TODO: search verb backwards ignore "бЄ
            if( ((PosTagHelper.hasPosTagStart(tokens[adjPos-2], "verb") || LemmaHelper.hasLemma(tokens[adjPos-2], Arrays.asList("би", "б")))
                  || Arrays.asList("поряд", "відміну", "порівнянні").contains(tokens[adjPos-2].getToken().toLowerCase()) )
                && PosTagHelper.hasPosTag(tokens[nounPos], "noun.*v_(naz|zna|oru).*") ) {
              //TODO: check noun case agreement with verb
              logException();
              return true;
            }
            
            List<InflectionHelper.Inflection> masterInflections_ = InflectionHelper.getAdjInflections(tokens[adjPos-2].getReadings());

            if( ! Collections.disjoint(masterInflections_, slaveInflections) ) {
              logException();
              return true;
            }
            
            // тотожні із загальносоюзними герб і прапор
            if( nounPos < tokens.length - 1
                && PosTagHelper.hasPosTagPart(tokens[adjPos], "adj:p:")
                && CONJ_FOR_PLURAL_WITH_COMMA.contains(tokens[nounPos+1].getToken().toLowerCase()) 
                && PosTagHelper.hasPosTagPart(tokens[adjPos-2], "adj:p:")
                && hasOverlapIgnoreGender(InflectionHelper.getAdjInflections(tokens[adjPos-2].getReadings()), slaveInflections, "p", null)) {
              logException();
              return true;
            }
          }
        }
      }
    }
    
    
    // adjp:pasv + adj:v_oru + noun (case governed by adjp)
    // підсвічений синім діамант
    if( adjPos > 1
        && PosTagHelper.hasPosTagPart(tokens[adjPos-1], "adjp:pasv") // could be :&adjp or :&&adjp
        && PosTagHelper.hasPosTag(tokens[adjPos], "adj.*v_oru.*")
        && ! Collections.disjoint(InflectionHelper.getAdjInflections(tokens[adjPos-1].getReadings()), slaveInflections) ) {
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
        && PosTagHelper.hasPosTagPart(tokens[nounPos], "v_oru") ) {
      logException();
      return true;
    }

    // adj:v_oru + noun:v_naz + (verb)
    // Найнижчою частка таких є на Півдні
    // Слабшою критики вважають
    if( ! PosTagHelper.hasPosTag(tokens[adjPos-1], ".*adjp:pasv.*|prep.*") 
        && PosTagHelper.hasPosTag(adjAnalyzedTokenReadings, "adj.*v_oru.*") 
        && PosTagHelper.hasPosTag(slaveTokenReadings, "noun.*v_naz.*") 
        && LemmaHelper.forwardPosTagSearch(tokens, nounPos+1, "verb", 3)) {
      logException();
      return true;
    }

    // verb + adj:v_oru + noun:v_zna
    // зроблять неможливою ротацію влади
    // we still want to trigger on: за наявною інформацію
    if( (nounPos < 3 || ! CaseGovernmentHelper.hasCaseGovernment(tokens[adjPos-1], "v_oru"))
        && PosTagHelper.hasPosTagPart(adjAnalyzedTokenReadings, "v_oru") 
        && PosTagHelper.hasPosTag(slaveTokenReadings, ".*v_zna.*") 
        && genderMatches(masterInflections, slaveInflections, "v_oru", "v_zna") ) {
      logException();
      return true;
    }


    if( caseGovernmentMatches(adjTokenReadings, slaveInflections) ) {

      if( nounPos < tokens.length - 1 
          && PosTagHelper.hasPosTagPart(tokens[nounPos+1], "noun:") ) {

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
        if( PosTagHelper.hasPosTag(tokens[nounPos+1], "noun.*v_(rod|oru|naz|dav).*") ) {
//            && ! PosTagHelper.hasPosTag(adjAnalyzedTokenReadins, "adj.*v_oru.*") ) {
          logException();
          return true;
        }

        // Нав’язаний Австрії нейтралитет
        List<InflectionHelper.Inflection> slave2Inflections = InflectionHelper.getNounInflections(tokens[nounPos+1].getReadings());

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
    if( adjPos > 1
        && PosTagHelper.hasPosTagPart(tokens[adjPos-1], "adj")
        && caseGovernmentMatches(tokens[adjPos-1].getReadings(), masterInflections) ) {

      List<Inflection> preAdjInflections = InflectionHelper.getAdjInflections(tokens[adjPos-1].getReadings());

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
        if( TokenAgreementNounVerbExceptionHelper.isNonPluralA(tokens, i) )
          return false;

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
                && ! LemmaHelper.isCapitalized(tokens[i+1].getCleanToken())
                && ! tokens[i+1].getToken().matches("[\"«“„]") ) )
          return false;

        return true;
      }

      if( ! PosTagHelper.hasPosTag(tokens[i], Pattern.compile("(noun|adj|prep|adv(?!p)|number:latin).*"))
            && ! LemmaHelper.isCapitalized(tokens[i].getCleanToken()) )  // for unknown last names: згадувані Костянтин Скоркін та Олена Заславська
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
