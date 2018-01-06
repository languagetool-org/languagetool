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
package org.languagetool.rules.sr;

import org.languagetool.rules.AbstractDateCheckFilter;

import java.util.Calendar;
import java.util.Locale;

/**
 * Serbian localization of {@link org.languagetool.rules.AbstractDateCheckFilter}.
 *
 * @since 4.0
 */
public class DateCheckFilter extends AbstractDateCheckFilter {

  @Override
  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.forLanguageTag("sr"));
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.startsWith("по") || day.equals("понедељак")) return Calendar.MONDAY;
    if (day.startsWith("ут")) return Calendar.TUESDAY;
    if (day.startsWith("ср")) return Calendar.WEDNESDAY;
    if (day.startsWith("че") || day.equals("четвртак")) return Calendar.THURSDAY;
    if (day.startsWith("пе") || day.equals("петак")) return Calendar.FRIDAY;
    if (day.startsWith("су") || day.equals("субота")) return Calendar.SATURDAY;
    if (day.startsWith("не") || day.equals("недеља")) return Calendar.SUNDAY;
    throw new RuntimeException("Редни број дана у недељи за '" + dayStr + "' не постоји.");
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    return date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.forLanguageTag("sr"));
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.equals("јануар") || monthStr.equals("I") || mon.equals("јануара") || mon.equals("јан")) return 1;
    if (mon.equals("фебруар") || monthStr.equals("II") || mon.equals("фебруара") || mon.equals("феб")) return 2;
    if (mon.equals("март") || monthStr.equals("III") || mon.equals("марта") || mon.equals("мар")) return 3;
    if (mon.equals("април") || monthStr.equals("IV") || mon.equals("априла") || mon.equals("апр")) return 4;
    if (mon.equals("мај") || monthStr.equals("V") || mon.equals("маја")) return 5;
    if (mon.equals("јун") || monthStr.equals("VI") || mon.equals("јуна") || mon.equals("јун")) return 6;
    if (mon.equals("јул") || monthStr.equals("VII") || mon.equals("јула") || mon.equals("јул")) return 7;
    if (mon.equals("август") || monthStr.equals("VIII") || mon.equals("августа") || mon.equals("авг")) return 8;
    if (mon.equals("септембар") || monthStr.equals("IX") || mon.equals("септембра") || mon.equals("сеп")) return 9;
    if (mon.equals("октобар") || monthStr.equals("X") || mon.equals("октобра") || mon.equals("окт")) return 10;
    if (mon.equals("новембар") || monthStr.equals("XI") || mon.equals("новембра") || mon.equals("нов")) return 11;
    if (mon.equals("децембар") || monthStr.equals("XII") || mon.equals("децембра") || mon.equals("дец")) return 12;
    throw new RuntimeException("Месец '" + monthStr + "' не постоји.");
  }
}
