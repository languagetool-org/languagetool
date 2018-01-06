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

import org.junit.Test;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test for Serbian jekavian tagger
 *
 * @author Zoltán Csala
 */
public class JekavianTaggerTest extends AbstractSerbianTaggerTest {

  protected JekavianTagger createTagger() {
    return new JekavianTagger();
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

  /**
   * Word that exists only in Јеkavian dictionary
   */
  @Test
  public void testTaggerSvijet() throws Exception {
    assertHasLemmaAndPos("цвијете", "цвијет", "IM:ZA:MU:0J:VO");
    assertHasLemmaAndPos("цвијетом", "цвијет", "IM:ZA:MU:0J:IN");
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("Ово је лијеп цвијет.", "Ово/[овај]ZM:PK:0:SR:0J:AK|Ово/[овај]ZM:PK:0:SR:0J:NO -- је/[јесам]GL:PM:PZ:3L:0J -- лијеп/[лијеп]PR:OP:PO:MU:0J:AK:ST|лијеп/[лијеп]PR:OP:PO:MU:0J:NO:NE|лијеп/[лијеп]PR:OP:PO:MU:0J:VO:NE -- цвијет/[цвијет]IM:ZA:MU:0J:AK:ST|цвијет/[цвијет]IM:ZA:MU:0J:NO", getTokenizer(), getTagger());
    // Proof that Jekavian tagger does not tag Ekavian words
    TestTools.myAssert("Ала је леп овај свет, онде поток, овде свет.", "Ала/[ала]IM:ZA:ZE:0J:NO|Ала/[ала]IM:ZA:ZE:0M:GE -- је/[јесам]GL:PM:PZ:3L:0J -- леп/[лепак]PR:OP:PO:MU:0J:VO:NE -- овај/[овај]ZM:PK:0:MU:0J:AK:ST|овај/[овај]ZM:PK:0:MU:0J:NO -- свет/[свет]PR:OP:PO:MU:0J:AK:ST|свет/[свет]PR:OP:PO:MU:0J:NO:NE|свет/[свет]PR:OP:PO:MU:0J:VO:NE -- онде/[null]null -- поток/[поток]IM:ZA:MU:0J:AK:ST|поток/[поток]IM:ZA:MU:0J:NO -- овде/[null]null -- свет/[свет]PR:OP:PO:MU:0J:AK:ST|свет/[свет]PR:OP:PO:MU:0J:NO:NE|свет/[свет]PR:OP:PO:MU:0J:VO:NE", getTokenizer(), getTagger());
  }
}