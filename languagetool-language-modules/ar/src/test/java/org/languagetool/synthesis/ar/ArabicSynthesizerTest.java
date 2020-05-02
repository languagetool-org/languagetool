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

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ArabicSynthesizerTest {

  @Test
  public final void testSynthesizeStringString() throws IOException {
    ArabicSynthesizer synth = new ArabicSynthesizer(new Arabic());
    assertEquals(Arrays.toString(synth.synthesize(dummyToken("خيار"), "NJ-;F2--;--L")), "[الخيارتان, الخياريتان]");
    assertEquals(Arrays.toString(synth.synthesize(dummyToken("بلاد"), "NJ-;F3A-;--H")),
      "[بلادتك, بلادتي, بلادك, بلادي, بلاديتك, بلاديتي, بلاديك, بلاديي]");

  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

}
