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

    //Tag random accepted compound words
    TestTools.myAssert("oorlogsbeker", "oorlogsbeker/[oorlogsbeker]ZNW:EKV:DE_", tokenizer, tagger);
    TestTools.myAssert("dorpswezen", "dorpswezen/[dorpswees]ZNW:MRV:DE_|dorpswezen/[dorpswezen]ZNW:EKV:HET", tokenizer, tagger);
    TestTools.myAssert("varkensweer", "varkensweer/[varkensweer]ZNW:EKV:HET", tokenizer, tagger);
    TestTools.myAssert("jongenstablet", "jongenstablet/[jongenstablet]ZNW:EKV:DE_|jongenstablet/[jongenstablet]ZNW:EKV:HET", tokenizer, tagger);
    TestTools.myAssert("passagierssituaties", "passagierssituaties/[passagierssituatie]ZNW:MRV:DE_", tokenizer, tagger);

    //As this does not end in "ings", should not be accepted by CompoundAcceptor
    TestTools.myAssert("beoordelinggeschiedenis", "beoordelinggeschiedenis/[null]null", tokenizer, tagger);
    //These do, and are tagged
    TestTools.myAssert("beoordelingsgeschiedenis", "beoordelingsgeschiedenis/[beoordelingsgeschiedenis]ZNW:EKV:DE_", tokenizer, tagger);
    TestTools.myAssert("beoordelingsgeschiedenisje", "beoordelingsgeschiedenisje/[beoordelingsgeschiedenis]ZNW:EKV:VRK:HET", tokenizer, tagger);
    TestTools.myAssert("Beoordelingsgeschiedenisjes", "Beoordelingsgeschiedenisjes/[beoordelingsgeschiedenis]ZNW:MRV:VRK:DE_", tokenizer, tagger);

    // Test regions
    TestTools.myAssert("Zuidoost-Gouda", "Zuidoost-Gouda/[Gouda]ENM:LOC:PTS", tokenizer, tagger);
    TestTools.myAssert("West-Bergambacht", "West-Bergambacht/[Bergambacht]ENM:LOC:PTS", tokenizer, tagger);

    // Test compound words with 2 parts
    TestTools.myAssert("beroertegeschiedenisje", "beroertegeschiedenisje/[beroertegeschiedenis]ZNW:EKV:VRK:HET", tokenizer, tagger);
    TestTools.myAssert("aspirant-burgemeestertje", "aspirant-burgemeestertje/[aspirant-burgemeester]ZNW:EKV:VRK:HET", tokenizer, tagger);
    // Test compound words with 3 parts
    TestTools.myAssert("gastkritiekgeschiedenis", "gastkritiekgeschiedenis/[null]null", tokenizer, tagger);
    // Test compound words with 3+ parts
    TestTools.myAssert("haarhalfbergnacht", "haarhalfbergnacht/[null]null", tokenizer, tagger);

    // Make sure part1 and part2 as duplicates are not accepted
    TestTools.myAssert("vriendenvrienden", "vriendenvrienden/[null]null", tokenizer, tagger);

    // This is not modified, as it's already found in dictionary. If it was, getCompoundPOS would give it postag ZNW:EKV, from "mout".
    TestTools.myAssert("havermout", "havermout/[havermout]ZNW:EKV:DE_", tokenizer, tagger);

    TestTools.myAssert("déúr", "déúr/[deur]ZNW:EKV:DE_", tokenizer, tagger);
    TestTools.myAssert("kómen", "kómen/[komen]WKW:TGW:INF", tokenizer, tagger);
    TestTools.myAssert("kán", "kán/[kan]ZNW:EKV:DE_|kán/[kunnen]WKW:TGW:1EP|kán/[kunnen]WKW:TGW:3EP", tokenizer,
        tagger);
    TestTools.myAssert("ín", "ín/[in]VRZ|ín/[innen]WKW:TGW:1EP", tokenizer, tagger);
    TestTools.myAssert("deur-knop", "deur-knop/[deurknop]ZNW:EKV:DE_", tokenizer, tagger);
  }
}
