package org.languagetool.tagging.uk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

/**
 * @since 2.9
 */
public class PosTagHelper {
  private static final Pattern NUM_REGEX = Pattern.compile("(noun|numr|adj|adjp.*):(.):v_.*");
  private static final Pattern CONJ_REGEX = Pattern.compile("(noun|numr|adj|adjp.*):[mfnp]:(v_...).*");
  private static final Pattern GENDER_REGEX = NUM_REGEX;
  private static final Pattern GENDER_CONJ_REGEX = Pattern.compile("(noun|adj|numr|adjp.*):(.:v_...).*");

  public static final Map<String, String> VIDMINKY_MAP;

  static {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("v_naz", "називний");
    map.put("v_rod", "родовий");
    map.put("v_dav", "давальний");
    map.put("v_zna", "знахідний");
    map.put("v_oru", "орудний");
    map.put("v_mis", "місцевий");
    map.put("v_kly", "кличний");
    VIDMINKY_MAP = Collections.unmodifiableMap(map);
  }
  
  private PosTagHelper() {
  }
  
  @Nullable
  public static String getGender(String posTag) {
    Matcher pos4matcher = GENDER_REGEX.matcher(posTag);
    if( pos4matcher.matches() ) {
      return pos4matcher.group(2);
    }

//    System.err.println("WARNING: gender field not found for " + posTag);
    return null;
  }

  @Nullable
  public static String getNum(String posTag) {
    Matcher pos4matcher = NUM_REGEX.matcher(posTag);
    if( pos4matcher.matches() ) {
      String group = pos4matcher.group(2);
      if( ! group.equals("p") ) {
        group = "s";
      }
      return group;
    }
  
//    System.err.println("WARNING: num field not found for " + posTag);
    return null;
  }

  @Nullable
  public static String getConj(String posTag) {
    Matcher pos4matcher = CONJ_REGEX.matcher(posTag);
    if( pos4matcher.matches() )
      return pos4matcher.group(2);
  
//    System.err.println("WARNING: conj field is not found for " + posTag);
    return null;
  }

  @Nullable
  public static String getGenderConj(String posTag) {
    Matcher pos4matcher = GENDER_CONJ_REGEX.matcher(posTag);
    if( pos4matcher.matches() )
      return pos4matcher.group(2);

//    System.err.println("WARNING: gender/conj fields is not found for " + posTag);
    return null;
  }

  public static boolean hasPosTag(AnalyzedTokenReadings analyzedTokenReadings, String posTagRegex) {
    for(AnalyzedToken analyzedToken: analyzedTokenReadings) {
      if( hasPosTag(analyzedToken, posTagRegex) )
        return true;
    }
    return false;
  }

  public static boolean hasPosTag(AnalyzedToken analyzedToken, String posTagRegex) {
    String posTag = analyzedToken.getPOSTag();
    return posTag != null && posTag.matches(posTagRegex);
  }

//private static String getNumAndConj(String posTag) {
//  Matcher pos4matcher = GENDER_CONJ_REGEX.matcher(posTag);
//  if( pos4matcher.matches() ) {
//    String group = pos4matcher.group(2);
//    if( group.charAt(0) != 'p' ) {
//      group = "s" + group.substring(1);
//    }
//    return group;
//  }
//
//  return null;
//}

}
