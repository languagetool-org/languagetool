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
package org.languagetool.dev.bigdata;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.rules.ConfusionSetLoader;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Automatically run {@link ConfusionRuleEvaluator} on word pairs.
 * @since 3.2
 */
@SuppressWarnings({"resource", "CallToPrintStackTrace"})
class AutomaticConfusionRuleEvaluator {
  
  private static final String LANGUAGE = "en";
  private static final boolean CASE_SENSITIVE = false;
  private static final int MAX_EXAMPLES = 1000;
  private static final List<Long> EVAL_FACTORS = Arrays.asList(10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L);
  private static final float MIN_PRECISION = 0.99f;
  private static final float MIN_RECALL = 0.1f;

  private final IndexSearcher searcher;
  private final Map<String, List<ConfusionSet>> knownSets;
  
  private int ignored = 0;

  AutomaticConfusionRuleEvaluator(File luceneIndexDir) throws IOException {
    DirectoryReader reader = DirectoryReader.open(FSDirectory.open(luceneIndexDir.toPath()));
    searcher = new IndexSearcher(reader);
    InputStream confusionSetStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/en/confusion_sets.txt");
    knownSets = new ConfusionSetLoader().loadConfusionSet(confusionSetStream);
  }

  private void run(List<String> lines, File indexDir) throws IOException {
    Language language = Languages.getLanguageForShortCode(LANGUAGE);
    LanguageModel lm = new LuceneLanguageModel(indexDir);
    ConfusionRuleEvaluator evaluator = new ConfusionRuleEvaluator(language, lm, CASE_SENSITIVE);
    for (String line : lines) {
      if (line.contains("#")) {
        System.out.println("Ignoring: " + line);
        continue;
      }
      String[] parts = line.split(";\\s*");
      if (parts.length != 2) {
        throw new IOException("Expected semicolon-separated input: " + line);
      }
      try {
        int i = 1;
        for (String part : parts) {
          // compare pair-wise - maybe we should compare every item with every other item?
          if (i < parts.length) {
            runOnPair(evaluator, line, removeComment(part), removeComment(parts[i]));
          }
          i++;
        }
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }
    System.out.println("Done. Ignored items because they are already known: " + ignored);
  }

  private String removeComment(String str) {
    return str.replaceFirst("\\|.*", "");
  }

  private void runOnPair(ConfusionRuleEvaluator evaluator, String line, String part1, String part2) throws IOException {
    for (Map.Entry<String, List<ConfusionSet>> entry : knownSets.entrySet()) {
      if (entry.getKey().equals(part1)) {
        List<ConfusionSet> confusionSet = entry.getValue();
        for (ConfusionSet set : confusionSet) {
          Set<String> stringSet = set.getSet().stream().map(l -> l.getString()).collect(Collectors.toSet());
          if (stringSet.containsAll(Arrays.asList(part1, part2))) {
            System.out.println("Ignoring: " + part1 + "/" + part2 + ", in active confusion sets already");
            ignored++;
            return;
          }
        }
      }
    }
    System.out.println("Working on: " + line);
    File sentencesFile = writeExampleSentencesToTempFile(new String[]{part1, part2});
    List<String> input = Arrays.asList(sentencesFile.getAbsolutePath());
    Map<Long, ConfusionRuleEvaluator.EvalResult> results = evaluator.run(input, part1, part2, MAX_EXAMPLES, EVAL_FACTORS);
    Map<Long, ConfusionRuleEvaluator.EvalResult> bestResults = findBestFactor(results);
    if (bestResults.size() > 0) {
      for (Map.Entry<Long, ConfusionRuleEvaluator.EvalResult> entry : bestResults.entrySet()) {
        System.out.println("=> " + entry.getValue().getSummary());
      }
    } else {
      System.out.println("No good result found for " + part1 + "/" + part2);
    }
  }

  private Map<Long, ConfusionRuleEvaluator.EvalResult> findBestFactor(Map<Long, ConfusionRuleEvaluator.EvalResult> results) {
    Map<Long, ConfusionRuleEvaluator.EvalResult> filteredResults = new HashMap<>();
    for (Map.Entry<Long, ConfusionRuleEvaluator.EvalResult> entry : results.entrySet()) {
      ConfusionRuleEvaluator.EvalResult result = entry.getValue();
      boolean candidate = result.getPrecision() >= MIN_PRECISION && result.getRecall() >= MIN_RECALL;
      if (candidate) {
        filteredResults.put(entry.getKey(), entry.getValue());
      }
    }
    return filteredResults;
  }

  private File writeExampleSentencesToTempFile(String[] words) throws IOException {
    File tempFile = new File("/tmp/example-sentences.txt");
    try (FileWriter fw = new FileWriter(tempFile)) {
      for (String word : words) {
        findExampleSentences(word, fw);
      }
      System.out.println("Example sentences written to " + tempFile);
    }
    return tempFile;
  }

  private void findExampleSentences(String word, FileWriter fw) throws IOException {
    Term term = new Term(TextIndexCreator.FIELD, CASE_SENSITIVE ? word.toLowerCase() : word);
    TopDocs topDocs = searcher.search(new TermQuery(term), CASE_SENSITIVE ? Integer.MAX_VALUE : MAX_EXAMPLES);
    int count = 0;
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      String sentence = searcher.doc(scoreDoc.doc).get(TextIndexCreator.FIELD);
      if (CASE_SENSITIVE) {
        if (sentence.contains(word)) {
          fw.write(sentence + "\n");
          count++;
        }
      } else {
        fw.write(sentence + "\n");
        count++;
      }
      if (count > MAX_EXAMPLES) {
        break;
      }
    }
    System.out.println("Found " + count + " examples for " + word);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("Usage: " + AutomaticConfusionRuleEvaluator.class.getSimpleName() + " <confusionPairCandidates> <exampleSentenceIndexDir> <ngramDir>");
      System.out.println("   <confusionPairCandidates> is a semicolon-separated list of words (one pair per line)");
      System.out.println("   <exampleSentenceIndexDir> is a Lucene index created by TextIndexCreator");
      System.exit(1);
    }
    List<String> lines = IOUtils.readLines(new FileInputStream(args[0]), "utf-8");
    AutomaticConfusionRuleEvaluator eval = new AutomaticConfusionRuleEvaluator(new File(args[1]));
    eval.run(lines, new File(args[2]));
  }

}
