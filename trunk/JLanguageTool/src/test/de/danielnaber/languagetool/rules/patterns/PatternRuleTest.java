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
package de.danielnaber.languagetool.rules.patterns;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.IncorrectExample;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Daniel Naber
 */
public class PatternRuleTest extends TestCase {

  private static JLanguageTool langTool;

  private static final Pattern PROBABLE_REGEX = Pattern.compile(".*([\\[\\]\\*\\+\\|\\^]|\\(.+\\)|\\[.+\\]|\\{.+\\}).*");

  
  @Override
  public void setUp() throws IOException {
    if (langTool == null) {
      langTool = new JLanguageTool(Language.ENGLISH);
    }
  }

  public void testGrammarRulesFromXML() throws IOException {
    testGrammarRulesFromXML(null, false);
  }

  private void testGrammarRulesFromXML(final Set<Language> ignoredLanguages,
      final boolean verbose) throws IOException {
    for (final Language lang : Language.LANGUAGES) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        if (verbose) {
          System.out.println("Ignoring tests for " + lang.getName());
        }
        continue;
      }
      if (verbose) {
        System.out.println("Running tests for " + lang.getName() + "...");
      }
      final PatternRuleLoader ruleLoader = new PatternRuleLoader();
      final JLanguageTool languageTool = new JLanguageTool(lang);
      final String name = "/rules/" + lang.getShortName() + "/grammar.xml";
      final List<PatternRule> rules = ruleLoader.getRules(this.getClass()
          .getResourceAsStream(name), name);
      warnIfRegexpSyntax(rules, lang);
      testGrammarRulesFromXML(rules, languageTool, lang);
    }
  }

  // TODO: probably this would be more useful for exceptions
  // instead of adding next methods to PatternRule
  // we can probably validate using XSD and specify regexes straight there
  private void warnIfRegexpSyntax(final List<PatternRule> rules,
      final Language lang) {
    for (final PatternRule rule : rules) {
      for (final Element element : rule.getElements()) {
        if (!element.isRegularExpression()
            && (PROBABLE_REGEX.matcher(element.getString())
                .matches())) {
          System.err.println("The " + lang.toString() + " rule: "
              + rule.getId() + " contains element " + "\"" + element
              + "\" that is not marked as regular expression"
              + " but probably is one.");
        }
        if (element.isRegularExpression()
            && (element.getString() == null || (!PROBABLE_REGEX.matcher(element.getString())
                .matches()))) {
          System.err.println("The " + lang.toString() + " rule: "
              + rule.getId() + " contains element " + "\"" + element
              + "\" that is marked as regular expression"
              + " but probably is not one.");
        }
      }
    }
  }

  private void testGrammarRulesFromXML(final List<PatternRule> rules,
      final JLanguageTool languageTool, final Language lang) throws IOException {
    int noSuggestionCount = 0;
    final HashMap<String, PatternRule> complexRules = new HashMap<String, PatternRule>();
    for (final PatternRule rule : rules) {
      final List<String> goodSentences = rule.getCorrectExamples();
      for (String goodSentence : goodSentences) {
        // enable indentation use
        goodSentence = goodSentence.replaceAll("[\\n\\t]+", "");
        goodSentence = cleanXML(goodSentence);
        assertTrue(goodSentence.trim().length() > 0);
        assertFalse(lang + ": Did not expect error in: " + goodSentence
            + " (Rule: " + rule + ")", match(rule, goodSentence, languageTool));
      }
      final List<IncorrectExample> badSentences = rule.getIncorrectExamples();
      for (IncorrectExample origBadExample : badSentences) {
        // enable indentation use
        String origBadSentence = origBadExample.getExample().replaceAll(
            "[\\n\\t]+", "");
        final List<String> suggestedCorrection = origBadExample
            .getCorrections();
        final int expectedMatchStart = origBadSentence.indexOf("<marker>");
        final int expectedMatchEnd = origBadSentence.indexOf("</marker>")
            - "<marker>".length();
        if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
          fail(lang
              + ": No error position markup ('<marker>...</marker>') in bad example in rule "
              + rule);
        }
        final String badSentence = cleanXML(origBadSentence);
        assertTrue(badSentence.trim().length() > 0);
        RuleMatch[] matches = getMatches(rule, badSentence, languageTool);
        if (!rule.isWithComplexPhrase()) {
          assertTrue(lang + ": Did expect one error in: \"" + badSentence
              + "\" (Rule: " + rule + "), got " + matches.length
              + ". Additional info:" + rule.getMessage(), matches.length == 1);
          assertEquals(lang
              + ": Incorrect match position markup (start) for rule " + rule,
              expectedMatchStart, matches[0].getFromPos());
          assertEquals(lang
              + ": Incorrect match position markup (end) for rule " + rule,
              expectedMatchEnd, matches[0].getToPos());
          // make sure suggestion is what we expect it to be
          if (suggestedCorrection != null && suggestedCorrection.size() > 0) {
            assertTrue(lang + ": Incorrect suggestions: "
                + suggestedCorrection.toString() + " != "
                + matches[0].getSuggestedReplacements() + " for rule " + rule,
                suggestedCorrection.equals(matches[0]
                    .getSuggestedReplacements()));
          }
          // make sure the suggested correction doesn't produce an error:
          if (matches[0].getSuggestedReplacements().size() > 0) {
            final int fromPos = matches[0].getFromPos();
            final int toPos = matches[0].getToPos();
            for (final String repl : matches[0].getSuggestedReplacements()) {
              final String fixedSentence = badSentence.substring(0, fromPos)
                  + repl + badSentence.substring(toPos);
              matches = getMatches(rule, fixedSentence, languageTool);
              assertEquals("Corrected sentence for rule " + rule
                  + " triggered error: " + fixedSentence, 0, matches.length);
            }
          } else {
            noSuggestionCount++;
          }
        } else { // for multiple rules created with complex phrases

          matches = getMatches(rule, badSentence, languageTool);
          if (matches.length == 0
              && !complexRules.containsKey(rule.getId() + badSentence)) {
            complexRules.put(rule.getId() + badSentence, rule);
          }

          if (matches.length != 0) {
            complexRules.put(rule.getId() + badSentence, null);
            assertTrue(lang + ": Did expect one error in: \"" + badSentence
                + "\" (Rule: " + rule + "), got " + matches.length,
                matches.length == 1);
            assertEquals(lang
                + ": Incorrect match position markup (start) for rule " + rule,
                expectedMatchStart, matches[0].getFromPos());
            assertEquals(lang
                + ": Incorrect match position markup (end) for rule " + rule,
                expectedMatchEnd, matches[0].getToPos());
            // make sure suggestion is what we expect it to be
            if (suggestedCorrection != null && suggestedCorrection.size() > 0) {
              assertTrue(
                  lang + ": Incorrect suggestions: "
                      + suggestedCorrection.toString() + " != "
                      + matches[0].getSuggestedReplacements() + " for rule "
                      + rule, suggestedCorrection.equals(matches[0]
                      .getSuggestedReplacements()));
            }
            // make sure the suggested correction doesn't produce an error:
            if (matches[0].getSuggestedReplacements().size() > 0) {
              final int fromPos = matches[0].getFromPos();
              final int toPos = matches[0].getToPos();
              for (final String repl : matches[0].getSuggestedReplacements()) {
                final String fixedSentence = badSentence.substring(0, fromPos)
                    + repl + badSentence.substring(toPos);
                matches = getMatches(rule, fixedSentence, languageTool);
                assertEquals("Corrected sentence for rule " + rule
                    + " triggered error: " + fixedSentence, 0, matches.length);
              }
            } else {
              noSuggestionCount++;
            }
          }
        }

      }
    }
    if (!complexRules.isEmpty()) {
      final Set<String> set = complexRules.keySet();
      final List<PatternRule> badRules = new ArrayList<PatternRule>();
      final Iterator<String> iter = set.iterator();
      while (iter.hasNext()) {
        final PatternRule badRule = complexRules.get(iter.next());
        if (badRule != null) {
          badRule.notComplexPhrase();
          badRule
              .setMessage("The rule contains a phrase that never matched any incorrect example.");
          badRules.add(badRule);
        }
      }
      if (!badRules.isEmpty()) {
        testGrammarRulesFromXML(badRules, languageTool, lang);
      }
    }
  }

  protected String cleanXML(final String str) {
    return str.replaceAll("<([^<].*?)>", "");
  }

  private boolean match(final Rule rule, final String sentence,
      final JLanguageTool languageTool) throws IOException {
    final AnalyzedSentence text = languageTool.getAnalyzedSentence(sentence);
    final RuleMatch[] matches = rule.match(text);
    /*
     * for (int i = 0; i < matches.length; i++) {
     * System.err.println(matches[i]); }
     */
    return matches.length > 0;
  }

  private RuleMatch[] getMatches(final Rule rule, final String sentence,
      final JLanguageTool languageTool) throws IOException {
    final AnalyzedSentence text = languageTool.getAnalyzedSentence(sentence);
    final RuleMatch[] matches = rule.match(text);
    /*
     * for (int i = 0; i < matches.length; i++) {
     * System.err.println(matches[i]); }
     */
    return matches;
  }

  public void testUppercasingSuggestion() throws IOException {
    final JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    langTool.activateDefaultPatternRules();
    final List<RuleMatch> matches = langTool
        .check("Were are in the process of ...");
    assertEquals(1, matches.size());
    final RuleMatch match = matches.get(0);
    final List<String> sugg = match.getSuggestedReplacements();
    assertEquals(2, sugg.size());
    assertEquals("Where", sugg.get(0));
    assertEquals("We", sugg.get(1));
  }

  public void testRule() throws IOException {
    PatternRule pr;
    RuleMatch[] matches;

    pr = makePatternRule("one");
    matches = pr
        .match(langTool.getAnalyzedSentence("A non-matching sentence."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool
        .getAnalyzedSentence("A matching sentence with one match."));
    assertEquals(1, matches.length);
    assertEquals(25, matches[0].getFromPos());
    assertEquals(28, matches[0].getToPos());
    // these two are not set if the rule is called standalone (not via
    // JLanguageTool):
    assertEquals(-1, matches[0].getColumn());
    assertEquals(-1, matches[0].getLine());
    assertEquals("ID1", matches[0].getRule().getId());
    assertTrue(matches[0].getMessage().equals("user visible message"));
    assertTrue(matches[0].getShortMessage().equals("short comment"));
    matches = pr.match(langTool
        .getAnalyzedSentence("one one and one: three matches"));
    assertEquals(3, matches.length);

    pr = makePatternRule("one two");
    matches = pr.match(langTool.getAnalyzedSentence("this is one not two"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("this is two one"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("this is one two three"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("one two"));
    assertEquals(1, matches.length);

    pr = makePatternRule("one|foo|xxxx two", false, true);
    matches = pr.match(langTool.getAnalyzedSentence("one foo three"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("one two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("foo two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("one foo two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("y x z one two blah foo"));
    assertEquals(1, matches.length);

    pr = makePatternRule("one|foo|xxxx two|yyy", false, true);
    matches = pr.match(langTool.getAnalyzedSentence("one, yyy"));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("one yyy"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("xxxx two"));
    assertEquals(1, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("xxxx yyy"));
    assertEquals(1, matches.length);
  }

  private PatternRule makePatternRule(final String s) {
    return makePatternRule(s, false, false);
  }

  private PatternRule makePatternRule(final String s,
      final boolean caseSensitive, final boolean regex) {
    final List<Element> elems = new ArrayList<Element>();
    final String[] parts = s.split(" ");
    boolean pos = false;
    Element se = null;
    for (final String element : parts) {
      if (element.equals("SENT_START")) {
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
      elems.add(se);
      pos = false;
    }
    final PatternRule rule = new PatternRule("ID1", Language.ENGLISH, elems,
        "test rule", "user visible message", "short comment");
    return rule;
  }

  public void testSentenceStart() throws IOException {
    PatternRule pr;
    RuleMatch[] matches;

    pr = makePatternRule("SENT_START One");
    matches = pr.match(langTool.getAnalyzedSentence("Not One word."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("One word."));
    assertEquals(1, matches.length);
  }

  private static String callFormatMultipleSynthesis(final String[] suggs,
      final String left, final String right) throws IllegalArgumentException,
      SecurityException, InvocationTargetException, IllegalAccessException,
      NoSuchMethodException {
    Class[] argClasses = { String[].class, String.class, String.class };
    Object[] argObjects = { suggs, left, right };
    return TestTools.callStringStaticMethod(PatternRule.class,
        "formatMultipleSynthesis", argClasses, argObjects);
  }

  /* test private methods as well */
  public void testformatMultipleSynthesis() throws IllegalArgumentException,
      SecurityException, InvocationTargetException, IllegalAccessException,
      NoSuchMethodException {
    final String[] suggArray = { "blah blah", "foo bar" };

    assertEquals(
        "This is how you should write: <suggestion>blah blah</suggestion>, <suggestion>foo bar</suggestion>.",

        callFormatMultipleSynthesis(suggArray,
            "This is how you should write: <suggestion>", "</suggestion>."));

    final String[] suggArray2 = { "test", " " };

    assertEquals(
        "This is how you should write: <suggestion>test</suggestion>, <suggestion> </suggestion>.",

        callFormatMultipleSynthesis(suggArray2,
            "This is how you should write: <suggestion>", "</suggestion>."));
  }

  /**
   * Test XML patterns, as a help for people developing rules that are not
   * programmers.
   */
  public static void main(final String[] args) throws IOException {
    final PatternRuleTest prt = new PatternRuleTest();
    System.out.println("Running XML pattern tests...");
    prt.setUp();
    final Set<Language> ignoredLanguages = new HashSet<Language>();
    // ignoredLanguages.add(Language.CZECH); // has no XML rules yet
    prt.testGrammarRulesFromXML(ignoredLanguages, true);
    System.out.println("Tests successful.");
  }

}
