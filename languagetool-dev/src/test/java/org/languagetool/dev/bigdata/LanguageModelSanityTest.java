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

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.languagemodel.LuceneLanguageModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.fail;

public class LanguageModelSanityTest {

  private static final String NGRAM_DIR = "/data/google-ngram-index";

  @Test
  @Ignore("Interactive use only, requires local ngram index")
  public void testEnglishLanguageModelSanity() throws IOException {
    LuceneLanguageModel lm = new LuceneLanguageModel(new File(NGRAM_DIR));
    // 1gram:
    assertMatches(lm, "the");
    assertMatches(lm, "The");
    assertMatches(lm, ",");
    assertMatches(lm, "0");
    assertMatches(lm, "1");
    assertMatches(lm, "2");
    assertMatches(lm, "3");
    assertMatches(lm, "4");
    assertMatches(lm, "5");
    assertMatches(lm, "6");
    assertMatches(lm, "7");
    assertMatches(lm, "8");
    assertMatches(lm, "9");
    assertMatches(lm, ":");
    assertMatches(lm, "(");
    assertMatches(lm, ")");
    assertMatches(lm, "£");
    // 2gram:
    assertMatches(lm, "the man");
    assertMatches(lm, "The man");
    assertMatches(lm, "_START_ the");
    assertMatches(lm, "_START_ The");
    assertMatches(lm, "it _END_");
    assertMatches(lm, "it .");
    assertMatches(lm, "Also ,");
    assertMatches(lm, "is 0");
    assertMatches(lm, ": it");
    assertMatches(lm, "( it");
    assertMatches(lm, "it )");
    assertMatches(lm, "£ 5");
    // 3gram:
    assertMatches(lm, "the man who");
    assertMatches(lm, "The man who");
    assertMatches(lm, "_START_ The man");
    assertMatches(lm, "it was _END_");
    assertMatches(lm, "it was .");
    assertMatches(lm, "Also , it");
    assertMatches(lm, "it is 0");
    assertMatches(lm, ": it is");
    assertMatches(lm, "( it is");
    assertMatches(lm, "it is )");
    assertMatches(lm, "five - pound");
    assertMatches(lm, "is £ 5");
    assertMatches(lm, "it 's a");
    assertMatches(lm, "it ' s");
    // 4gram:
    assertMatches(lm, "the man who could");
    assertMatches(lm, "The man who could");
    assertMatches(lm, "five - pound note");
    assertMatches(lm, "_START_ The man who");
    assertMatches(lm, "which it was _END_");
    assertMatches(lm, "Also , it is");
    assertMatches(lm, "when it is 0");
    assertMatches(lm, "it is £ 5");
  }

  private void assertMatches(LuceneLanguageModel lm, String phrase) {
    String[] words = phrase.split(" ");
    long count = lm.getCount(Arrays.asList(words));
    System.out.println(Arrays.toString(words) + ": " +  count);
    if (count < 10) {
      fail("Only got " + count + " matches for " + Arrays.toString(words));
    }
  }

}
