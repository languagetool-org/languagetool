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
package org.languagetool.rules.ru;

import org.languagetool.tools.StringTools;

import java.util.Calendar;
import java.util.Locale;

/**
 * @since 5.6
 * @author Yakov Reztsov
 */
class DateFilterHelper {

  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.forLanguageTag("ru"));
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  protected int getDayOfWeek(String dayStr) {
    String day = StringTools.trimSpecialCharacters(dayStr).toLowerCase();  // quickfix for special characters like soft hyphens
    if (day.startsWith("суб")) return Calendar.SATURDAY;
    if (day.startsWith("вс")) return Calendar.SUNDAY;
    if (day.startsWith("вос")) return Calendar.SUNDAY;
    if (day.startsWith("пн")) return Calendar.MONDAY;
    if (day.startsWith("пон")) return Calendar.MONDAY;
    if (day.startsWith("вт")) return Calendar.TUESDAY;
    if (day.startsWith("ср")) return Calendar.WEDNESDAY;
    if (day.startsWith("чт")) return Calendar.THURSDAY;
    if (day.startsWith("чет")) return Calendar.THURSDAY;
    if (day.startsWith("пт")) return Calendar.FRIDAY;
    if (day.startsWith("пят")) return Calendar.FRIDAY;
    if (day.startsWith("сб")) return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  protected String getDayOfWeek(Calendar date) {
    return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.forLanguageTag("ru"));
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  protected int getMonth(String monthStr) {
    String mon = StringTools.trimSpecialCharacters(monthStr).toLowerCase();
    if (mon.startsWith("янв")) return 1;
    if (mon.startsWith("фев")) return 2;
    if (mon.startsWith("мар")) return 3;
    if (mon.startsWith("апр")) return 4;
    if (mon.startsWith("май")) return 5;
    if (mon.startsWith("мая")) return 5; //
    if (mon.startsWith("июн")) return 6;
    if (mon.startsWith("июл")) return 7;
    if (mon.startsWith("авг")) return 8;
    if (mon.startsWith("сен")) return 9;
    if (mon.startsWith("окт")) return 10;
    if (mon.startsWith("ноя")) return 11;
    if (mon.startsWith("дек")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}

