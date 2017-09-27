package org.languagetool.rules.sr;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Serbian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class SimpleGrammarReplaceRuleTest {

  @Test
  public void testRule() throws IOException {
    SimpleGrammarReplaceRule rule = new SimpleGrammarReplaceRule(TestTools.getEnglishMessages());
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(new Serbian());

    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Данас је диван дан."));
    assertEquals(0, matches.length);

    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Син је вишљи од оца."));
    assertEquals(1, matches.length);
    assertEquals(1, matches[0].getSuggestedReplacements().size());
    assertEquals(Arrays.asList("виши"), matches[0].getSuggestedReplacements());

    matches = rule.match(langTool.getAnalyzedSentence("У то оправдано сумљам."));
    assertEquals(1, matches.length);
    assertEquals(1, matches[0].getSuggestedReplacements().size());
    assertEquals(Arrays.asList("сумњам"), matches[0].getSuggestedReplacements());
  }
}