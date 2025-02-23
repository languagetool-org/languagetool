package org.languagetool.rules.zh;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.language.Chinese;
import org.languagetool.rules.Rule;
import org.languagetool.rules.zh.ChineseConfusionProbabilityRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.zh.ChineseSentenceTokenizer;
import org.languagetool.tokenizers.zh.ChineseWordTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.HashMap;

public class ChineseExtraTest {

  @Test
  public void testGetShortCodeAndName() {
    Chinese lang = Chinese.getInstance();
    assertEquals("zh", lang.getShortCode());
    assertEquals("Chinese", lang.getName());
  }

  @Test
  public void testGetCountriesAndMaintainers() {
    Chinese lang = Chinese.getInstance();
    String[] countries = lang.getCountries();
    assertArrayEquals(new String[]{"CN"}, countries);
    assertNotNull(lang.getMaintainers());
    assertTrue(lang.getMaintainers().length > 0);
    // 假设维护者名称为 "Tao Lin"
    assertEquals("Tao Lin", lang.getMaintainers()[0].getName());
  }

  @Test
  public void testCreateDefaultTagger() {
    Chinese lang = Chinese.getInstance();
    Tagger tagger = lang.createDefaultTagger();
    assertNotNull(tagger);
    assertTrue(tagger instanceof org.languagetool.tagging.zh.ChineseTagger);
  }

  @Test
  public void testCreateDefaultWordTokenizer() {
    Chinese lang = Chinese.getInstance();
    Tokenizer tokenizer = lang.createDefaultWordTokenizer();
    assertNotNull(tokenizer);
    assertTrue(tokenizer instanceof ChineseWordTokenizer);
  }

  @Test
  public void testCreateDefaultSentenceTokenizer() {
    Chinese lang = Chinese.getInstance();
    SentenceTokenizer sentenceTokenizer = lang.createDefaultSentenceTokenizer();
    assertNotNull(sentenceTokenizer);
    assertTrue(sentenceTokenizer instanceof ChineseSentenceTokenizer);
  }

  @Ignore
  @Test
  public void testGetRelevantRules() {
    Chinese lang = Chinese.getInstance();
    // 使用默认的消息资源文件，此处假设资源文件 "org.languagetool.rules.messages" 存在
    ResourceBundle messages = ResourceBundle.getBundle("org.languagetool.rules.messages", Locale.ENGLISH);
    List<Rule> rules = lang.getRelevantRules(messages, null, null, null);
    assertNotNull(rules);
    // 期望返回两个规则：DoublePunctuationRule 和 MultipleWhitespaceRule
    assertEquals(2, rules.size());
    assertTrue(rules.get(0).getClass().getName().contains("DoublePunctuationRule"));
    assertTrue(rules.get(1).getClass().getName().contains("MultipleWhitespaceRule"));
  }

  @Ignore
  @Test
  public void testGetRelevantLanguageModelRules() throws IOException {
    Chinese lang = Chinese.getInstance();
    ResourceBundle messages = ResourceBundle.getBundle("org.languagetool.rules.messages", Locale.ENGLISH);
    // 使用 FakeLanguageModel 作为空的语言模型
    LanguageModel lm = new org.languagetool.rules.ngrams.FakeLanguageModel(new HashMap<>());
    List<Rule> lmRules = lang.getRelevantLanguageModelRules(messages, lm, null);
    assertNotNull(lmRules);
    assertEquals(1, lmRules.size());
    assertTrue(lmRules.get(0) instanceof ChineseConfusionProbabilityRule);
  }
}
