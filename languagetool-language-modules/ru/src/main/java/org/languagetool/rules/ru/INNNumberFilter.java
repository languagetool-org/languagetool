/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Yakov Reztsov
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
package org.languagetool.rules.ru;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;


import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks if INN number is incorrect
 * @author Yakov Reztsov
 * @since 6.1
 */

public class INNNumberFilter extends RuleFilter {

  private static final Pattern DIGIT_SYMBOL_PATTERN = Pattern.compile("(\\d*)");


  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {

  String INNNumberString = getRequired("inn", args);

    Matcher matcherdigitsymbol = DIGIT_SYMBOL_PATTERN.matcher(INNNumberString);

      try {
          if (matcherdigitsymbol.matches()) {

              int[] intTab = String.valueOf(INNNumberString).chars().map(Character::getNumericValue).toArray();
              int kz1=0;
              int kz2=0;
              switch (intTab.length) {
                  case 10:
                      kz1 = (intTab[0]*2 + intTab[1]*4 + intTab[2]*10 + intTab[3]*3 + intTab[4]*5+ intTab[5]*9 + intTab[6]*4 + intTab[7]*6 + intTab[8]*8)%11;
                      if (kz1>9) {kz1=kz1-10;};
                      if (intTab[9] == kz1)  {
                          return null;
                      } else {
                          return match;
                      }
                  case 12:
                      kz1 = (intTab[0]*7 + intTab[1]*2 + intTab[2]*4 + intTab[3]*10 + intTab[4]*3 + intTab[5]*5+ intTab[6]*9 + intTab[7]*4 + intTab[8]*6 + intTab[9]*8)%11;
                      kz2 = (intTab[0]*3 + intTab[1]*7 + intTab[2]*2 + intTab[3]*4 + intTab[4]*10 + intTab[5]*3 + intTab[6]*5+ intTab[7]*9 + intTab[8]*4 + intTab[9]*6 + intTab[10]*8)%11;
                      if (kz1>9) {kz1=kz1-10;};
                      if (kz2>9) {kz2=kz2-10;};
                      
                      if ((intTab[10] == kz1) && (intTab[11] == kz2)) {
                          return null;
                      } else {
                          return match;
                      }
                  default:
                      return null;
              }

          } else {
              return null;
      }
    } catch (IllegalArgumentException ignore) {
      // happens with 'inn' like '7676gj8778' - those should be caught by a different rule
      return null;
    }
  }
}