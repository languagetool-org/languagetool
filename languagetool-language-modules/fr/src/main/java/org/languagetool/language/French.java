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
    return new FrenchTagger();
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
            new LongSentenceRule(messages, userConfig, 40, true, true),
            new LongParagraphRule(messages, this, userConfig),
            // specific to French:
            new CompoundRule(messages),
            new QuestionWhitespaceStrictRule(messages, this),
            new QuestionWhitespaceRule(messages, this),
            new SimpleReplaceRule(messages),
            new AnglicismReplaceRule(messages)
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
      case "SA_CA": return 100; // greater than D_N
      case "MA": return 100; // greater than D_J
      case "A_INFINITIF": return 100;
      case "ON_ONT": return 100; // greater than PRONSUJ_NONVERBE
      case "LEURS_LEUR": return 100; // greater than N_V
      case "DU_DU": return 100; // greater than DU_LE
      case "ACCORD_CHAQUE": return 100; // greater than ACCORD_NOMBRE
      case "CEST_A_DIRE": return 100; // greater than A_A_ACCENT
      case "A_VERBE_INFINITIF": return 20; // greater than PRONSUJ_NONVERBE
      case "ESPACE_UNITES": return 10; // needs to have higher priority than spell checker
      case "BYTES": return 10; // needs to be higher than spell checker for 10MB style matches
      case "Y_A": return 10; // needs to be higher than spell checker for style suggestion
      case "A_A_ACCENT": return 10; // triggers false alarms for IL_FAUT_INF if there is no a/à correction 
      case "A_ACCENT_A": return 10; // greater than PRONSUJ_NONVERBE
      case "JE_M_APPEL": return 10;  // override NON_V
      case "ACCORD_R_PERS_VERBE": return 10;  // match before POSER_UNE_QUESTION
      case "JE_SUI": return 10;  // needs higher priority than spell checker
      //case "D_N": return 1; // needs to have higher priority than agreement postponed adj | Commented out because many other rules should be higher
      //case "ACCORD_COULEUR": return 1; // needs to have higher priority than agreement postponed adj
      case "CONFUSION_PAR_PART": return -1;  // turn off completely when PART_OU_PAR is activated
      case "FR_SIMPLE_REPLACE": return -10;
      case "IMP_PRON": return -10; // less than D_N
      case "PREP_V": return -20;
      case "TOO_LONG_PARAGRAPH": return -15;
      case "VERB_PRONOUN": return -50; // greater than FR_SPELLING_RULE; less than ACCORD_V_QUESTION
      case "AGREEMENT_POSTPONED_ADJ": return -50;
      case "FR_SPELLING_RULE": return -100;
      case "ELISION": return -200; // should be lower in priority than spell checker
      case "UPPERCASE_SENTENCE_START": return -300;
      case "FRENCH_WHITESPACE_STRICT": return -350; // picky; if on, it should overwrite FRENCH_WHITESPACE
      case "FRENCH_WHITESPACE": return -400; // lesser than UPPERCASE_SENTENCE_START and FR_SPELLING_RULE
    }
    if (id.startsWith("grammalecte_")) {
      return -150;
    }
    return super.getPriorityForId(id);
  }

}
