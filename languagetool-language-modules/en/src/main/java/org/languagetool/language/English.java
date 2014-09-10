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

import org.languagetool.Language;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.EnglishChunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.en.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.en.EnglishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;

/**
 * Support for English - use the sub classes {@link BritishEnglish}, {@link AmericanEnglish},
 * etc. if you need spell checking.
 * Make sure to call {@link #close()} after using this (currently only relevant if you make
 * use of {@link EnglishConfusionProbabilityRule}).
 */
public class English extends Language implements AutoCloseable {

  private static final Language AMERICAN_ENGLISH = new AmericanEnglish();

  private Tagger tagger;
  private Chunker chunker;
  private SentenceTokenizer sentenceTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
  private WordTokenizer wordTokenizer;
  private LuceneLanguageModel languageModel;
  private String name = "English";

  @Override
  public Language getDefaultLanguageVariant() {
    return AMERICAN_ENGLISH;
  }

  @Override
  public final SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public final String getShortName() {
    return "en";
  }

  @Override
  public String[] getCountries() {
    return new String[]{};
  }

  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new EnglishTagger();
    }
    return tagger;
  }

  /**
   * @since 2.3
   */
  @Override
  public Chunker getChunker() {
    if (chunker == null) {
      chunker = new EnglishChunker();
    }
    return chunker;
  }

  @Override
  public final Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new EnglishSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public final Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new XmlRuleDisambiguator(new English());
    }
    return disambiguator;
  }

  @Override
  public final WordTokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new EnglishWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    if (languageModel == null) {
      languageModel = new LuceneLanguageModel(indexDir);
    }
    return languageModel;
  }

  @Override
  public final Contributor[] getMaintainers() {
    return new Contributor[] { Contributors.MARCIN_MILKOWSKI, Contributors.DANIEL_NABER };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
        new CommaWhitespaceRule(messages),
        new DoublePunctuationRule(messages),
        new UppercaseSentenceStartRule(messages, this),
        new MultipleWhitespaceRule(messages, this),
        new LongSentenceRule(messages),
        new SentenceWhitespaceRule(messages),
        // specific to English:
        new EnglishUnpairedBracketsRule(messages, this),
        new EnglishWordRepeatRule(messages, this),
        new AvsAnRule(messages),
        new EnglishWordRepeatBeginningRule(messages, this),
        new CompoundRule(messages),
        new ContractionSpellingRule(messages)
    );
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    return Arrays.<Rule>asList(
        new EnglishConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  /** @since 2.7 */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }
}
