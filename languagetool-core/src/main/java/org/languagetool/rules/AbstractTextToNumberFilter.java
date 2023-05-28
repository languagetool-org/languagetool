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
package org.languagetool.rules;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;

public abstract class AbstractTextToNumberFilter extends RuleFilter {

  protected static Map<String, Float> numbers = new HashMap<String, Float>();
  protected static Map<String, Float> multipliers = new HashMap<String, Float>();

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    int posWord = 0;
    float total = 0;
    float current = 0;
    float totalDecimal = 0;
    float currentDecimal = 0;
    int addedZeros = 0;
    boolean percentage = false;
    boolean decimal = false;
    while (posWord < patternTokens.length && patternTokens[posWord].getEndPos() <= match.getToPos()) {
      // inside <marker>
      if (patternTokens[posWord].getStartPos() >= match.getFromPos()
          && patternTokens[posWord].getEndPos() <= match.getToPos()) {
        String form = patternTokens[posWord].getToken().toLowerCase();
        if (posWord > 0 && isPercentage(patternTokens, posWord)) {
          percentage = true;
          break;
        }
        if (isComma(form)) {
          decimal = true;
          posWord++;
          continue;
        }
        List<String> forms = tokenize(form);
        for (String subForm : forms) {
          if (!decimal) {
            if (numbers.containsKey(subForm)) {
              current += numbers.get(subForm);
            } else if (multipliers.containsKey(subForm)) {
              if (current == 0) {// mil
                current = 1;
              }
              total += current * multipliers.get(subForm);
              current = 0;
            }
          } else {
            if (numbers.containsKey(subForm)) {
              int zerosToAdd = format((numbers.get(subForm)), false).length();
              currentDecimal += numbers.get(subForm) / Math.pow(10, addedZeros + zerosToAdd);
              addedZeros++;
            } /* else: multipliers after the decimal comma are not expected */
          }
        }

      }
      posWord++;
    }
    total += current;
    totalDecimal += currentDecimal;
    total = total + totalDecimal /* / (Float.toString(totalDecimal).length() + addedZeros) */;
    RuleMatch ruleMatch = match;
    String sugg = format(total, percentage);

    ruleMatch.addSuggestedReplacement(sugg);
    return ruleMatch;
  }

  private String format(float d, boolean percentage) {
    String result;
    if (d == (long) d) {
      result = String.format("%d", (long) d);
    } else {
      result = String.format("%s", d);
    }
    if (percentage) {
      result = result + "\u202F%"; // narrow non-breaking space + percentage
    }
    return formatResult(result);
  }
  
  abstract protected boolean isComma(String s);
  
  abstract protected boolean isPercentage(AnalyzedTokenReadings[] patternTokens, int i);
  
  protected String formatResult(String s) {
    return s;
  };
  
  protected List<String> tokenize(String s) {
    return Collections.singletonList(s);
  };
  

}
