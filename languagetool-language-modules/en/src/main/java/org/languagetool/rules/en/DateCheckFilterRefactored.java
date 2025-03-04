/**
 * SWE 261P Software Testing Project By Kenny Chen, Haitong Yan, Jiacheng Zhuo
 */
package org.languagetool.rules.en;

import org.languagetool.rules.AbstractDateCheckFilter;
import org.languagetool.rules.AbstractDateCheckWithSuggestionsFilter;
import org.languagetool.tools.StringTools;

import java.util.Calendar;

/**
 * English localization of {@link AbstractDateCheckFilter}.
 * 
 * @since 2.7
 */
public class DateCheckFilterRefactored extends AbstractDateCheckWithSuggestionsFilter {

  private final DateFilterHelper dateFilterHelper;

  // Constructor Injection for better testability
  public DateCheckFilterRefactored(DateFilterHelper dateFilterHelper) {
    this.dateFilterHelper = dateFilterHelper;
  }

  @Override
  protected Calendar getCalendar() {
    return dateFilterHelper.getCalendar();
  }

  @Override
  protected String getErrorMessageWrongYear() {
    return "This date is wrong. Did you mean \"{currentYear}\"?";
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  protected int getDayOfWeek(String dayStr) {
    return dateFilterHelper.getDayOfWeek(dayStr);
  }

  @Override
  protected String getDayOfWeek(Calendar date) {
    return dateFilterHelper.getDayOfWeek(date);
  }

  @SuppressWarnings({"ControlFlowStatementWithoutBraces", "MagicNumber"})
  @Override
  protected int getMonth(String monthStr) {
    return dateFilterHelper.getMonth(monthStr);
  }

  @Override
  protected String getDayStrLikeOriginal(String day, String original) {
    if (StringTools.isNumeric(original)) {
      return day;
    }
    int number = Integer.parseInt(day);
    if (number >= 11 && number <= 13) {
      return number + "th";
    }
    switch (number % 10) {
      case 1:
        return number + "st";
      case 2:
        return number + "nd";
      case 3:
        return number + "rd";
      default:
        return number + "th";
    }
  }
}
