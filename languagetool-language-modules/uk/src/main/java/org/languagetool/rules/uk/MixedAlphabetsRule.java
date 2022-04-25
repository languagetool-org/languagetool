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
import org.languagetool.tagging.uk.PosTagHelper;

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

  private String getShort() {
    return "Мішанина розкладок";
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];
      String tokenString = tokenReadings.getCleanToken();

      // optimization: 1-letter tokens first
      if( i<tokens.length-1
          && ( tokenString.matches("[iya]")
            || (tokenString.equals("A") && i == 1) )
          && CYRILLIC_FIRST_LETTER.matcher(tokens[i+1].getToken()).matches()
          && Arrays.stream(tokens).noneMatch(t -> t.getToken().matches("[xbB]")) ) {    // filter out formulas
        String msg = "Вжито латинську «"+tokenString+"» замість кириличної";
        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, Arrays.asList(toCyrillic(tokenString)), msg, sentence);
        ruleMatches.add(potentialRuleMatch);
      }
      else if ("І".equals(tokenString)
          && likelyBadLatinI(tokens, i) ) {
        List<String> replacements = new ArrayList<>();
        replacements.add( toLatin(tokenString) );

        String msg = "Вжито кириличну літеру замість латинської";
        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, msg, sentence);
        ruleMatches.add(potentialRuleMatch);
      }
      else if (i <= tokens.length-1
          && "І.".equals(tokenString)
          && ( i > 1
              && ! "Тому".equals(tokens[i-1].getCleanToken())
              && ! "Франко".equals(tokens[i-1].getCleanToken())
              && PosTagHelper.hasPosTag(tokens[i-1], Pattern.compile("(?!.*:abbr).*fname.*"))) ) {
        List<String> replacements = new ArrayList<>();
        replacements.add( toLatin(tokenString) );

        String msg = "Вжито кириличну літеру замість латинської";
        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, msg, sentence);
        ruleMatches.add(potentialRuleMatch);
      }
      else if (COMMON_CYR_LETTERS.matcher(tokenString).matches()) {
        String prevLemma = tokens[i-1].getAnalyzedToken(0).getLemma();
        if( prevLemma != null && prevLemma.matches("гепатит|група|турнір") ) {
          List<String> replacements = new ArrayList<>();
          replacements.add( toLatin(tokenString) );

          String msg = "Вжито кириличну літеру замість латинської";
          RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, msg, sentence);
          ruleMatches.add(potentialRuleMatch);
        }
      }

      if( tokenString.length() < 2 ) {
        if( tokenString.equals("°") 
            && i < tokens.length - 1
            && tokens[i+1].getCleanToken().equals("С") ) {  // Cyrillic С
          List<String> replacements = new ArrayList<>();
          replacements.add("C");

          String msg = "Вжито кириличну літеру замість латинської";
          RuleMatch potentialRuleMatch = createRuleMatch(tokens[i+1], replacements, msg, sentence);
          ruleMatches.add(potentialRuleMatch);
        }

        continue;
      }

      if( MIXED_ALPHABETS.matcher(tokenString).matches() ) {

        String msg = "Вжито кириличні й латинські літери в одному слові";
        List<String> replacements = new ArrayList<>();

        if(!LATIN_ONLY.matcher(tokenString).matches() && ! LIKELY_LATIN_NUMBER.matcher(tokenString).matches()) {
          replacements.add( toCyrillic(tokenString) );
        }
        if(!CYRILLIC_ONLY.matcher(tokenString).matches() || LIKELY_LATIN_NUMBER.matcher(tokenString).matches()) {
          String converted = toLatinLeftOnly(tokenString);
          converted = adjustForInvalidSuffix(converted);
          replacements.add( converted );
          msg = "Вжито кириличні літери замість латинських";
          msg = adjustForInvalidSuffix(tokenString, msg);
        }

        if (replacements.size() > 0) {
          RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, msg, sentence);
          ruleMatches.add(potentialRuleMatch);
        }
      }
      else if(LATIN_NUMBER_WITH_CYRILLICS.matcher(tokenString).matches()) {
        List<String> replacements = new ArrayList<>();
        String converted = toLatinLeftOnly(tokenString);
        converted = adjustForInvalidSuffix(converted);
        replacements.add( converted );

        String msg = "Вжито кириличні літери замість латинських на позначення римської цифри";
        msg = adjustForInvalidSuffix(tokenString, msg);
        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, msg, sentence);
        ruleMatches.add(potentialRuleMatch);
      }

      if( tokenString.indexOf('\u0306') > 0 || tokenString.indexOf('\u0308') > 0 ) {
        if( tokenString.matches(".*(и\u0306|і\u0308).*") ) {
          String fix = tokenString.replaceAll("и\u0306", "й").replaceAll("і\u0308", "ї");

          String msg = "Вжито комбіновані символи замість українських літер";
          RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, Arrays.asList(fix), msg, sentence);
          ruleMatches.add(potentialRuleMatch);
        }
      }
      
    }
    
    return toRuleMatchArray(ruleMatches);
  }

  private boolean likelyBadLatinI(AnalyzedTokenReadings[] tokens, int i) {
    return i > 1
        && ((LemmaHelper.isCapitalized(tokens[i-1].getCleanToken())
          || (PosTagHelper.hasPosTagStart(tokens[i-1], "prep")
              && i < tokens.length - 1 && ! LemmaHelper.isAllUppercaseUk(tokens[i+1].getCleanToken())) )
        ||
        i < tokens.length - 1 && Arrays.asList("ст.", "тис.").contains(tokens[i+1].getCleanToken())
        || 
        i < tokens.length - 1 && Arrays.asList("квартал", "півріччя", "тисячоліття", "половина").contains(tokens[i+1].getCleanToken()));
  }

  private String adjustForInvalidSuffix(String tokenString) {
    if( tokenString.contains("-") ) {
      tokenString = tokenString.replaceFirst("-[а-яіїє]{1,4}", "");
    }
    return tokenString;
  }

  private String adjustForInvalidSuffix(String tokenString, String msg) {
    if( tokenString.contains("-") && tokenString.matches("[IVXІХ]+-[а-яіїє]{1,4}") ) {
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
