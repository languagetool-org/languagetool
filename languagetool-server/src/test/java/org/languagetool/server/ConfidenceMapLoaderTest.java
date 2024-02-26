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
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.tools.ConfidenceKey;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ConfidenceMapLoaderTest {

  @Test
  public void testLoading() throws IOException {
    Map<ConfidenceKey,Float> map = new ConfidenceMapLoader().load(new File("src/test/resources/org/languagetool/server/confidence-map-{lang}.csv"));
    assertThat(map.size(), is(7*3));  // 7 language variantes, 3 rules each
    Language en = Languages.getLanguageForShortCode("en");
    assertNotNull(map.get(new ConfidenceKey(en, "FOO_1")));
    assertThat(map.get(new ConfidenceKey(en, "FOO_1")), is(0.72f));
    assertNotNull(map.get(new ConfidenceKey(en, "OTHER_RULE_1")));
    assertNotNull(map.get(new ConfidenceKey(en, "OTHER_RULE_2")));
  }

  @Test
  public void testLoadingFail() throws IOException {
    try {
      new ConfidenceMapLoader().load(new File("src/test/resources/org/languagetool/server/confidence-map.csv"));
      fail();
    } catch (RuntimeException expected) {}
    try {
      new ConfidenceMapLoader().load(new File("src/test/resources/org/languagetool/server/does-not-exist-{lang}.csv"));
      fail();
    } catch (RuntimeException expected) {}
  }

}