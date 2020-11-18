/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Peter Gromov
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
package org.languagetool.rules.patterns;

import com.google.common.collect.Sets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.languagetool.rules.patterns.StringMatcher.getPossibleRegexpValues;

public class StringMatcherTest {

  @Test
  public void testGetPossibleValues() {
    assertNull(getPossibleRegexpValues("x.*"));
    assertNull(getPossibleRegexpValues("x+"));
    assertNull(getPossibleRegexpValues("^x$"));
    assertNull(getPossibleRegexpValues("a.c"));
    assertNull(getPossibleRegexpValues("a{2}"));
    assertNull(getPossibleRegexpValues("[a-z]"));
    assertNull(getPossibleRegexpValues("[^a]"));
    assertNull(getPossibleRegexpValues("a[a-z]"));
    assertNull(getPossibleRegexpValues("(?=a)"));

    assertPossibleValues("aa|bb", "aa", "bb");
    assertPossibleValues("aa", "aa");
    assertPossibleValues("aa?", "aa", "a");
    assertPossibleValues("[abc]", "a", "b", "c");
  }

  private static void assertPossibleValues(String regexp, String... expected) {
    assertEquals(Sets.newHashSet(expected), getPossibleRegexpValues(regexp));
  }

}
