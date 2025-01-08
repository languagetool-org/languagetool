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

import org.junit.BeforeClass;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Portuguese;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PortugueseOrthographyReplaceRuleTest {
  private static PortugueseOrthographyReplaceRule rule;
  private static JLanguageTool lt;

  @BeforeClass
  public static void setUp() throws Exception {
    lt = new JLanguageTool(Portuguese.getInstance());
    rule = new PortugueseOrthographyReplaceRule(TestTools.getMessages("pt"), lt.getLanguage());
  }

  @Test
  public void testRule() throws IOException {
    assertNoMatches("Já volto.");
    assertSingleMatch("Ja volto.", "Já");

    assertNoMatches("Gosto de você.");
    assertSingleMatch("Gosto de voce.", "você");
    assertNoMatches("Disse-me sotto voce.");  // multi-token spelling of Italian expression
  }

  private void assertRuleId(RuleMatch match) {
    assert match.getRule().getId().startsWith("PT_SIMPLE_REPLACE_ORTHOGRAPHY");
  }

  private void assertNoMatches(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertSingleMatch(String sentence, String ...suggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
    assertRuleId(matches[0]);
    List<String> returnedSuggestions = matches[0].getSuggestedReplacements();
    assertEquals(suggestions.length, returnedSuggestions.size());
    for (int i = 0; i < suggestions.length; i++) {
      assertEquals(suggestions[i], returnedSuggestions.get(i));
    }
  }
}
