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
package org.languagetool.rules.de;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.YMDDateHelper;

import java.util.Map;

/**
 * New year date filter that expects a 'date' argument in the format 'yyyy-mm-dd'.
 *
 * @since 4.3
 */
public class YMDNewYearDateFilter extends NewYearDateFilter {

  private final YMDDateHelper ymdDateHelper = new YMDDateHelper();

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, AnalyzedTokenReadings[] patternTokens) {
    if (args.containsKey("year") || args.containsKey("month") || args.containsKey("day")) {
      throw new RuntimeException("Set only 'weekDay' and 'date' for " + YMDDateCheckFilter.class.getSimpleName());
    }
    args = ymdDateHelper.parseDate(args);
    return super.acceptRuleMatch(ymdDateHelper.correctDate(match, args), args, patternTokens);
  }

}
