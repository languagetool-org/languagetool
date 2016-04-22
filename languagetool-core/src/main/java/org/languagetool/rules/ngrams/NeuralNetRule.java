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

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.languagetool.*;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Prototype of a DL4J-based rule.
 */
@Experimental
public class NeuralNetRule extends Rule {
  
  public static final String RULE_ID = "ML_RULE";

  private static final String BIN_FILE = "dl4j/coefficients-their-there.bin";
  private static final String JSON_FILE = "dl4j/conf.json";
  private static final int CONTEXT_SIZE = 2;
  private static final double THRESHOLD_INCORRECT = 0.3;
  private static final double THRESHOLD_CORRECT = 0.7;

  private final NeuralNetTools tools;
  private final MultiLayerNetwork model;
  private boolean warningShown = false;
  // TODO: words are hard-coded for now:
  private final List<WordPair> pairs = Arrays.asList(new WordPair("their", "there"));

  public NeuralNetRule(ResourceBundle messages) throws IOException {
    super(messages);
    tools = new NeuralNetTools();
    model = loadModel();
  }

  private MultiLayerNetwork loadModel() throws IOException {
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    try (DataInputStream dis = new DataInputStream(dataBroker.getFromResourceDirAsStream(BIN_FILE))) {
      String json = StringTools.readStream(dataBroker.getFromResourceDirAsStream(JSON_FILE), "utf-8");
      MultiLayerConfiguration confFromJson = MultiLayerConfiguration.fromJson(json);
      MultiLayerNetwork model = new MultiLayerNetwork(confFromJson);
      model.init();
      model.setParameters(Nd4j.read(dis));
      return model;
    }
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> matches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    int pos = 0;
    for (AnalyzedTokenReadings token : tokens) {
      float textProb = 1;
      float alternativeProb = 0;
      String suggestion = null;
      String lcToken = token.getToken().toLowerCase();
      Optional<WordPair> pair = getPairFor(token);
      if (pair.isPresent()) {
        if (pair.get().word1.equals(lcToken)) {
          suggestion = pair.get().word2;
        } else if (pair.get().word2.equals(lcToken)) {
          suggestion = pair.get().word1;
        }
        textProb = eval(tokens, pos);
        alternativeProb = eval(createVariant(tokens, pos, suggestion), pos);
        System.out.println("=> " + textProb + " " + sentence.getText());
        System.out.println("=> " + alternativeProb + " (with alternative: " + suggestion + ")");
      }
      if (textProb <= THRESHOLD_INCORRECT && alternativeProb >= THRESHOLD_CORRECT) {
        String message = "[DL4j] Statistic suggests that '" + suggestion + "' might be the correct word here. Please check.";
        RuleMatch match = new RuleMatch(this, token.getStartPos(), token.getEndPos(), message);
        match.setSuggestedReplacement(suggestion);
        matches.add(match);
      }
      pos++;
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  private Optional<WordPair> getPairFor(AnalyzedTokenReadings token) {
    WordPair result = null;
    for (WordPair pair : pairs) {
      String lcToken = token.getToken().toLowerCase();
      if (pair.word1.equals(lcToken) || pair.word2.equals(lcToken)) {
        result = pair;
        break;
      }
    }
    return Optional.ofNullable(result);
  }

  private AnalyzedTokenReadings[] createVariant(AnalyzedTokenReadings[] tokens, int pos, String alternativeToken) {
    AnalyzedTokenReadings[] newTokens = Arrays.copyOf(tokens, tokens.length);
    newTokens[pos] = new AnalyzedTokenReadings(new AnalyzedToken(alternativeToken, "fake", "fake"), tokens[pos].getStartPos());
    return newTokens;
  }

  private float eval(AnalyzedTokenReadings[] tokens, int wordPos) {
    INDArray labels = Nd4j.create(1, 2);
    INDArray example = tools.getContextVector(CONTEXT_SIZE, tokens, wordPos);
    DataSet testSet = new DataSet(example, labels);
    INDArray inputs = Nd4j.create(1, CONTEXT_SIZE*2+1);
    inputs.putRow(0, testSet.getFeatureMatrix());
    labels.putRow(0, testSet.getLabels());
    DataSet curr = new DataSet(inputs, labels);
    INDArray output = model.output(curr.getFeatureMatrix(), false);
    return output.getFloat(0);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return Tools.i18n(messages, "statistics_rule_description");
  }

  @Override
  public void reset() {
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
  
  static class WordPair {
    String word1;
    String word2;
    WordPair(String word1, String word2) {
      if (word1.compareTo(word2) < 0) {
        this.word1 = word1;
        this.word2 = word2;
      } else {
        this.word1 = word2;
        this.word2 = word1;
      }
    }
  }
}
