/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà (http://www.danielnaber.de)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tagging.es.SpanishTagger;
import org.languagetool.tools.StringTools;

public class FindSuggestionsFilter extends RuleFilter {
  
  final private int MAX_SUGGESTIONS = 10;
  private static final String DICT_FILENAME = "/es/es-ES.dict";
  private static MorfologikSpeller speller;
  private static final SpanishTagger tagger = new SpanishTagger();

  public FindSuggestionsFilter() throws IOException {
    // lazy init
    if (speller == null) {
      if (JLanguageTool.getDataBroker().resourceExists(DICT_FILENAME)) {
         speller = new MorfologikSpeller(DICT_FILENAME);
      } 
    }
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    List<String> replacements = new ArrayList<>();
    String wordFrom = getRequired("WordFrom", arguments);
    String desiredPostag = getRequired("DesiredPostag", arguments);
    
    if (wordFrom != null && desiredPostag != null) {
      int posWord = Integer.parseInt(wordFrom);
      if (posWord < 1 || posWord > patternTokens.length) {
        throw new IllegalArgumentException("FindSuggestionsFilter: Index out of bounds in " + match.getRule().getFullId()
            + ", wordFrom: " + posWord);
      }
      AnalyzedTokenReadings atrWord = patternTokens[posWord - 1];
      String wordToCheck = atrWord.getToken();
      if (atrWord.isTagged()) {
        wordToCheck = makeWrong(atrWord.getToken());
      }
      List<String> suggestions = new ArrayList<>();
      synchronized (speller) { 
        suggestions = speller.getSpeller().findReplacements(wordToCheck);
      }
      if (suggestions.size() > 0) {
        boolean isCapitalized = StringTools.isCapitalizedWord(wordToCheck);
        //TODO: do not tag capitalized words with tags for lower case
        List<AnalyzedTokenReadings> analyzedSuggestions = tagger.tag(suggestions);
        for (AnalyzedTokenReadings analyzedSuggestion : analyzedSuggestions) {
          if (analyzedSuggestion.matchesPosTagRegex(desiredPostag)) {
            if (!replacements.contains(analyzedSuggestion.getToken())
                && !replacements.contains(analyzedSuggestion.getToken().toLowerCase())) {
              if (isCapitalized) {
                replacements.add(StringTools.uppercaseFirstChar(analyzedSuggestion.getToken()));
              } else {
                replacements.add(analyzedSuggestion.getToken());
              }
            }
            if (replacements.size() >= MAX_SUGGESTIONS) {
              break;
            }
          }
        }
      }
    }
    
    String message = match.getMessage();
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
        message, match.getShortMessage());
    ruleMatch.setType(match.getType());
    
    List<String> definitiveReplacements = new ArrayList<>();
    boolean replacementsUsed = false;
    for (String s : match.getSuggestedReplacements()) {
      if (s.contains("{suggestion}")) {
        replacementsUsed = true;
        for (String s2 : replacements) {
          if (definitiveReplacements.size() >= MAX_SUGGESTIONS) {
            break;
          }
          definitiveReplacements.add(s.replace("{suggestion}", s2));
        }
      } else {
        definitiveReplacements.add(s);
      }
    }
    if (!replacementsUsed) {
      definitiveReplacements.addAll(replacements);
    }
    
    if (!definitiveReplacements.isEmpty()) {
      ruleMatch.setSuggestedReplacements(definitiveReplacements);
    }
    return ruleMatch;
  }

  /*
   * Invent a wrong word to find possible replacements. This is a hack to obtain
   * suggestions from the speller when the original word is a correct word.
   */
  private String makeWrong(String s) {
    if (s.contains("a")) { return s.replace("a", "ä"); }
    if (s.contains("e")) { return s.replace("e", "ë"); }
    if (s.contains("i")) { return s.replace("i", "ï"); }
    if (s.contains("o")) { return s.replace("o", "ö"); }
    if (s.contains("u")) { return s.replace("u", "ù"); }
    if (s.contains("á")) { return s.replace("á", "ä"); }
    if (s.contains("é")) { return s.replace("é", "ë"); }
    if (s.contains("í")) { return s.replace("í", "ï"); }
    if (s.contains("ó")) { return s.replace("ó", "ö"); }
    if (s.contains("ú")) { return s.replace("ú", "ù"); }
    return s + "-";
  }
}
