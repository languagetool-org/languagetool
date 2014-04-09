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

package org.languagetool.tagging.disambiguation.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.XMLValidator;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.patterns.PatternTestTools;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.disambiguation.xx.TrimDisambiguator;
import org.languagetool.tools.StringTools;
import org.xml.sax.SAXException;

public class DisambiguationRuleTest extends TestCase {

  /**
   * To be called from standalone - calling it here in core doesn't make
   * much sense actually as we don't have any languages.
   */
  public void testDisambiguationRulesFromXML() throws Exception {
    testDisambiguationRulesFromXML(null);
  }

  private void testDisambiguationRulesFromXML(final Set<Language> ignoredLanguages)
      throws IOException, ParserConfigurationException, SAXException {
    for (final Language lang : Language.REAL_LANGUAGES) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      System.out.println("Running disambiguation tests for " + lang.getName() + "...");
      final DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();
      final JLanguageTool languageTool = new JLanguageTool(lang);
      if (!(languageTool.getLanguage().getDisambiguator() instanceof DemoDisambiguator)
          && !(languageTool.getLanguage().getDisambiguator() instanceof TrimDisambiguator)) {
        final String name = JLanguageTool.getDataBroker().getResourceDir() + "/" + lang.getShortName()
            + "/disambiguation.xml";
        validateRuleFile(name);
        final List<DisambiguationPatternRule> rules = ruleLoader
            .getRules(ruleLoader.getClass().getResourceAsStream(name));
        for (DisambiguationPatternRule rule : rules) {
          PatternTestTools.warnIfRegexpSyntaxNotKosher(rule.getElements(),
              rule.getId(), rule.getSubId(), lang);
        }
        testDisambiguationRulesFromXML(rules, languageTool, lang);
        System.out.println(rules.size() + " rules tested.");
      }
    }
  }

  private void validateRuleFile(String filePath) throws IOException {
    final XMLValidator validator = new XMLValidator();
    final InputStream stream = this.getClass().getResourceAsStream(filePath);
    try {
      if (stream != null) {
        validator.validateWithXmlSchema(filePath, JLanguageTool.getDataBroker().getResourceDir() + "/disambiguation.xsd");
      }
    } finally {
      if (stream != null) { stream.close(); }
    }
  }

  private static String sortForms(final String wordForms) {
    if (",[,]".equals(wordForms)) {
      return wordForms;
    }
    final String word = wordForms.substring(0, wordForms.indexOf('[') + 1);
    final String forms = wordForms.substring(wordForms.indexOf('[') + 1, wordForms.length() -1);
    final String[] formToSort = forms.split(",");
    Arrays.sort(formToSort);
    return word + StringTools.listToString(Arrays.asList(formToSort), ",") + "]";
  }

  private void testDisambiguationRulesFromXML(
      final List<DisambiguationPatternRule> rules,
      final JLanguageTool languageTool, final Language lang) throws IOException {
    for (final DisambiguationPatternRule rule : rules) {
      final String id = rule.getId();
      if (rule.getUntouchedExamples() != null) {
        final List<String> goodSentences = rule.getUntouchedExamples();
        for (String goodSentence : goodSentences) {
          // enable indentation use
          goodSentence = goodSentence.replaceAll("[\\n\\t]+", "");
          goodSentence = cleanXML(goodSentence);

          assertTrue(goodSentence.trim().length() > 0);
          final AnalyzedSentence sent = disambiguateUntil(rules, id,
              languageTool.getRawAnalyzedSentence(goodSentence));
          final AnalyzedSentence sentToReplace = disambiguateUntil(rules, id,
              languageTool.getRawAnalyzedSentence(goodSentence));
          //note: we're testing only if string representations are equal
          //it's because getRawAnalyzedSentence does not set all properties
          //in AnalyzedSentence, and during equal test they are set for the
          //left-hand side
          assertEquals("The untouched example (" + goodSentence + ") for " + lang.getName() +
              " rule " + id +"["+ rule.getSubId() +"] was touched!",
              sent.toString(), rule.replace(sentToReplace).toString());
        }
      }
      final List<DisambiguatedExample> examples = rule.getExamples();
      if (examples != null) {
        for (final DisambiguatedExample example : examples) {

          final String outputForms = example.getDisambiguated();
          assertTrue("No input form found for: " + id, outputForms != null);
          assertTrue(outputForms.trim().length() > 0);
          final int expectedMatchStart = example.getExample().indexOf("<marker>");
          final int expectedMatchEnd = example.getExample().indexOf("</marker>") - "<marker>".length();
          if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
            fail(lang
                + ": No position markup ('<marker>...</marker>') in disambiguated example in rule " + rule);
          }
          final String inputForms = example.getAmbiguous();
          assertTrue("No input form found for: " + id, inputForms != null);
          assertTrue(inputForms.trim().length() > 0);
          assertTrue("Input and output forms for rule " + id + "are the same!",
              !outputForms.equals(inputForms));
          final AnalyzedSentence cleanInput = languageTool
              .getRawAnalyzedSentence(cleanXML(example.getExample()));
          final AnalyzedSentence sent = disambiguateUntil(rules, id,
              languageTool
              .getRawAnalyzedSentence(cleanXML(example.getExample())));
          final AnalyzedSentence disambiguatedSent = rule
              .replace(disambiguateUntil(rules, id, languageTool
                  .getRawAnalyzedSentence(cleanXML(example.getExample()))));
          assertTrue(
              "Disambiguated sentence is equal to the non-disambiguated sentence for rule: "
                  + id, !cleanInput.equals(disambiguatedSent));
          assertTrue(
              "Disambiguated sentence is equal to the input sentence for rule: "
                  + id + ". The sentence was: " + sent, !sent.equals(disambiguatedSent));
          String reading = "";
          String annotations = "";
          for (final AnalyzedTokenReadings readings : sent.getTokens()) {
            if (readings.isSentenceStart() && !inputForms.contains("<S>")) {
              continue;
            }
            if (readings.getStartPos() == expectedMatchStart) {
              final AnalyzedTokenReadings[] r = { readings };
              reading = new AnalyzedSentence(r).toCompactString(",");
              annotations = readings.getHistoricalAnnotations();
              assertTrue(
                  "Wrong marker position in the example for the rule " + id,
                  readings.getStartPos() == expectedMatchStart
                  && readings.getStartPos() + readings.getToken().length() == expectedMatchEnd);
              break;
            }
          }
          assertEquals("The input form for the rule " + id + " in the example: "
              + example.toString() + " is different than expected (expected "
              + inputForms + " but got " + sortForms(reading) + "). The token has been changed by the disambiguator: " + annotations,
              inputForms, sortForms(reading));
          for (final AnalyzedTokenReadings readings : disambiguatedSent.getTokens()) {
            if (readings.isSentenceStart() && !outputForms.contains("<S>")) {
              continue;
            }
            if (readings.getStartPos() == expectedMatchStart) {
              final AnalyzedTokenReadings[] r = { readings };
              reading = new AnalyzedSentence(r).toCompactString(",");
              assertTrue(readings.getStartPos() == expectedMatchStart
                  && readings.getStartPos() + readings.getToken().length() == expectedMatchEnd);
              break;
            }
          }
          assertEquals("The output form for the rule " + id + " in the example: "
              + example.toString() + " is different than expected (expected "
              + outputForms + " but got " + sortForms(reading) + "). The token has been changed by the disambiguator: " + annotations,
              outputForms, sortForms(reading));
        }
      }
    }
  }

  // useful for testing the rule cascade
  private static AnalyzedSentence disambiguateUntil(
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

  private static String cleanXML(final String str) {
    return str.replaceAll("<.*?>", "");
  }

  /**
   * Test XML patterns, as a help for people developing rules that are not
   * programmers.
   */
  public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
    final DisambiguationRuleTest test = new DisambiguationRuleTest();
    System.out.println("Running disambiguator rule tests...");
    if (args.length == 0) {
      test.testDisambiguationRulesFromXML(null);
    } else {
      final Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      test.testDisambiguationRulesFromXML(ignoredLanguages);
    }
    System.out.println("Tests successful.");
  }

}
