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
import org.languagetool.dev.dumpcheck.MixingSentenceSource;
import org.languagetool.dev.dumpcheck.Sentence;
import org.languagetool.dev.dumpcheck.SentenceSource;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;

/**
 * Loads sentences with a homophone (e.g. there/their) from Wikipedia or confusion set files TODO
 * and evaluates EnglishConfusionProbabilityRule with them.
 * 
 * @author Markus Brenneis
 */
class NeuralNetworkRuleEvaluator {

    private static final List<Double> EVAL_CERTAINTIES = Collections.singletonList(.5); //Arrays.asList(.5, .6, .75, 1.0, 1.2); TODO
    private static final int MAX_SENTENCES = 1000;

    private final Language language;
    private final Rule rule;
    private final Map<Double, EvalValues> evalValues = new HashMap<>();

    private boolean verbose = true;

    private NeuralNetworkRuleEvaluator(Language language, String ruleId) throws IOException {
        this.language = language;
        List<Rule> rules = language.getRelevantRules(JLanguageTool.getMessageBundle());
        rule = rules.stream().filter(r -> r.getId().equals(ruleId)).findFirst().orElse(null);
        if (rule == null) {
            throw new IllegalArgumentException("Language " + language + " has no rule with id " + ruleId);
        }
    }

    private Map<Double, ConfusionRuleEvaluator.EvalResult> run(List<String> inputsOrDir, String token1, String token2, int maxSentences, List<Double> evalCertainties) throws IOException {
        for (Double evalFactor : evalCertainties) {
            evalValues.put(evalFactor, new EvalValues());
        }
        List<Sentence> allToken1Sentences = getRelevantSentences(inputsOrDir, token1, maxSentences);
        List<Sentence> allToken2Sentences = getRelevantSentences(inputsOrDir, token2, maxSentences);
        evaluate(allToken1Sentences, true, token1, token2, evalCertainties);
        evaluate(allToken1Sentences, false, token2, token1, evalCertainties);
        evaluate(allToken2Sentences, false, token1, token2, evalCertainties);
        evaluate(allToken2Sentences, true, token2, token1, evalCertainties);
        return printEvalResult(allToken1Sentences, allToken2Sentences, token1, token2);
    }

    private void evaluate(List<Sentence> sentences, boolean isCorrect, String token1, String token2, List<Double> evalFactors) throws IOException {
        println("======================");
        printf("Starting evaluation on " + sentences.size() + " sentences with %s/%s:\n", token1, token2);
        JLanguageTool lt = new JLanguageTool(language);
        disableAllRules(lt);
        for (Sentence sentence : sentences) {
            evaluateSentence(isCorrect, token1, token2, evalFactors, lt, sentence);
        }
    }

    private void evaluateSentence(boolean isCorrect, String token1, String token2, List<Double> evalFactors, JLanguageTool lt, Sentence sentence) throws IOException {
        String textToken = isCorrect ? token1 : token2;
        String plainText = sentence.getText();
        String replacement = plainText.indexOf(textToken) == 0 ? StringTools.uppercaseFirstChar(token1) : token1;
        String replacedTokenSentence = isCorrect ? plainText : plainText.replaceFirst("(?i)\\b" + textToken + "\\b", replacement);
        AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(replacedTokenSentence);
        for (Double factor : evalFactors) {
            evaluateSentenceWithFactor(isCorrect, textToken, plainText, replacement, analyzedSentence, factor);
        }
    }

    private void evaluateSentenceWithFactor(boolean isCorrect, String textToken, String plainText, String replacement, AnalyzedSentence analyzedSentence, Double factor) throws IOException {
        RuleMatch[] matches = rule.match(analyzedSentence);
        boolean consideredCorrect = matches.length == 0;
        String displayStr = plainText.replaceFirst("(?i)\\b" + textToken + "\\b", "**" + replacement + "**");
        if (isCorrect) {
            if (consideredCorrect) {
                evalValues.get(factor).trueNegatives++;
//                println("true negative: " + displayStr);
            } else {
                evalValues.get(factor).falsePositives++;
                println("false positive with factor " + factor + ": " + displayStr);
            }
        } else {
            if (consideredCorrect) {
                println("false negative: " + displayStr);
                evalValues.get(factor).falseNegatives++;
            } else {
                evalValues.get(factor).truePositives++;
//                println("true positive: " + displayStr);
            }
        }
    }

