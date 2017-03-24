/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.gl;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;

/**
 * @author Susana Sotelo Docio
 * based on English test
 */
public class GalicianTaggerTest {

  private GalicianTagger tagger;
  private WordTokenizer tokenizer;
  
  @Before
  public void setUp() {
    tagger = new GalicianTagger();
    tokenizer = new WordTokenizer();
  }

  @Test
  public void testTagger() throws IOException {
    TestTools.myAssert("Todo vai mudar",
        "Todo/[todo]DI0MS0|Todo/[todo]PI0MS000 -- vai/[ir]VMIP3S0|vai/[ir]VMM02S0 -- mudar/[mudar]VMN0000|mudar/[mudar]VMN01S0|mudar/[mudar]VMN03S0|mudar/[mudar]VMSF1S0|mudar/[mudar]VMSF3S0", tokenizer, tagger);
    TestTools.myAssert("Se aínda somos galegos é por obra e graza do idioma",
        "Se/[se]CS|Se/[se]PP3PN000|Se/[se]PP3SN000 -- aínda/[aínda]CS|aínda/[aínda]RG -- somos/[ser]VSIP1P0 -- galegos/[galego]AQ0MP0|galegos/[galego]NCMP000 -- é/[ser]VSIP3S0 -- por/[por]SPS00 -- obra/[obra]NCFS000|obra/[obrar]VMIP3S0|obra/[obrar]VMM02S0 -- e/[e]CC|e/[e]NCMS000 -- graza/[graza]NCFS000 -- do/[de]SPS00:DA -- idioma/[idioma]NCMS000", tokenizer, tagger);
  }
}
