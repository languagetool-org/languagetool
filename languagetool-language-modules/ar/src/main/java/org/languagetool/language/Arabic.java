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

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.ar.*;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tokenizers.ArabicSentenceTokenizer;
import org.languagetool.tokenizers.ArabicWordTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Support for Arabic.
 */
public class Arabic extends Language implements AutoCloseable {

  private static final Language DEFAULT_ARABIC = new Arabic();
  private SentenceTokenizer sentenceTokenizer;
  private WordTokenizer wordTokenizer;
  private Tagger tagger;
  private Synthesizer synthesizer;
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
    return new String[]{"", "DZ", "TN"};
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return DEFAULT_ARABIC;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new ArabicSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public WordTokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new ArabicWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new ArabicTagger();
    }
    return tagger;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new ArabicSynthesizer(this);
    }
    return synthesizer;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[]{
      new Contributor("Taha Zerrouki"),
      new Contributor("Sohaib Afifi"),
      new Contributor("Imen Kali"),
      new Contributor("Karima Tchoketch"),
    };
  }


  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
      new MultipleWhitespaceRule(messages, this),
      new SentenceWhitespaceRule(messages),
      new GenericUnpairedBracketsRule(messages,
        Arrays.asList("[", "(", "{", "«", "﴾"),
        Arrays.asList("]", ")", "}", "»", "﴿")),
      // specific to Arabic :
      new HunspellRule(messages, this, userConfig, altLanguages),
      new ArabicCommaWhitespaceRule(messages),
      new ArabicDoublePunctuationRule(messages),
      new LongSentenceRule(messages, userConfig, -1, false),
      new ArabicWordRepeatRule(messages, this),
      new ArabicContractionSpellingRule(messages)
    );
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    return Arrays.<Rule>asList(
      new ArabicConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
  }

  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }
}
