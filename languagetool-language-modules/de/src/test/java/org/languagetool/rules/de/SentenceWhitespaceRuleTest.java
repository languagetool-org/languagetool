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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SentenceWhitespaceRuleTest {

  @Test
  public void testMatch() throws Exception {
    SentenceWhitespaceRule rule = new SentenceWhitespaceRule(TestTools.getEnglishMessages());
    JLanguageTool languageTool = new JLanguageTool(TestTools.getDemoLanguage());
    languageTool.addRule(rule);

    assertGood("Das ist ein Satz. Und hier der nächste.", rule, languageTool);
    assertGood("Das ist ein Satz! Und hier der nächste.", rule, languageTool);
    assertGood("Ist das ein Satz? Hier der nächste.", rule, languageTool);

    assertBad("Das ist ein Satz.Und hier der nächste.", rule, languageTool);
    assertBad("Das ist ein Satz!Und hier der nächste.", rule, languageTool);
    assertBad("Ist das ein Satz?Hier der nächste.", rule, languageTool);

    assertGood("Am 28. September.", rule, languageTool);
    assertBad("Am 28.September.", rule, languageTool);
  }

  private void assertGood(String text, SentenceWhitespaceRule rule, JLanguageTool languageTool) throws IOException {
    assertThat(languageTool.check(text).size(), is(0));
    rule.reset();
  }

  private void assertBad(String text, SentenceWhitespaceRule rule, JLanguageTool languageTool) throws IOException {
    assertThat(languageTool.check(text).size(), is(1));
    rule.reset();
  }
}
