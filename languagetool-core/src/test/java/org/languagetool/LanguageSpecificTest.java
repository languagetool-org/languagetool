/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.languagetool.language.Demo;
import org.languagetool.rules.*;
import org.languagetool.rules.ngrams.FakeLanguageModel;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;
import org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static junit.framework.Assert.fail;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class LanguageSpecificTest {

  protected void runTests(Language lang) throws IOException {
    runTests(lang, null);
  }

  protected void runTests(Language lang, String onlyRunCode) throws IOException {
    new WordListValidatorTest().testWordListValidity(lang);
    testNoQuotesAroundSuggestion(lang);
    testJavaRules(onlyRunCode);
    //testExampleAvailable(onlyRunCode);
    testConfusionSetLoading();
    countTempOffRules(lang);
    testCoherencyBaseformIsOtherForm(lang);
    try {
      new DisambiguationRuleTest().testDisambiguationRulesFromXML();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void testCoherencyBaseformIsOtherForm(Language lang) throws IOException {
    if (lang.getShortCode().equals("km")) {
      // "coherency.txt" is for a different rule for Khmer
      return;
    }
    JLanguageTool lt = new JLanguageTool(lang);
    TestTools.disableAllRulesExcept(lt, "EN_WORD_COHERENCY");
    WordCoherencyDataLoader loader = new WordCoherencyDataLoader();
    String path = "/" + lang.getShortCode() + "/coherency.txt";
    if (!JLanguageTool.getDataBroker().ruleFileExists(path)) {
      System.out.println("File not found (okay for many languages): "+ path);
      return;
    }
    System.out.println("Checking " + path + "...");
    Map<String, Set<String>> map = loader.loadWords(path);
    List<String> invalid = new ArrayList<>();
    for (String key : map.keySet()) {
      List<RuleMatch> matches = lt.check(key);
      if (matches.size() > 0) {
        invalid.add(key);
      }
    }
    if (invalid.size() > 0) {
      fail(lang + ": These words trigger the rule because their base form is one of the forms in coherency.txt, giving false alarms: " + invalid);
    }
  }
  
  private final static Map<String, Integer> idToExpectedMatches = new HashMap<>();
  static {
    idToExpectedMatches.put("STYLE_REPEATED_WORD_RULE_DE", 2);
  }
  private void testJavaRules(String onlyRunCode) throws IOException {
    Map<String,String> idsToClassName = new HashMap<>();
    Set<Class> ruleClasses = new HashSet<>();
    for (Language language : Languages.getWithDemoLanguage()) {
      if (onlyRunCode != null && !language.getShortCodeWithCountryAndVariant().equals(onlyRunCode)) {
        System.out.println("Skipping " + language);   // speed up for languages that are sub classes (e.g. simple German)
        continue;
      }
      JLanguageTool lt = new JLanguageTool(language);
      List<Rule> allRules = lt.getAllRules();
      for (Rule rule : allRules) {
        if (!(rule instanceof AbstractPatternRule)) {
          assertIdUniqueness(idsToClassName, ruleClasses, language, rule);
          assertIdValidity(language, rule);
          assertTrue(rule.supportsLanguage(language));
          testExamples(rule, lt);
        }
      }
    }
  }

  private void testConfusionSetLoading() {
    for (Language language : Languages.get()) {
      try {
        List<Rule> rules = language.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), new FakeLanguageModel(), null);
        if (rules.size() > 0) {
          String path = "/" + language.getShortCode() + "/confusion_sets.txt";
          try (InputStream confusionSetStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path)) {
            ConfusionSetLoader confusionSetLoader = new ConfusionSetLoader(new Demo());
            confusionSetLoader.loadConfusionPairs(confusionSetStream);  // would throw Exception if there's a problem
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Could not load confusion pairs for " + language.getName(), e);
      }
    }
  }

  private void testExampleAvailable(String onlyRunCode) {
    for (Language language : Languages.getWithDemoLanguage()) {
      if (onlyRunCode != null && !language.getShortCodeWithCountryAndVariant().equals(onlyRunCode)) {
        System.out.println("Skipping " + language);   // speed up for languages that are sub classes (e.g. simple German)
        continue;
      }
      JLanguageTool lt = new JLanguageTool(language);
      List<Rule> allRules = lt.getAllRules();
      for (Rule rule : allRules) {
        if (rule.getIncorrectExamples().size() == 0) {
          System.err.println("*** WARNING: " + language.getShortCodeWithCountryAndVariant() + " rule " + rule.getId() + " has no incorrect examples");
        }
      }
    }
  }

  // no quotes needed around <suggestion>...</suggestion> in XML:
  private void testNoQuotesAroundSuggestion(Language lang) throws IOException {
    if (lang.getShortCode().equals("fa") || lang.getShortCode().equals("zh")) {
      // ignore languages not maintained anyway
      System.out.println("Skipping testNoQuotesAroundSuggestion for " + lang.getName());
      return;
    }
    String dirBase = JLanguageTool.getDataBroker().getRulesDir() + "/" + lang.getShortCode() + "/";
    for (String ruleFileName : lang.getRuleFileNames()) {
      if (ruleFileName.contains("-test-")) {
        continue;
      }
      InputStream is = this.getClass().getResourceAsStream(ruleFileName);
      List<AbstractPatternRule> rules = new PatternRuleLoader().getRules(is, dirBase + "/" + ruleFileName);
      for (AbstractPatternRule rule : rules) {
        String message = rule.getMessage();
        if (message.matches(".*['\"«»“”’]<suggestion.*") && message.matches(".*</suggestion>['\"«»“”’].*")) {
          fail(lang.getName() + " rule " + rule.getFullId() + " uses quotes around <suggestion>...<suggestion> in its <message>, this should be avoided: '" + message + "'");
        }
      }
    }
  }

  protected void testDemoText(Language lang, String text, List<String> expectedMatchIds) throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    List<RuleMatch> matches = lt.check(text);
    int i = 0;
    List<String> actualRuleIds = new ArrayList<>();
    for (RuleMatch match : matches) {
      actualRuleIds.add(match.getRule().getId());
    }
    if (expectedMatchIds.size() != actualRuleIds.size()) {
      failTest(lang, text, expectedMatchIds, actualRuleIds);
    }
    for (String actualRuleId : actualRuleIds) {
      if (!expectedMatchIds.get(i).equals(actualRuleId)) {
        failTest(lang, text, expectedMatchIds, actualRuleIds);
      }
      i++;
    }
  }

  private void failTest(Language lang, String text, List<String> expectedMatchIds, List<String> actualRuleIds) {
    fail("The website demo text matches for " + lang + " have changed. Demo text:\n" + text +
            "\nExpected rule matches:\n" + expectedMatchIds + "\nActual rule matches:\n" + actualRuleIds);
  }

  private void assertIdUniqueness(Map<String,String> ids, Set<Class> ruleClasses, Language language, Rule rule) {
    String ruleId = rule.getId();
    if (ids.containsKey(ruleId) && !ruleClasses.contains(rule.getClass())) {
      throw new RuntimeException("Rule id occurs more than once: '" + ruleId + "', one of them " +
              rule.getClass() + ", the other one " + ids.get(ruleId) + ", language: " + language);
    }
    ids.put(ruleId, rule.getClass().getName());
    ruleClasses.add(rule.getClass());
  }

  private void assertIdValidity(Language language, Rule rule) {
    String ruleId = rule.getId();
    if (!ruleId.matches("^[A-Z_][A-Z0-9_]+$")) {
      throw new RuntimeException("Invalid character in rule id: '" + ruleId + "', language: "
              + language + ", only [A-Z0-9_] are allowed and the first character must be in [A-Z_]");
    }
  }

  private void testExamples(Rule rule, JLanguageTool lt) throws IOException {
    testCorrectExamples(rule, lt);
    testIncorrectExamples(rule, lt);
  }

  private void testCorrectExamples(Rule rule, JLanguageTool lt) throws IOException {
    List<CorrectExample> correctExamples = rule.getCorrectExamples();
    for (CorrectExample correctExample : correctExamples) {
      String input = cleanMarkers(correctExample.getExample());
      enableOnlyOneRule(lt, rule);
      List<RuleMatch> ruleMatches = lt.check(input);
      assertEquals("Got unexpected rule match for correct example sentence:\n"
              + "Text: " + input + "\n"
              + "Rule: " + rule.getId() + "\n"
              + "Matches: " + ruleMatches, 0, ruleMatches.size());
    }
  }

  private void testIncorrectExamples(Rule rule, JLanguageTool lt) throws IOException {
    List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
    for (IncorrectExample incorrectExample : incorrectExamples) {
      String input = cleanMarkers(incorrectExample.getExample());
      enableOnlyOneRule(lt, rule);
      List<RuleMatch> ruleMatches = lt.check(input);
      assertEquals("Did not get the expected rule match for the incorrect example sentence:\n"
              + "Text: " + input + "\n"
              + "Rule: " + rule.getId() + "\n"
              + "Matches: " + ruleMatches, (int)idToExpectedMatches.getOrDefault(rule.getId(), 1), ruleMatches.size());
    }
  }

  private void enableOnlyOneRule(JLanguageTool lt, Rule ruleToActivate) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    lt.enableRule(ruleToActivate.getId());
  }

  private String cleanMarkers(String example) {
    return example.replace("<marker>", "").replace("</marker>", "");
  }

  private void countTempOffRules(Language lang) {
    JLanguageTool lt = new JLanguageTool(lang);
    int count = 0;
    for (Rule rule : lt.getAllRules()) {
      if (rule.isDefaultTempOff()) {
        count++;
      }
    }
    System.out.println("Number of default='temp_off' rules for " + lang + ": " + count);
    int limit = 10;
    if (count > limit) {
      System.err.println("################################################################################################");
      System.err.println("WARNING: " + count + " default='temp_off' rules for " + lang + ", please make sure to turn on these");
      System.err.println("WARNING: rules after they have been tested (or use default='off' to turn them off permanently)");
      System.err.println("WARNING: (this warning appears if there are more than " + limit + " default='temp_off' rules)");
      System.err.println("################################################################################################");
    }
  }
  
}
