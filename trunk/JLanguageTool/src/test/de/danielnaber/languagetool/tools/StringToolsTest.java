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
import java.util.ArrayList;
import java.util.List;

import de.danielnaber.languagetool.Language;

/**
 * @author Daniel Naber
 */
public class StringToolsTest extends TestCase {

  public void testAssureSet() {
    String s = "";
    try {
      StringTools.assureSet(s, "varName");
      fail();
    } catch (IllegalArgumentException e) {
      // expected exception
    }
    s = " \t";
    try {
      StringTools.assureSet(s, "varName");
      fail();
    } catch (IllegalArgumentException e) {
      // expected exception
    }
    s = null;
    try {
      StringTools.assureSet(s, "varName");
      fail();
    } catch (NullPointerException e) {
      // expected exception
    }
    s = "foo";
    StringTools.assureSet(s, "varName");
  }
  
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
  
  public void testIsMixedCase() {
    assertTrue(StringTools.isMixedCase("AbC"));
    assertTrue(StringTools.isMixedCase("MixedCase"));
    assertTrue(StringTools.isMixedCase("iPod"));
    assertTrue(StringTools.isMixedCase("AbCdE"));
    
    assertFalse(StringTools.isMixedCase(""));
    assertFalse(StringTools.isMixedCase("ABC"));
    assertFalse(StringTools.isMixedCase("abc"));
    assertFalse(StringTools.isMixedCase("!"));
    assertFalse(StringTools.isMixedCase("Word"));
  }
  
  public void testIsCapitalizedWord() {
    assertTrue(StringTools.isCapitalizedWord("Abc"));
    assertTrue(StringTools.isCapitalizedWord("Uppercase"));
    assertTrue(StringTools.isCapitalizedWord("Ipod"));    
    
    assertFalse(StringTools.isCapitalizedWord(""));
    assertFalse(StringTools.isCapitalizedWord("ABC"));
    assertFalse(StringTools.isCapitalizedWord("abc"));
    assertFalse(StringTools.isCapitalizedWord("!"));
    assertFalse(StringTools.isCapitalizedWord("wOrD"));
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

  public void testEscapeXMLandHTML() {
    assertEquals("!ä&quot;&lt;&gt;&amp;&amp;", StringTools.escapeXML("!ä\"<>&&"));
    assertEquals("!ä&quot;&lt;&gt;&amp;&amp;", StringTools.escapeHTML("!ä\"<>&&"));
  }
  
  public void testGetContext() {
    String input = "This is a test sentence. Here's another sentence with more text.";
    String result = StringTools.getContext(8, 14, input, 5);
    assertEquals("...s is a test sent...\n        ^^^^^^     ", result);
  }
  
  public void testAddSpace() {
    assertEquals(" ", StringTools.addSpace("word", Language.ENGLISH));
    assertEquals("", StringTools.addSpace(",", Language.ENGLISH));
    assertEquals("", StringTools.addSpace(",", Language.FRENCH));
    assertEquals("", StringTools.addSpace(",", Language.ENGLISH));
    assertEquals(" ", StringTools.addSpace(":", Language.FRENCH));
    assertEquals("", StringTools.addSpace(",", Language.ENGLISH));
    assertEquals(" ", StringTools.addSpace(";", Language.FRENCH));    
  }
  
  public void testGetLabel() {    
    assertEquals("This is a Label", StringTools.getLabel("This is a &Label"));
    assertEquals("Bits & Pieces", StringTools.getLabel("Bits && Pieces"));
  }
  
  public void testGetOOoLabel() {    
    assertEquals("This is a ~Label", StringTools.getOOoLabel("This is a &Label"));
    assertEquals("Bits & Pieces", StringTools.getLabel("Bits && Pieces"));
  }
  
  public void testGetMnemonic() {
    assertEquals('F', StringTools.getMnemonic("&File"));
    assertEquals('O', StringTools.getMnemonic("&OK"));
    assertEquals('\u0000', 
        StringTools.getMnemonic("File && String operations"));
    assertEquals('O', 
      StringTools.getMnemonic("File && String &Operations"));
  }
  
  public void testListToString() {
    final List<String> list = new ArrayList<String>();
    list.add("foo");
    list.add("bar");
    list.add(",");
    assertEquals("foo,bar,,", StringTools.listToString(list, ","));
    assertEquals("foo\tbar\t,", StringTools.listToString(list, "\t"));
  }
  
  public void testIsWhitespace() {
    assertEquals(true, StringTools.isWhitespace("  "));
    assertEquals(true, StringTools.isWhitespace("\t"));
    assertEquals(true, StringTools.isWhitespace("\u2002"));    
    //non-breaking space is not a whitespace
    assertEquals(false, StringTools.isWhitespace("\u00a0"));
    assertEquals(false, StringTools.isWhitespace("abc"));
    //non-breaking OOo field
    assertEquals(false, StringTools.isWhitespace("\\u02"));
    assertEquals(false, StringTools.isWhitespace("\u0001"));
  }
  
  public void testIsPositiveNumber() {
    assertEquals(true, StringTools.isPositiveNumber('3'));
    assertEquals(false, StringTools.isPositiveNumber('a'));      
  }
  
  public void testIsEmpty() {
    assertEquals(true, StringTools.isEmpty(""));
    assertEquals(true, StringTools.isEmpty(null));
    assertEquals(false, StringTools.isEmpty("a"));      
  }

  
}
