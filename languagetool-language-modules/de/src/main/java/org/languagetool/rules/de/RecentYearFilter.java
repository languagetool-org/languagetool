/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.Calendar;
import java.util.Map;

/**
 * Keep only those matches whose 'year' value is last year or in
 * recent years (up to {@code maxYearsBack} years ago).
 * @since 2.7
 */
public class RecentYearFilter extends RuleFilter {
  
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedTokenReadings[] patternTokens) {
    int thisYear = Calendar.getInstance().get(Calendar.YEAR);
    int maxYear = thisYear - Integer.parseInt(arguments.get("maxYearsBack"));
    int year = Integer.parseInt(arguments.get("year"));
    if (year < thisYear && year >= maxYear) {
      return match;
    }
    return null;
  }

}
