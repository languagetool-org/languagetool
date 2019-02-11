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
import org.languagetool.Language;
import org.languagetool.languagemodel.BaseLanguageModel;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ngrams.LanguageModelUtils;

import java.util.*;
import java.util.stream.Collectors;

public class NewSuggestionsOrderer implements SuggestionsOrderer {

  private final Language language;
  private final LanguageModel languageModel;

  public NewSuggestionsOrderer(Language lang, LanguageModel languageModel) {
    language = lang;
    this.languageModel = languageModel;
  }

  @Override
  public boolean isMlAvailable() {
    return languageModel != null;
  }

  @Override
  public List<String> orderSuggestionsUsingModel(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos, int wordLength) {
    //List<AnalyzedTokenReadings> tokens = Arrays.asList(sentence.getTokensWithoutWhitespace());
    //Optional<AnalyzedTokenReadings> center =  tokens.stream()
    //  .filter(t -> !t.isSentenceStart() && !t.isWhitespace() && t.getStartPos() == startPos)
    //  .findFirst();
    //if (!center.isPresent()) {
    //  throw new RuntimeException(String.format("Could not find center token '%s' in sentence '%s'.", word, sentence));
    //}
    //int index = tokens.indexOf(center.get());
    //if (index == -1) {
    //  throw new RuntimeException(String.format("Could not find center token '%s' in sentence '%s'.", word, sentence));
    //}
    //List<String> words = tokens.stream().map(AnalyzedTokenReadings::getToken).collect(Collectors.toList());
    //List<String> candidates = corrector.getCandidates(words, index);
    //System.out.println(String.format("candidates for '%s' -> %s vs %s [%s @ %s]", word, candidates, suggestions, words.get(index), words));
    //return candidates;

    EditDistance<Integer> distance = LevenshteinDistance.getDefaultInstance();
    List<Feature> features = new ArrayList<>(suggestions.size());

    for (String candidate : suggestions) {
      double prob1 = languageModel.getPseudoProbability(Collections.singletonList(candidate)).getProb();
      double prob3 = LanguageModelUtils.get3gramProbabilityFor(language, languageModel, startPos, sentence, candidate);
      double prob4 = 0.0; //LanguageModelUtils.get4gramProbabilityFor(language, languageModel, startPos, sentence, candidate);
      long wordCount = 0;// ((BaseLanguageModel) languageModel).getCount(candidate);
      int dist = distance.apply(word, candidate);

      features.add(new Feature(prob1, prob3, prob4, wordCount, dist, candidate));
    }
    features.sort(Feature::compareTo);
    //features
    //  .stream().limit(10).forEachOrdered(f -> System.out.printf("Probabilities of suggestion '%20s': P_3 %.20f P_4 %.20f C_1 %20d%n",
    //     f.getWord(), f.prob3gram, f.prob4gram, f.wordCount));
    List<String> reordered = features.stream().map(Feature::getWord).collect(Collectors.toList());

    return reordered;
  }


  static class Feature implements Comparable<Feature>{
    private final double prob1gram;
    private final double prob3gram;
    private final double prob4gram;
    private final long wordCount;
    private final int levenshteinDistance;
    private final String word;

    Feature(double prob1, double prob3, double prob4, long wordCount, int levenshteinDistance, String word) {
      this.prob1gram = prob1;
      this.prob3gram = prob3;
      this.prob4gram = prob4;
      this.wordCount = wordCount;
      this.levenshteinDistance = levenshteinDistance;
      this.word = word;
    }

    public String getWord() {
      return word;
    }

    private double getMeanProbability() {
      double ngramProb = Math.log(prob1gram) + Math.log(prob3gram);// + Math.log(prob4gram);
      final double MISTAKE_PROB = 0.1;
      double misspellingProb = Math.pow(MISTAKE_PROB, levenshteinDistance);
      return ngramProb + Math.log(misspellingProb);
      //return prob3gram;
    }

    @Override
    public int compareTo(@NotNull Feature o) {
      // sort descending
      // maybe use threshold instead of 0
      return Double.compare(o.getMeanProbability(), this.getMeanProbability());
/*      if (this.wordCount == o.wordCount) {
        return 0;
      } else if (this.wordCount == 0) {
        return 1;
      } else if (o.wordCount == 0) {
        return -1;
      } else {
        return 0;
      }*/
    }
  }

}
