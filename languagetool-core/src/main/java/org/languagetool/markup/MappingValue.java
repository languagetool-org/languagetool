/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.markup;

/**
 * A fake markup data object giving information on the impact of the fake markup.
 * Use {@link AnnotatedTextBuilder} to create objects of this type.
 * @since 4.8
 */
class MappingValue {

  private final int totalPosition;
  private final int fakeMarkupLength;

  MappingValue(int totalPosition) {
    this(totalPosition, 0);
  }

  MappingValue(int totalPosition, int fakeMarkupLength) {
    this.totalPosition = totalPosition;
    this.fakeMarkupLength = fakeMarkupLength;
  }

  int getTotalPosition() {
    return totalPosition;
  }

  int getFakeMarkupLength() {
    return fakeMarkupLength;
  }

  @Override
  public String toString() {
    return "totalPos:" + totalPosition + ",fakeMarkupLen=" + fakeMarkupLength;
  }
}
