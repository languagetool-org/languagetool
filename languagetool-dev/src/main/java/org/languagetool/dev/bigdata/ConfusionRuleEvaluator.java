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
package org.languagetool.dev.bigdata;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.dev.dumpcheck.*;
import org.languagetool.dev.eval.FMeasure;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.ConfusionProbabilityRule;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.EnglishConfusionProbabilityRule;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Locale.ENGLISH;

/**
 * Loads sentences with a homophone (e.g. 'there') from Wikipedia or confusion set files
 * and evaluates EnglishConfusionProbabilityRule with them.
 *
 * Evaluation results for there/their with 2000 sentences as of Mon May 25 15:54:41 CEST 2015:
 * Precision: 0.998 (3 false positives)
 * Recall:    0.970 (60 false negatives)
 * F-measure: 0.993 (beta=0.5)
 * Matches:   1940 (true positives)
 * Inputs:    [/media/Data/wikipedia/en/enwiki-20140811-pages-articles-multistream_part.xml, /media/Data/tatoeba/tatoeba-en.txt]
 *
 * @since 3.0
 * @author Daniel Naber 
 */
class ConfusionRuleEvaluator {

  private static final String TOKEN = "there";
  private static final String TOKEN_HOMOPHONE = "their";
  private static final int FACTOR = 100;
  
  private static final int MAX_SENTENCES = 1000;

  private final Language language;
  private final ConfusionProbabilityRule rule;
  private final int grams;

  private int truePositives = 0;
  private int trueNegatives = 0;
  private int falsePositives = 0;
  private int falseNegatives = 0;

  private ConfusionRuleEvaluator(Language language, LanguageModel languageModel, int grams) {
    this.language = language;
    this.rule = new EnglishConfusionProbabilityRule(JLanguageTool.getMessageBundle(), languageModel, language, grams);
    rule.setConfusionSet(new ConfusionSet(FACTOR, TOKEN_HOMOPHONE, TOKEN));
    this.grams = grams;
  }

  private void run(List<String> inputsOrDir, String token, String homophoneToken, int maxSentences) throws IOException {
    List<Sentence> allTokenSentences = getRelevantSentences(inputsOrDir, token, maxSentences);
    // Load the sentences with a homophone and later replace it so we get error sentences:
    List<Sentence> allHomophoneSentences = getRelevantSentences(inputsOrDir, homophoneToken, maxSentences);
    evaluate(allTokenSentences, true, token, homophoneToken);
    evaluate(allTokenSentences, false, homophoneToken, token);
    evaluate(allHomophoneSentences, false, token, homophoneToken);
    evaluate(allHomophoneSentences, true, homophoneToken, token);
    printEvalResult(allTokenSentences, allHomophoneSentences, inputsOrDir);
  }

  @SuppressWarnings("ConstantConditions")
  private void evaluate(List<Sentence> sentences, boolean isCorrect, String token, String homophoneToken) throws IOException {
    System.out.println("======================");
    System.out.printf("Starting evaluation on " + sentences.size() + " sentences with %s/%s:\n", token, homophoneToken);
    JLanguageTool lt = new JLanguageTool(new English());
    for (Sentence sentence : sentences) {
      String textToken = isCorrect ? token : homophoneToken;
      String plainText = sentence.getText();
      String replacement = plainText.indexOf(textToken) == 0 ? StringTools.uppercaseFirstChar(token) : token;
      String replacedTokenSentence = isCorrect ? plainText : plainText.replaceFirst("(?i)\\b" + textToken + "\\b", replacement);
      AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(replacedTokenSentence);
      RuleMatch[] matches = rule.match(analyzedSentence);
      boolean consideredCorrect = matches.length == 0;
      String displayStr = plainText.replaceFirst("(?i)\\b" + textToken + "\\b", "**" + replacement + "**");
      if (consideredCorrect && isCorrect) {
        trueNegatives++;
      } else if (!consideredCorrect && isCorrect) {
        falsePositives++;
        System.out.println("false positive: " + displayStr);
      } else if (consideredCorrect && !isCorrect) {
        falseNegatives++;
      } else {
        truePositives++;
        //System.out.println("true positive: " + displayStr);
      }
    }
  }

