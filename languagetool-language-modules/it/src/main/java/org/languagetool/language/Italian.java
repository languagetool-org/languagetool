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
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.LanguageWithModel;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.it.ItalianConfusionProbabilityRule;
import org.languagetool.rules.it.ItalianWordRepeatRule;
import org.languagetool.rules.it.MorfologikItalianSpellerRule;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.it.ItalianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.it.ItalianRuleDisambiguator;
import org.languagetool.tagging.it.ItalianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class Italian extends LanguageWithModel {

  @Override
  public String getName() {
    return "Italian";
  }

  @Override
  public String getShortCode() {
    return "it";
  }
  
  @Override
  public String[] getCountries() {
    return new String[]{"IT", "CH"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new ItalianTagger();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Contributor[] getMaintainers() {
    Contributor contributor = new Contributor("Paolo Bianchini");
    return new Contributor[] { contributor };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new WhitespaceBeforePunctuationRule(messages),
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{", "»", "«" /*"‘"*/),
                    Arrays.asList("]", ")", "}", "«", "»" /*"’"*/)),
            new MorfologikItalianSpellerRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this),
            new ItalianWordRepeatRule(messages, this),
            new MultipleWhitespaceRule(messages, this)
    );
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Collections.singletonList(
      new ItalianConfusionProbabilityRule(messages, languageModel, this)
    );
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new ItalianRuleDisambiguator();
  }
  
  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return ItalianSynthesizer.INSTANCE;
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikItalianSpellerRule(messages, this, null, null);
  }
}
