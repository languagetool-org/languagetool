/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class CommonWordsTest {

  @Test
  public void test() throws IOException {
    Language de = Languages.getLanguageForShortCode("de");
    Language en = Languages.getLanguageForShortCode("en");
    CommonWords cw = new CommonWords();

    Map<Language, Integer> res1 = cw.getKnownWordsPerLanguage("Das ist bequem");
    assertNull(res1.get(en));
    assertThat(res1.get(de), is(2));

    Map<Language, Integer> res2 = cw.getKnownWordsPerLanguage("Das ist bequem ");
    assertNull(res2.get(en));
    assertThat(res2.get(de), is(3));

    Map<Language, Integer> res3 = cw.getKnownWordsPerLanguage("bequem");
    assertNull(res3.get(en));
    assertThat(res3.get(de), is(1));

    Map<Language, Integer> res4 = cw.getKnownWordsPerLanguage("this is a test");
    assertThat(res4.get(en), is(3));
    assertThat(res4.get(de), is(1));
  }

}
