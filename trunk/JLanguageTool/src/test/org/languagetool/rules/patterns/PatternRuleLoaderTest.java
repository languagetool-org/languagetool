package org.languagetool.rules.patterns;

import java.util.List;

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
  }
  
  private Rule getRuleById(String id, List<PatternRule> rules) {
    for (Rule rule : rules) {
      if (rule.getId().equals(id)) {
        return rule;
      }
    }
    throw new RuntimeException("No rule found for id '" + id + "'");
  }

}
