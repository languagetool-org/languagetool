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

package org.languagetool.synthesis.pt;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.language.Portuguese;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PortugueseSynthesizerTest {
  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

  @Test
  public final void testSynthesizeString() throws IOException {
    PortugueseSynthesizer synth = new PortugueseSynthesizer(new Portuguese());
    assertEquals(synth.synthesize(dummyToken("blablabla"), "blablabla").length, 0);

    assertEquals("[bois]", Arrays.toString(
            getSortedArray(synth.synthesize(dummyToken("boi"), "NCMP000", true))));
    
    assertEquals("[tentar]", Arrays.toString(getSortedArray(synth.synthesize(dummyToken("tentar"), "VMN0000", true))));
    assertEquals("[tentou]", Arrays.toString(getSortedArray(synth.synthesize(dummyToken("tentar"), "VMIS3S0", true))));
    
    assertEquals("[resolver]", Arrays.toString(getSortedArray(synth.synthesize(dummyToken("resolver"), "VMN0000", true))));
    assertEquals("[resolveu]", Arrays.toString(getSortedArray(synth.synthesize(dummyToken("resolver"), "VMIS3S0", true))));
  }

  private String[] getSortedArray(String... ar) {
    String[] newAr = ar.clone();
    Arrays.sort(newAr);
    return newAr;
  }
  

}