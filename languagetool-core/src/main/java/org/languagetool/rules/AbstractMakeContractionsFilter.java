/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;

public abstract class AbstractMakeContractionsFilter extends RuleFilter {
  
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    RuleMatch newMatch = match;
    List<String> newSugestions = new ArrayList<>();
    for (String suggestion: match.getSuggestedReplacements()) {
      newSugestions.add(fixContractions(suggestion));
    }
    newMatch.setSuggestedReplacements(newSugestions);
    return newMatch;
  }
  
  protected abstract String fixContractions(String suggestion);
}
