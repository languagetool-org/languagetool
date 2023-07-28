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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.AnalyzedToken;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tools.StringTools;

/*
 * Suggestions for rule PORTA_UNA_HORA: porta una hora -> fa una hora que
 */

public class PortarTempsSuggestionsFilter extends RuleFilter {

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
    StringBuilder suggestion = new StringBuilder();
    String verbPostag = tokens[posWord].readingWithTagRegex("V.*").getPOSTag();
    AnalyzedToken at = new AnalyzedToken("", "", "fer");
    String newPostag = verbPostag.substring(0, 4) + "[30][S0]." + verbPostag.substring(7, 8);
    String[] synthForms = synth.synthesize(at, newPostag, true,
      getLanguageVariantCode(match));
    if (synthForms.length == 0) {
      return null;
    }
    suggestion.append(synthForms[0]);
    int i = posWord + 1;
    while (tokens[i].getChunkTags().contains(new ChunkTag("PTime"))) {
      if (tokens[i].isWhitespaceBefore()) {
        suggestion.append(" ");
      }
      suggestion.append(tokens[i].getToken());
      i++;
    }
    int lastTokenPos = i;
    if (lastTokenPos + 1 >= tokens.length) {
      return null;
    }
    int adjustEndPos = 0;
    AnalyzedTokenReadings lastToken = tokens[lastTokenPos];
    if (lastToken.getToken().equals("que")) {
      suggestion.append(" que");
    } else if (lastToken.hasPosTagStartingWith("VMG") || lastToken.hasPosTagStartingWith("VSG")) {
      suggestion.append(" que ");
      String[] result = PronomsFeblesHelper.getTwoNextPronouns(tokens,lastTokenPos + 1);
      String pronoms = result[0];
      adjustEndPos += Integer.valueOf(result[1]);
      AnalyzedToken at2 = new AnalyzedToken("", "", lastToken.readingWithTagRegex("V.G.*").getLemma());
      String[] synthForms2 = synth.synthesize(at2, "V.I" + verbPostag.substring(3,8), true, getLanguageVariantCode(match));
      if (synthForms2.length == 0) {
        return null;
      }
      if (!pronoms.isEmpty()) {
        suggestion.append(PronomsFeblesHelper.transformDavant(pronoms, synthForms2[0]));
      }
      suggestion.append(synthForms2[0]);
    } else if (lastToken.getToken().equals("sense")
      && (tokens[lastTokenPos + 1].hasPosTagStartingWith("VSN")
      || tokens[lastTokenPos + 1].hasPosTagStartingWith("VMN"))) {
      suggestion.append(" que no ");
      adjustEndPos++;
      String[] result = PronomsFeblesHelper.getTwoNextPronouns(tokens,lastTokenPos + 2);
      String pronoms = result[0];
      adjustEndPos += Integer.valueOf(result[1]);
      AnalyzedToken at2 = new AnalyzedToken("", "", tokens[lastTokenPos + 1].readingWithTagRegex("V.N.*").getLemma());
      String[] synthForms2 = synth.synthesize(at2, "V.I" + verbPostag.substring(3,8), true, getLanguageVariantCode(match));
      if (synthForms2.length == 0) {
        return null;
      }
      if (!pronoms.isEmpty()) {
        suggestion.append(PronomsFeblesHelper.transformDavant(pronoms, synthForms2[0]));
      }
      suggestion.append(synthForms2[0]);
    } else if (lastToken.getToken().equals("així") || lastToken.getToken().equals("a") || lastToken.getToken().equals("en")
      || lastToken.getToken().equals("de")
      || lastToken.hasPosTagStartingWith("AQ")
      || lastToken.hasPosTagStartingWith("VMP")) {
      AnalyzedToken at2 = new AnalyzedToken("", "", "estar");
      String[] synthForms2 = synth.synthesize(at2, "V.I" + verbPostag.substring(3,8), true, getLanguageVariantCode(match));
      if (synthForms2.length == 0) {
        return null;
      }
      suggestion.append(" que " + synthForms2[0]);
      adjustEndPos--;
    }else {
      return null;
    }
    String replacement = suggestion.toString();
    replacement = StringTools.preserveCase(replacement, tokens[posWord].getToken());
    if (replacement.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[posWord].getStartPos(),
      tokens[lastTokenPos + adjustEndPos].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacement(replacement);
    return ruleMatch;
  }

  private String getLanguageVariantCode(RuleMatch match) {
    PatternRule pr = (PatternRule) match.getRule();
    return pr.getLanguage().getShortCodeWithCountryAndVariant();
  }
}