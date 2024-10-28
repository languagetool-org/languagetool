/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Jaume Ortolà (http://www.languagetool.org)
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
package org.languagetool.rules.pt;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.BrazilianPortuguese;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class PortugueseBarbarismRuleTest {
  private PortugueseBarbarismsRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
    lt = new JLanguageTool(new BrazilianPortuguese());
    rule = new PortugueseBarbarismsRule(TestTools.getMessages("pt"), "/pt/pt-BR/barbarisms.txt",
      lt.getLanguage());
  }

  private void assertNoMatches(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  @Test
  public void testReplaceBarbarisms() throws IOException {
    // These should not have matches, since they are *exceptions* (named entities, known multi-token matches, etc.)
    assertNoMatches("New York Stock Exchange");
    assertNoMatches("Yankee Doodle, faça o morra");
    assertNoMatches("mas inferior ao Opera Browser.");
  }
}
