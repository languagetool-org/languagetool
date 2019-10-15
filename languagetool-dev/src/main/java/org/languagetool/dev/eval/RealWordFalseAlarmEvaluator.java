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

import org.apache.commons.io.IOUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.en.EnglishConfusionProbabilityRule;
import org.languagetool.rules.ngrams.ConfusionProbabilityRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Runs LanguageTool's confusion rule on Wikipedia-extracted sentences that we assume to be correct.
 * @since 2.7
 */
class RealWordFalseAlarmEvaluator {

  private static final boolean EVAL_MODE = true;  // set to false to get data for homophones-info.txt
  private static final int MAX_SENTENCES = 1000;
  private static final int MAX_ERROR_DISPLAY = 50;
  // the minimum number of sentences in homophones-info.txt, items with less sentences will be ignored (eval mode only):
  private static final int MIN_SENTENCES = 0;
  // maximum error rate of a homophone in homophones-info.txt, items with a larger error rate will be ignored (eval mode only):
  private static final float MAX_ERROR_RATE = 10;
  
  private final JLanguageTool langTool;
  private final ConfusionProbabilityRule confusionRule;
  private final Map<String,List<ConfusionPair>> confusionPairs;
  private final LanguageModel languageModel;
  
  private int globalSentenceCount;
  private int globalRuleMatches;

  RealWordFalseAlarmEvaluator(File languageModelIndexDir) throws IOException {
    try (InputStream inputStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/en/confusion_sets.txt")) {
      ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader();
      confusionPairs = confusionSetLoader.loadConfusionPairs(inputStream);
    }
    langTool = new JLanguageTool(new BritishEnglish());
    List<Rule> rules = langTool.getAllActiveRules();
    for (Rule rule : rules) {
      langTool.disableRule(rule.getId());
    }
    languageModel = new LuceneLanguageModel(languageModelIndexDir);
    confusionRule = new EnglishConfusionProbabilityRule(JLanguageTool.getMessageBundle(), languageModel, new English());
    langTool.addRule(confusionRule);
  }
  
  void close() {
    if (languageModel != null) {
      languageModel.close();
    }
  }

  void run(File dir) throws IOException {
    if (EVAL_MODE) {
      System.out.println("Running in eval mode, no 'DATA' lines will be printed, only a subset of the homophones will be used.");
    } else {
      System.out.println("grep for '^DATA;' to get results in CVS format:");
      System.out.println("DATA;word;sentence_count;errors_found;errors_percent");
    }
    File[] files = dir.listFiles();
    //noinspection ConstantConditions
    int fileCount = 1;
    for (File file : files) {
      if (!file.getName().endsWith(".txt")) {
        System.out.println("Ignoring " + file + ", does not match *.txt");
        continue;
      }
      try (FileInputStream fis = new FileInputStream(file)) {
        System.out.println("===== Working on " + file.getName() + " (" + fileCount + "/" + files.length + ") =====");
        checkLines(IOUtils.readLines(fis), file.getName().replace(".txt", ""));
        fileCount++;
      }
    }
    System.out.println("==============================");
    System.out.println(globalSentenceCount + " sentences checked");
    System.out.println(globalRuleMatches + " errors found");
    float percentage = (float)globalRuleMatches/(float)globalSentenceCount*100;
    System.out.printf("%.2f%% of sentences have a match\n", percentage);
  }

  private void checkLines(List<String> lines, String name) throws IOException {
    List<ConfusionPair> subConfusionPair = confusionPairs.get(name);
    if (subConfusionPair == null) {
      System.out.println("Skipping '" + name + "', homophone not loaded");
      return;
    }
    if (subConfusionPair.size() > 1) {
      System.err.println("WARN: will only use first confusion set of " + subConfusionPair.size() + ": " + subConfusionPair.get(0));
    }
    confusionRule.setConfusionPair(subConfusionPair.get(0));
    int sentenceCount = 0;
    int ruleMatches = 0;
    for (String line : lines) {
      List<RuleMatch> matches = langTool.check(line);
      sentenceCount++;
      globalSentenceCount++;
      if (matches.size() > 0) {
        Set<String> suggestions = new HashSet<>();
        for (RuleMatch match : matches) {
          //System.out.println("    " + match + ": " + match.getSuggestedReplacements());
          suggestions.addAll(match.getSuggestedReplacements());
          ruleMatches++;
          globalRuleMatches++;
        }
        if (ruleMatches <= MAX_ERROR_DISPLAY) {
          System.out.println("[" + name + "] " + line + " => " + suggestions);
        }
      }
      if (sentenceCount > MAX_SENTENCES) {
        System.out.println("Max sentences (" + MAX_SENTENCES + ") reached, stopping");
        break;
      }
    }
    System.out.println(sentenceCount + " sentences checked");
    System.out.println(ruleMatches + " errors found");
    float percentage = (float)ruleMatches/(float)sentenceCount*100;
    System.out.printf("%.2f%% of sentences have a match\n", percentage);
    if (!EVAL_MODE) {
      System.out.printf(Locale.ENGLISH, "DATA;%s;%d;%d;%.2f\n\n", name, sentenceCount, ruleMatches, percentage);
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + RealWordFalseAlarmEvaluator.class.getSimpleName() + " <languageModel> <sentenceDirectory>");
      System.out.println("   <languageModel> is a Lucene index with ngram frequency information");
      System.out.println("   <sentenceDirectory> is a directory with filenames like 'xx.txt' where 'xx' is the homophone");
      System.exit(1);
    }
    RealWordFalseAlarmEvaluator evaluator = new RealWordFalseAlarmEvaluator(new File(args[0]));
    File dir = new File(args[1]);
    if (!dir.isDirectory()) {
      throw new RuntimeException("Not a directory: " + dir);
    }
    evaluator.run(dir);
    evaluator.close();
  }

}
