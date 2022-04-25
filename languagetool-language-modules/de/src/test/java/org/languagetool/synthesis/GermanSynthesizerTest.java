/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.synthesis;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedToken;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GermanSynthesizerTest {

  private final GermanSynthesizer synthesizer = GermanSynthesizer.INSTANCE;

  @Ignore("for interactive debugging only")
  @Test
  public void testSynthesizeX() throws IOException {
    String token = "musst";
    String lemma = "müssen";
    String posTag = "VER:MOD:2:SIN:PRÄ";
    String targetPosTag = ".*:PRT.*";
    String[] result = synthesizer.synthesize(new AnalyzedToken(token, posTag, lemma), targetPosTag, true);
    System.out.println("=> " + Arrays.toString(result));
  }

  @Test
  public void testSynthesize() throws IOException {
    assertThat(synth("123", "_spell_number_"), is("[einhundertdreiundzwanzig]"));
    assertThat(synth("Zug", "SUB:DAT:SIN:MAS"), is("[Zug]"));
    assertThat(synth("Tisch", "SUB:DAT:SIN:MAS"), is("[Tisch]"));
    assertThat(synth("Buschfeuer", "SUB:GEN:SIN:NEU"), is("[Buschfeuers]"));
    assertThat(synth("Äußerung", "SUB:NOM:PLU:FEM"), is("[Äußerungen]"));
    assertThat(synth("Äußerung", "SUB:NOM:PLU:MAS"), is("[]"));
    assertThat(synth("Haus", "SUB:AKK:PLU:NEU"), is("[Häuser]"));
    assertThat(synth("Haus", ".*", true), is("[Häuser, Haus, Häusern, Haus, Hause, Häuser, Hauses, Häuser, Haus]"));
    assertThat(synthesizer.synthesize(new AnalyzedToken("fake", "FAKE", null), "FAKE", false).length, is(0));  // could happen with soft hyphen in the input
  }

  @Test
  public void testSynthesizeCompounds() throws IOException {
    assertThat(synth("Regelsystem", "SUB:NOM:PLU:NEU"), is("[Regelsysteme]"));
    assertThat(synth("Regelsystem", "SUB:DAT:PLU:NEU"), is("[Regelsystemen]"));
    assertThat(synth("Regelsystem", ".*:PLU:.*", true), is("[Regelsysteme, Regelsystemen]"));
    assertThat(synth("Regel-System", ".*:PLU:.*", true), is("[Regelsysteme, Regelsystemen]"));
    assertThat(synth("Kühlschrankversuch", ".*:PLU:.*", true), is("[Kühlschrankversuche, Kühlschrankversuchen]"));
    assertThat(synth("Kühlschrank-Versuch", ".*:PLU:.*", true), is("[Kühlschrankversuche, Kühlschrankversuchen]"));
  }

  @Test
  public void testMorfologikBug() throws IOException {
    // see https://github.com/languagetool-org/languagetool/issues/586
    assertThat(synth("anfragen", "VER:1:PLU:KJ1:SFT:NEB"), is("[anfragen]"));
  }

  private String synth(String word, String posTag) throws IOException {
    return Arrays.toString(synthesizer.synthesize(dummyToken(word), posTag));
  }

  private String synth(String word, String posTag, boolean regEx) throws IOException {
    return Arrays.toString(synthesizer.synthesize(dummyToken(word), posTag, regEx));
  }

  private AnalyzedToken dummyToken(String tokenStr) {
    return new AnalyzedToken(tokenStr, tokenStr, tokenStr);
  }
}
