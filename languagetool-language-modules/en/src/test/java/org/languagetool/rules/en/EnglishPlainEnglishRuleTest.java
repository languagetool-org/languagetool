package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.URL;

public class EnglishPlainEnglishRuleTest {
  private static EnglishPlainEnglishRule englishPlainEnglishRule;

  static {
    try {
      englishPlainEnglishRule = new EnglishPlainEnglishRule(JLanguageTool.getMessageBundle());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testGetSuggestionsSeparator() throws IOException {
    assert(englishPlainEnglishRule.getSuggestionsSeparator() == " or ");
  }

  @Test
  public void testGetURL() throws IOException {
    URL url = new URL("https://en.wikipedia.org/wiki/List_of_plain_English_words_and_phrases");
    assert(englishPlainEnglishRule.getUrl().toString().equals(url.toString()));
  }
}
