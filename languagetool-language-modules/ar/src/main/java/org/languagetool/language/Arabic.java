/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.ar.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ar.ArabicHybridDisambiguator;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.ArabicWordTokenizer;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Support for Arabic.
 * @since 4.9
 */
public class Arabic extends Language implements AutoCloseable {

  private LanguageModel languageModel;

  @Override
  public String getName() {
    return "Arabic";
  }

  @Override
  public String getShortCode() {
    return "ar";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"", "SA", "DZ", "BH", "EG", "IQ", "JO", "KW", "LB", "LY", "MA", "OM", "QA", "SD", "SY", "TN", "AE", "YE"};
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new ArabicHybridDisambiguator();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new ArabicWordTokenizer();
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new ArabicTagger();
  }

  @Override
  public Synthesizer createDefaultSynthesizer() {
    return new ArabicSynthesizer(this);
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[]{
      new Contributor("Taha Zerrouki"),
      new Contributor("Sohaib Afifi")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
      new MultipleWhitespaceRule(messages, this),
      new SentenceWhitespaceRule(messages),
      new GenericUnpairedBracketsRule(messages,
        Arrays.asList("[", "(", "{", "«", "﴾", "\"", "'"),
        Arrays.asList("]", ")", "}", "»", "﴿", "\"", "'")),
      new CommaWhitespaceRule(messages, true),
      new LongSentenceRule(messages, userConfig, 50),

      // specific to Arabic :
      new ArabicHunspellSpellerRule(messages, userConfig),
      new ArabicCommaWhitespaceRule(messages),
      new ArabicQuestionMarkWhitespaceRule(messages),
      new ArabicSemiColonWhitespaceRule(messages),
      new ArabicDoublePunctuationRule(messages),
      new ArabicWordRepeatRule(messages),
      new ArabicSimpleReplaceRule(messages),
      new ArabicDiacriticsRule(messages),
      new ArabicDarjaRule(messages),
      new ArabicHomophonesRule(messages),
      new ArabicRedundancyRule(messages),
      new ArabicWordCoherencyRule(messages),
      new ArabicWordinessRule(messages),
      new ArabicWrongWordInContextRule(messages),
      new ArabicTransVerbRule(messages)
    );
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) {
    return Arrays.asList(
      new ArabicConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
  }

  @Override
  public void close() {
    if (languageModel != null) {
      languageModel.close();
    }
  }
}
