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

import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
import com.hankcs.hanlp.seg.common.Term;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static junit.framework.TestCase.assertEquals;


/**
 * The test of ChineseWordTokenizer.
 *
 * @author Ze Dang
 */
public class SChineseWordTokenizerTest {

  private SChineseWordTokenizer wordTokenizer = new SChineseWordTokenizer();

  @Test
  public void testWordTokenize() {
    List<String> tokens = wordTokenizer.tokenize("今天，刘志军案的关键人物,山西女商人丁书苗在市二中院出庭受审。");
    assertEquals(16, tokens.size());
    assertEquals("[今天/t, ，/w, 刘志军案/nr, 的/u, 关键人物/n, ,/w, 山西/ns, 女/b, 商人/n, 丁书苗/n, 在/p, 市二/s, 中院/n, 出庭/v, 受审/v, 。/w]",
            tokens.toString());

    List<String> token2 = wordTokenizer.tokenize("克里斯蒂娜·克罗尔说：不，我不是虎妈。我全家都热爱音乐，我也鼓励他们这么做。");
    assertEquals(23, token2.size());
    assertEquals("[克里斯蒂娜·克罗尔/nr, 说/v, ：/w, 不/d, ，/w, 我/r, 不/d, 是/v, 虎妈/n, 。/w, 我/r, 全家/n, 都/d, 热爱/v, 音乐/n, ，/w, 我/r, 也/d, 鼓励/v, 他们/r, 这么/r, 做/v, 。/w]",
            token2.toString());

    List<String> token3 = wordTokenizer.tokenize("商品和服务");
    assertEquals(3, token3.size());
    assertEquals("[商品/n, 和/c, 服务/vn]",
            token3.toString());

    List<String> token4 = wordTokenizer.tokenize("政府不存在有任何隐瞒。");
    assertEquals(7, token4.size());
    assertEquals("[政府/n, 不/d, 存在/v, 有/v, 任何/r, 隐瞒/v, 。/w]",
            token4.toString());
  }

  /** tests of Traditional Chinese*/
  @Test
  public void testWordTokenize2() {
    List<String> token = wordTokenizer.tokenize("政府不存在有任何隱瞞。");
    assertEquals(7, token.size());
    assertEquals("[政府/n, 不/d, 存在/v, 有/v, 任何/r, 隱瞞/v, 。/w]",
            token.toString());
  }

  @Test
  public void demoWordTokenizer() {
    String s = "大家得好老师";
    System.out.println(wordTokenizer.tokenize(s));
  }
}
