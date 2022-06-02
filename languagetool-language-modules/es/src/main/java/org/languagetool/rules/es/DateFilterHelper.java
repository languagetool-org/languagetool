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
package org.languagetool.rules.es;

import java.util.Calendar;
import java.util.Locale;

/**
 * @since 4.3
 */
class DateFilterHelper {

  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.FRANCE);
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.equals("do") || day.equals("domingo")) return Calendar.SUNDAY;
    if (day.equals("lu") || day.equals("lunes")) return Calendar.MONDAY;
    if (day.equals("ma") || day.equals("martes")) return Calendar.TUESDAY;
    if (day.equals("mi") || day.equals("miércoles")) return Calendar.WEDNESDAY;
    if (day.equals("ju") || day.equals("jueves")) return Calendar.THURSDAY;
    if (day.equals("vi") || day.equals("viernes")) return Calendar.FRIDAY;
    if (day.equals("sa") || day.equals("sábado")) return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  protected String getDayOfWeek(Calendar date) {
    String englishDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    if (englishDay.equals("Sunday")) return "domingo";
    if (englishDay.equals("Monday")) return "lunes";
    if (englishDay.equals("Tuesday")) return "martes";
    if (englishDay.equals("Wednesday")) return "miércoles";
    if (englishDay.equals("Thursday")) return "jueves";
    if (englishDay.equals("Friday")) return "viernes";
    if (englishDay.equals("Saturday")) return "sábado";
    return "";
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.startsWith("en")) return 1;
    if (mon.startsWith("fe")) return 2;
    if (mon.startsWith("mar") || mon.startsWith("mzo")) return 3;
    if (mon.startsWith("ab")) return 4;
    if (mon.startsWith("may") || mon.startsWith("my")) return 5;
    if (mon.startsWith("jun") || mon.equals("jn")) return 6;
    if (mon.startsWith("jul") || mon.equals("jl")) return 7;
    if (mon.startsWith("ag")) return 8;
    if (mon.startsWith("se") || mon.startsWith("sep")) return 9;
    if (mon.startsWith("oc")) return 10;
    if (mon.startsWith("no")) return 11;
    if (mon.startsWith("di")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}
