/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber & Marcin Miłkowski (http://www.languagetool.org)
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
import org.languagetool.rules.pl.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.pl.PolishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.pl.PolishHybridDisambiguator;
import org.languagetool.tagging.pl.PolishTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.pl.PolishWordTokenizer;

import java.io.IOException;
import java.util.*;

public class Polish extends Language {

  @Override
  public String getName() {
    return "Polish";
  }

  @Override
  public String getShortCode() {
    return "pl";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"PL"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new PolishTagger();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    PolishWordTokenizer wordTokenizer = new PolishWordTokenizer();
    wordTokenizer.setTagger(getTagger());
    return wordTokenizer;
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new PolishHybridDisambiguator();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return new PolishSynthesizer(this);
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { Contributors.MARCIN_MILKOWSKI };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
        new CommaWhitespaceRule(messages),
        new UppercaseSentenceStartRule(messages, this),
        new WordRepeatRule(messages, this),
        new MultipleWhitespaceRule(messages, this),
        new SentenceWhitespaceRule(messages),
        // specific to Polish:
        new PolishUnpairedBracketsRule(messages, this),
        new MorfologikPolishSpellerRule(messages, this, userConfig, altLanguages),
        new PolishWordRepeatRule(messages),
        new CompoundRule(messages, this, userConfig),
        new SimpleReplaceRule(messages),
        new WordCoherencyRule(messages),
        new DashRule(messages)
        );
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      case "ZDANIA_ZLOZONE": return -1;  //so that it does not override more important rules
    }
    return super.getPriorityForId(id);
  }
}
