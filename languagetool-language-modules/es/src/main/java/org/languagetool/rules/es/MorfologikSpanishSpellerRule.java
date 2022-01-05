/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.es;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tagging.es.SpanishTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @since 2.8
 */
public class MorfologikSpanishSpellerRule extends MorfologikSpellerRule {

  private static final Pattern PREFIX_WITH_WHITESPACE = Pattern.compile(
      "^(tele|anti|auto|ex|extra|macro|mega|meta|micro|multi|mono|mini|post|retro|semi|super|hiper|trans|re|g) (..+)|.+ s$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern PARTICULA_FINAL = Pattern.compile("^(..+) (que|cual)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern SPLIT_SUGGESTIONS = Pattern.compile("^(..+\\p{L}|en|de|del|al|a|y|o|con)(\\d+)$",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern ANY_TAG = Pattern.compile("[NVACPDRS].*");
  private static final SpanishTagger tagger = SpanishTagger.INSTANCE;

  public MorfologikSpanishSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    this.setIgnoreTaggedWords();
  }

  @Override
  public String getFileName() {
    return "/es/es-ES.dict";
  }

  @Override
  public final String getId() {
    return "MORFOLOGIK_RULE_ES";
  }

  @Override
  // Use this rule in LO/OO extension despite being a spelling rule
  public boolean useInOffice() {
    return true;
  }

  @Override
  protected List<SuggestedReplacement> orderSuggestions(List<SuggestedReplacement> suggestions, String word) {
    List<SuggestedReplacement> newSuggestions = new ArrayList<>();
    for (int i = 0; i < suggestions.size(); i++) {
      // remove wrong split prefixes
      if (PREFIX_WITH_WHITESPACE.matcher(suggestions.get(i).getReplacement()).matches()) {
        continue;
      }
      // move some split words to first place
      Matcher matcher = PARTICULA_FINAL.matcher(suggestions.get(i).getReplacement());
      if (matcher.matches()) {
        newSuggestions.add(0, suggestions.get(i));
        continue;
      }
      newSuggestions.add(suggestions.get(i));
    }
    return newSuggestions;
  }
  
  @Override
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word)
      throws IOException {
    List<String> suggestionsList = suggestions.stream().map(SuggestedReplacement::getReplacement)
        .collect(Collectors.toList());
    return SuggestedReplacement.convert(getAdditionalTopSuggestionsString(suggestionsList, word));
  }
  
  private List<String> getAdditionalTopSuggestionsString(List<String> suggestions, String word) throws IOException {
    String suggestion = "";
    suggestion = findSuggestion(suggestion, word, SPLIT_SUGGESTIONS, ANY_TAG, 1, " ", true);
    if (!suggestion.isEmpty()) {
      return Collections.singletonList(suggestion);
    }
    return Collections.emptyList();
  }
  
  private String findSuggestion(String suggestion, String word, Pattern wordPattern, Pattern postagPattern,
      int suggestionPosition, String separator, boolean recursive) throws IOException {
    if (!suggestion.isEmpty()) {
      return suggestion;
    }
    Matcher matcher = wordPattern.matcher(word);
    if (matcher.matches()) {
      String newSuggestion = matcher.group(suggestionPosition);
      AnalyzedTokenReadings newatr = tagger.tag(Arrays.asList(newSuggestion)).get(0);
      if ((!newatr.hasPosTag("VMIP1S0B") || newSuggestion.equals("fer")) && matchPostagRegexp(newatr, postagPattern)) {
        return matcher.group(1) + separator + matcher.group(2);
      }
      if (recursive) {
        List<String> moresugg = this.speller1.getSuggestions(newSuggestion);
        if (moresugg.size() > 0) {
          String newWord;
          if (suggestionPosition == 1) {
            newWord = moresugg.get(0) + matcher.group(2); //.toLowerCase()
          } else {
            newWord = matcher.group(1) + moresugg.get(0).toLowerCase();
          }
          return findSuggestion(suggestion, newWord, wordPattern, postagPattern, suggestionPosition, separator, false);
        }
      }
    }
    return "";
  }
  
  /**
   * Match POS tag with regular expression
   */
  private boolean matchPostagRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        return true;
      }
    }
    return false;
  }


}
