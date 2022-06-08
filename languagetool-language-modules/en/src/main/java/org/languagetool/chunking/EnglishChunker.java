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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tools.Tools;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * SpaCy-based chunker.
 */
public class EnglishChunker implements Chunker {

  private static final java.net.URL SERVER_URL = Tools.getUrl("http://localhost:5000/");

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
    try {
      JsonNode json = getSpacyResultsViaHttp(sb.toString(), SERVER_URL);
      JsonNode nounChunksList = json.get("noun_chunks");
      List<ChunkTaggedToken> chunkTags = getChunkTaggedTokens(tokenReadings, nounChunksList, sb.toString());
      //assignVerbPhrases(tokenReadings, json.get("tokens"), chunkTags);
      //
      // TODO: assign ADVP (50x), ADJP (48x), PP (81x), SBAR (38x), PRT (11x)
      //
      Collections.sort(chunkTags);
      List<ChunkTaggedToken> filteredChunkTags = chunkFilter.filter(chunkTags);
      assignChunksToReadings(filteredChunkTags);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    long t2 = System.currentTimeMillis();
    //System.out.println("time: " + (t2-t1) + "ms for " + sb.length()); // TODO: remove
  }

  private JsonNode getSpacyResultsViaHttp(String text, URL url) throws IOException {
    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
    HttpURLConnection.setFollowRedirects(false);
    huc.setConnectTimeout(1000);
    huc.setReadTimeout(3_000);  // TODO: enough for long texts?
    // longer texts take longer to check, so increase the timeout:
    huc.setRequestMethod("POST");
    huc.setDoOutput(true);
    try {
      huc.connect();
      try (DataOutputStream wr = new DataOutputStream(huc.getOutputStream())) {
        String urlParameters = "text=" + URLEncoder.encode(text, "UTF-8");
        wr.write(urlParameters.getBytes(StandardCharsets.UTF_8));
      }
      InputStream is = huc.getInputStream();
      return mapper.readTree(is);
    } finally {
      huc.disconnect();
    }
  }

  private void assignVerbPhrases(List<AnalyzedTokenReadings> tokenReadings, JsonNode spacyTokens, List<ChunkTaggedToken> chunkTags) {
    JsonNode prevSpacyToken = null;
    String prevPos = null;
    for (JsonNode spacyToken : spacyTokens) {
      String pos = spacyToken.get("pos").asText();
      AnalyzedTokenReadings atr = getAnalyzedTokenReadingsFor(spacyToken.get("from").asInt(), spacyToken.get("to").asInt(), tokenReadings);
      if ("AUX".equals(prevPos) && pos.equals("VERB")) {  // e.g. "is needed"
        AnalyzedTokenReadings prevAtr = getAnalyzedTokenReadingsFor(prevSpacyToken.get("from").asInt(), prevSpacyToken.get("to").asInt(), tokenReadings);
        chunkTags.add(new ChunkTaggedToken(prevSpacyToken.get("text").asText(), Collections.singletonList(new ChunkTag("B-VP")), prevAtr));
        chunkTags.add(new ChunkTaggedToken(spacyToken.get("text").asText(), Collections.singletonList(new ChunkTag("I-VP")), atr));
      } else if (pos.equals("VERB")) {
        chunkTags.add(new ChunkTaggedToken(spacyToken.get("text").asText(), Collections.singletonList(new ChunkTag("B-VP")), atr));
      } else if (pos.equals("PUNCT")) {
        // simulate OpenNLP chunker:
        chunkTags.add(new ChunkTaggedToken(spacyToken.get("text").asText(), Collections.singletonList(new ChunkTag("O")), atr));
      }
      // TODO: more cases, see testInteractive()
      prevPos = pos;
      prevSpacyToken = spacyToken;
    }
  }

  @NotNull
  private List<ChunkTaggedToken> getChunkTaggedTokens(List<AnalyzedTokenReadings> tokenReadings, JsonNode parts, String text) {
    List<ChunkTaggedToken> chunkTags = new ArrayList<>();
    for (JsonNode partsForChunk : parts) {
      int i = 0;
      for (JsonNode fromTo : partsForChunk) {
        String[] posParts = fromTo.asText().split("-");
        int startPos = Integer.parseInt(posParts[0]);
        int endPos = Integer.parseInt(posParts[1]);
        AnalyzedTokenReadings atr = getAnalyzedTokenReadingsFor(startPos, endPos, tokenReadings);
        String tag = i == 0 ? "B-NP" : "I-NP";
        chunkTags.add(new ChunkTaggedToken(atr != null ? atr.getToken() : text.substring(startPos, endPos), Collections.singletonList(new ChunkTag(tag)), atr));
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
      if (tokenStart == startPos && tokenEnd == endPos) {
        return tokenReading;
      }
      pos = tokenEnd;
    }
    //System.out.println("No ATR found for " + startPos + "-" + endPos + " in " + tokenReadings);
    return null;
  }

}
