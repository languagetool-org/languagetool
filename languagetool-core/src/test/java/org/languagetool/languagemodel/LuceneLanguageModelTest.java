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

public class LuceneLanguageModelTest extends LanguageModelTest {

  /**
   * Some values for average time per lookup on 2grams on a 3.7GB Lucene 4.8.1 index with 118,941,740 docs:
   * -no data in OS cache, index on external USB disk: 17626µs = 17ms
   * -no data in OS cache, index on SSD: 739µs = <0ms
   * -all data in OS cache (by running the test more than once): 163µs = <0ms
   */
  @Test
  @Ignore("for interactive use only")
  public void testPerformance() throws Exception {
    LanguageModel model = new LuceneLanguageModel(new File("/media/Data/google-ngram/2gram/lucene-index/merged/"));
    super.testPerformance(model);
  }
  
}
