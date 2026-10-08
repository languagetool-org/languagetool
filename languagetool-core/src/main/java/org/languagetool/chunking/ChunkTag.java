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

import java.util.regex.Pattern;

/**
 * The name of a chunk. Just a string - this class exists mostly for better type safety.
 * @since 2.3
 */
public class ChunkTag {

  private final String chunkTag;
  private final boolean isRegexp;
  private final Pattern pattern;  // only for regexp chunk tags

  public ChunkTag(String chunkTag) {
    this(chunkTag, false);
  }

  /** @since 5.1 */
  public ChunkTag(String chunkTag, boolean isRegexp) {
    if (chunkTag == null || chunkTag.trim().isEmpty()) {
      throw new IllegalArgumentException("chunkTag cannot be null or empty: '" + chunkTag + "'");
    }
    this.chunkTag = chunkTag;
    this.isRegexp = isRegexp;
    this.pattern = isRegexp ? Pattern.compile(chunkTag) : null;
  }

  public String getChunkTag() {
    return chunkTag;
  }

  /** @since 5.1 */
  public boolean isRegexp() {
    return isRegexp;
  }

  /**
   * For regexp chunk tags: whether the given chunk tag fully matches this regular expression
   * (same as {@code tag.getChunkTag().matches(getChunkTag())}, but without compiling the regex each time).
   * For non-regexp chunk tags: whether the given chunk tag equals this one.
   * @since 6.9
   */
  public boolean matches(ChunkTag tag) {
    return pattern != null ? pattern.matcher(tag.getChunkTag()).matches() : chunkTag.equals(tag.getChunkTag());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChunkTag other = (ChunkTag) o;
    return chunkTag.equals(other.chunkTag);
  }

  @Override
  public int hashCode() {
    return chunkTag.hashCode();
  }

  @Override
  public String toString() {
    return chunkTag + (isRegexp ? "[regex]" : "");
  }
}
