package org.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.languagetool.JLanguageTool;
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
    assertEquals(2, categories.size());
    assertTrue(categories.contains("misc"));
    assertTrue(categories.contains("otherCategory"));
  }

  private Set<String> getCategoryNames(List<PatternRule> rules) {
    final Set<String> categories = new HashSet<String>();
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
    final List<Rule> result = new ArrayList<Rule>();
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        result.add(rule);
      }
    }
    return result;
  }

}
