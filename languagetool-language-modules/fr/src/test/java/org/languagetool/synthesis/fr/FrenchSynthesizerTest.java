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
package org.languagetool.synthesis.fr;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.language.French;
import org.languagetool.synthesis.FrenchSynthesizer;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FrenchSynthesizerTest {

  @Test
  public final void testSynthesizeStringString() throws IOException {
    FrenchSynthesizer synth = new FrenchSynthesizer(new French());
    assertEquals(synth.synthesize(dummyToken("blablabla"), "blablabla").length, 0);
    
    assertEquals("[nagent]", Arrays.toString(synth.synthesize(dummyToken("nager"), "V ind pres 3 p")));
    assertEquals("[les]", Arrays.toString(synth.synthesize(dummyToken("le"), "D (m|e) (p|sp)", true)));
    assertEquals("[marins]", Arrays.toString(synth.synthesize(dummyToken("marin"), "J [me] (p|sp)", true)));
    assertEquals("[marins]", Arrays.toString(synth.synthesize(new AnalyzedToken("marine", "J f s", "marin"), "J [me] (p|sp)", true)));
    assertEquals("[marinés]", Arrays.toString(synth.synthesize(new AnalyzedToken("marine", "V ind pres 1 s", "mariner"), "V ppa [me] (p|sp)", true)));
  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

}
