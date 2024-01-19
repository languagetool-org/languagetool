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
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spanish extends Language implements AutoCloseable {
  
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

  @Override
  public Language getDefaultLanguageVariant() {
    return Languages.getLanguageForShortCode("es");
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return SpanishTagger.INSTANCE;
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new SpanishHybridDisambiguator(getDefaultLanguageVariant());
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new SpanishWordTokenizer();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return SpanishSynthesizer.INSTANCE;
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Jaume Ortolà")
    };
  }

  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikSpanishSpellerRule(messages, this, null, Collections.emptyList());
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
            new SpanishWrongWordInContextRule(messages, this),
            new LongSentenceRule(messages, userConfig, 60),
            new LongParagraphRule(messages, this, userConfig),
            new SimpleReplaceRule(messages, this),
            new SimpleReplaceVerbsRule(messages, this),
            new SpanishWordRepeatBeginningRule(messages, this),
            new CompoundRule(messages, this, userConfig),
            new SpanishRepeatedWordsRule(messages)
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
    if (id.startsWith("ES_SIMPLE_REPLACE_SIMPLE")) {
      return 30;
    }
    if (id.startsWith("ES_COMPOUNDS")) {
      return 50;
    }
    switch (id) {
      case "CONFUSIONS2": return 50; // greater than CONFUSIONS
      case "RARE_WORDS": return 50;
      case "LOS_MAPUCHE": return 50;
      case "TE_TILDE": return 50;
      case "DE_TILDE": return 50; // greater than CONTRACCIONES
      case "PLURAL_SEPARADO": return 50;
      case "PERSONAJES_FAMOSOS": return 50;
      case "INCORRECT_EXPRESSIONS": return 40;
      case "MISSPELLING": return 40;  
      case "CONFUSIONS": return 40;
      case "NO_SEPARADO": return 40;
      case "PARTICIPIO_MS": return 40;
      case "VERBO_MODAL_INFINITIVO": return 40; // greater than DIACRITICS
      case "EL_NO_TILDE": return 40; // greater than SE_CREO
      case "SE_CREO": return 35; // greater than DIACRITICS --> or less than DIACRITICS_VERB_N_ADJ ????
      case "DIACRITICS": return 30;
      case "POR_CIERTO": return 30;
      case "DEGREE_CHAR": return 30; // greater than SPACE_UNITIES
      case "LO_LOS": return 30;
      case "ETCETERA": return 30; // greater than other typography rules
      case "P_EJ": return 30; // greater than other typography rules
      case "SE_CREO2": return 25; 
      //case "ESPACIO_DESPUES_DE_PUNTO": return 25; // greater than other typography rules
      case "AGREEMENT_ADJ_NOUN_AREA": return 30; // greater than AGREEMENT_DET_NOUN
      case "PRONOMBRE_SIN_VERBO": return 25; // inside CONFUSIONS, but less than other rules ?
      case "AGREEMENT_DET_ABREV": return 25; // greater than AGREEMENT_DET_NOUN
      case "MUCHO_NF": return 25; // greater than AGREEMENT_DET_NOUN
      case "AGREEMENT_DET_NOUN_EXCEPTIONS": return 25; // greater than AGREEMENT_DET_NOUN
      case "TYPOGRAPHY": return 20; // greater than AGREEMENT_DET_NOUN
      case "AGREEMENT_DET_NOUN": return 15;
      //case "PRONOMBRE_SIN_VERBO": return 20;
      case "AGREEMENT_DET_ADJ": return 10;
      case "CONFUSION_ES_SE": return 20; //lower than diacrtics rules
      case "HALLA_HAYA": return 10;
      case "VALLA_VAYA": return 10;
      case "SI_AFIRMACION": return 10; // less than DIACRITICS
      case "TE_TILDE2": return 10; // less than PRONOMBRE_SIN_VERBO
      case "SEPARADO": return 1;
      case "ES_SPLIT_WORDS": return -10;
      case "U_NO": return -10;
      case "E_EL": return -10;
      case "EL_TILDE": return -10;
      case "SINGLE_CHARACTER": return -15; // less than ES_SPLIT_WORDS
      case "TOO_LONG_PARAGRAPH": return -15;
      case "PREP_VERB": return -20;
      case "SUBJUNTIVO_FUTURO": return -30;
      case "SUBJUNTIVO_PASADO": return -30;
      case "SUBJUNTIVO_PASADO2": return -30;
      case "AGREEMENT_ADJ_NOUN": return -30;
      case "AGREEMENT_PARTICIPLE_NOUN": return -30;
      case "AGREEMENT_POSTPONED_ADJ": return -30;
      case "MULTI_ADJ": return -30;
      case "SUBJUNTIVO_INCORRECTO": return -40;
      case "COMMA_SINO": return -40;
      case "COMMA_SINO2": return -40;
      case "VOSEO": return -40;
      case "REPETITIONS_STYLE": return -50;
      case "MORFOLOGIK_RULE_ES": return -100;
      case "PHRASE_REPETITION": return -150;
      case "SPANISH_WORD_REPEAT_RULE": return -150;
      case "UPPERCASE_SENTENCE_START": return -200;
      case "ES_QUESTION_MARK": return -250;
    }

    if (id.startsWith("AI_ES_HYDRA_LEO")) { // prefer more specific rules (also speller)
      return -101;
    }
    if (id.startsWith("AI_ES_GGEC")) { // prefer more specific rules (also speller)
      return 0;
      //return -102;
    }
    if (id.startsWith("ES_MULTITOKEN_SPELLING")) {
      return -95;
    }

    //STYLE is -50
    return super.getPriorityForId(id);
  }

  public boolean hasMinMatchesRules() {
    return true;
  }
  
  private static final Pattern ES_CONTRACTIONS = Pattern.compile("\\b([Aa]|[Dd]e) e(l)\\b");
  
  @Override
  public String adaptSuggestion(String replacement) {
    Matcher m = ES_CONTRACTIONS.matcher(replacement);
    String newReplacement = m.replaceAll("$1$2");
    return newReplacement;
  }

  @Override
  public String prepareLineForSpeller(String line) {
    String[] parts = line.split("#");
    if (parts.length == 0) {
      return line;
    }
    String[] formTag = parts[0].split("[\t;]");
    if (formTag.length > 1) {
      String tag = formTag[1].trim();
      if (tag.startsWith("N") || tag.equals("_Latin_") || tag.equals("LOC_ADV")) {
        return formTag[0].trim();
      } else {
        return "";
      }
    }
    return line;
  }

  public MultitokenSpeller getMultitokenSpeller() {
    return SpanishMultitokenSpeller.INSTANCE;
  }
}
