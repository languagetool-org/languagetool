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
import org.languagetool.rules.*;
import org.languagetool.rules.ca.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
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

public class Catalan extends Language {
  
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
            new CatalanWrongWordInContextRule(messages),
            new SimpleReplaceVerbsRule(messages, this),
            new SimpleReplaceBalearicRule(messages),
            new SimpleReplaceRule(messages),
            new SimpleReplaceMultiwordsRule(messages),
            new ReplaceOperationNamesRule(messages, this),
            new SimpleReplaceDiacriticsIEC(messages),
            new SimpleReplaceAnglicism(messages), 
            new PronomFebleDuplicateRule(messages),
            new CheckCaseRule(messages, this),
            new SimpleReplaceAdverbsMent(messages),
            new CatalanWordRepeatBeginningRule(messages, this),
            new CompoundRule(messages, this, userConfig),
            new CatalanRepeatedWordsRule(messages), 
            new SimpleReplaceDNVRule(messages, this),
            new SimpleReplaceDNVColloquialRule(messages, this),
            new SimpleReplaceDNVSecondaryRule(messages, this)
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
    return CatalanSynthesizer.INSTANCE;
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
    return new CatalanWordTokenizer();
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
  
  @Override
  public String toAdvancedTypography (String input) {
    String output = super.toAdvancedTypography(input);
    
    // special cases: apostrophe + quotation marks
    output = output.replaceAll("(\\b[lmnstdLMNSTD])'", "$1’");
    output = output.replaceAll("(\\b[lmnstdLMNSTD])’\"", "$1’" + getOpeningDoubleQuote());
    output = output.replaceAll("(\\b[lmnstdLMNSTD])’'", "$1’" + getOpeningSingleQuote());
    
    return output;
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }
  
