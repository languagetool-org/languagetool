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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.nl.CompoundRule;
import org.languagetool.rules.nl.DutchWrongWordInContextRule;
import org.languagetool.rules.nl.MorfologikDutchSpellerRule;
import org.languagetool.rules.nl.SimpleReplaceRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.nl.DutchSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.nl.DutchTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.nl.DutchWordTokenizer;

public class Dutch extends Language {

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
  private Tokenizer wordTokenizer;
  private String name= "Dutch";

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public final String getShortName() {
    return "nl";
  }

  @Override
  public final String[] getCountries() {
    return new String[]{"NL", "BE"};
  }

  @Override
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "“", "\u2039", "\u201c", "\u201e" };
  }

  @Override
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "”", "\u203a", "\u201d", "\u201d" };
  }
  
  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new DutchTagger();
    }
    return tagger;
  }

  @Override
  public final Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new DutchSynthesizer();
    }
    return synthesizer;
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
    if (wordTokenizer == null) {
      wordTokenizer = new DutchWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public final Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new XmlRuleDisambiguator(new Dutch());
    }
    return disambiguator;
  }

  @Override
  public final Contributor[] getMaintainers() {
    final Contributor contributor = new Contributor("OpenTaal");
    contributor.setUrl("http://www.opentaal.org");
    return new Contributor[] { contributor };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages, this),
            new UppercaseSentenceStartRule(messages, this),
            new MorfologikDutchSpellerRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new CompoundRule(messages),
            new DutchWrongWordInContextRule(messages),
            new SimpleReplaceRule(messages)
    );
  }

}
