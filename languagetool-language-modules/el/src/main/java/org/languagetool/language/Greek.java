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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.*;
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
  public String getShortName() {
    return "el";
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String[] getCountries() {
    return new String[]{"GR"};
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[]{
            new Contributor("Panagiotis Minos")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule("EL_UNPAIRED_BRACKETS", messages,
                    Arrays.asList("[", "(", "{", "“", "\"", "«"),
                    Arrays.asList("]", ")", "}", "”", "\"", "»")),
            new LongSentenceRule(messages),
            new MorfologikGreekSpellerRule(messages, this),
            new UppercaseSentenceStartRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new WordRepeatBeginningRule(messages, this),
            new WordRepeatRule(messages, this));
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new GreekTagger();
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
  public Tokenizer getWordTokenizer() {
    return new GreekWordTokenizer();
  }

  @Override
  public Synthesizer getSynthesizer() {
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
