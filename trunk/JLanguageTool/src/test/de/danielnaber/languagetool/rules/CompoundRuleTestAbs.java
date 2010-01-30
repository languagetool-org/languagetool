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
package de.danielnaber.languagetool.rules;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;

/**
 * Abstract test case for CompoundRule. <br/>
 * Based on an original version for [en] and [pl].
 *    
 * @author Daniel Naber
 * 
 */
public abstract class CompoundRuleTestAbs extends TestCase {

  // the object used for checking text against different rules
  protected JLanguageTool langTool;
  // the rule that checks that compounds (if in the list) are not written as separate words. Language specific.
  protected AbstractCompoundRule rule;

  protected void setUp() throws Exception {
    super.setUp();
    // concrete classes will initialize langTool and rule variables here.
  }

  public void check(int expectedErrors, String text) throws IOException {
    check(expectedErrors, text, null);
  }
  
  /**
   * Check the text against the compound rule.    
   * @param expectedErrors teh number of extected errors.
   * @param text the text to check.
   * @param expSuggestions the expected suggestions.
   * @throws IOException thrown by JLanguageTool.
   */
  public void check(int expectedErrors, String text, String[] expSuggestions) throws IOException {
    assertNotNull("Please initialize langTool!", langTool);
    assertNotNull("Please initialize 'rule'!", rule);
    final RuleMatch[] ruleMatches = rule.match(langTool.getAnalyzedSentence(text));
    assertEquals(expectedErrors, ruleMatches.length);
    if (expSuggestions != null && expectedErrors != 1) {
      throw new RuntimeException("Sorry, test case can only check suggestion if there's one rule match");
    }
    if (expSuggestions != null) {
    	final RuleMatch ruleMatch = ruleMatches[0];
      assertEquals(String.format("Got these suggestions: %s, expected %d ", ruleMatch.getSuggestedReplacements(), expSuggestions.length),
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
