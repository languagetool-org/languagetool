/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Marcin Miłkowski (http://www.languagetool.org)
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
package org.languagetool.gui;

import junit.framework.TestCase;

public class ToolsTest extends TestCase {

  public void testShortenComment() {
    final String testString = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt.";
    final String testStringShortened = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt.";
    final String testLongString = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt ut labore (et dolore magna aliqua).";
    final String testLongStringShortened = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt ut labore.";
    final String testVeryLongString = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod (tempor incidunt [ut labore et dolore magna aliqua]).";
    final String testVeryLongStringShortened = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod (tempor incidunt).";
    final String shortenedString = Tools.shortenComment(testString);
    assertEquals(testStringShortened, shortenedString);
    final String shortenedLongString = Tools.shortenComment(testLongString);
    assertEquals(testLongStringShortened, shortenedLongString);
    final String shortenedVeryLongString = Tools.shortenComment(testVeryLongString);
    assertEquals(testVeryLongStringShortened, shortenedVeryLongString);
  }

  public void testGetLabel() {
    assertEquals("This is a Label", Tools.getLabel("This is a &Label"));
    assertEquals("Bits & Pieces", Tools.getLabel("Bits && Pieces"));
  }

  public void testGetOOoLabel() {
    assertEquals("Bits & Pieces", Tools.getLabel("Bits && Pieces"));
  }

  public void testGetMnemonic() {
    assertEquals('F', Tools.getMnemonic("&File"));
    assertEquals('O', Tools.getMnemonic("&OK"));
    assertEquals('\u0000', Tools.getMnemonic("File && String operations"));
    assertEquals('O', Tools.getMnemonic("File && String &Operations"));
  }

}
