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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.patterns.PatternRule;

public class RuleTest extends TestCase {

  public void testJavaRuleIds() throws IOException {
    final Set<String> ids = new HashSet<>();
    final Set<Class> ruleClasses = new HashSet<>();
    if (Language.LANGUAGES.length <= 1) {
      System.err.println("***************************************************************************");
      System.err.println("WARNING: found only these languages - the tests might not be complete:");
      System.err.println(Arrays.toString(Language.LANGUAGES));
      System.err.println("***************************************************************************");
    }
    for (Language language : Language.LANGUAGES) {
      final JLanguageTool lt = new JLanguageTool(language);
      final List<Rule> allRules = lt.getAllRules();
      for (Rule rule : allRules) {
        assertIdUniqueness(ids, ruleClasses, language, rule);
        assertIdValidity(language, rule);
        if (rule instanceof PatternRule) {
          // doesn't make sense, but that's the way it works for PatternRules
          // and it's documented that it always returns false:
          assertFalse(rule.supportsLanguage(language));
        } else {
          assertTrue(rule.supportsLanguage(language));
        }
      }
    }
  }

  private void assertIdUniqueness(Set<String> ids, Set<Class> ruleClasses, Language language, Rule rule) {
    final String ruleId = rule.getId();
    if (ids.contains(ruleId) && !ruleClasses.contains(rule.getClass())) {
      throw new RuntimeException("Rule id occurs more than once: '" + ruleId + "', language: " + language);
    }
    ids.add(ruleId);
    ruleClasses.add(rule.getClass());
  }

  private void assertIdValidity(Language language, Rule rule) {
    final String ruleId = rule.getId();
    if (!ruleId.matches("^[A-Z_]+$")) {
      throw new RuntimeException("Invalid character in rule id: '" + ruleId + "', language: "
              + language + ", only [A-Z_] are allowed");
    }
  }

}
