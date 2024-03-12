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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Portuguese;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BrazilianPortugueseSimpleReplaceRuleTest {
  private BrazilianPortugueseReplaceRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
    lt = new JLanguageTool(new Portuguese());
    rule = new BrazilianPortugueseReplaceRule(TestTools.getMessages("pt"), "/pt/pt-BR/replace.txt",
      lt.getLanguage());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Fui de ônibus até o açougue italiano.")).length);

    // incorrect sentences:
    checkSimpleReplaceRule("Vou de autocarro.", "ônibus");
    checkSimpleReplaceRule("O lançamento de dardo é um desporto.", "esporte");
    checkSimpleReplaceRule("Está no meu ADN!", "DNA");

    // these are dialect-dependent but are marked as exceptions (e.g. named entities)
    assertNoMatches("José António Miranda Coutinho");
    assertNoMatches("Jerónimo Soares");
  }

  /**
   * Check if a specific replace rule applies.
   * @param sentence the sentence containing the incorrect/misspelled word.
   * @param word the word that is correct (the suggested replacement).
   */
  private void checkSimpleReplaceRule(String sentence, String word) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals("Invalid matches.length while checking sentence: "
      + sentence, 1, matches.length);
    assertEquals("Invalid replacement count while checking sentence: "
      + sentence, 1, matches[0].getSuggestedReplacements().size());
    assertEquals("Invalid suggested replacement while checking sentence: "
      + sentence, word, matches[0].getSuggestedReplacements().get(0));
  }

  private void assertNoMatches(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }
}
