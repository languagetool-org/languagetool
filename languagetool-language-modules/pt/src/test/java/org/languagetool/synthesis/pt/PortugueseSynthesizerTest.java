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

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.languagetool.AnalyzedToken;

public class PortugueseSynthesizerTest extends TestCase {
  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

  public final void testSynthesizeString() throws IOException {
    PortugueseSynthesizer synth = new PortugueseSynthesizer();
    assertEquals(synth.synthesize(dummyToken("blablabla"), "blablabla").length, 0);

    assertEquals("[bois]", Arrays.toString(
            getSortedArray(synth.synthesize(dummyToken("boi"), "NCMP000", true))));

  }

  private String[] getSortedArray(String... ar) {
    String[] newAr = ar.clone();
    Arrays.sort(newAr);
    return newAr;
  }
  

}