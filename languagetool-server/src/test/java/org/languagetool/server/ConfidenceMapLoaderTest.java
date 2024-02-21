/* LanguageTool, a natural language style checker
 * Copyright (C) 2024 Daniel Naber (http://www.danielnaber.de)
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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class ConfidenceMapLoaderTest {

  @Test
  public void testLoading() throws IOException {
    Map<String, Float> map = new ConfidenceMapLoader().load(new File("src/test/resources/org/languagetool/server/confidence-map.csv"));
    assertThat(map.size(), is(3));
    System.out.println(map);
    assertNotNull(map.get("FOO_1"));
    assertThat(map.get("FOO_1"), is(0.72f));
    assertNotNull(map.get("OTHER_RULE_1"));
    assertNotNull(map.get("OTHER_RULE_2"));
  }

}