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

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.language.Catalan;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CatalanSynthesizerTest {

  private final CatalanSynthesizer synth = new CatalanSynthesizer(new Catalan());

  @Test
  public final void testSynthesizeStringString() throws IOException {
      
    assertEquals("[un]", synth("1", "_spell_number_"));
    assertEquals("[onze]", synth("11", "_spell_number_"));
    assertEquals("[vint-i-un]", synth("21", "_spell_number_"));
    assertEquals("[vint-i-quatre]", synth("24", "_spell_number_"));
    assertEquals("[vint-i-una]", synth("21", "_spell_number_:feminine"));
    assertEquals("[vint-i-dues]", synth("22", "_spell_number_:feminine"));
    assertEquals("[dos]", synth("2", "_spell_number_"));
    assertEquals("[dues]", synth("2", "_spell_number_:feminine"));
    assertEquals("[dues-centes quaranta-dues]", synth("242", "_spell_number_:feminine"));
    assertEquals("[dos milions dues-centes cinquanta-una mil dues-centes quaranta-una]", synth("2251241", "_spell_number_:feminine"));
    
    assertEquals(0, synth.synthesize(dummyToken("blablabla"), "blablabla").length);

    assertEquals("[sento]", synth("sentir", "VMIP1S0C"));
    assertEquals("[sent]", synth("sentir", "VMIP1S0Z"));
    assertEquals("[sent]", synth("sentir", "VMIP1S0V"));
    assertEquals("[sent]", synth("sentir", "VMIP1S0B"));
    assertEquals("[senta]", synth("sentir", "VMSP3S0V"));
    assertEquals("[nostres]", synth("nostre", "PX1CP0P0"));
    assertEquals("[presidents]", synth("president", "NCMP000"));
    assertEquals("[comprovat]", synth("comprovar", "VMP00SM.?"));
    assertEquals("[arribe, arribi]", synth("arribar", "VMSP3S00"));
    assertEquals("[arribe, arribi]", synthRegex("arribar", "VMSP3S.0"));
    assertEquals("[albèrxics]", synthRegex("albèrxic", "NCMP000"));
    
    assertEquals("[faig servir]", synth("fer servir", "VMIP1S0C"));

    //with regular expressions:
    assertEquals("[comprovades, comprovats, comprovada, comprovat]", synthRegex("comprovar", "V.P.*"));
    assertEquals("[contestant, contestar]", synthRegex("contestar", "VM[GN]0000.?"));

    //with special definite article:
    assertEquals("[les universitats, la universitat]", synthNonRegex("universitat", "DT"));
    assertEquals("[les úniques, l'única, els únics, l'únic]", synthNonRegex("únic", "DT"));
    assertEquals("[per les úniques, per l'única, pels únics, per l'únic]", synthNonRegex("únic", "DTper"));
    assertEquals("[per la covid]", synthNonRegex("covid", "DTper"));
  }

  private String synth(String word, String pos) throws IOException {
    return Arrays.toString(synth.synthesize(dummyToken(word), pos));
  }

  private String synthRegex(String word, String pos) throws IOException {
    return Arrays.toString(synth.synthesize(dummyToken(word), pos, true));
  }

  private String synthNonRegex(String word, String pos) throws IOException {
    return Arrays.toString(synth.synthesize(dummyToken(word), pos, false));
  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

}
