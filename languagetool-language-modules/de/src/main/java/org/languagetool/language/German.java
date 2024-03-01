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
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;
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
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Support for German - use the sub classes {@link GermanyGerman}, {@link SwissGerman}, or {@link AustrianGerman}
 * if you need spell checking.
 */
public class German extends Language implements AutoCloseable {

  private static final Pattern TYPOGRAPHY_PATTERN = compile("\\b([a-zA-Z]\\.)([a-zA-Z]\\.)");
  private static final Pattern AI_DE_GGEC_MISSING_PUNCT =
    compile("AI_DE_GGEC_MISSING_PUNCTUATION_\\d+_DASH_J(_|AE)HRIG|AI_DE_GGEC_REPLACEMENT_CONFUSION", Pattern.CASE_INSENSITIVE);

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
    return new GermanRuleDisambiguator(getDefaultLanguageVariant());
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
            new GermanUnpairedQuotesRule(messages, this),
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
            new OldSpellingRule(messages, this),
            new SentenceWhitespaceRule(messages),
            new GermanDoublePunctuationRule(messages),
            new MissingVerbRule(messages, this),
            new GermanWordRepeatRule(messages, this),
            new GermanWordRepeatBeginningRule(messages, this),
            new GermanWrongWordInContextRule(messages, this),
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
            new GermanRepeatedWordsRule(messages),
            new StyleTooOftenUsedVerbRule(messages, this, userConfig),
            new StyleTooOftenUsedNounRule(messages, this, userConfig),
            new StyleTooOftenUsedAdjectiveRule(messages, this, userConfig)
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
      new ProhibitedCompoundRule(messages, languageModel, userConfig, this)
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
    output = TYPOGRAPHY_PATTERN.matcher(output).replaceAll("$1\u00a0$2");
    output = TYPOGRAPHY_PATTERN.matcher(output).replaceAll("$1\u00a0$2");
    return output;
  }
  
  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  private final static Map<String, Integer> id2prio = new HashMap<>();
  static {
    id2prio.put("DE_PROHIBITED_PHRASE", 11);
    id2prio.put("WRONG_SPELLING_PREMIUM_INTERNAL", 10);
    id2prio.put("OLD_SPELLING_RULE", 10);
    id2prio.put("DE_COMPOUNDS", 10);
    id2prio.put("E_MAIL_SIGNATUR", 10);
    id2prio.put("TELEFON_NR", 10);
    id2prio.put("IRGEND_COMPOUND", 10);
    id2prio.put("DA_DURCH", 2); // prefer over SUBSTANTIVIERUNG_NACH_DURCH and DURCH_SCHAUEN and DURCH_WACHSEN
    id2prio.put("BEI_GOOGLE", 2);   // prefer over agreement rules and VOR_BEI
    id2prio.put("EINE_ORIGINAL_RECHNUNG_TEST", 2);   // prefer over agreement rules
    id2prio.put("VON_SEITEN_RECOMMENDATION", 2);   // prefer over AI_DE_GGEC_UNNECESSARY_ORTHOGRAPHY_SPACE
    id2prio.put("AUFFORDERUNG_SIE", 2);   // prefer over AI_DE_GGEC_REPLACEMENT_ORTHOGRAPHY_LOWERCASE
    id2prio.put("WEIS_ICH", 2);   // prefer over AI_DE_GGEC_*
    id2prio.put("VONSTATTEN_GEHEN", 2);   // prefer over EINE_ORIGINAL_RECHNUNG
    id2prio.put("VERWECHSLUNG_MIR_DIR_MIR_DIE", 1); // prefer over MIR_DIR
    id2prio.put("ERNEUERBARE_ENERGIEN", 1); // prefer over VEREINBAREN
    id2prio.put("DRIVE_IN", 1); // prefer over agreement rules
    id2prio.put("DIES_MAL", 1); // prefer over GROSSSCHREIBUNG_MAL
    id2prio.put("AN_STATT", 1); // prefer over agreement rules
    id2prio.put("VOR_BEI", 1); // prefer over BEI_BEHALTEN
    id2prio.put("SUB_VER_KLEIN", 1); // prefer over casing rules
    id2prio.put("ALLES_GUTE", 1); // prefer over premium rules
    id2prio.put("NEUN_NEUEN", 1); // prefer over VIELZAHL_PLUS_SINGULAR
    id2prio.put("VERWANDET_VERWANDTE", 1); // prefer over DE_CASE
    id2prio.put("IN_DEUTSCHE_SPRACHE", 1); // prefer over most other rules
    id2prio.put("SCHMIERE_STEHEN", 1); // prefer over most other rules
    id2prio.put("UEBER_EIN_MANGEL", 1); // prefer over PRAEP_AKK
    id2prio.put("SEIT_LAENGEREN", 1); // prefer over DE_CASE
    id2prio.put("WIR_GEFUEHL", 1); // prefer over DE_CASE
    id2prio.put("VORHER_NACHHER_BILD", 1); // prefer over DE_CASE
    id2prio.put("SEIT_KLEIN_AUF", 1); // prefer over agreement rules
    id2prio.put("SEIT_GEBURT_AN", 1); // prefer over agreement rules
    id2prio.put("WO_VON", 1); // prefer over most agreement rules
    id2prio.put("ICH_BIN_STAND_JETZT_KOMMA", 1); // prefer over most agreement rules
    id2prio.put("EIN_LOGGEN", 1); // prefer over most agreement rules
    id2prio.put("ZU_GENÜGE", 1);   // prefer over ZU_KOENNE
    id2prio.put("SEIT_BEKANNT_WERDEN", 1);   // prefer over agreement and comma rules
    id2prio.put("IMPFLICHT", 1);   // prefer over agreement rules DE_AGREEMENT
    id2prio.put("NULL_KOMMA_NICHTS", 1);   // prefer over agreement rules
    id2prio.put("ZWEI_AN_HALB", 1);   // prefer over agreement rules
    id2prio.put("BLUETOOTH_LAUTSPRECHER", 1);   // prefer over agreement rules
    //id2prio.put("KOENNT_ICH", 1);   // prefer over DE_VERBAGREEMENT
    id2prio.put("WIR_HABE", 1);   // prefer over DE_VERBAGREEMENT
    id2prio.put("DAS_IST_GLAUBE_ICH_EGAL", 1);   // prefer over agreement rules
    id2prio.put("ICH_KOENNT", 1);   // prefer over DE_VERBAGREEMENT
    id2prio.put("HAT_DU", 1);   // prefer over agreement rules
    id2prio.put("HAST_DICH", 1);   // prefer over agreement rules
    id2prio.put("GRUNDE", 1);   // prefer over agreement rules
    id2prio.put("EIN_FACH", 1);   // prefer over agreement rules
    id2prio.put("WOGEN_SUBST", 1);   // prefer over agreement rules
    id2prio.put("SO_WIES_IST", 1);   // prefer over agreement rules
    id2prio.put("SICH_SICHT", 1);   // prefer over agreement rules
    id2prio.put("MIT_VERANTWORTLICH", 1);   // prefer over agreement rules
    id2prio.put("VOR_LACHEN", 1);   // prefer over ZUSAMMENGESETZTE_VERBEN
    id2prio.put("TOUREN_SUBST", 1);   // prefer over ZUSAMMENGESETZTE_VERBEN
    id2prio.put("AUF_DRÄNGEN", 1);   // prefer over ZUSAMMENGESETZTE_VERBEN
    id2prio.put("AUF_ZACK", 1);   // prefer over ZUSAMMENGESETZTE_VERBEN
    id2prio.put("UNTER_DRUCK", 1);   // prefer over ZUSAMMENGESETZTE_VERBEN
    id2prio.put("ZUCCHINIS", 1);   // overwrite spell checker
    id2prio.put("PASSWORTE", 1);   // overwrite agreement rules
    id2prio.put("ANGL_PA_ED_UNANGEMESSEN", 1);   // overwrite spell checker
    id2prio.put("ANFUEHRUNGSZEICHEN_DE_AT", 1); // higher prio than UNPAIRED_BRACKETS
    id2prio.put("ANFUEHRUNGSZEICHEN_CH_FR", 1); // higher prio than UNPAIRED_BRACKETS
    id2prio.put("EMAIL", 1);  // better suggestion than SIMPLE_AGREEMENT_*
    id2prio.put("IM_STICH_LASSEN", 1);  // higher prio than agreement rules
    id2prio.put("ZULANGE", 1);  // better suggestion than SAGT_RUFT
    id2prio.put("ROCK_N_ROLL", 1);  // better error than DE_CASE
    id2prio.put("JOE_BIDEN", 1);  // better error than DE_CASE
    //id2prio.put("RESOURCE_RESSOURCE", 1);  // better error than DE_CASE
    id2prio.put("ANS_OHNE_APOSTROPH", 1);
    id2prio.put("DIESEN_JAHRES", 1);
    id2prio.put("TAG_EIN_TAG_AUS", 1); // prefer over agreement rules
    id2prio.put("WERT_SEIN", 1); // prefer over DE_AGREEMENT
    id2prio.put("EBEN_FALLS", 1);
    //id2prio.put("DA_DRAUS", 1);
    id2prio.put("AUSSER_ORDENTLICH", 1);
    id2prio.put("IN_UND_AUSWENDIG", 1); // prefer over DE_CASE
    id2prio.put("HIER_MIT", 1); // prefer over agreement rules
    id2prio.put("HIER_FUER", 1); // prefer over agreement rules
    id2prio.put("MIT_REISSEN", 1); // prefer over agreement rules
    id2prio.put("JEDEN_FALLS", 1);
    id2prio.put("MOEGLICHER_WEISE_ETC", 1); // prefer over agreement rules
    id2prio.put("UST_ID", 1);
    id2prio.put("INS_FITNESS", 1); // prefer over DE_AGREEMENT
    id2prio.put("MIT_UNTER", 1); // prefer over agreement rules
    id2prio.put("SEIT_VS_SEID", 1); // prefer over some agreement rules (HABE_BIN from premium)
    id2prio.put("ZU_KOMMEN_LASSEN", 1); // prefer over INFINITIVGRP_VERMOD_PKT
    id2prio.put("ZU_SCHICKEN_LASSEN", 1); // prefer over INFINITIVGRP_VERMOD_PKT
    id2prio.put("IM_UM", 1); // prefer over MIT_MIR and IM_ERSCHEINUNG (premium)
    id2prio.put("EINEN_VERSUCH_WERT", 1); // prefer over DE_AGREEMENT
    id2prio.put("DASS_DAS_PA2_DAS_PROIND", 1); // prefer over HILFSVERB_HABEN_SEIN, DE_AGREEMENT
    id2prio.put("AUF_BITTEN", 1); // prefer over ZUSAMMENGESETZTE_VERBEN
    id2prio.put("MEINET_WEGEN", 1); // prefer over AUF_DEM_WEG
    id2prio.put("FUER_INSBESONDERE", 1); // prefer over KOMMA_VOR_ERLAEUTERUNG
    id2prio.put("COVID_19", 1); // prefer over PRAEP_GEN and DE_AGREEMENT
    id2prio.put("DA_VOR", 1); // prefer over ZUSAMMENGESETZTE_VERBEN
    id2prio.put("DAS_WUENSCHE_ICH", 1); // prefer over DE_AGREEMENT
    id2prio.put("KLEINSCHREIBUNG_MAL", 1); // prefer over DE_AGREEMENT
    id2prio.put("VERINF_DAS_DASS_SUB", 1); // prefer over DE_AGREEMENT
    id2prio.put("IM_ALTER", 1); // prefer over ART_ADJ_SOL
    id2prio.put("DAS_ALTER", 1); // prefer over ART_ADJ_SOL
    id2prio.put("VER_INF_PKT_VER_INF", 1); // prefer over DE_CASE
    id2prio.put("DASS_MIT_VERB", 1); // prefer over SUBJUNKTION_KOMMA ("Dass wird Konsequenzen haben.")
    id2prio.put("AB_TEST", 1); // prefer over spell checker and agreement
    id2prio.put("BZGL_ABK", 1); // prefer over spell checker
    id2prio.put("DURCH_WACHSEN", 1); // prefer over SUBSTANTIVIERUNG_NACH_DURCH
    //id2prio.put("ICH_WARTE", 1); // prefer over verb agreement rules (e.g. SUBJECT_VERB_AGREEMENT)
    id2prio.put("RUNDUM_SORGLOS_PAKET", 1); // higher prio than DE_CASE
    id2prio.put("MIT_FREUNDLICHEN_GRUESSE", 1); // higher prio than MEIN_KLEIN_HAUS
    id2prio.put("OK", 1); // higher prio than KOMMA_NACH_PARTIKEL_SENT_START[3]
    id2prio.put("EINE_ORIGINAL_RECHNUNG", 1); // higher prio than DE_CASE, DE_AGREEMENT and MEIN_KLEIN_HAUS
    //id2prio.put("VALENZ_TEST", 1); // see if this generates more corpus matches
    id2prio.put("WAEHRUNGSANGABEN_CHF", 1); // higher prio than WAEHRUNGSANGABEN_KOMMA
    // default is 0
    id2prio.put("FALSCHES_ANFUEHRUNGSZEICHEN", -1); // less prio than most grammar rules but higher prio than UNPAIRED_BRACKETS
    id2prio.put("VER_KOMMA_PRO_RIN", -1); // prefer WENN_WEN
    id2prio.put("VER_INF_VER_INF", -1); // prefer id2prio.put(rules
    id2prio.put("DE_COMPOUND_COHERENCY", -1);  // prefer EMAIL
    id2prio.put("GEFEATURED", -1); // prefer over spell checker
    id2prio.put("NUMBER_SUB", -1); // prefer over spell checker
    id2prio.put("MFG", -1); // prefer over spell checker
    id2prio.put("VER123_VERAUXMOD", -1); // prefer casing rules
    id2prio.put("DE_AGREEMENT", -1);  // prefer RECHT_MACHEN, MONTAGS, KONJUNKTION_DASS_DAS, DESWEITEREN, DIES_BEZUEGLICH and other
    id2prio.put("DE_AGREEMENT2", -1);  // prefer WILLKOMMEN_GROSS and other rules that offer suggestions
    id2prio.put("KOMMA_NEBEN_UND_HAUPTSATZ", -1);  // prefer SAGT_RUFT
    id2prio.put("FALSCHES_RELATIVPRONOMEN", -1); // prefer dass/das rules
    id2prio.put("AKZENT_STATT_APOSTROPH", -1);  // lower prio than PLURAL_APOSTROPH
    id2prio.put("BEENDE_IST_SENTEND", -1); // prefer more specific rules
    id2prio.put("VER_ADJ_ZU_SCHLAFEN", -1); // prefer ETWAS_GUTES
    id2prio.put("MIO_PUNKT", -1); // higher prio than spell checker
    id2prio.put("AUSLASSUNGSPUNKTE_LEERZEICHEN", -1); // higher prio than spell checker
    id2prio.put("IM_ERSCHEINUNG_SPELLING_RULE", -1); // prefer ZUM_FEM_NOMEN
    id2prio.put("SPACE_BEFORE_OG", -1); // higher prio than spell checker
    id2prio.put("VERSEHENTLICHERWEISE", -1); // higher prio than spell checker
    id2prio.put("VERMOD_SKIP_VER_PKT", -1); // less prio than casing rules
    id2prio.put("N_NETTER_TYP", -1); // higher prio than EINZELBUCHSTABE_PREMIUM and speller
    id2prio.put("EINZELBUCHSTABE_PREMIUM", -2);  // lower prio than "A_LA_CARTE"
    id2prio.put("ART_IND_ADJ_SUB", -2);  // prefer DE_AGREEMENT rules
    id2prio.put("KATARI", -2); // higher prio than spell checker
    id2prio.put("SCHOENE_WETTER", -2); // prefer more specific rules that offer a suggestion (e.g. DE_AGREEMENT)
    id2prio.put("MEIN_KLEIN_HAUS", -2); // prefer more specific rules that offer a suggestion (e.g. DIES_BEZÜGLICH)
    id2prio.put("UNPAIRED_BRACKETS", -2);
    id2prio.put("DE_UNPAIRED_QUOTES", -2); // less prio than FALSCHES_ANFUEHRUNGSZEICHEN
    id2prio.put("ICH_GLAUBE_FUER_EUCH", -2); // prefer agreement rules
    id2prio.put("OBJECT_AGREEMENT", -2); // less prio than DE_AGREEMENT
    id2prio.put("ICH_INF_PREMIUM", -2); // prefer more specific rules that offer a suggestion (e.g. SUBJECT_VERB_AGREEMENT)
    id2prio.put("MEHRERE_WOCHE_PREMIUM", -2);  // less prio than DE_AGREEMENT
    id2prio.put("DOPPELTER_NOMINATIV", -2);  // give precedence to wie-wir-wird confusion rules
    id2prio.put("KUDAMM", -2);   // overwrite spell checker
    id2prio.put("ALTERNATIVEN_FUER_ANGLIZISMEN", -2);   // overwrite spell checker
    //id2prio.put("ANGLIZISMUS_INTERNAL", -2);   // overwrite spell checker
    id2prio.put("DOPPELUNG_VER_MOD_AUX", -2);
    //id2prio.put("AERZTEN_INNEN", -2);  // overwrite speller ("Ärzte/-innen")
    id2prio.put("ANGLIZISMEN", -2);   // overwrite spell checker
    id2prio.put("ANGLIZISMUS_PA_MIT_ED", -2);   // overwrite spell checker
    id2prio.put("MEINSTE", -2);   // overwrite spell checker
    //id2prio.put("ZAHL_IM_WORT", -2); //should not override rules like H2O
    id2prio.put("ICH_LIEBS", -2);  // higher prio than spell checker
    id2prio.put("WENNS_UND_ABERS", -2);  // higher prio than spell checker
    id2prio.put("ABERS_SATZANFANG_SPELLING_RULE", -2);  // higher prio than spell checker
    id2prio.put("VERNEB", -2);  // higher prio than spell checker
    id2prio.put("ZAHL_IM_WORT_SPELLING_RULE", -2); // higher prio than spell checker
    id2prio.put("GERMAN_SPELLER_RULE", -3);  // assume most other rules are more specific and helpful than the spelling rule
    id2prio.put("AUSTRIAN_GERMAN_SPELLER_RULE", -3);  // assume most other rules are more specific and helpful than the spelling rule
    id2prio.put("SWISS_GERMAN_SPELLER_RULE", -3);  // assume most other rules are more specific and helpful than the spelling rule
    id2prio.put("DE_VERBAGREEMENT", -4); // prefer more specific rules (e.g DU_WUENSCHT) and speller
    id2prio.put("PUNKT_ENDE_DIREKTE_REDE", -4); // prefer speller
    id2prio.put("LEERZEICHEN_NACH_VOR_ANFUEHRUNGSZEICHEN", -4); // prefer speller
    id2prio.put("ZEICHENSETZUNG_DIREKTE_REDE", -4); // prefer speller
    id2prio.put("GROSSSCHREIBUNG_WOERTLICHER_REDE", -4); // prefer speller
    id2prio.put("IM_IHM_SPELLING_RULE", -4);  // lower prio than spell checker
    id2prio.put("IN_UNKNOWNKLEIN_VER", -4);  // lower prio than spell checker
    id2prio.put("SEHR_GEEHRTER_NAME", -4);  // lower prio than spell checker
    id2prio.put("DE_PHRASE_REPETITION", -4);  // lower prio than spell checker
    id2prio.put("FRAGEZEICHEN_NACH_DIREKTER_REDE", -4);  // lower prio than spell checker
    id2prio.put("PUNCTUATION_PARAGRAPH_END", -4);  // don't hide spelling mistakes
    id2prio.put("F_ANSTATT_PH_2", -4);  // don't hide spelling mistakes
    id2prio.put("DAS_WETTER_IST", -5); // lower prio than spell checker
    id2prio.put("VEREIZ_VERINF_PKT", -5); // lower prio than spell checker
    id2prio.put("WER_STARK_SCHWITZ", -5); // lower prio than spell checker
    id2prio.put("VERBEN_PRAEFIX_AUS", -5); // lower prio than spell checker
    id2prio.put("ANFUEHRUNG_VERSCHACHTELT", -5);  // lower prio than speller and FALSCHES_ANFUEHRUNGSZEICHEN
    id2prio.put("SATZBAU_AN_DEN_KOMMT", -5);  // lower prio than rules that give a suggestion
    id2prio.put("SUBJECT_VERB_AGREEMENT", -5); // prefer more specific rules that offer a suggestion (e.g. DE_VERBAGREEMENT)
    id2prio.put("SAGT_SAGT", -9); // higher prio than KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ_2 and GERMAN_WORD_REPEAT_RULE
    //id2prio.put("PUNKT_ENDE_ABSATZ", -10);  // should never hide other errors, as chance for a false alarm is quite high
    //id2prio.put("KOMMA_VOR_RELATIVSATZ", -10);
    id2prio.put("VON_LEBENSLAEUFE_SPELLING_RULE", -12); // less prio than AI
    id2prio.put("VER_WER_VER_3", -12); // less prio than AI
    id2prio.put("PA_WAS", -12); // less prio than AI
    id2prio.put("ICH_GEHE_DU_BLEIBST", -12); // prefer ICH_GLAUBE_FUER_EUCH and less prio than AI
    id2prio.put("PROPERNOMSIN_VERIMPSIN", -12); // less prio than AI
    id2prio.put("VER123_VERAUXMOD_TEST1", -12); // less prio than AI to produce a single suggestion
    id2prio.put("ZUSAMMENGESETZTE_VERBEN", -12); // less prio than most more specific rules and AI
    id2prio.put("PRP_VER_PRGK", -13); // lower prio than ZUSAMMENGESETZTE_VERBEN
    id2prio.put("COMMA_IN_FRONT_RELATIVE_CLAUSE", -13); // prefer other rules (KONJUNKTION_DASS_DAS, ALL_DAS_WAS_KOMMA, AI) but higher prio than style
    id2prio.put("SAGT_RUFT", -13); // prefer id2prio.put(rules, DE_VERBAGREEMENT, AI and speller
    id2prio.put("KANNST_WERDEN", -13); // prefer more specific rules that offer a suggestion (A.I., spelling)
    id2prio.put("KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ_2", -14); // lower prio than SAGT_SAGT, but higher than GERMAN_WORD_REPEAT_RULE
    id2prio.put("MAN_SIEHT_SEHR_SCHOEN", -14); // prefer over SEHR_SCHOEN
    id2prio.put("BEI_VERB", -14); // prefer case, spelling and AI rules
    id2prio.put("MODALVERB_FLEKT_VERB", -14); // prefer case, spelling and AI rules
    id2prio.put("DATIV_NACH_PRP", -14); // spelling and AI rules
    id2prio.put("DAT_ODER_AKK_NACH_PRP", -14); // prefer more specific rules that offer a suggestion (A.I., spelling)
    id2prio.put("SENT_START_SIN_PLU", -14); // prefer more specific rules that offer a suggestion (A.I., spelling)
    id2prio.put("SENT_START_PLU_SIN", -14); // prefer more specific rules that offer a suggestion (A.I., spelling)
    id2prio.put("VER_INFNOMEN", -14);  // prefer spelling and AI rules
    id2prio.put("GERMAN_WORD_REPEAT_RULE", -15); // lower prio than SAGT_RUFT and KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ_2
    id2prio.put("TOO_LONG_PARAGRAPH", -15);
    id2prio.put("ALL_UPPERCASE", -15);
    id2prio.put("NUR_LEDIGLICH", -16); // lower prio than GERMAN_WORD_REPEAT_RULE
    id2prio.put("COMMA_BEHIND_RELATIVE_CLAUSE", -52); // less prio than AI_DE_HYDRA_LEO
    id2prio.put("DOPPELUNG_MODALVERB", -52); // prefer comma rules (DOPPELUNG_MODALVERB, AI)
    id2prio.put("VER_DOPPELUNG", -52); // prefer comma rules (including AI)
    id2prio.put("DEF_ARTIKEL_INDEF_ADJ", -52); // less prio than DE_AGREMEENT and less prio than most comma rules
    id2prio.put("PRP_ADJ_AGREEMENT", -52); // less prio than DE_AGREMEENT and less prio than most comma rules
    id2prio.put("SIE_WOLLTEN_SIND", -52);
    id2prio.put("ART_ADJ_SOL", -52); // prefer comma rules
    id2prio.put("WURDEN_WORDEN_1", -52); // prefer comma rules
    id2prio.put("WAR_WAHR", -52); // higher prio than KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ
    id2prio.put("KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ", -53);
    id2prio.put("VERB_IST", -53); // less prio than comma rules and spell checker
    id2prio.put("WAR_WERDEN", -53); // less prio than comma rules
    id2prio.put("INF_VER_MOD_SPELLING_RULE", -53); // prefer case, spelling and AI rules
    id2prio.put("DOPPELTES_VERB", -53); // prefer comma rules (including AI)
    id2prio.put("VERB_FEM_SUBST", -54); // prefer comma rules (including AI)
    id2prio.put("SUBJUNKTION_KOMMA_2", -54); // lower prio than KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ and KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ_2
    id2prio.put("DOPPELUNG_GLEICHES_VERB", -55); // prefer comma rules
    id2prio.put("FEHLENDES_NOMEN", -60); // lower prio than most rules
    id2prio.put("REPETITIONS_STYLE", -60);
    id2prio.put("GERMAN_WORD_REPEAT_BEGINNING_RULE", -61); 
    // Category ids - make sure style issues don't hide overlapping "real" errors:
    id2prio.put("TYPOGRAPHY", -14);
    id2prio.put("COLLOQUIALISMS", -15);
    id2prio.put("STYLE", -15);
    id2prio.put("REDUNDANCY", -15);
    id2prio.put("GENDER_NEUTRALITY", -15);
  }

  @Override
  public Map<String, Integer> getPriorityMap() {
    return id2prio;
  }
  
  @Override
  protected int getPriorityForId(String id) {
    Integer prio = id2prio.get(id);
    if (prio != null) {
      return prio;
    }
    if (id.startsWith("DE_PROHIBITED_COMPOUNDS_") || id.startsWith("DE_PROHIBITED_COMPOUNDS_PREMIUM_")) {   // don't hide spelling mistakes
      return -4;
    }
    if (id.startsWith("DE_MULTITOKEN_SPELLING")) {
      return -2;
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
    if (id.startsWith("AI_DE_GGEC")) {
      // gGEC IDs that should have less prio than rules with default prio
      // e. g. ABKUERZUNG_FEHLENDE_PUNKTE
      switch (id) {
        case "AI_DE_GGEC_MISSING_PUNCTUATION_E_DASH_MAIL":  // less prio than EMAIL
          return 0;
        case "AI_DE_GGEC_REPLACEMENT_ADJECTIVE":
        case "AI_DE_GGEC_REPLACEMENT_ADVERB":
        case "AI_DE_GGEC_REPLACEMENT_NOUN":
        case "AI_DE_GGEC_REPLACEMENT_ORTHOGRAPHY_LOWERCASE":
        case "AI_DE_GGEC_REPLACEMENT_ORTHOGRAPHY_SPELL":
        case "AI_DE_GGEC_REPLACEMENT_OTHER":
        case "AI_DE_GGEC_REPLACEMENT_VERB":
        case "AI_DE_GGEC_REPLACEMENT_VERB_FORM":
        case "AI_DE_GGEC_UNNECESSARY_ORTHOGRAPHY_SPACE":
        case "AI_DE_GGEC_UNNECESSARY_OTHER":
        case "AI_DE_GGEC_UNNECESSARY_SPACE":
          return -1;
      }
      if (id.startsWith("AI_DE_GGEC_MISSING_PUNCTUATION_PERIOD")) {  // less prio than spell checker
        return -4;
      }
      if (id.startsWith("AI_DE_GGEC_UNNECESSARY_PUNCTUATION")) {  // less prio than FALSCHES_ANFUEHRUNGSZEICHEN
        return -2;
      }
      if (AI_DE_GGEC_MISSING_PUNCT.matcher(id).find()) {
        return -1;
      }
      return 1;
    }
    return super.getPriorityForId(id);
  }

  public boolean hasMinMatchesRules() {
    return true;
  }

  @Override
  public List<String> prepareLineForSpeller(String line) {
    List<String> results = new ArrayList<>();
    String[] parts = line.split("#");
    if (parts.length == 0) {
      return Arrays.asList(line);
    }
    String[] formTag = parts[0].split("[/]");
    if (formTag.length == 0) {
      return Arrays.asList("");
    }
    String form = formTag[0];
    results.add(form);
    String tag = "";
    if (formTag.length==2) {
      tag = formTag[1];
    }
    if (tag.contains("E")) {
      results.add(form + "e");
    }
    if (tag.contains("S")) {
      results.add(form + "s");
    }
    if (tag.contains("N")) {
      results.add(form + "n");
    }
    return results;
  }

  public MultitokenSpeller getMultitokenSpeller() {
    return GermanMultitokenSpeller.INSTANCE;
  }
}
