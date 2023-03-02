/* LanguageTool, a natural language style checker 
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.patterns.RuleFilter;

public abstract class AbstractSuppressIfAnyRuleMatchesFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
//    if (match.getSentence().getText().contains("t'ho has cregut")) {
//      int ii=0;
//      ii++;
//    }
    List<String> ruleIDs = Arrays.asList(getRequired("ruleIDs", arguments).split(","));
    JLanguageTool lt = getJLanguageTool();
    String sentence = match.getSentence().getText();
    for (String replacement : match.getSuggestedReplacements()) {
      String newSentence = sentence.substring(0, match.getFromPos()) + replacement
          + sentence.substring(match.getToPos());
      List<RuleMatch> matches = lt.check(newSentence);
      for (RuleMatch m : matches) {
        if (ruleIDs.contains(m.getRule().getId())) {
          if ((m.getToPos() >= match.getFromPos() && m.getToPos() <= match.getToPos())
              || (match.getToPos() >= m.getFromPos() && match.getToPos() <= m.getToPos())) {
            return null;
          }
        }
      }
    }
    return match;
  }
  
  protected abstract JLanguageTool getJLanguageTool();

}
