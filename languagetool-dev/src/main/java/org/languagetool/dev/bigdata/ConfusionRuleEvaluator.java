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
import org.languagetool.chunking.Chunker;
import org.languagetool.dev.dumpcheck.*;
import org.languagetool.dev.eval.FMeasure;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.ConfusionProbabilityRule;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;

/**
 * Loads sentences with a homophone (e.g. there/their) from Wikipedia or confusion set files
 * and evaluates EnglishConfusionProbabilityRule with them.
 *
 * @since 3.0
 * @author Daniel Naber 
 */
class ConfusionRuleEvaluator {

  private static final String TOKEN = "there";
  private static final String TOKEN_HOMOPHONE = "their";
  private static final int FACTOR = 100;
  private static final boolean CASE_SENSITIVE = false;
  private static final int NGRAM_LEVEL = 3;
  private static final int MAX_SENTENCES = 1000;

  private final Language language;
  private final ConfusionProbabilityRule rule;
  private final int grams;

  private int truePositives = 0;
  private int trueNegatives = 0;
  private int falsePositives = 0;
  private int falseNegatives = 0;
  private boolean verbose = true;

  ConfusionRuleEvaluator(Language language, LanguageModel languageModel, int grams) {
    this.language = language;
    try {
      List<Rule> rules = language.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), languageModel);
      if (rules == null) {
        throw new RuntimeException("Language " + language + " doesn't seem to support a language model");
      }
      if (rules.size() > 1) {
        throw new RuntimeException("Language " + language + " has more than one language model rule, this is not supported yet");
      }
      this.rule = (ConfusionProbabilityRule)rules.get(0);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.grams = grams;
  }
  
  void setVerboseMode(boolean verbose) {
    this.verbose = verbose;
  }

  String run(List<String> inputsOrDir, String token, String homophoneToken, long factor, int maxSentences) throws IOException {
    truePositives = 0;
    trueNegatives = 0;
    falsePositives = 0;
    falseNegatives = 0;
    rule.setConfusionSet(new ConfusionSet(factor, homophoneToken, token));
    List<Sentence> allTokenSentences = getRelevantSentences(inputsOrDir, token, maxSentences);
    // Load the sentences with a homophone and later replace it so we get error sentences:
    List<Sentence> allHomophoneSentences = getRelevantSentences(inputsOrDir, homophoneToken, maxSentences);
    evaluate(allTokenSentences, true, token, homophoneToken);
    evaluate(allTokenSentences, false, homophoneToken, token);
    evaluate(allHomophoneSentences, false, token, homophoneToken);
    evaluate(allHomophoneSentences, true, homophoneToken, token);
    return printEvalResult(allTokenSentences, allHomophoneSentences, inputsOrDir, factor);
  }

  @SuppressWarnings("ConstantConditions")
  private void evaluate(List<Sentence> sentences, boolean isCorrect, String token, String homophoneToken) throws IOException {
    println("======================");
    printf("Starting evaluation on " + sentences.size() + " sentences with %s/%s:\n", token, homophoneToken);
    JLanguageTool lt = new JLanguageTool(language);
    List<Rule> allActiveRules = lt.getAllActiveRules();
    for (Rule activeRule : allActiveRules) {
      lt.disableRule(activeRule.getId());
    }
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
        println("false positive: " + displayStr);
      } else if (consideredCorrect && !isCorrect) {
        //println("false negative: " + displayStr);
        falseNegatives++;
      } else {
        truePositives++;
        //System.out.println("true positive: " + displayStr);
      }
    }
  }

  private String printEvalResult(List<Sentence> allTokenSentences, List<Sentence> allHomophoneSentences, List<String> inputsOrDir, long factor) {
    float precision = (float) truePositives / (truePositives + falsePositives);
    float recall = (float) truePositives / (truePositives + falseNegatives);
    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    String summary = String.format(ENGLISH, "p=%.3f, r=%.3f, %d, %dgrams, %s",
            precision, recall, allTokenSentences.size() + allHomophoneSentences.size(), grams, date);
    if (verbose) {
      int sentences = allTokenSentences.size() + allHomophoneSentences.size();
      System.out.println("======================");
      System.out.println("Evaluation results for " + TOKEN + "/" + TOKEN_HOMOPHONE
              + " with " + sentences + " sentences as of " + new Date() + ":");
      System.out.printf(ENGLISH, "  Precision:    %.3f (%d false positives)\n", precision, falsePositives);
      System.out.printf(ENGLISH, "  Recall:       %.3f (%d false negatives)\n", recall, falseNegatives);
      double fMeasure = FMeasure.getWeightedFMeasure(precision, recall);
      System.out.printf(ENGLISH, "  F-measure:    %.3f (beta=0.5)\n", fMeasure);
      System.out.printf(ENGLISH, "  Good Matches: %d (true positives)\n", truePositives);
      System.out.printf(ENGLISH, "  All matches:  %d\n", truePositives + falsePositives);
      System.out.printf(ENGLISH, "  Factor:       %d\n", factor);
      System.out.printf(ENGLISH, "  Case sensit.: %s\n", CASE_SENSITIVE);
      System.out.printf(ENGLISH, "  Inputs:       %s\n", inputsOrDir);
      System.out.printf("  Summary:      " + summary + "\n");
    }
    return summary;
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
    Pattern pattern = Pattern.compile(".*\\b" + (CASE_SENSITIVE ? token : token.toLowerCase()) + "\\b.*");
    while (sentenceSource.hasNext()) {
      Sentence sentence = sentenceSource.next();
      String sentenceText = CASE_SENSITIVE ? sentence.getText() : sentence.getText().toLowerCase();
      Matcher matcher = pattern.matcher(sentenceText);
      if (matcher.matches()) {
        sentences.add(sentence);
        if (sentences.size() % 250 == 0) {
          println("Loaded sentence " + sentences.size() + " with '" + token + "' from " + inputs);
        }
        if (sentences.size() >= maxSentences) {
          break;
        }
      }
    }
    println("Loaded " + sentences.size() + " sentences with '" + token + "' from " + inputs);
    return sentences;
  }
  
  private void println(String msg) {
    if (verbose) {
      System.out.println(msg);
    }
  }

  private void printf(String msg, String... args) {
    if (verbose) {
      System.out.printf(msg, args);
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 3 || args.length > 4) {
      System.err.println("Usage: " + ConfusionRuleEvaluator.class.getSimpleName()
              + " <langCode> <languageModelTopDir> <wikipediaXml|tatoebaFile|dir>...");
      System.err.println("   <languageModelTopDir> is a directory with sub-directories '1grams', '2grams' and '3grams' with Lucene indexes");
      System.err.println("   <wikipediaXml|tatoebaFile|dir> either a Wikipedia XML dump, or a Tatoeba file or");
      System.err.println("                      a directory with example sentences (where <word>.txt contains only the sentences for <word>).");
      System.err.println("                      You can specify both a Wikipedia file and a Tatoeba file.");
      System.exit(1);
    }
    long startTime = System.currentTimeMillis();
    String langCode = args[0];
    Language lang;
    if ("en".equals(langCode)) {
      lang = new EnglishLight();
    } else {
      lang = Languages.getLanguageForShortName(langCode);
    }
    LanguageModel languageModel = new LuceneLanguageModel(new File(args[1], lang.getShortName()));
    List<String> inputsFiles = new ArrayList<>();
    inputsFiles.add(args[2]);
    if (args.length >= 4) {
      inputsFiles.add(args[3]);
    }
    ConfusionRuleEvaluator generator = new ConfusionRuleEvaluator(lang, languageModel, NGRAM_LEVEL);
    generator.run(inputsFiles, TOKEN, TOKEN_HOMOPHONE, FACTOR, MAX_SENTENCES);
    long endTime = System.currentTimeMillis();
    System.out.println("Time: " + (endTime-startTime)+"ms");
  }
  
  // faster version of English as it uses no chunking:
  static class EnglishLight extends English {
    
    private DemoTagger tagger;

    @Override
    public String getName() {
      return "English Light";
    }
    
    @Override
    public Tagger getTagger() {
      if (tagger == null) {
        tagger = new DemoTagger();
      }
      return tagger;
    }

    @Override
    public Chunker getChunker() {
      return null;
    }
  }
}
