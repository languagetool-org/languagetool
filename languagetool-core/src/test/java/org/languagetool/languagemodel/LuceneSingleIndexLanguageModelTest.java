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
import org.languagetool.JLanguageTool;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LuceneSingleIndexLanguageModelTest extends LanguageModelTest {

  @Test
  public void testLanguageModel() throws Exception {
    URL ngramUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/yy/ngram-index");
    try (LuceneLanguageModel model = new LuceneLanguageModel(new File(ngramUrl.getFile()))) {
      assertThat(model.getCount("the"), is(55L));
      assertThat(model.getCount(Arrays.asList("the", "nice")), is(3L));
      assertThat(model.getCount(Arrays.asList("the", "nice", "building")), is(1L));
      assertThat(model.getCount("not-in-here"), is(0L));
      assertThat(model.getTotalTokenCount(), is(3L));
    }
  }

  /**
   * Some values for average time per lookup on 2grams on a 3.7GB Lucene 4.8.1 index with 118,941,740 docs:
   * -no data in OS cache, index on external USB disk: 17626µs = 17ms
   * -no data in OS cache, index on SSD: 739µs = <0ms
   * -all data in OS cache (by running the test more than once): 163µs = <0ms
   * 
   * Some values for average time per lookup on 3grams on a 7.0GB Lucene 4.9 index:
   * -no data in OS cache, index on external USB disk: 13256µs = 13ms
   * -no data in OS cache, index on SSD: 791µs = <0ms
   * -all(?) data in OS cache (by running the test more than once): 162µs = <0ms
   * 
   * The tests have been performed on a Dell XSP13 (i7-3537U CPU) under Ubuntu 12.04, with Java 1.7.
   */
  @Test
  @Ignore("for interactive use only")
  public void testPerformance() throws Exception {
    // 2grams:
    //LanguageModel model = new LuceneLanguageModel(new File("/media/Data/google-ngram/2gram/lucene-index/merged/"));
    //super.testPerformance(model, 2);
    // 3grams:
    //LanguageModel model = new LuceneLanguageModel(new File("/media/Data/google-ngram/3gram/aggregated/lucene-index/merged/"));
    LuceneLanguageModel model = new LuceneLanguageModel(new File("/data/google-gram-index/"));
    super.testPerformance(model, 3);
  }
  
}
