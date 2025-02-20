package org.languagetool.rules.en;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;
import org.languagetool.language.English;

import java.io.IOException;
import java.util.Arrays;

public class MorfologikAmericanSpellerRuleExtraTest {

  private static final English language = (English) Languages.getLanguageForShortCode("en-US");
  private static MorfologikAmericanSpellerRule rule;
  private static JLanguageTool lt;

  @BeforeClass
  public static void setUp() throws IOException {
    rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language);
    lt = new JLanguageTool(language);
  }

  /**
   * Test handling of null input.
   * Expect a NullPointerException.
   */
  @Test(expected = NullPointerException.class)
  public void testNullInput() throws IOException {
    lt.getAnalyzedSentence(null);
  }

  /**
   * Test numeric-only input.
   * Input: "123456"
   * Expected: No spelling errors.
   */
  @Test
  public void testNumericInput() throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("123456"));
    assertEquals("Numeric input should yield no spelling errors", 0, matches.length);
  }

  /**
   * Test input with multiple spelling errors in one sentence.
   * Input: "He teached and buyed things."
   * Expected: Two errors detected; suggestions include "taught" and "bought".
   */
  @Test
  public void testMultipleErrors() throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("He teached and buyed things."));
    assertEquals("Expected two spelling errors", 2, matches.length);
    boolean hasTaught = Arrays.stream(matches).anyMatch(m -> m.getSuggestedReplacements().contains("taught"));
    boolean hasBought = Arrays.stream(matches).anyMatch(m -> m.getSuggestedReplacements().contains("bought"));
    assertTrue("Expected suggestion 'taught'", hasTaught);
    assertTrue("Expected suggestion 'bought'", hasBought);
  }

  /**
   * Test error position calculation in input with extra spaces.
   * Input: "He    teached  us."
   * Expected: Error on "teached" with correct from/to positions.
   */
  @Test
  public void testErrorPositionWithExtraSpaces() throws IOException {
    String input = "He    teached  us.";
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertEquals("Expected one error due to subject-verb issue", 1, matches.length);
    String errorSubstring = lt.getAnalyzedSentence(input)
      .getText()
      .substring(matches[0].getFromPos(), matches[0].getToPos());
    assertEquals("teached", errorSubstring);
  }

  /**
   * Test input with only punctuation.
   * Input: "!!!"
   * Expected: No errors should be detected.
   */
  @Test
  public void testOnlyPunctuationInput() throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("!!!"));
    assertEquals("Only punctuation should yield no spelling errors", 0, matches.length);
  }

  /**
   * Test input with only whitespace.
   * Input: "     "
   * Expected: No errors should be detected.
   */
  @Test
  public void testWhitespaceOnlyInput() throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("     "));
    assertEquals("Only whitespace should yield no spelling errors", 0, matches.length);
  }

  /**
   * Test input containing Unicode characters and emojis.
   * Input: "I love 🍕 and Café moments."
   * Expected: No errors should be detected.
   */
  @Test
  public void testInputWithUnicodeAndEmoji() throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("I love 🍕 and Café moments."));
    assertEquals("Unicode and emoji input should yield no spelling errors", 0, matches.length);
  }

  /**
   * Test handling of empty string input.
   * Input: ""
   * Expected: No errors should be detected.
   */
  @Test
  public void testEmptyString() throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(""));
    assertEquals("Empty string should yield no errors", 0, matches.length);
  }

  /**
   * Test a possibly misspelled contraction.
   * Input: "I ain't goin to the store."
   * Expected: Depending on rule design, an error might be detected with suggestions.
   */
  @Test
  public void testMisspelledContraction() throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("I ain't goin to the store."));
    // If rule flags the contraction, check for suggestion; adjust expected behavior as per implementation.
    if (matches.length > 0) {
      assertFalse("Suggestion list should not be empty for a contraction error",
        matches[0].getSuggestedReplacements().isEmpty());
    }
  }

  /**
   * Test that proper nouns are ignored.
   * Input: "Peter"
   * Expected: No errors should be detected.
   */
  @Test
  public void testProperNounIgnore() throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Peter"));
    assertEquals("Proper noun should yield no errors", 0, matches.length);
  }
}
