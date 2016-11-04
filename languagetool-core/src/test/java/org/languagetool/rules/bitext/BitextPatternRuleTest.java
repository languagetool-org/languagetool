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

import org.junit.Test;
import org.languagetool.*;
import org.languagetool.bitext.StringPair;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.bitext.BitextPatternRule;
import org.languagetool.rules.patterns.bitext.BitextPatternRuleLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class BitextPatternRuleTest {

  /**
   * To be called from standalone - calling it here in core doesn't make
   * much sense actually as we don't have any languages.
   */
  @Test
  public void testBitextRulesFromXML() throws IOException {
    testBitextRulesFromXML(null);
  }

  private void testBitextRulesFromXML(Set<Language> ignoredLanguages) throws IOException {
    for (Language lang : Languages.getWithDemoLanguage()) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      BitextPatternRuleLoader ruleLoader = new BitextPatternRuleLoader();
      String name = "/" + lang.getShortCode() + "/bitext.xml";
      InputStream is;
      try {
        is = JLanguageTool.getDataBroker().getFromRulesDirAsStream(name);
      } catch (RuntimeException ignored) {
        // thrown if there is no bitext.xml file
        continue;
      }
      System.out.println("Running tests for " + lang.getName() + "...");
      JLanguageTool languageTool = new JLanguageTool(lang);
      List<BitextPatternRule> rules = ruleLoader.getRules(is, name);
      testBitextRulesFromXML(rules, languageTool, lang);
    }
  }
  
  private void testBitextRulesFromXML(List<BitextPatternRule> rules,
      JLanguageTool languageTool, Language lang) throws IOException {    
    for (BitextPatternRule rule : rules) {
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

  private void testBadSentence(String origBadSentence,
                               List<String> suggestedCorrection, int expectedMatchStart,
                               int expectedMatchEnd, AbstractPatternRule rule,
                               Language lang,
                               JLanguageTool languageTool) throws IOException {
    String badSentence = cleanXML(origBadSentence);
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
              + suggestedCorrection + " != "
              + matches[0].getSuggestedReplacements() + " for rule " + rule,
              suggestedCorrection.equals(matches[0]
                      .getSuggestedReplacements()));
      if (matches[0].getSuggestedReplacements().size() > 0) {
        int fromPos = matches[0].getFromPos();
        int toPos = matches[0].getToPos();
        for (String repl : matches[0].getSuggestedReplacements()) {
          String fixedSentence = badSentence.substring(0, fromPos)
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

  private void testBitextRule(BitextPatternRule rule, Language lang,
                              JLanguageTool languageTool) throws IOException {
    JLanguageTool srcTool = new JLanguageTool(rule.getSourceLanguage());
    List<StringPair> goodSentences = rule.getCorrectBitextExamples();
    for (StringPair goodSentence : goodSentences) {
      assertTrue("Got good sentence: '" + goodSentence.getSource() + "'", cleanSentence(goodSentence.getSource()).trim().length() > 0);
      assertTrue("Got good sentence: '" + goodSentence.getTarget() + "'", cleanSentence(goodSentence.getTarget()).trim().length() > 0);
      assertFalse(lang + ": Did not expect error in: " + goodSentence
              + " (Rule: " + rule + ")",
              match(rule, goodSentence.getSource(), goodSentence.getTarget(),
                      srcTool, languageTool));
    }
    List<IncorrectBitextExample> badSentences = rule.getIncorrectBitextExamples();
    for (IncorrectBitextExample origBadExample : badSentences) {
      // enable indentation use
      StringPair example = origBadExample.getExample();
      String origBadSrcSentence = example.getSource().replaceAll("[\\n\\t]+", "");
      String origBadTrgSentence = example.getTarget().replaceAll("[\\n\\t]+", "");
      List<String> suggestedCorrection = origBadExample.getCorrections();
      int expectedSrcMatchStart = origBadSrcSentence.indexOf("<marker>");
      int expectedSrcMatchEnd = origBadSrcSentence.indexOf("</marker>")
              - "<marker>".length();
      testMarker(expectedSrcMatchStart, expectedSrcMatchEnd, rule, lang);
      int expectedTrgMatchStart = origBadTrgSentence.indexOf("<marker>");
      int expectedTrgMatchEnd = origBadTrgSentence.indexOf("</marker>")
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

  private String cleanXML(String str) {
    return str.replaceAll("<([^<].*?)>", "");
  }
  
  private boolean match(BitextPatternRule rule, String src, String trg,
      JLanguageTool srcLanguageTool,
      JLanguageTool trgLanguageTool) throws IOException {
    AnalyzedSentence srcText = srcLanguageTool.getAnalyzedSentence(src);
    AnalyzedSentence trgText = trgLanguageTool.getAnalyzedSentence(trg);
    RuleMatch[] matches = rule.match(srcText, trgText);
    return matches.length > 0;
  }

  
  private RuleMatch[] getMatches(Rule rule, String sentence,
      JLanguageTool languageTool) throws IOException {
    AnalyzedSentence analyzedSentence = languageTool.getAnalyzedSentence(sentence);
    RuleMatch[] matches = rule.match(analyzedSentence);
    return matches;
  }

  /**
   * Test XML patterns, as a help for people developing rules that are not
   * programmers.
   */
  public static void main(String[] args) throws IOException {
    BitextPatternRuleTest prt = new BitextPatternRuleTest();
    System.out.println("Running XML bitext pattern tests...");   
    if (args.length == 0) {
      prt.testBitextRulesFromXML(null);
    } else {
      Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      prt.testBitextRulesFromXML(ignoredLanguages);
    }
    System.out.println("Tests successful.");
  }

}
