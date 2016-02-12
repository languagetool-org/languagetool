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
import org.deeplearning4j.datasets.fetchers.BaseDataFetcher;
import org.deeplearning4j.datasets.iterator.BaseDatasetIterator;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Experimental;
import org.languagetool.JLanguageTool;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.nd4j.linalg.util.FeatureUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("MagicNumber")
@Experimental
public abstract class NeuralNetTrainer {

  static final String BIN_FILE = "/lt/dl4j/coefficients.bin";
  static final String JSON_FILE = "/lt/dl4j/conf.json";

  //private static final String CORRECT_SENTENCES = "/home/dnaber/sentences-their-there10K.txt";
  //private static final String INCORRECT_SENTENCES = "/home/dnaber/sentences-their-there-incorrect10K.txt";
  //private static final String CORRECT_SENTENCES = "/home/dnaber/sentences-their10K.txt";
  //private static final String INCORRECT_SENTENCES = "/home/dnaber/sentences-their-incorrect10K.txt";
  private static final String CORRECT_SENTENCES = "/home/dnaber/sentences-there10K.txt";
  private static final String INCORRECT_SENTENCES = "/home/dnaber/sentences-there-incorrect10K.txt";
  private static final int CONTEXT_SIZE = 2;   // to the left and to the right so that 2*CONTEXT_SIZE+1 tokens are considered

  private final NeuralNetTools tools = new NeuralNetTools();
  private final JLanguageTool lt;
  private final String word1;
  private final String word2;
  
  protected abstract JLanguageTool getLanguageTool();

  protected NeuralNetTrainer(String word1, String word2) {
    this.word1 = word1;
    this.word2 = word2;
    lt = getLanguageTool();
  }
  
  public void trainAndEval() throws IOException {

    Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
    final int numRows = 1;
    final int numColumns = CONTEXT_SIZE*2 + 1;
    int outputNum = 2;
    int numSamples = 10_000;
    int batchSize = 50;
    int iterations = 30;
    int seed = 123;
    int listenerFreq = iterations/5;
    int splitTrainNum = (int) (batchSize*.8);

    System.out.println("Load data....");
    File correctSentenceFile = new File(CORRECT_SENTENCES);
    File incorrectSentenceFile = new File(INCORRECT_SENTENCES);
    ErrorDataFetcher fetcher = new ErrorDataFetcher(correctSentenceFile, incorrectSentenceFile);
    DataSetIterator dataIter = new BaseDatasetIterator(batchSize, numSamples, fetcher);

    System.out.println("Build model....");
    // see http://deeplearning4j.org/trainingtricks.html for general tips:
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .iterations(iterations)
            //.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
            .learningRate(1e-1f)   //  1e^-1, 1^e-3, and 1e^-6
            //.momentum(0.5)
            //.momentumAfter(Collections.singletonMap(3, 0.9))
            //.useDropConnect(true)
            .list(2)
            .layer(0, new DenseLayer.Builder()
                    .nIn(numRows * numColumns)
                    .nOut(100)
                    .activation("relu")
                    .weightInit(WeightInit.XAVIER)
                    .build())
            .layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
                    .nIn(100)
                    .nOut(outputNum)
                    .activation("softmax")
                    // "If you’re running a classifier, ensure that your weights are initialized to zero on the output layer"
                    // (http://deeplearning4j.org/troubleshootingneuralnets.html):
                    .weightInit(WeightInit.ZERO)
                    //.weightInit(WeightInit.XAVIER)
                    .build())
            .build();

    MultiLayerNetwork model = new MultiLayerNetwork(conf);
    model.init();

    System.out.println("Train model....");
    model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(listenerFreq)));
    List<INDArray> testInput = new ArrayList<>();
    List<INDArray> testLabels = new ArrayList<>();
    while (dataIter.hasNext()) {
      DataSet errorData = dataIter.next();
      //SplitTestAndTrain trainTest = errorData.splitTestAndTrain(splitTrainNum, new Random(seed));
      SplitTestAndTrain trainTest = errorData.splitTestAndTrain(splitTrainNum);
      DataSet trainInput = trainTest.getTrain();
      testInput.add(trainTest.getTest().getFeatureMatrix());
      testLabels.add(trainTest.getTest().getLabels());
      model.fit(trainInput);
    }

    // TODO: real cross-validation is needed here...
    System.out.println("Evaluate model (" + testInput.size() + ")....");
    Evaluation eval = new Evaluation(outputNum);
    for(int i = 0; i < testInput.size(); i++) {
      INDArray output = model.output(testInput.get(i));
      eval.eval(testLabels.get(i), output);
    }
    System.out.println(eval.stats());
    System.out.println("Save model....");
    saveModel(model);
  }

  private void saveModel(MultiLayerNetwork model) throws IOException {
    OutputStream fos = Files.newOutputStream(Paths.get(BIN_FILE));
    try (DataOutputStream dos = new DataOutputStream(fos)) {
      Nd4j.write(model.params(), dos);
    }
    FileUtils.write(new File(JSON_FILE), model.getLayerWiseConfigurations().toJson());
  }

  private class ErrorDataFetcher extends BaseDataFetcher {

    private final Iterator<String> correctIterator;
    private final Iterator<String> incorrectIterator;

    ErrorDataFetcher(File correct, File incorrect) throws IOException {
      List<String> correctSentences = Files.readAllLines(correct.toPath(), Charset.forName("utf-8"));
      List<String> incorrectSentences = Files.readAllLines(incorrect.toPath(), Charset.forName("utf-8"));
      System.out.println("Loaded correct/incorrect sentences: " + correctSentences.size() + ", " + incorrectSentences.size());
      totalExamples = correctSentences.size() + incorrectSentences.size();
      numOutcomes = 2;
      correctIterator = correctSentences.iterator();
      incorrectIterator = incorrectSentences.iterator();
    }

    @Override
    public void fetch(int numExamples) {
      List<DataSet> toConvert = new ArrayList<>();
      for (int i = 0; i < numExamples; ) {
        String sentence;
        boolean correct;
        if (i % 2 == 0) {
          sentence = correctIterator.next();
          correct = true;
        } else {
          sentence = incorrectIterator.next();
          correct = false;
        }
        cursor++;
        try {
          AnalyzedTokenReadings[] tokens = lt.getAnalyzedSentence(sentence).getTokensWithoutWhitespace();
          int word1Pos = findPos(tokens, word1);
          int word2Pos = findPos(tokens, word2);
          if (word1Pos != -1 && word2Pos != -1) {
            throw new RuntimeException("Found both words ('" + word1 + "' and '" + word2 + "'): " + sentence);
          }
          if (word1Pos == -1 && word2Pos == -1) {
            throw new RuntimeException("Expected words ('" + word1 + "' or '" + word2 + "') not found: " + sentence);
          }
          INDArray in = tools.getSentenceVector(CONTEXT_SIZE, tokens, word1Pos != -1 ? word1Pos : word2Pos);
          INDArray out = FeatureUtil.toOutcomeVector(correct ? 1 : 0, 2);
          toConvert.add(new DataSet(in, out));
          //System.out.println(correct + " " + sentence.replaceAll("(there|their)", "***$1***"));
          i++;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      inputColumns = CONTEXT_SIZE*2 + 1;
      initializeCurrFromList(toConvert);
    }

    private int findPos(AnalyzedTokenReadings[] tokens, String word) {
      int i = 0;
      for (AnalyzedTokenReadings token : tokens) {
        if (token.getToken().equalsIgnoreCase(word)) {
          return i;
        }
        i++;
      }
      return -1;
    }

  }

}
