/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.languagetool.tokenizers.zh;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * The test of ChineseWordTokenizer.
 *
 * @author Minshan Chen
 * @author Xiaohui Wu
 * @author Jiamin Zheng
 * @author Zihao Li
 * @author Ze Dang
 */
public class ChineseWordTokenizerTest {

  private ChineseWordTokenizer wordTokenizer = new ChineseWordTokenizer();

  /** Tests of Simplified Chinese*/
  @Test
  public void testTokenize() {

    List<String> tokens = wordTokenizer.tokenize("主任强调指出错误的地方。");
    assertEquals(tokens.size(), 6);
    assertEquals("[主任/n, 强调指出/v, 错误/a, 的/u, 地方/n, 。/w]",
            tokens.toString());

    List<String> tokens2 = wordTokenizer.tokenize("她胸前挂着一块碧绿的玉。");
    assertEquals(tokens2.size(), 10);
    assertEquals("[她/r, 胸前/s, 挂/v, 着/u, 一/m, 块/q, 碧绿/z, 的/u, 玉/n, 。/w]",
            tokens2.toString());

    List<String> tokens3 = wordTokenizer.tokenize("“鲯鳅”的研究结果有什么奥妙？");
    assertEquals(tokens3.size(), 10);
    assertEquals(
            "[“/w, 鲯鳅/n, ”/w, 的/u, 研究/vn, 结果/d, 有/v, 什么/r, 奥妙/an, ？/w]",
            tokens3.toString());

    List<String> tokens4 = wordTokenizer.tokenize("我们的女组长真是百尺竿头更进一步。");
    assertEquals(tokens4.size(), 7);
    assertEquals(
            "[我们/r, 的/u, 女/b, 组长/n, 真是/d, 百尺竿头更进一步/l, 。/w]",
            tokens4.toString());

  }

  /** Tests of Traditional Chinese*/
  @Test
  public void testTokenizer2() {

    List<String> tokens = wordTokenizer.tokenize("喬丹所屬的北卡大學對上喬治城大學。");
    assertEquals(tokens.size(), 8);
    assertEquals("[喬丹/nr, 所屬/n, 的/u, 北卡大學/nt, 對/p, 上/f, 喬治城大學/nt, 。/w]",
            tokens.toString());
  }


}
