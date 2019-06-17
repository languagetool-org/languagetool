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
package org.languagetool.rules.de;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;

public class SentenceWhitespaceRuleTest {

  @Test
  public void testMatch() throws Exception {
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    TestTools.disableAllRulesExcept(lt, "DE_SENTENCE_WHITESPACE");

    assertGood("Das ist ein Satz. Und hier der nächste.", lt);
    assertGood("Das ist ein Satz! Und hier der nächste.", lt);
    assertGood("Ist das ein Satz? Hier der nächste.", lt);

    assertBad("Das ist ein Satz.Und hier der nächste.", lt);
    assertBad("Das ist ein Satz!Und hier der nächste.", lt);
    assertBad("Ist das ein Satz?Hier der nächste.", lt);

    assertGood("Am 28. September.", lt);
    assertBad("Am 28.September.", lt);

    assertTrue(lt.check("Am 7.September 2014.").get(0).getMessage().contains("nach Ordnungszahlen"));
    assertTrue(lt.check("Im September.Dann der nächste Satz.").get(0).getMessage().contains("zwischen Sätzen"));
  }

  private void assertGood(String text, JLanguageTool lt) throws IOException {
    assertThat(lt.check(text).size(), is(0));
  }

  private void assertBad(String text, JLanguageTool lt) throws IOException {
    assertThat(lt.check(text).size(), is(1));
  }
}
