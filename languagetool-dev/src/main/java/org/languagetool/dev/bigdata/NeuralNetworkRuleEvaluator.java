/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Markus Brenneis
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
import org.languagetool.dev.dumpcheck.MixingSentenceSource;
import org.languagetool.dev.dumpcheck.Sentence;
import org.languagetool.dev.dumpcheck.SentenceSource;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;

/**
 * Loads sentences containing the given tokens (e.g. there/their) from Wikipedia or other sentence sources and evaluates
 * a NeuralNetworkRule with them.
 *
 * @author Markus Brenneis
 */
class NeuralNetworkRuleEvaluator {

  private static final List<Double> EVAL_MIN_SCORES = Arrays.asList(.5, .75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75, 3.0, 3.25, 3.5, 3.75, 4.0);
  private static final int MAX_SENTENCES = 5000;
  private static final float RECOMMENDED_MIN_PRECISION = 0.99f;

  private final Language language;
  private final List<NeuralNetworkRule> rules;
  private final Map<Double, EvalValues> evalValues1 = new HashMap<>();
  private final Map<Double, EvalValues> evalValues2 = new HashMap<>();

  private boolean verbose = true;

  private NeuralNetworkRuleEvaluator(Language language, File word2vecDir, String ruleId) throws IOException {
    this.language = language;
    Word2VecModel word2vecModel = language.getWord2VecModel(word2vecDir);
    List<Rule> allRules = language.getRelevantWord2VecModelRules(JLanguageTool.getMessageBundle(), word2vecModel);
    rules = allRules.stream()
            .filter(r -> r instanceof NeuralNetworkRule)
            .map(r -> (NeuralNetworkRule) r)
            .filter(r -> ruleId.equals("ALL") || ruleId.contains(r.getId()))
            .collect(toList());
    if (rules.isEmpty()) {
      throw new IllegalArgumentException("Language " + language + " has no neural network rule with id " + ruleId);
    }
  }

  private List<Map<Double, EvalResult>> runAll(List<String> inputsOrDir, int maxSentences, List<Double> evalMinScores) {
    return rules.stream().map(rule -> run(rule, inputsOrDir, maxSentences, evalMinScores)).collect(toList());
  }

  private Map<Double, EvalResult> run(NeuralNetworkRule rule, List<String> inputsOrDir, int maxSentences, List<Double> evalMinScores) {
    for (Double evalFactor : evalMinScores) {
      evalValues1.put(evalFactor, new EvalValues());
      evalValues2.put(evalFactor, new EvalValues());
    }
    String token1 = rule.getSubjects().get(0);
    String token2 = rule.getSubjects().get(1);
    List<Sentence> allToken1Sentences = getRelevantSentences(inputsOrDir, token1, maxSentences);
    List<Sentence> allToken2Sentences = getRelevantSentences(inputsOrDir, token2, maxSentences);
    evaluate(rule, allToken1Sentences, true, token1, token2, evalValues1, evalMinScores);
    evaluate(rule, allToken1Sentences, false, token2, token1, evalValues1, evalMinScores);
    evaluate(rule, allToken2Sentences, true, token2, token1, evalValues2, evalMinScores);
    evaluate(rule, allToken2Sentences, false, token1, token2, evalValues2, evalMinScores);
    return printEvalResult(allToken1Sentences, allToken2Sentences, token1, token2);
  }

  private void evaluate(NeuralNetworkRule rule, List<Sentence> sentences, boolean token1IsCorrect, String token1, String token2, Map<Double, EvalValues> evalValues, List<Double> evalMinScores) {
    println("======================");
    printf("Starting evaluation on " + sentences.size() + " sentences with %s/%s:\n", token1, token2);
    JLanguageTool lt = new JLanguageTool(language);
    disableAllRules(lt);
    for (Sentence sentence : sentences) {
      evaluateSentence(rule, token1IsCorrect, token1, token2, evalValues, evalMinScores, lt, sentence);
    }
  }

