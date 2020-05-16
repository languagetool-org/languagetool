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
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tagging.Tagger;

import java.io.IOException;
import java.util.*;

/**
 * Specific to {@code KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ} - helps setting the comma suggestion, if easily possible.
 * @since 4.5
 */
public class InsertCommaFilter extends RuleFilter {

  private final static Tagger tagger = Languages.getLanguageForShortCode("de").getTagger();

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
    List<String> suggestions = new ArrayList<>();
    for (String replacement : match.getSuggestedReplacements()) {
      String[] parts = replacement.split("\\s");
      if (parts.length == 2) {
        suggestions.add(parts[0] + ", " + parts[1]);
      } else if (parts.length == 3) {
        try {
          List<AnalyzedTokenReadings> tags1 = tagger.tag(Collections.singletonList(parts[0]));
          List<AnalyzedTokenReadings> tags2 = tagger.tag(Collections.singletonList(parts[1]));
          List<AnalyzedTokenReadings> tags3 = tagger.tag(Collections.singletonList(parts[2]));
          //System.out.println("#"+tagger.tag(Collections.singletonList(parts[0])));
          //System.out.println("#"+tagger.tag(Collections.singletonList(parts[1])));
          //System.out.println("#"+tagger.tag(Collections.singletonList(parts[2])));
          if (tags1.stream().anyMatch(k -> k.hasPosTagStartingWith("VER:")) && tags2.stream().anyMatch(k -> k.hasPosTagStartingWith("PRO:PER:"))) {
            // "Ich hoffe(,) es geht Ihnen gut."
            suggestions.add(parts[0] + ", " + parts[1] + " " + parts[2]);
          } else if (parts[0].matches("Sag|Sagt") && parts[1].matches("mal") &&
                  tagger.tag(Collections.singletonList(parts[2])).stream().anyMatch(k -> k.hasPosTagStartingWith("VER:"))) {
            // "Sag mal(,) hast du"
            suggestions.add(parts[0] + " " + parts[1] + ", " + parts[2]);
          } else if (tags1.stream().anyMatch(k -> k.hasPosTagStartingWith("VER:")) && tags2.stream().anyMatch(k -> k.hasPosTagStartingWith("ADV:")) &&
            tags3.stream().anyMatch(k -> k.hasPosTagStartingWith("VER:"))) {
            // "Ich denke(,) hier kann aber auch ..."
            suggestions.add(parts[0] + ", " + parts[1] + " " + parts[2]);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else if (parts.length == 4) {
        if (patternTokenPos == 2 &&
          patternTokens[0].hasPosTagStartingWith("VER:") &&
          patternTokens[1].getToken().matches("der|die|das|seine|ihre|deine|unsere|meine|folgender|dieser")) {
          // "Aristoteles meint(,) das Genussleben führe nicht zum Glück."
          suggestions.add(parts[0] + ", " + parts[1] + " " + parts[2] + " " + parts[3]);
        }
      }
    }
    ruleMatch.setSuggestedReplacements(suggestions);
    ruleMatch.setType(match.getType());
    return ruleMatch;
  }
}
