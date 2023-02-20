package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.languagetool.JLanguageTool.getMessageBundle;

public class SimpleReplaceRuleTest {
  Language language = Languages.getLanguageForShortCode("en-US");
  ResourceBundle messages = getMessageBundle(language);
  SimpleReplaceRule simpleReplaceRule = new SimpleReplaceRule(messages, language);
  @Test
  public void testGetShort(){
    assertEquals("Wrong word", simpleReplaceRule.getShort());
  }

  @Test
  public void testGetMessage(){
    assertEquals("Did you mean $suggestions?", simpleReplaceRule.getMessage());
  }

  @Test
  public void testGetSuggestionsSeparator() {
    assertEquals(", ", simpleReplaceRule.getSuggestionsSeparator());
  }
}
