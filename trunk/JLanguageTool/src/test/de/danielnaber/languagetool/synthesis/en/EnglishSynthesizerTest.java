package de.danielnaber.languagetool.synthesis.en;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

public class EnglishSynthesizerTest extends TestCase {

  public final void testSynthesizeStringString() throws IOException {
    EnglishSynthesizer synth = new EnglishSynthesizer();
    assertNull(synth.synthesize("blablabla", "blablabla"));
    
    assertEquals("[presidents]", Arrays.toString(synth.synthesize("president", "NNS")));
    assertEquals("[tested]", Arrays.toString(synth.synthesize("test", "VBD")));
  }

}
