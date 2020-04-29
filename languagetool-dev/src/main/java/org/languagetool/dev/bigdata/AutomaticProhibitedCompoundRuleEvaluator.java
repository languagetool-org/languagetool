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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.ConfusionPair;
import org.languagetool.rules.ConfusionSetLoader;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Automatically run {@link ConfusionRuleEvaluator} on word pairs.
 * @since 3.2
 */
@SuppressWarnings({"resource", "CallToPrintStackTrace"})
class AutomaticProhibitedCompoundRuleEvaluator {

  private static final String LANGUAGE = "de";
  private static final int MAX_EXAMPLES = 1000;
  private static final int MIN_EXAMPLES = 50;
  private static final List<Long> EVAL_FACTORS = Arrays.asList(10L);//, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L);
  private static final float MIN_PRECISION = 0.95f;
  private static final float MIN_RECALL = 0.1f;
  private static final String LUCENE_CONTENT_FIELD = "fieldLowercase";

  private final IndexSearcher searcher;
  private final Map<String, List<ConfusionPair>> knownSets;
  private final Set<String> finishedPairs = new HashSet<>();

  private int ignored = 0;

  AutomaticProhibitedCompoundRuleEvaluator(File luceneIndexDir) throws IOException {
    DirectoryReader reader = DirectoryReader.open(FSDirectory.open(luceneIndexDir.toPath()));
    searcher = new IndexSearcher(reader);
    InputStream confusionSetStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/" + LANGUAGE + "/confusion_sets.txt");
    knownSets = new ConfusionSetLoader().loadConfusionPairs(confusionSetStream);
  }

  private void run(List<String> lines, File indexDir) throws IOException {
    Language language = Languages.getLanguageForShortCode(LANGUAGE);
    LanguageModel lm = new LuceneLanguageModel(indexDir);
    ProhibitedCompoundRuleEvaluator evaluator = new ProhibitedCompoundRuleEvaluator(language, lm);
    int lineCount = 0;
    for (String line : lines) {
      lineCount++;
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
            runOnPair(evaluator, line, lineCount, lines.size(), removeComment(part), removeComment(parts[i]));
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
    return str.replaceFirst("\\|.*", "").trim();
  }

  private void runOnPair(ProhibitedCompoundRuleEvaluator evaluator, String line, int lineCount, int totalLines, String part1, String part2) throws IOException {
    if (finishedPairs.contains(part1 + "/" + part2) || finishedPairs.contains(part2 + "/" + part1)) {
      System.out.println("Ignoring: " + part1 + "/" + part2 + ", finished before");
      return;
    }
    for (Map.Entry<String, List<ConfusionPair>> entry : knownSets.entrySet()) {
      if (entry.getKey().equals(part1)) {
        List<ConfusionPair> confusionPair = entry.getValue();
        for (ConfusionPair pair : confusionPair) {
          Set<String> stringSet = pair.getTerms().stream().map(l -> l.getString()).collect(Collectors.toSet());
          if (stringSet.containsAll(Arrays.asList(part1, part2))) {
            System.out.println("Ignoring: " + part1 + "/" + part2 + ", in active confusion sets already");
            ignored++;
            return;
          }
        }
      }
    }
    System.out.println("Working on: " + line + " (" + lineCount + " of " + totalLines + ")");
    try {
      File sentencesFile = writeExampleSentencesToTempFile(new String[]{part1, part2});
      List<String> input = Arrays.asList(sentencesFile.getAbsolutePath());
      Map<Long, RuleEvalResult> results = evaluator.run(input, part1, part2, MAX_EXAMPLES, EVAL_FACTORS);
      Map<Long, RuleEvalResult> bestResults = findBestFactor(results);
      if (bestResults.size() > 0) {
        for (Map.Entry<Long, RuleEvalResult> entry : bestResults.entrySet()) {
          System.out.println("=> " + entry.getValue().getSummary());
        }
      } else {
        System.out.println("No good result found for " + part1 + "/" + part2);
      }
      finishedPairs.add(part1 + "/" + part2);
    } catch (TooFewExamples e) {
      System.out.println("Skipping " + part1 + "/" + part2 + ", too few examples: " + e.getMessage());
    }
  }

  private Map<Long, RuleEvalResult> findBestFactor(Map<Long, RuleEvalResult> results) {
    Map<Long, RuleEvalResult> filteredResults = new HashMap<>();
    for (Map.Entry<Long, RuleEvalResult> entry : results.entrySet()) {
      RuleEvalResult result = entry.getValue();
      boolean candidate = result.getPrecision() >= MIN_PRECISION && result.getRecall() >= MIN_RECALL;
      if (candidate) {
        filteredResults.put(entry.getKey(), entry.getValue());
      }
    }
    return filteredResults;
  }

  private File writeExampleSentencesToTempFile(String[] words) throws IOException {
    File tempFile = new File("/tmp/example-sentences.txt");
    int count = 0;
    try (FileWriter fw = new FileWriter(tempFile)) {
      for (String word : words) {
        int tmpCount = findExampleSentences(word, fw);
        if (tmpCount <= MIN_EXAMPLES) {
          throw new TooFewExamples(word, tmpCount);
        }
        count += tmpCount;
      }
      System.out.println(count + " example sentences written to " + tempFile);
    }
    return tempFile;
  }

  private int findExampleSentences(String word, FileWriter fw) throws IOException {
    Term term = new Term(LUCENE_CONTENT_FIELD, ".+" + word + "|" + StringUtils.capitalize(word) + ".+");
    long t1 = System.currentTimeMillis();
    TopDocs topDocs = searcher.search(new RegexpQuery(term), MAX_EXAMPLES);
    long t2 = System.currentTimeMillis();
    int count = 0;
    Set<String> foundSentences = new HashSet<>();
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      String sentence = searcher.doc(scoreDoc.doc).get(LUCENE_CONTENT_FIELD);
      if (!foundSentences.contains(sentence)) {
        fw.write(sentence + "\n");
        foundSentences.add(sentence);
        count++;
      }

      if (count > MAX_EXAMPLES) {
        break;
      }
    }
    long t3 = System.currentTimeMillis();
    long searchTime = t2 - t1;
    long iterateTime = t3 - t2;
    System.out.println("Found " + count + " examples for " + word + " (" + searchTime + "ms, " + iterateTime + "ms)");
    return count;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("Usage: " + AutomaticProhibitedCompoundRuleEvaluator.class.getSimpleName() + " <confusionPairCandidates> <exampleSentenceIndexDir> <ngramDir>");
      System.out.println("   <confusionPairCandidates> is a semicolon-separated list of words (one pair per line)");
      System.out.println("   <exampleSentenceIndexDir> is a Lucene index created by TextIndexCreator");
      System.exit(1);
    }
    List<String> lines = IOUtils.readLines(new FileInputStream(args[0]), "utf-8");
    AutomaticProhibitedCompoundRuleEvaluator eval = new AutomaticProhibitedCompoundRuleEvaluator(new File(args[1]));
    eval.run(lines, new File(args[2]));
  }

  class TooFewExamples extends RuntimeException {
    private String word;
    private int exampleCount;
    TooFewExamples(String word, int exampleCount) {
      this.word = word;
      this.exampleCount = exampleCount;
    }
    @Override
    public String getMessage() {
      return exampleCount + " matches for " + word;
    }
  }
}
