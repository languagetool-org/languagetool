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
package org.languagetool.dev.wikipedia.atom;

import org.junit.Test;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AtomFeedItemTest {
  
  @Test
  public void testModifiedContent() throws IOException {
    AtomFeedItem item = getSummary("summary1.txt");
    assertThat(item.getOldContent().size(), is(1));
    assertThat(item.getOldContent().get(0), is("}}added"));
    assertThat(item.getNewContent().size(), is(1));
    assertThat(item.getNewContent().get(0), is("}}"));
  }

  @Test
  public void testAddedParagraphs() throws IOException {
    AtomFeedItem item = getSummary("summary2.txt");
    assertThat(item.getOldContent().size(), is(0));  // some content was added, so there's no old version
    assertThat(item.getNewContent().size(), is(3));
    assertThat(item.getNewContent().get(0), is("* [http://www.rp-online.de/nrw/staedte/]"));
    assertThat(item.getNewContent().get(1), is("* [http://www.vmtubes.com]"));
    assertThat(item.getNewContent().get(2), is(""));
  }

  @Test
  public void testDeletedParagraphs() throws IOException {
    AtomFeedItem item = getSummary("summary3.txt");
    assertThat(item.getOldContent().size(), is(3));
    assertThat(item.getOldContent().get(0), is("* [http://www.rp-online.de/nrw/staedte/]"));
    assertThat(item.getOldContent().get(1), is("* [http://www.vmtubes.com]"));
    assertThat(item.getOldContent().get(2), is(""));
    assertThat(item.getNewContent().size(), is(0));  // some content was deleted, so there's no new version
  }

  @Test
  public void testAddedTableLine() throws IOException {
    // The table changes we get may be incomplete tables, so Sweble cannot filter
    // them and we'd be left with Mediawiki syntax without filtering...
    AtomFeedItem item = getSummary("summary-table.txt");
    assertThat(item.getOldContent().size(), is(0));
    assertThat(item.getNewContent().size(), is(1));
    assertThat(item.getNewContent().get(0), is("Besetzung"));  // was "!Besetzung" in XML
  }

  private AtomFeedItem getSummary(String filename) throws IOException {
    InputStream stream = Tools.getStream("/org/languagetool/dev/wikipedia/atom/" + filename);
    return new AtomFeedItem("fakeId", "fakeTitle", StringTools.streamToString(stream, "UTF-8"), new Date(100000));
  }

}
