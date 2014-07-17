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
package org.languagetool.languagemodel;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MorfologikLanguageModelTest extends LanguageModelTest {

  @Test
  public void testGetCount() throws Exception {
    LanguageModel model = new MorfologikLanguageModel(new File("src/test/resources/org/languagetool/languagemodel/frequency.dict"));
    assertThat(model.getCount("one", "two"), is(10L));
    assertThat(model.getCount("two", "three"), is(20L));
    assertThat(model.getCount("a", "café"), is(1000L));
    assertThat(model.getCount("an", "café"), is(2L));
  }
  
  @Test
  @Ignore("for interactive use only")
  public void testPerformance() throws Exception {
    LanguageModel model = new MorfologikLanguageModel(new File("/lt/en-homophones.dict"));
    super.testPerformance(model, 2);
  }
  
}
