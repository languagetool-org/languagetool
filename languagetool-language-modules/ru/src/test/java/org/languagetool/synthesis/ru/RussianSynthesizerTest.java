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

package org.languagetool.synthesis.ru;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.language.Russian;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class RussianSynthesizerTest {
  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

  @Test
  public final void testSynthesizeString() throws IOException {
    RussianSynthesizer synth = RussianSynthesizer.INSTANCE;
    assertEquals(synth.synthesize(dummyToken("blablabla"), "blablabla").length, 0);

    assertEquals("[семья]", Arrays.toString(synth.synthesize(dummyToken("семья"), "NN:Inanim:Fem:Sin:Nom")));
    assertEquals("[семьи]", Arrays.toString(synth.synthesize(dummyToken("семья"), "NN:Inanim:Fem:Sin:R")));
  }

}