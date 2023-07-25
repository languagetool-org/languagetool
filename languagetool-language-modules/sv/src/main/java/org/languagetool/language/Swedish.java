/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.rules.sv.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.sv.SwedishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.sv.SwedishHybridDisambiguator;
import org.languagetool.tagging.sv.SwedishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
//import org.languagetool.tokenizers.sv.SwedishWordTokenizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.util.Arrays.asList;

/**
 *
 * Deprecated in 3.6, but actively maintained again since v6.2+
 *
 */
@Deprecated
public class Swedish extends Language implements AutoCloseable {

  private LanguageModel languageModel;

  @Override
  public String getName() {
    return "Swedish";
  }

  @Override
  public String getShortCode() {
    return "sv";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"SE", "FI"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new SwedishTagger();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

/*
  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new SwedishWordTokenizer();
  }
*/

  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return asList(
        new UpperCaseNgramRule(messages, languageModel, this, userConfig),
        new SwedishConfusionProbabilityRule(messages, languageModel, this)
        //new SwedishNgramProbabilityRule(messages, languageModel, this)
    );
  }

/*
  @Override
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel lm, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    if (lm != null && motherTongue != null) {
      if ("en".equals(motherTongue.getShortCode())) {
        return asList(new SwedishForEnglishNativesFalseFriendRule(messages, lm, motherTongue, this));
      } else if ("de".equals(motherTongue.getShortCode())) {
        return asList(new SwedishForGermansFalseFriendRule(messages, lm, motherTongue, this));
      } else if ("da".equals(motherTongue.getShortCode())) {
        return asList(new SwedishForDanesFalseFriendRule(messages, lm, motherTongue, this));
      } else if ("no".equals(motherTongue.getShortCode())) {
        return asList(new SwedishForNorwegiansFalseFriendRule(messages, lm, motherTongue, this));
      }
    }
    return asList();
  }

  @Override
  public boolean hasNGramFalseFriendRule(Language motherTongue) {
    return motherTongue != null && (
      "en".equals(motherTongue.getShortCode()) ||
      "de".equals(motherTongue.getShortCode()) ||
      "da".equals(motherTongue.getShortCode()) ||
      "no".equals(motherTongue.getShortCode()));
  }

*/

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new SwedishHybridDisambiguator();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return SwedishSynthesizer.INSTANCE;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("ljo@fps_gbg")};
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages),
            new HunspellRule(messages, this, userConfig, altLanguages),
            // fixme! - A suitable paragraph length should be tuned automatically per text type,
            // so make sure to get the type from LO and COOL
            new LongParagraphRule(messages, this, userConfig, 150),
            new UppercaseSentenceStartRule(messages, this),
            new LongSentenceRule(messages, userConfig, 40),
            new WordRepeatRule(messages, this),
            new WordCoherencyRule(messages),
            new MultipleWhitespaceRule(messages, this),
            new SentenceWhitespaceRule(messages),
            new CompoundRule(messages, this, userConfig)
    );
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new HunspellRule(messages, this, null, null);
  }

  /** @since 6.2+ */
//  @Override
//  public String getOpeningDoubleQuote() {
//    return "”";
//  }

  /** @since 6.2+ */
//  @Override
//  public String getClosingDoubleQuote() {
//    return "”";
//  }

  /** @since 6.2+ */
//  @Override
//  public String getOpeningSingleQuote() {
//    return "’";
//  }

  /** @since 6.2+ */
//  @Override
//  public String getClosingSingleQuote() {
//    return "’";
//  }

  /**
   * Closes the language model, if any.
   * @since 6.2+
   */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }

}

