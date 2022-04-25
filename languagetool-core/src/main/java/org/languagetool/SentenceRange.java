/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import java.util.Objects;

/**
 * A range in a text that makes up a sentence.
 * @since 5.8
 */
public class SentenceRange {

  private final int fromPos;
  private final int toPos;

  SentenceRange(int fromPos, int toPos) {
    this.fromPos = fromPos;
    this.toPos = toPos;
  }

  public int getFromPos() {
    return fromPos;
  }

  public int getToPos() {
    return toPos;
  }

  @Override
  public String toString() {
    return fromPos + "-" + toPos;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SentenceRange range = (SentenceRange) o;
    return fromPos == range.fromPos && toPos == range.toPos;
  }

  @Override
  public int hashCode() {
    return Objects.hash(fromPos, toPos);
  }
}
