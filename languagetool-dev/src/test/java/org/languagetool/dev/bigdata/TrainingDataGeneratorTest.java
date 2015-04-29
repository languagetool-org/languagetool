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
package org.languagetool.dev.bigdata;

import org.junit.Test;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TrainingDataGeneratorTest {

  @Test
  public void test() {
    TrainingDataGenerator prg = new TrainingDataGenerator(new English(), new FakeLanguageModel());
    assertThat(prg.getContext("This is a test.", 2, "XX", 1, 1).toString(), is("[is, XX, test]"));
    assertThat(prg.getContext("This is a test.", 2, "XX", 0, 2).toString(), is("[XX, test, _END_]"));
    assertThat(prg.getContext("This is a test.", 2, "XX", 2, 0).toString(), is("[This, is, XX]"));
    assertThat(prg.getContext("This is a test.", 2, "XX", 3, 0).toString(), is("[_START_, This, is, XX]"));
  }
  
  static class FakeLanguageModel implements LanguageModel {
    @Override
    public long getCount(String token1, String token2) {
      return 1;
    }
    @Override
    public long getCount(String token1, String token2, String token3) {
      return 2;
    }
    @Override
    public void close() {}
  }
  
}
