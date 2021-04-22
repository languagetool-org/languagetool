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
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * A rule that finds hidden characters in the text
 * 
 * @author Andriy Rysin
 * @since 2.9
 */
public class HiddenCharacterRule extends Rule {

  private static final Character HIDDEN_CHAR = '\u00AD'; // soft hyphen
  
  public HiddenCharacterRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  @Override
  public final String getId() {
    return "UK_HIDDEN_CHARS";
  }

  @Override
  public String getDescription() {
    return "Приховані символи: знак м’якого перенесення";
  }

  public String getShort() {
    return "Приховані символи";
  }

  public String getSuggestion(String word) {
    String highlighted = word.replace(HIDDEN_CHAR, '-');
    return " містить невидимий знак м’якого перенесення: «"+ highlighted +"», виправлення: ";
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (AnalyzedTokenReadings tokenReadings: tokens) {
      String tokenString = tokenReadings.getToken();

      if( tokenString.indexOf(HIDDEN_CHAR) != -1 ) {
          RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, sentence);
          ruleMatches.add(potentialRuleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private RuleMatch createRuleMatch(AnalyzedTokenReadings readings, AnalyzedSentence sentence) {
    String tokenString = readings.getToken();
    String replacement = tokenString.replace(HIDDEN_CHAR.toString(), "");
    String msg = tokenString + getSuggestion(tokenString) + replacement;

    RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, readings.getStartPos(), readings.getEndPos(), msg, getShort());
    potentialRuleMatch.setSuggestedReplacements(Arrays.asList(replacement));

    return potentialRuleMatch;
  }

}
