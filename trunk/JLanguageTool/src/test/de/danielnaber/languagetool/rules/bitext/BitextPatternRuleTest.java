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

package de.danielnaber.languagetool.rules.bitext;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.bitext.StringPair;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.patterns.PatternRule;
import de.danielnaber.languagetool.rules.patterns.bitext.BitextPatternRule;
import de.danielnaber.languagetool.rules.patterns.bitext.BitextPatternRuleLoader;
import junit.framework.TestCase;

public class BitextPatternRuleTest extends TestCase {

  public void testBitextRulesFromXML() throws IOException {
    testBitextRulesFromXML(null, false);
  }

  private void testBitextRulesFromXML(final Set<Language> ignoredLanguages,
                                      final boolean verbose) throws IOException {
    int testCount = 0;
    for (final Language lang : Language.LANGUAGES) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      final BitextPatternRuleLoader ruleLoader = new BitextPatternRuleLoader();
      final String name = "/" + lang.getShortName() + "/bitext.xml";
      try {
        testCount++;
        final InputStream is = JLanguageTool.getDataBroker().getFromRulesDirAsStream(name);
        if (verbose) {
          System.out.println("Running tests for " + lang.getName() + "...");
        }
        final JLanguageTool languageTool = new JLanguageTool(lang);
        final List<BitextPatternRule> rules = ruleLoader.getRules(is, name);
        testBitextRulesFromXML(rules, languageTool, lang);
      } catch (RuntimeException ignored) {
        // thrown if there is no bitext.xml file
      }
    }
    assertTrue(testCount >= 1);
  }
  
  private void testBitextRulesFromXML(final List<BitextPatternRule> rules,
      final JLanguageTool languageTool, final Language lang) throws IOException {    
    final HashMap<String, PatternRule> complexRules = new HashMap<String, PatternRule>();
    for (final BitextPatternRule rule : rules) {
      testBitextRule(rule, lang, languageTool);      
    }
    /*
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
    */
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
//    if (!rule.isWithComplexPhrase()) {
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
//      }
      // make sure the suggested correction doesn't produce an error:
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
    JLanguageTool srcTool = new JLanguageTool(rule.getSourceLang());
    //int noSuggestionCount = 0;
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
      String origBadSrcSentence = origBadExample.getExample().getSource().replaceAll(
          "[\\n\\t]+", "");
      String origBadTrgSentence = origBadExample.getExample().getTarget().replaceAll(
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
      
      /*      } else { // for multiple rules created with complex phrases

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
        } */
      } 

  
    
    
  protected String cleanXML(final String str) {
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
    final AnalyzedSentence text = languageTool.getAnalyzedSentence(sentence);
    final RuleMatch[] matches = rule.match(text);
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
      prt.testBitextRulesFromXML(null, true);
    } else {
      final Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      prt.testBitextRulesFromXML(ignoredLanguages, true);
    }
    System.out.println("Tests successful.");
  }

}
