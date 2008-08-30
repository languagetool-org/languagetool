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
package de.danielnaber.languagetool.rules.ru;

import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * Russian Compound rule test
 * @author Yakov Reztsov 
 * Based on German Compound rule test
 * @author Daniel Naber
 */
public class RussianCompoundRuleTest extends TestCase {

  private JLanguageTool langTool;
  private RussianCompoundRule rule;
  
  public void testRule() throws IOException {
    langTool = new JLanguageTool(Language.RUSSIAN);
    rule = new RussianCompoundRule(null);
    // correct sentences:
    check(0, "Он вышел из-за дома.");
    // Both  suggestion for some words:
    check(0, "естественно-научный");
    // incorrect sentences:
    check(1, "из за", new String[]{"из-за"});
    check(1, "нет нет из за да да");
    //FIXME: suggestions / longest match
    check(1, "Ростов на Дону", new String[]{"Ростов-на-Дону"});
    // no hyphen suggestion for some words:
    check(1, "кругло суточный", new String[]{"круглосуточный"});
    // also accept incorrect upper/lowercase spelling:
    check(1, "Ростов на дону", new String[]{"Ростов-на-дону"});
    // also detect an error if only some of the hyphens are missing:
    check(1, "Ростов-на Дону", new String[]{"Ростов-на-Дону"});
    // first part is a single character:
    check(0, "в-третьих");
    check(1, "в третьих", new String[]{"в-третьих"});
  }

  private void check(int expectedErrors, String text) throws IOException {
    check(expectedErrors, text, null);
  }
  
  private void check(int expectedErrors, String text, String[] expSuggestions) throws IOException {
    RuleMatch[] ruleMatches = rule.match(langTool.getAnalyzedSentence(text));
    assertEquals(expectedErrors, ruleMatches.length);
    if (expSuggestions != null && expectedErrors != 1) {
      throw new RuntimeException("Sorry, test case can only check suggestion if there's one rule match");
    }
    if (expSuggestions != null) {
      RuleMatch ruleMatch = ruleMatches[0];
      assertEquals("Got these suggestions: " + ruleMatch.getSuggestedReplacements() + 
          ", expected " + expSuggestions.length,
          expSuggestions.length, ruleMatch.getSuggestedReplacements().size());
      int i = 0;
      for (Iterator iter = ruleMatch.getSuggestedReplacements().iterator(); iter.hasNext();) {
        String suggestion = (String) iter.next();
        //System.err.println(">>"+suggestion);
        assertEquals(expSuggestions[i], suggestion);
        i++;
      }
    }
  }
  
}
