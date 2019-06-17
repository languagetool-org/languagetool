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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.FakeLanguage;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.XMLValidator;
import org.languagetool.rules.Category;
import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.ErrorTriggeringExample;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

/**
 * @author Daniel Naber
 */
public class PatternRuleTest extends AbstractPatternRuleTest {

  // A test sentence should only be a single sentence - if that's not the case it can
  // happen that rules are checked as being correct that in reality will never match.
  // This check prints a warning for affected rules, but it's disabled by default because
  // it makes the tests very slow:
  private static final boolean CHECK_WITH_SENTENCE_SPLITTING = false;
  private static final Pattern PATTERN_MARKER_START = Pattern.compile(".*<pattern[^>]*>\\s*<marker>.*", Pattern.DOTALL);
  private static final Pattern PATTERN_MARKER_END = Pattern.compile(".*</marker>\\s*</pattern>.*", Pattern.DOTALL);
  private static final Comparator<Match> MATCH_COMPARATOR = (m1, m2) -> Integer.compare( m1.getTokenRef(), m2.getTokenRef());

  public void testFake() {
    // there's no test here - the languages are supposed to extend this class and call runGrammarRulesFromXmlTest() 
  }

  @Test
  public void testSupportsLanguage() {
    FakeLanguage fakeLanguage1 = new FakeLanguage("yy");
    FakeLanguage fakeLanguage2 = new FakeLanguage("zz");
    PatternRule patternRule1 = new PatternRule("ID", fakeLanguage1, Collections.<PatternToken>emptyList(), "", "", "");
    assertTrue(patternRule1.supportsLanguage(fakeLanguage1)); 
    assertFalse(patternRule1.supportsLanguage(fakeLanguage2));
    FakeLanguage fakeLanguage1WithVariant1 = new FakeLanguage("zz", "VAR1");
    FakeLanguage fakeLanguage1WithVariant2 = new FakeLanguage("zz", "VAR2");
    PatternRule patternRuleVariant1 = new PatternRule("ID", fakeLanguage1WithVariant1, Collections.<PatternToken>emptyList(), "", "", "");
    assertTrue(patternRuleVariant1.supportsLanguage(fakeLanguage1WithVariant1));    
    assertFalse(patternRuleVariant1.supportsLanguage(fakeLanguage1));
    assertFalse(patternRuleVariant1.supportsLanguage(fakeLanguage2));
    assertFalse(patternRuleVariant1.supportsLanguage(fakeLanguage1WithVariant2));
  }

