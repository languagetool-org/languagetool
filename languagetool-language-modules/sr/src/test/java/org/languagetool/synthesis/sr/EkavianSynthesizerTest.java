/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.synthesis.sr;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.languagetool.AnalyzedToken;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class EkavianSynthesizerTest {

  @Test
  public final void testSynthesizeString() throws IOException {
    EkavianSynthesizer synth = new EkavianSynthesizer();

    // Negative test - POS tag that does not exist
    assertEquals(synth.synthesize(dummyToken("катаклингер"), "катаклингер").length, 0);

    // Check all cases
    assertEquals("[оловка]", Arrays.toString(synth.synthesize(dummyToken("оловка"), "IM:ZA:ZE:0J:NO")));
    assertEquals("[оловке]", Arrays.toString(synth.synthesize(dummyToken("оловка"), "IM:ZA:ZE:0J:GE")));
    assertEquals("[мајци]", Arrays.toString(synth.synthesize(dummyToken("мајка"), "IM:ZA:ZE:0J:DA")));
    assertEquals("[оловку]", Arrays.toString(synth.synthesize(dummyToken("оловка"), "IM:ZA:ZE:0J:AK")));
    assertEquals("[пушко]", Arrays.toString(synth.synthesize(dummyToken("пушка"), "IM:ZA:ZE:0J:VO", true)));
    assertEquals("[оловком]", Arrays.toString(synth.synthesize(dummyToken("оловка"), "IM:ZA:ZE:0J:IN")));
    assertEquals("[оловци]", Arrays.toString(synth.synthesize(dummyToken("оловка"), "IM:ZA:ZE:0J:LO")));

    // regular expressions
    assertEquals("[оловка, оловка, оловке, оловко, оловком, оловку, оловци, оловци]", Arrays.toString(
            getSortedArray(synth.synthesize(
                    dummyToken("оловка"), "IM:ZA:ZE:0J:.*", true))));
    assertEquals("[један, један, један, једна, једна, једна, једна, једна, једне, једне, једне, једне, једни, једни, једни, једни, једним, једним, једним, једним, једним, једним, једним, једним, једним, једним, једним, једнима, једнима, једнима, једнима, једнима, једнима, једнима, једнима, једнима, једних, једних, једних, једно, једно, једно, једног, једног, једног, једнога, једнога, једнога, једном, једном, једном, једном, једном, једноме, једноме, једноме, једноме, једному, једному, једному, једному, једној, једној, једну]", Arrays.toString(
            getSortedArray(synth.synthesize(
                    dummyToken("један"), "BR:.*:.*:.*:.*", true))));
    assertEquals("[орала, орала, орале, орали, орало, орао]", Arrays.toString(
            getSortedArray(synth.synthesize(
                    dummyToken("орати"), "GL:.*:.*:.*:.*", true))));
    assertEquals("[али]", Arrays.toString(
            getSortedArray(synth.synthesize(
                    dummyToken("али"), "VE:.*", true))));
  }

  @NotNull
  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

  private String[] getSortedArray(String[] ar) {
    String[] newAr = ar.clone();
    Arrays.sort(newAr);
    return newAr;
  }

}