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
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleTest {
  
  private final static Map<String, Integer> idToExpectedMatches = new HashMap<>();
  static {
    idToExpectedMatches.put("STYLE_REPEATED_WORD_RULE_DE", 2);
  }

  @Test
  public void testJavaRules() throws IOException {
    Map<String,String> idsToClassName = new HashMap<>();
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
          assertIdUniqueness(idsToClassName, ruleClasses, language, rule);
          assertIdValidity(language, rule);
          assertTrue(rule.supportsLanguage(language));
          testExamples(rule, lt);
        }
      }
    }
  }

  private void assertIdUniqueness(Map<String,String> ids, Set<Class> ruleClasses, Language language, Rule rule) {
    String ruleId = rule.getId();
    if (ids.containsKey(ruleId) && !ruleClasses.contains(rule.getClass())) {
      throw new RuntimeException("Rule id occurs more than once: '" + ruleId + "', one of them " +
              rule.getClass() + ", the other one " + ids.get(ruleId) + ", language: " + language);
    }
    ids.put(ruleId, rule.getClass().getName());
    ruleClasses.add(rule.getClass());
  }

  private void assertIdValidity(Language language, Rule rule) {
    String ruleId = rule.getId();
    if (!ruleId.matches("^[A-Z_][A-Z0-9_]+$")) {
      throw new RuntimeException("Invalid character in rule id: '" + ruleId + "', language: "
              + language + ", only [A-Z0-9_] are allowed and the first character must be in [A-Z_]");
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
              + "Matches: " + ruleMatches, (int)idToExpectedMatches.getOrDefault(rule.getId(), 1), ruleMatches.size());
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
