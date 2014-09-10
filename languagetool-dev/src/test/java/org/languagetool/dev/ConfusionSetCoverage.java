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
package org.languagetool.dev;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.rules.ConfusionProbabilityRule;
import org.languagetool.rules.ConfusionSetLoader;
import org.languagetool.tools.StringTools;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Measure how many homophones there are per sentence on average. Useful so we can estimate
 * the number of ngram lookups needed by the confusion rule.
 * @since 2.7
 */
class ConfusionSetCoverage {

  private int sentences = 0;
  private int homophones = 0;
  private int lookupsNeeded = 0;

  private void run(String filename) throws IOException {
    Map<String, ConfusionProbabilityRule.ConfusionSet> confusionSet = getConfusionSet();
    try (FileReader reader = new FileReader(filename)) {
      String text = StringTools.readerToString(reader);
      JLanguageTool languageTool = new JLanguageTool(new English());
      List<AnalyzedSentence> analyzedSentences = languageTool.analyzeText(text);
      for (AnalyzedSentence sentence : analyzedSentences) {
        runOnSentence(sentence, confusionSet);
      }
    }
    System.out.println("Homophones set: " + confusionSet.size() + " items");
    System.out.println("Sentences: " + sentences);
    System.out.println("Homophones: " + homophones + " = " + ((float)homophones/sentences) + " per sentence");
    System.out.println("Lookups: " + lookupsNeeded + " = " + ((float)lookupsNeeded/sentences) + " per sentence");
    System.out.println(" (Lookups is the number of lookups needed to see which word in the homophones set " +
            "is more common. Actually even more ngram lookups will be needed, depending on what ngrams we have.)");
  }

  private Map<String, ConfusionProbabilityRule.ConfusionSet> getConfusionSet() throws IOException {
    ConfusionSetLoader loader = new ConfusionSetLoader();
    InputStream homophoneStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/en/homophones.txt");
    return loader.loadConfusionSet(homophoneStream);
  }

  private void runOnSentence(AnalyzedSentence sentence, Map<String, ConfusionProbabilityRule.ConfusionSet> confusionSet) {
    sentences++;
    for (AnalyzedTokenReadings token : sentence.getTokensWithoutWhitespace()) {
      String tokenStr = token.getToken();
      if (confusionSet.containsKey(tokenStr)) {
        homophones++;
        lookupsNeeded += confusionSet.get(tokenStr).getSet().size();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + ConfusionSetCoverage.class.getSimpleName() + " <textfile>");
      System.exit(1);
    }
    ConfusionSetCoverage coverage = new ConfusionSetCoverage();
    coverage.run(args[0]);
  }

}
