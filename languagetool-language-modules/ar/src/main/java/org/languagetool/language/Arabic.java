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
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ar.ArabicHybridDisambiguator;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.ArabicWordTokenizer;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

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

  private static final Language DEFAULT_ARABIC = new AlgerianArabic();
  public static final String TASHKEEL_CHARS =
    "\u064B"    // Fathatan
    + "\u064C"  // Dammatan
    + "\u064D"  // Kasratan
    + "\u064E"  // Fatha
    + "\u064F"  // Damma
    + "\u0650"  // Kasra
    + "\u0651"  // Shadda
    + "\u0652"  // Sukun
    + "\u0653"  // Maddah Above
    + "\u0654"  // Hamza Above
    + "\u0655"  // Hamza Below
    + "\u0656"  // Subscript Alef
    + "\u0640"; // Tatweel

  private SentenceTokenizer sentenceTokenizer;
  private WordTokenizer wordTokenizer;
  private Tagger tagger;
  private Synthesizer synthesizer;
  private LanguageModel languageModel;
  private Disambiguator disambiguator;

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
  public Language getDefaultLanguageVariant() {
    return DEFAULT_ARABIC;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new ArabicHybridDisambiguator();
    }
    return disambiguator;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
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

      // specific to Arabic :
      new ArabicHunspellSpellerRule(messages, userConfig),
      //new MorfologikArabicSpellerRule(messages, this),
      new ArabicCommaWhitespaceRule(messages),
      new ArabicDoublePunctuationRule(messages),
      new LongSentenceRule(messages, userConfig, -1, false),
      new ArabicWordRepeatRule(messages),
      new ArabicSimpleReplaceRule(messages),
      new ArabicDiacriticsRule(messages),
      new ArabicRedundancyRule(messages),
      new ArabicWordCoherencyRule(messages),
      new ArabicWordinessRule(messages),
      new ArabicWrongWordInContextRule(messages)
    );
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    return Arrays.asList(
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
