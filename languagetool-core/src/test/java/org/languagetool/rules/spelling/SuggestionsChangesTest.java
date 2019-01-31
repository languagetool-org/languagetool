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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * needs to run with classpath of languagetool-standalone (needs access to modules for single languages)
 * configure via java properties (i.e. -Dproperty=value on command line)
 * correctionsFileLocation: path to csv dump with corrections data; created via
 select sentence, suggestion_pos, covered, replacement, language from corrections where rule_id = "..." and sentence <> "" and sentence is not null
 * languages: restrict languages that are tested; comma-separated list of language codes
 * suggestionsTestMode: Test original (A), updated (B) or both (AB) suggestion algorithms. Some changes may not be able to run in AB mode
 * SuggestionsChange: name of the change to test (use this when writing your own tests)
 * SuggestionsChangesTestAlternativeEnabled: set by this class - 0 for A, 1 for B
 *
 * Prints results on interrupt, or after finishing.
 */
@Ignore("Interactive test for evaluating changes to suggestions based on data")
public class SuggestionsChangesTest {

  private final AtomicInteger numOriginalCorrect = new AtomicInteger(0),
    numReorderedCorrect = new AtomicInteger(0),
    numOtherCorrect = new AtomicInteger(0),
    numBothCorrect = new AtomicInteger(0),
    numMatches = new AtomicInteger(0),
    numCorrectSuggestion = new AtomicInteger(0),
    numTotal = new AtomicInteger(0),
    sumPositionsOriginal = new AtomicInteger(0),
    sumPositionsReordered =  new AtomicInteger(0),
    sumPositions = new AtomicInteger(0);

  private static final Random sampler = new Random(0);
  private static final float SAMPLE_RATE = 0.10f;

  private String testMode;

  static class SuggestionTestData {
    private final String language;
    private final String sentence;
    private final String covered;
    private final String replacement;

    public SuggestionTestData(String language, String sentence, String covered, String replacement) {
      this.language = language;
      this.sentence = sentence;
      this.covered = covered;
      this.replacement = replacement;
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
  }

  class SuggestionTestThread extends Thread {

    private Map<Language, JLanguageTool> ltMap;
    private Map<Language, Rule> rules;
    private BlockingQueue<SuggestionTestData> tasks;

    SuggestionTestThread(BlockingQueue<SuggestionTestData> tasks) {
      ltMap = new HashMap<>();
      rules = new HashMap<>();
      this.tasks = tasks;
    }

    @Override
    public void run() {
      while(!isInterrupted()) {
        try {
          SuggestionTestData entry = tasks.poll(1L, TimeUnit.SECONDS);
          if (entry == null) {
            break;
          } else {
            doWork(entry);
          }
        } catch (InterruptedException | IOException e)  {
          throw new RuntimeException(e);
        }
      }
    }

