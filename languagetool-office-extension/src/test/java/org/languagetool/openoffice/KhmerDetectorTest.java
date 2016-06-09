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

import org.junit.Test;

import static org.junit.Assert.*;

public class KhmerDetectorTest {

  @Test
  public void testIsThisLanguage() {
    KhmerDetector detector = new KhmerDetector();
    
    assertTrue(detector.isThisLanguage("ប៉ុ"));
    assertTrue(detector.isThisLanguage("ប៉ុន្តែ​តើ"));
    assertTrue(detector.isThisLanguage("ហើយដោយ​ព្រោះ​"));
    assertTrue(detector.isThisLanguage("«ទៅ​បាន​។ «"));

    assertFalse(detector.isThisLanguage("Hallo"));
    assertFalse(detector.isThisLanguage("öäü"));

    assertFalse(detector.isThisLanguage(""));
    try {
      assertFalse(detector.isThisLanguage(null));
      fail();
    } catch (NullPointerException ignored) {}
  }
  
}
