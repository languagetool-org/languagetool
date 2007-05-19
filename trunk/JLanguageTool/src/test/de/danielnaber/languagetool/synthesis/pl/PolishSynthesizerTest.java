package de.danielnaber.languagetool.synthesis.pl;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedToken;

public class PolishSynthesizerTest extends TestCase {
  private final AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }

  public final void testSynthesizeString() throws IOException {
    PolishSynthesizer synth = new PolishSynthesizer();
    assertNull(synth.synthesize(dummyToken("blablabla"), "blablabla"));
    
    assertEquals("[Aaru]", Arrays.toString(synth.synthesize(dummyToken("Aar"), "subst:sg:gen:m3")));
    assertEquals("[Abchazem]", Arrays.toString(synth.synthesize(dummyToken("Abchaz"), "subst:sg:inst:m3")));
    assertEquals("[nieduży]", Arrays.toString(synth.synthesize(dummyToken("duży"), "adj:sg:nom.voc:m1.m2:pos:neg")));        
    assertEquals("[miała]", Arrays.toString(synth.synthesize(dummyToken("mieć"), "verb:praet:sg:ter:f:?perf")));    
    assertEquals("[brzydziej]", Arrays.toString(synth.synthesize(dummyToken("brzydko"), "adv:comp")));
    //with regular expressions
    assertEquals("[tonera]", Arrays.toString(synth.synthesize(dummyToken("toner"), ".*sg:gen.*", true)));
    assertEquals("[niedużego, niedużemu, niedużym, nieduży, nieduży]", Arrays.toString(synth.synthesize(dummyToken("duży"), "adj:sg.*(m[0-9]?|m.n):pos:neg", true)));    
    assertEquals("[miałabym, miałbym, miałabyś, miałbyś, miałaby, miałby, miałoby, miałam, miałem, miałaś, miałeś, miała, miał, miało]", 
          Arrays.toString(synth.synthesize(dummyToken("mieć"), ".*praet:sg.*", true)));
  }

}
