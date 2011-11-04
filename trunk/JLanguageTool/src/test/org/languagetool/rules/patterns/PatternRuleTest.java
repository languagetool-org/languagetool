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
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

  // The  [^cfmnt123]\\.|\\.[^mvngl]  part is there to consider a string as a
  // regexp if and only if it is not enclosed on both sides by those characters.
  // This is to cope with Polish POS tags which contain dots without being
  // a regexp.
  private static final Pattern PROBABLE_PATTERN = Pattern.compile("(.+[+?^{}|\\[\\]].*)|(.*[+?^{}|\\[\\]].+)|(\\(.*\\))|(\\\\[^0-9].*)|[^cfmnt123]\\.|\\.[^mvngl]|(.+\\.$)");
  private static final Pattern CASE_PATTERN = Pattern.compile("\\[(.)(.)\\]");
  private static final Pattern EMPTY_DISJUNCTION = Pattern.compile("^[|]|[|][|]|[|]$");

  private static JLanguageTool langTool;

  @Override
  public void setUp() throws IOException {
    if (langTool == null) {
      langTool = new JLanguageTool(Language.ENGLISH);
    }
  }

  public void testGrammarRulesFromXML() throws IOException {
    testGrammarRulesFromXML(null);
  }

  public void testGrammarRulesFromXML2() throws IOException {
    new PatternRule("-1", Language.ENGLISH, Collections.<Element>emptyList(), "", "", "");
  }
    
  public void testGrammarRulesFromXML(Set<Language> ignoredLanguages) throws IOException {
    for (final Language lang : Language.LANGUAGES) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      System.out.println("Running tests for " + lang.getName() + "...");
      final PatternRuleLoader ruleLoader = new PatternRuleLoader();
      final JLanguageTool languageTool = new JLanguageTool(lang);
      final String name = "/" + lang.getShortName() + "/grammar.xml";
      final List<PatternRule> rules = ruleLoader.getRules(JLanguageTool.getDataBroker().
              getFromRulesDirAsStream(name), name);
      warnIfRegexpSyntaxNotKosher(rules, lang);
      testGrammarRulesFromXML(rules, languageTool, lang);
    }
  }

  private void testGrammarRulesFromXML(final List<PatternRule> rules,
                                       final JLanguageTool languageTool, final Language lang) throws IOException {
    final HashMap<String, PatternRule> complexRules = new HashMap<String, PatternRule>();
    for (final PatternRule rule : rules) {
      testCorrectSentences(languageTool, lang, rule);
      testBadSentences(languageTool, lang, complexRules, rule);
    }
    if (!complexRules.isEmpty()) {
      final Set<String> set = complexRules.keySet();
      final List<PatternRule> badRules = new ArrayList<PatternRule>();
      for (String aSet : set) {
        final PatternRule badRule = complexRules.get(aSet);
        if (badRule != null) {
          badRule.notComplexPhrase();
          badRule.setMessage("The rule contains a phrase that never matched any incorrect example.");
          badRules.add(badRule);
        }
      }
      if (!badRules.isEmpty()) {
        testGrammarRulesFromXML(badRules, languageTool, lang);
      }
    }
  }

  // TODO: probably this would be more useful for exceptions
  // instead of adding next methods to PatternRule
  // we can probably validate using XSD and specify regexes straight there
  private void warnIfRegexpSyntaxNotKosher(final List<PatternRule> rules,
      final Language lang) {
    for (final PatternRule rule : rules) {
      int i = 0;
      for (final Element element : rule.getElements()) {
        i++;

        // Check whether token value is consistent with regexp="..."
        warnIfElementNotKosher(
          element.getString(),
          element.isRegularExpression(),
          element.getCaseSensitive(),
          element.isInflected(),
          lang, rule.getId());

        // Check postag="..." is consistent with postag_regexp="..."
        warnIfElementNotKosher(
          element.getPOStag() == null ? "" : element.getPOStag(),
          element.isPOStagRegularExpression(),
          element.getCaseSensitive(),
          false,
          lang, rule.getId() + " (exception in POS tag) ");

        if (element.getExceptionList() != null) {
          for (final Element exception: element.getExceptionList()) {
            // Check whether exception value is consistent with regexp="..."
            // Don't check string "." since it is sometimes used as a regexp
            // and sometimes used as non regexp.
            if (!exception.getString().equals(".")) {
              warnIfElementNotKosher(
                exception.getString(),
                exception.isRegularExpression(),
                exception.getCaseSensitive(),
                exception.isInflected(),
                lang, rule.getId() + " (exception in token [" + i + "]) ");
            }
            // Check postag="..." of exception is consistent with postag_regexp="..."
            warnIfElementNotKosher(
              exception.getPOStag() == null ? "" : exception.getPOStag(),
              exception.isPOStagRegularExpression(),
              exception.getCaseSensitive(),
              false,
              lang, rule.getId() + " (exception in POS tag of token [" + i + "]) ");
          }
        }
      }
    }
  }

  private void warnIfElementNotKosher(
      final String stringValue,
      final boolean isRegularExpression,
      final boolean isCaseSensitive,
      final boolean isInflected,
      final Language lang, final String ruleId) {

    if (!isRegularExpression
        && PROBABLE_PATTERN.matcher(stringValue).find()) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + " contains " + "\"" + stringValue
          + "\" that is not marked as regular expression but probably is one.");
    }

    if (isRegularExpression && "".equals(stringValue)) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + " contains an empty string " + "\"" + stringValue
          + "\" that is marked as regular expression.");
    } else if (isRegularExpression
        && !PROBABLE_PATTERN.matcher(stringValue)
            .find()) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + " contains " + "\"" + stringValue
          + "\" that is marked as regular expression but probably is not one.");
    }

    if (isInflected && "".equals(stringValue)) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + " contains " + "\"" + stringValue
          + "\" that is marked as inflected but is empty, so the attribute is redundant.");
    }
    if (isRegularExpression && ".*".equals(stringValue)) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + " marked as regular expression contains "
          + "regular expression \".*\" which is useless: "
          + "(use an empty string without regexp=\"yes\" such as <token/>)");
    }

    if (isRegularExpression && !isCaseSensitive) {
      final Matcher matcher = CASE_PATTERN.matcher(stringValue);
      if (matcher.find()) {
        final String letter1 = matcher.group(1);
        final String letter2 = matcher.group(2);
          final boolean lettersAreSameWithDifferentCase = !letter1.equals(letter2)
                  && letter1.toLowerCase().equals(letter2.toLowerCase());
          if (lettersAreSameWithDifferentCase) {
            System.err.println("The " + lang.toString() + " rule: "
               + ruleId + " contains regexp part [" + letter1 + letter2
               + "] which is useless without case_sensitive=\"yes\".");
        }
      }
    }

    if (isRegularExpression && stringValue.contains("|")) {
      final Matcher matcher = EMPTY_DISJUNCTION.matcher(stringValue);
      if (matcher.find()) {
        // Empty disjunctions in regular expression are most likely not intended.
        System.err.println("The " + lang.toString() + " rule: "
            + ruleId + " contains empty disjunction | within " + "\"" + stringValue + "\".");
      }
      final String[] groups = stringValue.split("\\)");
      for (final String group : groups) {
        final String[] alt = group.split("\\|");
        final Set<String> partSet = new HashSet<String>();
        final Set<String> partSetNoCase = new HashSet<String>();
        for (String part : alt) {
          final String partNoCase = isCaseSensitive ? part : part.toLowerCase();
          if (partSetNoCase.contains(partNoCase)) {
            if (partSet.contains(part)) {
              // Duplicate disjunction parts "foo|foo".
              System.err.println("The " + lang.toString() + " rule: "
                  + ruleId + " contains duplicated disjunction part ("
                  + part + ") within " + "\"" + stringValue + "\".");
            } else {
              // Duplicate disjunction parts "Foo|foo" since element ignores case.
              System.err.println("The " + lang.toString() + " rule: "
                  + ruleId + " contains duplicated non case sensitive disjunction part ("
                  + part + ") within " + "\"" + stringValue + "\". Did you "
                  + "forget case_sensitive=\"yes\"?");
            }
          }
          partSetNoCase.add(partNoCase);
          partSet.add(part);
        }
      }
    }
  }

  private void testBadSentences(JLanguageTool languageTool, Language lang,
                                HashMap<String, PatternRule> complexRules, PatternRule rule) throws IOException {
    final List<IncorrectExample> badSentences = rule.getIncorrectExamples();
      for (IncorrectExample origBadExample : badSentences) {
        // enable indentation use
        final String origBadSentence = origBadExample.getExample().replaceAll(
            "[\\n\\t]+", "");
        final List<String> suggestedCorrections = origBadExample
            .getCorrections();
        final int expectedMatchStart = origBadSentence.indexOf("<marker>");
        final int expectedMatchEnd = origBadSentence.indexOf("</marker>")
            - "<marker>".length();
        if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
          fail(lang
              + ": No error position markup ('<marker>...</marker>') in bad example in rule " + rule);
        }
        final String badSentence = cleanXML(origBadSentence);
        assertTrue(badSentence.trim().length() > 0);
        RuleMatch[] matches = getMatches(rule, badSentence, languageTool);
        if (!rule.isWithComplexPhrase()) {
          assertTrue(lang + ": Did expect one error in: \"" + badSentence
              + "\" (Rule: " + rule + "), but found " + matches.length
              + ". Additional info:" + rule.getMessage(), matches.length == 1);
          assertEquals(lang
              + ": Incorrect match position markup (start) for rule " + rule + ", sentence: " + badSentence,
              expectedMatchStart, matches[0].getFromPos());
          assertEquals(lang
              + ": Incorrect match position markup (end) for rule " + rule + ", sentence: " + badSentence,
              expectedMatchEnd, matches[0].getToPos());
          // make sure suggestion is what we expect it to be
          if (suggestedCorrections != null && suggestedCorrections.size() > 0) {
            assertTrue("You specified a correction but your message has no suggestions in rule " + rule,
              rule.getMessage().contains("<suggestion>")
            );
            assertTrue(lang + ": Incorrect suggestions: "
                + suggestedCorrections.toString() + " != "
                + matches[0].getSuggestedReplacements() + " for rule " + rule + " on input: " + badSentence,
                suggestedCorrections.equals(matches[0].getSuggestedReplacements()));
          }
          // make sure the suggested correction doesn't produce an error:
          if (matches[0].getSuggestedReplacements().size() > 0) {
            final int fromPos = matches[0].getFromPos();
            final int toPos = matches[0].getToPos();
            for (final String replacement : matches[0].getSuggestedReplacements()) {
              final String fixedSentence = badSentence.substring(0, fromPos)
                  + replacement + badSentence.substring(toPos);
              matches = getMatches(rule, fixedSentence, languageTool);
              if (matches.length > 0) {
                  fail("Incorrect input:\n"
                          + "  " + badSentence
                            + "\nCorrected sentence:\n"
                          + "  " + fixedSentence
                          + "\nBy Rule:\n"
                          + "  " + rule
                          + "\nThe correction triggered an error itself:\n"
                          + "  " + matches[0] + "\n");
              }
            }
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
            assertEquals(lang + ": Incorrect match position markup (start) for rule " + rule,
                expectedMatchStart, matches[0].getFromPos());
            assertEquals(lang + ": Incorrect match position markup (end) for rule " + rule,
                expectedMatchEnd, matches[0].getToPos());
            assertSuggestions(suggestedCorrections, lang, matches, rule);
            assertSuggestionsDoNotCreateErrors(languageTool, rule, badSentence, matches);
          }
        }

      }
  }

  private void assertSuggestions(List<String> suggestedCorrections, Language lang, RuleMatch[] matches, Rule rule) {
    if (suggestedCorrections != null && suggestedCorrections.size() > 0) {
      final boolean isExpectedSuggestion = suggestedCorrections.equals(matches[0].getSuggestedReplacements());
      assertTrue(lang + ": Incorrect suggestions: "
              + suggestedCorrections.toString() + " != " + matches[0].getSuggestedReplacements()
              + " for rule " + rule, isExpectedSuggestion);
    }
  }

  private void assertSuggestionsDoNotCreateErrors(JLanguageTool languageTool, PatternRule rule, String badSentence, RuleMatch[] matches) throws IOException {
    if (matches[0].getSuggestedReplacements().size() > 0) {
      final int fromPos = matches[0].getFromPos();
      final int toPos = matches[0].getToPos();
      for (final String replacement : matches[0].getSuggestedReplacements()) {
        final String fixedSentence = badSentence.substring(0, fromPos)
            + replacement + badSentence.substring(toPos);
        matches = getMatches(rule, fixedSentence, languageTool);
        assertEquals("Corrected sentence for rule " + rule
            + " triggered error: " + fixedSentence, 0, matches.length);
      }
    }
  }

  private void testCorrectSentences(JLanguageTool languageTool, Language lang, PatternRule rule) throws IOException {
      final List<String> goodSentences = rule.getCorrectExamples();
      for (String goodSentence : goodSentences) {
        // enable indentation use
        goodSentence = goodSentence.replaceAll("[\\n\\t]+", "");
        goodSentence = cleanXML(goodSentence);
        assertTrue(goodSentence.trim().length() > 0);
        assertFalse(lang + ": Did not expect error in: " + goodSentence
            + " (Rule: " + rule + ")", match(rule, goodSentence, languageTool));
      }
  }

  protected String cleanXML(final String str) {
    return str.replaceAll("<([^<].*?)>", "");
  }

  private boolean match(final Rule rule, final String sentence,
      final JLanguageTool languageTool) throws IOException {
    final AnalyzedSentence text = languageTool.getAnalyzedSentence(sentence);
    final RuleMatch[] matches = rule.match(text);
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

  public void testMakeSuggestionUppercase() throws IOException {
    final JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    langTool.activateDefaultPatternRules();
    final List<RuleMatch> matches = langTool
        .check("Were are in the process of ...");
    assertEquals(1, matches.size());
    final RuleMatch match = matches.get(0);
    final List<String> replacements = match.getSuggestedReplacements();
    assertEquals(2, replacements.size());
    assertEquals("Where", replacements.get(0));
    assertEquals("We", replacements.get(1));
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
    final List<Element> elements = new ArrayList<Element>();
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
    final PatternRule rule = new PatternRule("ID1", Language.ENGLISH, elements,
        "test rule", "user visible message", "short comment");
    return rule;
  }

  public void testSentenceStart() throws IOException {
    final PatternRule pr = makePatternRule("SENT_START One");
    RuleMatch[] matches = pr.match(langTool.getAnalyzedSentence("Not One word."));
    assertEquals(0, matches.length);
    matches = pr.match(langTool.getAnalyzedSentence("One word."));
    assertEquals(1, matches.length);
  }

  /* test private methods as well */
  public void testFormatMultipleSynthesis() throws Exception {
    final String[] suggestions1 = { "blah blah", "foo bar" };

    assertEquals(
        "This is how you should write: <suggestion>blah blah</suggestion>, <suggestion>foo bar</suggestion>.",

        callFormatMultipleSynthesis(suggestions1,
            "This is how you should write: <suggestion>", "</suggestion>."));

    final String[] suggestions2 = { "test", " " };

    assertEquals(
        "This is how you should write: <suggestion>test</suggestion>, <suggestion> </suggestion>.",

        callFormatMultipleSynthesis(suggestions2,
            "This is how you should write: <suggestion>", "</suggestion>."));
  }

  private static String callFormatMultipleSynthesis(final String[] suggestions,
      final String left, final String right) throws Exception {
    final Class[] argClasses = { String[].class, String.class, String.class };
    final Object[] argObjects = { suggestions, left, right };
    return TestTools.callStringStaticMethod(PatternRule.class,
        "formatMultipleSynthesis", argClasses, argObjects);
  }

  /**
   * Test XML patterns, as a help for people developing rules that are not
   * programmers.
   */
  public static void main(final String[] args) throws IOException {
    final PatternRuleTest test = new PatternRuleTest();
    System.out.println("Running XML pattern tests...");
    test.setUp();
    if (args.length == 0) {
      test.testGrammarRulesFromXML(null);
    } else {
      final Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      test.testGrammarRulesFromXML(ignoredLanguages);
    }
    System.out.println("Tests finished!");
  }

}
