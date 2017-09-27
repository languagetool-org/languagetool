package org.languagetool.rules.sr;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Serbian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class SimpleStyleReplaceRuleTest {

  @Test
  public void testRule() throws IOException {
    SimpleStyleReplaceRule rule = new SimpleStyleReplaceRule(TestTools.getEnglishMessages());
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(new Serbian());

    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Он је добар."));
    assertEquals(0, matches.length);

    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Она је дебела."));
    assertEquals(1, matches.length);
    assertEquals(1, matches[0].getSuggestedReplacements().size());
    assertEquals(Arrays.asList("елегантно попуњена"), matches[0].getSuggestedReplacements());
  }
}