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
import org.languagetool.tools.Tools;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AtomFeedParserTest {
  
  @Test
  public void testParsing() throws IOException, XMLStreamException {
    AtomFeedParser atomFeedParser = new AtomFeedParser();
    List<AtomFeedItem> items = atomFeedParser.getAtomFeedItems(Tools.getStream("/org/languagetool/dev/wikipedia/atom/feed1.xml"));
    assertThat(items.size(), is(3));

    AtomFeedItem item1 = items.get(0);
    assertThat(item1.getId(), is("//de.wikipedia.org/w/index.php?title=Peter_Bichsel&diff=125079808&oldid=125079797"));
    assertThat(item1.getTitle(), is("Peter Bichsel"));
    assertThat(item1.getDiffId(), is(125079808L));
    assertThat(item1.getOldContent().toString(), is("[}}llllllllll]"));
    assertThat(item1.getNewContent().toString(), is("[}}]"));

    AtomFeedItem item2 = items.get(1);
    assertThat(item2.getId(), is("//de.wikipedia.org/wiki/Timo_b%C3%A4cker"));
    assertThat(item2.getTitle(), is("Timo bäcker"));
    assertThat(item2.getDiffId(), is(0L));
    assertThat(item2.getOldContent().toString(), is("[]"));
    assertThat(item2.getNewContent().toString(), is("[]"));

    AtomFeedItem item3 = items.get(2);
    assertThat(item3.getId(), is("//de.wikipedia.org/w/index.php?title=Vallourec_Deutschland_GmbH&diff=125079807&oldid=124992032"));
    assertThat(item3.getTitle(), is("Vallourec Deutschland GmbH"));
    assertThat(item3.getDiffId(), is(125079807L));
    assertThat(item3.getOldContent().toString(), is("[]"));
    assertThat(item3.getNewContent().toString(), is("[* [http://www.rp-online.de/nrw/staedte/] Fehler: der Haus, * [http://www.vmtubes.com], ]"));
  }
}