    private static void disableAllRules(JLanguageTool lt) {
        List<Rule> allActiveRules = lt.getAllActiveRules();
        for (Rule activeRule : allActiveRules) {
            lt.disableRule(activeRule.getId());
        }
    }

    private Map<Double,ConfusionRuleEvaluator.EvalResult> printEvalResult(List<Sentence> allTokenSentences, List<Sentence> allHomophoneSentences,
                                                                        String token, String homophoneToken) {
        Map<Double,ConfusionRuleEvaluator.EvalResult> results = new HashMap<>();
        int sentences = allTokenSentences.size() + allHomophoneSentences.size();
        System.out.println("\nEvaluation results for " + token + "/" + homophoneToken
                + " with " + sentences + " sentences as of " + new Date() + ":");
        List<Double> certainties = evalValues.keySet().stream().sorted().collect(toList());
        for (Double certainty : certainties) {
            EvalValues certaintyEvalValues = evalValues.get(certainty);
            float precision = (float)certaintyEvalValues.truePositives / (certaintyEvalValues.truePositives + certaintyEvalValues.falsePositives);
            float recall = (float) certaintyEvalValues.truePositives / (certaintyEvalValues.truePositives + certaintyEvalValues.falseNegatives);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String summary = String.format(ENGLISH, "%s; %s; %f4.2; # p=%.3f, r=%.3f, %d+%d, %s",
                    token, homophoneToken, certainty, precision, recall, allTokenSentences.size(), allHomophoneSentences.size(), date);
            results.put(certainty, new ConfusionRuleEvaluator.EvalResult(summary, precision, recall));
            if (verbose) {
                System.out.println();
                System.out.printf(ENGLISH, "Certainty: %f4.2 - %d false positives, %d false negatives, %d true positives, %d true negatives\n",
                        certainty, certaintyEvalValues.falsePositives, certaintyEvalValues.falseNegatives, certaintyEvalValues.truePositives, certaintyEvalValues.trueNegatives);
                System.out.printf(summary + "\n");
            }
        }
        return results;
    }

    private List<Sentence> getRelevantSentences(List<String> inputs, String token, int maxSentences) throws IOException {
        SentenceSource sentenceSource = MixingSentenceSource.create(inputs, language);
        return getSentencesFromSource(inputs, token, maxSentences, sentenceSource);
    }

    private List<Sentence> getSentencesFromSource(List<String> inputs, String token, int maxSentences, SentenceSource sentenceSource) {
        List<Sentence> sentences = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*\\b" + token + "\\b.*");
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
        if (args.length < 5) {
            System.err.println("Usage: " + ConfusionRuleEvaluator.class.getSimpleName()
                    + " <token1> <token2> <langCode> <ruleId> <wikipediaXml|tatoebaFile|plainTextFile|dir>...");
            System.err.println("   <wikipediaXml|tatoebaFile|plainTextFile> either a Wikipedia XML dump, or a Tatoeba file, or");
            System.err.println("                      a plain text file with one sentence per line, a Wikipedia or Tatoeba file");
            System.exit(1);
        }
        long startTime = System.currentTimeMillis();
        String token1 = args[0];
        String token2 = args[1];
        String langCode = args[2];
        String ruleId = args[3];
        Language lang;
        lang = Languages.getLanguageForShortCode(langCode);
        List<String> inputsFiles = Arrays.stream(args).skip(4).collect(toList());
        NeuralNetworkRuleEvaluator generator = new NeuralNetworkRuleEvaluator(lang, ruleId);
        generator.run(inputsFiles, token1, token2, MAX_SENTENCES, EVAL_CERTAINTIES);
        long endTime = System.currentTimeMillis();
        System.out.println("\nTime: " + (endTime-startTime)+"ms");
    }

    static class EvalValues { // TODO share class
        private int truePositives = 0;
        private int trueNegatives = 0;
        private int falsePositives = 0;
        private int falseNegatives = 0;
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
