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

package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.languagetool.Language;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

public final class MorfologikCatalanSpellerRule extends MorfologikSpellerRule {

  //private static final String RESOURCE_FILENAME = "/ca/hunspell/ca_ES.dict";
  private static final String RESOURCE_FILENAME = "/ca/catalan.dict";
  private static final Pattern CHAR_PAIRS = Pattern.compile("ou|uo|bv|vb",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  //|aà|eè|eé|ií|oó|oò|uú|iï|uü
  
  public MorfologikCatalanSpellerRule(ResourceBundle messages,
                                      Language language) throws IOException {
    super(messages, language);
    this.setIgnoreTaggedWords();
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_CA_ES";
  }
  
  @Override
  protected List<String> getAdditionalSuggestions(List<String> suggestions, String word) {
    final String wordWithoutDiacritics = removeAccents(word).toLowerCase(conversionLocale);
    List<String> moreSuggestions = new ArrayList<String>();
    //If few suggestions are found, try to get more from the word without diacritics and lowercase
    if (suggestions.size() < 5 && !word.equals(wordWithoutDiacritics)) {
      moreSuggestions = speller.getSuggestions(wordWithoutDiacritics);
      if (!speller.isMisspelled(wordWithoutDiacritics)) {
        moreSuggestions.add(wordWithoutDiacritics);
      }
    }
    //Add non previously existing suggestions
    for (String moreSuggestion : moreSuggestions) {
      if (!suggestions.contains(moreSuggestion)) {
        suggestions.add(moreSuggestion);
      }
    }
    return suggestions;
  }
  
  @Override
  protected List<String> orderSuggestions(List<String> suggestions, String word) {
    List<String> orderedSuggestions1 = new ArrayList<String>();
    List<String> orderedSuggestions2 = new ArrayList<String>();
    String myWord = removeAccents(word);
    for (String suggestion : suggestions) {
      int diffIdx = StringUtils.indexOfDifference(suggestion, myWord);
      String dif = "";
      if (diffIdx < 0) {
        orderedSuggestions1.add(suggestion);
      } else if (diffIdx < myWord.length() && diffIdx < suggestion.length()
          && myWord.substring(diffIdx+1, myWord.length()).equals(suggestion.substring(diffIdx+1, suggestion.length()))) {
        dif = myWord.substring(diffIdx, diffIdx+1);
        dif += suggestion.substring(diffIdx, diffIdx+1);
        Matcher mCharPairs = CHAR_PAIRS.matcher(dif);
        if (mCharPairs.matches()) {
          orderedSuggestions1.add(suggestion);
        } else {
          orderedSuggestions2.add(suggestion);
        }
      } else {
        orderedSuggestions2.add(suggestion);
      }      
    }
    List<String> orderedSuggestions = new ArrayList<String>();
    orderedSuggestions.addAll(orderedSuggestions1);
    orderedSuggestions.addAll(orderedSuggestions2);
    return orderedSuggestions;
  }

}
