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
package org.languagetool.tokenizers.ml;

import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Malayalam;
import org.languagetool.tokenizers.SRXSentenceTokenizer;

public class MalayalamSRXSentenceTokenizerTest {

  private final SRXSentenceTokenizer stokenizer = new SRXSentenceTokenizer(new Malayalam());

  @Test
  public void testTokenize() {
    testSplit("1983 ൽ റിച്ചാർഡ് സ്റ്റാൾമാൻ സ്ഥാപിച്ച ഗ്നു എന്ന സംഘടനയിൽ നിന്നും വളർന്നു വന്ന സോഫ്റ്റ്‌വെയറും ടൂളുകളുമാണ് ഇന്ന് ഗ്നൂ/ലിനക്സിൽ ലഭ്യമായിട്ടുള്ള സോഫ്റ്റ്‌വെയറിൽ സിംഹഭാഗവും. ",
              "ഗ്നു സംഘത്തിന്റെ മുഖ്യലക്ഷ്യം സ്വതന്ത്ര സോഫ്റ്റ്‌വെയറുകൾ മാത്രം ഉപയോഗിച്ചുകൊണ്ട് യുണിക്സ് പോലുള്ള ഒരു ഓപ്പറേറ്റിംഗ് സിസ്റ്റം നിർമ്മിക്കുന്നതായിരുന്നു.");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(sentences, stokenizer);
  }
  
}
