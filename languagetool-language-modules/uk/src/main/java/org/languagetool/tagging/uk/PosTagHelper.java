package org.languagetool.tagging.uk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

/**
 * @since 2.9
 */
public class PosTagHelper {
  private static final Pattern GENDER_REGEX = Pattern.compile("(noun|adjp?|numr):(.):v_.*");
  private static final Pattern GENDER_CONJ_REGEX = Pattern.compile("(noun|adjp?|numr):(.:v_...).*");

  private PosTagHelper() {
  }
  
  @Nullable
  public static String getGender(String posTag) {
    Matcher pos4matcher = GENDER_REGEX.matcher(posTag);
    if( pos4matcher.matches() ) {
      return pos4matcher.group(2);
    }

    return null;
  }

  @Nullable
  public static String getNum(String posTag) {
    Matcher pos4matcher = Pattern.compile("(noun|adjp?|numr):(.):v_.*").matcher(posTag);
    if( pos4matcher.matches() ) {
      String group = pos4matcher.group(2);
      if( ! group.equals("p") ) {
        group = "s";
      }
      return group;
    }
  
    return null;
  }

  @Nullable
  public static String getConj(String posTag) {
    Matcher pos4matcher = Pattern.compile("(noun|adjp?|numr):[mfnp]:(v_...).*").matcher(posTag);
    if( pos4matcher.matches() )
      return pos4matcher.group(2);
  
    return null;
  }

  @Nullable
  public static String getGenderConj(String posTag) {
    Matcher pos4matcher = GENDER_CONJ_REGEX.matcher(posTag);
    if( pos4matcher.matches() )
      return pos4matcher.group(2);

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
