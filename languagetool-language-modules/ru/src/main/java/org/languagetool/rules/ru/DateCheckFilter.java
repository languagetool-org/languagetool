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
    if (day.startsWith("пн") || day.equals("понедельник")) return Calendar.MONDAY;
    if (day.startsWith("вт")) return Calendar.TUESDAY;
    if (day.startsWith("ср")) return Calendar.WEDNESDAY;
    if (day.startsWith("чт") || day.equals("четверг")) return Calendar.THURSDAY;
    if (day.equals("пт") || day.equals("пятница")) return Calendar.FRIDAY;
    if (day.startsWith("сб") || day.equals("суббота")) return Calendar.SATURDAY;
    if (day.startsWith("вс") || day.equals("воскресенье")) return Calendar.SUNDAY;
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
    if (mon.equals("январь") || monthStr.equals("I") || monthStr.equals("января")) return 1;
    if (mon.equals("февраль") || monthStr.equals("II") ||  monthStr.equals("февраля")) return 2;
    if (mon.equals("март") || monthStr.equals("III") || monthStr.equals("марта")) return 3;
    if (mon.equals("апрель") || monthStr.equals("IV") || monthStr.equals("апреля")) return 4;
    if (mon.equals("май") || monthStr.equals("V") || monthStr.equals("мая")) return 5;
    if (mon.equals("июнь") || monthStr.equals("VI") || monthStr.equals("июня")) return 6;
    if (mon.equals("июль") || monthStr.equals("VII") || monthStr.equals("июля")) return 7;
    if (mon.equals("август") || monthStr.equals("VIII") || monthStr.equals("августа")) return 8;
    if (mon.equals("сентябрь") || monthStr.equals("IX") || monthStr.equals("сентября")) return 9;
    if (mon.equals("октябрь") || monthStr.equals("X") || monthStr.equals("октября")) return 10;
    if (mon.equals("ноябрь") || monthStr.equals("XI") || monthStr.equals("ноября")) return 11;
    if (mon.equals("декабрь") || monthStr.equals("XII") || monthStr.equals("декабря")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}
