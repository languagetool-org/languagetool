/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UnderlineSpacesFilter extends RuleFilter {

  /*
   * Underline the whitespaces before and/or after the marker in the pattern
   */
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    String underlineSpaces = getRequired("underlineSpaces", arguments); // before/after/both
    String sentence = match.getSentence().getText();
    if (underlineSpaces.equals("before") || underlineSpaces.equals("both")) {
      if (match.getFromPos() - 1 >= 0
        && StringTools.isWhitespace(sentence.substring(match.getFromPos() - 1, match.getFromPos()))) {
        match.setOffsetPosition(match.getFromPos() - 1, match.getToPos());
      }
    }
    if (underlineSpaces.equals("after") || underlineSpaces.equals("both")) {
      if (match.getToPos() + 1 < sentence.length()
        && StringTools.isWhitespace(sentence.substring(match.getToPos(), match.getToPos() + 1))) {
        match.setOffsetPosition(match.getFromPos(), match.getToPos() + 1);
      }
    }
    return match;
  }
}
