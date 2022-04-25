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
package org.languagetool.rules.pl;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Polish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DashRuleTest {

  private JLanguageTool lt;
  private Rule rule;

  @Before
  public void setUp() throws Exception {
    Language lang = new Polish();
    lt = new JLanguageTool(lang);
    rule = new DashRule(JLanguageTool.getMessageBundle(lang));
  }

  @Test
  public void testRule() throws IOException {
    // correct sentences:
    check(0, "Nie róbmy nic na łapu-capu.");
    check(0, "Jedzmy kogel-mogel.");
    check(0, "To jest ładna nota — bene, bene — odpowiedział Józek."); // not really a real mistake
    // incorrect sentences:
    check(1, "bim – bom", new String[]{"bim-bom"});
    check(1, "Papua–Nowa Gwinea", new String[]{"Papua-Nowa"});
    check(1, "Papua — Nowa Gwinea", new String[]{"Papua-Nowa"});
    check(1, "Aix — en — Provence", new String[]{"Aix-en-Provence"});
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
    assertNotNull("Please initialize langTool!", lt);
    assertNotNull("Please initialize 'rule'!", rule);
    RuleMatch[] ruleMatches = rule.match(lt.getAnalyzedSentence(text));
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
