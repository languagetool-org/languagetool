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

package de.danielnaber.languagetool.tagging.disambiguation.rules;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.xx.TrimDisambiguator;

public class DisambiguationRuleTest extends TestCase {

  private static JLanguageTool langTool;

  @Override
  public void setUp() throws IOException {
    if (langTool == null) {
      langTool = new JLanguageTool(Language.ENGLISH);
    }
  }

  public void testGrammarRulesFromXML() throws IOException,
      ParserConfigurationException, SAXException {
    testDisambiguationRulesFromXML(null, false);
  }

  private void testDisambiguationRulesFromXML(
      final Set<Language> ignoredLanguages, final boolean verbose)
      throws IOException, ParserConfigurationException, SAXException {
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
      final DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();
      final JLanguageTool languageTool = new JLanguageTool(lang);
      if (!(languageTool.getLanguage().getDisambiguator() instanceof DemoDisambiguator)
          && !(languageTool.getLanguage().getDisambiguator() instanceof TrimDisambiguator)) {
        final String name = "/resource/" + lang.getShortName()
            + "/disambiguation.xml";
        final List<DisambiguationPatternRule> rules = ruleLoader
            .getRules(ruleLoader.getClass().getResourceAsStream(name));
        testDisambiguationRulesFromXML(rules, languageTool, lang);
      }
    }
  }

  private void testDisambiguationRulesFromXML(
      final List<DisambiguationPatternRule> rules,
      final JLanguageTool languageTool, final Language lang) throws IOException {
    for (final DisambiguationPatternRule rule : rules) {
      if (rule.getUntouchedExamples() != null) {
        final List<String> goodSentences = rule.getUntouchedExamples();
        for (String goodSentence : goodSentences) {
          // enable indentation use
          goodSentence = goodSentence.replaceAll("[\\n\\t]+", "");
          goodSentence = cleanXML(goodSentence);

          assertTrue(goodSentence.trim().length() > 0);
          final AnalyzedSentence sent = disambiguateUntil(rules, rule.getId(),
              languageTool.getRawAnalyzedSentence(goodSentence));
          assertEquals(sent, rule.replace(sent));
        }
      }
      final List<DisambiguatedExample> examples = rule.getExamples();
      if (examples != null) {
        for (final DisambiguatedExample example : examples) {

          final String outputForms = example.getDisambiguated();
          final int expectedMatchStart = example.getExample().indexOf(
              "<marker>");
          final int expectedMatchEnd = example.getExample()
              .indexOf("</marker>")
              - "<marker>".length();
          if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
            fail(lang
                + ": No error position markup ('<marker>...</marker>') in bad example in rule "
                + rule);
          }
          final String inputForms = example.getAmbiguous();
          assertTrue(inputForms.trim().length() > 0);
          assertNotSame(outputForms, inputForms);
          final AnalyzedSentence sent = disambiguateUntil(rules, rule.getId(),
              languageTool
                  .getRawAnalyzedSentence(cleanXML(example.getExample())));
          final AnalyzedSentence disambiguatedSent = rule.replace(sent);
          assertNotSame(sent, disambiguatedSent);
          String reading = "";
          for (final AnalyzedTokenReadings readings : sent.getTokens()) {
            if (readings.isSentStart() && inputForms.indexOf("<S>") == -1) {
              continue;
            }
            if (readings.getStartPos() == expectedMatchStart) {
              final AnalyzedTokenReadings r[] = { readings };
              reading = new AnalyzedSentence(r).toString();
              assertTrue(readings.getStartPos() == expectedMatchStart
                  && readings.getStartPos() + readings.getToken().length() == expectedMatchEnd);
              break;
            }
          }
          assertTrue("The input form for the rule " + rule.getId()
              + " in the example: " + example.toString()
              + " is different than expected (" + reading + ").", reading
              .equals(inputForms));
          for (final AnalyzedTokenReadings readings : disambiguatedSent
              .getTokens()) {
            if (readings.isSentStart() && outputForms.indexOf("<S>") == -1) {
              continue;
            }
            if (readings.getStartPos() == expectedMatchStart) {
              final AnalyzedTokenReadings r[] = { readings };
              reading = new AnalyzedSentence(r).toString();
              assertTrue(readings.getStartPos() == expectedMatchStart
                  && readings.getStartPos() + readings.getToken().length() == expectedMatchEnd);
              break;
            }
          }
          assertTrue("The output form for the rule " + rule.getId()
              + " in the example: " + example.toString()
              + " is different than expected (" + reading + ").", reading
              .equals(outputForms));
        }
      }
    }
  }

  // useful for testing the rule cascade
  private AnalyzedSentence disambiguateUntil(
      final List<DisambiguationPatternRule> rules, final String ruleID,
      final AnalyzedSentence sentence) throws IOException {
    AnalyzedSentence disambiguated = sentence;
    for (final DisambiguationPatternRule rule : rules) {
      if (ruleID.equals(rule.getId())) {
        break;
      }
      disambiguated = rule.replace(disambiguated);
    }
    return disambiguated;
  }

  private String cleanXML(final String str) {
    return str.replaceAll("<.*?>", "");
  }

}
