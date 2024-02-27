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
import org.languagetool.rules.pt.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;
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
    return Languages.getLanguageForShortCode("pt-PT");
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Marco A.G. Pinto", "http://www.marcoagpinto.com/"),
            new Contributor("Susana Boatto (pt-BR)"),
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
    return new PortugueseHybridDisambiguator(getDefaultLanguageVariant());
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new PortugueseWordTokenizer();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return PortugueseSynthesizer.INSTANCE;
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikPortugueseSpellerRule(messages, this, null, null);
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
            new MorfologikPortugueseSpellerRule(messages, this, userConfig, altLanguages),
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
            new PortugueseColourHyphenationRule(messages, this, userConfig),
            new PortugueseReplaceRule(messages, this),
            new PortugueseBarbarismsRule(messages, "/pt/barbarisms.txt", this),
            //new PortugueseArchaismsRule(messages, "/pt/archaisms-pt.txt"),   // see https://github.com/languagetool-org/languagetool/issues/3095
            new PortugueseClicheRule(messages, "/pt/cliches.txt", this),
            new PortugueseFillerWordsRule(messages, this, userConfig),
            new PortugueseRedundancyRule(messages, "/pt/redundancies.txt", this),
            new PortugueseWordinessRule(messages, "/pt/wordiness.txt", this),
            //new PortugueseWeaselWordsRule(messages),
            new PortugueseWikipediaRule(messages, "/pt/wikipedia.txt", this),
            new PortugueseWordRepeatRule(messages, this),
            new PortugueseWordRepeatBeginningRule(messages, this),
            new PortugueseAccentuationCheckRule(messages),
            new PortugueseDiacriticsRule(messages),
            new PortugueseWrongWordInContextRule(messages, this),
            new PortugueseWordCoherencyRule(messages),
            new PortugueseUnitConversionRule(messages),
            new PortugueseReadabilityRule(messages, this, userConfig, true),
            new PortugueseReadabilityRule(messages, this, userConfig, false),
            new DoublePunctuationRule(messages)
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
  
  private final static Map<String, Integer> id2prio = new HashMap<>();
  static {
    id2prio.put("FRAGMENT_TWO_ARTICLES", 50);
    id2prio.put("DEGREE_MINUTES_SECONDS", 30);
    id2prio.put("INTERJECTIONS_PUNTUATION", 20);
    id2prio.put("CONFUSION_POR_PÔR_V2", 10);
    id2prio.put("PARONYM_POLITICA_523", 10);
    id2prio.put("PARONYM_PRONUNCIA_262", 10);
    id2prio.put("PARONYM_CRITICA_397", 10);
    id2prio.put("PARONYM_INICIO_169", 10);
    id2prio.put("LP_PARONYMS", 10);
    id2prio.put("PARONYM_MUSICO_499_bis", 10);
    id2prio.put("NA_NÃO", 10);
    id2prio.put("VERB_COMMA_CONJUNCTION", 10); // greater than PORTUGUESE_WORD_REPEAT_RULE
    id2prio.put("HOMOPHONE_AS_CARD", 5);
    id2prio.put("TODOS_FOLLOWED_BY_NOUN_PLURAL", 3);
    id2prio.put("TODOS_FOLLOWED_BY_NOUN_SINGULAR", 2);
    id2prio.put("AUSENCIA_VIRGULA", 1);
    id2prio.put("EMAIL", 1);
    id2prio.put("UNPAIRED_BRACKETS", -5);
    id2prio.put("PROFANITY", -6);
    id2prio.put("PT_BARBARISMS_REPLACE", -10);
    id2prio.put("BARBARISMS_PT_PT_V3", -10);
    id2prio.put("PT_PT_SIMPLE_REPLACE", -11);  // for pt-PT, not lower than speller, not sure why
    id2prio.put("PT_REDUNDANCY_REPLACE", -12);
    id2prio.put("PT_WORDINESS_REPLACE", -13);
    id2prio.put("PT_CLICHE_REPLACE", -17);
    id2prio.put("INTERNET_ABBREVIATIONS", -24);
    id2prio.put("CHILDISH_LANGUAGE", -25);
    id2prio.put("ARCHAISMS", -26);
    id2prio.put("INFORMALITIES", -27);
    id2prio.put("BIASED_OPINION_WORDS", -31);
    id2prio.put("PT_AGREEMENT_REPLACE", -35);
    id2prio.put("CONTA_TO", -44);
    id2prio.put("PT_DIACRITICS_REPLACE", -45);  // prefer over spell checker
    id2prio.put("DIACRITICS", -45);
    id2prio.put("PT_COMPOUNDS_POST_REFORM", -45);
    id2prio.put("AUX_VERBO", -45);
    id2prio.put("ENSINO_A_DISTANCIA", -45);
    id2prio.put("EMAIL_SEM_HIFEN_ORTHOGRAPHY", -45); // HIGHER THAN SPELLER
    // MORFOLOGIK SPELLER FITS HERE AT -50 ---------------------  // SPELLER (-50)
    id2prio.put("PRETERITO_PERFEITO", -51);  // LOWER THAN SPELLER
    id2prio.put("PT_BR_SIMPLE_REPLACE", -51);
    id2prio.put("CRASE_CONFUSION", -54);
    id2prio.put("NAO_MILITARES", -54);
    id2prio.put("NA_QUELE", -54);
    id2prio.put("NOTAS_FICAIS", -54);
    id2prio.put("GENERAL_VERB_AGREEMENT_ERRORS", -55);
    id2prio.put("GENERAL_NUMBER_AGREEMENT_ERRORS", -56);
    id2prio.put("GENERAL_GENDER_NUMBER_AGREEMENT_ERRORS", -56);
    id2prio.put("FINAL_STOPS", -75);
    id2prio.put("EU_NÓS_REMOVAL", -90);
    id2prio.put("COLOCAÇÃO_ADVÉRBIO", -90);
    id2prio.put("FAZER_USO_DE-USAR-RECORRER", -90);
    id2prio.put("FORMAL_T-V_DISTINCTION", -100);
    id2prio.put("FORMAL_T-V_DISTINCTION_ALL", -101);
    id2prio.put("REPEATED_WORDS", -210);
    id2prio.put("PT_WIKIPEDIA_COMMON_ERRORS", -500);
    id2prio.put("FILLER_WORDS_PT", -990);
    id2prio.put(LongSentenceRule.RULE_ID, -997);
    id2prio.put(LongParagraphRule.RULE_ID, -998);
    id2prio.put("READABILITY_RULE_SIMPLE_PT", -1100);
    id2prio.put("READABILITY_RULE_DIFFICULT_PT", -1101);
    id2prio.put("UNKNOWN_WORD", -2000);
  }

  @Override
  public Map<String, Integer> getPriorityMap() {
    return id2prio;
  }
  
  @Override
  protected int getPriorityForId(String id) {
    if (id.startsWith("MORFOLOGIK_RULE")) {
      return -50;
    }
    if (id.startsWith("PT_MULTITOKEN_SPELLING")) {
      return -49;
    }
    Integer prio = id2prio.get(id);
    if (prio != null) {
      return prio;
    }

    if (id.startsWith("AI_PT_HYDRA_LEO")) { // prefer more specific rules (also speller)
      if (id.startsWith("AI_PT_HYDRA_LEO_MISSING_COMMA")) {
        return -51; // prefer comma style rules.
      }
      return -51;
    }
    return super.getPriorityForId(id);
  }

  @Override
  public List<String> prepareLineForSpeller(String line) {
    String[] parts = line.split("#");
    if (parts.length == 0) {
      return Arrays.asList(line);
    }
    String[] formTag = parts[0].split("[\t;]");
    String form = formTag[0].trim();
    if (formTag.length > 1) {
      String tag = formTag[1].trim();
      if (tag.startsWith("N") || tag.equals("_Latin_")) {
        return Arrays.asList(form);
      } else {
        return Arrays.asList("");
      }
    }
    return Arrays.asList(line);
  }

  public MultitokenSpeller getMultitokenSpeller() {
    return PortugueseMultitokenSpeller.INSTANCE;
  }
}
