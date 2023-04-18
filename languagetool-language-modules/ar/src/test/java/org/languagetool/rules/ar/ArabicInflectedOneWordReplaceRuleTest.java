package org.languagetool.rules.ar;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ArabicInflectedOneWordReplaceRuleTest {
  private ArabicInflectedOneWordReplaceRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws IOException {
    rule = new ArabicInflectedOneWordReplaceRule(TestTools.getEnglishMessages());
    lt = new JLanguageTool(Languages.getLanguageForShortCode("ar"));
  }

  @Test
  public void testRule() throws IOException {
    // Correct sentences:
    assertCorrect("أجريت بحوثا في المخبر");
    assertCorrect("وجعل لكم من أزواجكم بنين وحفدة");

    // errors:
    assertIncorrect("أجريت أبحاثا في المخبر", 2);
    assertIncorrect("وجعل لكم من أزواجكم بنين وأحفاد", 5);
  }

  private void assertCorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence, int index) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(index, matches.length);
  }

}
