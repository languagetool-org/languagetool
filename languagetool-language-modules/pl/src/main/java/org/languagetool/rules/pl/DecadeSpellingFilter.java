/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.pl;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.Map;

/**
 * The filter is used to create suggestions for the DATA_DEKADY rule.
 * The parameter is called 'lata'.
 */
public class DecadeSpellingFilter extends RuleFilter {

  private static int[]    numbers = { 1000,  900,  500,  400,  100,   90,
      50,   40,   10,    9,    5,    4,    1 };

  private static String[] letters = { "M",  "CM",  "D",  "CD", "C",  "XC",
      "L",  "XL",  "X",  "IX", "V",  "IV", "I" };

  /**
   * Gets the Roman notation for numbers.
   * @param num the integer to convert
   * @return the String using the number.
   */
  private String getRomanNumber(int num) {
    String roman = "";  // The roman numeral.
    int N = num;        // N represents the part of num that still has
    //   to be converted to Roman numeral representation.
    for (int i = 0; i < numbers.length; i++) {
      while (N >= numbers[i]) {
        roman += letters[i];
        N -= numbers[i];
      }
    }
    return roman;
  }

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedTokenReadings[] patternTokens) {
    try {
      String decade = arguments.get("lata").substring(2);
      String century = arguments.get("lata").substring(0, 2);
      int cent = Integer.parseInt(century);
      String message = match.getMessage()
          .replace("{dekada}", decade)
          .replace("{wiek}", getRomanNumber(cent + 1));
      RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), message, match.getShortMessage(),
              match.getFromPos() == 0, null);
      ruleMatch.setType(match.getType());
      return ruleMatch;
    } catch (IllegalArgumentException ignore) {
      // ignore it silently
      return null;
    }

  }


}
