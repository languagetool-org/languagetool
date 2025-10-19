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
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/*
 * Suggestions for rule ANAR_A_INFINITIU: anem a fer-li-ho -> li ho farem, li ho fem
 */

public class AnarASuggestionsFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    int initPos = 0;
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (initPos < tokens.length
      && (tokens[initPos].getStartPos() < match.getFromPos() || tokens[initPos].isSentenceStart())) {
      initPos++;
    }
    String verbPostag = tokens[initPos].readingWithTagRegex("V.IP.*").getPOSTag();
    String lemma = tokens[initPos + 2].readingWithTagRegex("V.N.*").getLemma();
    AnalyzedToken at = new AnalyzedToken("", "", lemma);
    String newPostag =  "V[MS]I[PF]" + verbPostag.substring(4, 8);
    Synthesizer synth = getSynthesizerFromRuleMatch(match);
    String[] synthForms = synth.synthesize(at, newPostag, true);
    if (synthForms.length == 0) {
      return null;
    }

    int adjustEndPos = 0;
    String[] result = PronomsFeblesHelper.getTwoNextPronouns(tokens,initPos + 3);
    String pronomsDarrere = result[0];
    adjustEndPos += Integer.valueOf(result[1]);

    int adjustStartPos = 0;
    String[] result2 = PronomsFeblesHelper.getPreviousPronouns(tokens, initPos - 1);
    String pronomsDavant = result2[0];
    adjustStartPos += Integer.valueOf(result2[1]);

    List<String> replacements = new ArrayList<>();
    for (String verb : synthForms) {
      String suggestion = "";
      if (!pronomsDarrere.isEmpty()) {
        suggestion = PronomsFeblesHelper.transformDavant(pronomsDarrere, verb);
      } else if (!pronomsDavant.isEmpty()) {
        suggestion = PronomsFeblesHelper.transformDavant(pronomsDavant, verb);
      }
      suggestion += verb;
      suggestion = StringTools.preserveCase(suggestion, tokens[initPos - adjustStartPos].getToken());
      replacements.add(suggestion);
    }
    if (replacements.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[initPos - adjustStartPos].getStartPos(),
      tokens[initPos + 2 + adjustEndPos].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(replacements);
    return ruleMatch;
  }

}