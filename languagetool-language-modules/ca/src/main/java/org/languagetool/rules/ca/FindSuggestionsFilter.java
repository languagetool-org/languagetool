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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tagging.ca.CatalanTagger;

public class FindSuggestionsFilter extends RuleFilter {
 
  private static final CatalanTagger tagger = new CatalanTagger(new Catalan());
  private MorfologikCatalanSpellerRule morfologikRule;
  final private int MAX_SUGGESTIONS = 10;
  
  

  public FindSuggestionsFilter() throws IOException {
    ResourceBundle messages = JLanguageTool.getDataBroker().getResourceBundle(JLanguageTool.MESSAGE_BUNDLE,
        new Locale("ca"));
    morfologikRule = new MorfologikCatalanSpellerRule(messages, new Catalan(), null, Collections.emptyList());
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
            + ", PronounFrom: " + posWord);
      }
      AnalyzedTokenReadings atrWord = patternTokens[posWord - 1];

      AnalyzedTokenReadings[] auxPatternTokens = new AnalyzedTokenReadings[1];
      if (atrWord.isTagged()) {
        auxPatternTokens[0] = new AnalyzedTokenReadings(new AnalyzedToken(makeWrong(atrWord.getToken()), null, null));
      } else {
        auxPatternTokens[0] = atrWord;
      }
      AnalyzedSentence sentence = new AnalyzedSentence(auxPatternTokens);
      RuleMatch[] matches = morfologikRule.match(sentence);
      if (matches.length > 0) {
        List<String> suggestions = matches[0].getSuggestedReplacements();
        //TODO: do not tag capitalized words with tags for lower case
        List<AnalyzedTokenReadings> analyzedSuggestions = tagger.tag(suggestions);
        for (AnalyzedTokenReadings analyzedSuggestion : analyzedSuggestions) {
          if (analyzedSuggestion.matchesPosTagRegex(desiredPostag)) {
            if (!replacements.contains(analyzedSuggestion.getToken())
                && !replacements.contains(analyzedSuggestion.getToken().toLowerCase())) {
              replacements.add(analyzedSuggestion.getToken());  
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
    if (s.contains("à")) { return s.replace("à", "ä"); }
    if (s.contains("é")) { return s.replace("é", "ë"); }
    if (s.contains("è")) { return s.replace("è", "ë"); }
    if (s.contains("í")) { return s.replace("í", "ì"); }
    if (s.contains("ó")) { return s.replace("ó", "ö"); }
    if (s.contains("ò")) { return s.replace("ò", "ö"); }
    if (s.contains("ú")) { return s.replace("ú", "ù"); }
    return s + "-";
  }
}
