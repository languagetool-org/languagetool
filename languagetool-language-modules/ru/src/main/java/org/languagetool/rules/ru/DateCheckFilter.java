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
  package org.languagetool.rules.ru;
    import org.languagetool.rules.AbstractDateCheckFilter;
    import java.util.Calendar;
    import java.util.Locale;

/**
 * Russian localization of {@link org.languagetool.rules.AbstractDateCheckFilter}.
 * @since 2.7
 */
public class DateCheckFilter extends AbstractDateCheckFilter {

  @Override
  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.forLanguageTag("ru"));
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.startsWith("пн")) return Calendar.MONDAY;
    if (day.startsWith("вт")) return Calendar.TUESDAY;
    if (day.startsWith("ср")) return Calendar.WEDNESDAY;
    if (day.startsWith("чт")) return Calendar.THURSDAY;
    if (day.equals("пт") || day.equals("пятница")) return Calendar.FRIDAY;
    if (day.startsWith("сб")) return Calendar.SATURDAY;
    if (day.startsWith("вск")) return Calendar.SUNDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.forLanguageTag("ru"));
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.equals("январь") || monthStr.equals("I") || monthStr.equals("1")) return 1;
    if (mon.equals("февраль") || monthStr.equals("II") ||  monthStr.equals("2")) return 2;
    if (mon.equals("март") || monthStr.equals("III") || monthStr.equals("3")) return 3;
    if (mon.equals("апрель") || monthStr.equals("IV") || monthStr.equals("4")) return 4;
    if (mon.equals("май") || monthStr.equals("V") || monthStr.equals("5")) return 5;
    if (mon.equals("июнь") || monthStr.equals("VI") || monthStr.equals("6")) return 6;
    if (mon.equals("июль") || monthStr.equals("VII") || monthStr.equals("7")) return 7;
    if (mon.equals("август") || monthStr.equals("VIII") || monthStr.equals("8")) return 8;
    if (mon.equals("сентябрь") || monthStr.equals("IX") || monthStr.equals("9")) return 9;
    if (mon.equals("октябрь") || monthStr.equals("X") || monthStr.equals("10")) return 10;
    if (mon.equals("ноябрь") || monthStr.equals("XI") || monthStr.equals("11")) return 11;
    if (mon.equals("декабрь") || monthStr.equals("XII") || monthStr.equals("12")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}
