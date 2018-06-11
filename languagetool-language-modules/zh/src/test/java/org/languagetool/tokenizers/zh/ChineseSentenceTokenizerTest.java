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
import org.languagetool.TestTools;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.util.List;
import static org.junit.Assert.assertEquals;

/**
 * The test of ChineseSentenceTokenizer.
 *
 * @author Minshan Chen
 * @author Xiaohui Wu
 * @author Jiamin Zheng
 * @author Zihao Li
 * @author Ze Dang
 */
public class ChineseSentenceTokenizerTest {

  private final SentenceTokenizer stokenizer = new ChineseSentenceTokenizer();

  @Test
  public void testTokenize() {

    String t1 = "他说：";
    String t2 = "我们是中国人";
    String t3 = "中国人很好";

    char[] punctuation1 = { '_', '/',  ':',  '@', '#', '$', '%', '\uff1a', '\u2026', '\u3001',
            '^', '&',  '+', '*',  }; // '：', '…', '、',
    for (char i : punctuation1) {
      testSplit(t2 + i + t3);// 例子：我们是中国人#中国人很好
    }

    //char[] punctuation2 = { ';', '!', '.', '?', '，', '！', '？', '；', '。' };
    char[] punctuation2 = { ';', '!', '.', '?',
            '\uff0c', '\uff01', '\uff1f', '\uff1b', '\u3002'};
    for (char i : punctuation2) {
      testSplit(t2 + i, t3);// 例子：我们是中国人？/中国人很好
    }

    String[] punctuation3 = { "\"", "\'", "‘", "(", "（", "“", "”", "）",
            ")", "’", "\'", "\"" };
    for (int i = 0; i < punctuation3.length / 2; i++) {
      testSplit(t1 + punctuation3[i] + t2 + "，", t3
              + punctuation3[punctuation2.length - 1 - i]); // 例子：他说："我们是中国人，/中国人很好)"
    }

    String[] punctuation4 = { "〝", "『", "«", "「", "〖", "{", "【", "[", "<",
            "《", "》", ">", "]", "】", "}", "〗", "」", "»", "』", "〞" };
    for (int i = 0; i < punctuation4.length / 2; i++) {
      testSplit(t1 + punctuation4[i] + t2 + "，", t3
              + punctuation4[punctuation4.length - 1 - i]); // 例子：他说〝我们是中国人，/中国人很好〞
    }

  }

  @Test
  public void testTokenize2() {
    testSplit("Linux是一種自由和開放源碼的類UNIX操作系統。",
              "该操作系统的内核由林纳斯·托瓦兹在1991年10月5日首次发布。",
              "在加上使用者空間的應用程式之後，", "成為Linux作業系統。");
  }

  @Test
  public void testTokenize3() {
    String[] sentenceArray = {"会上，",
            "李书记指出：“厂里要实行两项改革措施，",
            "一是持证上岗，",
            "二是脱产培训。"};
    String sentenceString = String.join("", sentenceArray);
    List<String> sentenceList = stokenizer.tokenize(sentenceString);
    for (int i = 0; i < sentenceArray.length; i ++) {
      assertEquals(sentenceList.get(i), sentenceArray[i]);
    }
  }

  private void testSplit(final String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
