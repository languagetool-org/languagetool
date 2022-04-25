/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Before;
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
 */
public class ChineseTaggerTest {

  private ChineseTagger tagger;
  private ChineseWordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new ChineseTagger();
    tokenizer = new ChineseWordTokenizer();
  }

  @Test
  public void testTagger() throws IOException {

    TestTools.myAssert(
      "主任强调指出错误的地方。",
      "主任/[null]n -- 强调/[null]v -- 指出/[null]v -- 错误/[null]n -- 的/[null]uj -- 地方/[null]n -- 。/[null]w",
      tokenizer, tagger);

    TestTools.myAssert(
      "她胸前挂着一块碧绿的玉。",
      "她/[null]r -- 胸前/[null]s -- 挂/[null]v -- 着/[null]uz -- 一块/[null]s -- 碧绿/[null]z -- 的/[null]uj -- 玉/[null]n -- 。/[null]w",
      tokenizer, tagger);

    TestTools.myAssert(
      "“鲯鳅”的研究结果有什么奥妙？",
      "“/[null]w -- 鲯/[null]n -- 鳅/[null]x -- ”/[null]w -- 的/[null]uj -- 研究/[null]vn -- 结果/[null]n -- 有/[null]v -- 什么/[null]r -- 奥妙/[null]n -- ？/[null]w",
      tokenizer, tagger);

    TestTools.myAssert(
      "我们的女组长真是尺竿头更进一步。",
      "我们/[null]r -- 的/[null]uj -- 女/[null]b -- 组长/[null]n -- 真是/[null]d -- 尺/[null]q -- 竿/[null]ng -- 头/[null]n -- 更进一步/[null]l -- 。/[null]w",
      tokenizer, tagger);

    TestTools.myAssert(
      "国务院，非国家工作人员不能随便进去的地方。",
      "国务院/[null]nt -- ，/[null]w -- 非/[null]h -- 国家/[null]n -- 工作/[null]vn -- 人员/[null]n -- 不能/[null]v -- 随便/[null]ad -- 进去/[null]v -- 的/[null]uj -- 地方/[null]n -- 。/[null]w",
      tokenizer, tagger);

    TestTools.myAssert(
      "“哇……”珠海北师大操场上的师生大吃一惊！",
      "“/[null]w -- 哇/[null]o -- ……/[null]w -- ”/[null]w -- 珠海/[null]ns -- 北师大/[null]j -- 操场/[null]n -- 上/[null]f -- 的/[null]uj -- 师生/[null]n -- 大吃一惊/[null]l -- ！/[null]w",
      tokenizer, tagger);

    TestTools.myAssert(
      "在炎热的暑假里，我和其他同学们参加了姜老师的一个项目。",
      "在/[null]p -- 炎热/[null]a -- 的/[null]uj -- 暑假/[null]t -- 里/[null]f -- ，/[null]w -- 我/[null]r -- 和/[null]c -- 其他/[null]r -- 同学/[null]n -- 们/[null]k -- 参加/[null]v -- 了/[null]ul -- 姜/[null]n -- 老师/[null]n -- 的/[null]uj -- 一个/[null]mq -- 项目/[null]n -- 。/[null]w",
      tokenizer, tagger);

    TestTools.myAssert(
      "“咕咚，”一台联想ThinkPad T系列电脑从关羽的宿舍飞了下来。",
      "“/[null]w -- 咕咚/[null]o -- ，/[null]w -- ”/[null]w -- 一/[null]m -- 台/[null]q -- 联想/[null]nz -- ThinkPad/[null]nx --  /[null]w -- T/[null]nx -- 系列/[null]q -- 电脑/[null]n -- 从/[null]p -- 关/[null]v -- 羽/[null]q -- 的/[null]uj -- 宿舍/[null]n -- 飞/[null]v -- 了/[null]ul -- 下来/[null]v -- 。/[null]w",
      tokenizer, tagger);
  }
}