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

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.languagemodel.LanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


public class XGBoostSuggestionsOrderer extends SuggestionsOrdererFeatureExtractor implements SuggestionsRanker {
  private static final Logger logger = LoggerFactory.getLogger(XGBoostSuggestionsOrderer.class);

  // Booster.predict is not thread safe (see https://github.com/dmlc/xgboost/issues/311)
  // TODO: configure eviction / pre-loading / ...
  private static final KeyedObjectPool<Language, Booster> modelPool = new GenericKeyedObjectPool<>(new BaseKeyedPooledObjectFactory<Language, Booster>() {
    @Override
    public Booster create(Language language) throws Exception {
      String modelPath = getModelPath(language);
      InputStream savedModel = JLanguageTool.getDataBroker().getFromResourceDirAsStream(modelPath);
      return XGBoost.loadModel(savedModel);
    }

    @Override
    public PooledObject<Booster> wrap(Booster booster) {
      return new DefaultPooledObject<>(booster);
    }
  });

  @NotNull
  private static String getModelPath(Language language) {
    return "/" + language.getShortCode() + "/spelling_correction_model.bin";
  }

  private static final Map<Language, Float> autoCorrectThreshold = new HashMap<>();
  private static final Map<Language, List<Integer>> modelClasses = new HashMap<>();
  private boolean modelAvailableForLanguage = false;

  static {
    List<Integer> defaultClasses = Arrays.asList(-1, 0, 1, 2, 3, 4);
    autoCorrectThreshold.put(Languages.getLanguageForShortCode("en-US"), 1.00f);
    modelClasses.put(Languages.getLanguageForShortCode("en-US"), defaultClasses);
    // disabled German model for now, no manually labeled validation set
    //autoCorrectThreshold.put(Languages.getLanguageForShortCode("de-DE"), 0.90f);
    //modelClasses.put(Languages.getLanguageForShortCode("de-DE"), Arrays.asList(-1, 0, 1, 2, 3, 4));
  }

  /**
   * For testing purposes only
   */
  public static void setAutoCorrectThresholdForLanguage(Language lang, float value) {
    autoCorrectThreshold.replace(lang, value);
  }

  public XGBoostSuggestionsOrderer(Language lang, LanguageModel languageModel) {
    super(lang, languageModel);
    if (autoCorrectThreshold.containsKey(lang) && modelClasses.containsKey(lang) &&
      JLanguageTool.getDataBroker().resourceExists(getModelPath(language))) {
      try {
        Booster model = modelPool.borrowObject(language);
        if (model != null) {
          modelPool.returnObject(language, model);
          modelAvailableForLanguage = true;
        }
      } catch (Exception e) {
        logger.warn("Could not load spelling suggestion ranking model for language " + language, e);
      }
    }
  }

  @Override
  protected void initParameters() {
    topN = 5;
    score = "noop";
    mistakeProb = 0.0; // not used because of score value
  }

  @Override
  public boolean isMlAvailable() {
    return super.isMlAvailable() && modelAvailableForLanguage;
  }

  @Override
  public List<String> orderSuggestionsUsingModel(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos) {
    return rankSuggestions(suggestions, word, sentence, startPos).getLeft();
  }

  @Override
  public Pair<List<String>, List<Float>> rankSuggestions(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos) {
    long featureStartTime = System.currentTimeMillis();
    Triple<List<String>, SortedMap<String, Float>, List<SortedMap<String, Float>>> candidatesAndFeatures = computeFeatures(suggestions, word, sentence, startPos);
    //System.out.printf("Computing %d features took %d ms.%n", suggestions.size(), System.currentTimeMillis() - featureStartTime);
    List<String> candidates = candidatesAndFeatures.getLeft();
    SortedMap<String, Float> matchFeatures = candidatesAndFeatures.getMiddle();
    List<SortedMap<String, Float>> suggestionFeatures = candidatesAndFeatures.getRight();
    List<Pair<String, Float>> candidatesWithProbabilities = new ArrayList<>(candidates.size());
    if (candidates.size() == 0) {
      return Pair.of(Collections.emptyList(), Collections.emptyList());
    }
    if (candidates.size() != suggestionFeatures.size()) {
      throw new RuntimeException(
        String.format("Mismatch between candidates and corresponding feature list: length %d / %d",
          candidates.size(), suggestionFeatures.size()));
    }

    int numFeatures = matchFeatures.size() + topN * suggestionFeatures.get(0).size(); // padding with zeros
    float[] data = new float[numFeatures];

    int featureIndex = 0;
    for (Map.Entry<String, Float> feature : matchFeatures.entrySet()) {
      data[featureIndex++] = feature.getValue();
    }
    for (SortedMap<String, Float> candidateFeatures : suggestionFeatures) {
      for (Map.Entry<String, Float> feature : candidateFeatures.entrySet()) {
        data[featureIndex++] = feature.getValue();
      }
    }
    List<Integer> labels = modelClasses.get(language);

    Booster model = null;
    try {
      long modelStartTime = System.currentTimeMillis();
      model = modelPool.borrowObject(language);
      //System.out.printf("Loading model took %d ms.%n", System.currentTimeMillis() - modelStartTime);
      DMatrix matrix = new DMatrix(data, 1, numFeatures);
      long predictStartTime = System.currentTimeMillis();
      float[][] output = model.predict(matrix);
      //System.out.printf("Prediction took %d ms.%n", System.currentTimeMillis() - predictStartTime);
      if (output.length != 1) {
        throw new XGBoostError(String.format(
          "XGBoost returned array with first dimension of length %d, expected 1.", output.length));
      }
      float[] probabilities = output[0];
      if (probabilities.length != labels.size()) {
        throw new XGBoostError(String.format(
          "XGBoost returned array with second dimension of length %d, expected %d.", probabilities.length, labels.size()));
      }
      // TODO: could react to label -1 (not in list) by e.g. evaluating more candidates
      //if (labels.get(0) != -1) {
      //  throw new IllegalStateException(String.format(
      //    "Expected first label of ML ranking model to be -1 (= suggestion not in list), was %d", labels.get(0)));
      //}
      //float notInListProbabilily = probabilites[0];
      for (int candidateIndex = 0; candidateIndex < candidates.size(); candidateIndex++) {
        int labelIndex = labels.indexOf(candidateIndex);
        float prob = 0.0f;
        if (labelIndex != -1) {
          prob = probabilities[labelIndex];
        }
        candidatesWithProbabilities.add(Pair.of(candidates.get(candidateIndex), prob));
      }
    } catch (XGBoostError xgBoostError) {
      logger.error("Error while applying XGBoost model to spelling suggestions", xgBoostError);
      return Pair.of(suggestions, Collections.emptyList());
    } catch (Exception e) {
      logger.error("Error while loading XGBoost model for spelling suggestions", e);
      return Pair.of(suggestions, Collections.emptyList());
    } finally {
      if (model != null) {
        try {
          modelPool.returnObject(language,model);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    // TODO: implement treshold + auto correct
    candidatesWithProbabilities.sort(Collections.reverseOrder(Comparator.comparing(Pair::getRight)));
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
