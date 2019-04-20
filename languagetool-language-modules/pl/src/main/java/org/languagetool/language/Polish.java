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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.pl.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.pl.PolishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.pl.PolishHybridDisambiguator;
import org.languagetool.tagging.pl.PolishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.pl.PolishWordTokenizer;

public class Polish extends Language {

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private PolishWordTokenizer wordTokenizer;
  private Disambiguator disambiguator;
  private Synthesizer synthesizer;

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

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new PolishTagger();
    }
    return tagger;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public WordTokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new PolishWordTokenizer();
      wordTokenizer.setTagger(getTagger());
    }
    return wordTokenizer;
  }


  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new PolishHybridDisambiguator();
    }
    return disambiguator;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new PolishSynthesizer();
    }
    return synthesizer;
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
        new CompoundRule(messages),
        new SimpleReplaceRule(messages),
        new DashRule()
        );
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

}
