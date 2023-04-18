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

package org.languagetool.synthesis.uk;

import org.junit.Test;
import org.languagetool.AnalyzedToken;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class UkrainianSynthesizerTest {

  @Test
  public final void testSynthesizeString() throws IOException {
    UkrainianSynthesizer synth = UkrainianSynthesizer.INSTANCE;

    assertEquals(synth.synthesize(dummyToken("щосьтамтаке"), "щосьтамтаке").length, 0);

//    assertEquals("[Андрія]", Arrays.toString(synth.synthesize(dummyToken("Андрій"), "noun:m:v_rod")));
    assertEquals("[міста]", Arrays.toString(synth.synthesize(dummyToken("місто"), "noun:inanim:n:v_rod")));

    //with regular expressions
    assertEquals("[червоної]", Arrays.toString(
            getSortedArray(synth.synthesize(dummyToken("червоний"), "adj:f:v_rod.*", true))));
    assertEquals("[червоної]", Arrays.toString(
            getSortedArray(synth.synthesize(dummyToken("червоний"), "adj:f:v_rod:compb", true))));
    assertEquals("[червоним, червоним, червоними, червоною]", Arrays.toString(
            getSortedArray(synth.synthesize(dummyToken("червоний"), "adj:.:v_oru:compb", true))));
  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

  private String[] getSortedArray(String[] ar) {
    String[] newAr = ar.clone();
    Arrays.sort(newAr);
    return newAr;
  }

}
