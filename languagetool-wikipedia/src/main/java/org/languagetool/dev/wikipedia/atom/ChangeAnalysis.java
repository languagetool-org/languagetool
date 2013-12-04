/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia.atom;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * The results of the check of the changes in one article.
 * @since 2.4
 */
class ChangeAnalysis {
  
  private final String title;
  private final long diffId;
  private final List<WikipediaRuleMatch> oldMatches;
  private final List<WikipediaRuleMatch> newMatches;

  ChangeAnalysis(String title, long diffId, List<WikipediaRuleMatch> oldMatches, List<WikipediaRuleMatch> newMatches) {
    this.title = Objects.requireNonNull(title);
    this.diffId = Objects.requireNonNull(diffId);
    this.oldMatches = Objects.requireNonNull(oldMatches);
    this.newMatches = Objects.requireNonNull(newMatches);
  }

  String getTitle() {
    return title;
  }

  long getDiffId() {
    return diffId;
  }

  List<WikipediaRuleMatch> getAddedMatches() {
    List<Delta> deltas = getDeltas();
    return getWikipediaRuleMatches(deltas, Delta.TYPE.INSERT);
  }

  List<WikipediaRuleMatch> getRemovedMatches() {
    List<Delta> deltas = getDeltas();
    return getWikipediaRuleMatches(deltas, Delta.TYPE.DELETE);
  }

  private List<WikipediaRuleMatch> getWikipediaRuleMatches(List<Delta> deltas, Delta.TYPE changeType) {
    List<WikipediaRuleMatch> matches = new ArrayList<>();
    for (Delta delta : deltas) {
      if (delta.getType().equals(changeType)) {
        List<?> lines = changeType == Delta.TYPE.INSERT ? delta.getRevised().getLines() : delta.getOriginal().getLines();
        matches.addAll((Collection<WikipediaRuleMatch>) lines);
      }
    }
    return matches;
  }

  private List<Delta> getDeltas() {
    Patch diff = DiffUtils.diff(oldMatches, newMatches);
    return diff.getDeltas();
  }

  @Override
  public String toString() {
    return "ChangeAnalysis{title=" + title + ",diffId=" + diffId + "}";
  }
  
}
