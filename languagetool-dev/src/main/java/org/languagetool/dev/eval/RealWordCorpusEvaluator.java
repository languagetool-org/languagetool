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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.dev.errorcorpus.ErrorCorpus;
import org.languagetool.dev.errorcorpus.ErrorSentence;
import org.languagetool.dev.errorcorpus.PedlerCorpus;
import org.languagetool.rules.RuleMatch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs LanguageTool on Jenny Pedler's Real-word Error Corpus, available at
 * http://www.dcs.bbk.ac.uk/~jenny/resources.html.
 * 
 * Results as of 2014-09-03 (pure LT without corpus data, i.e. without confusion rule):
 * <pre>
 * Counting matches, no matter whether the first suggestion is correct:
 * 147 out of 206 matches are real errors => 0,71 precision, 0,18 recall
 * => 0,4438 F(0.5) measure
 *
 * Counting only matches with a perfect first suggestion:
 * 119 out of 206 matches are real errors => 0,58 precision, 0,14 recall
 * => 0,3593 F(0.5) measure
 * </pre>
 * 
 * Results as of 2014-09-03 (LT with 3grams from Google ngram index, in form of a Lucene index), with a cleaned
 * up Pedler corpus (see resources/data/pedler_corpus.diff):
 * <pre>
 * 673 lines checked with 832 errors.
 * Confusion rule matches: 53 perfect, 0 good, 0 bad ([])
 *
 * Counting matches, no matter whether the first suggestion is correct:
 * 200 out of 259 matches are real errors => 0,77 precision, 0,24 recall
 * => 0,5353 F(0.5) measure
 *
 * Counting only matches with a perfect first suggestion:
 * 172 out of 259 matches are real errors => 0,66 precision, 0,21 recall
 * => 0,4604 F(0.5) measure
 * </pre>
 * 
 * <p>After the Deadline has a precision of 89.4% and a recall of 27.1%  ("The Design of a Proofreading Software Service",
 * Raphael Mudge, 2010). The recall is calculated by comparing only the first suggestion to the expected correction.
 * Also see the constructor of this class where you can comment in AtDEvalChecker to run a direct comparison.
 * </p>
 * @since 2.7
 */
class RealWordCorpusEvaluator {

  private final Evaluator evaluator;
  private final List<String> badConfusionMatchWords = new ArrayList<>();
  
  private int sentenceCount;
  private int errorsInCorpusCount;
  private int perfectMatches;
  private int goodMatches;
  private int matchCount;
  private int perfectConfusionMatches;
  private int goodConfusionMatches;
  private int badConfusionMatches;

  RealWordCorpusEvaluator(File indexDir) throws IOException {
    evaluator = getEvaluator(indexDir);
  }

  @NotNull
  protected Evaluator getEvaluator(File indexTopDir) throws IOException {
    Evaluator checker = new LanguageToolEvaluator(indexTopDir);
    // use this to run AtD as the backend, so results can easily be compared to LT:
    //checker = new AtDEvalChecker("http://en.service.afterthedeadline.com/checkDocument?key=test&data=");
    return checker;
  }

  @NotNull
  protected ErrorCorpus getCorpus(File dir) throws IOException {
    return new PedlerCorpus(dir);
  }
  
  void close() {
    evaluator.close();
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
    System.out.println("    [//]  = not counted because already matches by a different rule");
    System.out.println("");
    ErrorCorpus corpus = getCorpus(dir);
    checkLines(corpus);
    printResults();
  }

