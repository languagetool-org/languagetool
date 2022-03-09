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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;

import java.util.*;

public class RemoteRuleResult {
  private final boolean remote; // was remote needed/involved? rules may filter input sentences and only call remote on some; for metrics
  private final boolean success; // successful -> for caching, so that we can cache: remote not needed for this sentence
  private final List<RuleMatch> matches;
  private final Set<AnalyzedSentence> processedSentences;
  // which sentences were processed? to distinguish between no matches because not processed (e.g. cached)
  // and no errors/corrections found

  private final Map<AnalyzedSentence, List<RuleMatch>> sentenceMatches = new HashMap<>();

  public RemoteRuleResult(boolean remote, boolean success, List<RuleMatch> matches, List<AnalyzedSentence> processedSentences) {
    this.remote = remote;
    this.success = success;
    this.matches = matches;
    this.processedSentences = Collections.unmodifiableSet(new HashSet<>(processedSentences));

    for (RuleMatch match : matches) {
      sentenceMatches.compute(match.getSentence(), (sentence, ruleMatches) -> {
        if (ruleMatches == null) {
          return new ArrayList<>(Collections.singletonList(match));
        } else {
          ruleMatches.add(match);
          return ruleMatches;
        }
      });
    }
  }

  public boolean isRemote() {
    return remote;
  }

  public boolean isSuccess() {
    return success;
  }

  public List<RuleMatch> getMatches() {
    return matches;
  }

  public Set<AnalyzedSentence> matchedSentences() {
    return sentenceMatches.keySet();
  }

  public Set<AnalyzedSentence> processedSentences() {
    return processedSentences;
  }

  /**
   * get matches for a specific sentence
   * @param sentence sentence to look up
   * @return null if sentence not processed, else returned matches
   */
  @Nullable
  public List<RuleMatch> matchesForSentence(AnalyzedSentence sentence) {
    List<RuleMatch> defaultValue = processedSentences.contains(sentence) ?
      Collections.emptyList() : null;
    return sentenceMatches.getOrDefault(sentence, defaultValue);
  }
}
