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

import org.apache.commons.lang3.StringUtils;
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
import org.languagetool.rules.*;
import org.languagetool.rules.ngrams.ConfusionProbabilityRule;
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
import static java.util.stream.Collectors.toList;

/**
 * Loads sentences with a homophone (e.g. there/their) from Wikipedia or confusion set files
 * and evaluates EnglishConfusionProbabilityRule with them.
 * 
 * @see AutomaticConfusionRuleEvaluator
 * @since 3.0
 * @author Daniel Naber 
 */
class ConfusionRuleEvaluator {

  private static final boolean CASE_SENSITIVE = false;
  private static final List<Long> EVAL_FACTORS = Arrays.asList(10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L);
  private static final int MAX_SENTENCES = 1000;

  private final Language language;
  private final boolean caseSensitive;
  private final boolean bothDirections;  // replace A by B and vice versa?
  private final ConfusionProbabilityRule rule;
  private final Map<Long, RuleEvalValues> evalValues = new HashMap<>();

  private boolean verbose = true;

  ConfusionRuleEvaluator(Language language, LanguageModel languageModel, boolean caseSensitive, boolean bothDirections) {
    this.language = language;
    this.caseSensitive = caseSensitive;
    this.bothDirections = bothDirections;
    try {
      List<Rule> rules = language.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), languageModel);
      if (rules == null) {
        throw new RuntimeException("Language " + language + " doesn't seem to support a language model");
      }
      ConfusionProbabilityRule foundRule = null;
      for (Rule rule : rules) {
        if (rule.getId().equals(ConfusionProbabilityRule.RULE_ID)) {
          foundRule = (ConfusionProbabilityRule)rule;
          break;
        }
      }
      if (foundRule == null) {
        throw new RuntimeException("Language " + language + " has no language model rule with id " + ConfusionProbabilityRule.RULE_ID);
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
    List<Sentence> allTokenSentences = getRelevantSentences(inputsOrDir, token, maxSentences);
    // Load the sentences with a homophone and later replace it so we get error sentences:
    List<Sentence> allHomophoneSentences = getRelevantSentences(inputsOrDir, homophoneToken, maxSentences);
    //if (allTokenSentences.size() < 20 || allHomophoneSentences.size() < 20) {
    //  System.out.println("Skipping " + token + " / " + homophoneToken);
    //  return null;
    //}
    evaluate(allTokenSentences, true, token, homophoneToken, evalFactors);
    if (bothDirections) {
      evaluate(allTokenSentences, false, homophoneToken, token, evalFactors);
    }
    evaluate(allHomophoneSentences, false, token, homophoneToken, evalFactors);
    if (bothDirections) {
      evaluate(allHomophoneSentences, true, homophoneToken, token, evalFactors);
    }
    return printEvalResult(allTokenSentences, allHomophoneSentences, inputsOrDir, token, homophoneToken);
  }

