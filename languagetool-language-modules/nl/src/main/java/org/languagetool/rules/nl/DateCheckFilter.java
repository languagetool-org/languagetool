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
package org.languagetool.rules.nl;

import org.languagetool.rules.AbstractDateCheckFilter;

import java.util.Calendar;
import java.util.Locale;

/**
 * Dutch localization of {@link AbstractDateCheckFilter}.
 * @since 2.8
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
    if (day.equals("zo") || day.equals("zondag")) return Calendar.SUNDAY;
    if (day.equals("ma") || day.equals("maandag")) return Calendar.MONDAY;
    if (day.equals("di") || day.equals("dinsdag")) return Calendar.TUESDAY;
    if (day.equals("wo") || day.equals("woensdag")) return Calendar.WEDNESDAY;
    if (day.equals("do") || day.equals("donderdag")) return Calendar.THURSDAY;
    if (day.equals("vr") || day.equals("vrijdag")) return Calendar.FRIDAY;
    if (day.equals("za") || day.equals("zaterdag")) return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected String getDayOfWeek(Calendar date) {
    String englishDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    if (englishDay.equals("Sunday")) return "zondag";
    if (englishDay.equals("Monday")) return "maandag";
    if (englishDay.equals("Tuesday")) return "dinsdag";
    if (englishDay.equals("Wednesday")) return "woensdag";
    if (englishDay.equals("Thursday")) return "donderdag";
    if (englishDay.equals("Friday")) return "vrijdag";
    if (englishDay.equals("Saturday")) return "zaterdag";
    return "";
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.startsWith("jan")) return 1;
    if (mon.startsWith("feb")) return 2;
    if (mon.startsWith("maa")) return 3;
    if (mon.startsWith("mrt")) return 3;
    if (mon.startsWith("mar")) return 3;
    if (mon.startsWith("apr")) return 4;
    if (mon.startsWith("mei")) return 5;
    if (mon.startsWith("jun")) return 6;
    if (mon.startsWith("jul")) return 7;
    if (mon.startsWith("aug")) return 8;
    if (mon.startsWith("sep")) return 9;
    if (mon.startsWith("okt") || mon.startsWith("oct")) return 10;
    if (mon.startsWith("nov")) return 11;
    if (mon.startsWith("dec")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}
