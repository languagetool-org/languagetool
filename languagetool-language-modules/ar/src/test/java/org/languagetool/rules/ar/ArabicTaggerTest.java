/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Arabic;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class ArabicTaggerTest {

  private ArabicTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new ArabicTagger();
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Arabic());
  }

  @Test
  public void testTagger() throws IOException {

    TestTools.myAssert("هذه",
      "هذه/[هذه]DFS", tokenizer, tagger);

    TestTools.myAssert("الخياريتان",
      "الخياريتان/[خيار]NJ-;F2--;--L", tokenizer, tagger);

    TestTools.myAssert("السماء زرقاء",
            "السماء/[سماء]NJ-;F1--;--L|السماء/[سماء]NJ-;F1A-;--L|السماء/[سماء]NJ-;F1I-;--L|السماء/[سماء]NJ-;F1U-;--L -- زرقاء/[زرقاء]NA-;F1--;---|زرقاء/[زرقاء]NA-;F1A-;---|زرقاء/[زرقاء]NA-;F1I-;---|زرقاء/[زرقاء]NA-;F1U-;---", tokenizer, tagger);

    // non-existing-word
    TestTools.myAssert("العباره",
      "العباره/[null]null", tokenizer, tagger);

    TestTools.myAssert("والبلاد",
"والبلاد/[والبلاد]NJ-;F3--;W-L|والبلاد/[والبلاد]NJ-;F3A-;W-L|والبلاد/[والبلاد]NJ-;F3I-;W-L|والبلاد/[والبلاد]NJ-;F3U-;W-L|والبلاد/[والبلاد]NJ-;M1--;W-L|والبلاد/[والبلاد]NJ-;M1A-;W-L|والبلاد/[والبلاد]NJ-;M1I-;W-L|والبلاد/[والبلاد]NJ-;M1U-;W-L",   tokenizer, tagger);


    TestTools.myAssert("بلادهما",
"بلادهما/[بلادهما]NJ-;F3--;--H|بلادهما/[بلادهما]NJ-;F3A-;--H|بلادهما/[بلادهما]NJ-;F3I-;--H|بلادهما/[بلادهما]NJ-;F3U-;--H|بلادهما/[بلادهما]NJ-;M1--;--H|بلادهما/[بلادهما]NJ-;M1A-;--H|بلادهما/[بلادهما]NJ-;M1I-;--H|بلادهما/[بلادهما]NJ-;M1U-;--H",
      tokenizer, tagger);
     /*
    TestTools.myAssert("وبلادهما",
      "وبلادهما/[وبلادهما]NJ-;-1--;W-H|وبلادهما/[وبلادهما]NJ-;-3--;W-H",
      tokenizer, tagger);
    TestTools.myAssert("كبلاد",
            "كبلاد/[كبلاد]NJ-;-1--;-K-|كبلاد/[كبلاد]NJ-;-3--;-K-",
            tokenizer, tagger);
    TestTools.myAssert("وكالبلاد",
            "وكالبلاد/[وكالبلاد]NJ-;-1--;WKL|وكالبلاد/[وكالبلاد]NJ;-3--;WKL",
            tokenizer, tagger);
     */
  }
}
