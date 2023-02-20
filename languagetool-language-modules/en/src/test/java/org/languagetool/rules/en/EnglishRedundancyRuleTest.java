package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.languagetool.JLanguageTool.getMessageBundle;

public class EnglishRedundancyRuleTest {
  Language language = Languages.getLanguageForShortCode("en-US");
  ResourceBundle messages = getMessageBundle(language);
  EnglishRedundancyRule englishRedundancyRule;

  {
    try {
      englishRedundancyRule = new EnglishRedundancyRule(messages);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testGetSuggestionsSeparator(){
    assertEquals(" or ", englishRedundancyRule.getSuggestionsSeparator());
  }

  @Test
  public void testGetUrl(){
    assertEquals(Tools.getUrl("https://en.wikipedia.org/wiki/Redundancy_(linguistics)"), englishRedundancyRule.getUrl());
  }

}
