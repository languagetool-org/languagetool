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
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ContractionSpellingRuleTest {

  private ContractionSpellingRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
    rule = new ContractionSpellingRule(TestTools.getMessages("en"));
    lt = new JLanguageTool(Languages.getLanguageForShortCode("en"));
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("It wasn't me.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("I'm ill.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Staatszerfall im südlichen Afrika.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("by IVE")).length);
    
    // incorrect sentences:

    // at the beginning of a sentence
    checkSimpleReplaceRule("Wasnt this great", "Wasn't");
    checkSimpleReplaceRule("YOURE WRONG", "YOU'RE");
    checkSimpleReplaceRule("Dont do this", "Don't");
    //checkSimpleReplaceRule("Ive been doing", "I've");
    // inside sentence
    checkSimpleReplaceRule("It wasnt me", "wasn't");
    checkSimpleReplaceRule("You neednt do this", "needn't");
    checkSimpleReplaceRule("I know Im wrong", "I'm");

    //two suggestions
    final RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Whereve you are"));
    assertEquals(2, matches[0].getSuggestedReplacements().size());
    assertEquals("Where've", matches[0].getSuggestedReplacements().get(0));
    assertEquals("Wherever", matches[0].getSuggestedReplacements().get(1));

  }

  /**
   * Check if a specific replace rule applies.
   * @param sentence the sentence containing the incorrect/misspelled word.
   * @param word the word that is correct (the suggested replacement).
   */
  private void checkSimpleReplaceRule(String sentence, String word) throws IOException {
    final RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals("Invalid matches.length while checking sentence: "
        + sentence, 1, matches.length);
    assertEquals("Invalid replacement count wile checking sentence: "
        + sentence, 1, matches[0].getSuggestedReplacements().size());
    assertEquals("Invalid suggested replacement while checking sentence: "
        + sentence, word, matches[0].getSuggestedReplacements().get(0));
  }
}
