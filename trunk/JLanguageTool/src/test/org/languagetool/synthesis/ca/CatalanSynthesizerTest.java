/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà i Font
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

package org.languagetool.synthesis.ca;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import org.languagetool.AnalyzedToken;

public class CatalanSynthesizerTest extends TestCase {

  public final void testSynthesizeStringString() throws IOException {
    final CatalanSynthesizer synth = new CatalanSynthesizer();
    assertEquals(synth.synthesize(dummyToken("blablabla"),
        "blablabla").length, 0);

    assertEquals("[nostres]", Arrays.toString(synth.synthesize(dummyToken("nostre"), "PX1CP0P0")));
    assertEquals("[presidents]", Arrays.toString(synth.synthesize(dummyToken("president"), "NCMP000")));
    assertEquals("[comprovat]", Arrays.toString(synth.synthesize(dummyToken("comprovar"), "VMP00SM")));
    //with regular expressions
    assertEquals("[comprovades, comprovats, comprovada, comprovat]", Arrays.toString(synth.synthesize(dummyToken("comprovar"), "V.P.*", true)));
    assertEquals("[contestant, contestar]", Arrays.toString(synth.synthesize(dummyToken("contestar"), "VM[GN]0000", true)));
    //with special definite article
    assertEquals("[les universitats, la universitat]", Arrays.toString(synth.synthesize(dummyToken("universitat"), "+DT", false)));
    assertEquals("[les úniques, l'única, els únics, l'únic]", Arrays.toString(synth.synthesize(dummyToken("únic"), "+DT", false)));
  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

}
