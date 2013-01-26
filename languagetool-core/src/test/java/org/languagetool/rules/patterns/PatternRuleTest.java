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

import java.io.IOException;
import java.lang.String;
import java.util.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.languagetool.*;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.language.Demo;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

/**
 * @author Daniel Naber
 */
public class PatternRuleTest extends TestCase {

  // A test sentence should only be a single sentence - if that's not the case it can
  // happen that rules are checked as being correct that in reality will never match.
  // This check prints a warning for affected rules, but it's disabled by default because
  // it makes the tests very slow:
  private static final boolean CHECK_WITH_SENTENCE_SPLITTING = false;

  // The  [^cfmnt123]\\.|\\.[^mvngl]  part is there to consider a string as a
  // regexp if and only if it is not enclosed on both sides by those characters.
  // This is to cope with Polish POS tags which contain dots without being
  // a regexp.
  private static final Pattern PROBABLE_PATTERN = Pattern.compile("(.+[+?^{}()|\\[\\]].*)|(.*[+?^{}()|\\[\\]].+)|(\\(.*\\))|(\\\\[^0-9].*)|[^cfmnt123]\\.|\\.[^ampvngl]|(.+\\.$)");
  private static final Pattern EMPTY_DISJUNCTION = Pattern.compile("^[|]|[|][|]|[|]$");
  private static final Pattern CHAR_SET_PATTERN = Pattern.compile("(\\(\\?-i\\))?.*(?<!\\\\)\\[^?([^\\]]+)\\]");

  private static JLanguageTool langTool;

  @Override
  public void setUp() throws IOException {
    if (langTool == null) {
      langTool = new JLanguageTool(Language.DEMO);
    }
  }

  public void testGrammarRulesFromXML2() throws IOException {
    new PatternRule("-1", Language.DEMO, Collections.<Element>emptyList(), "", "", "");
  }

  public void testDemoLanguageGrammarRules() throws IOException {
    runTestForLanguage(new Demo());
  }

  /** To be called from language modules. */
  protected void runGrammarRulesFromXmlTest() throws IOException {
    for (final Language lang : Language.REAL_LANGUAGES) {
      if (skipCountryVariant(lang) && Language.REAL_LANGUAGES.length>1) {
        System.out.println("Skipping " + lang + " because there are no specific rules for that variant");
        continue;
      }
      validatePatternFile(lang);
      runTestForLanguage(lang);
    }
  }

  private boolean skipCountryVariant(Language lang) {
    final ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    return !dataBroker.ruleFileExists(getGrammarFileName(lang));
  }

  private String getGrammarFileName(Language lang) {
    final String shortNameWithVariant = lang.getShortNameWithVariant();
    final String fileName;
    if (shortNameWithVariant.contains("-") && !shortNameWithVariant.endsWith("-ANY")  && Language.REAL_LANGUAGES.length>1) {
      fileName = lang.getShortName() + "/" + shortNameWithVariant + "/" + JLanguageTool.PATTERN_FILE;
    } else {
      fileName = lang.getShortName() + "/" + JLanguageTool.PATTERN_FILE;
    }
    return fileName;
  }
  
  private void runTestForLanguage(Language lang) throws IOException {
    System.out.print("Running pattern rule tests for " + lang.getName() + "... ");
    final JLanguageTool languageTool = new JLanguageTool(lang);
    if (CHECK_WITH_SENTENCE_SPLITTING) {
      languageTool.activateDefaultPatternRules();
      disableSpellingRules(languageTool);
    }
    final JLanguageTool allRulesLanguageTool = new JLanguageTool(lang);
    allRulesLanguageTool.activateDefaultPatternRules();
    final List<PatternRule> rules = new ArrayList<PatternRule>();
    for (String patternRuleFileName : lang.getRuleFileName()) {
      rules.addAll(languageTool.loadPatternRules(patternRuleFileName));
    }
    warnIfRegexpSyntaxNotKosher(rules, lang);
    testGrammarRulesFromXML(rules, languageTool, allRulesLanguageTool, lang);
    System.out.println(rules.size() + " rules tested.");
  }

