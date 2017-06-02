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
import org.languagetool.LanguageMaintainedState;
//import org.languagetool.chunking.Chunker;
//import org.languagetool.chunking.ArabicChunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.ar.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.ar.ArabicWordTokenizer;
import org.languagetool.rules.spelling.hunspell.HunspellNoSuggestionRule;
/**
 * Support for Arabic - use the sub classes {@link BritishArabic}, {@link AmericanArabic},
 * etc. if you need spell checking.
 * Make sure to call {@link #close()} after using this (currently only relevant if you make
 * use of {@link ArabicConfusionProbabilityRule}).
 */
public class Arabic extends Language implements AutoCloseable {

  private Tagger tagger;
  //private Chunker chunker;
  private SentenceTokenizer sentenceTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
  private WordTokenizer wordTokenizer;
  private LuceneLanguageModel languageModel;

  /**
   * @deprecated use {@link AmericanArabic} or {@link BritishArabic} etc. instead -
   *  they have rules for spell checking, this class doesn't (deprecated since 3.2)
   
  @Deprecated
  public Arabic() {
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return this.Arabic(); 
  }
*/
  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

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
    return new String[]{"DZ","EG","TN","SA"};
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new ArabicTagger();
    }
    return tagger;
  }

  /**
   * @since 2.3
   */
 /* @Override
  public Chunker getChunker() {
    if (chunker == null) {
      chunker = new ArabicChunker();
    }
    return chunker;
  }
*/
  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new ArabicSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new XmlRuleDisambiguator(new Arabic());
    }
    return disambiguator;
  }

  @Override
  public WordTokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new ArabicWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    if (languageModel == null) {
      languageModel = new LuceneLanguageModel(new File(indexDir, getShortCode()));
    }
    return languageModel;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Taha Zerrouki"), new Contributor("Kali Imen"), new Contributor("TCHOKETCH Karima") };
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
        new CommaWhitespaceRule(messages,
                Example.wrong("نعم<marker> ,</marker> لقد نجحنا."),
                Example.fixed("نعم<marker>,</marker> لقد نجحنا.")),
        new DoublePunctuationRule(messages),
        new MultipleWhitespaceRule(messages, this),
        new LongSentenceRule(messages),
        new SentenceWhitespaceRule(messages),
        // specific to Arabic:
        new ArabicUnpairedBracketsRule(messages, this),
        new ArabicWordRepeatRule(messages, this),
        new HunspellNoSuggestionRule(messages, this, Example.wrong("هذا <marker>الانسان</marker> رائع"), Example.fixed("هذا <marker>الإنسان</marker> رائع"))
        //new AvsAnRule(messages),
        //new ArabicWordRepeatBeginningRule(messages, this),
        //new CompoundRule(messages),
        //new ContractionSpellingRule(messages)
    );
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws IOException {
    return Arrays.<Rule>asList(
        new ArabicConfusionProbabilityRule(messages, languageModel, this)
        //new ArabicNgramProbabilityRule(messages, languageModel, this)
    );
  }

  /**
   * Closes the language model, if any. 
   * @since 2.7 
   */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }
}
