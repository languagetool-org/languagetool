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

package org.languagetool.rules.spelling.suggestions;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SuggestedReplacement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * needs to run with classpath of languagetool-standalone (needs access to language modules)
 * configure via JSON file, format specified in SuggestionChangesTestCenfig.java
 * specify path via JVM parameter -Dconfig=... (system property)
 * used to create training data (features) for suggestion ranking models or test how code changes affect LT's performance
 *
 * Prints results on interrupt, or after finishing.
 */
public class SuggestionChangesTest {

  static class SuggestionTestData {
    private final String language;
    private final String sentence;
    private final String covered;
    private final String replacement;
    private final String correction;
    private final SuggestionChangesDataset dataset;

    public SuggestionTestData(String language, String sentence, String covered, String replacement, String correction, SuggestionChangesDataset dataset) {
      this.language = language;
      this.sentence = sentence;
      this.covered = covered;
      this.replacement = replacement;
      this.correction = correction;
      this.dataset = dataset;
    }

    public String getLanguage() {
      return language;
    }

    public String getSentence() {
      return sentence;
    }

    public String getCovered() {
      return covered;
    }

    public String getReplacement() {
      return replacement;
    }

    public String getCorrection() {
      return correction;
    }

    public SuggestionChangesDataset getDataset() {
      return dataset;
    }
  }

  static class SuggestionTestResultData {
    private final SuggestionTestData input;
    private final List<RuleMatch> suggestions; // results for each experiment, lists should match

    SuggestionTestResultData(SuggestionTestData input, List<RuleMatch> suggestions) {
      this.input = input;
      this.suggestions = suggestions;
    }

    public SuggestionTestData getInput() {
      return input;
    }

    public List<RuleMatch> getSuggestions() {
      return suggestions;
    }
  }

  /**
   * Worker thread that runs specified experiments on sentences read from the shared queue
   */
  static class SuggestionTestThread extends Thread {
    private final Random sampler = new Random(0);

    private final ConcurrentLinkedQueue<Pair<SuggestionTestResultData, String>> results;
    private JLanguageTool standardLt;
    private Rule standardRule;
    private final Map<SuggestionChangesExperiment, Rule> rules;
    private final BlockingQueue<SuggestionTestData> tasks;

    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    SuggestionTestThread(BlockingQueue<SuggestionTestData> tasks, ConcurrentLinkedQueue<Pair<SuggestionTestResultData, String>> results) {
      rules = new HashMap<>();
      this.tasks = tasks;
      this.results = results;
    }

    @Override
    public void run() {
      Language lang = Languages.getLanguageForShortCode(SuggestionsChanges.getInstance().getConfig().language);
      init(lang);
      while (!isInterrupted()) {
        try {
          SuggestionTestData entry = tasks.poll(1L, TimeUnit.SECONDS);
          if (entry == null) {
            break;
          } else {
            doWork(entry);
          }
        } catch (InterruptedException | IOException e) {
          throw new RuntimeException(e);
        }
      }
    }


