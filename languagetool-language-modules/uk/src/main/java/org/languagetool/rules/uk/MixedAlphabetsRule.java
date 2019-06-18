/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.uk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * A rule that matches words Latin and Cyrillic characters in them
 * 
 * @author Andriy Rysin
 */
public class MixedAlphabetsRule extends Rule {

  private static final Pattern LIKELY_LATIN_NUMBER = Pattern.compile("[XVIХІ]{2,8}(-[а-яіїє]{1,3})?");
  private static final Pattern LATIN_NUMBER_WITH_CYRILLICS = Pattern.compile("(Х{1,3}І{1,3}|І{1,3}Х{1,3}|Х{2,3}|І{2,3})(-[а-яіїє]{1,4})?");
  private static final Pattern MIXED_ALPHABETS = Pattern.compile(".*([a-zA-ZïáÁéÉíÍḯḮóÓúýÝ]'?[а-яіїєґА-ЯІЇЄҐ]|[а-яіїєґА-ЯІЇЄҐ]'?[a-zA-ZïáÁéÉíÍḯḮóÓúýÝ]).*");
  private static final Pattern CYRILLIC_ONLY = Pattern.compile(".*[бвгґдєжзийїлнпфцчшщьюяБГҐДЄЖЗИЙЇЛПФЦЧШЩЬЮЯ].*");
  private static final Pattern LATIN_ONLY = Pattern.compile(".*[bdfghjlqrstvzDFGJLNQRSUVZ].*");
  private static final Pattern COMMON_CYR_LETTERS = Pattern.compile("[АВЕІКОРСТУХ]+");
  private static final Pattern CYRILLIC_FIRST_LETTER = Pattern.compile("[а-яіїєґА-ЯІЇЄҐ].*");

  public MixedAlphabetsRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  @Override
  public final String getId() {
    return "UK_MIXED_ALPHABETS";
  }

  @Override
  public String getDescription() {
    return "Змішування кирилиці й латиниці";
  }

  public String getShort() {
    return "Мішанина розкладок";
  }

  public String getSuggestion(String word) {
    String highlighted = word.replaceAll("([a-zA-Z])([а-яіїєґА-ЯІЇЄҐ])", "$1/$2");
    highlighted = highlighted.replaceAll("([а-яіїєґА-ЯІЇЄҐ])([a-zA-Z])", "$1/$2");
    return " містить суміш кирилиці та латиниці: «"+ highlighted +"», виправлення: ";
  }

  /**
   * Indicates if the rule is case-sensitive. 
   * @return true if the rule is case-sensitive, false otherwise.
   */
  public boolean isCaseSensitive() {
    return true;
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];
      String tokenString = tokenReadings.getToken();

