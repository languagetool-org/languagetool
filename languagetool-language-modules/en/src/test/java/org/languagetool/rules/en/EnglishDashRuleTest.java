/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class EnglishDashRuleTest {

  private JLanguageTool lt;
  private Rule rule;

  @Before
  public void setUp() throws Exception {
    lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));
    rule = new EnglishDashRule();
  }

  @Test
  public void testRule() throws IOException {
    // correct sentences:
    check(0, "This is my T-shirt.");
    check(0, "This is water-proof.");
    check(0, "This works semi-automatically.");
    check(0, "She's a newcomer.");   // compounds.txt: "new-comer+"
    check(0, "I sent you and e-mail.");   // compounds.txt: "e-mail"
    // incorrect sentences:
    check(1, "T – shirt", "T-shirt");
    check(1, "three–way street", "three-way");
    check(1, "surface — to — surface", "surface-to-surface");
    check(1, "This works semi–automatically.", "semi-automatically");  // compounds.txt: "semi-automatic$" and "semi-automatically$"
    check(1, "This works semi – automatically.", "semi-automatically");
    check(1, "I sent you and e–mail.", "e-mail");   // compounds.txt: "e-mail"
    //check(1, "She's a new-comer.", "newcomer");   // not yet supported
    //check(1, "She's a new–comer.", "newcomer");   // not yet supported
  }

  private void check(int expectedErrors, String text) throws IOException {
    check(expectedErrors, text, null);
  }

  private void check(int expectedErrors, String text, String expectedSuggestion) throws IOException {
    RuleMatch[] ruleMatches = rule.match(lt.getAnalyzedSentence(text));
    assertEquals("Expected " + expectedErrors + " errors, but got: " + Arrays.toString(ruleMatches),
        expectedErrors, ruleMatches.length);
    if (expectedSuggestion != null) {
      RuleMatch ruleMatch = ruleMatches[0];
      String errorMessage =
          String.format("Got these suggestions: %s, expected %s ", ruleMatch.getSuggestedReplacements(), expectedSuggestion);
      assertEquals(errorMessage, 1, ruleMatch.getSuggestedReplacements().size());
      assertEquals(expectedSuggestion, ruleMatch.getSuggestedReplacements().get(0));
    }
  }

}
