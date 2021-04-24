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
package org.languagetool.rules.uk;

import org.languagetool.rules.AbstractDateCheckFilter;

import java.util.Calendar;
import java.util.Locale;

/**
 * Ukrainian localization of {@link AbstractDateCheckFilter}.
 * @since 2.7
 */
public class DateCheckFilter extends AbstractDateCheckFilter {

  @Override
  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.forLanguageTag("uk"));
  }

  @Override
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.startsWith("по") || day.equals("пн")) return Calendar.MONDAY;
    if (day.startsWith("ві") || day.equals("вт")) return Calendar.TUESDAY;
    if (day.startsWith("се") || day.equals("ср")) return Calendar.WEDNESDAY;
    if (day.startsWith("че") || day.equals("чт")) return Calendar.THURSDAY;
    if (day.startsWith("п'") || day.startsWith("п’") || day.equals("пт")) return Calendar.FRIDAY;
    if (day.startsWith("су") || day.equals("сб")) return Calendar.SATURDAY;
    if (day.startsWith("не") || day.equals("нд")) return Calendar.SUNDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.forLanguageTag("uk"));
  }

  @Override
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.startsWith("сі")) return Calendar.JANUARY + 1;
    if (mon.startsWith("лю")) return Calendar.FEBRUARY + 1;
    if (mon.startsWith("бе")) return Calendar.MARCH + 1;
    if (mon.startsWith("кв")) return Calendar.APRIL + 1;
    if (mon.startsWith("тр")) return Calendar.MAY + 1;
    if (mon.startsWith("че")) return Calendar.JUNE + 1;
    if (mon.startsWith("ли")) return Calendar.JULY + 1;
    if (mon.startsWith("се")) return Calendar.AUGUST + 1;
    if (mon.startsWith("ве")) return Calendar.SEPTEMBER + 1;
    if (mon.startsWith("жо")) return Calendar.OCTOBER + 1;
    if (mon.startsWith("ли")) return Calendar.NOVEMBER + 1;
    if (mon.startsWith("гр")) return Calendar.DECEMBER + 1;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }

}
