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

import org.apache.tika.io.IOUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.EnglishConfusionProbabilityRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Runs LanguageTool on Jenny Pedler's Real-word Error Corpus, available at
 * http://www.dcs.bbk.ac.uk/~jenny/resources.html.
 * 
 * Results as of 2014-05-10:
 * <pre>
 * 673 lines checked with 834 errors.
 * 144 errors found that are marked as errors in the corpus (not counting whether LanguageTool's correction was useful)
 * => 17,75% recall
 * 103 errors found where the first suggestion was the correct one
 * => 12,35% recall
 * </pre>
 * 
 * <p>After the Deadline has a recall of 27.1% ("The Design of a Proofreading Software Service"), even
 * considering only correct suggestions (by comparing the first suggestion to the expected correction).</p>
 * 
 * @since 2.6
 */
class RealWordCorpusEvaluator {

  private static final String NORMALIZE_REGEX = "\\s*<ERR targ\\s*=\\s*([^>]*?)\\s*>\\s*(.*?)\\s*</ERR>\\s*";

  private final JLanguageTool langTool;
  
  private int sentenceCount;
  private int errorsInCorpusCount;
  private int perfectMatches;
  private int goodMatches;

  RealWordCorpusEvaluator(File languageModel) throws IOException {
    langTool = new JLanguageTool(new BritishEnglish());
    langTool.activateDefaultPatternRules();
    if (languageModel != null) {
      langTool.addRule(new EnglishConfusionProbabilityRule(JLanguageTool.getMessageBundle(), languageModel));
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
    File[] files = dir.listFiles();
    if (files == null) {
      throw new RuntimeException("Directory not found: " + dir);
    }
    for (File file : files) {
      if (!file.getName().endsWith(".txt")) {
        System.out.println("Ignoring " + file + ", does not match *.txt");
        continue;
      }
      try (FileInputStream fis = new FileInputStream(file)) {
        checkLines(IOUtils.readLines(fis));
      }
    }
    printResults();
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

  private void checkLines(List<String> lines) throws IOException {
    for (String line : lines) {
      if (!line.contains("<ERR ")) {
        System.out.println("No error markup found, ignoring: " + line);
        continue;
      }
      ErrorSentence sentence = getIncorrectSentence(line);
      List<RuleMatch> matches = langTool.check(sentence.annotatedText);
      sentenceCount++;
      errorsInCorpusCount += sentence.errors.size();
      System.out.println(sentence.markupText + " => " + matches.size());
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

  private boolean errorAlreadyCounted(RuleMatch match, List<Span> detectedErrorPositions) {
    for (Span span : detectedErrorPositions) {
      Span matchSpan = new Span(match.getFromPos(), match.getToPos());
      if (span.covers(matchSpan) || matchSpan.covers(span)) {
        return true;
      }
    }
    return false;
  }

  private ErrorSentence getIncorrectSentence(String line) {
    String normalized = line.replaceAll(NORMALIZE_REGEX, " <ERR targ=$1>$2</ERR> ").replaceAll("\\s+", " ").trim();
    List<Error> errors = new ArrayList<>();
    int startPos = 0;
    while (normalized.indexOf("<ERR targ=", startPos) != -1) {
      int startTagStart = normalized.indexOf("<ERR targ=", startPos);
      int startTagEnd = normalized.indexOf(">", startTagStart);
      int endTagStart = normalized.indexOf("</ERR>", startTagStart);
      int correctionEnd = normalized.indexOf(">", startTagStart);
      String correction = normalized.substring(startTagStart + "<ERR targ=".length(), correctionEnd);
      errors.add(new Error(startTagEnd + 1, endTagStart, correction));
      startPos = startTagStart + 1;
    }
    return new ErrorSentence(normalized, makeAnnotatedText(normalized), errors);
  }

  private AnnotatedText makeAnnotatedText(String pseudoXml) {
    AnnotatedTextBuilder builder = new AnnotatedTextBuilder();
    StringTokenizer tokenizer = new StringTokenizer(pseudoXml, "<>", true);
    boolean inMarkup = false;
    while (tokenizer.hasMoreTokens()) {
      String part = tokenizer.nextToken();
      if (part.startsWith("<")) {
        builder.addMarkup(part);
        inMarkup = true;
      } else if (part.startsWith(">")) {
        inMarkup = false;
        builder.addMarkup(part);
      } else {
        if (inMarkup) {
          builder.addMarkup(part);
        } else {
          builder.addText(part);
        }
      }
    }
    return builder.build();
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1 && args.length != 2) {
      System.out.println("Usage: " + RealWordCorpusEvaluator.class.getSimpleName() + " <corpusDirectory> [languageModel]");
      System.out.println("   [languageModel] is a morfologik file with ngram frequency information (optional)");
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

  class ErrorSentence {
    private final String markupText;
    private final AnnotatedText annotatedText;
    private final List<Error> errors;

    ErrorSentence(String markupText, AnnotatedText annotatedText, List<Error> errors) {
      this.markupText = markupText;
      this.annotatedText = annotatedText;
      this.errors = errors;
    }

    boolean hasErrorCoveredByMatchAndGoodFirstSuggestion(RuleMatch match) {
      if (hasErrorCoveredByMatch(match)) {
        List<String> suggestion = match.getSuggestedReplacements();
        if (suggestion.size() > 0) {
          String firstSuggestion = suggestion.get(0);
          for (Error error : errors) {
            if (error.correction.equals(firstSuggestion)) {
              return true;
            }
          }
        }
      }
      return false;
    }
    
    boolean hasErrorCoveredByMatch(RuleMatch match) {
      for (Error error : errors) {
        if (match.getFromPos() <= error.startPos && match.getToPos() >= error.endPos) {
          return true;
        }
      }
      return false;
    }
  }
  
  class Error {
    private final int startPos;
    private final int endPos;
    private final String correction;

    Error(int startPos, int endPos, String correction) {
      this.startPos = startPos;
      this.endPos = endPos;
      this.correction = correction;
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
