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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.language.Catalan;

import java.io.IOException;
import java.util.Arrays;

public class CatalanSynthesizerTest {

  private final CatalanSynthesizer synth = new CatalanSynthesizer(new Catalan());

  @Test
  public final void testSynthesizeStringString() throws IOException {
      
    Assertions.assertEquals("[un]", synth("1", "_spell_number_"));
    Assertions.assertEquals("[onze]", synth("11", "_spell_number_"));
    Assertions.assertEquals("[vint-i-un]", synth("21", "_spell_number_"));
    Assertions.assertEquals("[vint-i-quatre]", synth("24", "_spell_number_"));
    Assertions.assertEquals("[vint-i-una]", synth("21", "_spell_number_:feminine"));
    Assertions.assertEquals("[vint-i-dues]", synth("22", "_spell_number_:feminine"));
    Assertions.assertEquals("[dos]", synth("2", "_spell_number_"));
    Assertions.assertEquals("[dues]", synth("2", "_spell_number_:feminine"));
    Assertions.assertEquals("[dues-centes quaranta-dues]", synth("242", "_spell_number_:feminine"));
    Assertions.assertEquals("[dos milions dues-centes cinquanta-una mil dues-centes quaranta-una]", synth("2251241", "_spell_number_:feminine"));
    
    Assertions.assertEquals(0, synth.synthesize(dummyToken("blablabla"), "blablabla").length);

    Assertions.assertEquals("[sento]", synth("sentir", "VMIP1S0C"));
    Assertions.assertEquals("[sent]", synth("sentir", "VMIP1S0Z"));
    Assertions.assertEquals("[sent]", synth("sentir", "VMIP1S0V"));
    Assertions.assertEquals("[sent]", synth("sentir", "VMIP1S0B"));
    Assertions.assertEquals("[senta]", synth("sentir", "VMSP3S0V"));
    Assertions.assertEquals("[nostres]", synth("nostre", "PX1CP0P0"));
    Assertions.assertEquals("[presidents]", synth("president", "NCMP000"));
    Assertions.assertEquals("[comprovat]", synth("comprovar", "VMP00SM.?"));
    Assertions.assertEquals("[arribe, arribi]", synth("arribar", "VMSP3S00"));
    Assertions.assertEquals("[arribe, arribi]", synthRegex("arribar", "VMSP3S.0"));
    Assertions.assertEquals("[albèrxics]", synthRegex("albèrxic", "NCMP000"));
    
    Assertions.assertEquals("[faig servir]", synth("fer servir", "VMIP1S0C"));

    //with regular expressions:
    Assertions.assertEquals("[comprovades, comprovats, comprovada, comprovat]", synthRegex("comprovar", "V.P.*"));
    Assertions.assertEquals("[contestant, contestar]", synthRegex("contestar", "VM[GN]0000.?"));

    //with special definite article:
    Assertions.assertEquals("[les universitats, la universitat]", synthNonRegex("universitat", "DT"));
    Assertions.assertEquals("[les úniques, l'única, els únics, l'únic]", synthNonRegex("únic", "DT"));
    Assertions.assertEquals("[per les úniques, per l'única, pels únics, per l'únic]", synthNonRegex("únic", "DTper"));
    Assertions.assertEquals("[per la covid]", synthNonRegex("covid", "DTper"));
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
