/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AtDXmlSerializerTest {
  
  @Test
  public void testGetPreContext() {
    AtDXmlSerializer serializer = new AtDXmlSerializer();
    assertThat(serializer.getPreContext("test", 0), is(""));
    assertThat(serializer.getPreContext("a hour", 0), is(""));
    assertThat(serializer.getPreContext("is a hour", 3), is("is"));
    assertThat(serializer.getPreContext("an test", 0), is(""));
    assertThat(serializer.getPreContext("is an test", 3), is("is"));
    assertThat(serializer.getPreContext(" an test", 1), is(""));
    assertThat(serializer.getPreContext("  an test", 2), is(""));
    assertThat(serializer.getPreContext("  is an test", 5), is("is"));
    assertThat(serializer.getPreContext("This is an test", 8), is("is"));
    assertThat(serializer.getPreContext("This is  an test", 9), is("is"));
    assertThat(serializer.getPreContext("This is   an test", 10), is("is"));
    assertThat(serializer.getPreContext("This was a hour ago.", 9), is("was"));
    assertThat(serializer.getPreContext("This  was  a hour ago.", 11), is("was"));
    assertThat(serializer.getPreContext("This is, an test", 9), is(""));  // that's what AtD does
    assertThat(serializer.getPreContext("This is: an test", 9), is(""));  // that's what AtD does
    assertThat(serializer.getPreContext("Das hier hier ist ein Test.", 4), is("Das"));
  }

}
