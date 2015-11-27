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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@SuppressWarnings("MagicNumber")
public class MultiLanguageModelTest {
  
  @Test
  public void test() {
    LanguageModel lm1 = new FakeLanguageModel(10);
    LanguageModel lm2 = new FakeLanguageModel(15);
    MultiLanguageModel lm = new MultiLanguageModel(Arrays.asList(lm1, lm2));
    assertThat(lm.getCount("foo"), is(25L));
    assertThat(lm.getCount("foo", "bar"), is(4L));
    assertThat(lm.getCount("foo", "bar", "blah"), is(6L));
    assertThat(lm.getCount(Arrays.asList("a", "b", "c")), is(8L));
    assertThat(lm.getTotalTokenCount(), is(18L));
  }

  private class FakeLanguageModel implements LanguageModel {
    private final int fakeValue;
    FakeLanguageModel(int fakeValue) {
      this.fakeValue = fakeValue;
    }
    @Override
    public long getCount(String token1) {
      return fakeValue;
    }
    @Override
    public long getCount(String token1, String token2) {
      return 2;
    }
    @Override
    public long getCount(String token1, String token2, String token3) {
      return 3;
    }
    @Override
    public long getCount(List<String> tokens) {
      return 4;
    }
    @Override
    public long getTotalTokenCount() {
      return 9;
    }
    @Override public void close() {}
  }
}