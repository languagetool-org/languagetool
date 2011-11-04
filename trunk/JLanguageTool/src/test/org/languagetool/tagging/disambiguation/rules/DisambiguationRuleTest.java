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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import de.danielnaber.languagetool.*;
import junit.framework.TestCase;

import org.xml.sax.SAXException;

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

  public void testDisambiguationRulesFromXML() throws IOException,
      ParserConfigurationException, SAXException {
    testDisambiguationRulesFromXML(null, false);
  }

  private void testDisambiguationRulesFromXML(
      final Set<Language> ignoredLanguages, final boolean verbose)
      throws IOException, ParserConfigurationException, SAXException {
    for (final Language lang : Language.LANGUAGES) {
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      if (verbose) {
        System.out.println("Running tests for " + lang.getName() + "...");
      }
      final DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();
      final JLanguageTool languageTool = new JLanguageTool(lang);
      if (!(languageTool.getLanguage().getDisambiguator() instanceof DemoDisambiguator)
          && !(languageTool.getLanguage().getDisambiguator() instanceof TrimDisambiguator)) {
        final String name = JLanguageTool.getDataBroker().getResourceDir() + "/" + lang.getShortName()
            + "/disambiguation.xml";
        final List<DisambiguationPatternRule> rules = ruleLoader
            .getRules(ruleLoader.getClass().getResourceAsStream(name));
        testDisambiguationRulesFromXML(rules, languageTool, lang);
      }
    }
  }
  
  static String combine(String[] s, String glue) {
    int k=s.length;
    if (k==0)
      return null;
    StringBuilder out=new StringBuilder();
    out.append(s[0]);
    for (int x=1;x<k;++x)
      out.append(glue).append(s[x]);
    return out.toString();
  }

  
  static String sortForms(final String wordForms) {
    if (",[,]".equals(wordForms)) {
      return wordForms;
    }
    String word = wordForms.substring(0, wordForms.indexOf('[') + 1);
    String forms = wordForms.substring(wordForms.indexOf('[')
        + 1, wordForms.length() -1);    
    String[] formToSort = forms.split(",");
    Arrays.sort(formToSort);
    return word + 
    combine(formToSort, ",")
    + "]";   
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
          assertTrue("The untouched example for rule " + id + "was touched!",
              sent.equals(rule.replace(sent)));
        }
      }
      final List<DisambiguatedExample> examples = rule.getExamples();
      if (examples != null) {
        for (final DisambiguatedExample example : examples) {

          final String outputForms = example.getDisambiguated();
          assertTrue("No input form found for: " + id, outputForms != null);
          assertTrue(outputForms.trim().length() > 0);
          final int expectedMatchStart = example.getExample().indexOf(
              "<marker>");
          final int expectedMatchEnd = example.getExample()
              .indexOf("</marker>")
              - "<marker>".length();
          if (expectedMatchStart == -1 || expectedMatchEnd == -1) {
            fail(lang
                + ": No position markup ('<marker>...</marker>') in disambiguated example in rule "
                + rule);
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
              "Disambiguated sentence is equal to the non-disambiguated sentence for rule :"
                  + id, !cleanInput.equals(disambiguatedSent));
          assertTrue(
              "Disambiguated sentence is equal to the input sentence for rule :"
                  + id, !sent.equals(disambiguatedSent));
          String reading = "";
          for (final AnalyzedTokenReadings readings : sent.getTokens()) {
            if (readings.isSentStart() && inputForms.indexOf("<S>") == -1) {
              continue;
            }
            if (readings.getStartPos() == expectedMatchStart) {
              final AnalyzedTokenReadings r[] = { readings };
              reading = new AnalyzedSentence(r).toString();
              assertTrue(
                  "Wrong marker position in the example for the rule " + id,
                  readings.getStartPos() == expectedMatchStart
                      && readings.getStartPos() + readings.getToken().length() == expectedMatchEnd);
              break;
            }
          }
          assertTrue("The input form for the rule " + id + " in the example: "
              + example.toString() + " is different than expected (expected "
              + inputForms + " but got " + sortForms(reading) + ").", sortForms(reading)
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
          assertTrue("The output form for the rule " + id + " in the example: "
              + example.toString() + " is different than expected (expected "
              + outputForms + " but got " + sortForms(reading) + ").", sortForms(reading)
              .equals(outputForms));
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
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
    final DisambiguationRuleTest prt = new DisambiguationRuleTest();
    System.out.println("Running disambiguator rule tests...");
    prt.setUp();
    if (args.length == 0) {
	  prt.testDisambiguationRulesFromXML(null, true);
    } else {
      final Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      prt.testDisambiguationRulesFromXML(ignoredLanguages, true);
    }
    System.out.println("Tests successful.");
  }
  
}
