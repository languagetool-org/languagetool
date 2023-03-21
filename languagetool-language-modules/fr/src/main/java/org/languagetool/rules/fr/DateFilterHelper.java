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
package org.languagetool.rules.fr;

import java.util.Calendar;
import java.util.Locale;

/**
 * @since 4.3
 */
class DateFilterHelper {

  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.FRENCH);
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.startsWith("dim")) return Calendar.SUNDAY;
    if (day.startsWith("lun")) return Calendar.MONDAY;
    if (day.startsWith("mar")) return Calendar.TUESDAY;
    if (day.startsWith("mer")) return Calendar.WEDNESDAY;
    if (day.startsWith("jeu")) return Calendar.THURSDAY;
    if (day.startsWith("ven")) return Calendar.FRIDAY;
    if (day.startsWith("sam")) return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  
  protected String getDayOfWeek(Calendar date) {
    return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.FRENCH);
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.startsWith("jan")) return 1;
    if (mon.startsWith("fév")) return 2;
    if (mon.startsWith("fev")) return 2;
    if (mon.startsWith("mar")) return 3;
    if (mon.startsWith("avr")) return 4;
    if (mon.startsWith("mai")) return 5;
    // "juin" and "juillet" are never abbreviated with 3 letters
    // since it would be ambiguous (both start with "jui").
    if (mon.startsWith("juin")) return 6;
    if (mon.startsWith("juil")) return 7;
    if (mon.startsWith("aou") ||
        mon.startsWith("aoû")) return 8;
    if (mon.startsWith("sep")) return 9;
    if (mon.startsWith("oct")) return 10;
    if (mon.startsWith("nov")) return 11;
    if (mon.startsWith("déc")) return 12;
    if (mon.startsWith("dec")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}
