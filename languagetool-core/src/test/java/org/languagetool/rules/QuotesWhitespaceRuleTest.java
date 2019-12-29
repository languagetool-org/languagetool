/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Nataliia Stulova (s0nata.github.io)
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
package org.languagetool.rules;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;

public class QuotesWhitespaceRuleTest {

  private QuotesWhitespaceRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() {
    rule = new QuotesWhitespaceRule(TestTools.getEnglishMessages(), null, null);
    langTool = new JLanguageTool(TestTools.getDemoLanguage());
  }

  @Test
  public void testRule() throws IOException {
	// correct examples:
    assertMatches("A sentence 'with' ten \"correct\" examples of ’using’ quotation “marks” at «once» in it.", 0);

    // erroneous examples:
    assertMatches("A sentence ' with ' ten \" incorrect \" examples of ’ using ’ quotation “ marks ” at « once » in it.", 10);

  }

  private void assertMatches(String text, int expectedMatches) throws IOException {
    assertEquals(expectedMatches, rule.match(langTool.getAnalyzedSentence(text)).length);
  }

}
