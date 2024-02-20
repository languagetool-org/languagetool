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
package org.languagetool.tagging.pt;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Portuguese;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.pt.PortugueseWordTokenizer;

import java.io.IOException;

public class PortugueseTaggerTest {

  private PortugueseTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new PortugueseTagger();
    tokenizer = new PortugueseWordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Portuguese());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("Estes são os meus amigos.",
        "Estes/[este]AQ0CP0|Estes/[este]DD0MP0|Estes/[este]NCMP000|Estes/[este]PD0MP000 -- "
            + "são/[ser]VMIP3P0|são/[são]AQ0MS0|são/[são]NCMS000 -- "
            + "os/[ele]PP3MPA00|os/[o]DA0MP0|os/[o]PD0MP000 -- "
            + "meus/[meu]AP0MP1S|meus/[meu]DP1MPS -- "
            + "amigos/[amigo]AQ0MP0|amigos/[amigo]NCMP000", tokenizer, tagger);
    
    TestTools.myAssert("tentou resolver",
        "tentou/[tentar]VMIS3S0 -- resolver/[resolver]VMN0000|resolver/[resolver]VMN01S0|resolver/[resolver]VMN03S0|resolver/[resolver]VMSF1S0|resolver/[resolver]VMSF3S0"
        , tokenizer, tagger);
    TestTools.myAssert("Deixe-me",
      "Deixe/[deixar]VMM03S0|Deixe/[deixar]VMSP1S0|Deixe/[deixar]VMSP3S0 -- me/[eu]PP1CSO00",
      tokenizer, tagger);
  }

  @Test
  public void testTaggerTagsOrdinalAbbreviations() throws IOException {
    // with regular lowercase letters
    TestTools.myAssert("1.as", "1.as/[1.º]AO0FP0|1.as/[1.º]NCFP000", tokenizer, tagger);
    TestTools.myAssert("2as", "2as/[2º]AO0FP0|2as/[2º]NCFP000", tokenizer, tagger);
    TestTools.myAssert("300as", "300as/[300º]AO0FP0|300as/[300º]NCFP000", tokenizer, tagger);
    // with ordinal indicator
    TestTools.myAssert("4.ªˢ", "4.ªˢ/[4.º]AO0FP0|4.ªˢ/[4.º]NCFP000", tokenizer, tagger);
    TestTools.myAssert("5ªˢ", "5ªˢ/[5º]AO0FP0|5ªˢ/[5º]NCFP000", tokenizer, tagger);
    TestTools.myAssert("600ªˢ", "600ªˢ/[600º]AO0FP0|600ªˢ/[600º]NCFP000", tokenizer, tagger);
    // with superscript
    TestTools.myAssert("7.ᵃˢ", "7.ᵃˢ/[7.º]AO0FP0|7.ᵃˢ/[7.º]NCFP000", tokenizer, tagger);
    TestTools.myAssert("8ᵃˢ", "8ᵃˢ/[8º]AO0FP0|8ᵃˢ/[8º]NCFP000", tokenizer, tagger);
    TestTools.myAssert("900ᵃˢ", "900ᵃˢ/[900º]AO0FP0|900ᵃˢ/[900º]NCFP000", tokenizer, tagger);
    // percent sign
    TestTools.myAssert("10%", "10%/[10%]NCMP000", tokenizer, tagger);
    TestTools.myAssert("−11.000%", "−11.000%/[−11.000%]NCMP000", tokenizer, tagger);
    // degree sign
    TestTools.myAssert("12°", "12°/[12°]NCMP000", tokenizer, tagger);
  }

  @Test
  public void testContractionTagging() throws IOException {
    TestTools.myAssert("das", "das/[de:o]SPS00:DA0FP0", tokenizer, tagger);
    TestTools.myAssert("to", "to/[tu:ele]PP2CSO00:PP3MSA00", tokenizer, tagger);
    TestTools.myAssert("ao", "ao/[a:o]SPS00:DA0MS0", tokenizer, tagger);
  }

  @Test
  public void testTaggerTagsCompoundsRegardlessOfLetterCase() throws IOException {
    TestTools.myAssert("jiu-jitsu", "jiu-jitsu/[jiu-jitsu]NCMS000", tokenizer, tagger);
    TestTools.myAssert("Jiu-jitsu", "Jiu-jitsu/[jiu-jitsu]NCMS000", tokenizer, tagger);
    TestTools.myAssert("JIU-JITSU", "JIU-JITSU/[jiu-jitsu]NCMS000", tokenizer, tagger);
    TestTools.myAssert("Jiu-Jitsu", "Jiu-Jitsu/[jiu-jitsu]NCMS000", tokenizer, tagger);
  }
}
