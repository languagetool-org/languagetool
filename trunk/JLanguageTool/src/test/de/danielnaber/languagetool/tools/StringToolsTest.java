/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.tools;

import junit.framework.TestCase;

/**
 * @author Daniel Naber
 */
public class StringToolsTest extends TestCase {

  public void testIsAllUppercase() {
    assertTrue(StringTools.isAllUppercase("A"));
    assertTrue(StringTools.isAllUppercase("ABC"));
    assertTrue(StringTools.isAllUppercase("ASV-EDR"));
    assertTrue(StringTools.isAllUppercase("ASV-ÖÄÜ"));
    assertTrue(StringTools.isAllUppercase(""));
    
    assertFalse(StringTools.isAllUppercase("ß"));
    assertFalse(StringTools.isAllUppercase("AAAAAAAAAAAAq"));
    assertFalse(StringTools.isAllUppercase("a"));
    assertFalse(StringTools.isAllUppercase("abc"));
  }

  public void testStartsWithUppercase() {
    assertTrue(StringTools.startsWithUppercase("A"));
    assertTrue(StringTools.startsWithUppercase("ÄÖ"));
    
    assertFalse(StringTools.startsWithUppercase(""));
    assertFalse(StringTools.startsWithUppercase("ß"));
    assertFalse(StringTools.startsWithUppercase("-"));
  }

  public void testUppercaseFirstChar() {
    assertEquals("", StringTools.uppercaseFirstChar(""));
    assertEquals("A", StringTools.uppercaseFirstChar("A"));
    assertEquals("Öäü", StringTools.uppercaseFirstChar("öäü"));
    assertEquals("ßa", StringTools.uppercaseFirstChar("ßa"));
  }

}