    private void init(Language lang) {
      Iterator<SuggestionChangesExperiment> iterator = SuggestionsChanges.getInstance().getExperiments().iterator();
      // parameters for experiments are shared via Singleton, so initialization must block
      synchronized (tasks) {
        SuggestionsChanges.getInstance().setCurrentExperiment(null);
        standardLt = new JLanguageTool(lang);
        standardRule = standardLt.getAllRules().stream().filter(Rule::isDictionaryBasedSpellingRule)
          .findFirst().orElse(null);

        while (iterator.hasNext()) {
          SuggestionChangesExperiment experiment = iterator.next();
          SuggestionsChanges.getInstance().setCurrentExperiment(experiment);

          JLanguageTool lt = new JLanguageTool(lang);
          try {
            lt.activateLanguageModelRules(new File(SuggestionsChanges.getInstance().getConfig().ngramLocation));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          Rule spellerRule = lt.getAllRules().stream().filter(Rule::isDictionaryBasedSpellingRule)
            .findFirst().orElse(null);

          rules.put(experiment, spellerRule);
        }
      }
    }


    void doWork(SuggestionTestData entry) throws IOException, InterruptedException {

      AnalyzedSentence sentence = standardLt.getAnalyzedSentence(entry.getSentence());

      if (entry.getDataset().enforceCorrect) {
        AnalyzedSentence correction = standardLt.getAnalyzedSentence(entry.getCorrection());
        RuleMatch[] correctionMatches = standardRule.match(correction);
        if (correctionMatches.length != 0) {
          String message = String.format("Error found in sentence '%s', ignoring because of 'enforceCorrect' flag.%n", entry.getCorrection());
          results.add(Pair.of(new SuggestionTestResultData(entry, null), message));
          return;
        }
      }

      // needs to be here to make combined filtering + sampling more transparent
      if (sampler.nextFloat() > entry.getDataset().sampleRate) {
        return;
      }

      List<SuggestionChangesExperiment> experiments = SuggestionsChanges.getInstance().getExperiments();
      int experimentId = 0;

      StringBuilder message = new StringBuilder();
      message.append(String.format("Checking candidates for correction '%s' -> '%s' in sentence '%s':%n",
        entry.getCovered(), entry.getReplacement(), entry.getSentence()));
      List<String> correct = new ArrayList<>();
      List<RuleMatch> gatheredSuggestions = new ArrayList<>(experiments.size());
      int textSize = sentence.getText().length();
      for (SuggestionChangesExperiment experiment : experiments) {
        experimentId++;

        Rule spellerRule = rules.get(experiment);
        if (spellerRule == null) {
          continue;
        }
        long startTime = System.currentTimeMillis();
        RuleMatch[] matches = spellerRule.match(sentence);
        long computationTime = System.currentTimeMillis() - startTime;

        for (RuleMatch match : matches) {
          String matchedWord = sentence.getText().substring(match.getFromPos(), match.getToPos());
          if (!matchedWord.equals(entry.getCovered())) {
            //System.out.println("Other spelling error detected, ignoring: " + matchedWord + " / " + covered);
            continue;
          }
          List<String> suggestions = match.getSuggestedReplacements();
          gatheredSuggestions.add(match);
          if (suggestions.isEmpty()) { // TODO should be tracked as well
            continue;
          }
          int position = suggestions.indexOf(entry.getReplacement());
          SuggestionsChanges.getInstance().trackExperimentResult(Pair.of(experiment, entry.getDataset()),
            position, textSize, computationTime);
          if (position == 0) {
            correct.add(String.valueOf(experimentId));
          }

          message.append(String.format("Experiment #%d: %s -> accepted @ #%d%n", experimentId, suggestions, position));
        }
      }
      message.append(String.format("Correct suggestions by experiments: %s%n", String.join(", ", correct)));

      results.add(Pair.of(new SuggestionTestResultData(entry, gatheredSuggestions), message.toString()));
    }
  }


  public void testText() throws IOException {
    File configFile = new File(System.getProperty("config", "SuggestionChangesTestConfig.json"));
    ObjectMapper mapper = new ObjectMapper(new JsonFactory().enable(JsonParser.Feature.ALLOW_COMMENTS));
    SuggestionChangesTestConfig config = mapper.readValue(configFile, SuggestionChangesTestConfig.class);
    SuggestionsChanges.init(config, null);

    Path inputFile = Paths.get(System.getProperty("input"));
    String text = String.join("\n", Files.readAllLines(inputFile));
    File ngrams = new File(SuggestionsChanges.getInstance().getConfig().ngramLocation);
    Language lang = Languages.getLanguageForShortCode(config.language);
    List<SuggestionChangesExperiment> experiments = SuggestionsChanges.getInstance().getExperiments();
    for (SuggestionChangesExperiment experiment : experiments) {
      SuggestionsChanges.getInstance().setCurrentExperiment(experiment);
      JLanguageTool lt = new JLanguageTool(lang);
      lt.activateLanguageModelRules(ngrams);
      List<RuleMatch> matches = lt.check(text);
      System.out.printf("%nExperiment %s running...%n", experiment);
      for (RuleMatch match : matches) {
        if (!match.getRule().isDictionaryBasedSpellingRule()) {
          continue;
        }

        String covered = text.substring(match.getFromPos(), match.getToPos());
        System.out.printf("Correction: '%s' -> %s%n", covered, match.getSuggestedReplacements());
      }
    }
  }

