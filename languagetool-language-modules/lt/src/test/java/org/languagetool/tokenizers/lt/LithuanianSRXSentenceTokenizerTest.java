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
package org.languagetool.tokenizers.lt;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Lithuanian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class LithuanianSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Lithuanian());

  @Test
  public void testTokenize() {
    testSplit("Linux – laisvos operacinės sistemos branduolio (kernel) pavadinimas. ",
              "Dažnai taip sutrumpintai vadinama ir bendrai visa Unix-tipo operacinė sistema naudojanti Linux branduolį kartu su sisteminėmis programomis bei bibliotekomis iš GNU projekto.");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
