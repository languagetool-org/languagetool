/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;

public class PatternRuleLoaderTest extends TestCase {

  public void testGetRules() throws Exception {
    final PatternRuleLoader prg = new PatternRuleLoader();
    final String name = "/xx/grammar.xml";
    final List<PatternRule> rules = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(name), name);
    assertTrue(rules.size() >= 30);

    final Rule demoRule1 = getRuleById("DEMO_RULE", rules);
    assertEquals("http://fake-server.org/foo-bar-error-explained", demoRule1.getUrl().toString());
    assertEquals("[This is <marker>fuu bah</marker>.]", demoRule1.getCorrectExamples().toString());
    final List<IncorrectExample> incorrectExamples = demoRule1.getIncorrectExamples();
    assertEquals(1, incorrectExamples.size());
    assertEquals("This is <marker>foo bar</marker>.", incorrectExamples.get(0).getExample());

    final Rule demoRule2 = getRuleById("API_OUTPUT_TEST_RULE", rules);
    assertNull(demoRule2.getUrl());

    assertEquals("uncategorized", demoRule1.getLocQualityIssueType());
    assertEquals("tag inheritance failed", "addition", getRuleById("TEST_GO", rules).getLocQualityIssueType());
    assertEquals("tag inheritance overwrite failed", "uncategorized", getRuleById("TEST_PHRASES1", rules).getLocQualityIssueType());
    assertEquals("tag inheritance overwrite failed", "characters", getRuleById("test_include", rules).getLocQualityIssueType());

    final List<Rule> groupRules1 = getRulesById("test_spacebefore", rules);
    assertEquals("tag inheritance form category failed", "addition", groupRules1.get(0).getLocQualityIssueType());
    assertEquals("tag inheritance overwrite failed", "duplication", groupRules1.get(1).getLocQualityIssueType());
    final List<Rule> groupRules2 = getRulesById("test_unification_with_negation", rules);
    assertEquals("tag inheritance from rulegroup failed", "grammar", groupRules2.get(0).getLocQualityIssueType());

    final Set<String> categories = getCategoryNames(rules);
    assertEquals(3, categories.size());
    assertTrue(categories.contains("misc"));
    assertTrue(categories.contains("otherCategory"));
    assertTrue(categories.contains("Test tokens with min and max attributes"));

    final PatternRule demoRuleWithChunk = (PatternRule) getRuleById("DEMO_CHUNK_RULE", rules);
    final List<Element> elements = demoRuleWithChunk.getElements();
    assertEquals(2, elements.size());
    assertEquals(null, elements.get(1).getPOStag());
    assertEquals(new ChunkTag("B-NP-singular"), elements.get(1).getChunkTag());
  }

  private Set<String> getCategoryNames(List<PatternRule> rules) {
    final Set<String> categories = new HashSet<>();
    for (PatternRule rule : rules) {
      categories.add(rule.getCategory().getName());
    }
    return categories;
  }

  private Rule getRuleById(String id, List<PatternRule> rules) {
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        return rule;
      }
    }
    throw new RuntimeException("No rule found for id '" + id + "'");
  }

  private List<Rule> getRulesById(String id, List<PatternRule> rules) {
    final List<Rule> result = new ArrayList<>();
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        result.add(rule);
      }
    }
    return result;
  }

}