  public void testChanges() throws IOException, InterruptedException {

    File configFile = new File(System.getProperty("config", "SuggestionChangesTestConfig.json"));
    ObjectMapper mapper = new ObjectMapper(new JsonFactory().enable(JsonParser.Feature.ALLOW_COMMENTS));
    SuggestionChangesTestConfig config = mapper.readValue(configFile, SuggestionChangesTestConfig.class);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    String timestamp = dateFormat.format(new Date());
    Path loggingFile = Paths.get(config.logDir, String.format("suggestionChangesExperiment_%s.log", timestamp));
    Path datasetFile = Paths.get(config.logDir, String.format("suggestionChangesExperiment_%s.csv", timestamp));

    BufferedWriter writer = Files.newBufferedWriter(loggingFile);
    CSVPrinter datasetWriter = new CSVPrinter(Files.newBufferedWriter(datasetFile), CSVFormat.DEFAULT.withEscape('\\'));
    List<String> datasetHeader = new ArrayList<>(Arrays.asList("sentence", "correction", "covered", "replacement", "dataset_id"));

    SuggestionsChanges.init(config, writer);
    writer.write("Evaluation configuration: \n");
    String configContent = String.join("\n", Files.readAllLines(configFile.toPath()));
    writer.write(configContent);
    writer.write("\nRunning experiments: \n");
    int experimentId = 0;
    for (SuggestionChangesExperiment experiment : SuggestionsChanges.getInstance().getExperiments()) {
      experimentId++;
      writer.write(String.format("#%d: %s%n", experimentId, experiment));
      datasetHeader.add(String.format("experiment_%d_suggestions", experimentId));
      datasetHeader.add(String.format("experiment_%d_metadata", experimentId));
      datasetHeader.add(String.format("experiment_%d_suggestions_metadata", experimentId));
    }
    writer.newLine();
    datasetWriter.printRecord(datasetHeader);

    BlockingQueue<SuggestionTestData> tasks = new LinkedBlockingQueue<>(1000);
    ConcurrentLinkedQueue<Pair<SuggestionTestResultData, String>> results = new ConcurrentLinkedQueue<>();
    List<SuggestionTestThread> threads = new ArrayList<>();
    for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
      SuggestionTestThread worker = new SuggestionTestThread(tasks, results);
      worker.start();
      threads.add(worker);
    }

