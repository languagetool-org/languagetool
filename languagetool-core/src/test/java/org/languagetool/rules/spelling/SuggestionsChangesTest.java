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

package org.languagetool.rules.spelling;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * needs to run with classpath of languagetool-standalone (needs access to modules for single languages)
 * configure via java properties (i.e. -Dproperty=value on command line)
 * correctionsFileLocation: path to csv dump with corrections data; created via
 * select sentence, suggestion_pos, covered, replacement, language from corrections where rule_id = "..." and sentence <> "" and sentence is not null
 * languages: restrict languages that are tested; comma-separated list of language codes
 * suggestionsTestMode: Test original (A), updated (B) or both (AB) suggestion algorithms. Some changes may not be able to run in AB mode
 * SuggestionsChange: name of the change to test (use this when writing your own tests)
 * SuggestionsChangesTestAlternativeEnabled: set by this class - 0 for A, 1 for B
 * ngramLocation
 * <p>
 * Prints results on interrupt, or after finishing.
 */
@Ignore("Interactive test for evaluating changes to suggestions based on data")
public class SuggestionsChangesTest {

  private static final Random sampler = new Random(0);

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
    private final List<List<String>> suggestions;

    SuggestionTestResultData(SuggestionTestData input, List<List<String>> suggestions) {
      this.input = input;
      this.suggestions = suggestions;
    }

    public SuggestionTestData getInput() {
      return input;
    }

    public List<List<String>> getSuggestions() {
      return suggestions;
    }
  }

  static class SuggestionTestThread extends Thread {

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

      List<SuggestionChangesExperiment> experiments = SuggestionsChanges.getInstance().getExperiments();
      int experimentId = 0;

      StringBuilder message = new StringBuilder();
      message.append(String.format("Checking candidates for correction '%s' -> '%s' in sentence '%s':%n",
        entry.getCovered(), entry.getReplacement(), entry.getSentence()));
      List<String> correct = new ArrayList<>();
      List<List<String>> gatheredSuggestions = new ArrayList<>(experiments.size());
      for (SuggestionChangesExperiment experiment : experiments) {
        experimentId++;

        Rule spellerRule = rules.get(experiment);
        if (spellerRule == null) {
          continue;
        }
        RuleMatch[] matches = spellerRule.match(sentence);

        for (RuleMatch match : matches) {
          String matchedWord = sentence.getText().substring(match.getFromPos(), match.getToPos());
          if (!matchedWord.equals(entry.getCovered())) {
            //System.out.println("Other spelling error detected, ignoring: " + matchedWord + " / " + covered);
            continue;
          }
          List<String> suggestions = match.getSuggestedReplacements();
          gatheredSuggestions.add(suggestions);
          if (suggestions.size() == 0) { // TODO should be tracked as well
            continue;
          }
          int position = suggestions.indexOf(entry.getReplacement());
          SuggestionsChanges.getInstance().trackExperimentResult(Pair.of(experiment, entry.getDataset()), position);
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


  @Ignore
  @Test
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

  /***
   * TODO: document
   * @throws IOException
   */
  @Ignore("interactive")
  @Test
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
    List<String> datasetHeader = new ArrayList<>(Arrays.asList("sentence", "correction", "covered", "replacement"));

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

    Thread logger = new Thread(() -> {
      try {
        long messages = 0;
        //noinspection InfiniteLoopStatement
        while (true) {
          Pair<SuggestionTestResultData, String> message = results.poll();
          if (message != null) {
            writer.write(message.getRight());

            SuggestionTestResultData result = message.getLeft();
            if (result != null && result.getSuggestions() != null &&
              !result.getSuggestions().isEmpty() && result.getSuggestions().stream().noneMatch(s -> s.size() == 0)) {
              List<Object> record = new ArrayList<>(Arrays.asList(
                result.getInput().getSentence(), result.getInput().getCorrection(),
                result.getInput().getCovered(), result.getInput().getReplacement()));
              for (List<String> suggestions : result.getSuggestions()) {
                List<String> reduced = suggestions.subList(0, Math.min(5, suggestions.size()));
                record.add(mapper.writeValueAsString(reduced));
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

    String[] header = {"id", "sentence", "correction", "language", "rule_id", "suggestion_pos", "accept_language",
      "country", "region", "created_at", "updated_at", "covered", "replacement", "text_session_id", "client"};

    for (SuggestionChangesDataset dataset : config.datasets) {

      CSVFormat format = CSVFormat.DEFAULT;
      if (dataset.type.equals("dump")) {
        format = format.withEscape('\\').withNullString("\\N").withHeader(header);
      } else if (dataset.type.equals("artificial")) {
        format = format.withEscape('\\').withFirstRecordAsHeader();
      }
      try (CSVParser parser = new CSVParser(new FileReader(dataset.path), format)) {
        for (CSVRecord record : parser) {

          if (sampler.nextFloat() > dataset.sampleRate) {
            continue;
          }

          String lang = record.get("language");
          String rule = dataset.type.equals("dump") ? record.get("rule_id") : "";
          String covered = record.get("covered");
          String replacement = record.get("replacement");
          String sentence = record.get("sentence");
          String correction = record.get("correction");

          if (sentence == null || sentence.trim().isEmpty()) {
            continue;
          }

          if (!config.language.equals(lang)) {
            continue; // TODO handle auto maybe?
          }
          if (dataset.type.equals("dump") && !config.rule.equals(rule)) {
            continue;
          }

          tasks.put(new SuggestionTestData(lang, sentence, covered, replacement, correction, dataset));
        }
      }

    }

    for (Thread t : threads) {
      t.join();
    }
    logger.join(10000L);
    datasetWriter.close();
  }
}
