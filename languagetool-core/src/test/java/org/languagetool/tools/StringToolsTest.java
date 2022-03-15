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
package org.languagetool.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.languagetool.FakeLanguage;
import org.languagetool.Language;
import org.languagetool.TestTools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Naber
 */
public class StringToolsTest {

  @Test
  public void testAssureSet() {
    try {
      StringTools.assureSet("", "varName");
      Assertions.fail();
    } catch (IllegalArgumentException ignored) {}
    try {
      StringTools.assureSet(" \t", "varName");
      Assertions.fail();
    } catch (IllegalArgumentException ignored) {}
    try {
      StringTools.assureSet(null, "varName");
      Assertions.fail();
    } catch (NullPointerException ignored) {}
    StringTools.assureSet("foo", "varName");
  }

  @Test
  public void testReadStream() throws IOException {
    String content = StringTools.readStream(new FileInputStream("src/test/resources/testinput.txt"), "utf-8");
    Assertions.assertEquals("one\ntwo\nöäüß\nșțîâăȘȚÎÂĂ\n", content);
  }

  @Test
  public void testIsAllUppercase() {
    Assertions.assertTrue(StringTools.isAllUppercase("A"));
    Assertions.assertTrue(StringTools.isAllUppercase("ABC"));
    Assertions.assertTrue(StringTools.isAllUppercase("ASV-EDR"));
    Assertions.assertTrue(StringTools.isAllUppercase("ASV-ÖÄÜ"));
    Assertions.assertTrue(StringTools.isAllUppercase(""));
    
    Assertions.assertFalse(StringTools.isAllUppercase("ß"));
    Assertions.assertFalse(StringTools.isAllUppercase("AAAAAAAAAAAAq"));
    Assertions.assertFalse(StringTools.isAllUppercase("a"));
    Assertions.assertFalse(StringTools.isAllUppercase("abc"));
  }

  @Test
  public void testIsMixedCase() {
    Assertions.assertTrue(StringTools.isMixedCase("AbC"));
    Assertions.assertTrue(StringTools.isMixedCase("MixedCase"));
    Assertions.assertTrue(StringTools.isMixedCase("iPod"));
    Assertions.assertTrue(StringTools.isMixedCase("AbCdE"));
    
    Assertions.assertFalse(StringTools.isMixedCase(""));
    Assertions.assertFalse(StringTools.isMixedCase("ABC"));
    Assertions.assertFalse(StringTools.isMixedCase("abc"));
    Assertions.assertFalse(StringTools.isMixedCase("!"));
    Assertions.assertFalse(StringTools.isMixedCase("Word"));
  }

  @Test
  public void testIsCapitalizedWord() {
    Assertions.assertTrue(StringTools.isCapitalizedWord("Abc"));
    Assertions.assertTrue(StringTools.isCapitalizedWord("Uppercase"));
    Assertions.assertTrue(StringTools.isCapitalizedWord("Ipod"));
    
    Assertions.assertFalse(StringTools.isCapitalizedWord(""));
    Assertions.assertFalse(StringTools.isCapitalizedWord("ABC"));
    Assertions.assertFalse(StringTools.isCapitalizedWord("abc"));
    Assertions.assertFalse(StringTools.isCapitalizedWord("!"));
    Assertions.assertFalse(StringTools.isCapitalizedWord("wOrD"));
  }

  @Test
  public void testStartsWithUppercase() {
    Assertions.assertTrue(StringTools.startsWithUppercase("A"));
    Assertions.assertTrue(StringTools.startsWithUppercase("ÄÖ"));
    
    Assertions.assertFalse(StringTools.startsWithUppercase(""));
    Assertions.assertFalse(StringTools.startsWithUppercase("ß"));
    Assertions.assertFalse(StringTools.startsWithUppercase("-"));
  }

  @Test
  public void testUppercaseFirstChar() {
    Assertions.assertNull(StringTools.uppercaseFirstChar(null));
    Assertions.assertEquals("", StringTools.uppercaseFirstChar(""));
    Assertions.assertEquals("A", StringTools.uppercaseFirstChar("A"));
    Assertions.assertEquals("Öäü", StringTools.uppercaseFirstChar("öäü"));
    Assertions.assertEquals("ßa", StringTools.uppercaseFirstChar("ßa"));
    Assertions.assertEquals("'Test'", StringTools.uppercaseFirstChar("'test'"));
    Assertions.assertEquals("''Test", StringTools.uppercaseFirstChar("''test"));
    Assertions.assertEquals("''T", StringTools.uppercaseFirstChar("''t"));
    Assertions.assertEquals("'''", StringTools.uppercaseFirstChar("'''"));
  }

  @Test
  public void testLowercaseFirstChar() {
    Assertions.assertNull(StringTools.lowercaseFirstChar(null));
    Assertions.assertEquals("", StringTools.lowercaseFirstChar(""));
    Assertions.assertEquals("a", StringTools.lowercaseFirstChar("A"));
    Assertions.assertEquals("öäü", StringTools.lowercaseFirstChar("Öäü"));
    Assertions.assertEquals("ßa", StringTools.lowercaseFirstChar("ßa"));
    Assertions.assertEquals("'test'", StringTools.lowercaseFirstChar("'Test'"));
    Assertions.assertEquals("''test", StringTools.lowercaseFirstChar("''Test"));
    Assertions.assertEquals("''t", StringTools.lowercaseFirstChar("''T"));
    Assertions.assertEquals("'''", StringTools.lowercaseFirstChar("'''"));
  }

