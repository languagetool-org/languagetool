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
package org.languagetool.rules;

import java.util.Objects;

/**
 * @since 2.9
 */
class MatchPosition {

  private final int start;
  private final int end;

  MatchPosition(int start, int end) {
    this.start = start;
    this.end = end;
  }

  int getStart() {
    return start;
  }

  int getEnd() {
    return end;
  }

  @Override
  public String toString() {
    return start + "-" + end;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MatchPosition other = (MatchPosition) o;
    return Objects.equals(start, other.start) && Objects.equals(end, other.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end);
  }

}
