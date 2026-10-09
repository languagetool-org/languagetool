package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class UrlCapitalizationTest {

  private final JLanguageTool langTool = new JLanguageTool(AmericanEnglish.getInstance());

  @Test
  public void testHttpsUrlDoesNotTriggerCapitalization() throws IOException {
    String text = "See more here: https://github.com/languagetool-org/languagetool.";
    assertEquals(0, langTool.check(text).size());
  }

  @Test
  public void testHttpUrlDoesNotTriggerCapitalization() throws IOException {
    String text = "Open http://example.com for details.";
    assertEquals(0, langTool.check(text).size());
  }

  @Test
  public void testWwwUrlDoesNotTriggerCapitalization() throws IOException {
    String text = "Visit www.example.com for documentation.";
    assertEquals(0, langTool.check(text).size());
  }

  @Test
  public void testUrlAtSentenceStartDoesNotTriggerCapitalization() throws IOException {
    String text = "https://example.com is the official site.";
    assertEquals(0, langTool.check(text).size());
  }

  @Test
  public void testUrlWithTrailingPunctuationDoesNotTriggerCapitalization() throws IOException {
    String text = "Read more at https://example.com/docs, then continue.";
    assertEquals(0, langTool.check(text).size());
  }

  @Test
  public void testRegularCapitalizationErrorStillTriggers() throws IOException {
    String text = "the united states is large.";
    // We expect at least one capitalization rule to fire here
    int matches = langTool.check(text).size();
    assertTrue(matches > 0);
  }
}
