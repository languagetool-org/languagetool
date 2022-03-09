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
package org.languagetool.rules.uk;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DateCheckFilterTest {

  @Test
  public void testGetDayOfWeek() throws Exception {
    DateCheckFilter filter = new DateCheckFilter();
    assertThat(filter.getDayOfWeek("Нед"), is(1));
    assertThat(filter.getDayOfWeek("Пон"), is(2));
    assertThat(filter.getDayOfWeek("пон"), is(2));
    assertThat(filter.getDayOfWeek("Понед."), is(2));
    assertThat(filter.getDayOfWeek("Понеділок"), is(2));
    assertThat(filter.getDayOfWeek("понеділок"), is(2));
    assertThat(filter.getDayOfWeek("Вт"), is(3));
    assertThat(filter.getDayOfWeek("Сер"), is(4));
    assertThat(filter.getDayOfWeek("П'ят"), is(6));
    assertThat(filter.getDayOfWeek("Суб"), is(7));
  }

  @Test
  public void testMonth() throws Exception {
    DateCheckFilter filter = new DateCheckFilter();
    assertThat(filter.getMonth("січ"), is(1));
    assertThat(filter.getMonth("гру"), is(12));
    assertThat(filter.getMonth("грудень"), is(12));
    assertThat(filter.getMonth("Грудень"), is(12));
    assertThat(filter.getMonth("ГРУДЕНЬ"), is(12));
  }

}