  @SuppressWarnings("ConstantConditions")
  private void evaluate(List<Sentence> sentences, boolean isCorrect, String token, String homophoneToken, List<Long> evalFactors) throws IOException {
    println("======================");
    printf("Starting evaluation on " + sentences.size() + " " + (isCorrect ? "correct" : "incorrect") + " sentences with %s/%s:\n", token, homophoneToken);
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
      for (Long factor : evalFactors) {
        rule.setConfusionPair(new ConfusionPair(token, homophoneToken, factor, bothDirections));
        RuleMatch[] matches = rule.match(analyzedSentence);
        boolean consideredCorrect = matches.length == 0;
        String displayStr = plainText.replaceFirst("(?i)\\b" + textToken + "\\b", "**" + replacement + "**");
        //System.out.println("consideredCorrect=" + consideredCorrect + ", correct=" + isCorrect + ": " + replacedTokenSentence);
        if (consideredCorrect && isCorrect) {
          evalValues.get(factor).trueNegatives++;
        } else if (!consideredCorrect && isCorrect) {
          evalValues.get(factor).falsePositives++;
          println("false positive with factor " + factor + ": " + displayStr);
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

  private Map<Long, RuleEvalResult> printEvalResult(List<Sentence> allTokenSentences, List<Sentence> allHomophoneSentences, List<String> inputsOrDir,
                                                    String token, String homophoneToken) {
    Map<Long, RuleEvalResult> results = new LinkedHashMap<>();
    int sentences = allTokenSentences.size() + allHomophoneSentences.size();
    System.out.println("\nEvaluation results for " + token + "/" + homophoneToken
            + " with " + sentences + " sentences as of " + new Date() + ":");
    System.out.printf(ENGLISH, "Inputs:       %s\n", inputsOrDir);
    System.out.printf(ENGLISH, "Case sensit.: %s\n", caseSensitive);
    List<Long> factors = evalValues.keySet().stream().sorted().collect(toList());
    for (Long factor : factors) {
      RuleEvalValues evalValues = this.evalValues.get(factor);
      float precision = (float)evalValues.truePositives / (evalValues.truePositives + evalValues.falsePositives);
      float recall = (float) evalValues.truePositives / (evalValues.truePositives + evalValues.falseNegatives);
      String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
      String spaces = StringUtils.repeat(" ", 82-Long.toString(factor).length());
      String word1 = token;
      String word2 = homophoneToken;
      String delimiter = " -> ";
      if (bothDirections) {
        delimiter = "; ";
        if (word1.compareTo(word2) > 0) {
          String temp = word1;
          word1 = word2;
          word2 = temp;
        }
      }
      float fMeasureBeta = 0.5f;
      String summary = String.format(ENGLISH, "%s%s%s; %d; %s # p=%.3f, r=%.3f, f%.1f=%.3f, %d+%d, %dgrams, %s",
              word1, delimiter, word2, factor, spaces, precision, recall, fMeasureBeta, FMeasure.getFMeasure(precision, recall, fMeasureBeta),
              allTokenSentences.size(), allHomophoneSentences.size(), rule.getNGrams(), date);
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
    Pattern pattern = Pattern.compile(".*\\b" + (caseSensitive ? token : token.toLowerCase()) + "\\b.*");
    while (sentenceSource.hasNext()) {
      Sentence sentence = sentenceSource.next();
      String sentenceText = caseSensitive ? sentence.getText() : sentence.getText().toLowerCase();
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
    if (args.length < 5 || args.length > 6) {
      System.err.println("Usage: " + ConfusionRuleEvaluator.class.getSimpleName()
              + " <token> <homophoneToken> <langCode> <languageModelTopDir> <wikipediaXml|tatoebaFile|plainTextFile|dir>...");
      System.err.println("   <languageModelTopDir> is a directory with sub-directories like 'en' which then again contain '1grams',");
      System.err.println("                      '2grams', and '3grams' sub directories with Lucene indexes");
      System.err.println("                      See http://wiki.languagetool.org/finding-errors-using-n-gram-data");
      System.err.println("   <wikipediaXml|tatoebaFile|plainTextFile|dir> either a Wikipedia XML dump, or a Tatoeba file, or");
      System.err.println("                      a plain text file with one sentence per line, or a directory with");
      System.err.println("                      example sentences (where <word>.txt contains only the sentences for <word>).");
      System.err.println("                      You can specify both a Wikipedia file and a Tatoeba file.");
      System.exit(1);
    }
    long startTime = System.currentTimeMillis();
    String token = args[0];
    String homophoneToken = args[1];
    String langCode = args[2];
    Language lang;
    if ("en".equals(langCode)) {
      lang = new EnglishLight();
    } else {
      lang = Languages.getLanguageForShortCode(langCode);
    }
    LanguageModel languageModel = new LuceneLanguageModel(new File(args[3], lang.getShortCode()));
    //LanguageModel languageModel = new BerkeleyRawLanguageModel(new File("/media/Data/berkeleylm/google_books_binaries/ger.blm.gz"));
    //LanguageModel languageModel = new BerkeleyLanguageModel(new File("/media/Data/berkeleylm/google_books_binaries/ger.blm.gz"));
    List<String> inputsFiles = new ArrayList<>();
    inputsFiles.add(args[4]);
    if (args.length >= 6) {
      inputsFiles.add(args[5]);
    }
    boolean bothDirections = true;
    System.out.println("NOTE: assuming pair works in both directions (A -> B and B -> A)");
    ConfusionRuleEvaluator generator = new ConfusionRuleEvaluator(lang, languageModel, CASE_SENSITIVE, bothDirections);
    generator.run(inputsFiles, token, homophoneToken, MAX_SENTENCES, EVAL_FACTORS);
    long endTime = System.currentTimeMillis();
    System.out.println("\nTime: " + (endTime-startTime)+"ms");
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
