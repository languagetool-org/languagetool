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
package org.languagetool.chunking;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TokenPredicateTest {

  @Test
  public void test() {
    List<ChunkTag> chunkTags = Arrays.asList(new ChunkTag("CHUNK1"), new ChunkTag("CHUNK2"));
    AnalyzedTokenReadings readings = new AnalyzedTokenReadings(new AnalyzedToken("mytoken", "MYPOS", "mylemma"), 0);
    ChunkTaggedToken chunkTaggedToken = new ChunkTaggedToken("mytoken", chunkTags, readings);

    assertMatch("mytoken", chunkTaggedToken);
    assertNoMatch("mytoken2", chunkTaggedToken);
    assertMatch("string=mytoken", chunkTaggedToken);
    assertNoMatch("string=mytoken2", chunkTaggedToken);
    assertMatch("regex=my[abct]oken", chunkTaggedToken);
    assertNoMatch("regex=my[abc]oken", chunkTaggedToken);
    assertMatch("chunk=CHUNK1", chunkTaggedToken);
    assertMatch("chunk=CHUNK2", chunkTaggedToken);
    assertNoMatch("chunk=OTHERCHUNK", chunkTaggedToken);
    assertMatch("pos=MYPOS", chunkTaggedToken);
    assertNoMatch("pos=OTHER", chunkTaggedToken);
    assertMatch("posre=M.POS", chunkTaggedToken);
    assertNoMatch("posre=O.HER", chunkTaggedToken);

    try {
      assertNoMatch("invalid=token", chunkTaggedToken);
      fail();
    } catch (RuntimeException expected) {
      //expected
    }
  }

  private void assertMatch(String expr, ChunkTaggedToken chunkTaggedToken) {
    TokenPredicate predicate = new TokenPredicate(expr, false);
    assertTrue(predicate.apply(chunkTaggedToken));
  }

  private void assertNoMatch(String expr, ChunkTaggedToken chunkTaggedToken) {
    TokenPredicate predicate = new TokenPredicate(expr, false);
    assertFalse(predicate.apply(chunkTaggedToken));
  }
}
