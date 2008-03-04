package de.danielnaber.languagetool.synthesis.nl;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import de.danielnaber.languagetool.AnalyzedToken;

public class DutchSynthesizerTest extends TestCase {

  private final AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }
  public final void testSynthesizeStringString() throws IOException {
    DutchSynthesizer synth = new DutchSynthesizer();
    assertNull(synth.synthesize(dummyToken("blablabla"), 
        "blablabla"));
    
    assertEquals("[Ajaxsupportertje]", Arrays.toString(synth.synthesize(dummyToken("Ajaxsupporter"), "NN1r")));
    assertEquals("[80-jarigen]", Arrays.toString(synth.synthesize(dummyToken("80-jarige"), "NN2")));
    //with regular expressions
    assertEquals("[doorgeseind]", Arrays.toString(synth.synthesize(dummyToken("doorseinen"), "VBp", true)));    
    assertEquals("[doorgeseind, doorgeseinde]", Arrays.toString(synth.synthesize(dummyToken("doorseinen"), "VBp.*", true)));
  }

}
