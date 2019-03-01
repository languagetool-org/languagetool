/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules;

import org.languagetool.Experimental;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

/**
 * Rule match with fields for additional data to e.g.
 * feed into a classifier that estimates confidence for automatic correction
 */
@Experimental
public class ExtendedRuleMatch extends RuleMatch {
  private List<SortedMap<String, Float>> suggestedReplacementsMetadata = Collections.emptyList();
  private SortedMap<String, Float> ruleMatchMetadata = Collections.emptySortedMap();
  private List<Float> suggestionConfidence = Collections.emptyList();
  private boolean autoCorrect = false;

  public ExtendedRuleMatch(RuleMatch originalMatch) {
    super(originalMatch.getRule(), originalMatch.getSentence(), originalMatch.getFromPos(), originalMatch.getToPos(),
      originalMatch.getMessage(), originalMatch.getShortMessage());
    setUrl(originalMatch.getUrl());
    setType(originalMatch.getType());
    setSuggestedReplacements(originalMatch.getSuggestedReplacements());
  }

  public void setRuleMatchMetadata(SortedMap<String, Float> ruleMatchMetadata) {
    this.ruleMatchMetadata = ruleMatchMetadata;
  }

  public void setSuggestedReplacementsMetadata(List<SortedMap<String, Float>> suggestedReplacementsMetadata) {
    this.suggestedReplacementsMetadata = suggestedReplacementsMetadata;
  }

  public SortedMap<String, Float> getRuleMatchMetadata() {
    return ruleMatchMetadata;
  }

  public List<SortedMap<String, Float>> getSuggestedReplacementsMetadata() {
    return suggestedReplacementsMetadata;
  }

  public List<Float> getSuggestionConfidence() {
    return suggestionConfidence;
  }

  public void setSuggestionConfidence(List<Float> suggestionConfidence) {
    this.suggestionConfidence = suggestionConfidence;
  }

  public boolean isAutoCorrect() {
    return autoCorrect;
  }

  public void setAutoCorrect(boolean autoCorrect) {
    this.autoCorrect = autoCorrect;
  }
}
