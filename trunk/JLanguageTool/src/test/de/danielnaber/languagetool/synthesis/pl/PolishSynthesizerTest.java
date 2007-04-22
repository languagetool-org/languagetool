package de.danielnaber.languagetool.synthesis.pl;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

public class PolishSynthesizerTest extends TestCase {
  public final void testSynthesizeStringString() throws IOException {
    PolishSynthesizer synth = new PolishSynthesizer();
    assertNull(synth.synthesize("blablabla", "blablabla"));
    
    assertEquals("[Aaru]", Arrays.toString(synth.synthesize("Aar", "subst:sg:gen:m3")));
    assertEquals("[Abchazem]", Arrays.toString(synth.synthesize("Abchaz", "subst:sg:inst:m3")));
    assertEquals("[nieduży]", Arrays.toString(synth.synthesize("duży", "adj:sg:nom:m:neg")));
//FIXME: lametyzator bug, will be fixed 
    //assertEquals("[miała]", Arrays.toString(synth.synthesize("mieć", "verb:praet:sg:ter:f:?perf")));    
    //with regular expressions
    assertEquals("[tonera]", Arrays.toString(synth.synthesize("toner", ".*sg:gen.*", true)));
    assertEquals("[niedużego, niedużemu, nieduży]", Arrays.toString(synth.synthesize("duży", "adj:sg.*m:neg", true)));    
//FIXME: lametyzator bug, will be fixed    
//    assertEquals("[miał]", Arrays.toString(synth.synthesize("mieć", ".*praet:sg.*", true)));
  }

}
