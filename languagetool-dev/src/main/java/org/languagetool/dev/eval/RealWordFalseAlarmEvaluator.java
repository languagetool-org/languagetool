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
import org.languagetool.rules.ConfusionProbabilityRule;
import org.languagetool.rules.ConfusionSetLoader;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.EnglishConfusionProbabilityRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Runs LanguageTool's confusion rule on Wikipedia-extracted sentences that we assume to be correct.
 * @since 2.6
 */
class RealWordFalseAlarmEvaluator {

  private static final int MAX_SENTENCES = 1000;
  
  private final JLanguageTool langTool;
  private final ConfusionProbabilityRule confusionRule;
  private final Map<String,ConfusionProbabilityRule.ConfusionSet> confusionSet;
  
  private int globalSentenceCount;
  private int globalRuleMatches;

  RealWordFalseAlarmEvaluator(File languageModel) throws IOException {
    ConfusionSetLoader confusionSetLoader =  new ConfusionSetLoader();
    InputStream inputStream = JLanguageTool.getDataBroker().getFromRulesDirAsStream("homophonedb.txt");
    confusionSet = confusionSetLoader.loadConfusionSet(inputStream);
    langTool = new JLanguageTool(new BritishEnglish());
    langTool.activateDefaultPatternRules();
    List<Rule> rules = langTool.getAllActiveRules();
    for (Rule rule : rules) {
      langTool.disableRule(rule.getId());
    }
    confusionRule = new EnglishConfusionProbabilityRule(JLanguageTool.getMessageBundle(), languageModel);
    langTool.addRule(confusionRule);
  }

  void run(File dir) throws IOException {
    File[] files = dir.listFiles();
    //noinspection ConstantConditions
    for (File file : files) {
      if (!file.getName().endsWith(".txt")) {
        System.out.println("Ignoring " + file + ", does not match *.txt");
        continue;
      }
      try (FileInputStream fis = new FileInputStream(file)) {
        System.out.println("===== Working on " + file.getName() + " =====");
        checkLines(IOUtils.readLines(fis), file.getName().replace(".txt", ""));
      }
    }
    System.out.println(globalSentenceCount + " sentences checked");
    System.out.println(globalRuleMatches + " errors found");
  }

  private void checkLines(List<String> lines, String name) throws IOException {
    confusionRule.setConfusionSet(confusionSet.get(name));
    int sentenceCount = 0;
    int ruleMatches = 0;
    for (String line : lines) {
      List<RuleMatch> matches = langTool.check(line);
      sentenceCount++;
      globalSentenceCount++;
      if (matches.size() > 0) {
        System.out.println("[" + name + "] " + line + " => " + matches.size());
        for (RuleMatch match : matches) {
          System.out.println("    " + match + ": " + match.getSuggestedReplacements());
          ruleMatches++;
          globalRuleMatches++;
        }
      }
      if (sentenceCount > MAX_SENTENCES) {
        System.out.println("Max sentences (" + MAX_SENTENCES + ") reached, stopping");
        break;
      }
    }
    System.out.println(sentenceCount + " sentences checked");
    System.out.println(ruleMatches + " errors found");
    float percentage = ((float)ruleMatches/(float)sentenceCount*100);
    System.out.printf("%.2f%% of sentences have a match\n\n", percentage);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + RealWordFalseAlarmEvaluator.class.getSimpleName() + " <languageModel> <sentenceDirectory>");
      System.out.println("   <languageModel> is a morfologik file with ngram frequency information");
      System.exit(1);
    }
    RealWordFalseAlarmEvaluator evaluator = new RealWordFalseAlarmEvaluator(new File(args[0]));
    evaluator.run(new File(args[1]));
  }

}
