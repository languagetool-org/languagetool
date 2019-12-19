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
package org.languagetool.rules.spelling.morfologik;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class WeightedSuggestion implements Comparable<WeightedSuggestion> {

  private final String word;
  private final int weight;

  WeightedSuggestion(String word, int weight) {
    this.word = Objects.requireNonNull(word);
    this.weight = weight;
  }

  String getWord() {
    return word;
  }

  int getWeight() {
    return weight;
  }

  @Override
  public int compareTo(@NotNull WeightedSuggestion o) {
    return Integer.compare(this.weight, o.getWeight());
  }

  @Override
  public String toString() {
    return word + "/" + weight;
  }
}
