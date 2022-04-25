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
package org.languagetool.rules;

import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accepts rule matches if a date doesn't match the accompanying weekday, e.g. if {@code Monday, 8 November 2003}
 * isn't actually a Monday. Replaces {@code {realDay}} with the real day of the date in the rule's message,
 * and {@code {day}} with the claimed day from the text (might be useful in case the text uses an abbreviation).
 * @since 2.7
 */
public abstract class AbstractDateCheckFilter extends RuleFilter {

  private static final Logger logger = LoggerFactory.getLogger(AbstractDateCheckFilter.class);

  // The day of the month may contain not only digits but also extra letters
  // such as"22nd" in English or "22-an" in Esperanto. The regexp extracts
  // the numerical part.
  private static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d+).*");

  /**
   * Implement so that Sunday returns {@code 1}, Monday {@code 2} etc.
   * @param localizedWeekDayString a week day name or abbreviation thereof
   */
  protected abstract int getDayOfWeek(String localizedWeekDayString);

  /**
   * Get the localized name of the day of week for the given date.
   */
  protected abstract String getDayOfWeek(Calendar date);

  /**
   * Implement so that "first" returns {@code 1}, second returns {@code 2} etc.
   * @param localizedDayOfMonth name of day of the month or abbreviation thereof
   */
  protected int getDayOfMonth(String localizedDayOfMonth) {
    return 0;
  }

  /**
   * Implement so that January returns {@code 1}, February {@code 2} etc.
   * @param localizedMonth name of a month or abbreviation thereof
   */
  protected abstract int getMonth(String localizedMonth);

  protected abstract Calendar getCalendar();

  /**
   * @param args a map with values for {@code year}, {@code month}, {@code day} (day of month), {@code weekDay}
   */
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    try {
      int dayOfWeekFromString = getDayOfWeek(getRequired("weekDay", args).replace("\u00AD", ""));  // replace soft hyphen
      Calendar dateFromDate = getDate(args);
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
        String message = match.getMessage()
          .replace("{realDay}", getDayOfWeek(dateFromDate))
          .replace("{day}", getDayOfWeek(calFromDateString))
          .replace("{currentYear}", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), message, match.getShortMessage());
        ruleMatch.setType(match.getType());
        ruleMatch.setUrl(Tools.getUrl("https://www.timeanddate.com/calendar/?year=" + dateFromDate.get(Calendar.YEAR)));
        return ruleMatch;
      } else {
        return null;
      }
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (RuntimeException e) {
      // this can happen with some special characters which the Java regex matches but which the Java code
      // cannot map to days, e.g. German "Dıenstag" vs "Dienstag" (note the difference on the second character -
      // the first word is not valid, but it should not crash LT):
      logger.warn("Skipping potential match for " + match.getRule().getFullId(), e);
      return null;
    }
  }

  private Calendar getDate(Map<String, String> args) {
    String yearArg = args.get("year");
    int year;
    if (yearArg == null && TestHackHelper.isJUnitTest()) {
      // Volkswagen-style testing
      // Hack for tests of date - weekday match with missing year
      // in production, we assume the current year
      // For xml tests, we use weekdays of the year 2014
      year = 2014;
    } else if (yearArg == null) {
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
