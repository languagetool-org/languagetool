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

package org.languagetool.tokenizers.ja;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class JapaneseWordTokenizerTest {

  @Test
  public void testTokenize() {
    JapaneseWordTokenizer w = new JapaneseWordTokenizer();
    List<String> testList = w.tokenize("これはペンです。");
    assertEquals(testList.size(), 5);
    assertEquals("[これ 名詞-代名詞-一般 これ, は 助詞-係助詞 は, ペン 名詞-一般 ペン, です 助動詞 です, 。 記号-句点 。]", testList.toString());
    testList = w.tokenize("私は「うん、そうだ」と答えた。");
    assertEquals(testList.size(), 12);
    assertEquals("[私 名詞-代名詞-一般 私, は 助詞-係助詞 は, 「 記号-括弧開 「, うん 感動詞 うん, 、 記号-読点 、, そう 副詞-助詞類接続 そう, だ 助動詞 だ, 」 記号-括弧閉 」, と 助詞-格助詞-引用 と, 答え 動詞-自立 答える, た 助動詞 た, 。 記号-句点 。]", testList.toString());
  }
}
