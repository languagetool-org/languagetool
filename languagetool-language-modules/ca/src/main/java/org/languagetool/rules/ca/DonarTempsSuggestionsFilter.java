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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.AnalyzedToken;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tools.StringTools;

/*
 * Suggestions for rule DONAR_TEMPS: no em dóna temps -> no hi ha temps, no tinc temps
 */

public class DonarTempsSuggestionsFilter extends RuleFilter {

  static private CatalanSynthesizer synth = CatalanSynthesizer.INSTANCE;

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    int posWord = 0;
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (posWord < tokens.length
        && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    String pronomPostag = tokens[posWord].readingWithTagRegex("P.*").getPOSTag();
    String pronomGenderNumber = pronomPostag.substring(2, 3) + pronomPostag.substring(4, 5);
    int indexFirstVerb = posWord + 1;
    int indexMainVerb = indexFirstVerb;
    while (!tokens[indexMainVerb].hasAnyLemma("donar")) {
      indexMainVerb++;
    }
    String verbPostag = tokens[indexMainVerb].readingWithTagRegex("V.*").getPOSTag();

    // haver-hi temps
    AnalyzedToken at = new AnalyzedToken("", "", "haver");
    String[] synthForms = synth.synthesize(at, "VA" + verbPostag.substring(2, 8),
        getLanguageVariantCode(match));
    StringBuilder suggestion1 = new StringBuilder();
    if (synthForms.length > 0) {
      int index = indexFirstVerb;
      suggestion1.append("hi");
      while (index < indexMainVerb) {
        if (tokens[index].isWhitespaceBefore() || suggestion1.length()==2) {
          suggestion1.append(" ");
        }
        suggestion1.append(tokens[index].getToken());
        index++;
      }
      suggestion1.append(" "+ synthForms[0] + " temps");
    }
    List<String> replacements = new ArrayList<>();
    String sugg1 = suggestion1.toString().replace("de haver", "d'haver");
    sugg1 = StringTools.preserveCase(sugg1, tokens[posWord].getToken());
    if (!sugg1.isEmpty()) {
      replacements.add(sugg1);
    }

    // tenir temps
    StringBuilder suggestion2 = new StringBuilder();
    int index = indexFirstVerb;
    if (index == indexMainVerb) {
      String[] synthForms2 = synth.synthesize(new AnalyzedToken("", "", "tenir"),
        verbPostag.substring(0, 4) + pronomGenderNumber + verbPostag.substring(6, 8), getLanguageVariantCode(match));
      if (synthForms2.length > 0) {
        suggestion2.append(synthForms2[0] + " temps");
      }
    } else {
      AnalyzedToken at2 = tokens[indexFirstVerb].getAnalyzedToken(0);
      String[] synthForms2 = synth.synthesize( at2,
        at2.getPOSTag().substring(0, 4) + pronomGenderNumber + at2.getPOSTag().substring(6, 8), getLanguageVariantCode(match));
      if (synthForms2.length > 0) {
        suggestion2.append(synthForms2[0]);
        index++;
        while (index < indexMainVerb) {
          if (tokens[index].isWhitespaceBefore()) {
            suggestion2.append(" ");
          }
          suggestion2.append(tokens[index].getToken());
          index++;
        }
        String[] synthForms3 = synth.synthesize( new AnalyzedToken("", "", "tenir"),
          tokens[indexMainVerb].getAnalyzedToken(0).getPOSTag(), getLanguageVariantCode(match));
        if (synthForms3.length > 0) {
          suggestion2.append(" " + synthForms3[0] + " temps");
        } else {
          suggestion2 = new StringBuilder();
        }
      }
    }
    String sugg2 = suggestion2.toString();
    sugg2 = StringTools.preserveCase(sugg2, tokens[posWord].getToken());
    if (!sugg2.isEmpty()) {
      replacements.add(sugg2);
    }
    if (replacements.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[posWord].getStartPos(),
        tokens[indexMainVerb + 1].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(replacements);
    return ruleMatch;
  }

  private String getLanguageVariantCode(RuleMatch match) {
    PatternRule pr = (PatternRule) match.getRule();
    return pr.getLanguage().getShortCodeWithCountryAndVariant();
  }
}