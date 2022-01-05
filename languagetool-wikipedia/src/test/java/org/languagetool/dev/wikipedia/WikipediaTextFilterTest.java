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

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Ignore
public class WikipediaTextFilterTest {

  private final SwebleWikipediaTextFilter swebleFilter = new SwebleWikipediaTextFilter();

  @Test
  public void testImageRemoval() {
    assertExtract("foo [[Datei:Bundesarchiv Bild 183-1990-0803-017.jpg|miniatur|Mit Lothar de Maizière im August 1990]] bar",
                  "foo bar");
  }

  @Test
  public void testRemovalOfImageWithLink() {
    assertExtract("foo [[Datei:Bundesarchiv Bild 183-1990-0803-017.jpg|miniatur|Mit [[Lothar de Maizière]] im August 1990]] bar [[Link]]",
                  "foo bar Link");
  }

  @Test
  public void testLink1() {
    assertExtract("foo [[Test]] bar", "foo Test bar");
  }

  @Test
  public void testLink2() {
    assertExtract("foo [[Target|visible link]] bar", "foo visible link bar");
  }

  @Test
  public void testEntity() {
    assertExtract("rund 20&nbsp;Kilometer südlich", "rund 20\u00A0Kilometer südlich");
    assertExtract("one&lt;br/&gt;two", "one<br/>two");
    assertExtract("one &ndash; two", "one – two");
    assertExtract("one &mdash; two", "one — two");
    assertExtract("one &amp; two", "one & two");
  }

  @Test
  public void testLists() {
    assertExtract("# one\n# two\n", "one\n\ntwo");
    assertExtract("* one\n* two\n", "one\n\ntwo");
  }

  @Test
  public void testOtherStuff() {
    assertExtract("Daniel Guerin, ''[http://theanarchistlibrary.org Anarchism: From Theory to Practice]''",
                  "Daniel Guerin, Anarchism: From Theory to Practice");
    assertExtract("foo <ref>\"At the end of the century in France [http://theanarchistlibrary.org] [[Daniel Guérin]]. ''Anarchism'']</ref>",
                  "foo");
    assertExtract("* [http://theanarchistlibrary.org ''Anarchism: From Theory to Practice''] by [[Daniel Guerin]]. Monthly Review Press.\n",
                  "Anarchism: From Theory to Practice by Daniel Guerin. Monthly Review Press.");
    assertExtract("The <code>$pattern</code>", "The $pattern");
    assertExtract("foo <source lang=\"bash\">some source</source> bar", "foo bar");
  }

  private void assertExtract(String input, String expected) {
    assertEquals(expected, swebleFilter.filter(input).getPlainText());
  }

}
