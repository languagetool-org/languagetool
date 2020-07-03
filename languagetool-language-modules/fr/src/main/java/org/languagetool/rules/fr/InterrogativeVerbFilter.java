/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.FrenchSynthesizer;

/*
 * Get appropriate suggestions for French verbs in interrogative form
 * e.g. prérères-tu
 */

public class InterrogativeVerbFilter extends RuleFilter {

  // private static final Pattern PronounSubject = Pattern.compile("R pers suj
  // ([123] [sp])");
  private static final FrenchSynthesizer synth = new FrenchSynthesizer(new French());

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    Set<String> replacements = new HashSet<>();
    String PronounFrom = getRequired("PronounFrom", arguments);
    String VerbFrom = getRequired("VerbFrom", arguments);
    String desiredPostag = null;

    if (PronounFrom != null && VerbFrom != null) {
      int posPronoun = Integer.parseInt(PronounFrom);
      if (posPronoun < 1 || posPronoun > patternTokens.length) {
        throw new IllegalArgumentException(
            "ConfusionCheckFilter: Index out of bounds in " + match.getRule().getFullId() + ", PronounFrom: " + posPronoun);
      }
      int posVerb = Integer.parseInt(VerbFrom);
      if (posVerb < 1 || posVerb > patternTokens.length) {
        throw new IllegalArgumentException(
            "ConfusionCheckFilter: Index out of bounds in " + match.getRule().getFullId() + ", VerbFrom: " + posVerb);
      }
      
      AnalyzedTokenReadings atrVerb = patternTokens[posVerb - 1];
      AnalyzedTokenReadings atrPronoun = patternTokens[posPronoun - 1];
      if (atrPronoun.matchesPosTagRegex(".* 1 s")) {
        desiredPostag = "V .*(ind|cond).* 1 s";
      }
      if (atrPronoun.matchesPosTagRegex(".* 2 s")) {
        desiredPostag = "V .*(ind|cond).* 2 s";
      }
      if (atrPronoun.matchesPosTagRegex(".* 3 s")) {
        desiredPostag = "V .*(ind|cond).* 3 s";
      }
      if (atrPronoun.matchesPosTagRegex(".* 1 p")) {
        desiredPostag = "V .*(ind|cond).* 1 p";
      }
      if (atrPronoun.matchesPosTagRegex(".* 2 p")) {
        desiredPostag = "V .*(ind|cond).* 2 p";
      }
      if (atrPronoun.matchesPosTagRegex(".* 3 p")) {
        desiredPostag = "V .*(ind|cond).* 3 p";
      }
      if (atrVerb.matchesPosTagRegex("V .*") && desiredPostag != null) {
        for (AnalyzedToken at : atrVerb) {
          if (at.getPOSTag().startsWith("V ")) {
            String synthesized[] = synth.synthesize(at, desiredPostag, true);
            if (synthesized != null) {
              replacements.addAll(Arrays.asList(synthesized));
            }
          }
        }
      
      } 
      /*TODO
      else {
        //if there isn't a verb try to find one with the speller
      }*/
    }
    String message = match.getMessage();
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
        message, match.getShortMessage());
    ruleMatch.setType(match.getType());
    if (!replacements.isEmpty()) {
      ruleMatch.setSuggestedReplacements(new ArrayList<String>(replacements));
    }
    return ruleMatch;
  }
}
