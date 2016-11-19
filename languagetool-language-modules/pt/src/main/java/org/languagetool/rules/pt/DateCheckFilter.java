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
package org.languagetool.rules.pt;

import org.languagetool.rules.AbstractDateCheckFilter;

import java.util.Calendar;
import java.util.Locale;

/**
 * Portuguese localization of {@link AbstractDateCheckFilter}.
 * @since 3.6
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
    if (day.equals("dom") || day.equals("domingo")) return Calendar.SUNDAY;
    if (day.equals("seg") || day.equals("segunda-feira")) return Calendar.MONDAY;
    if (day.equals("ter") || day.equals("terça-feira")) return Calendar.TUESDAY;
    if (day.equals("qua") || day.equals("quarta-feira")) return Calendar.WEDNESDAY;
    if (day.equals("qui") || day.equals("quinta-feira")) return Calendar.THURSDAY;
    if (day.equals("sex") || day.equals("sexta-feira")) return Calendar.FRIDAY;
    if (day.equals("sáb") || day.equals("sábado")) return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected String getDayOfWeek(Calendar date) {
    String englishDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    if (englishDay.equals("Sunday")) return "domingo";
    if (englishDay.equals("Monday")) return "segunda-feira";
    if (englishDay.equals("Tuesday")) return "terça-feira";
    if (englishDay.equals("Wednesday")) return "quarta-feira";
    if (englishDay.equals("Thursday")) return "quinta-feira";
    if (englishDay.equals("Friday")) return "sexta-feira";
    if (englishDay.equals("Saturday")) return "sábado";
    return "";
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.startsWith("jan")) return 1;
    if (mon.startsWith("fev")) return 2;
    if (mon.startsWith("mar")) return 3;
    if (mon.startsWith("abr")) return 4;
    if (mon.startsWith("mai")) return 5;
    if (mon.startsWith("jun")) return 6;
    if (mon.startsWith("jul")) return 7;
    if (mon.startsWith("ago")) return 8;
    if (mon.startsWith("set")) return 9;
    if (mon.startsWith("out")) return 10;
    if (mon.startsWith("nov")) return 11;
    if (mon.startsWith("dez")) return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }
}
