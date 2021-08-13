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
package org.languagetool.rules.es;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

public class TextToNumberFilter extends RuleFilter {

  private static Map<String, Long> numbers = new HashMap<String, Long>();
  private static Map<String, Long> multipliers = new HashMap<String, Long>();

  static {
    numbers.put("un", (long) 1);
    numbers.put("uno", (long) 1);
    numbers.put("una", (long) 1);
    numbers.put("dos", (long) 2);
    numbers.put("tres", (long) 3);
    numbers.put("cuatro", (long) 4);
    numbers.put("cinco", (long) 5);
    numbers.put("seis", (long) 6);
    numbers.put("siete", (long) 7);
    numbers.put("ocho", (long) 8);
    numbers.put("nueve", (long) 9);
    numbers.put("diez", (long) 10);
    numbers.put("once", (long) 11);
    numbers.put("doce", (long) 12);
    numbers.put("trece", (long) 13);
    numbers.put("catorce", (long) 14);
    numbers.put("quince", (long) 15);
    numbers.put("diecisés", (long) 16);
    numbers.put("diecisiete", (long) 17);
    numbers.put("dieciocho", (long) 18);
    numbers.put("diecinueve", (long) 19);
    numbers.put("veinte", (long) 20);
    numbers.put("veintiuno", (long) 21);
    numbers.put("veintidós", (long) 22);
    numbers.put("veintitrés", (long) 23);
    numbers.put("veinticuatro", (long) 24);
    numbers.put("veinticinco", (long) 25);
    numbers.put("veintiséis", (long) 26);
    numbers.put("veintisiete", (long) 27);
    numbers.put("veintiocho", (long) 28);
    numbers.put("veintinueve", (long) 29);
    numbers.put("treinta", (long) 30);
    numbers.put("cuarenta", (long) 40);
    numbers.put("cincuenta", (long) 50);
    numbers.put("sesenta", (long) 60);
    numbers.put("setenta", (long) 70);
    numbers.put("ochenta", (long) 80);
    numbers.put("noventa", (long) 90);
    numbers.put("cien", (long) 100);
    numbers.put("ciento", (long) 100);
    numbers.put("doscientos", (long) 200);
    numbers.put("trescientos", (long) 300);
    numbers.put("cuatrocientos", (long) 400);
    numbers.put("quinientos", (long) 500);
    numbers.put("seiscientos", (long) 600);
    numbers.put("setecientos", (long) 700);
    numbers.put("ochocientos", (long) 800);
    numbers.put("novecientos", (long) 900);
    numbers.put("doscientas", (long) 200);
    numbers.put("trescientas", (long) 300);
    numbers.put("cuatrocientas", (long) 400);
    numbers.put("quinientas", (long) 500);
    numbers.put("seiscientas", (long) 600);
    numbers.put("setecientas", (long) 700);
    numbers.put("ochocientas", (long) 800);
    numbers.put("novecientas", (long) 900);
    multipliers.put("mil", (long) 1000);
    multipliers.put("millón", (long) 1000000);
    multipliers.put("millones", (long) 1000000);
    multipliers.put("billón", (long) 10^12);
    multipliers.put("billones", (long) 10^12);
    multipliers.put("trillón", (long) 10^18);
    multipliers.put("trillones", (long) 10^18);
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    int posWord = 0;
    long total = 0;
    int current = 0;
    while (posWord < patternTokens.length && patternTokens[posWord].getEndPos() <= match.getToPos()) {
      // inside <marker>
      if (patternTokens[posWord].getStartPos() >= match.getFromPos()
          && patternTokens[posWord].getEndPos() <= match.getToPos()) {
        String form = patternTokens[posWord].getToken().toLowerCase();
        if (numbers.containsKey(form)) {
          current += numbers.get(form);
        } else if (multipliers.containsKey(form)) {
          if (current == 0) {
            // mil
            current = 1;
          }
          total += current * multipliers.get(form);
          current = 0;
        }
      }
      posWord++;
    }
    total += current;
    RuleMatch ruleMatch = match;
    ruleMatch.addSuggestedReplacement(Long.toString(total));
    return ruleMatch;
  }

}
