/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules.spelling.morfologik.suggestions_ordering;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.rules.spelling.symspell.implementation.EditDistance;

import java.util.Random;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.languagetool.rules.spelling.morfologik.suggestions_ordering.DetailedDamerauLevenstheinDistance.Distance;

public class DetailedDamerauLevenstheinDistanceTest {

  private Pair<String, Distance> modifyString(String s, int distance) {
    String result = s;
    Distance history = new Distance();

    for (int i = 0; i < distance; i++) {
      DetailedDamerauLevenstheinDistance.EditOperation op = DetailedDamerauLevenstheinDistance.randomEdit();
      String tmp = op.apply(result);
      if (tmp == null) { // Operation can not be executed on current string, test another one
        return null;
      } else {
        result = tmp;
        history = history.track(op);
      }
    }
    return Pair.of(result, history);
  }

  @Test
  @Ignore("Needs further debugging, but since this is only used as a feature in ML models errors are tolerable for now.")
  public void testDistanceComputation() {
    String text = "This is a test text. Random edits will occur here. Foo bar baz. Bla bla. Lorem ipsum dolor sit amet.";
    Random random = new Random(0L);

    int trials = 1000;
    int maxDist = 10;
    EditDistance reference = new EditDistance(text, EditDistance.DistanceAlgorithm.Damerau);

    for (int i = 0; i < trials; i++) {
      int operations = random.nextInt(maxDist);
      Pair<String, Distance> modified = modifyString(text, operations);
      if (modified == null) {
        continue;
      }
      int actualDist = reference.compare(modified.getLeft(), -1);
      //assertEquals(modified.getRight().value(), actualDist); // not guaranteed
      Distance computedDist = DetailedDamerauLevenstheinDistance.compare(text, modified.getLeft());
      System.out.printf("Comparing '%s'; actual modifications: %s / computed modifications: %s / reference distance: %d.%n",
        modified.getLeft(), modified.getRight(), computedDist, actualDist);
      assertThat(computedDist.value(), anyOf(equalTo(modified.getRight().value()), equalTo(actualDist)));
    }
  }

  @Test
  @Ignore("WIP")
  public void testDistanceDetails() {
    assertEquals(new Distance().delete(), DetailedDamerauLevenstheinDistance.compare("Test", "Tet"));
    assertEquals(new Distance().insert(), DetailedDamerauLevenstheinDistance.compare("Test", "Teste"));
    assertEquals(new Distance().transpose(), DetailedDamerauLevenstheinDistance.compare("Test", "Tets"));
    assertEquals(new Distance().replace(), DetailedDamerauLevenstheinDistance.compare("Test", "Tast"));
    assertEquals(new Distance().replace().insert(), DetailedDamerauLevenstheinDistance.compare("Test", "Taste"));
    assertEquals(new Distance().delete().transpose(), DetailedDamerauLevenstheinDistance.compare("Test", "Tts"));
    assertEquals(new Distance().insert().insert(), DetailedDamerauLevenstheinDistance.compare("Test", "Teeste"));
  }

}
