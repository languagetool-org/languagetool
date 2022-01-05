/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.dumpcheck;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.language.English;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore
public class WikipediaSentenceSourceTest {
  
  @Test
  public void testWikipediaSource() throws XMLStreamException, IOException {
    InputStream stream = WikipediaSentenceSourceTest.class.getResourceAsStream("/org/languagetool/dev/wikipedia/wikipedia-en.xml");
    WikipediaSentenceSource source = new WikipediaSentenceSource(stream, new English());
    assertTrue(source.hasNext());
    assertThat(source.next().getText(), is("This is the first document."));
    assertThat(source.next().getText(), is("It has three sentences."));
    assertThat(source.next().getText(), is("Here's the last sentence."));
    
    assertThat(source.next().getText(), is("This is the second document."));
    assertThat(source.next().getText(), is("It has two sentences."));
    assertFalse(source.hasNext());
  }
  
}
