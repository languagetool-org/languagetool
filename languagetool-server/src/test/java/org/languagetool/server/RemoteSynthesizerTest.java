package org.languagetool.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RemoteSynthesizerTest {
  
  @Test
  public void testSynthesis() throws Exception {
    RemoteSynthesizer remoteSynth = new RemoteSynthesizer();
    
    assertEquals(remoteSynth.synthesize("de", "Äußerung","SUB:NOM:PLU:FEM").toString(), "[Äußerungen]");
    // Removing duplicates!
    assertEquals(remoteSynth.synthesize("de", "Äußerung","SUB:.*:PLU:FEM").toString(), "[Äußerungen]");
    assertEquals(remoteSynth.synthesize("pt", "resolver","VMIS3S0").toString(), "[resolveu]");
    assertEquals(remoteSynth.synthesize("es", "cantar","VMIP1S0").toString(), "[canto]");
    assertEquals(remoteSynth.synthesize("es", "señor","NC.P.*").toString(), "[señoras, señores]");
    assertEquals(remoteSynth.synthesize("fr", "monde","N m p").toString(), "[mondes]");
    assertEquals(remoteSynth.synthesize("fr", "chanter","V ppa.*").toString(), "[chantées, chantée, chantés, chanté]");
    assertEquals(remoteSynth.synthesize("en", "be","VBZ").toString(), "[is]");
    assertEquals(remoteSynth.synthesize("en-US", "be","VBZ").toString(), "[is]");
    assertEquals(remoteSynth.synthesize("en-GB", "be","VBZ").toString(), "[is]");
    assertEquals(remoteSynth.synthesize("en", "be","V.*").toString(), "[be, was, were, being, been, are, is]");
    assertEquals(remoteSynth.synthesize("en", "be","N.*").toString(), "[]");
    
  }
  
}
