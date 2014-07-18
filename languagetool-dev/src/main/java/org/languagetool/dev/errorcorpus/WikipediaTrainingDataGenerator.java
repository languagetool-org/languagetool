/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.errorcorpus;

import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.languagetool.Language;
import org.languagetool.dev.dumpcheck.Sentence;
import org.languagetool.dev.dumpcheck.WikipediaSentenceSource;
import org.languagetool.dev.eval.FMeasure;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Loads sentences with a homophone (e.g. 'there') from Wikipedia and uses them for training, 
 * assuming they are correct. Loads sentences with a confusable homophone (e.g. 'their')
 * from Wikipedia, replacing them with the other homophone, assuming their are now wrong
 * and uses them for training, too. Finally, it runs a cross-validation.
 * @since 2.7
 * @author Daniel Naber 
 */
class WikipediaTrainingDataGenerator {

  private static final String TOKEN = "there";
  private static final String TOKEN_HOMOPHONE = "their";
  private static final String NEURAL_NETWORK_OUTPUT = "/tmp/languagetool_network.net";
  private static final int MAX_SENTENCES = 100;  // TODO
  private static final int ITERATIONS = 1;
  private static final float TEST_SET_FACTOR = 0.2f;
  
  private final EnglishWordTokenizer tokenizer = new EnglishWordTokenizer();
  private final Language language;
  private final LanguageModel languageModel;

  WikipediaTrainingDataGenerator(Language language, LanguageModel languageModel) {
    this.language = language;
    this.languageModel = languageModel;
  }

  private void run(File corpusFile, String token, String homophoneToken) throws IOException {

    List<Sentence> allSentences = getRelevantSentences(corpusFile, token, MAX_SENTENCES);
    ListSplit<Sentence> split = split(allSentences, TEST_SET_FACTOR);
    List<Sentence> trainingSentences = split.trainingList;
    List<Sentence> testSentences = split.testList;
    System.out.println("Found " + trainingSentences.size() + " training sentences with '" + token + "'");
    System.out.println("Found " + testSentences.size() + " with '" + token + "'");

    // Load the sentences with a homophone to 'token' and later replace it with 'token' so we get error sentences:
    List<Sentence> allHomophoneSentences = getRelevantSentences(corpusFile, homophoneToken, MAX_SENTENCES);
    ListSplit<Sentence> homophoneSplit = split(allHomophoneSentences, TEST_SET_FACTOR);
    List<Sentence> homophoneTrainingSentences = homophoneSplit.trainingList;
    List<Sentence> homophoneTestSentences = homophoneSplit.testList;    
    System.out.println("Found " + homophoneTrainingSentences.size() + " training sentences with '" + homophoneToken + "' (will be turned into errors)");
    System.out.println("Found " + homophoneTestSentences.size() + " with '" + homophoneToken + "'");
    
    for (int i = 0; i < ITERATIONS; i++) {
      System.out.println("===== Iteration " + i + " ===========================================================");
      //Collections.shuffle(sentences);  // TODO: shuffle all...
      try (MachineLearning machineLearning = new MachineLearning()) {
        trainSentences(token, token, trainingSentences, machineLearning, 1);  // correct sentences
        trainSentences(homophoneToken, token, homophoneTrainingSentences, machineLearning, 0); // incorrect sentences
        System.out.println("Training neural network (" + new Date() + ")...");
        machineLearning.train(new File(NEURAL_NETWORK_OUTPUT));
        System.out.println("Saved neural network to " + NEURAL_NETWORK_OUTPUT + " (" + new Date() + ")");
        List<ValidationSentence> validationSentences = new ArrayList<>();
        validationSentences.addAll(getValidationSentences(testSentences, true));
        validationSentences.addAll(getValidationSentences(homophoneTestSentences, false));
        crossValidate(validationSentences, token, homophoneToken);
      }
    }
  }

  private ListSplit<Sentence> split(List<Sentence> all, float testSetFactor) {
    int boundary = (int) (all.size() * testSetFactor);
    List<Sentence> trainingList = all.subList(boundary, all.size());
    List<Sentence> testList = all.subList(0, boundary);
    return new ListSplit<>(trainingList, testList);
  }

  private List<ValidationSentence> getValidationSentences(List<Sentence> sentences, boolean isCorrect) {
    List<ValidationSentence> validationSentences = new ArrayList<>();
    for (Sentence sentence : sentences) {
      validationSentences.add(new ValidationSentence(sentence, isCorrect));
    }
    return validationSentences;
  }

  @SuppressWarnings("ConstantConditions")
  private void crossValidate(List<ValidationSentence> sentences, String token, String homophoneToken) throws IOException {
    System.out.println("Starting cross validation on " + sentences.size() + " sentences");
    int truePositives = 0;
    int falsePositives = 0;
    int falseNegatives = 0;
    try (MachineLearning machineLearning = new MachineLearning()) {
      BasicNetwork loadedNet = (BasicNetwork) machineLearning.load(new File(NEURAL_NETWORK_OUTPUT));
      for (ValidationSentence sentence : sentences) {
        boolean expectCorrect = sentence.isCorrect;
        String textToken = expectCorrect ? token : homophoneToken;
        List<String> context = getContext(sentence.sentence, textToken, token);
        double[] features = getFeatures(context);
        BasicMLData data = new BasicMLData(features);
        boolean consideredCorrect = loadedNet.compute(data).getData(0) > 0.5f;
        System.out.println("cross val: " + consideredCorrect + ", expected: "  + expectCorrect + ": " 
                + sentence.sentence.toString().replaceFirst(textToken, "**" + token + "**"));
        if (consideredCorrect && expectCorrect) {
          truePositives++;
        } else if (!consideredCorrect && expectCorrect) {
          falseNegatives++;
        } else if (consideredCorrect && !expectCorrect) {
          falsePositives++;
        }
      }
    }
    float precision = (float)truePositives / (truePositives + falsePositives);
    System.out.println("Cross validation results:");
    System.out.printf("  Precision: %.3f\n", precision);
    float recall = (float)truePositives / (truePositives + falseNegatives);
    System.out.printf("  Recall: %.3f\n", recall);
    System.out.printf("  F-measure(beta=0.5): %.3f\n", FMeasure.getFMeasure(precision, recall));
  }

