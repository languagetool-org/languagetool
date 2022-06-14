/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2020 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.synthesis.ar;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.language.Arabic;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ArabicSynthesizerTest {

  @Test
  public final void testSynthesizeStringString() {
    ArabicSynthesizer synth = new ArabicSynthesizer(new Arabic());

    assertEquals(Arrays.toString(synth.synthesize(dummyToken("خيار"), "NJ-;F2--;---")), "[خيارتان, خياريتان]");

    assertEquals(Arrays.toString(synth.synthesize(dummyToken("بلاد"), "NJ-;F3A-;--H")),
      "[بلادت, بلادتي, بلاد, بلادي]");   // assertEquals(Arrays.toString(synth.synthesize(dummyToken("بلاد"), "NJ-;F3A-;--H\\+RP", true)),


    // an example with specific postag with regex flag enabled
    assertEquals(Arrays.toString(synth.synthesize(dummyToken("اِسْتَعْمَلَ"), "V61;M3Y-pa-;--H")),
      "[استعملتمو]");

    // an example with specific postag without regex flag
    assertEquals(Arrays.toString(synth.synthesize(dummyToken("اِسْتَكْمَلَ"), "V61;M3Y-pa-;---")),
      "[استكملتم]");

    // an example with specific postag without regex flag + code flag
    assertEquals(Arrays.toString(synth.synthesize(dummyToken("اِسْتَمَعَ"), "V51;M3Y-pa-;---")),
      "[استمعتم]");

  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

}
