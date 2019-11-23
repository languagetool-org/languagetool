/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.nl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * Finds words for which a better variant exists.
 * @since 4.1
 */
public class PreferredWordRule extends Rule {

  private static final String DESC = "Suggereert een gebruikelijker woord.";

  private static final PreferredWordData data = new PreferredWordData(DESC);

  public PreferredWordRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("Hij vindt <marker>rijwiel</marker> een ouderwets woord."),
                   Example.fixed("En ik vind <marker>fiets</marker> ook beter."));
  }

  @Override
  public String getId() {
    return "NL_PREFERRED_WORD_RULE";
  }

  @Override
  public String getDescription() {
    return DESC;
  }

  @Override
  public int estimateContextForSureMatch() {
    return 1;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    for (PreferredWordRuleWithSuggestion ruleWithSuggestion : data.get()) {
      Rule rule = ruleWithSuggestion.rule;
      RuleMatch[] matches = rule.match(sentence);
      if (matches.length > 0) {
        RuleMatch match = matches[0];
        String matchedText = sentence.getText().substring(match.getFromPos(), match.getToPos());
        //String textFromMatch = sentence.getText().substring(match.getFromPos());
        String suggestion = matchedText.replace(ruleWithSuggestion.oldWord, ruleWithSuggestion.newWord);
        if (!suggestion.equals(matchedText)) {
          match.setSuggestedReplacement(suggestion);
          ruleMatches.addAll(Arrays.asList(matches));
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }
  
}
