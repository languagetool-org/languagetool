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

import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.TraditionalChineseTokenizer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
        assertEquals("[今天/t, ，/w, 刘志军/nr, 案/ng, 的/uj, 关键/n, 人物/n, ,/w, 山西女/nrj, 商人/n, 丁书苗/nr, 在/p, 市二中院/nt, 出庭/v, 受审/v, 。/w]",
                tokens.toString());

        List<String> tokens2 = wordTokenizer.tokenize("“咕咚，”一台联想ThinkPad T系列电脑从关羽的宿舍飞了下来。");
        assertEquals(19, tokens2.size());
        assertEquals("[“/w, 咕咚/o, ，/w, ”/w, 一台/ns, 联想/nz, ThinkPad/nx,  /w, T/nx, 系列/q, 电脑/n, 从/p, 关羽/nr, 的/uj, 宿舍/n, 飞/v, 了/ul, 下来/v, 。/w]",
                tokens2.toString());

        List<String> token3 = wordTokenizer.tokenize("总统普京与特朗普通电话讨论太空探索技术公司");
        assertEquals(8, token3.size());
        assertEquals("[总统/n, 普京/nrf, 与/p, 特朗普/nrf, 通/v, 电话/n, 讨论/v, 太空探索技术公司/nt]",
                token3.toString());

        List<String> token4 = wordTokenizer.tokenize("克里斯蒂娜·克罗尔说：不，我不是虎妈。我全家都热爱音乐，我也鼓励他们这么做。");
        assertEquals(21, token4.size());
        assertEquals("[克里斯蒂娜·克罗尔/nrf, 说/v, ：/w, 不/d, ，/w, 我/r, 不是/c, 虎妈/nz, 。/w, 我/r, 全家/n, 都热/nr, 爱音乐/nz, ，/w, 我/r, 也/d, 鼓励/v, 他们/r, 这么/r, 做/v, 。/w]",
                token4.toString());

        List<String> token5 = wordTokenizer.tokenize("商品和服务");
        assertEquals(3, token5.size());
        assertEquals("[商品/n, 和/c, 服务/vn]",
                token5.toString());
    }

    /** tests of Traditional Chinese*/
    @Test
    public void testWordTokenize2() {
        List<String> token = wordTokenizer.tokenize("鐵桿部隊憤怒情緒集結 馬英九腹背受敵");

        List<String> token2 = wordTokenizer.tokenize("馬英九回應連勝文“丐幫說”：稱黨內同志談話應謹慎");

        List<String> token3 = wordTokenizer.tokenize("微软公司於1975年由比爾·蓋茲和保羅·艾倫創立，18年啟動以智慧雲端、前端為導向的大改組。");

    }

}
