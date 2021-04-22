/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.nl;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Dutch;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.nl.DutchWordTokenizer;

public class DutchTaggerTest {

  private DutchTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new DutchTagger();
    tokenizer = new DutchWordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Dutch());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("Aardappels koken.", "Aardappels/[aardappel]ZNW:MRV:DE_ -- koken/[koken]WKW:TGW:INF", tokenizer,
        tagger);
    TestTools.myAssert("zwijnden", "zwijnden/[zwijnen]WKW:VLT:INF", tokenizer, tagger);
    TestTools.myAssert("déúr", "déúr/[deur]ZNW:EKV:DE_", tokenizer, tagger);
    TestTools.myAssert("kómen", "kómen/[komen]WKW:TGW:INF", tokenizer, tagger);
    TestTools.myAssert("kán", "kán/[kan]ZNW:EKV:DE_|kán/[kunnen]WKW:TGW:1EP|kán/[kunnen]WKW:TGW:3EP", tokenizer,
        tagger);
    TestTools.myAssert("ín", "ín/[in]VRZ|ín/[innen]WKW:TGW:1EP", tokenizer, tagger);
    TestTools.myAssert("deur-knop", "deur-knop/[deurknop]ZNW:EKV:DE_", tokenizer, tagger);
  }
}
