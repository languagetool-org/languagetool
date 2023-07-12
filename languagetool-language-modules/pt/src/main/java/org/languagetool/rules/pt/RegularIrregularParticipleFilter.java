/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.pt;

import java.io.IOException;
import java.util.Map;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.language.Portuguese;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.pt.PortugueseSynthesizer;
import org.languagetool.tools.StringTools;

public class RegularIrregularParticipleFilter extends RuleFilter {

  private Language language = new Portuguese();
  private PortugueseSynthesizer synth = (PortugueseSynthesizer) language.getSynthesizer();

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    String direction = getRequired("direction", arguments); // RegularToIrregular or IrregularToRegular
    AnalyzedTokenReadings atr = null;
    for (AnalyzedTokenReadings token : patternTokens) {
      if (token.getStartPos() == match.getFromPos()) {
        atr = token;
        break;
      }
    }
    String replacement = null;
    if (atr.hasPosTagStartingWith("VMP")) {
      AnalyzedToken selectedAT = null;
      String desiredPostag = null;
      for (AnalyzedToken at : atr) {
        if (at.getPOSTag() != null && at.getPOSTag().startsWith("VMP")) {
          selectedAT = at;
          desiredPostag = at.getPOSTag();
        }
      }
      // assente SC assentado SM
      if (desiredPostag.endsWith("C")) {
        desiredPostag = desiredPostag.substring(0,desiredPostag.length() - 1) + "[MC]";
      } else {
        desiredPostag = desiredPostag.substring(0,desiredPostag.length() - 1) 
            + "["+ desiredPostag.substring(desiredPostag.length() - 1) + "C]";
      }
      
      String[] participles;
      try {
        participles = synth.synthesize(selectedAT, desiredPostag, true);
      } catch (IOException e) {
        throw new IOException("Cannot synthesize " + selectedAT.toString() + e);
      }
      if (participles != null && participles.length > 1) {
        if (direction.equalsIgnoreCase("RegularToIrregular") && isRegular(atr.getToken())) {
          if (!isRegular(participles[0])) {
            replacement = participles[0];
          } else if (!isRegular(participles[1])) {
            replacement = participles[1];
          }
        } else if (direction.equalsIgnoreCase("IrregularToRegular") && !isRegular(atr.getToken())) {
          if (isRegular(participles[0])) {
            replacement = participles[0];
          } else if (isRegular(participles[1])) {
            replacement = participles[1];
          }
        }
        if (replacement != null) {
          String message = match.getMessage();
          RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(),
              match.getToPos(), message, match.getShortMessage());
          ruleMatch.setType(match.getType());
          String suggestion = match.getSuggestedReplacements().get(0).replace("{suggestion}", replacement);
          suggestion = suggestion.replace("{Suggestion}", StringTools.uppercaseFirstChar(replacement));
          suggestion = suggestion.replace("{SUGGESTION}", replacement.toUpperCase());
          ruleMatch.setSuggestedReplacement(suggestion);
          return ruleMatch;
        }
      }
    }
    return null;
  }

  boolean isRegular(String p) {
    String lp = p.toLowerCase();
    return lp.endsWith("do") || lp.endsWith("dos") || lp.endsWith("da") || lp.endsWith("das");
  }
}
