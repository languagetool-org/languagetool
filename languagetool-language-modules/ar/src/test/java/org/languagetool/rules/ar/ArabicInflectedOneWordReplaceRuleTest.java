package org.languagetool.rules.ar;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
    assertIncorrect("أجريت أبحاثا في المخبر");
    assertIncorrect("وجعل لكم من أزواجكم بنين وأحفاد");
  }

  private void assertCorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentence));
    assertNotEquals(matches.length, 0);
  }

}
