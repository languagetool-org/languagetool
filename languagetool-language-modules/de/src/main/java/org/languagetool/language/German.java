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
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.GermanChunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.de.LongSentenceRule;
import org.languagetool.rules.de.SentenceWhitespaceRule;
import org.languagetool.rules.de.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.synthesis.GermanSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.de.GermanRuleDisambiguator;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tokenizers.de.GermanWordTokenizer;
import org.languagetool.tools.Tools;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Support for German - use the sub classes {@link GermanyGerman}, {@link SwissGerman}, or {@link AustrianGerman}
 * if you need spell checking.
 */
public class German extends Language implements AutoCloseable {

  private LanguageModel languageModel;

  /**
   * @deprecated use {@link GermanyGerman}, {@link AustrianGerman}, or {@link SwissGerman} instead -
   *  they have rules for spell checking, this class doesn't (deprecated since 3.2)
   */
  @Deprecated
  public German() {
  }
  
  @Override
  public Language getDefaultLanguageVariant() {
    return GermanyGerman.INSTANCE;
  }

  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new GermanSpellerRule(messages, this);
  }

  @NotNull
  @Override
  public GermanSpellerRule getDefaultSpellingRule() {
    return (GermanSpellerRule) Objects.requireNonNull(super.getDefaultSpellingRule());
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new GermanRuleDisambiguator();
  }

  @Nullable
  @Override
  public Chunker createDefaultPostDisambiguationChunker() {
    return new GermanChunker();
  }

  @Override
  public String getName() {
    return "German";
  }

  @Override
  public String getShortCode() {
    return "de";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"LU", "LI", "BE"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return GermanTagger.INSTANCE;
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return GermanSynthesizer.INSTANCE;
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Jan Schreiber"),
        Contributors.DANIEL_NABER,
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new GermanCommaWhitespaceRule(messages,
                    Example.wrong("Die Partei<marker> ,</marker> die die letzte Wahl gewann."),
                    Example.fixed("Die Partei<marker>,</marker> die die letzte Wahl gewann."),
                    Tools.getUrl("https://languagetool.org/insights/de/beitrag/grammatik-leerzeichen/#fehler-1-leerzeichen-vor-und-nach-satzzeichen")),
            new GermanUnpairedBracketsRule(messages, this),
            new UppercaseSentenceStartRule(messages, this,
                    Example.wrong("Das Haus ist alt. <marker>es</marker> wurde 1950 gebaut."),
                    Example.fixed("Das Haus ist alt. <marker>Es</marker> wurde 1950 gebaut."),
                    Tools.getUrl("https://languagetool.org/insights/de/beitrag/gross-klein-schreibung-rechtschreibung/#1-satzanf%C3%A4nge-schreiben-wir-gro%C3%9F")),
            new MultipleWhitespaceRule(messages, this),
            new WhiteSpaceBeforeParagraphEnd(messages, this),
            new WhiteSpaceAtBeginOfParagraph(messages),
            new EmptyLineRule(messages, this),
            new LongParagraphRule(messages, this, userConfig),
            new PunctuationMarkAtParagraphEnd(messages, this),
            // specific to German:
            new SimpleReplaceRule(messages, this),
            new OldSpellingRule(messages),
            new SentenceWhitespaceRule(messages),
            new GermanDoublePunctuationRule(messages),
            new MissingVerbRule(messages, this),
            new GermanWordRepeatRule(messages, this),
            new GermanWordRepeatBeginningRule(messages, this),
            new GermanWrongWordInContextRule(messages),
            new AgreementRule(messages, this),
            new AgreementRule2(messages, this),
            new CaseRule(messages, this),
            new DashRule(messages),
            new VerbAgreementRule(messages, this),
            new SubjectVerbAgreementRule(messages, this),
            new WordCoherencyRule(messages),
            new SimilarNameRule(messages),
            new WiederVsWiderRule(messages),
            new GermanStyleRepeatedWordRule(messages, this, userConfig),
            new CompoundCoherencyRule(messages),
            new LongSentenceRule(messages, userConfig, 40),
            new GermanFillerWordsRule(messages, this, userConfig),
            new PassiveSentenceRule(messages, this, userConfig),
            new SentenceWithModalVerbRule(messages, this, userConfig),
            new SentenceWithManRule(messages, this, userConfig),
            new ConjunctionAtBeginOfSentenceRule(messages, this, userConfig),
            new NonSignificantVerbsRule(messages, this, userConfig),
            new UnnecessaryPhraseRule(messages, this, userConfig),
            new GermanParagraphRepeatBeginningRule(messages, this),
            new DuUpperLowerCaseRule(messages),
            new UnitConversionRule(messages),
            new MissingCommaRelativeClauseRule(messages),
            new MissingCommaRelativeClauseRule(messages, true),
            new RedundantModalOrAuxiliaryVerb(messages),
            new GermanReadabilityRule(messages, this, userConfig, true),
            new GermanReadabilityRule(messages, this, userConfig, false),
            new CompoundInfinitivRule(messages, this, userConfig),
            new StyleRepeatedVeryShortSentences(messages, this),
            new StyleRepeatedSentenceBeginning(messages),
            new GermanRepeatedWordsRule(messages)
    );
  }

  /**
   * @since 2.7
   */
  public CompoundWordTokenizer getNonStrictCompoundSplitter() {
    GermanCompoundTokenizer tokenizer = GermanCompoundTokenizer.getNonStrictInstance();  // there's a spelling mistake in (at least) one part, so strict mode wouldn't split the word
    return word -> new ArrayList<>(tokenizer.tokenize(word));
  }

  /**
   * @since 2.7
   */
  public GermanCompoundTokenizer getStrictCompoundTokenizer() {
    return GermanCompoundTokenizer.getStrictInstance();
  }

  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Arrays.asList(
      new UpperCaseNgramRule(messages, languageModel, this),
      new GermanConfusionProbabilityRule(messages, languageModel, this),
      new ProhibitedCompoundRule(messages, languageModel, userConfig)
    );
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new GermanWordTokenizer();
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
  
  /** @since 5.1 */
  @Override
  public String getOpeningDoubleQuote() {
    return "„";
  }

  /** @since 5.1 */
  @Override
  public String getClosingDoubleQuote() {
    return "“";
  }
  
  /** @since 5.1 */
  @Override
  public String getOpeningSingleQuote() {
    return "‚";
  }

  /** @since 5.1 */
  @Override
  public String getClosingSingleQuote() {
    return "‘";
  }
  
  /** @since 5.1 */
  @Override
  public boolean isAdvancedTypographyEnabled() {
    return true;
  }
  
  @Override
  public String toAdvancedTypography(String input) {
    String output = super.toAdvancedTypography(input);
    //non-breaking space
    output = output.replaceAll("\\b([a-zA-Z]\\.)([a-zA-Z]\\.)", "$1\u00a0$2");
    output = output.replaceAll("\\b([a-zA-Z]\\.)([a-zA-Z]\\.)", "$1\u00a0$2");
    return output;
  }
  
  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      // Rule ids:
      case "DE_PROHIBITED_PHRASE": return 11;
      case "WRONG_SPELLING_PREMIUM_INTERNAL": return 10;
      case "OLD_SPELLING_INTERNAL": return 10;
      case "DE_COMPOUNDS": return 10;
      case "TELEFON_NR": return 10;
      case "IRGEND_COMPOUND": return 10;
      case "DA_DURCH": return 2; // prefer over SUBSTANTIVIERUNG_NACH_DURCH and DURCH_SCHAUEN and DURCH_WACHSEN
      case "BEI_GOOGLE" : return 2;   // prefer over agreement rules and VOR_BEI
      case "EINE_ORIGINAL_RECHNUNG_TEST" : return 2;   // prefer over agreement rules
      case "VONSTATTEN_GEHEN" : return 2;   // prefer over EINE_ORIGINAL_RECHNUNG
      case "VERWECHSLUNG_MIR_DIR_MIR_DIE": return 1; // prefer over MIR_DIR
      case "ERNEUERBARE_ENERGIEN": return 1; // prefer over VEREINBAREN
      case "DRIVE_IN": return 1; // prefer over agreement rules
      case "AN_STATT": return 1; // prefer over agreement rules
      case "VOR_BEI": return 1; // prefer over BEI_BEHALTEN
      case "SUB_VER_KLEIN": return 1; // prefer over casing rules
      case "ALLES_GUTE": return 1; // prefer over premium rules
      case "NEUN_NEUEN": return 1; // prefer over VIELZAHL_PLUS_SINGULAR
      case "VERWANDET_VERWANDTE": return 1; // prefer over DE_CASE
      case "IN_DEUTSCHE_SPRACHE": return 1; // prefer over most other rules
      case "SCHMIERE_STEHEN": return 1; // prefer over most other rules
      case "UEBER_EIN_MANGEL": return 1; // prefer over PRAEP_AKK
      case "SEIT_LAENGEREN": return 1; // prefer over DE_CASE
      case "WIR_GEFUEHL": return 1; // prefer over DE_CASE
      case "VORHER_NACHHER_BILD": return 1; // prefer over DE_CASE
      case "SEIT_KLEIN_AUF": return 1; // prefer over agreement rules
      case "SEIT_GEBURT_AN": return 1; // prefer over agreement rules
      case "WO_VON": return 1; // prefer over most agreement rules
      case "ICH_BIN_STAND_JETZT_KOMMA": return 1; // prefer over most agreement rules
      case "EIN_LOGGEN": return 1; // prefer over most agreement rules
      case "ZU_GENÜGE" : return 1;   // prefer over ZU_KOENNE
      case "SEIT_BEKANNT_WERDEN" : return 1;   // prefer over agreement and comma rules
      case "IMPFLICHT" : return 1;   // prefer over agreement rules DE_AGREEMENT
      case "NULL_KOMMA_NICHTS" : return 1;   // prefer over agreement rules
      case "ZWEI_AN_HALB" : return 1;   // prefer over agreement rules
      case "BLUETOOTH_LAUTSPRECHER" : return 1;   // prefer over agreement rules
      case "KOENNT_ICH" : return 1;   // prefer over DE_VERBAGREEMENT
      case "WIR_HABE" : return 1;   // prefer over DE_VERBAGREEMENT
      case "DAS_IST_GLAUBE_ICH_EGAL" : return 1;   // prefer over agreement rules
      case "ICH_KOENNT" : return 1;   // prefer over DE_VERBAGREEMENT
      case "HAT_DU" : return 1;   // prefer over agreement rules
      case "HAST_DICH" : return 1;   // prefer over agreement rules
      case "GRUNDE" : return 1;   // prefer over agreement rules
      case "EIN_FACH" : return 1;   // prefer over agreement rules
      case "WOGEN_SUBST" : return 1;   // prefer over agreement rules
      case "SO_WIES_IST" : return 1;   // prefer over agreement rules
      case "SICH_SICHT" : return 1;   // prefer over agreement rules
      case "MIT_VERANTWORTLICH" : return 1;   // prefer over agreement rules
      case "VOR_LACHEN" : return 1;   // prefer over ZUSAMMENGESETZTE_VERBEN
      case "TOUREN_SUBST" : return 1;   // prefer over ZUSAMMENGESETZTE_VERBEN
      case "AUF_DRÄNGEN" : return 1;   // prefer over ZUSAMMENGESETZTE_VERBEN
      case "AUF_ZACK" : return 1;   // prefer over ZUSAMMENGESETZTE_VERBEN
      case "UNTER_DRUCK" : return 1;   // prefer over ZUSAMMENGESETZTE_VERBEN
      case "ZUCCHINIS" : return 1;   // overwrite spell checker
      case "PASSWORTE" : return 1;   // overwrite agreement rules
      case "ANGL_PA_ED_UNANGEMESSEN" : return 1;   // overwrite spell checker
      case "ANFUEHRUNGSZEICHEN_DE_AT": return 1; // higher prio than UNPAIRED_BRACKETS
      case "ANFUEHRUNGSZEICHEN_CH_FR": return 1; // higher prio than UNPAIRED_BRACKETS
      case "EMAIL": return 1;  // better suggestion than SIMPLE_AGREEMENT_*
      case "IM_STICH_LASSEN": return 1;  // higher prio than agreement rules
      case "ZULANGE": return 1;  // better suggestion than SAGT_RUFT
      case "ROCK_N_ROLL": return 1;  // better error than DE_CASE
      case "JOE_BIDEN": return 1;  // better error than DE_CASE
      case "RESOURCE_RESSOURCE": return 1;  // better error than DE_CASE
      case "DE_PROHIBITED_COMPOUNDS": return 1;  // a more detailed error message than from spell checker
      case "ANS_OHNE_APOSTROPH": return 1;
      case "DIESEN_JAHRES": return 1;
      case "TAG_EIN_TAG_AUS": return 1; // prefer over agreement rules
      case "WERT_SEIN": return 1; // prefer over DE_AGREEMENT
      case "EBEN_FALLS": return 1;
      case "IN_UND_AUSWENDIG": return 1; // prefer over DE_CASE
      case "HIER_MIT": return 1; // prefer over agreement rules
      case "HIER_FUER": return 1; // prefer over agreement rules
      case "MIT_REISSEN": return 1; // prefer over agreement rules
      case "JEDEN_FALLS": return 1;
      case "MOEGLICHER_WEISE_ETC": return 1; // prefer over agreement rules
      case "UST_ID": return 1;
      case "INS_FITNESS": return 1; // prefer over DE_AGREEMENT
      case "MIT_UNTER": return 1; // prefer over agreement rules
      case "SEIT_VS_SEID": return 1; // prefer over some agreement rules (HABE_BIN from premium)
      case "ZU_KOMMEN_LASSEN": return 1; // prefer over INFINITIVGRP_VERMOD_PKT
      case "ZU_SCHICKEN_LASSEN": return 1; // prefer over INFINITIVGRP_VERMOD_PKT
      case "IM_UM": return 1; // prefer over MIT_MIR and IM_ERSCHEINUNG (premium)
      case "EINEN_VERSUCH_WERT": return 1; // prefer over DE_AGREEMENT
      case "DASS_DAS_PA2_DAS_PROIND": return 1; // prefer over HILFSVERB_HABEN_SEIN, DE_AGREEMENT
      case "AUF_BITTEN": return 1; // prefer over ZUSAMMENGESETZTE_VERBEN
      case "MEINET_WEGEN": return 1; // prefer over AUF_DEM_WEG
      case "FUER_INBESONDERE": return 1; // prefer over KOMMA_VOR_ERLAEUTERUNG
      case "COVID_19": return 1; // prefer over PRAEP_GEN and DE_AGREEMENT
      case "DA_VOR": return 1; // prefer over ZUSAMMENGESETZTE_VERBEN
      case "KLEINSCHREIBUNG_MAL": return 1; // prefer over DE_AGREEMENT
      case "VERINF_DAS_DASS_SUB": return 1; // prefer over DE_AGREEMENT
      case "IM_ALTER": return 1; // prefer over ART_ADJ_SOL
      case "DAS_ALTER": return 1; // prefer over ART_ADJ_SOL
      case "VER_INF_PKT_VER_INF": return 1; // prefer over DE_CASE
      case "DASS_MIT_VERB": return 1; // prefer over SUBJUNKTION_KOMMA ("Dass wird Konsequenzen haben.")
      case "AB_TEST": return 1; // prefer over spell checker and agreement
      case "BZGL_ABK": return 1; // prefer over spell checker
      case "DURCH_WACHSEN": return 1; // prefer over SUBSTANTIVIERUNG_NACH_DURCH
      case "ICH_WARTE": return 1; // prefer over verb agreement rules (e.g. SUBJECT_VERB_AGREEMENT)
      case "RUNDUM_SORGLOS_PAKET": return 1; // higher prio than DE_CASE
      case "MIT_FREUNDLICHEN_GRUESSE": return 1; // higher prio than MEIN_KLEIN_HAUS
      case "OK": return 1; // higher prio than KOMMA_NACH_PARTIKEL_SENT_START[3]
      case "EINE_ORIGINAL_RECHNUNG": return 1; // higher prio than DE_CASE, DE_AGREEMENT and MEIN_KLEIN_HAUS
      case "VALENZ_TEST": return 1; // see if this generates more corpus matches
      case "WAEHRUNGSANGABEN_CHF": return 1; // higher prio than WAEHRUNGSANGABEN_KOMMA
      // default is 0
      case "FALSCHES_ANFUEHRUNGSZEICHEN": return -1; // less prio than most grammar rules but higher prio than UNPAIRED_BRACKETS
      case "VER_KOMMA_PRO_RIN": return -1; // prefer WENN_WEN
      case "DE_PROHIBITED_COMPOUNDS_PREMIUM": return -1; // prefer other rules (e.g. AUS_MITTEL)
      case "VER_INF_VER_INF": return -1; // prefer case rules
      case "DE_COMPOUND_COHERENCY": return -1;  // prefer EMAIL
      case "GEFEATURED": return -1; // prefer over spell checker
      case "NUMBER_SUB": return -1; // prefer over spell checker
      case "VER123_VERAUXMOD": return -1; // prefer casing rules
      case "DE_AGREEMENT": return -1;  // prefer RECHT_MACHEN, MONTAGS, KONJUNKTION_DASS_DAS, DESWEITEREN, DIES_BEZUEGLICH and other
      case "DE_AGREEMENT2": return -1;  // prefer WILLKOMMEN_GROSS and other rules that offer suggestions
      case "CONFUSION_RULE": return -1;  // probably less specific than the rules from grammar.xml
      case "KOMMA_NEBEN_UND_HAUPTSATZ": return -1;  // prefer SAGT_RUFT
      case "FALSCHES_RELATIVPRONOMEN": return -1; // prefer dass/das rules
      case "AKZENT_STATT_APOSTROPH": return -1;  // lower prio than PLURAL_APOSTROPH
      case "BEENDE_IST_SENTEND": return -1; // prefer more specific rules
      case "VER_ADJ_ZU_SCHLAFEN": return -1; // prefer ETWAS_GUTES
      case "MIO_PUNKT": return -1; // higher prio than spell checker
      case "AUSLASSUNGSPUNKTE_LEERZEICHEN": return -1; // higher prio than spell checker
      case "IM_ERSCHEINUNG": return -1; // prefer ZUM_FEM_NOMEN
      case "SPACE_BEFORE_OG": return -1; // higher prio than spell checker
      case "VERSEHENTLICHERWEISE": return -1; // higher prio than spell checker
      case "VERMOD_SKIP_VER_PKT": return -1; // less prio than casing rules
      case "EINZELBUCHSTABE_PREMIUM": return -1;  // lower prio than "A_LA_CARTE"
      case "ART_IND_ADJ_SUB": return -2;  // prefer DE_AGREEMENT rules
      case "KATARI": return -2; // higher prio than spell checker
      case "SCHOENE_WETTER": return -2; // prefer more specific rules that offer a suggestion (e.g. DE_AGREEMENT)
      case "MEIN_KLEIN_HAUS": return -2; // prefer more specific rules that offer a suggestion (e.g. DIES_BEZÜGLICH)
      case "UNPAIRED_BRACKETS": return -2;
      case "ICH_GLAUBE_FUER_EUCH": return -2; // prefer agreement rules
      case "OBJECT_AGREEMENT": return -2; // less prio than DE_AGREEMENT
      case "ICH_INF_PREMIUM": return -2; // prefer more specific rules that offer a suggestion (e.g. SUBJECT_VERB_AGREEMENT)
      case "MEHRERE_WOCHE_PREMIUM": return -2;  // less prio than DE_AGREEMENT
      case "DOPPELTER_NOMINATIV": return -2;  // give precedence to wie-wir-wird confusion rules
      case "KUDAMM": return -2;   // overwrite spell checker
      case "ALTERNATIVEN_FUER_ANGLIZISMEN" : return -2;   // overwrite spell checker
      case "ANGLIZISMUS_INTERNAL" : return -2;   // overwrite spell checker
      case "DOPPELUNG_VER_MOD_AUX": return -2;
      case "AERZTEN_INNEN": return -2;  // overwrite speller ("Ärzte/-innen")
      case "ANGLIZISMEN" : return -2;   // overwrite spell checker
      case "ANGLIZISMUS_PA_MIT_ED" : return -2;   // overwrite spell checker
      case "MEINSTE" : return -2;   // overwrite spell checker
      case "ZAHL_IM_WORT": return -2; //should not override rules like H2O
      case "ICH_LIEBS": return -2;  // higher prio than spell checker
      case "ICH_GEHE_DU_BLEIBST": return -3; // prefer ICH_GLAUBE_FUER_EUCH
      case "GERMAN_SPELLER_RULE": return -3;  // assume most other rules are more specific and helpful than the spelling rule
      case "AUSTRIAN_GERMAN_SPELLER_RULE": return -3;  // assume most other rules are more specific and helpful than the spelling rule
      case "SWISS_GERMAN_SPELLER_RULE": return -3;  // assume most other rules are more specific and helpful than the spelling rule
      case "DE_VERBAGREEMENT": return -4; // prefer more specific rules (e.g DU_WUENSCHT) and speller
      case "PUNKT_ENDE_DIREKTE_REDE": return -4; // prefer speller
      case "LEERZEICHEN_NACH_VOR_ANFUEHRUNGSZEICHEN": return -4; // prefer speller
      case "ZEICHENSETZUNG_DIREKTE_REDE": return -4; // prefer speller
      case "GROSSSCHREIBUNG_WOERTLICHER_REDE": return -4; // prefer speller
      case "IM_IHM": return -4;  // lower prio than spell checker
      case "IN_UNKNOWNKLEIN_VER": return -4;  // lower prio than spell checker
      case "SEHR_GEEHRTER_NAME": return -4;  // lower prio than spell checker
      case "DE_PHRASE_REPETITION": return -4;  // lower prio than spell checker
      case "FRAGEZEICHEN_NACH_DIREKTER_REDE": return -4;  // lower prio than spell checker
      case "PUNCTUATION_PARAGRAPH_END": return -4;  // don't hide spelling mistakes
      case "TEST_F_ANSTATT_PH": return -4;  // don't hide spelling mistakes
      case "DAS_WETTER_IST": return -5; // lower prio than spell checker
      case "VEREIZ_VERINF_PKT": return -5; // lower prio than spell checker
      case "WER_STARK_SCHWITZ": return -5; // lower prio than spell checker
      case "VERBEN_PRAEFIX_AUS": return -5; // lower prio than spell checker
      case "ANFUEHRUNG_VERSCHACHTELT": return -5;  // lower prio than speller and FALSCHES_ANFUEHRUNGSZEICHEN
      case "SATZBAU_AN_DEN_KOMMT": return -5;  // lower prio than rules that give a suggestion
      case "SUBJECT_VERB_AGREEMENT": return -5; // prefer more specific rules that offer a suggestion (e.g. DE_VERBAGREEMENT)
      case "SAGT_SAGT": return -9; // higher prio than KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ_2 and GERMAN_WORD_REPEAT_RULE
      case "PUNKT_ENDE_ABSATZ": return -10;  // should never hide other errors, as chance for a false alarm is quite high
      case "KOMMA_VOR_RELATIVSATZ": return -10;
      case "VON_LEBENSLAEUFE": return -12; // less prio than AI
      case "ZUSAMMENGESETZTE_VERBEN": return -12; // less prio than most more specific rules and AI
      case "PRP_VER_PRGK": return -13; // lower prio than ZUSAMMENGESETZTE_VERBEN
      case "COMMA_IN_FRONT_RELATIVE_CLAUSE": return -13; // prefer other rules (KONJUNKTION_DASS_DAS, ALL_DAS_WAS_KOMMA, AI) but higher prio than style
      case "SAGT_RUFT": return -13; // prefer case rules, DE_VERBAGREEMENT, AI and speller
      case "KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ_2": return -14; // lower prio than SAGT_SAGT, but higher than GERMAN_WORD_REPEAT_RULE
      case "BEI_VERB": return -14; // prefer case, spelling and AI rules
      case "MODALVERB_FLEKT_VERB": return -14; // prefer case, spelling and AI rules
      case "DATIV_NACH_PRP": return -14; // spelling and AI rules
      case "DAT_ODER_AKK_NACH_PRP": return -14; // prefer more specific rules that offer a suggestion (A.I., spelling)
      case "SENT_START_SIN_PLU": return -14; // prefer more specific rules that offer a suggestion (A.I., spelling)
      case "SENT_START_PLU_SIN": return -14; // prefer more specific rules that offer a suggestion (A.I., spelling)
      case "VER_INFNOMEN": return -14;  // prefer spelling and AI rules
      case "GERMAN_WORD_REPEAT_RULE": return -15; // lower prio than SAGT_RUFT and KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ_2
      case "TOO_LONG_PARAGRAPH": return -15;
      case "ALL_UPPERCASE": return -15;
      case "NUR_LEDIGLICH": return -16; // lower prio than GERMAN_WORD_REPEAT_RULE
      case "COMMA_BEHIND_RELATIVE_CLAUSE": return -52; // less prio than AI_DE_HYDRA_LEO
      case "DOPPELUNG_MODALVERB": return -52; // prefer comma rules (DOPPELUNG_MODALVERB, AI)
      case "VER_DOPPELUNG": return -52; // prefer comma rules (including AI)
      case "DEF_ARTIKEL_INDEF_ADJ": return -52; // less prio than DE_AGREMEENT and less prio than most comma rules
      case "PRP_ADJ_AGREEMENT": return -52; // less prio than DE_AGREMEENT and less prio than most comma rules
      case "SIE_WOLLTEN_SIND": return -52;
      case "ART_ADJ_SOL": return -52; // prefer comma rules
      case "WURDEN_WORDEN_1": return -52; // prefer comma rules
      case "WAR_WAHR": return -52; // higher prio than KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ
      case "KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ": return -53;
      case "VERB_IST": return -53; // less prio than comma rules and spell checker
      case "WAR_WERDEN": return -53; // less prio than comma rules
      case "INF_VER_MOD": return -53; // prefer case, spelling and AI rules
      case "VERB_FEM_SUBST": return -54; // prefer comma rules (including AI)
      case "SUBJUNKTION_KOMMA_2": return -54; // lower prio than KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ and KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ_2
      case "DOPPELUNG_GLEICHES_VERB": return -55; // prefer comma rules
      case "FEHLENDES_NOMEN": return -60; // lower prio than most rules
      case "REPETITIONS_STYLE": return -60;
      case "MAN_SIEHT_SEHR_SCHOEN": return -14; // prefer over SEHR_SCHOEN
      // Category ids - make sure style issues don't hide overlapping "real" errors:
      case "TYPOGRAPHY": return -14;
      case "COLLOQUIALISMS": return -15;
      case "STYLE": return -15;
      case "REDUNDANCY": return -15;
      case "GENDER_NEUTRALITY": return -15;
    }
    if (id.startsWith("CONFUSION_RULE_")) {
      return -1;
    }
    if (id.startsWith("AI_DE_HYDRA_LEO")) { // prefer more specific rules (also speller)
      if (id.startsWith("AI_DE_HYDRA_LEO_MISSING_COMMA")) {
        return -51; // prefer comma style rules.
      }
      if (id.startsWith("AI_DE_HYDRA_LEO_CP")) {
        return 2;
      }
      if (id.startsWith("AI_DE_HYDRA_LEO_DATAKK")) {
        return 1;
      }
      return -11;
    }
    if (id.startsWith("AI_DE_KOMMA")) {
      return -52; // prefer comma style rules and AI_DE_HYDRA_LEO_MISSING_COMMA
    }
    return super.getPriorityForId(id);
  }

  public boolean hasMinMatchesRules() {
    return true;
  }


}
