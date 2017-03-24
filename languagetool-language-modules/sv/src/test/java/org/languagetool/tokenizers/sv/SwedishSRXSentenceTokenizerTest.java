/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.sv;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Swedish;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class SwedishSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Swedish());

  @Test
  public void testTokenize() {
    testSplit("Onkel Toms stuga är en roman skriven av Harriet Beecher Stowe, publicerad den 1852. ",
              "Den handlar om slaveriet i USA sett ur slavarnas perspektiv och bidrog starkt till att slaveriet avskaffades 1865 efter amerikanska inbördeskriget.");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
