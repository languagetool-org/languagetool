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
package org.languagetool.rules.br;

import org.languagetool.rules.AbstractDateCheckFilter;

import java.util.Calendar;
import java.util.Locale;

/**
 * Breton localization of {@link AbstractDateCheckFilter}.
 * @since 2.7
 */
public class DateCheckFilter extends AbstractDateCheckFilter {

  @Override
  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.UK);
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.equals("sul"))      return Calendar.SUNDAY;
    if (day.equals("lun"))      return Calendar.MONDAY;
    if (day.equals("meurzh"))   return Calendar.TUESDAY;
    if (day.equals("merc'her")) return Calendar.WEDNESDAY;
    if (day.equals("yaou"))     return Calendar.THURSDAY;
    if (day.equals("gwengolo")) return Calendar.FRIDAY;
    if (day.equals("sadorn"))   return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    String englishDay=date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    if (englishDay.equals("Sunday"))    return "Sul";
    if (englishDay.equals("Monday"))    return "Lun";
    if (englishDay.equals("Tuesday"))   return "Meurzh";
    if (englishDay.equals("Wednesday")) return "Merc’her";
    if (englishDay.equals("Thursday"))  return "Yaou";
    if (englishDay.equals("Friday"))    return "Gwener";
    if (englishDay.equals("Saturday"))  return "Sadorn";
    return "";
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.equals("genver"))    return 1;
    if (mon.equals("c'hwevrer")) return 2;
    if (mon.equals("meurzh"))    return 3;
    if (mon.equals("ebrel"))     return 4;
    if (mon.equals("mae"))       return 5;
    if (mon.equals("mezheven"))  return 6;
    if (mon.equals("gouere"))    return 7;
    if (mon.equals("eost"))      return 8;
    if (mon.equals("gwengolo"))  return 9;
    if (mon.equals("here"))      return 10;
    if (mon.equals("du"))        return 11;
    if (mon.equals("kerzu"))     return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}
