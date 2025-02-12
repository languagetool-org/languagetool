package org.languagetool.tokenizers.en;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class EnglishSentenceTokenizerTest {

  private final EnglishWordTokenizer tokenizer = new EnglishWordTokenizer();

  /**
   * Test the tokenization of basic sentences:
   * input："The quick brown fox jumps over the lazy dog."
   * Expected output：["The", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog", "."]
   */
  @Test
  public void testBasicSentenceTokenization() {
    String input = "The quick brown fox jumps over the lazy dog.";
    List<String> actualTokens = tokenizer.tokenize(input);
    List<String> expectedTokens = Arrays.asList(
            "The", " ", "quick", " ", "brown", " ", "fox", " ", "jumps", " ", "over", " ", "the", " ", "lazy", " ", "dog", "."
    );
    assertEquals("Basic sentence tokenization should match expected tokens", expectedTokens, actualTokens);
  }

  /**
   * Test a sentence with punctuation：
   * input："Hello, world! How are you?"
   * Expected output：["Hello", ",", "world", "!", "How", "are", "you", "?"]
   */
  @Test
  public void testPunctuationTokenization() {
    String input = "Hello, world! How are you?";
    List<String> actualTokens = tokenizer.tokenize(input);
    List<String> expectedTokens = Arrays.asList("Hello", ",", " ", "world", "!", " ", "How", " ", "are", " ", "you", "?");
    assertEquals("Punctuation should be tokenized separately", expectedTokens, actualTokens);
  }

  /**
   *  Test case for handling abbreviations and hyphenated words:：
   * input："Mr. Smith's well-known speech."
   * Expected output：According to the code logic, it should be split into ["Mr.", "Smith's", "well-known", "speech", "."]
   */
  @Test
  public void testAbbreviationAndHyphenTokenization() {
    String input = "Mr. Smith's well-known speech.";
    List<String> actualTokens = tokenizer.tokenize(input);
    List<String> expectedTokens = Arrays.asList(
            "Mr", ".", " ", "Smith", "'s", " ", "well-known", " ", "speech", "."
    );
    assertEquals("Abbreviations and hyphenated words should be tokenized correctly",
      expectedTokens, actualTokens);
  }

  /**
   * Test for handling a trailing hyphen in a word：
   * input："hello-"
   * Expected output：["hello", "-"]
   * Explanation: According to the wordsToAdd method, the trailing hyphen is removed and then re-appended as a separate token.
   */
  @Test
  public void testTrailingHyphenTokenization() {
    String input = "hello-";
    List<String> actualTokens = tokenizer.tokenize(input);
    List<String> expectedTokens = Arrays.asList("hello", "-");
    assertEquals("Trailing hyphen should be tokenized as a separate token", expectedTokens, actualTokens);
  }

  /**
   * Test for handling empty strings：
   * input："" and "   "（only spaces）
   * Expected output：An empty token list (or a reasonable output according to the design, but no exceptions should be thrown)
   */
  @Test
  public void testEmptyInputTokenization() {
    String input = "";
    List<String> actualTokens = tokenizer.tokenize(input);
    assertTrue("Empty input should yield an empty token list", actualTokens.isEmpty());

    input = "   ";
    actualTokens = tokenizer.tokenize(input);
    // According to the design, spaces may not generate valid tokens, so the expected result is an empty list.
    List<String> expectedTokens = Arrays.asList(" ", " ", " ");
    assertEquals("Input with only spaces should yield an empty token list", expectedTokens, actualTokens);
  }

  /**
   * Test tokenization with special symbols and punctuation.
   * input: "$100 is 100€."
   * Expected output: ["$100", " ", "is", " ", "100€", "."]
   */
  @Test
  public void testSpecialSymbolsTokenization() {
    String input = "$100 is 100€.";
    List<String> actualTokens = tokenizer.tokenize(input);
    List<String> expectedTokens = Arrays.asList("$100", " ", "is", " ", "100€", ".");
    assertEquals("Special symbols should be tokenized correctly", expectedTokens, actualTokens);
  }

  /**
   * Test tokenization with Unicode accented words.
   * input: "Café naïve."
   * Expected output: ["Café", " ", "naïve", "."]
   */
  @Test
  public void testUnicodeAccentedWordsTokenization() {
    String input = "Café naïve.";
    List<String> actualTokens = tokenizer.tokenize(input);
    List<String> expectedTokens = Arrays.asList("Café", " ", "naïve", ".");
    assertEquals("Accented words should be tokenized correctly", expectedTokens, actualTokens);
  }

  /**
   * Test tokenization with combined punctuation.
   * input: "Really?!!"
   * Expected output: ["Really", "?", "!", "!"]
   */
  @Test
  public void testCombinedPunctuationTokenization() {
    String input = "Really?!!";
    List<String> actualTokens = tokenizer.tokenize(input);
    List<String> expectedTokens = Arrays.asList("Really", "?", "!", "!");
    assertEquals("Combined punctuation should be tokenized separately", expectedTokens, actualTokens);
  }
}
