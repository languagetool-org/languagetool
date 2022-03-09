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
import org.languagetool.rules.fr.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.synthesis.FrenchSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.fr.FrenchHybridDisambiguator;
import org.languagetool.tagging.fr.FrenchTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.fr.FrenchWordTokenizer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class French extends Language implements AutoCloseable {

  private LanguageModel languageModel;

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public String getName() {
    return "French";
  }

  @Override
  public String getShortCode() {
    return "fr";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"FR", "", "BE", "CH", "CA", "LU", "MC", "CM",
            "CI", "HT", "ML", "SN", "CD", "MA", "RE"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return FrenchTagger.INSTANCE;
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return new FrenchSynthesizer(this);
  }
  
  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new FrenchWordTokenizer();
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new FrenchHybridDisambiguator();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        Contributors.DOMINIQUE_PELLE
    };
  }


  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikFrenchSpellerRule(messages, this, null, Collections.emptyList());
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages, false),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{" /*"«", "‘"*/),
                    Arrays.asList("]", ")", "}"
                         /*"»", French dialog can contain multiple sentences. */
                         /*"’" used in "d’arm" and many other words */)),
            new MorfologikFrenchSpellerRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new SentenceWhitespaceRule(messages),
            new LongSentenceRule(messages, userConfig, 40),
            new LongParagraphRule(messages, this, userConfig),
            // specific to French:
            new CompoundRule(messages, this, userConfig),
            new QuestionWhitespaceStrictRule(messages, this),
            new QuestionWhitespaceRule(messages, this),
            new SimpleReplaceRule(messages),
            new AnglicismReplaceRule(messages),
            new FrenchRepeatedWordsRule(messages)
    );
  }

  @Override
  public List<Rule> getRelevantRulesGlobalConfig(ResourceBundle messages, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    if (globalConfig != null && globalConfig.getGrammalecteServer() != null) {
      rules.add(new GrammalecteRule(messages, globalConfig));
    }
    return rules;
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Arrays.asList(
            new FrenchConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  /** @since 3.1 */
  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
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
    String beforeApostrophe = "([cjnmtsldCJNMTSLD]|qu|jusqu|lorsqu|puisqu|quoiqu|Qu|Jusqu|Lorsqu|Puisqu|Quoiqu|QU|JUSQU|LORSQU|PUISQU|QUOIQU)";
    output = output.replaceAll("(\\b"+beforeApostrophe+")'", "$1’");
    output = output.replaceAll("(\\b"+beforeApostrophe+")’\"", "$1’" + getOpeningDoubleQuote());
    output = output.replaceAll("(\\b"+beforeApostrophe+")’'", "$1’" + getOpeningSingleQuote());
    
    // non-breaking (thin) space 
    // according to https://fr.wikipedia.org/wiki/Espace_ins%C3%A9cable#En_France
    output = output.replaceAll("\u00a0;", "\u202f;");
    output = output.replaceAll("\u00a0!", "\u202f!");
    output = output.replaceAll("\u00a0\\?", "\u202f?");
    output = output.replaceAll(";", "\u202f;");
    output = output.replaceAll("!", "\u202f!");
    output = output.replaceAll("\\?", "\u202f?");
    
    output = output.replaceAll(":", "\u00a0:");
    output = output.replaceAll("»", "\u00a0»");
    output = output.replaceAll("«", "«\u00a0");
    
    //remove duplicate spaces
    output = output.replaceAll("\u00a0\u00a0", "\u00a0");
    output = output.replaceAll("\u202f\u202f", "\u202f");
    output = output.replaceAll("  ", " ");
    output = output.replaceAll("\u00a0 ", "\u00a0");
    output = output.replaceAll(" \u00a0", "\u00a0");
    output = output.replaceAll(" \u202f", "\u202f");
    output = output.replaceAll("\u202f ", "\u202f");
    
    return output;
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
      case "FR_COMPOUNDS": return 500; // greater than agreement rules
      case "AGREEMENT_EXCEPTIONS": return 100; // greater than D_N
      case "EXPRESSIONS_VU": return 100; // greater than A_ACCENT_A
      case "SA_CA_SE": return 100; // greater than D_N
      case "SIL_VOUS_PLAIT": return 100; // greater than ACCORD_R_PERS_VERBE
      case "QUASI_NOM": return 100; // greater than D_N
      case "MA": return 100; // greater than D_J
      case "SON_SONT": return 100; // greater than D_J
      case "JE_TES": return 100; // greater than D_J
      case "A_INFINITIF": return 100;
      case "ON_ONT": return 100; // greater than PRONSUJ_NONVERBE
      case "LEURS_LEUR": return 100; // greater than N_V
      case "DU_DU": return 100; // greater than DU_LE
      case "ACCORD_CHAQUE": return 100; // greater than ACCORD_NOMBRE
      case "J_N2": return 100; // greater than J_N
      case "CEST_A_DIRE": return 100; // greater than A_A_ACCENT
      case "FAIRE_VPPA": return 100; // greater than A_ACCENT_A
      case "GENS_ACCORD": return 100; // greater than AGREEMENT_POSTPONED_ADJ
      case "VIRGULE_EXPRESSIONS_FIGEES": return 100; // greater than agreement rules
      case "TRAIT_UNION": return 100; // greater than other rules for trait d'union
      case "PLURIEL_AL2": return 100; // greater than other rules for pluriel al
      case "FR_SPLIT_WORDS_HYPHEN": return 100; // greater than MOTS_INCOMP
      case "PAS_DE_TRAIT_UNION": return 50; //  // greater than agreement rules
      case "MOTS_INCOMP": return 50; // greater than PRONSUJ_NONVERBE and DUPLICATE_DETERMINER
      case "PRIME-TIME": return 50; //  // greater than agreement rules
      case "A_VERBE_INFINITIF": return 20; // greater than PRONSUJ_NONVERBE
      case "DE_OU_DES": return 20; // greater than PAS_ADJ
      case "EMPLOI_EMPLOIE": return 20; // greater than MOTS_INCOMP
      case "VOIR_VOIRE": return 20; // greater than PLACE_DE_LA_VIRGULE
      case "CAT_TYPOGRAPHIE": return 20; // greater than PRONSUJ_NONVERBE or agreement rules
      case "CAT_HOMONYMES_PARONYMES": return 20;
      case "CAT_TOURS_CRITIQUES": return 20;
      case "D_VPPA": return 20; //greater than D_J
      case "EST_CE_QUE": return 20; // greater than TRAIT_UNION_INVERSION
      case "CONFUSION_PARLEZ_PARLER": return 10; // greater than N_V
      case "AGREEMENT_TOUT_LE": return 10; // compare to TOUT_LES
      case "ESPACE_UNITES": return 10; // needs to have higher priority than spell checker
      case "BYTES": return 10; // needs to be higher than spell checker for 10MB style matches
      case "Y_A": return 10; // needs to be higher than spell checker for style suggestion
      case "COTE": return 10; // needs to be higher than D_N
      case "PEUTETRE": return 10; // needs to be higher than AUX_ETRE_VCONJ
      case "A_A_ACCENT": return 10; // triggers false alarms for IL_FAUT_INF if there is no a/à correction 
      case "A_ACCENT_A": return 10; // greater than PRONSUJ_NONVERBE
      case "JE_M_APPEL": return 10;  // override NON_V
      case "ACCORD_R_PERS_VERBE": return 10;  // match before POSER_UNE_QUESTION
      case "JE_SUI": return 10;  // needs higher priority than spell checker
      //case "D_N": return 10; // needs to have higher priority than agreement postponed adj | Commented out because many other rules should be higher: CAT_REGIONALISMES, CAT_TYPOGRAPHIE, CAT_GRAMMAIRE...
      //case "ACCORD_COULEUR": return 1; // needs to have higher priority than agreement postponed adj
      case "R_VAVOIR_VINF": return 10; // needs higher priority than A_INFINITIF
      case "AN_EN": return 10; // needs higher priority than AN_ANNEE
      case "APOS_M": return 10; // needs higher priority than APOS_ESPACE
      case "ACCORD_PLURIEL_ORDINAUX": return 10; // needs higher priority than D_J
      case "ADJ_ADJ_SENT_END": return 10; // needs higher priority than ACCORD_COULEUR
      case "SE_CE": return -10; // needs higher priority than ELISION
      case "SYNONYMS": return -10; // less than ELISION
      case "PAS_DE_SOUCIS": return 10; // needs higher priority than PAS_DE_PB_SOUCIS (premium)
      //case "PRONSUJ_NONVERBE": return 10; // needs higher priority than AUXILIAIRE_MANQUANT
      //case "AUXILIAIRE_MANQUANT": return 5; // needs higher priority than ACCORD_NOM_VERBE
      case "CONFUSION_PAR_PART": return -5;  // turn off completely when PART_OU_PAR is activated
      case "SONT_SON": return -5; // less than ETRE_VPPA_OU_ADJ
      case "FR_SIMPLE_REPLACE": return -10;
      case "J_N": return -10; // needs lesser priority than D_J
      case "TE_NV": return -20; // less than SE_CE, SE_SA and SE_SES
      case "TE_NV2": return -10; // less than SE_CE, SE_SA and SE_SES
      case "PLURIEL_AL": return -10; // less than AGREEMENT_POSTPONED_ADJ
      case "INTERROGATIVE_DIRECTE": return -10; // less than OU
      case "D_J_N": return -10; // less than J_N
      case "FAMILIARITES": return -10; // less than grammar rules
      case "V_J_A_R": return -10; // less than grammar rules
      case "TRES_TRES_ADJ": return -10; // less than grammar rules
      case "IMP_PRON": return -10; // less than D_N
      case "TOO_LONG_PARAGRAPH": return -15;
      case "PREP_VERBECONJUGUE": return -20;
      case "LA_LA2": return -20; // less than LA_LA
      case "FRENCH_WORD_REPEAT_RULE": return -20; // less than TRES_TRES_ADJ
      case "CROIRE": return -20; // less than JE_CROIS_QUE
      case "PAS_DE_VERBE_APRES_POSSESSIF_DEMONSTRATIF": return -20;
      case "VIRGULE_VERBE": return -20; // less than grammar rules
      case "VERBES_FAMILIERS": return -25;  // less than PREP_VERBECONJUGUE + PAS_DE_VERBE_APRES_POSSESSIF_DEMONSTRATIF
      case "VERB_PRONOUN": return -50; // greater than FR_SPELLING_RULE; less than ACCORD_V_QUESTION
      case "IL_VERBE": return -50; // greater than FR_SPELLING_RULE
      case "ILS_VERBE": return -50; // greater than FR_SPELLING_RULE
      case "AGREEMENT_POSTPONED_ADJ": return -50;
      case "MULTI_ADJ": return -50;
      case "ESSENTIEL": return -50; // lesser than grammar rules
      case "CONFUSION_AL_LA": return -50; // lesser than AUX_AVOIR_VCONJ
      case "IMPORTANT": return -50; // lesser than grammar rules
      case "SOUHAITER": return -50; // lesser than grammar rules
      case "CAR": return -50; // lesser than grammar rules
      case "AIMER": return -50; // lesser than grammar rules
      case "CONFUSION_RULE_PREMIUM": return -50; // lesser than PRONSUJ_NONVERBE
      case "FR_SPELLING_RULE": return -100;
      case "ET_SENT_START": return -151; // lower than grammalecte rules
      case "MAIS_SENT_START": return -151; // lower than grammalecte rules
      case "EN_CE_QUI_CONCERNE": return -152;  // less than MAIS_SENT_START + ET_SENT_START
      case "EN_MEME_TEMPS": return -152;  // less than MAIS_SENT_START + ET_SENT_START
      case "ET_AUSSI": return -152;  // less than CONFUSION_EST_ET + ET_SENT_START
      case "MAIS_AUSSI": return -152;  // less than MAIS_SENT_START
      case "ELISION": return -200; // should be lower in priority than spell checker
      case "POINT": return -200; // should be lower in priority than spell checker
      case "REPETITIONS_STYLE": return -250;  // repetition style rules, usually with prefix REP_
      case "FR_REPEATEDWORDS_EXIGER": return -250;  // repetition style rules,
      case "POINTS_SUSPENSIONS_SPACE": return -250;  // should be lower in priority than ADJ_ADJ_SENT_END
      case "UPPERCASE_SENTENCE_START": return -300;
      case "FRENCH_WHITESPACE_STRICT": return -350; // picky; if on, it should overwrite FRENCH_WHITESPACE
      case "TOUT_MAJUSCULES": return -400;
      case "FRENCH_WHITESPACE": return -400; // lesser than UPPERCASE_SENTENCE_START and FR_SPELLING_RULE
      case "MOT_TRAIT_MOT": return -400; // lesser than UPPERCASE_SENTENCE_START and FR_SPELLING_RULE
      case "FRENCH_WORD_REPEAT_BEGINNING_RULE": return -350; // less than REPETITIONS_STYLE
    }

    if (id.startsWith("grammalecte_")) {
      return -150;
    }

    if (id.startsWith("AI_FR_HYDRA_LEO")) { // prefer more specific rules (also speller)
      if (id.startsWith("AI_FR_HYDRA_LEO_MISSING_COMMA")) {
        return -51; // prefer comma style rules.
      }
      return -11;
    }

    return super.getPriorityForId(id);
  }
  
  public boolean hasMinMatchesRules() {
    return true;
  }

  @Override
  public List<RuleMatch> adaptSuggestions(List<RuleMatch> ruleMatches, Set<String> enabledRules) {
    if (enabledRules.contains("APOS_TYP")) {
      List<RuleMatch> newRuleMatches = new ArrayList<>();
      for (RuleMatch rm : ruleMatches) {
        List<String> replacements = rm.getSuggestedReplacements();
        List<String> newReplacements = new ArrayList<>();
        for (String s : replacements) {
          if (s.length() > 1) {
            s = s.replace("'", "’");
          }
          newReplacements.add(s);
        }
        RuleMatch newMatch = new RuleMatch(rm, newReplacements);
        newRuleMatches.add(newMatch);
      }
      return newRuleMatches;
    }
    return ruleMatches;
  }


  @Override
  public List<Rule> getRelevantRemoteRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
    List<Rule> rules = new ArrayList<>(super.getRelevantRemoteRules(
      messageBundle, configs, globalConfig, userConfig, motherTongue, altLanguages, inputLogging));

    // no description needed - matches based on automatically created rules with descriptions provided by remote server
    rules.addAll(GRPCRule.createAll(this, configs, inputLogging,
      "AI_FR_", "INTERNAL - dynamically loaded rule supported by remote server"));
    return rules;
  }
}
