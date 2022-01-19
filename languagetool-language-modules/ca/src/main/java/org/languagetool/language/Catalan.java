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

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Catalan extends Language {

  private static final Language DEFAULT_CATALAN = new Catalan();
  
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
    return DEFAULT_CATALAN;
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
            new LongSentenceRule(messages, userConfig, 50),
            // specific to Catalan:
            new CatalanWordRepeatRule(messages, this),
            new MorfologikCatalanSpellerRule(messages, this, userConfig, altLanguages),
            new CatalanUnpairedQuestionMarksRule(messages, this),
            new CatalanUnpairedExclamationMarksRule(messages, this),
            new CatalanWrongWordInContextRule(messages),
            new SimpleReplaceVerbsRule(messages, this),
            new SimpleReplaceBalearicRule(messages),
            new SimpleReplaceRule(messages),
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
    return new CatalanSynthesizer(this);
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new CatalanHybridDisambiguator();
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
  protected int getPriorityForId(String id) {
    switch (id) {
      case "CA_SIMPLE_REPLACE_BALEARIC": return 100;
      case "CA_COMPOUNDS": return 50;
      case "INCORRECT_EXPRESSIONS": return 50;
      case "PERSONATGES_FAMOSOS": return 50;
      case "CONFUSIONS2": return 50;
      case "MOTS_NO_SEPARATS": return 40;
      case "REPETEAD_ELEMENTS": return 40;
      case "ESPAIS_SOBRANTS": return 40; // greater than L
      case "CONCORDANCES_CASOS_PARTICULARS": return 30;
      case "CONFUSIONS_PRONOMS_FEBLES": return 30; // greater than ES (DIACRITICS)
      case "GERUNDI_PERD_T": return 30;
      case "CONFUSIONS": return 30;
      case "CA_SIMPLE_REPLACE": return 30; // greater than CA_SIMPLE_REPLACE_VERBS
      case "INCORRECT_WORDS_IN_CONTEXT": return 25; // similar to but lower than CONFUSIONS
      case "CONFUSIONS_ACCENT": return 20;
      case "DIACRITICS": return 20;
      case "MOTS_SENSE_GUIONETS": return 20; // greater than CONCORDANCES_NUMERALS
      case "PASSAT_PERIFRASTIC": return 20;
      case "PRONOMS_FEBLES_SOLTS2": return 20;  // greater than PRONOMS_FEBLES_SOLTS
      case "ORDINALS": return 20; // greater than SEPARAT
      case "SUPER": return 20;
      case "FALTA_CONDICIONAL": return 10; // greater than POTSER_SIGUI
      case "ACCENTUATION_CHECK": return 10;
      case "CONCORDANCES_NUMERALS": return 10;
      case "HAVER_SENSE_HAC": return 10;
      case "CAP_ELS_CAP_ALS": return 10; // greater than DET_GN
      case "CASING": return 10; // greater than CONCORDANCES_DET_NOM
      case "MOTS_GUIONET": return 10; // greater than CONCORDANCES_DET_NOM
      case "CONCORDANCES_DET_NOM": return 5;
      case "DET_GN": return 5; // greater than DE_EL_S_APOSTROFEN
      case "VENIR_NO_REFLEXIU": return 5;
      case "ARTICLE_TOPONIM_MIN": return -10; // lesser than CONTRACCIONS, CONCORDANCES_DET_NOM 
      case "PEL_QUE": return -10; // lesser than PEL_QUE_FA
      case "COMMA_LOCUTION": return -10;
      case "REGIONAL_VERBS": return -10;
      case "PRONOMS_FEBLES_SOLTS": return -10; //lesser than SPELLING
      case "AGREEMENT_POSTPONED_ADJ": return -15;
      case "FALTA_COMA_FRASE_CONDICIONAL": return -20;
      case "ESPAIS_QUE_FALTEN_PUNTUACIO": return -20;
      case "REPETITIONS_STYLE": return -50;
      case "MUNDAR": return -50;
      case "NOMBRES_ROMANS": return -90;
      case "MORFOLOGIK_RULE_CA_ES": return -100;
      case "EXIGEIX_ACCENTUACIO_VALENCIANA": return -120;
      case "PHRASE_REPETITION": return -150;
      case "SUBSTANTIUS_JUNTS": return -150;
      case "FALTA_ELEMENT_ENTRE_VERBS": return -200;
      case "UPPERCASE_SENTENCE_START": return -500;
      case "MAJUSCULA_IMPROBABLE": return -500;
      case "ELA_GEMINADA_WIKI": return -200;
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
  
  
  private static final Pattern CA_OLD_DIACRITICS = Pattern.compile(".*\\b(dóna|vénen|véns|fóra)\\b.*",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  
  @Override
  public List<RuleMatch> adaptSuggestions(List<RuleMatch> ruleMatches, Set<String> enabledRules) {
    if (enabledRules.contains("APOSTROF_TIPOGRAFIC") || !enabledRules.contains("DIACRITICS_TRADITIONAL_RULES")) {
      List<RuleMatch> newRuleMatches = new ArrayList<>();
      for (RuleMatch rm : ruleMatches) {
        List<String> replacements = rm.getSuggestedReplacements();
        List<String> newReplacements = new ArrayList<>();
        for (String s : replacements) {
          if (enabledRules.contains("APOSTROF_TIPOGRAFIC") && s.length() > 1) {
            s = s.replace("'", "’");
          }
          Matcher m = CA_OLD_DIACRITICS.matcher(s);
          if (!enabledRules.contains("DIACRITICS_TRADITIONAL_RULES") && m.matches()) {
            // skip this suggestion with traditional diacritics
          } else {
            newReplacements.add(s);
          }
        }
        RuleMatch newMatch = new RuleMatch(rm, newReplacements);
        newRuleMatches.add(newMatch);
      }
      return newRuleMatches;
    }
    return ruleMatches;
  }
}
