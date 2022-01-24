/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
 * A range in a text with its (potential/guessed) language.
 * @since 5.3
 */
public class Range {

  private final int fromPos;
  private final int toPos;
  private final String lang;

  Range(int fromPos, int toPos, String lang) {
    this.fromPos = fromPos;
    this.toPos = toPos;
    this.lang = Objects.requireNonNull(lang);
  }

  public int getFromPos() {
    return fromPos;
  }

  public int getToPos() {
    return toPos;
  }

  public String getLang() {
    return lang;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Range range = (Range) o;
    return fromPos == range.fromPos && toPos == range.toPos && lang.equals(range.lang);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fromPos, toPos, lang);
  }
}
