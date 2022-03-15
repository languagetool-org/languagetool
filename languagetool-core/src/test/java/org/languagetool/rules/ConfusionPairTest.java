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

import org.junit.jupiter.api.Test;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;

import static org.hamcrest.CoreMatchers.is;

public class ConfusionPairTest {

  @Test
  public void testGet() {
    ConfusionPair confusionSet = new ConfusionPair("one", "two", 1L, true);
    MatcherAssert.assertThat(confusionSet.getTerms().size(), is(2));
    Assertions.assertTrue(confusionSet.getTerms().toString().contains("one"));
    Assertions.assertTrue(confusionSet.getTerms().toString().contains("two"));
    MatcherAssert.assertThat(confusionSet.getUppercaseFirstCharTerms().size(), is(2));
    Assertions.assertTrue(confusionSet.getUppercaseFirstCharTerms().toString().contains("One"));
    Assertions.assertTrue(confusionSet.getUppercaseFirstCharTerms().toString().contains("Two"));
  }

  @Test
  public void testEquals() {
    ConfusionPair confusionSet1a = new ConfusionPair("one", "two", 1L, true);
    ConfusionPair confusionSet1a2 = new ConfusionPair("one", "two", 1L, true);
    ConfusionPair confusionSet1b = new ConfusionPair("two", "one", 1L, true);
    ConfusionPair confusionSet3 = new ConfusionPair("Two", "one", 1L, true);
    ConfusionPair confusionSet4 = new ConfusionPair("Two", "one", 2L, true);
    Assertions.assertEquals(confusionSet1a, confusionSet1a2);
    Assertions.assertNotEquals(confusionSet1a, confusionSet1b);
    Assertions.assertNotEquals(confusionSet1a, confusionSet3);
    Assertions.assertNotEquals(confusionSet1b, confusionSet3);
    Assertions.assertNotEquals(confusionSet3, confusionSet4);
  }

}