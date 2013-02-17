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
package org.languagetool.dev.wikipedia;

import junit.framework.TestCase;

public class WikipediaTextFilterTest extends TestCase {

  final SwebleWikipediaTextFilter swebleFilter = new SwebleWikipediaTextFilter();
  
  public void testImageRemoval() throws Exception {
    final String input = "foo [[Datei:Bundesarchiv Bild 183-1990-0803-017.jpg|miniatur|Mit Lothar de Maizière im August 1990]] bar";
    assertEquals("foo bar", swebleFilter.filter(input));
  }
  
  public void testRemovalOfImageWithLink() throws Exception {
    final String input = "foo [[Datei:Bundesarchiv Bild 183-1990-0803-017.jpg|miniatur|Mit [[Lothar de Maizière]] im August 1990]] bar [[Link]]";
    assertEquals("foo bar Link", swebleFilter.filter(input));
  }

  public void testLink1() throws Exception {
    final String input = "foo [[Test]] bar";
    assertEquals("foo Test bar", swebleFilter.filter(input));
  }

  public void testLink2() throws Exception {
    final String input = "foo [[Target|visible link]] bar";
    assertEquals("foo visible link bar", swebleFilter.filter(input));
  }

  public void testEntity() throws Exception {
    final String input = "rund 20&nbsp;Kilometer südlich";
    assertEquals("rund 20 Kilometer südlich", swebleFilter.filter(input));
  }

  public void testLists() throws Exception {
    final String input1 = "# one\n# two\n";
    assertEquals("one\ntwo", swebleFilter.filter(input1));
    final String input2 = "* one\n* two\n";
    assertEquals("one\ntwo", swebleFilter.filter(input2));
  }
    
}
