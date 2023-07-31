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
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/*
 * Suggestions for rule ANAR_A_INFINITIU: anem a fer-li-ho -> li ho farem, li ho fem
 */

public class AnarASuggestionsFilter extends RuleFilter {

  static private CatalanSynthesizer synth = CatalanSynthesizer.INSTANCE;

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens) throws IOException {
    int initPos = 0;
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (initPos < tokens.length
      && (tokens[initPos].getStartPos() < match.getFromPos() || tokens[initPos].isSentenceStart())) {
      initPos++;
    }
    String verbPostag = tokens[initPos].readingWithTagRegex("V.IP.*").getPOSTag();
    String lemma = tokens[initPos + 2].readingWithTagRegex("V.N.*").getLemma();
    AnalyzedToken at = new AnalyzedToken("", "", lemma);
    String newPostag =  "V.I[PF]" + verbPostag.substring(4, 8);
    String[] synthForms = synth.synthesize(at, newPostag, true,
      getLanguageVariantCode(match));
    if (synthForms.length == 0) {
      return null;
    }
    int adjustEndPos = 0;
    String[] result = PronomsFeblesHelper.getTwoNextPronouns(tokens,initPos + 3);
    String pronomsDarrere = result[0];
    adjustEndPos += Integer.valueOf(result[1]);
    List<String> replacements = new ArrayList<>();
    for (String verb : synthForms) {
      String suggestion = "";
      if (!pronomsDarrere.isEmpty()) {
        suggestion = PronomsFeblesHelper.transformDavant(pronomsDarrere, verb);
      }
      suggestion += verb;
      suggestion = StringTools.preserveCase(suggestion, tokens[initPos].getToken());
      replacements.add(suggestion);
    }
    if (replacements.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[initPos].getStartPos(),
      tokens[initPos + 2 + adjustEndPos].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(replacements);
    return ruleMatch;
  }

  private String getLanguageVariantCode(RuleMatch match) {
    PatternRule pr = (PatternRule) match.getRule();
    return pr.getLanguage().getShortCodeWithCountryAndVariant();
  }
}