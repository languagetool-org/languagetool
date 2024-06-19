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
import org.languagetool.tagging.de.GermanTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static java.util.regex.Pattern.compile;

/**
 * Specific to {@code KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ} - helps setting the comma suggestion, if easily possible.
 * @since 4.5
 */
public class InsertCommaFilter extends RuleFilter {

  private static final Pattern WHITESPACE = compile("\\s");
  private static final Pattern SAGT = compile("[Ss]agt?");
  private static final Pattern DER_ETC = compile("der|die|das|seine|ihre|deine|unsere|meine|folgender|dieser");
  private static final Pattern DENKE_ETC = compile("denke|dachte|glaube|schätze|vermute|behaupte");
  private static final Pattern BEI_FUER_MIT = compile("bei|für|mit");
  private static final Pattern DIR_ETC = compile("[Di]ir|[Dd]ich|[Ee]uer|[Ee]uch");

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) {
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(), match.getShortMessage());
    List<String> suggestions = new ArrayList<>();
    for (String replacement : match.getSuggestedReplacements()) {
      String[] parts = WHITESPACE.split(replacement);
      try {
        if (parts.length == 2) {
          suggestions.add(parts[0] + ", " + parts[1]);
        } else if (parts.length == 3) {
          List<AnalyzedTokenReadings> tags1 = getTag(0, parts);
          List<AnalyzedTokenReadings> tags2 = getTag(1, parts);
          List<AnalyzedTokenReadings> tags3 = getTag(2, parts);
          if (hasTag(tags1, "VER:") && hasTag(tags2, "PRO:PER:")) {
            // "Ich hoffe(,) es geht Ihnen gut."
            suggestions.add(parts[0] + ", " + parts[1] + " " + parts[2]);
          } else if (SAGT.matcher(parts[0]).matches() && parts[1].equals("mal") && hasTag(tags3, "VER:")) {
            // "Sag mal(,) hast du"
            suggestions.add(parts[0] + " " + parts[1] + ", " + parts[2]);
          } else if (hasTag(tags1, "VER:") && hasTag(tags2, "ADV:") && hasTag(tags3, "VER:")) {
            // "Ich denke(,) hier kann aber auch ..."
            suggestions.add(parts[0] + ", " + parts[1] + " " + parts[2]);
          }
        } else if (parts.length >= 4 && parts.length <= 7) {
          List<AnalyzedTokenReadings> tags1 = getTag(0, parts);
          List<AnalyzedTokenReadings> tags2 = getTag(1, parts);
          List<AnalyzedTokenReadings> tags3 = getTag(2, parts);
          List<AnalyzedTokenReadings> tags4 = getTag(3, parts);
          String rest1 = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
          if (patternTokenPos <= 2 || (patternTokenPos == 3 && match.getSentence().getTokens().length >= 1 && match.getSentence().getTokens()[1].hasPosTagStartingWith("ADV:"))) {
            if (parts.length == 5 && hasTag(tags1, "VER:") && hasTag(tags2, "ART:") && hasTag(tags3, "SUB:") && hasTag(getTag(3, parts), "SUB:") && hasTag(getTag(4, parts), "VER:")) {
              // "Ist der Kunde Verbraucher(,) gilt ..."
              suggestions.add(parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3] + ",");
            } else if (parts.length == 4 &&
              patternTokens[0].hasPosTagStartingWith("VER:") &&
              DER_ETC.matcher(patternTokens[1].getToken()).matches()) {
              // "Aristoteles meint(,) das Genussleben führe nicht zum Glück."
              suggestions.add(parts[0] + ", " + rest1);
            } else if (hasTag(tags1, "VER:") && hasTag(tags2, "PRO:POS:") && hasTag(tags3, "SUB:")) {
              // "Ich glaube(,) eure Premium-Accounts sind noch aktiv."
              suggestions.add(parts[0] + ", " + rest1);
            } else if (hasTag(tags1, "VER:") && hasTag(tags2, "PRO:PER:") && hasTag(tags3, "ADV:INR")) {
              // "Weißt du(,) warum diese Regel aus ist?"
              String rest2 = String.join(" ", Arrays.asList(parts).subList(2, parts.length));
              suggestions.add(parts[0] + " " + parts[1] + ", " + rest2);
            } else if (hasTag(tags1, "VER:") && hasTag(tags2, "PRO:POS:") && hasTag(tags3, "ADJ:")) {
              // "Ich glaube(,) eure individuellen Premium-Accounts sind noch aktiv."
              suggestions.add(parts[0] + ", " + rest1);
            } else if (DENKE_ETC.matcher(parts[0]).matches() && hasTag(tags2, "PRO:DEM:") && hasTag(tags3, "SUB:")) {
              // "Ich schätze(,) diese Krawatte passt gut zum Anzug."
              suggestions.add(parts[0] + ", " + rest1);
            } else if (patternTokenPos == 1 && BEI_FUER_MIT.matcher(parts[1]).matches() &&
                       DIR_ETC.matcher(parts[2]).matches() && hasTag(tags4, "VER:")) {
              // "Hoffe(,) bei euch ist alles gut."
              suggestions.add(parts[0] + ", " + rest1);
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    ruleMatch.setSuggestedReplacements(suggestions);
    ruleMatch.setType(match.getType());
    return ruleMatch;
  }

  private static List<AnalyzedTokenReadings> getTag(int i, String[] parts) throws IOException {
    return GermanTagger.INSTANCE.tag(singletonList(parts[i]));
  }

  private boolean hasTag(List<AnalyzedTokenReadings> tags, String tagStart) {
    return tags.stream().anyMatch(k -> k.hasPosTagStartingWith(tagStart));
  }
}
