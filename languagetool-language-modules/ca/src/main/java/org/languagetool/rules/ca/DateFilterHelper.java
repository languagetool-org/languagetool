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
package org.languagetool.rules.ca;

import java.util.Calendar;
import java.util.Locale;

/**
 * @since 5.7
 */
class DateFilterHelper {

  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.FRANCE);
  }
  
  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.equals("dg") || day.equals("diumenge")) return Calendar.SUNDAY;
    if (day.equals("dl") || day.equals("dilluns")) return Calendar.MONDAY;
    if (day.equals("dt") || day.equals("dimarts")) return Calendar.TUESDAY;
    if (day.equals("dc") || day.equals("dimecres")) return Calendar.WEDNESDAY;
    if (day.equals("dj") || day.equals("dijous")) return Calendar.THURSDAY;
    if (day.equals("dv") || day.equals("divendres")) return Calendar.FRIDAY;
    if (day.equals("ds") || day.equals("dissabte")) return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  protected String getDayOfWeek(Calendar date) {
    String englishDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    if (englishDay.equals("Sunday")) return "diumenge";
    if (englishDay.equals("Monday")) return "dilluns";
    if (englishDay.equals("Tuesday")) return "dimarts";
    if (englishDay.equals("Wednesday")) return "dimecres";
    if (englishDay.equals("Thursday")) return "dijous";
    if (englishDay.equals("Friday")) return "divendres";
    if (englishDay.equals("Saturday")) return "dissabte";
    return "";
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.startsWith("gen")) return 1;
    if (mon.startsWith("febr")) return 2;
    if (mon.startsWith("març")) return 3;
    if (mon.startsWith("abr")) return 4;
    if (mon.startsWith("maig")) return 5;
    if (mon.startsWith("juny")) return 6;
    if (mon.startsWith("jul")) return 7;
    if (mon.startsWith("ag")) return 8;
    if (mon.startsWith("set")) return 9;
    if (mon.startsWith("oct")) return 10;
    if (mon.startsWith("nov")) return 11;
    if (mon.startsWith("des")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }

}
