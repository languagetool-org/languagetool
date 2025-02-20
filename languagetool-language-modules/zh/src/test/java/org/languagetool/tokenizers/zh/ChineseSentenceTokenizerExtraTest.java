package org.languagetool.tokenizers.zh;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class ChineseSentenceTokenizerExtraTest {

  private final ChineseSentenceTokenizer tokenizer = new ChineseSentenceTokenizer();

  /**
   * Test tokenization with an empty string.
   * Expected output: an empty list.
   */
  @Test
  public void testTokenizeEmptyString() {
    List<String> tokens = tokenizer.tokenize("");
    assertTrue("Empty input should return an empty list", tokens.isEmpty());
  }

  /**
   * Test tokenization with input containing only whitespace.
   * Expected output: a single token that is the whitespace string.
   */
  @Test
  public void testTokenizeOnlyWhitespace() {
    String input = "   \n\t";
    List<String> tokens = tokenizer.tokenize(input);
    // The implementation collects all whitespace and adds it as one token.
    assertEquals("Only whitespace should yield one token", 1, tokens.size());
    assertEquals("   \n\t", tokens.get(0));
  }

  /**
   * Test tokenization with input that contains no whitespace.
   * Expected output: a single token containing the full input.
   */
  @Test
  public void testTokenizeNoWhitespace() {
    // For Chinese text without whitespace, SentencesUtil.toSentenceList should return the full text as one sentence.
    String input = "我爱编程。";
    List<String> tokens = tokenizer.tokenize(input);
    assertNotNull("Non-whitespace input should not be null", tokens);
    assertEquals("Input with no whitespace should return one sentence", 1, tokens.size());
    assertEquals("我爱编程。", tokens.get(0));
  }

  /**
   * Test tokenization with mixed whitespace and non-whitespace segments.
   * Expected output: separate tokens for the text segments and the whitespace in between.
   */
  @Test
  public void testTokenizeMixedWhitespace() {
    String input = "我爱编程   我爱测试";
    List<String> tokens = tokenizer.tokenize(input);
    // Expected tokens: "我爱编程", "   ", "我爱测试"
    assertNotNull(tokens);
    assertEquals("Mixed input should yield three tokens", 3, tokens.size());
    assertEquals("我爱编程", tokens.get(0));
    assertEquals("   ", tokens.get(1));
    assertEquals("我爱测试", tokens.get(2));
  }

  /**
   * Test tokenization with multiple alternating whitespace and non-whitespace segments.
   * Example: "测试 123 测试" should be split into: ["测试", " ", "123", " ", "测试"]
   */
  @Test
  public void testTokenizeAlternatingSegments() {
    String input = "测试 123 测试";
    List<String> tokens = tokenizer.tokenize(input);
    assertNotNull(tokens);
    assertEquals("Input should yield five tokens", 5, tokens.size());
    assertEquals("测试", tokens.get(0));
    assertEquals(" ", tokens.get(1));
    assertEquals("123", tokens.get(2));
    assertEquals(" ", tokens.get(3));
    assertEquals("测试", tokens.get(4));
  }

  /**
   * Test the behavior of setSingleLineBreaksMarksParagraph.
   * Since this method has no effect for Chinese, it should not throw any exception.
   */
  @Test
  public void testSetSingleLineBreaksMarksParagraph() {
    // Call with different values to ensure no exceptions are thrown.
    tokenizer.setSingleLineBreaksMarksParagraph(true);
    tokenizer.setSingleLineBreaksMarksParagraph(false);
  }

  /**
   * Test the singleLineBreaksMarksPara method.
   * Expected output: always false for ChineseSentenceTokenizer.
   */
  @Test
  public void testSingleLineBreaksMarksPara() {
    assertFalse("singleLineBreaksMarksPara should always return false", tokenizer.singleLineBreaksMarksPara());
  }
}
