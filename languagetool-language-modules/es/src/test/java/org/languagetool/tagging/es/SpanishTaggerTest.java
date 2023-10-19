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
package org.languagetool.tagging.es;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class SpanishTaggerTest {

  private SpanishTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = SpanishTagger.INSTANCE;
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Spanish());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("destacadamente", "destacadamente/[destacadamente]RG", tokenizer, tagger);
    TestTools.myAssert("DESTACADAMENTE", "DESTACADAMENTE/[destacadamente]RG", tokenizer, tagger);
    TestTools.myAssert("alucinadamente", "alucinadamente/[alucinadamente]RG", tokenizer, tagger);
    TestTools.myAssert("disputadamente", "disputadamente/[disputadamente]RG", tokenizer, tagger);
    
    // all-upper case that is common noun & proper noun
    TestTools.myAssert("RIOJA", "RIOJA/[Rioja]NPCNG00|RIOJA/[rioja]NCMS000", tokenizer, tagger);
    
    TestTools.myAssert("dímelo", "dímelo/[decir]VMM02S0:PP1CS000:PP3MSA00", tokenizer, tagger);

    TestTools.myAssert("Soy un hombre muy honrado.",
        "Soy/[ser]VSIP1S0 -- un/[uno]DI0MS0 -- hombre/[hombre]I|hombre/[hombre]NCMS000 -- muy/[muy]RG -- honrado/[honrar]VMP00SM",
        tokenizer, tagger);
    TestTools.myAssert("Tengo que ir a mi casa.",
        "Tengo/[tener]VMIP1S0 -- que/[que]CS|que/[que]PR0CN000 -- ir/[ir]VMN0000 -- a/[a]NCFS000|a/[a]SPS00 -- mi/[mi]DP1CSS|mi/[mi]NCMS000 -- casa/[casa]NCFS000|casa/[casar]VMIP3S0|casa/[casar]VMM02S0",
        tokenizer, tagger);
    TestTools.myAssert("blablabla", "blablabla/[null]null", tokenizer, tagger);
    TestTools.myAssert("autoeducan", "autoeducan/[autoeducar]VMIP3P0", tokenizer, tagger);
    TestTools.myAssert("autorretratarán", "autorretratarán/[autorretratar]VMIF3P0", tokenizer, tagger);
    TestTools.myAssert("autorralentizan", "autorralentizan/[autorralentizar]VMIP3P0", tokenizer, tagger);
    TestTools.myAssert("autoralentizan", "autoralentizan/[null]null", tokenizer, tagger);
    TestTools.myAssert("autoretratarán", "autoretratarán/[null]null", tokenizer, tagger);
    TestTools.myAssert("económico-sociales", "económico-sociales/[económico-social]AQ0CP0", tokenizer, tagger);
    TestTools.myAssert("jurídico-económicas", "jurídico-económicas/[jurídico-económico]AQ0FP0", tokenizer, tagger);

  }
}
