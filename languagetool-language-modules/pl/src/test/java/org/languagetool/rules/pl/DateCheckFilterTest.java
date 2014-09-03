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
package org.languagetool.rules.pl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DateCheckFilterTest {

  @Test
  public void testGetDayOfWeek() throws Exception {
    DateCheckFilter filter = new DateCheckFilter();
    assertThat(filter.getDayOfWeek("niedz"), is(1));
    assertThat(filter.getDayOfWeek("pon"), is(2));
    assertThat(filter.getDayOfWeek("Pon"), is(2));
    assertThat(filter.getDayOfWeek("pon."), is(2));
    assertThat(filter.getDayOfWeek("poniedziałek"), is(2));
    assertThat(filter.getDayOfWeek("Poniedziałek"), is(2));
    assertThat(filter.getDayOfWeek("wtorek"), is(3));
    assertThat(filter.getDayOfWeek("pt"), is(6));
    assertThat(filter.getDayOfWeek("piątek"), is(6));
  }

  @Test
  public void testMonth() throws Exception {
    DateCheckFilter filter = new DateCheckFilter();
    assertThat(filter.getMonth("I"), is(1));
    assertThat(filter.getMonth("XII"), is(12));
    assertThat(filter.getMonth("grudnia"), is(12));
    assertThat(filter.getMonth("Grudnia"), is(12));
    assertThat(filter.getMonth("GRUDNIA"), is(12));
  }

}
