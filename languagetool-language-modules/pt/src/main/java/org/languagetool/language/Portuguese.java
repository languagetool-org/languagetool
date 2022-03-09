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
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRuleCreator;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.pt.*;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.pt.PortugueseSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.pt.PortugueseHybridDisambiguator;
import org.languagetool.tagging.pt.PortugueseTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.pt.PortugueseWordTokenizer;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Post-spelling-reform Portuguese.
 */
public class Portuguese extends Language implements AutoCloseable {

  private static final Language PORTUGAL_PORTUGUESE = new PortugalPortuguese();

  private LanguageModel languageModel;

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
            new Contributor("Tiago F. Santos (3.6-4.7)", "https://github.com/TiagoSantos81"),
            new Contributor("Matheus Poletto (pt-BR)", "https://github.com/MatheusPoletto")
    };
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new PortugueseTagger();
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new PortugueseHybridDisambiguator();
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new PortugueseWordTokenizer();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return new PortugueseSynthesizer(this);
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages,
                Example.wrong("Tomamos café<marker> ,</marker> queijo, bolachas e uvas."),
                Example.fixed("Tomamos café<marker>,</marker> queijo, bolachas e uvas.")),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{", "\"", "“" /*, "«", "'", "‘" */),
                    Arrays.asList("]", ")", "}", "\"", "”" /*, "»", "'", "’" */)),
            new HunspellRule(messages, this, userConfig, altLanguages),
            new LongSentenceRule(messages, userConfig, 50),
            new LongParagraphRule(messages, this, userConfig),
            new UppercaseSentenceStartRule(messages, this,
                Example.wrong("Esta casa é velha. <marker>foi</marker> construida em 1950."),
                Example.fixed("Esta casa é velha. <marker>Foi</marker> construida em 1950.")),
            new MultipleWhitespaceRule(messages, this),
            new SentenceWhitespaceRule(messages),
            new WhiteSpaceBeforeParagraphEnd(messages, this),
            new WhiteSpaceAtBeginOfParagraph(messages),
            new EmptyLineRule(messages, this),
            new ParagraphRepeatBeginningRule(messages, this),
            new PunctuationMarkAtParagraphEnd(messages, this, true),
            //Specific to Portuguese:
            new PostReformPortugueseCompoundRule(messages, this, userConfig),
            new PortugueseReplaceRule(messages),
            new PortugueseBarbarismsRule(messages, "/pt/barbarisms-pt.txt"),
            //new PortugueseArchaismsRule(messages, "/pt/archaisms-pt.txt"),   // see https://github.com/languagetool-org/languagetool/issues/3095
            new PortugueseClicheRule(messages),
            new PortugueseFillerWordsRule(messages, this, userConfig),
            new PortugueseRedundancyRule(messages),
            new PortugueseWordinessRule(messages),
            new PortugueseWeaselWordsRule(messages),
            new PortugueseWikipediaRule(messages),
            new PortugueseWordRepeatRule(messages, this),
            new PortugueseWordRepeatBeginningRule(messages, this),
            new PortugueseAccentuationCheckRule(messages),
            new PortugueseDiacriticsRule(messages),
            new PortugueseWrongWordInContextRule(messages),
            new PortugueseWordCoherencyRule(messages),
            new PortugueseUnitConversionRule(messages),
            new PortugueseReadabilityRule(messages, this, userConfig, true),
            new PortugueseReadabilityRule(messages, this, userConfig, false)
    );
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  /** @since 3.6 */
  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
  }

  /** @since 3.6 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Arrays.asList(
            new PortugueseConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  /** @since 4.0 */
  @Override
  public synchronized Word2VecModel getWord2VecModel(File indexDir) throws IOException {
    return new Word2VecModel(indexDir + File.separator + getShortCode());
  }

  /** @since 4.0 */
  @Override
  public List<Rule> getRelevantWord2VecModelRules(ResourceBundle messages, Word2VecModel word2vecModel) throws IOException {
    return NeuralNetworkRuleCreator.createRules(messages, this, word2vecModel);
  }

  /** @since 3.6 */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }

  /** @since 5.1 */
  @Override
  public String getOpeningDoubleQuote() {
    return "“";
  }

  /** @since 5.1 */
  @Override
  public String getClosingDoubleQuote() {
    return "”";
  }
  
  /** @since 5.1 */
  @Override
  public String getOpeningSingleQuote() {
    return "‘";
  }

  /** @since 5.1 */
  @Override
  public String getClosingSingleQuote() {
    return "’";
  }
  
  /** @since 5.1 */
  @Override
  public boolean isAdvancedTypographyEnabled() {
    return true;
  }
  
  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      case "FRAGMENT_TWO_ARTICLES":     return 50;
      case "DEGREE_MINUTES_SECONDS":    return 30;
      case "INTERJECTIONS_PUNTUATION":  return 20;
      case "CONFUSION_POR":             return 10;
      case "VERB_COMMA_CONJUNCTION":    return 10; // greater than PORTUGUESE_WORD_REPEAT_RULE
      case "HOMOPHONE_AS_CARD":         return  5;
      case "TODOS_FOLLOWED_BY_NOUN_PLURAL":    return  3;
      case "TODOS_FOLLOWED_BY_NOUN_SINGULAR":  return  2;
      case "EMAIL":                     return  1;
      case "UNPAIRED_BRACKETS":         return -5;
      case "PROFANITY":                 return -6;
      case "PT_BARBARISMS_REPLACE":     return -10;
      case "PT_PT_SIMPLE_REPLACE":      return -11;
      case "PT_REDUNDANCY_REPLACE":     return -12;
      case "PT_WORDINESS_REPLACE":      return -13;
      case "PT_CLICHE_REPLACE":         return -17;
      case "INTERNET_ABBREVIATIONS":    return -24;
      case "CHILDISH_LANGUAGE":         return -25;
      case "ARCHAISMS":                 return -26;
      case "INFORMALITIES":             return -27;
      case "PUFFERY":                   return -30;
      case "BIASED_OPINION_WORDS":      return -31;
      case "WEAK_WORDS":                return -32;
      case "PT_AGREEMENT_REPLACE":      return -35;
      case "PT_DIACRITICS_REPLACE":     return -45;   // prefer over spell checker
      case "HUNSPELL_RULE":             return -50;
      case "NO_VERB":                   return -52;
      case "CRASE_CONFUSION":           return -55;
      case "FINAL_STOPS":               return -75;
      case "EU_NÓS_REMOVAL":            return -90;
      case "T-V_DISTINCTION":           return -100;
      case "T-V_DISTINCTION_ALL":       return -101;
      case "REPEATED_WORDS":            return -210;
      case "REPEATED_WORDS_3X":         return -211;
      case "PT_WIKIPEDIA_COMMON_ERRORS":return -500;
      case "FILLER_WORDS_PT":           return -990;
      case LongSentenceRule.RULE_ID:    return -997;
      case LongParagraphRule.RULE_ID:   return -998;
      case "READABILITY_RULE_SIMPLE_PT":       return -1100;
      case "READABILITY_RULE_DIFFICULT_PT":    return -1101;
      case "CACOPHONY":                 return -1500;
      case "UNKNOWN_WORD":              return -2000;
    }
    return 0;
  }
}
