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
    rule = new BrazilianPortugueseReplaceRule(TestTools.getMessages("pt"), "/pt/pt-BR/replace.txt");
    lt = new JLanguageTool(new Portuguese());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Fui de ônibus até o açougue italiano.")).length);

    // incorrect sentences:
    checkSimpleReplaceRule("Vou de autocarro.", "ônibus");
    checkSimpleReplaceRule("O lançamento de dardo é um desporto.", "esporte");
    checkSimpleReplaceRule("Está no meu ADN!", "DNA");

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
}
