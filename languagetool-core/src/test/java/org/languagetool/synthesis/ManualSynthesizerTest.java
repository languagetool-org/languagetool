/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayInputStream;
import java.io.IOException;


/**
 * @author Ionuț Păduraru
 */
public class ManualSynthesizerTest {

  private ManualSynthesizer synthesizer;

  @BeforeEach
  public void setUp() throws Exception {
    String data = 
      "# some test data\n" +
      "InflectedForm11\tLemma1\tPOS1\n" +
      "InflectedForm121\tLemma1\tPOS2\n" +
      "InflectedForm122\tLemma1\tPOS2\n" +
      "InflectedForm2\tLemma2\tPOS1\n";
    synthesizer = new ManualSynthesizer(new ByteArrayInputStream(data.getBytes("UTF-8")));
  }

  @Test
  public void testLookupNonExisting() throws IOException {
    Assertions.assertNull(synthesizer.lookup("", ""));
    Assertions.assertNull(synthesizer.lookup("", null));
    Assertions.assertNull(synthesizer.lookup(null, ""));
    Assertions.assertNull(synthesizer.lookup(null, null));
    Assertions.assertNull(synthesizer.lookup("NONE", "UNKNOWN"));
  }

  /**
   * Lookup values that do not exist in the dictionary but they do exist in different form (like other POS).
   */
  @Test
  public void testInvalidLookup() throws IOException {
    Assertions.assertNull(synthesizer.lookup("NONE", "POS1"));
    Assertions.assertNull(synthesizer.lookup("Lemma1", "UNKNOWN"));
    Assertions.assertNull(synthesizer.lookup("Lemma1", "POS.")); // no reg exp
    Assertions.assertNull(synthesizer.lookup("Lemma2", "POS2"));
  }

  @Test
  public void testValidLookup() throws IOException {
    Assertions.assertEquals("[InflectedForm11]", String.valueOf(synthesizer.lookup("Lemma1", "POS1")));
    Assertions.assertEquals("[InflectedForm121, InflectedForm122]", String.valueOf(synthesizer.lookup("Lemma1", "POS2")));
    Assertions.assertEquals("[InflectedForm2]", String.valueOf(synthesizer.lookup("Lemma2", "POS1")));
  }

  @Test
  public void testCaseSensitive() throws IOException {
    // lookup is case sensitive:
    Assertions.assertNull(synthesizer.lookup("LEmma1", "POS1"));
  }
  
}
