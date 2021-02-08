package org.languagetool.rules.spelling.hunspell;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class HunspellDigitsTest {

  @Test
  public void test_1_digit_misspelled() throws IOException {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"),
      Languages.getLanguageForShortCode("de-DE"), null);
    JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Viele Grü0ße"));
    assertEquals(1, matches.length);
    assertEquals(6, matches[0].getFromPos());
    assertEquals(12, matches[0].getToPos());
    assertThat(matches[0].getSuggestedReplacements(), hasItem("Grüße"));
  }

  @Test
  public void test_2_more_digits_consecutive_correct() throws IOException {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"),
      Languages.getLanguageForShortCode("de-DE"), null);
    JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("Airbus A320"));
    assertEquals(0, matches.length);
  }

  @Test
  public void test_2_more_digits_nonconsecutive_correct() throws IOException {
    HunspellRule rule = new HunspellRule(TestTools.getMessages("de"),
      Languages.getLanguageForShortCode("de-DE"), null);
    JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("foo1bar9"));
    assertEquals(0, matches.length);
  }
}
