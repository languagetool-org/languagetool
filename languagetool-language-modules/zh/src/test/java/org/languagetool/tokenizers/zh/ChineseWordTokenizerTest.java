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
 */
public class ChineseWordTokenizerTest {

  @Test
  public void testTokenize() {

    ChineseWordTokenizer wordTokenizer = new ChineseWordTokenizer();

    List<String> tokens = wordTokenizer.tokenize("主任强调指出错误的地方。");
    assertEquals(tokens.size(), 7);
    assertEquals("[主任/n, 强调/vd, 指出/v, 错误/a, 的/u, 地方/n, 。/w]",
            tokens.toString());

    List<String> tokens2 = wordTokenizer.tokenize("她胸前挂着一块碧绿的玉。");
    assertEquals(tokens2.size(), 10);
    assertEquals("[她/r, 胸前/s, 挂/v, 着/u, 一/m, 块/q, 碧绿/z, 的/u, 玉/n, 。/w]",
            tokens2.toString());

    List<String> tokens3 = wordTokenizer.tokenize("“鲯鳅”的研究结果有什么奥妙？");
    assertEquals(tokens3.size(), 11);
    assertEquals(
            "[“/w, 鲯/x, 鳅/x, ”/w, 的/u, 研究/vn, 结果/n, 有/v, 什么/r, 奥妙/an, ？/w]",
            tokens3.toString());

    List<String> tokens4 = wordTokenizer.tokenize("我们的女组长真是尺竿头更进一步。");
    assertEquals(tokens4.size(), 11);
    assertEquals(
            "[我们/r, 的/u, 女/b, 组长/n, 真/d, 是/v, 尺/ng, 竿/ng, 头/n, 更进一步/l, 。/w]",
            tokens4.toString());

    List<String> tokens5 = wordTokenizer.tokenize("国务院，非国家工作人员不能随便进去的地方。");
    assertEquals(tokens5.size(), 12);
    assertEquals(
            "[国务院/nt, ，/w, 非/h, 国家/n, 工作/vn, 人员/n, 不能/v, 随便/ad, 进去/v, 的/u, 地方/n, 。/w]",
            tokens5.toString());

    List<String> tokens6 = wordTokenizer.tokenize("“哇……”珠海北师大操场上的师生大吃一惊！");
    assertEquals(tokens6.size(), 13);
    assertEquals(
            "[“/w, 哇/y, …/w, …/w, ”/w, 珠海/ns, 北师大/j, 操场/n, 上/f, 的/u, 师生/n, 大吃一惊/i, ！/w]",
            tokens6.toString());

    List<String> tokens7 = wordTokenizer
            .tokenize("在炎热的暑假里，我和其他同学们参加了姜老师的一个项目。");
    assertEquals(tokens7.size(), 19);
    assertEquals(
            "[在/p, 炎热/a, 的/u, 暑假/t, 里/f, ，/w, 我/r, 和/c, 其他/r, 同学/n, 们/k, 参加/v, 了/u, 姜/n, 老师/n, 的/u, 一个/m, 项目/n, 。/w]",
            tokens7.toString());

    List<String> tokens8 = wordTokenizer
            .tokenize("“咕咚，”一台联想ThinkPad T系列电脑从关羽的宿舍飞了下来。");
    assertEquals(tokens8.size(), 20);
    assertEquals(
            "[“/w, 咕咚/o, ，/w, ”/w, 一/m, 台/q, 联想/nz, ThinkPad/nx, , T/nx, 系列/q, 电脑/n, 从/p, 关羽/nr, 的/u, 宿舍/n, 飞/v, 了/u, 下来/v, 。/w]",
            tokens8.toString());
  }

}
