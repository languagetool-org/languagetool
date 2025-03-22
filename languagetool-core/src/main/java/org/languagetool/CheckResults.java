/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (www.danielnaber.de)
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

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.languagetool.rules.RuleMatch;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @since 5.3
 */
public class CheckResults {

  @Getter
  private List<RuleMatch> ruleMatches;
  @Getter
  private final List<Range> ignoredRanges;
  @Getter
  private final List<ExtendedSentenceRange> extendedSentenceRanges;
  private final List<SentenceRange> sentenceRanges = new ArrayList<>();


  public CheckResults(List<RuleMatch> ruleMatches, List<Range> ignoredRanges) {
    this(ruleMatches, ignoredRanges, Collections.emptyList());
  }

  public CheckResults(List<RuleMatch> ruleMatches, List<Range> ignoredRanges, List<ExtendedSentenceRange> extendedSentenceRanges) {
    this.ruleMatches = Objects.requireNonNull(ruleMatches);
    this.ignoredRanges = Objects.requireNonNull(ignoredRanges);
    this.extendedSentenceRanges = Objects.requireNonNull(extendedSentenceRanges.stream().sorted().collect(Collectors.toList()));
    //TODO: use this later, when we are sure the sentenceRanges (from extendedSentenceRange) are are correct.
    // Right now the sentenceRanges are calculated different from those in extendedSentenceRange.
    // extendedSentenceRanges.forEach(extendedSentenceRange -> this.sentenceRanges.add(new SentenceRange(extendedSentenceRange.getFromPos(), extendedSentenceRange.getToPos())));
  }

  @NotNull
  public List<SentenceRange> getSentenceRanges() {
    return Collections.unmodifiableList(this.sentenceRanges);
  }

  public void addSentenceRanges(List<SentenceRange> sentenceRanges) {
    this.sentenceRanges.addAll(sentenceRanges);
  }

  public void setRuleMatches(List<RuleMatch> ruleMatches) {
    this.ruleMatches = Objects.requireNonNull(ruleMatches);
  }
}
