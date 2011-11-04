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
package de.danielnaber.languagetool.dev.wikipedia;

import junit.framework.TestCase;

public class WikipediaTextFilterTest extends TestCase {

  public void testImageRemoval() throws Exception {
    final String input = "foo [[Datei:Bundesarchiv Bild 183-1990-0803-017.jpg|miniatur|Mit Lothar de Maizière im August 1990]] bar";
    final WikipediaTextFilter filter = new WikipediaTextFilter();
    assertEquals("foo  bar", filter.filter(input));
  }
  
  public void testRemovalOfImageWithLink() throws Exception {
    final String input = "foo [[Datei:Bundesarchiv Bild 183-1990-0803-017.jpg|miniatur|Mit [[Lothar de Maizière]] im August 1990]] bar [[Link]]";
    final WikipediaTextFilter filter = new WikipediaTextFilter();
    assertEquals("foo  bar Link", filter.filter(input));
  }
  
}
