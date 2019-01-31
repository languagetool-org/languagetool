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

package org.languagetool.rules.spelling.morfologik.suggestions_ordering;

import org.apache.commons.text.similarity.EditDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ngrams.Probability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NewSuggestionsOrderer implements SuggestionsOrderer {

  private final LanguageModel languageModel;

  public NewSuggestionsOrderer(LanguageModel languageModel) {
    this.languageModel = languageModel;
  }

  @Override
  public boolean isMlAvailable() {
    return languageModel != null;
  }

  @Override
  public List<String> orderSuggestionsUsingModel(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos, int wordLength) {
    List<AnalyzedTokenReadings> tokens = Arrays.asList(sentence.getTokensWithoutWhitespace());
    AnalyzedTokenReadings center = tokens.stream()
      .filter(token -> token.getStartPos() == startPos && !token.isSentenceStart()).findFirst().orElse(null);
    if (center == null) {
      System.err.printf("Could not find context to order suggestions: %s @ %d in '%s'%n", word, startPos, sentence);
      return suggestions;
    }
    System.out.printf("Center token: %s %n", center);
    int idx = tokens.indexOf(center);
    int contextSize = 1;
    //List<AnalyzedTokenReadings> left = tokens.subList(Math.max(0, idx-contextSize), idx);
    //List<AnalyzedTokenReadings> right = tokens.subList(idx+1, Math.min(idx+1+contextSize, tokens.size()));
    //System.out.printf("Context for suggestions for '%s' out of %s: %s / %s # %s%n", word, suggestions,
    //  left.stream().map(AnalyzedTokenReadings::getToken).collect(Collectors.toList()),
    //  right.stream().map(AnalyzedTokenReadings::getToken).collect(Collectors.toList()),
    //  tokens.stream().map(AnalyzedTokenReadings::getToken).collect(Collectors.toList()));

    List<String> words = tokens.stream().map(AnalyzedTokenReadings::getToken).collect(Collectors.toList());
    // given sentence w_-2 w_-1 w_0 w_1 w_2, contextSize = 1
    //
    List<String> contextCenter = words.subList(Math.max(0, idx-contextSize), Math.min(idx+1+contextSize, tokens.size()));
    int contextCenterIdx = idx - Math.max(0, idx-contextSize);
    List<String> contextLeft = words.subList(Math.max(0, idx-contextSize-1), Math.min(idx+contextSize, tokens.size()));
    List<String> contextRight = words.subList(Math.max(0, idx-contextSize+1), Math.min(idx+2+contextSize, tokens.size()));
    int contextLeftIdx = contextLeft.size() - 1;
    int contextRightIdx = 0;
    System.out.printf("Context for suggestions for '%s' : L %s C %s R %s # %s%n",
      word, contextLeft, contextCenter, contextRight, sentence.getText());

    EditDistance<Integer> distance = LevenshteinDistance.getDefaultInstance();
    List<Feature> features = new ArrayList<>(suggestions.size());

    // TODO: check influence of empty strings contained here
/*    contextLeft = contextLeft.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());;
    contextRight = contextRight.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());;
    contextCenter = contextCenter.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());;
    System.out.printf("Context (filtered) for suggestions for '%s' : L %s C %s R %s # %s%n",
      word, contextLeft, contextCenter, contextRight, sentence.getText());*/

    for (String candidate : suggestions) {
      contextCenter.set(contextCenterIdx, candidate);
      contextLeft.set(contextLeftIdx, candidate);
      contextRight.set(contextRightIdx, candidate);
      Probability probC = languageModel.getPseudoProbability(contextCenter);
      Probability probL = languageModel.getPseudoProbability(contextLeft);
      Probability probR = languageModel.getPseudoProbability(contextRight);
      //System.out.printf("Probabilities of suggestion '%s' : L %f C %f R %f%n",
      //  candidate, probL.getProb(), probC.getProb(), probR.getProb());
      int dist = distance.apply(word, candidate);
      features.add(new Feature(probC, probL, probR, dist, candidate));
    }
    features.sort(Feature::compareTo);
    List<String> reordered = features.stream().map(Feature::getWord).collect(Collectors.toList());

    return reordered;
  }


  static class Feature implements Comparable<Feature>{
    private final Probability probabilityC;
    private final Probability probabilityL;
    private final Probability probabilityR;
    private final int levenshteinDistance;
    private final String word;

    Feature(Probability probabilityC, Probability probabilityL, Probability probabilityR, int levenshteinDistance, String word) {
      this.probabilityC = probabilityC;
      this.probabilityL = probabilityL;
      this.probabilityR = probabilityR;
      this.levenshteinDistance = levenshteinDistance;
      this.word = word;
    }

    public String getWord() {
      return word;
    }

    private double getMeanProbability() {
      //return (probabilityC.getProb() + probabilityL.getProb() + probabilityR.getProb()) / 3.0;
      return probabilityC.getProb();
    }

    @Override
    public int compareTo(@NotNull Feature o) {
      // sort descending
      return Double.compare(o.getMeanProbability(), this.getMeanProbability());
    }
  }

}
