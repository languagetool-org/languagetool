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
package org.languagetool.tokenizers.de;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GermanCompoundTokenizerTest {
  
  @Test
  public void test() throws IOException {
    GermanCompoundTokenizer tokenizer = new GermanCompoundTokenizer(true);
    assertThat(tokenizer.tokenize("Bahnhofsuhr").toString(), is("[Bahnhofs, uhr]"));
    assertThat(tokenizer.tokenize("natofreundlich").toString(), is("[nato, freundlich]"));
    assertThat(tokenizer.tokenize("natofreundliches").toString(), is("[nato, freundliches]"));
    assertThat(tokenizer.tokenize("Firefox-Add-on").toString(), is("[Firefox, , Add-on]"));  // why the space?
  }

  @Test
  @Ignore("for interactive use only")
  public void testInteractively() throws IOException {
    GermanCompoundTokenizer.ExtendedGermanWordSplitter splitter = new GermanCompoundTokenizer.ExtendedGermanWordSplitter(false);
    String wordsInput = "Bahnhofsuhr, Bahnhofssanierung, Thermostattest";
    String[] words = wordsInput.split(",\\s*");
    for (String word : words) {
      List<String> parts = splitter.splitWord(word);
      System.out.println(parts);
    }
  }

}
