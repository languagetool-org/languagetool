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

import org.languagetool.tools.StringTools;

import java.util.Calendar;
import java.util.Locale;

/**
 * @since 4.3
 */
class DateFilterHelper {

  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.GERMANY);
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  protected int getDayOfWeek(String dayStr) {
    // quickfix for special characters like soft hyphens
    String day = StringTools.trimSpecialCharacters(dayStr).toLowerCase();
    if (day.startsWith("sonnabend")) return Calendar.SATURDAY;
    if (day.startsWith("so")) return Calendar.SUNDAY;
    if (day.startsWith("mo")) return Calendar.MONDAY;
    if (day.startsWith("di")) return Calendar.TUESDAY;
    if (day.startsWith("mi")) return Calendar.WEDNESDAY;
    if (day.startsWith("do")) return Calendar.THURSDAY;
    if (day.startsWith("fr")) return Calendar.FRIDAY;
    if (day.startsWith("sa")) return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  protected String getDayOfWeek(Calendar date) {
    return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.GERMAN);
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  protected int getMonth(String monthStr) {
    String mon = StringTools.trimSpecialCharacters(monthStr).toLowerCase();
    if (mon.startsWith("jän")) return 1;
    if (mon.startsWith("jan")) return 1;
    if (mon.startsWith("feb")) return 2;
    if (mon.startsWith("mär")) return 3;
    if (mon.startsWith("apr")) return 4;
    if (mon.startsWith("mai")) return 5;
    if (mon.startsWith("jun")) return 6;
    if (mon.startsWith("jul")) return 7;
    if (mon.startsWith("aug")) return 8;
    if (mon.startsWith("sep")) return 9;
    if (mon.startsWith("okt")) return 10;
    if (mon.startsWith("nov")) return 11;
    if (mon.startsWith("dez")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}
