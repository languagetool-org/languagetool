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
      "الخياريتان/[خيار]N.jamed.;-2-;--L-", tokenizer, tagger);

    TestTools.myAssert("السماء زرقاء",
      "السماء/[سماء]N.jamed.;-1-;--L- -- زرقاء/[زرقاء]N.adj.;F1-;----", tokenizer, tagger);

    // non-existing-word
    TestTools.myAssert("العباره",
      "العباره/[null]null", tokenizer, tagger);

    TestTools.myAssert("والبلاد",
      "والبلاد/[والبلاد]N.jamed.;-1-;--L-W|والبلاد/[والبلاد]N.jamed.;-3-;--L-W",
      tokenizer, tagger);

    TestTools.myAssert("بلادهما",
      "بلادهما/[بلادهما]N.jamed.;-1-;---H|بلادهما/[بلادهما]N.jamed.;-3-;---H",
      tokenizer, tagger);

    TestTools.myAssert("وبلادهما",
      "وبلادهما/[وبلادهما]N.jamed.;-1-;---HW|وبلادهما/[وبلادهما]N.jamed.;-3-;---HW",
      tokenizer, tagger);

  }
}
