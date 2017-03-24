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
package org.languagetool.tokenizers.it;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Italian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class ItalianSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Italian());

  @Test
  public void testTokenize() {
    testSplit("Il Castello Reale di Racconigi è situato a Racconigi, in provincia di Cuneo ma poco distante da Torino. ",
              "Nel corso della sua quasi millenaria storia ha visto numerosi rimaneggiamenti e divenne di proprietà dei Savoia a partire dalla seconda metà del XIV secolo.");
    testSplit("Dott. Bunsen Honeydew");  // abbreviation
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
