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
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.SimilarityScore;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ngrams.LanguageModelUtils;
import org.languagetool.rules.spelling.SuggestionChangesExperiment;
import org.languagetool.rules.spelling.SuggestionsChanges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class NewSuggestionsOrderer implements SuggestionsOrderer {

  private final Language language;
  private final LanguageModel languageModel;
  private final SuggestionChangesExperiment experiment;

  private static final Logger logger = LoggerFactory.getLogger(NewSuggestionsOrderer.class);

  public NewSuggestionsOrderer(Language lang, LanguageModel languageModel) {
    language = lang;
    this.languageModel = languageModel;
    this.experiment = SuggestionsChanges.getInstance().getCurrentExperiment();
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

    EditDistance<Integer> levenshteinDistance = new LevenshteinDistance(9);
    SimilarityScore<Double> jaroWrinklerDistance = new JaroWinklerDistance();
    List<Feature> features = new ArrayList<>(suggestions.size());

    for (String candidate : suggestions) {
      double prob1 = languageModel.getPseudoProbability(Collections.singletonList(candidate)).getProb();
      double prob3 = LanguageModelUtils.get3gramProbabilityFor(language, languageModel, startPos, sentence, candidate);
      double prob4 = 0.0; //LanguageModelUtils.get4gramProbabilityFor(language, languageModel, startPos, sentence, candidate);
      long wordCount = 0;// ((BaseLanguageModel) languageModel).getCount(candidate);
      int levenstheinDist = levenshteinDistance.apply(word, candidate);
      double jaroWrinklerDist = jaroWrinklerDistance.apply(word, candidate);

      features.add(new Feature(prob1, prob3, prob4, wordCount, levenstheinDist, jaroWrinklerDist, candidate));
    }
    features.sort(Feature::compareTo);
    logger.trace("Features for '%s' in '%s': %n", word, sentence.getText());
    features.stream().map(Feature::toString).forEach(logger::trace);
    //features
    //  .stream().limit(10).forEachOrdered(f -> System.out.printf("Probabilities of suggestion '%20s': P_3 %.20f P_4 %.20f C_1 %20d%n",
    //     f.getWord(), f.prob3gram, f.prob4gram, f.wordCount));
    List<String> reordered = features.stream().map(Feature::getWord).collect(Collectors.toList());

    return reordered;
  }


  class Feature implements Comparable<Feature>{
    private final double prob1gram;
    private final double prob3gram;
    private final double prob4gram;
    private final long wordCount;
    private final int levenshteinDistance;
    private final double jaroWrinklerDistance;
    private final String word;

    Feature(double prob1, double prob3, double prob4, long wordCount, int levenshteinDistance, double jaroWrinklerDistance, String word) {
      this.prob1gram = prob1;
      this.prob3gram = prob3;
      this.prob4gram = prob4;
      this.wordCount = wordCount;
      this.levenshteinDistance = levenshteinDistance;
      this.jaroWrinklerDistance = jaroWrinklerDistance;
      this.word = word;
    }

    public String getWord() {
      return word;
    }

    //\binom{n}{k}p^k(1-p)^{n-k}
    //\frac{n!}{k!(n-k)!}

    private int factorial(int n) {
      int factor = n;
      int result = 1;
      while (factor > 1) {
        result *= factor--;
      }
      return result;
    }

    private int binomialCoefficient(int n, int k) {
      return factorial(n) / (factorial(k) * factorial(n - k));
    }

    private double binomialProbability(double p, int n, int k) {
      return binomialCoefficient(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    private double getMeanProbability() {
      // TODO: test weighing unigrams less
      // TODO: test 4grams again
      double ngramProb = Math.log(prob1gram) + Math.log(prob3gram);// + Math.log(prob4gram);
      String score = (String) experiment.parameters.get("score");
      if ("ngrams+levensthein".equals(score)) {
        double mistakeProb = (double) experiment.parameters.get("levenstheinProb");
        double misspellingProb = Math.pow(mistakeProb, levenshteinDistance);
        return ngramProb + Math.log(misspellingProb);
      } else if ("ngrams".equals(score)) {
        return ngramProb;
      } else if ("ngrams+binomialLevensthein".equals(score)) {
        double mistakeProb = (double) experiment.parameters.get("binomialLevenstheinProb");
        double misspellingProb = binomialProbability(mistakeProb, word.length(), levenshteinDistance);
        return ngramProb + Math.log(misspellingProb);
      } else {
        throw new RuntimeException("Unknown scoring method: " + score);
      }
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

    @Override
    public String toString() {
      return "Feature{" +
        "prob1gram=" + prob1gram +
        ", prob3gram=" + prob3gram +
        ", prob4gram=" + prob4gram +
        ", wordCount=" + wordCount +
        ", levenshteinDistance=" + levenshteinDistance +
        ", jaroWrinklerDistance=" + jaroWrinklerDistance +
        ", word='" + word + '\'' +
        '}';
    }
  }

}
