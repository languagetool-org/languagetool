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

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EnglishChunkFilterTest {

  @Test
  public void testSingular() {
    assertChunks("He/B-NP owns/B-VP a/B-NP nice/I-NP house/I-NP in/X Berlin/B-NP ./.",
                 "He/B-NP-singular,E-NP-singular owns/B-VP a/B-NP-singular nice/I-NP-singular house/E-NP-singular in/X Berlin/B-NP-singular,E-NP-singular ./.");
  }

  @Test
  @Ignore("fails...")
  public void testPluralByAnd() {
    assertChunks("He/B-NP owns/B-VP a/B-NP large/I-NP house/I-NP and/I-NP a/I-NP ship/I-NP in/X Berlin/B-NP ./.",
                 "He/B-NP-singular owns/B-VP a/B-NP-plural large/I-NP-plural house/I-NP-plural and/I-NP-plural a/I-NP-plural ship/I-NP-plural in/X Berlin/B-NP-singular ./.");
  }

  @Test
  public void testPluralByPluralNoun() throws IOException {
    String input = "I/X have/N-VP ten/B-NP books/I-NP ./.";
    List<ChunkTaggedToken> tokens = makeTokens(input);
    tokens.remove(3);  // 'books'
    AnalyzedTokenReadings readings = new AnalyzedTokenReadings(Arrays.asList(
            new AnalyzedToken("books", "NNS", "book"),
            new AnalyzedToken("books", "VBZ", "book")),
            0
    );
    tokens.add(3, new ChunkTaggedToken("books", Collections.singletonList(new ChunkTag("I-NP")), readings));
    assertChunks(tokens, "I/X have/N-VP ten/B-NP-plural books/E-NP-plural ./.");
  }

  private void assertChunks(String input, String expected) {
    List<ChunkTaggedToken> tokens = makeTokens(input);
    assertChunks(tokens, expected);
  }

  private void assertChunks(List<ChunkTaggedToken> tokens, String expected) {
    EnglishChunkFilter filter = new EnglishChunkFilter();
    List<ChunkTaggedToken> result = filter.filter(tokens);
    assertThat(StringUtils.join(result, " "), is(expected));
  }

  private List<ChunkTaggedToken> makeTokens(String tokensAsString) {
    List<ChunkTaggedToken> result = new ArrayList<>();
    for (String token : tokensAsString.split(" ")) {
      String[] parts = token.split("/");
      if (parts.length != 2) {
        throw new RuntimeException("Invalid token, form 'x/y' required: " + token);
      }
      ChunkTag chunkTag = new ChunkTag(parts[1]);
      result.add(new ChunkTaggedToken(parts[0], Collections.singletonList(chunkTag), null));
    }
    return result;
  }

}
