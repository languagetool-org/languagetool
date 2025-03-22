/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.LanguageWithModel;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.zh.ChineseConfusionProbabilityRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.zh.ChineseTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.zh.ChineseSentenceTokenizer;
import org.languagetool.tokenizers.zh.ChineseWordTokenizer;

import java.io.IOException;
import java.util.*;

public class Chinese extends LanguageWithModel {
  private static final String LANGUAGE_SHORT_CODE = "zh-CN";

  private static volatile Throwable instantiationTrace;

  public Chinese() {
    Throwable trace = instantiationTrace;
    if (trace != null) {
      throw new RuntimeException("Language was already instantiated, see the cause stacktrace below.", trace);
    }
    instantiationTrace = new Throwable();
  }

  /**
   * This is a fake constructor overload for the subclasses. Public constructors can only be used by the LT itself.
   */
  protected Chinese(boolean fakeValue) {
  }


  @Override
  public String getShortCode() {
    return "zh";
  }

  @Override
  public String getName() {
    return "Chinese";
  }

  @Override
  public String[] getCountries() {
    return new String[] { "CN" };
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Tao Lin") };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
    return Arrays.asList(
            new DoublePunctuationRule(messages),
            new MultipleWhitespaceRule(messages, this)
    );
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new ChineseTagger();
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new ChineseWordTokenizer();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new ChineseSentenceTokenizer();
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Collections.singletonList(
      new ChineseConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  public static @NotNull Chinese getInstance() {
    Language language = Objects.requireNonNull(Languages.getLanguageForShortCode(LANGUAGE_SHORT_CODE));
    if (language instanceof Chinese chinese) {
      return chinese;
    }
    throw new RuntimeException("Chinese language expected, got " + language);
  }
}
