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
package org.languagetool.rules.ar.filters;

import org.junit.Test;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ar.ArabicWordinessRule;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ArabicDateCheckFilterTest {

  private final RuleMatch match = new RuleMatch(new FakeRule(), null, 0, 10, "message");
  private final ArabicDateCheckFilter filter = new ArabicDateCheckFilter();

  @Test
  public void testAccept() {
    assertNull(filter.acceptRuleMatch(match, makeMap("2022", "3", "12", "السبت"), -1, null));  // correct date
    assertNotNull(filter.acceptRuleMatch(match, makeMap("2022", "3", "12", "الأحد"), -1, null));  // incorrect date
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAcceptIncompleteArgs() {
    Map<String, String> map = makeMap("2022", "3", "12", "السبت");
    map.remove("weekDay");
    filter.acceptRuleMatch(match, map, -1, null);
  }

  @Test
  public void testGetDayOfWeek1() {
    assertThat(filter.getDayOfWeek("الأحد"), is(Calendar.SUNDAY));
    assertThat(filter.getDayOfWeek("الإثنين"), is(Calendar.MONDAY));
    assertThat(filter.getDayOfWeek("الثلاثاء"), is(Calendar.TUESDAY));
    assertThat(filter.getDayOfWeek("الأربعاء"), is(Calendar.WEDNESDAY));
    assertThat(filter.getDayOfWeek("الخميس"), is(Calendar.THURSDAY));
    assertThat(filter.getDayOfWeek("الجمعة"), is(Calendar.FRIDAY));
    assertThat(filter.getDayOfWeek("السبت"), is(Calendar.SATURDAY));
    // inverse
    assertThat(filter.getDayOfWeek(Calendar.SUNDAY), is("الأحد"));
    assertThat(filter.getDayOfWeek(Calendar.MONDAY), is("الإثنين"));
    assertThat(filter.getDayOfWeek(Calendar.TUESDAY), is("الثلاثاء"));
    assertThat(filter.getDayOfWeek(Calendar.WEDNESDAY), is("الأربعاء"));
    assertThat(filter.getDayOfWeek(Calendar.THURSDAY), is("الخميس"));
    assertThat(filter.getDayOfWeek(Calendar.FRIDAY), is("الجمعة"));
    assertThat(filter.getDayOfWeek(Calendar.SATURDAY), is("السبت"));
  }

  @Test
  public void testGetDayOfWeek2() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(2022, Calendar.MARCH, 25);
    assertThat(filter.getDayOfWeek(calendar), is("الجمعة"));

    calendar.set(2022, Calendar.MARCH, 26);
    assertThat(filter.getDayOfWeek(calendar), is("السبت"));
  }

  @Test
  public void testGetMonth() {
    assertThat(filter.getMonth("جانفي"), is(1));
    assertThat(filter.getMonth("جانفييه"), is(1));
    assertThat(filter.getMonth("يناير"), is(1));
    assertThat(filter.getMonth("ديسمبر"), is(12));
    assertThat(filter.getMonth("كانون الأول"), is(12));
    assertThat(filter.getMonth("كانون أول"), is(12));
    assertThat(filter.getMonth("أبريل"), is(4));
    assertThat(filter.getMonth("نيسان"), is(4));

  }

  private Map<String, String> makeMap(String year, String month, String dayOfMonth, String weekDay) {
    Map<String, String> map = new HashMap<>();
    map.put("year", year);
    map.put("month", month);
    map.put("day", dayOfMonth);
    map.put("weekDay", weekDay);
    return map;
  }
}
