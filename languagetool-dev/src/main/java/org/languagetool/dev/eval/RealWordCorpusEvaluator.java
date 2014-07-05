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
 * Results as of 2014-07-05 (pure LT without corpus data, i.e. without confusion rule):
 * <pre>
 * Counting matches, no matter whether the first suggestion is correct:
 * 147 out of 214 matches are real errors => 68,69% precision, 17,67% recall
 * => 43,54 F(0.5) measure
 *
 * Counting only matches with a perfect first suggestion:
 * 103 out of 214 matches are real errors => 48,13% precision, 12,38% recall
 * => 30,51 F(0.5) measure
 * </pre>
 * 
 * Results as of 2014-07-05 (LT with 2grams from Google ngram index, in form of a Lucene index), with a cleaned
 * up Pedler corpus (see resources/data/pedler_corpus.diff):
 * <pre>
 * 673 lines checked with 832 errors.
 * Confusion rule matches: 116 perfect, 9 good, 12 bad
 *
 * Counting matches, no matter whether the suggestion is correct:
 * 272 out of 351 matches are real errors => 77,49% precision, 32,69% recall
 * => 60,82 F(0.5) measure
 *
 * Counting only matches with a perfect first suggestion:
 * 219 out of 351 matches are real errors => 62,39% precision, 26,32% recall
 * => 48,97 F(0.5) measure
 * </pre>
 * 
 * <p>After the Deadline has a recall of 27.1% and a precision of 89.4% ("The Design of a Proofreading Software Service",
 * Raphael Mudge, 2010). The recall is calculated by comparing only the first suggestion to the expected correction.</p>
 * 
 * @since 2.6
 */
class RealWordCorpusEvaluator {

  private final JLanguageTool langTool;
  
  private int sentenceCount;
  private int errorsInCorpusCount;
  private int perfectMatches;
  private int goodMatches;
  private int matchCount;
  private int perfectConfusionMatches;
  private int goodConfusionMatches;
  private int badConfusionMatches;

  RealWordCorpusEvaluator(File languageModelFileOrDir) throws IOException {
    langTool = new JLanguageTool(new BritishEnglish());
    langTool.activateDefaultPatternRules();
    // The Pedler corpus has some real errors that have no error markup, so we disable
    // some rules that typically match those:
    langTool.disableRule("COMMA_PARENTHESIS_WHITESPACE");
    langTool.disableRule("SENT_START_CONJUNCTIVE_LINKING_ADVERB_COMMA");
    langTool.disableRule("EN_QUOTES");
    langTool.disableRule("I_LOWERCASE");
    // turn off style rules:
    langTool.disableRule("LITTLE_BIT");
    langTool.disableRule("ALL_OF_THE");
    langTool.disableRule("SOME_OF_THE");
    // British English vs. American English - not clear whether the corpus contains only BE:
    langTool.disableRule("EN_GB_SIMPLE_REPLACE");
    langTool.disableRule("APARTMENT-FLAT");

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
              //new EnglishConfusionProbabilityRule(JLanguageTool.getMessageBundle(), languageModel, new File("/tmp/languagetool_network.net"));
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
    System.out.println("    [//]  = not counted because already matches by a different rule");
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
          matchCount++;
          if (isConfusionRule(match)) {
            perfectConfusionMatches++;
          }
          System.out.println("    [++] " + match + ": " + match.getSuggestedReplacements());
        } else if (!alreadyCounted && sentence.hasErrorCoveredByMatch(match)) {
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
            + goodConfusionMatches + " good, " + badConfusionMatches + " bad");

    System.out.println("\nCounting matches, no matter whether the first suggestion is correct:");
    
    System.out.print("  " + goodMatches + " out of " + matchCount + " matches are real errors");
    float goodPrecision = (float)goodMatches / matchCount * 100;
    float goodRecall = (float)goodMatches / errorsInCorpusCount * 100;
    System.out.printf(" => %.2f%% precision, %.2f%% recall\n", goodPrecision, goodRecall);

    // F0.5 puts more emphasis on precision than recall:
    System.out.printf("  => %.2f F(0.5) measure\n",
            getFMeasure(goodPrecision, goodRecall, 0.5f));
    
    System.out.println("\nCounting only matches with a perfect first suggestion:");

    System.out.print("  " + perfectMatches + " out of " + matchCount + " matches are real errors");
    float perfectPrecision = (float)perfectMatches / matchCount * 100;
    float perfectRecall = (float)perfectMatches / errorsInCorpusCount * 100;
    System.out.printf(" => %.2f%% precision, %.2f%% recall\n", perfectPrecision, perfectRecall);

    System.out.printf("  => %.2f F(0.5) measure\n",
            getFMeasure(perfectPrecision, perfectRecall, 0.5f));
  }

  private double getFMeasure(float precision, float recall, float beta) {
    double betaSquared = Math.pow(beta, 2);
    return (1 + betaSquared) * (precision * recall) / ((betaSquared * precision) + recall);
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
