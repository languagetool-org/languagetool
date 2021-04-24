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
package org.languagetool.rules.ru;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DateCheckFilterTest {

  @Test
  public void testGetDayOfWeek() throws Exception {
    DateCheckFilter filter = new DateCheckFilter();
//    assertThat(filter.getDayOfWeek("вс"), is(1));
    assertThat(filter.getDayOfWeek("пн"), is(2));
//    assertThat(filter.getDayOfWeek("понедельник"), is(2));
    assertThat(filter.getDayOfWeek("пн."), is(2));
//    assertThat(filter.getDayOfWeek("Понедельник"), is(2));
//    assertThat(filter.getDayOfWeek("Пн"), is(2));
    assertThat(filter.getDayOfWeek("вт"), is(3));
    assertThat(filter.getDayOfWeek("пт"), is(6));
//    assertThat(filter.getDayOfWeek("пятница"), is(6));
  }

  @Test
  public void testMonth() throws Exception {
    DateCheckFilter filter = new DateCheckFilter();
    assertThat(filter.getMonth("I"), is(1));
    assertThat(filter.getMonth("XII"), is(12));
    assertThat(filter.getMonth("декабрь"), is(12));
    assertThat(filter.getMonth("Декабрь"), is(12));
    assertThat(filter.getMonth("ДЕКАБРЬ"), is(12));
  }

}
