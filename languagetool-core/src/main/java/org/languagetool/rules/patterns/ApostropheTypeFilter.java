/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import java.io.IOException;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;

public class ApostropheTypeFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    String wordFrom = getRequired("wordFrom", arguments);
    boolean hasTypographicalApostrophe = getRequired("hasTypographicalApostrophe", arguments).equalsIgnoreCase("true");
    if (wordFrom != null) {
      int posWord = 0;
      if (wordFrom.equals("marker")) {
        while (posWord < patternTokens.length && patternTokens[posWord].getStartPos() < match.getFromPos()) {
          posWord++;
        }
        posWord++;
      } else {
        posWord = Integer.parseInt(wordFrom);
      }
      if (posWord < 1 || posWord > patternTokens.length) {
        throw new IllegalArgumentException("ApostropheTypeFilter: Index out of bounds in "
            + match.getRule().getFullId() + ", wordFrom: " + posWord);
      }
      AnalyzedTokenReadings atrWord = patternTokens[posWord - 1];
      if (hasTypographicalApostrophe == atrWord.hasTypographicApostrophe()) {
        return match;
      }
    }
    return null;
  }

}
