/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.dev.errorcorpus.ErrorCorpus;
import org.languagetool.dev.errorcorpus.SimpleCorpus;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.en.EnglishNgramProbabilityRule;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Evaluates the ngram rule with a simple corpus, see {@link SimpleCorpus}.
 * @since 3.2
 */
class SimpleCorpusEvaluator extends RealWordCorpusEvaluator {

  SimpleCorpusEvaluator(File indexTopDir) throws IOException {
    super(indexTopDir);
  }

  @NotNull
  @Override
  protected Evaluator getEvaluator(File indexTopDir) throws IOException {
    return new NgramLanguageToolEvaluator(indexTopDir);
  }

  @NotNull
  @Override
  protected ErrorCorpus getCorpus(File dir) throws IOException {
    return new SimpleCorpus(dir);
  }
  
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + SimpleCorpusEvaluator.class.getSimpleName() + " <corpusFile> <languageModelDir>");
      System.out.println("   [languageModel] is a Lucene index directory with ngram frequency information");
      System.exit(1);
    }
    File inputFile = new File(args[0]);
    File languageModelTopDir = new File(args[1]);
    System.out.println("Running with language model from " + languageModelTopDir);
    SimpleCorpusEvaluator evaluator = new SimpleCorpusEvaluator(languageModelTopDir);
    evaluator.run(inputFile);
    evaluator.close();
  }

  static class NgramLanguageToolEvaluator implements Evaluator {

    private final JLanguageTool langTool;
    private final LanguageModel languageModel;

    NgramLanguageToolEvaluator(File indexTopDir) throws IOException {
      langTool = new JLanguageTool(new English());
      disableAllRules();
      languageModel = new LuceneLanguageModel(indexTopDir);
      System.out.println("Using Lucene language model from " + languageModel);
      EnglishNgramProbabilityRule probabilityRule =
              new EnglishNgramProbabilityRule(JLanguageTool.getMessageBundle(), languageModel, new English());
      langTool.addRule(probabilityRule);
    }

    @Override
    public void close() {
      if (languageModel != null) {
        languageModel.close();
      }
    }

    private void disableAllRules() {
      for (Rule rule : langTool.getAllActiveRules()) {
        langTool.disableRule(rule.getId());
      }
    }

    @Override
    public List<RuleMatch> check(AnnotatedText annotatedText) throws IOException {
      return langTool.check(annotatedText);
    }
  }

}
