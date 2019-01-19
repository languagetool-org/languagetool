/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber
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
package org.languagetool.rules.de;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Specific to {@code KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ} - helps setting the comma suggestion, if easily possible.
 * @since 4.5
 */
public class InsertCommaFilter extends RuleFilter {

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedTokenReadings[] patternTokens) {
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
    List<String> suggestions = new ArrayList<>();
    for (String replacement : match.getSuggestedReplacements()) {
      String[] parts = replacement.split("\\s");
      if (parts.length == 2) {
        // the other cases don't seem to be that easy...
        suggestions.add(parts[0] + ", " + parts[1]);
      }
    }
    ruleMatch.setSuggestedReplacements(suggestions);
    ruleMatch.setType(match.getType());
    return ruleMatch;
  }
}
