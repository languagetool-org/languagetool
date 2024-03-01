package org.languagetool.tagging.uk;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Helps to tag numeric adjectives with letter ending
 */
class LetterEndingForNumericHelper {
  private static final Map<String, List<LetterEndingForNumericHelper.RegexToCaseList>> NUMR_ADJ_ENDING_MAP;
  private static final Map<String, List<LetterEndingForNumericHelper.RegexToCaseList>> NUMR_NOUN_ENDING_MAP;

  static {
    Map<String, List<RegexToCaseList>> map2 = new HashMap<>();
    // TODO: many of those depend on the last digit we can do better with regex
    map2.put("й", Arrays.asList(RegexToCaseList.always(":m:v_naz", ":m:v_zna:rinanim", ":f:v_dav", ":f:v_mis"))); // 1-й
    map2.put("ий", Arrays.asList(RegexToCaseList.always(":m:v_naz", ":m:v_zna:rinanim"))); // 2-ий
    map2.put("ій", Arrays.asList(RegexToCaseList.regex(".*([^3]|13)", ":f:v_dav", ":f:v_mis"), // 5-ій 
                                 RegexToCaseList.always(":m:v_naz", ":m:v_zna:rinanim", ":f:v_dav", ":f:v_mis"))); // 3-ій
    map2.put("го", Arrays.asList(RegexToCaseList.always(":m:v_rod", ":m:v_zna:ranim", ":n:v_rod")));
    map2.put("му", Arrays.asList(RegexToCaseList.regex(".*(?!<1)7", ":m:v_dav", ":m:v_mis", ":n:v_dav", ":n:v_mis", ":f:v_zna"), // 7-му
                                 RegexToCaseList.regex(".*(?!<1)8", ":f:v_zna", ":m:v_dav", ":m:v_mis", ":n:v_dav", ":n:v_mis"), // 8-му
                                 RegexToCaseList.always(":m:v_dav", ":m:v_mis", ":n:v_dav", ":n:v_mis")));
    map2.put("ма", Arrays.asList(RegexToCaseList.regex(".*(?!<1)[78]", ":f:v_naz")));
//                                RegexToCaseList.always(":p:v_oru:bad"))); // 3-ма - помилка
    map2.put("м", Arrays.asList(RegexToCaseList.always(":m:v_oru", ":n:v_oru", ":p:v_dav"))); // theoretically can also be -ім v_mis but rare
    map2.put("им", Arrays.asList(RegexToCaseList.always(":m:v_oru", ":n:v_oru", ":p:v_dav")));
    map2.put("ім", Arrays.asList(RegexToCaseList.regex(".*(?!<1)3", ":m:v_oru", ":m:v_mis", ":n:v_oru", ":n:v_mis"),
                                 RegexToCaseList.always(":m:v_mis", ":n:v_oru", ":n:v_mis")));
    map2.put("а", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 1-а (також часто номер будинку)
    map2.put("ва", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 40-ва
    map2.put("ша", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 1-ша
    map2.put("га", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 2-га
    map2.put("тя", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 3-тя
    map2.put("я", Arrays.asList(RegexToCaseList.regex(".*(?!<1)3", ":f:v_naz"))); // 3-я
    map2.put("та", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 4-та
    map2.put("ї", Arrays.asList(RegexToCaseList.always(":f:v_rod"))); // 4-ї
    map2.put("ої", Arrays.asList(RegexToCaseList.always(":f:v_rod"))); // 4-ої
    map2.put("у", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 1-у
    map2.put("шу", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 1-шу
    map2.put("гу", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 2-гу
    map2.put("ту", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 4-ту
    map2.put("тю", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 3-тю
    map2.put("ою", Arrays.asList(RegexToCaseList.always(":f:v_oru"))); // 4-ою
    map2.put("ю", Arrays.asList(RegexToCaseList.regex(".*([^3]|13)", ":f:v_oru"), // 4-ю
                                RegexToCaseList.always(":f:v_zna", ":f:v_oru"))); // 3-ю
    map2.put("е", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 1-е
    map2.put("є", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 3-є
    map2.put("ше", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 1-ше
    map2.put("ге", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 2-ге
    map2.put("тє", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 3-тє
    map2.put("те", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 4-те
    map2.put("ме", Arrays.asList(RegexToCaseList.regex(".*(?!<1)[78]", ":n:v_naz", ":n:v_zna"))); // 7-те
    map2.put("і", Arrays.asList(RegexToCaseList.always(":p:v_naz", ":p:v_zna:rinanim")));
    map2.put("ті", Arrays.asList(RegexToCaseList.always(":p:v_naz", ":p:v_zna:rinanim")));
    map2.put("ні", Arrays.asList(RegexToCaseList.always(":p:v_naz", ":p:v_zna:rinanim"))); // 2000-ні
    map2.put("ми", Arrays.asList(RegexToCaseList.always(":p:v_oru"))); // 33-ми калібрами
    map2.put("х", Arrays.asList(RegexToCaseList.always(":p:v_rod", ":p:v_zna:ranim", ":p:v_mis"))); // 5-х
    map2.put("их", Arrays.asList(RegexToCaseList.always(":p:v_rod", ":p:v_zna:ranim", ":p:v_mis"))); // 5-их
    map2.put("ві", Arrays.asList(RegexToCaseList.regex(".*40", ":p:v_naz", ":p:v_zna:rinanim"), // 40-ві
                                 RegexToCaseList.regex(".*%", ":p:v_naz", ":p:v_zna:rinanim"))); // 40%-ві
    //TODO: вживають іменником: у 7-ми томах
//    map2.put("ми", Arrays.asList(RegexToCaseList.always(":p:v_oru"))); // 8-ми

    // bad
    map2.put("тій", Arrays.asList(
        RegexToCaseList.regex(".*([^3]|13)", ":f:v_dav:bad", ":f:v_mis:bad"), // 5-тій
        RegexToCaseList.always(":m:v_naz:bad", ":m:v_zna:rinanim:bad", ":f:v_dav:bad", ":f:v_mis:bad"))); // 3-ій
    map2.put("мій", Arrays.asList(RegexToCaseList.always(":f:v_dav:bad", ":f:v_mis:bad"))); // 8-мій
    map2.put("мою", Arrays.asList(RegexToCaseList.always(":f:v_oru:bad")));
    map2.put("тою", Arrays.asList(RegexToCaseList.always(":f:v_oru:bad")));
    map2.put("тої", Arrays.asList(RegexToCaseList.always(":f:v_rod:bad"))); // 4-тої
    map2.put("того", Arrays.asList(RegexToCaseList.always(":m:v_rod:bad", ":n:v_rod:bad")));
    map2.put("тього", Arrays.asList(RegexToCaseList.always(":m:v_rod:bad", ":n:v_rod:bad")));
    map2.put("тому", Arrays.asList(RegexToCaseList.always(":m:v_dav:bad", ":m:v_mis:bad", ":n:v_rod:bad", ":n:v_mis:bad")));
    map2.put("тьому", Arrays.asList(RegexToCaseList.always(":m:v_dav:bad", ":m:v_mis:bad", ":n:v_rod:bad", ":n:v_mis:bad")));
    map2.put("тими", Arrays.asList(RegexToCaseList.always(":p:v_oru:bad")));
    map2.put("тім", Arrays.asList(RegexToCaseList.always(":m:v_mis:bad", ":n:v_mis:bad")));
    map2.put("мої", Arrays.asList(RegexToCaseList.always(":f:v_rod:bad")));
    map2.put("тий", Arrays.asList(RegexToCaseList.always(":m:v_naz:bad", ":m:v_zna:rinanim:bad")));
    map2.put("мий", Arrays.asList(RegexToCaseList.always(":m:v_naz:bad", ":m:v_zna:rinanim:bad")));
    map2.put("тих", Arrays.asList(RegexToCaseList.always(":p:v_rod:bad", ":p:v_mis:bad")));
    map2.put("ого", Arrays.asList(RegexToCaseList.always(":m:v_rod:bad", ":m:v_zna:ranim:bad", ":n:v_rod:bad")));
    map2.put("ому", Arrays.asList(RegexToCaseList.always(":m:v_dav:bad", ":m:v_mis:bad", ":n:v_dav:bad", ":n:v_mis:bad")));
    map2.put("тим", Arrays.asList(RegexToCaseList.always(":m:v_oru:bad", ":n:v_oru:bad", ":p:v_dav:bad")));
    map2.put("ома", Arrays.asList(RegexToCaseList.always(":f:v_naz:bad", ":p:v_oru:bad"))); // 7-ома, 12-ома
    map2.put("ший", Arrays.asList(RegexToCaseList.always(":m:v_naz:bad", ":m:v_zna:rinanim:bad")));
    map2.put("гій", Arrays.asList(RegexToCaseList.always(":f:v_mis:bad", ":f:v_dav:bad")));

    NUMR_ADJ_ENDING_MAP = Collections.unmodifiableMap(map2);
    
    
    Map<String, List<RegexToCaseList>> map3 = new HashMap<>();
    map3.put("ти", Arrays.asList(RegexToCaseList.regex(".*([0569]|1[0-9])", ":p:v_rod:bad", ":p:v_dav:bad", ":p:v_mis:bad"))); // 20-ти
    map3.put("ці", Arrays.asList(RegexToCaseList.regex(".*([03456789]|1[0-9])", ":f:v_dav:bad", ":f:v_mis:bad"))); // 20-ці
    map3.put("ма", Arrays.asList(RegexToCaseList.regex(".*([023456789]|1[0-9])", ":p:v_oru:bad"))); // 20-ма
    map3.put("ми", Arrays.asList(RegexToCaseList.always(":p:v_rod:bad", ":p:v_mis:bad"))); // 148-ми
    map3.put("ві", Arrays.asList(RegexToCaseList.regex(".*(?!<1)2", ":p:v_naz:bad", ":p:v_zna:rinanim:bad"))); // 42-ві
    map3.put("ть", Arrays.asList(RegexToCaseList.always(":p:v_naz:bad", ":p:v_zna:rinanim:bad"))); // 75-ть
    
    NUMR_NOUN_ENDING_MAP = Collections.unmodifiableMap(map3);
  }
  
  private static class RegexToCaseList {
    private final Pattern pattern;
    private final String[] cases;
  
    public RegexToCaseList(String regex, String... cases) {
      this.pattern = regex != null ? Pattern.compile(regex) : null;
      this.cases = cases;
    }
    
    // 2nd constructor with just String... is ambiguous
    private static RegexToCaseList always(String... cases) {
      return new RegexToCaseList(null, cases);
    }
    private static RegexToCaseList regex(String regex, String... cases) {
      return new RegexToCaseList(regex, cases);
    }
    
    private static String[] getCaseTags(String leftWord, List<RegexToCaseList> caseLists) {
      for (RegexToCaseList regexToCaseList : caseLists) {
        if( regexToCaseList.pattern == null 
            || regexToCaseList.pattern.matcher(leftWord).matches() )
          return regexToCaseList.cases;
      }
//      System.err.println("Not found cases for " + leftWord + ", " + caseLists);
      return null;
    }

    @Override
    public String toString() {
      return String.format("%s: %s", pattern, Arrays.asList(cases));
    }
  }

  public static boolean isPossibleAdjAdjEnding(String leftWord, String rightWord) {
    return NUMR_ADJ_ENDING_MAP.containsKey(rightWord);
  }
  
  public static String[] findTagsAdj(String leftWord, String rightWord) {
    if( NUMR_ADJ_ENDING_MAP.containsKey(rightWord) ) {
      String[] tags = RegexToCaseList.getCaseTags(leftWord, NUMR_ADJ_ENDING_MAP.get(rightWord));
//      if( tags == null ) {
//        System.err.println("Not found cases for " + leftWord + ", " + rightWord);
//      }
      return tags;
    }
    return null;
  }

  public static String[] findTagsNoun(String leftWord, String rightWord) {
    if( NUMR_NOUN_ENDING_MAP.containsKey(rightWord) ) {
      String[] tags = RegexToCaseList.getCaseTags(leftWord, NUMR_NOUN_ENDING_MAP.get(rightWord));
//      if( tags == null ) {
//        System.err.println("Not found cases for " + leftWord + ", " + rightWord);
//      }
      return tags;
    }
    return null;
  }

}
