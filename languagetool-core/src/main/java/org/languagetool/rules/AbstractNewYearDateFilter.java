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

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Accepts rule matches if we are in the first days of a new year and the user
 * may have entered a date with the old year (but not a date in December).
 * @since 4.3
 */
public abstract class AbstractNewYearDateFilter extends RuleFilter {
  // The day of the month may contain not only digits but also extra letters
  // such as "22nd" in English or "22-an" in Esperanto. The regexp extracts
  // the numerical part.
  private static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d+).*");

  /**
   * Return true if the year recently changed (= it is January)
  */
  protected boolean isJanuary() {
    if (TestHackHelper.isJUnitTest()) {
      return true;
    }
    return getCalendar().get(Calendar.MONTH) == Calendar.JANUARY;
  }

  protected int getCurrentYear() {
    if (TestHackHelper.isJUnitTest()) {
      return 2014;
    }
    return getCalendar().get(Calendar.YEAR);
  }

  /**
   * Implement so that January returns {@code 1}, February {@code 2} etc.
   * @param localizedMonth name of a month or abbreviation thereof
   */
  protected abstract int getMonth(String localizedMonth);

  protected abstract Calendar getCalendar();

  /**
   * Implement so that "first" returns {@code 1}, second returns {@code 2} etc.
   * @param localizedDayOfMonth name of day of the month or abbreviation thereof
   */
  protected int getDayOfMonth(String localizedDayOfMonth) {
    return 0;
  }

  /**
   * @param args a map with values for {@code year}, {@code month}, {@code day} (day of month)
   */
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    Calendar dateFromText = getDate(args);
    int monthFromText;
    int yearFromText;
    try {
      monthFromText = dateFromText.get(Calendar.MONTH);
      yearFromText = dateFromText.get(Calendar.YEAR);
    } catch (IllegalArgumentException e) {
      return null; // date is not valid; another rule is responsible
    }
    int currentYear = getCurrentYear();
    if (isJanuary() && monthFromText != 11 /*December*/ && yearFromText + 1 == currentYear) {
      String message = match.getMessage()
              .replace("{year}", Integer.toString(yearFromText))
              .replace("{realYear}", Integer.toString(currentYear));
      RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), message, match.getShortMessage());
      ruleMatch.setType(match.getType());
      return ruleMatch;
    } else {
      return null;
    }
  }

  private Calendar getDate(Map<String, String> args) {
    int year = Integer.parseInt(getRequired("year", args));
    int month = getMonthFromArguments(args);
    int dayOfMonth = getDayOfMonthFromArguments(args);

    Calendar calendar = getCalendar();
    calendar.setLenient(false);  // be strict about validity of dates
    //noinspection MagicConstant
    calendar.set(year, month, dayOfMonth, 0, 0, 0);
    return calendar;
  }

  private int getDayOfMonthFromArguments(Map<String, String> args) {
    String dayOfMonthString = getRequired("day", args).replace("\u00AD", "");  // replace soft hyphen
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
    String monthStr = getRequired("month", args).replace("\u00AD", "");  // replace soft hyphen
    int month;
    if (StringUtils.isNumeric(monthStr)) {
      month = Integer.parseInt(monthStr);
    } else {
      month = getMonth(monthStr);
    }
    return month - 1;
  }

}