  private void checkLines(ErrorCorpus corpus) throws IOException {
    for (ErrorSentence sentence : corpus) {
      List<RuleMatch> matches = evaluator.check(sentence.getAnnotatedText());
      sentenceCount++;
      errorsInCorpusCount += sentence.getErrors().size();
      System.out.println(sentence.getMarkupText() + " => " + matches.size());
      for (RuleMatch match : matches) {
        int length = match.getToPos() - match.getFromPos();
        System.out.println(StringUtils.repeat(" ", match.getFromPos()) + StringUtils.repeat("^", length));
      }
      List<Span> detectedErrorPositions = new ArrayList<>();
      for (RuleMatch match : matches) {
        boolean alreadyCounted = errorAlreadyCounted(match, detectedErrorPositions);
        if (!alreadyCounted && sentence.hasErrorCoveredByMatchAndGoodFirstSuggestion(match)) {
          //TODO: it depends on the order of matches whether [++] comes before [ +] (it should!)
          goodMatches++;
          perfectMatches++;
          matchCount++;
          if (isConfusionRule(match)) {
            perfectConfusionMatches++;
          }
          System.out.println("    [++] " + match + ": " + match.getSuggestedReplacements());
        } else if (!alreadyCounted && sentence.hasErrorCoveredByMatch(match)) {
        //} else if (!alreadyCounted && sentence.hasErrorOverlappingWithMatch(match)) {
          goodMatches++;
          matchCount++;
          if (isConfusionRule(match)) {
            goodConfusionMatches++;
          }
          System.out.println("    [+ ] " + match + ": " + match.getSuggestedReplacements());
        } else if (alreadyCounted) {
          System.out.println("    [//]  " + match + ": " + match.getSuggestedReplacements());
        } else {
          System.out.println("    [  ] " + match + ": " + match.getSuggestedReplacements());
          matchCount++;
          if (isConfusionRule(match)) {
            badConfusionMatches++;
            badConfusionMatchWords.add(sentence.getMarkupText().substring(match.getFromPos(), match.getToPos()));
          }
        }
        detectedErrorPositions.add(new Span(match.getFromPos(), match.getToPos()));
      }
    }
  }

  private boolean isConfusionRule(RuleMatch match) {
    return match.getRule().getId().equals("CONFUSION_RULE");
  }

  private void printResults() {
    System.out.println("");
    System.out.println(sentenceCount + " lines checked with " + errorsInCorpusCount + " errors.");
    System.out.println("Confusion rule matches: " + perfectConfusionMatches+ " perfect, "
            + goodConfusionMatches + " good, " + badConfusionMatches + " bad (" + badConfusionMatchWords + ")");

    System.out.println("\nCounting matches, no matter whether the first suggestion is correct:");
    
    System.out.print("  " + goodMatches + " out of " + matchCount + " matches are real errors");
    float goodPrecision = (float)goodMatches / matchCount;
    float goodRecall = (float)goodMatches / errorsInCorpusCount;
    System.out.printf(" => %.2f precision, %.2f recall\n", goodPrecision, goodRecall);

    System.out.printf("  => %.4f F(0.5) measure\n",
            FMeasure.getWeightedFMeasure(goodPrecision, goodRecall));
    
    System.out.println("\nCounting only matches with a perfect first suggestion:");

    System.out.print("  " + perfectMatches + " out of " + matchCount + " matches are real errors");
    float perfectPrecision = (float)perfectMatches / matchCount;
    float perfectRecall = (float)perfectMatches / errorsInCorpusCount;
    System.out.printf(" => %.2f precision, %.2f recall\n", perfectPrecision, perfectRecall);

    System.out.printf("  => %.4f F(0.5) measure\n",
            FMeasure.getWeightedFMeasure(perfectPrecision, perfectRecall));
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
      System.out.println("   [languageModel] is a Lucene index directory with ngram frequency information (optional)");
      System.exit(1);
    }
    File languageModelTopDir = null;
    if (args.length == 1) {
      System.out.println("Running without language model");
    } else {
      languageModelTopDir = new File(args[1]);
      System.out.println("Running with language model from " + languageModelTopDir);
    }
    RealWordCorpusEvaluator evaluator = new RealWordCorpusEvaluator(languageModelTopDir);
    evaluator.run(new File(args[0]));
    evaluator.close();
  }
  
}
