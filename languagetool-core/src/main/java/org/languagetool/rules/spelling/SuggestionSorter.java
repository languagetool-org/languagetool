/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling;

import org.jetbrains.annotations.NotNull;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ngrams.Probability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Sort spell checking suggestions by their probability.
 * @deprecated still work in progress
 * @since 3.9
 */
public class SuggestionSorter {

  private LanguageModel lm;

  public SuggestionSorter(LanguageModel lm) {
    this.lm = Objects.requireNonNull(lm);
  }

  public List<String> sortSuggestions(List<String> suggestions) {
    List<RankedSuggestion> ranked = new ArrayList<>();
    for (String suggestion : suggestions) {
      // TODO: consider context
      // TODO: consider phrases
      Probability p = lm.getPseudoProbability(Collections.singletonList(suggestion));
      ranked.add(new RankedSuggestion(p.getProb(), suggestion));
    }
    Collections.sort(ranked);
    /*for (RankedSuggestion rankedSuggestion : ranked) {
      System.out.println(rankedSuggestion.prob + " " + rankedSuggestion.suggestion);
    }*/
    return ranked.stream().map(k -> k.suggestion).collect(toList());
  }
  
  class RankedSuggestion implements Comparable<RankedSuggestion> {
    private final double prob;
    private final String suggestion;
    
    RankedSuggestion(double prob, String suggestion) {
      this.prob = prob;
      this.suggestion = suggestion;
    }

    @Override
    public int compareTo(@NotNull RankedSuggestion o) {
      return Double.compare(o.prob, prob);
    }
  }

}
