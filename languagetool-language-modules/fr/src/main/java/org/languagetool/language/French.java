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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class French extends Language implements AutoCloseable {

  private LanguageModel languageModel;
  
  private static final Pattern APOSTROPHE = Pattern.compile("(\\p{L})'([\\p{L}\u202f\u00a0 !\\?,\\.;:])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

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
            new LongSentenceRule(messages, userConfig, 35, true, true),
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
  public String getOpeningQuote() {
    return "« ";
  }

  /** @since 5.1 */
  @Override
  public String getClosingQuote() {
    return " »";
  }
  
  @Override
  public String toAdvancedTypography (String input) {
    String output = input;
    
    // Apostrophe and closing single quote
    Matcher matcher = APOSTROPHE.matcher(output);
    output = matcher.replaceAll("$1’$2");
    
    // single quotes
    if (output.startsWith("'")) { 
      output = output.replaceFirst("'", "‘");
    }
    output = output.replaceAll(" '", " ‘");
    if (output.endsWith("'")) { 
      output = output.substring(0, output.length() - 1 ) + "’";
    }

    // guillemets
    if (output.startsWith("\"")) { 
      output = output.replaceFirst("\"", "«");
    }
    if (output.endsWith("\"")) { 
      output = output.substring(0, output.length() - 1 ) + "»";
    }
    output = output.replaceAll(" \"", " «");
    output = output.replaceAll("\"([\\u202f\\u00a0 !\\?,\\.;:])", "»$1");
       
    
    // non-breaking (thin) space 
    // according to https://fr.wikipedia.org/wiki/Espace_ins%C3%A9cable#En_France
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
      case "DU_DU": return 10; // greater than DU_LE
      case "ACCORD_CHAQUE": return 10; // greater than ACCORD_NOMBRE
      case "CEST_A_DIRE": return 10; // greater than A_A_ACCENT
      case "ESPACE_UNITES": return 1; // needs to have higher priority than spell checker
      case "BYTES": return 1; // needs to be higher than spell checker for 10MB style matches
      case "Y_A": return 1; // needs to be higher than spell checker for style suggestion
      case "A_A_ACCENT": return 1; // triggers false alarms for IL_FAUT_INF if there is no a/à correction
      case "FRENCH_WHITESPACE_STRICT": return 1;  // default off, but if on, it should overwrite FRENCH_WHITESPACE 
      case "FRENCH_WHITESPACE": return 0;
      case "JE_SUI": return 1;  // needs higher priority than spell checker
      case "TOO_LONG_PARAGRAPH": return -15;
      case "FR_SPELLING_RULE": return -100;
      case "ELISION": return -200; // should be lower in priority than spell checker
      case "NONVERB_PRON": return -200; // show the suggestion by the spell checker if exists
      case "UPPERCASE_SENTENCE_START": return -300;
    }
    if (id.startsWith("grammalecte_")) {
      return -150;
    }
    return super.getPriorityForId(id);
  }

}
