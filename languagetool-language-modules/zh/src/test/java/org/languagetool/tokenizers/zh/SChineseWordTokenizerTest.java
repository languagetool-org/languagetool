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
  public void test() throws IOException{
    String content = readHealer();
    List<String> lengthNotEqual = new ArrayList<>();
    List<String> segNotEqual = new ArrayList<>();
    String[] groups = content.split("\r\n=====\r\n");

    String xml = "";
    for (String group : groups) {
      String groupId = null;
      String groupName = null;
      String[] terms = group.split("\r\n");

      String aGroupRule = "";
      for (int i = 0; i < terms.length; i++) {
        String[] sentences = terms[i].split(" ");
        String firstItem = sentences[0];
        String secondItem = sentences[1];
        if (i == 0) {
          groupId = firstItem;
          groupName = secondItem;
          aGroupRule += makeGroupLabel(groupId, groupName);
          continue;
        } else if (firstItem.length() != secondItem.length()) {
//          String info = firstItem + " " + secondItem + " " + groupId;
//          lengthNotEqual.add(info);
          continue;
        } else if (!isSameSegment(firstItem, secondItem)) {
//          String info = firstItem + " " + secondItem + " " + groupId;
//          segNotEqual.add(info);
          continue;
        } else {
          String rule = makeRuleLabel(firstItem, secondItem);
          aGroupRule += rule;
        }
      }
      aGroupRule += "</rulegroup>\n";
      xml += aGroupRule;
    }
    System.out.println(xml);
  }

  public String makeRuleLabel(String wrong, String gold) throws IOException{
    PerceptronLexicalAnalyzer analyzer = new PerceptronLexicalAnalyzer();
    analyzer.enableNameRecognize(true);
    analyzer.enableCustomDictionary(true);

    String token = "";
    String suggestion = "";
    int offSef = 0;
    List<Term> wrongList = analyzer.seg(wrong);
    List<Term> goldList  = analyzer.seg(gold);

    for (int i = 0; i < goldList.size(); i++) {
      String wtoken = wrongList.get(i).word;
      String gtoken = goldList.get(i).word;
      if (!wtoken.equals(gtoken)) {
        token = wtoken;
        suggestion = gtoken;
        offSef = goldList.get(i).offset;
      }
    }

    wrong = wrong.replace(token, "<marker>"+token+"</marker>");
    String pattern = "<pattern>\n<marker>\n<token>" + token + "</token>\n</marker>\n</pattern>\n";
    String message = "<message>\n您的意思是\""  + suggestion + "\"吗？\n</message>\n";
    String exam_wrong = "<example correction=\"\">" + wrong + "</example>\n";
    String exam_gold = "<example>" + gold + "</example>\n";
    String rule = "<rule>\n" + pattern + message + exam_wrong + exam_gold + "</rule>\n";
    return rule;
  }

  @Test
  public void testMakerule() throws IOException {
    String wrong = "新同事跟大家相处得非常融恰。";
    String gold =  "新同事跟大家相处得非常融洽。";
    String rule = makeRuleLabel(wrong, gold);
    System.out.println(rule);
  }

  public boolean isSameSegment(String s1, String s2) throws IOException{
    PerceptronLexicalAnalyzer analyzer = new PerceptronLexicalAnalyzer();
    analyzer.enableNameRecognize(true);
    analyzer.enableCustomDictionary(true);
    List<Term> termList1 = analyzer.seg(s1);
    List<Term> termList2 = analyzer.seg(s2);

    if (termList1.size() == termList2.size()) {
      return true;
    }
    return false;
  }

  public String makeGroupLabel(String groupId, String groupName) {
    return "<rulegroup id=\"" + groupId + "\" name=\"" + groupName + "\">\n";
  }


  private String readHealer() throws IOException {
    String path = "G:\\languagetool\\languagetool-language-modules\\zh\\src\\1.txt";
    String content = "";

    FileInputStream file = new FileInputStream(path);
    InputStreamReader reader = new InputStreamReader(file, "GBK");
    BufferedReader br = new BufferedReader(reader);
    String line = null;
    while ((line = br.readLine()) != null) {
      content += line;
      content += "\r\n";
    }
    return content;
  }

  @Test
  public void testSeg() throws IOException {
    String s1 = "账篷是必须带的。";
    String s2 = "帐篷是必须带的。";
    PerceptronLexicalAnalyzer analyzer = new PerceptronLexicalAnalyzer();
    System.out.println(analyzer.seg(s1) + "\n" + analyzer.seg(s2));
  }

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

}
