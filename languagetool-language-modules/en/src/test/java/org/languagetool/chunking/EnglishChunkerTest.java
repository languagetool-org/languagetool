/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.chunking;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EnglishChunkerTest {

  @Test
  public void testAddChunkTags() throws Exception {
    EnglishChunker chunker = new EnglishChunker();
    List<AnalyzedTokenReadings> readingsList = createReadingsList("A test of the bicycle is needed");
    chunker.addChunkTags(readingsList);
    assertThat(readingsList.size(), is(13));
    // "A test":
    assertThat(readingsList.get(0).getAnalyzedToken(0).getChunkTag().toString(), is("B-NP-singular"));
    assertThat(readingsList.get(2).getAnalyzedToken(0).getChunkTag().toString(), is("I-NP-singular"));
    // "the chunker":
    assertThat(readingsList.get(6).getAnalyzedToken(0).getChunkTag().toString(), is("B-NP-singular"));
    assertThat(readingsList.get(8).getAnalyzedToken(0).getChunkTag().toString(), is("I-NP-singular"));
    // "is"
    assertThat(readingsList.get(10).getAnalyzedToken(0).getChunkTag().toString(), is("B-VP"));
    assertThat(readingsList.get(12).getAnalyzedToken(0).getChunkTag().toString(), is("I-VP"));
  }

  @Test
  public void testContractions() throws Exception {
    JLanguageTool langTool = new JLanguageTool(new English());
    AnalyzedSentence analyzedSentence = langTool.getAnalyzedSentence("I'll be there");
    AnalyzedTokenReadings[] tokens = analyzedSentence.getTokens();
    assertThat(tokens[1].getReadings().get(0).getChunkTag(), is(new ChunkTag("B-NP-singular")));
    assertNull(tokens[2].getReadings().get(0).getChunkTag());  // "'" cannot be mapped as we tokenize differently
    assertNull(tokens[3].getReadings().get(0).getChunkTag());  // "ll" cannot be mapped as we tokenize differently
    assertThat(tokens[5].getReadings().get(0).getChunkTag(), is(new ChunkTag("I-VP")));
  }

  private List<AnalyzedTokenReadings> createReadingsList(String sentence) {
    StringTokenizer tokenizer = new StringTokenizer(sentence, " ", true);
    List<AnalyzedTokenReadings> result = new ArrayList<>();
    int pos = 0;
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (token.trim().isEmpty()) {
        result.add(new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), pos));
      } else {
        result.add(new AnalyzedTokenReadings(new AnalyzedToken(token, "fake", "fake"), pos));
      }
      pos += token.length();
    }
    return result;
  }
}
