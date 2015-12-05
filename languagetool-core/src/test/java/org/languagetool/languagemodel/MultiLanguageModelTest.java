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

import org.junit.Test;
import org.languagetool.rules.ngrams.Probability;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("MagicNumber")
public class MultiLanguageModelTest {
  
  @Test
  public void test() {
    LanguageModel lm1 = new FakeLanguageModel(0.5f);
    LanguageModel lm2 = new FakeLanguageModel(0.2f);
    MultiLanguageModel lm = new MultiLanguageModel(Arrays.asList(lm1, lm2));
    assertEquals(0.7f, lm.getPseudoProbability(Arrays.asList("foo", "bar", "blah")).getProb(), 0.01f);
    assertEquals(0.35f, lm.getPseudoProbability(Arrays.asList("foo", "bar", "blah")).getCoverage(), 0.01f);
  }

  private class FakeLanguageModel implements LanguageModel {
    private final float fakeValue;
    FakeLanguageModel(float fakeValue) {
      this.fakeValue = fakeValue;
    }
    @Override
    public Probability getPseudoProbability(List<String> context) {
      return new Probability(fakeValue, 0.5f);
    }
    @Override public void close() {}
  }
}
