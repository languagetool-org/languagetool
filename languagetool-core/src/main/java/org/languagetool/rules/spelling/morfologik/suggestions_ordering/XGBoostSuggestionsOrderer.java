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

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.apache.commons.lang3.tuple.Pair;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.languagemodel.LanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class XGBoostSuggestionsOrderer extends SuggestionsOrdererFeatureExtractor implements SuggestionsRanker {
  private static final Logger logger = LoggerFactory.getLogger(XGBoostSuggestionsOrderer.class);

  private Booster model;
  private static final Map<Language, Float> autoCorrectThreshold = new HashMap<>();

  static {
    // TODO
    autoCorrectThreshold.put(Languages.getLanguageForShortCode("en-US"), 0.99f);
  }

  public XGBoostSuggestionsOrderer(Language lang, LanguageModel languageModel) {
    super(lang, languageModel);
    String modelPath = "/" + lang + "/spelling_correction_model.bin";
    InputStream savedModel = JLanguageTool.getDataBroker().getFromResourceDirAsStream(modelPath);
    try {
      model = XGBoost.loadModel(savedModel);
    } catch (XGBoostError | IOException e) {
      throw new RuntimeException("Could not XGBoost model for automatic spelling correction.", e);
    }
  }

  @Override
  protected void initParameters() {
    topN = 10;
    score = "nop";
    mistakeProb = 0.0; // not used because of score value
  }

  @Override
  public boolean isMlAvailable() {
    return false;
  }

  @Override
  public List<String> orderSuggestionsUsingModel(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos) {
    return rankSuggestions(suggestions, word, sentence, startPos).getLeft();
  }

  @Override
  public Pair<List<String>, List<Float>> rankSuggestions(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos) {
    Pair<List<String>, List<SortedMap<String, Float>>> candidatesAndFeatures = computeFeatures(suggestions, word, sentence, startPos);
    List<String> candidates = candidatesAndFeatures.getLeft();
    List<SortedMap<String, Float>> features = candidatesAndFeatures.getRight();
    List<Pair<String, Float>> candidatesWithProbabilities = new ArrayList<>(candidates.size());
    if (candidates.size() == 0) {
      return Pair.of(Collections.emptyList(), Collections.emptyList());
    }
    if (candidates.size() != features.size()) {
      throw new RuntimeException(
        String.format("Mismatch between candidates and corresponding feature list: length %d / %d",
          candidates.size(), features.size()));
    }

    int numFeatures = features.size() * features.get(0).size();
    float[] data = new float[numFeatures];

    int featureIndex = 0;
    for (SortedMap<String, Float> candidateFeatures : features) {
      for (Map.Entry<String, Float> feature : candidateFeatures.entrySet()) {
        data[featureIndex++] = feature.getValue();
      }
    }

    try {
      DMatrix matrix = new DMatrix(data, 1, numFeatures);
      float[][] output = model.predict(matrix);
      if (output.length != 1) {
        throw new XGBoostError(String.format(
          "XGBoost returned array with first dimension of length %d, expected 1.", output.length));
      }
      float[] probabilites = output[0];
      if (probabilites.length != features.size()) {
        throw new XGBoostError(String.format(
          "XGBoost returned array with second dimension of length %d, expected %d.", probabilites.length, features.size()));
      }
      for (int i = 0; i < candidates.size(); i++) {
        candidatesWithProbabilities.add(Pair.of(candidates.get(i), probabilites[i]));
      }
    } catch (XGBoostError xgBoostError) {
      logger.error("Error while applying XGBoost model to spelling suggestions", xgBoostError);
      return Pair.of(suggestions, Collections.emptyList());
    }
    // TODO: implement treshold + auto correct
    candidatesWithProbabilities.sort(Comparator.comparing(Pair::getRight));
    List<String> sortedCandidates = candidatesWithProbabilities.stream().map(Pair::getLeft).collect(Collectors.toList());
    List<Float> confidence = candidatesWithProbabilities.stream().map(Pair::getRight).collect(Collectors.toList());
    return Pair.of(sortedCandidates, confidence);
  }

  @Override
  public boolean shouldAutoCorrect(Pair<List<String>, List<Float>> rankedSuggestions) {
    List<Float> confidence = rankedSuggestions.getRight();
    if (confidence.size() == 0) {
      return false;
    }
    return confidence.get(0) >= autoCorrectThreshold.getOrDefault(language, Float.MAX_VALUE);
  }
}
