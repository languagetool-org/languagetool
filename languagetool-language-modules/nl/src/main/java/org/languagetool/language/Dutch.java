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
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.nl.DutchSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.nl.DutchTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.nl.DutchWordTokenizer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Dutch extends Language {

  private LanguageModel languageModel;

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
    return new DutchTagger();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return new DutchSynthesizer(this);
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
    return new XmlRuleDisambiguator(this);
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
            new CompoundRule(messages),
            new DutchWrongWordInContextRule(messages),
            new WordCoherencyRule(messages),
            new SimpleReplaceRule(messages),
            new LongSentenceRule(messages, userConfig, -1, true),
            new PreferredWordRule(messages),
            new SpaceInCompoundRule(messages),
            new SentenceWhitespaceRule(messages)
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

  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      case SimpleReplaceRule.DUTCH_SIMPLE_REPLACE_RULE: return 1;
      case LongSentenceRule.RULE_ID: return -1;
    }
    return super.getPriorityForId(id);
  }

  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    String dirBase = JLanguageTool.getDataBroker().getRulesDir() + "/" + getShortCode() + "/";
    ruleFileNames.add(dirBase + "grammar-test-1.xml");
    return ruleFileNames;
  }

}