  @Override
  public int getRulePriority(Rule rule) {
    int categoryPriority = this.getPriorityForId(rule.getCategory().getId().toString());
    int rulePriority = this.getPriorityForId(rule.getId());
    // if there is a priority defined for the rule,
    // it takes precedence over category priority
    if (rulePriority != 0) {
      return rulePriority;
    }
    if (categoryPriority != 0) {
      return categoryPriority;
    }
    if (rule.getLocQualityIssueType().equals(ITSIssueType.Style)) {
      // don't let style issues hide more important errors
      return -50;
    }
    return 0;
  }

  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      case "CONFUSIONS2": return 80;
      case "DEU_NI_DO": return 80; // greater then rules about pronouns
      case "INCORRECT_EXPRESSIONS": return 50;
      case "PERSONATGES_FAMOSOS": return 50;
      case "CONEIXO_CONEC": return 50;
      case "OFERTAR_OFERIR": return 50; // greater than PRONOMS_FEBLES_SOLTS2
      case "DESDE_UN": return 40;
      case "MOTS_NO_SEPARATS": return 40;
      case "REPETEAD_ELEMENTS": return 40;
      case "ESPERANT_US_AGRADI": return 40;
      case "ESPAIS_SOBRANTS": return 40; // greater than L
      case "ELA_GEMINADA": return 35; // greater than agreement rules, pronoun rules
      case "CA_SPLIT_WORDS": return 30;
      case "PRONOMS_FEBLES_TEMPS_VERBAL": return 35;
      case "ET_AL": return 30; // greater than apostrophes and pronouns
      case "PRONOMS_FEBLES_COLLOQUIALS": return 30; // greater than PRONOMS_FEBLES_SOLTS2
      case "CONCORDANCES_CASOS_PARTICULARS": return 30;
      case "CONFUSIONS_PRONOMS_FEBLES": return 30; // greater than ES (DIACRITICS)
      case "GERUNDI_PERD_T": return 30;
      case "CONFUSIONS": return 30;
      case "PRONOMS_FEBLES_DARRERE_VERB": return 30; // greater than PRONOMS_FEBLES_SOLTS2
      case "HAVER_SENSE_HAC": return 28; // greater than CONFUSIONS_ACCENT avia, lower than CONFUSIONS_E
      case "REEMPRENDRE": return 28; // equal to CA_SIMPLE_REPLACE_VERBS
      case "INCORRECT_WORDS_IN_CONTEXT": return 28; // similar to but lower than CONFUSIONS, greater than ES_KNOWN
      case "PRONOMS_FEBLES_SOLTS2": return 26;  // greater than PRONOMS_FEBLES_SOLTS, ES, HAVER_SENSE_HAC
      case "ES_UNKNOWN": return 25;
      case "PASSAT_PERIFRASTIC": return 25; // greater than CONFUSIONS_ACCENT
      case "CONFUSIONS_ACCENT": return 20;
      case "DIACRITICS": return 20;
      case "CAP_GENS": return 20; //greater than CAP_ELS_CAP_ALS, CONCORDANCES_DET_NOM
      case "MOTS_SENSE_GUIONETS": return 20; // greater than CONCORDANCES_NUMERALS
      case "ORDINALS": return 20; // greater than SEPARAT
      case "SUPER": return 20;
      case "PRONOM_FEBLE_HI": return 20; // greater than HAVER_PARTICIPI_HAVER_IMPERSONAL
      case "HAVER_PARTICIPI_HAVER_IMPERSONAL": return 15; // greater than ACCENTUATION_CHECK
      case "CONCORDANCES_NUMERALS_DUES": return 10; // greater than CONCORDANCES_NUMERALS
      case "FALTA_CONDICIONAL": return 10; // greater than POTSER_SIGUI
      case "ACCENTUATION_CHECK": return 10;
      case "CONCORDANCES_NUMERALS": return 10;
      case "COMMA_IJ": return 10;
      case "AVIS": return 10;
      case "CAP_ELS_CAP_ALS": return 10; // greater than DET_GN
      case "CASING": return 10; // greater than CONCORDANCES_DET_NOM
      case "DOS_ARTICLES": return 10; // greater than apostrophation rules
      case "MOTS_GUIONET": return 10; // greater than CONCORDANCES_DET_NOM
      case "SELS_EN_VA": return 10;
      case "ZERO_O": return 10; //greater than SPELLING
      case "URL": return 10; //greater than SPELLING
      case "CONCORDANCES_DET_NOM": return 5;
      case "PASSAR_SE": return 5; // greater than OBLIDARSE
      case "DET_GN": return 5; // greater than DE_EL_S_APOSTROFEN
      case "SPELLING": return 5;
      case "VENIR_NO_REFLEXIU": return 5;
      case "DEUS_SEUS": return 5;
      case "SON_BONIC": return 5;
      case "CONTRACCIONS": return 0; // lesser than apostrophations
      case "CASING_START": return -5;
      case "ARTICLE_TOPONIM_MIN": return -10; // lesser than CONTRACCIONS, CONCORDANCES_DET_NOM 
      case "PEL_QUE": return -10; // lesser than PEL_QUE_FA
      case "COMMA_LOCUTION": return -10;
      case "REGIONAL_VERBS": return -10;
      case "PRONOMS_FEBLES_SOLTS": return -10; //lesser than SPELLING
      case "AGREEMENT_POSTPONED_ADJ": return -15;
      case "FALTA_COMA_FRASE_CONDICIONAL": return -20;
      case "ESPAIS_QUE_FALTEN_PUNTUACIO": return -20;
      case "VERBS_NOMSPROPIS": return -20;
      case "VERBS_PRONOMINALS": return -25;
      case "PORTA_UNA_HORA": return -40;
      case "REPETITIONS_STYLE": return -50;
      case "MUNDAR": return -50;
      case "NOMBRES_ROMANS": return -90;
      case "MORFOLOGIK_RULE_CA_ES": return -100;
      case "EXIGEIX_ACCENTUACIO_VALENCIANA": return -120;
      //case "APOSTROFACIO_MOT_DESCONEGUT": return -120; // lesser than MORFOLOGIK_RULE_CA_ES
      case "PHRASE_REPETITION": return -150;
      case "SUBSTANTIUS_JUNTS": return -150;
      case "FALTA_ELEMENT_ENTRE_VERBS": return -200;
      case "PUNT_FINAL": return -200;
      case "UPPERCASE_SENTENCE_START": return -500;
      case "MAJUSCULA_IMPROBABLE": return -500;
      case "ELA_GEMINADA_WIKI": return -200;
    }
    if (id.startsWith("CA_SIMPLE_REPLACE_MULTIWORDS")) {
      return 70;
    }
    if (id.startsWith("CA_SIMPLE_REPLACE_BALEARIC")) {
      return 60;
    }
    if (id.startsWith("CA_SIMPLE_REPLACE_VERBS")) {
      return 28;
    }if (id.startsWith("CA_SIMPLE_REPLACE_ANGLICISM")) {
      return 10;
    }
    if (id.startsWith("CA_COMPOUNDS")) {
      return 50;
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
  
  private static final Pattern CA_OLD_DIACRITICS = Pattern.compile(".*\\b(sóc|dóna|dónes|vénen|véns|fóra)\\b.*",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  
  @Override
  public List<RuleMatch> adaptSuggestions(List<RuleMatch> ruleMatches, Set<String> enabledRules) {
    List<RuleMatch> newRuleMatches = new ArrayList<>();
    for (RuleMatch rm : ruleMatches) {
      String errorStr = rm.getOriginalErrorStr();
      List<SuggestedReplacement> suggestedReplacements = rm.getSuggestedReplacementObjects();
      List<SuggestedReplacement> newReplacements = new ArrayList<>();
      for (SuggestedReplacement suggestedReplacement : suggestedReplacements) {
        String newReplStr = suggestedReplacement.getReplacement();
        if (errorStr.length() > 2 && errorStr.endsWith("'") && !newReplStr.endsWith("'") && !newReplStr.endsWith("’")) {
          newReplStr = newReplStr + " ";
        }
        if (enabledRules.contains("APOSTROF_TIPOGRAFIC") && newReplStr.length() > 1) {
          newReplStr = newReplStr.replace("'", "’");
        }
        if (enabledRules.contains("EXIGEIX_POSSESSIUS_U") && newReplStr.length() > 3) {
          Matcher m = POSSESSIUS_v.matcher(newReplStr);
          newReplStr = m.replaceAll("$1u$2");
          Matcher m2 = POSSESSIUS_V.matcher(newReplStr);
          newReplStr = m2.replaceAll("$1U$2");
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
      RuleMatch newMatch = new RuleMatch(rm, newReplacements);
      newRuleMatches.add(newMatch);
    }
    return newRuleMatches;
  }
  
  private String removeOldDiacritics(String s) {
    return s.replace("dóna", "dona")
        .replace("dónes", "dones")
        .replace("sóc", "soc")
        .replace("vénen", "venen")
        .replace("véns", "véns")
        .replace("fóra", "fora")
        .replace("Dóna", "Dona")
        .replace("Dónes", "Dones")
        .replace("Sóc", "Soc")
        .replace("Vénen", "Venen")
        .replace("Véns", "Vens")
        .replace("Fóra", "Fora");
  }
  
  private static final Pattern CA_CONTRACTIONS = Pattern.compile("\\b([Aa]|[Dd]e) e(ls?)\\b");
  private static final Pattern CA_APOSTROPHES1 = Pattern.compile("\\b([LDNSTMldnstm]['’]) ");
  // exceptions: l'FBI, l'statu quo
  private static final Pattern CA_APOSTROPHES2 = Pattern.compile("\\b([mtlsn])['’]([^1haeiouáàèéíòóúA-ZÀÈÉÍÒÓÚ“«\"])");
  // exceptions: el iogurt, la essa
  private static final Pattern CA_APOSTROPHES3 = Pattern.compile("\\be?([mtsldn])e? (h?[aeiouàèéíòóú])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern CA_APOSTROPHES4 = Pattern.compile("\\b(l)a ([aeoàúèéí][^ ])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern CA_APOSTROPHES5 = Pattern.compile("\\b([mts]e) (['’])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern CA_APOSTROPHES6 = Pattern.compile("\\bs'e(ns|ls)\\b",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern POSSESSIUS_v = Pattern.compile("\\b([mtsMTS]e)v(a|es)\\b",
      Pattern.UNICODE_CASE);
  private static final Pattern POSSESSIUS_V = Pattern.compile("\\b([MTS]E)V(A|ES)\\b",
      Pattern.UNICODE_CASE);

  @Override
  public String adaptSuggestion(String s) {
    // Exceptions: Digues-me alguna cosa, urbi et orbi, Guns N' Roses
    boolean capitalized = StringTools.isCapitalizedWord(s);
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
    if (capitalized) {
      s = StringTools.uppercaseFirstChar(s);
    }
    s = s.replace(" ,", ",");
    return s;
  }
  
  
}
