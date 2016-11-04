/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleTest {

  @Test
  public void testJavaRules() throws IOException {
    Set<String> ids = new HashSet<>();
    Set<Class> ruleClasses = new HashSet<>();
    if (Languages.getWithDemoLanguage().size() <= 1) {
      System.err.println("***************************************************************************");
      System.err.println("WARNING: found only these languages - the tests might not be complete:");
      System.err.println(Languages.getWithDemoLanguage());
      System.err.println("***************************************************************************");
    }
    for (Language language : Languages.getWithDemoLanguage()) {
      JLanguageTool lt = new JLanguageTool(language);
      List<Rule> allRules = lt.getAllRules();
      for (Rule rule : allRules) {
        if (!(rule instanceof AbstractPatternRule)) {
          assertIdUniqueness(ids, ruleClasses, language, rule);
          assertIdValidity(language, rule);
          assertTrue(rule.supportsLanguage(language));
          testExamples(rule, lt);
        }
      }
    }
  }

  private void assertIdUniqueness(Set<String> ids, Set<Class> ruleClasses, Language language, Rule rule) {
    String ruleId = rule.getId();
    if (ids.contains(ruleId) && !ruleClasses.contains(rule.getClass())) {
      throw new RuntimeException("Rule id occurs more than once: '" + ruleId + "', language: " + language);
    }
    ids.add(ruleId);
    ruleClasses.add(rule.getClass());
  }

  private void assertIdValidity(Language language, Rule rule) {
    String ruleId = rule.getId();
    if (!ruleId.matches("^[A-Z_]+$")) {
      throw new RuntimeException("Invalid character in rule id: '" + ruleId + "', language: "
              + language + ", only [A-Z_] are allowed");
    }
  }

  private void testExamples(Rule rule, JLanguageTool lt) throws IOException {
    testCorrectExamples(rule, lt);
    testIncorrectExamples(rule, lt);
  }

  private void testCorrectExamples(Rule rule, JLanguageTool lt) throws IOException {
    List<CorrectExample> correctExamples = rule.getCorrectExamples();
    for (CorrectExample correctExample : correctExamples) {
      String input = cleanMarkers(correctExample.getExample());
      enableOnlyOneRule(lt, rule);
      List<RuleMatch> ruleMatches = lt.check(input);
      assertEquals("Got unexpected rule match for correct example sentence:\n"
              + "Text: " + input + "\n"
              + "Rule: " + rule.getId() + "\n"
              + "Matches: " + ruleMatches, 0, ruleMatches.size());
    }
  }

  private void testIncorrectExamples(Rule rule, JLanguageTool lt) throws IOException {
    List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
    for (IncorrectExample incorrectExample : incorrectExamples) {
      String input = cleanMarkers(incorrectExample.getExample());
      enableOnlyOneRule(lt, rule);
      List<RuleMatch> ruleMatches = lt.check(input);
      assertEquals("Did not get the expected rule match for the incorrect example sentence:\n"
              + "Text: " + input + "\n"
              + "Rule: " + rule.getId() + "\n"
              + "Matches: " + ruleMatches, 1, ruleMatches.size());
    }
  }

  private void enableOnlyOneRule(JLanguageTool lt, Rule ruleToActivate) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    lt.enableRule(ruleToActivate.getId());
  }

  private String cleanMarkers(String example) {
    return example.replace("<marker>", "").replace("</marker>", "");
  }

}
