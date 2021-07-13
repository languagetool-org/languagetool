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
import org.languagetool.rules.es.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.es.SpanishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.es.SpanishHybridDisambiguator;
import org.languagetool.tagging.es.SpanishTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.es.SpanishWordTokenizer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Spanish extends Language implements AutoCloseable {

  private static final Language DEFAULT_VARIANT = new Spanish();
  
  private LanguageModel languageModel;

  @Override
  public String getName() {
    return "Spanish";
  }

  @Override
  public String getShortCode() {
    return "es";
  }

  @Override
  public String[] getCountries() {
    return new String[]{
            "ES", "", "MX", "GT", "CR", "PA", "DO",
            "VE", "PE", "AR", "EC", "CL", "UY", "PY",
            "BO", "SV", "HN", "NI", "PR", "US", "CU"
    };
  }
  
  public Language getDefaultLanguageVariant() {
    return DEFAULT_VARIANT;
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return SpanishTagger.INSTANCE;
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new SpanishHybridDisambiguator();
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new SpanishWordTokenizer();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return new SpanishSynthesizer(this);
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Juan Martorell", "http://languagetool-es.blogspot.com/"),
            new Contributor("Jaume Ortolà")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages,
                Example.wrong("En su opinión<marker> ,</marker> no era verdad."),
                Example.fixed("En su opinión<marker>,</marker> no era verdad.")),
            new DoublePunctuationRule(messages),
            new SpanishUnpairedBracketsRule(messages),
            new QuestionMarkRule(messages),
            new MorfologikSpanishSpellerRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this, 
                Example.wrong("Venta al público. <marker>ha</marker> subido mucho."),
                Example.fixed("Venta al público. <marker>Ha</marker> subido mucho.")),
            new SpanishWordRepeatRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new SpanishWikipediaRule(messages),
            new SpanishWrongWordInContextRule(messages),
            new LongSentenceRule(messages, userConfig, 50),
            new LongParagraphRule(messages, this, userConfig),
            new SimpleReplaceRule(messages),
            new SimpleReplaceVerbsRule(messages, this),
            new SpanishWordRepeatBeginningRule(messages, this)
    );
  }

  /** @since 3.1 */
  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Arrays.asList(
            new SpanishConfusionProbabilityRule(messages, languageModel, this)
    );
  }
  
  /** @since 5.1 */
  public String getOpeningDoubleQuote() {
    return "«";
  }

  /** @since 5.1 */
  public String getClosingDoubleQuote() {
    return "»";
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

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }
  
  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      case "CONFUSIONS2": return 50; // greater than CONFUSIONS
      case "LOS_MAPUCHE": return 50;
      case "TE_TILDE": return 50;
      case "PLURAL_SEPARADO": return 50;
      case "INCORRECT_EXPRESSIONS": return 40;
      case "MISSPELLING": return 40;  
      case "CONFUSIONS": return 40;
      case "NO_SEPARADO": return 40;
      case "PARTICIPIO_MS": return 40;
      case "DIACRITICS": return 30;
      case "POR_CIERTO": return 30;
      case "LO_LOS": return 30;
      case "ES_SIMPLE_REPLACE": return 30; // greater than typography rules
      case "ETCETERA": return 30; // greater than other typography rules
      case "P_EJ": return 30; // greater than other typography rules
      //case "ESPACIO_DESPUES_DE_PUNTO": return 25; // greater than other typography rules
      case "AGREEMENT_ADJ_NOUN_AREA": return 30; // greater than AGREEMENT_DET_NOUN
      case "SE_CREO": return 25; // less than DIACRITICS_VERB_N_ADJ
      case "PRONOMBRE_SIN_VERBO": return 25; // inside CONFUSIONS, but less than other rules ?
      case "AGREEMENT_DET_ABREV": return 25; // greater than AGREEMENT_DET_NOUN
      case "MUCHO_NF": return 25; // greater than AGREEMENT_DET_NOUN
      case "AGREEMENT_DET_NOUN_EXCEPTIONS": return 25; // greater than AGREEMENT_DET_NOUN
      case "TYPOGRAPHY": return 20; // greater than AGREEMENT_DET_NOUN
      case "AGREEMENT_DET_NOUN": return 15;
      //case "PRONOMBRE_SIN_VERBO": return 20;
      case "AGREEMENT_DET_ADJ": return 10;
      case "HALLA_HAYA": return 10;
      case "VALLA_VAYA": return 10;
      case "TE_TILDE2": return 10; // less than PRONOMBRE_SIN_VERBO
      case "SINGLE_CHARACTER": return 5;
      case "SEPARADO": return 1;
      case "E_EL": return -10;
      case "EL_TILDE": return -10;
      case "TOO_LONG_PARAGRAPH": return -15;
      case "PREP_VERB": return -20;
      case "SUBJUNTIVO_FUTURO": return -30;
      case "SUBJUNTIVO_PASADO": return -30;
      case "SUBJUNTIVO_PASADO2": return -30;
      case "AGREEMENT_ADJ_NOUN": return -30;
      case "AGREEMENT_PARTICIPLE_NOUN": return -30;
      case "AGREEMENT_POSTPONED_ADJ": return -30;
      case "COMMA_SINO": return -40;
      case "VOSEO": return -40;
      case "MORFOLOGIK_RULE_ES": return -100;
      case "PHRASE_REPETITION": return -150;
      case "SPANISH_WORD_REPEAT_RULE": return -150;
      case "UPPERCASE_SENTENCE_START": return -200;
    }
    //STYLE is -50
    return super.getPriorityForId(id);
  }

}
