/**
 * SWE 261P Software Testing Project By Kenny Chen, Haitong Yan, Jiacheng Zhuo
 */
package org.languagetool.rules.en;

import org.junit.Test;
import java.util.Calendar;
import static org.junit.Assert.assertEquals;
import org.junit.Before;

public class DateCheckFilterRefactoredTest {
  private DateFilterHelper dateFilterHelper;
  private DateCheckFilterRefactored dateCheckFilterRefactored;

  @Before
  public void setUp() {
    dateFilterHelper = new DateFilterHelper();
    dateCheckFilterRefactored = new DateCheckFilterRefactored(dateFilterHelper);
  }

  @Test
  public void testGetCalender() {
    Calendar expectedCalendar = dateFilterHelper.getCalendar();
    Calendar actualCalendar = dateCheckFilterRefactored.getCalendar();

    assertEquals(expectedCalendar.get(Calendar.YEAR), actualCalendar.get(Calendar.YEAR));
    assertEquals(expectedCalendar.get(Calendar.MONTH), actualCalendar.get(Calendar.MONTH));
    assertEquals(expectedCalendar.get(Calendar.DAY_OF_MONTH),
        actualCalendar.get(Calendar.DAY_OF_MONTH));
    assertEquals(expectedCalendar.get(Calendar.HOUR_OF_DAY),
        actualCalendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(expectedCalendar.get(Calendar.MINUTE), actualCalendar.get(Calendar.MINUTE));
    assertEquals(expectedCalendar.get(Calendar.SECOND), actualCalendar.get(Calendar.SECOND));
  }

  @Test
  public void testGetDayOfWeek() {
    int dayOfWeek = dateFilterHelper.getDayOfWeek("mon");
    assertEquals(dayOfWeek, dateCheckFilterRefactored.getDayOfWeek("Monday"));
  }

  @Test
  public void testGetMonth() {
    int month = dateFilterHelper.getMonth("dec");
    assertEquals(month, dateCheckFilterRefactored.getMonth("December"));
  }
}
