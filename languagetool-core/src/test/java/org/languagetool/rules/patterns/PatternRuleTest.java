/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.*;

import junit.framework.TestCase;

import org.languagetool.*;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

/**
 * @author Daniel Naber
 */
public class PatternRuleTest extends TestCase {

  // A test sentence should only be a single sentence - if that's not the case it can
  // happen that rules are checked as being correct that in reality will never match.
  // This check prints a warning for affected rules, but it's disabled by default because
  // it makes the tests very slow:
  private static final boolean CHECK_WITH_SENTENCE_SPLITTING = false;
  
  public void testFake() {
    // there's no test here - the languages are supposed to extend this class and call runGrammarRulesFromXmlTest() 
  }

  public void testSupportsLanguage() {
    FakeLanguage fakeLanguage1 = new FakeLanguage("yy");
    FakeLanguage fakeLanguage2 = new FakeLanguage("zz");
    PatternRule patternRule1 = new PatternRule("ID", fakeLanguage1, Collections.<Element>emptyList(), "", "", "");
    assertTrue(patternRule1.supportsLanguage(fakeLanguage1)); 
    assertFalse(patternRule1.supportsLanguage(fakeLanguage2));
    FakeLanguage fakeLanguage1WithVariant1 = new FakeLanguage("zz", "VAR1");
    FakeLanguage fakeLanguage1WithVariant2 = new FakeLanguage("zz", "VAR2");
    PatternRule patternRuleVariant1 = new PatternRule("ID", fakeLanguage1WithVariant1, Collections.<Element>emptyList(), "", "", "");
    assertTrue(patternRuleVariant1.supportsLanguage(fakeLanguage1WithVariant1));    
    assertFalse(patternRuleVariant1.supportsLanguage(fakeLanguage1));
    assertFalse(patternRuleVariant1.supportsLanguage(fakeLanguage2));
    assertFalse(patternRuleVariant1.supportsLanguage(fakeLanguage1WithVariant2));
  }

  /**
   * To be called from language modules. Language.REAL_LANGUAGES knows only the languages that's in the classpath.
   * @param ignoredLanguage ignore this language - useful to speed up tests from languages that 
   *                        have another language as a dependency
   */
  protected void runGrammarRulesFromXmlTest(Language ignoredLanguage) throws IOException {
    int count = 0;
    for (final Language lang : Language.REAL_LANGUAGES) {
      if (ignoredLanguage.getShortNameWithCountryAndVariant().equals(lang.getShortNameWithCountryAndVariant())) {
        continue;
      }
      runGrammarRuleForLanguage(lang);
      count++;
    }
    if (count == 0) {
      System.err.println("Warning: no languages found in classpath - cannot run any grammar rule tests");
    }
  }
  
  /**
   * To be called from language modules. Language.REAL_LANGUAGES knows only the languages that's in the classpath.
   */
  protected void runGrammarRulesFromXmlTest() throws IOException {
    for (final Language lang : Language.REAL_LANGUAGES) {
      runGrammarRuleForLanguage(lang);
    }
    if (Language.REAL_LANGUAGES.length == 0) {
      System.err.println("Warning: no languages found in classpath - cannot run any grammar rule tests");
    }
  }

  private void runGrammarRuleForLanguage(Language lang) throws IOException {
    if (skipCountryVariant(lang)) {
      System.out.println("Skipping " + lang + " because there are no specific rules for that variant");
      return;
    }
    runTestForLanguage(lang);
  }

  private boolean skipCountryVariant(Language lang) {
    final ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    boolean hasGrammarFiles = false;
    for (String grammarFile : getGrammarFileNames(lang)) {
      if (dataBroker.ruleFileExists(grammarFile)) {
        hasGrammarFiles = true;
      }
    }
    return !hasGrammarFiles && Language.REAL_LANGUAGES.length > 1;
  }

  private List<String> getGrammarFileNames(Language lang) {
    final String shortNameWithVariant = lang.getShortNameWithCountryAndVariant();
    final List<String> fileNames = new ArrayList<>();
    for (String ruleFile : lang.getRuleFileNames()) {
      final String nameOnly = new File(ruleFile).getName();
      final String fileName;
      if (shortNameWithVariant.contains("-x-")) {
        fileName = lang.getShortName() + "/" + nameOnly;
      } else if (shortNameWithVariant.contains("-") && !shortNameWithVariant.equals("xx-XX")
              && !shortNameWithVariant.endsWith("-ANY") && Language.REAL_LANGUAGES.length > 1) {
        fileName = lang.getShortName() + "/" + shortNameWithVariant + "/" + nameOnly;
      } else {
        fileName = lang.getShortName() + "/" + nameOnly;
      }
      if (!fileNames.contains(fileName)) {
        fileNames.add(fileName);
      }
    }
    return fileNames;
  }

