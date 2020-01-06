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
package org.languagetool.rules.ga;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractDateCheckFilter;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.ga.Utils;
import org.languagetool.tools.StringTools;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Irish localisation of {@link AbstractDateCheckFilter}.
 * Copied from Breton.
 */
public class DateCheckFilter extends AbstractDateCheckFilter {

  // The day of the month may contain not only digits but also extra letters
  // such as"22nd" in English or "22-an" in Esperanto. The regexp extracts
  // the numerical part.
  private static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d+).*");

  @Override
  protected Calendar getCalendar() {
    return Calendar.getInstance(Locale.UK);
  }

  @Override
  protected int getDayOfMonth(String dayStr) {
    switch(Utils.toLowerCaseIrish(dayStr)) {
      case "chéad":
      case "céad":
      case "aonú":
      case "t-aonú":
        return 1;
      case "dara":
      case "dóú":
        return 2;
      case "tríú":
        return 3;
      case "ceathrú":
        return 4;
      case "cúigiú":
        return 5;
      case "séú":
        return 6;
      case "seachtú":
        return 7;
      case "ochtú":
      case "t-ochtú":
        return 8;
      case "naoú":
        return 9;
      case "deichiú":
        return 10;
      case "fichiú":
        return 20;
      case "tríochadú":
        return 30;
      default:
        return 0;
    }
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected int getDayOfWeek(String dayStr) {
    String day = dayStr.toLowerCase();
    if (day.startsWith("domh"))  return Calendar.SUNDAY;
    if (day.startsWith("lua"))   return Calendar.MONDAY;
    if (day.startsWith("má"))    return Calendar.TUESDAY;
    if (day.startsWith("mhá"))   return Calendar.TUESDAY;
    if (day.startsWith("cé"))    return Calendar.WEDNESDAY;
    if (day.startsWith("ché"))   return Calendar.WEDNESDAY;
    if (day.startsWith("déar"))  return Calendar.THURSDAY;
    if (day.startsWith("aoin"))  return Calendar.FRIDAY;
    if (day.startsWith("haoin"))  return Calendar.FRIDAY;
    if (day.startsWith("sath"))  return Calendar.SATURDAY;
    throw new RuntimeException("Could not find day of week for '" + dayStr + "'");
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected String getDayOfWeek(Calendar date) {
    String englishDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    if (englishDay.equals("Sunday"))    return "Domhnach";
    if (englishDay.equals("Monday"))    return "Luan";
    if (englishDay.equals("Tuesday"))   return "Máirt";
    if (englishDay.equals("Wednesday")) return "Céadaoin";
    if (englishDay.equals("Thursday"))  return "Déardaoin";
    if (englishDay.equals("Friday"))    return "Aoine";
    if (englishDay.equals("Saturday"))  return "Satharn";
    return "";
  }

  protected String getDayOfWeek(Calendar date, String prefix) {
    String englishDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.UK);
    if(prefix.toLowerCase().equals("an")) {
      if (englishDay.equals("Sunday"))    return "an Domhnach";
      if (englishDay.equals("Monday"))    return "an Luan";
      if (englishDay.equals("Tuesday"))   return "an Mháirt";
      if (englishDay.equals("Wednesday")) return "an Chéadaoin";
      if (englishDay.equals("Thursday"))  return "an Déardaoin";
      if (englishDay.equals("Friday"))    return "an Aoine";
      if (englishDay.equals("Saturday"))  return "an Satharn";
      return "";
    } else if(prefix.toLowerCase().equals("dé") || prefix.toLowerCase().equals("de")) {
      if (englishDay.equals("Sunday"))    return "Dé Domhnaigh";
      if (englishDay.equals("Monday"))    return "Dé Luain";
      if (englishDay.equals("Tuesday"))   return "Dé Máirt";
      if (englishDay.equals("Wednesday")) return "Dé Céadaoin";
      if (englishDay.equals("Thursday"))  return "Déardaoin";
      if (englishDay.equals("Friday"))    return "Dé hAoine";
      if (englishDay.equals("Saturday"))  return "Dé Sathairn";
      return "";
    } else {
      if (englishDay.equals("Sunday"))    return "Domhnach";
      if (englishDay.equals("Monday"))    return "Luan";
      if (englishDay.equals("Tuesday"))   return "Máirt";
      if (englishDay.equals("Wednesday")) return "Céadaoin";
      if (englishDay.equals("Thursday"))  return "Déardaoin";
      if (englishDay.equals("Friday"))    return "Aoine";
      if (englishDay.equals("Saturday"))  return "Satharn";
      return "";
    }
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    String mon = monthStr.toLowerCase();
    if (mon.equals("eanáir"))       return 1;
    if (mon.equals("feabhra"))      return 2;
    if (mon.equals("márta"))        return 3;
    if (mon.equals("aibreán"))      return 4;
    if (mon.equals("bealtaine"))    return 5;
    if (mon.equals("meitheamh"))    return 6;
    if (mon.equals("mheitheamh"))   return 6;
    if (mon.equals("iúil"))         return 7;
    if (mon.equals("lúnasa"))       return 8;
    if (mon.equals("meán"))         return 9;
    if (mon.equals("mf"))           return 9;
    if (mon.equals("deireadh"))     return 10;
    if (mon.equals("df"))           return 10;
    if (mon.equals("samhain"))      return 11;
    if (mon.equals("samhna"))       return 11;
    if (mon.equals("nollaig"))      return 12;
    if (mon.equals("nollag"))       return 12;
    throw new RuntimeException("Could not find month '" + monthStr + "'");
  }

  /**
   * @param args a map with values for {@code year}, {@code month}, {@code day} (day of month), {@code weekDay}
   */
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    int dayOfWeekFromString = getDayOfWeek(getRequired("weekDay", args).replace("\u00AD", ""));  // replace soft hyphen
    String tensNumber = args.get("tens");
    if (tensNumber.toLowerCase().startsWith("d")) {
      dayOfWeekFromString += 10;
    } else if (tensNumber.toLowerCase().startsWith("f")) {
      dayOfWeekFromString += 20;
    }
    Calendar dateFromDate = getDate(args);
    String dayPrefix = args.get("dayPrefix");
    int dayOfWeekFromDate;
    try {
      dayOfWeekFromDate = dateFromDate.get(Calendar.DAY_OF_WEEK);
    } catch (IllegalArgumentException ignore) {
      // happens with 'dates' like '32.8.2014' - those should be caught by a different rule
      return null;
    }
    if (dayOfWeekFromString != dayOfWeekFromDate) {
      Calendar calFromDateString = Calendar.getInstance();
      calFromDateString.set(Calendar.DAY_OF_WEEK, dayOfWeekFromString);
      String dayOfWeek = (dayPrefix != null) ? getDayOfWeek(dateFromDate, dayPrefix) : getDayOfWeek(dateFromDate);
      String message = match.getMessage()
        .replace("{realDay}", dayOfWeek)
        .replace("{day}", getDayOfWeek(calFromDateString))
        .replace("{currentYear}", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
      RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), message, match.getShortMessage());
      ruleMatch.setType(match.getType());
      return ruleMatch;
    } else {
      return null;
    }
  }

  private Calendar getDate(Map<String, String> args) {
    String yearArg = args.get("year");
    int year;
    if (yearArg == null) {
      // assume current year for rule DATUM_WOCHENTAG_OHNE_JAHR etc.
      year = getCalendar().get(Calendar.YEAR);
    } else {
      year = Integer.parseInt(yearArg);
    }
    int month = getMonthFromArguments(args);
    int dayOfMonth = getDayOfMonthFromArguments(args);

    Calendar calendar = getCalendar();
    calendar.setLenient(false);  // be strict about validity of dates
    //noinspection MagicConstant
    calendar.set(year, month, dayOfMonth, 0, 0, 0);
    return calendar;
  }

  private int getDayOfMonthFromArguments(Map<String, String> args) {
    String dayOfMonthString = getRequired("day", args);
    int dayOfMonth;
    Matcher matcherDayOfMonth = DAY_OF_MONTH_PATTERN.matcher(dayOfMonthString);
    if (matcherDayOfMonth.matches()) {
      // The day of the month is a number, possibly with a suffix such
      // as "22nd" for example.
      dayOfMonth = Integer.parseInt(matcherDayOfMonth.group(1));
    } else {
      // In some languages, the day of the month can also be written with
      // letters rather than with digits, so parse localized numbers.
      dayOfMonth = getDayOfMonth(dayOfMonthString);
    }
    return dayOfMonth;
  }

  private int getMonthFromArguments(Map<String, String> args) {
    String monthStr = getRequired("month", args);
    int month;
    if (StringUtils.isNumeric(monthStr)) {
      month = Integer.parseInt(monthStr);
    } else {
      month = getMonth(StringTools.trimSpecialCharacters(monthStr));
    }
    return month - 1;
  }
}
