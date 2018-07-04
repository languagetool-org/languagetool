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

package org.languagetool.tagging.zh;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.tokenizers.zh.ChineseWordTokenizer;

import java.io.IOException;

/**
 * The test of ChineseTagger.
 * 
 * @author Minshan Chen
 * @author Xiaohui Wu
 * @author Jiamin Zheng
 * @author Zihao Li
 * @author Ze Dang
 */
public class ChineseTaggerTest {

  private ChineseTagger tagger = new ChineseTagger();
  private ChineseWordTokenizer tokenizer = new ChineseWordTokenizer();

  @Test
  public void testTagger() throws IOException {

    TestTools.myAssert(
            "主任强调指出错误的地方。",
            "主任/[null]n -- 强调指出/[null]v -- 错误/[null]a -- 的/[null]u -- 地方/[null]n -- 。/[null]w",
            tokenizer, tagger);

    TestTools.myAssert(
            "她胸前挂着一块碧绿的玉。",
            "她/[null]r -- 胸前/[null]s -- 挂/[null]v -- 着/[null]u -- 一/[null]m -- 块/[null]q -- 碧绿/[null]z -- 的/[null]u -- 玉/[null]n -- 。/[null]w",
            tokenizer, tagger);

    TestTools.myAssert(
            "“鲯鳅”的研究结果有什么奥妙？",
            "“/[null]w -- 鲯鳅/[null]n -- ”/[null]w -- 的/[null]u -- 研究/[null]vn -- 结果/[null]d -- 有/[null]v -- 什么/[null]r -- 奥妙/[null]an -- ？/[null]w",
            tokenizer, tagger);

    TestTools.myAssert(
            "我们的女组长真是百尺竿头更进一步。",
            "我们/[null]r -- 的/[null]u -- 女/[null]b -- 组长/[null]n -- 真是/[null]d -- 百尺竿头更进一步/[null]l -- 。/[null]w",
            tokenizer, tagger);

  }
}
