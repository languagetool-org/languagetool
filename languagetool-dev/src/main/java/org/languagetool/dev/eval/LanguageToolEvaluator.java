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

import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.EnglishConfusionProbabilityRule;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Wrapper around LanguageTool for easier use from the evaluation scripts.
 * @since 2.7
 */
class LanguageToolEvaluator implements Evaluator {

  private final JLanguageTool lt;
  private final LanguageModel languageModel;

  LanguageToolEvaluator(File indexTopDir) throws IOException {
    lt = new JLanguageTool(new BritishEnglish());
    disableRules();
    if (indexTopDir != null) {
      if (indexTopDir.isDirectory()) {
        languageModel = new LuceneLanguageModel(indexTopDir);
        System.out.println("Using Lucene language model from " + languageModel);
        EnglishConfusionProbabilityRule probabilityRule =
                new EnglishConfusionProbabilityRule(JLanguageTool.getMessageBundle(), languageModel, new English());
        //new EnglishConfusionProbabilityRule(JLanguageTool.getMessageBundle(), languageModel, new File("/tmp/languagetool_network.net"));
        lt.addRule(probabilityRule);
      } else {
        throw new RuntimeException("Does not exist or not a directory: " + indexTopDir);
      }
    } else {
      languageModel = null;
    }
  }
  
  @Override
  public void close() {
    if (languageModel != null) {
      languageModel.close();
    }
  }

  private void disableRules() {
    // The Pedler corpus has some real errors that have no error markup, so we disable
    // some rules that typically match those:
    lt.disableRule("COMMA_PARENTHESIS_WHITESPACE");
    lt.disableRule("SENT_START_CONJUNCTIVE_LINKING_ADVERB_COMMA");
    lt.disableRule("EN_QUOTES");
    lt.disableRule("I_LOWERCASE");
    //langTool.disableRule("MORFOLOGIK_RULE_EN_GB");  // disabling spell rule improves precision 0.77 -> 0.88 (as of 2014-07-18)
    // turn off style rules:
    lt.disableRule("LITTLE_BIT");
    lt.disableRule("ALL_OF_THE");
    lt.disableRule("SOME_OF_THE");
    // British English vs. American English - not clear whether the corpus contains only BE:
    lt.disableRule("EN_GB_SIMPLE_REPLACE");
    lt.disableRule("APARTMENT-FLAT");
  }

  @Override
  public List<RuleMatch> check(AnnotatedText annotatedText) throws IOException {
    return lt.check(annotatedText);
  }
}
