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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

public class DateCheckFilterTest {

  private final RuleMatch match = new RuleMatch(new FakeRule(), null, 0, 10, "message");
  private final DateCheckFilter filter = new DateCheckFilter();

  @Test
  public void testAccept() {
    Assertions.assertNull(filter.acceptRuleMatch(match, makeMap("2014", "8" ,"23", "Samstag"), -1, null));  // correct date
    Assertions.assertNotNull(filter.acceptRuleMatch(match, makeMap("2014", "8" ,"23", "Sonntag"), -1, null));  // incorrect date
  }

  @Test
  public void testAcceptIncompleteArgs() {
    IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
      Map<String,String> map = makeMap("2014", "8" ,"23", "Samstag");
      map.remove("weekDay");
      filter.acceptRuleMatch(match, map, -1, null);      
    });
  }

  @Test
  public void testGetDayOfWeek1() {
    MatcherAssert.assertThat(filter.getDayOfWeek("So"), is(1));
    MatcherAssert.assertThat(filter.getDayOfWeek("Mo"), is(2));
    MatcherAssert.assertThat(filter.getDayOfWeek("mo"), is(2));
    MatcherAssert.assertThat(filter.getDayOfWeek("Mon."), is(2));
    MatcherAssert.assertThat(filter.getDayOfWeek("Montag"), is(2));
    MatcherAssert.assertThat(filter.getDayOfWeek("montag"), is(2));
    MatcherAssert.assertThat(filter.getDayOfWeek("Di"), is(3));
    MatcherAssert.assertThat(filter.getDayOfWeek("Fr"), is(6));
    MatcherAssert.assertThat(filter.getDayOfWeek("Samstag"), is(7));
    MatcherAssert.assertThat(filter.getDayOfWeek("Sonnabend"), is(7));
  }

  @Test
  public void testGetDayOfWeek2() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2014, 8-1, 29);
    MatcherAssert.assertThat(filter.getDayOfWeek(calendar), is("Freitag"));
    calendar.set(2014, 8-1, 30);
    MatcherAssert.assertThat(filter.getDayOfWeek(calendar), is("Samstag"));
  }

  @Test
  public void testGetMonth() {
    MatcherAssert.assertThat(filter.getMonth("Januar"), is(1));
    MatcherAssert.assertThat(filter.getMonth("Jan"), is(1));
    MatcherAssert.assertThat(filter.getMonth("Jan."), is(1));
    MatcherAssert.assertThat(filter.getMonth("Dezember"), is(12));
    MatcherAssert.assertThat(filter.getMonth("Dez"), is(12));
    MatcherAssert.assertThat(filter.getMonth("dez"), is(12));
    MatcherAssert.assertThat(filter.getMonth("DEZEMBER"), is(12));
    MatcherAssert.assertThat(filter.getMonth("dezember"), is(12));
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
