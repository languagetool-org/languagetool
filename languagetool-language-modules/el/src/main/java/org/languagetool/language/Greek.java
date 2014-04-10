/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.rules.LongSentenceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhitespaceRule;
import org.languagetool.rules.WordRepeatBeginningRule;
import org.languagetool.rules.WordRepeatRule;
import org.languagetool.rules.el.GreekUnpairedBracketsRule;
import org.languagetool.rules.el.MorfologikGreekSpellerRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.el.GreekSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.el.GreekTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.el.GreekWordTokenizer;

/**
 *
 * @author Panagiotis Minos <pminos@gmail.com>
 */
public class Greek extends Language {

  private Disambiguator disambiguator;
  private SentenceTokenizer sentenceTokenizer;
  private Synthesizer synthesizer;
  private Tagger tagger;
  private String name = "Greek";

  @Override
  public final String getShortName() {
    return "el";
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public final String[] getCountries() {
    return new String[]{"GR"};
  }

  @Override
  public final Contributor[] getMaintainers() {
    return new Contributor[]{
            new Contributor("Panagiotis Minos")
    };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GreekUnpairedBracketsRule.class,
            LongSentenceRule.class,
            MorfologikGreekSpellerRule.class,
            UppercaseSentenceStartRule.class,
            WhitespaceRule.class,
            WordRepeatBeginningRule.class,
            WordRepeatRule.class);
  }

  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new GreekTagger();
    }
    return tagger;
  }

  @Override
  public final SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public final Tokenizer getWordTokenizer() {
    return new GreekWordTokenizer();
  }

  @Override
  public final Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new GreekSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new XmlRuleDisambiguator(new Greek());
    }
    return disambiguator;
  }
}