  private void printEvalResult(List<Sentence> allTokenSentences, List<Sentence> allHomophoneSentences, List<String> inputsOrDir) {
    int sentences = allTokenSentences.size() + allHomophoneSentences.size();
    System.out.println("======================");
    System.out.println("Evaluation results for " + TOKEN + "/" + TOKEN_HOMOPHONE
            + " with " + sentences + " sentences as of " + new Date() + ":");
    float precision = (float) truePositives / (truePositives + falsePositives);
    float recall = (float) truePositives / (truePositives + falseNegatives);
    double fMeasure = FMeasure.getWeightedFMeasure(precision, recall);
    System.out.printf(ENGLISH, "  Precision: %.3f (%d false positives)\n", precision, falsePositives);
    System.out.printf(ENGLISH, "  Recall:    %.3f (%d false negatives)\n", recall, falseNegatives);
    System.out.printf(ENGLISH, "  F-measure: %.3f (beta=0.5)\n", fMeasure);
    System.out.printf(ENGLISH, "  Matches:   %d (true positives)\n", truePositives);
    System.out.printf(ENGLISH, "  Inputs:    %s\n", inputsOrDir);
    System.out.printf(ENGLISH, "  Summary:   precision=%.3f, recall=%.3f (%s) using %dgrams\n",
            precision, recall, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), grams);
  }

  private List<Sentence> getRelevantSentences(List<String> inputs, String token, int maxSentences) throws IOException {
    List<Sentence> sentences = new ArrayList<>();
    for (String input : inputs) {
      if (new File(input).isDirectory()) {
        File file = new File(input, token + ".txt");
        if (!file.exists()) {
          throw new RuntimeException("File with example sentences not found: " + file);
        }
        try (FileInputStream fis = new FileInputStream(file)) {
          SentenceSource sentenceSource = new PlainTextSentenceSource(fis, language);
          sentences = getSentencesFromSource(inputs, token, maxSentences, sentenceSource);
        }
      } else {
        SentenceSource sentenceSource = MixingSentenceSource.create(inputs, language);
        sentences = getSentencesFromSource(inputs, token, maxSentences, sentenceSource);
      }
    }
    return sentences;
  }

  private List<Sentence> getSentencesFromSource(List<String> inputs, String token, int maxSentences, SentenceSource sentenceSource) {
    List<Sentence> sentences = new ArrayList<>();
    while (sentenceSource.hasNext()) {
      Sentence sentence = sentenceSource.next();
      if (sentence.getText().toLowerCase().matches(".*\\b" + token + "\\b.*")) {
        sentences.add(sentence);
        if (sentences.size() % 100 == 0) {
          System.out.println("Loaded sentence " + sentences.size() + " with '" + token + "' from " + inputs);
        }
        if (sentences.size() >= maxSentences) {
          break;
        }
      }
    }
    System.out.println("Loaded " + sentences.size() + " sentences with '" + token + "' from " + inputs);
    return sentences;
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 3 || args.length > 4) {
      System.err.println("Usage: " + ConfusionRuleEvaluator.class.getSimpleName()
              + " <langCode> <languageModelTopDir> <wikipediaXml|tatoebaFile|dir>...");
      System.err.println("   <languageModelTopDir> is a directory with sub-directories '1grams', '2grams' and '3grams' with Lucene indexes");
      System.err.println("   <wikipediaXml|tatoebaFile| dir> either a Wikipedia XML dump, or a Tatoeba file or");
      System.err.println("                      a directory with example sentences (where <word>.txt contains only the sentences for <word>).");
      System.err.println("                      You can specify both a Wikipedia file and a Tatoeba file.");
      System.exit(1);
    }
    Language lang = Languages.getLanguageForShortName(args[0]);
    LanguageModel languageModel = new LuceneLanguageModel(new File(args[1]));
    List<String> inputsFiles = new ArrayList<>();
    inputsFiles.add(args[2]);
    if (args.length >= 4) {
      inputsFiles.add(args[3]);
    }
    ConfusionRuleEvaluator generator = new ConfusionRuleEvaluator(lang, languageModel, 3);
    generator.run(inputsFiles, TOKEN, TOKEN_HOMOPHONE, MAX_SENTENCES);
    //ConfusionRuleEvaluator generator2 = new ConfusionRuleEvaluator(lang, languageModel, 4);
    //generator2.run(inputsFiles, TOKEN, TOKEN_HOMOPHONE, MAX_SENTENCES);
  }
  
}
