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

import org.languagetool.AnalyzedToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Our chunker detects noun phrases but not whether they are singular
 * or plural noun phrases. We add this information here.
 * @since 2.3
 */
class EnglishChunkFilter {

  private static final ChunkTag BEGIN_NOUN_PHRASE_TAG = new ChunkTag("B-NP");
  private static final ChunkTag IN_NOUN_PHRASE_TAG = new ChunkTag("I-NP");

  enum ChunkType { SINGULAR, PLURAL }

  List<ChunkTaggedToken> filter(List<ChunkTaggedToken> tokens) {
    List<ChunkTaggedToken> result = new ArrayList<>();
    String newChunkTag = null;
    int i = 0;
    for (ChunkTaggedToken taggedToken : tokens) {
      if (isBeginningOfNounPhrase(taggedToken)) {
        ChunkType chunkType = getChunkType(tokens, i);
        if (chunkType == ChunkType.SINGULAR) {
          result.add(new ChunkTaggedToken(taggedToken.getToken(), new ChunkTag("B-NP-singular"), taggedToken.getReadings()));
          newChunkTag = "I-NP-singular";
        } else if (chunkType == ChunkType.PLURAL) {
          result.add(new ChunkTaggedToken(taggedToken.getToken(), new ChunkTag("B-NP-plural"), taggedToken.getReadings()));
          newChunkTag = "I-NP-plural";
        } else {
          throw new IllegalStateException("Unknown chunk type: " + chunkType);
        }
      } else if (isContinuationOfNounPhrase(taggedToken)) {
        if (newChunkTag != null) {
          result.add(new ChunkTaggedToken(taggedToken.getToken(), new ChunkTag(newChunkTag), taggedToken.getReadings()));
        } else {
          result.add(taggedToken);
        }
      } else {
        // NP ends here
        newChunkTag = null;
        result.add(taggedToken);
      }
      i++;
    }
    return result;
  }

  private boolean isBeginningOfNounPhrase(ChunkTaggedToken taggedToken) {
    return BEGIN_NOUN_PHRASE_TAG.equals(taggedToken.getChunkTag());
  }

  private boolean isContinuationOfNounPhrase(ChunkTaggedToken taggedToken) {
    return IN_NOUN_PHRASE_TAG.equals(taggedToken.getChunkTag());
  }

  /**
   * Get the type of the chunk that starts at the given position.
   */
  private ChunkType getChunkType(List<ChunkTaggedToken> tokens, int chunkStartPos) {
    boolean isPlural = false;
    for (int i = chunkStartPos; i < tokens.size(); i++) {
      ChunkTaggedToken token = tokens.get(i);
      if (false && "and".equals(token.getToken())) {   // e.g. "Tarzan and Jane" is a plural noun phrase
        // TODO: "Additionally, there are over 500 college and university chapter."
        isPlural = true;
      } else if (hasNounWithPluralReading(token)) {   // e.g. "ten books" is a plural noun phrase
        isPlural = true;
      }
      if (!isBeginningOfNounPhrase(token) && !isContinuationOfNounPhrase(token)) {
        break;
      }
    }
    return isPlural ? ChunkType.PLURAL : ChunkType.SINGULAR;
  }

  private boolean hasNounWithPluralReading(ChunkTaggedToken token) {
    if (token.getReadings() != null) {
      for (AnalyzedToken analyzedToken : token.getReadings().getReadings()) {
        if ("NNS".equals(analyzedToken.getPOSTag())) {
          return true;
        }
      }
    }
    return false;
  }

}
