/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.*;

public class LanguageIdentifierTest {

  private final LanguageIdentifier identifier = new LanguageIdentifier();

  @Test
  public void testDetection() {
    langAssert(null, "");
    langAssert(null, "X");
    langAssert("de", "Das ist ein deutscher Text");
    langAssert("en", "This is an English text");
    langAssert("fr", "Le mont Revard est un sommet du département français ...");
    langAssert("km", "អ្នក\u200Bអាច\u200Bជួយ\u200Bលើក\u200Bស្ទួយ\u200Bវិគីភីឌាភាសាខ្មែរ\u200Bនេះ\u200Bឱ្យ\u200Bមាន\u200Bលក្ខណៈ");
    langAssert("eo", "Imperiestraj pingvenoj manĝas ĉefe krustacojn kaj malgrandajn ...");
  }

  @Test
  public void testKnownLimitations() {
    // not activated because it impairs detection of Spanish, so ast and gl may be mis-detected:
    langAssert("es", "L'Iberorrománicu o Iberromance ye un subgrupu de llingües romances que posiblemente ...");
    langAssert(null, "Dodro é un concello da provincia da Coruña pertencente á comarca do Sar ...");
  }

  private void langAssert(String expectedLangCode, String text) {
    String detectedLangCode = identifier.detectLanguageCode(text);
    if (!Objects.equals(expectedLangCode, detectedLangCode)) {
      fail("Got '" + detectedLangCode + "', expected '" + expectedLangCode + "' for '" + text + "'");
    }
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidLanguage() throws IOException {
    new LanguageIdentifier(Arrays.asList("ZZ"));
  }
}
