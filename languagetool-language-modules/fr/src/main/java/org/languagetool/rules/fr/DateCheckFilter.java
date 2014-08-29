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
package org.languagetool.rules.fr;

import org.languagetool.rules.AbstractDateCheckFilter;

import java.util.Calendar;
import java.util.Locale;

/**
 * French localization of {@link AbstractDateCheckFilter}.
 * @since 2.7
 */
public class DateCheckFilter extends AbstractDateCheckFilter {

  @Override
  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.FRENCH);
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.equals("dimanche")) return Calendar.SUNDAY;
    if (day.equals("lundi"))    return Calendar.MONDAY;
    if (day.equals("mardi"))    return Calendar.TUESDAY;
    if (day.equals("mercredi")) return Calendar.WEDNESDAY;
    if (day.equals("jeudi"))    return Calendar.THURSDAY;
    if (day.equals("vendredi")) return Calendar.FRIDAY;
    if (day.equals("samedi"))   return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.FRENCH);
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.equals("janvier")   || mon.equals("1") || mon.equals("01")) return 1;
    if (mon.equals("février")   || mon.equals("2") || mon.equals("02")) return 2;
    if (mon.equals("mars")      || mon.equals("3") || mon.equals("03")) return 3;
    if (mon.equals("avril")     || mon.equals("4") || mon.equals("04")) return 4;
    if (mon.equals("mai")       || mon.equals("5") || mon.equals("05")) return 5;
    if (mon.equals("juin")      || mon.equals("6") || mon.equals("06")) return 6;
    if (mon.equals("juillet")   || mon.equals("7") || mon.equals("07")) return 7;
    if (mon.equals("aout")      ||
        mon.equals("août")      || mon.equals("8") || mon.equals("08")) return 8;
    if (mon.equals("septembre") || mon.equals("9") || mon.equals("09")) return 9;
    if (mon.equals("octobre")   || mon.equals("10")) return 10;
    if (mon.equals("novembre")  || mon.equals("11")) return 11;
    if (mon.equals("décembre")  || mon.equals("12")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }

}
