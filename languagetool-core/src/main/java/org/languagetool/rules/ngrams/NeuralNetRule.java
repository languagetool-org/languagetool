/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ngrams;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Experimental;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.Tools;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Prototype of a DL4J-based rule.
 */
@Experimental
public class NeuralNetRule extends Rule {
  
  // results with there/their:
  //   Summary:  p=0.987, r=0.999, 1000+1000, 0grams, 2016-02-10  - ~10,000 examples (sentences-there10K.txt), threshold 0.5
  //   Summary:  p=0.985, r=0.994, 1000+1000, 0grams, 2016-02-10  - ~10,000 examples (sentences-there10K.txt), threshold 0.5, mit CONTEXT_SIZE=3
  
  public static final String RULE_ID = "ML_RULE";

  private static final String BIN_FILE = "/lt/dl4j/coefficients.bin"; // TODO: load from classpath
  private static final String JSON_FILE = "/lt/dl4j/conf.json";
  private static final int CONTEXT_SIZE = 2;
  private static final double THRESHOLD = 0.5;

  private final NeuralNetTools tools;
  private final MultiLayerNetwork model;
  private boolean warningShown = false;

  public NeuralNetRule(ResourceBundle messages) throws IOException {
    super(messages);
    tools = new NeuralNetTools();
    model = loadModel();
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> matches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    int pos = 0;
    for (AnalyzedTokenReadings token : tokens) {
      float p = 0;
      String suggestion = null;
      if ("their".equalsIgnoreCase(token.getToken())) {
        p = eval(tokens, pos);
        suggestion = "there";
        //System.out.println("=>" + p + " " + sentence.getText());
      } else if ("there".equalsIgnoreCase(token.getToken())) {
        p = eval(tokens, pos);
        suggestion = "Their";
        //System.out.println("=>" + p + " " + sentence.getText());
      }
      if (p > THRESHOLD) {
        String message = "Statistic suggests that '" + suggestion + "' might be the correct word here. Please check. (p=" + p + ")";
        RuleMatch match = new RuleMatch(this, token.getStartPos(), token.getEndPos(), message);
        match.setSuggestedReplacement(suggestion);
        matches.add(match);
      }
      pos++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  @Override
  public String getDescription() {
    return Tools.i18n(messages, "statistics_rule_description");
  }

  @Override
  public void reset() {
  }

  private float eval(AnalyzedTokenReadings[] tokens, int wordPos) {
    INDArray labels = Nd4j.create(1, 2);
    INDArray example = tools.getSentenceVector(CONTEXT_SIZE, tokens, wordPos);
    DataSet testSet = new DataSet(example, labels);
    INDArray inputs = Nd4j.create(1, CONTEXT_SIZE*2+1);
    inputs.putRow(0, testSet.getFeatureMatrix());
    labels.putRow(0, testSet.getLabels());
    DataSet curr = new DataSet(inputs, labels);
    INDArray output = model.output(curr.getFeatureMatrix(), false);
    return output.getFloat(0);
  }

  private MultiLayerNetwork loadModel() throws IOException {
    MultiLayerConfiguration confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(JSON_FILE)));
    try (DataInputStream dis = new DataInputStream(new FileInputStream(BIN_FILE))) {
      INDArray newParams = Nd4j.read(dis);
      MultiLayerNetwork model = new MultiLayerNetwork(confFromJson);
      model.init();
      model.setParameters(newParams);
      return model;
    }
  }

  public void setConfusionSet(ConfusionSet confusionSet) {
    if (!warningShown) {
      System.err.println("*** WARNING: setConfusionSet is not implemented in NeuralNetRule");
      warningShown = true;
    }
  }

  public int getNGrams() {
    return 0;
  }
}
