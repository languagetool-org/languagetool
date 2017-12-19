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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.languagetool.TestTools;

import java.io.IOException;

/**
 * Test for Serbian ekavian tagger
 *
 * @author Zoltán Csala
 */
public class EkavianTaggerTest extends AbstractSerbianTaggerTest {

  @NotNull
  protected EkavianTagger createTagger() {
    return new EkavianTagger();
  }

  /**
   * First we test if the tagger works fine with single words
   */
  @Test
  public void testTaggerRaditi() throws Exception {
    // to work
    assertHasLemmaAndPos("радим", "радити", "GL:GV:PZ:1L:0J");
    // Глаголски прилог садашњи
    assertHasLemmaAndPos("радећи", "радити", "PL:PN");
  }

  /**
   * Special case for auxiliary verb "jesam" (I am)
   */
  @Test
  public void testTaggerJesam() throws IOException {
    assertHasLemmaAndPos("је", "јесам", "GL:PM:PZ:3L:0J");
    assertHasLemmaAndPos("јеси", "јесам", "GL:PM:PZ:2L:0J");
    assertHasLemmaAndPos("смо", "јесам", "GL:PM:PZ:1L:0M");
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("Данас је леп дан.", "Данас/[данас]PL:GN:PO -- је/[јесам]GL:PM:PZ:3L:0J -- леп/[леп]PR:OP:PO:MU:0J:AK:ST|леп/[леп]PR:OP:PO:MU:0J:NO:NE|леп/[леп]PR:OP:PO:MU:0J:VO:NE|леп/[лепак]PR:OP:PO:MU:0J:VO:NE -- дан/[дан]IM:ZA:MU:0J:AK:ST|дан/[дан]IM:ZA:MU:0J:NO|дан/[дан]PR:OP:PO:MU:0J:AK:ST|дан/[дан]PR:OP:PO:MU:0J:NO:NE|дан/[дан]PR:OP:PO:MU:0J:VO:NE|дан/[дати]PR:PC:PO:MU:0J:AK:ST|дан/[дати]PR:PC:PO:MU:0J:NO:NE|дан/[дати]PR:PC:PO:MU:0J:VO:NE", getTokenizer(), getTagger());
    TestTools.myAssert("Oво је велика кућа.", "Oво/[null]null -- је/[јесам]GL:PM:PZ:3L:0J -- велика/[велик]PR:OP:PO:MU:0J:AK:ZI|велика/[велик]PR:OP:PO:MU:0J:GE:NE|велика/[велик]PR:OP:PO:SR:0J:GE:NE|велика/[велик]PR:OP:PO:SR:0M:AK:OR|велика/[велик]PR:OP:PO:SR:0M:NO:OR|велика/[велик]PR:OP:PO:SR:0M:VO:OR|велика/[велик]PR:OP:PO:ZE:0J:NO:OR|велика/[велик]PR:OP:PO:ZE:0J:VO:OR -- кућа/[кућа]IM:ZA:ZE:0J:NO|кућа/[кућа]IM:ZA:ZE:0M:GE", getTokenizer(), getTagger());
    TestTools.myAssert("Растао сам поред Дунава.", "Растао/[растати]GL:GV:RA:0:0J:MU|Растао/[расти]GL:GV:RA:0:0J:MU -- сам/[сам]PR:OP:PO:MU:0J:AK:ST|сам/[сам]PR:OP:PO:MU:0J:NO:NE|сам/[сам]PR:OP:PO:MU:0J:VO:NE|сам/[јесам]GL:PM:PZ:1L:0J -- поред/[поред]PE:GE|поред/[поред]PL:GN:PO -- Дунава/[Дунав]IM:VL:MU:0J:GE|Дунава/[Дунав]IM:VL:MU:0M:GE", getTokenizer(), getTagger());
    TestTools.myAssert("Србијом је владао Петар I, краљ ослободилац.", "Србијом/[Србија]IM:VL:ZE:0J:IN -- је/[јесам]GL:PM:PZ:3L:0J -- владао/[владати]GL:GV:RA:0:0J:MU -- Петар/[Петар]IM:VL:MU:0J:NO|Петар/[Петар]IM:VL:MU:0J:NO:ZI -- I/[I]BR:RI:ON|I/[i]BR:RI:ON|I/[i]RE:MO|I/[i]UZ|I/[i]VE:SA -- краљ/[краљ]IM:ZA:MU:0J:NO -- ослободилац/[ослободилац]IM:ZA:MU:0J:NO", getTokenizer(), getTagger());
    TestTools.myAssert("Луђа кућа.", "Луђа/[луд]PR:OP:KM:SR:0M:AK:OR|Луђа/[луд]PR:OP:KM:SR:0M:NO:OR|Луђа/[луд]PR:OP:KM:SR:0M:VO:OR|Луђа/[луд]PR:OP:KM:ZE:0J:NO:OR|Луђа/[луд]PR:OP:KM:ZE:0J:VO:OR|Луђа/[луђи]PR:OP:PO:SR:0M:AK:OR|Луђа/[луђи]PR:OP:PO:SR:0M:NO:OR|Луђа/[луђи]PR:OP:PO:SR:0M:VO:OR|Луђа/[луђи]PR:OP:PO:ZE:0J:NO:OR|Луђа/[луђи]PR:OP:PO:ZE:0J:VO:OR -- кућа/[кућа]IM:ZA:ZE:0J:NO|кућа/[кућа]IM:ZA:ZE:0M:GE", getTokenizer(), getTagger());
    // Proof that Ekavian tagger does not tag Jekavian words („лијеп“, „цвијет“, „свијет“)
    TestTools.myAssert("Ала је лијеп овај свијет, ондје поток, овдје цвијет.", "Ала/[ала]IM:ZA:ZE:0J:NO|Ала/[ала]IM:ZA:ZE:0M:GE -- је/[јесам]GL:PM:PZ:3L:0J -- лијеп/[null]null -- овај/[овај]ZM:PK:0:MU:0J:AK:ST|овај/[овај]ZM:PK:0:MU:0J:NO -- свијет/[null]null -- ондје/[null]null -- поток/[поток]IM:ZA:MU:0J:AK:ST|поток/[поток]IM:ZA:MU:0J:NO -- овдје/[null]null -- цвијет/[null]null", getTokenizer(), getTagger());
  }
}