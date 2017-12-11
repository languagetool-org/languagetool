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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class DateCheckFilterTest {

  private final RuleMatch match = new RuleMatch(new FakeRule(), null, 0, 10, "message");
  private final DateCheckFilter filter = new DateCheckFilter();

  @Test
  public void testAccept() throws Exception {
    assertNull(filter.acceptRuleMatch(match, makeMap("2014", "8" ,"23", "Samstag"), null));  // correct date
    assertNotNull(filter.acceptRuleMatch(match, makeMap("2014", "8" ,"23", "Sonntag"), null));  // incorrect date
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAcceptIncompleteArgs() throws Exception {
    Map<String,String> map = makeMap("2014", "8" ,"23", "Samstag");
    map.remove("weekDay");
    filter.acceptRuleMatch(match, map, null);
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidDay() throws Exception {
    filter.acceptRuleMatch(match, makeMap("2014", "8", "23", "invalid"), null);
  }

  @Test
  public void testGetDayOfWeek1() throws Exception {
    assertThat(filter.getDayOfWeek("So"), is(1));
    assertThat(filter.getDayOfWeek("Mo"), is(2));
    assertThat(filter.getDayOfWeek("mo"), is(2));
    assertThat(filter.getDayOfWeek("Mon."), is(2));
    assertThat(filter.getDayOfWeek("Montag"), is(2));
    assertThat(filter.getDayOfWeek("montag"), is(2));
    assertThat(filter.getDayOfWeek("Di"), is(3));
    assertThat(filter.getDayOfWeek("Fr"), is(6));
    assertThat(filter.getDayOfWeek("Samstag"), is(7));
    assertThat(filter.getDayOfWeek("Sonnabend"), is(7));
  }

  @Test
  public void testGetDayOfWeek2() throws Exception {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2014, 8-1, 29);
    assertThat(filter.getDayOfWeek(calendar), is("Freitag"));
    calendar.set(2014, 8-1, 30);
    assertThat(filter.getDayOfWeek(calendar), is("Samstag"));
  }

  @Test
  public void testGetMonth() throws Exception {
    assertThat(filter.getMonth("Januar"), is(1));
    assertThat(filter.getMonth("Jan"), is(1));
    assertThat(filter.getMonth("Jan."), is(1));
    assertThat(filter.getMonth("Dezember"), is(12));
    assertThat(filter.getMonth("Dez"), is(12));
    assertThat(filter.getMonth("dez"), is(12));
    assertThat(filter.getMonth("DEZEMBER"), is(12));
    assertThat(filter.getMonth("dezember"), is(12));
  }

  private Map<String, String> makeMap(String year, String month, String dayOfMonth, String weekDay) {
    Map<String,String> map = new HashMap<>();
    map.put("year", year);
    map.put("month", month);
    map.put("day", dayOfMonth);
    map.put("weekDay", weekDay);
    return map;
  }
}
