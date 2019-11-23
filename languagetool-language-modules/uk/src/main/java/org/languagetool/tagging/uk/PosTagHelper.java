package org.languagetool.tagging.uk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.TaggedWord;

/**
 * @since 2.9
 */
public final class PosTagHelper {
  private static final Pattern NUM_REGEX = Pattern.compile("(noun:(?:in)?anim|numr|adj|adjp.*):(.):v_.*");
  private static final Pattern CONJ_REGEX = Pattern.compile("(noun:(?:in)?anim|numr|adj|adjp.*):[mfnp]:(v_...).*");
  private static final Pattern GENDER_REGEX = NUM_REGEX;
  private static final Pattern GENDER_CONJ_REGEX = Pattern.compile("(noun:(?:in)?anim|adj|numr|adjp.*):(.:v_...).*");
  public static final Pattern ADJ_COMP_REGEX = Pattern.compile(":comp[bcs]");

  public static final Map<String, String> VIDMINKY_MAP;
  public static final Map<String, String> GENDER_MAP;
  public static final List<String> BASE_GENDERS = Arrays.asList("m", "f", "n", "p");
  public static final Map<String, String> PERSON_MAP;
  public static final String NO_VIDMINOK_SUBSTR = ":nv";

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

    Map<String, String> map2 = new LinkedHashMap<>();
    map2.put("m", "ч.р.");
    map2.put("f", "ж.р.");
    map2.put("n", "с.р.");
    map2.put("p", "мн.");
    map2.put("s", "одн.");
    map2.put("i", "інф.");
    map2.put("o", "безос. форма");
    GENDER_MAP = Collections.unmodifiableMap(map2);

    Map<String, String> map3 = new LinkedHashMap<>();
    map3.put("1", "1-а особа");
    map3.put("2", "2-а особа");
    map3.put("3", "3-я особа");
    map3.put("s", "одн.");
    map3.put("p", "мн.");
    PERSON_MAP = Collections.unmodifiableMap(map3);
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

  public static boolean hasPosTag(AnalyzedTokenReadings analyzedTokenReadings, Pattern posTagRegex) {
    return hasPosTag(analyzedTokenReadings.getReadings(), posTagRegex);
  }

  public static boolean hasPosTag(AnalyzedTokenReadings analyzedTokenReadings, String posTagRegex) {
    return hasPosTag(analyzedTokenReadings.getReadings(), posTagRegex);
  }
  
  public static boolean hasPosTag(Collection<AnalyzedToken> analyzedTokenReadings, Pattern posTagRegex) {
    for(AnalyzedToken analyzedToken: analyzedTokenReadings) {
      if( hasPosTag(analyzedToken, posTagRegex) )
        return true;
    }
    return false;
  }
  
