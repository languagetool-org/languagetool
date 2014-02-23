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

import java.util.Arrays;
import java.util.List;

import org.languagetool.Language;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.SentenceWhitespaceRule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhitespaceRule;
import org.languagetool.rules.WordRepeatRule;
import org.languagetool.rules.pl.CompoundRule;
import org.languagetool.rules.pl.MorfologikPolishSpellerRule;
import org.languagetool.rules.pl.PolishUnpairedBracketsRule;
import org.languagetool.rules.pl.PolishWordRepeatRule;
import org.languagetool.rules.pl.SimpleReplaceRule;
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

  public Polish() {
    wordTokenizer = new PolishWordTokenizer();
    wordTokenizer.setTagger(getTagger());
  }

  @Override
  public String getName() {
    return "Polish";
  }

  @Override
  public String getShortName() {
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
  public final WordTokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new PolishWordTokenizer();
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
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
        CommaWhitespaceRule.class,
        DoublePunctuationRule.class,
        UppercaseSentenceStartRule.class,
        WordRepeatRule.class,
        WhitespaceRule.class,
        SentenceWhitespaceRule.class,
        // specific to Polish:
        PolishUnpairedBracketsRule.class,
        MorfologikPolishSpellerRule.class,
        PolishWordRepeatRule.class,
        CompoundRule.class,
        SimpleReplaceRule.class
        );
  }

}
