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
   // assertEquals(Arrays.toString(synth.synthesize(dummyToken("بلاد"), "NJ-;F3A-;--H\\+RP", true)),
    //  "[بلادتك, بلادتي, بلادك, بلادي, بلاديتك, بلاديتي, بلاديك, بلاديي]");
    assertEquals(Arrays.toString(synth.synthesize(dummyToken("بلاد"), "NJ-;F3A-;--H(\\+RP)?")),
      "[بلاد, بلادة, بلادي, بلاديا, بلادية]");
      // an example with specific postag with regex flag enabled
 assertEquals(Arrays.toString(synth.synthesize(dummyToken("اِسْتَعْمَلَ"), "V-1;M3Y-pa-;--H(\\+RP)?")),
      "[استعملتم]");
      // an example with specific postag without regex flag
    assertEquals(Arrays.toString(synth.synthesize(dummyToken("اِسْتَكْمَلَ"), "V-1;M3Y-pa-;---")),
      "[استكملتم]");
      //~ // an example with regexg postag with regex flag     
    //~ assertEquals(Arrays.toString(synth.synthesize(dummyToken("اِسْتَعْمَرَ"), "V.*\\+RP", true)),
      //~ "[استعمرتم]");      
     // an example with specific postag without regex flag + code flag
    assertEquals(Arrays.toString(synth.synthesize(dummyToken("اِسْتَمَعَ"), "V-1;M3Y-pa-;---(\\+RP)?")),
      "[استمعتم]");
     // an example with specific postag without regex flag + code flag
    //~ assertEquals(Arrays.toString(synth.synthesize(dummyToken("اِسْتَبَقَ"), "V.*\\+RP", true)),
     //~ "[استبقتم]");      
     // an example with specific postag with regex flag
    //~ assertEquals(Arrays.toString(synth.synthesize(dummyToken("احتجتموهَ"), "V.*", true)),
    //~ "[يحتاجونه]");         
     // an example with specific postag with
    //~ assertEquals(Arrays.toString(synth.synthesize(dummyToken("اِسْتَلَمَ"), "V.*\\+RP", true)),
//~ "[تستلمن, تستلم, تستلمك, تستلم, تستلمك, تستلمنك, تستلم, تستلمك, تستلمن, تستلم, تستلم, تستلم, استلمت, استلمتك, استلمت, تستلمن, تستلمي, تستلميك, تستلمي, تستلميك, تستلمنك, تستلمين, تستلمينك, تستلمن, تستلمي, تستلمي, تستلمين, استلمن, استلمي, استلمنك, استلميك, استلمت, استلمتك, استلمت, تستلمان, تستلما, تستلماك, تستلما, تستلماك, تستلمانك, تستلمان, تستلمانك, تستلمان, تستلما, تستلما, تستلمان, استلمتا, استلمتاك, استلمتا, يستلمنان, يستلمن, يستلمنك, يستلمن, يستلمنك, يستلمنانك, يستلمن, يستلمنك, يستلمنان, يستلمن, يستلمن, يستلمن, استلمن, استلمنك, استلمن, تستلمنان, تستلمن, تستلمنك, تستلمن, تستلمنك, تستلمنانك, تستلمن, تستلمنك, تستلمنان, تستلمن, تستلمن, تستلمن, استلمن, استلمنان, استلمنانك, استلمنك, استلمتن, استلمتنك, استلمتن, يستلمن, يستلم, يستلمك, يستلم, يستلمك, يستلمنك, يستلم, يستلمك, يستلمن, يستلم, يستلم, يستلم, استلم, استلمك, استلم, أستلمن, أستلم, أستلمك, أستلم, أستلمك, أستلمنك, أستلم, أستلمك, أستلمن, أستلم, أستلم, أستلم, استلمت, استلمتك, استلمت, تستلمن, تستلم, تستلمك, تستلم, تستلمك, تستلمنك, تستلم, تستلمك, تستلمن, تستلم, تستلم, تستلم, استلم, استلمن, استلمك, استلمنك, استلمت, استلمتك, استلمت, يستلمان, يستلما, يستلماك, يستلما, يستلماك, يستلمانك, يستلمان, يستلمانك, يستلمان, يستلما, يستلما, يستلمان, استلما, استلماك, استلما, تستلمان, تستلما, تستلماك, تستلما, تستلماك, تستلمانك, تستلمان, تستلمانك, تستلمان, تستلما, تستلما, تستلمان, استلما, استلمان, استلماك, استلمانك, استلمتما, استلمتماك, استلمتما, يستلمن, يستلموا, يستلموك, يستلموا, يستلموك, يستلمنك, يستلمون, يستلمونك, يستلمن, يستلموا, يستلموا, يستلمون, استلموا, استلموك, استلموا, نستلمن, نستلم, نستلمك, نستلم, نستلمك, نستلمنك, نستلم, نستلمك, نستلمن, نستلم, نستلم, نستلم, استلمنا, استلمناك, استلمنا, تستلمن, تستلموا, تستلموك, تستلموا, تستلموك, تستلمنك, تستلمون, تستلمونك, تستلمن, تستلموا, تستلموا, تستلمون, استلمن, استلموا, استلمنك, استلموك, استلمتم, استلمتموك, استلمتم]");
 
      

  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

}