  private void validatePatternFile(Language lang) throws IOException {
    final XMLValidator validator = new XMLValidator();
    final String grammarFile = getGrammarFileName(lang);
    System.out.println("Running XML validation for " + grammarFile + "...");
    final String rulesDir = JLanguageTool.getDataBroker().getRulesDir();
    validator.validate(rulesDir + "/" + grammarFile, rulesDir + "/rules.xsd");
  }
  
  public void runGrammarRulesFromXmlTestIgnoringLanguages(Set<Language> ignoredLanguages) throws IOException {
    System.out.println("Known languages: " + Arrays.toString(Language.LANGUAGES));
    for (final Language lang : Language.LANGUAGES) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      System.out.println("Running tests for " + lang.getName() + "...");
      final JLanguageTool languageTool = new JLanguageTool(lang);
      if (CHECK_WITH_SENTENCE_SPLITTING) {
        languageTool.activateDefaultPatternRules();
        disableSpellingRules(languageTool);
      }
      final JLanguageTool allRulesLanguageTool = new JLanguageTool(lang);
      allRulesLanguageTool.activateDefaultPatternRules();
      final List<PatternRule> rules = new ArrayList<PatternRule>();
      for (String patternRuleFileName : lang.getRuleFileName()) {
        rules.addAll(languageTool.loadPatternRules(patternRuleFileName));
      }
      warnIfRegexpSyntaxNotKosher(rules, lang);
      testGrammarRulesFromXML(rules, languageTool, allRulesLanguageTool, lang);
    }
  }

  private void disableSpellingRules(JLanguageTool languageTool) {
    final List<Rule> allRules = languageTool.getAllRules();
    for (Rule rule : allRules) {
      if (rule instanceof SpellingCheckRule) {
        languageTool.disableRule(rule.getId());
      }
    }
  }

  private void testGrammarRulesFromXML(final List<PatternRule> rules,
                                       final JLanguageTool languageTool,
                                       final JLanguageTool allRulesLanguageTool, final Language lang) throws IOException {
    final HashMap<String, PatternRule> complexRules = new HashMap<String, PatternRule>();
    for (final PatternRule rule : rules) {
      testCorrectSentences(languageTool, allRulesLanguageTool, lang, rule);
      testBadSentences(languageTool, allRulesLanguageTool, lang, complexRules, rule);
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
        testGrammarRulesFromXML(badRules, languageTool, allRulesLanguageTool, lang);
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

        if (element.isReferenceElement()) {
            continue;
        }

        // Check whether token value is consistent with regexp="..."
        warnIfElementNotKosher(
          element.getString(),
          element.isRegularExpression(),
          element.getCaseSensitive(),
          element.getNegation(),
          element.isInflected(),
          lang, rule.getId() + ":" + rule.getSubId(),
          i);

        // Check postag="..." is consistent with postag_regexp="..."
        warnIfElementNotKosher(
          element.getPOStag() == null ? "" : element.getPOStag(),
          element.isPOStagRegularExpression(),
          element.getCaseSensitive(),
          element.getPOSNegation(),
          false,
          lang, rule.getId() + ":" + rule.getSubId() + " (POS tag)",
          i);

        List<Element> exceptionElements = new ArrayList<Element>();
        if (element.getExceptionList() != null) {
          for (final Element exception: element.getExceptionList()) {
            // Detect useless exception or missing skip="...".
            if (exception.hasNextException() && element.getSkipNext() == 0) {
              System.err.println("The " + lang.toString() + " rule: "
                  + rule.getId() + ":" + rule.getSubId()
                  + " (exception in token [" + i + "])"
                  + " has no skip=\"...\" and yet contains scope=\"next\""
                  + " so the exception never applies. "
                  + " Did you forget skip=\"...\"?");
            }

            // Check whether exception value is consistent with regexp="..."
            // Don't check string "." since it is sometimes used as a regexp
            // and sometimes used as non regexp.
            if (!exception.getString().equals(".")) {
              warnIfElementNotKosher(
                exception.getString(),
                exception.isRegularExpression(),
                exception.getCaseSensitive(),
                exception.getNegation(),
                exception.isInflected(),
                lang,
                rule.getId() + ":" + rule.getSubId()+ " (exception in token [" + i + "])",
                i);
            }
            // Check postag="..." of exception is consistent with postag_regexp="..."
            warnIfElementNotKosher(
              exception.getPOStag() == null ? "" : exception.getPOStag(),
              exception.isPOStagRegularExpression(),
              exception.getCaseSensitive(),
              exception.getPOSNegation(),
              false,
              lang,
              rule.getId() + ":" + rule.getSubId() + " (exception in POS tag of token [" + i + "])",
              i);

            // Search for duplicate exceptions (which are useless).
            // Since there are 2 nested loops on the list of exceptions,
            // this has thus a O(n^2) complexity, where n is the number
            // of exception in a token. But n is small and it is also
            // for testing only so that's OK.
            for (final Element otherException: exceptionElements) {
              if (equalException(exception, otherException)) {
                System.err.println("The " + lang.toString() + " rule: "
                    + rule.getId() + ":" + rule.getSubId()
                    + " in token [" + i + "]"
                    + " contains duplicate exceptions with"
                    + " string=[" + exception.getString() + "]"
                    + " POS tag=[" + exception.getPOStag() + "]"
                    + " negate=[" + exception.getNegation() + "]"
                    + " POS negate=[" + exception.getPOSNegation() + "]");
                break;
              }
            }
            exceptionElements.add(exception);
          }
        }
      }
    }
  }

  /**
   * Predicate to check whether two exceptions are identical or whether
   * one exception always implies the other.
   *
   * Example #1, useless identical exceptions:
   * <exception>xx</exception><exception>xx</exception>
   *
   * Example #2, first exception implies the second exception:
   * <exception>xx</exception><exception postag="A">xx</exception>
   */
  private static boolean equalException(final Element exception1,
                                        final Element exception2)
  {
    String string1 = exception1.getString() == null ? "" : exception1.getString();
    String string2 = exception2.getString() == null ? "" : exception2.getString();
    if (!exception1.getCaseSensitive() || !exception2.getCaseSensitive()) {
      // String comparison is done case insensitive if one or both strings
      // are case insensitive, because the case insensitive one would imply
      // the case sensitive one.
      string1 = string1.toLowerCase();
      string2 = string2.toLowerCase();
    }
    if (!string1.isEmpty() && !string2.isEmpty()) {
      if (!string1.equals(string2)) {
        return false;
      }
    }

    final String posTag1 = exception1.getPOStag() == null ? "" : exception1.getPOStag();
    final String posTag2 = exception2.getPOStag() == null ? "" : exception2.getPOStag();
    if (!posTag1.isEmpty() && !posTag2.isEmpty()) {
      if (!posTag1.equals(posTag2)) {
        return false;
      }
    }

    if ( string1.isEmpty() != string2.isEmpty()
      && posTag1.isEmpty() != posTag2.isEmpty()) {
      return false;
    }

    // We should not need to check for:
    // - isCaseSensitive() since an exception without isCaseSensitive
    //   imply the one with isCaseSensitive.
    // - isInflected() since an exception with inflected="yes"
    //   implies the one without inflected="yes" if they have
    //   identical strings.
    //   without inflected="yes".
    // - isRegularExpression() since a given string is either
    //   a regexp or not.
    return exception1.getNegation() == exception2.getNegation()
        && exception1.getPOSNegation() == exception2.getPOSNegation()
        && exception1.hasNextException() == exception2.hasNextException()
        && exception1.hasPreviousException() == exception2.hasPreviousException();
  }

  private void warnIfElementNotKosher(
      final String stringValue,
      final boolean isRegularExpression,
      final boolean isCaseSensitive,
      final boolean isNegated,
      final boolean isInflected,
      final Language lang,
      final String ruleId,
      final int tokenIndex) {

    if (!isRegularExpression
        && PROBABLE_PATTERN.matcher(stringValue).find()) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + ", token [" + tokenIndex + "], contains " + "\"" + stringValue
          + "\" that is not marked as regular expression but probably is one.");
    }

    if (isRegularExpression && stringValue.isEmpty()) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + ", token [" + tokenIndex + "], contains an empty string " + "\""
          + stringValue + "\" that is marked as regular expression.");
    } else if (isRegularExpression
        && !PROBABLE_PATTERN.matcher(stringValue)
            .find()) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + ", token [" + tokenIndex + "], contains " + "\"" + stringValue
          + "\" that is marked as regular expression but probably is not one.");
    }

    if (isNegated && stringValue.isEmpty()) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + ", token [" + tokenIndex + "], marked as negated but is "
          + "empty so the negation is useless. Did you mix up "
          + "negate=\"yes\" and negate_pos=\"yes\"?");
    }
    if (isInflected && stringValue.isEmpty()) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + ", token [" + tokenIndex + "], contains " + "\"" + stringValue
          + "\" that is marked as inflected but is empty, so the attribute is redundant.");
    }
    if (isRegularExpression && ".*".equals(stringValue)) {
      System.err.println("The " + lang.toString() + " rule: "
          + ruleId + ", token [" + tokenIndex + "], marked as regular expression contains "
          + "regular expression \".*\" which is useless: "
          + "(use an empty string without regexp=\"yes\" such as <token/>)");
    }

    if (isRegularExpression) {
      final Matcher matcher = CHAR_SET_PATTERN.matcher(stringValue);
      if (matcher.find()) {
        // Remove things like \p{Punct} which are irrelevant here.
        String s = matcher.group(2).replaceAll("\\\\p\\{[^}]*\\}", "");
        // case sensitive if pattern contains (?-i).
        if (s.indexOf('|') >= 0) {
          System.err.println("The " + lang.toString() + " rule: "
             + ruleId + ", token [" + tokenIndex + "], contains | (pipe) in "
             + " regexp bracket expression [" + matcher.group(2)
             + "] which is unlikely to be correct.");
        }

        /* Disabled case insensitive check for now: it gives several errors
         * in German which are minor and debatable whether it adds value. 
        final boolean caseSensitive = matcher.group(1) != null || isCaseSensitive;
        if (!caseSensitive) {
          s = s.toLowerCase();
        }
        */
        char[] sorted = s.toCharArray();
        // Sort characters in string, so finding duplicate characters can be done by
        // looking for identical adjacent characters.
        java.util.Arrays.sort(sorted);
        for (int i = 1; i < sorted.length; ++i) {
          char c = sorted[i];
          if ("&\\-|".indexOf(c) < 0 && sorted[i - 1] == c) {
            System.err.println("The " + lang.toString() + " rule: "
               + ruleId + ", token [" + tokenIndex + "], contains "
               + " regexp part [" + matcher.group(2)
               + "] which contains duplicated char [" + c + "].");
            break;
          }
        }
      }
    }

    if (isRegularExpression && stringValue.contains("|")) {
      final Matcher matcher = EMPTY_DISJUNCTION.matcher(stringValue);
      if (matcher.find()) {
        // Empty disjunctions in regular expression are most likely not intended.
        System.err.println("The " + lang.toString() + " rule: "
            + ruleId + ", token [" + tokenIndex + "], contains empty "
            + "disjunction | within " + "\"" + stringValue + "\".");
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
                  + ruleId + ", token [" + tokenIndex + "], contains "
                  + "duplicated disjunction part ("
                  + part + ") within " + "\"" + stringValue + "\".");
            } else {
              // Duplicate disjunction parts "Foo|foo" since element ignores case.
              System.err.println("The " + lang.toString() + " rule: "
                  + ruleId + ", token [" + tokenIndex + "], contains duplicated "
                  + "non case sensitive disjunction part ("
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

  private void testBadSentences(JLanguageTool languageTool, JLanguageTool allRulesLanguageTool, Language lang,
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
        List<RuleMatch> matches = getMatches(rule, badSentence, languageTool);
        if (!rule.isWithComplexPhrase()) {
          assertTrue(lang + ": Did expect one error in: \"" + badSentence
              + "\" (Rule: " + rule + "), but found " + matches.size()
              + ". Additional info:" + rule.getMessage() + ", Matches: " + matches, matches.size() == 1);
          assertEquals(lang
                  + ": Incorrect match position markup (start) for rule " + rule + ", sentence: " + badSentence,
                  expectedMatchStart, matches.get(0).getFromPos());
          assertEquals(lang
                  + ": Incorrect match position markup (end) for rule " + rule + ", sentence: " + badSentence,
                  expectedMatchEnd, matches.get(0).getToPos());
          // make sure suggestion is what we expect it to be
          if (suggestedCorrections != null && suggestedCorrections.size() > 0) {
            assertTrue("You specified a correction but your message has no suggestions in rule " + rule,
              rule.getMessage().contains("<suggestion>")
            );
            assertTrue(lang + ": Incorrect suggestions: "
                + suggestedCorrections.toString() + " != "
                + matches.get(0).getSuggestedReplacements() + " for rule " + rule + " on input: " + badSentence,
                suggestedCorrections.equals(matches.get(0).getSuggestedReplacements()));
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
      return x <= b ? true : false;
    } else {
      return a <= y ? true : false;
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
        List<RuleMatch> tempMatches = getMatches(rule, fixedSentence, languageTool);
        assertEquals("Corrected sentence for rule " + rule
            + " triggered error: " + fixedSentence, 0, tempMatches.size());
      }
    }
  }

  private void testCorrectSentences(JLanguageTool languageTool, JLanguageTool allRulesLanguageTool,
                                    Language lang, PatternRule rule) throws IOException {
      final List<String> goodSentences = rule.getCorrectExamples();
      for (String goodSentence : goodSentences) {
        // enable indentation use
        goodSentence = goodSentence.replaceAll("[\\n\\t]+", "");
        goodSentence = cleanXML(goodSentence);
        assertTrue(goodSentence.trim().length() > 0);
        assertFalse(lang + ": Did not expect error in: " + goodSentence
            + " (Rule: " + rule + ")", match(rule, goodSentence, languageTool));
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

  private boolean match(final Rule rule, final String sentence,
      final JLanguageTool languageTool) throws IOException {
    final AnalyzedSentence text = languageTool.getAnalyzedSentence(sentence);
    final RuleMatch[] matches = rule.match(text);
    return matches.length > 0;
  }

  private List<RuleMatch> getMatches(final Rule rule, final String sentence,
      final JLanguageTool languageTool) throws IOException {
    final AnalyzedSentence text = languageTool.getAnalyzedSentence(sentence);
    final RuleMatch[] matches = rule.match(text);
    if (CHECK_WITH_SENTENCE_SPLITTING) {
      // "real check" with sentence splitting:
      for (Rule r : languageTool.getAllActiveRules()) {
        languageTool.disableRule(r.getId());
      }
      languageTool.enableRule(rule.getId());
      final List<RuleMatch> realMatches = languageTool.check(sentence);
      final List<String> realMatchRuleIds = new ArrayList<String>();
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

  public void testMakeSuggestionUppercase() throws IOException {
    final JLanguageTool langTool = new JLanguageTool(Language.DEMO);
    langTool.activateDefaultPatternRules();

    final Element element = new Element("Were", false, false, false);
    final String message = "Did you mean: <suggestion>where</suggestion> or <suggestion>we</suggestion>?";
    final PatternRule rule = new PatternRule("MY_ID", Language.DEMO, Collections.singletonList(element), "desc", message, "msg");
    final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Were are in the process of ..."));

    assertEquals(1, matches.length);
    final RuleMatch match = matches[0];
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
    final PatternRule rule = new PatternRule("ID1", Language.DEMO, elements,
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
    return TestTools.callStringStaticMethod(PatternRuleMatcher.class,
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
      test.runGrammarRulesFromXmlTestIgnoringLanguages(null);
    } else {
      final Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      test.runGrammarRulesFromXmlTestIgnoringLanguages(ignoredLanguages);
    }
    System.out.println("Tests finished!");
  }

}
