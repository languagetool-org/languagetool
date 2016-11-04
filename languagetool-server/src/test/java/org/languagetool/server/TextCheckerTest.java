/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TextCheckerTest {

  private final String english = "This is clearly an English text, should be easy to detect.";
  private final TextChecker checker = new V2TextChecker(new HTTPServerConfig(), false);

  @Test
  public void testDetectLanguageOfString() {
    assertThat(checker.detectLanguageOfString("", "en", Arrays.asList("en-GB")).getShortCodeWithCountryAndVariant(), is("en-GB"));
    assertThat(checker.detectLanguageOfString("X", "en", Arrays.asList("en-GB")).getShortCodeWithCountryAndVariant(), is("en-GB"));
    assertThat(checker.detectLanguageOfString("X", "en", Arrays.asList("en-ZA")).getShortCodeWithCountryAndVariant(), is("en-ZA"));
    assertThat(checker.detectLanguageOfString(english, "de", Arrays.asList("en-GB", "de-AT")).getShortCodeWithCountryAndVariant(), is("en-GB"));
    assertThat(checker.detectLanguageOfString(english, "de", Arrays.asList()).getShortCodeWithCountryAndVariant(), is("en-US"));
    assertThat(checker.detectLanguageOfString(english, "de", Arrays.asList("de-AT", "en-ZA")).getShortCodeWithCountryAndVariant(), is("en-ZA"));
    String german = "Das hier ist klar ein deutscher Text, sollte gut zu erkennen sein.";
    assertThat(checker.detectLanguageOfString(german, "fr", Arrays.asList("de-AT", "en-ZA")).getShortCodeWithCountryAndVariant(), is("de-AT"));
    assertThat(checker.detectLanguageOfString(german, "fr", Arrays.asList("de-at", "en-ZA")).getShortCodeWithCountryAndVariant(), is("de-AT"));
    assertThat(checker.detectLanguageOfString(german, "fr", Arrays.asList()).getShortCodeWithCountryAndVariant(), is("de-DE"));
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidPreferredVariant() {
    checker.detectLanguageOfString(english, "de", Arrays.asList("en"));  // that's not a variant
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidPreferredVariant2() {
    checker.detectLanguageOfString(english, "de", Arrays.asList("en-YY"));  // variant doesn't exist
  }

}
