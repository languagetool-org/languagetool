package org.languagetool.chunking;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedTokenReadings;

import java.util.List;

public class ComparableChunkTaggedToken extends ChunkTaggedToken implements Comparable<ComparableChunkTaggedToken> {

  private final int startPos;

  public ComparableChunkTaggedToken(String token, List<ChunkTag> chunkTags, AnalyzedTokenReadings readings, int startPos) {
    super(token, chunkTags, readings);
    this.startPos = startPos;
  }

  @Override
  public int compareTo(@NotNull ComparableChunkTaggedToken o) {
    return startPos - o.startPos;
  }

  public int getStartPos() {
    return startPos;
  }
}
