package org.languagetool.rules;

import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class YMDDateHelperTest {

  private YMDDateHelper dateHelper;

  /* This setUp method will be executed before each testcase */
  @Before
  public void setUp() {
    dateHelper = new YMDDateHelper();
  }

  /* This checks the "parseData" method */
  @Test
  public void testParseDate() {
    //Create a hash map with a date
    Map<String, String> args = new HashMap<>();
    args.put("date", "2023-11-02");

    //Call the parse data method
    Map<String, String> result = dateHelper.parseDate(args);

    //Check that the result is not NULL.
    assertNotNull(result);

    //Check if the year value matches the tokenised value.
    assertEquals("2023", result.get("year"));

    //Check if the month value matches the tokenised value.
    assertEquals("11", result.get("month"));

    //Check if the day value matches the tokenised value.
    assertEquals("02", result.get("day"));
  }

}
