/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://danielnaber.de/)
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

import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;

public class WhitespaceCheckFilter extends RuleFilter  {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) {
    String wsChar = getRequired("whitespaceChar", arguments);
    int pos = Integer.parseInt(getRequired("position", arguments));
    if (pos < 1 || pos > patternTokens.length) {
      throw new IllegalArgumentException("Wrong position in WhitespaceCheckFilter: " + pos + ", must be 1 to " + patternTokens.length);
    }
    if (!patternTokens[pos - 1].getWhitespaceBefore().equals(wsChar)) {
      return match;
    } else {
      return null;
    }
  }

}
