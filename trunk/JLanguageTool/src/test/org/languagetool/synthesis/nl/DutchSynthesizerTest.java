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

package de.danielnaber.languagetool.synthesis.nl;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedToken;

public class DutchSynthesizerTest extends TestCase {

  private final AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }
  public final void testSynthesizeStringString() throws IOException {
    DutchSynthesizer synth = new DutchSynthesizer();
    assertEquals(synth.synthesize(dummyToken("blablabla"), 
        "blablabla").length, 0);
    
    assertEquals("[zwommen]", Arrays.toString(synth.synthesize(dummyToken("zwemmen"), "VBh")));
    assertEquals("[Afro-Surinamers]", Arrays.toString(synth.synthesize(dummyToken("Afro-Surinamer"), "NN2")));
    assertEquals("[hebt, heeft]", Arrays.toString(synth.synthesize(dummyToken("hebben"), "VB3", true)));
    //with regular expressions
    assertEquals("[doorgeseind]", Arrays.toString(synth.synthesize(dummyToken("doorseinen"), "VBp", true)));    
    assertEquals("[doorsein, doorseint, doorseinden, doorseinde, doorseinen, doorgeseind, doorgeseinde]", Arrays.toString(synth.synthesize(dummyToken("doorseinen"), "VB.*", true)));
  }

}
