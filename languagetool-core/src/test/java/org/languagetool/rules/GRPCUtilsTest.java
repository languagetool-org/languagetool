package org.languagetool.rules;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;

public class GRPCUtilsTest
{

  @Test
  public void testURLFromRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Demo());
    AnalyzedSentence s = lt.getAnalyzedSentence("This is a test");
    Rule rule = new FakeRule();
    rule.setUrl(new URL("http://example.com/"));
    RuleMatch m = new RuleMatch(rule, s, 0, 1, "test");
    String url = GRPCUtils.toGRPC(m).getUrl();
    assertEquals("http://example.com/", url);
  }

  @Test
  public void testURLFromRuleMatch() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Demo());
    AnalyzedSentence s = lt.getAnalyzedSentence("This is a test");
    Rule rule = new FakeRule();
    rule.setUrl(new URL("http://example.com/wrong"));
    RuleMatch m = new RuleMatch(rule, s, 0, 1, "test");
    m.setUrl(new URL("http://example.com/"));
    String url = GRPCUtils.toGRPC(m).getUrl();
    assertEquals("http://example.com/", url);
  }
}
