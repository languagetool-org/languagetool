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

package org.languagetool.synthesis.pl;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.language.Polish;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PolishSynthesizerTest {

  @Test
  public final void testSynthesizeString() throws IOException {
    PolishSynthesizer synth = PolishSynthesizer.INSTANCE;
    assertEquals(synth.synthesize(dummyToken("blablabla"), "blablabla").length, 0);

    assertEquals("[Aarona]", Arrays.toString(synth.synthesize(dummyToken("Aaron"), "subst:sg:gen:m1")));
    assertEquals("[Abchazem]", Arrays.toString(synth.synthesize(dummyToken("Abchaz"), "subst:sg:inst:m1")));
    // assertEquals("[nieduży]", Arrays.toString(synth.synthesize(dummyToken("nieduży"), "adj:sg:nom:m:pos:neg")));        
    assertEquals("[miała]", Arrays.toString(synth.synthesize(dummyToken("mieć"), "verb:praet:sg:f:ter:imperf:refl.nonrefl")));
        assertEquals("[brzydziej]", Arrays.toString(synth.synthesize(dummyToken("brzydko"), "adv:com")));
    //with regular expressions
    assertEquals("[tonera]", Arrays.toString(
            getSortedArray(synth.synthesize(dummyToken("toner"), "subst:sg:gen:m.*", true))));
    assertEquals("[niedużego, nieduży]", Arrays.toString(
            getSortedArray(synth.synthesize(dummyToken("nieduży"), "adj:sg.*(m[0-9]?|m.n):pos", true))));    
    assertEquals("[miał, miała, miałam, miałaś, miałem, miałeś, miało, miałom, miałoś]", 
          Arrays.toString(
                  getSortedArray(synth.synthesize(dummyToken("mieć"), ".*praet:sg.*", true))));
  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

  private String[] getSortedArray(String... ar) {
    String[] newAr = ar.clone();
    Arrays.sort(newAr);
    return newAr;
  }
  
}
