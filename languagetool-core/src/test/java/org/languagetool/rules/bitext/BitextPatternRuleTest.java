/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Marcin Miłkowski (www.languagetool.org)
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
package org.languagetool.rules.bitext;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.bitext.StringPair;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.bitext.BitextPatternRule;
import org.languagetool.rules.patterns.bitext.BitextPatternRuleLoader;

public class BitextPatternRuleTest extends TestCase {

  /**
   * To be called from standalone - calling it here in core doesn't make
   * much sense actually as we don't have any languages.
   */  
  public void testBitextRulesFromXML() throws IOException {
    testBitextRulesFromXML(null);
  }

  private void testBitextRulesFromXML(final Set<Language> ignoredLanguages) throws IOException {
    for (final Language lang : Language.LANGUAGES) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      final BitextPatternRuleLoader ruleLoader = new BitextPatternRuleLoader();
      final String name = "/" + lang.getShortName() + "/bitext.xml";
      final InputStream is;
      try {
        is = JLanguageTool.getDataBroker().getFromRulesDirAsStream(name);
      } catch (RuntimeException ignored) {
        // thrown if there is no bitext.xml file
        continue;
      }
      System.out.println("Running tests for " + lang.getName() + "...");
      final JLanguageTool languageTool = new JLanguageTool(lang);
      final List<BitextPatternRule> rules = ruleLoader.getRules(is, name);
      testBitextRulesFromXML(rules, languageTool, lang);
    }
  }
  
  private void testBitextRulesFromXML(final List<BitextPatternRule> rules,
      final JLanguageTool languageTool, final Language lang) throws IOException {    
    for (final BitextPatternRule rule : rules) {
      testBitextRule(rule, lang, languageTool);
    }
  }

  private String cleanSentence(String str) {
    return cleanXML(str.replaceAll("[\\n\\t]+", ""));    
  }
  
  private void testMarker(int expectedMatchStart,
      int expectedMatchEnd, Rule rule, Language lang) {
    if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
      fail(lang
          + ": No error position markup ('<marker>...</marker>') in bad example in rule "
          + rule);
    }

  }

  private void testBadSentence(final String origBadSentence,
                               final List<String> suggestedCorrection, final int expectedMatchStart,
                               final int expectedMatchEnd, final PatternRule rule,
                               final Language lang,
                               final JLanguageTool languageTool) throws IOException {
    final String badSentence = cleanXML(origBadSentence);
    assertTrue(badSentence.trim().length() > 0);
    RuleMatch[] matches = getMatches(rule, badSentence, languageTool);
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
      assertTrue("You specified a correction but your message has no suggestions in rule " + rule,
              rule.getMessage().contains("<suggestion>")
      );
      assertTrue(lang + ": Incorrect suggestions: "
              + suggestedCorrection.toString() + " != "
              + matches[0].getSuggestedReplacements() + " for rule " + rule,
              suggestedCorrection.equals(matches[0]
                      .getSuggestedReplacements()));
      if (matches[0].getSuggestedReplacements().size() > 0) {
        final int fromPos = matches[0].getFromPos();
        final int toPos = matches[0].getToPos();
        for (final String repl : matches[0].getSuggestedReplacements()) {
          final String fixedSentence = badSentence.substring(0, fromPos)
                  + repl + badSentence.substring(toPos);
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
    }
  }

  private void testBitextRule(final BitextPatternRule rule, final Language lang,
                              final JLanguageTool languageTool) throws IOException {
    final JLanguageTool srcTool = new JLanguageTool(rule.getSourceLanguage());
    final List<StringPair> goodSentences = rule.getCorrectBitextExamples();
    for (StringPair goodSentence : goodSentences) {
      assertTrue(cleanSentence(goodSentence.getSource()).trim().length() > 0);
      assertTrue(cleanSentence(goodSentence.getTarget()).trim().length() > 0);
      assertFalse(lang + ": Did not expect error in: " + goodSentence
              + " (Rule: " + rule + ")",
              match(rule, goodSentence.getSource(), goodSentence.getTarget(),
                      srcTool, languageTool));
    }
    final List<IncorrectBitextExample> badSentences = rule.getIncorrectBitextExamples();
    for (IncorrectBitextExample origBadExample : badSentences) {
      // enable indentation use
      final String origBadSrcSentence = origBadExample.getExample().getSource().replaceAll(
              "[\\n\\t]+", "");
      final String origBadTrgSentence = origBadExample.getExample().getTarget().replaceAll(
              "[\\n\\t]+", "");
      final List<String> suggestedCorrection = origBadExample
              .getCorrections();
      final int expectedSrcMatchStart = origBadSrcSentence.indexOf("<marker>");
      final int expectedSrcMatchEnd = origBadSrcSentence.indexOf("</marker>")
              - "<marker>".length();
      testMarker(expectedSrcMatchStart, expectedSrcMatchEnd, rule, lang);
      final int expectedTrgMatchStart = origBadTrgSentence.indexOf("<marker>");
      final int expectedTrgMatchEnd = origBadTrgSentence.indexOf("</marker>")
              - "<marker>".length();
      testMarker(expectedTrgMatchStart, expectedTrgMatchEnd, rule, lang);

      testBadSentence(origBadSrcSentence,
              suggestedCorrection, expectedSrcMatchStart,
              expectedSrcMatchEnd, rule.getSrcRule(),
              lang,
              srcTool);

      testBadSentence(origBadTrgSentence,
              suggestedCorrection, expectedTrgMatchStart,
              expectedTrgMatchEnd, rule.getTrgRule(),
              lang,
              languageTool);
    }
  }

  private String cleanXML(final String str) {
    return str.replaceAll("<([^<].*?)>", "");
  }
  
  private boolean match(final BitextPatternRule rule, final String src, final String trg,
      final JLanguageTool srcLanguageTool,
      final JLanguageTool trgLanguageTool) throws IOException {
    final AnalyzedSentence srcText = srcLanguageTool.getAnalyzedSentence(src);
    final AnalyzedSentence trgText = trgLanguageTool.getAnalyzedSentence(trg);
    final RuleMatch[] matches = rule.match(srcText, trgText);
    return matches.length > 0;
  }

  
  private RuleMatch[] getMatches(final Rule rule, final String sentence,
      final JLanguageTool languageTool) throws IOException {
    final AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(sentence);
    final RuleMatch[] matches = rule.match(analyzedSentence);
    return matches;
  }

  /**
   * Test XML patterns, as a help for people developing rules that are not
   * programmers.
   */
  public static void main(final String[] args) throws IOException {
    final BitextPatternRuleTest prt = new BitextPatternRuleTest();
    System.out.println("Running XML bitext pattern tests...");   
    if (args.length == 0) {
      prt.testBitextRulesFromXML(null);
    } else {
      final Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      prt.testBitextRulesFromXML(ignoredLanguages);
    }
    System.out.println("Tests successful.");
  }

}
