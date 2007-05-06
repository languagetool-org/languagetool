package de.danielnaber.languagetool.synthesis.en;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedToken;

public class EnglishSynthesizerTest extends TestCase {

  private final AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }
  public final void testSynthesizeStringString() throws IOException {
    EnglishSynthesizer synth = new EnglishSynthesizer();
    assertNull(synth.synthesize(dummyToken("blablabla"), 
        "blablabla"));
    
    assertEquals("[were, wast, was]", Arrays.toString(synth.synthesize(dummyToken("be"), "VBD")));
    assertEquals("[presidents]", Arrays.toString(synth.synthesize(dummyToken("president"), "NNS")));
    assertEquals("[tested]", Arrays.toString(synth.synthesize(dummyToken("test"), "VBD")));
    assertEquals("[tested]", Arrays.toString(synth.synthesize(dummyToken("test"), "VBD", false)));
    //with regular expressions
    assertEquals("[tested]", Arrays.toString(synth.synthesize(dummyToken("test"), "VBD", true)));    
    assertEquals("[tested, testing]", Arrays.toString(synth.synthesize(dummyToken("test"), "VBD|VBG", true)));
    //with special indefinite article
    assertEquals("[a university, the university]", Arrays.toString(synth.synthesize(dummyToken("university"), "+DT", false)));
    assertEquals("[an hour, the hour]", Arrays.toString(synth.synthesize(dummyToken("hour"), "+DT", false)));
    assertEquals("[an hour]", Arrays.toString(synth.synthesize(dummyToken("hour"), "+INDT", false)));
  }

}
