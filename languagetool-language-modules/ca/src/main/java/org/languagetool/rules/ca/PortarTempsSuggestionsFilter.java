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
    if (synthForms.length > 0) {
      suggestion.append(synthForms[0]);
    } else {
      return null;
    }
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
    int correctEnd = 0;
    AnalyzedTokenReadings lastToken = tokens[lastTokenPos];
    if (lastToken.getToken().equals("que")) {
      suggestion.append(" que");
    } else if (lastToken.hasPosTagStartingWith("VMG") || lastToken.hasPosTagStartingWith("VSG")) {
      suggestion.append(" que ");
      String pronoms = getTwoNextPronouns(tokens,lastTokenPos + 1);
      correctEnd += Integer.valueOf(pronoms.substring(pronoms.length() - 1));
      pronoms = pronoms.substring(0, pronoms.length() - 1);
      AnalyzedToken at2 = new AnalyzedToken("", "", lastToken.readingWithTagRegex("V.G.*").getLemma());
      String[] synthForms2 = synth.synthesize(at2, verbPostag, getLanguageVariantCode(match));
      if (synthForms2.length > 0) {
        if (!pronoms.isEmpty()) {
          suggestion.append(PronomsFeblesHelper.transformDavant(pronoms, synthForms2[0]));
        }
        suggestion.append(synthForms2[0]);
      }  else {
        return null;
      }
    } else if (lastToken.getToken().equals("sense")
      && (tokens[lastTokenPos + 1].hasPosTagStartingWith("VSN")
      || tokens[lastTokenPos + 1].hasPosTagStartingWith("VMN"))) {
      suggestion.append(" que no ");
      correctEnd++;
      String pronoms = getTwoNextPronouns(tokens,lastTokenPos + 2);
      correctEnd += Integer.valueOf(pronoms.substring(pronoms.length() - 1));
      pronoms = pronoms.substring(0, pronoms.length() - 1);
      AnalyzedToken at2 = new AnalyzedToken("", "", tokens[lastTokenPos + 1].readingWithTagRegex("V.N.*").getLemma());
      String[] synthForms2 = synth.synthesize(at2, verbPostag, getLanguageVariantCode(match));
      if (synthForms2.length > 0) {
        if (!pronoms.isEmpty()) {
          suggestion.append(PronomsFeblesHelper.transformDavant(pronoms, synthForms2[0]));
        }
        suggestion.append(synthForms2[0]);
      }  else {
        return null;
      }
    } else {
      return null;
    }

    String replacement = suggestion.toString();
    replacement = StringTools.preserveCase(replacement, tokens[posWord].getToken());
    if (replacement.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[posWord].getStartPos(),
      tokens[lastTokenPos + correctEnd].getEndPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacement(replacement);
    return ruleMatch;
  }

  private String getTwoNextPronouns(AnalyzedTokenReadings[] tokens, int from) {
    int correctEnd = 0;
    String pronoms = "";
    if (from < tokens.length && !tokens[from].isWhitespaceBefore()) {
      AnalyzedToken pronom = tokens[from].readingWithTagRegex("P.*");
      if (pronom != null) {
        pronoms = pronom.getToken();
        correctEnd++;
      }
      if (from + 1 < tokens.length && !tokens[from + 1].isWhitespaceBefore()) {
        AnalyzedToken pronom2 = tokens[from + 1].readingWithTagRegex("P.*");
        if (pronom2 != null) {
          pronoms = pronoms + pronom2.getToken();
          correctEnd++;
        }
      }
    }
    return pronoms + String.valueOf(correctEnd);
  }
  private String getLanguageVariantCode(RuleMatch match) {
    PatternRule pr = (PatternRule) match.getRule();
    return pr.getLanguage().getShortCodeWithCountryAndVariant();
  }
}