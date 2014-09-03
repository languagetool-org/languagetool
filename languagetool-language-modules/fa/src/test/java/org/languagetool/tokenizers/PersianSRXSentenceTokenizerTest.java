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
package org.languagetool.tokenizers;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Persian;

public class PersianSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Persian());
  
  @Test
  public void test() {
    // NOTE: sentences here need to end with a space character so they
    // have correct whitespace when appended:
    testSplit("این یک جمله است. ", "جملهٔ بعدی");
    testSplit("آیا این یک جمله است؟ ", "جملهٔ بعدی");
    testSplit("یک جمله!!! ", "جملهٔ بعدی");

    testSplit("جملهٔ اول... خوب نیست؟ ", "جملهٔ دوم.");
    testSplit("جملهٔ اول (...) ادامهٔ متن. ");
    testSplit("جملهٔ اول [...] ادامهٔ متن. ");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }

}