  /**
   * To be called from language modules. Languages.get() knows only the languages that's in the classpath.
   * @param ignoredLanguage ignore this language - useful to speed up tests from languages that 
   *                        have another language as a dependency
   */
  protected void runGrammarRulesFromXmlTest(Language ignoredLanguage) throws IOException {
    int count = 0;
    for (Language lang : Languages.get()) {
      if (ignoredLanguage.getShortCodeWithCountryAndVariant().equals(lang.getShortCodeWithCountryAndVariant())) {
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
   * To be called from language modules. Languages.get() only knows the languages that are in the classpath,
   * and that's only the demo language for languagetool-core.
   */
  protected void runGrammarRulesFromXmlTest() throws IOException {
    for (Language lang : Languages.get()) {
      runGrammarRuleForLanguage(lang);
    }
    if (Languages.get().isEmpty()) {
      System.err.println("Warning: no languages found in classpath - cannot run any grammar rule tests");
    }
  }

  protected void runGrammarRuleForLanguage(Language lang) throws IOException {
    if (skipCountryVariant(lang)) {
      System.out.println("Skipping " + lang + " because there are no specific rules for that variant");
      return;
    }
    runTestForLanguage(lang);
  }

  private void runGrammarRulesFromXmlTestIgnoringLanguages(Set<Language> ignoredLanguages) throws IOException {
    System.out.println("Known languages: " + Languages.getWithDemoLanguage());
    for (Language lang : Languages.getWithDemoLanguage()) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      runTestForLanguage(lang);
    }
  }

  public void runTestForLanguage(Language lang) throws IOException {
    validatePatternFile(lang);
    System.out.print("Running pattern rule tests for " + lang.getName() + "... ");
    MultiThreadedJLanguageTool languageTool = new MultiThreadedJLanguageTool(lang);
    if (CHECK_WITH_SENTENCE_SPLITTING) {
      disableSpellingRules(languageTool);
    }
    MultiThreadedJLanguageTool allRulesLanguageTool = new MultiThreadedJLanguageTool(lang);
    validateRuleIds(lang, allRulesLanguageTool);
    List<AbstractPatternRule> rules = getAllPatternRules(lang, languageTool);
    for (AbstractPatternRule rule : rules) {
      // Test the rule pattern.
      /* check for useless 'marker' elements commented out - too slow to always run:
      PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
      String xml = creator.toXML(rule.getPatternRuleId(), lang);
      if (PATTERN_MARKER_START.matcher(xml).matches() && PATTERN_MARKER_END.matcher(xml).matches()) {
        System.err.println("WARNING " + lang + ": useless <marker>: " + rule.getFullId());
      }*/

      // too aggressive for now:
      //PatternTestTools.failIfWhitespaceInToken(rule.getPatternTokens(), rule, lang);
              
      PatternTestTools.warnIfRegexpSyntaxNotKosher(rule.getPatternTokens(),
              rule.getId(), rule.getSubId(), lang);

      // Test the rule antipatterns.
      List<DisambiguationPatternRule> antiPatterns = rule.getAntiPatterns();
      for (DisambiguationPatternRule antiPattern : antiPatterns) {
        PatternTestTools.warnIfRegexpSyntaxNotKosher(antiPattern.getPatternTokens(),
            antiPattern.getId(), antiPattern.getSubId(), lang);
      }
      if (rule.getCorrectExamples().isEmpty()) {
        boolean correctionExists = false;
        for (IncorrectExample incorrectExample : rule.getIncorrectExamples()) {
          if (incorrectExample.getCorrections().size() > 0) {
            correctionExists = true;
            break;
          }
        }
        if (!correctionExists) {
          fail("Rule " + rule.getFullId() + " in language " + lang
                  + " needs at least one <example> with a 'correction' attribute"
                  + " or one <example> of type='correct'.");
        }
      }
    }
    testGrammarRulesFromXML(rules, languageTool, allRulesLanguageTool, lang);
    System.out.println(rules.size() + " rules tested.");
    allRulesLanguageTool.shutdown();
    languageTool.shutdown();
  }

  private void validatePatternFile(Language lang) throws IOException {
    XMLValidator validator = new XMLValidator();
    List<String> grammarFiles = getGrammarFileNames(lang);
    for (String grammarFile : grammarFiles) {
      System.out.println("Running XML validation for " + grammarFile + "...");
      String rulesDir = JLanguageTool.getDataBroker().getRulesDir();
      String ruleFilePath = rulesDir + "/" + grammarFile;
      try (InputStream xmlStream = this.getClass().getResourceAsStream(ruleFilePath)) {
        if (xmlStream == null) {
          System.out.println("No rule file found at " + ruleFilePath + " in classpath");
          continue;
        }
        // if there are multiple xml grammar files we'll prepend all unification elements 
        // from the first file to the rest of them 
        if (grammarFiles.size() > 1 && !grammarFiles.get(0).equals(grammarFile)) {
          validator.validateWithXmlSchema(rulesDir + "/" + grammarFiles.get(0), ruleFilePath, rulesDir + "/rules.xsd");
        } else {
          validator.validateWithXmlSchema(ruleFilePath, rulesDir + "/rules.xsd");
        }
      }
    }
  }

  private void validateRuleIds(Language lang, JLanguageTool languageTool) {
    List<Rule> allRules = languageTool.getAllRules();
    Set<String> ids = new HashSet<>();
    Set<Class> ruleClasses = new HashSet<>();
    Set<String> categoryIds = new HashSet<>();
    for (Rule rule : allRules) {
      assertIdUniqueness(ids, ruleClasses, lang, rule);
      if (rule.getId().equalsIgnoreCase("ID")) {
        System.err.println("WARNING: " + lang.getShortCodeWithCountryAndVariant() + " has a rule with id 'ID', this should probably be changed");
      }
      Category category = rule.getCategory();
      if (category != null && category.getId() != null) {
        String catId = category.getId().toString();
        if (!catId.matches("[A-Z0-9_-]+") && !categoryIds.contains(catId)) {
          System.err.println("WARNING: category id '" + catId + "' doesn't match expected regexp [A-Z0-9_-]+");
          categoryIds.add(catId);
        }
      }
    }
  }

  private void assertIdUniqueness(Set<String> ids, Set<Class> ruleClasses, Language language, Rule rule) {
    String ruleId = rule.getId();
    Class relevantClass = rule instanceof AbstractPatternRule ? AbstractPatternRule.class : rule.getClass();
    if (ids.contains(ruleId) && !ruleClasses.contains(relevantClass)) {
      throw new RuntimeException("Rule id occurs more than once: '" + ruleId + "', language: " + language);
    }
    ids.add(ruleId);
    ruleClasses.add(relevantClass);
  }

  private void disableSpellingRules(JLanguageTool languageTool) {
    List<Rule> allRules = languageTool.getAllRules();
    for (Rule rule : allRules) {
      if (rule instanceof SpellingCheckRule) {
        languageTool.disableRule(rule.getId());
      }
    }
  }

  public void testGrammarRulesFromXML(List<AbstractPatternRule> rules,
                                      JLanguageTool languageTool,
                                      JLanguageTool allRulesLanguageTool, Language lang) throws IOException {
    Map<String, AbstractPatternRule> complexRules = new HashMap<>();
    for (AbstractPatternRule rule : rules) {
      testCorrectSentences(languageTool, allRulesLanguageTool, lang, rule);
      testBadSentences(languageTool, allRulesLanguageTool, lang, complexRules, rule);
      testErrorTriggeringSentences(languageTool, lang, rule);
    }
    if (!complexRules.isEmpty()) {
      Set<String> set = complexRules.keySet();
      List<AbstractPatternRule> badRules = new ArrayList<>();
      for (String aSet : set) {
        AbstractPatternRule badRule = complexRules.get(aSet);
        if (badRule != null && badRule instanceof PatternRule) {
          ((PatternRule)badRule).notComplexPhrase();
          badRule.setMessage("The rule contains a phrase that never matched any incorrect example.\n" + ((PatternRule) badRule).toPatternString());
          badRules.add(badRule);
        }
      }
      if (!badRules.isEmpty()) {
        testGrammarRulesFromXML(badRules, languageTool, allRulesLanguageTool, lang);
      }
    }
  }

  private void testBadSentences(JLanguageTool languageTool, JLanguageTool allRulesLanguageTool, Language lang,
                                Map<String, AbstractPatternRule> complexRules, AbstractPatternRule rule) throws IOException {
    List<IncorrectExample> badSentences = rule.getIncorrectExamples();
    if (badSentences.isEmpty()) {
      fail("No incorrect examples found for rule " + rule.getFullId());
    }
    // necessary for XML Pattern rules containing <or>
    List<AbstractPatternRule> rules = allRulesLanguageTool.getPatternRulesByIdAndSubId(rule.getId(), rule.getSubId());
    for (IncorrectExample origBadExample : badSentences) {
      // enable indentation use
      String origBadSentence = origBadExample.getExample().replaceAll("[\\n\\t]+", "");
      List<String> expectedCorrections = origBadExample.getCorrections();
      int expectedMatchStart = origBadSentence.indexOf("<marker>");
      int expectedMatchEnd = origBadSentence.indexOf("</marker>") - "<marker>".length();
      if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
        fail(lang + ": No error position markup ('<marker>...</marker>') in bad example in rule " + rule.getFullId());
      }
      String badSentence = cleanXML(origBadSentence);
      assertTrue(badSentence.trim().length() > 0);
      
      // necessary for XML Pattern rules containing <or>
      List<RuleMatch> matches = new ArrayList<>();
      for (Rule auxRule : rules) { 
        matches.addAll(getMatches(auxRule, badSentence, languageTool));
      }
      
      if (rule instanceof RegexPatternRule || rule instanceof PatternRule && !((PatternRule)rule).isWithComplexPhrase()) {
        if (matches.size() != 1) {
          AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(badSentence);
          StringBuilder sb = new StringBuilder("Analyzed token readings:");
          for (AnalyzedTokenReadings atr : analyzedSentence.getTokens()) {
            sb.append(" ").append(atr);
          }
          String info = "";
          if (rule instanceof RegexPatternRule) {
            info = "\nRegexp: " + ((RegexPatternRule) rule).getPattern().toString();
          }
          fail(lang + " rule " + rule.getFullId() + ":\n\"" + badSentence + "\"\n"
                  + "Errors expected: 1\n"
                  + "Errors found   : " + matches.size() + "\n"
                  + "Message: " + rule.getMessage() + "\n" + sb + "\nMatches: " + matches + info);
        }

        int maxReference = 0;
        if (rule.getSuggestionMatches() != null) {
          Optional<Match> opt = rule.getSuggestionMatches().stream().max(MATCH_COMPARATOR);
          maxReference = opt.isPresent() ? opt.get().getTokenRef() : 0;
        }
        maxReference = Math.max( rule.getMessage() != null ? findLargestReference(rule.getMessage()) : 0, maxReference);
        if (rule.getPatternTokens() != null && maxReference > rule.getPatternTokens().size()) {
          System.err.println("Warning: Rule "+rule.getFullId()+" refers to token \\"+(maxReference)+" but has only "+rule.getPatternTokens().size()+" tokens.");
        }

        assertEquals(lang
                + ": Incorrect match position markup (start) for rule " + rule.getFullId() + ", sentence: " + badSentence,
                expectedMatchStart, matches.get(0).getFromPos());
        assertEquals(lang
                + ": Incorrect match position markup (end) for rule " + rule.getFullId() + ", sentence: " + badSentence,
                expectedMatchEnd, matches.get(0).getToPos());
        // make sure suggestion is what we expect it to be
        assertSuggestions(badSentence, lang, expectedCorrections, rule, matches);
        // make sure the suggested correction doesn't produce an error:
        if (matches.get(0).getSuggestedReplacements().size() > 0) {
          int fromPos = matches.get(0).getFromPos();
          int toPos = matches.get(0).getToPos();
          for (String replacement : matches.get(0).getSuggestedReplacements()) {
            String fixedSentence = badSentence.substring(0, fromPos)
                + replacement + badSentence.substring(toPos);
            matches = getMatches(rule, fixedSentence, languageTool);
            if (matches.size() > 0) {
                fail("Incorrect input:\n"
                        + "  " + badSentence
                          + "\nCorrected sentence:\n"
                        + "  " + fixedSentence
                        + "\nBy Rule:\n"
                        + "  " + rule.getFullId()
                        + "\nThe correction triggered an error itself:\n"
                        + "  " + matches.get(0) + "\n");
            }
          }
        }
      } else { // for multiple rules created with complex phrases

        matches = getMatches(rule, badSentence, languageTool);
        if (matches.isEmpty()
            && !complexRules.containsKey(rule.getId() + badSentence)) {
          complexRules.put(rule.getId() + badSentence, rule);
        }

        if (matches.size() != 0) {
          complexRules.put(rule.getId() + badSentence, null);
          assertTrue(lang + ": Did expect one error in: \"" + badSentence
              + "\" (Rule: " + rule.getFullId() + "), got " + matches.size(),
              matches.size() == 1);
          assertEquals(lang + ": Incorrect match position markup (start) for rule " + rule.getFullId(),
                  expectedMatchStart, matches.get(0).getFromPos());
          assertEquals(lang + ": Incorrect match position markup (end) for rule " + rule.getFullId(),
                  expectedMatchEnd, matches.get(0).getToPos());
          assertSuggestions(badSentence, lang, expectedCorrections, rule, matches);
          assertSuggestionsDoNotCreateErrors(badSentence, languageTool, rule, matches);
        }
      }

      // check for overlapping rules
      /*matches = getMatches(rule, badSentence, languageTool);
      List<RuleMatch> matchesAllRules = allRulesLanguageTool.check(badSentence);
      for (RuleMatch match : matchesAllRules) {
        if (!match.getRule().getId().equals(rule.getId()) && !matches.isEmpty()
            && rangeIsOverlapping(matches.get(0).getFromPos(), matches.get(0).getToPos(), match.getFromPos(), match.getToPos()))
          System.err.println("WARN: " + lang.getShortCode() + ": '" + badSentence + "' in "
                  + rule.getId() + " also matched " + match.getRule().getId());
      }*/
    }
  }

  private int findLargestReference (String message) {
    Pattern pattern = Pattern.compile("\\\\[0-9]+");
    Matcher matcher = pattern.matcher(message);
    int max = 0;
    while (matcher.find()) {
      max = Math.max(max, Integer.parseInt(matcher.group().replace("\\", "")));
    }
    return max;
  }

  private void testErrorTriggeringSentences(JLanguageTool languageTool, Language lang,
                                            AbstractPatternRule rule) throws IOException {
    for (ErrorTriggeringExample example : rule.getErrorTriggeringExamples()) {
      String sentence = cleanXML(example.getExample());
      List<RuleMatch> matches = getMatches(rule, sentence, languageTool);
      if (matches.isEmpty()) {
        fail(lang + ": " + rule.getFullId() + ": Example sentence marked with 'triggers_error' didn't actually trigger an error: '" + sentence + "'");
      }
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

  private void assertSuggestions(String sentence, Language lang, List<String> expectedCorrections, AbstractPatternRule rule, List<RuleMatch> matches) {
    if (!expectedCorrections.isEmpty()) {
      boolean expectedNonEmptyCorrection = expectedCorrections.get(0).length() > 0;
      if (expectedNonEmptyCorrection) {
        assertTrue("You specified a correction but your message has no suggestions in rule " + rule.getFullId(),
                rule.getMessage().contains("<suggestion>") || rule.getSuggestionsOutMsg().contains("<suggestion>"));
      }
      List<String> realSuggestions = matches.get(0).getSuggestedReplacements();
      if (realSuggestions.isEmpty()) {
        boolean expectedEmptyCorrection = expectedCorrections.size() == 1 && expectedCorrections.get(0).length() == 0;
        assertTrue(lang + ": Incorrect suggestions: "
                        + expectedCorrections + " != "
                        + " <no suggestion> for rule " + rule.getFullId() + " on input: " + sentence,
                expectedEmptyCorrection);
      } else {
        assertEquals(lang + ": Incorrect suggestions: "
                        + expectedCorrections + " != "
                        + realSuggestions + " for rule " + rule.getFullId() + " on input: " + sentence,
                expectedCorrections, realSuggestions);
      }
    }
  }

  private void assertSuggestionsDoNotCreateErrors(String badSentence, JLanguageTool languageTool, AbstractPatternRule rule, List<RuleMatch> matches) throws IOException {
    if (matches.get(0).getSuggestedReplacements().size() > 0) {
      int fromPos = matches.get(0).getFromPos();
      int toPos = matches.get(0).getToPos();
      for (String replacement : matches.get(0).getSuggestedReplacements()) {
        String fixedSentence = badSentence.substring(0, fromPos)
            + replacement + badSentence.substring(toPos);
        List<RuleMatch> tempMatches = getMatches(rule, fixedSentence, languageTool);
        assertEquals("Corrected sentence for rule " + rule.getFullId()
            + " triggered error: " + fixedSentence, 0, tempMatches.size());
      }
    }
  }

  private void testCorrectSentences(JLanguageTool languageTool, JLanguageTool allRulesLanguageTool,
                                    Language lang, AbstractPatternRule rule) throws IOException {
    List<CorrectExample> goodSentences = rule.getCorrectExamples();
    // necessary for XML Pattern rules containing <or>
    List<AbstractPatternRule> rules = allRulesLanguageTool.getPatternRulesByIdAndSubId(rule.getId(), rule.getSubId());
    for (CorrectExample goodSentenceObj : goodSentences) {
      // enable indentation use
      String goodSentence = goodSentenceObj.getExample().replaceAll("[\\n\\t]+", "");
      goodSentence = cleanXML(goodSentence);
      assertTrue(lang + ": Empty correct example in rule " + rule.getFullId(), goodSentence.trim().length() > 0);
      boolean isMatched = false;
      // necessary for XML Pattern rules containing <or>
      for (Rule auxRule : rules) {
        isMatched = isMatched || match(auxRule, goodSentence, languageTool);
      }
      if (isMatched) {
        AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(goodSentence);
        StringBuilder sb = new StringBuilder("Analyzed token readings:");
        for (AnalyzedTokenReadings atr : analyzedSentence.getTokens()) {
          sb.append(" ").append(atr);
        }
        fail(lang + ": Did not expect error in:\n" +
                "  " + goodSentence + "\n" +
                "  " + sb + "\n" +
                "Matching Rule: " + rule.getFullId());
      }
      // avoid matches with all the *other* rules:
      /*
      List<RuleMatch> matches = allRulesLanguageTool.check(goodSentence);
      for (RuleMatch match : matches) {
        System.err.println("WARN: " + lang.getShortCode() + ": '" + goodSentence + "' did not match "
                + rule.getId() + " but matched " + match.getRule().getId());
      }
      */
    }
  }

  protected String cleanXML(String str) {
    return str.replaceAll("<([^<].*?)>", "");
  }

  private boolean match(Rule rule, String sentence, JLanguageTool languageTool) throws IOException {
    AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(sentence);
    RuleMatch[] matches = rule.match(analyzedSentence);
    return matches.length > 0;
  }

  private List<RuleMatch> getMatches(Rule rule, String sentence,
      JLanguageTool languageTool) throws IOException {
    AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(sentence);
    RuleMatch[] matches = rule.match(analyzedSentence);
    if (CHECK_WITH_SENTENCE_SPLITTING) {
      // "real check" with sentence splitting:
      for (Rule r : languageTool.getAllActiveRules()) {
        languageTool.disableRule(r.getId());
      }
      languageTool.enableRule(rule.getId());
      List<RuleMatch> realMatches = languageTool.check(sentence);
      List<String> realMatchRuleIds = new ArrayList<>();
      for (RuleMatch realMatch : realMatches) {
        realMatchRuleIds.add(realMatch.getRule().getId());
      }
      for (RuleMatch match : matches) {
        String ruleId = match.getRule().getId();
        if (!match.getRule().isDefaultOff() && !realMatchRuleIds.contains(ruleId)) {
          System.err.println("WARNING: " + languageTool.getLanguage().getName()
                  + ": missing rule match " + ruleId + " when splitting sentences for test sentence '" + sentence + "'");
        }
      }
    }
    return Arrays.asList(matches);
  }

  protected PatternRule makePatternRule(String s, boolean caseSensitive, boolean regex) {
    List<PatternToken> patternTokens = new ArrayList<>();
    String[] parts = s.split(" ");
    boolean pos = false;
    PatternToken pToken;
    for (String element : parts) {
      if (element.equals(JLanguageTool.SENTENCE_START_TAGNAME)) {
        pos = true;
      }
      if (!pos) {
        pToken = new PatternToken(element, caseSensitive, regex, false);
      } else {
        pToken = new PatternToken("", caseSensitive, regex, false);
      }
      if (pos) {
        pToken.setPosToken(new PatternToken.PosToken(element, false, false));
      }
      patternTokens.add(pToken);
      pos = false;
    }
    PatternRule rule = new PatternRule("ID1", TestTools.getDemoLanguage(), patternTokens,
        "test rule", "user visible message", "short comment");
    return rule;
  }

  /**
   * Test XML patterns, as a help for people developing rules that are not
   * programmers.
   */
  public static void main(String[] args) throws IOException {
    PatternRuleTest test = new PatternRuleTest();
    System.out.println("Running XML pattern tests...");
    if (args.length == 0) {
      test.runGrammarRulesFromXmlTestIgnoringLanguages(null);
    } else {
      Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      test.runGrammarRulesFromXmlTestIgnoringLanguages(ignoredLanguages);
    }
    System.out.println("Tests finished!");
  }

}
