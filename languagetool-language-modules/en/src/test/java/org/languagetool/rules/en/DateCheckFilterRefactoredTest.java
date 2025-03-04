/**
 * SWE 261P Software Testing Project By Kenny Chen, Haitong Yan, Jiacheng Zhuo
 */
package org.languagetool.rules.en;

import org.junit.Test;

import static org.mockito.Mockito.*;
import java.util.Calendar;
import static org.junit.Assert.assertEquals;
import org.junit.Before;

public class DateCheckFilterRefactoredTest {
  private DateFilterHelper dateFilterHelper;
  private DateCheckFilterRefactored dateCheckFilterRefactored;

  @Before
  public void setUp() {
    dateFilterHelper = mock(DateFilterHelper.class);
    dateCheckFilterRefactored = new DateCheckFilterRefactored(dateFilterHelper);
  }

  @Test
  public void testGetCalender() {
    Calendar calendar = Calendar.getInstance();
    when(dateFilterHelper.getCalendar()).thenReturn(calendar);
    assertEquals(calendar, dateCheckFilterRefactored.getCalendar());
  }
}
