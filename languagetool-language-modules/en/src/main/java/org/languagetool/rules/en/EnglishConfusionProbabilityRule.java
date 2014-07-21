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
package org.languagetool.rules.en;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.ConfusionProbabilityRule;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class EnglishConfusionProbabilityRule extends ConfusionProbabilityRule {

  public EnglishConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    super(messages, languageModel);
  }

  @Override
  public String getDescription() {
    return "Statistically detect wrong use of words that are easily confused";
  }
  
  /**
   * For internal testing only.
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + EnglishConfusionProbabilityRule.class.getSimpleName() + " <sentence>");
      System.exit(1);
    }
    LanguageModel languageModel = new LuceneLanguageModel(new File("/data/google-2gram-index/"));
    EnglishConfusionProbabilityRule rule = new EnglishConfusionProbabilityRule(JLanguageTool.getMessageBundle(), languageModel);
    JLanguageTool languageTool = new JLanguageTool(new English());
    AnalyzedSentence sentence = languageTool.getAnalyzedSentence(args[0]);
    System.out.println("Input: " + args[0]);
    rule.match(sentence);  // we only want to see the debugging output
  }

}
