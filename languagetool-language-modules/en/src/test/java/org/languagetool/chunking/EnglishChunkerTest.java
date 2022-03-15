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

import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;

public class EnglishChunkerTest {

  @Test
  public void testAddChunkTags() throws Exception {
    EnglishChunker chunker = new EnglishChunker();
    List<AnalyzedTokenReadings> readingsList = createReadingsList("A short test of the bicycle is needed");
    chunker.addChunkTags(readingsList);
    MatcherAssert.assertThat(readingsList.size(), is(15));
    // "A short test":
    MatcherAssert.assertThat(readingsList.get(0).getChunkTags().toString(), is("[B-NP-singular]"));
    MatcherAssert.assertThat(readingsList.get(2).getChunkTags().toString(), is("[I-NP-singular]"));
    MatcherAssert.assertThat(readingsList.get(4).getChunkTags().toString(), is("[E-NP-singular]"));
    // "the chunker":
    MatcherAssert.assertThat(readingsList.get(8).getChunkTags().toString(), is("[B-NP-singular]"));
    MatcherAssert.assertThat(readingsList.get(10).getChunkTags().toString(), is("[E-NP-singular]"));
    // "is"
    MatcherAssert.assertThat(readingsList.get(12).getChunkTags().toString(), is("[B-VP]"));
    MatcherAssert.assertThat(readingsList.get(14).getChunkTags().toString(), is("[I-VP]"));
  }

  @Test
  public void testAddChunkTagsSingular() throws Exception {
    EnglishChunker chunker = new EnglishChunker();
    JLanguageTool lt = new JLanguageTool(new English());
    List<AnalyzedSentence> sentences = lt.analyzeText("The abacus shows how numbers can be stored");
    List<AnalyzedTokenReadings> readingsList = Arrays.asList(sentences.get(0).getTokens());
    chunker.addChunkTags(readingsList);
    // "The abacus":
    MatcherAssert.assertThat(readingsList.get(1).getChunkTags().toString(), is("[B-NP-singular]"));
    MatcherAssert.assertThat(readingsList.get(3).getChunkTags().toString(), is("[E-NP-singular]"));
    // "numbers":
    MatcherAssert.assertThat(readingsList.get(9).getChunkTags().toString(), is("[B-NP-plural, E-NP-plural]"));
  }

  @Test
  public void testContractions() throws Exception {
    JLanguageTool lt = new JLanguageTool(new English());
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("I'll be there");
    AnalyzedTokenReadings[] tokens = analyzedSentence.getTokens();
    MatcherAssert.assertThat(tokens[1].getChunkTags().get(0), is(new ChunkTag("B-NP-singular")));
    MatcherAssert.assertThat(tokens[2].getChunkTags().get(0), is(new ChunkTag("B-VP"))); 
    MatcherAssert.assertThat(tokens[4].getChunkTags().get(0), is(new ChunkTag("I-VP"))); 
    MatcherAssert.assertThat(tokens[6].getChunkTags().get(0), is(new ChunkTag("I-VP")));
  }

  @Test
  public void testTokenize() {
    EnglishChunker chunker = new EnglishChunker();
    String expected = "[I, 'm, going, to, London]";
    MatcherAssert.assertThat(Arrays.toString(chunker.tokenize("I'm going to London")), is(expected));
    MatcherAssert.assertThat(Arrays.toString(chunker.tokenize("I’m going to London")), is(expected));  // different apostrophe char
  }

  @Test
  public void testNonBreakingSpace() throws IOException {
    String expectedTags = "[[/SENT_START], [away/JJ, away/NN, away/RB, away/RP, away/UH], [from/IN, from/RP], " +
      "[home/NN:UN], [often/RB], [?/SENT_END, ?/PCT]]";
    String expectedChunks = "[[], [B-ADVP], [], [B-PP], [], [B-NP-singular, E-NP-singular], [], [B-ADVP], [O]]";
    EnglishChunker chunker = new EnglishChunker();
    JLanguageTool lt = new JLanguageTool(new English());
    String input1 = "Away from home often?";
    MatcherAssert.assertThat(getPosTagsAsString(input1, lt).toString().replaceAll("\\[./null\\], ", ""), is(expectedTags));
    MatcherAssert.assertThat(getChunksAsString(input1, chunker, lt).toString(), is(expectedChunks));
    String input2 = "Away from home\u00A0often?";
    MatcherAssert.assertThat(getPosTagsAsString(input2, lt).toString().replaceAll("\\[./null\\], ", ""), is(expectedTags));
    MatcherAssert.assertThat(getChunksAsString(input2, chunker, lt).toString(), is(expectedChunks));
  }

  @Test
  @Disabled("active when #2119 is fixed")
  public void testZeroWidthNoBreakSpace() throws IOException {
    String expected = "[[], [B-NP-singular], [], [I-NP-singular], [], [E-NP-singular]]";
    EnglishChunker chunker = new EnglishChunker();
    JLanguageTool lt = new JLanguageTool(new English());
    List<String> res1 = getChunksAsString("The long-term stability", chunker, lt);
    MatcherAssert.assertThat(res1.toString(), is(expected));
    List<String> res2 = getChunksAsString("The\u200B \u200Blong-term\u200B \u200Bstability\u200B", chunker, lt);
    MatcherAssert.assertThat(res2.toString(), is(expected));
  }
  
  @NotNull
  private List<String> getChunksAsString(String input, EnglishChunker chunker, JLanguageTool lt) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(input);
    AnalyzedTokenReadings[] tokens = analyzedSentence.getTokens();
    List<AnalyzedTokenReadings> l = Arrays.asList(tokens);
    chunker.addChunkTags(l);
    return l.stream().map(k -> k.getChunkTags().toString()).collect(Collectors.toList());
  }

  @NotNull
  private List<String> getPosTagsAsString(String input, JLanguageTool lt) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(input);
    AnalyzedTokenReadings[] tokens = analyzedSentence.getTokens();
    List<AnalyzedTokenReadings> l = Arrays.asList(tokens);
    return l.stream().map(k -> k.getReadings().toString()).collect(Collectors.toList());
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
