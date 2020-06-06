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
      "السماء/[سماء]NJ-;F1--;--L|" +
        "السماء/[سماء]NJ-;F1A-;--L|" +
        "السماء/[سماء]NJ-;F1I-;--L|" +
        "السماء/[سماء]NJ-;F1U-;--L" +
        " -- " +
        "زرقاء/[زرقاء]NA-;F1--;---|" +
        "زرقاء/[زرقاء]NA-;F1A-;---|" +
        "زرقاء/[زرقاء]NA-;F1I-;---|" +
        "زرقاء/[زرقاء]NA-;F1U-;---", tokenizer, tagger);

    // non-existing-word
    TestTools.myAssert("العباره",
      "العباره/[null]null", tokenizer, tagger);

    TestTools.myAssert("والبلاد",
      "والبلاد/[بلاد]NJ-;F3--;W-L|" +
        "والبلاد/[بلاد]NJ-;F3A-;W-L|" +
        "والبلاد/[بلاد]NJ-;F3I-;W-L|" +
        "والبلاد/[بلاد]NJ-;F3U-;W-L|" +
        "والبلاد/[بلاد]NJ-;M1--;W-L|" +
        "والبلاد/[بلاد]NJ-;M1A-;W-L|" +
        "والبلاد/[بلاد]NJ-;M1I-;W-L|" +
        "والبلاد/[بلاد]NJ-;M1U-;W-L", tokenizer, tagger);


    TestTools.myAssert("بلادهما",
      "بلادهما/[بلاد]NJ-;F3--;--H|" +
        "بلادهما/[بلاد]NJ-;F3A-;--H|" +
        "بلادهما/[بلاد]NJ-;F3I-;--H|" +
        "بلادهما/[بلاد]NJ-;F3U-;--H|" +
        "بلادهما/[بلاد]NJ-;M1--;--H|" +
        "بلادهما/[بلاد]NJ-;M1A-;--H|" +
        "بلادهما/[بلاد]NJ-;M1I-;--H|" +
        "بلادهما/[بلاد]NJ-;M1U-;--H",
      tokenizer, tagger);

    TestTools.myAssert("وبلادهما", "وبلادهما/[بلاد]NJ-;F3--;W-H|" +
        "وبلادهما/[بلاد]NJ-;F3A-;W-H|" +
        "وبلادهما/[بلاد]NJ-;F3I-;W-H|" +
        "وبلادهما/[بلاد]NJ-;F3U-;W-H|" +
        "وبلادهما/[بلاد]NJ-;M1--;W-H|" +
        "وبلادهما/[بلاد]NJ-;M1A-;W-H|" +
        "وبلادهما/[بلاد]NJ-;M1I-;W-H|" +
        "وبلادهما/[بلاد]NJ-;M1U-;W-H",
      tokenizer, tagger);
    TestTools.myAssert("كبلاد",
      "كبلاد/[بلاد]NJ-;F3--;-K-|" +
        "كبلاد/[بلاد]NJ-;F3A-;-K-|" +
        "كبلاد/[بلاد]NJ-;F3I-;-K-|" +
        "كبلاد/[بلاد]NJ-;F3U-;-K-|" +
        "كبلاد/[بلاد]NJ-;M1--;-K-|" +
        "كبلاد/[بلاد]NJ-;M1A-;-K-|" +
        "كبلاد/[بلاد]NJ-;M1I-;-K-|" +
        "كبلاد/[بلاد]NJ-;M1U-;-K-",
      tokenizer, tagger);
    TestTools.myAssert("وكالبلاد",
      "وكالبلاد/[بلاد]NJ-;F3--;WKL|" +
        "وكالبلاد/[بلاد]NJ-;F3A-;WKL|" +
        "وكالبلاد/[بلاد]NJ-;F3I-;WKL|" +
        "وكالبلاد/[بلاد]NJ-;F3U-;WKL|" +
        "وكالبلاد/[بلاد]NJ-;M1--;WKL|" +
        "وكالبلاد/[بلاد]NJ-;M1A-;WKL|" +
        "وكالبلاد/[بلاد]NJ-;M1I-;WKL|" +
        "وكالبلاد/[بلاد]NJ-;M1U-;WKL",
      tokenizer, tagger);

  }
}
