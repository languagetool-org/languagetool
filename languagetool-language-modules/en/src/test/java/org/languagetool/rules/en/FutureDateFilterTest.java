package org.languagetool.rules.en;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FutureDateFilterTest {
  FutureDateFilter futureDateFilter = new FutureDateFilter();
  @Test
  public void testGetMonth(){
    assertEquals(9, futureDateFilter.getMonth("Sep"));
  }

  @Test
  public void testGetCalender(){
    assertTrue(futureDateFilter.getCalendar() instanceof Calendar);
  }
}
