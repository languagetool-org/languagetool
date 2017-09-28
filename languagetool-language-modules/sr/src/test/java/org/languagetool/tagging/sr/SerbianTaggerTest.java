/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.sr;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Serbian;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

public class SerbianTaggerTest {

  private SerbianTagger tagger;
  private WordTokenizer tokenizer;

  @Before
  public void setUp() throws Exception {
    tagger = new SerbianTagger();
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    TestTools.testDictionary(tagger, new Serbian());
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("Данас је леп дан.", "Данас/[данас]PL:RA:PO -- је/[бити]GL:PM:PZ:3L:0J|је/[он]ZM:LI:3L:ZE:0J:AK|је/[он]ZM:LI:3L:ZE:0J:GE -- леп/[леп]PR:OP:PO:MU:0J:AK:NE:ST|леп/[леп]PR:OP:PO:MU:0J:NO:NE|леп/[леп]PR:OP:PO:MU:0J:VO:NE|леп/[лепак]PR:OP:PO:MU:0J:VO:NE -- дан/[дан]IM:ZA:MU:0J:AK:ST|дан/[дан]IM:ZA:MU:0J:NO|дан/[дан]PR:OP:PO:MU:0J:AK:NE:ST|дан/[дан]PR:OP:PO:MU:0J:NO:NE|дан/[дан]PR:OP:PO:MU:0J:VO:NE|дан/[дати]PR:PC:PO:MU:0J:AK:NE:ST|дан/[дати]PR:PC:PO:MU:0J:NO:NE|дан/[дати]PR:PC:PO:MU:0J:VO:NE", tokenizer, tagger);
    TestTools.myAssert("Oво је велика кућа.", "Oво/[null]null -- је/[бити]GL:PM:PZ:3L:0J|је/[он]ZM:LI:3L:ZE:0J:AK|је/[он]ZM:LI:3L:ZE:0J:GE -- велика/[велик]PR:OP:PO:MU:0J:AK:NE:ZI|велика/[велик]PR:OP:PO:MU:0J:GE:NE|велика/[велик]PR:OP:PO:SR:0J:GE:NE|велика/[велик]PR:OP:PO:SR:0M:AK:OR|велика/[велик]PR:OP:PO:SR:0M:NO:OR|велика/[велик]PR:OP:PO:SR:0M:VO:OR|велика/[велик]PR:OP:PO:ZE:0J:NO:OR|велика/[велик]PR:OP:PO:ZE:0J:VO:OR -- кућа/[кућа]IM:ZA:ZE:0J:NO|кућа/[кућа]IM:ZA:ZE:0M:GE", tokenizer, tagger);
    TestTools.myAssert("Растао сам поред Дунава.", "Растао/[растати]GL:GV:PC:0:0J:MU|Растао/[расти]GL:GV:PC:0:0J:MU -- сам/[бити]GL:PM:PZ:1L:0J|сам/[сам]PR:OP:PO:MU:0J:AK:NE:ST|сам/[сам]PR:OP:PO:MU:0J:NO:NE|сам/[сам]PR:OP:PO:MU:0J:VO:NE -- поред/[поред]PE:GE|поред/[поред]PL:RA:PO -- Дунава/[Дунав]IM:VL:MU:0J:GE|Дунава/[Дунав]IM:VL:MU:0M:GE", tokenizer, tagger);
    TestTools.myAssert("Француском влада Луј V.", "Француском/[Француска]IM:VL:ZE:0J:IN|Француском/[француски]PR:OP:PO:MU:0J:DA:OR|Француском/[француски]PR:OP:PO:MU:0J:LO:OR|Француском/[француски]PR:OP:PO:SR:0J:DA:OR|Француском/[француски]PR:OP:PO:SR:0J:LO:OR|Француском/[француски]PR:OP:PO:ZE:0J:IS:OR -- влада/[влада]IM:ZA:ZE:0J:NO|влада/[влада]IM:ZA:ZE:0M:GE|влада/[владати]GL:GV:PZ:3L:0J -- Луј/[null]null -- V/[null]null", tokenizer, tagger);
    TestTools.myAssert("Луђа кућа.", "Луђа/[луд]PR:OP:KM:SR:0M:AK:OR|Луђа/[луд]PR:OP:KM:SR:0M:NO:OR|Луђа/[луд]PR:OP:KM:SR:0M:VO:OR|Луђа/[луд]PR:OP:KM:ZE:0J:NO:OR|Луђа/[луд]PR:OP:KM:ZE:0J:VO:OR|Луђа/[луђи]PR:OP:PO:SR:0M:AK:OR|Луђа/[луђи]PR:OP:PO:SR:0M:NO:OR|Луђа/[луђи]PR:OP:PO:SR:0M:VO:OR|Луђа/[луђи]PR:OP:PO:ZE:0J:NO:OR|Луђа/[луђи]PR:OP:PO:ZE:0J:VO:OR -- кућа/[кућа]IM:ZA:ZE:0J:NO|кућа/[кућа]IM:ZA:ZE:0M:GE", tokenizer, tagger);
  }
}