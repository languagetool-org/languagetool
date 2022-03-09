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
  private static final Map<String, List<LetterEndingForNumericHelper.RegexToCaseList>> NUMR_ENDING_MAP;

  static {
    Map<String, List<RegexToCaseList>> map2 = new HashMap<>();
    // TODO: many of those depend on the last digit we can do better with regex
    map2.put("й", Arrays.asList(RegexToCaseList.always(":m:v_naz", ":m:v_zna:rinanim", ":f:v_dav", ":f:v_mis"))); // 1-й
    map2.put("ий", Arrays.asList(RegexToCaseList.always(":m:v_naz", ":m:v_zna:rinanim"))); // 2-ий
    map2.put("ій", Arrays.asList(
        RegexToCaseList.regex(".*([^3]|13)", ":f:v_dav", ":f:v_mis"), // 5-ій 
        RegexToCaseList.always(":m:v_naz", ":m:v_zna:rinanim", ":f:v_dav", ":f:v_mis"))); // 3-ій
    map2.put("тій", Arrays.asList(
        RegexToCaseList.regex(".*([^3]|13)", ":f:v_dav", ":f:v_mis"), // 5-ій 
        RegexToCaseList.always(":m:v_naz", ":m:v_zna:rinanim", ":f:v_dav", ":f:v_mis"))); // 3-ій
    map2.put("го", Arrays.asList(RegexToCaseList.always(":m:v_rod", ":m:v_zna:ranim", ":n:v_rod")));
    map2.put("му", Arrays.asList(RegexToCaseList.always(":m:v_dav", ":m:v_mis", ":n:v_dav", ":n:v_mis", ":f:v_zna")));
    map2.put("м", Arrays.asList(RegexToCaseList.always(":m:v_oru", ":n:v_oru", ":p:v_dav"))); // theoretically can also be -ім v_mis but rare
    map2.put("им", Arrays.asList(RegexToCaseList.always(":m:v_oru", ":n:v_oru", ":p:v_dav")));
    map2.put("ім", Arrays.asList(RegexToCaseList.always(":m:v_oru", ":m:v_mis", ":n:v_oru", ":n:v_mis")));
    map2.put("а", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 1-а (також часто номер будинку)
    map2.put("ша", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 1-ша
    map2.put("га", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 2-га
    map2.put("тя", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 3-тя
    map2.put("та", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // 4-та
    map2.put("ма", Arrays.asList(RegexToCaseList.always(":f:v_naz"))); // (також часто 8-ма=вісьма ":p:v_oru")
    map2.put("ї", Arrays.asList(RegexToCaseList.always(":f:v_rod"))); // 4-ї
    map2.put("ої", Arrays.asList(RegexToCaseList.always(":f:v_rod"))); // 4-ої
    map2.put("тої", Arrays.asList(RegexToCaseList.always(":f:v_rod"))); // 4-тої
    map2.put("у", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 1-у
    map2.put("шу", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 1-шу
    map2.put("гу", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 2-гу
    map2.put("ту", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 4-ту
    map2.put("тю", Arrays.asList(RegexToCaseList.always(":f:v_zna"))); // 3-тю
    map2.put("ою", Arrays.asList(RegexToCaseList.always(":f:v_oru"))); // 4-ою
    map2.put("ю", Arrays.asList(
        RegexToCaseList.regex(".*([^3]|13)", ":f:v_oru"), // 4-ю
        RegexToCaseList.always(":f:v_zna", ":f:v_oru"))); // 3-ю
    map2.put("е", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 1-е
    map2.put("є", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 3-є
    map2.put("ше", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 1-ше
    map2.put("ге", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 2-ге
    map2.put("тє", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 3-тє
    map2.put("те", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 4-те
    map2.put("ме", Arrays.asList(RegexToCaseList.always(":n:v_naz", ":n:v_zna"))); // 7-те
    map2.put("і", Arrays.asList(RegexToCaseList.always(":p:v_naz", ":p:v_zna:rinanim")));
    map2.put("ті", Arrays.asList(RegexToCaseList.always(":p:v_naz", ":p:v_zna:rinanim")));
    map2.put("ні", Arrays.asList(RegexToCaseList.always(":p:v_naz", ":p:v_zna:rinanim")));
    map2.put("х", Arrays.asList(RegexToCaseList.always(":p:v_rod", ":p:v_zna:ranim", ":p:v_mis"))); // 5-х
    map2.put("их", Arrays.asList(RegexToCaseList.always(":p:v_rod", ":p:v_zna:ranim", ":p:v_mis"))); // 5-их
    //TODO: частіше вживають іменником: у 7-ми томах
    map2.put("ми", Arrays.asList(RegexToCaseList.always(":p:v_oru"))); // 8-ми
    NUMR_ENDING_MAP = Collections.unmodifiableMap(map2);
  }
  
  static class RegexToCaseList {
    private final Pattern pattern;
    private final String[] cases;
  
    public RegexToCaseList(String regex, String... cases) {
      this.pattern = regex != null ? Pattern.compile(regex) : null;
      this.cases = cases;
    }
    
    // 2nd constructor with just String... is ambiguous
    public static RegexToCaseList always(String... cases) {
      return new RegexToCaseList(null, cases);
    }
    public static RegexToCaseList regex(String regex, String... cases) {
      return new RegexToCaseList(regex, cases);
    }
    
    public static String[] getCaseTags(String leftWord, List<RegexToCaseList> caseLists) {
      for (RegexToCaseList regexToCaseList : caseLists) {
        if( regexToCaseList.pattern == null 
            || regexToCaseList.pattern.matcher(leftWord).matches() )
          return regexToCaseList.cases;
      }
      System.err.println("Not found cases for " + leftWord);
      return new String[0];
    }
  }

  public static String[] findTags(String leftWord, String rightWord) {
    if( NUMR_ENDING_MAP.containsKey(rightWord) ) {
      return RegexToCaseList.getCaseTags(leftWord, NUMR_ENDING_MAP.get(rightWord));
    }
    return null;
  }

}
