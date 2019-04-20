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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.ru.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ru.RussianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.ru.RussianHybridDisambiguator;
import org.languagetool.tagging.ru.RussianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class Russian extends Language implements AutoCloseable {

  private Tagger tagger;
  private Disambiguator disambiguator;
  private Synthesizer synthesizer;
  private SentenceTokenizer sentenceTokenizer;
  private LuceneLanguageModel languageModel;

  @Override
  public Pattern getIgnoredCharactersRegex() {
    return Pattern.compile("[\u00AD\u0301\u0300]");
  }

  @Override
  public String getName() {
    return "Russian";
  }

  @Override
  public String getShortCode() {
    return "ru";
  }

  @Override
  public String[] getCountries() {
    return new String[] {"RU"};
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new RussianTagger();
    }
    return tagger;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new RussianHybridDisambiguator();
    }
    return disambiguator;
  }
  
  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new RussianSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Yakov Reztsov", "http://myooo.ru/content/view/83/43/")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages,
                    Example.wrong("Не род<marker> ,</marker> а ум поставлю в воеводы."),
                    Example.fixed("Не род<marker>,</marker> а ум поставлю в воеводы.")),
            new DoublePunctuationRule(messages),
            new UppercaseSentenceStartRule(messages, this,
                    Example.wrong("Закончилось лето. <marker>дети</marker> снова сели за школьные парты."),
                    Example.fixed("Закончилось лето. <marker>Дети</marker> снова сели за школьные парты.")),
            new MorfologikRussianSpellerRule(messages, this, userConfig, altLanguages),
            new WordRepeatRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            // specific to Russian :
            new RussianUnpairedBracketsRule(messages, this),
            new RussianCompoundRule(messages),
            new RussianSimpleReplaceRule(messages),
            new RussianWordCoherencyRule(messages),
            new RussianWordRepeatRule(messages),
            new RussianVerbConjugationRule(messages),
            new RussianDashRule()
    );
  }

  /** @since 3.1 */
  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    if (languageModel == null) {
      languageModel = new LuceneLanguageModel(new File(indexDir, getShortCode()));
    }
    return languageModel;
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    return Arrays.<Rule>asList(
            new RussianConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  /**
   * Closes the language model, if any. 
   * @since 3.1
   */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }

  /** @since 3.3 */
  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }
}
