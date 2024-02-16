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
package org.languagetool.rules.ca;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractDateCheckFilter;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.TestHackHelper;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Catalan localization of {@link AbstractDateCheckFilter}.
 * @since 2.7
 */
public class DateCheckFilter extends AbstractDateCheckFilter {

  private final DateFilterHelper dateFilterHelper = new DateFilterHelper();

  @Override
  protected int getMonth(String localizedMonth) {
    return dateFilterHelper.getMonth(localizedMonth);
  }

  @Override
  protected Calendar getCalendar() {
    return dateFilterHelper.getCalendar();
  }

  protected int getDayOfWeek(String dayStr) {
    return dateFilterHelper.getDayOfWeek(dayStr);
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    return dateFilterHelper.getDayOfWeek(date);
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    try {
      int dayOfWeekPos = Integer.parseInt(getRequired("weekDay", args)) - 1;
      int dayPos = Integer.parseInt(getRequired("day", args)) - 1;
      int monthPos = Integer.parseInt(getRequired("month", args)) - 1;
      int yearPos = Integer.parseInt(getOptional("year", args, "-1")) - 1;
      String dayOfWeekStr = patternTokens[dayOfWeekPos].getToken().replace("\u00AD", "");  // replace soft hyphen
      String dayStr = patternTokens[dayPos].getToken();
      String monthStr = patternTokens[monthPos].getToken();
      String yearStr = (yearPos > -1 ? patternTokens[yearPos].getToken() : null);
      int dayOfWeekFromString = getDayOfWeek(dayOfWeekStr);
      int day = getDayOfMonthFromArguments(dayStr);
      int month = getMonthFromArguments(monthStr);
      int year = getYear(yearStr);
      Calendar dateFromDate = getDate(day, month, year);
      int dayOfWeekFromDate = getdayOfWeekFromDate(dateFromDate);
      if (dayOfWeekFromDate == -1) {
        return null;
      }
      if (dayOfWeekFromString != dayOfWeekFromDate) {
        Calendar calFromDateString = Calendar.getInstance();
        calFromDateString.set(Calendar.DAY_OF_WEEK, dayOfWeekFromString);
        String message = match.getMessage()
          .replace("{realDay}", getDayOfWeek(dateFromDate))
          .replace("{day}", getDayOfWeek(calFromDateString))
          .replace("{currentYear}", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        int startIndex;
        int endIndex;
        if (dayOfWeekPos < dayPos) {
          startIndex = dayOfWeekPos;
          endIndex = dayPos;
        } else {
          startIndex = dayPos;
          endIndex = dayOfWeekPos;
        }
        RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), patternTokens[startIndex].getStartPos(), patternTokens[endIndex].getEndPos(), message, match.getShortMessage());
        ruleMatch.setType(match.getType());
        ruleMatch.setUrl(Tools.getUrl("https://www.timeanddate.com/calendar/?year=" + dateFromDate.get(Calendar.YEAR)));
        // suggestion changing day of week
        StringBuilder suggestion = new StringBuilder();
        boolean isFirst = true;
        for (int j = startIndex; j <= endIndex; j++) {
          if (isFirst) {
            isFirst = false;
          } else if (patternTokens[j].isWhitespaceBefore()) {
            suggestion.append(" ");
          }
          if (j == dayOfWeekPos) {
            suggestion.append(StringTools.preserveCase(getDayOfWeek(dateFromDate), dayOfWeekStr));
          } else {
            suggestion.append(patternTokens[j].getToken());
          }
        }
        if (!suggestion.toString().isEmpty()) {
          ruleMatch.setSuggestedReplacement(suggestion.toString());
        }
        // suggestion changing day of month
        String correctedDayofMonth = findNewDayOfMonth(day, month, year, dayOfWeekFromString);
        if (!correctedDayofMonth.isEmpty()) {
          suggestion = new StringBuilder();
          isFirst = true;
          for (int j = startIndex; j <= endIndex; j++) {
            if (isFirst) {
              isFirst = false;
            } else if (patternTokens[j].isWhitespaceBefore()) {
              suggestion.append(" ");
            }
            if (j == dayPos) {
              suggestion.append(correctedDayofMonth);
            } else {
              suggestion.append(patternTokens[j].getToken());
            }
          }
          if (!suggestion.toString().isEmpty()) {
            ruleMatch.addSuggestedReplacement(suggestion.toString());
          }
        }
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
      //logger.warn("Skipping potential match for " + match.getRule().getFullId(), e);
      return null;
    }
  }

  private String findNewDayOfMonth(int day, int month, int year, int dayOfWeekFromString) {
    int difference = 1;
    while (difference < 7) {
      if (day - difference > 0) {
        Calendar dateFromDate = getDate(day - difference, month, year);
        int dayOfWeekFromDate = getdayOfWeekFromDate(dateFromDate);
        if (dayOfWeekFromString == dayOfWeekFromDate) {
          return String.valueOf(day - difference);
        }
      }
      if (day + difference < 32) {
        Calendar dateFromDate = getDate(day + difference, month, year);
        int dayOfWeekFromDate = getdayOfWeekFromDate(dateFromDate);
        if (dayOfWeekFromString == dayOfWeekFromDate) {
          return String.valueOf(day + difference);
        }
      }
      difference++;
    }
    return "";
  }

  private int getdayOfWeekFromDate(Calendar dateFromDate) {
    int dayOfWeekFromDate;
    try {
      dayOfWeekFromDate = dateFromDate.get(Calendar.DAY_OF_WEEK);
    } catch (
      IllegalArgumentException ignore) {
      // happens with 'dates' like '32.8.2014' - those should be caught by a different rule
      return -1;
    }
    return dayOfWeekFromDate;
  }

  protected Calendar getDate(int dayOfMonth, int month, int year) {
    Calendar calendar = getCalendar();
    calendar.setLenient(false);  // be strict about validity of dates
    //noinspection MagicConstant
    calendar.set(year, month, dayOfMonth, 0, 0, 0);
    return calendar;
  }

  private int getYear(String yearArg) {
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
    return year;
  }

  private int getDayOfMonthFromArguments(String dayOfMonthString) {
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

  private int getMonthFromArguments(String monthStr) {
    int month;
    if (StringUtils.isNumeric(monthStr)) {
      month = Integer.parseInt(monthStr);
    } else {
      month = getMonth(StringTools.trimSpecialCharacters(monthStr));
    }
    return month - 1;
  }
}
