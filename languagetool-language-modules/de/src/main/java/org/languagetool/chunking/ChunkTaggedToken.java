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
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;

import java.util.List;
import java.util.Objects;

/**
 * @since 2.9
 */
class ChunkTaggedToken {

  private final String token;
  private final List<ChunkTag> chunkTags;
  private final AnalyzedTokenReadings readings;

  /**
   * @param readings may be null, caused by differences in tokenization we don't always have a 1:1 mapping
   */
  ChunkTaggedToken(String token, List<ChunkTag> chunkTags, AnalyzedTokenReadings readings) {
    this.token = Objects.requireNonNull(token);
    this.chunkTags = Objects.requireNonNull(chunkTags);
    this.readings = readings;
  }

  String getToken() {
    return token;
  }

  List<ChunkTag> getChunkTags() {
    return chunkTags;
  }

  /**
   * @return readings or {@code null}
   */
  @Nullable
  AnalyzedTokenReadings getReadings() {
    return readings;
  }

  @Override
  public String toString() {
    return token + '/' + StringUtils.join(chunkTags, ",");
  }
}
