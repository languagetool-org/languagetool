/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

public class KhmerDetectorTest {

  @Test
  public void testIsThisLanguage() {
    KhmerDetector detector = new KhmerDetector();
    
    Assertions.assertTrue(detector.isThisLanguage("ប៉ុ"));
    Assertions.assertTrue(detector.isThisLanguage("ប៉ុន្តែ​តើ"));
    Assertions.assertTrue(detector.isThisLanguage("ហើយដោយ​ព្រោះ​"));
    Assertions.assertTrue(detector.isThisLanguage("«ទៅ​បាន​។ «"));

    Assertions.assertFalse(detector.isThisLanguage("Hallo"));
    Assertions.assertFalse(detector.isThisLanguage("öäü"));

    Assertions.assertFalse(detector.isThisLanguage(""));
    try {
      Assertions.assertFalse(detector.isThisLanguage(null));
      Assertions.fail();
    } catch (NullPointerException ignored) {}
  }
  
}