  private void runGrammarRulesFromXmlTestIgnoringLanguages(Set<Language> ignoredLanguages) throws IOException {
    System.out.println("Known languages: " + Arrays.toString(Language.LANGUAGES));
    for (final Language lang : Language.LANGUAGES) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      runTestForLanguage(lang);
    }
  }

  public void runTestForLanguage(Language lang) throws IOException {
    validatePatternFile(lang);
    System.out.print("Running pattern rule tests for " + lang.getName() + "... ");
    final JLanguageTool languageTool = new MultiThreadedJLanguageTool(lang);
    if (CHECK_WITH_SENTENCE_SPLITTING) {
      languageTool.activateDefaultPatternRules();
      disableSpellingRules(languageTool);
    }
    final JLanguageTool allRulesLanguageTool = new MultiThreadedJLanguageTool(lang);
    allRulesLanguageTool.activateDefaultPatternRules();
    validateRuleIds(lang, allRulesLanguageTool);
    final List<PatternRule> rules = new ArrayList<>();
    for (String patternRuleFileName : lang.getRuleFileNames()) {
      rules.addAll(languageTool.loadPatternRules(patternRuleFileName));
    }
    for (PatternRule rule : rules) {
      // Test the rule pattern.
      PatternTestTools.warnIfRegexpSyntaxNotKosher(rule.getElements(),
              rule.getId(), rule.getSubId(), lang);

      // Test the rule antipatterns.
      List<DisambiguationPatternRule> antiPatterns = rule.getAntiPatterns();
      for (DisambiguationPatternRule antiPattern : antiPatterns) {
        PatternTestTools.warnIfRegexpSyntaxNotKosher(antiPattern.getElements(),
            antiPattern.getId(), antiPattern.getSubId(), lang);
      }
      if (rule.getCorrectExamples().size() == 0) {
        boolean correctionExists = false;
        for (IncorrectExample incorrectExample : rule.getIncorrectExamples()) {
          if (incorrectExample.getCorrections().size() > 0) {
            correctionExists = true;
            break;
          }
        }
        if (!correctionExists) {
          fail("Rule " + rule.getId() + "[" + rule.getSubId() + "]" + " in language " + lang
                  + " needs at least one <example> with a 'correction' attribute"
                  + " or one <example> of type='correct'.");
        }
      }
    }
    testGrammarRulesFromXML(rules, languageTool, allRulesLanguageTool, lang);
    System.out.println(rules.size() + " rules tested.");
  }

  private void validatePatternFile(Language lang) throws IOException {
    final XMLValidator validator = new XMLValidator();
    final List<String> grammarFiles = getGrammarFileNames(lang);
    for (String grammarFile : grammarFiles) {
      System.out.println("Running XML validation for " + grammarFile + "...");
      final String rulesDir = JLanguageTool.getDataBroker().getRulesDir();
      final String ruleFilePath = rulesDir + "/" + grammarFile;
      final InputStream xmlStream = this.getClass().getResourceAsStream(ruleFilePath);
      if (xmlStream == null) {
        System.out.println("No rule file found at " + ruleFilePath + " in classpath");
        continue;
      }
      try {
        // if there are multiple xml grammar files we'll prepend all unification elements 
        // from the first file to the rest of them 
        if (grammarFiles.size() > 1 && !grammarFiles.get(0).equals(grammarFile)) {
          validator.validateWithXmlSchema(rulesDir + "/" + grammarFiles.get(0), ruleFilePath, rulesDir + "/rules.xsd");
        } else {
          validator.validateWithXmlSchema(ruleFilePath, rulesDir + "/rules.xsd");
        }
      } finally {
        xmlStream.close();
      }
    }
  }

  private void validateRuleIds(Language lang, JLanguageTool languageTool) {
    final List<Rule> allRules = languageTool.getAllRules();
    final Set<String> ids = new HashSet<>();
    final Set<Class> ruleClasses = new HashSet<>();
    for (Rule rule : allRules) {
      assertIdUniqueness(ids, ruleClasses, lang, rule);
    }
  }

  private void assertIdUniqueness(Set<String> ids, Set<Class> ruleClasses, Language language, Rule rule) {
    final String ruleId = rule.getId();
    if (ids.contains(ruleId) && !ruleClasses.contains(rule.getClass())) {
      throw new RuntimeException("Rule id occurs more than once: '" + ruleId + "', language: " + language);
    }
    ids.add(ruleId);
    ruleClasses.add(rule.getClass());
  }

  private void disableSpellingRules(JLanguageTool languageTool) {
    final List<Rule> allRules = languageTool.getAllRules();
    for (Rule rule : allRules) {
      if (rule instanceof SpellingCheckRule) {
        languageTool.disableRule(rule.getId());
      }
    }
  }

  public void testGrammarRulesFromXML(final List<PatternRule> rules,
                                       final JLanguageTool languageTool,
                                       final JLanguageTool allRulesLanguageTool, final Language lang) throws IOException {
    final Map<String, PatternRule> complexRules = new HashMap<>();
    for (final PatternRule rule : rules) {
      testCorrectSentences(languageTool, allRulesLanguageTool, lang, rule);
      testBadSentences(languageTool, allRulesLanguageTool, lang, complexRules, rule);
    }
    if (!complexRules.isEmpty()) {
      final Set<String> set = complexRules.keySet();
      final List<PatternRule> badRules = new ArrayList<>();
      for (String aSet : set) {
        final PatternRule badRule = complexRules.get(aSet);
        if (badRule != null) {
          badRule.notComplexPhrase();
          badRule.setMessage("The rule contains a phrase that never matched any incorrect example.");
          badRules.add(badRule);
        }
      }
      if (!badRules.isEmpty()) {
        testGrammarRulesFromXML(badRules, languageTool, allRulesLanguageTool, lang);
      }
    }
  }

  private void testBadSentences(JLanguageTool languageTool, JLanguageTool allRulesLanguageTool, Language lang,
                                Map<String, PatternRule> complexRules, PatternRule rule) throws IOException {
    final List<IncorrectExample> badSentences = rule.getIncorrectExamples();
    // necessary for XML Pattern rules containing <or>
    List<PatternRule> rules = allRulesLanguageTool.getPatternRulesByIdAndSubId(rule.getId(), rule.getSubId());
    for (IncorrectExample origBadExample : badSentences) {
      // enable indentation use
      final String origBadSentence = origBadExample.getExample().replaceAll("[\\n\\t]+", "");
      final List<String> suggestedCorrections = origBadExample.getCorrections();
      final int expectedMatchStart = origBadSentence.indexOf("<marker>");
      final int expectedMatchEnd = origBadSentence.indexOf("</marker>") - "<marker>".length();
      if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
        fail(lang + ": No error position markup ('<marker>...</marker>') in bad example in rule " + rule);
      }
      final String badSentence = cleanXML(origBadSentence);
      assertTrue(badSentence.trim().length() > 0);
      
      // necessary for XML Pattern rules containing <or>
      List<RuleMatch> matches = new ArrayList<>();
      for (Rule auxRule : rules) { 
        matches.addAll(getMatches(auxRule, badSentence, languageTool));
      }
      
      if (!rule.isWithComplexPhrase()) {
        if (matches.size() != 1) {
          final AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(badSentence);
          final StringBuilder sb = new StringBuilder("Analyzed token readings:");
          for (AnalyzedTokenReadings atr : analyzedSentence.getTokens()) {
            sb.append(" ").append(atr.toString());
          }
          fail(lang + " rule " + rule.getId() + "[" + rule.getSubId() + "]" + ":\n\"" + badSentence + "\"\n"
                  + "Errors expected: 1\n"
                  + "Errors found   : " + matches.size() + "\n"
                  + "Message: " + rule.getMessage() + "\n" + sb + "\nMatches: " + matches);
        }
        assertEquals(lang
                + ": Incorrect match position markup (start) for rule " + rule + ", sentence: " + badSentence,
                expectedMatchStart, matches.get(0).getFromPos());
        assertEquals(lang
                + ": Incorrect match position markup (end) for rule " + rule + ", sentence: " + badSentence,
                expectedMatchEnd, matches.get(0).getToPos());
        // make sure suggestion is what we expect it to be
        if (suggestedCorrections != null && suggestedCorrections.size() > 0) {
          assertTrue("You specified a correction but your message has no suggestions in rule " + rule,
            rule.getMessage().contains("<suggestion>") || rule.getSuggestionsOutMsg().contains("<suggestion>")
          );
          assertEquals(lang + ": Incorrect suggestions: "
              + suggestedCorrections.toString() + " != "
              + matches.get(0).getSuggestedReplacements() + " for rule " + rule + "[" + rule.getSubId() + "] on input: " + badSentence,
              suggestedCorrections, matches.get(0).getSuggestedReplacements());
        }
        // make sure the suggested correction doesn't produce an error:
        if (matches.get(0).getSuggestedReplacements().size() > 0) {
          final int fromPos = matches.get(0).getFromPos();
          final int toPos = matches.get(0).getToPos();
          for (final String replacement : matches.get(0).getSuggestedReplacements()) {
            final String fixedSentence = badSentence.substring(0, fromPos)
                + replacement + badSentence.substring(toPos);
            matches = getMatches(rule, fixedSentence, languageTool);
            if (matches.size() > 0) {
                fail("Incorrect input:\n"
                        + "  " + badSentence
                          + "\nCorrected sentence:\n"
                        + "  " + fixedSentence
                        + "\nBy Rule:\n"
                        + "  " + rule
                        + "\nThe correction triggered an error itself:\n"
                        + "  " + matches.get(0) + "\n");
            }
          }
        }
      } else { // for multiple rules created with complex phrases

        matches = getMatches(rule, badSentence, languageTool);
        if (matches.size() == 0
            && !complexRules.containsKey(rule.getId() + badSentence)) {
          complexRules.put(rule.getId() + badSentence, rule);
        }

        if (matches.size() != 0) {
          complexRules.put(rule.getId() + badSentence, null);
          assertTrue(lang + ": Did expect one error in: \"" + badSentence
              + "\" (Rule: " + rule + "), got " + matches.size(),
              matches.size() == 1);
          assertEquals(lang + ": Incorrect match position markup (start) for rule " + rule,
                  expectedMatchStart, matches.get(0).getFromPos());
          assertEquals(lang + ": Incorrect match position markup (end) for rule " + rule,
                  expectedMatchEnd, matches.get(0).getToPos());
          assertSuggestions(suggestedCorrections, lang, matches, rule);
          assertSuggestionsDoNotCreateErrors(languageTool, rule, badSentence, matches);
        }
      }

      // check for overlapping rules
      /*matches = getMatches(rule, badSentence, languageTool);
      final List<RuleMatch> matchesAllRules = allRulesLanguageTool.check(badSentence);
      for (RuleMatch match : matchesAllRules) {
        if (!match.getRule().getId().equals(rule.getId()) && matches.length != 0
            && rangeIsOverlapping(matches[0].getFromPos(), matches[0].getToPos(), match.getFromPos(), match.getToPos()))
          System.err.println("WARN: " + lang.getShortName() + ": '" + badSentence + "' in "
                  + rule.getId() + " also matched " + match.getRule().getId());
      }*/

    }
  }

  /**
   * returns true if [a, b] has at least one number in common with [x, y]
   */
  private boolean rangeIsOverlapping(int a, int b, int x, int y) {
    if (a < x) {
      return x <= b;
    } else {
      return a <= y;
    }
  }

  private void assertSuggestions(List<String> suggestedCorrections, Language lang, List<RuleMatch> matches, Rule rule) {
    if (suggestedCorrections != null && suggestedCorrections.size() > 0) {
      final boolean isExpectedSuggestion = suggestedCorrections.equals(matches.get(0).getSuggestedReplacements());
      assertTrue(lang + ": Incorrect suggestions: "
              + suggestedCorrections.toString() + " != " + matches.get(0).getSuggestedReplacements()
              + " for rule " + rule, isExpectedSuggestion);
    }
  }

  private void assertSuggestionsDoNotCreateErrors(JLanguageTool languageTool, PatternRule rule, String badSentence, List<RuleMatch> matches) throws IOException {
    if (matches.get(0).getSuggestedReplacements().size() > 0) {
      final int fromPos = matches.get(0).getFromPos();
      final int toPos = matches.get(0).getToPos();
      for (final String replacement : matches.get(0).getSuggestedReplacements()) {
        final String fixedSentence = badSentence.substring(0, fromPos)
            + replacement + badSentence.substring(toPos);
        final List<RuleMatch> tempMatches = getMatches(rule, fixedSentence, languageTool);
        assertEquals("Corrected sentence for rule " + rule
            + " triggered error: " + fixedSentence, 0, tempMatches.size());
      }
    }
  }

  private void testCorrectSentences(JLanguageTool languageTool, JLanguageTool allRulesLanguageTool,
                                    Language lang, PatternRule rule) throws IOException {
    final List<String> goodSentences = rule.getCorrectExamples();
    // necessary for XML Pattern rules containing <or>
    List<PatternRule> rules = allRulesLanguageTool.getPatternRulesByIdAndSubId(rule.getId(), rule.getSubId());
    for (String goodSentence : goodSentences) {
      // enable indentation use
      goodSentence = goodSentence.replaceAll("[\\n\\t]+", "");
      goodSentence = cleanXML(goodSentence);
      assertTrue(lang + ": Empty correct example in rule " + rule.getId(), goodSentence.trim().length() > 0);
      boolean isMatched = false;
      // necessary for XML Pattern rules containing <or>
      for (Rule auxRule : rules) {
        isMatched = isMatched || match(auxRule, goodSentence, languageTool);
      }
      assertFalse(lang + ": Did not expect error in:\n" +
              "  " + goodSentence + "\n" +
              "Matching Rule: " + rule, isMatched);
      // avoid matches with all the *other* rules:
      /*
      final List<RuleMatch> matches = allRulesLanguageTool.check(goodSentence);
      for (RuleMatch match : matches) {
        System.err.println("WARN: " + lang.getShortName() + ": '" + goodSentence + "' did not match "
                + rule.getId() + " but matched " + match.getRule().getId());
      }
      */
    }
  }

  protected String cleanXML(final String str) {
    return str.replaceAll("<([^<].*?)>", "");
  }

  private boolean match(final Rule rule, final String sentence, final JLanguageTool languageTool) throws IOException {
    final AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(sentence);
    final RuleMatch[] matches = rule.match(analyzedSentence);
    return matches.length > 0;
  }

  private List<RuleMatch> getMatches(final Rule rule, final String sentence,
      final JLanguageTool languageTool) throws IOException {
    final AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(sentence);
    final RuleMatch[] matches = rule.match(analyzedSentence);
    if (CHECK_WITH_SENTENCE_SPLITTING) {
      // "real check" with sentence splitting:
      for (Rule r : languageTool.getAllActiveRules()) {
        languageTool.disableRule(r.getId());
      }
      languageTool.enableRule(rule.getId());
      final List<RuleMatch> realMatches = languageTool.check(sentence);
      final List<String> realMatchRuleIds = new ArrayList<>();
      for (RuleMatch realMatch : realMatches) {
        realMatchRuleIds.add(realMatch.getRule().getId());
      }
      for (RuleMatch match : matches) {
        final String ruleId = match.getRule().getId();
        if (!match.getRule().isDefaultOff() && !realMatchRuleIds.contains(ruleId)) {
          System.err.println("WARNING: " + languageTool.getLanguage().getName()
                  + ": missing rule match " + ruleId + " when splitting sentences for test sentence '" + sentence + "'");
        }
      }
    }
    return Arrays.asList(matches);
  }

  protected PatternRule makePatternRule(final String s, final boolean caseSensitive, final boolean regex) {
    final List<Element> elements = new ArrayList<>();
    final String[] parts = s.split(" ");
    boolean pos = false;
    Element se;
    for (final String element : parts) {
      if (element.equals(JLanguageTool.SENTENCE_START_TAGNAME)) {
        pos = true;
      }
      if (!pos) {
        se = new Element(element, caseSensitive, regex, false);
      } else {
        se = new Element("", caseSensitive, regex, false);
      }
      if (pos) {
        se.setPosElement(element, false, false);
      }
      elements.add(se);
      pos = false;
    }
    final PatternRule rule = new PatternRule("ID1", TestTools.getDemoLanguage(), elements,
        "test rule", "user visible message", "short comment");
    return rule;
  }

  /**
   * Test XML patterns, as a help for people developing rules that are not
   * programmers.
   */
  public static void main(final String[] args) throws IOException {
    final PatternRuleTest test = new PatternRuleTest();
    System.out.println("Running XML pattern tests...");
    if (args.length == 0) {
      test.runGrammarRulesFromXmlTestIgnoringLanguages(null);
    } else {
      final Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      test.runGrammarRulesFromXmlTestIgnoringLanguages(ignoredLanguages);
    }
    System.out.println("Tests finished!");
  }

}
