package org.languagetool;

import org.junit.Test;
import org.languagetool.language.LanguageIdentifier;

import static org.junit.Assert.assertEquals;

public class languageAutoDetectionTest {

  private LanguageIdentifier identifier = new LanguageIdentifier();

  @Test
  public void SimplifiedChinesetest() {
    String text = "这是简体中文";
    Language language = identifier.detectLanguage(text);
    assertEquals("zh-CN",language.getShortCodeWithCountryAndVariant());
  }

  @Test
  public void TraditionalChinesetest() {
    String text = "這是繁體中文";
    Language language = identifier.detectLanguage(text);
    assertEquals("zh-TW", language.getShortCodeWithCountryAndVariant());
  }
}
