package org.languagetool.rules.en;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.en.EnglishWrongWordInContextRule;

public class EnglishWrongWordInContextRuleTest {

  private JLanguageTool langTool;
  private EnglishWrongWordInContextRule rule;
  
  @Before
  public void setUp() throws IOException {
    langTool = new JLanguageTool(new AmericanEnglish());
    rule = new EnglishWrongWordInContextRule(null);
  }

  @Test
  public void testRule() throws IOException {
    // prescribe/proscribe
    assertBad("I have proscribed you a course of antibiotics.");
    assertGood("I have prescribed you a course of antibiotics.");
    assertGood("Name one country that does not proscribe theft.");
    assertBad("Name one country that does not prescribe theft.");
    assertEquals("prescribed", rule.match(langTool.getAnalyzedSentence("I have proscribed you a course antibiotics."))[0].getSuggestedReplacements().get(0));
  }

  private void assertGood(String sentence) throws IOException {
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(sentence)).length);
  }

  private void assertBad(String sentence) throws IOException {
    assertEquals(1, rule.match(langTool.getAnalyzedSentence(sentence)).length);
  }
}
