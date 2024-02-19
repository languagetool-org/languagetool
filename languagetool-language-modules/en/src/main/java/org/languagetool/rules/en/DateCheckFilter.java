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
package org.languagetool.rules.en;

import org.languagetool.rules.AbstractDateCheckFilter;
import org.languagetool.rules.AbstractDateCheckWithSuggestionsFilter;
import org.languagetool.tools.StringTools;

import java.util.Calendar;

/**
 * English localization of {@link AbstractDateCheckFilter}.
 * @since 2.7
 */
public class DateCheckFilter extends AbstractDateCheckWithSuggestionsFilter {

  private final DateFilterHelper dateFilterHelper = new DateFilterHelper();

  @Override
  protected Calendar getCalendar() {
    return dateFilterHelper.getCalendar();
  }

  @Override
  protected String getErrorMessageWrongYear() {
    return "This date is wrong. Did you mean \"{currentYear}\"?";
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected int getDayOfWeek(String dayStr) {
    return dateFilterHelper.getDayOfWeek(dayStr);
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    return dateFilterHelper.getDayOfWeek(date);
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    return dateFilterHelper.getMonth(monthStr);
  }

  @Override
  protected String getDayStrLikeOriginal(String day, String original) {
    if (StringTools.isNumeric(original)) {
      return day;
    }
    int number = Integer.parseInt(day);
    if (number >= 11 && number <= 13) {
      return number + "th";
    }
    switch (number % 10) {
      case 1:
        return number + "st";
      case 2:
        return number + "nd";
      case 3:
        return number + "rd";
      default:
        return number + "th";
    }
  }
}
