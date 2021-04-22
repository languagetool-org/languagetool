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
package org.languagetool.rules.en;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DateCheckFilterTest {

  @Test
  public void testGetDayOfWeek() throws Exception {
    DateCheckFilter filter = new DateCheckFilter();
    assertThat(filter.getDayOfWeek("Sun"), is(1));
    assertThat(filter.getDayOfWeek("Mon"), is(2));
    assertThat(filter.getDayOfWeek("mon"), is(2));
    assertThat(filter.getDayOfWeek("Mon."), is(2));
    assertThat(filter.getDayOfWeek("Monday"), is(2));
    assertThat(filter.getDayOfWeek("monday"), is(2));
    assertThat(filter.getDayOfWeek("Tue"), is(3));
    assertThat(filter.getDayOfWeek("Fri"), is(6));
    assertThat(filter.getDayOfWeek("Fr"), is(6));
    assertThat(filter.getDayOfWeek("Saturday"), is(7));
  }

  @Test
  public void testMonth() throws Exception {
    DateCheckFilter filter = new DateCheckFilter();
    assertThat(filter.getMonth("jan"), is(1));
    assertThat(filter.getMonth("dec"), is(12));
    assertThat(filter.getMonth("december"), is(12));
    assertThat(filter.getMonth("December"), is(12));
    assertThat(filter.getMonth("DECEMBER"), is(12));
  }

}
