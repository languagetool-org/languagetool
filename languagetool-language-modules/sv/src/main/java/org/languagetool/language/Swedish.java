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
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.rules.sv.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.sv.SwedishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.sv.SwedishHybridDisambiguator;
import org.languagetool.tagging.sv.SwedishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.io.IOException;
import java.util.*;

/**
 * @deprecated this language is unmaintained in LT and might be removed in a future release if we cannot find contributors for it (deprecated since 3.6)
 * Actively maintained since v6.2+
 */
@Deprecated
public class Swedish extends Language {

  @Override
  public String getName() {
    return "Swedish";
  }

  @Override
  public String getShortCode() {
    return "sv";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"SE", "FI"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new SwedishTagger();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new SwedishHybridDisambiguator();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return SwedishSynthesizer.INSTANCE;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("ljo@fps_gbg")};
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
            new GenericUnpairedBracketsRule(messages),
            new HunspellRule(messages, this, userConfig, altLanguages),
            // fixme! - A suitable paragraph length should be tuned automatically per text type,
            // so make sure to get the type from LO and COOL
            new LongParagraphRule(messages, this, userConfig, 150),
            new UppercaseSentenceStartRule(messages, this),
            new LongSentenceRule(messages, userConfig, 40),
            new WordRepeatRule(messages, this),
            new WordCoherencyRule(messages),
            new MultipleWhitespaceRule(messages, this),
            new SentenceWhitespaceRule(messages),
            new CompoundRule(messages, this, userConfig)
    );
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new HunspellRule(messages, this, null, null);
  }
}
