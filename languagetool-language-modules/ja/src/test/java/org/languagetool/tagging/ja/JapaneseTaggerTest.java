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
package org.languagetool.tagging.ja;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.tokenizers.ja.JapaneseWordTokenizer;

import java.io.IOException;

public class JapaneseTaggerTest {

  private JapaneseTagger tagger;
  private JapaneseWordTokenizer tokenizer;
  
  @Before
  public void setUp() {
    tagger = new JapaneseTagger();
    tokenizer = new JapaneseWordTokenizer();
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("これは簡単なテストです。",
        "これ/[これ]名詞 -- は/[は]助詞 -- 簡単/[簡単]名詞 -- な/[だ]助動詞 -- テスト/[テスト]名詞 -- です/[です]助動詞 -- 。/[。]記号", tokenizer, tagger);
    TestTools.myAssert("私は眠い。",
        "私/[私]名詞 -- は/[は]助詞 -- 眠い/[眠い]形容詞 -- 。/[。]記号", tokenizer, tagger);
    TestTools.myAssert("とても冷たい飲み物。",
        "とても/[とても]副詞 -- 冷たい/[冷たい]形容詞 -- 飲み物/[飲み物]名詞 -- 。/[。]記号", tokenizer, tagger);
  }
  

}
