package org.languagetool.tagging.zh;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.tokenizers.zh.SChineseWordTokenizer;

import java.io.IOException;

/**
 * The test of ChineseTagger.
 *
 * @author Minshan Chen
 * @author Xiaohui Wu
 * @author Jiamin Zheng
 * @author Zihao Li
 */
public class SChineseTaggerTest {

  private final SChineseTagger tagger = new SChineseTagger();
  private final SChineseWordTokenizer stokenizer = new SChineseWordTokenizer();

  @Test
  public void testTagger() throws IOException {

    TestTools.myAssert(
            "主任强调指出错误的地方。",
            "主任/[null]n -- 强调指出/[null]v -- 错误/[null]a -- 的/[null]u -- 地方/[null]n -- 。/[null]w",
            stokenizer, tagger);

    TestTools.myAssert(
            "她胸前挂着一块碧绿的玉。",
            "她/[null]r -- 胸前/[null]s -- 挂/[null]v -- 着/[null]u -- 一/[null]m -- 块/[null]q -- 碧绿/[null]z -- 的/[null]u -- 玉/[null]n -- 。/[null]w",
            stokenizer, tagger);

    TestTools.myAssert(
            "“鲯鳅”的研究结果有什么奥妙？",
            "“/[null]w -- 鲯鳅/[null]n -- ”/[null]w -- 的/[null]u -- 研究/[null]vn -- 结果/[null]d -- 有/[null]v -- 什么/[null]r -- 奥妙/[null]an -- ？/[null]w",
            stokenizer, tagger);

    TestTools.myAssert(
            "我们的女组长真是百尺竿头更进一步。",
            "我们/[null]r -- 的/[null]u -- 女/[null]b -- 组长/[null]n -- 真是/[null]d -- 百尺竿头更进一步/[null]l -- 。/[null]w",
            stokenizer, tagger);

  }

  /** Test of Traditonal Chinses sentences. */
  @Test
  public void testTagger2() throws IOException {

    TestTools.myAssert("下個星期，我跟我朋唷打算去法國玩兒。",
            "下/[null]f -- 個/[null]q -- 星期/[null]n -- ，/[null]w -- 我/[null]r -- 跟/[null]p -- 我/[null]r -- 朋唷/[null]d -- 打算/[null]v -- 去/[null]v -- 法國/[null]ns -- 玩兒/[null]v -- 。/[null]w",
            stokenizer,tagger);
  }
}
