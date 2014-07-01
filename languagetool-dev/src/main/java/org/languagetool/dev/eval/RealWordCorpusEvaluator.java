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
package org.languagetool.dev.eval;

import org.languagetool.JLanguageTool;
import org.languagetool.dev.errorcorpus.ErrorCorpus;
import org.languagetool.dev.errorcorpus.ErrorSentence;
import org.languagetool.dev.errorcorpus.PedlerCorpus;
import org.languagetool.language.BritishEnglish;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.languagemodel.MorfologikLanguageModel;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.EnglishConfusionProbabilityRule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs LanguageTool on Jenny Pedler's Real-word Error Corpus, available at
 * http://www.dcs.bbk.ac.uk/~jenny/resources.html.
 * 
 * Results as of 2014-05-10 (pure LT without corpus data):
 * <pre>
 * 673 lines checked with 834 errors.
 * 144 errors found that are marked as errors in the corpus (not counting whether LanguageTool's correction was useful)
 * => 17,75% recall
 * 103 errors found where the first suggestion was the correct one
 * => 12,35% recall
 * </pre>
 * 
 * Results as of 2014-06-24 (LT with 2grams from Google ngram index, in form of a Lucene index):
 * <pre>
 * 673 lines checked with 834 errors.
 * 269 errors found that are marked as errors in the corpus (not counting whether LanguageTool's correction was useful)
 * => 32,25% recall
 * 216 errors found where the first suggestion was the correct one
 * => 25,90% recall
 * </pre>
 * 
 * <p>After the Deadline has a recall of 27.1% ("The Design of a Proofreading Software Service"), even
 * considering only correct suggestions (by comparing the first suggestion to the expected correction).</p>
 * 
 * @since 2.6
 */
class RealWordCorpusEvaluator {

  private final JLanguageTool langTool;
  
  private int sentenceCount;
  private int errorsInCorpusCount;
  private int perfectMatches;
  private int goodMatches;

  RealWordCorpusEvaluator(File languageModelFileOrDir) throws IOException {
    langTool = new JLanguageTool(new BritishEnglish());
    langTool.activateDefaultPatternRules();
    if (languageModelFileOrDir != null) {
      LanguageModel languageModel;
      if (languageModelFileOrDir.isDirectory()) {
        System.out.println("Using Lucene language model from " + languageModelFileOrDir);
        languageModel = new LuceneLanguageModel(languageModelFileOrDir);
      } else {
        System.out.println("Using Morfologik language model from " + languageModelFileOrDir);
        languageModel = new MorfologikLanguageModel(languageModelFileOrDir);
      }
      EnglishConfusionProbabilityRule probabilityRule = 
              new EnglishConfusionProbabilityRule(JLanguageTool.getMessageBundle(), languageModel);
      langTool.addRule(probabilityRule);
    }
  }

  int getSentencesChecked() {
    return sentenceCount;
  }

  int getErrorsChecked() {
    return errorsInCorpusCount;
  }

  int getRealErrorsFound() {
    return goodMatches;
  }

  int getRealErrorsFoundWithGoodSuggestion() {
    return perfectMatches;
  }

  void run(File dir) throws IOException {
    System.out.println("Output explanation:");
    System.out.println("    [  ] = this is not an expected error");
    System.out.println("    [+ ] = this is an expected error");
    System.out.println("    [++] = this is an expected error and the first suggestion is correct");
    System.out.println("");
    ErrorCorpus corpus = new PedlerCorpus(dir);
    checkLines(corpus);
    printResults();
  }

  private void checkLines(ErrorCorpus corpus) throws IOException {
    for (ErrorSentence sentence : corpus) {
      List<RuleMatch> matches = langTool.check(sentence.getAnnotatedText());
      sentenceCount++;
      errorsInCorpusCount += sentence.getErrors().size();
      System.out.println(sentence.getMarkupText() + " => " + matches.size());
      //System.out.println("###"+sentence.annotatedText.toString().replaceAll("<.*?>", ""));
      List<Span> detectedErrorPositions = new ArrayList<>();
      for (RuleMatch match : matches) {
        boolean alreadyCounted = errorAlreadyCounted(match, detectedErrorPositions);
        if (!alreadyCounted && sentence.hasErrorCoveredByMatchAndGoodFirstSuggestion(match)) {
          //TODO: it depends on the order of matches whether [++] comes before [ +] (it should!)
          goodMatches++;
          perfectMatches++;
          System.out.println("    [++] " + match + ": " + match.getSuggestedReplacements());
        } else if (!alreadyCounted && sentence.hasErrorCoveredByMatch(match)) {
          goodMatches++;
          System.out.println("    [+ ] " + match + ": " + match.getSuggestedReplacements());
        } else {
          System.out.println("    [  ] " + match + ": " + match.getSuggestedReplacements());
        }
        detectedErrorPositions.add(new Span(match.getFromPos(), match.getToPos()));
      }
    }
  }

  private void printResults() {
    System.out.println("");
    System.out.println(sentenceCount + " lines checked with " + errorsInCorpusCount + " errors.");
    System.out.println(goodMatches + " errors found that are marked as errors in the corpus " +
            "(not counting whether LanguageTool's correction was useful)");
    float goodRecall = (float)goodMatches / errorsInCorpusCount * 100;
    System.out.printf(" => %.2f%% recall\n", goodRecall);
    float perfectRecall = (float)perfectMatches / errorsInCorpusCount * 100;
    System.out.println(perfectMatches + " errors found where the first suggestion was the correct one");
    System.out.printf(" => %.2f%% recall\n", perfectRecall);
  }

  private boolean errorAlreadyCounted(RuleMatch match, List<Span> detectedErrorPositions) {
    for (Span span : detectedErrorPositions) {
      Span matchSpan = new Span(match.getFromPos(), match.getToPos());
      if (span.covers(matchSpan) || matchSpan.covers(span)) {
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1 && args.length != 2) {
      System.out.println("Usage: " + RealWordCorpusEvaluator.class.getSimpleName() + " <corpusDirectory> [languageModel]");
      System.out.println("   [languageModel] is a morfologik file or Lucene index directory with ngram frequency information (optional)");
      System.exit(1);
    }
    if (args.length == 1) {
      System.out.println("Running without language model");
      RealWordCorpusEvaluator evaluator = new RealWordCorpusEvaluator(null);
      evaluator.run(new File(args[0]));
    } else {
      File languageModel = new File(args[1]);
      System.out.println("Running with language model from " + languageModel);
      RealWordCorpusEvaluator evaluator = new RealWordCorpusEvaluator(languageModel);
      evaluator.run(new File(args[0]));
    }
  }
  
  class Span {
    private final int startPos;
    private final int endPos;

    Span(int startPos, int endPos) {
      this.startPos = startPos;
      this.endPos = endPos;
    }

    boolean covers(Span other) {
      return startPos <= other.startPos && endPos >= other.endPos;
    }
  }
}
