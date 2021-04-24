/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import org.languagetool.*;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public final class MorfologikUkrainianSpellerRule extends MorfologikSpellerRule {

  private static final String ABBREVIATION_CHAR = ".";
  private static final String RESOURCE_FILENAME = "/uk/hunspell/uk_UA.dict";
  private static final Pattern UKRAINIAN_LETTERS = Pattern.compile(".*[а-яіїєґА-ЯІЇЄҐ].*");
  private static final Pattern DO_NOT_SUGGEST_SPACED_PATTERN = Pattern.compile(
        "(авіа|авто|анти|аудіо|відео|водо|гідро|екстра|квазі|кіно|лже|мета|моно|мото|псевдо|пост|радіо|стерео|супер|ультра|фото) .*");
  private static final Pattern INFIX_PATTERN = Pattern.compile("-[а-яіїєґ]{1,5}-");
  private static final Map<String, String> dashPrefixes2019;

  static {
    dashPrefixes2019 = ExtraDictionaryLoader.loadMap("/uk/dash_prefixes.txt");
    dashPrefixes2019.entrySet().removeIf(entry -> entry.getValue().matches(":(ua_1992|bad|alt|slang)") || ! entry.getKey().matches("[а-яіїєґ]{3,}") );
  }

  public MorfologikUkrainianSpellerRule(ResourceBundle messages,
                                        Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
//    setCheckCompound(true);
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_UK_UA";
  }
  
  @Override
  protected boolean isMisspelled(MorfologikMultiSpeller speller, String word) {
    if( word.endsWith("-") ) {
      return !word.startsWith("-") || !INFIX_PATTERN.matcher(word).matches();
    }
  
    if( word.endsWith("²") || word.endsWith("³") ) {
      word = word.substring(0, word.length() - 1); 
    }

    // in some places disambiguator may leave tokens with ignored characters
    // so we have to ignore them here
    // we fixed "filter" action but if this problem still happens we can uncomment code below
//    Matcher matcher = language.getIgnoredCharactersRegex().matcher(word);
//    if( matcher.find() ) {
//      word = matcher.replaceAll("");
//    }
    
    return super.isMisspelled(speller, word);
  }

  @Override
  protected List<SuggestedReplacement> getAdditionalSuggestions(List<SuggestedReplacement> suggestions, String word) {
    boolean isCapitalized = StringTools.isCapitalizedWord(word);
    if( isCapitalized ) {
      word = word.toLowerCase();
    }
    for(String key: dashPrefixes2019.keySet()) {
      if( word.startsWith(key) 
          && word.length() > key.length() + 2 
          && word.charAt(key.length()) != '-' ) {
        String second = word.substring(key.length());
        suggestions.add(new SuggestedReplacement(key + "-" + second));
      }
    }
    return suggestions;
  }
  
  @Override
  protected boolean ignoreToken(AnalyzedTokenReadings[] tokens, int idx) throws IOException {
    String word = tokens[idx].getToken();

    // don't check words that don't have Ukrainian letters
    if( ! UKRAINIAN_LETTERS.matcher(word).matches() )
      return true;

    if( super.ignoreToken(tokens, idx) )
      return true;

    if( idx < tokens.length - 1 && tokens[idx+1].getToken().equals(ABBREVIATION_CHAR) ) {
      if( super.ignoreWord(word + ABBREVIATION_CHAR) ) {
        return true;
      }
      if( word.matches("[А-ЯІЇЄҐ]") ) {  //TODO: only do this for initials when last name is followed
        return true;
      }
    }
    
//    if( word.contains("-") || word.contains("\u2011") || word.endsWith(".") 
//            || word.equalsIgnoreCase("раза") ) {
      return hasGoodTag(tokens[idx]); // && ! PosTagHelper.hasPosTagPart(tokens[idx], ":bad");
//    }
//
//    return false;
  }

  private static boolean hasGoodTag(AnalyzedTokenReadings tokens) {
    for (AnalyzedToken analyzedToken : tokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag != null 
            && ! posTag.equals(JLanguageTool.SENTENCE_END_TAGNAME) 
            && ! posTag.equals(JLanguageTool.PARAGRAPH_END_TAGNAME) 
//            && (! posTag.contains(IPOSTag.bad.getText()) || posTag.contains(":latin"))  
            && ! (posTag.contains(":inanim") && posTag.contains(":v_kly")) )
        return true;
    }
    return false;
  }

  @Override
  protected List<SuggestedReplacement> filterSuggestions(List<SuggestedReplacement> suggestions) {
    suggestions = super.filterSuggestions(suggestions);
    // do not suggest "кіно прокат, вело- прогулянка...":
    suggestions.removeIf(item -> item.getReplacement().contains(" ") &&
        DO_NOT_SUGGEST_SPACED_PATTERN.matcher(item.getReplacement()).matches() ||
        item.getReplacement().contains("- "));
    return suggestions;
  }

  // workaround to allow other rules generate spelling suggestions without invoking match()
  MorfologikMultiSpeller getSpeller1() {
    if( speller1 == null ) {
      try {
        // we can't call initSpellers() as it's private so we're calling method we can
        isMisspelled("1");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return speller1;
  }

  @Override
  protected boolean isLatinScript() {
    return false;
  }
}
