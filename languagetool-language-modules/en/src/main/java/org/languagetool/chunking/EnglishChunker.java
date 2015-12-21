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

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OpenNLP-based chunker. Also uses the OpenNLP tokenizer and POS tagger and
 * maps the result to our own tokens (we have our own tokenizer), as far as trivially possible.
 * @since 2.3
 */
public class EnglishChunker implements Chunker {

  private static final String TOKENIZER_MODEL = "/en-token.bin";
  private static final String POS_TAGGER_MODEL = "/en-pos-maxent.bin";
  private static final String CHUNKER_MODEL = "/en-chunker.bin";

  /**
   * This needs to be static to save memory: as Language.LANGUAGES is static, any language
   * that is once created there will never be released. As English has several variants,
   * we'd have as many posModels etc. as we have variants -> huge waste of memory:
   */
  private static volatile TokenizerModel tokenModel;
  private static volatile POSModel posModel;
  private static volatile ChunkerModel chunkerModel;

  private final EnglishChunkFilter chunkFilter;

  public EnglishChunker() {
    try {
      if (tokenModel == null) {
        tokenModel = new TokenizerModel(Tools.getStream(TOKENIZER_MODEL));
      }
      if (posModel == null) {
        posModel = new POSModel(Tools.getStream(POS_TAGGER_MODEL));
      }
      if (chunkerModel == null) {
        chunkerModel = new ChunkerModel(Tools.getStream(CHUNKER_MODEL));
      }
      chunkFilter = new EnglishChunkFilter();
    } catch (IOException e) {
      throw new RuntimeException("Could not initialize English chunker", e);
    }
  }

  @Override
  public void addChunkTags(List<AnalyzedTokenReadings> tokenReadings) {
    List<ChunkTaggedToken> origChunkTags = getChunkTagsForReadings(tokenReadings);
    List<ChunkTaggedToken> chunkTags = chunkFilter.filter(origChunkTags);
    assignChunksToReadings(chunkTags);
  }

  private List<ChunkTaggedToken> getChunkTagsForReadings(List<AnalyzedTokenReadings> tokenReadings) {
    // these are not thread-safe, so create them here, not as members:
    String sentence = getSentence(tokenReadings);
    String[] tokens = tokenize(sentence);
    String[] posTags = posTag(tokens);
    String[] chunkTags = chunk(tokens, posTags);
    if (tokens.length != posTags.length || tokens.length != chunkTags.length) {
      throw new RuntimeException("Length of results must be the same: " + tokens.length + ", " + posTags.length + ", " + chunkTags.length);
    }
    return getTokensWithTokenReadings(tokenReadings, tokens, chunkTags);
  }

  // non-private for test cases
  String[] tokenize(String sentence) {
    TokenizerME tokenizer = new TokenizerME(tokenModel);
    String cleanString = sentence.replace('’', '\'');  // this is the type of apostrophe that OpenNLP expects
    return tokenizer.tokenize(cleanString);
  }

  private String[] posTag(String[] tokens) {
    POSTaggerME posTagger = new POSTaggerME(posModel);
    return posTagger.tag(tokens);
  }

  private String[] chunk(String[] tokens, String[] posTags) {
    ChunkerME chunker = new ChunkerME(chunkerModel);
    return chunker.chunk(tokens, posTags);
  }

  private List<ChunkTaggedToken> getTokensWithTokenReadings(List<AnalyzedTokenReadings> tokenReadings, String[] tokens, String[] chunkTags) {
    List<ChunkTaggedToken> result = new ArrayList<>();
    int i = 0;
    int pos = 0;
    for (String chunkTag : chunkTags) {
      int startPos = pos;
      int endPos = startPos + tokens[i].length();
      //System.out.println("OPEN: " + tokens[i]);
      AnalyzedTokenReadings readings = getAnalyzedTokenReadingsFor(startPos, endPos, tokenReadings);
      result.add(new ChunkTaggedToken(tokens[i], Collections.singletonList(new ChunkTag(chunkTag)), readings));
      pos = endPos;
      i++;
    }
    return result;
  }

  private void assignChunksToReadings(List<ChunkTaggedToken> chunkTaggedTokens) {
    for (ChunkTaggedToken taggedToken : chunkTaggedTokens) {
      AnalyzedTokenReadings readings = taggedToken.getReadings();
      if (readings != null) {
        readings.setChunkTags(taggedToken.getChunkTags());
      }
    }
  }

  private String getSentence(List<AnalyzedTokenReadings> sentenceTokens) {
    StringBuilder sb = new StringBuilder();
    for (AnalyzedTokenReadings token : sentenceTokens) {
      sb.append(token.getToken());
    }
    return sb.toString();
  }

  // Get only exact position matches - i.e. this can only be used for a trivial mapping
  // where tokens that are not exactly at the same position will be skipped. For example,
  // the tokens of "I'll" ([I] ['ll] vs [I]['][ll) cannot be mapped with this.
  @Nullable
  private AnalyzedTokenReadings getAnalyzedTokenReadingsFor(int startPos, int endPos, List<AnalyzedTokenReadings> tokenReadings) {
    int pos = 0;
    for (AnalyzedTokenReadings tokenReading : tokenReadings) {
      String token = tokenReading.getToken();
      if (token.trim().isEmpty()) {
        continue;  // the OpenNLP result has no whitespace, so we need to skip it
      }
      int tokenStart = pos;
      int tokenEnd = pos + token.length();
      if (tokenStart == startPos && tokenEnd == endPos) {
        //System.out.println("!!!" + startPos + " " + endPos + "  " + tokenReading);
        return tokenReading;
      }
      pos = tokenEnd;
    }
    return null;
  }

}
