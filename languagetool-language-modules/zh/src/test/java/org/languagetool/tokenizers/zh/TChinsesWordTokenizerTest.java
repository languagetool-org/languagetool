package org.languagetool.tokenizers.zh;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TChinsesWordTokenizerTest {

  private TChinsesWordTokenizer wordTokenizer = new TChinsesWordTokenizer();

  @Test
  public void testWordTokenize() {
    List<String> token = wordTokenizer.tokenize("鐵桿部隊憤怒情緒集結 馬英九腹背受敵");
    assertEquals(7, token.size());
    assertEquals("[鐵桿/n, 部隊/nis, 憤怒/a, 情緒/n, 集結/v, 馬英九/nr, 腹背受敵/vl]",
            token.toString());

    List<String> token2 = wordTokenizer.tokenize("馬英九回應連勝文“丐幫說”：稱黨內同志談話應謹慎");
    assertEquals(14, token2.size());
    assertEquals("[馬英九/nr, 迴應/vn, 連勝文/nr, “/w, 丐幫/n, 說/v, ”/w, ：/w, 稱/v, 黨內/s, 同志/n, 談話/vn, 應/v, 謹慎/a]",
            token2.toString());

    List<String> token3 = wordTokenizer.tokenize("微软公司於1975年由比爾·蓋茲和保羅·艾倫創立，18年啟動以智慧雲端、前端為導向的大改組。");
    assertEquals(24, token3.size());
    assertEquals("[微软公司/ntc, 於/p, 1975/m, 年/qt, 由/p, 比爾·蓋茲/nrf, 和/cc, 保羅·艾倫/nrf, 創立/v, ，/w, 18/m, 年/qt, 啟動/v, 以/p, 智慧/n, 雲端/n, 、/w, 前端/f, 為/p, 導向/n, 的/ude1, 大/a, 改組/v, 。/w]",
            token3.toString());
  }

  @Test
  public void testWordTokenize2() {
    List<String> token = wordTokenizer.tokenize("現在不知吉凶如何，急得他走頭無路，恨不能立時插翅回去。");
    System.out.print(token);
  }
}