    // Thread for writing results from worker threads into CSV
    Thread logger = new Thread(() -> {
      try {
        long messages = 0;
        //noinspection InfiniteLoopStatement
        while (true) {
          Pair<SuggestionTestResultData, String> message = results.poll();
          if (message != null) {
            writer.write(message.getRight());

            SuggestionTestResultData result = message.getLeft();
            int datasetId = 1 + config.datasets.indexOf(result.getInput().getDataset());
            if (result != null && result.getSuggestions() != null &&
              !result.getSuggestions().isEmpty() && result.getSuggestions().stream()
              .noneMatch(m -> m.getSuggestedReplacements() == null || m.getSuggestedReplacements().isEmpty())) {

              List<Object> record = new ArrayList<>(Arrays.asList(
                result.getInput().getSentence(), result.getInput().getCorrection(),
                result.getInput().getCovered(), result.getInput().getReplacement(), datasetId));
              for (RuleMatch match : result.getSuggestions()) {
                List<String> suggestions = match.getSuggestedReplacements();
                record.add(mapper.writeValueAsString(suggestions));
                // features extracted by SuggestionsOrdererFeatureExtractor
                record.add(mapper.writeValueAsString(match.getFeatures()));
                List<SortedMap<String, Float>> suggestionsMetadata = new ArrayList<>();
                for (SuggestedReplacement replacement : match.getSuggestedReplacementObjects()) {
                  suggestionsMetadata.add(replacement.getFeatures());
                }
                record.add(mapper.writeValueAsString(suggestionsMetadata));
              }
              datasetWriter.printRecord(record);
            }

            if (++messages % 1000 == 0) {
              writer.flush();
              System.out.printf("Evaluated %d corrections.%n", messages);
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    logger.setDaemon(true);
    logger.start();

    // format straight from database dump
    String[] header = {"id", "sentence", "correction", "language", "rule_id", "suggestion_pos", "accept_language",
      "country", "region", "created_at", "updated_at", "covered", "replacement", "text_session_id", "client"};

    int datasetId = 0;
    // read data, send to worker threads via queue
    for (SuggestionChangesDataset dataset : config.datasets) {

      writer.write(String.format("Evaluating dataset #%d: %s.%n", ++datasetId, dataset));

      CSVFormat format = CSVFormat.DEFAULT;
      if (dataset.type.equals("dump")) {
        format = format.withEscape('\\').withNullString("\\N").withHeader(header);
      } else if (dataset.type.equals("artificial")) {
        format = format.withEscape('\\').withFirstRecordAsHeader();
      }
      try (CSVParser parser = new CSVParser(new FileReader(dataset.path), format)) {
        for (CSVRecord record : parser) {

          String lang = record.get("language");
          String rule = dataset.type.equals("dump") ? record.get("rule_id") : "";
          String covered = record.get("covered");
          String replacement = record.get("replacement");
          String sentence = record.get("sentence");
          String correction = record.isSet("correction") ? record.get("correction") : "";
          String acceptLanguage = dataset.type.equals("dump") ? record.get("accept_language") : "";

          if (sentence == null || sentence.trim().isEmpty()) {
            continue;
          }

          if (!config.language.equals(lang)) {
            continue; // TODO handle auto maybe?
          }
          if (dataset.type.equals("dump") && !config.rule.equals(rule)) {
            continue;
          }

          // correction column missing in export from doccano; workaround
          if (dataset.enforceCorrect && !record.isSet("correction")) {
            throw new IllegalStateException("enforceCorrect in dataset configuration enabled," +
              " but column 'correction' is not set for entry " + record);
          }

          if (dataset.type.equals("dump") && dataset.enforceAcceptLanguage) {
            if (acceptLanguage != null) {
              String[] entries = acceptLanguage.split(",", 2);
              if (entries.length == 2) {
                String userLanguage = entries[0]; // TODO: what to do with e.g. de-AT,de-DE;...
                if (!config.language.equals(userLanguage)) {
                  continue;
                }
              }
            }
          }

          tasks.put(new SuggestionTestData(lang, sentence, covered, replacement, correction, dataset));
        }
      }

    }

    for (Thread t : threads) {
      t.join();
    }
    logger.join(10000L);
    logger.interrupt();
    datasetWriter.close();
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 2) {
      System.out.println("Usage: SuggestionChangesTest [mode] CONFIG_FILE");
      System.out.println("Mode is either 'text' or 'csv'");
      System.exit(1);
    }
    String mode = args[0];
    String config = args[1];
    System.setProperty("config", config);

    SuggestionChangesTest test = new SuggestionChangesTest();

    if ("text".equals(mode)) {
      test.testText();
    } else if ("csv".equals(mode)) {
      test.testChanges();
    } else {
      System.out.println("Usage: SuggestionChangesTest [mode] CONFIG_FILE");
      System.out.println("Mode is either 'text' or 'csv'");
      System.exit(1);
    }
  }
}
