/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ConfusionPairTest {

  @Test
  public void testGet() {
    ConfusionPair confusionSet = new ConfusionPair("one", "two", 1L, true);
    assertThat(confusionSet.getTerms().size(), is(2));
    assertTrue(confusionSet.getTerms().toString().contains("one"));
    assertTrue(confusionSet.getTerms().toString().contains("two"));
    assertThat(confusionSet.getUppercaseFirstCharTerms().size(), is(2));
    assertTrue(confusionSet.getUppercaseFirstCharTerms().toString().contains("One"));
    assertTrue(confusionSet.getUppercaseFirstCharTerms().toString().contains("Two"));
  }

  @Test
  public void testEquals() {
    ConfusionPair confusionSet1a = new ConfusionPair("one", "two", 1L, true);
    ConfusionPair confusionSet1a2 = new ConfusionPair("one", "two", 1L, true);
    ConfusionPair confusionSet1b = new ConfusionPair("two", "one", 1L, true);
    ConfusionPair confusionSet3 = new ConfusionPair("Two", "one", 1L, true);
    ConfusionPair confusionSet4 = new ConfusionPair("Two", "one", 2L, true);
    assertTrue(confusionSet1a.equals(confusionSet1a2));
    assertFalse(confusionSet1a.equals(confusionSet1b));
    assertFalse(confusionSet1a.equals(confusionSet3));
    assertFalse(confusionSet1b.equals(confusionSet3));
    assertFalse(confusionSet3.equals(confusionSet4));
  }

}