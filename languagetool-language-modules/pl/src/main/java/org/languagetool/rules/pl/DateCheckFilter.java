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
  package org.languagetool.rules.pl;
    import org.languagetool.rules.AbstractDateCheckFilter;
    import java.util.Calendar;
    import java.util.Locale;

/**
 * Polish localization of {@link org.languagetool.rules.AbstractDateCheckFilter}.
 * @since 2.7
 */
public class DateCheckFilter extends AbstractDateCheckFilter {

  @Override
  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.forLanguageTag("pl"));
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.startsWith("pon")) return Calendar.MONDAY;
    if (day.startsWith("wt")) return Calendar.TUESDAY;
    if (day.startsWith("śr")) return Calendar.WEDNESDAY;
    if (day.startsWith("czw")) return Calendar.THURSDAY;
    if (day.equals("pt") || day.startsWith("piątk") || day.equals("piątek")) return Calendar.FRIDAY;
    if (day.startsWith("sob")) return Calendar.SATURDAY;
    if (day.startsWith("niedz")) return Calendar.SUNDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.forLanguageTag("pl"));
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.equals("stycznia") || monthStr.equals("I")) return 1;
    if (mon.equals("lutego") || monthStr.equals("II")) return 2;
    if (mon.equals("marca") || monthStr.equals("III")) return 3;
    if (mon.equals("kwietnia") || monthStr.equals("IV")) return 4;
    if (mon.equals("maja") || monthStr.equals("V")) return 5;
    if (mon.equals("czerwca") || monthStr.equals("VI")) return 6;
    if (mon.equals("lipca") || monthStr.equals("VII")) return 7;
    if (mon.equals("sierpnia") || monthStr.equals("VIII")) return 8;
    if (mon.equals("września") || monthStr.equals("IX")) return 9;
    if (mon.equals("października") || monthStr.equals("X")) return 10;
    if (mon.equals("listopada") || monthStr.equals("XI")) return 11;
    if (mon.equals("grudnia") || monthStr.equals("XII")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}
