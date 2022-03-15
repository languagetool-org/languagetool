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
package org.languagetool.rules.ru;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

public class RussianDashRuleTest {

  private JLanguageTool lt;
  private Rule rule;

  @BeforeEach
  public void setUp() {
    Language lang = Languages.getLanguageForShortCode("ru");
    lt = new JLanguageTool(lang);
    rule = new RussianDashRule(JLanguageTool.getMessageBundle(lang));
  }

  @Test
  public void testRule() throws IOException {
    // correct sentences:
    check(0, "Он вышел из-за забора.");
    check(0, "Ростов-на-Дону.");
    check(0, "ведром — работай");
    // incorrect sentences:
    check(1, "из—за", new String[]{"из-за"});
    check(1, "Ростов — на — Дону", new String[]{"Ростов-на-Дону"});
  }

  private void check(int expectedErrors, String text) throws IOException {
    check(expectedErrors, text, null);
  }

  private void check(int expectedErrors, String text, String[] expSuggestions) throws IOException {
    RuleMatch[] ruleMatches = rule.match(lt.getAnalyzedSentence(text));
    Assertions.assertEquals(expectedErrors, ruleMatches.length, "Expected " + expectedErrors + " errors, but got: " + Arrays.toString(ruleMatches));
    if (expSuggestions != null && expectedErrors != 1) {
      throw new RuntimeException("Sorry, test case can only check suggestion if there's one rule match");
    }
    if (expSuggestions != null) {
      RuleMatch ruleMatch = ruleMatches[0];
      String errorMessage =
          String.format("Got these suggestions: %s, expected %s ", ruleMatch.getSuggestedReplacements(),
              Arrays.toString(expSuggestions));
      Assertions.assertEquals(expSuggestions.length, ruleMatch.getSuggestedReplacements().size(), errorMessage);
      int i = 0;
      for (Object element : ruleMatch.getSuggestedReplacements()) {
        String suggestion = (String) element;
        Assertions.assertEquals(expSuggestions[i], suggestion);
        i++;
      }
    }
  }

}
