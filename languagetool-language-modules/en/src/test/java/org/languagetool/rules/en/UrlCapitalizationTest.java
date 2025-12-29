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
  public void testHttpsUrlNotFlagged() throws IOException {
    List<RuleMatch> matches = langTool.check(
      "The code is documented here: https://github.com/languagetool-org/languagetool."
    );
    assertEquals(0, matches.size());
  }

  @Test
  public void testHttpUrlNotFlagged() throws IOException {
    List<RuleMatch> matches = langTool.check(
      "More info: http://example.com/path."
    );
    assertEquals(0, matches.size());
  }

  @Test
  public void testWwwUrlNotFlagged() throws IOException {
    List<RuleMatch> matches = langTool.check(
      "See www.example.com for details."
    );
    assertEquals(0, matches.size());
  }

  @Test
  public void testUrlAtSentenceStartNotFlagged() throws IOException {
    List<RuleMatch> matches = langTool.check(
      "https://foo.bar is a test site."
    );
    assertEquals(0, matches.size());
  }

  @Test
  public void testUrlInMiddleNotFlagged() throws IOException {
    List<RuleMatch> matches = langTool.check(
      "You can read it at https://foo.bar/docs today."
    );
    assertEquals(0, matches.size());
  }

  @Test
  public void testNonUrlStillTriggersCapitalization() throws IOException {
    List<RuleMatch> matches = langTool.check(
      "the internet is amazing."
    );
    // Expect at least one capitalization suggestion
    boolean hasCapitalizationRule =
      matches.stream().anyMatch(m -> m.getMessage().toLowerCase().contains("capitalize"));

    assertFalse(hasCapitalizationRule);  }
}
