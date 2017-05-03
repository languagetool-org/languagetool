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

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.rules.*;
import org.languagetool.rules.pt.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.pt.PortugueseSynthesizer;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.pt.PortugueseHybridDisambiguator;
import org.languagetool.tagging.pt.PortugueseTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.pt.PortugueseWordTokenizer;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;

/**
 * Post-spelling-reform Portuguese.
 */
public class Portuguese extends Language implements AutoCloseable {

  private static final Language PORTUGAL_PORTUGUESE = new PortugalPortuguese();
  
  private Tagger tagger;
  private Disambiguator disambiguator;
  private Tokenizer wordTokenizer;
  private Synthesizer synthesizer;
  private SentenceTokenizer sentenceTokenizer;
  private LuceneLanguageModel languageModel;

  @Override
  public String getName() {
    return "Portuguese";
  }

  @Override
  public String getShortCode() {
    return "pt";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"", "CV", "GW", "MO", "ST", "TL"};
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return PORTUGAL_PORTUGUESE;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Marco A.G. Pinto", "http://www.marcoagpinto.com/"),
            new Contributor("Matheus Poletto", "https://github.com/MatheusPoletto"),
            new Contributor("Tiago F. Santos (3.6+)", "https://github.com/TiagoSantos81")
    };
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new PortugueseTagger();
    }
    return tagger;
  }

  /**
   * @since 3.6
   */
  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new PortugueseHybridDisambiguator();
    }
    return disambiguator;
  }

  /**
   * @since 3.6
   */
  @Override
  public Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new PortugueseWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new PortugueseSynthesizer();
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
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages,
                Example.wrong("Tomamos café<marker> ,</marker> queijo, bolachas e uvas."),
                Example.fixed("Tomamos café<marker>,</marker> queijo, bolachas e uvas")),
            new GenericUnpairedBracketsRule(messages),
            new HunspellRule(messages, this),
            new LongSentenceRule(messages, 45, true),
            new UppercaseSentenceStartRule(messages, this,
                Example.wrong("Esta casa é velha. <marker>foi</marker> construida em 1950."),
                Example.fixed("Esta casa é velha. <marker>Foi</marker> construida em 1950.")),
            new MultipleWhitespaceRule(messages, this),
            new SentenceWhitespaceRule(messages),
            //Specific to Portuguese:
            new PostReformPortugueseCompoundRule(messages),
            new PortugueseReplaceRule(messages),
            new PortugueseReplaceRule2(messages),
            new PortugueseClicheRule(messages),
            new PortugueseRedundancyRule(messages),
            new PortugueseWordynessRule(messages),
            new PortugueseWikipediaRule(messages),
            new PortugueseWordRepeatRule(messages, this),
            new PortugueseWordRepeatBeginningRule(messages, this),
            new PortugueseAccentuationCheckRule(messages),
            new PortugueseWrongWordInContextRule(messages)
    );
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  /** @since 3.6 */
  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    if (languageModel == null) {
      languageModel = new LuceneLanguageModel(new File(indexDir, getShortCode()));
    }
    return languageModel;
  }

  /** @since 3.6 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    return Arrays.<Rule>asList(
            new PortugueseConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  /** @since 3.6 */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case "FRAGMENT_TWO_ARTICLES":     return 50;
      case "INTERJECTIONS_PUNTUATION":  return  5;
      case "PT_MULTI_REPLACE":          return -10;
      case "PT_PT_SIMPLE_REPLACE":      return -11;
      case "PT_REDUNDANCY_REPLACE":     return -12;
      case "PT_WORDYNESS_REPLACE":      return -13;
      case "PT_CLICHE_REPLACE":         return -17;
      case "HUNSPELL_RULE":             return -20;
      case "CRASE_CONFUSION":           return -25;
      case "FINAL_STOPS":               return -35;
      case "T-V_DISTINCTION":           return -50;
      case "T-V_DISTINCTION_ALL":       return -51;
      case "REPEATED_WORDS":            return -90;
      case "REPEATED_WORDS_3X":         return -91;
      case "WIKIPEDIA_COMMON_ERRORS":   return -100;
      case "TOO_LONG_SENTENCE":         return -1000;
    }
    return 0;
  }
}
