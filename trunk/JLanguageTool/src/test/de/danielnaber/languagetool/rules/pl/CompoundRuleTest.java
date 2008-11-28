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
package de.danielnaber.languagetool.rules.pl;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Daniel Naber
 */
public class CompoundRuleTest extends TestCase {

  private JLanguageTool langTool;
  private CompoundRule rule;
  
  public void testRule() throws IOException {
    langTool = new JLanguageTool(Language.POLISH);
    rule = new CompoundRule(null);
    // correct sentences:
    check(0, "Nie róbmy nic na łapu-capu.");
    check(0, "Jedzmy kogiel-mogiel.");
    // incorrect sentences:
    check(1, "bim bom", new String[]{"bim-bom"});
  }

  private void check(int expectedErrors, String text) throws IOException {
    check(expectedErrors, text, null);
  }
  
  private void check(int expectedErrors, String text, String[] expSuggestions) throws IOException {
    final RuleMatch[] ruleMatches = rule.match(langTool.getAnalyzedSentence(text));
    assertEquals(expectedErrors, ruleMatches.length);
    if (expSuggestions != null && expectedErrors != 1) {
      throw new RuntimeException("Sorry, test case can only check suggestion if there's one rule match");
    }
    if (expSuggestions != null) {
      final RuleMatch ruleMatch = ruleMatches[0];
      assertEquals("Got these suggestions: " + ruleMatch.getSuggestedReplacements() + 
          ", expected " + expSuggestions.length,
          expSuggestions.length, ruleMatch.getSuggestedReplacements().size());
      int i = 0;
      for (final Object element : ruleMatch.getSuggestedReplacements()) {
        final String suggestion = (String) element;
        //System.err.println(">>"+suggestion);
        assertEquals(expSuggestions[i], suggestion);
        i++;
      }
    }
  }
  
}
