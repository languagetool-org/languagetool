/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.languagemodel.LuceneLanguageModel;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SuggestionSorterTest {
  
  @Test
  @Ignore("interferes with LuceneSingleIndexLanguageModel")
  public void testSort() {
    URL ngramUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/yy/ngram-index");
    try (LuceneLanguageModel model = new LuceneLanguageModel(new File(ngramUrl.getFile()))) {
      SuggestionSorter sorter = new SuggestionSorter(model);
      assertThat(sorter.sortSuggestions(Arrays.asList("thee", "the", "teh")), is(Arrays.asList("the", "thee", "teh")));
      assertThat(sorter.sortSuggestions(Arrays.asList("nuce", "foo", "nice")), is(Arrays.asList("nice", "nuce", "foo")));
      assertThat(sorter.sortSuggestions(Arrays.asList("nuce")), is(Arrays.asList("nuce")));
      assertThat(sorter.sortSuggestions(Arrays.asList("nice")), is(Arrays.asList("nice")));
      assertThat(sorter.sortSuggestions(Arrays.asList("")), is(Arrays.asList("")));
    }
  }

  @Test
  @Ignore("needs full index")
  public void testSortWithFullNgrams() {
    String ngramIndex = "/home/dnaber/data/google-ngram-index/de";
    try (LuceneLanguageModel model = new LuceneLanguageModel(new File(ngramIndex))) {
      SuggestionSorter sorter = new SuggestionSorter(model);
      //TODO: support phrases
      //assertThat(sorter.sortSuggestions(Arrays.asList("alluvial", "allzu viel")), is(Arrays.asList("allzu viel", "alluvial")));
    }
  }

}