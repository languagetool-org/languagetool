package org.languagetool.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

public class RemoteSynthesizerTest {

  @Test
  public void testSynthesis() throws Exception {
    RemoteSynthesizer remoteSynth = new RemoteSynthesizer();

    assertEquals(remoteSynth.synthesize("de", "Äußerung", "SUB:NOM:PLU:FEM", false).toString(), "[Äußerungen]");
    // Removing duplicates!
    assertEquals(remoteSynth.synthesize("de", "Äußerung", "SUB:.*:PLU:FEM", true).toString(), "[Äußerungen]");
    assertEquals(remoteSynth.synthesize("pt", "resolver", "VMIS3S0", false).toString(), "[resolveu]");
    assertEquals(remoteSynth.synthesize("es", "cantar", "VMIP1S0", false).toString(), "[canto]");
    assertEquals(remoteSynth.synthesize("es", "señor", "NC.P.*", true).toString(), "[señoras, señores]");
    assertEquals(remoteSynth.synthesize("fr", "monde", "N m p", false).toString(), "[mondes]");
    assertEquals(remoteSynth.synthesize("fr", "chanter", "V ppa.*", true).toString(),
        "[chantées, chantée, chantés, chanté]");
    assertEquals(remoteSynth.synthesize("en", "be", "VBZ", false).toString(), "[is]");
    assertEquals(remoteSynth.synthesize("en-US", "be", "VBZ", false).toString(), "[is]");
    assertEquals(remoteSynth.synthesize("en-GB", "be", "VBZ", false).toString(), "[is]");
    assertEquals(remoteSynth.synthesize("en", "be", "V.*", true).toString(), "[be, was, were, being, been, are, is]");
    assertEquals(remoteSynth.synthesize("en", "be", "N.*", true).toString(), "[]");

    AnalyzedToken atr = new AnalyzedToken("is", "VBZ", "be");
    AnalyzedTokenReadings atrs = new AnalyzedTokenReadings(atr);
    atrs.addReading(new AnalyzedToken("is", "NN", "is"), "");
    assertEquals(remoteSynth.synthesize("en", atrs, true, "(V.)(.)", "$1$2", "come").toString(), "[comes]");
    assertEquals(remoteSynth.synthesize("en", atrs, true, "(V.)(.)", "$1D", "").toString(), "[was, were]");
    assertEquals(remoteSynth.synthesize("en", atrs, true, "(V.)(.)", "$1D", "sing").toString(), "[sang]");

    AnalyzedToken atr2 = new AnalyzedToken("extraño", "AQ0MS0", "extraño");
    AnalyzedTokenReadings atrs2 = new AnalyzedTokenReadings(atr2);
    atrs2.addReading(new AnalyzedToken("extraño", "NCMS000", "extraño"), "");
    atrs2.addReading(new AnalyzedToken("extraño", "VMIP1S0", "extrañar"), "");
    assertEquals(remoteSynth.synthesize("es", atrs2, true, "AQ0(.)(.).", "V.P..$2$1", "extrañar").toString(),
        "[extrañado]");
    assertEquals(remoteSynth.synthesize("es", atrs2, true, "(VM)IP1S0", "$1G....", "").toString(), "[extrañando]");
    // catched error
    assertEquals(remoteSynth.synthesize("es", atrs2, true, "(VM)IP1S0", "$2G....", ""), null);

  }

}
