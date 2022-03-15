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

package org.languagetool.synthesis.en;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.language.English;

import java.io.IOException;
import java.util.Arrays;

public class EnglishSynthesizerTest {

  private AnalyzedToken dummyToken(String tokenStr, String tokenLemma) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenLemma);
  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

  @Test
  public void testSynthesizeStringString() throws IOException {
    EnglishSynthesizer synth = new EnglishSynthesizer(new English());
    Assertions.assertEquals(synth.synthesize(dummyToken("blablabla"), "blablabla").length, 0);

    Assertions.assertEquals("[was, were]", Arrays.toString(synth.synthesize(dummyToken("be"), "VBD")));
    Assertions.assertEquals("[presidents]", Arrays.toString(synth.synthesize(dummyToken("president"), "NNS")));
    Assertions.assertEquals("[tested]", Arrays.toString(synth.synthesize(dummyToken("test"), "VBD")));
    Assertions.assertEquals("[tested]", Arrays.toString(synth.synthesize(dummyToken("test"), "VBD", false)));
    // with regular expressions
    Assertions.assertEquals("[tested]", Arrays.toString(synth.synthesize(dummyToken("test"), "VBD", true)));
    Assertions.assertEquals("[tested, testing]", Arrays.toString(synth.synthesize(dummyToken("test"), "VBD|VBG", true)));
    // with special indefinite article
    Assertions.assertEquals("[a university, the university]", Arrays.toString(synth.synthesize(dummyToken("university"), "+DT", false)));
    Assertions.assertEquals("[an hour, the hour]", Arrays.toString(synth.synthesize(dummyToken("hour"), "+DT", false)));
    Assertions.assertEquals("[an hour]", Arrays.toString(synth.synthesize(dummyToken("hour"), "+INDT", false)));
    // indefinite article and other changes...
    Assertions.assertEquals("[an hour]", Arrays.toString(synth.synthesize(dummyToken("hours", "hour"), "NN\\+INDT", true)));
    Assertions.assertEquals("[a hexagon]", Arrays.toString(synth.synthesize(dummyToken("hexagon"), "NN|NN:.*\\+INDT", true)));
    // indefinite article and other changes...
    Assertions.assertEquals("[the hour]", Arrays.toString(synth.synthesize(dummyToken("hours", "hour"), "NN\\+DT", true)));
    // from added.txt:
    Assertions.assertEquals("[absolutized]", Arrays.toString(synth.synthesize(dummyToken("absolutize"), "VBD", false)));
    Assertions.assertEquals("[absolutized]", Arrays.toString(synth.synthesize(dummyToken("absolutize"), "VB[XD]", true)));
    // from removed.txt:
    Assertions.assertEquals("[]", Arrays.toString(synth.synthesize(dummyToken("Christmas"), "VBZ", false)));

    Assertions.assertEquals("[twelve]", Arrays.toString(synth.synthesize(dummyToken("12"), "_spell_number_", false)));
    Assertions.assertEquals("[one thousand two hundred forty-three]", Arrays.toString(synth.synthesize(dummyToken("1243"), "_spell_number_", false)));
    Assertions.assertEquals("[twelve]", Arrays.toString(synth.synthesize(dummyToken("12"), "_spell_number_", true)));

    Assertions.assertEquals("[I]", Arrays.toString(synth.synthesize(dummyToken("myself", "I"), "PRP_S1S", true)));

    Assertions.assertEquals("[mixed]", Arrays.toString(synth.synthesize(dummyToken("mix"), "VBD")));
    Assertions.assertEquals("[mixed]", Arrays.toString(synth.synthesize(dummyToken("mix"), "VBN")));
  }

}
