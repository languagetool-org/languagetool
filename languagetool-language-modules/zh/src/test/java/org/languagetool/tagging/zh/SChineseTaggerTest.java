package org.languagetool.tagging.zh;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.tokenizers.zh.ChineseWordTokenizer;
import org.languagetool.tokenizers.zh.SChineseWordTokenizer;
import org.languagetool.tokenizers.zh.TChinsesWordTokenizer;

import java.io.IOException;

/**
 * The test of ChineseTagger.
 *
 * @author Minshan Chen
 * @author Xiaohui Wu
 * @author Jiamin Zheng
 * @author Zihao Li
 */
public class SChineseTaggerTest {

    private final SChineseTagger tagger = new SChineseTagger();
    private final SChineseWordTokenizer stokenizer = new SChineseWordTokenizer();
    private final TChinsesWordTokenizer ttokenizer = new TChinsesWordTokenizer();

    @Test
    public void testTagger() throws IOException {

        TestTools.myAssert(
                "主任强调指出错误的地方。",
                "主任/[null]nnt -- 强调指出/[null]l -- 错误/[null]n -- 的/[null]ude1 -- 地方/[null]n -- 。/[null]w",
                stokenizer, tagger);

        TestTools.myAssert(
                "她胸前挂着一块碧绿的玉。",
                "她/[null]rr -- 胸前/[null]s -- 挂着/[null]v -- 一块/[null]s -- 碧绿/[null]z -- 的/[null]ude1 -- 玉/[null]n -- 。/[null]w",
                stokenizer, tagger);

        TestTools.myAssert(
                "“鲯鳅”的研究结果有什么奥妙？",
                "“/[null]w -- 鲯鳅/[null]nba -- ”/[null]w -- 的/[null]ude1 -- 研究/[null]vn -- 结果/[null]n -- 有/[null]vyou -- 什么/[null]ry -- 奥妙/[null]an -- ？/[null]w",
                stokenizer, tagger);

        TestTools.myAssert(
                "我们的女组长真是百尺竿头更进一步。",
                "我们/[null]rr -- 的/[null]ude1 -- 女/[null]b -- 组长/[null]nnt -- 真是/[null]d -- 百尺竿头更进一步/[null]nz -- 。/[null]w",
                stokenizer, tagger);

        TestTools.myAssert(
                "国务院，非国家工作人员不能随便进去的地方。",
                "国务院/[null]nt -- ，/[null]w -- 非国家/[null]l -- 工作人员/[null]nnt -- 不能/[null]v -- 随便/[null]ad -- 进去/[null]vf -- 的/[null]ude1 -- 地方/[null]n -- 。/[null]w",
                stokenizer, tagger);

        TestTools.myAssert(
                "“哇……”珠海北师大操场上的师生大吃一惊！",
                "“/[null]w -- 哇/[null]o -- ……/[null]w -- ”/[null]w -- 珠海/[null]ns -- 北师大/[null]nt -- 操场上/[null]v -- 的/[null]ude1 -- 师生/[null]n -- 大吃一惊/[null]vl -- ！/[null]w",
                stokenizer, tagger);

        TestTools.myAssert(
                "在炎热的暑假里，我和其他同学们参加了姜老师的一个项目。",
                "在/[null]p -- 炎热/[null]a -- 的/[null]ude1 -- 暑假/[null]t -- 里/[null]f -- ，/[null]w -- 我/[null]rr -- 和/[null]cc -- 其他同学/[null]l -- 们/[null]k -- 参加/[null]v -- 了/[null]ule -- 姜/[null]n -- 老师/[null]nnt -- 的/[null]ude1 -- 一个/[null]mq -- 项目/[null]n -- 。/[null]w",
                stokenizer, tagger);

        TestTools.myAssert(
                "“咕咚，”一台联想ThinkPad T系列电脑从关羽的宿舍飞了下来。",
                "“/[null]w -- 咕咚/[null]o -- ，/[null]w -- ”/[null]w -- 一台/[null]ns -- 联想/[null]vi -- ThinkPad/[null]nx --  /[null]w -- T/[null]nx -- 系列/[null]n -- 电脑/[null]n -- 从/[null]p -- 关羽/[null]nr -- 的/[null]ude1 -- 宿舍/[null]n -- 飞/[null]vi -- 了/[null]ule -- 下来/[null]vf -- 。/[null]w",
                stokenizer, tagger);
    }

    /** Test of Traditonal Chinses sentences. */
    @Test
    public void testTagger2() throws IOException {

        TestTools.myAssert("鐵桿部隊憤怒情緒集結 馬英九腹背受敵", 
                "鐵桿/[null]n -- 部隊/[null]nis -- 憤怒/[null]a -- 情緒/[null]n -- 集結/[null]v -- 馬英九/[null]nr -- 腹背受敵/[null]vl",
                ttokenizer, tagger);

        TestTools.myAssert("馬英九回應連勝文“丐幫說”：稱黨內同志談話應謹慎",
                "馬英九/[null]nr -- 迴應/[null]vn -- 連勝文/[null]nr -- “/[null]w -- 丐幫/[null]n -- 說/[null]v -- ”/[null]w -- ：/[null]w -- 稱/[null]v -- 黨內/[null]s -- 同志/[null]n -- 談話/[null]vn -- 應/[null]v -- 謹慎/[null]a",
                ttokenizer, tagger);

        TestTools.myAssert("下個星期，我跟我朋唷打算去法國玩兒。",
                "下個星期/[null]nz -- ，/[null]w -- 我/[null]rr -- 跟/[null]p -- 我/[null]rr -- 朋/[null]ng -- 唷/[null]x -- 打算/[null]v -- 去/[null]vf -- 法國/[null]nsf -- 玩兒/[null]v -- 。/[null]w",
                ttokenizer,tagger);
    }
}
