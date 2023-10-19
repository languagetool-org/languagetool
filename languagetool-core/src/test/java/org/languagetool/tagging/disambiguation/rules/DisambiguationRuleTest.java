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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.languagetool.JLanguageTool.getDataBroker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.XMLValidator;
import org.languagetool.rules.patterns.PatternTestTools;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.xml.sax.SAXException;

public class DisambiguationRuleTest {

  /**
   * To be called from standalone or language modules - calling it here in core doesn't make
   * much sense actually as we don't have any languages.
   */
  @Test
  public void testDisambiguationRulesFromXML() throws Exception {
    testDisambiguationRulesFromXML(null);
  }

  private void testDisambiguationRulesFromXML(Set<Language> ignoredLanguages)
      throws IOException, ParserConfigurationException, SAXException {
    for (Language lang : Languages.getWithDemoLanguage()) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      if (lang.isVariant()) {
        System.out.println("Skipping variant: " + lang);
        continue;
      }
      System.out.println("Running disambiguation tests for " + lang.getName() + "...");
      DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();
      JLanguageTool lt = new JLanguageTool(lang);
      if (!(lt.getLanguage().getDisambiguator() instanceof DemoDisambiguator)) {
        long startTime = System.currentTimeMillis();
        String name = getDataBroker().getResourceDir() + "/" + lang.getShortCode() + "/disambiguation.xml";
        validateRuleFile(name);
        List<DisambiguationPatternRule> rules = ruleLoader.getRules(ruleLoader.getClass().getResourceAsStream(name));
        for (DisambiguationPatternRule rule : rules) {
          PatternTestTools.warnIfRegexpSyntaxNotKosher(rule.getPatternTokens(), rule.getId(), rule.getSubId(), lang);
        }
        testDisambiguationRulesFromXML(rules, lt, lang);
        long endTime = System.currentTimeMillis();
        System.out.println(rules.size() + " rules tested (" + (endTime-startTime) + "ms)");
      }
    }
  }

  private void validateRuleFile(String filePath) throws IOException {
    XMLValidator validator = new XMLValidator();
    try (InputStream stream = this.getClass().getResourceAsStream(filePath)) {
      if (stream != null) {
        validator.validateWithXmlSchema(filePath, getDataBroker().getResourceDir() + "/disambiguation.xsd");
      }
    }
  }

  private static String sortForms(String wordForms) {
    if (",[,]".equals(wordForms)) {
      return wordForms;
    }
    if (wordForms.length()==0) {
      return wordForms;
    }
    String word = wordForms.substring(0, wordForms.indexOf('[') + 1);
    
    String forms = wordForms.substring(wordForms.indexOf('[') + 1, wordForms.length() -1);
    String[] formToSort = forms.split(",");
    Arrays.sort(formToSort);
    return word + String.join(",", Arrays.asList(formToSort)) + "]";
  }

  private void testDisambiguationRulesFromXML(List<DisambiguationPatternRule> rules, JLanguageTool lt, Language lang) throws IOException {
    int i = 0;
    for (DisambiguationPatternRule rule : rules) {
      if (++i % 100 == 0) {
        System.out.println(i + "...");
      }
      String id = rule.getId();
      if (rule.getUntouchedExamples() != null) {
        List<String> goodSentences = rule.getUntouchedExamples();
        for (String goodSentence : goodSentences) {
          // enable indentation use
          goodSentence = goodSentence.replaceAll("[\\n\\t]+", "");
          goodSentence = cleanXML(goodSentence);

          assertTrue(goodSentence.trim().length() > 0);
          AnalyzedSentence sent = disambiguateUntil(lang, rules, id, lt.getRawAnalyzedSentence(goodSentence));
          AnalyzedSentence sentToReplace = disambiguateUntil(lang, rules, id, lt.getRawAnalyzedSentence(goodSentence));
          //note: we're testing only if string representations are equal
          //it's because getRawAnalyzedSentence does not set all properties
          //in AnalyzedSentence, and during equal test they are set for the
          //left-hand side
          assertEquals("The untouched example (" + goodSentence + ") for " + lang.getName() +
              " rule " + rule + "] was touched!",
              sent.toString(), rule.replace(sentToReplace).toString());
        }
      }
      List<DisambiguatedExample> examples = rule.getExamples();
      if (examples != null) {
        for (DisambiguatedExample example : examples) {

          String outputForms = example.getDisambiguated();
          assertTrue("No output form found for: " + id, outputForms != null);
          assertTrue("Output form must not be empty", outputForms.trim().length() > 0);
          int expectedMatchStart = example.getExample().indexOf("<marker>");
          int expectedMatchEnd = example.getExample().indexOf("</marker>") - "<marker>".length();
          if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
            fail(lang
                + ": No position markup ('<marker>...</marker>') in disambiguated example in rule " + rule);
          }
          String inputForms = example.getAmbiguous();
          assertTrue("No input form found for: " + id, inputForms != null);
          assertTrue(inputForms.trim().length() > 0);
          assertTrue("Input and output forms for rule " + id + " are the same!",
              !outputForms.equals(inputForms));
          AnalyzedSentence cleanInput = lt.getRawAnalyzedSentence(cleanXML(example.getExample()));
          AnalyzedSentence sent = disambiguateUntil(lang, rules, id, lt.getRawAnalyzedSentence(cleanXML(example.getExample())));
          AnalyzedSentence disambiguatedSent = rule.replace(disambiguateUntil(lang, rules, id,
                  lt.getRawAnalyzedSentence(cleanXML(example.getExample()))));
          assertTrue(
              "Disambiguated sentence is equal to the non-disambiguated sentence for rule: "
                  + id + ". The sentence was: " + sent, !cleanInput.equals(disambiguatedSent));
          assertTrue(
              "Disambiguated sentence is equal to the input sentence for rule: "
                  + id + ". The sentence was: " + sent, !sent.equals(disambiguatedSent));
          String reading = "";
          String annotations = "";
          for (AnalyzedTokenReadings readings : sent.getTokens()) {
            if (readings.isSentenceStart() && !inputForms.contains("<S>")) {
              continue;
            }
            if (readings.getStartPos() == expectedMatchStart) {
              AnalyzedTokenReadings[] r = { readings };
              reading = new AnalyzedSentence(r).toShortString(",");
              annotations = readings.getHistoricalAnnotations();
              int startPos = readings.getStartPos();
              int endPos = readings.getEndPos();
              assertTrue(
                  "Wrong marker position in the example for the rule " + id +
                  ": got " + startPos + "-" + endPos + ", expected " + expectedMatchStart + "-" + expectedMatchEnd + ". Sentence: '" + sent + "'",
                  startPos == expectedMatchStart && endPos == expectedMatchEnd);
              break;
            }
          }
          assertEquals("The input form for the rule " + id + " in the example: "
              + example + " is different than expected (expected "
              + inputForms + " but got " + sortForms(reading) + "). The token has been changed by the disambiguator: " + annotations,
              inputForms, sortForms(reading));
          for (AnalyzedTokenReadings readings : disambiguatedSent.getTokens()) {
            if (readings.isSentenceStart() && !outputForms.contains("<S>")) {
              continue;
            }
            if (readings.getStartPos() == expectedMatchStart) {
              AnalyzedTokenReadings[] r = { readings };
              reading = new AnalyzedSentence(r).toShortString(",");
              assertTrue(readings.getStartPos() == expectedMatchStart
                  && readings.getEndPos() == expectedMatchEnd);
              break;
            }
          }
          assertEquals("The output form for the rule " + id + " in the example: "
              + example + " is different than expected (expected "
              + outputForms + " but got " + sortForms(reading) + "). The token has been changed by the disambiguator: " + annotations,
              outputForms, sortForms(reading));
        }
      }
    }
  }

  // useful for testing the rule cascade
  private AnalyzedSentence disambiguateUntil(
      Language lang, List<DisambiguationPatternRule> rules, String ruleID,
      AnalyzedSentence sentence) throws IOException {
    AnalyzedSentence disambiguated = sentence;
    disambiguated = lang.getDisambiguator().preDisambiguate(disambiguated);
    for (DisambiguationPatternRule rule : rules) {
      if (ruleID.equals(rule.getId())) {
        break;
      }
      disambiguated = rule.replace(disambiguated);
    }
    return disambiguated;
  }

  private static String cleanXML(String str) {
    return str.replaceAll("<.*?>", "");
  }

  /**
   * Test XML patterns, as a help for people developing rules that are not
   * programmers.
   */
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    DisambiguationRuleTest test = new DisambiguationRuleTest();
    System.out.println("Running disambiguator rule tests...");
    if (args.length == 0) {
      test.testDisambiguationRulesFromXML(null);
    } else {
      Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      test.testDisambiguationRulesFromXML(ignoredLanguages);
    }
    System.out.println("Disambiguator tests successful.");
  }

}
