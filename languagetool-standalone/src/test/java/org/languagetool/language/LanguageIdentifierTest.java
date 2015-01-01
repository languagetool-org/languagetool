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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class LanguageIdentifierTest {

  @Test
  public void testDetection() {
    LanguageIdentifier identifier = new LanguageIdentifier(Arrays.asList("en", "de"));
    assertNull(identifier.detectLanguageCode(""));
    assertNull(identifier.detectLanguageCode("X"));
    assertThat(identifier.detectLanguageCode("This is an English text"), is("en"));
    assertThat(identifier.detectLanguageCode("Das ist ein deutscher Text"), is("de"));
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidLanguage() throws IOException {
    new LanguageIdentifier(Arrays.asList("ZZ"));
  }
}
