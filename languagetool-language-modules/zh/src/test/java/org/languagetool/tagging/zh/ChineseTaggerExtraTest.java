package org.languagetool.tagging.zh;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.zh.ChineseWordTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ChineseTaggerExtraTest {

  private ChineseTagger tagger;
  private ChineseWordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new ChineseTagger();
    tokenizer = new ChineseWordTokenizer();
  }

  /**
   * Test the createNullToken method.
   * Input: token "测试" with startPos = 5.
   * Expected: An AnalyzedTokenReadings with token "测试" and startPos equal to 5.
   */
  @Test
  public void testCreateNullToken() {
    AnalyzedTokenReadings nullToken = tagger.createNullToken("测试", 5);
    assertNotNull(nullToken);
    assertEquals("测试", nullToken.getReadings().get(0).getToken());
    assertEquals(5, nullToken.getStartPos());
  }

  /**
   * Test the createToken method.
   * Input: token "测试" and posTag "n".
   * Expected: An AnalyzedToken with token "测试" and pos tag "n".
   */
  @Test
  public void testCreateToken() {
    AnalyzedToken token = tagger.createToken("测试", "n");
    assertNotNull(token);
    assertEquals("测试", token.getToken());
    assertEquals("n", token.getPOSTag());
  }

  /**
   * Test the tag method with multiple tokens to indirectly test asAnalyzedToken.
   * This covers:
   * - Tokens that do not contain "/" should yield a default token (" ").
   * - Tokens in the format "测试/n" should be split into token "测试" with pos tag "n".
   * - Tokens satisfying the special condition (e.g., "/abc/w") should trigger the special branch.
   */
  @Test
  public void testTagMethodWithMultipleTokens() throws IOException {
    List<String> tokens = Arrays.asList("测试/n", "不含斜杠", "/abc/w");
    List<AnalyzedTokenReadings> readings = tagger.tag(tokens);

    // Case 1: "测试/n" should be split into "测试" and pos tag "n"
    AnalyzedToken token1 = readings.get(0).getReadings().get(0);
    assertEquals("测试", token1.getToken());
    assertEquals("n", token1.getPOSTag());

    // Case 2: "不含斜杠" does not contain "/" so asAnalyzedToken returns " "
    AnalyzedToken token2 = readings.get(1).getReadings().get(0);
    assertEquals(" ", token2.getToken());

    // Case 3: "/abc/w" triggers special branch:
    // For input "/abc/w" (length=6), expected token is word.substring(0, word.length()-2) = "/abc"
    // and pos tag is word.substring(word.length()-1, word.length()) = "w"
    AnalyzedToken token3 = readings.get(2).getReadings().get(0);
    assertEquals("/abc", token3.getToken());
    assertEquals("w", token3.getPOSTag());
  }

  /**
   * Test that the tag method correctly accumulates start positions.
   * For two tokens, the start position of the second token should equal the length of the first token.
   */
  @Test
  public void testTagPositionCalculation() throws IOException {
    // "测试/n" produces token "测试" which is 2 characters in Chinese.
    List<String> tokens = Arrays.asList("测试/n", "中文/n");
    List<AnalyzedTokenReadings> readings = tagger.tag(tokens);
    int pos1 = readings.get(0).getStartPos();
    int pos2 = readings.get(1).getStartPos();
    // Expected: pos1 == 0 and pos2 == length of "测试" (which is 2)
    assertEquals(0, pos1);
    assertEquals(2, pos2);
  }
}