  private List<Sentence> getRelevantSentences(File corpusFile, String token, int maxSentences) throws IOException {
    List<Sentence> sentences = new ArrayList<>();
    try (FileInputStream fis = new FileInputStream(corpusFile)) {
      WikipediaSentenceSource source = new WikipediaSentenceSource(fis, language);
      while (source.hasNext()) {
        Sentence sentence = source.next();
        if (sentence.getText().matches(".*\\b" + token + "\\b.*")) { // TODO: use real tokenizer?
          sentences.add(sentence);
          if (sentences.size() % 25 == 0) {
            System.out.println("Loaded sentence " + sentences.size() + " with '" + token + "' from " + corpusFile.getName());
          }
          if (sentences.size() >= maxSentences) {
            break;
          }
        }
      }
    }
    return sentences;
  }

  private void trainSentences(String token, String newToken, List<Sentence> sentences, MachineLearning machineLearning, float targetValue) {
    for (Sentence sentence : sentences) {
      List<String> context = getContext(sentence, token, newToken);
      double[] features = getFeatures(context);
      machineLearning.addData(targetValue, features);
      System.out.printf(targetValue + " %s " + StringTools.listToString(context, " ") + "\n", Arrays.toString(features));
    }
  }

  private List<String> getContext(Sentence sentence, String token, String newToken) {
    String plainText = sentence.getText();
    List<String> tokens = removeWhitespaceTokens(tokenizer.tokenize(plainText));
    int i = 0;
    int tokenPos = -1;
    for (String t : tokens) {
      if (t.equals(token)) {
        tokenPos = i;
      }
      i++;
    }
    List<String> contextTokens = new ArrayList<>();
    if (tokenPos == -1) {
      throw new RuntimeException("Not found: '" + token + "'");
    } else if (tokenPos == 0) {
      contextTokens.add(LanguageModel.GOOGLE_SENTENCE_START);
      contextTokens.add(newToken);
      contextTokens.add(tokens.get(tokenPos + 1));
    } else if (tokenPos >= tokens.size() - 1) {
      contextTokens.add(tokens.get(tokenPos - 1));
      contextTokens.add(newToken);
      contextTokens.add(LanguageModel.GOOGLE_SENTENCE_END);
    } else {
      contextTokens.add(tokens.get(tokenPos - 1));
      contextTokens.add(newToken);
      contextTokens.add(tokens.get(tokenPos + 1));
    }
    return contextTokens;
  }

  private double[] getFeatures(List<String> context) {
    long maxVal = 123200814; // TODO: find this automatically
    long ngram2Left = languageModel.getCount(context.get(0), context.get(1));
    long ngram2Right = languageModel.getCount(context.get(1), context.get(2));
    double ngram2LeftNorm = (double)ngram2Left / maxVal;
    double ngram2RightNorm = (double)ngram2Right / maxVal;
    long ngram3 = languageModel.getCount(context.get(0), context.get(1), context.get(2));
    double ngram3Norm = (double)ngram3 / maxVal;
    return new double[] {ngram2LeftNorm, ngram2RightNorm, ngram3Norm};
  }

  private List<String> removeWhitespaceTokens(List<String> tokens) {
    List<String> result = new ArrayList<>();
    for (String token : tokens) {
      if (!token.trim().isEmpty()) {
        result.add(token);
      }
    }
    return result;
  }
  
  class ValidationSentence {
    final Sentence sentence;
    final boolean isCorrect;
    ValidationSentence(Sentence sentence, boolean correct) {
      this.sentence = sentence;
      isCorrect = correct;
    }
  }
  
  class ListSplit<T> {
    final List<T> trainingList;
    final List<T> testList;
    ListSplit(List<T> trainingList, List<T> testList) {
      this.trainingList = trainingList;
      this.testList = testList;
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.err.println("Usage: " + WikipediaTrainingDataGenerator.class.getSimpleName()
              + " <langCode> <wikipediaXml> <languageModelTopDir>");
      System.err.println("   <languageModelTopDir> is a directory with sub-directories '2grams' and/or '3grams' with Lucene indexes");
      System.exit(1);
    }
    Language lang = Language.getLanguageForShortName(args[0]);
    File wikipediaFile = new File(args[1]);
    File indexTopDir = new File(args[2]);
    LanguageModel languageModel = new LuceneLanguageModel(indexTopDir);
    WikipediaTrainingDataGenerator generator = new WikipediaTrainingDataGenerator(lang, languageModel);
    generator.run(wikipediaFile, TOKEN, TOKEN_HOMOPHONE);
  }
  
}
