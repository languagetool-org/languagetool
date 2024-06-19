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
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Accepts rule matches if a date doesn't match the accompanying weekday, e.g. if {@code Monday, 8 November 2003}
 * isn't actually a Monday. Replaces {@code {realDay}} with the real day of the date in the rule's message,
 * and {@code {day}} with the claimed day from the text (might be useful in case the text uses an abbreviation).
 * @since 2.7
 */
public abstract class AbstractDateCheckWithSuggestionsFilter extends RuleFilter {

  private static final Logger logger = LoggerFactory.getLogger(AbstractDateCheckWithSuggestionsFilter.class);

  // The day of the month may contain not only digits but also extra letters
  // such as"22nd" in English or "22-an" in Esperanto. The regexp extracts
  // the numerical part.
  protected static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d+).*");

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

  protected abstract String getErrorMessageWrongYear();

  /**
   * @param args           a map with values for {@code year}, {@code month}, {@code day} (day of month), {@code weekDay}
   */
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> args, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) {
    try {
      int dayOfWeekPos = getSkipCorrectedReference(tokenPositions, Integer.parseInt(getRequired("weekDay", args)));
      String dayOfWeekStr = patternTokens[dayOfWeekPos].getToken().replace("\u00AD", "");  // replace soft hyphen
      int dayPos;
      int monthPos;
      int yearPos;
      String dayStr;
      String monthStr;
      String yearStr;
      boolean isFullDateToken = false;
      int fullDatePos = getSkipCorrectedReference(tokenPositions, Integer.parseInt(getOptional("date", args, "-1"))); // format yyyy-mm-dd
      if (fullDatePos > -1 ) {
        String [] parts = patternTokens[fullDatePos].getToken().split("-");
        isFullDateToken = true;
        dayPos = fullDatePos;
        monthPos = fullDatePos;
        yearPos = fullDatePos;
        dayStr = parts[2];
        monthStr = parts[1];
        yearStr = parts[0];
      } else {
        dayPos = getSkipCorrectedReference(tokenPositions, Integer.parseInt(getRequired("day", args)));
        monthPos = getSkipCorrectedReference(tokenPositions, Integer.parseInt(getRequired("month", args)));
        yearPos = getSkipCorrectedReference(tokenPositions, Integer.parseInt(getOptional("year", args, "-1")));
        dayStr = patternTokens[dayPos].getToken();
        monthStr = patternTokens[monthPos].getToken();
        yearStr = (yearPos > -1 ? patternTokens[yearPos].getToken() : null);
      }
      int dayOfWeekFromString = getDayOfWeek(dayOfWeekStr);
      int day = getDayOfMonthFromStr(dayStr);
      int month = getMonthFromStr(monthStr);
      int year = getYearFromStr(yearStr);
      Calendar dateFromDate = getDate(day, month, year);
      int dayOfWeekFromDate = getdayOfWeekFromDate(dateFromDate);
      if (dayOfWeekFromDate == -1) {
        return null;
      }
      if (dayOfWeekFromString != dayOfWeekFromDate) {
        Calendar calFromDateString = Calendar.getInstance();
        calFromDateString.set(Calendar.DAY_OF_WEEK, dayOfWeekFromString);
        String message;
        // suggest changing the year (to current year)
        int currentYear = getYearFromStr(null);
        Calendar dateFromDateChangeYear = getDate(day, month, currentYear);
        int dayOfWeekFromDateChangeYear = getdayOfWeekFromDate(dateFromDateChangeYear);
        if (dayOfWeekFromString == dayOfWeekFromDateChangeYear) {
          message = getErrorMessageWrongYear().replace("{currentYear}", String.valueOf(currentYear));
          RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(),
            patternTokens[yearPos].getStartPos(), patternTokens[yearPos].getEndPos(), message, match.getShortMessage());
          ruleMatch.setType(match.getType());
          ruleMatch.setUrl(Tools.getUrl("https://www.timeanddate.com/calendar/?year=" + dateFromDateChangeYear.get(Calendar.YEAR)));
          if (isFullDateToken) {
            ruleMatch.setSuggestedReplacement(String.valueOf(currentYear)+"-"+monthStr+"-"+dayStr);
          } else {
            ruleMatch.setSuggestedReplacement(String.valueOf(currentYear));
          }
          return ruleMatch;
        }
        // suggest changing day of week or day of month
        message = match.getMessage()
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
        // suggest changing day of week
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
          ruleMatch.setSuggestedReplacement(adjustSuggestion(suggestion.toString()));
        }
        // suggest changing day of month
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
              if (isFullDateToken) {
                suggestion.append(yearStr+"-"+monthStr+"-"+correctedDayofMonth);
              } else {
                suggestion.append(getDayStrLikeOriginal(correctedDayofMonth, dayStr));
              }
            } else {
              suggestion.append(patternTokens[j].getToken());
            }
          }
          if (!suggestion.toString().isEmpty()) {
            ruleMatch.addSuggestedReplacement(adjustSuggestion(suggestion.toString()));
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

  private int getYearFromStr(String yearArg) {
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

  private int getDayOfMonthFromStr(String dayOfMonthString) {
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

  private int getMonthFromStr(String monthStr) {
    int month;
    if (StringUtils.isNumeric(monthStr)) {
      month = Integer.parseInt(monthStr);
    } else {
      month = getMonth(StringTools.trimSpecialCharacters(monthStr));
    }
    return month - 1;
  }

  protected String getDayStrLikeOriginal(String day, String original) {
    return day;
  }

  // typographical adjusments needed in some language
  protected String adjustSuggestion(String sugg) {
    return sugg;
  }

}
