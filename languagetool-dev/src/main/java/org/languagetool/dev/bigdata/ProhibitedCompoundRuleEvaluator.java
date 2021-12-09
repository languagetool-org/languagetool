/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.dev.bigdata;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.dev.dumpcheck.MixingSentenceSource;
import org.languagetool.dev.dumpcheck.PlainTextSentenceSource;
import org.languagetool.dev.dumpcheck.Sentence;
import org.languagetool.dev.dumpcheck.SentenceSource;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.de.ProhibitedCompoundRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;

/**
 * Loads sentences with with compounds that have a given token as a part and
 * evaluates this variant with another, easily confused variant via ProhibitedCompoundRule
 * @since 4.3
 * @author Fabian Richter
 */
class ProhibitedCompoundRuleEvaluator {
  // TODO maybe remove
  private static final List<Long> EVAL_FACTORS = Arrays.asList(10L);//, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L);
  private static final int MAX_SENTENCES = 10;//00;

  private final Language language;
  private final ProhibitedCompoundRule rule;
  private final Map<Long, RuleEvalValues> evalValues = new HashMap<>();
  private boolean verbose = true;

  ProhibitedCompoundRuleEvaluator(Language language, LanguageModel languageModel) {
    this.language = language;
    try {
      List<Rule> rules = language.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), languageModel, null);
      if (rules == null) {
        throw new RuntimeException("Language " + language + " doesn't seem to support a language model");
      }
      ProhibitedCompoundRule foundRule = null;
      for (Rule rule : rules) {
        if (rule.getId().equals(ProhibitedCompoundRule.RULE_ID)) {
          foundRule = (ProhibitedCompoundRule) rule;
          break;
        }
      }
      if (foundRule == null) {
        throw new RuntimeException("Language " + language + " has no language model rule with id " + ProhibitedCompoundRule.RULE_ID);
      } else {
        this.rule = foundRule;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void setVerboseMode(boolean verbose) {
    this.verbose = verbose;
  }

  Map<Long, RuleEvalResult> run(List<String> inputsOrDir, String token, String homophoneToken, int maxSentences, List<Long> evalFactors) throws IOException {
    for (Long evalFactor : evalFactors) {
      evalValues.put(evalFactor, new RuleEvalValues());
    }
    List<Map.Entry<Sentence, Map.Entry<Integer, Integer>>> allTokenSentences = getRelevantSentences(inputsOrDir, token, maxSentences);
    // Load the sentences with a homophone and later replace it so we get error sentences:
    List<Map.Entry<Sentence, Map.Entry<Integer, Integer>>> allHomophoneSentences = getRelevantSentences(inputsOrDir, homophoneToken, maxSentences);
    //if (allTokenSentences.size() < 20 || allHomophoneSentences.size() < 20) {
    //  System.out.println("Skipping " + token + " / " + homophoneToken);
    //  return null;
    //}
    evaluate(allTokenSentences, true, token, homophoneToken, evalFactors);
    evaluate(allTokenSentences, false, homophoneToken, token, evalFactors);
    evaluate(allHomophoneSentences, false, token, homophoneToken, evalFactors);
    evaluate(allHomophoneSentences, true, homophoneToken, token, evalFactors);
    return printRuleEvalResult(allTokenSentences, allHomophoneSentences, inputsOrDir, token, homophoneToken);
  }

  @SuppressWarnings("ConstantConditions")
  private void evaluate(List<Map.Entry<Sentence, Map.Entry<Integer, Integer>>> sentences, boolean isCorrect, String token, String homophoneToken, List<Long> evalFactors) throws IOException {
    println("======================");
    printf("Starting evaluation on " + sentences.size() + " sentences with %s/%s (%s):\n", token, homophoneToken, String.valueOf(isCorrect));
    JLanguageTool lt = new JLanguageTool(language);
    List<Rule> allActiveRules = lt.getAllActiveRules();
    for (Rule activeRule : allActiveRules) {
      lt.disableRule(activeRule.getId());
    }
    for (Map.Entry<Sentence, Map.Entry<Integer, Integer>> sentenceMatch : sentences) {
      Sentence sentence = sentenceMatch.getKey();
      String plainText = sentence.getText();
      int matchStart = sentenceMatch.getValue().getKey();
      int matchEnd = sentenceMatch.getValue().getValue();
      String match = plainText.substring(matchStart, matchEnd);
      String textToken = Character.isUpperCase(match.charAt(0)) ? StringUtils.capitalize(token) : StringUtils.uncapitalize(token);
      String evaluated = plainText;
      if (!isCorrect) {
        evaluated = plainText.substring(0, matchStart) + textToken + plainText.substring(matchEnd);
      }
      //printf("%nCorrect: %s%nPlain text: %s%nToken: %s%nHomophone: %s%nMatch: '%s'%nReplacement: %s%n%n", String.valueOf(isCorrect), plainText, token, homophoneToken, match, replacement);
      AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(evaluated);
      for (Long factor : evalFactors) {
        rule.setConfusionPair(new ProhibitedCompoundRule.Pair(homophoneToken, "", token, ""));
        RuleMatch[] matches = rule.match(analyzedSentence);
        String displayStr = plainText.substring(0, matchStart) + token.toUpperCase() + plainText.substring(matchStart + (isCorrect ? token.length() : homophoneToken.length()));
        boolean consideredCorrect = matches.length == 0;
        if (consideredCorrect && isCorrect) {
          evalValues.get(factor).trueNegatives++;
          //println("true negative: " + displayStr);
        } else if (!consideredCorrect && isCorrect) {
          evalValues.get(factor).falsePositives++;
          //println("false positive: " + displayStr);
        } else if (consideredCorrect && !isCorrect) {
          //println("false negative: " + displayStr);
          evalValues.get(factor).falseNegatives++;
        } else {
          evalValues.get(factor).truePositives++;
          //System.out.println("true positive: " + displayStr);
        }
      }
    }
  }

  private Map<Long,RuleEvalResult> printRuleEvalResult(List<Map.Entry<Sentence, Map.Entry<Integer, Integer>>> allTokenSentences, List<Map.Entry<Sentence, Map.Entry<Integer, Integer>>> allHomophoneSentences, List<String> inputsOrDir,
                                                       String token, String homophoneToken) {
    Map<Long,RuleEvalResult> results = new HashMap<>();
    int sentences = allTokenSentences.size() + allHomophoneSentences.size();
    System.out.println("\nEvaluation results for " + token + "/" + homophoneToken
      + " with " + sentences + " sentences as of " + new Date() + ":");
    System.out.printf(ENGLISH, "Inputs:       %s\n", inputsOrDir);
    List<Long> factors = evalValues.keySet().stream().sorted().collect(toList());
    for (Long factor : factors) {
      RuleEvalValues evalValues = this.evalValues.get(factor);
      float precision = (float)evalValues.truePositives / (evalValues.truePositives + evalValues.falsePositives);
      float recall = (float) evalValues.truePositives / (evalValues.truePositives + evalValues.falseNegatives);
      String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
      String spaces = StringUtils.repeat(" ", 82-Long.toString(factor).length());

      // alphabetical ordering for ConfusionSetLoader
      String firstToken = token, secondToken = homophoneToken;
      if (secondToken.compareTo(firstToken) < 0) {
        String tmp = firstToken;
        firstToken = secondToken;
        secondToken = tmp;
      }

      String summary = String.format(ENGLISH, "%s; %s; %d; %s # p=%.3f, r=%.3f, %d+%d, %s",
        firstToken, secondToken, factor, spaces, precision, recall, allTokenSentences.size(), allHomophoneSentences.size(), date);
      results.put(factor, new RuleEvalResult(summary, precision, recall));
      if (verbose) {
        System.out.println();
        System.out.printf(ENGLISH, "Factor: %d - %d false positives, %d false negatives, %d true positives, %d true negatives\n",
          factor, evalValues.falsePositives, evalValues.falseNegatives, evalValues.truePositives, evalValues.trueNegatives);
        //System.out.printf(ENGLISH, "Precision:    %.3f (%d false positives)\n", precision, evalValues.falsePositives);
        //System.out.printf(ENGLISH, "Recall:       %.3f (%d false negatives)\n", recall, evalValues.falseNegatives);
        //double fMeasure = FMeasure.getWeightedFMeasure(precision, recall);
        //System.out.printf(ENGLISH, "F-measure:    %.3f (beta=0.5)\n", fMeasure);
        //System.out.printf(ENGLISH, "Good Matches: %d (true positives)\n", evalValues.truePositives);
        //System.out.printf(ENGLISH, "All matches:  %d\n", evalValues.truePositives + evalValues.falsePositives);
        System.out.printf(summary + "\n");
      }
    }
    return results;
  }

  // TODO deduplicate / delegate
  private List<Map.Entry<Sentence, Map.Entry<Integer, Integer>>> getRelevantSentences(List<String> inputs, String token, int maxSentences) throws IOException {
    List<Map.Entry<Sentence, Map.Entry<Integer, Integer>>> sentences = new ArrayList<>();
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

  private List<Map.Entry<Sentence, Map.Entry<Integer, Integer>>> getSentencesFromSource(List<String> inputs, String token, int maxSentences, SentenceSource sentenceSource) {
    List<Map.Entry<Sentence, Map.Entry<Integer, Integer>>> sentences = new ArrayList<>();
    Pattern pattern = Pattern.compile("(?iu)\\b(" + token.toLowerCase() + ")\\p{Alpha}+\\b|\\b\\p{Alpha}+(" + token.toLowerCase() + ")\\b");
    while (sentenceSource.hasNext()) {
      Sentence sentence = sentenceSource.next();
      Matcher matcher = pattern.matcher(sentence.getText());
      if (matcher.find() && Character.isUpperCase(matcher.group().charAt(0))) {
        Map.Entry<Integer, Integer> range = new AbstractMap.SimpleEntry<>(
          // -1 if group did not match anything -> max gets result from group that matched
          Math.max(matcher.start(1), matcher.start(2)),
          Math.max(matcher.end(1), matcher.end(2)));
        sentences.add(new AbstractMap.SimpleEntry<>(sentence, range));
        //if (sentences.size() % 250 == 0) {
        //  println("Loaded sentence " + sentences.size() + " with '" + token + "' from " + inputs);
        //}
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
    if (args.length < 4 || args.length > 5) {
      System.err.println("Usage: " + ProhibitedCompoundRuleEvaluator.class.getSimpleName()
              + " <tokens> <langCode> <languageModelTopDir> <wikipediaXml|tatoebaFile|plainTextFile|dir>...");
      System.err.println("   <tokens> is confusion set file with token/homophone pairs");
      System.err.println("   <languageModelTopDir> is a directory with sub-directories like 'en' which then again contain '1grams',");
      System.err.println("                      '2grams', and '3grams' sub directories with Lucene indexes");
      System.err.println("                      See https://dev.languagetool.org/finding-errors-using-n-gram-data");
      System.err.println("   <wikipediaXml|tatoebaFile|plainTextFile|dir> either a Wikipedia XML dump, or a Tatoeba file, or");
      System.err.println("                      a plain text file with one sentence per line, or a directory with");
      System.err.println("                      example sentences (where <word>.txt contains only the sentences for <word>).");
      System.err.println("                      You can specify both a Wikipedia file and a Tatoeba file.");
      System.exit(1);
    }
    long startTime = System.currentTimeMillis();
    String confusionSetFile = args[0];
    String langCode = args[1];
    Language lang = Languages.getLanguageForShortCode(langCode);
    ConfusionSetLoader loader = new ConfusionSetLoader(lang);
    Map<String, List<ConfusionPair>> confusionSet = loader.loadConfusionPairs(new FileInputStream(confusionSetFile));
    LanguageModel languageModel = new LuceneLanguageModel(new File(args[2], lang.getShortCode()));
    //LanguageModel languageModel = new BerkeleyRawLanguageModel(new File("/media/Data/berkeleylm/google_books_binaries/ger.blm.gz"));
    //LanguageModel languageModel = new BerkeleyLanguageModel(new File("/media/Data/berkeleylm/google_books_binaries/ger.blm.gz"));
    List<String> inputsFiles = new ArrayList<>();
    inputsFiles.add(args[3]);
    if (args.length >= 5) {
      inputsFiles.add(args[4]);
    }
    ProhibitedCompoundRuleEvaluator generator = new ProhibitedCompoundRuleEvaluator(lang, languageModel);
    for (List<ConfusionPair> entries : confusionSet.values()) {
      for (ConfusionPair pair : entries) {
          ConfusionString[] words  = pair.getTerms().toArray(new ConfusionString[0]);
          if (words.length < 2) {
            throw new RuntimeException("Invalid confusion set entry: " + pair);
          }
          generator.run(inputsFiles, words[0].getString(), words[1].getString(), MAX_SENTENCES, EVAL_FACTORS);
      }
    }
    long endTime = System.currentTimeMillis();
    System.out.println("\nTime: " + (endTime-startTime)+"ms");
  }
}