  private void evaluateSentence(NeuralNetworkRule rule, boolean token1IsCorrect, String token1, String token2, Map<Double, EvalValues> evalValues, List<Double> evalMinScores, JLanguageTool lt, Sentence sentence) {
    String textToken = token1IsCorrect ? token1 : token2;
    String plainText = sentence.getText();
    String replacement = token1;
    String replacedTokenSentence = token1IsCorrect ? plainText : plainText.replaceFirst("(?i)\\b" + textToken + "\\b", replacement);
    try {
      AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(replacedTokenSentence);
      for (Double minScore : evalMinScores) {
        evaluateSentenceWithMinScore(rule, token1IsCorrect, textToken, plainText, replacement, analyzedSentence, evalValues, minScore);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error while analyzing sentence", e);
    }
  }

  private void evaluateSentenceWithMinScore(NeuralNetworkRule rule, boolean textTokenIsCorrect, String textToken, String plainText, String replacement, AnalyzedSentence analyzedSentence, Map<Double, EvalValues> evalValues, Double minScore) throws IOException {
    rule.setMinScore(minScore);
    RuleMatch[] matches = rule.match(analyzedSentence);
    boolean consideredCorrect = matches.length == 0;
    String displayStr = plainText.replaceFirst("(?i)\\b" + textToken + "\\b", "**" + replacement + "**");
    if (textTokenIsCorrect) {
      if (consideredCorrect) {
        evalValues.get(minScore).trueNegatives++;
//        println("true negative: " + displayStr);
      } else {
        evalValues.get(minScore).falsePositives++;
        println("false positive with minScore " + minScore + ": " + displayStr);
      }
    } else {
      if (consideredCorrect) {
        evalValues.get(minScore).falseNegatives++;
//        println("false negative with minScore " + minScore + ": " + displayStr);
      } else {
        evalValues.get(minScore).truePositives++;
//        println("true positive: " + displayStr);
      }
    }
  }

  private static void disableAllRules(JLanguageTool lt) {
    List<Rule> allActiveRules = lt.getAllActiveRules();
    for (Rule activeRule : allActiveRules) {
      lt.disableRule(activeRule.getId());
    }
  }

  private Map<Double, EvalResult> printEvalResult(List<Sentence> allToken1Sentences, List<Sentence> allToken2Sentences,
                                                  String token1, String token2) {
    Map<Double, EvalResult> results = new HashMap<>();
    int sentences = allToken1Sentences.size() + allToken2Sentences.size();
    System.out.println("\nEvaluation results for " + token1 + "/" + token2
            + " with " + sentences + " sentences as of " + new Date() + ":");

    System.out.println("Results for " + token1 + " (where " + token1 + " is correctly used or " + token1 + " must be suggested)");
    evalValues1.keySet().stream()
            .sorted()
            .map(certainty -> new EvalResult(allToken1Sentences, allToken2Sentences, token1, token2, evalValues1.get(certainty), certainty))
            .forEach(evalResult -> System.out.println(evalResult.getSummary()));
    System.out.println();

    System.out.println("Results for " + token2 + " (where " + token2 + " is correctly used or " + token2 + " must be suggested)");
    evalValues2.keySet().stream()
            .sorted()
            .map(certainty -> new EvalResult(allToken1Sentences, allToken2Sentences, token1, token2, evalValues2.get(certainty), certainty))
            .forEach(evalResult -> System.out.println(evalResult.getSummary()));
    System.out.println();

    System.out.println("Results for both tokens");
    evalValues1.keySet().stream()
            .sorted()
            .forEach(certainty -> results.put(certainty, new EvalResult(allToken1Sentences, allToken2Sentences, token1, token2, evalValues1.get(certainty).plus(evalValues2.get(certainty)), certainty)));

    results.keySet().stream()
            .sorted()
            .forEach(certainty -> System.out.println(results.get(certainty).getSummary()));

    return results;
  }

  private List<Sentence> getRelevantSentences(List<String> inputs, String token, int maxSentences) {
    SentenceSource sentenceSource;
    try {
      sentenceSource = MixingSentenceSource.create(inputs, language);
    } catch (IOException e) {
      throw new RuntimeException("Error while loading sentence source", e);
    }
    return getSentencesFromSource(inputs, token, maxSentences, sentenceSource);
  }

  private List<Sentence> getSentencesFromSource(List<String> inputs, String token, int maxSentences, SentenceSource sentenceSource) {
    List<Sentence> sentences = new ArrayList<>();
    Pattern pattern = Pattern.compile(".*[^-]\\b" + token + "\\b[^-].*");
    while (sentenceSource.hasNext()) {
      Sentence sentence = sentenceSource.next();
      String sentenceText = sentence.getText();
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
    if (args.length < 4) {
      System.err.println("Usage: " + NeuralNetworkRuleEvaluator.class.getSimpleName()
              + " <langCode> <word2vecDir> <ruleId> <wikipediaXml|tatoebaFile|plainTextFile|dir>...");
      System.err.println("   <word2vecDir> is a directory with sub-directories 'en' etc. with dictionary.txt and final_embeddings.txt");
      System.err.println("   <ruleId> id of a NeuralNetworkRule or ALL for evaluating all NeuralNetworkRules");
      System.err.println("   <wikipediaXml|tatoebaFile|plainTextFile> either a Wikipedia XML dump, or a Tatoeba file, or");
      System.err.println("                      a plain text file with one sentence per line, a Wikipedia or Tatoeba file");
      System.exit(1);
    }
    long startTime = System.currentTimeMillis();
    String langCode = args[0];
    File word2vecDir = new File(args[1]);
    String ruleId = args[2];
    Language lang;
    lang = Languages.getLanguageForShortCode(langCode);
    List<String> inputsFiles = Arrays.stream(args).skip(3).collect(toList());
    NeuralNetworkRuleEvaluator generator = new NeuralNetworkRuleEvaluator(lang, word2vecDir, ruleId);
    List<Map<Double, EvalResult>> evaluationResults = generator.runAll(inputsFiles, MAX_SENTENCES, EVAL_MIN_SCORES);
    long endTime = System.currentTimeMillis();
    System.out.println("\nTime: " + (endTime - startTime) + "ms");

    System.out.println("Recommended configuration:");
    System.out.println(ConfusionFileIndenter.indent(confusionSetConfig(evaluationResults, RECOMMENDED_MIN_PRECISION)));
  }

  private static List<String> confusionSetConfig(List<Map<Double, EvalResult>> evaluationResults, float minPrecision) {
    return evaluationResults.stream()
            .map(evaluationResult -> confusionSetConfig(evaluationResult, minPrecision))
            .collect(toList());
  }

  static String confusionSetConfig(Map<Double, EvalResult> evaluationResults, float minPrecision) {
    return evaluationResults.keySet().stream()
            .sorted()
            .map(evaluationResults::get)
            .filter(evalResult -> evalResult.getPrecision() >= minPrecision)
            .map(EvalResult::getSummary)
            .findFirst()
            .orElse("###");
  }

  static class EvalValues { // TODO share class
    private int truePositives = 0;
    private int trueNegatives = 0;
    private int falsePositives = 0;
    private int falseNegatives = 0;

    EvalValues() {
    }

    EvalValues(int truePositives, int trueNegatives, int falsePositives, int falseNegatives) {
      this.truePositives = truePositives;
      this.trueNegatives = trueNegatives;
      this.falsePositives = falsePositives;
      this.falseNegatives = falseNegatives;
    }

    EvalValues plus(EvalValues that) {
      return new EvalValues(this.truePositives + that.truePositives,
              this.trueNegatives + that.trueNegatives,
              this.falsePositives + that.falsePositives,
              this.falseNegatives + that.falseNegatives);
    }
  }

  static class EvalResult { // TODO share class

    private final String summary;
    private final float precision;
    private final float recall;

    EvalResult(String summary, float precision, float recall) {
      this.summary = summary;
      this.precision = precision;
      this.recall = recall;
    }

    EvalResult (List<Sentence> allToken1Sentences, List<Sentence> allToken2Sentences, String token1, String token2, EvalValues certaintyEvalValues, Double certainty) {
      float precision = (float) certaintyEvalValues.truePositives / (certaintyEvalValues.truePositives + certaintyEvalValues.falsePositives);
      float recall = (float) certaintyEvalValues.truePositives / (certaintyEvalValues.truePositives + certaintyEvalValues.falseNegatives);
      String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
      this.summary = String.format(ENGLISH, "%s; %s; %4.2f # p=%.3f, r=%.3f, tp=%d, tn=%d, fp=%d, fn=%d, %d+%d, %s",
              token1, token2, certainty, precision, recall, certaintyEvalValues.truePositives, certaintyEvalValues.trueNegatives,
              certaintyEvalValues.falsePositives, certaintyEvalValues.falseNegatives, allToken1Sentences.size(), allToken2Sentences.size(), date);
      this.precision = precision;
      this.recall = recall;
    }

    String getSummary() {
      return summary;
    }

    float getPrecision() {
      return precision;
    }

    float getRecall() {
      return recall;
    }
  }
}
