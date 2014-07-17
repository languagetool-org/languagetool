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

import org.languagetool.Language;
import org.languagetool.dev.dumpcheck.Sentence;
import org.languagetool.dev.dumpcheck.WikipediaSentenceSource;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 input: en-there.txt, 'there', 'their'
 n Sätze suchen mit 'there' -> Training als 1
 n Sätze suche mit 'their' und ersetzen -> Training als 0
 (genau so viele?!)
 loop mit immer anderen sätzen (shuffle):
 trainieren des networks mit n sätzen und target = 1
 trainieren des networks mit n sätzen, wort durch homophon ersetzt, und target = 0
 cross-validation ab Satz n bis Satz n + 20%
 cross-validation heißt:
 korrekter satz als eingabe
 gefaket falscher satz als eingabe
 => precision + recall + f-measure ermitteln
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

  private void run(File corpusFile, String token) throws IOException {
    int testSetSize = (int)(MAX_SENTENCES*TEST_SET_FACTOR);
    System.out.println("testSetSize: " + testSetSize);
    int maxSentences = MAX_SENTENCES + testSetSize;
    System.out.println("Loading " + maxSentences + " sentences from " + corpusFile + "...");
    List<Sentence> allSentences = getRelevantSentences(corpusFile, token, maxSentences);
    List<Sentence> sentences = allSentences.subList(0, MAX_SENTENCES);
    List<Sentence> testSentences = sentences.subList(MAX_SENTENCES, sentences.size());
    List<Sentence> homophoneSentences = getRelevantSentences(corpusFile, TOKEN_HOMOPHONE, MAX_SENTENCES);
    System.out.println("Found " + sentences.size() + " training sentences with '" + token + "'");
    System.out.println("Found " + homophoneSentences.size() + " training sentences with '" + TOKEN_HOMOPHONE + "'");
    System.out.println("Found " + testSentences.size() + " with '" + TOKEN_HOMOPHONE + "'");  // TODO: also from homophones...
    for (int i = 0; i < ITERATIONS; i++) {
      //Collections.shuffle(sentences);  // TODO: shuffle all...
      try (MachineLearning machineLearning = new MachineLearning()) {
        trainSentences(token, token, sentences, machineLearning, 1);  // correct sentences
        trainSentences(TOKEN_HOMOPHONE, token, homophoneSentences, machineLearning, 0); // incorrect sentences
        System.out.println("Training neural network (" + new Date() + ")...");
        machineLearning.train(new File(NEURAL_NETWORK_OUTPUT));
        System.out.println("Saved neural network to " + NEURAL_NETWORK_OUTPUT + " (" + new Date() + ")");
        //
        // TODO: cross validation
        //
      }
    }
  }

  private List<Sentence> getRelevantSentences(File corpusFile, String token, int maxSentences) throws IOException {
    List<Sentence> sentences = new ArrayList<>();
    try (FileInputStream fis = new FileInputStream(corpusFile)) {
      WikipediaSentenceSource source = new WikipediaSentenceSource(fis, language);
      while (source.hasNext()) {
        Sentence sentence = source.next();
        if (sentence.getText().matches(".*\\b" + token + "\\b.*")) { // TODO: use real tokenizer?
          sentences.add(sentence);
          if (sentences.size() % 10 == 0) {
            System.out.println(sentences.size());
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
      long ngram2Left = languageModel.getCount(context.get(0), context.get(1));
      long ngram2Right = languageModel.getCount(context.get(1), context.get(2));
      double ngram2LeftNorm = (double)ngram2Left / 123200814;
      double ngram2RightNorm = (double)ngram2Right / 123200814;
      
      long ngram3 = languageModel.getCount(context.get(0), context.get(1), context.get(2));
      double ngram3Norm = (double)ngram3 / 123200814;
      
      machineLearning.addData(targetValue, ngram2LeftNorm, ngram2RightNorm, ngram3Norm);
      System.out.printf(targetValue + " l:%.4f r:%.4f 3g:%.7f " + StringTools.listToString(context, " ") + "\n",
              ngram2LeftNorm, ngram2RightNorm, ngram3Norm);
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

  private List<String> removeWhitespaceTokens(List<String> tokens) {
    List<String> result = new ArrayList<>();
    for (String token : tokens) {
      if (!token.trim().isEmpty()) {
        result.add(token);
      }
    }
    return result;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 4) {
      System.err.println("Usage: " + WikipediaTrainingDataGenerator.class.getSimpleName()
              + " <langCode> <wikipediaXml> <languageModel2GramDir> <languageModel3GramDir>");
      System.exit(1);
    }
    Language lang = Language.getLanguageForShortName(args[0]);
    File wikipediaFile = new File(args[1]);
    File indexDir2gram = new File(args[2]);
    File indexDir3gram = new File(args[3]);
    LanguageModel languageModel = new LuceneLanguageModel(indexDir2gram, indexDir3gram);
    WikipediaTrainingDataGenerator generator = new WikipediaTrainingDataGenerator(lang, languageModel);
    generator.run(wikipediaFile, TOKEN);
  }
  
}
