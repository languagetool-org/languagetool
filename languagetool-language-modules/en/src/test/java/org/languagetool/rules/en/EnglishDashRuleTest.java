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
import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EnglishDashRuleTest {

  private JLanguageTool langTool;
  private Rule rule;

  @Before
  public void setUp() throws Exception {
    Language lang = new AmericanEnglish();
    langTool = new JLanguageTool(lang);
    rule = new EnglishDashRule();
  }

  @Test
  public void testRule() throws IOException {
    // correct sentences:
    check(0, "This is my T-shirt.");
    check(0, "This is water-proof.");
    // incorrect sentences:
    check(1, "T – shirt", new String[]{"T-shirt"});
    check(1, "three–way street", new String[]{"three-way"});
    check(1, "surface — to — surface", new String[]{"surface-to-surface"});
  }

  private void check(int expectedErrors, String text) throws IOException {
    check(expectedErrors, text, null);
  }

  /**
   * Check the text against the compound rule.
   * @param expectedErrors the number of expected errors
   * @param text the text to check
   * @param expSuggestions the expected suggestions
   */
  private void check(int expectedErrors, String text, String[] expSuggestions) throws IOException {
    assertNotNull("Please initialize langTool!", langTool);
    assertNotNull("Please initialize 'rule'!", rule);
    RuleMatch[] ruleMatches = rule.match(langTool.getAnalyzedSentence(text));
    assertEquals("Expected " + expectedErrors + "errors, but got: " + Arrays.toString(ruleMatches),
        expectedErrors, ruleMatches.length);
    if (expSuggestions != null && expectedErrors != 1) {
      throw new RuntimeException("Sorry, test case can only check suggestion if there's one rule match");
    }
    if (expSuggestions != null) {
      RuleMatch ruleMatch = ruleMatches[0];
      String errorMessage =
          String.format("Got these suggestions: %s, expected %s ", ruleMatch.getSuggestedReplacements(),
              Arrays.toString(expSuggestions));
      assertEquals(errorMessage, expSuggestions.length, ruleMatch.getSuggestedReplacements().size());
      int i = 0;
      for (Object element : ruleMatch.getSuggestedReplacements()) {
        String suggestion = (String) element;
        assertEquals(expSuggestions[i], suggestion);
        i++;
      }
    }
  }

}
