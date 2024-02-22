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

import org.languagetool.rules.AbstractDateCheckFilter;
import org.languagetool.rules.AbstractDateCheckWithSuggestionsFilter;

import java.util.Calendar;

/**
 * German localization of {@link AbstractDateCheckFilter}.
 * @since 2.7
 */
public class DateCheckFilter extends AbstractDateCheckWithSuggestionsFilter {

  private final DateFilterHelper dateFilterHelper = new DateFilterHelper();

  @Override
  protected Calendar getCalendar() {
    return dateFilterHelper.getCalendar();
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
  protected String getErrorMessageWrongYear()  {
    return "Dieses Datum stimmt nicht mit dem Tag überein. Meinten Sie \"{currentYear}\"?";
  }

  @Override
  protected String adjustSuggestion(String sugg) {
    // So. vs Sonntag
    int dotCommaPos = sugg.indexOf(".,");
    if (dotCommaPos > 5 && dotCommaPos < 12) {
      // remove unnecessary dot
      return sugg.replace(".,", ",");
    }
    int commaPos = sugg.indexOf(",");
    if (dotCommaPos < 0 && commaPos > 0 && commaPos < 5) {
      // add dot
      return sugg.replace(",", ".,");
    }
    return sugg;
  }
}
