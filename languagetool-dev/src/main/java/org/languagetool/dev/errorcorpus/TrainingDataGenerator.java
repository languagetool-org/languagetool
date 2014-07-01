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

import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes a gold-standard corpus and creates training data
 * for it by looking up the available ngram numbers and the expected result
 * (1 = error, 1 = no error). For example, these inputs
 * 
 * <pre>"This is there report."
 *   is there = 0.2
 *   there report = 0.0001</pre>
 * 
 * <pre>"This is their report."
 *   is there = 0.2
 *   their report = 0.3</pre>
 *   
 * ...would produce training data like this:
 * 
 * <pre>
 * 0.2 0.0001 0
 * 0.2 0.3    1
 * </pre>
 * @author Daniel Naber 
 */
class TrainingDataGenerator {

  private final EnglishWordTokenizer tokenizer = new EnglishWordTokenizer();
  private final LanguageModel languageModel;

  TrainingDataGenerator(LanguageModel languageModel) {
    this.languageModel = languageModel;
  }

  private void run(File corpusDir) throws IOException {
    int problemCount = 0;
    ErrorCorpus corpus = new PedlerCorpus(corpusDir);
    for (ErrorSentence sentence : corpus) {
      List<Error> errors = sentence.getErrors();
      for (Error error : errors) {
        try {
          List<String> errorContext = get2gramContext(sentence, error, false);
          writeTrainingData(errorContext, false);
          List<String> correctedContext = get2gramContext(sentence, error, true);
          writeTrainingData(correctedContext, true);
        } catch (AmbiguousErrorPositionException e) {
          System.err.println("Ignoring sentence due to limited algorithm: " + e.getMessage());
          problemCount++;
        }
      }
    }
    System.err.println("Sentences ignored due to limited algorithm (TODO): " + problemCount);
  }

  private List<String> get2gramContext(ErrorSentence sentence, Error error, boolean useCorrection) {
    String plainText = sentence.getAnnotatedText().getPlainText();
    List<String> tokens = removeWhitespaceTokens(tokenizer.tokenize(plainText));
    String markupText = sentence.getMarkupText();
    String errorToken = markupText.substring(error.getStartPos(), error.getEndPos());
    String errorOrCorrection = useCorrection ? error.getCorrection() : errorToken;
    List<Integer> errorTokens = new ArrayList<>();
    int i = 0;
    for (String token : tokens) {
      if (token.equals(errorToken)) {
        errorTokens.add(i);
      }
      i++;
    }
    if (errorTokens.size() != 1) {
      throw new AmbiguousErrorPositionException("Did not find exactly one error token position for sentence '" + sentence + "': " + errorTokens);
    }
    int errorTokenPos = errorTokens.get(0);
    List<String> contextTokens = new ArrayList<>();
    if (errorTokenPos == 0) {
      contextTokens.add(LanguageModel.GOOGLE_SENTENCE_START);
      contextTokens.add(errorOrCorrection);
      contextTokens.add(tokens.get(errorTokenPos + 1));
    } else if (errorTokenPos >= tokens.size()) {
      contextTokens.add(tokens.get(errorTokenPos - 1));
      contextTokens.add(errorOrCorrection);
      contextTokens.add(LanguageModel.GOOGLE_SENTENCE_END);
    } else {
      contextTokens.add(tokens.get(errorTokenPos - 1));
      contextTokens.add(errorOrCorrection);
      contextTokens.add(tokens.get(errorTokenPos + 1));
    }
    return contextTokens;
  }

  private void writeTrainingData(List<String> context, boolean isCorrected) {
    long leftCount = languageModel.getCount(context.get(0), context.get(1));
    long rightCount = languageModel.getCount(context.get(1), context.get(2));
    // the final part is just for debugging:
    int targetValue = isCorrected ? 1 : 0;
    System.out.println(leftCount + "\t" + rightCount + "\t" + targetValue + "\t" + context);
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
    if (args.length != 2) {
      System.err.println("Usage: " + TrainingDataGenerator.class.getSimpleName() + " <corpusDir> <languageModelDir>");
      System.exit(1);
    }
    File corpusDir = new File(args[0]);
    File indexDir = new File(args[1]);
    LanguageModel languageModel = new LuceneLanguageModel(indexDir);
    //TODO:
    /*System.out.println(languageModel.getCount("_START_", "I"));
    System.out.println(languageModel.getCount("walked", "_END_"));
    System.out.println(languageModel.getCount(".", "_END_"));
    System.out.println(languageModel.getCount("walked", "."));
    System.exit(1);*/
    TrainingDataGenerator generator = new TrainingDataGenerator(languageModel);
    generator.run(corpusDir);
  }
  
  class AmbiguousErrorPositionException extends RuntimeException {
    public AmbiguousErrorPositionException(String message) {
      super(message);
    }
  }
}
