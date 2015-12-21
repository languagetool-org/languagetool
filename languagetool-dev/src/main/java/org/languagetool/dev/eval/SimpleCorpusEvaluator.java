/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.dev.errorcorpus.ErrorCorpus;
import org.languagetool.dev.errorcorpus.ErrorSentence;
import org.languagetool.dev.errorcorpus.SimpleCorpus;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.languagemodel.LuceneSingleIndexLanguageModel;
import org.languagetool.languagemodel.MultiLanguageModel;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.EnglishNgramProbabilityRule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Evaluates the ngram rule with a simple corpus, see {@link SimpleCorpus}.
 * @since 3.2
 */
public class SimpleCorpusEvaluator {

  private static final double START_THRESHOLD = 0.000001;
  private static final double END_THRESHOLD   = 0.00000000000000001;
  private static final double STEP_FACTOR     = 0.1;

  private static volatile EnglishNgramProbabilityRule probabilityRule;

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

  public SimpleCorpusEvaluator(File indexTopDir) throws IOException {
    evaluator = getEvaluator(indexTopDir);
  }

  public SimpleCorpusEvaluator(File... indexTopDirs) throws IOException {
    evaluator = getEvaluator(indexTopDirs);
  }

  @NotNull
  private Evaluator getEvaluator(File... indexTopDirs) throws IOException {
    return new NgramLanguageToolEvaluator(indexTopDirs);
  }

  @NotNull
  private ErrorCorpus getCorpus(File file) throws IOException {
    return new SimpleCorpus(file);
  }

  void close() {
    evaluator.close();
  }

  public PrecisionRecall run(File file, double threshold) throws IOException {
    probabilityRule.setMinProbability(threshold);
    System.out.println("Output explanation:");
    System.out.println("    [  ] = this is not an expected error");
    System.out.println("    [+ ] = this is an expected error");
    System.out.println("    [++] = this is an expected error and the first suggestion is correct");
    System.out.println("    [//]  = not counted because already matches by a different rule");
    System.out.println("");
    checkLines(getCorpus(file));
    return printAndResetResults();
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

  private PrecisionRecall printAndResetResults() {
    System.out.println("");
    System.out.println(sentenceCount + " lines checked with " + errorsInCorpusCount + " errors.");
    System.out.println("Confusion rule matches: " + perfectConfusionMatches+ " perfect, "
            + goodConfusionMatches + " good, " + badConfusionMatches + " bad (" + badConfusionMatchWords + ")");

    System.out.println("\nCounting matches, no matter whether the first suggestion is correct:");

    System.out.print("  " + goodMatches + " out of " + matchCount + " matches are real errors");
    float precision = (float)goodMatches / matchCount;
    float recall = (float)goodMatches / errorsInCorpusCount;
    double fMeasure = FMeasure.getFMeasure(precision, recall, 1.0f);
    System.out.printf(" => %.3f precision, %.3f recall, %.5f f-measure\n", precision, recall, fMeasure);

    sentenceCount = 0;
    errorsInCorpusCount = 0;
    perfectMatches = 0;
    goodMatches = 0;
    matchCount = 0;
    perfectConfusionMatches = 0;
    goodConfusionMatches = 0;
    badConfusionMatches = 0;

    return new PrecisionRecall(precision, recall);
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
    if (args.length != 2) {
      System.out.println("Usage: " + SimpleCorpusEvaluator.class.getSimpleName() + " <corpusFile> <languageModelDir>");
      System.out.println("   <languageModelDir> is a Lucene index directory with ngram frequency information (use comma but not space to specify more than one)");
      System.exit(1);
    }
    File inputFile = new File(args[0]);
    List<File> indexDirs = Arrays.stream(args[1].split(",")).map(File::new).collect(Collectors.toList());
    System.out.println("Running with language model from " + indexDirs);
    SimpleCorpusEvaluator evaluator = new SimpleCorpusEvaluator(indexDirs.toArray(new File[]{}));
    List<String> results = new ArrayList<>();
    double threshold = START_THRESHOLD;
    while (threshold >= END_THRESHOLD) {
      PrecisionRecall res = evaluator.run(inputFile, threshold);
      //String thresholdStr = String.format(Locale.ENGLISH, "%.20f", threshold);
      String thresholdStr = StringUtils.rightPad(String.valueOf(threshold), 22);
      double fMeasure = FMeasure.getFMeasure(res.getPrecision(), res.getRecall(), 1.0f);
      String fMeasureStr = String.format(Locale.ENGLISH, "%.3f", fMeasure);
      String precision = String.format(Locale.ENGLISH, "%.3f", res.getPrecision());
      String recall = String.format(Locale.ENGLISH, "%.3f", res.getRecall());
      results.add(thresholdStr + ": f=" + fMeasureStr + ", precision=" + precision + ", recall=" + recall);
      threshold = threshold * STEP_FACTOR;
    }
    System.out.println("=== Results: ==================================");
    for (String result : results) {
      System.out.println(result);
    }
    evaluator.close();
  }

  static class NgramLanguageToolEvaluator implements Evaluator {

    private final JLanguageTool langTool;
    private final LanguageModel languageModel;

    NgramLanguageToolEvaluator(File... indexTopDirs) throws IOException {
      langTool = new JLanguageTool(new English());
      disableAllRules();
      List<LanguageModel> lms = new ArrayList<>();
      for (File indexTopDir : indexTopDirs) {
        lms.add(new LuceneLanguageModel(indexTopDir));
      }
      languageModel = new MultiLanguageModel(lms);
      LuceneSingleIndexLanguageModel.clearCaches();
      System.out.println("Using Lucene language model from " + languageModel);
      probabilityRule = new EnglishNgramProbabilityRule(JLanguageTool.getMessageBundle(), languageModel, new English());
      probabilityRule.setDefaultOn();
      langTool.addRule(probabilityRule);
    }

    @Override
    public void close() {
      if (languageModel != null) {
        languageModel.close();
      }
    }

    private void disableAllRules() {
      for (Rule rule : langTool.getAllActiveRules()) {
        langTool.disableRule(rule.getId());
      }
    }

    @Override
    public List<RuleMatch> check(AnnotatedText annotatedText) throws IOException {
      return langTool.check(annotatedText);
    }
  }

}
