/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2023.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ExtendedSentenceRange implements Comparable<ExtendedSentenceRange> {

  @Getter
  private final int fromPos;
  @Getter
  private final int toPos;
  @Getter
  private final Map<String, Float> languageConfidenceRates; //languageCode;0-1 confidenceRate from LanguageDetectionService

  ExtendedSentenceRange(int fromPos, int toPos, String languageCode) {
    this(fromPos, toPos, Collections.singletonMap(languageCode, 1.0f));
  }

  ExtendedSentenceRange(int fromPos, int toPos, @NotNull Map<String, Float> languageConfidenceRates) {
    this.fromPos = fromPos;
    this.toPos = toPos;
    this.languageConfidenceRates = new LinkedHashMap<>(languageConfidenceRates);
  }

  public void updateLanguageConfidenceRates(@NotNull Map<String, Float> languageConfidenceRates) {
    this.languageConfidenceRates.clear();
    this.languageConfidenceRates.putAll(languageConfidenceRates);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExtendedSentenceRange extendedSentenceRange = (ExtendedSentenceRange) o;
    return fromPos == extendedSentenceRange.fromPos && toPos == extendedSentenceRange.toPos;
  }

  @Override
  public int hashCode() {
    int result = fromPos;
    result = 31 * result + toPos;
    return result;
  }

  @Override
  public String toString() {
    return fromPos + "-" + toPos + ":" + languageConfidenceRates;
  }

  @Override
  public int compareTo(@NotNull ExtendedSentenceRange o) {
    return Integer.compare(this.fromPos, o.fromPos);
  }
}
