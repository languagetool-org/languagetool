package org.languagetool.rules.en;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class MorfologikDigitsTest {
  @Test
  public void test_1_digit_misspelled() throws IOException {
    RuleMatch[] matches = getMatches("Good Morn0ing", "en-US");
    assertEquals(1, matches.length);
    assertEquals(6, matches[0].getFromPos());
    assertEquals(12, matches[0].getToPos());
    assertThat(matches[0].getSuggestedReplacements(), hasItem("Morning"));
  }

  @Test
  public void test_2_more_digits_consecutive_correct() throws IOException {
    RuleMatch[] matches = getMatches("Airbus A320", "en-US");
    assertEquals(0, matches.length);
  }

  @Test
  public void test_2_more_digits_nonconsecutive_correct() throws IOException {
    RuleMatch[] matches = getMatches("foo1bar9", "en-US");
    assertEquals(0, matches.length);
  }

  private RuleMatch[] getMatches(String inputText, String langCode) throws IOException {
    Rule rule = new MorfologikAmericanSpellerRule(TestTools.getMessages(langCode.substring(0, 2)),
      Languages.getLanguageForShortCode(langCode));
    JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode(langCode));
    return rule.match(langTool.getAnalyzedSentence(inputText));
  }
}