  @Test
  public void testReaderToString() throws IOException {
    String str = StringTools.readerToString(new StringReader("bla\nöäü"));
    Assertions.assertEquals("bla\nöäü", str);
    StringBuilder longStr = new StringBuilder();
    for (int i = 0; i < 4000; i++) {
      longStr.append('x');
    }
    longStr.append("1234567");
    Assertions.assertEquals(4007, longStr.length());
    String str2 = StringTools.readerToString(new StringReader(longStr.toString()));
    Assertions.assertEquals(longStr.toString(), str2);
  }

  @Test
  public void testEscapeXMLandHTML() {
    Assertions.assertEquals("foo bar", StringTools.escapeXML("foo bar"));
    Assertions.assertEquals("!ä&quot;&lt;&gt;&amp;&amp;", StringTools.escapeXML("!ä\"<>&&"));
    Assertions.assertEquals("!ä&quot;&lt;&gt;&amp;&amp;", StringTools.escapeHTML("!ä\"<>&&"));
  }

  @Test
  public void testListToString() {
    List<String> list = new ArrayList<>();
    list.add("foo");
    list.add("bar");
    list.add(",");
    Assertions.assertEquals("foo,bar,,", String.join(",", list));
    Assertions.assertEquals("foo\tbar\t,", String.join("\t", list));
  }

  @Test
  public void testTrimWhitespace() {
    try {
      Assertions.assertNull(StringTools.trimWhitespace(null));
      Assertions.fail();
    } catch (NullPointerException ignored) {}
    Assertions.assertEquals("", StringTools.trimWhitespace(""));
    Assertions.assertEquals("", StringTools.trimWhitespace(" "));
    Assertions.assertEquals("XXY", StringTools.trimWhitespace(" \nXX\t Y"));
    Assertions.assertEquals("XXY", StringTools.trimWhitespace(" \r\nXX\t Y"));
    Assertions.assertEquals("word", StringTools.trimWhitespace("word"));
    //only one space in the middle of the word is significant:
    Assertions.assertEquals("1 234,56", StringTools.trimWhitespace("1 234,56"));
    Assertions.assertEquals("1234,56", StringTools.trimWhitespace("1  234,56"));
  }

  @Test
  public void testAddSpace() {
    Language demoLanguage = TestTools.getDemoLanguage();
    Assertions.assertEquals(" ", StringTools.addSpace("word", demoLanguage));
    Assertions.assertEquals("", StringTools.addSpace(",", demoLanguage));
    Assertions.assertEquals("", StringTools.addSpace(",", demoLanguage));
    Assertions.assertEquals("", StringTools.addSpace(",", demoLanguage));
    Assertions.assertEquals("", StringTools.addSpace(".", new FakeLanguage("fr")));
    Assertions.assertEquals("", StringTools.addSpace(".", new FakeLanguage("de")));
    Assertions.assertEquals(" ", StringTools.addSpace("!", new FakeLanguage("fr")));
    Assertions.assertEquals("", StringTools.addSpace("!", new FakeLanguage("de")));
  }

  @Test
  public void testIsWhitespace() {
    Assertions.assertTrue(StringTools.isWhitespace("\uFEFF"));
    Assertions.assertTrue(StringTools.isWhitespace("  "));
    Assertions.assertTrue(StringTools.isWhitespace("\t"));
    Assertions.assertTrue(StringTools.isWhitespace("\u2002"));
    //non-breaking space is also a whitespace
    Assertions.assertTrue(StringTools.isWhitespace("\u00a0"));
    Assertions.assertFalse(StringTools.isWhitespace("abc"));
    //non-breaking OOo field
    Assertions.assertFalse(StringTools.isWhitespace("\\u02"));
    Assertions.assertFalse(StringTools.isWhitespace("\u0001"));
    // narrow nbsp:
    Assertions.assertTrue(StringTools.isWhitespace("\u202F"));
  }

  @Test
  public void testIsPositiveNumber() {
    Assertions.assertTrue(StringTools.isPositiveNumber('3'));
    Assertions.assertFalse(StringTools.isPositiveNumber('a'));
  }

  @Test
  public void testIsEmpty() {
    Assertions.assertTrue(StringTools.isEmpty(""));
    Assertions.assertTrue(StringTools.isEmpty(null));
    Assertions.assertFalse(StringTools.isEmpty("a"));
  }

  @Test
  public void testFilterXML() {
    Assertions.assertEquals("test", StringTools.filterXML("test"));
    Assertions.assertEquals("<<test>>", StringTools.filterXML("<<test>>"));
    Assertions.assertEquals("test", StringTools.filterXML("<b>test</b>"));
    Assertions.assertEquals("A sentence with a test", StringTools.filterXML("A sentence with a <em>test</em>"));
  }

  @Test
  public void testAsString() {
    Assertions.assertNull(StringTools.asString(null));
    Assertions.assertEquals("foo!", "foo!");
  }

  @Test
  public void testIsCamelCase() {
    Assertions.assertFalse(StringTools.isCamelCase("abc"));
    Assertions.assertFalse(StringTools.isCamelCase("ABC"));
    Assertions.assertTrue(StringTools.isCamelCase("iSomething"));
    Assertions.assertTrue(StringTools.isCamelCase("iSomeThing"));
    Assertions.assertTrue(StringTools.isCamelCase("mRNA"));
    Assertions.assertTrue(StringTools.isCamelCase("microRNA"));
    Assertions.assertTrue(StringTools.isCamelCase("microSomething"));
    Assertions.assertTrue(StringTools.isCamelCase("iSomeTHING"));
  }

}