    void doWork(SuggestionTestData entry) throws IOException {

      Language lang = Languages.getLanguageForShortCode(entry.getLanguage());

      JLanguageTool lt = ltMap.computeIfAbsent(lang, langCode -> {
        try {
          JLanguageTool tool = new JLanguageTool(lang);
          tool.activateLanguageModelRules(new File("ngrams/"));
          return tool;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      Rule spellerRule = rules.computeIfAbsent(lang, langCode ->
        lt.getAllRules().stream().filter(Rule::isDictionaryBasedSpellingRule)
          .findFirst().orElse(null));

      if (spellerRule == null) {
        return;
      }

      //AnalyzedSentence sentence = lt.getAnalyzedSentence(entry.getCovered());
      AnalyzedSentence sentence = lt.getAnalyzedSentence(entry.getSentence());

      if (testMode.equals("AB")) {
        System.setProperty("SuggestionsChangesTestAlternativeEnabled", "0");
        RuleMatch[] originalMatches = spellerRule.match(sentence);
        System.setProperty("SuggestionsChangesTestAlternativeEnabled", "1");
        RuleMatch[] alternativeMatches = spellerRule.match(sentence);
        assertEquals(originalMatches.length, alternativeMatches.length);

        for (int i = 0; i < originalMatches.length; i++) {
          RuleMatch original = originalMatches[i];
          RuleMatch alternative = alternativeMatches[i];

          String matchedWord = sentence.getText().substring(original.getFromPos(), original.getToPos());
          String matchedWord2 = sentence.getText().substring(alternative.getFromPos(), alternative.getToPos());
          assertEquals(matchedWord, matchedWord2);
          if (!matchedWord.equals(entry.getCovered())) {
            //System.out.println("Other spelling error detected, ignoring: " + matchedWord + " / " + covered);
            continue;
          }
          List<String> originalSuggestions = original.getSuggestedReplacements();
          List<String> alternativeSuggestions = alternative.getSuggestedReplacements();
          if (originalSuggestions.size() == 0 || alternativeSuggestions.size() == 0) {
            continue;
          }
          int posOriginal = originalSuggestions.indexOf(entry.getReplacement());
          if (posOriginal != -1) {
            sumPositionsOriginal.addAndGet(posOriginal);
          }
          int posReordered = alternativeSuggestions.indexOf(entry.getReplacement());
          if (posReordered != -1) {
            sumPositionsReordered.addAndGet(posReordered);
          }

          String firstOriginal = originalSuggestions.get(0);
          String firstAlternative = alternativeSuggestions.get(0);
          if (firstOriginal.equals(firstAlternative)) {
            if (firstOriginal.equals(entry.getReplacement())) {
              numBothCorrect.incrementAndGet();
            } else {
              numOtherCorrect.incrementAndGet();
            }
            System.out.println("No change for match: " + matchedWord);
          } else {
            String correct;
            if (firstOriginal.equals(entry.getReplacement())) {
              numOriginalCorrect.incrementAndGet();
              correct = "A";
            } else if (firstAlternative.equals(entry.getReplacement())) {
              numReorderedCorrect.incrementAndGet();
              correct = "B";
            } else {
              numOtherCorrect.incrementAndGet();
              correct = "other";
            }
            System.out.printf("Ordering changed for match %s, before: %s (#%d), after: %s (#%d), choosen: %s, correct: %s%n", matchedWord, firstOriginal, posOriginal, firstAlternative, posReordered,
              entry.getReplacement(), correct);
          }
        }

      } else {
        RuleMatch[] matches = spellerRule.match(sentence);

        for (RuleMatch match : matches) {
          String matchedWord = sentence.getText().substring(match.getFromPos(), match.getToPos());
          if (!matchedWord.equals(entry.getCovered())) {
            //System.out.println("Other spelling error detected, ignoring: " + matchedWord + " / " + covered);
            continue;
          }
          List<String> suggestions = match.getSuggestedReplacements();
          if (suggestions.size() == 0) {
            continue;
          }
          String first = suggestions.get(0);
          int position = suggestions.indexOf(entry.getReplacement());
          if (position != -1) {
            sumPositions.addAndGet(position);
          }
          numTotal.incrementAndGet();
          System.out.printf("Correction for %s: %s %s / chosen: %s -> position %d (%s)%n", entry.getCovered(), first,
            suggestions.subList(1, Math.min(suggestions.size(), 5)), entry.getReplacement(), position, sentence.getText());
          if (first.equals(entry.getReplacement())) {
            numCorrectSuggestion.incrementAndGet();
          }
        }
      }
    }
  }

  /***
   * TODO: document
   * @throws IOException
   */
  @Test
  public void testChanges() throws IOException, InterruptedException {

    String correctionsFileLocation = System.getProperty("correctionsFileLocation");
    assertNotEquals("needs corrections data", null, correctionsFileLocation);

    testMode = System.getProperty("suggestionsTestMode");
    assertThat(testMode, is(anyOf(equalTo("A"), equalTo("B"), equalTo("AB"))));

    if (testMode.equals("A") || testMode.equals("B")) {
      String modeValue = testMode.equals("A") ? "0" : "1";
      System.setProperty("SuggestionsChangesTestAlternativeEnabled", modeValue);
    }

    String languagesValue = System.getProperty("languages");
    Set<Language> languages = new HashSet<>();
    if (languagesValue == null) { // default -> all languages
      languages.addAll(Languages.get());
    } else {
      for (String langCode : languagesValue.split(",")) {
        languages.add(Languages.getLanguageForShortCode(langCode));
      }
    }

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (testMode.equals("AB")) {
        System.out.printf("%n**** Correct Suggestions ****%nBoth: %d / Original: %d / Reordered: %d / Other: %d%n",
          numBothCorrect.intValue(), numOriginalCorrect.intValue(), numReorderedCorrect.intValue(), numOtherCorrect.intValue());
        int total = numOriginalCorrect.intValue() + numReorderedCorrect.intValue() + numOtherCorrect.intValue() + numBothCorrect.intValue();
        float accuracyA = (float) (numBothCorrect.intValue() + numOriginalCorrect.intValue()) / total;
        float accuracyB = (float) (numBothCorrect.intValue() + numReorderedCorrect.intValue()) / total;
        System.out.printf("**** Accuracy ****%nA: %f / B: %f%n", accuracyA, accuracyB);
        System.out.printf("**** Positions ****%nA: %d / B: %d%n", sumPositionsOriginal.intValue(), sumPositionsReordered.intValue());
      } else {
        String name = testMode.equals("A") ? "Original" : "Alternative";
        int correct = numCorrectSuggestion.intValue();
        int total = numTotal.intValue();
        float percentage = 100f * ((float) correct / total);
        System.out.printf("%n**** Correct Suggestions ****%n %s: %d / %d (%f%%)%nSum of positions: %d%n",
          name, correct, total, percentage, sumPositions.intValue());
      }
    }));

    BlockingQueue<SuggestionTestData> tasks = new LinkedBlockingQueue<>(1000);
    List<SuggestionTestThread> threads = new ArrayList<>();
    for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
      SuggestionTestThread t = new SuggestionTestThread(tasks);
      t.start();
      threads.add(t);
    }

    try (CSVParser parser = new CSVParser(new FileReader(correctionsFileLocation), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
      for (CSVRecord record : parser) {

        if (sampler.nextFloat() > SAMPLE_RATE) {
          continue;
        }

        String lang = record.get("language");
        String covered = record.get("covered");
        String replacement = record.get("replacement");
        String sentence = record.get("sentence");

        if (lang.equals("auto")) {
          continue; // TODO do language detection?
        }
        Language language = Languages.getLanguageForShortCode(lang);

        if (!languages.contains(language)) {
          continue;
        }

        numMatches.incrementAndGet();

        tasks.put(new SuggestionTestData(lang, sentence, covered, replacement));
      }
    }
    for (Thread t : threads) {
      t.join();
    }
  }
}
