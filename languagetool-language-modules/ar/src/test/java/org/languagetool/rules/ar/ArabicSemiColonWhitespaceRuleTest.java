/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Sohaib Afifi, Taha Zerrouki
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

package org.languagetool.rules.ar;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ArabicSemiColonWhitespaceRuleTest {
  private ArabicSemiColonWhitespaceRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() {
    rule = new ArabicSemiColonWhitespaceRule(TestTools.getEnglishMessages());
    lt = new JLanguageTool(Languages.getLanguageForShortCode("ar"));
  }

  @Test
  public void testRule() throws IOException {
    // correct
    assertMatches("This is a test sentence؛", 0);
    assertMatches("أهذه تجربة؛", 0);

    // errors:

    //Arabic semi colon
    assertMatches("أهذه تجربة ؛", 1);
  }

  private void assertMatches(String text, int expectedMatches) throws IOException {
    assertEquals(expectedMatches, rule.match(lt.getAnalyzedSentence(text)).length);
  }
}
