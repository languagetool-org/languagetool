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
package org.languagetool.rules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;

/**
 * @since 4.5
 */
public class SuggestedReplacement {

  private String replacement;
  private String shortDescription;
  private SortedMap<String, Float> features = Collections.emptySortedMap();
  private Float confidence = null;
  public SuggestedReplacement(String replacement) {
    this.replacement = Objects.requireNonNull(replacement);
  }

  public SuggestedReplacement(SuggestedReplacement clone) {
    this.replacement = clone.replacement;
    setShortDescription(clone.getShortDescription());
    setConfidence(clone.getConfidence());
    setFeatures(clone.getFeatures());
  }

  public String getReplacement() {
    return replacement;
  }

  public void setReplacement(String replacement) {
    this.replacement = Objects.requireNonNull(replacement);
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public void setShortDescription(String desc) {
    this.shortDescription = desc;
  }

  @Override
  public String toString() {
    return replacement + '(' + shortDescription + ')';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SuggestedReplacement that = (SuggestedReplacement) o;
    return replacement.equals(that.replacement) &&
            Objects.equals(shortDescription, that.shortDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(replacement, shortDescription);
  }

  @Nullable
  public Float getConfidence() {
    return confidence;
  }

  public void setConfidence(@Nullable Float confidence) {
    this.confidence = confidence;
  }

  @NotNull
  public SortedMap<String, Float> getFeatures() {
    return Collections.unmodifiableSortedMap(features);
  }

  public void setFeatures(@NotNull SortedMap<String, Float> features) {
    this.features = features;
  }
}
