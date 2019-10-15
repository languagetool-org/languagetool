/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Oleg Serikov
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
package org.languagetool.rules.spelling.morfologik.suggestions_ordering;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.Demo;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.suggestions.SuggestionsOrderer;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;

@Ignore("Deleted data, test can't run")
public class SuggestionsOrdererTest {
  
  private String originalConfigNgramsPathValue;
  private boolean originalConfigMLSuggestionsOrderingEnabledValue;

  @Before
  public void setUp() throws Exception {
    originalConfigNgramsPathValue = SuggestionsOrdererConfig.getNgramsPath();
    originalConfigMLSuggestionsOrderingEnabledValue = SuggestionsOrdererConfig.isMLSuggestionsOrderingEnabled();
  }

  @After
  public void tearDown() {
    SuggestionsOrdererConfig.setNgramsPath(originalConfigNgramsPathValue);
    SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(originalConfigMLSuggestionsOrderingEnabledValue);
  }
  
  @Test
  public void orderSuggestionsUsingModelNonExistingRuleId() throws IOException {
    Language language = new Demo();
    String rule_id = "rule_id";
    testOrderingHappened(language, rule_id);
  }

  @Test
  public void orderSuggestionsUsingModelExistingRuleId() throws IOException {
    Language language = new Demo();
    String rule_id = "MORFOLOGIK_RULE_EN_US";
    testOrderingHappened(language, rule_id);
  }

  @Test
  public void orderSuggestionsWithEnabledML() throws IOException {
    SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(true);
    orderSuggestionsUsingModelExistingRuleId();
  }

  @Test
  public void orderSuggestionsWithDisabledML() throws IOException {
    SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(false);
    orderSuggestionsUsingModelExistingRuleId();
  }

  private void testOrderingHappened(Language language, String rule_id) throws IOException {
    JLanguageTool languageTool = new JLanguageTool(language);
    SuggestionsOrderer suggestionsOrderer = new SuggestionsOrdererGSoC(language, null, rule_id);

    String word = "wprd";
    String sentence = String.join(" ","a", word, "containing", "sentence");

    LinkedList<String> suggestions = new LinkedList<>();
    suggestions.add("word");
    suggestions.add("weird");

    int startPos = sentence.indexOf(word);
    int wordLength = word.length();
    List<String> suggestionsOrdered = suggestionsOrderer.orderSuggestionsUsingModel(
            suggestions, word, languageTool.getAnalyzedSentence(sentence), startPos);
    assertTrue(suggestionsOrdered.containsAll(suggestions));
  }

  public static void main(String[] args) throws IOException {
    Map<String, JLanguageTool> ltMap = new HashMap<>();
    Map<String, Rule> rules = new HashMap<>();
    Map<String, SuggestionsOrderer> ordererMap = new HashMap<>();
    final AtomicInteger numOriginalCorrect = new AtomicInteger(0),
      numReorderedCorrect = new AtomicInteger(0),
      numOtherCorrect = new AtomicInteger(0),
      numBothCorrect = new AtomicInteger(0),
      numTotalReorderings = new AtomicInteger(0),
      numMatches = new AtomicInteger(0);
    AtomicLong totalReorderingComputationTime = new AtomicLong(0),
          totalHunspellComputationTime = new AtomicLong(0);
    Runtime.getRuntime().addShutdownHook(new Thread(() ->
      System.out.printf("%n**** Correct Suggestions ****%nBoth: %d / Original: %d / Reordered: %d / Other: %d%n" +
          "Average time per reordering: %fms / Average time in match(): %fms%n",
        numBothCorrect.intValue(), numOriginalCorrect.intValue(), numReorderedCorrect.intValue(), numOtherCorrect.intValue(),
      (double) totalReorderingComputationTime.get() / numTotalReorderings.get(),
      (double) totalHunspellComputationTime.get() / numMatches.get())));
    SuggestionsOrdererConfig.setNgramsPath(args[1]);
    try (CSVParser parser = new CSVParser(new FileReader(args[0]), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
      for (CSVRecord record : parser) {
        String lang = record.get("language");
        String covered = record.get("covered");
        String replacement = record.get("replacement");
        String sentenceStr = record.get("sentence");

        if (lang.equals("auto") || !(lang.equals("en-US") || lang.equals("de-DE"))) { // TODO: debugging only
          continue; // TODO do language detection?
        }
        Language language = Languages.getLanguageForShortCode(lang);
        JLanguageTool lt = ltMap.computeIfAbsent(lang, langCode ->
          new JLanguageTool(language));
        Rule spellerRule = rules.computeIfAbsent(lang, langCode ->
            lt.getAllRules().stream().filter(Rule::isDictionaryBasedSpellingRule)
            .findFirst().orElse(null)
        );
        if (spellerRule == null) {
          continue;
        }
        SuggestionsOrderer orderer = null;
        try {
          orderer = ordererMap.computeIfAbsent(lang, langCode -> new SuggestionsOrdererGSoC(language,null, spellerRule.getId()));
        } catch (RuntimeException ignored) {
        }
        if (orderer == null) {
          continue;
        }
        numMatches.incrementAndGet();
        AnalyzedSentence sentence = lt.getAnalyzedSentence(sentenceStr);
        long startTime = System.currentTimeMillis();
        RuleMatch[] matches = spellerRule.match(sentence);
        totalHunspellComputationTime.addAndGet(System.currentTimeMillis() - startTime);
        for (RuleMatch match : matches) {
          String matchedWord = sentence.getText().substring(match.getFromPos(), match.getToPos());
          if (!matchedWord.equals(covered)) {
            //System.out.println("Other spelling error detected, ignoring: " + matchedWord + " / " + covered);
            continue;
          }
          List<String> original = match.getSuggestedReplacements();
          SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(true);
          numTotalReorderings.incrementAndGet();
          startTime = System.currentTimeMillis();
          List<String> reordered = orderer.orderSuggestionsUsingModel(original, matchedWord, sentence, match.getFromPos());
          totalReorderingComputationTime.addAndGet(System.currentTimeMillis() - startTime);
          SuggestionsOrdererConfig.setMLSuggestionsOrderingEnabled(false);
          if (original.isEmpty() || reordered.isEmpty()) {
            continue;
          }
          String firstOriginal = original.get(0);
          String firstReordered = reordered.get(0);
          if (firstOriginal.equals(firstReordered)) {
            if (firstOriginal.equals(replacement)) {
              numBothCorrect.incrementAndGet();
            } else {
              numOtherCorrect.incrementAndGet();
            }
            //System.out.println("No change for match: " + matchedWord);
          } else {
            System.out.println("Ordering changed for match " + matchedWord + ", before: " + firstOriginal + ", after: " + firstReordered + ", choosen: " + replacement);
            if (firstOriginal.equals(replacement)) {
              numOriginalCorrect.incrementAndGet();
            } else if (firstReordered.equals(replacement)) {
              numReorderedCorrect.incrementAndGet();
            } else {
              numOtherCorrect.incrementAndGet();
            }
          }
        }
      }
    }
  }

}
