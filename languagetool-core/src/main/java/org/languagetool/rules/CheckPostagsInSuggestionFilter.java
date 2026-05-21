/* LanguageTool, a natural language style checker
 * Copyright (C) 2025 Jaume Ortolà
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
package org.languagetool.rules;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tagging.Tagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Check that the suggestions in a rule have the desired POStags. The suggestion can contain one or more tokens.
 */
public class CheckPostagsInSuggestionFilter extends RuleFilter {

  public CheckPostagsInSuggestionFilter() {
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    RuleMatch ruleMatch = match;
    Language language = ((PatternRule) match.getRule()).getLanguage();
    Tagger tagger = language.getTagger();
    if (tagger == null) {
      throw new IOException("Language tagger not available in rule " + match.getRule().getFullId());
    }
    List<String> replacements = match.getSuggestedReplacements();
    List<String> newReplacements = new ArrayList<>();
    String postagsListStr = getRequired("PostagsList", arguments);
    @NotNull String[] postagsList = postagsListStr.split(",");
    for (String replacement: replacements) {
      @NotNull String[] tokensInSuggestion = replacement.split("\\s+");
      if (tokensInSuggestion.length != postagsList.length || postagsList.length == 0) {
        throw new IOException("Mismatch between number of tokens and number of tags in rule " + match.getRule().getFullId()
        + " " + List.of(tokensInSuggestion)+ " " + List.of(postagsList));
      }
      boolean postagsMatch = true;
      List<AnalyzedTokenReadings> atrs = tagger.tag(List.of(tokensInSuggestion));
      for (int i=0; i<postagsList.length; i++) {
        postagsMatch = postagsMatch & atrs.get(i).matchesPosTagRegex(postagsList[i]);
        if (!postagsMatch) {
          break;
        }
      }
      if (postagsMatch) {
        newReplacements.add(replacement);
      }
    }
    if (newReplacements.isEmpty()) {
      return null;
    } else {
      ruleMatch.setSuggestedReplacements(newReplacements);
      return ruleMatch;
    }
  }

}