/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.languagetool.rules.ca.PronomsFeblesHelper.getTwoNextPronouns;
import static org.languagetool.rules.ca.PronomsFeblesHelper.getPreviousPronouns;
import static org.languagetool.rules.ca.PronomsFeblesHelper.transformDavant;

/*
 * Suggestions for rule PORTAR_GERUNDI: porto fent-ho -> ho faig, ho he fet
 */

public class PortarGerundiSuggestionsFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    int posWord = 0;
    Synthesizer synth = getSynthesizerFromRuleMatch(match);
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    String newLemma = getOptional("newLemma", arguments, "");
    while (posWord < tokens.length
      && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    AnalyzedToken atr1 = tokens[posWord].readingWithTagRegex("V.[IS].*");
    AnalyzedToken atr2 = tokens[posWord + 1].readingWithTagRegex("V.G.*");
    List<String> replacements = new ArrayList<>();
    // he fet
    String lemma = (newLemma.isEmpty()? atr2.getLemma(): newLemma);
    String[] synthForms1 = synth.synthesize(new AnalyzedToken("", "", "haver"), "VA"+atr1.getPOSTag().substring(2), true);
    String[] synthForms2 = synth.synthesize(new AnalyzedToken("", "", lemma), "V.P..SM.", true);
    if (synthForms1 != null && synthForms2 != null) {
      for (String synthForm1 : synthForms1) {
        for (String synthForm2 : synthForms2) {
          replacements.add(synthForm1+" "+synthForm2);
        }
      }
    }
    // faig
    String[] synthForms3 = synth.synthesize(new AnalyzedToken("", "", lemma), "V."+atr1.getPOSTag().substring(2), true);
    if (synthForms3 != null) {
      replacements.add(synthForms3[0]);
    }
    if (replacements.isEmpty()) {
      return null;
    }
    String[] nextPronouns = getTwoNextPronouns(tokens, posWord + 2);
    String[] previousPronouns = getPreviousPronouns(tokens, posWord - 1);
    int correctStartIndex = 0;
    int correctEndIndex = 0;
    for (int i = 0; i < replacements.size(); i++) {
      String pronounsSuggestion = "";
      if (!nextPronouns[0].isEmpty()) {
        pronounsSuggestion = transformDavant(nextPronouns[0], replacements.get(i));
        correctEndIndex = Integer.parseInt(nextPronouns[1]);
      } else if (!previousPronouns[0].isEmpty()) {
        pronounsSuggestion = transformDavant(previousPronouns[0], replacements.get(i));
        correctStartIndex = - Integer.parseInt(previousPronouns[1]);
      }
      replacements.set(i, StringTools.preserveCase(pronounsSuggestion + replacements.get(i), tokens[posWord + correctStartIndex].getToken()));
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[posWord + correctStartIndex].getStartPos(),
      tokens[posWord + 1 + correctEndIndex].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(replacements);
    return ruleMatch;
  }

}