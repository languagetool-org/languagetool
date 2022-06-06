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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jep.Jep;
import jep.SharedInterpreter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SpaCy-based chunker.
 */
public class EnglishChunker implements Chunker {

  private final EnglishChunkFilter chunkFilter;
  private final ObjectMapper mapper;

  public EnglishChunker() {
    chunkFilter = new EnglishChunkFilter();
    mapper = new ObjectMapper(new JsonFactory());
  }

  @Override
  public void addChunkTags(List<AnalyzedTokenReadings> tokenReadings) {
    long t1 = System.currentTimeMillis();
    StringBuilder sb = new StringBuilder();
    for (AnalyzedTokenReadings tokenReading : tokenReadings) {
      sb.append(tokenReading.getToken());
    }
    try (Jep jep = new SharedInterpreter()) {
      jep.runScript("/home/dnaber/lt/git/languagetool/spacy-test.py");  // TODO: call once??
      jep.eval("result = chunking('" + sb + "')");
      String result = (String) jep.getValue("result");
      //System.out.println("-->" + result);
      try {
        JsonNode jsonNode = mapper.readTree(result);
        JsonNode nounChunksList = jsonNode.get("noun_chunks");
        for (JsonNode nounChunks : nounChunksList) {
          System.out.println(">>"+nounChunks);
        }
        List<ChunkTaggedToken> chunkTags = getChunkTaggedTokens(tokenReadings, nounChunksList);
        List<ChunkTaggedToken> filteredChunkTags = chunkFilter.filter(chunkTags);
        assignChunksToReadings(filteredChunkTags);
        /*for (AnalyzedTokenReadings tokenReading : tokenReadings) {  // TODO: remove
          System.out.println("TR: " + tokenReading.getToken() + " " + tokenReading.getChunkTags());
        }*/
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    long t2 = System.currentTimeMillis();
    System.out.println("time: " + (t2-t1) + "ms for " + sb.length()); // TODO: remove
  }

  @NotNull
  private List<ChunkTaggedToken> getChunkTaggedTokens(List<AnalyzedTokenReadings> tokenReadings, JsonNode parts) {
    List<ChunkTaggedToken> chunkTags = new ArrayList<>();
    for (JsonNode partsForChunk : parts) {
      int i = 0;
      for (JsonNode fromTo : partsForChunk) {
        String[] posParts = fromTo.asText().split("-");
        int startPos = Integer.parseInt(posParts[0]);
        int endPos = Integer.parseInt(posParts[1]);
        AnalyzedTokenReadings atr = getAnalyzedTokenReadingsFor(startPos, endPos, tokenReadings);
        String tag = i == 0 ? "B-NP" : "I-NP";
        chunkTags.add(new ChunkTaggedToken("", Collections.singletonList(new ChunkTag(tag)), atr));
        i++;
      }
    }
    return chunkTags;
  }

  private void assignChunksToReadings(List<ChunkTaggedToken> chunkTaggedTokens) {
    for (ChunkTaggedToken taggedToken : chunkTaggedTokens) {
      AnalyzedTokenReadings readings = taggedToken.getReadings();
      if (readings != null) {
        readings.setChunkTags(taggedToken.getChunkTags());
      }
    }
  }

  // Get only exact position matches - i.e. this can only be used for a trivial mapping
  // where tokens that are not exactly at the same position will be skipped. For example,
  // the tokens of "I'll" ([I] ['ll] vs [I]['][ll) cannot be mapped with this.
  @Nullable
  private AnalyzedTokenReadings getAnalyzedTokenReadingsFor(int startPos, int endPos, List<AnalyzedTokenReadings> tokenReadings) {
    int pos = 0;
    for (AnalyzedTokenReadings tokenReading : tokenReadings) {
      String token = tokenReading.getToken();
      int tokenStart = pos;
      int tokenEnd = pos + token.length();
      //System.out.println("# " + token + ": " + tokenStart + " =? " + startPos + " && " + tokenEnd + " ?= " + endPos);
      if (tokenStart == startPos && tokenEnd == endPos) {
        return tokenReading;
      }
      pos = tokenEnd;
    }
    return null;
  }

}
