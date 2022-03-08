/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.languagemodel;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.rules.ngrams.FakeLanguageModel;
import org.languagetool.rules.ngrams.Probability;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;

public class BaseLanguageModelTest {

  @Test
  public void testPseudoProbability() throws IOException {
    try (FakeLanguageModel lm = new FakeLanguageModel()) {
      Probability prob1 = lm.getPseudoProbability(Arrays.asList("no", "data", "here"));
      double delta = 0.001;
      Assertions.assertEquals(0.0081, prob1.getProb(), delta);  // artificially not zero
      MatcherAssert.assertThat(prob1.getCoverage(), is(0.0f));
      Probability prob2 = lm.getPseudoProbability(Arrays.asList("1", "2", "3", "4"));
      Assertions.assertEquals(0.0081, prob2.getProb(), delta);  // artificially not zero
      MatcherAssert.assertThat(prob2.getCoverage(), is(0.0f));
      Probability prob3 = lm.getPseudoProbability(Arrays.asList("There", "are"));
      Assertions.assertEquals(0.082, prob3.getProb(), delta);
      MatcherAssert.assertThat(prob3.getCoverage(), is(0.5f));
    }
  }

  @Test
  public void testPseudoProbabilityFail1() throws IOException {
    IndexOutOfBoundsException thronw = Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
        try (FakeLanguageModel lm = new FakeLanguageModel()) {
        lm.getPseudoProbability(Collections.emptyList());
      } 
    });
  }
}
