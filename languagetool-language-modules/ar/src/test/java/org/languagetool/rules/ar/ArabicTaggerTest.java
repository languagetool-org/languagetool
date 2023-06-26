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

    TestTools.myAssert("الخياريتان",
      "الخياريتان/[خيار]NA-;F3--;--L" +
        "|الخياريتان/[خيار]NJ-;F2--;--L" +
        "|الخياريتان/[خيار]NJ-;F3--;--L",
      tokenizer, tagger);

    TestTools.myAssert("السماء زرقاء",
      "السماء/[سماء]NJ-;F1--;--L|" +
        "السماء/[سماء]NJ-;F1A-;--L|" +
        "السماء/[سماء]NJ-;F1I-;--L|" +
        "السماء/[سماء]NJ-;F1U-;--L" +
        " -- " +
        "زرقاء/[زرقاء]NA-;F1--;---|" +
        "زرقاء/[زرقاء]NA-;F1A-;---|" +
        "زرقاء/[زرقاء]NA-;F1I-;---|" +
        "زرقاء/[زرقاء]NA-;F1U-;---",
      tokenizer, tagger);

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
        "والبلاد/[بلاد]NJ-;M1U-;W-L",
      tokenizer, tagger);


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

    TestTools.myAssert("وبلادهما",
      "وبلادهما/[بلاد]NJ-;F3--;W-H|" +
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
        "كبلاد/[بلاد]NJ-;F3I-;-K-|" +
        "كبلاد/[بلاد]NJ-;M1--;-K-|" +
        "كبلاد/[بلاد]NJ-;M1I-;-K-",
      tokenizer, tagger);

    TestTools.myAssert("وكالبلاد",
      "وكالبلاد/[بلاد]NJ-;F3--;WKL|" +
        "وكالبلاد/[بلاد]NJ-;F3I-;WKL|" +
        "وكالبلاد/[بلاد]NJ-;M1--;WKL|" +
        "وكالبلاد/[بلاد]NJ-;M1I-;WKL",
      tokenizer, tagger);

    TestTools.myAssert("فاستعملها",
      "فاستعملها/[اِسْتَعْمَلَ]V61;M1H-pa-;W-H|فاستعملها/[اِسْتَعْمَلَ]V61;M1Y-i--;W-H",
      tokenizer, tagger);


    TestTools.myAssert("سيعملون",
      "سيعملون/[أَعْمَلَ]V41;M3H-faU;-S-|" +
        "سيعملون/[أَعْمَلَ]V41;M3H-fpU;-S-|" +
        "سيعملون/[عَمِلَ]V31;M3H-faU;-S-|" +
        "سيعملون/[عَمِلَ]V31;M3H-fpU;-S-|" +
        "سيعملون/[عَمَّلَ]V41;M3H-faU;-S-|" +
        "سيعملون/[عَمَّلَ]V41;M3H-fpU;-S-",
      tokenizer, tagger);

    TestTools.myAssert("فسيعملون",
      "فسيعملون/[أَعْمَلَ]V41;M3H-faU;WS-|" +
        "فسيعملون/[أَعْمَلَ]V41;M3H-fpU;WS-|" +
        "فسيعملون/[عَمِلَ]V31;M3H-faU;WS-|" +
        "فسيعملون/[عَمِلَ]V31;M3H-fpU;WS-|" +
        "فسيعملون/[عَمَّلَ]V41;M3H-faU;WS-|" +
        "فسيعملون/[عَمَّلَ]V41;M3H-fpU;WS-",
      tokenizer, tagger);

    TestTools.myAssert("كتاب",
      "كتاب/[كتاب]NA-;-3--;---|" +
        "كتاب/[كتاب]NA-;-3A-;---|" +
        "كتاب/[كتاب]NA-;-3I-;---|" +
        "كتاب/[كتاب]NA-;-3U-;---|" +
        "كتاب/[كتاب]NJ-;M1--;---|" +
        "كتاب/[كتاب]NJ-;M1A-;---|" +
        "كتاب/[كتاب]NJ-;M1I-;---|" +
        "كتاب/[كتاب]NJ-;M1U-;---|" +
        "كتاب/[كتاب]NM-;M1--;---|" +
        "كتاب/[كتاب]NM-;M1A-;---|" +
        "كتاب/[كتاب]NM-;M1I-;---|" +
        "كتاب/[كتاب]NM-;M1U-;---",
      tokenizer, tagger);

    TestTools.myAssert("للبلاد",
      "للبلاد/[بلاد]NJ-;F3--;-LL|" +
        "للبلاد/[بلاد]NJ-;F3I-;-LL|" +
        "للبلاد/[بلاد]NJ-;M1--;-LL|" +
        "للبلاد/[بلاد]NJ-;M1I-;-LL",
      tokenizer, tagger);

    TestTools.myAssert("للاعب",
      "للاعب/[لاعب]NA-;M1--;-L-|" +
        "للاعب/[لاعب]NA-;M1--;-LL|" +
        "للاعب/[لاعب]NA-;M1I-;-L-|" +
        "للاعب/[لاعب]NA-;M1I-;-LL|" +
        "للاعب/[لَاعَبَ]V41;M1H-pa-;-L-|" +
        "للاعب/[لَاعَبَ]V41;M1Y-i--;-L-",
      tokenizer, tagger);
    
    TestTools.myAssert("ببلاد",
      "ببلاد/[بلاد]NJ-;F3--;-B-|" +
        "ببلاد/[بلاد]NJ-;F3I-;-B-|" +
        "ببلاد/[بلاد]NJ-;M1--;-B-|" +
        "ببلاد/[بلاد]NJ-;M1I-;-B-",
      tokenizer, tagger);
  }
}