      // optimization: 1-letter tokens first
      if( i<tokens.length-1
          && tokenString.equals("i")
          && CYRILLIC_FIRST_LETTER.matcher(tokens[i+1].getToken()).matches() ) {
        String msg = "Вжито латинську «і» замість кирилічної";
        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, Arrays.asList(toCyrillic(tokenString)), msg, sentence);
        ruleMatches.add(potentialRuleMatch);
      }
      else if (COMMON_CYR_LETTERS.matcher(tokenString).matches()) {
        String prevLemma = tokens[i-1].getAnalyzedToken(0).getLemma();
        if( prevLemma != null && prevLemma.matches("гепатит|група|турнір") ) {
          List<String> replacements = new ArrayList<>();
          replacements.add( toLatin(tokenString) );

          String msg = "Вжито кирилічну літеру замість латинської";
          RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, msg, sentence);
          ruleMatches.add(potentialRuleMatch);
        }
      }

      if( tokenString.length() < 2 )
        continue;

      if( MIXED_ALPHABETS.matcher(tokenString).matches() ) {

        List<String> replacements = new ArrayList<>();

        if(!LATIN_ONLY.matcher(tokenString).matches() && ! LIKELY_LATIN_NUMBER.matcher(tokenString).matches()) {
          replacements.add( toCyrillic(tokenString) );
        }
        if(!CYRILLIC_ONLY.matcher(tokenString).matches() || LIKELY_LATIN_NUMBER.matcher(tokenString).matches()) {
          String converted = toLatinLeftOnly(tokenString);
          converted = adjustForInvalidSuffix(converted);
          replacements.add( converted );
        }

        if (replacements.size() > 0) {
          String msg = "Вжито кирилічні літери замість латинських на позначення римської цифри";
          msg = adjustForInvalidSuffix(tokenString, msg);

          RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, msg, sentence);
          ruleMatches.add(potentialRuleMatch);
        }
      }
      else if(LATIN_NUMBER_WITH_CYRILLICS.matcher(tokenString).matches()) {
        List<String> replacements = new ArrayList<>();
        String converted = toLatinLeftOnly(tokenString);
        converted = adjustForInvalidSuffix(converted);
        replacements.add( converted );

        String msg = "Вжито кирилічні літери замість латинських на позначення римської цифри";
        msg = adjustForInvalidSuffix(tokenString, msg);
        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, msg, sentence);
        ruleMatches.add(potentialRuleMatch);
      }
      else if( tokenString.endsWith("°С") ) {  // cyrillic С
        List<String> replacements = new ArrayList<>();
        int length = tokenString.length();
        replacements.add( tokenString.substring(0,  length-1) + toLatin(tokenString.substring(length-1, tokenString.length())) );

        String msg = "Вжито кирилічну літеру замість латинської";
        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, msg, sentence);
        ruleMatches.add(potentialRuleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private String adjustForInvalidSuffix(String tokenString) {
    if( tokenString.contains("-") ) {
      tokenString = tokenString.replaceFirst("-.*", "");
    }
    return tokenString;
  }

  private String adjustForInvalidSuffix(String tokenString, String msg) {
    if( tokenString.contains("-") ) {
      msg += ". Також: до римських цифр букви не дописуються.";
    }
    return msg;
  }

  private String toLatinLeftOnly(String tokenString) {
    String[] parts = tokenString.split("-", 2);
    String right = parts.length > 1 ? "-" + parts[1] : "";
    String converted = toLatin(parts[0]) + right;
    return converted;
  }
  
//  private RuleMatch createRuleMatch(AnalyzedTokenReadings readings, List<String> replacements, AnalyzedSentence sentence) {
//    String tokenString = readings.getToken();
//    String msg = tokenString + getSuggestion(tokenString) + String.join(", ", replacements);
//    
//    return createRuleMatch(readings, replacements, msg, sentence);
//  }

  private RuleMatch createRuleMatch(AnalyzedTokenReadings readings, List<String> replacements, String msg, AnalyzedSentence sentence) {
    RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, readings.getStartPos(), readings.getEndPos(), msg, getShort());
    potentialRuleMatch.setSuggestedReplacements(replacements);

    return potentialRuleMatch;
  }

  private static final Map<Character, Character> toLatMap = new HashMap<>();
  private static final Map<Character, Character> toCyrMap = new HashMap<>();
  private static final String cyrChars = "аеіїкморстухАВЕІКМНОРСТУХ";
  private static final String latChars = "aeiïkmopctyxABEIKMHOPCTYX";
  private static final String[] umlauts = { "á", "Á", "é", "É", "í", "Í", "ḯ", "Ḯ", "ó", "Ó", "ú", "ý", "Ý" };
  private static final String[] umlautsReplace = { "а́", "А́", "е́", "Е́", "і́", "І́", "ї́", "Ї́", "о́", "О́", "и́", "у́", "У́" };

  static {
    for (int i = 0; i < cyrChars.length(); i++) {
      toLatMap.put(cyrChars.charAt(i), latChars.charAt(i));
      toCyrMap.put(latChars.charAt(i), cyrChars.charAt(i));
    }
  }

  private static String toCyrillic(String word) {
    for (Map.Entry<Character, Character> entry : toCyrMap.entrySet()) {
      word = word.replace(entry.getKey(), entry.getValue());
    }
    for(int i=0; i<umlauts.length; i++) {
      word = word.replace(umlauts[i], umlautsReplace[i]);
    }
    return word;
  }

  private static String toLatin(String word) {
    for (Map.Entry<Character, Character> entry : toLatMap.entrySet()) {
      word = word.replace(entry.getKey(), entry.getValue());
    }
    return word;
  }

}
