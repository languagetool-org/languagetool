/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class FastTextTest {

  @Test
  public void testParsing() throws Exception {
    FastText ft = new FastText();
    List<String> l = Arrays.asList("en", "fy", "de", "es", "nl");
    Map<String, Double> res1 = ft.parseBuffer("__label__nl 0.423696 __label__fy 0.207109", l);
    assertThat(res1.size(), is(2));
    assertThat(res1.get("nl"), is(0.423696));
    assertThat(res1.get("fy"), is(0.207109));
    Map<String, Double> res2 = ft.parseBuffer("__label__de 0.999985 __label__es 2.02195e-05", l);
    assertThat(res2.size(), is(2));
    assertThat(res2.get("de"), is(0.999985));
    assertThat(res2.get("es"), is(2.02195e-05));
    Map<String, Double> res3 = ft.parseBuffer("__label__en 1", l);
    assertThat(res3.size(), is(1));
    assertThat(res3.get("en"), is(1.0));
    Map<String, Double> res4 = ft.parseBuffer("__label__de 1.00003", l);  // values larger 1 can actually happen
    assertThat(res4.size(), is(1));
    assertThat(res4.get("de"), is(1.00003));
    try {
      System.out.println("Testing invalid input, ignore the following 'Error while parsing':");
      ft.parseBuffer("xxx", l);
      fail();
    } catch (RuntimeException expected) {}
    try {
      System.out.println("Testing invalid input, ignore the following warning:");
      ft.parseBuffer("xxx foo", l);
      fail();
    } catch (NumberFormatException expected) {}
  }

}
