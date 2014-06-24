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
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MorfologikLanguageModelTest {

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
    try (FileInputStream fis = new FileInputStream("/lt/performance-test/en.txt")) {
      String content = StringTools.readStream(fis, "UTF-8");
      WordTokenizer wordTokenizer = new WordTokenizer();
      List<String> words = wordTokenizer.tokenize(content);
      String prevWord = null;
      for (String word : words) {
        if (word.trim().isEmpty()) {
          continue;
        }
        if (prevWord != null) {
          long t1 = System.nanoTime()/1000;
          long count = model.getCount(prevWord, word);
          long t2 = System.nanoTime()/1000;
          long time = t2-t1;
          System.out.println(prevWord + " " + word + ": " + count + ", " + time + "µs");
        }
        prevWord = word;
      }
    }
  }
  
}
