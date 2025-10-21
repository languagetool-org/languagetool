/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.*;
import org.languagetool.rules.ca.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.ca.CatalanHybridDisambiguator;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.ca.CatalanWordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class Catalan extends Language {

  private static final String LANGUAGE_SHORT_CODE = "ca-ES";

  private static volatile Throwable instantiationTrace;

  public Catalan() {
    Throwable trace = instantiationTrace;
    if (trace != null) {
      throw new RuntimeException("Language was already instantiated, see the cause stacktrace below.", trace);
    }
    instantiationTrace = new Throwable();
  }

  /**
   * This is a fake constructor overload for the subclasses. Public constructors can only be used by the LT itself.
   */
  protected Catalan(boolean fakeValue) {
  }

  public static @NotNull Catalan getInstance() {
    Language language = Objects.requireNonNull(Languages.getLanguageForShortCode(LANGUAGE_SHORT_CODE));
    if (language instanceof Catalan catalan) {
      return catalan;
    }
    throw new RuntimeException("Catalan language expected, got " + language);
  }

  @Override
  public String getName() {
    return "Catalan";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"ES"}; // "AD", "FR", "IT"
  }
  
  @Override
  public String getShortCode() {
    return "ca";
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return Languages.getLanguageForShortCode("ca-ES");
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Ricard Roca"), new Contributor("Jaume Ortolà") };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages, 
            		Example.wrong("A parer seu<marker> ,</marker> no era veritat."),
            		Example.fixed("A parer seu<marker>,</marker> no era veritat.")),
            new DoublePunctuationRule(messages),
            new CatalanUnpairedBracketsRule(messages, this),
            new UppercaseSentenceStartRule(messages, this,
            		Example.wrong("Preus de venda al públic. <marker>han</marker> pujat molt."),
            		Example.fixed("Preus de venda al públic. <marker>Han</marker> pujat molt.")),
            new MultipleWhitespaceRule(messages, this),
            new LongSentenceRule(messages, userConfig, 60),
            // specific to Catalan:
            new CatalanWordRepeatRule(messages, this),
            new MorfologikCatalanSpellerRule(messages, this, userConfig, altLanguages),
            new CatalanUnpairedQuestionMarksRule(messages, this),
            new CatalanUnpairedExclamationMarksRule(messages, this),
            new CatalanWrongWordInContextRule(messages, this),
            new SimpleReplaceVerbsRule(messages, this),
            new SimpleReplaceBalearicRule(messages, this),
            new SimpleReplaceRule(messages, this),
            new SimpleReplaceMultiwordsRule(messages),
            new ReplaceOperationNamesRule(messages, this),
            new SimpleReplaceDiacriticsIEC(messages, this),
            new SimpleReplaceAnglicism(messages), 
            new PronomFebleDuplicateRule(messages),
            new CheckCaseRule(messages, this),
            new SimpleReplaceAdverbsMent(messages),
            new CatalanWordRepeatBeginningRule(messages, this),
            new CompoundRule(messages, this, userConfig),
            //new CatalanRepeatedWordsRule(messages, this),
            new SimpleReplaceDNVRule(messages, this),
            new SimpleReplaceDNVColloquialRule(messages, this),
            new SimpleReplaceDNVSecondaryRule(messages, this),
            new WordCoherencyRule(messages),
            new PunctuationMarkAtParagraphEnd(messages, this)
    );
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return CatalanTagger.INSTANCE_CAT;
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return CatalanSynthesizer.INSTANCE_CAT;
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new CatalanHybridDisambiguator(getDefaultLanguageVariant());
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return CatalanWordTokenizer.INSTANCE;
  }
  
  /** @since 5.1 */
  @Override
  public String getOpeningDoubleQuote() {
    return "«";
  }

  /** @since 5.1 */
  @Override
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

  private static final Pattern PATTERN_1 = compile("(\\b[lmnstdLMNSTD])'");
  private static final Pattern PATTERN_2 = compile("(\\b[lmnstdLMNSTD])’\"");
  private static final Pattern PATTERN_3 = compile("(\\b[lmnstdLMNSTD])’'");

  @Override
  public String toAdvancedTypography (String input) {
    String output = super.toAdvancedTypography(input);
    
    // special cases: apostrophe + quotation marks
    output = PATTERN_1.matcher(output).replaceAll("$1’");
    output = PATTERN_2.matcher(output).replaceAll("$1’" + getOpeningDoubleQuote());
    output = PATTERN_3.matcher(output).replaceAll("$1’" + getOpeningSingleQuote());
    
    return output;
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }
  
  @Override
  protected int getDefaultRulePriorityForStyle() {
    return -50;
  }

  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      case "CONFUSIONS2": return 80;
      case "DEU_NI_DO": return 80; // greater than rules about pronouns
      case "FER_LOGIN": return 70; // greater than anglicisms
      case "L_OK": return 70; // greater than anglicisms
      case "INCORRECT_EXPRESSIONS": return 50;
      case "PERSONATGES_FAMOSOS": return 50;
      case "CONEIXO_CONEC": return 50;
      case "COMETES_INCORRECTES": return 50; // greater than PRONOMS_FEBLES
      case "OFERTAR_OFERIR": return 50; // greater than PRONOMS_FEBLES_SOLTS2
      case "PREGUEM_DISCULPIN": return 45; // greater than ESPERANT_US_AGRADI
      case "DESDE_UN": return 40;
      case "CONEIXET": return 40;
      case "CONEIXENTS": return 40;
      case "MOTS_NO_SEPARATS": return 40;
      case "REPETEAD_ELEMENTS": return 40;
      case "ESPERANT_US_AGRADI": return 40;
      case "LO_NEUTRE": return 40; // lower than other INCORRECT_EXPRESSIONS
      case "ESPAIS_SOBRANTS": return 40; // greater than L
      case "PER_A_QUE_PERQUE": return 40;
      case "PRONOMS_FEBLES_COMBINACIONS_SE": return 40;
      case "ELA_GEMINADA": return 35; // greater than agreement rules, pronoun rules
      case "TENIR_QUE": return 35; // greater than CA_SIMPLE_REPLACE
      case "CONFUSIONS_PRONOMS_FEBLES": return 35; // greater than ES (DIACRITICS), PRONOMS_FEBLES_DARRERE_VERB
      case "COMMA_PERO1": return 35; // greater than CA_SIMPLE_REPLACE
      case "CA_SPLIT_WORDS": return 30;
      case "PRONOMS_FEBLES_TEMPS_VERBAL": return 35;
      case "ET_AL": return 30; // greater than apostrophes and pronouns
      case "PRONOMS_FEBLES_COLLOQUIALS": return 30; // greater than PRONOMS_FEBLES_SOLTS2
      case "CONCORDANCES_CASOS_PARTICULARS": return 30;
      case "GERUNDI_PERD_T": return 30;
      case "CONFUSIONS": return 30;
      case "PRONOMS_FEBLES_DARRERE_VERB": return 30; // greater than PRONOMS_FEBLES_SOLTS2
      case "VERBS_NO_INCOATIUS": return 30; // greater than PRONOMS_FEBLES_SOLTS2
      case "ARRIBAN_ARRIBANT": return 30;
      case "PERO_PERO": return 30; // lower than COMMA_PERO1
      case "PUNT_LLETRA": return 30; // greater than CONCORDANCES_DET_NOM
      case "REEMPRENDRE": return 28; // equal to CA_SIMPLE_REPLACE_VERBS
      case "INCORRECT_WORDS_IN_CONTEXT": return 28; // similar to but lower than CONFUSIONS, greater than ES_KNOWN
      case "PRONOMS_FEBLES_SOLTS2": return 26;  // greater than PRONOMS_FEBLES_SOLTS, ES, HAVER_SENSE_HAC
      case "ES_UNKNOWN": return 25;
      case "HAVER_SENSE_HAC": return 25; // greater than CONFUSIONS_ACCENT avia, lower than CONFUSIONS_E
      case "HA_A": return 25; //  lower than CA_SIMPLE_REPLACE_VERBS
      case "PASSAT_PERIFRASTIC": return 25; // greater than CONFUSIONS_ACCENT
      case "PREPOSITIONS": return 25;
      case "CONFUSIONS_ACCENT": return 20;
      case "CONFUSIO_PASSAT_INFINITIU": return 20; // greater than ACCENTUATION_CHECK
      case "DIACRITICS": return 20;
      case "COMMA_ENTRE_DALTRES": return 20; //greater than CONCORDANCES_DET_NOM
      case "CAP_GENS": return 20; //greater than CAP_ELS_CAP_ALS, CONCORDANCES_DET_NOM
      case "MOTS_SENSE_GUIONETS": return 20; // greater than CONCORDANCES_NUMERALS
      case "ORDINALS": return 20; // greater than SEPARAT
      case "SUPER": return 20;
      case "PRONOM_FEBLE_HI": return 20; // greater than HAVER_PARTICIPI_HAVER_IMPERSONAL
      case "HAVER_PARTICIPI_HAVER_IMPERSONAL": return 15; // greater than ACCENTUATION_CHECK
      case "CONCORDANCES_NUMERALS_DUES": return 10; // greater than CONCORDANCES_NUMERALS
      case "POSTULARSE": return 10;
      case "FALTA_CONDICIONAL": return 10; // greater than POTSER_SIGUI
      case "ACCENTUATION_CHECK": return 10;
      case "CONCORDANCA_GRIS": return 10;
      case "SELS_EN_VA_DE_LES_MANS": return 10;
      case "A_PER": return 10;
      case "CONCORDANCES_NUMERALS": return 10;
      case "COMMA_IJ": return 10;
      case "AVIS": return 10;
      case "CAP_ELS_CAP_ALS": return 10; // greater than DET_GN
      case "CASING": return 10; // greater than CONCORDANCES_DET_NOM
      case "DOS_ARTICLES": return 10; // greater than apostrophation rules
      case "MOTS_GUIONET": return 10; // greater than CONCORDANCES_DET_NOM
      case "SELS_EN_VA": return 10;
      case "RECENT": return 10;
      case "CONCORDANCES_NOUNS_PRIORITY": return 10;
      case "PREFIXOS_SENSE_GUIONET_EN_DICCIONARI": return 10; // greater than SPELLING
      case "ZERO_O": return 10; //greater than SPELLING
      case "URL": return 10; //greater than SPELLING
      case "CONCORDANCES_DET_NOM": return 5; // greater than DE_EL_S_APOSTROFEN
      case "CONCORDANCES_DET_ADJ": return 5; // greater than DE_EL_S_APOSTROFEN
      case "CONCORDANCES_DET_POSSESSIU": return 5; // greater than CONCORDANCES_ADJECTIUS_NEUTRES
      case "PASSAR_SE": return 5; // greater than OBLIDARSE
      case "DET_GN": return 5; // greater than DE_EL_S_APOSTROFEN
      case "SPELLING": return 5;
      case "APOSTROF_ANYS": return 5; // greater than typography options
      case "VENIR_NO_REFLEXIU": return 5;
      case "DEUS_SEUS": return 5;
      case "SON_BONIC": return 5;
      case "ACCENTUACIO": return 5;
      case "FIDEUA": return 5; // la cremà
      case "L_NO_APOSTROFA": return 5;
      case "L_D_N_NO_S_APOSTROFEN": return 5;
      case "AMB_EM": return 5;
      case "CONTRACCIONS": return 0; // lesser than apostrophations
      case "CASING_START": return -5;
      case "CA_WORD_COHERENCY": return -10; // lesser than EVITA_DEMOSTRATIUS_ESTE
      case "CA_WORD_COHERENCY_VALENCIA": return -10; // lesser than EVITA_DEMOSTRATIUS_ESTE
    // TA_DEMOSTRATIUS_ESTE
      case "QUAN_PREPOSICIO": return -10; // lesser than QUANT_MES_MES
      case "ARTICLE_TOPONIM_MIN": return -10; // lesser than CONTRACCIONS, CONCORDANCES_DET_NOM 
      case "PEL_QUE": return -10; // lesser than PEL_QUE_FA
      case "COMMA_LOCUTION": return -10;
      case "REGIONAL_VERBS": return -10;
      case "UN_ALTRE_DISTRIBUTIVES": return -10; // no suggestions
      case "PRONOMS_FEBLES_SOLTS": return -10; //lesser than SPELLING
      case "CONCORDANCA_PRONOMS_CATCHALL": return -10;
      case "AGREEMENT_POSTPONED_ADJ": return -15;
      case "FALTA_COMA_FRASE_CONDICIONAL": return -20;
      case "ESPAIS_QUE_FALTEN_PUNTUACIO": return -20;
      case "VERBS_NOMSPROPIS": return -20;
      case "VERBS_PRONOMINALS": return -25;
      case "PORTO_LLEGINT": return -30;
      case "PORTA_UNA_HORA": return -40;
      case "REPETITIONS_STYLE": return -50;
      case "MUNDAR": return -50;
      case "NOMBRES_ROMANS": return -90;
      case "TASCAS_TASQUES": return -97;
      case "PREPOSICIONS_MINUSCULA": return -97; // less than CA_MULTITOKEN_SPELLING
      case "SUGGERIMENTS_LE": return -97; // less than CA_MULTITOKEN_SPELLING
      case "MORFOLOGIK_RULE_CA_ES": return -100;
      case "EXIGEIX_ACCENTUACIO_VALENCIANA": return -120;
      //case "APOSTROFACIO_MOT_DESCONEGUT": return -120; // lesser than MORFOLOGIK_RULE_CA_ES
      case "PHRASE_REPETITION": return -150;
      case "SUBSTANTIUS_JUNTS": return -150;
      case "REPETITION_ADJ_N_ADJ": return -155;
      case "FALTA_ELEMENT_ENTRE_VERBS": return -200;
      case "PUNT_FINAL": return -200;
      case "PUNCTUATION_PARAGRAPH_END": return -200;
      case "CA_END_PARAGRAPH_PUNCTUATION": return -250;
      case "DICENDI_QUE": return -250;
      case "UPPERCASE_SENTENCE_START": return -500;
      case "MAJUSCULA_IMPROBABLE": return -500;
      case "ELA_GEMINADA_WIKI": return -500;
    }
    if (id.startsWith("CA_MULTITOKEN_SPELLING")) {
      return -95;
    }
    if (id.startsWith("CA_SIMPLE_REPLACE_MULTIWORDS")) {
      return 70;
    }
    if (id.startsWith("CA_SIMPLE_REPLACE_ANGLICISM")) {
      return 65; // greater than CA_SIMPLE_REPLACE_BALEARIC
    }
    if (id.startsWith("CA_SIMPLE_REPLACE_BALEARIC")) {
      return 60;
    }
    if (id.startsWith("CA_SIMPLE_REPLACE_VERBS")) {
      return 28;
    }
    if (id.startsWith("CA_COMPOUNDS")) {
      return 50;
    }
    if (id.startsWith("CA_SIMPLE_REPLACE_DIACRITICS_IEC")) {
      return 0;
    }
    if (id.startsWith("CA_SIMPLE_REPLACE")) {
      return 30;
    }
    return super.getPriorityForId(id);
  }
  
  public boolean hasMinMatchesRules() {
    return true;
  }
  
  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
      return new MorfologikCatalanSpellerRule(messages, this, null, Collections.emptyList());
  }
  
  private static final Pattern CA_OLD_DIACRITICS = compile(".*\\b(sóc|dóna|dónes|vénen|véns|fóra|adéu|féu|desféu|vés|contrapèl)\\b.*",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  private RuleMatch adjustCatalanMatch(RuleMatch ruleMatch, Set<String> enabledRules) {
    String errorStr = ruleMatch.getOriginalErrorStr();
    List<String> suggestedReplacements = ruleMatch.getSuggestedReplacements();
    List<SuggestedReplacement> newReplacements = new ArrayList<>();
    for (String suggestedReplacement : suggestedReplacements) {
      String newReplStr = suggestedReplacement;
      if (errorStr.length() > 2 && errorStr.endsWith("'") && !newReplStr.endsWith("'") && !newReplStr.endsWith("’")) {
        newReplStr = newReplStr + " ";
      }
      if (!newReplStr.equalsIgnoreCase("després") && enabledRules.contains("EXIGEIX_ACCENTUACIO_GENERAL")) {
        if (newReplStr.contains("é") && suggestedReplacements.contains(newReplStr.replace("é", "è"))) {
          continue;
        }
        if (newReplStr.contains("É") && suggestedReplacements.contains(newReplStr.replace("É", "È"))) {
          continue;
        }
      } else if (enabledRules.contains("EXIGEIX_ACCENTUACIO_VALENCIANA")) {
        if (newReplStr.contains("è") && suggestedReplacements.contains(newReplStr.replace("è", "é"))) {
          continue;
        }
        if (newReplStr.contains("È") && suggestedReplacements.contains(newReplStr.replace("È", "É"))) {
          continue;
        }
      }
      if (enabledRules.contains("APOSTROF_TIPOGRAFIC") && newReplStr.length() > 1) {
        newReplStr = newReplStr.replace("'", "’");
      }
      if (enabledRules.contains("EXIGEIX_POSSESSIUS_U") && newReplStr.length() > 3) {
        Matcher m = POSSESSIUS_v.matcher(newReplStr);
        newReplStr = m.replaceAll("$1u$2");
        Matcher m2 = POSSESSIUS_V.matcher(newReplStr);
        newReplStr = m2.replaceAll("$1U$2");
        newReplStr = newReplStr.replace("feina", "faena");
        newReplStr = newReplStr.replace("feiner", "faener");
        newReplStr = newReplStr.replace("feinera", "faenera");
      }
      // s = adaptContractionsApostrophes(s);
      Matcher m5 = CA_OLD_DIACRITICS.matcher(newReplStr);
      if (!enabledRules.contains("DIACRITICS_TRADITIONAL_RULES") && m5.matches()) {
        SuggestedReplacement newSuggestedReplacement = new SuggestedReplacement(suggestedReplacement);
        newSuggestedReplacement.setReplacement(removeOldDiacritics(newReplStr));
        if (!newReplacements.contains(newSuggestedReplacement)) {
          newReplacements.add(newSuggestedReplacement);
        }
      } else {
        SuggestedReplacement newSuggestedReplacement = new SuggestedReplacement(suggestedReplacement);
        newSuggestedReplacement.setReplacement(newReplStr);
        if (!newReplacements.contains(newSuggestedReplacement)) {
          newReplacements.add(newSuggestedReplacement);
        }
      }
    }
    RuleMatch newRuleMatch = new RuleMatch(ruleMatch, newReplacements);
    return newRuleMatch;
  }
  
  private String removeOldDiacritics(String s) {
    return s
        .replace("contrapèl", "contrapel")
        .replace("Contrapèl", "Contrapel")
        .replace("vés", "ves")
        .replace("féu", "feu")
        .replace("desféu", "desfeu")
        .replace("adéu", "adeu")
        .replace("dóna", "dona")
        .replace("dónes", "dones")
        .replace("sóc", "soc")
        .replace("vénen", "venen")
        .replace("véns", "véns")
        .replace("fóra", "fora")
        .replace("Vés", "Ves")
        .replace("Féu", "Feu")
        .replace("Desféu", "Desfeu")
        .replace("Adéu", "Adeu")
        .replace("Dóna", "Dona")
        .replace("Dónes", "Dones")
        .replace("Sóc", "Soc")
        .replace("Vénen", "Venen")
        .replace("Véns", "Vens")
        .replace("Fóra", "Fora");
  }
  
  private static final Pattern CA_CONTRACTIONS = compile("\\b([Aa]|[DdPp]e)r? e(ls?)\\b");
  private static final Pattern CA_APOSTROPHES1 = compile("\\b([LDNSTMldnstm]['’]) ");
  // exceptions: l'FBI, l'statu quo
  private static final Pattern CA_APOSTROPHES2 = compile("\\b([mtlsn])['’]([^1haeiouáàèéíòóúA-ZÀÈÉÍÒÓÚ“«\"])");
  // exceptions: el iogurt, la essa
  private static final Pattern CA_APOSTROPHES3 = compile("\\be?([mtsldn])e? (h?[aeiouàèéíòóú])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern CA_APOSTROPHES4 = compile("\\b(l)a ([aeoàúèéí][^ ])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern CA_APOSTROPHES5 = compile("\\b([mts]e) (['’])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern CA_APOSTROPHES6 = compile("\\bs'e(ns|ls)\\b",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern CA_APOSTROPHES7 = compile("\\b(de|a)l (h?[aeoàúèéí][^ ])",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern CA_APOSTROPHES8 = compile("\\b([MTLSN])['’]([^1haeiouáàèéíòóúA-ZÀÈÉÍÒÓÚ“«\"])");
  private static final Pattern POSSESSIUS_v = compile("\\b([mtsMTS]e)v(a|es)\\b",
      Pattern.UNICODE_CASE);
  private static final Pattern POSSESSIUS_V = compile("\\b([MTS]E)V(A|ES)\\b",
      Pattern.UNICODE_CASE);
  private static final Pattern CA_REMOVE_SPACES = compile("\\b(a|de|pe) (ls?)(?!['’])\\b",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  @Override
  public String adaptSuggestion(String s, String originalErrorStr) {
    // Exceptions: Digues-me alguna cosa, urbi et orbi, Guns N' Roses
    boolean capitalized = StringTools.isCapitalizedWord(s);
    s = s.replace("gens traça", "gens de traça");
    s = s.replace("gens facilitat", "gens de facilitat");
    Matcher m = CA_CONTRACTIONS.matcher(s);
    s = m.replaceAll("$1$2");
    Matcher m1 = CA_APOSTROPHES1.matcher(s);
    s = m1.replaceAll("$1");
    Matcher m2 = CA_APOSTROPHES2.matcher(s);
    s = m2.replaceAll("e$1 $2");
    Matcher m3 = CA_APOSTROPHES3.matcher(s);
    s = m3.replaceAll("$1'$2");
    Matcher m4 = CA_APOSTROPHES4.matcher(s);
    s = m4.replaceAll("$1'$2");
    Matcher m5 = CA_APOSTROPHES5.matcher(s);
    s = m5.replaceAll("$1$2");
    Matcher m6 = CA_APOSTROPHES6.matcher(s);
    s = m6.replaceAll("se'$1");
    Matcher m7 = CA_APOSTROPHES7.matcher(s);
    s = m7.replaceAll("$1 l'$2");
    // T'comença -> Et comença
    Matcher m8 = CA_APOSTROPHES8.matcher(s);
    StringBuffer sb = new StringBuffer();
    while (m8.find()) {
      String group1 = m8.group(1).toLowerCase();
      String group2 = m8.group(2);
      m8.appendReplacement(sb, "E" + group1 + " " + group2);
    }
    m8.appendTail(sb);
    s = sb.toString();
    Matcher m9 = CA_REMOVE_SPACES.matcher(s);
    s = m9.replaceAll("$1$2");
    if (capitalized) {
      s = StringTools.uppercaseFirstChar(s);
    }
    s = s.replace(" ,", ",");
    return StringTools.preserveCase(s, originalErrorStr);
  }
  
  private final List<String> spellerExceptions = Arrays.asList("San Juan", "Copa América", "Colección Jumex", "Banco Santander",
    "San Marcos", "Santa Ana", "San Joaquín", "Naguib Mahfouz", "Rosalía", "Aristide Maillol", "Alexia Putellas",
    "Mónica Randall", "Vicente Blasco Ibáñez", "Copa Sudamericana", "Série A", "Banco Sabadell");

  @Override
  public List<String> prepareLineForSpeller(String line) {
    String[] parts = line.split("#");
    if (parts.length == 0) {
      return Arrays.asList(line);
    }
    String[] formTag = parts[0].split("[\t;]");
    String form = formTag[0].trim();
    if (spellerExceptions.contains(form)) {
      return Arrays.asList("");
    }
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
    return CatalanMultitokenSpeller.INSTANCE;
  }

  @Override
  public List<RuleMatch> filterRuleMatches(List<RuleMatch> ruleMatches, AnnotatedText text, Set<String> enabledRules) {
    List<RuleMatch> results = new ArrayList<>();
    for (int i=0; i<ruleMatches.size(); i++) {
      RuleMatch ruleMatch = ruleMatches.get(i);
      if (ruleMatch.getRule().getFullId().equals("FALTA_ELEMENT_ENTRE_VERBS[3]") ||
        ruleMatch.getRule().getFullId().equals("FALTA_ELEMENT_ENTRE_VERBS[4]")) {
        if (i+1 < ruleMatches.size()) {
          if (ruleMatches.get(i+1).getFromPosSentence()>-1
            && !ruleMatches.get(i+1).getRule().getFullId().equals("FALTA_ELEMENT_ENTRE_VERBS[5]")
            && ruleMatches.get(i+1).getFromPosSentence() - ruleMatch.getToPosSentence()<20) {
            continue;
          }
        }
      }
      if (i>0 && ruleMatch.getRule().getFullId().equals("FALTA_ELEMENT_ENTRE_VERBS[5]") &&
        ruleMatches.get(i-1).getRule().getId().equals("FALTA_ELEMENT_ENTRE_VERBS")) {
      continue;
      }
      results.add(adjustCatalanMatch(ruleMatch, enabledRules));
    }
    return results;
  }

}
