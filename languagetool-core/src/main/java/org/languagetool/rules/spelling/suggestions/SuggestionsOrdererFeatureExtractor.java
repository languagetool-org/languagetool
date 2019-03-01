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

package org.languagetool.rules.spelling.suggestions;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.SimilarityScore;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.languagemodel.BaseLanguageModel;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.ngrams.LanguageModelUtils;
import org.languagetool.rules.spelling.morfologik.suggestions_ordering.DetailedDamerauLevenstheinDistance;
import org.languagetool.rules.spelling.symspell.implementation.EditDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes features that can be stored for training a model to rank suggestions,
 * or when using the same model for prediction
 * Used directly by SuggestionChangesTest for the former,
 * and as a superclass for the latter
 */
public class SuggestionsOrdererFeatureExtractor implements SuggestionsOrderer {

  private static final Logger logger = LoggerFactory.getLogger(SuggestionsOrdererFeatureExtractor.class);

  protected final Language language;
  protected final LanguageModel languageModel;

  protected int topN = -1;
  protected String score;
  protected double mistakeProb;


  public SuggestionsOrdererFeatureExtractor(Language lang, LanguageModel languageModel) {
    language = lang;
    this.languageModel = languageModel;
    initParameters();
  }

  protected void initParameters() {
    SuggestionChangesExperiment experiment = SuggestionsChanges.getInstance().getCurrentExperiment();
    topN = (Integer) experiment.parameters.getOrDefault("topN", -1);
    score = (String) experiment.parameters.get("score");
    mistakeProb = (double) experiment.parameters.getOrDefault("levenstheinProb", 1.0);
  }

  @Override
  public boolean isMlAvailable() {
    return languageModel != null;
  }

  @Override
  public List<SuggestedReplacement> orderSuggestions(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos) {
    return computeFeatures(suggestions, word, sentence, startPos).getLeft();
  }

  /**
   * compute features for training or prediction of a ranking model for suggestions
   * @param suggestions
   * @param word
   * @param sentence
   * @param startPos
   * @return correction candidates, features for the match in general, features specific to candidates
   */
  public Pair<List<SuggestedReplacement>, SortedMap<String, Float>> computeFeatures(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos) {
    if (suggestions.isEmpty()) {
      return Pair.of(Collections.emptyList(), Collections.emptySortedMap());
    }
    if (topN <= 0) {
      topN = suggestions.size();
    }
    List<String> topSuggestions = suggestions.subList(0, Math.min(suggestions.size(), topN));
    //EditDistance<Integer> levenshteinDistance = new LevenshteinDistance(4);
    EditDistance levenstheinDistance = new EditDistance(word, EditDistance.DistanceAlgorithm.Damerau);
    SimilarityScore<Double> jaroWrinklerDistance = new JaroWinklerDistance();
    List<Feature> features = new ArrayList<>(topSuggestions.size());

    for (String candidate : topSuggestions) {
      double prob1 = languageModel.getPseudoProbability(Collections.singletonList(candidate)).getProb();
      double prob3 = LanguageModelUtils.get3gramProbabilityFor(language, languageModel, startPos, sentence, candidate);
      //double prob4 = LanguageModelUtils.get4gramProbabilityFor(language, languageModel, startPos, sentence, candidate);
      long wordCount = ((BaseLanguageModel) languageModel).getCount(candidate);
      int levenstheinDist = levenstheinDistance.compare(candidate, 3);
      double jaroWrinklerDist = jaroWrinklerDistance.apply(word, candidate);
      DetailedDamerauLevenstheinDistance.Distance detailedDistance = DetailedDamerauLevenstheinDistance.compare(word, candidate);

      features.add(new Feature(prob1, prob3, wordCount, levenstheinDist, detailedDistance, jaroWrinklerDist, candidate));
    }
    if (!"noop".equals(score)) {
      features.sort(Feature::compareTo);
    }
    //logger.trace("Features for '%s' in '%s': %n", word, sentence.getText());
    //features.stream().map(Feature::toString).forEach(logger::trace);
    List<String> words = features.stream().map(Feature::getWord).collect(Collectors.toList());

    // compute general features, not tied to candidates
    SortedMap<String, Float> matchData = new TreeMap<>();
    matchData.put("candidateCount", (float) words.size());

    List<SuggestedReplacement> suggestionsData = features.stream().map(f -> {
      SuggestedReplacement s = new SuggestedReplacement(f.getWord());
      s.setFeatures(f.getData());
      return s;
    }).collect(Collectors.toList());
    return Pair.of(suggestionsData, matchData);
  }


  class Feature implements Comparable<Feature>{
    private final double prob1gram;
    private final double prob3gram;
    //private final double prob4gram;
    private final long wordCount;
    private final int levenshteinDistance;
    private final DetailedDamerauLevenstheinDistance.Distance detailedDistance;
    private final double jaroWrinklerDistance;
    private final String word;

    Feature(double prob1, double prob3, long wordCount, int levenshteinDistance,
            DetailedDamerauLevenstheinDistance.Distance detailedDistance, double jaroWrinklerDistance, String word) {
      this.prob1gram = prob1;
      this.prob3gram = prob3;
      this.wordCount = wordCount;
      this.levenshteinDistance = levenshteinDistance;
      this.detailedDistance = detailedDistance;
      this.jaroWrinklerDistance = jaroWrinklerDistance;
      this.word = word;
    }

    public String getWord() {
      return word;
    }

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
      if ("ngrams+levensthein".equals(score)) {
        double misspellingProb = Math.pow(mistakeProb, levenshteinDistance);
        return ngramProb + Math.log(misspellingProb);
      } else if ("ngrams".equals(score)) {
        return ngramProb;
      } else if ("ngrams+binomialLevensthein".equals(score)) {
        double misspellingProb = binomialProbability(mistakeProb, word.length(), levenshteinDistance);
        return ngramProb + Math.log(misspellingProb);
      } else if ("noop".equals(score)) {
        return 0;
      } else {
        throw new RuntimeException("Unknown scoring method: " + score);
      }
    }

    @Override
    public int compareTo(@NotNull Feature o) {
      // sort descending
      return Double.compare(o.getMeanProbability(), this.getMeanProbability());
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
        .append("word", word)
        .append("data", getData())
        .build();
    }

    public SortedMap<String, Float> getData() {
      SortedMap<String, Float> data = new TreeMap<>();
      data.put("prob1gram", (float) prob1gram);
      data.put("prob3gram", (float) prob3gram);
      data.put("wordCount", (float) wordCount);
      data.put("levensthein", (float) levenshteinDistance);
      data.put("jaroWrinkler", (float) jaroWrinklerDistance);
      data.put("inserts", (float) detailedDistance.inserts);
      data.put("deletes", (float) detailedDistance.deletes);
      data.put("replaces", (float) detailedDistance.replaces);
      data.put("transposes", (float) detailedDistance.transposes);
      data.put("wordLength", (float) word.length());
      // TODO: new features: at sentence start / end;
      return data;
    }
  }


}
