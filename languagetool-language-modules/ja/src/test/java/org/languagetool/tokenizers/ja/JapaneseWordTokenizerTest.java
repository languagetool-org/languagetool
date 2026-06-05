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
    assertEquals(5, testList.size());
    assertEquals("[これ 名詞 これ, は 助詞 は, ペン 名詞 ペン, です 助動詞 です, 。 記号 。]", testList.toString());
    testList = w.tokenize("私は「うん、そうだ」と答えた。");
    assertEquals(12, testList.size());
    assertEquals("[私 名詞 私, は 助詞 は, 「 記号 「, うん 感動詞 うん, 、 記号 、, そう 副詞 そう, だ 助動詞 だ, 」 記号 」, と 助詞 と, 答え 動詞 答える, た 助動詞 た, 。 記号 。]", testList.toString());
  }
}
