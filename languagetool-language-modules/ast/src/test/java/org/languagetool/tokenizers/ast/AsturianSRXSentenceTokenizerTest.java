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
package org.languagetool.tokenizers.ast;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Asturian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class AsturianSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Asturian());

  @Test
  public void testTokenize() {
    testSplit("De secute, los hackers de Minix aportaron idegues y códigu al núcleu Linux, y güey recibiera contribuciones de miles de programadores. ",
              "Torvalds sigue lliberando nueves versiones del núcleu, consolidando aportes d'otros programadores y faciendo cambios el mesmu.");
    stokenizer.setSingleLineBreaksMarksParagraph(false);
    testSplit("De secute,\nlos hackers de Minix...");
    testSplit("De secute,\n\n", "los hackers de Minix...");
    stokenizer.setSingleLineBreaksMarksParagraph(true);
    testSplit("De secute,\n", "los hackers de Minix...");
    testSplit("De secute,\n", "\n", "los hackers de Minix...");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
