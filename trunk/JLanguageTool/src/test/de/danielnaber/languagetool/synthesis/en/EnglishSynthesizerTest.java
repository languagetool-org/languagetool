package de.danielnaber.languagetool.synthesis.en;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

public class EnglishSynthesizerTest extends TestCase {

  public final void testSynthesizeStringString() throws IOException {
    EnglishSynthesizer synth = new EnglishSynthesizer();
    assertNull(synth.synthesize("blablabla", "blablabla"));
    
    assertEquals("[were, wast, was]", Arrays.toString(synth.synthesize("be", "VBD")));
    assertEquals("[presidents]", Arrays.toString(synth.synthesize("president", "NNS")));
    assertEquals("[tested]", Arrays.toString(synth.synthesize("test", "VBD")));
    assertEquals("[tested]", Arrays.toString(synth.synthesize("test", "VBD", false)));
    //with regular expressions
    assertEquals("[tested]", Arrays.toString(synth.synthesize("test", "VBD", true)));    
    assertEquals("[tested, testing]", Arrays.toString(synth.synthesize("test", "VBD|VBG", true)));
  }

}
