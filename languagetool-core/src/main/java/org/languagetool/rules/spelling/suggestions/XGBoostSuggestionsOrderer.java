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
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.SuggestedReplacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
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
      String modelPath = "";
      try {
        modelPath = getModelPath(language);
        InputStream savedModel = JLanguageTool.getDataBroker().getFromResourceDirAsStream(modelPath);
        return XGBoost.loadModel(savedModel);
      } catch (FileNotFoundException e) {
        logger.warn(String.format("Could not load suggestion ranking model at '%s'. Platform might be unsupported by the official XGBoost" +
          " maven package, or model might be missing/corrupted.", modelPath), e);
        return null;
      }
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

  private static final Map<String, Float> autoCorrectThreshold = new HashMap<>();
  private static final Map<String, List<Integer>> modelClasses = new HashMap<>();
  private static final Map<String, Integer> candidateFeatureCount = new HashMap<>();
  private static final Map<String, Integer> matchFeatureCount = new HashMap<>();
  private boolean modelAvailableForLanguage = false;
  private static boolean xgboostNotSupported = false;

  static {
    List<Integer> defaultClasses = Arrays.asList(-1, 0, 1, 2, 3, 4);
    autoCorrectThreshold.put("en-US", 0.99897194f);
    modelClasses.put("en-US", defaultClasses);
    candidateFeatureCount.put("en-US", 10);
    matchFeatureCount.put("en-US", 1);
    // disabled German model for now, no manually labeled validation set
  }

  /**
   * For testing purposes only
   */
  public static void setAutoCorrectThresholdForLanguage(Language lang, float value) {
    autoCorrectThreshold.replace(lang.getShortCodeWithCountryAndVariant(), value);
  }

  public XGBoostSuggestionsOrderer(Language lang, LanguageModel languageModel) {
    super(lang, languageModel);
    String langCode = lang.getShortCodeWithCountryAndVariant();
    if (xgboostNotSupported) {
      return;
    } else if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
      xgboostNotSupported = true;
      System.err.println("Warning: At the moment, your platform (Windows) is not supported by the official XGBoost maven package;" +
        " ML-based suggestion reordering is disabled.");
      return;
    }
    if (autoCorrectThreshold.containsKey(langCode) && modelClasses.containsKey(langCode) &&
      JLanguageTool.getDataBroker().resourceExists(getModelPath(language))) {
      try {
        Booster model = modelPool.borrowObject(language);
        if (model != null) {
          modelPool.returnObject(language, model);
          modelAvailableForLanguage = true;
        }
      } catch (NoClassDefFoundError | ExceptionInInitializerError | UnsatisfiedLinkError e) {
          /*
          Workaround because maven package for XGBoost is missing libraries for Windows;
          just disable functionality of this class via modelAvailableForLanguage and print a warning
          Proper fix would involve building from source or using pre-built packages that include Windows dependencies
          See note here: https://xgboost.readthedocs.io/en/latest/jvm/index.html#id7
           */
          logger.warn("At the moment, your platform (Windows?) or architecture (32 bit?) is not supported by the official XGBoost maven package;" +
            " ML-based suggestion reordering is disabled.", e);
          xgboostNotSupported = true;
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
  public List<SuggestedReplacement> orderSuggestions(List<String> suggestions, String word, AnalyzedSentence sentence, int startPos) {
    if (!isMlAvailable()) {
      throw new IllegalStateException("Illegal call to orderSuggestions() - isMlAvailable() returned false.");
    }
    long featureStartTime = System.currentTimeMillis();

    String langCode = language.getShortCodeWithCountryAndVariant();

    Pair<List<SuggestedReplacement>, SortedMap<String, Float>> candidatesAndFeatures = computeFeatures(suggestions, word, sentence, startPos);
    //System.out.printf("Computing %d features took %d ms.%n", suggestions.size(), System.currentTimeMillis() - featureStartTime);
    List<SuggestedReplacement> candidates = candidatesAndFeatures.getLeft();
    SortedMap<String, Float> matchFeatures = candidatesAndFeatures.getRight();
    List<SortedMap<String, Float>> suggestionFeatures = candidates.stream().map(SuggestedReplacement::getFeatures).collect(Collectors.toList());
    if (candidates.isEmpty()) {
      return Collections.emptyList();
    }
    if (candidates.size() != suggestionFeatures.size()) {
      throw new RuntimeException(
        String.format("Mismatch between candidates and corresponding feature list: length %d / %d",
          candidates.size(), suggestionFeatures.size()));
    }

    int numFeatures = matchFeatures.size() + topN * suggestionFeatures.get(0).size(); // padding with zeros
    float[] data = new float[numFeatures];

    int featureIndex = 0;
    //System.out.printf("Features for match on '%s': %n", word);
    int expectedMatchFeatures = matchFeatureCount.getOrDefault(langCode, -1);
    int expectedCandidateFeatures = candidateFeatureCount.getOrDefault(langCode, -1);
    if (matchFeatures.size() != expectedMatchFeatures) {
      logger.warn(String.format("Match features '%s' do not have expected size %d.",
        matchFeatures, expectedMatchFeatures));
    }
    for (Map.Entry<String, Float> feature : matchFeatures.entrySet()) {
      //System.out.printf("%s = %f%n", feature.getKey(), feature.getValue());
      data[featureIndex++] = feature.getValue();
    }
    //int suggestionIndex = 0;
    for (SortedMap<String, Float> candidateFeatures : suggestionFeatures) {
      if (candidateFeatures.size() != expectedCandidateFeatures) {
        logger.warn(String.format("Candidate features '%s' do not have expected size %d.",
          candidateFeatures, expectedCandidateFeatures));
      }
      //System.out.printf("Features for candidate '%s': %n", candidates.get(suggestionIndex++).getReplacement());
      for (Map.Entry<String, Float> feature : candidateFeatures.entrySet()) {
        //System.out.printf("%s = %f%n", feature.getKey(), feature.getValue());
        data[featureIndex++] = feature.getValue();
      }
    }
    List<Integer> labels = modelClasses.get(langCode);

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
        candidates.get(candidateIndex).setConfidence(prob);
      }
    } catch (XGBoostError xgBoostError) {
      logger.error("Error while applying XGBoost model to spelling suggestions", xgBoostError);
      return candidates;
    } catch (Exception e) {
      logger.error("Error while loading XGBoost model for spelling suggestions", e);
      return candidates;
    } finally {
      if (model != null) {
        try {
          modelPool.returnObject(language,model);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    candidates.sort(Collections.reverseOrder(Comparator.comparing(SuggestedReplacement::getConfidence)));
    return candidates;
  }

  @Override
  public boolean shouldAutoCorrect(List<SuggestedReplacement> rankedSuggestions) {
    if (rankedSuggestions.isEmpty() || rankedSuggestions.stream().anyMatch(s -> s.getConfidence() == null)) {
      return false;
    }
    float threshold = autoCorrectThreshold.getOrDefault(language.getShortCodeWithCountryAndVariant(), Float.MAX_VALUE);
    return rankedSuggestions.get(0).getConfidence() >= threshold;
  }
}
