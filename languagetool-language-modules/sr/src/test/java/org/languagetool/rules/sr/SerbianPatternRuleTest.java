package org.languagetool.rules.sr;

import org.junit.Test;
import org.languagetool.rules.patterns.PatternRuleTest;

import java.io.IOException;

public class SerbianPatternRuleTest extends PatternRuleTest {

  @Test
  public void testRules() throws IOException {
    runGrammarRulesFromXmlTest();
  }
}
