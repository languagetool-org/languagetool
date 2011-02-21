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

package de.danielnaber.languagetool.synthesis.pl;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedToken;

public class PolishSynthesizerTest extends TestCase {
  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

  public final void testSynthesizeString() throws IOException {
    PolishSynthesizer synth = new PolishSynthesizer();
    assertEquals(synth.synthesize(dummyToken("blablabla"), "blablabla").length, 0);

    assertEquals("[Aaru]", Arrays.toString(synth.synthesize(dummyToken("Aar"), "subst:sg:gen:m3")));
    assertEquals("[Abchazem]", Arrays.toString(synth.synthesize(dummyToken("Abchaz"), "subst:sg:inst:m1")));
    assertEquals("[nieduży]", Arrays.toString(synth.synthesize(dummyToken("duży"), "adj:sg:nom:m:pos:neg")));        
    assertEquals("[miała]", Arrays.toString(synth.synthesize(dummyToken("mieć"), "verb:praet:sg:ter:f:imperf")));
        assertEquals("[brzydziej]", Arrays.toString(synth.synthesize(dummyToken("brzydko"), "adv:comp")));
    //with regular expressions
    assertEquals("[tonera]", Arrays.toString(synth.synthesize(dummyToken("toner"), ".*sg.*[\\.:]gen.*", true)));
    assertEquals("[niedużego, nieduży, niedużemu, niedużego, niedużym, nieduży, nieduży]", Arrays.toString(synth.synthesize(dummyToken("duży"), "adj:sg.*(m[0-9]?|m.n):pos:neg", true)));    
    assertEquals("[miałabym, miałbym, miałabyś, miałbyś, miałaby, miałby, miałoby, miałam, miałem, miałaś, miałeś, miała, miał, miało]", 
          Arrays.toString(synth.synthesize(dummyToken("mieć"), ".*praet:sg.*", true)));
  }

}