  public static boolean hasPosTag(Collection<AnalyzedToken> analyzedTokenReadings, String posTagRegex) {
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

  public static boolean hasPosTagPart(AnalyzedToken analyzedToken, String posTagPart) {
    String posTag = analyzedToken.getPOSTag();
    return posTag != null && posTag.contains(posTagPart);
  }

  public static boolean hasPosTag(AnalyzedToken analyzedToken, Pattern posTagRegex) {
    String posTag = analyzedToken.getPOSTag();
    return posTag != null && posTagRegex.matcher(posTag).matches();
  }

  public static boolean hasPosTag(TaggedWord analyzedToken, Pattern posTagRegex) {
    String posTag = analyzedToken.getPosTag();
    return posTag != null && posTagRegex.matcher(posTag).matches();
  }

  public static boolean hasPosTagPart(AnalyzedTokenReadings analyzedTokenReadings, String posTagPart) {
    return hasPosTagPart(analyzedTokenReadings.getReadings(), posTagPart);
  }
  
  public static boolean hasPosTagPart(List<AnalyzedToken> analyzedTokenReadings, String posTagPart) {
    for(AnalyzedToken analyzedToken: analyzedTokenReadings) {
      if( analyzedToken.getPOSTag() != null && analyzedToken.getPOSTag().contains(posTagPart) )
        return true;
    }
    return false;
  }

  public static boolean hasPosTagPart2(List<TaggedWord> taggedWords, String posTagPart) {
    for(TaggedWord analyzedToken: taggedWords) {
      if( analyzedToken.getPosTag() != null && analyzedToken.getPosTag().contains(posTagPart) )
        return true;
    }
    return false;
  }

  public static boolean hasPosTag2(List<TaggedWord> taggedWords, Pattern pattern) {
    for(TaggedWord analyzedToken: taggedWords) {
      if( analyzedToken.getPosTag() != null && pattern.matcher(analyzedToken.getPosTag()).matches() )
        return true;
    }
    return false;
  }

  public static boolean startsWithPosTag2(List<AnalyzedToken> analyzedTokenReadings, String posTagPart) {
    for(AnalyzedToken analyzedToken: analyzedTokenReadings) {
      if( analyzedToken.getPOSTag() != null && analyzedToken.getPOSTag().startsWith(posTagPart) )
        return true;
    }
    return false;
  }

  public static boolean startsWithPosTag(List<TaggedWord> taggedWords, String posTagPart) {
    for(TaggedWord analyzedToken: taggedWords) {
      if( analyzedToken.getPosTag() != null && analyzedToken.getPosTag().startsWith(posTagPart) )
        return true;
    }
    return false;
  }

  public static String getGenders(AnalyzedTokenReadings tokenReadings, String posTagRegex) {
    Pattern posTagPattern = Pattern.compile(posTagRegex);

    StringBuilder sb = new StringBuilder(4);
    for (AnalyzedToken tokenReading: tokenReadings) {
      String posTag = tokenReading.getPOSTag();
      if( posTagPattern.matcher(posTag).matches() ) {
        String gender = getGender(posTag);
        if( sb.indexOf(gender) == -1 ) {
          sb.append(gender);
        }
      }
    }

    return sb.toString();
  }

  @NotNull
  public static List<AnalyzedToken> generateTokensForNv(String word, String gender, String extraTags) {
    String posTagBase = "noun:inanim:" + gender + ":";
  
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();
    for(String vidm: VIDMINKY_MAP.keySet()) {
      if( vidm.equals("v_kly") )
        continue;
  
      String posTag = posTagBase + vidm + PosTagHelper.NO_VIDMINOK_SUBSTR;
      if( extraTags != null ) {
        posTag += extraTags;
      }
      newAnalyzedTokens.add(new AnalyzedToken(word, posTag, word));
    }
    
    return newAnalyzedTokens;
  }

  @NotNull
  public static String addIfNotContains(@NotNull String tag, @NotNull String addTag) {
    if( ! tag.contains(addTag) )
      return tag + addTag;
    return tag;
  }

  @NotNull
  public static List<TaggedWord> addIfNotContains(@NotNull List<TaggedWord> taggedWords, @NotNull String addTag) {
    return addIfNotContains(taggedWords, addTag, null);
  }
  
  @NotNull
  public static List<TaggedWord> addIfNotContains(@NotNull List<TaggedWord> taggedWords, @NotNull String addTag, @Nullable String lemma) {
    return taggedWords.stream()
        .map(w -> new TaggedWord(lemma != null ? lemma : w.getLemma(), addIfNotContains(w.getPosTag(), addTag)))
        .collect(Collectors.toList());
  }

  public static List<AnalyzedToken> filter(List<AnalyzedToken> analyzedTokens, Pattern posTag) {
    return 
        analyzedTokens.stream()
        .filter(token -> hasPosTag(token, posTag) )
        .collect(Collectors.toList());
  }

  public static List<TaggedWord> filter2(List<TaggedWord> analyzedTokens, Pattern posTag) {
    return 
        analyzedTokens.stream()
        .filter(token -> hasPosTag(token, posTag) )
        .collect(Collectors.toList());
  }

  private static Pattern WORD_PATTERN = Pattern.compile("[а-яіїєґa-z'-]+", Pattern.UNICODE_CASE|Pattern.CASE_INSENSITIVE);
  public static boolean isUnknownWord(AnalyzedTokenReadings analyzedTokenReadings) {
    return analyzedTokenReadings.getAnalyzedToken(0).hasNoTag()
        && WORD_PATTERN.matcher(analyzedTokenReadings.getToken()).matches();
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
