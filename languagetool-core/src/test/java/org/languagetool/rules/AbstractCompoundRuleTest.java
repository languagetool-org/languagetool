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
package org.languagetool.rules;

import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Abstract test case for CompoundRule.
 * Based on an original version for [en] and [pl].
 *    
 * @author Daniel Naber
 */
public abstract class AbstractCompoundRuleTest {

  // the object used for checking text against different rules
  protected JLanguageTool lt;
  // the rule that checks that compounds (if in the list) are not written as separate words. Language specific.
  protected AbstractCompoundRule rule;

  public void check(int expectedErrors, String text) throws IOException {
    check(expectedErrors, text, null);
  }
  
  /**
   * Check the text against the compound rule.    
   * @param expectedErrors the number of expected errors
   * @param text the text to check
   * @param expSuggestions the expected suggestions
   */
  public void check(int expectedErrors, String text, String... expSuggestions) throws IOException {
    assertNotNull("Please initialize langTool!", lt);
    assertNotNull("Please initialize 'rule'!", rule);
    RuleMatch[] ruleMatches = rule.match(lt.getAnalyzedSentence(text));
    assertEquals("Expected " + expectedErrors + " error(s), but got: " + Arrays.toString(ruleMatches),
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
      for (String element : ruleMatch.getSuggestedReplacements()) {
        assertEquals(expSuggestions[i], element);
        i++;
      }
    }
  }
  
  public void testAllCompounds() throws IOException {
    for (String compound : rule.getCompoundRuleData().getIncorrectCompounds()) {
      String suggestion ="";
      if (rule.getCompoundRuleData().getDashSuggestion().contains(compound)) {
        suggestion = compound.replace(" ", "-");
        if (rule.isMisspelled(suggestion)) {
          printWarning(suggestion);
        }
      }
      if (rule.getCompoundRuleData().getJoinedSuggestion().contains(compound)) {
        suggestion = rule.mergeCompound(compound, rule.getCompoundRuleData().getJoinedLowerCaseSuggestion().contains(compound));
        if (rule.isMisspelled(suggestion)) {
          printWarning(suggestion);
        }
      }
    }
  }
  
  private void printWarning(String suggestion) {
    System.err.println("WARNING: Suggested compound word is possibly misspelled: "+suggestion);
  }
  
}
