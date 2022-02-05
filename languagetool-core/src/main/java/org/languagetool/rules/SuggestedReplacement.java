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
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * @since 4.5
 */
public class SuggestedReplacement {

  private String replacement;
  private String shortDescription;
  private String suffix;
  private SortedMap<String, Float> features = Collections.emptySortedMap();
  private Float confidence = null;
  private SuggestionType type = SuggestionType.Default;

  /**
    classify the type of the suggestion
    so that downstream tasks (e.g. resorting, as in {@link BERTSuggestionRanking})
    can treat them accordingly
    Default - default, no special treatment
    Translation - offers to translate words from native language into text language;
      suggestion ranking extends size of list of candidates
    Curated - a manually curated suggestion / special case / ...; don't resort
   */
  public enum SuggestionType {
    Default, Translation, Curated
  }

  public SuggestedReplacement(String replacement) {
    this(replacement, null, null);
  }

  public SuggestedReplacement(String replacement, String shortDescription) {
    this(replacement, shortDescription, null);
  }

  public SuggestedReplacement(String replacement, String shortDescription, String suffix) {
    this.replacement = Objects.requireNonNull(replacement);
    this.shortDescription = shortDescription;
    this.suffix = suffix;
  }

  public SuggestedReplacement(SuggestedReplacement repl) {
    this.replacement = repl.replacement;
    this.suffix = repl.suffix;
    setShortDescription(repl.getShortDescription());
    setConfidence(repl.getConfidence());
    setFeatures(repl.getFeatures());
    setType(repl.getType());
  }

  public String getReplacement() {
    return replacement;
  }

  public void setReplacement(String replacement) {
    this.replacement = Objects.requireNonNull(replacement);
  }

  @Nullable
  public String getShortDescription() {
    return shortDescription;
  }

  public void setShortDescription(String desc) {
    this.shortDescription = desc;
  }

  /** @since 4.9 */
  public void setType(SuggestionType type) {
    this.type = Objects.requireNonNull(type);
  }

  /** @since 4.9 */
  @NotNull
  public SuggestionType getType() {
    return type;
  }

  /**
   * Value shown in the UI after the replacement (but not part of it).
   */
  @Nullable
  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String val) {
    this.suffix = val;
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

  public static List<SuggestedReplacement> convert(List<String> suggestions) {
    return suggestions.stream().map(SuggestedReplacement::new).collect(Collectors.toList());
  }

  public static List<SuggestedReplacement> topMatch(String word) {
    return topMatch(word, null);
  }

  public static List<SuggestedReplacement> topMatch(String word, String shortDesc) {
    SuggestedReplacement sugg = new SuggestedReplacement(word, shortDesc);
    sugg.setConfidence(SpellingCheckRule.HIGH_CONFIDENCE);
    return singletonList(sugg);
  }

}
