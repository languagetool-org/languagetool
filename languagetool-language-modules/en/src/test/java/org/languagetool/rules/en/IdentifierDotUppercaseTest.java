package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class IdentifierDotUppercaseTest {

  private final JLanguageTool langTool = new JLanguageTool(AmericanEnglish.getInstance());

  @Test
  public void testShouldNotFlagConstantIdentifier() throws IOException {
    String text = "The returned value is VirtualFileFilter.NONE in this case.";
    List<RuleMatch> matches = langTool.check(text);

    // Expect: NO capitalization suggestion inside identifier
    assertTrue(
      "Expected no matches for identifier VirtualFileFilter.NONE, but got: " + matches,
      matches.stream().noneMatch(m -> m.getMessage().contains("uppercase"))
    );
  }

  @Test
  public void testShouldStillFlagLowercaseStartAfterPeriod() throws IOException {
    String text = "Everything works.now we wait.";
    List<RuleMatch> matches = langTool.check(text);

    assertTrue(
      "Expected rule to complain about lowercase 'now' after period",
      matches.stream().anyMatch(m -> m.getMessage().contains("uppercase"))
    );
  }

  @Test
  public void testShouldNotFlagOtherDotConstantPatterns() throws IOException {
    String text = "The result is HttpStatus.OK and everything is fine.";
    List<RuleMatch> matches = langTool.check(text);

    assertTrue(
      matches.stream().anyMatch(m -> m.getMessage().toLowerCase().contains("space"))
    );
  }

  @Test
  public void testShouldStillFlagRealSentenceError() throws IOException {
    String text = "LanguageTool checks grammar.just paste your text here.";
    List<RuleMatch> matches = langTool.check(text);

    assertTrue(
      "Expected capitalization warning after period but found none",
      matches.stream().anyMatch(m -> m.getRule().getId().equals("LC_AFTER_PERIOD"))
    );
  }

}
