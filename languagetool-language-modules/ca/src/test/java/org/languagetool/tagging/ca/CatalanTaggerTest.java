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
package org.languagetool.tagging.ca;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class CatalanTaggerTest {

  private CatalanTagger tagger = CatalanTagger.INSTANCE_CAT;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() {
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Catalan());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("UPF", "UPF/[UPF]NPFSO00", tokenizer, tagger);
    TestTools
        .myAssert(
            "Sóc un home molt honrat.",
            "Sóc/[ser]VSIP1S00 -- un/[un]DI0MS0|un/[un]PI0MS000 -- home/[home]I|home/[home]NCMS000 -- molt/[moldre]VMP00SM0|molt/[molt]DI0MS0|molt/[molt]PI0MS000|molt/[molt]RG -- honrat/[honrar]VMP00SM0|honrat/[honrat]AQ0MS0",
            tokenizer, tagger);
    TestTools.myAssert("blablabla", "blablabla/[null]null", tokenizer, tagger);
    TestTools.myAssert("inajornablement",
        "inajornablement/[inajornablement]RG", tokenizer, tagger);
    TestTools.myAssert("Acomplexadament",
        "Acomplexadament/[acomplexadament]RG", tokenizer, tagger);
    TestTools.myAssert("FRANÇA",
        "FRANÇA/[França]NPFSG00", tokenizer, tagger);
  }
}
