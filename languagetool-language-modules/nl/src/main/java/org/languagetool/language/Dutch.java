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
import org.languagetool.rules.nl.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.multitoken.MultitokenSpeller;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.nl.DutchSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.nl.DutchTagger;
import org.languagetool.tagging.nl.DutchHybridDisambiguator;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.nl.DutchWordTokenizer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Dutch extends Language {

  private LanguageModel languageModel;

  @Override
  public Language getDefaultLanguageVariant() {
    return Languages.getLanguageForShortCode("nl");
  }

  @Override
  public String getName() {
    return "Dutch";
  }

  @Override
  public String getShortCode() {
    return "nl";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"NL", "BE"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return DutchTagger.INSTANCE;
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return DutchSynthesizer.INSTANCE;
  }

  public static CompoundAcceptor getCompoundAcceptor() {
    return CompoundAcceptor.INSTANCE;
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new DutchWordTokenizer();
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new DutchHybridDisambiguator(getDefaultLanguageVariant());
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("OpenTaal", "http://www.opentaal.org"),
            new Contributor("TaalTik", "http://www.taaltik.nl")
    };
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{", "“", "‹", "“", "„", "\""),
                    Arrays.asList("]", ")", "}", "”", "›", "”", "”", "\"")),
            new UppercaseSentenceStartRule(messages, this),
            new MorfologikDutchSpellerRule(messages, this, userConfig, altLanguages),
            new MultipleWhitespaceRule(messages, this),
            new CompoundRule(messages, this, userConfig),
            new DutchWrongWordInContextRule(messages, this),
            new WordCoherencyRule(messages),
            new SimpleReplaceRule(messages),
            new LongSentenceRule(messages, userConfig, 40),
            new LongParagraphRule(messages, this, userConfig),
            new PreferredWordRule(messages),
            new SpaceInCompoundRule(messages, this),
            new SentenceWhitespaceRule(messages),
            new CheckCaseRule(messages, this)
    );
  }

  /** @since 4.5 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Arrays.asList(
            new DutchConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  /** @since 4.5 */
  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
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
    id2prio.put(LongSentenceRule.RULE_ID, -1);
    // default: 0
    id2prio.put("SINT_X",3); // higher than simple replace
    id2prio.put("ET_AL", 1); // needs higher priority than MORFOLOGIK_RULE_NL_NL
    id2prio.put("N_PERSOONS", 1); // needs higher priority than MORFOLOGIK_RULE_NL_NL
    id2prio.put("HOOFDLETTERS_OVERBODIG_A", 1); // needs higher priority than MORFOLOGIK_RULE_NL_NL
    id2prio.put("VERSCHILLENDE_AANHALINGSTEKENS", 1); // needs higher priority than UNPAIRED_BRACKETS
    id2prio.put("STAM_ZONDER_IK", -1);  // see https://github.com/languagetool-org/languagetool/issues/7644
    id2prio.put("KOMMA_ONTBR", -1);   // see https://github.com/languagetool-org/languagetool/issues/7644
    id2prio.put("KOMMA_KOMMA", -1); // needs higher priority than DOUBLE_PUNCTUATION
    id2prio.put("HET_FIETS", -2); // first let other rules check for compound words
    id2prio.put("JIJ_JOU_JOUW", -2);  // needs higher priority than JOU_JOUW
    id2prio.put("JOU_JOUW", -3);
    id2prio.put("BE", -3); // needs lower priority than BE_GE_SPLITST
    id2prio.put("DOUBLE_PUNCTUATION", -3);
    id2prio.put("EINDE_ZIN_ONVERWACHT", -5);  //so that spelling errors are recognized first
    id2prio.put("TOO_LONG_PARAGRAPH", -15);
    id2prio.put("ERG_LANG_WOORD", -20);  // below spell checker and simple replace rule
    id2prio.put("DE_ONVERWACHT", -20);  // below spell checker and simple replace rule
    // category style : -50	  
  }

  @Override
  public Map<String, Integer> getPriorityMap() {
    return id2prio;
  }

  @Override
  protected int getPriorityForId(String id) {
    if (id.startsWith(SimpleReplaceRule.DUTCH_SIMPLE_REPLACE_RULE) || id.startsWith("NL_SPACE_IN_COMPOUND")) {
        return 1;
    }
    Integer prio = id2prio.get(id);
    if (prio != null) {
      return prio;
    }
    if (id.startsWith("AI_NL_HYDRA_LEO")) { // prefer more specific rules (also speller)
      if (id.startsWith("AI_NL_HYDRA_LEO_MISSING_COMMA")) {
        return -51; // prefer comma style rules.
      }
      return -5;
    }
    return super.getPriorityForId(id);
  }

  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    String dirBase = JLanguageTool.getDataBroker().getRulesDir() + "/" + getShortCode() + "/";
    ruleFileNames.add(dirBase + "nl-NL/grammar.xml");
    //ruleFileNames.add(dirBase + "grammar-test.xml");
    return ruleFileNames;
  }
  
  @Override
  public SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikDutchSpellerRule(messages, this, null, Collections.emptyList());
  }

  public MultitokenSpeller getMultitokenSpeller() {
    return DutchMultitokenSpeller.INSTANCE;
  }
}
