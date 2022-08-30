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
      List<ChunkTag> chunkTags = new ArrayList<>();
      if (isBeginningOfNounPhrase(taggedToken)) {
        ChunkType chunkType = getChunkType(tokens, i);
        if (chunkType == ChunkType.SINGULAR || endOfNounPhraseIsSingular(tokens, i)) {
          chunkTags.add(new ChunkTag("B-NP-singular"));
          newChunkTag = "NP-singular";
        } else if (chunkType == ChunkType.PLURAL) {
          chunkTags.add(new ChunkTag("B-NP-plural"));
          newChunkTag = "NP-plural";
        } else {
          throw new IllegalStateException("Unknown chunk type: " + chunkType);
        }
      }
      if (newChunkTag != null && isEndOfNounPhrase(tokens, i)) {
        chunkTags.add(new ChunkTag("E-" + newChunkTag));
        newChunkTag = null;
      }
      if (newChunkTag != null && isContinuationOfNounPhrase(taggedToken)) {
        chunkTags.add(new ChunkTag("I-" + newChunkTag));
      }
      if (chunkTags.size() > 0) {
        result.add(new ChunkTaggedToken(taggedToken.getToken(), chunkTags, taggedToken.getReadings()));
      } else {
        result.add(taggedToken);
      }
      i++;
    }
    return result;
  }

  private boolean endOfNounPhraseIsSingular(List<ChunkTaggedToken> tokens, int i) {
    for (int j = i; j < tokens.size(); j++) {
      if (isEndOfNounPhrase(tokens, j)) {
        return getChunkType(tokens, j) == ChunkType.SINGULAR;
      }
    }
    return false;
  }

  private boolean isBeginningOfNounPhrase(ChunkTaggedToken taggedToken) {
    return taggedToken.getChunkTags().contains(BEGIN_NOUN_PHRASE_TAG);
  }

  private boolean isEndOfNounPhrase(List<ChunkTaggedToken> tokens, int i) {
    if (i > tokens.size() - 2) {
      return true;
    }
    if (!isContinuationOfNounPhrase(tokens.get(i + 1))) {
      return true;
    }
    return false;
  }

  private boolean isContinuationOfNounPhrase(ChunkTaggedToken taggedToken) {
    return taggedToken.getChunkTags().contains(IN_NOUN_PHRASE_TAG);
  }

  /**
   * Get the type of the chunk that starts at the given position.
   */
  private ChunkType getChunkType(List<ChunkTaggedToken> tokens, int chunkStartPos) {
    boolean isPlural = false;
    for (int i = chunkStartPos; i < tokens.size(); i++) {
      ChunkTaggedToken token = tokens.get(i);
      if (!isBeginningOfNounPhrase(token) && !isContinuationOfNounPhrase(token)) {
        break;
      }
      if (false && "and".equals(token.getToken())) {   // e.g. "Tarzan and Jane" is a plural noun phrase
        // TODO: "Additionally, there are over 500 college and university chapter."
        isPlural = true;
      } else if (hasNounWithPluralReading(token)) {   // e.g. "ten books" is a plural noun phrase
        isPlural = true;
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
