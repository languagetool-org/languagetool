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
package org.languagetool.rules;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.Map;

/**
 * Checks whether a shortened year range (such as '1998-92') is valid, i.e., that the starting
 * date happens before the end date. The check is trivial: simply check
 * whether the first integer number is smaller than the second, so this
 * can be implemented for any language.
 *
 * The parameters used in the XML file are called 'x' and 'y'.
 * @since 3.3
 */
public class ShortenedYearRangeChecker extends RuleFilter {

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, AnalyzedTokenReadings[] patternTokens) {
    try {
      int x = Integer.parseInt(arguments.get("x"));
      String centuryPrefix = arguments.get("x").substring(0, 2);
      int y = Integer.parseInt(centuryPrefix + arguments.get("y"));
      if (x >= y) {
        return match;
      }
    } catch (IllegalArgumentException ignore) {
      // if something's fishy with the number – ignore it silently,
      // it's not a date range
      return null;
    }
    return null;
  }

}
