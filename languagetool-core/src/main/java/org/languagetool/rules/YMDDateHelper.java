/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Fabian Richter
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

/**
 * @since 4.3
 */
public class YMDDateHelper {

  public YMDDateHelper() {
  }

  public Map<String, String> parseDate(Map<String, String> args) {
    String dateString = args.get("date");
    if (dateString == null) {
      throw new IllegalArgumentException("Missing key 'date'");
    }
    String[] parts = dateString.split("-");
    if (parts.length != 3) {
      throw new RuntimeException("Expected date in format 'yyyy-mm-dd': '" + dateString + "'");
    }
    args.put("year", parts[0]);
    args.put("month", parts[1]);
    args.put("day", parts[2]);
    return args;
  }

  public RuleMatch correctDate(RuleMatch match, Map<String, String> args) {
    String year = args.get("year");
    String month = args.get("month");
    String day = args.get("day");
    int correctYear = Integer.parseInt(year) + 1;
    String correctDate = String.format("%d-%s-%s", correctYear, month, day);
    String message = match.getMessage()
            .replace("{realDate}", correctDate);
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(),
            match.getToPos(), message, match.getShortMessage());
    ruleMatch.setType(match.getType());
    return ruleMatch;
  }
}